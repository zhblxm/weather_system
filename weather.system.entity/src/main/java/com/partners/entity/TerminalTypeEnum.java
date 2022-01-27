package com.partners.entity;

public enum TerminalTypeEnum {
	PORTABLETERMINAL(1, "便携自动站"), INTELLIGENCETESTTERMINAL(2, "智能温湿仪"), MBBA2MONITORTERMINAL(3, "MA-BA2农业自动气象监测站"), MBBA1MONITORTERMINAL(4, "MA-BA1农业自动气象监测站");
	private final int terminalNumber;
	private final String description;

	TerminalTypeEnum(int terminalNumber, String description) {
		this.terminalNumber = terminalNumber;
		this.description = description;
	}

	public int getTerminalNumber() {
		return terminalNumber;
	}

	public String getDescription() {
		return description;
	}
}
