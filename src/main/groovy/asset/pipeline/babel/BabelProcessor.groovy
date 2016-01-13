package asset.pipeline.babel

import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.AssetPipelineConfigHolder
import com.google.gson.Gson

class BabelProcessor extends AbstractProcessor {


    String globalBabelOptions

    private static final CONVERTER = new Gson()
    private static Babelifier babelifier

    BabelProcessor(AssetCompiler precompiler) {
        super(precompiler)
        babelifier = configuration?.processor == 'node' ? new NodeBabelifier() : new DirectBabelifier()
        globalBabelOptions = CONVERTER.toJson(configuration?.options ?: [:])
    }

    String process(String input, AssetFile assetFile) {
        // only process if
        // processing is enabled AND
        // the given AssetFile is a Es6File OR JsxFile OR processJsFiles is enabled
        if (enabled && (processJsFiles || assetFile in Es6AssetFile || assetFile in JsxAssetFile)) {
            babelifier.babelify(input, assetFile)
        } else {
            return input
        }
    }

    static void print(text) {
        println text
    }

    static def getConfiguration() {
        AssetPipelineConfigHolder.config?.babel
    }

    static boolean isEnabled() {
        configuration?.containsKey('enabled') ? configuration.enabled as Boolean : true
    }

    static boolean isProcessJsFiles() {
        configuration?.processJsFiles != null ? configuration.processJsFiles as Boolean : false
    }

}