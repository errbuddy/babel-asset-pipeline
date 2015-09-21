package asset.pipeline.babel

import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.AssetPipelineConfigHolder
import com.google.gson.Gson
import groovy.util.logging.Slf4j
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject

@Slf4j
class BabelProcessor extends AbstractProcessor {

    static Scriptable globalScope

    String babelOptions

    public static final ThreadLocal THREAD_LOCAL = new ThreadLocal()
    private static final CONVERTER = new Gson()
    private static boolean contextInitialized = false

    BabelProcessor(AssetCompiler precompiler) {
        super(precompiler)
        if (config) {
            babelOptions = CONVERTER.toJson(config.options ?: [:])
        } else {
            babelOptions = "{}" //
        }
        // only load all the javascript if it is enabled AND not yet initialized
        if (enabled && !contextInitialized) {
            ClassLoader classLoader = this.class.classLoader
            def shellJsResource = classLoader.getResource('asset/pipeline/babel/shell.js')
            def envRhinoJsResource = classLoader.getResource('asset/pipeline/babel/env.rhino.js')
            def objectResource = classLoader.getResource('asset/pipeline/babel/object.js')
//            def requireResource = classLoader.getResource('asset/pipeline/babel/r.js')
            def babelResource = classLoader.getResource('asset/pipeline/babel/browser.js')
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
        // only process if
        // processing is enabled AND
        // the given AssetFile is a Es6File OR processJsFiles is enabled
        if (enabled && (processJsFiles || assetFile in Es6AssetFile)) {
            try {
                Context cx = Context.enter()
                THREAD_LOCAL.set(assetFile)
                def compileScope = cx.newObject(globalScope)
                compileScope.setParentScope(globalScope)
                compileScope.put("es6Source", compileScope, input)
                def result = cx.evaluateString(compileScope, "babel.transform(es6Source, $babelOptions).code", "babel command", 0, null)
                return result.toString()

            } catch (Exception e) {
                throw new Exception("babel-transforming $assetFile.name failed: $e")
            } finally {
                Context.exit()
            }
        } else {
            return input
        }
    }

    static void print(text) {
        log.debug text
    }

    static def getConfig() {
        AssetPipelineConfigHolder.config?.babel
    }

    static boolean isEnabled(){
        config?.enabled != null ? config.enabled as Boolean : true
    }

    static boolean isProcessJsFiles(){
        config?.processJsFiles != null ? config.processJsFiles as Boolean : false
    }

}