package asset.pipeline.babel

import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.AssetPipelineConfigHolder
import com.google.gson.Gson
import groovy.util.logging.Log
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject

@Log
class BabelProcessor extends AbstractProcessor {

    static Scriptable globalScope

    String globalBabelOptions

    public static final ThreadLocal THREAD_LOCAL = new ThreadLocal()
    private static final CONVERTER = new Gson()
    private static boolean contextInitialized = false

    BabelProcessor(AssetCompiler precompiler) {
        super(precompiler)
        if (configuration) {
            globalBabelOptions = CONVERTER.toJson(configuration.options ?: [:])
        } else {
            globalBabelOptions = "{}" //
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
        // the given AssetFile is a Es6File OR JsxFile OR processJsFiles is enabled
        if (enabled && (processJsFiles || assetFile in Es6AssetFile || assetFile in JsxAssetFile)) {
            try {
                String localBabelOptions = globalBabelOptions
                Map config = configuration?.options ? configuration.options.clone() as HashMap : [:]
                if (config?.moduleIds) {
                    // Used when transpiling ES2015 modules to other formats,
                    // provides babel with a module ID based on the assetFile's location.
                    config.moduleId = removeExtensionFromPath(assetFile.path)
                    localBabelOptions = CONVERTER.toJson(config ?: [:])
                }
                Context cx = Context.enter()
                THREAD_LOCAL.set(assetFile)
                def compileScope = cx.newObject(globalScope)
                compileScope.setParentScope(globalScope)
                compileScope.put("es6Source", compileScope, input)
                def result = cx.evaluateString(compileScope, "babel.transform(es6Source, $localBabelOptions).code", "babel command", 0, null)
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
        log.info text
    }

    static def getConfiguration() {
        AssetPipelineConfigHolder.config?.babel
    }

    static boolean isEnabled(){
        configuration?.containsKey('enabled') ? configuration.enabled as Boolean : true
    }

    static boolean isProcessJsFiles(){
        configuration?.processJsFiles != null ? configuration.processJsFiles as Boolean : false
    }

    static String removeExtensionFromPath(String path) {

        String separator = System.getProperty("file.separator")

        // Make sure paths with dots are not truncated. e.g. foo.bar/baz.js
        int lastSeparatorIndex = path.lastIndexOf(separator)
        int extensionIndex = path.lastIndexOf(".")

        if (lastSeparatorIndex < extensionIndex) return path.substring(0, extensionIndex)

        return path
    }

}