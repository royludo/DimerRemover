import org.apache.log4j.Level;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

public class Main {

    @Option(name="-i",aliases = {"--input"}, usage = "The input file")
    private String filename="";

    @Option(name="-j",aliases = {"--inputR2"}, usage = "The input file for the second read")
    private String filenameR2="";

    @Option(name = "-h",aliases = {"--help"}, usage = "Display this help")
    private boolean help;

    @Option(name="-a",aliases = {"--adapter"}, usage = "The adapter sequence")
    private String adapter="";

    @Option(name="--output-nodimer", usage = "Output file containing no adapters")
    private String noAdapterOutputFilename="";

    @Option(name="--output-dimer", usage = "Output file containing the adapters")
    private String adapterOutputFilename="";

    @Option(name="--output-nodimerR2", usage = "Output file containing no adapters for the second read")
    private String noAdapterOutputR2Filename="";

    @Option(name="--output-dimerR2", usage = "Output file containing adapters for the second read")
    private String adapterOutputR2Filename="";

    /*@Option(name="-hash", usage = "The input file R2")
    private String hashOutputFilename;    */

    @Option(name="-c",aliases = {"--count"}, usage = "Remove adapter dimers instead of just counting them")
    boolean countAdapters =false;

    //@Option(name="-p", usage = "Number of threads")
    private int threadCount=0;

    @Option(name="-s",aliases = {"--SID"}, usage = "Substitution Insertion Deletion. String that code the variations to apply to the adapter sequence. See the doc.")
    private String SID="i2:s2(d1):d2";

    @Option(name="--hashfile", usage = "Output file that will store the hash table")
    private String hashfile="";

    @Option(name="--restorehash", usage = "Use a previously saved hash instead of an adapter sequence")
    private String restorehash="";

    @Option(name="--readshift", usage = "Shift the position of the read from 1 to this number (in bp)")
    private int readshift=5;

    @Option(name="--adaptershift", usage = "Shift the position of the adapter from 1 to this number (in bp)")
    private int adaptershift=5;

    @Option(name="--compression-level", usage = "Compression level as defined by the GZIP format")
    private int compressionLevel=4;

    @Option(name="-l",aliases = {"--log"}, usage = "Log file with stats and results")
    private String logFilename="";

    @Option(name="--uncompressed-input", usage = "The input is in uncompressed fastq format")
    private boolean uncompressedInput;

    @Option(name="--uncompressed-output", usage = "The output will not be compressed. It is faster.")
    private boolean uncompressedOutput;

    @Option(name="-v",aliases = {"--version"}, usage = "Version of the program.")
    private boolean version;

    @Option(name="--debug", usage = "Verbose mode.")
    private boolean debug;


    static char[] alphabet = {'N', 'A', 'T', 'G', 'C'};
    static Logger logger = Logger.getLogger(Main.class);

    @Argument
    private List<String> arguments = new ArrayList<String>();


