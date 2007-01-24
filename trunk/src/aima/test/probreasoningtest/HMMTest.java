package aima.test.probreasoningtest;

import junit.framework.TestCase;
import aima.probability.reasoning.HMMFactory;
import aima.probability.reasoning.HMMAgent;
import aima.probability.reasoning.HiddenMarkovModel;
import aima.probability.reasoning.HmmConstants;

public class HMMTest extends TestCase {
	private static final double TOLERANCE = 0.001;
	private HMMAgent robot,rainman;

	public void setUp(){

		robot = new HMMAgent(HMMFactory.createRobotHMM());
		rainman = new HMMAgent(HMMFactory.createRainmanHMM());
	}

	
	public void testRobotHMMInitialization(){
		HiddenMarkovModel robotHmm = HMMFactory.createRobotHMM();
		assertEquals(0.5, robotHmm.prior().getProbabilityOf(HmmConstants.DOOR_OPEN));
		assertEquals(0.5, robotHmm.prior().getProbabilityOf(HmmConstants.DOOR_CLOSED));
	}
	
	public void testRainmanHmmInitialization(){
		HiddenMarkovModel rainmanHmm = HMMFactory.createRainmanHMM();
		assertEquals(0.5, rainmanHmm.prior().getProbabilityOf(HmmConstants.RAINING));
		assertEquals(0.5, rainmanHmm.prior().getProbabilityOf(HmmConstants.NOT_RAINING));
	}
	
	public void testRobotInitialization(){		
		assertEquals(0.5, robot.belief().getProbabilityOf(HmmConstants.DOOR_OPEN));
		assertEquals(0.5, robot.belief().getProbabilityOf(HmmConstants.DOOR_CLOSED));
	}
	
	public void testRobotHMMPredictionAndMeasurementUpdateStepsModifyBeliefCorrectly(){
		
		assertEquals(0.5, robot.belief().getProbabilityOf(HmmConstants.DOOR_OPEN));
		assertEquals(0.5, robot.belief().getProbabilityOf(HmmConstants.DOOR_CLOSED));
		
		robot.act(HmmConstants.DO_NOTHING);
		assertEquals(0.5, robot.belief().getProbabilityOf(HmmConstants.DOOR_OPEN));
		assertEquals(0.5, robot.belief().getProbabilityOf(HmmConstants.DOOR_CLOSED));
		
		robot.perceive(HmmConstants.SEE_DOOR_OPEN);
		assertEquals(0.75, robot.belief().getProbabilityOf(HmmConstants.DOOR_OPEN),TOLERANCE);
		assertEquals(0.25, robot.belief().getProbabilityOf(HmmConstants.DOOR_CLOSED),TOLERANCE);
		
		robot.act(HmmConstants.PUSH_DOOR);
		assertEquals(0.95, robot.belief().getProbabilityOf(HmmConstants.DOOR_OPEN));
		assertEquals(0.05, robot.belief().getProbabilityOf(HmmConstants.DOOR_CLOSED));
		
		robot.perceive(HmmConstants.SEE_DOOR_OPEN);
		assertEquals(0.983, robot.belief().getProbabilityOf(HmmConstants.DOOR_OPEN),TOLERANCE);
		assertEquals(0.017, robot.belief().getProbabilityOf(HmmConstants.DOOR_CLOSED),TOLERANCE);
		
		
	}
	
	public void testRainmanInitialization(){
		assertEquals(0.5, rainman.belief().getProbabilityOf(HmmConstants.RAINING));
		assertEquals(0.5, rainman.belief().getProbabilityOf(HmmConstants.NOT_RAINING));
	}
	
	public void testRainmanHMMPredictionAndMeasurementUpdateStepsModifyBeliefCorrectly(){
		assertEquals(0.5, rainman.belief().getProbabilityOf(HmmConstants.RAINING));
		assertEquals(0.5, rainman.belief().getProbabilityOf(HmmConstants.NOT_RAINING));
		
		rainman.waitForPerception();
		assertEquals(0.5, rainman.belief().getProbabilityOf(HmmConstants.RAINING));
		assertEquals(0.5, rainman.belief().getProbabilityOf(HmmConstants.NOT_RAINING));
		
		rainman.perceive(HmmConstants.SEE_UMBRELLA);
		assertEquals(0.818, rainman.belief().getProbabilityOf(HmmConstants.RAINING),TOLERANCE);
		assertEquals(0.182, rainman.belief().getProbabilityOf(HmmConstants.NOT_RAINING),TOLERANCE);

		rainman.waitForPerception();
		assertEquals(0.627, rainman.belief().getProbabilityOf(HmmConstants.RAINING),TOLERANCE);
		assertEquals(0.373, rainman.belief().getProbabilityOf(HmmConstants.NOT_RAINING),TOLERANCE);
		
		rainman.perceive(HmmConstants.SEE_UMBRELLA);
		assertEquals(0.883, rainman.belief().getProbabilityOf(HmmConstants.RAINING),TOLERANCE);
		assertEquals(0.117, rainman.belief().getProbabilityOf(HmmConstants.NOT_RAINING),TOLERANCE);
		
		
	}
}
