package TEST_HARNESS;

import static TEST_HARNESS.Util.fetchProperty;
import static TEST_HARNESS.Util.isValidParserSelection;
import static TEST_HARNESS.Util.setComparisonParameter;
import java.util.concurrent.BlockingQueue;

public class ParsePdf implements Runnable {
    private int nPThreads;

    private int nSThreads;

    private int maxPThreads = 5;

    private int maxSThreads = 5;

    private double pRateLimit;

    private double sRateLimit;

    private double maxPRateLimit = 50;

    private double maxSRateLimit = 50;

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
        this.pBlockingQueue = Application.primaryParserBlockingQueue;
        this.sBlockingQueue = Application.secondaryParserBlockingQueue;
        this.pParserThread = new Thread(new ParsingExecutor(nPThreads, pRateLimit, pBlockingQueue, ParserType.PRIMARY));
        this.sParserThread = new Thread(new ParsingExecutor(nSThreads, sRateLimit, sBlockingQueue, ParserType.SECONDARY));
        this.pParserThread.setName("pParserThread");
        this.sParserThread.setName("sParserThread");
    }

    private void parse() throws InterruptedException {
        if (Application.getCompareAgainst() == CompareAgainst.SECONDARY && isValidParserSelection() == false) {
            System.exit(0);
        }
        pParserThread.start();
        if (Application.getCompareAgainst() == CompareAgainst.SECONDARY && isValidParserSelection() == true) {
            sParserThread.start();
        }
    }

    //    public void triggerTerminationOfPrimaryParserThread() {
    //
    //        }
    //
    //    public void triggerTerminationOfSecondaryParserThread() {
    //
    //    }

    public void run() {
        try {
            parse();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
