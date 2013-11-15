pushd %~d0%~p0\target
set version=0.7.7

call mvn deploy:deploy-file -DrepositoryId=avb-releases -Durl=dav:https://swd.noncd.rz.db.de/svn/topologiedb/dist/maven2/ -DgroupId=org.flowr -DartifactId=utils -Dversion=%version% -Dpackaging=jar  -Dfile=utils-%version%.jar 
call mvn deploy:deploy-file -DrepositoryId=avb-releases -Durl=dav:https://swd.noncd.rz.db.de/svn/topologiedb/dist/maven2/ -DgroupId=org.flowr -DartifactId=utils -Dclassifier=sources -Dversion=%version% -Dpackaging=jar  -Dfile=utils-%version%-sources.jar 

rem call mvn deploy:deploy-file -DrepositoryId=avb-snapshots -Durl=dav:https://swd.noncd.rz.db.de/svn/topologiedb/dist/maven2/ -DgroupId=de.db.avb.topodb -DartifactId=avb-data-exchange -Dclassifier=sources -Dversion=3.2-SNAPSHOT -Dpackaging=jar  -Dfile=utils-0.7.0-sources.jar 

popd