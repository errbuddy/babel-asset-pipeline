package asset.pipeline.babel

import asset.pipeline.AssetFile
import asset.pipeline.AssetPipelineConfigHolder
import com.google.gson.Gson

abstract class Babelifier {

    static final CONVERTER = new Gson()

    Map options

    public Babelifier() {
        options = configuration?.options ?: [:]
    }

    abstract String babelify(String string, AssetFile file)

    static def getConfiguration() {
        AssetPipelineConfigHolder.config?.babel
    }

    protected String getOptionsString(){
        CONVERTER.toJson(options)
    }
}