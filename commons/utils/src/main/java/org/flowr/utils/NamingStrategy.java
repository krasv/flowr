/*******************************************************************************
 * Copyright (c) 2008 flowr.org - all rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License (EPL) v1.0. The EPL is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: flowr.org - initial API and implementation
 ******************************************************************************/
package org.flowr.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * helper class creating java and emf conform names
 *
 * @author krausesv
 *
 */
@SuppressWarnings("nls")
public abstract class NamingStrategy {

	private static final String CLASSNAME_IGNORE_CHARS = ":!%. -/()%&�\",";
	private static final String PACKAGENAME_IGNORE_CHARS = CLASSNAME_IGNORE_CHARS + "_";
	private static final String QUALIFIED_PACKAGENAME_IGNORE_CHARS = ":!% -/()%&�\"," + "_";

	private final static String[][] REPLACEMENTS = new String[][] { { "�", "ae" }, { "�", "Ae" }, { "�", "oe" }, { "�", "Oe" }, { "�", "ue" }, { "�", "Ue" }, { "�", "sz" }, };

	/**
	 * builds a string usable as valid java class name. The name elements will be "camel cased".
	 *
	 * @param names
	 *        the name element parts.
	 * @return
	 */
	public static String className(String... names) {
		StringBuilder b = new StringBuilder();
		for (String name : names) {
			b.append(camelCase(name, CLASSNAME_IGNORE_CHARS, null));
		}
		return makeValidIdentifier(b.toString());
	}

	/**
	 * builds an abbreviation based on the given name parts using the "camel cased" words first letter.
	 *
	 * @param names
	 *        the name element parts.
	 * @return
	 */
	public static String initials(String... names) {
		StringBuilder b = new StringBuilder();
		for (String name : names) {
			b.append(camelCase(name, CLASSNAME_IGNORE_CHARS, null));
		}
		String[] words = StringUtilsExt.splitByCharacterTypeCamelCase(b.toString());
		b = new StringBuilder();
		for (String word : words) {
			char c = word.charAt(0);
			switch (Character.getType(c)) {
				case Character.UPPERCASE_LETTER:
					if (word.length() > 1 && Character.getType(word.charAt(1)) == Character.UPPERCASE_LETTER) {
						b.append(word);
					} else {
						b.append(c);
					}
					break;
				case Character.LOWERCASE_LETTER:
					b.append(c);
					break;
			}
		}
		return b.toString();
	}

	/**
	 * builds a string usable as java package name part.
	 *
	 * @param names
	 * @return
	 */
	public static String packageName(String... names) {
		StringBuilder b = new StringBuilder();
		for (String name : names) {
			b.append(camelCase(name, PACKAGENAME_IGNORE_CHARS, null));
		}
		return makeValidIdentifier(b.toString()).toLowerCase();
	}

	/**
	 * builds a string usable as java package name part.
	 *
	 * @param names
	 * @return
	 */
	public static String qualifiedPackageName(String... names) {
		StringBuilder b = new StringBuilder();
		for (String name : names) {
			if (b.length() > 0) b.append("."); //$NON-NLS-1$
			b.append(camelCase(name, QUALIFIED_PACKAGENAME_IGNORE_CHARS, null));
		}
		return makePackagePart(b.toString()).toLowerCase();
	}

	/**
	 * builds a string usable as java member name.
	 *
	 * @param names
	 * @return
	 */
	public static String propertyName(String... names) {
		return StringUtils.uncapitalize(className(names));
	}

	public static String propertyAccessor(String... names) {
		return "get" + StringUtils.capitalize(className(names)); //$NON-NLS-1$
	}

	private static String camelCase(String name, String separatorChars, String space) {
		String[] tokens = StringUtils.splitPreserveAllTokens(name, separatorChars, 0);
		StringBuilder b = new StringBuilder();
		for (String word : tokens) {
			if (space != null && b.length() > 0) {
				b.append(space);
			}
			b.append(StringUtils.capitalize(word));
		}
		return b.toString();
	}

