package asset.pipeline.babel

import asset.pipeline.AssetFile

class WebpackBabelifier extends Babelifier {

    private static final OUT_DIR = "target/webpack/"
    private String runScript
    private String webpackConfigLocation

    public WebpackBabelifier() {
        // make sure out dir is preset
        File outDir = new File(outdir)
        if (!outDir.exists()) {
            outDir.mkdirs()
        }
        webpackConfigLocation = configuration.externalWebpackConfig
        runScript = 'node_modules/gradle-babel-asset-pipeline-helper/babel-webpack.js'
    }

    String babelify(String string, AssetFile file) {
        File outFile = File.createTempFile('webpack.', '.bundle.js')
        Process process = getProcessString(file, outFile).execute()

        def out = new StringBuffer()
        def err = new StringBuffer()
        process.consumeProcessOutput(out, err)
        process.waitFor()

        if (process.exitValue() > 0) {
            println err.toString()
            throw new BabelifierException(err.toString())
        } else if (configuration.debug) {
            println out.toString()
        }

        String output = outFile.text
        outFile.delete() // manually delete
        return output
    }


    String getProcessString(AssetFile file, File outFile) {
        def processString = "$nodeExec $runScript --entry=${getFileRepresentation(file).getAbsolutePath()} --output=$outFile.absolutePath"
        if(webpackConfigLocation) {
            processString += " --config=$webpackConfigLocation"
        }
        processString
    }

    static File getFileRepresentation(AssetFile file) {
        new File("$file.sourceResolver.baseDirectory", file.path)
    }

    static String getOutdir() {
        configuration.ourDir ?: OUT_DIR
    }

    static getNodeExec() {
        configuration.nodeExec ?: '/usr/local/bin/node'
    }

}
