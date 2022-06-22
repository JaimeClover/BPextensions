+ SequenceableCollection {
// supports a variation of Mikael Laurson's rhythm list RTM-notation.
	convertRhythmBP {
		var list, tie;
		list = List.new;
		tie = this.convertOneRhythmBP(list);
		if (tie > 0.0, { list.add(tie) });  // check for tie at end of rhythm
		^list
	}
	sumRhythmDivisionsBP {
		var sum = 0;
		this.do {|beats|
			sum = sum + abs(if (beats.isSequenceableCollection) {
                beats[0];// / (beats[2] ? 1);
			}{
				beats
			});
		};
		^sum
	}
	convertOneRhythmBP { arg list, tie = 0.0, stretch = 1.0;
		var beats, divisions, repeats;
		#beats, divisions, repeats = this;
		repeats = repeats ? 1;
		stretch = stretch * beats / divisions.sumRhythmDivisionsBP / repeats;
		repeats.do({
			divisions.do { |val|
				if (val.isSequenceableCollection) {
					tie = val.convertOneRhythmBP(list, tie, stretch)
				}{
					val = val * stretch;
					if (val > 0.0) {
						list.add(val + tie);
						tie = 0.0;
					}{
						tie = tie - val
					};
				};
			};
		});
		^tie
	}
}