+ NodeProxy {
    levelLine {|from, to, beats, faderSlot=10|
        this.put(faderSlot, \filter -> {|in| in * Line.kr(from, to, beats * clock.beatDur)});
    }
}
