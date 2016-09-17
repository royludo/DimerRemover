import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class AdapterRemover implements Runnable{

    HashSet<String> hash;
    String[] bufferR1;
    String[] bufferR2;
    String adapter;
    BufferedWriter noAdapterOutput;
    BufferedWriter adapterOutput;
    BufferedWriter noAdapterOutputR2;
    BufferedWriter adapterOutputR2;
    public final static AtomicInteger threadCount = new AtomicInteger(0);
    //public static volatile int threadCount=0;
    boolean countAdapters;
    public static int totalCount=0;
    private int readShift;
    private int adapterShift;
    boolean isPE = false;
    Logger logger;

    // Paired end
    public AdapterRemover(HashSet<String> hash, String[] bufferR1, String[] bufferR2, String adapter, BufferedWriter noAdapterOutput, BufferedWriter adapterOutput, BufferedWriter noAdapterOutputR2, BufferedWriter adapterOutputR2, boolean countAdapters, int readShift, int adapterShift, int id){
        this.bufferR1 =bufferR1;
        this.bufferR2 = bufferR2;
        this.hash=hash;
        this.adapter=adapter;
        this.noAdapterOutput =noAdapterOutput;
        this.adapterOutput =adapterOutput;
        this.noAdapterOutputR2 =noAdapterOutputR2;
        this.adapterOutputR2 =adapterOutputR2;
        this.countAdapters =countAdapters;
        this.isPE = true;
        this.readShift = readShift;
        this.adapterShift = adapterShift;
        //this.logger = Logger.getLogger(Main.class+".AdapterRemover"+id);
        this.logger=Main.logger;

    }

    // Single read
    public AdapterRemover(HashSet<String> hash, String[] bufferR1, String adapter, BufferedWriter noAdapterOutput, BufferedWriter adapterOutput, boolean removeAdapters, int readShift, int adapterShift, int id){
        this.bufferR1 =bufferR1;
        this.bufferR2 = null;
        this.hash=hash;
        this.adapter=adapter;
        this.noAdapterOutput = noAdapterOutput;
        this.adapterOutput = adapterOutput;
        this.noAdapterOutputR2 = null;
        this.adapterOutputR2 = null;
        this.countAdapters =removeAdapters;
        this.isPE = false;
        this.readShift = readShift;
        this.adapterShift = adapterShift;
        //this.logger = Logger.getLogger(Main.class+".AdapterRemover"+id);
        this.logger=Main.logger;
    }

    public void run(){
        logger.debug("Thread running");
        threadCount.incrementAndGet();
        //System.out.println("start thread threadcount: "+threadCount.get()+" with buffersizeR1: "+bufferR1.length+" isPE "+this.isPE);
        long t1 = System.currentTimeMillis();
        int count=0;
        boolean readFlag=false;
        boolean qualFlag=false;
        boolean isAdapter=false;
        String[] fastqReadBufferR1=new String[4];
        String[] fastqReadBufferR2=new String[4];
        String[] outBuffR1adapt = new String[bufferR1.length];
        String[] outBuffR1noadapt = new String[bufferR1.length];
        String[] outBuffR2adapt = null;
        String[] outBuffR2noadapt = null;
        int cAdapt = 0;
        int cNoadapt = 0;
        if(isPE){
            outBuffR2adapt = new String[bufferR1.length];
            outBuffR2noadapt = new String[bufferR1.length];
        }

        for(int i=0;i< bufferR1.length;i++){
            if(i%4 == 0){
                readFlag=true;
                fastqReadBufferR1[0]= bufferR1[i];
                if(isPE){
                    fastqReadBufferR2[0]= bufferR2[i];
                }
                isAdapter=false;
                continue;
            }
            if(readFlag){
                readFlag=false;
                fastqReadBufferR1[1]= bufferR1[i];
                if(isPE){
                    fastqReadBufferR2[1]= bufferR2[i];
                }
                /*for(int j=0;j<readShift;j++){
                //for(int j=0;j<(bufferR1[i].length()-adapter.length());j++){
                    for(int k = 0;k<adapterShift;k++){
                        //String sub = bufferR1[i].substring(j, adapter.length() - k+j);
                        String sub = bufferR1[i].substring(j, adapter.length() - k);
                        if(hash.contains(sub)){
                            isAdapter=true;
                            count++;
                            break;
                        }
                    }
                    if(isAdapter){
                        break;
                    }
                }   */
                isAdapter = this.core(i);
                if(isAdapter) count++;
                continue;
            }
            if(i%4 == 2){
                qualFlag=true;
                fastqReadBufferR1[2]= bufferR1[i];
                if(isPE){
                    fastqReadBufferR2[2]= bufferR2[i];
                }
                continue;
            }
            if(qualFlag){
                qualFlag=false;
                fastqReadBufferR1[3]= bufferR1[i];
                if(isPE){
                    fastqReadBufferR2[3]= bufferR2[i];
                    if(!countAdapters){
                        if(isAdapter){
                            for(int k=0;k<4;k++){
                                outBuffR1adapt[cAdapt] = fastqReadBufferR1[k];
                                outBuffR2adapt[cAdapt] = fastqReadBufferR2[k];
                                cAdapt++;
                            }
                            //Main.write(fastqReadBufferR1, fastqReadBufferR2, adapterOutput, adapterOutputR2);
                        }
                        else{
                            for(int k=0;k<4;k++){
                                outBuffR1noadapt[cNoadapt] = fastqReadBufferR1[k];
                                outBuffR2noadapt[cNoadapt] = fastqReadBufferR2[k];
                                cNoadapt++;
                            }
                            //Main.write(fastqReadBufferR1, fastqReadBufferR2, noAdapterOutput, noAdapterOutputR2);
                        }
                    }
                }
                else{
                    if(!countAdapters){
                        if(isAdapter){
                            for(int k=0;k<4;k++){
                                outBuffR1adapt[cAdapt] = fastqReadBufferR1[k];
                                cAdapt++;
                            }
                            //Main.write(fastqReadBufferR1,adapterOutput);
                        }
                        else{
                            for(int k=0;k<4;k++){
                                outBuffR1noadapt[cNoadapt] = fastqReadBufferR1[k];
                                cNoadapt++;
                            }
                            //Main.write(fastqReadBufferR1,noAdapterOutput);
                        }
                    }
                }
                continue;
            }

        }

        if(isPE){
            Main.write(outBuffR1adapt, outBuffR2adapt, cAdapt, adapterOutput, adapterOutputR2);
            Main.write(outBuffR1noadapt, outBuffR2noadapt, cNoadapt, noAdapterOutput, noAdapterOutputR2);
        }
        else{
            Main.write(outBuffR1adapt, cAdapt, adapterOutput);
            Main.write(outBuffR1noadapt, cNoadapt, noAdapterOutput);
        }

        long t2 = System.currentTimeMillis();
        //System.out.println("Count "+count+" Compute time: "+(t2-t1)+" Thread count: "+threadCount);
        totalCount+=count;
        threadCount.decrementAndGet();
        logger.debug("Thread done");
        //System.out.println("threadcount now: "+threadCount.get()+" thread finished");
        //Main.releaseToken();
    }

    boolean core(int i){
        for(int j=0;j<this.readShift;j++){
            //for(int j=0;j<(bufferR1[i].length()-adapter.length());j++){
            for(int k = 0;k<this.adapterShift;k++) {
                //String sub = bufferR1[i].substring(j, adapter.length() - k+j);
                String sub = this.bufferR1[i].substring(j, this.adapter.length() - k);
                if (hash.contains(sub)) {
                    return true;
                }
            }
        }
        return false;
    }


    /*

     for(my $j=0;$j<$read_shift;$j++){
                # try for different adapter length
                # => shift the adapter from $k base to the left
                for($k = 0;$k<$adapter_max_shift;$k++){
                        my $read_beginning = substr($read,$j,$adapter_length-$k);
                        if($hashref->{$read_beginning}){
                                return 1;
                        }
                }
        }



     */


}
