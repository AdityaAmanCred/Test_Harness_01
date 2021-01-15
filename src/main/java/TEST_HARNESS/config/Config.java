package TEST_HARNESS.config;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import TEST_HARNESS.Application;
import TEST_HARNESS.parse.Environment;
import TEST_HARNESS.parse.ParserName;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Prithvi Patil
 * @version 1.0
 */
@Setter
@Getter
public class Config {
    private static CompareAgainst compareAgainst;

    private static ParserName primaryParserName;

    private static ParserName secondaryParserName;

    private static Environment primaryParserEnv;

    private static Environment secondaryParserEnv;

    private static BlockingQueue<String> primaryParserBlockingQueue = new ArrayBlockingQueue(5000);

    private static BlockingQueue<String> secondaryParserBlockingQueue = new ArrayBlockingQueue(5000);

    private static boolean downLoadThreadIsTerminated = false;

    public static void setDownloadThreadIsTerminated(boolean flag) {
        Config.downLoadThreadIsTerminated = flag;
    }

    public static boolean IsDownloadThreadIsTerminated() {
        return Config.downLoadThreadIsTerminated;
    }

    public static Environment getPrimaryParserEnv() {
        return Config.primaryParserEnv;
    }

    public static Environment getSecondaryParserEnv() {
        return Config.secondaryParserEnv;
    }

    public static void setCompareAgainst(CompareAgainst compareAgainst) {
        Config.compareAgainst = compareAgainst;
    }

    public static CompareAgainst getCompareAgainst() {
        return Config.compareAgainst;
    }

    public static ParserName getPrimaryParserName() {
        return Config.primaryParserName;
    }

    public static void setPrimaryParserName(ParserName parserName) {
        Config.primaryParserName = parserName;
    }

    public static ParserName getSecondaryParserName() {
        return Config.secondaryParserName;
    }

    public static void setPrimaryParserEnv(Environment parserEnv) {
        Config.primaryParserEnv = parserEnv;
    }

    public static void setSecondaryParserEnv(Environment parserEnv) {
        Config.secondaryParserEnv = parserEnv;
    }

    public static void setSecondaryParserName(ParserName parserName) {
        Config.secondaryParserName = parserName;
    }

    public static BlockingQueue<String> getPrimaryParserBlockingQueue() {
        return Config.primaryParserBlockingQueue;
    }

    public static BlockingQueue<String> getSecondaryParserBlockingQueue() {
        return Config.secondaryParserBlockingQueue;
    }
}
