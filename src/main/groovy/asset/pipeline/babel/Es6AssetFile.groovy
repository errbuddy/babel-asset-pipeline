package asset.pipeline.babel

import asset.pipeline.AbstractAssetFile

import java.util.regex.Pattern

class Es6AssetFile extends AbstractAssetFile {
    static final List<String> contentType = ['application/javascript', 'application/x-javascript','text/javascript']
    static List<String> extensions = ['es6']
    static String compiledExtension = 'js'
    static processors = [BabelProcessor]
    Pattern directivePattern = ~/(?m)^\/\/=(.*)/
}