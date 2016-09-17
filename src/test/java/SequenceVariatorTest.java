import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.HashSet;

public class SequenceVariatorTest {

    private String seqG, seqATG, seqCAAA, seqATGC;
    char[] alphabet;

    @Before public void setUp(){
        this.seqG="G";
        this.seqATG="ATG";
        this.seqCAAA="CAAA";
        this.seqATGC="ATGC";
        this.alphabet= new char[]{'N', 'A', 'T', 'G', 'C'};
    }

    @Test public void testDeletion1onG(){
        HashSet<String> expected=new HashSet<String>();
        expected.add("");
        HashSet<String> actual = new HashSet<String>();
        SequenceVariator.generate_deletion(seqG.toCharArray(),1,actual,0);
        assertEquals(expected, actual);
    }

    @Test public void testDeletion2onG(){
        HashSet<String> expected=new HashSet<String>();
        expected.add("");
        HashSet<String> actual = new HashSet<String>();
        SequenceVariator.generate_deletion(seqG.toCharArray(),2,actual,0);
        assertEquals(expected, actual);
    }

    @Test public void testDeletion1onATG(){
        HashSet<String> expected=new HashSet<String>();
        expected.add("TG");
        expected.add("AG");
        expected.add("AT");
        HashSet<String> actual = new HashSet<String>();
        SequenceVariator.generate_deletion(seqATG.toCharArray(),1,actual,0);
        assertEquals(expected, actual);
    }

    @Test public void testDeletion2onATG(){
        HashSet<String> expected=new HashSet<String>();
        expected.add("TG");
        expected.add("AG");
        expected.add("AT");
        expected.add("G");
        expected.add("A");
        expected.add("T");
        HashSet<String> actual = new HashSet<String>();
        SequenceVariator.generate_deletion(seqATG.toCharArray(),2,actual,0);
        assertEquals(expected, actual);
    }

    @Test public void testDeletion1onCAAA(){
        HashSet<String> expected=new HashSet<String>();
        expected.add("AAA");
        expected.add("CAA");
        HashSet<String> actual = new HashSet<String>();
        SequenceVariator.generate_deletion(seqCAAA.toCharArray(),1,actual,0);
        assertEquals(expected, actual);
    }

    @Test public void testDeletion2onCAAA(){
        HashSet<String> expected=new HashSet<String>();
        expected.add("AA");
        expected.add("AAA");
        expected.add("CAA");
        expected.add("CA");
        HashSet<String> actual = new HashSet<String>();
        SequenceVariator.generate_deletion(seqCAAA.toCharArray(),2,actual,0);
        assertEquals(expected, actual);
    }

    @Test public void testDeletion1onATGC(){
        HashSet<String> expected=new HashSet<String>();
        expected.add("TGC");
        expected.add("AGC");
        expected.add("ATC");
        expected.add("ATG");
        HashSet<String> actual = new HashSet<String>();
        SequenceVariator.generate_deletion(seqATGC.toCharArray(),1,actual,0);
        assertEquals(expected, actual);
    }

    @Test public void testDeletion2onATGC(){
        HashSet<String> expected=new HashSet<String>();
        expected.add("TGC");
        expected.add("AGC");
        expected.add("ATC");
        expected.add("ATG");
        expected.add("AT");
        expected.add("TG");
        expected.add("GC");
        expected.add("AC");
        expected.add("AG");
        expected.add("TC");
        HashSet<String> actual = new HashSet<String>();
        SequenceVariator.generate_deletion(seqATGC.toCharArray(),2,actual,0);
        assertEquals(expected, actual);
    }

    @Test public void testInsertion1onG(){
        HashSet<String> expected=new HashSet<String>(Arrays.asList("NG","AG","TG","GG","CG","GN","GA","GT","GC"));
        HashSet<String> actual = new HashSet<String>();
        SequenceVariator.generate_insertion(seqG.toCharArray(), alphabet, 1, actual, 0);
        assertEquals(expected, actual);
    }

