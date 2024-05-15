Compressor {
    *ar {|sig, sidechain, ratio = 4, threshold = -40, atk = 0.01, rel = 0.1, gain = 3, lookahead = 0|
        var amp, excess, cut;
        amp = Amplitude.ar(sidechain).ampdb.lagud(atk, rel);
        excess = (amp - threshold).max(0.0);
        cut = excess * (ratio.reciprocal - 1);
        sig = DelayN.ar(sig, lookahead, lookahead) * cut.dbamp * gain.dbamp;
        ^sig;
    }
}

Multiband4 {
    *ar {|sig, ratio = 4, threshold = -40, atk = 0.01, rel = 0.1, gain = 3, freqs = #[112.5, 632.5, 3556.6]|
        var amp, excess, cut;
        sig = BandSplitter4.ar(sig, *freqs);
        amp = Amplitude.ar(sig).lagud(atk, rel).ampdb;
        excess = (amp - threshold).max(0.0);
        cut = excess * (ratio.reciprocal - 1);
        sig = sig * cut.dbamp * gain.dbamp;
        ^sig.sum;
    }
}

Multiband8 {
    *ar {|sig, ratio = 4, threshold = -40, atk = 0.01, rel = 0.1, gain = 3, freqs = #[47.4, 112.5, 266.7, 632.5, 1500.0, 3556.6, 8433.9]|
        var amp, excess, cut;
        sig = BandSplitter8.ar(sig, *freqs);
        amp = Amplitude.ar(sig).lagud(atk, rel).ampdb;
        excess = (amp - threshold).max(0.0);
        cut = excess * (ratio.reciprocal - 1);
        sig = sig * cut.dbamp * gain.dbamp;
        ^sig.sum;
    }
}