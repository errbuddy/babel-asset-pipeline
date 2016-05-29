package babel.asset.pipeline

import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.fs.FileSystemAssetResolver
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll

@Unroll
@Stepwise
class WebpackDevserverBabelifierSpec extends Specification {

    def setup() {
        AssetPipelineConfigHolder.config = [
                babel: [
                        processor: 'webpack-dev-server',
                        nodeExec: BabelSpecTools.guessNodePath()
                ]
        ]
    }

    def "process should work"() {
        given:
        File base = new File("src/test/node-test.es6")
        String code = base.text
        WebpackDevserverBabelifier babelifier = buildBabelifier()
        Es6AssetFile file = newAssetFile
        file.path = base.absolutePath
        when:
        babelifier.babelify(code, file)


        then:
        "http://localhost:3000/node-test.js".toURL().text != null
        cleanup:
        babelifier.killDevServer()
        sleep(500)
    }

    def "isDevServerRunning should return true if server really is running"() {
        given:
        File base = new File("src/test/node-test.es6")
        String code = base.text
        WebpackDevserverBabelifier babelifier = buildBabelifier()
        Es6AssetFile file = newAssetFile
        file.path = base.absolutePath
        babelifier.babelify(code, file) // start the server

        expect:
        babelifier.devServerRunning == true

        cleanup:
        babelifier.killDevServer()
        sleep(500)
    }

    def "isDevServerRunning should return false if server is not running"() {
        given:
        WebpackDevserverBabelifier babelifier = buildBabelifier()

        expect:
        babelifier.devServerRunning == false

        cleanup:
        babelifier.killDevServer()
        sleep(500)
    }

    def "defining a different port should work"() {
        given:
        File base = new File("src/test/node-test.es6")
        String code = base.text
        WebpackDevserverBabelifier babelifier = buildBabelifier()
        Es6AssetFile file = newAssetFile
        file.path = base.absolutePath

        and:
        AssetPipelineConfigHolder.config = [
                babel: [
                        processor: 'webpack-dev-server',
                        port     : 3001,
                        nodeEnv  : "production"
                ]
        ]
        when:
        babelifier.babelify(code, file)

        then:
        "http://localhost:3001/node-test.js".toURL().text != null
        cleanup:
        babelifier.killDevServer()
        sleep(500)
    }

    private static WebpackDevserverBabelifier buildBabelifier() {
        new WebpackDevserverBabelifier()
    }

    private static Es6AssetFile getNewAssetFile() {
        Es6AssetFile file = new Es6AssetFile()
        file.sourceResolver = new FileSystemAssetResolver('testResolver', '/')
        file
    }

}