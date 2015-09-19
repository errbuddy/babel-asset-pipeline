package asset.pipeline.babel

import asset.pipeline.AssetCompiler
import asset.pipeline.AssetPipelineConfigHolder
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class BabelProcessorSpec extends Specification {

    def es6code = """
        var a = ["Hydrogen","Helium","Lithium","Beryl­lium"];
        var result = a.map(s => s.length);
    """

    def setup() {
        AssetPipelineConfigHolder.config = [babel: [options: [blacklist: ['useStrict']]]]
    }

    def "process should work"() {
        given:
        BabelProcessor processor = new BabelProcessor(new AssetCompiler())
        Es6AssetFile file = new Es6AssetFile()
        file.path = "test.js"
        when:
        def result = processor.process(es6code, file)
        then:
        result == '''
var a = ["Hydrogen", "Helium", "Lithium", "Beryl­lium"];
var result = a.map(function (s) {
        return s.length;
});'''
    }

    def "process should work two times"() {
        given:
        String code = new File("src/test/test.js").text
        BabelProcessor processor = new BabelProcessor(new AssetCompiler())
        Es6AssetFile file = new Es6AssetFile()
        file.path = "test/test.js"
        when:
        def result = processor.process(code, file)
        then:
        result != null // we just want to know that this works!
    }
}
