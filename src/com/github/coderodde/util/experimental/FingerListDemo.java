package com.github.coderodde.util.experimental;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class FingerListDemo {

    private static final int NUMBER_OF_ADDS = 20_000;
    private static final int NUMBER_OF_GETS = 50_000;
    private static final int FINGERS = 100;
    
    public static void main(String[] args) {
        long seed = System.currentTimeMillis();
        Random javaRandom = new Random(seed);
        Random fingerRandom = new Random(seed);
        Random linkedBlockRandom = new Random(seed);
        Random commonsTreeRandom = new Random(seed);
        Integer[] integers = new Integer[NUMBER_OF_ADDS];
        
        for (int i = 0; i < integers.length; i++) {
            integers[i] = i;
        }
        
        FingerList<Integer> fingerList = new FingerList<>(FINGERS);
        long fingerListTotalTime = 0L;
        long start = System.currentTimeMillis();
        
        for (Integer integer : integers) {
            fingerList.add(
                    fingerRandom.nextInt(fingerList.size() + 1 ), 
                    integer);
        }
        
        long end = System.currentTimeMillis();
        fingerListTotalTime += end - start;
        
        System.out.println("FingerList.add in " + (end - start) + " ms.");
        
        start = System.currentTimeMillis();
        
        for (int i = 0; i < NUMBER_OF_GETS; i++) {
            fingerList.get(fingerRandom.nextInt(fingerList.size()));
        }
        
        end = System.currentTimeMillis();
        fingerListTotalTime += end - start;
        
        System.out.println("FingerList.get in " + (end - start) + " ms.");
        
        start = System.currentTimeMillis();
        
        for (int i = 0; i < NUMBER_OF_ADDS; i++) {
            fingerList.remove(fingerRandom.nextInt(fingerList.size()));
        }
        
        end = System.currentTimeMillis();
        fingerListTotalTime += end - start;
        
        System.out.println("FingerList.remove in " + (end - start) + " ms.");
        System.out.println("FingerList total time: " + fingerListTotalTime +
                " ms.");
        System.out.println("------");
        ////
        
        LinkedBlockList<Integer> blockList = new LinkedBlockList<>(256);
        
        long blockListTotalTime = 0L;
        start = System.currentTimeMillis();
        
        for (Integer integer : integers) {
            blockList.add(linkedBlockRandom.nextInt(blockList.size() + 1), integer);
        }
        
        end = System.currentTimeMillis();
        blockListTotalTime = end - start;
        System.out.println("LinkedBlockList.add in " + (end - start) + " ms.");
        
        start = System.currentTimeMillis();
        
        for (int i = 0; i < NUMBER_OF_GETS; i++) {
            blockList.get(linkedBlockRandom.nextInt(blockList.size()));
        }
        
        end = System.currentTimeMillis();
        blockListTotalTime += end - start;
        System.out.println("LinkedBlockList.get in " + (end - start) + " ms.");
        
        start = System.currentTimeMillis();
        
        for (int i = 0; i < NUMBER_OF_ADDS; i++) {
            blockList.remove(linkedBlockRandom.nextInt(blockList.size()));
        }
        
        end = System.currentTimeMillis();
        blockListTotalTime += end - start;
        System.out.println(
                "LinkedBlockList.remove in " + (end - start) + " ms.");
        System.out.println("LinkedBlockList total time: " + 
                blockListTotalTime + " ms.");
        System.out.println("------");
        ////
        long commonsTreeListTotal = 0L;
        LapTimer timer = new LapTimer();
        List<Integer> commonsTreeList = new CommonsTreeList<>();
        timer.push();
        
        for (Integer integer : integers) {
            commonsTreeList.add(integer);
        }
        
        long elapsed = timer.pop();
        System.out.println("commons.TreeList" + timer);
        commonsTreeListTotal += elapsed;
        timer.push();
        
        for (int i = 0; i < NUMBER_OF_GETS; i++) {
            commonsTreeList.get(
                    commonsTreeRandom.nextInt(
                            commonsTreeList.size()));
        }
        
        elapsed = timer.pop();
        commonsTreeListTotal += elapsed;
        System.out.println("commons.TreeList" + timer);
        timer.push();
        
        for (int i = 0; i < NUMBER_OF_ADDS; i++) {
            commonsTreeList.remove(commonsTreeRandom.nextInt(commonsTreeList.size()));
        }
        
        elapsed = timer.pop();
        commonsTreeListTotal += elapsed;
        System.out.println("commons.TreeList" + timer);
        System.out.println("commons.TreeList total time: " +
                commonsTreeListTotal + " ms.");
        System.out.println("------");
        ////
        List<Integer> javaList = new LinkedList<>();
        long javaListTotalTime = 0L;
        start = System.currentTimeMillis();
        
        for (Integer integer : integers) {
            javaList.add(
                    javaRandom.nextInt(javaList.size() + 1 ), 
                    integer);
        }
        
        end = System.currentTimeMillis();
        javaListTotalTime += end - start;
        System.out.println("LinkedList.add in " + (end - start) + " ms.");
        start = System.currentTimeMillis();
        
        for (int i = 0; i < NUMBER_OF_GETS; i++) {
            javaList.get(javaRandom.nextInt(javaList.size()));
        }
        
        end = System.currentTimeMillis();
        javaListTotalTime += end - start;
        
        System.out.println("LinkedList.get in " + (end - start) + " ms.");
        
        start = System.currentTimeMillis();
        
        for (int i = 0; i < NUMBER_OF_ADDS; i++) {
            javaList.remove(javaRandom.nextInt(javaList.size()));
        }
        
        end = System.currentTimeMillis();
        javaListTotalTime += end - start;
        
        System.out.println("LinkedList.remove in " + (end - start) + " ms.");
        System.out.println("LinkedList total time: "  + javaListTotalTime +
                " ms.");
    }
}
