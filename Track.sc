Track : Ndef {
    var <>pattern, <>inbus;

    *new {|name, pattern, clock, quant=0, fadeTime=0.0, numInputs=2, numOutputs=2|
        var res = super.new(name, {DC.ar(0!2)});
        res.ar(numOutputs);
        res.initTrack(clock, quant, fadeTime, numInputs, numOutputs);
        res.addPattern(pattern);
        ^res;
    }

    initTrack {|clock, quant, fadeTime, numInputs, numOutputs|
        this.inbus = Bus.audio(defaultServer, numInputs);
        this.clock = clock ? TempoClock.default;
        this.quant = quant;
        this.fadeTime = fadeTime;
        this.ar(numOutputs);
        this.put(0, {In.ar(this.inbus, numInputs)});
    }

    addPattern {|pattern|
        this.pattern = Pchain(pattern, Pbind(\out, this.inbus));
    }

   /* make {|name, server, pattern, clock, quant=0, fadeTime=0.0, numInputs=2, numOutputs=2|
        this.inbus = Bus.audio(server, numInputs);
        this.clock = clock ? TempoClock.default;
        this.quant = quant;
        this.ndef = Ndef(name);
        this.ndef.ar(numOutputs);
        this.ndef.clock = this.clock;
        this.ndef.quant = quant;
        this.ndef.fadeTime = fadeTime;
        this.ndef[0] = {In.ar(this.inbus, numInputs)};
        this.pattern = pattern <> Pbind(\out, this.inbus);
    }*/

    play {|fadeTime|
        super.play(fadeTime: fadeTime ? this.fadeTime);
        this.pattern.play(this.clock, quant: this.quant);
    }
}