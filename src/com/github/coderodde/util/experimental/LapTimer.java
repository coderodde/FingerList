package com.github.coderodde.util.experimental;

public final class LapTimer {
    
    private long lastPushMillis = Long.MAX_VALUE;
    
    public void push() {
        lastPushMillis = System.currentTimeMillis();
    }
    
    public long pop() {
        if (lastPushMillis == Long.MAX_VALUE) {
            throw new IllegalStateException("Push not called yet.");
        }
        
        return System.currentTimeMillis() - lastPushMillis;
    }
    
    @Override
    public String toString() {
        long p = pop();
        return " in " + pop() + " millsecond" + (p != 1 ? "s" : "");
    }
}
