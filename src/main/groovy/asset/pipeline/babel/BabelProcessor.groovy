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
        initProcessor()
    }

    private initProcessor() {
        globalBabelOptions = CONVERTER.toJson(configuration?.options ?: [:])
        println configuration
        String processorString = configuration?.processor
        switch (processorString) {
            case 'webpack':
                println "using webpack"
                babelifier = new WebpackBabelifier()
                break
            case 'webpack-dev-server':
                println "using webpack-dev-server"
                babelifier = new WebpackDevserverBabelifier()
                break
            default:
                println "using rhino"
                babelifier = new DirectBabelifier()
                break
        }
    }

    String process(String input, AssetFile assetFile) {
        // only process if
        // processing is enabled AND
        // the given AssetFile is a Es6File OR JsxFile OR processJsFiles is enabled
        if (enabled && shouldFileBeProcessed(assetFile)) {
            babelifier.babelify(input, assetFile)
        } else {
            return input
        }
    }

    /**
     * checks whether the given AssetFile should be processed this can be false in certain conditions e.g.
     * processing is outsourced to the webpack-dev-server or
     * processing of js files is disabled
     * @param assetFile
     * @return
     */
    private boolean shouldFileBeProcessed(AssetFile assetFile) {
        return !babelifier.usingDevServer && (processJsFiles || assetFile in Es6AssetFile || assetFile in JsxAssetFile)
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