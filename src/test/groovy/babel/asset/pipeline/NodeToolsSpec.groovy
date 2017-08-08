package babel.asset.pipeline

import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
class NodeToolsSpec extends Specification {

    def "gradle node installation is returned"() {
        when:
        def result = BabelSpecTools.guessNodePath()
        then:
        result != null
        result != '/this/is/a/test/path' // just make sure this is not not failing because of the wild mocking we did
    }

}
