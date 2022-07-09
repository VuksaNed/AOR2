/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.ac.bg.etf.aor2.replacementpolicy;

import java.util.ArrayList;
import java.util.Random;
import rs.ac.bg.etf.aor2.memory.MemoryOperation;
import rs.ac.bg.etf.aor2.memory.cache.ICacheMemory;
import rs.ac.bg.etf.aor2.memory.cache.Tag;

/**
 *
 * @author vukasin
 */
public class RandomPseudoLRUReplacementPolicy implements IReplacementPolicy{
    
    protected ICacheMemory ICacheMemory;
    protected int[] LRUCnts;
    protected int setAsoc;
    protected int opsegRandom;
    protected Random rand;
    protected  int x=-1;
    
    public RandomPseudoLRUReplacementPolicy(){
        rand = new Random();
    }

    @Override
    public void init(ICacheMemory c) {
        this.ICacheMemory = c;
        setAsoc = (int) c.getSetAsociativity();
        int size = (int) ICacheMemory.getSetNum()*(setAsoc/4);

        LRUCnts = new int[size];

        for (int i = 0; i < size; i++) {
            LRUCnts[i] = 0;
        }
        
        opsegRandom=setAsoc/4;
        
        if (setAsoc % 4!=0)
            throw new RuntimeException(
                    "Implemented PseudoLRU support only size 4");
    }

    @Override
    public int getBlockIndexToReplace(long adr) {
        int set = (int) ICacheMemory.extractSet(adr);
        return set * setAsoc + getEntry(adr);
    }
    
    private int getEntry(long adr) {//////
        x = rand.nextInt(opsegRandom);
        int set = (int) ICacheMemory.extractSet(adr);
        ArrayList<Tag> tagMemory = ICacheMemory.getTags();
        int result = 0;
        for (int i = x; i < x+(setAsoc/4); i++) {
            int block = set * setAsoc + i;
            Tag tag = tagMemory.get(block);
            if (!tag.V) {
                return i;
            }
        }
        int LRUCnt = LRUCnts[set+x];
        int convert[] = {3, 3, 2, 2, 1, 0, 1, 0};
        result = convert[LRUCnt & 7];

        return result;
    }

    @Override
    public void doOperation(MemoryOperation operation) {
        MemoryOperation.MemoryOperationType opr = operation.getType();
        if (x==-1) x = rand.nextInt(opsegRandom);
        if ((opr == MemoryOperation.MemoryOperationType.READ)
                || (opr == MemoryOperation.MemoryOperationType.WRITE)) {

            long adr = operation.getAddress();
            int set = (int) ICacheMemory.extractSet(adr);
            long tagTag = ICacheMemory.extractTag(adr);
            ArrayList<Tag> tagMemory = ICacheMemory.getTags();
            int entry = 0;
            for (int i = x; i < x+ (setAsoc/4); i++) {
                int block = set * setAsoc + i;
                Tag tag = tagMemory.get(block);
                if (tag.V && (tag.tag == tagTag)) {
                    entry = i;
                    break;
                }
            }
            int LRUCnt = LRUCnts[set+x];
            LRUCnt = LRUCnt & 7;
            switch (entry) {
                case 0:
                    LRUCnt = LRUCnt & 2;
                    break;
                case 1:
                    LRUCnt = (LRUCnt & 2) | 1;
                    break;
                case 2:
                    LRUCnt = (LRUCnt & 1) | 4;
                    break;
                case 3:
                    LRUCnt = (LRUCnt & 1) | 6;
                    break;
            }
            LRUCnts[set] = LRUCnt;

        } else if (operation.getType() == MemoryOperation.MemoryOperationType.FLUSHALL) {
            for (int i = 0; i < LRUCnts.length; i++) {
                LRUCnts[i] = 0;
            }

        }
        x=-1;
    }

    @Override
    public String printValid() {
        //nista
        return "";
    }

    @Override
    public String printAll() {
        //nista
        return "";
    }

    @Override
    public void reset() {
       for (int i = 0; i < LRUCnts.length; i++) {
            LRUCnts[i] = 0;
        }
    }
    
}