	private static String replaceEach(String text, String[]... replace) {
		for (String[] strings : replace) {
			text = StringUtilsExt.replaceEach(text, new String[] { strings[0] }, new String[] { strings[1] });
		}
		return text;
	}

	private static String makeValidIdentifier(String name) {
		if (!javax.lang.model.SourceVersion.isIdentifier(name)) {
			name = replaceEach(name, REPLACEMENTS);
			if (!javax.lang.model.SourceVersion.isIdentifier(name)) {
				char c = name.charAt(0);
				if (Character.isDigit(c)) {
					name = "A" + name;
				} else {
					name = name + "1";
				}
			}
		}
		return name;
	}

	private static String makePackagePart(String name) {
		if (!javax.lang.model.SourceVersion.isName(name)) {
			name = replaceEach(name, REPLACEMENTS);
			if (!javax.lang.model.SourceVersion.isName(name)) {
				char c = name.charAt(0);
				if (Character.isDigit(c)) {
					name = "A" + name;
				} else {
					name = name + "1";
				}
			}
		}
		return name;
	}

	public static void main(String[] args) {
		System.out.println(replaceEach("��", new String[] { "�", "ae" }, new String[] { "�", "oe" }));
		System.out.println(StringUtilsExt.replaceEach("��", new String[] { "�", "�" }, new String[] { "ae", "oe" }));

		System.out.println(qualifiedPackageName("de.db.systel", "Diagram0815", "Detailierte Beschreibung"));
		System.out.println(camelCase("Deatilierte Beschreibung", PACKAGENAME_IGNORE_CHARS, null));
		System.out.println(javax.lang.model.SourceVersion.isName("de.db"));
		System.out.println(javax.lang.model.SourceVersion.isIdentifier("de.db"));
		System.out.println(propertyName("das", "ist", "ein", "test"));
		System.out.println(propertyAccessor("das", "ist", "ein", "test"));
	}

	private static class StringUtilsExt extends StringUtils {

		/**
		 * <p>
		 * Method added from apache.commons.lang.StringUtils (2.4).
		 * </p>
		 * <p>
		 * Splits a String by Character type as returned by <code>java.lang.Character.getType(char)</code>. Groups of contiguous characters of the same type are returned as complete tokens, with the
		 * following exception: the character of type <code>Character.UPPERCASE_LETTER</code>, if any, immediately preceding a token of type <code>Character.LOWERCASE_LETTER</code> will belong to the
		 * following token rather than to the preceding, if any, <code>Character.UPPERCASE_LETTER</code> token.
		 *
		 * <pre>
		 * StringUtils.splitByCharacterTypeCamelCase(null)         = null
		 * StringUtils.splitByCharacterTypeCamelCase(&quot;&quot;)           = []
		 * StringUtils.splitByCharacterTypeCamelCase(&quot;ab de fg&quot;)   = [&quot;ab&quot;, &quot; &quot;, &quot;de&quot;, &quot; &quot;, &quot;fg&quot;]
		 * StringUtils.splitByCharacterTypeCamelCase(&quot;ab   de fg&quot;) = [&quot;ab&quot;, &quot;   &quot;, &quot;de&quot;, &quot; &quot;, &quot;fg&quot;]
		 * StringUtils.splitByCharacterTypeCamelCase(&quot;ab:cd:ef&quot;)   = [&quot;ab&quot;, &quot;:&quot;, &quot;cd&quot;, &quot;:&quot;, &quot;ef&quot;]
		 * StringUtils.splitByCharacterTypeCamelCase(&quot;number5&quot;)    = [&quot;number&quot;, &quot;5&quot;]
		 * StringUtils.splitByCharacterTypeCamelCase(&quot;fooBar&quot;)     = [&quot;foo&quot;, &quot;Bar&quot;]
		 * StringUtils.splitByCharacterTypeCamelCase(&quot;foo200Bar&quot;)  = [&quot;foo&quot;, &quot;200&quot;, &quot;Bar&quot;]
		 * StringUtils.splitByCharacterTypeCamelCase(&quot;ASFRules&quot;)   = [&quot;ASF&quot;, &quot;Rules&quot;]
		 * </pre>
		 *
		 * @param str
		 *        the String to split, may be <code>null</code>
		 * @return an array of parsed Strings, <code>null</code> if null String input
		 * @since 2.4
		 */
		public static String[] splitByCharacterTypeCamelCase(String str) {
			return splitByCharacterType(str, true);
		}

