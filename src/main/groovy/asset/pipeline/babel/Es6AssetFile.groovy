package asset.pipeline.babel

import asset.pipeline.AbstractAssetFile

import java.util.regex.Pattern

class Es6AssetFile extends AbstractAssetFile {
    static final String contentType = ['application/javascript','application/x-javascript','text/javascript']
    static extensions = ['es6']
    static compiledExtension = 'js'
    static processors = [BabelProcessor]
    Pattern directivePattern = ~/(?m)^\/\/=(.*)/
}