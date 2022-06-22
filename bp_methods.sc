+ SimpleNumber { // Credit: Eli Fieldsteel
    tempodur {
        ^(60 / this)
    }

    durtempo {
        ^(60 / this)
    }

    play {
        arg out=0;
        {Out.ar(out, SinOsc.ar(this.midicps, mul: 0.4!2) * Env.perc(0.01, 0.3).kr(2))}.play;
    }
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
