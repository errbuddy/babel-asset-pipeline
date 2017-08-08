package babel.asset.pipeline

import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.AssetPipelineConfigHolder
import com.google.gson.Gson
import groovy.util.logging.Slf4j

@Slf4j
class BabelProcessor extends AbstractProcessor {


    static String globalBabelOptions
    private static final CONVERTER = new Gson()
    private static Babelifier babelifierInstance

    BabelProcessor(AssetCompiler precompiler) {
        super(precompiler)
    }

    private static Babelifier getBabelifier() {
        if (!babelifierInstance) {
            initProcessor()
        }
        babelifierInstance
    }

    static initProcessor() {
        globalBabelOptions = CONVERTER.toJson(configuration?.options ?: [:])
        String processorString = configuration?.processor
        switch (processorString) {
            case 'webpack':
                babelifierInstance = new WebpackBabelifier()
                break
            case 'webpack-dev-server':
                babelifierInstance = new WebpackDevserverBabelifier()
                break
            default:
                babelifierInstance = new DirectBabelifier()
                break
        }
        log.info 'initiated {}', babelifierInstance.class.simpleName
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
        return (processJsFiles || assetFile in Es6AssetFile || assetFile in JsxAssetFile)
    }

    static boolean isExternalServerUsed() {
        babelifier.usingDevServer
    }

    static boolean isExternalServerRunning() {
        babelifier.devServerRunning
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