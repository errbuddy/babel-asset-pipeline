package babel.asset.pipeline

class BabelSpecTools {

    public static final String NODE_VERSION = "v5.6.0"

    static String guessNodePath() {
        String nodeHome = null
        String nodeRootFromGradle = nodeRootFromGradleDefault
        if (nodeRootFromGradle) {
            nodeHome = nodeRootFromGradle
        } else if (nodeRootFromEnv) {
            nodeHome = nodeRootFromEnv
        }

        return nodeHome ? getNodeExecutable(nodeHome) : null
    }

    private static String getNodeExecutable(String nodeHome) {
        File binDir = new File("$nodeHome/bin")
        File bin = binDir.listFiles().find { File file ->
            file.name.contains("node")
        }
        return bin?.absolutePath
    }


    private static getNodeRootFromEnv() {
        System.getenv('NODE_ROOT')
    }

    private static String getNodeRootFromGradleDefault() {
        String rootDir = new File("").absolutePath
        def nodeDir = new File("$rootDir/.gradle/nodejs")
        if (nodeDir.exists()) {
            def directories = nodeDir.listFiles()
            def dir = directories.find { File f ->
                // check that this is a directory and has the predefined version
                f.directory && f.name.contains(NODE_VERSION)
            }

            if (dir) {
                return dir.absolutePath
            }
        }
        return null
    }
}
