package com.partners.entity;

import java.util.List;

public class Terminalparameters implements java.io.Serializable {

	private static final long serialVersionUID = -4813438083728418444L;
	private String name;
	private String fileName;
	private List<TerminalParametersAttrs> terminalParametersAttrs;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Terminalparameters(String name, String fileName) {
		this.name = name;
		this.fileName = fileName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TerminalParametersAttrs> getTerminalParametersAttrs() {
		return terminalParametersAttrs;
	}

	public void setTerminalParametersAttrs(List<TerminalParametersAttrs> terminalParametersAttrs) {
		this.terminalParametersAttrs = terminalParametersAttrs;
	}

}