		/**
		 * <p>
		 * Method added from apache.commons.lang.StringUtils (2.4).
		 * </p>
		 * <p>
		 * Splits a String by Character type as returned by <code>java.lang.Character.getType(char)</code>. Groups of contiguous characters of the same type are returned as complete tokens, with the
		 * following exception: if <code>camelCase</code> is <code>true</code>, the character of type <code>Character.UPPERCASE_LETTER</code>, if any, immediately preceding a token of type
		 * <code>Character.LOWERCASE_LETTER</code> will belong to the following token rather than to the preceding, if any, <code>Character.UPPERCASE_LETTER</code> token.
		 *
		 * @param str
		 *        the String to split, may be <code>null</code>
		 * @param camelCase
		 *        whether to use so-called "camel-case" for letter types
		 * @return an array of parsed Strings, <code>null</code> if null String input
		 * @since 2.4
		 */
		@SuppressWarnings("unchecked")
		private static String[] splitByCharacterType(String str, boolean camelCase) {
			if (str == null) { return null; }
			if (str.length() == 0) { return ArrayUtils.EMPTY_STRING_ARRAY; }
			char[] c = str.toCharArray();
			@SuppressWarnings("rawtypes")
			List list = new ArrayList();
			int tokenStart = 0;
			int currentType = Character.getType(c[tokenStart]);
			for (int pos = tokenStart + 1; pos < c.length; pos++) {
				int type = Character.getType(c[pos]);
				if (type == currentType) {
					continue;
				}
				if (camelCase && type == Character.LOWERCASE_LETTER && currentType == Character.UPPERCASE_LETTER) {
					int newTokenStart = pos - 1;
					if (newTokenStart != tokenStart) {
						list.add(new String(c, tokenStart, newTokenStart - tokenStart));
						tokenStart = newTokenStart;
					}
				} else {
					list.add(new String(c, tokenStart, pos - tokenStart));
					tokenStart = pos;
				}
				currentType = type;
			}
			list.add(new String(c, tokenStart, c.length - tokenStart));
			return (String[]) list.toArray(new String[list.size()]);
		}

		/**
		 * <p>
		 * Method added from apache.commons.lang.StringUtils (2.4).
		 * </p>
		 * <p>
		 * Replaces all occurrences of Strings within another String.
		 * </p>
		 *
		 * <p>
		 * A <code>null</code> reference passed to this method is a no-op, or if any "search string" or "string to replace" is null, that replace will be ignored. This will not repeat. For repeating
		 * replaces, call the overloaded method.
		 * </p>
		 *
		 * <pre>
		 *  StringUtils.replaceEach(null, *, *)        = null
		 *  StringUtils.replaceEach(&quot;&quot;, *, *)          = &quot;&quot;
		 *  StringUtils.replaceEach(&quot;aba&quot;, null, null) = &quot;aba&quot;
		 *  StringUtils.replaceEach(&quot;aba&quot;, new String[0], null) = &quot;aba&quot;
		 *  StringUtils.replaceEach(&quot;aba&quot;, null, new String[0]) = &quot;aba&quot;
		 *  StringUtils.replaceEach(&quot;aba&quot;, new String[]{&quot;a&quot;}, null)  = &quot;aba&quot;
		 *  StringUtils.replaceEach(&quot;aba&quot;, new String[]{&quot;a&quot;}, new String[]{&quot;&quot;})  = &quot;b&quot;
		 *  StringUtils.replaceEach(&quot;aba&quot;, new String[]{null}, new String[]{&quot;a&quot;})  = &quot;aba&quot;
		 *  StringUtils.replaceEach(&quot;abcde&quot;, new String[]{&quot;ab&quot;, &quot;d&quot;}, new String[]{&quot;w&quot;, &quot;t&quot;})  = &quot;wcte&quot;
		 *  (example of how it does not repeat)
		 *  StringUtils.replaceEach(&quot;abcde&quot;, new String[]{&quot;ab&quot;, &quot;d&quot;}, new String[]{&quot;d&quot;, &quot;t&quot;})  = &quot;dcte&quot;
		 * </pre>
		 *
		 * @param text
		 *        text to search and replace in, no-op if null
		 * @param searchList
		 *        the Strings to search for, no-op if null
		 * @param replacementList
		 *        the Strings to replace them with, no-op if null
		 * @return the text with any replacements processed, <code>null</code> if null String input
		 * @throws IndexOutOfBoundsException
		 *         if the lengths of the arrays are not the same (null is ok, and/or size 0)
		 * @since 2.4
		 */
		public static String replaceEach(String text, String[] searchList, String[] replacementList) {
			return replaceEach(text, searchList, replacementList, false, 0);
		}

