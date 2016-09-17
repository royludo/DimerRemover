import java.io.BufferedReader;
import java.io.IOException;


public class FastqBufferedReader {

    int bufferSize = 400000;
    BufferedReader br;

    public FastqBufferedReader(BufferedReader br){
        this.br = br;
    }
    public FastqBufferedReader(BufferedReader br, int bufferSize){
        this.br = br;
        this.bufferSize = bufferSize;
    }


    public String[] fillBuffer(){
        String line;
        String[] buffer=new String[bufferSize];
        int i=0;
        try {
            while (i < bufferSize){
                if((line = br.readLine()) != null){
                    buffer[i] = line;
                }
                else{
                    break;
                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        if(i == 0){
            return null;
        }
        else if( i < bufferSize){  // EOF reached
            if(i%4 != 0){
                System.out.println("Not multiple of 4 !");
            }
            else{
                //System.out.println("Fill buffer i: "+i);
                String[] updatedBuffer = new String[i];
                for(int j=0;j<i;j++){ // copy halfempty bufferR1 into updatebuffer with correct size
                    updatedBuffer[j] = buffer[j];
                }
                return updatedBuffer;
            }
        }

        return buffer;
    }
}
