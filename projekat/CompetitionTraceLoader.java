/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.ac.bg.etf.aor2.loader;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;
import rs.ac.bg.etf.aor2.memory.MemoryOperation;

/**
 *
 * @author vukasin
 */
public class CompetitionTraceLoader implements ITraceLoader {
    long counter=0;
    DataInputStream in;
    CispenInstruction inst;
    boolean modify_set=false;
    public  CompetitionTraceLoader(String path) {
        try{
         FileInputStream fi=new FileInputStream(path);
         BufferedInputStream bis=new BufferedInputStream(fi);
         this.in = new DataInputStream(bis);
        }catch(Exception e){
            System.err.println("Exception");
        }
    }
    private static byte[] longtoBytes(long data) {
        return new byte[]{
        (byte) ((data >> 56) & 0xff),
        (byte) ((data >> 48) & 0xff),
        (byte) ((data >> 40) & 0xff),
        (byte) ((data >> 32) & 0xff),
        (byte) ((data >> 24) & 0xff),
        (byte) ((data >> 16) & 0xff),
        (byte) ((data >> 8) & 0xff),
        (byte) ((data >> 0) & 0xff),
        };
}
    private static long convertByteArrayToLong(byte[] longBytes){
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
        byteBuffer.put(longBytes);
        byteBuffer.flip();
        return byteBuffer.getLong();
    }
    public static long getNormalLong(long l){
        byte readBuffer[] = longtoBytes(l);
        byte temp;
        for(int i=0;i<4;i++){
            temp=readBuffer[7-i];
            readBuffer[7-i]=readBuffer[i];
            readBuffer[i]=temp;
        }
        return convertByteArrayToLong(readBuffer);
    }
    @Override
    public MemoryOperation getNextOperation() {
        try{
            MemoryOperation mem=null;
            while(mem==null){
                if(!modify_set){
                    counter++;
                    if(counter%1000000000==0){
                        System.out.println(counter%1000000000+"B");
                    }
                    inst= new CispenInstruction(getNormalLong(in.readLong()), in.readByte(), in.readByte(), in.readByte(), in.readByte(), in.readByte(), in.readByte(),
                        in.readByte(), in.readByte(), getNormalLong(in.readLong()), getNormalLong(in.readLong()), getNormalLong(in.readLong()),getNormalLong( in.readLong()), getNormalLong(in.readLong()), getNormalLong(in.readLong()));
                    //System.out.print(inst.toString());
                }
                if(inst.hasMemSrc()&&inst.hasMemDst()&&modify_set==false){
                    modify_set=true;
                    mem=new MemoryOperation(MemoryOperation.MemoryOperationType.READ, inst.source_memory0);
                    //System.out.println("M");
                }
                else if(inst.hasMemDst()){
                    modify_set=false;
                    mem=new MemoryOperation(MemoryOperation.MemoryOperationType.WRITE, inst.destination_memory0);
                    //System.out.println("W");
                }
                else if(inst.hasMemSrc()){
                    mem=new MemoryOperation(MemoryOperation.MemoryOperationType.READ, inst.source_memory0);
                    //System.out.println("R");
                }
                else{
                    //System.out.println("-");
                }
            }
            return mem;
        }
        catch(Exception e){
            System.err.print(e.getClass());
        }
        return null;
    }
    @Override
    public boolean isInstructionOperation() {
        return false;
    }

    @Override
    public boolean hasOperationToLoad() {
        return true;
    }

    @Override
    public void reset() {
        
    }
    
}