		/**
		 * <p>
		 * Method added from apache.commons.lang.StringUtils (2.4).
		 * </p>
		 * <p>
		 * Replaces all occurrences of Strings within another String.
		 * </p>
		 *
		 * <p>
		 * A <code>null</code> reference passed to this method is a no-op, or if any "search string" or "string to replace" is null, that replace will be ignored.
		 * </p>
		 *
		 * <pre>
		 *  StringUtils.replaceEach(null, *, *, *) = null
		 *  StringUtils.replaceEach(&quot;&quot;, *, *, *) = &quot;&quot;
		 *  StringUtils.replaceEach(&quot;aba&quot;, null, null, *) = &quot;aba&quot;
		 *  StringUtils.replaceEach(&quot;aba&quot;, new String[0], null, *) = &quot;aba&quot;
		 *  StringUtils.replaceEach(&quot;aba&quot;, null, new String[0], *) = &quot;aba&quot;
		 *  StringUtils.replaceEach(&quot;aba&quot;, new String[]{&quot;a&quot;}, null, *) = &quot;aba&quot;
		 *  StringUtils.replaceEach(&quot;aba&quot;, new String[]{&quot;a&quot;}, new String[]{&quot;&quot;}, *) = &quot;b&quot;
		 *  StringUtils.replaceEach(&quot;aba&quot;, new String[]{null}, new String[]{&quot;a&quot;}, *) = &quot;aba&quot;
		 *  StringUtils.replaceEach(&quot;abcde&quot;, new String[]{&quot;ab&quot;, &quot;d&quot;}, new String[]{&quot;w&quot;, &quot;t&quot;}, *) = &quot;wcte&quot;
		 *  (example of how it repeats)
		 *  StringUtils.replaceEach(&quot;abcde&quot;, new String[]{&quot;ab&quot;, &quot;d&quot;}, new String[]{&quot;d&quot;, &quot;t&quot;}, false) = &quot;dcte&quot;
		 *  StringUtils.replaceEach(&quot;abcde&quot;, new String[]{&quot;ab&quot;, &quot;d&quot;}, new String[]{&quot;d&quot;, &quot;t&quot;}, true) = &quot;tcte&quot;
		 *  StringUtils.replaceEach(&quot;abcde&quot;, new String[]{&quot;ab&quot;, &quot;d&quot;}, new String[]{&quot;d&quot;, &quot;ab&quot;}, *) = IllegalArgumentException
		 * </pre>
		 *
		 * @param text
		 *        text to search and replace in, no-op if null
		 * @param searchList
		 *        the Strings to search for, no-op if null
		 * @param replacementList
		 *        the Strings to replace them with, no-op if null
		 * @param repeat
		 *        if true, then replace repeatedly until there are no more possible replacements or timeToLive < 0
		 * @param timeToLive
		 *        if less than 0 then there is a circular reference and endless loop
		 * @return the text with any replacements processed, <code>null</code> if null String input
		 * @throws IllegalArgumentException
		 *         if the search is repeating and there is an endless loop due to outputs of one being inputs to another
		 * @throws IndexOutOfBoundsException
		 *         if the lengths of the arrays are not the same (null is ok, and/or size 0)
		 * @since 2.4
		 */
		private static String replaceEach(String text, String[] searchList, String[] replacementList, boolean repeat, int timeToLive) {

			// mchyzer Performance note: This creates very few new objects (one major goal)
			// let me know if there are performance requests, we can create a harness to measure

			if (text == null || text.length() == 0 || searchList == null || searchList.length == 0 || replacementList == null || replacementList.length == 0) { return text; }

			// if recursing, this shouldnt be less than 0
			if (timeToLive < 0) { throw new IllegalStateException("TimeToLive of " + timeToLive + " is less than 0: " + text); }

			int searchLength = searchList.length;
			int replacementLength = replacementList.length;

			// make sure lengths are ok, these need to be equal
			if (searchLength != replacementLength) { throw new IllegalArgumentException("Search and Replace array lengths don't match: " + searchLength + " vs " + replacementLength); }

			// keep track of which still have matches
			boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];

