BitCrush {
    *ar {arg in, nbits=16;
        ^in.round(2.pow(1 - nbits.clip(2, 16)));
    }
}