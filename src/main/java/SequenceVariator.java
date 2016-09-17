import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SequenceVariator implements Serializable{

    char[] sequence;
    char[] alphabet;
    String SID;
    HashSet<String> hash;

    public SequenceVariator(char[] sequence, char[] alphabet, HashSet<String> hash, String SID){
        this.sequence=sequence;
        this.alphabet=alphabet;
        this.SID=SID;
        this.hash=hash;
        this.validateSID();
    }


    public void validateSID() throws IllegalArgumentException {
        char[] chars=this.SID.toCharArray();
        boolean isOp=false;
        boolean isCount=false;
        char currentOp='\0';
        String countString="";
        int count;
        int level=0;
        HashSet<String> previous_hash;
        Iterator<String> iterator;
        for(int i=0;i<chars.length;i++){
            char c=chars[i];
            if(c =='s' || c=='d' || c=='i'){
                currentOp=c;
                isOp=true;
                continue;
            }
            else if(new String(""+c).matches("\\d")){
                isCount=true;
                if(isOp){
                    countString=""+c;
                    isOp=false;
                }
                else{
                    countString+=c;
                }
            }
            if(c == ':' || c=='(' || c==')' || i==chars.length-1){ // : or ( or )
                // apply current operation with specified number
                count=Integer.parseInt(countString);
                if(level>0){
                    switch(currentOp){
                        case 's':
                           //System.out.println(count+" substitution foreach");
                            previous_hash = new HashSet<String>(hash);
                            iterator= previous_hash.iterator();
                            while(iterator.hasNext()){
                                generate_mismatch(iterator.next().toCharArray(), this.alphabet, count, this.hash, 0);
                            }
                            break;
                        case 'i':
                            //System.out.println(count+" insertion foreach");
                            previous_hash = new HashSet<String>(hash);
                            iterator= previous_hash.iterator();
                            while(iterator.hasNext()){
                                generate_insertion(iterator.next().toCharArray(), this.alphabet, count, this.hash, 0);
                            }
                            break;
                        case 'd':
                            //System.out.println(count+" deletion foreach");
                            previous_hash = new HashSet<String>(hash);
                            iterator= previous_hash.iterator();
                            while(iterator.hasNext()){
                                generate_deletion(iterator.next().toCharArray(), count, this.hash, 0);
                            }
                            break;
                    }
                    //System.out.println(hash.size());
                }
                else{
                    for(int k=0;k<5;k++){
                        String subseq=new String(this.sequence).substring(k, this.sequence.length);
                        switch(currentOp){
                            case 's':
                                //System.out.println(count+" substitution");
                                generate_mismatch(subseq.toCharArray(), this.alphabet, count, this.hash, 0);
                                break;
                            case 'i':
                                //System.out.println(count+" insertion");
                                generate_insertion(subseq.toCharArray(), this.alphabet, count, this.hash, 0);
                                break;
                            case 'd':
                                //System.out.println(count+" deletion");
                                generate_deletion(subseq.toCharArray(), count, this.hash, 0);
                                break;
                        }

                    }
                    //System.out.println(hash.size());
                }


                if(c == '('){
                    level++;
                }
                else if(c==')'){
                    level--;
                }
                currentOp='\0';
            }



        }

    }

    public void serialize(String filename) throws IOException {
        ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(filename));
        oos.writeObject(this);
        oos.close();
    }

    public static void generate_deletion(char[] sequence, int nb_deletion, HashSet<String> hash, int offset){

        // for the length of the sequence
        for(int c=offset;c<sequence.length;c++){
            char[] new_seq = new char[sequence.length-1];
            int newseq_index=0;
            // copy the char array, applying the appropriate modifications
            for(int i=0;i<sequence.length;i++){
                if(i==c) continue;
                new_seq[newseq_index] = sequence[i];
                newseq_index++;
            }
            hash.add(new String(new_seq));
            if(nb_deletion > 1){
                generate_deletion(new_seq,nb_deletion-1,hash,offset);
            }
        }
    }

    public static void generate_insertion(char[] sequence, char[] alphabet, int nb_insertion, HashSet<String> hash, int offset){

        for(int c=offset;c<sequence.length+1;c++){
            for(char letter:alphabet){
                char[] new_seq = new char[sequence.length+1];
                int newseq_index=0;
                for(int i=0;i<sequence.length;i++){
                    if(i==c){
                        new_seq[newseq_index] = letter;
                        newseq_index++;
                    }
                    new_seq[newseq_index] = sequence[i];
                    newseq_index++;

                }
                if(newseq_index<sequence.length+1){
                    new_seq[newseq_index] = letter;
                }
                hash.add(new String(new_seq));
                if(nb_insertion > 1){
                    generate_insertion(new_seq,alphabet, nb_insertion - 1, hash, offset + 1);
                }
            }
        }
    }

    public static void generate_mismatch(char[] sequence, char[] alphabet, int nb_mismatch, HashSet<String> hash, int offset){

        for(int c=offset;c<sequence.length;c++){
            for(char letter:alphabet){
                char[] new_seq = new char[sequence.length];
                int newseq_index=0;
                for(int i=0;i<sequence.length;i++){
                    if(i==c){
                        new_seq[newseq_index] = letter;
                        newseq_index++;
                        continue;
                    }
                    new_seq[newseq_index] = sequence[i];
                    newseq_index++;
                }
                hash.add(new String(new_seq));
                if(nb_mismatch > 1){
                    generate_mismatch(new_seq, alphabet, nb_mismatch - 1, hash, offset + 1);
                }
            }
        }
    }


}
