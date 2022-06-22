Echo {
    *ar {arg in, dec=0.2, maxdelay=0.2, delay=0.2, tail=3;
        ^CombN.ar(Decay.ar(in, dec, WhiteNoise.ar), maxdelay, delay, tail);
    }
}