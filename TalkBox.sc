TalkBox {
    *ar{|sig, talkrate = 5, maxrq = 0.5|
        var lo, lomid, himid, hi;
        lo = RLPF.ar(sig, LFNoise2.ar(talkrate).exprange(100, 100*3), LFNoise2.ar(0.5).range(0.1, maxrq));
        lomid = BPF.ar(sig, LFNoise2.ar(talkrate).exprange(1000/3, 1000), LFNoise2.ar(0.5).range(0.1, maxrq));
        himid = BPF.ar(sig, LFNoise2.ar(talkrate).exprange(10000/9, 10000/3), LFNoise2.ar(0.5).range(0.1, maxrq));
        hi = RHPF.ar(sig, LFNoise2.ar(talkrate).exprange(100000/27, 100000/9), LFNoise2.ar(0.5).range(0.1, maxrq));
        ^lo * -5.dbamp + (lomid * (5.dbamp)) + (himid * (0.dbamp)) + (hi * (-5.dbamp));
    }
}