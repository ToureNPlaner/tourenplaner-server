/*
 * Copyright 2012 ToureNPlaner
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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