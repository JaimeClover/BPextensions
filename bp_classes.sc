VerbEF {

    *ar {
        arg in, dec=3.5, mix=0.08, lpf1=2000, lpf2=6000, predel=0.025, mul=1, add=0;
        var dry, wet, sig;
        dry = in;
        wet = in;
        wet = DelayN.ar(wet, 0.5, predel.clip(0.0001,0.5));
        wet = 16.collect{
            var temp;
            temp = CombL.ar(
                wet,
                0.1,
                LFNoise1.kr({ExpRand(0.02,0.04)}!2).exprange(0.02,0.099),
                dec
            );
            temp = LPF.ar(temp, lpf1);
        }.sum * 0.25;
        8.do{
            wet = AllpassL.ar(
                wet,
                0.1,
                LFNoise1.kr({ExpRand(0.02,0.04)}!2).exprange(0.02,0.099),
                dec
            );
        };
        wet = LeakDC.ar(wet);
        wet = LPF.ar(wet, lpf2, 0.5);
        sig = dry.blend(wet, mix);
        sig = sig * mul + add;
        ^sig;
    }

}

ColorEF {
    var <color, <window;

    *new { ^super.new.make; }

    close { this.window.close; }

    make {
        var palette;

        color = Color.new(0.8,0.8,0.8);
        window = Window.new("ColorEF", Rect(50,50,210,160)).front;

        Knob.new(window, Rect(10,10,40,40))
        .mode_(\vert)
        .value_(color.red)
        .color_([Color.red])
        .action_({
            arg view;
            color.red_(view.value);
            palette.background_(color)
        });

        Knob.new(window, Rect(10,60,40,40))
        .mode_(\vert)
        .value_(color.green)
        .color_([Color.green])
        .action_({
            arg view;
            color.green_(view.value);
            palette.background_(color)
        });

        Knob.new(window, Rect(10,110,40,40))
        .mode_(\vert)
        .value_(color.blue)
        .color_([Color.blue])
        .action_({
            arg view;
            color.blue_(view.value);
            palette.background_(color)
        });

        palette = View.new(window, Rect(60,10,140,140))
        .background_(color);
    }

}

ControlPoster {

    *new {
        arg synthname = \default, class = "Pbind";
        var prefix = switch(
            class.toLower,
            "pbind", {"Pbind(*[instrument: %%,".format("\\", synthname)},
            "pmono", {"Pmono(*[%%,".format("\\", synthname)},
            "pmonoartic", {"PmonoArtic(*[%%,".format("\\", synthname)},
            "synth", {"Synth(%%, [".format("\\", synthname)}
        );
        prefix.postln;
        SynthDescLib.global.synthDescs.at(synthname).controls.do{
            arg control;
            var name = control.name;
            var val = control.defaultValue;
            "\t%: %,".format(name, val).postln;
        };
        "]);".postln;
    }
}

BarberPole {

    *ar {|in, sweep_rate = 0.01, db_cut = (-50),
        fm_rate = 0.1, fm_midinotes = 0,
        am_rate = 0.1, am_min = (-6), am_max = (-6),
        rq_rate = 0.1, rq_min = 0.1, rq_max = 0.1|
        var freq_phases, fm, freqs, am, atten_phases, attenuations, rq, sig, dome;
        freq_phases = ((0..9) / 5 + 1).mod(2);
        fm = SinOsc.ar(fm_rate * {ExpRand(0.5, 2)}!10, {Rand(0, 2pi)}!10).bipolar(fm_midinotes).midiratio;
        freqs = (LFSaw.ar(sweep_rate, freq_phases).linexp(-1, 1, 20, 20 * (2**10)) * fm).clip(20, 20000);
        atten_phases = ((0..9) / 10 * pi).mod(2pi);
        dome = SinOsc.ar(sweep_rate / 2, atten_phases).abs;
        am = SinOsc.ar(am_rate * {ExpRand(0.5, 2)}!10, {Rand(0, 2pi)}!10).bipolar(dome.linexp(0, 1, am_min, am_max));
        attenuations = dome.linexp(0, 1, -0.0001, db_cut);
        rq = SinOsc.ar(rq_rate * {ExpRand(0.5, 2)}!10, {Rand(0, 2pi)}!10).range(rq_min, rq_max);
        sig = in;
        freqs.do{|freq, i| sig = BPeakEQ.ar(sig, freq, rq, attenuations[i] + am[i])};
        // sig = BPF.ar(sig, 1200, 4);
        // sig = OnePole.ar(sig, 0.9);
        sig = Limiter.ar(sig);
        ^sig
    }
}

