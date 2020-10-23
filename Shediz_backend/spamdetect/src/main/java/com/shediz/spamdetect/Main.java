package com.shediz.spamdetect;

public class Main
{
    public static void main(String... args)
    {
        SpamFilter spamFilter = new SpamFilter();
        spamFilter.buildModel();
        spamFilter.loadModel();
        spamFilter.testFilter();
    }
}
