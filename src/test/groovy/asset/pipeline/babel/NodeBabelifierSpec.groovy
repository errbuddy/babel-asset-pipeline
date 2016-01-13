package asset.pipeline.babel

import asset.pipeline.AssetPipelineConfigHolder
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll

@Unroll
@Stepwise
class NodeBabelifierSpec extends Specification {


    def setup() {
        AssetPipelineConfigHolder.config = [
                babel: [
                        processor: 'node',
                        options  : [
                                blacklist: ['useStrict'],
                                loose    : 'all'
                        ]
                ]
        ]
    }

    @Ignore("find out how to override environment variables")
    def "test that an exception is thrown if nodePath is not set"() {
        given:
        AssetPipelineConfigHolder.config = [
                babel: [
                        processor: 'node',
                        nodePath : 'node'
                ]
        ]
        when:
        new NodeBabelifier()

        then:
        thrown(Exception)
    }

    @Ignore("find out how to override environment variables")
    def "test that an exception is thrown if browserifyPath is not set"() {
        given:
        AssetPipelineConfigHolder.config = [
                babel: [
                        processor: 'node',
                ]
        ]
        when:
        new NodeBabelifier()

        then:
        thrown(Exception)
    }

    def "process should work"() {
        given:
        File base = new File("src/test/node-test.es6")
        String code = base.text
        Babelifier babelifier = buildBabelifier()
        Es6AssetFile file = new Es6AssetFile()
        file.path = base.absolutePath
        when:
        def result = babelifier.babelify(code, file)
        then:
        result != null // we just want to know that this works!
    }

    def "process should throw an exception if file is broken"() {
        given:
        File base = new File("src/test/broken.es6")
        String code = base.text
        Babelifier babelifier = buildBabelifier()
        Es6AssetFile file = new Es6AssetFile()
        file.path = base.absolutePath
        when:
        babelifier.babelify(code, file)
        then:
        thrown(NodeBabelifier.NodeBabelifierException)
    }

    private static Babelifier buildBabelifier() {
        new NodeBabelifier()
    }

}