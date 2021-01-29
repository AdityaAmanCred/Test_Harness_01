package TEST_HARNESS.parse;

import static TEST_HARNESS.utils.Util.fetchProperty;
import static TEST_HARNESS.utils.Util.isValidParserSelection;
import static TEST_HARNESS.utils.Util.setComparisonParameter;
import java.util.concurrent.BlockingQueue;
import TEST_HARNESS.config.CompareAgainst;
import TEST_HARNESS.config.Config;

public class ParsePdf implements Runnable {
    private int nPThreads;

    private int nSThreads;

    private int maxPThreads = 5;

    private int maxSThreads = 5;

    private double pRateLimit;

    private double sRateLimit;

    private double maxPRateLimit = Config.getPrimaryParserEnv() == Environment.STAGE ? (Config
            .getPrimaryParserName() == ParserName.BUMBLEBEE ? 3 : 10) : 1;

    private double maxSRateLimit = Config.getSecondaryParserEnv() == Environment.STAGE ? (Config
            .getSecondaryParserName() == ParserName.BUMBLEBEE ? 3 : 10) : 1;

    private BlockingQueue<String> pBlockingQueue;

    private BlockingQueue<String> sBlockingQueue;

    private Thread pParserThread;

    private Thread sParserThread;

    public ParsePdf() {
        setComparisonParameter();
        this.nPThreads = Math.min(maxPThreads, Integer.parseInt(fetchProperty("NUM_PRIMARYPARSER_THREADS")));
        this.nSThreads = Math.min(maxSThreads, Integer.parseInt(fetchProperty("NUM_SECONDARYPARSER_THREADS")));
        this.pRateLimit = Math.min(maxPRateLimit, Double.parseDouble(fetchProperty("PRIMARY_PARSER_RATELIMIT")));
        this.sRateLimit = Math.min(maxSRateLimit, Double.parseDouble(fetchProperty("SECONDARY_PARSER_RATELIMIT")));
        this.pBlockingQueue = Config.getPrimaryParserBlockingQueue();
        this.sBlockingQueue = Config.getSecondaryParserBlockingQueue();
        this.pParserThread = new Thread(new ParsingExecutor(nPThreads, pRateLimit, pBlockingQueue, ParserType.PRIMARY));
        this.sParserThread = new Thread(new ParsingExecutor(nSThreads, sRateLimit, sBlockingQueue, ParserType.SECONDARY));
        this.pParserThread.setName("pParserThread");
        this.sParserThread.setName("sParserThread");
    }

    private void parse() throws InterruptedException {
        if (Config.getCompareAgainst() == CompareAgainst.SECONDARY && isValidParserSelection() == false) {
            System.exit(0);
        }
        pParserThread.start();
        if (Config.getCompareAgainst() == CompareAgainst.SECONDARY && isValidParserSelection() == true) {
            sParserThread.start();
        }

    }

    public void run() {
        try {
            parse();
            pParserThread.join();
            sParserThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
