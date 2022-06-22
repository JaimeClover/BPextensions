+ SequenceableCollection {
    around {|number|
        ^(2 * number - this)
    }

    q {|repeats = inf, offset = 0|
        ^Pseq(this, repeats, offset);
    }

    p {^Pbind(*this)}

    play {|clock, protoEvent, quant|
        ^Pbind(*this).play(clock, protoEvent, quant);
    }
}

+ Collection {
    onlyFactors {|factorList = 2|
        factorList = factorList.asArray;
        ^this.select{|i| i.factors.asSet.every{|j| factorList.includes(j)}}
    }

    monoFactors {
        ^this.select{|i| i.factors.size == i.factors.asSet.size}
    }
}