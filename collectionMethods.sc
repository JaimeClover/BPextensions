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