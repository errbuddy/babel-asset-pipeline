import Bar from './bar.es6'
const _ = require('lodash');

class Foo {

    static foo(){
        return "foo"
    }

    static bar(){
        return new Bar().bar()
    }
}