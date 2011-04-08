package aima.core.probability.proposed.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import aima.core.probability.proposed.model.domain.FiniteDomain;
import aima.core.probability.proposed.model.proposition.AssignmentProposition;
import aima.core.util.SetOps;
import aima.core.util.math.MixedRadixNumber;

/**
 * Artificial Intelligence A Modern Approach (3rd Edition): page 487.
 * 
 * A vector of numbers, where we assume a predefined ordering on the domain(s)
 * of the Random Variables used to create the distribution.
 * 
 * This class is used to represent both Probability and Joint Probability
 * distributions for finite domains.
 * 
 * @author Ciaran O'Reilly
 */
public class Distribution {
	private double[] distribution = null;
	//
	private Map<RandomVariable, RVInfo> randomVarInfo = new LinkedHashMap<RandomVariable, RVInfo>();
	private int[] radixs = null;
	//
	private String toString = null;
	private double sum = -1;

	public interface Iterator {
		void iterate(Map<RandomVariable, Object> possibleWorld,
				double probability);

		Object getPostIterateValue();
	}

	public static int expectedSizeOfDistribution(RandomVariable... vars) {
		// initially 1, as this will represent constant assignments
		// e.g. Dice1 = 1.
		int expectedSizeOfDistribution = 1;
		if (null != vars) {
			for (RandomVariable rv : vars) {
				// Create ordered domains for each variable
				if (!(rv.getDomain() instanceof FiniteDomain)) {
					throw new IllegalArgumentException(
							"Cannot have an infinite domain for a variable in a Distribution:"
									+ rv);
				}
				FiniteDomain d = (FiniteDomain) rv.getDomain();
				expectedSizeOfDistribution *= d.size();
			}
		}

		return expectedSizeOfDistribution;
	}

	public Distribution(Collection<RandomVariable> vars) {
		this(vars.toArray(new RandomVariable[vars.size()]));
	}

	public Distribution(RandomVariable... vars) {
		this(new double[expectedSizeOfDistribution(vars)], vars);
	}

	public Distribution(double[] values, RandomVariable... vars) {
		if (null == values) {
			throw new IllegalArgumentException(
					"Distribution values must be specified");
		}
		if (values.length != expectedSizeOfDistribution(vars)) {
			throw new IllegalArgumentException("Distribution of length "
					+ distribution.length
					+ " is not the correct size, should be "
					+ expectedSizeOfDistribution(vars)
					+ " in order to represent all possible combinations.");
		}
		if (null != vars) {
			for (RandomVariable rv : vars) {
				// Track index information relevant to each variable.
				randomVarInfo.put(rv, new RVInfo(rv));
			}
		}

		distribution = new double[values.length];
		System.arraycopy(values, 0, distribution, 0, values.length);

		radixs = new int[randomVarInfo.size()];
		// Read in reverse order so that the enumeration
		// through the distributions is of the following
		// order using a MixedRadixNumber, e.g. for two Booleans:
		// X Y
		// true true
		// true false
		// false true
		// false false
		// which corresponds with how displayed in book.
		int x = randomVarInfo.size() - 1;
		for (RVInfo rvInfo : randomVarInfo.values()) {
			radixs[x] = rvInfo.getDomainSize();
			rvInfo.setRadixIdx(x);
			x--;
		}
	}

	// Note: Document that callers of this method should not change
	// the values of the array returned directly but should
	// instead use setValue().
	public double[] getValues() {
		return distribution;
	}

	public int getIndex(Object... values) {
		if (values.length != randomVarInfo.size()) {
			throw new IllegalArgumentException(
					"Values passed in is not the same size as variables making up distribution.");
		}
		int[] radixValues = new int[values.length];
		int i = 0;
		for (RVInfo rvInfo : randomVarInfo.values()) {
			radixValues[rvInfo.getRadixIdx()] = rvInfo
					.getIdxForDomain(values[i]);
			i++;
		}

		MixedRadixNumber mrn = new MixedRadixNumber(radixValues, radixs);
		return mrn.intValue();
	}

	public void setValue(int idx, double value) {
		distribution[idx] = value;
		reinitLazyValues();
	}

	public double getValueFor(AssignmentProposition... values) {
		if (values.length != randomVarInfo.size()) {
			throw new IllegalArgumentException(
					"Values passed in is not the same size as variables making up distribution.");
		}
		int[] radixValues = new int[values.length];
		for (AssignmentProposition ap : values) {
			RVInfo rvInfo = randomVarInfo.get(ap.getTermVariable());
			if (null == rvInfo) {
				throw new IllegalArgumentException(
						"Values passed for a variable that is not part of this distribution:"
								+ ap.getTermVariable());
			}
			radixValues[rvInfo.getRadixIdx()] = rvInfo.getIdxForDomain(ap
					.getValue());
		}
		MixedRadixNumber mrn = new MixedRadixNumber(radixValues, radixs);
		return distribution[mrn.intValue()];
	}

	public double getSum() {
		if (-1 == sum) {
			sum = 0;
			for (int i = 0; i < distribution.length; i++) {
				sum += distribution[i];
			}
		}
		return sum;
	}

