package com.shediz.spamdetect;

import java.io.Serializable;
import java.util.HashMap;

public class Model implements Serializable
{
    private final HashMap<String, MutableInt> spamDistribution;

    private final HashMap<String, MutableInt> hamDistribution;

    public Model(HashMap<String, MutableInt> spamDistribution, HashMap<String, MutableInt> hamDistribution)
    {
        this.spamDistribution = spamDistribution;
        this.hamDistribution = hamDistribution;
    }

    public HashMap<String, MutableInt> getSpamDistribution()
    {
        return spamDistribution;
    }

    public HashMap<String, MutableInt> getHamDistribution()
    {
        return hamDistribution;
    }
}
