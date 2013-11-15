/**
 * DB Systel GmbH / i.S.A. Dresden GmbH & Co. KG
 * (c) 2010
 */
package org.flowr.ant.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Manifest;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.ant.types.FileSet;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ContentFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * @author SvenKrause
 *
 */
public class IncludeFeatureTask extends Task {

    private List<FileSet> fileSets = new ArrayList<FileSet>();
    private String target;
    private String pluginsDir;
    private File pluginsBaseDir;

    /**
     * @param target
     *            the target to set
     */
    public void setTarget(String target) {
        this.target = target;
        log("target = " + target, Project.MSG_VERBOSE);
    }

    public void setPluginsDir(String pluginsDir) {
        this.pluginsDir = pluginsDir;
        log("pluginsDir = " + pluginsDir, Project.MSG_VERBOSE);
    }

    public void addFileSet(FileSet fileSet) {
        fileSets.add(fileSet);
    }

    @Override
    public void execute() throws BuildException {
        if (target == null)
            throw new BuildException("target is not set");
        if (pluginsDir == null)
            throw new BuildException("pluginsDir is not set");
        pluginsBaseDir = new File(pluginsDir);
        File file = new File(target);
        if (!file.exists())
            throw new BuildException("can not read find file '"
                    + file.getAbsolutePath() + "'");
        if (!file.canWrite())
            throw new BuildException("can not write file '"
                    + file.getAbsolutePath() + "'");

        try {
            FeatureHandler featureHandler = new FeatureHandler(file);

            for (FileSet fs : fileSets) {
                File dir = fs.getDir();
                DirectoryScanner ds = fs.getDirectoryScanner();
                String[] includedFiles = ds.getIncludedFiles();
                for (String f : includedFiles) {
                    log("include: " + f);
                    File ff = new File(dir, f);
                    if (!ff.canRead())
                        throw new BuildException(
                                "can not import feature file '"
                                        + ff.getAbsolutePath() + "'");
                    FeatureHandler fh = new FeatureHandler(ff);
                    featureHandler.addFeature(fh);
                }
            }

            featureHandler.save();
        } catch (Exception e) {
            throw new BuildException("can update feature file '"
                    + file.getAbsolutePath() + "'", e);
        }
    }

    private static class PluginHandler {
        private Manifest manifest;

