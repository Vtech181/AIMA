package aima.test.logictest.foltest;

import junit.framework.TestCase;
import aima.logic.fol.CNFConverter;
import aima.logic.fol.domain.FOLDomain;
import aima.logic.fol.kb.data.CNF;
import aima.logic.fol.parsing.DomainFactory;
import aima.logic.fol.parsing.FOLParser;
import aima.logic.fol.parsing.ast.Sentence;

/**
 * @author Ciaran O'Reilly
 * 
 */
// TODO : more tests!
public class CNFConverterTest extends TestCase {

	public void testExamplePg295() {
		FOLDomain domain = DomainFactory.weaponsDomain();
		FOLParser parser = new FOLParser(domain);

		Sentence origSentence = parser
				.parse("FORALL x ((((American(x) AND Weapon(y)) AND Sells(x, y, z)) AND Hostile(z)) => Criminal(x))");

		CNFConverter cnfConv = new CNFConverter(parser);

		CNF cnf = cnfConv.convertToCNF(origSentence);

		assertEquals(
				"{~American(x),~Weapon(y),~Sells(x,y,z),~Hostile(z),Criminal(x)}",
				cnf.toString());
	}

	public void testExamplePg296() {
		FOLDomain domain = DomainFactory.lovesAnimalDomain();
		FOLParser parser = new FOLParser(domain);

		Sentence origSentence = parser
				.parse("FORALL x (FORALL y (Animal(y) => Loves(x, y)) => EXISTS y Loves(y, x))");

		CNFConverter cnfConv = new CNFConverter(parser);

		CNF cnf = cnfConv.convertToCNF(origSentence);

		assertEquals(
				"{Animal(SF0(x)),Loves(SF1(x),x)},{~Loves(x,SF0(x)),Loves(SF1(x),x)}",
				cnf.toString());
	}

	public void testExamplesPg299() {
		FOLDomain domain = DomainFactory.lovesAnimalDomain();
		FOLParser parser = new FOLParser(domain);

		// FOL A.
		Sentence origSentence = parser
				.parse("FORALL x (FORALL y (Animal(y) => Loves(x, y)) => EXISTS y Loves(y, x))");

		CNFConverter cnfConv = new CNFConverter(parser);

		CNF cnf = cnfConv.convertToCNF(origSentence);

		// CNF A1. and A2.
		assertEquals(
				"{Animal(SF0(x)),Loves(SF1(x),x)},{~Loves(x,SF0(x)),Loves(SF1(x),x)}",
				cnf.toString());

		// FOL B.
		origSentence = parser
				.parse("FORALL x (EXISTS y (Animal(y) AND Kills(x, y)) => FORALL z NOT(Loves(z, x)))");

		cnf = cnfConv.convertToCNF(origSentence);

		// CNF B.
		assertEquals(
				"{~Animal(y),~Kills(x,y),~Loves(z,x)}",
				cnf.toString());

		// FOL C.
		origSentence = parser.parse("FORALL x (Animal(x) => Loves(Jack, x))");

		cnf = cnfConv.convertToCNF(origSentence);

		// CNF C.
		assertEquals("{~Animal(x),Loves(Jack,x)}", cnf
				.toString());

		// FOL D.
		origSentence = parser
				.parse("(Kills(Jack, Tuna) OR Kills(Curiosity, Tuna))");

		cnf = cnfConv.convertToCNF(origSentence);

		// CNF D.
		assertEquals("{Kills(Jack,Tuna),Kills(Curiosity,Tuna)}", cnf
				.toString());

		// FOL E.
		origSentence = parser.parse("Cat(Tuna)");

		cnf = cnfConv.convertToCNF(origSentence);

		// CNF E.
		assertEquals("{Cat(Tuna)}", cnf.toString());

		// FOL F.
		origSentence = parser.parse("FORALL x (Cat(x) => Animal(x))");

		cnf = cnfConv.convertToCNF(origSentence);

		// CNF F.
		assertEquals("{~Cat(x),Animal(x)}", cnf.toString());

		// FOL G.
		origSentence = parser.parse("NOT(Kills(Curiosity, Tuna))");

		cnf = cnfConv.convertToCNF(origSentence);

		// CNF G.
		assertEquals("{~Kills(Curiosity,Tuna)}", cnf.toString());
	}

