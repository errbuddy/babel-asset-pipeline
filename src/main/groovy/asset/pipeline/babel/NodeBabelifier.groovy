package asset.pipeline.babel

import asset.pipeline.AssetFile

class NodeBabelifier extends Babelifier {

    public NodeBabelifier() {
        if (!nodePath) {
            throw new Exception("nodePath not defined. please see README.md")
        }
        if (!browserifyPath) {
            throw new Exception("browserifyPath not defined. please see README.md")
        }
    }

    String babelify(String string, AssetFile file) {
        Process process = "$nodePath $browserifyPath ${getAbsoluteFilename(file)} -t [ babelify --presets [ es2015 react ] ]".execute()
        def out = new StringBuffer()
        def err = new StringBuffer()
        process.consumeProcessOutput(out, err)
        process.waitFor()
        String output = out.toString()
        if (process.exitValue() > 0) {
            throw new NodeBabelifierException(err.toString())
        }
        return output
    }

    private getAbsoluteFilename(AssetFile file) {
        new File(file.sourceResolver.baseDirectory, file.path).getAbsolutePath()
    }

    private static String getBrowserifyPath() {
        System.getenv('BROWSERIFY_PATH') ?: configuration.browserifyPath
    }

    private static String getNodePath() {
        System.getenv('NODE_PATH') ?: configuration.nodePath
    }

    class NodeBabelifierException extends RuntimeException {

        NodeBabelifierException(String error) {
            super(error)
        }

    }
}
