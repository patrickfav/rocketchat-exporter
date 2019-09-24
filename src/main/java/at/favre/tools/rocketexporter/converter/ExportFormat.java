package at.favre.tools.rocketexporter.converter;

import at.favre.tools.rocketexporter.model.Message;

import java.io.OutputStream;
import java.util.List;

public interface ExportFormat {
    /**
     * Export given messages to provided stream
     *
     * @param messages     to export
     * @param outputStream to write to
     */
    void export(List<Message> messages, OutputStream outputStream);
}
