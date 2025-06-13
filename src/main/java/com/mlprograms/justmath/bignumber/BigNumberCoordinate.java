package com.mlprograms.justmath.bignumber;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class BigNumberCoordinate {

	@NonNull
	private BigNumber x;
	@NonNull
	private BigNumber y;

}
