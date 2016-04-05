package asset.pipeline.babel

import asset.pipeline.AssetFile
import org.apache.commons.io.FilenameUtils

import java.util.concurrent.TimeUnit

class WebpackDevserverBabelifier extends Babelifier {

    private String webpackConfigLocation
    private String devServerRunScript
    private static Process devServerProcess
    private static final StringBuffer ERROR_BUFFER = new StringBuffer()
    private int portNumber = 3000

    public WebpackDevserverBabelifier() {
        // make sure out dir is preset
        devServerRunScript = 'node_modules/gradle-babel-asset-pipeline-helper/babel-webpack-dev-server.js'
        webpackConfigLocation = configuration.externalWebpackConfig

        if (configuration.port) {
            portNumber = configuration.port
        }
        Runtime.getRuntime().addShutdownHook {
            killDevServer()
        }

    }

    @Override
    boolean isUsingDevServer() {
        return true
    }

    String babelify(String string, AssetFile file) {
        if (!devServerProcess) {
            startDevServer(file)
        }
        return string
    }

    static boolean isDevServerRunning() {
        try {
            Socket socket = new Socket()
            socket.connect(new InetSocketAddress('127.0.0.1', port), 200)
            socket.close()
            return true
        } catch (Exception ignore) {
            return false
        }
    }

    void startDevServer(AssetFile file) {
        devServerProcess = getProcessString(file).execute()
        devServerProcess.consumeProcessOutput(System.out, ERROR_BUFFER)
        if (devServerProcess.waitFor(5000, TimeUnit.MILLISECONDS)) {
            throw new BabelifierException(ERROR_BUFFER.toString())
        }
    }

    void killDevServer() {
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

    String getProcessString(AssetFile file) {
        File inFile = WebpackBabelifier.getFileRepresentation(file)
        def processString = "$WebpackBabelifier.nodeExec $devServerRunScript --entry=$inFile.absolutePath --outName=${FilenameUtils.getBaseName(inFile.name)}.js --port=$port"
        if (webpackConfigLocation) {
            // add the external config location
            processString += " --config=$webpackConfigLocation"
        }
        processString
    }
}
