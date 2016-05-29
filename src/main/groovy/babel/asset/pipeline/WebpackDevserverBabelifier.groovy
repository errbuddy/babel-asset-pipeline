package babel.asset.pipeline

import asset.pipeline.AssetFile
import groovy.util.logging.Log
import org.apache.commons.io.FilenameUtils

import java.util.concurrent.TimeUnit
import java.util.logging.Level

@Log
class WebpackDevserverBabelifier extends Babelifier {

    private String webpackConfigLocation
    private String devServerRunScript
    synchronized private static Process devServerProcess
    static String currentlyServingFile = ""

    static LoggingOutputStream infoOut
    static LoggingOutputStream errOut

    public WebpackDevserverBabelifier() {
        // make sure out dir is preset
        devServerRunScript = 'node_modules/gradle-babel-asset-pipeline-helper/babel-webpack-dev-server.js'
        webpackConfigLocation = configuration.externalWebpackConfig
    }

    String babelify(String string, AssetFile file) {
        File inFile = WebpackBabelifier.getFileRepresentation(file)
        currentlyServingFile = getPublicFileName(inFile)
        if (!devServerRunning) {
            startDevServer(inFile)
        }
        "http://localhost:$port/${getPublicFileName(inFile)}".toURL().text
    }

    boolean isUsingDevServer() {
        return true
    }

    boolean isDevServerRunning() {
        if (devServerProcess?.alive) {
            try {
                Socket socket = new Socket()
                socket.connect(new InetSocketAddress('127.0.0.1', port), 200)
                socket.close()
                return true
            } catch (Exception ignore) {
            }
        }
        return false
    }

    void startDevServer(File file) {
        log.info "starting dev server on port $port"
        Runtime.getRuntime().addShutdownHook {
            killDevServer()
        }
        if (!infoOut) {
            infoOut = new LoggingOutputStream(log, Level.INFO)
            errOut = new LoggingOutputStream(log, Level.SEVERE)
        }

        devServerProcess = getProcessString(file).execute(WebpackBabelifier.environmentVariables, null)
        devServerProcess.consumeProcessOutput(infoOut, errOut)
        if (devServerProcess.waitFor(5000, TimeUnit.MILLISECONDS)) {
            throw new BabelifierException("Webpack-dev-server has not started up")
        }
    }

    static void killDevServer() {
        log.info "killing webpack-dev-server listening on port $port"
        if (devServerProcess && !devServerProcess.alive) {
            log.warning("could not gracefully stop the dev server")
        }
        devServerProcess?.waitForOrKill(100)
        devServerProcess = null
        infoOut?.close()
        errOut?.close()
        infoOut = null
        errOut = null
    }

    static int getPort() {
        configuration.port ?: 3000
    }

    static String getPublicFileName(File file) {
        "${FilenameUtils.getBaseName(file.name)}.js"
    }

    String getProcessString(File inFile) {
        def processString = "$WebpackBabelifier.nodeExec $devServerRunScript --entry=$inFile.absolutePath --outName=${getPublicFileName(inFile)} --port=$port"

        if (webpackConfigLocation) {
            // add the external config location
            processString += " --config=$webpackConfigLocation"
        }
        processString
    }
}
