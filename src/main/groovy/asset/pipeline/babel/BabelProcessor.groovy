package asset.pipeline.babel

import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.AssetPipelineConfigHolder
import com.google.gson.Gson
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject

class BabelProcessor extends AbstractProcessor {

    public static final ThreadLocal threadLocal = new ThreadLocal()
    static Scriptable globalScope
    final String BABEL_OPTIONS = new Gson().toJson(AssetPipelineConfigHolder.config?.babel?.options ?: [:])

    private static boolean contextInitialized = false

    BabelProcessor(AssetCompiler precompiler) {
        super(precompiler)
        ClassLoader classLoader = this.class.classLoader
        if (!contextInitialized) {
            def shellJsResource = classLoader.getResource('asset/pipeline/babel/shell.js')
            def envRhinoJsResource = classLoader.getResource('asset/pipeline/babel/env.rhino.js')
            def objectResource = classLoader.getResource('asset/pipeline/babel/object.js')
//            def requireResource = classLoader.getResource('asset/pipeline/babel/r.js')
            def babelResource = classLoader.getResource('asset/pipeline/babel/babel.js')
            Context context = Context.enter()
            context.setOptimizationLevel(-1)
            context.setLanguageVersion(170)
            globalScope = context.initStandardObjects()
            globalScope.defineProperty("arguments", context, ScriptableObject.DONTENUM)
            context.evaluateString(globalScope, shellJsResource.getText('UTF-8'), shellJsResource.file, 1, null)
            context.evaluateString(globalScope, envRhinoJsResource.getText('UTF-8'), envRhinoJsResource.file, 1, null)
            context.evaluateString(globalScope, objectResource.getText('UTF-8'), objectResource.file, 1, null)
            context.evaluateString(globalScope, babelResource.getText('UTF-8'), babelResource.file, 1, null)
            Context.exit()
            contextInitialized = true
        }
    }

    String process(String input, AssetFile assetFile) {
        try {
            Context cx = Context.enter()
            threadLocal.set(assetFile)
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
        println text
    }

}