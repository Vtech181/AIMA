/*
 * Created on Sep 14, 2003 by Ravi Mohan
 *
 */
package aima.logic.fol.parsing.ast;

import aima.logic.fol.parsing.FOLVisitor;

public class Constant extends Term {
	private String value;

	public Constant(String s) {
		value = s;
	}

	public String getValue() {
		return value;
	}

	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}
		if ((o == null) || (this.getClass() != o.getClass())) {
			return false;
		}
		Constant c = (Constant) o;
		return (c.getValue().equals(getValue()));

	}

	public int hashCode() {
		int result = 17;
	    result = 37 * result + value.hashCode();
		
		return result;
	}

	public boolean renamingEquals(Sentence s) {
		return equals(s);
	}

	public String toString() {
		return value;
	}

	public Object accept(FOLVisitor v, Object arg) {
		return v.visitConstant(this, arg);
	}

	public Object clone() {
		return new Constant(value);
	}

	public Constant copy() {
		return new Constant(value);
	}

}
