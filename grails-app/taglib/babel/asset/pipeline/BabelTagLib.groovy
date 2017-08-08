package babel.asset.pipeline

import asset.pipeline.AssetCompiler
import asset.pipeline.fs.FileSystemAssetResolver
import grails.util.Environment
import org.apache.commons.io.FilenameUtils

class BabelTagLib {

    static namespace = "babel"

    def webpack = { attrs, body ->
        if (!attrs.src) {
            throwTagError('missing attribute [src]')
        }

        def crossorigin = ""
        if(attrs.crossorigin) {
            crossorigin = "crossorigin"
        }

        def async = ""
        if(attrs.async) {
            async = " async"
        }
        def src = attrs.src
        def scriptTag
        if (Environment.developmentMode && BabelProcessor.externalServerUsed) { // checking for devMode first prevents the usage in production
            if (BabelProcessor.externalServerRunning && params.getBoolean('restartWebpack', false)) {
                WebpackDevserverBabelifier.killDevServer()
            }
            if (!BabelProcessor.externalServerRunning) {
                // at this point we HAVE TO call the processor once, even if the result is cached
                // so we have to do a little stunt
                def file = new FileSystemAssetResolver("temp", "grails-app/assets/").getAsset(src)
                new BabelProcessor(new AssetCompiler()).process('', file)
            }

            scriptTag = "<script src='http://localhost:$WebpackDevserverBabelifier.port/${FilenameUtils.getBaseName(src)}.js'$crossorigin$async></script>"
            // outputting this will make the browser request the right stuff
        } else {
            scriptTag = asset.javascript(src: src, absolute: true)
        }
        out << scriptTag
    }

}
