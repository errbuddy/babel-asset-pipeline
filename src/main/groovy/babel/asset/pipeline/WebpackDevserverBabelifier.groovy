package babel.asset.pipeline

import asset.pipeline.AssetFile
import org.apache.commons.io.FilenameUtils

import java.util.concurrent.TimeUnit

class WebpackDevserverBabelifier extends Babelifier {

    private String webpackConfigLocation
    private String devServerRunScript
    synchronized private static Process devServerProcess
    private static final StringBuffer ERROR_BUFFER = new StringBuffer()

    public WebpackDevserverBabelifier() {
        // make sure out dir is preset
        devServerRunScript = 'node_modules/gradle-babel-asset-pipeline-helper/babel-webpack-dev-server.js'
        webpackConfigLocation = configuration.externalWebpackConfig

    }

    String babelify(String string, AssetFile file) {
        File inFile = WebpackBabelifier.getFileRepresentation(file)
        if (!devServerProcess) {
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
        println "starting dev server on port $port"
        Runtime.getRuntime().addShutdownHook {
            killDevServer()
        }
        devServerProcess = getProcessString(file).execute()
        devServerProcess.consumeProcessOutput(System.out, ERROR_BUFFER)
        if (devServerProcess.waitFor(5000, TimeUnit.MILLISECONDS)) {
            throw new BabelifierException(ERROR_BUFFER.toString())
        }
    }

    static void killDevServer() {
        println("killing webpack-dev-server listening on port $port")
        if (devServerProcess && !devServerProcess.alive) {
            throw new BabelifierException(ERROR_BUFFER.toString())
        }
        devServerProcess?.waitForOrKill(100)
        devServerProcess = null
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