    @Test public void testInsertion2onG(){
        HashSet<String> expected=new HashSet<String>(Arrays.asList(
                "NG","AG","TG","GG","CG",
                "GN","GA","GT","GG","GC",

                "NNG","NAG","NTG","NGG","NCG",
                "NGN","NGA","NGT","NGG","NGC",
                "NNG","ANG","TNG","GNG","CNG",
                "GNN","GNA","GNT","GNG","GNC",
                "NGN","AGN","TGN","GGN","CGN",
                "GNN","GAN","GTN","GGN","GCN",

                "ANG","AAG","ATG","AGG","ACG",
                "AGN","AGA","AGT","AGG","AGC",
                "NAG","AAG","TAG","GAG","CAG",
                "GAN","GAA","GAT","GAG","GAC",
                "NGA","AGA","TGA","GGA","CGA",
                "GNA","GAA","GTA","GGA","GCA",

                "TNG","TAG","TTG","TGG","TCG",
                "TGN","TGA","TGT","TGG","TGC",
                "NTG","ATG","TTG","GTG","CTG",
                "GTN","GTA","GTT","GTG","GTC",
                "NGT","AGT","TGT","GGT","CGT",
                "GNT","GAT","GTT","GGT","GCT",

                "GNG","GAG","GTG","GGG","GCG",
                "GGN","GGA","GGT","GGG","GGC",
                "NGG","AGG","TGG","GGG","CGG",
                "GGN","GGA","GGT","GGG","GGC",
                "NGG","AGG","TGG","GGG","CGG",
                "GNG","GAG","GTG","GGG","GCG",

                "CNG","CAG","CTG","CGG","CCG",
                "CGN","CGA","CGT","CGG","CGC",
                "NCG","ACG","TCG","GCG","CCG",
                "GCN","GCA","GCT","GCG","GCC",
                "NCG","ACG","TCG","GCG","CCG",
                "GCN","GCA","GCT","GCG","GCC"
        ));
        HashSet<String> actual = new HashSet<String>();
        SequenceVariator.generate_insertion(seqG.toCharArray(), alphabet, 2, actual, 0);
        assertEquals(expected, actual);
    }

    @Test public void testInsertion1onATG(){
        HashSet<String> expected=new HashSet<String>(Arrays.asList(
                "NATG","ANTG","ATNG","ATGN",
                "AATG","AATG","ATAG","ATGA",
                "TATG","ATTG","ATTG","ATGT",
                "GATG","AGTG","ATGG","ATGG",
                "CATG","ACTG","ATCG","ATGC"

        ));
        HashSet<String> actual = new HashSet<String>();
        SequenceVariator.generate_insertion(seqATG.toCharArray(), alphabet, 1, actual, 0);
        assertEquals(expected, actual);
    }

    @Test public void testInsertion1onCAAA(){
        HashSet<String> expected=new HashSet<String>(Arrays.asList(
                "NCAAA","CNAAA","CANAA","CAANA","CAAAN",
                "ACAAA","CAAAA","CAAAA","CAAAA","CAAAA",
                "TCAAA","CTAAA","CATAA","CAATA","CAAAT",
                "GCAAA","CGAAA","CAGAA","CAAGA","CAAAG",
                "CCAAA","CCAAA","CACAA","CAACA","CAAAC"
        ));
        HashSet<String> actual = new HashSet<String>();
        SequenceVariator.generate_insertion(seqCAAA.toCharArray(), alphabet, 1, actual, 0);
        assertEquals(expected, actual);
    }

    @Test public void testInsertion1onATGC(){
        HashSet<String> expected=new HashSet<String>(Arrays.asList(
                "NATGC","ANTGC","ATNGC","ATGNC","ATGCN",
                "AATGC","AATGC","ATAGC","ATGAC","ATGCA",
                "TATGC","ATTGC","ATTGC","ATGTC","ATGCT",
                "GATGC","AGTGC","ATGGC","ATGGC","ATGCG",
                "CATGC","ACTGC","ATCGC","ATGCC","ATGCC"
        ));
        HashSet<String> actual = new HashSet<String>();
        SequenceVariator.generate_insertion(seqATGC.toCharArray(), alphabet, 1, actual, 0);
        assertEquals(expected, actual);
    }

    @Test public void testSubstitution1onG(){
        HashSet<String> expected=new HashSet<String>(Arrays.asList("A","T","N","G","C"));
        HashSet<String> actual = new HashSet<String>();
        SequenceVariator.generate_mismatch(seqG.toCharArray(), alphabet, 1, actual, 0);
        assertEquals(expected, actual);
    }

    @Test public void testSubstitution2onG(){
        HashSet<String> expected=new HashSet<String>(Arrays.asList("A","T","N","G","C"));
        HashSet<String> actual = new HashSet<String>();
        SequenceVariator.generate_mismatch(seqG.toCharArray(), alphabet, 2, actual, 0);
        assertEquals(expected, actual);
    }

    @Test public void testSubstitution1onATG(){
        HashSet<String> expected= new HashSet<String>(Arrays.asList(
                "NTG","ANG","ATN",
                "ATG","AAG","ATA",
                "TTG","ATG","ATT",
                "CTG","ACG","ATC",
                "GTG","AGG","ATG"

        ));
        HashSet<String> actual = new HashSet<String>();
        SequenceVariator.generate_mismatch(seqATG.toCharArray(), alphabet, 1, actual, 0);
        assertEquals(expected, actual);
    }
}
