package asset.pipeline.babel

import asset.pipeline.AbstractAssetFile

import java.util.regex.Pattern

class JsxAssetFile extends AbstractAssetFile {
    static final List<String> contentType = ['text/jsx', 'application/javascript','application/x-javascript','text/javascript']
    static List<String> extensions = ['jsx']
    static String compiledExtension = 'js'
    static processors = [BabelProcessor]
    Pattern directivePattern = ~/(?m)^\/\/=(.*)/
}