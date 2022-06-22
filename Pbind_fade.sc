+ Pattern {
    fadein {|fadeTime = 1.0, holdTime = 0.0, tolerance = 0.0001|
        ^PfadeIn(this, fadeTime, holdTime, tolerance);
    }

    fadeout {|fadeTime = 1.0, holdTime = 0.0, tolerance = 0.0001|
        ^PfadeOut(this, fadeTime, holdTime, tolerance);
    }

    fadeinout {|fadeInTime = 1.0, holdTime = 0.0, fadeOutTime = 1.0, tolerance = 0.0001|
        ^PfadeInOut(this, fadeInTime, holdTime, fadeOutTime, tolerance);
    }
}