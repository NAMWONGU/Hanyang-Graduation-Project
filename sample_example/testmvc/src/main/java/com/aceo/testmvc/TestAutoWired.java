package com.aceo.testmvc;

import org.springframework.stereotype.Component;

@Component
public class TestAutoWired {
	
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
