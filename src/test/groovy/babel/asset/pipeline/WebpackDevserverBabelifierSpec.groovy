package babel.asset.pipeline

import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.fs.FileSystemAssetResolver
import babel.asset.pipeline.Es6AssetFile
import babel.asset.pipeline.WebpackDevserverBabelifier
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
    }

    def "isDevServerRunning should return false if server is not running"() {
        given:
        WebpackDevserverBabelifier babelifier = buildBabelifier()

        expect:
        babelifier.devServerRunning == false

        cleanup:
        babelifier.killDevServer()
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
                        port     : 3001
                ]
        ]
        when:
        babelifier.babelify(code, file)

        then:
        "http://localhost:3001/node-test.js".toURL().text != null
        cleanup:
        babelifier.killDevServer()
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