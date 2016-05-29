class Config {
	public static String oldVersion = '2.0.0'
	public static String versionPrefix = '3.2.'
	public static String gitRepo = 'https://github.com/monochromata/jactr-eclipse.git'
}

// TODO: Even this might be moved into workflowLibs, passing just a Config instance
node("1gb") {
   installToolsIfNecessary()
   withCredentials([[$class: 'FileBinding', credentialsId: 'settings.xml', variable: 'PATH_TO_SETTINGS_XML'],
   					[$class: 'FileBinding', credentialsId: 'jarsigner.keystore', variable: 'PATH_TO_JARSIGNER_KEYSTORE'],
   					[$class: 'FileBinding', credentialsId: 'upload.server.ssh.signature.file', variable: 'PATH_TO_UPLOAD_SERVER_SSH_FINGERPRINT_FILE'],
   					[$class: 'StringBinding', credentialsId: 'upload.server.name', variable: 'UPLOAD_SERVER_NAME'],
   					[$class: 'StringBinding', credentialsId: 'upload.path.products', variable: 'UPLOAD_PATH_PRODUCTS'],
   					[$class: 'UsernamePasswordMultiBinding', credentialsId: 'upload.credentials',
   						usernameVariable: 'UPLOAD_USER_NAME', passwordVariable: 'UPLOAD_PASSWORD']]) {
   					
		withEnv(["PATH+MAVEN=${tool 'mvn'}/bin", 
				 "PATH+JAVA=${tool 'jdk8'}/bin"]) {
			stage 'Checkout'
			git url: Config.gitRepo
   
			stage 'Set versions'
			// TODO: Adjust the version numbers with a Bash script, not via Maven, to speed up dramatically
			maven('''-P replace-version-in-manifest-mf-files \
					 clean com.google.code.maven-replacer-plugin:replacer:replace''')
			maven('''-P replace-version-in-xml-files \
					 com.google.code.maven-replacer-plugin:replacer:replace''')
		    maven('''--file parent/pom.xml \
				     versions:set''')
					 
			stage 'Install'
			maven('install')
			
			stage name: 'Deploy P2 repo', concurrency: 1
			sh '''touch ~/.ssh/known_hosts \
				&& ssh-keygen -f ~/.ssh/known_hosts -R $UPLOAD_SERVER_NAME \
				&& cat $PATH_TO_UPLOAD_SERVER_SSH_FINGERPRINT_FILE >> ~/.ssh/known_hosts'''
			// Retry is necessary because upload is unreliable
			retry(3) {
				maven('--file p2repo/pom.xml \
					   -Dmaven.install.skip=true \
					   -DskipTests=true \
		       		   -DskipITs=true \
	   				   deploy')
		    }
   			
   			stage name: 'Deploy product', concurrency: 1
   			// Create index.html and add version number to files
   			def newVersion = getNextVersion()
			sh """cp org.jactr.eclipse.product/index.html org.jactr.eclipse.product/target/products \
				  && cd org.jactr.eclipse.product/target/products/ \
				  && sed 's/jACT-R Eclipse for/jACT-R Eclipse ${newVersion} for/' index.html \
				  && sed 's/jactr-eclipse-/jactr-eclipse-${newVersion}-/' index.html \
				  && rename 's/jactr-eclipse-/jactr-eclipse-${newVersion}-/' *"""
			// Retry is necessary because upload is unreliable
   			retry(3) {
   				sh '''sshpass -p $UPLOAD_PASSWORD scp ./jactr-eclipse-* $UPLOAD_USER_NAME@$UPLOAD_SERVER_NAME:/$UPLOAD_PATH_PRODUCTS/org.jactr.eclipse/ \
   					  && sshpass -p $UPLOAD_PASSWORD scp ./index.html $UPLOAD_USER_NAME@$UPLOAD_SERVER_NAME:/$UPLOAD_PATH_PRODUCTS/org.jactr.eclipse/'''
   			}
			
			stage name: 'Site deploy', concurrency: 1
			// Retry is necessary because upload is unreliable
			retry(3) {
	       		maven('''-DskipTests=true \
	       				 -DskipITs=true \
	       				 site-deploy''')
			}
		}
	}
}

// TODO: Move to workflowLibs
def maven(String optionsAndGoals) {
   def newVersion=getNextVersion()
   sh '''mvn \
   		 -Djarsigner.keystore.path=$PATH_TO_JARSIGNER_KEYSTORE \
         --errors \
         --settings $PATH_TO_SETTINGS_XML \
         -DoldVersion='''+Config.oldVersion+''' \
         -DnewVersion='''+newVersion+''' \
         '''+optionsAndGoals
}

// TODO: Move to workflowLibs
// TODO: Auto-assign version numbers via an algorithm that
//		 * does not yield un-deployed versions for failed builds
// 		 * permits major and minor numbers to be incremented via tags in the commit message
//		 * starts with a patch number of 0 if the minor was incremented (same for minor if major was incremented)
//       ! Note that from then on (including "Set versions" stage, concurrency 1
//		   will be required.
//       ! Note that multiple invocations of this method must return the same value
def getNextVersion() {
	return Config.versionPrefix+env.BUILD_NUMBER
}

// TODO: Move to workflowLibs
def installToolsIfNecessary() {
   sh '''echo "deb http://http.debian.net/debian jessie-backports main" > /etc/apt/sources.list.d/jessie-backports.list \
        && apt-get update \
        && apt-get remove --yes openjdk-7-jdk \
        && apt-get install --yes openjdk-8-jre-headless openjdk-8-jdk git maven sshpass\
        && /usr/sbin/update-java-alternatives -s java-1.8.0-openjdk-amd64 '''
}