package babel.asset.pipeline

class BabelSpecTools {

    public static final String NODE_VERSION = "v8.2.1"

    static String guessNodePath() {
        return "$nodeRootFromGradleDefault/bin/node"
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
