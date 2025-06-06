package com.mlprograms.justmath.bignumber;

import jdk.jfr.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigNumberTest {

	BigNumber bigNumber1;
	BigNumber bigNumber2;

	@BeforeEach
	public void setup() {
		bigNumber1 = new BigNumber("123456");
		bigNumber2 = new BigNumber("654321");
	}

	@Test
	@Name("Test BigNumber Addition")
	void additionTest() {
		BigNumber result = bigNumber1.add(bigNumber2);
		assertEquals("777777", result.toString());

		bigNumber1 = new BigNumber("0.000000000000001", Locale.US);
		bigNumber2 = new BigNumber("0.000000000000002", Locale.US);

		result = bigNumber1.add(bigNumber2);
		assertEquals("0.000000000000003", result.toString());
	}

}
