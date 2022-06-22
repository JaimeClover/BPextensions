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

    *exprange {arg lo = 0.01, hi = 1.0, freq = 1, curve = 'exp', repeats = inf;
        ^PLFNoise(freq, 1.0, 0.0, curve, repeats).linexp(-1, 1, lo, hi);
    }

    *curverange {arg lo = 0.01, hi = 1.0, curve = -4, freq = 1, envcurve = 'lin', repeats = inf;
        ^PLFNoise(freq, 1.0, 0.0, envcurve, repeats).lincurve(-1, 1, lo, hi, curve);
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
        ^((tPattern / Pfunc{thisThread.clock.beatsPerBar} + phase).mod(periodBars) / periodBars * 2 - 1) * mul + add;
    }

    *range { arg lo = -1.0, hi=1.0, periodBars=1, phase=0, repeats=inf;
        ^Psaw.new(periodBars, phase, 1, 0, repeats).linlin(-1,1, lo,hi);
    }

    *exprange { arg lo = 0.01, hi=1.0, periodBars=1, phase=0, repeats=inf;
        ^Psaw.new(periodBars, phase, 1, 0, repeats).linexp(-1,1, lo,hi);
    }

    *curverange { arg lo = 0.01, hi=1.0, curve = -4, periodBars=1, phase=0, repeats=inf;
        ^Psaw.new(periodBars, phase, 1, 0, repeats).lincurve(-1,1, lo,hi, curve);
    }
}
