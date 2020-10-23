package com.shediz.spamdetect;

import java.io.Serializable;

class MutableInt implements Serializable
{
    int value = 1;
    public void increment ()
    {
        ++value;
    }
    public int get ()
    {
        return value;
    }
}