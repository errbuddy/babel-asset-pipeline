[![Build Status](https://travis-ci.org/errbuddy/babel-asset-pipeline.svg?branch=master)](https://travis-ci.org/errbuddy/babel-asset-pipeline)

# babel-asset-pipeline
babel.js transformation for asset-pipeline

# usage
simply add
```
compile 'net.errbuddy.plugins:babel-asset-pipeline:2.0.0'
```
to dependencies 

and 

```
maven { url "http://dl.bintray.com/errbuddy/plugins" }
```


to you repositories in build.gradle.
The plugin will *ONLY process .es6 and .jsx* files if not otherwise told.

# important notice to 2.0.0

2.0.0 changed alot! Your old configuration should still work but before upgrading in production you should check whether everything is still working

# Configuration
```
grails.assets.babel.enabled = true // boolean
```
default to true. enables the plugin

```
grails.assets.babel.processJsFiles = false // boolean
```
defaults to false. Whether to process JsAssetFiles (.js) too. *By default to Processor only touches Es6AssetFiles (.es6)!*

```
grails.assets.babel.options = [blacklist: ['useStrict'], loose: 'all'] // babel transfom options. see https://babeljs.io/docs/usage/options/ for more information
```
defaults to null. A Map of options passed to babels transform method. see https://babeljs.io/docs/usage/options/ for possible values

```
grails.assets.babel.processor = 'node'
```
defaults to null which enabled the built in babel5 Processor. Set to "node" if you want to enable the NodeProcessor which requires to have node and some dependencies installed in your project. Check the node section to see how to setup everything


## Modules
```
grails.asset.babel.options = [modules: 'amd', moduleIds: true]
```
When the `moduleIds` option is set the plugin provides Babel with a `moduleId` for each file. The ID is the relative path of the file inside `grails-app/assets/javascripts` with the file extension removed. 

```
e.g.
# File Path:
grails-app/assets/javascripts/foo/bar.js
# Generated moduleId:
foo/bar
```

Note: Explicit module IDs are not available when generating CommonJS modules.

# Node Setup

To use the NodeBabelifier you have to setup your project first. You should already have node installed (if not i suggest [nvm](https://github.com/creationix/nvm)).
Then you should initiate a node project in your applications root directory
```
npm init
```

Now you should install the needed dependencies in your package.json file
```
npm i --save-dev browserify babelify babel-preset-es2015 babel-preset-react
```

As a last step you need to tell your application where to find node and browserify there is two ways either you define `NODE_PATH` and `BROWSERIFY_PATH` in you environment e.g.:
```
$ export NODE_PATH=/Users/philipp/.nvm/versions/node/v5.0.0/bin/node
$ export BROWSERIFY_PATH=node_modules/browserify/bin/cmd.js
```
or you can set `nodePath` and `browserifyPath` in your asset.babel configuration. e.g. in `applicaiton.yml`:
```
grails:
  assets:
    babel:
      enabled: true,
      processor: 'node'
      processJsFiles: true
      nodePath: '/Users/philipp/.nvm/versions/node/v5.0.0/bin/node'
      browserifyPath: 'node_modules/browserify/bin/cmd.js'
      options:
        blacklist: ['useStrict']
        loose: 'all'
```
One of the two methods have to be used otherwise an exception will be thrown!
