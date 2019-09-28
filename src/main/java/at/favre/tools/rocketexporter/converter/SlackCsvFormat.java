package at.favre.tools.rocketexporter.converter;

import at.favre.tools.rocketexporter.model.Message;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SlackCsvFormat implements ExportFormat {
    @Override
    public void export(List<Message> messages, OutputStream outputStream) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

            for (Message normalizedMessage : messages) {
                writer.write("\"" + normalizedMessage.getTimestamp().getEpochSecond() + "\"," +
                        "\"" + normalizedMessage.getChannel() + "\"," +
                        "\"" + normalizedMessage.getUsername() + "\"," +
                        "\"" + normalizedMessage.getMessage().replaceAll("\"", "\\\\\"") + "\"" +
                        "\n");
            }
            writer.flush();
            writer.close();

        } catch (IOException e) {
            throw new IllegalStateException("could not write to stream", e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ex) {
                throw new IllegalStateException("could not close", ex);
            }
        }
    }

    @Override
    public String fileExtension() {
        return "csv";
    }
}
