package babel.asset.pipeline

import asset.pipeline.AssetFile
import groovy.util.logging.Slf4j
import org.slf4j.event.Level

@Slf4j
class WebpackBabelifier extends Babelifier {

    private String runScript
    private String webpackConfigLocation

    public WebpackBabelifier() {
        webpackConfigLocation = configuration.externalWebpackConfig
        runScript = 'node_modules/gradle-babel-asset-pipeline-helper/babel-webpack.js'
    }

    String babelify(String string, AssetFile file) {
        LoggingOutputStream infoOut = new LoggingOutputStream(log, Level.INFO)
        LoggingOutputStream errOut = new LoggingOutputStream(log, Level.ERROR)
        File outFile = File.createTempFile('webpack.', '.bundle.js')
        String result
        try {
            Process process = getProcessString(file, outFile).execute(environmentVariables, null)

            process.consumeProcessOutput(infoOut, errOut)
            process.waitFor()

            if (process.exitValue() > 0) {
                throw new BabelifierException("webpack exited with ${process.exitValue()} for $file")
            }

        } finally {
            result = outFile.text
            outFile.delete() // manually delete here as we don't want to leave to much garbage
            infoOut.close()
            errOut.close()
        }

        return result
    }


    String getProcessString(AssetFile file, File outFile) {
        def processString = "$nodeExec $runScript --entry=${getFileRepresentation(file).getAbsolutePath()} --output=$outFile.absolutePath"
        if (webpackConfigLocation) {
            processString += " --config=$webpackConfigLocation"
        }
        processString
    }

    static List getEnvironmentVariables() {
        List result = []
        if (nodeEnv) {
            result << "NODE_ENV=$nodeEnv"
        }
        // TODO: add the possibility to add custom env variables

        result
    }

    static getNodeEnv() {
        configuration.nodeEnv ?: null
    }


    static File getFileRepresentation(AssetFile file) {
        new File("$file.sourceResolver.baseDirectory", file.path)
    }

    static getNodeExec() {
        configuration.nodeExec ?: '/usr/local/bin/node'
    }

}
