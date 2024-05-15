Pxbrown : Pbrown {
    calcNext { arg cur, step;
        ^cur * rand(step, step.reciprocal)
    }
}

PlazyExpand : Pattern {
    // credit: Scott Carver (I think)
    // multi-channel expand (or truncate) the items yielded by the input pattern
    // to match the size of the largest item in the current Pbind Event.

    *new { arg pattern;
        ^Pfunc({ arg array;
            Routine({
                array.asArray.do(_.yield)
            })
        }) <> pattern;
    }
}

Pmeanrandn : Pattern {
    var <>lo, <>hi, <>n, <>length;
    *new { arg lo=0.0, hi=1.0, n=2, length=inf;
        ^super.newCopyArgs(lo, hi, n, length)
    }
    storeArgs { ^[lo,hi,n,length] }
    embedInStream { arg inval;
        var	localLength = length.value(inval);
        var pat = Pwhite(lo, hi, localLength);
        var result = pat;
        var cleanN = n.clip(1, 100).round.asInteger;
        if(cleanN > 1){
            (cleanN - 1).do{result = result + pat}
        };
        ^(result / cleanN).embedInStream(inval);
    }
}

PwrandTuple : Pattern {
    *new {arg list, weights, repeats = 1;
        ^Ptuple(list, repeats).collect(_.wchoose(weights));
    }
}

PwnrandTuple : Pattern {
    *new {arg list, weights, repeats = 1;
        ^PwrandTuple(list, weights.normalizeSum, repeats);
    }
}

PLFNoise : Pattern {
    *new {arg freq = 1, mul = 1.0, add = 0.0, curve = 'lin', repeats = inf;
        var start, end;
        end = mul.rand2 + add;
        ^Pfin(repeats,
            Pn(Plazy {
                start = end;
                end = mul.rand2 + add;
                Pseg([start, end], freq.reciprocal, curve);
            })
        );
    }

    *range {arg lo = -1.0, hi = 1.0, freq = 1, curve = 'lin', repeats = inf;
        var mul = (hi - lo).abs / 2;
        var add = (hi + lo) / 2;
        ^PLFNoise(freq, mul, add, curve, repeats);
    }

    *exprange {arg lo = 0.01, hi = 1.0, freq = 1, repeats = inf;
        ^PLFNoise(freq, 1.0, 0.0, 'lin', repeats).linexp(-1, 1, lo, hi);
    }

    *curverange {arg lo = 0.01, hi = 1.0, curve = -4, freq = 1, repeats = inf;
        ^PLFNoise(freq, 1.0, 0.0, 'lin', repeats).lincurve(-1, 1, lo, hi, curve);
    }

    *bipolar {arg mul = 1.0, freq = 1, curve = 'lin', repeats = inf;
        ^PLFNoise(freq, mul, 0.0, curve, repeats);
    }

    *unipolar {arg mul = 1.0, freq = 1, curve = 'lin', repeats = inf;
        ^PLFNoise(freq, mul / 2, mul / 2, curve, repeats);
    }
}

Pyes : ListPattern {
    var <>offset;
    *new {arg list, repeats=1, offset=0;
        ^super.new(list, repeats).offset_(offset);
    }

    embedInStream {arg inval;
        var item, offsetValue;
        offsetValue = offset.value(inval);
        if (inval.eventAt('reverse') == true, {
            repeats.value(inval).do({ arg j;
                list.size.reverseDo({ arg i;
                    item = list.wrapAt(i + offsetValue);
                    inval = item.coin.asRest.embedInStream(inval);
                });
            });
        },{
            repeats.value(inval).do({ arg j;
                list.size.do({ arg i;
                    item = list.wrapAt(i + offsetValue);
                    inval = item.coin.asRest.embedInStream(inval);
                });
            });
        });
        ^inval;
    }
}

PfadeInOut : FilterPattern {
    *new {|pattern, fadeInTime = 1.0, holdTime = 0.0, fadeOutTime = 1.0, tolerance = 0.0001|
        var fadein = PfadeIn(pattern, fadeInTime, 0.0, tolerance);
        ^PfadeOut(fadein, fadeOutTime, fadeInTime + holdTime, tolerance);
    }
}

Pgeomrange : Pgeom {
    *new {arg start = 1, end = 0.5, length = 2;
        var grow;
        if (start == 0) {^Pn(start, length)};
        grow = (end / start).pow((length - 1).reciprocal);
        ^Pgeom(start, grow, length);
    }
}

