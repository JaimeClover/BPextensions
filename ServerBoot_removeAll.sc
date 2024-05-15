+ ServerBoot {
	*removeAll {
        "This breaks NodeProxy and FreqScope. Recompile class library to fix them".warn;
		super.removeAll;
	}
}