package asset.pipeline.babel

import asset.pipeline.AssetCompiler
import spock.lang.Specification

class BabelProcessorSpec extends Specification{

    def es6code = """
        var a = ["Hydrogen","Helium","Lithium","Beryl­lium"];
        var result = a.map(s => s.length);
    """

    def "process should work"(){
        given:
        BabelProcessor processor = new BabelProcessor(new AssetCompiler())
        Es6AssetFile file = new Es6AssetFile()
        file.path = "test.js"
        when:
        def result = processor.process(es6code, file)
        then:
        result == '''var a = ["Hydrogen", "Helium", "Lithium", "Beryl­lium"];
var result = a.map(function (s) {
        return s.length;
});'''
    }
}
