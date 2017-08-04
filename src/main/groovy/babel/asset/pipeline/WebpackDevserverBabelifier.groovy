package babel.asset.pipeline

import asset.pipeline.AssetFile
import groovy.util.logging.Slf4j
import org.apache.commons.io.FilenameUtils
import org.slf4j.event.Level

import java.util.concurrent.TimeUnit

@Slf4j
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
        if (devServerProcess && isDevserverAlive(devServerProcess)) {
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
            errOut = new LoggingOutputStream(log, Level.ERROR)
        }

        devServerProcess = getProcessString(file).execute(WebpackBabelifier.environmentVariables, null)
        devServerProcess.consumeProcessOutput(infoOut, errOut)
        if (devServerProcess.waitFor(5000, TimeUnit.MILLISECONDS)) {
            throw new BabelifierException("Webpack-dev-server has not started up")
        }
    }

    static void killDevServer() {
        if (devServerProcess && isDevserverAlive(devServerProcess)) {
            log.info "killing webpack-dev-server listening on port $port"
            devServerProcess?.waitForOrKill(100)
            devServerProcess = null
            infoOut?.close()
            errOut?.close()
            infoOut = null
            errOut = null
        }
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

    /**
     * as Process.alive is a java8 feature, we are implementing it ourselves here
     * @return
     */
    static boolean isDevserverAlive(Process process) {
        try {
            process.exitValue();
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