	public Distribution divideBy(final Distribution divisor) {
		Distribution rVal = null;

		if (1 == divisor.getValues().length) {
			double d = divisor.getValues()[0];
			rVal = new Distribution(randomVarInfo.keySet());
			for (int i = 0; i < rVal.getValues().length; i++) {
				if (0 == d) {
					rVal.getValues()[i] = 0;
				} else {
					rVal.getValues()[i] = getValues()[i] / d;
				}
			}
		} else {
			if (randomVarInfo.keySet().containsAll(
					divisor.randomVarInfo.keySet())) {
				final int sizeDivisor = divisor.getValues().length;
				final int quotient = getValues().length / sizeDivisor;
				final Distribution qd = new Distribution(randomVarInfo.keySet());
				Distribution.Iterator di = new Distribution.Iterator() {
					private int pos = 0;

					public void iterate(
							Map<RandomVariable, Object> possibleWorld,
							double probability) {
						for (int i = 0; i < quotient; i++) {
							int offset = pos + (sizeDivisor * i);
							if (0 == probability) {
								qd.getValues()[offset] = 0;
							} else {
								qd.getValues()[offset] += getValues()[offset]
										/ probability;
							}
						}
						pos++;
					}

					public Object getPostIterateValue() {
						return null; // N/A
					}
				};
				divisor.iterateDistribution(di);

				rVal = qd;
			} else {
				throw new IllegalArgumentException(
						"Divisor must be a subset of the dividend.");
			}
		}

		return rVal;
	}

	public Distribution multiplyBy(final Distribution multiplier) {
		Distribution rVal = null;

		if (1 == multiplier.getValues().length) {
			double m = multiplier.getValues()[0];
			rVal = new Distribution(randomVarInfo.keySet());
			for (int i = 0; i < rVal.getValues().length; i++) {
				rVal.getValues()[i] = getValues()[i] * m;
			}
		} else {
			final Distribution product = new Distribution(SetOps.union(SetOps
					.difference(randomVarInfo.keySet(),
							multiplier.randomVarInfo.keySet()),
					multiplier.randomVarInfo.keySet()));
			final Object[] term1Values = new Object[randomVarInfo.size()];
			final Object[] term2Values = new Object[multiplier.randomVarInfo
					.size()];
			Distribution.Iterator di = new Distribution.Iterator() {
				private int idx = 0;

				public void iterate(Map<RandomVariable, Object> possibleWorld,
						double probability) {
					popTermValues(term1Values, Distribution.this, possibleWorld);
					popTermValues(term2Values, multiplier, possibleWorld);

					product.getValues()[idx] = getValues()[getIndex(term1Values)]
							* multiplier.getValues()[multiplier
									.getIndex(term2Values)];

					idx++;
				}

				public Object getPostIterateValue() {
					return null; // N/A
				}

				private void popTermValues(Object[] termValues, Distribution d,
						Map<RandomVariable, Object> possibleWorld) {

					int i = 0;
					for (RandomVariable rv : d.randomVarInfo.keySet()) {
						termValues[i] = possibleWorld.get(rv);
						i++;
					}
				}
			};
			product.iterateDistribution(di);

			rVal = product;
		}

		return rVal;
	}

	public Distribution normalize() {
		double s = getSum();
		if (s != 0) {
			for (int i = 0; i < distribution.length; i++) {
				distribution[i] = distribution[i] / s;
			}
			reinitLazyValues();
		}
		return this;
	}

	public void iterateDistribution(Iterator di) {
		Map<RandomVariable, Object> possibleWorld = new LinkedHashMap<RandomVariable, Object>();
		MixedRadixNumber mrn = new MixedRadixNumber(0, radixs);
		do {
			for (RVInfo rvInfo : randomVarInfo.values()) {
				possibleWorld.put(rvInfo.getVariable(), rvInfo
						.getDomainValueAt(mrn.getCurrentNumeralValue(rvInfo
								.getRadixIdx())));
			}
			di.iterate(possibleWorld, distribution[mrn.intValue()]);

		} while (mrn.increment());
	}

	@Override
	public String toString() {
		if (null == toString) {
			StringBuilder sb = new StringBuilder();
			sb.append("<");
			for (int i = 0; i < distribution.length; i++) {
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(distribution[i]);
			}
			sb.append(">");

			toString = sb.toString();
		}
		return toString;
	}

	//
	// PRIVATE METHODS
	//
	private void reinitLazyValues() {
		sum = -1;
		toString = null;
	}

	private class RVInfo {
		private RandomVariable variable;
		private Map<Integer, Object> idxDomainMap = new HashMap<Integer, Object>();
		private Map<Object, Integer> domainIdxMap = new HashMap<Object, Integer>();
		private int radixIdx = 0;

		public RVInfo(RandomVariable rv) {
			variable = rv;
			int idx = 0;
			for (Object pv : ((FiniteDomain) variable.getDomain())
					.getPossibleValues()) {
				domainIdxMap.put(pv, idx);
				idxDomainMap.put(idx, pv);
				idx++;
			}
		}

		public RandomVariable getVariable() {
			return variable;
		}

		public int getDomainSize() {
			return domainIdxMap.size();
		}

		public int getIdxForDomain(Object value) {
			return domainIdxMap.get(value);
		}

		public Object getDomainValueAt(int idx) {
			return idxDomainMap.get(idx);
		}

		public void setRadixIdx(int idx) {
			radixIdx = idx;
		}

		public int getRadixIdx() {
			return radixIdx;
		}
	}
}