	public void testNestedExistsAndOrs() {
		FOLDomain domain = new FOLDomain();
		domain.addPredicate("P");
		domain.addPredicate("R");
		domain.addPredicate("Q");

		FOLParser parser = new FOLParser(domain);

		Sentence origSentence = parser
				.parse("EXISTS w (FORALL x ( (EXISTS z (Q(w, z))) => (EXISTS y (NOT(P(x, y)) AND R(y))) ) )");

		CNFConverter cnfConv = new CNFConverter(parser);

		CNF cnf = cnfConv.convertToCNF(origSentence);
		
		assertEquals("{~Q(SC0,z),~P(x,SF0(x))},{~Q(SC0,z),R(SF0(x))}", cnf
				.toString());

		// Ax.Ay.(p(x,y) => Ez.(q(x,y,z)))
		origSentence = parser
				.parse("FORALL x1 (FORALL y1 (P(x1, y1) => EXISTS z1 (Q(x1, y1, z1))))");

		cnf = cnfConv.convertToCNF(origSentence);

		assertEquals("{~P(x1,y1),Q(x1,y1,SF1(x1,y1))}", cnf.toString());

		// Ex.Ay.Az.(r(y,z) <=> q(x,y,z))
		origSentence = parser
				.parse("EXISTS x2 (FORALL y2 (FORALL z2 (R(y2, z2) <=> Q(x2, y2, z2))))");

		cnf = cnfConv.convertToCNF(origSentence);

		assertEquals("{~R(y2,z2),Q(SC1,y2,z2)},{~Q(SC1,y2,z2),R(y2,z2)}", cnf
				.toString());
		
		// Ax.Ey.(~p(x,y) => Az.(q(x,y,z)))
		origSentence = parser
				.parse("FORALL x3 (EXISTS y3 (NOT(P(x3, y3)) => FORALL z3 (Q(x3, y3, z3))))");

		cnf = cnfConv.convertToCNF(origSentence);

		assertEquals("{P(x3,SF2(x3)),Q(x3,SF2(x3),z3)}", cnf.toString());
		
		// Ew.Ex.Ey.Ez.(r(x,y) & q(x,w,z))
		origSentence = parser
				.parse("NOT(EXISTS w4 (EXISTS x4 (EXISTS y4 ( EXISTS z4 (R(x4, y4) AND Q(x4, w4, z4))))))");

		cnf = cnfConv.convertToCNF(origSentence);

		assertEquals("{~R(x4,y4),~Q(x4,w4,z4)}", cnf.toString());
	}

	public void testImplicationsAndExtendedAndsOrs() {
		FOLDomain domain = new FOLDomain();
		domain.addPredicate("Cheat");
		domain.addPredicate("Extra");
		domain.addPredicate("Knows");
		domain.addPredicate("Diff");
		domain.addPredicate("F");
		domain.addPredicate("A");
		domain.addPredicate("Probation");
		domain.addPredicate("Award");

		FOLParser parser = new FOLParser(domain);
		CNFConverter cnfConv = new CNFConverter(parser);

		// cheat(x,y) => f(x,y)
		Sentence def1 = parser.parse("(Cheat(x,y) => F(x,y))");
		CNF cnfDef1 = cnfConv.convertToCNF(def1);
		
		assertEquals("{~Cheat(x,y),F(x,y)}", cnfDef1.toString());
		
		// extra(x,y) | knows(x) => a(x,y)
		Sentence def2 = parser.parse("((Extra(x,y) OR Knows(x)) => A(x,y))");
		CNF cnfDef2 = cnfConv.convertToCNF(def2);

		assertEquals("{~Extra(x,y),A(x,y)},{~Knows(x),A(x,y)}", cnfDef2
				.toString());
		
		// f(x,y) & f(x,z) & diff(y,z) <=> probation(x)
		Sentence def3 = parser
				.parse("(((NOT(((F(x,y) AND F(x,z)) AND Diff(y,z)))) OR Probation(x)) AND (((F(x,y) AND F(x,z)) AND Diff(y,z)) OR NOT(Probation(x))))");
		CNF cnfDef3 = cnfConv.convertToCNF(def3);

		assertEquals(
				"{~F(x,y),~F(x,z),~Diff(y,z),Probation(x)},{~Probation(x),F(x,y)},{~Probation(x),F(x,z)},{~Probation(x),Diff(y,z)}",
				cnfDef3.toString());
		
		// a(x,y) & a(x,z) & diff(y,z) <=> award(x)
		Sentence def4 = parser
				.parse("(((NOT(((A(x,y) AND A(x,z)) AND Diff(y,z)))) OR Award(x)) AND (((A(x,y) AND A(x,z)) AND Diff(y,z)) OR NOT(Award(x))))");
		CNF cnfDef4 = cnfConv.convertToCNF(def4);

		assertEquals(
				"{~A(x,y),~A(x,z),~Diff(y,z),Award(x)},{~Award(x),A(x,y)},{~Award(x),A(x,z)},{~Award(x),Diff(y,z)}",
				cnfDef4.toString());
		
		// f(x,y) <=> ~a(x,y)
		Sentence def5 = parser
				.parse("( ( NOT(F(x,y)) OR NOT(A(x,y))) AND ( F(x,y) OR NOT(NOT(A(x,y))) ) )");
		CNF cnfDef5 = cnfConv.convertToCNF(def5);

		assertEquals("{~F(x,y),~A(x,y)},{F(x,y),A(x,y)}", cnfDef5.toString());
	}
}
