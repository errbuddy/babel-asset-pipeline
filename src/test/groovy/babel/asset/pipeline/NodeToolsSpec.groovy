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

    def "if NODE_PATH is set, it is returned"() {
        given:
        def tool = GroovySpy(BabelSpecTools, global: true)

        and:
        String nodePath = '/this/is/a/test/path'

        when:
        String result = tool.guessNodePath()

        then:
        1 * BabelSpecTools.getNodeRootFromGradleDefault() >> null
        2 * BabelSpecTools.getNodeRootFromEnv() >> nodePath
        1 * BabelSpecTools.getNodeExecutable(nodePath) >> "$nodePath/bin/node"
        result == "$nodePath/bin/node"
    }

}
