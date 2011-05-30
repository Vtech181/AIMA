package aima.core.util.math;

import java.util.List;

/**
 * @see <a
 *      href="http://demonstrations.wolfram.com/MixedRadixNumberRepresentations/"
 *      >Mixed Radix Number Representations.</a>
 * @author Ciaran O'Reilly
 */
public class MixedRadixNumber extends Number {
	//
	private static final long serialVersionUID = 1L;
	//
	private long value = 0L;
	private long maxValue = 0L;
	private int[] radixs = null;
	private int[] currentNumeralValue = null;
	private boolean recalculate = true;

	public MixedRadixNumber(long value, int[] radixs) {
		this.value = value;
		this.radixs = new int[radixs.length];
		System.arraycopy(radixs, 0, this.radixs, 0, radixs.length);
		calculateMaxValue();
	}

	public MixedRadixNumber(long value, List<Integer> radixs) {
		this.value = value;
		this.radixs = new int[radixs.size()];
		for (int i = 0; i < radixs.size(); i++) {
			this.radixs[i] = radixs.get(i);
		}
		calculateMaxValue();
	}

	public MixedRadixNumber(int[] radixValues, int[] radixs) {
		this(0, radixs);
		setCurrentValueFor(radixValues);
	}

	public long getCurrentValueFor(int[] radixValues) {
		if (radixValues.length != radixs.length) {
			throw new IllegalArgumentException(
					"Radix values not same size as Radixs.");
		}

		long cvalue = 0;
		long mvalue = 1;
		for (int i = 0; i < radixValues.length; i++) {
			if (radixValues[i] < 0 || radixValues[i] > radixs[i]) {
				throw new IllegalArgumentException("Radix value " + i
						+ " is out of range for radix at this position");
			}
			if (i > 0) {
				mvalue *= radixs[i - 1];
			}
			cvalue += mvalue * radixValues[i];
		}
		return cvalue;
	}

	public void setCurrentValueFor(int[] radixValues) {
		this.value = getCurrentValueFor(radixValues);
		System.arraycopy(radixValues, 0, this.currentNumeralValue, 0,
				radixValues.length);
		recalculate = false;
	}

	public long getMaxAllowedValue() {
		return maxValue;
	}

	public boolean increment() {
		if (value < maxValue) {
			value++;
			recalculate = true;
			return true;
		}

		return false;
	}

	public boolean decrement() {
		if (value > 0) {
			value--;
			recalculate = true;
			return true;
		}
		return false;
	}

	public int getCurrentNumeralValue(int atPosition) {
		if (atPosition >= 0 && atPosition < radixs.length) {
			if (recalculate) {
				long quotient = value;
				for (int i = 0; i < radixs.length; i++) {
					if (0 != quotient) {
						currentNumeralValue[i] = (int) quotient % radixs[i];
						quotient = quotient / radixs[i];
					} else {
						currentNumeralValue[i] = 0;
					}

				}
				recalculate = false;
			}
			return currentNumeralValue[atPosition];
		}
		throw new IllegalArgumentException(
				"Argument atPosition must be >=0 and < " + radixs.length);
	}

	//
	// START-Number
	@Override
	public int intValue() {
		return (int) longValue();
	}

	@Override
	public long longValue() {
		return value;
	}

	@Override
	public float floatValue() {
		return longValue();
	}

	@Override
	public double doubleValue() {
		return longValue();
	}

	// END-Number
	//

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < radixs.length; i++) {
			sb.append("[");
			sb.append(this.getCurrentNumeralValue(i));
			sb.append("]");
		}

		return sb.toString();
	}

	//
	// PRIVATE
	//
	private void calculateMaxValue() {
		if (0 == radixs.length) {
			throw new IllegalArgumentException(
					"At least 1 radix must be defined.");
		}
		for (int i = 0; i < radixs.length; i++) {
			if (radixs[i] < 2) {
				throw new IllegalArgumentException(
						"Invalid radix, must be >= 2");
			}
		}

		// Calcualte the maxValue allowed
		maxValue = radixs[0];
		for (int i = 1; i < radixs.length; i++) {
			maxValue *= radixs[i];
		}
		maxValue -= 1;

		if (value > maxValue) {
			throw new IllegalArgumentException(
					"The value ["
							+ value
							+ "] cannot be represented with the radixs provided, max value is "
							+ maxValue);
		}

		currentNumeralValue = new int[radixs.length];
	}
}