BarberPole2 {

    *ar {|in, rate = 0.01, fm_rate = 0.1, fm_amt = 0,
        am_rate = 0.1, am_amt = 0, min_db = -3, max_db = -50,
        rq_rate = 0.1, rq_min = 0.1, rq_max = 0.1|
        var sig, freqs, fm, rq, attenuations, freq_phases, atten_phases;
        sig = in;
        // sig = sig + Splay.ar(Saw.ar(80 * ((-2..2) * \detun.kr(0.1)).midiratio, 0.6!2));

        freq_phases = (
            (0..9) / 5 // phase offsets create octave invervals
            + 1 // start at the bottom of the sweep
        ).mod(2);

        fm = SinOsc.ar(fm_rate * ({ExpRand(0.5, 2)}!10), {Rand(0, 2pi)}!10).bipolar(fm_amt).midiratio; // modulate frequency of each peak (optional)

        // 10 sweeps, exponentially increasing:
        freqs = (LFSaw.ar(rate, freq_phases).linexp(-1, 1, 20, 20 * (2**10)) * fm).clip(20, 20000);

        atten_phases = (
            (0..9) / 10 * pi // phase offsets align attenuations with corresponding freqs
            + SinOsc.ar(am_rate * ({ExpRand(0.5, 2)}!10), {Rand(0, 2pi)}!10).bipolar(am_amt) // modulate amplitude of each peak (optional)
        ).mod(2pi);

        // 10 attenuations, using sine shape to get maximum attenuation in the midrange:
        attenuations = SinOsc.ar(rate / 2, atten_phases).abs.linexp(0, 1, min_db, max_db);

        rq = SinOsc.ar(rq_rate * ({ExpRand(0.5, 2.0)}!10), {Rand(0, 2pi)}!10).range(rq_min, rq_max);

        // apply attenuations for each freq, in a cascade:
        freqs.do{|freq, i|
            sig = BPeakEQ.ar(sig, freq, rq[i], attenuations[i]); // cut the octaves
            // sig = BPeakEQ.ar(sig, freq * 0.75, rq, attenuations[i].neg); // boost the fifths (optional)
        };

        // trim the highs and lows for more pronounced psychological effect (optional):
        // sig = BPF.ar(sig, 1200, 4);
        // sig = OnePole.ar(sig, 0.9);

        sig = Limiter.ar(sig);
        ^sig;
    }
}

LPF1pole {
    *ar {|in, freq=1000, mul=1.0, add=0.0|
        ^OnePole.ar(in, exp(-2pi * freq * SampleDur.ir), mul, add);
    }
}

HPF1pole {
    *ar {|in, freq=1000, mul=1.0, add=0.0|
        ^(in - OnePole.ar(in, exp(-2pi * freq * SampleDur.ir), mul, add));
    }
}

Schroeder2 {
    *ar {|in, roomSize=1.0, lpf=8000, hpf=100|
        var output, delrd, sig, deltimes;
        delrd = LocalIn.ar(4);
        output = in + delrd[[0,1]];
        // Cross-fertilise the four delay lines with each other:
        sig = [output[0]+output[1], output[0]-output[1], delrd[2]+delrd[3], delrd[2]-delrd[3]];
        sig = [sig[0]+sig[2], sig[1]+sig[3], sig[0]-sig[2], sig[1]-sig[3]];
        // Attenutate the delayed signals so they decay:
        sig = sig * [0.4, 0.37, 0.333, 0.3] * roomSize;
        deltimes = [101, 143, 165, 177] * 0.001 - ControlDur.ir;
        LocalOut.ar(DelayN.ar(sig, deltimes, deltimes));
        ^LeakDC.ar(LPF.ar(HPF.ar(delrd[[0,1]], hpf), lpf))
    }
}