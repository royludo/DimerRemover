import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.Assert.*;

public class FastqBufferedReaderTest {

    String simpleFastqEntry, doubleFastqEntry;

    @Before public void setUp(){
        this.simpleFastqEntry = ">id1\nATGC\n+\n@+>>\n";
        this.doubleFastqEntry = ">id1\nATGC\n+\n@+>>\n>id2\nGAAAAACC\n+\n@>++EEJH";
    }

    @Test public void testBufferSizeReturned(){
        FastqBufferedReader fastqBr;
        BufferedReader simpleBr = new BufferedReader(new StringReader(this.simpleFastqEntry));
        fastqBr = new FastqBufferedReader(simpleBr);
        assertEquals(4, fastqBr.fillBuffer().length);

        BufferedReader doubleBr = new BufferedReader(new StringReader(this.doubleFastqEntry));
        fastqBr = new FastqBufferedReader(doubleBr);
        assertEquals(8, fastqBr.fillBuffer().length);
    }

    @Test public void testSimpleEntryBufferContent(){
        FastqBufferedReader fastqBr;
        BufferedReader simpleBr = new BufferedReader(new StringReader(this.simpleFastqEntry));
        fastqBr = new FastqBufferedReader(simpleBr);
        String[] buffer = fastqBr.fillBuffer();
        assertEquals(">id1", buffer[0]);
        assertEquals("ATGC", buffer[1]);
        assertEquals("+", buffer[2]);
        assertEquals("@+>>", buffer[3]);
    }

    @Test public void testDoubleEntryBufferContent(){
        FastqBufferedReader fastqBr;
        BufferedReader doubleBr = new BufferedReader(new StringReader(this.doubleFastqEntry));
        fastqBr = new FastqBufferedReader(doubleBr);
        String[] buffer = fastqBr.fillBuffer();
        assertEquals(">id1", buffer[0]);
        assertEquals("ATGC", buffer[1]);
        assertEquals("+", buffer[2]);
        assertEquals("@+>>", buffer[3]);
        assertEquals(">id2", buffer[4]);
        assertEquals("GAAAAACC", buffer[5]);
        assertEquals("+", buffer[6]);
        assertEquals("@>++EEJH", buffer[7]);
    }
}
