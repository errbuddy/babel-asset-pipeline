package babel.asset.pipeline

import asset.pipeline.AssetFile
import com.google.gson.Gson
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject

class DirectBabelifier extends Babelifier {

    static Scriptable globalScope
    public static final ThreadLocal THREAD_LOCAL = new ThreadLocal()
    private static boolean contextInitialized = false
    private static final CONVERTER = new Gson()

    public DirectBabelifier() {
        super()
        // only load all the javascript if it is enabled AND not yet initialized
        if (!contextInitialized) {
            ClassLoader classLoader = this.class.classLoader
            def shellJsResource = classLoader.getResource('babel/asset/pipeline/shell.js')
            def envRhinoJsResource = classLoader.getResource('babel/asset/pipeline/env.rhino.js')
            def objectResource = classLoader.getResource('babel/asset/pipeline/object.js')
            def babelResource = classLoader.getResource('babel/asset/pipeline/browser.js')
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

    String babelify(String input, AssetFile file) {
        try {
            Context cx = Context.enter()
            String localBabelOptions = optionsString
            Map config = options.clone() as HashMap
            if (config?.moduleIds) {
                // Used when transpiling ES2015 modules to other formats,
                // provides babel with a module ID based on the assetFile's location.
                config.moduleId = removeExtensionFromPath(file.path)
                localBabelOptions = CONVERTER.toJson(config ?: [:])
            }
            THREAD_LOCAL.set(file)
            def compileScope = cx.newObject(globalScope)
            compileScope.setParentScope(globalScope)
            compileScope.put("es6Source", compileScope, input)
            def result = cx.evaluateString(compileScope, "babel.transform(es6Source, $localBabelOptions).code", "babel command", 0, null)
            return result.toString()
        } catch (Throwable throwable) {
            throw new Exception("Exception while babelifying $file.name", throwable)
        } finally {
            Context.exit()
        }
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
