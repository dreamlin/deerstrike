package org.linjia.deerstrike.model;

import java.util.List;

public class DbCommandText {
	private String			commandText;
	private List<String>	parameters;

	public DbCommandText(String commandText, List<String> parameters) {
		this.commandText = commandText;
		this.parameters = parameters;
	}

	public String getCommandText() {
		return commandText;
	}

	public List<String> getParameters() {
		return parameters;
	}
}
