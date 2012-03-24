package de.tourenplaner.server;

class CLIParser {

	public String getConfigFilePath() {
		return this.configFilePath;
	}

	public boolean loadTextGraph() {
		return !this.readFromDumpedGraph;
	}

    public boolean dumpgraph() {
        return dumpgraph;
    }

    private String configFilePath;
	private boolean readFromDumpedGraph;
    private boolean dumpgraph;

	public CLIParser(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if ("-c".equals(args[i]) && ((i + 1) < args.length) && !(args[i+1].startsWith("-"))) {
				configFilePath = args[i + 1];
			} else if ("-f".equals(args[i]) && ((i + 1) < args.length) &&
                       "dump".equals(args[i + 1])) {
                readFromDumpedGraph = true;
            } else if ("dumpgraph".equals(args[i])) {
				dumpgraph = true;
			}
		}
	}
}