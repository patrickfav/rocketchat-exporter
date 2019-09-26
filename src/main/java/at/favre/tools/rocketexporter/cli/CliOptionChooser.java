package at.favre.tools.rocketexporter.cli;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

/**
 * Simple console number selector.
 */
class CliOptionChooser {

    private final InputStream inputStream;
    private final PrintStream out;
    private final List<String> options;
    private final String promptMessage;

    CliOptionChooser(InputStream inputStream, PrintStream out, List<String> options, String promptMessage) {
        this.inputStream = inputStream;
        this.out = out;
        this.options = options;
        this.promptMessage = promptMessage;
    }

    /**
     * Starts an interactive prompt to let the user choose an options.
     *
     * @return chosen option
     */
    int prompt() {
        out.println(promptMessage);

        IntStream
                .range(0, options.size())
                .forEach(i -> out.println(String.format("\t(%d) %s", i + 1, options.get(i))));

        while (true) {
            Scanner scanner = new Scanner(inputStream);
            out.println("Select option (1-" + options.size() + "):");

            try {
                int chosenGroup = scanner.nextInt();

                if (chosenGroup <= 0 || chosenGroup > options.size()) {
                    out.println("Invalid input '" + chosenGroup + "'. Please choose a number between 1-" + options.size() + ".");
                } else {
                    return chosenGroup - 1;
                }
            } catch (Exception e) {
                out.println("Invalid input, please try again.");
            }
        }
    }

}
