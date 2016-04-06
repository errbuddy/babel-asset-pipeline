package babel.asset.pipeline

import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.fs.FileSystemAssetResolver
import babel.asset.pipeline.Babelifier
import babel.asset.pipeline.BabelifierException
import babel.asset.pipeline.Es6AssetFile
import babel.asset.pipeline.WebpackBabelifier
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll

@Unroll
@Stepwise
class WebpackBabelifierSpec extends Specification {

    def setup() {
        AssetPipelineConfigHolder.config = [
                babel: [
                        processor: 'webpack',
                        debug    : true,
                        options  : [
                                blacklist: ['useStrict'],
                                loose    : 'all'
                        ]
                ]
        ]
    }

    def "process should work"() {
        given:
        File base = new File("src/test/node-test.es6")
        String code = base.text
        Babelifier babelifier = buildBabelifier()
        Es6AssetFile file = newAssetFile
        file.path = base.absolutePath
        when:
        def result = babelifier.babelify(code, file)
        then:
        result != null // we just want to know that this works!
        result.size() > 0
    }

    def "process should throw an exception if file is broken"() {
        given:
        File base = new File("src/test/broken.es6")
        String code = base.text
        Babelifier babelifier = buildBabelifier()
        Es6AssetFile file = newAssetFile
        file.path = base.absolutePath
        when:
        babelifier.babelify(code, file)
        then:
        thrown(BabelifierException)
    }

    private static Babelifier buildBabelifier() {
        new WebpackBabelifier()
    }

    private static Es6AssetFile getNewAssetFile() {
        Es6AssetFile file = new Es6AssetFile()
        file.sourceResolver = new FileSystemAssetResolver('testResolver', '/')
        file
    }

}