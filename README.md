[![Build Status](https://travis-ci.org/errbuddy/babel-asset-pipeline.svg?branch=master)](https://travis-ci.org/errbuddy/babel-asset-pipeline)

# babel-asset-pipeline
babel.js transformation for asset-pipeline

# usage
simply add
```
compile 'net.errbuddy.plugins:babel-asset-pipeline:2.0.6'
```
to dependencies

and

The plugin will *ONLY process *.es6 and *.jsx files if not configured to also process *.js files .

# Configuration

All configuration has to be done under grails.assets.babel. if you experience issues (e.g. configuration options not being picked up when building war files) you should add the configration to build.gradle too.


```
grails:
    assets:
        babel:
            enabled: true,
            processor: 'direct'
            processJsFiles: false
            options: {blacklist: ['useStrict'], loose: 'all'}
```

## Options

```
grails.assets.babel.enabled = true // boolean
```
default to true. enables the plugin

```
grails.assets.babel.processJsFiles = false // boolean
```
defaults to false. Whether to process JsAssetFiles (.js) too. *By default to Processor only touches Es6AssetFiles (.es6)!*

```
grails.assets.babel.processor = 'direct'
```
defaults to direct which uses the "old" rhino->babel v5 Processor. see Processors section for other options

```
grails.assets.babel.options = [blacklist: ['useStrict'], loose: 'all'] // babel transfom options. see https://babeljs.io/docs/usage/options/ for more information
```
defaults to null. A Map of options passed to babels transform method. see https://babeljs.io/docs/usage/options/ for possible values. *only used for "direct" processor, otherwise ignored*

## Modules
```
grails.assets.babel.options = [modules: 'amd', moduleIds: true]
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

## Processors
since version 2.0 babel-asset-pipeline comes with a new Processor which uses webpack to transpile es6 code. There is some really important things to keep in mind when switching this on:

* the asset-pipeline dependency managment is ignored completly but as webpack is used you can simply require or import stuff
* you have to setup your project appropriately (see section about Webpack on how to do it)
* you should use the babel:webpack tag see https://github.com/peh/babel-test-app/blob/master/grails-app/views/index.gsp
* the "options" Configuration option is ignored completly as all this is handled in the webpack config

## Webpack

### Setup
To use webpack you will need some prerequesites. Firstly you need to have node installed.
If you do not want to install it manually or you don't want to manage node yourself [gradle-node-plugin](https://github.com/srs/gradle-node-plugin/) should exactly be what you are looking for.
The only thing you will have to manually do in this case is configure the node executable (see config section below).

Secondly you have to install gradle-babel-asset-pipeline-helper with npm. this can be done by running `npm install --save gradle-babel-asset-pipeline-helper` (you may need to `npm init` first). *If you are using gradle-node-plugin you could copy https://github.com/peh/babel-test-app/blob/master/package.json to your project root and simply run `gradle npmInstall`*
gradle-babel-asset-pipeline-helper depends on everything that you will need to use webpack in your grails app and also comes with two default webpack configurations and run scripts that are being executed by the webpack processors.

Lastly you should use &lt;babel:webpack src="file.js" /&gt; to reference your js files. This will come in handy if you are using the webpack dev server (otherwise it is not needed!)


### Restarting the WebpackDevserver
In some cases it is needed to restart the devserver. Simply append ?restartWebpack=true to the url you are requesting. The Taglib will take care of killing and restarting the webpack devserver.

### Configuration
There are a few additional configuration options you might need to touch
```
grails.assets.babel.processor = "webpack" // or "webpack-dev-server"
```
If you want to use Hot Module Reloading you can use webpack-dev-server. *You should only do that in development environments* for production you should always use webpack!


```
grails.assets.babel.nodeExec = "/usr/local/bin/node"
```
The node exectuable default to "/usr/local/bin/node". If you are on Windows or you are using [gradle-node-plugin](https://github.com/srs/gradle-node-plugin/) ( or[nvm](https://github.com/creationix/nvm)) you will have to change this to point to your local node exectuable

For gradle-node-plugin users, node is installed in your projects local .gradle directory.


```
grails.assets.babel.externalWebpackConfig = null
```
This one is for advanced users only! By defining a different webpack config location you are overwriting the default configuration taken from gradle-babel-asset-pipeline-helper.
The default config is build using the [buildConfig function](https://github.com/peh/gradle-babel-asset-pipeline-helper/blob/master/babel-webpack.js#L29).
If you want to use a custom one you can define a file here which is required by the package script.
Your configuration should either be a webpack config object (see webpack documenation) or a function (which is recommended) which is then called with the same parameters the default build function is called with.
The default function should give you a fair idea on what the parameters are and how to use it properly.
This is usable for webpack-dev-server to but here it is important to stick closely to [the default buildConfig()](https://github.com/peh/gradle-babel-asset-pipeline-helper/blob/master/babel-webpack-dev-server.js#L38) as HMR is breaking pretty easy when something is not configured right.


### Plugin Development

If you want to help extending this plugin you can get setup in minutes by:

```
git clone https://github.com/errbuddy/babel-asset-pipeline.git
cd babel-asset-pipeline
./gradlew npmInstall
```

Now your local environment has the required nodeJs version installed and you can start hacking. Feel free to create a PR for your changes