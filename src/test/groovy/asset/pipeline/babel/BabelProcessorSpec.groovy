package asset.pipeline.babel

import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.JsAssetFile
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll

@Unroll
@Stepwise
class BabelProcessorSpec extends Specification {

    def "process calls a babelifier is enabled"() {
        given:
        AssetPipelineConfigHolder.config = [babel: [enabled: true, options: [blacklist: ['useStrict']]]]
        and:
        BabelProcessor processor = new BabelProcessor(null)
        and:
        Es6AssetFile file = new Es6AssetFile()
        file.path = "test.js"
        and:
        def babelifier = Mock(DirectBabelifier)
        processor.babelifier = babelifier
        and:
        def input = '()=>{console.log("foo")}'

        when:
        processor.process(input, file)
        then:
        1 * babelifier.babelify(input, file) >> ""
    }

    def "process does nothing if not enabled"() {
        given:
        AssetPipelineConfigHolder.config = [babel: [enabled: false, options: [blacklist: ['useStrict']]]]
        and:
        BabelProcessor processor = new BabelProcessor(null)
        and:
        Es6AssetFile file = new Es6AssetFile()
        file.path = "test.js"
        and:
        def babelifier = Mock(DirectBabelifier)
        processor.babelifier = babelifier
        and:
        def input = '()=>{console.log("foo")}'

        when:
        processor.process(input, file)
        then:
        0 * babelifier.babelify(input, file) >> ""
    }

    def "JsAssetFiles are handled if processJsFiles is enabled"() {
        given:
        AssetPipelineConfigHolder.config = [babel: [processJsFiles: true, enabled: true, options: [blacklist: ['useStrict']]]]
        and:
        def processor = new BabelProcessor(null)

        and:
        JsAssetFile file = new JsAssetFile()
        file.path = "test.js"

        and:
        def babelifier = Mock(DirectBabelifier)
        processor.babelifier = babelifier

        when:
        processor.process("", file)

        then:
        1 * babelifier.babelify(_, _)
    }

    def "JsAssetFiles are not handled if processJsFiles is disabled"() {
        given:
        AssetPipelineConfigHolder.config = [babel: [processJsFiles: false, enabled: true, options: [blacklist: ['useStrict']]]]
        and:
        def processor = new BabelProcessor(null)

        and:
        JsAssetFile file = new JsAssetFile()
        file.path = "test.js"

        and:
        def babelifier = Mock(DirectBabelifier)
        processor.babelifier = babelifier

        when:
        processor.process("", file)

        then:
        0 * babelifier.babelify(_, _)
    }

    def "Processor is initiated with the right babelifier"() {
        given:
        AssetPipelineConfigHolder.config = [babel: [
                processor: processor,
                options  : [
                        blacklist: ['useStrict'],
                        loose    : 'all'
                ]
        ]]
        expect:
        new BabelProcessor(null).babelifier.class == expectedClass
        where:
        processor | expectedClass
        ''        | DirectBabelifier
        'foo'     | DirectBabelifier
        null      | DirectBabelifier
        'node'    | NodeBabelifier

    }
}