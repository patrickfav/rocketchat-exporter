package at.favre.tools.rocketexporter.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CliOptionChooserTest {
    private CliOptionChooser chooser;
    private ByteArrayOutputStream bos = new ByteArrayOutputStream();

    @Before
    public void setup() {
        chooser = new CliOptionChooser(new ByteArrayInputStream("2".getBytes()), new PrintStream(bos), List.of("a", "b", "c"), "Choose");
    }

    @Test
    public void prompt() {
        int out = chooser.prompt();
        assertEquals(1, out);
        assertNotNull(bos.toString());
    }
}
