package com.fredplugins.attacktimer;

public enum AttackType
{
    CRUSH,
    SLASH,
    STAB,
    RANGED,
    MAGIC,
    NONE;

    public boolean IsMelee()
    {
        return this.equals(CRUSH) || this.equals(SLASH) || this.equals(STAB);
    }
}