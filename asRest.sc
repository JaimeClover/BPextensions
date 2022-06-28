+ Collection {
    asRest {
        ^this.collect(_.asRest)
    }
}

+ Object {
    asRest {
        if (this.isFloat) {if (this == 0.0) {^Rest()} {^this}};
        if (this.asInteger == 0) {^Rest()} {^this}
    }
}

+ AbstractFunction {
    asRest { ^this.composeUnaryOp('asRest') }
}