package babel.asset.pipeline

import asset.pipeline.AssetPipelineConfigHolder
import babel.asset.pipeline.Babelifier
import babel.asset.pipeline.DirectBabelifier
import babel.asset.pipeline.Es6AssetFile
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll

@Unroll
@Stepwise
class DirectBabelifierSpec extends Specification {

    static final JSX_INPUT = "var foo = (<h1>Hello</h1>);"
    static final JSX_OUTPUT = "var foo = React.createElement(\n  \"h1\",\n  null,\n  \"Hello\"\n);"
    static final ES6_INPUT = """var a = ["Hydrogen","Helium","Lithium","Beryl­lium"];var result = a.map(s => s.length);"""

    static final ES5_OUTPUT = '''var a = ["Hydrogen", "Helium", "Lithium", "Beryl­lium"];var result = a.map(function (s) {
  return s.length;
});'''

    static final LOOSE_INPUT = "let foo = ['foo','bar']; for (var i of foo) {i};"
    static final ALL_LOOSE_OUT = '''var foo = ['foo', 'bar'];for (var _iterator = foo, _isArray = Array.isArray(_iterator), _i = 0, _iterator = _isArray ? _iterator : _iterator[Symbol.iterator]();;) {
  var _ref;

  if (_isArray) {
    if (_i >= _iterator.length) break;
    _ref = _iterator[_i++];
  } else {
    _i = _iterator.next();
    if (_i.done) break;
    _ref = _i.value;
  }

  var i = _ref;
  i;
};'''
    static final NON_LOOSE_OUT = '''var foo = ['foo', 'bar'];var _iteratorNormalCompletion = true;
var _didIteratorError = false;
var _iteratorError = undefined;

try {
  for (var _iterator = foo[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
    var i = _step.value;
    i;
  }
} catch (err) {
  _didIteratorError = true;
  _iteratorError = err;
} finally {
  try {
    if (!_iteratorNormalCompletion && _iterator['return']) {
      _iterator['return']();
    }
  } finally {
    if (_didIteratorError) {
      throw _iteratorError;
    }
  }
}

;'''

    def setup() {
        AssetPipelineConfigHolder.config = [babel: [options: [blacklist: ['useStrict'], loose: 'all']]]
    }

    def "process should work"() {
        given:
        Babelifier babelifier = new DirectBabelifier()
        Es6AssetFile file = new Es6AssetFile()
        file.path = "test.js"
        when:
        def result = babelifier.babelify(ES6_INPUT, file)
        then:
        result == ES5_OUTPUT
    }

    def "process should work two times"() {
        given:
        String code = new File("src/test/test.es6").text
        Babelifier babelifier = new DirectBabelifier()
        Es6AssetFile file = new Es6AssetFile()
        file.path = "test/test.js"
        when:
        def result = babelifier.babelify(code, file)
        then:
        result != null // we just want to know that this works!
    }

    def "loose mode is working as expected if loose mode setting is #loose"() {
        given:
        AssetPipelineConfigHolder.config = [babel: [options: [blacklist: ['useStrict'], loose: loose]]]
        and:
        Babelifier babelifier = new DirectBabelifier()
        and:
        Es6AssetFile file = new Es6AssetFile()
        file.path = "test.js"
        when:
        def result = babelifier.babelify(LOOSE_INPUT, file)
        then:
        result == out
        where:
        loose | out
        'all' | ALL_LOOSE_OUT
        null  | NON_LOOSE_OUT
    }

    def "process should transform JSX"() {
        given:
        Babelifier babelifier = new DirectBabelifier()
        Es6AssetFile file = new Es6AssetFile()
        file.path = "test.jsx"
        when:
        def result = babelifier.babelify(JSX_INPUT, file)
        then:
        result == JSX_OUTPUT
    }

    def "no exception should be thrown when there is no options defined. (test for issue#7)"() {
        given:
        def before = AssetPipelineConfigHolder.config
        AssetPipelineConfigHolder.config = [babel: [enabled: true]]
        Babelifier babelifier = new DirectBabelifier()
        Es6AssetFile file = new Es6AssetFile()
        file.path = "test.js"
        when:
        babelifier.babelify(ES6_INPUT, file)
        then:
        noExceptionThrown()
        cleanup:
        AssetPipelineConfigHolder.config = before
    }

    def "Test that module IDs are generated correctly #path => #expected"() {
        given:
        DirectBabelifier processor = new DirectBabelifier()
        when:
        def moduleId = processor.removeExtensionFromPath(path)

        then:
        moduleId == expected

        where:
        path            | expected
        'a/b/c'         | 'a/b/c'
        'a/b/c.jpg'     | 'a/b/c'
        'a/b/c.jpg.jpg' | 'a/b/c.jpg'
        'a.b/c'         | 'a.b/c'
        'a.b/c.jpg'     | 'a.b/c'
        'a.b/c.jpg.jpg' | 'a.b/c.jpg'
        'c'             | 'c'
        'c.jpg'         | 'c'
        'c.jpg.jpg'     | 'c.jpg'
    }
}