        static PluginHandler locate(File baseDir, final String pluginId) throws FileNotFoundException, IOException {
            File[] pluginFiles = baseDir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    File file = new File(dir, name);
                    if(file.exists() && file.isDirectory()) {
                    	File manifestFile = new File(new File(file, "META-INF"), "MANIFEST.MF");
                    	try {
							Manifest manifest = new Manifest(new FileInputStream(manifestFile));
							String symbolicName = manifest.getMainAttributes().getValue(Constants.BUNDLE_SYMBOLICNAME);
							if(symbolicName.indexOf(';') > 0) {
								symbolicName = symbolicName.substring(0, symbolicName.indexOf(';'));
							}
							if(pluginId.equals(symbolicName)) {
								return true;
							}
						} catch (Exception e) {
							// no manifest or can not read
							return false;
						}
                    }
                    return false;
                }
            });
            if (pluginFiles != null && pluginFiles.length == 1) {
                File pluginDir = pluginFiles[0];
                File manifestFile = new File(new File(pluginDir, "META-INF"), "MANIFEST.MF");
                if(manifestFile.exists() && manifestFile.canRead()) {
                    return new PluginHandler(manifestFile);
                }
            }
            return null;
        }

        private PluginHandler(File manifestFile) throws FileNotFoundException, IOException {
            manifest = new Manifest(new FileInputStream(manifestFile));
        }

        public Version getBundleVersion() {
            String versionString = manifest.getMainAttributes().getValue(Constants.BUNDLE_VERSION);
            return versionString != null ? Version.parseVersion(versionString) : null;
        }
    }

    private class FeatureHandler {
        private File featureFile;
        private FeatureModel model = new FeatureModel();
        private Document document;

        /**
         * @param featureFile
         * @throws IOException
         * @throws JDOMException
         * @throws FileNotFoundException
         */
        public FeatureHandler(File featureFile) throws FileNotFoundException,
                JDOMException, IOException {
            this.featureFile = featureFile;
            load();
        }

        private void load() throws FileNotFoundException, JDOMException,
                IOException {
            SAXBuilder builder = new SAXBuilder();
            document = builder.build(new FileInputStream(featureFile));
            Element root = document.getRootElement();
            model.load(root);
        }

        public void save() throws IOException {
            for (Feature feature : model.includedFeatures) {
                log(feature.toString(), Project.MSG_VERBOSE);
            }
            for (Plugin p : model.includedPlugins) {
                log(p.toString(), Project.MSG_VERBOSE);
            }
            for (Import im : model.requiredPlugins) {
                log(im.toString(), Project.MSG_VERBOSE);
            }
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            FileWriter fileWriter = new FileWriter(featureFile);
            outputter.output(document, fileWriter);
            fileWriter.flush();
            fileWriter.close();
        }

        /**
         * @param fh
         * @throws IOException
         * @throws ManifestException
         * @throws FileNotFoundException
         */
        public void addFeature(FeatureHandler fh) throws FileNotFoundException, IOException {
            for (Plugin plugin : fh.model.includedPlugins) {
                if (plugin.version != null
                        && !(Version.emptyVersion.equals(plugin.version))) {
                    model.addRequiredPlugin(plugin.id, plugin.version,
                            Match.greaterOrEqual);
                } else {
                    PluginHandler pluginHandler = PluginHandler.locate(pluginsBaseDir, plugin.id);
                    Version bundleVersion = pluginHandler != null ? pluginHandler.getBundleVersion() : null;
                    if(bundleVersion != null) {
                    	model.addRequiredPlugin(plugin.id, new Version(bundleVersion.getMajor(), bundleVersion.getMinor(), bundleVersion.getMicro()), Match.greaterOrEqual);
                    } else {
                    	model.addRequiredPlugin(plugin.id);
                    }
                }
            }
            model.addFeature(fh.model.id, Version.emptyVersion);
        }

    }

    private static class Feature {
        private static final String ATT_ID = "id";
        private static final String ATT_VERSION = "version";
        private String id;
        private Version version;

        static Feature create(Element element) {
            Feature f = new Feature();
            f.id = element.getAttributeValue(ATT_ID);
            f.version = element.getAttributeValue(ATT_VERSION) != null ? new Version(
                    element.getAttributeValue(ATT_VERSION))
                    : null;
            return f;
        }

        void write(Element element) {
            element.setAttribute(ATT_ID, id);
            if (version != null)
                element.setAttribute(ATT_VERSION, version.toString());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result
                    + ((version == null) ? 0 : version.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Feature other = (Feature) obj;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            if (version == null) {
                if (other.version != null)
                    return false;
            } else if (!version.equals(other.version))
                return false;
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Feature [id=");
            builder.append(id);
            builder.append(", version=");
            builder.append(version);
            builder.append("]");
            return builder.toString();
        }

    }

    /**
     * represents an included plugin.
     *
     * @author SvenKrause
     *
     */
    private static class Plugin {

        private static final String ATT_ID = "id";
        private static final String ATT_DOWNLOAD_SIZE = "download-size";
        private static final String ATT_INSTALL_SIZE = "install-size";
        private static final String ATT_VERSION = "version";
        private static final String ATT_UNPACK = "unpack";

        private String id;
        private long downloadSize;
        private long installSize;
        private Version version;
        private Boolean unpack;

        /**
         * constructs an instance based on the given xml data.
         *
         * @param element
         * @return
         */
        private static Plugin create(Element element) {
            Plugin p = new Plugin();
            p.id = element.getAttributeValue(ATT_ID);
            p.downloadSize = element.getAttributeValue(ATT_DOWNLOAD_SIZE) != null ? Long
                    .parseLong(element.getAttributeValue(ATT_DOWNLOAD_SIZE))
                    : 0;
            p.installSize = element.getAttributeValue(ATT_INSTALL_SIZE) != null ? Long
                    .parseLong(element.getAttributeValue(ATT_INSTALL_SIZE))
                    : 0;
            p.version = element.getAttributeValue(ATT_VERSION) != null ? new Version(
                    element.getAttributeValue(ATT_VERSION))
                    : null;
            p.unpack = element.getAttributeValue(ATT_UNPACK) != null ? Boolean
                    .valueOf(element.getAttributeValue(ATT_UNPACK)) : null;
            return p;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + (int) (downloadSize ^ (downloadSize >>> 32));
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result
                    + (int) (installSize ^ (installSize >>> 32));
            result = prime * result
                    + ((unpack == null) ? 0 : unpack.hashCode());
            result = prime * result
                    + ((version == null) ? 0 : version.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Plugin other = (Plugin) obj;
            if (downloadSize != other.downloadSize)
                return false;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            if (installSize != other.installSize)
                return false;
            if (unpack == null) {
                if (other.unpack != null)
                    return false;
            } else if (!unpack.equals(other.unpack))
                return false;
            if (version == null) {
                if (other.version != null)
                    return false;
            } else if (!version.equals(other.version))
                return false;
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Plugin [id=");
            builder.append(id);
            builder.append(", version=");
            builder.append(version);
            builder.append("]");
            return builder.toString();
        }

    }

    /**
     * represents a required plugin.
     *
     * @author SvenKrause
     *
     */
    private static class Import {

        private static final String ATT_PLUGIN = "plugin";
        private static final String ATT_VERSION = "version";
        private static final String ATT_MATCH = "match";
        private String plugin;
        private Version version;
        private Match match;

        /**
         * constructs an instance based on the given xml data.
         *
         * @param eImport
         * @return
         */
        private static Import create(Element eImport) {
            Import i = new Import();
            i.plugin = eImport.getAttributeValue(ATT_PLUGIN);
            i.version = eImport.getAttributeValue(ATT_VERSION) != null ? new Version(
                    eImport.getAttributeValue(ATT_VERSION))
                    : null;
            if (i.version != null
                    && eImport.getAttributeValue(ATT_MATCH) != null) {
                i.match = Match.valueOf(eImport.getAttributeValue(ATT_MATCH));
            }
            return i;
        }

        private Import() {
        }

        protected Import(String plugin, Version version, Match match) {
            this();
            this.plugin = plugin;
            this.version = version;
            this.match = match;
        }

        void write(Element element) {
            element.setAttribute(ATT_PLUGIN, plugin);
            if (version != null)
                element.setAttribute(ATT_VERSION, version.toString());
            if (match != null)
                element.setAttribute(ATT_MATCH, match.name());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((match == null) ? 0 : match.hashCode());
            result = prime * result
                    + ((plugin == null) ? 0 : plugin.hashCode());
            result = prime * result
                    + ((version == null) ? 0 : version.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Import other = (Import) obj;
            if (match == null) {
                if (other.match != null)
                    return false;
            } else if (!match.equals(other.match))
                return false;
            if (plugin == null) {
                if (other.plugin != null)
                    return false;
            } else if (!plugin.equals(other.plugin))
                return false;
            if (version == null) {
                if (other.version != null)
                    return false;
            } else if (!version.equals(other.version))
                return false;
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Import [plugin=");
            builder.append(plugin);
            builder.append(", version=");
            builder.append(version);
            builder.append(", match=");
            builder.append(match);
            builder.append("]");
            return builder.toString();
        }

    }

    /**
     * helper class for defining version matching rules.
     *
     * @author SvenKrause
     *
     */
    private static enum Match {
        greaterOrEqual, perfect, equivalent, compatible;
    }

    /**
     * helper class encapsulates the feature data.
     *
     * @author SvenKrause
     *
     */
    static class FeatureModel {

        private static final String ATT_ID = "id";

        private static final String ELEMENT_FEATURE = "feature";
        private static final String ELEMENT_PLUGIN = "plugin";
        private static final String ELEMENT_REQUIRES = "requires";
        private static final String ELEMENT_IMPORT = "import";
        private static final String ELEMENT_INCLUDES = "includes";

        private String id;
        private Set<Plugin> includedPlugins = new LinkedHashSet<Plugin>();
        private Set<Import> requiredPlugins = new LinkedHashSet<Import>();
        private Set<Feature> includedFeatures = new LinkedHashSet<Feature>();
        private Element root;
        private Element requiresElement;

        public FeatureModel() {
        }

        /**
         * loads the feature data from the feature xml node.
         *
         * @param root
         */
        void load(Element root) {
            this.root = root;
            if (ELEMENT_FEATURE.equals(root.getName())) {
                id = root.getAttributeValue(ATT_ID);
                for (Iterator<?> childIterator = root.getContent(
                        new ContentFilter(ContentFilter.ELEMENT)).iterator(); childIterator
                        .hasNext();) {
                    Element child = (Element) childIterator.next();
                    if (ELEMENT_PLUGIN.equals(child.getName())) {
                        includedPlugins.add(Plugin.create(child));
                    } else if (ELEMENT_REQUIRES.equals(child.getName())) {
                        this.requiresElement = child;
                        for (Iterator<?> importIterator = child.getContent(
                                new ContentFilter(ContentFilter.ELEMENT))
                                .iterator(); importIterator.hasNext();) {
                            Element eImport = (Element) importIterator.next();
                            if (ELEMENT_IMPORT.equals(eImport.getName())) {
                                requiredPlugins.add(Import.create(eImport));
                            }
                        }
                    } else if (ELEMENT_INCLUDES.equals(child.getName())) {
                        includedFeatures.add(Feature.create(child));
                    }
                }
            }
        }

        public void addFeature(String id, Version version) {
            Feature f = new Feature();
            f.id = id;
            f.version = version != null ? version : new Version("0.0.0");
            Element element = new Element(ELEMENT_INCLUDES);
            f.write(element);
            boolean added = includedFeatures.add(f);
            if (added) {
                int idx = lastIndexOf(root, ELEMENT_INCLUDES, "licence",
                        "copyright", "description");
                if (idx != -1) {
                    if (idx != -1) {
                        root.addContent(idx + 1, element);
                    } else {
                        root.addContent(0, element);
                    }
                }
            }
        }

        /**
         * adds a required plugin without a version specification.
         *
         * @param id
         */
        public void addRequiredPlugin(String id) {
            addRequiredPlugin(id, null, null);
        }

        /**
         * adds a required plugin with dedicated required version.
         *
         * @param id
         * @param range
         * @param match
         */
        public void addRequiredPlugin(String id, Version range, Match match) {
            Element element = new Element(ELEMENT_IMPORT);
            Import imp = new Import(id, range, match);
            imp.write(element);
            boolean added = requiredPlugins.add(imp);
            if (added) {
            	System.out.println("adding required plugin : "  + imp);
                if (requiresElement == null) {
                    requiresElement = new Element(ELEMENT_REQUIRES);

                    int idx = lastIndexOf(root, ELEMENT_INCLUDES, "licence",
                            "copyright", "description");
                    if (idx != -1) {
                        root.addContent(idx + 1, requiresElement);
                    } else {
                        root.addContent(0, requiresElement);
                    }
                }
                requiresElement.addContent(element);
            }
        }

    }

    private static int lastIndexOf(Element e, String... names) {
        int idx = -1;
        for (String name : names) {
            @SuppressWarnings("unchecked")
            List<Element> children = e.getChildren(name);
            if (children != null) {
                for (Element child : children) {
                    idx = Math.max(idx, e.indexOf(child));
                }
            }
        }
        return idx;
    }
}