Pwnxrand : Pwrand {
    *new { arg list, weights, repeats=1;
        ^super.new(list, weights, repeats)
    }
    embedInStream {  arg inval;
        var item, wVal;
        var wStr = weights.asStream;
        var thisIndex;
        repeats.value(inval).do({ arg i;
            wVal = wStr.next(inval).copy;
            if(wVal.isNil) { ^inval };
            wVal = wVal.collect{|item, i| if(i == thisIndex, {0}, {item})};
            wVal = wVal.normalizeSum;
            thisIndex = wVal.windex;
            item = list.at(thisIndex);
            inval = item.embedInStream(inval);
        });
        ^inval
    }
}

Pwnrand : Pwrand {
    *new { arg list, weights, repeats=1;
        ^super.new(list, weights, repeats)
    }
    embedInStream {  arg inval;
        var item, wVal;
        var wStr = weights.asStream;
        repeats.value(inval).do({ arg i;
            wVal = wStr.next(inval).copy;
            if(wVal.isNil) { ^inval };
            item = list.at(wVal.normalizeSum.windex);
            inval = item.embedInStream(inval);
        });
        ^inval
    }
    storeArgs { ^[ list, weights, repeats ] }
}

Prand3 : Pattern {
    var <>minimum, <>midpoint, <>maximum, <>repeats, <>tolerance, <>tweak;

    *new {|minimum, midpoint, maximum, repeats=inf, tolerance=0.001, tweak = 1.0|
        ^super.newCopyArgs(minimum, midpoint, maximum, repeats, tolerance, tweak);
    }

    embedInStream {|inval|
        var minStr, midStr, maxStr, tolStr, tweakStr, median, curve, midGuess, cache;
        minStr = minimum.asStream;
        midStr = midpoint.asStream;
        maxStr = maximum.asStream;
        tolStr = tolerance.asStream;
        tweakStr = tweak.asStream;
        repeats.value(inval).do {
            var vals, min, mid, max, tol, twk;
            vals = [minStr.next(inval), midStr.next(inval), maxStr.next(inval),
                tolStr.next(inval), tweakStr.next(inval)];
            vals.do {|val| if(val.isNil) {^inval}};
            #min, mid, max = vals[..2].sort;
            #tol, twk = vals[3..];
            cache = Archive.global.at(\rand3cachedValues);
            if(cache.isKindOf(Dictionary).not or: {cache.size > 1024}) {
                cache = Dictionary.new;
                Archive.global.put(\rand3cachedValues, cache);
            };
            curve = cache.atFail([min, mid, max, tol, twk], {
                var twerk = twk.copy;
                median = min + max / 2;
                curve = 0;
                midGuess = median.lincurve(min, max, min, max, curve);
                while({abs(midGuess - mid) > tol}, {
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
                cache.put([min, mid, max, tol, twk], curve);
                curve;
            });
            inval = rrand(min, max.asFloat).lincurve(min, max, min, max, curve).yield;
        };
        ^inval;
    }
}

Polybjorklund : Pattern {
    var <>k, <>n, <>length, <>offset, <>weight, <>maxval;

    *new { arg k, n, length = inf, offset = 0, weight = 1, maxval = 1;
        ^super.new.k_(k.asArray).n_(n.asArray).length_(length)
        .offset_(offset.asArray).weight_(weight.asArray).maxval_(maxval);
    }

    embedInStream { arg inval;
        var results = ();
        var output = 0;
        weight.asSet.do{|w| results[w] = 0};
        maxItem([k.size, n.size, offset.size, weight.size]).do{|i|
            var thisPolygon = Pbjorklund(k.wrapAt(i), n.wrapAt(i), length, offset.wrapAt(i));
            results[weight.wrapAt(i)] = (results[weight.wrapAt(i)] + thisPolygon).clip(0, maxval);
        };
        results.keysValuesDo{|weight, pattern|
            output = output + (pattern * weight);
        };
        ^output.clip(0, maxval).embedInStream(inval);
    }
}

Polybjorklund2 : Pattern {
    *new { arg k, n, length = inf, offset = 0, weight = 1;
        var polystream, first;
        polystream = Polybjorklund(k, n, length, offset, weight).asStream;
        first = polystream.next;
        ^Pfunc{
            var duration = 1;
            var thisbeat = polystream.next;
            while(
                {thisbeat == 0},
                {
                    thisbeat = polystream.next;
                    duration = duration + 1
                }
            );
            if(first == 0, {first = 1; Rest(duration)}, {duration});
        }
    }
}

Psine { // Credit: Glen Fraser (github: totalgee/bacalao)
    *new { arg periodBars=1, phase=0, mul=1, add=0, repeats=inf;
        // We don't use Ptime (except as fallback when a 'time' key isn't there)
        // so we get elapsed time for the "overall" pattern, not sub-patterns.
        var tPattern = Pif(Pfunc{|ev| ev[\time].notNil }, Pn(Pkey(\time), repeats), Ptime(repeats));
        ^((tPattern / Pfunc{thisThread.clock.beatsPerBar} / periodBars + phase) * 2pi).sin * mul + add;
    }

    *range { arg lo = -1.0, hi=1.0, periodBars=1, phase=0, repeats=inf;
        ^Psine.new(periodBars, phase, 1, 0, repeats).linlin(-1,1, lo,hi);
    }

    *exprange { arg lo = 0.01, hi=1.0, periodBars=1, phase=0, repeats=inf;
        ^Psine.new(periodBars, phase, 1, 0, repeats).linexp(-1,1, lo,hi);
    }

    *curverange { arg lo = 0.01, hi=1.0, curve = -4, periodBars=1, phase=0, repeats=inf;
        ^Psine.new(periodBars, phase, 1, 0, repeats).lincurve(-1,1, lo,hi, curve);
    }
}

Psaw { // Credit: Glen Fraser (github: totalgee/bacalao)
    *new { arg periodBars=1, phase=0, mul=1, add=0, repeats=inf;
        // We don't use Ptime (except as fallback when a 'time' key isn't there)
        // so we get elapsed time for the "overall" pattern, not sub-patterns.
        var tPattern = Pif(Pfunc{|ev| ev[\time].notNil }, Pn(Pkey(\time), repeats), Ptime(repeats));
        ^((tPattern / Pfunc{thisThread.clock.beatsPerBar} + (phase * periodBars)).mod(periodBars) / periodBars * 2 - 1) * mul + add;
    }

    *range { arg lo = -1.0, hi=1.0, periodBars=1, phase=0, repeats=inf;
        ^Psaw.new(periodBars, phase, 1, 0, repeats).linlin(-1,1, lo, hi);
    }

    *exprange { arg lo = 0.01, hi=1.0, periodBars=1, phase=0, repeats=inf;
        ^Psaw.new(periodBars, phase, 1, 0, repeats).linexp(-1,1, lo, hi);
    }

    *curverange { arg lo = 0.01, hi=1.0, curve = -4, periodBars=1, phase=0, repeats=inf;
        ^Psaw.new(periodBars, phase, 1, 0, repeats).lincurve(-1,1, lo, hi, curve);
    }
}

Ptri {
    *new { arg periodBars=1, phase=0, mul=1, add=0, repeats=inf;
        var start;
        phase = (phase + 0.25).wrap(0.0, 1.0);
        start = phase * 2;
        ^Pfin(repeats, Pseg([0.0, 2.0] + start, periodBars * Pfunc{thisThread.clock.beatsPerBar}, \lin, inf).fold(0.0, 1.0).linlin(0, 1, -1, 1)) * mul + add
    }

    *range { arg lo = -1.0, hi=1.0, periodBars=1, phase=0, repeats=inf;
        ^Ptri.new(periodBars, phase, 1, 0, repeats).linlin(-1, 1, lo, hi);
    }

    *exprange { arg lo = 0.01, hi=1.0, periodBars=1, phase=0, repeats=inf;
        ^Ptri.new(periodBars, phase, 1, 0, repeats).linexp(-1, 1, lo, hi);
    }

    *curverange { arg lo = -1.0, hi=1.0, curve = -4, periodBars=1, phase=0, repeats=inf;
        ^Ptri.new(periodBars, phase, 1, 0, repeats).lincurve(-1, 1, lo, hi, curve);
    }
}

Ppulse {
    *new { arg periodBars=1, phase=0, width=0.5, mul=1, add=0, repeats=inf;
        var tPattern = Pif(Pfunc{|ev| ev[\time].notNil }, Pn(Pkey(\time), repeats), Ptime(repeats));
        var placeInPeriod = (tPattern / Pfunc{thisThread.clock.beatsPerBar} + (phase * periodBars)).mod(periodBars) / periodBars;
        var result = Pif(placeInPeriod < width, 1, -1) * mul + add;
        ^result;
    }

    *range { arg lo = -1.0, hi=1.0, periodBars=1, phase=0, width=0.5, repeats=inf;
        ^Ppulse.new(periodBars, phase, width, 1, 0, repeats).linlin(-1,1, lo, hi);
    }
}
