+ SimpleNumber {
    tempodur { // Credit: Eli Fieldsteel
        ^(60 / this)
    }

    durtempo { // Credit: Eli Fieldsteel
        ^(60 / this)
    }

    play { // Credit: Eli Fieldsteel
        arg out=0;
        {Out.ar(out, SinOsc.ar(this.midicps, mul: 0.4!2) * Env.perc(0.01, 0.3).kr(2))}.play;
    }

    exprand2 {|median|
        var that = exp(log(this) + (2 * log(median / this)));
        ^exprand(this, that)
    }

    randcurve {|aNumber, curve = 0|
        ^rrand(this, aNumber.asFloat).lincurve(this, aNumber, this, aNumber, curve)
    }

    rand3 {|mid, max, tolerance=0.001, tweak = 1.0|
        var min, median, curve, midGuess, cache;
        #min, mid, max = [this, mid, max].sort;
        cache = Archive.global.at(\rand3cachedValues);
        if(cache.isKindOf(Dictionary).not or: {cache.size > 1024}) {
            cache = Dictionary.new;
            Archive.global.put(\rand3cachedValues, cache);
        };
        curve = cache.atFail([min, mid, max, tolerance, tweak], {
            var twerk = tweak.copy;
            median = min + max / 2;
            curve = 0;
            midGuess = median.lincurve(min, max, min, max, curve);
            while({abs(midGuess - mid) > tolerance}, {
                var curveUp, curveDown, upGuess, downGuess;
                curveUp = curve + twerk;
                upGuess = median.lincurve(min, max, min, max, curveUp);
                if(abs(upGuess - mid) < abs(midGuess - mid)) {curve = curveUp} {
                    curveDown = curve - twerk;
                    downGuess = median.lincurve(min, max, min, max, curveDown);
                    if(abs(downGuess - mid) < abs(midGuess - mid)) {curve = curveDown} {
                        twerk = twerk / 2;
                    }
                };
                midGuess = median.lincurve(min, max, min, max, curve);
            });
            cache.put([min, mid, max, tolerance, tweak], curve);
            curve;
        });
        ^rrand(min, max.asFloat).lincurve(min, max, min, max, curve);
    }

    /*
    // this doesn't work as intended
    randcurve2 {|median, maximum|
        var a, b, c, m, i, j, k, r;
        #a, b, c = [this, median, maximum].sort;
        m = (a + c) / 2;
        j = ((c * m.squared) - (a * m.squared) - (c * a.squared) + (a * c.squared) + (b * a.squared) - (b * c.squared)) /
        ((m * a.squared) + (a * c.squared) - (m * c.squared) - (a * m.squared) - (c * a.squared) + (c * m.squared));
        i = (a - b - ((a - m) * j)) / (a.squared - m.squared);
        k = a * (1 - (i * a) - j);
        // [i, j, k].postln;
        r = rrand(a, c.asFloat);
        ^i * r.squared + (j * r) + k;
    }
    */
}

+ Env { // Credit: Eli Fieldsteel
	*rand {
		arg numSegs, dur=1, bipolar=true;
		var env, levels, times, curves, minLevel;
        levels = [0] ++ ({rrand(-1.0,1.0)}!(numSegs - 1)) ++ [0];
		minLevel = bipolar.asInteger.neg;
		levels = levels.normalize(minLevel, 1);
		times = {exprand(1,10)}!numSegs;
		times = times.normalizeSum * dur;
		curves = {rrand(-4.0,4.0)}!numSegs;
		env = this.new(levels, times, curves);
		^env;
	}

}

+ Window { // Credit: Eli Fieldsteel
	*blackout {
		var win;
		win = Window.new("", Window.screenBounds, false, false);
		win.view.background_(Color.black);
		win.view.keyDownAction_({
			arg view, char, mod, uni;
			if(uni == 27, {win.close});
		});
		win.front;
	}

}