    public void doStuff() throws Exception {
        long t1 = System.currentTimeMillis(); // for benchmark, written in log
        logger.debug("Entering main function");

        this.checkOptions();


        boolean isPE = false;
        if(filenameR2 != ""){
            isPE = true;
        }



        HashSet<String> hash = new HashSet<String>();

        SequenceVariator variator=null;
        if(restorehash != ""){
            logger.debug("Restoring hash entries");
            try {
                ObjectInputStream ois=new ObjectInputStream(new FileInputStream(restorehash));
                variator=(SequenceVariator) ois.readObject();
                hash=variator.hash;
                adapter = new String(variator.sequence);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        else{
            logger.debug("Computing hash entries");
            long tx = System.currentTimeMillis();
            variator=new SequenceVariator(adapter.toCharArray(),alphabet, hash,SID);
            long ty = System.currentTimeMillis();
            //System.out.println((ty-tx));
        }
        logger.debug(variator.hash.size()+" keys will be used");

        if(hashfile != ""){
            logger.debug("Saving hash entries");
            try {
                variator.serialize(hashfile);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        FastqBufferedReader in=null;
        FastqBufferedReader inR2=null;
        try {
            if(uncompressedInput){
                in = new FastqBufferedReader(new BufferedReader(new InputStreamReader(new FileInputStream(this.filename))));
                if(isPE){
                    inR2 = new FastqBufferedReader(new BufferedReader(new InputStreamReader(new FileInputStream(this.filenameR2))));
                }
            }
            else{
                in = new FastqBufferedReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(this.filename)))));
                if(isPE){
                    inR2 = new FastqBufferedReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(this.filenameR2)))));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        BufferedWriter noAdapterOutput=null;
        BufferedWriter adapterOutput=null;
        BufferedWriter noAdapterOutputR2=null;
        BufferedWriter adapterOutputR2=null;
        if(!countAdapters){
            try {
                OutputStream out=null;
                OutputStream out2=null;
                if(uncompressedOutput){
                    out = new FileOutputStream(noAdapterOutputFilename);
                    out2 = new FileOutputStream(adapterOutputFilename);
                }
                else{
                    out = new GZIPOutputStream(new FileOutputStream(noAdapterOutputFilename)){{def.setLevel(compressionLevel);}};
                    out2 = new GZIPOutputStream(new FileOutputStream(adapterOutputFilename)){{def.setLevel(compressionLevel);}};
                }

                noAdapterOutput=new BufferedWriter(new OutputStreamWriter(out));
                adapterOutput=new BufferedWriter(new OutputStreamWriter(out2));


                if(isPE){
                    if(uncompressedOutput){
                        out = new FileOutputStream(noAdapterOutputR2Filename);
                        out2 = new FileOutputStream(adapterOutputR2Filename);
                    }
                    else{
                        out = new GZIPOutputStream(new FileOutputStream(noAdapterOutputR2Filename)){{def.setLevel(compressionLevel);}};
                        out2 = new GZIPOutputStream(new FileOutputStream(adapterOutputR2Filename)){{def.setLevel(compressionLevel);}};
                    }
                    noAdapterOutputR2=new BufferedWriter(new OutputStreamWriter(out));
                    adapterOutputR2=new BufferedWriter(new OutputStreamWriter(out2));
                }
                //hashOutput=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hashOutputFilename)));
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        BufferedWriter log=null;
        log=new BufferedWriter(new FileWriter(logFilename));

        /*iterator= hash.iterator();
        while(iterator.hasNext()){
            try {
                hashOutput.append(iterator.next());
                hashOutput.newLine();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }     */

        logger.debug("Start reading");
        ExecutorService computeExecutor = Executors.newSingleThreadExecutor();

        String[] buffer;
        String[] bufferR2 = null;
        boolean readFlag=false;
        int bufferCount=0;
        int readCount=0;
        int totalReadTimeMillis=0;
        while(true){
            //System.out.println("Buffer: "+bufferCount);
            long r1 = System.currentTimeMillis();
            if((buffer = in.fillBuffer()) != null){
                readCount+=buffer.length/4;    // buffer contain fastq lines, 4 lines per read
                logger.debug("Buffered " + (buffer.length / 4) + " reads in buffer " + bufferCount);
                if(isPE){
                    bufferR2 = inR2.fillBuffer();
                    readCount+=bufferR2.length/4;
                }
                long r2 = System.currentTimeMillis();
                totalReadTimeMillis+=(r2-r1);
                logger.trace("Read time: " + (r2 - r1) + " ms");
                while(AdapterRemover.threadCount.get()>threadCount){
                //while(!getToken()){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                AdapterRemover thread;
                if(isPE){
                    thread = new AdapterRemover(hash,buffer,bufferR2, adapter,noAdapterOutput,adapterOutput,noAdapterOutputR2,adapterOutputR2, countAdapters, readshift, adaptershift, bufferCount);
                }
                else{
                    thread = new AdapterRemover(hash,buffer,adapter,noAdapterOutput,adapterOutput, countAdapters, readshift, adaptershift, bufferCount);
                }
                logger.debug("Launching thread");
                computeExecutor.submit(thread);
                bufferCount++;

            }
            else{
                break;
            }
        }
        logger.debug("Shutting down executerservice");
        computeExecutor.shutdown();
        /*  it is important to wait a little here to avoid a race condition
            else, sometime we can get to the while before the start of the last submitted thread
            in some case, it can lead to a thread running empty, trying to write on closed streams
        */
        Thread.sleep(100);

        //System.out.println("Out of main loop");
        //System.out.println("Waiting loop with threadcount: "+AdapterRemover.threadCount.get());
        logger.debug("Thread count " + AdapterRemover.threadCount.get());
        while(AdapterRemover.threadCount.get()>0){
            try {
                logger.debug("Await termination");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        logger.debug("Terminated");

        long t2 = System.currentTimeMillis();

        log.write("Compute time (s): "+String.format("%.2f",(double)(t2-t1)/1000)+"\n");
        log.write("Total reads processed: "+readCount+"\n");
        log.write("Average reading time (s/M of reads): "+String.format("%.2f",(double)totalReadTimeMillis/(readCount/1000000)/1000)+"\n");
        log.write("Average time for complete processing (s/M of reads): "+String.format("%.2f",(double)(t2-t1)/(readCount/1000000)/1000)+"\n");
        log.write("Total dimers: "+AdapterRemover.totalCount+"\n");
        if(isPE){
            readCount/=2;
        }
        log.write("Percentage of reads in dimers: "+String.format("%.2f",((double)AdapterRemover.totalCount/readCount)*100)+"\n");
        log.close();

        if(!countAdapters){
            try {
                noAdapterOutput.close();
                adapterOutput.close();

                if(isPE){
                    noAdapterOutputR2.close();
                    adapterOutputR2.close();
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }

    public static synchronized void write(String[] bufferR1, String[] bufferR2, int l, BufferedWriter R1, BufferedWriter R2){

        try {
            for(int i = 0; i < l; i++){
                R1.append(bufferR1[i]);
                R1.newLine();
                R2.append(bufferR2[i]);
                R2.newLine();

            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    public static synchronized void write(String[] bufferR1, int l, BufferedWriter R1){

        try {
            for(int i = 0; i < l; i++){
                R1.append(bufferR1[i]);
                R1.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    public void checkOptions() throws Exception {

        if(version){
            System.out.println("v0.9.2");
            System.exit(0);
        }
        else{
            if(filename == "" || logFilename == ""){
                throw new Exception("Wrong options. Input file and result file are required.");
            }
        }

        if(!countAdapters){
            // PE or not
            if(filenameR2 != ""){
                if(noAdapterOutputR2Filename == "" || adapterOutputR2Filename == "" ){
                    throw new Exception("Wrong options. Output files for second reads are not specified.");
                }
            }
            else{
                if(noAdapterOutputR2Filename != "" || adapterOutputR2Filename != "" ){
                    throw new Exception("Wrong options. Output files for second reads are given but no second input file is specified.");
                }
            }

            if(noAdapterOutputFilename == "" || adapterOutputFilename == ""){
                throw new Exception("Wrong options. Output files are necessary.");
            }
        }

        // adapter or hash
        if(adapter != "" && restorehash != ""){
            throw new Exception("You can not provide an adapter sequence AND an input hash file.");
        }
        if(restorehash == "" && adapter == ""){
            throw new Exception("You have to provide an adapter sequence.");
        }

        if(compressionLevel < 0 || compressionLevel > 9){
            throw new Exception("The compression level must be an integer between 0 and 9.");
        }

    }


    public static void main (String args[]) throws Exception {

        // prevents decimal numbers in output to be written with a comma
        Locale.setDefault(new Locale("en", "US"));
        // log4j config
        BasicConfigurator.configure();
        
        Main app = new Main();
        CmdLineParser cmdparser = new CmdLineParser(app);
        try {
            cmdparser.parseArgument(args);

        } catch (CmdLineException e) {
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            System.err.println("java -jar [options]");
            // print the list of available options
            cmdparser.printUsage(System.err);
            System.err.println();
            throw new Exception("Wrong options.");

        }
        if(app.debug){
            logger.setLevel(Level.DEBUG);
        }
        else{
            logger.setLevel(Level.WARN);
        }
        app.doStuff();

    }
}
