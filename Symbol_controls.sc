+ Symbol {
    controls {|verbose = true|
        var synthdesc, controlNames, controlDict;
        synthdesc = SynthDescLib.global.at(this);
        synthdesc ?? {verbose.if {"no synthdef named %".format(this).postln}; ^nil};
        controlNames = synthdesc.controlNames.sort;
        controlDict = synthdesc.controlDict;
        verbose.if {
            controlNames.do {|cname|
                [cname, controlDict.at(cname).rate, controlDict.at(cname).defaultValue].postln
            }
        };
        ^controlDict;
    }
}
    