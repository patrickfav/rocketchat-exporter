package at.favre.tools.rocketexporter.converter;

import at.favre.tools.rocketexporter.model.Message;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static java.time.Instant.EPOCH;
import static junit.framework.TestCase.assertEquals;

public class SlackCsvFormatTest {

    private ExportFormat exportFormat = new SlackCsvFormat();

    @Test
    public void export() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        exportFormat.export(
                List.of(
                        new Message("m1", "u1", "c1", EPOCH),
                        new Message("m2", "u2", "c3", EPOCH.plusSeconds(1)),
                        new Message(null, "u3", "c3", EPOCH)
                ),
                bout);

        String out = bout.toString();

        assertEquals("\"0\",\"c1\",\"u1\",\"m1\"\n" +
                "\"1\",\"c3\",\"u2\",\"m2\"\n" +
                "\"0\",\"c3\",\"u3\",\"\"\n", out);
    }
}
