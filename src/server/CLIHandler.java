package server;

import config.ConfigManager;

class CLIHandler {
	public boolean configFileProvided() {
		return this.hasConfigFile;
	}

	public String getConfigFilePath() {
		return this.configFilePath;
	}

	public boolean loadTextGraph() {
		return this.textGraph;
	}

	public boolean serializegraph() {
		return this.serializegraph;
	}

	public boolean parametersProvided() {
		return parametersProvided;
	}

	private boolean hasConfigFile = false;
	private String configFilePath;
	private boolean textGraph = true;;
	private boolean serializegraph = false;
	private boolean parametersProvided;

	public CLIHandler(String[] args) {
		parametersProvided = args.length > 0;
		textGraph = true;
		serializegraph = false;
		for (int i = 0; i < args.length; i++) {
			// Load Config
			if (args[i].startsWith("-c") && ((i + 1) < args.length)) {
				hasConfigFile = true;
				configFilePath = args[i + 1];
			} else if (args[i].trim().equals("serializegraph")) {
				serializegraph = true;
			} else if (args[i].trim().startsWith("-f")
					&& ((i + 1) < args.length)
					&& args[i + 1].trim().equals("dump")) {
				textGraph = false;
			}
		}
		if (parametersProvided()) {
			if (!configFileProvided()) {
				System.err
						.println("Missing config path parameter, using defaults");
				System.err
						.println("Use \"#server -c PATH\" to load the config at PATH");
			} else {
				try {
					ConfigManager.Init(getConfigFilePath());
				} catch (Exception e) {
					System.err.println("Couldn't load configuration File: "
							+ e.getMessage());
					return;
				}
			}
		} else {
			System.out
					.println("Usage: \"#server -c PATH [-f dump|text] [serializegraph]");
			System.out.println("Defaults: (config builtin), -f text");
		}

	}
}