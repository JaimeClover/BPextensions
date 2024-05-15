+ Ndef {
    slotAsNdef { |index, toKey ...args|
        var ndef;
        if(toKey.isNil or: { key == toKey }) {
            toKey = ("_" ++ key ++ index).asSymbol;
            // this.class.dictFor(server).envir.at(toKey) !? { "Overwriting % key".format(toKey).warn };
        };
        ndef = this.class.new(toKey).put(nil, this.at(index).source, extraArgs: args);
        this.put(index, ndef.asUGenInput);
    }

    set1 { |index ...args|
        this.slotAsNdef(index, nil, *args);
    }

    unset1 { |index ... keys |
        var ndef;
        if (index.isNil) { ^this };
        ndef = this.at(index);
        if (ndef.isKindOf(Ndef)) {
            this.put(index, ndef.source)
        };
        ndef.end;
    }
}

/*+ NodeProxy {
    unset { | ... keys |
		var bundle = MixedBundle.new;
		this.unsetToBundle(bundle, keys);
        bundle.schedSend(server, clock ? TempoClock.default, quant);
		/*if(bundle.notEmpty) {
			server.listSendBundle(server.latency, bundle)
		};*/
	}
}*/