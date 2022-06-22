+ Collection {
    onlyFactors {|factorList = 2|
        factorList = factorList.asArray;
        ^this.select{|i| i.factors.asSet.every{|j| factorList.includes(j)}}
    }

    monoFactors {
        ^this.select{|i| i.factors.size == i.factors.asSet.size}
    }
}