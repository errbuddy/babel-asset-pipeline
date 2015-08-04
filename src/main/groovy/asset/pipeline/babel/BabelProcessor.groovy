package asset.pipeline.babel

import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.AssetPipelineConfigHolder
import com.google.gson.Gson
import groovy.util.logging.Log4j
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject

import javax.script.Invocable

@Log4j
class BabelProcessor extends AbstractProcessor {

    public static final ThreadLocal threadLocal = new ThreadLocal();
    Scriptable globalScope
    ClassLoader classLoader
    Invocable invocable
    static final String BABEL_OPTIONS = new Gson().toJson(AssetPipelineConfigHolder.config?.babel?.options ?: [:])

    BabelProcessor(AssetCompiler precompiler) {
        super(precompiler)

        classLoader = this.class.classLoader
        def shellJsResource = classLoader.getResource('asset/pipeline/babel/shell.js')
        def envRhinoJsResource = classLoader.getResource('asset/pipeline/babel/env.rhino.js')
        def objectResource = classLoader.getResource('asset/pipeline/babel/object.js')
        def requireResource = classLoader.getResource('asset/pipeline/babel/r.js')
        def babelResource = classLoader.getResource('asset/pipeline/babel/babel.js')
        Context cx = Context.enter()

        cx.setOptimizationLevel(-1)
        globalScope = cx.initStandardObjects()
        globalScope.defineProperty("arguments", cx, ScriptableObject.DONTENUM);
        cx.evaluateString(globalScope, shellJsResource.getText('UTF-8'), shellJsResource.file, 1, null)
        cx.evaluateString(globalScope, envRhinoJsResource.getText('UTF-8'), envRhinoJsResource.file, 1, null)
        cx.evaluateString(globalScope, objectResource.getText('UTF-8'), objectResource.file, 1, null)
        cx.evaluateString(globalScope, babelResource.getText('UTF-8'), babelResource.file, 1, null)
    }

    String process(String input, AssetFile assetFile) {
        log.info("transforming $assetFile.name")
        try {
            threadLocal.set(assetFile);

            def cx = Context.enter()
            def compileScope = cx.newObject(globalScope)
            compileScope.setParentScope(globalScope)
            compileScope.put("es6Source", compileScope, input)
            def result = cx.evaluateString(compileScope, "babel.transform(es6Source, $BABEL_OPTIONS).code", "babel command", 0, null)
            return result.toString()

        } catch (Exception e) {
            throw new Exception("babel-transforming $assetFile.name failed: $e")
        } finally {
            Context.exit()
        }
    }

    static void print(text) {
        log.debug text
    }

}