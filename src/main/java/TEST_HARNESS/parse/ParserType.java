package TEST_HARNESS.parse;

public enum ParserType {
    PRIMARY {
        @Override
        public String getDirectoryKeyName() {
            return "PRIMARY_DIR";
        }

    },
    SECONDARY {
        @Override
        public String getDirectoryKeyName() {
            return "SECONDARY_DIR";
        }
    };

    public abstract String getDirectoryKeyName();
}

