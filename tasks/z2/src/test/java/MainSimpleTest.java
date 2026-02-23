

import org.junit.Test;
import static org.junit.Assert.*;
import ture.Main;

public class MainSimpleTest {

    @Test
    public void testFirstUniqCharSimple() {
        assertEquals("w", Main.getFirstUniqChar("swiss"));
        assertEquals("h", Main.getFirstUniqChar("hello"));
        assertEquals("c", Main.getFirstUniqChar("aabbcddee"));
        assertEquals("a", Main.getFirstUniqChar("abcde"));
        assertNull(Main.getFirstUniqChar("aabbcc"));
        assertNull(Main.getFirstUniqChar(""));
        assertNull(Main.getFirstUniqChar(null));
        assertEquals("a", Main.getFirstUniqChar("a"));
    }
}