			// index on index that the match was found
			int textIndex = -1;
			int replaceIndex = -1;
			int tempIndex = -1;

			// index of replace array that will replace the search string found
			// NOTE: logic duplicated below START
			for (int i = 0; i < searchLength; i++) {
				if (noMoreMatchesForReplIndex[i] || searchList[i] == null || searchList[i].length() == 0 || replacementList[i] == null) {
					continue;
				}
				tempIndex = text.indexOf(searchList[i]);

				// see if we need to keep searching for this
				if (tempIndex == -1) {
					noMoreMatchesForReplIndex[i] = true;
				} else {
					if (textIndex == -1 || tempIndex < textIndex) {
						textIndex = tempIndex;
						replaceIndex = i;
					}
				}
			}
			// NOTE: logic mostly below END

			// no search strings found, we are done
			if (textIndex == -1) { return text; }

			int start = 0;

			// get a good guess on the size of the result buffer so it doesnt have to double if it goes over a bit
			int increase = 0;

			// count the replacement text elements that are larger than their corresponding text being replaced
			for (int i = 0; i < searchList.length; i++) {
				int greater = replacementList[i].length() - searchList[i].length();
				if (greater > 0) {
					increase += 3 * greater; // assume 3 matches
				}
			}
			// have upper-bound at 20% increase, then let Java take over
			increase = Math.min(increase, text.length() / 5);

			StringBuffer buf = new StringBuffer(text.length() + increase);

			while (textIndex != -1) {

				for (int i = start; i < textIndex; i++) {
					buf.append(text.charAt(i));
				}
				buf.append(replacementList[replaceIndex]);

				start = textIndex + searchList[replaceIndex].length();

				textIndex = -1;
				replaceIndex = -1;
				tempIndex = -1;
				// find the next earliest match
				// NOTE: logic mostly duplicated above START
				for (int i = 0; i < searchLength; i++) {
					if (noMoreMatchesForReplIndex[i] || searchList[i] == null || searchList[i].length() == 0 || replacementList[i] == null) {
						continue;
					}
					tempIndex = text.indexOf(searchList[i], start);

					// see if we need to keep searching for this
					if (tempIndex == -1) {
						noMoreMatchesForReplIndex[i] = true;
					} else {
						if (textIndex == -1 || tempIndex < textIndex) {
							textIndex = tempIndex;
							replaceIndex = i;
						}
					}
				}
				// NOTE: logic duplicated above END

			}
			int textLength = text.length();
			for (int i = start; i < textLength; i++) {
				buf.append(text.charAt(i));
			}
			String result = buf.toString();
			if (!repeat) { return result; }

			return replaceEach(result, searchList, replacementList, repeat, timeToLive - 1);
		}
	}

}
