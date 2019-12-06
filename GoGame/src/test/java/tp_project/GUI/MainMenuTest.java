package tp_project.GUI;

import org.junit.Test;
import static org.junit.Assert.*;

public class MainMenuTest {
    @Test
    public void isValidIPTest() {
        assertTrue(MainMenu.isValidIP("255.255.255.255"));
        assertTrue(MainMenu.isValidIP("0.0.0.0"));
        assertTrue(MainMenu.isValidIP("1.11.111.1"));
        assertFalse(MainMenu.isValidIP(" 21.31.123.23  "));
        assertFalse(MainMenu.isValidIP(".255.255.255"));
        assertFalse(MainMenu.isValidIP("255. 255.255.255"));
        assertFalse(MainMenu.isValidIP("255.255 .255.255"));
        assertFalse(MainMenu.isValidIP("255.2 55.255.255"));
        assertFalse(MainMenu.isValidIP("255..255.255"));
        assertFalse(MainMenu.isValidIP("255.255..255"));
        assertFalse(MainMenu.isValidIP("255.255.255."));
        assertFalse(MainMenu.isValidIP("a255.255.255.255"));
        assertFalse(MainMenu.isValidIP("255.255.255.300"));
        assertFalse(MainMenu.isValidIP("255.255.-23.255"));
        assertFalse(MainMenu.isValidIP("xyz"));
    }

    @Test
    public void isValidPortTest() {
        assertTrue(MainMenu.isValidPort("10"));
        assertTrue(MainMenu.isValidPort("100"));
        assertTrue(MainMenu.isValidPort("10000"));
        assertFalse(MainMenu.isValidPort("70000"));
        assertFalse(MainMenu.isValidPort("-1"));
        assertFalse(MainMenu.isValidPort("abd"));
    }
}
