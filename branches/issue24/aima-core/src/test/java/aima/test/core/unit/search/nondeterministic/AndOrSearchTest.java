package aima.test.core.unit.search.nondeterministic;

import aima.core.agent.Action;
import aima.core.agent.Agent;
import aima.core.environment.vacuum.NondeterministicVacuumAgent;
import aima.core.environment.vacuum.NondeterministicVacuumEnvironment;
import aima.core.environment.vacuum.VacuumEnvironment;
import aima.core.environment.vacuum.VacuumEnvironmentState;
import aima.core.search.framework.ActionsFunction;
import aima.core.search.framework.GoalTest;
import aima.core.search.framework.StepCostFunction;
import aima.core.search.nondeterministic.*;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the AND-OR search algorithm using the erratic vacuum world of page 133,
 * AIMAv3. In essence, a two-square office is cleaned by a vacuum that randomly
 * (1) cleans the square, (2) cleans both squares, or (3) dirties the square it
 * meant to clean.
 *
 * @author Andrew Brown Brown
 */
public class AndOrSearchTest {

    NondeterministicVacuumAgent agent;
    NondeterministicVacuumEnvironment world;
    NondeterministicProblem problem;

    /**
     * Create the vacuum world with the classes defined in this file.
     */
    @Before
    public void setUp() {
        this.agent = new NondeterministicVacuumAgent();
        // create world
        this.world = new NondeterministicVacuumEnvironment(VacuumEnvironment.LocationState.Dirty, VacuumEnvironment.LocationState.Dirty);
        this.world.addAgent(this.agent, VacuumEnvironment.LOCATION_A);
        // create state: both rooms are dirty and the vacuum is in room A
        VacuumEnvironmentState state = new VacuumEnvironmentState();
        state.setLocationState(VacuumEnvironment.LOCATION_A, VacuumEnvironment.LocationState.Dirty);
        state.setLocationState(VacuumEnvironment.LOCATION_B, VacuumEnvironment.LocationState.Dirty);
        state.setAgentLocation(this.agent, VacuumEnvironment.LOCATION_A);
        // create problem
        this.problem = new NondeterministicProblem(
                state,
                new VacuumWorldActions(),
                new VacuumWorldResults(this.agent),
                new VacuumWorldGoalTest(this.agent),
                new VacuumWorldStepCost());
    }

    /**
     * Test whether two identically-initialized states will equals() each other.
     */
    @Test
    public void testStateEquality() {
        // create state 1
        VacuumEnvironmentState s1 = new VacuumEnvironmentState();
        s1.setLocationState(VacuumEnvironment.LOCATION_A, VacuumEnvironment.LocationState.Dirty);
        s1.setLocationState(VacuumEnvironment.LOCATION_B, VacuumEnvironment.LocationState.Dirty);
        s1.setAgentLocation(this.agent, VacuumEnvironment.LOCATION_A);
        // create state 2
        VacuumEnvironmentState s2 = new VacuumEnvironmentState();
        s2.setLocationState(VacuumEnvironment.LOCATION_A, VacuumEnvironment.LocationState.Dirty);
        s2.setLocationState(VacuumEnvironment.LOCATION_B, VacuumEnvironment.LocationState.Dirty);
        s2.setAgentLocation(this.agent, VacuumEnvironment.LOCATION_A);
        // test
        boolean expected = true;
        boolean actual = s1.equals(s2);
        Assert.assertEquals(expected, actual);
    }

    /**
     * Test whether a Path contains() a state; uses state enumeration from page
     * 134, AIMAv3.
     */
    @Test
    public void testPathContains() {
        // create state 1
        VacuumEnvironmentState s1 = new VacuumEnvironmentState();
        s1.setLocationState(VacuumEnvironment.LOCATION_A, VacuumEnvironment.LocationState.Dirty);
        s1.setLocationState(VacuumEnvironment.LOCATION_B, VacuumEnvironment.LocationState.Dirty);
        s1.setAgentLocation(this.agent, VacuumEnvironment.LOCATION_A);
        // create state 2
        VacuumEnvironmentState s2 = new VacuumEnvironmentState();
        s2.setLocationState(VacuumEnvironment.LOCATION_A, VacuumEnvironment.LocationState.Dirty);
        s2.setLocationState(VacuumEnvironment.LOCATION_B, VacuumEnvironment.LocationState.Dirty);
        s2.setAgentLocation(this.agent, VacuumEnvironment.LOCATION_B);
        // create state 3
        VacuumEnvironmentState s3 = new VacuumEnvironmentState();
        s3.setLocationState(VacuumEnvironment.LOCATION_A, VacuumEnvironment.LocationState.Dirty);
        s3.setLocationState(VacuumEnvironment.LOCATION_B, VacuumEnvironment.LocationState.Clean);
        s3.setAgentLocation(this.agent, VacuumEnvironment.LOCATION_A);
        // create state 4
        VacuumEnvironmentState s4 = new VacuumEnvironmentState();
        s4.setLocationState(VacuumEnvironment.LOCATION_A, VacuumEnvironment.LocationState.Dirty);
        s4.setLocationState(VacuumEnvironment.LOCATION_B, VacuumEnvironment.LocationState.Clean);
        s4.setAgentLocation(this.agent, VacuumEnvironment.LOCATION_B);
        // create test state 1
        VacuumEnvironmentState test1 = new VacuumEnvironmentState();
        test1.setLocationState(VacuumEnvironment.LOCATION_A, VacuumEnvironment.LocationState.Dirty);
        test1.setLocationState(VacuumEnvironment.LOCATION_B, VacuumEnvironment.LocationState.Clean);
        test1.setAgentLocation(this.agent, VacuumEnvironment.LOCATION_A);
        // create test state 2
        VacuumEnvironmentState test2 = new VacuumEnvironmentState();
        test2.setLocationState(VacuumEnvironment.LOCATION_A, VacuumEnvironment.LocationState.Clean);
        test2.setLocationState(VacuumEnvironment.LOCATION_B, VacuumEnvironment.LocationState.Clean);
        test2.setAgentLocation(this.agent, VacuumEnvironment.LOCATION_B);
        // add to path
        Path path = new Path();
        path.append(s1, s2, s3, s4);
        // test
        Assert.assertEquals(true, path.contains(test1));
        Assert.assertEquals(false, path.contains(test2));
    }

    /**
     * Use AND-OR search to create a contingency plan; execute the plan and
     * verify that it successfully cleans the NondeterministicVacuumWorld.
     */
    @Test
    public void testSearchExecutesSuccessfully() {
        AndOrSearch s = new AndOrSearch();
        Plan plan = null;
        try {
            plan = s.search(this.problem);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // execute plan
        this.agent.setContingencyPlan(plan);
        // StringBuilder sb = new StringBuilder();
        // this.world.addEnvironmentView(new EnvironmentViewActionTracker(sb));
        this.world.stepUntilDone();
        // System.out.println("Plan: "+plan);
        // System.out.println("Actions Taken: "+sb);
        // test
        VacuumEnvironmentState endState = (VacuumEnvironmentState) this.world.getCurrentState();
        VacuumEnvironment.LocationState a = endState.getLocationState(VacuumEnvironment.LOCATION_A);
        VacuumEnvironment.LocationState b = endState.getLocationState(VacuumEnvironment.LOCATION_B);
        Assert.assertEquals(VacuumEnvironment.LocationState.Clean, a);
        Assert.assertEquals(VacuumEnvironment.LocationState.Clean, b);
    }

}

/**
 * Specifies the actions available to the agent at state s
 *
 * @author Andrew Brown
 */
class VacuumWorldActions implements ActionsFunction {

    public static Set<Action> actions = new HashSet<Action>();

    /**
     * Returns possible actions given this state
     *
     * @param s
     * @return
     */
    @Override
    public Set<Action> actions(Object s) {
        if (VacuumWorldActions.actions.isEmpty()) {
            VacuumWorldActions.actions.add(VacuumEnvironment.ACTION_SUCK);
            VacuumWorldActions.actions.add(VacuumEnvironment.ACTION_MOVE_LEFT);
            VacuumWorldActions.actions.add(VacuumEnvironment.ACTION_MOVE_RIGHT);
        }
        return VacuumWorldActions.actions;
    }
}

/**
 * Returns possible results
 *
 * @author Andrew Brown Brown
 */
class VacuumWorldResults implements ResultsFunction {

    public Agent agent;

    /**
     * Constructor
     *
     * @param agent
     */
    public VacuumWorldResults(Agent agent) {
        this.agent = agent;
    }

    /**
     * Returns a list of possible results for a given state and action
     *
     * @param _state
     * @param action
     * @return
     */
    @Override
    public Set<Object> results(Object _state, Action action) {
        // setup
        VacuumEnvironmentState state = (VacuumEnvironmentState) _state;
        Set<Object> results = new HashSet<Object>();
        String current_location = state.getAgentLocation(agent);
        String adjacent_location = (current_location.equals(VacuumEnvironment.LOCATION_A)) ? VacuumEnvironment.LOCATION_B : VacuumEnvironment.LOCATION_A;
        //
        if (VacuumEnvironment.ACTION_MOVE_RIGHT == action) {
            VacuumEnvironmentState s = new VacuumEnvironmentState();
            s.setLocationState(current_location, state.getLocationState(current_location));
            s.setLocationState(adjacent_location, state.getLocationState(adjacent_location));
            s.setAgentLocation(this.agent, VacuumEnvironment.LOCATION_B);
            results.add(s);
        } else if (VacuumEnvironment.ACTION_MOVE_LEFT == action) {
            VacuumEnvironmentState s = new VacuumEnvironmentState();
            s.setLocationState(current_location, state.getLocationState(current_location));
            s.setLocationState(adjacent_location, state.getLocationState(adjacent_location));
            s.setAgentLocation(this.agent, VacuumEnvironment.LOCATION_B);
            results.add(s);
        } else if (VacuumEnvironment.ACTION_SUCK == action) {
            // case: square is dirty
            if (VacuumEnvironment.LocationState.Dirty == state.getLocationState(state.getAgentLocation(this.agent))) {
                // always clean current
                VacuumEnvironmentState s1 = new VacuumEnvironmentState();
                s1.setLocationState(current_location, VacuumEnvironment.LocationState.Clean);
                s1.setLocationState(adjacent_location, state.getLocationState(adjacent_location));
                s1.setAgentLocation(this.agent, current_location);
                results.add(s1);
                // sometimes clean adjacent as well
                VacuumEnvironmentState s2 = new VacuumEnvironmentState();
                s2.setLocationState(current_location, VacuumEnvironment.LocationState.Clean);
                s2.setLocationState(adjacent_location, VacuumEnvironment.LocationState.Clean);
                s2.setAgentLocation(this.agent, current_location);
                results.add(s2);
            } // case: square is clean
            else {
                // sometimes do nothing
                VacuumEnvironmentState s1 = new VacuumEnvironmentState();
                s1.setLocationState(current_location, state.getLocationState(current_location));
                s1.setLocationState(adjacent_location, state.getLocationState(adjacent_location));
                s1.setAgentLocation(this.agent, current_location);
                results.add(s1);
                // sometimes deposit dirt
                VacuumEnvironmentState s2 = new VacuumEnvironmentState();
                s2.setLocationState(current_location, VacuumEnvironment.LocationState.Dirty);
                s2.setLocationState(adjacent_location, state.getLocationState(adjacent_location));
                s2.setAgentLocation(this.agent, current_location);
                results.add(s2);
            }
        } else if (action.isNoOp()) {
            // do nothing
        }

        return results;
    }
}

/**
 * Tests for goals states
 *
 * @author Andrew Brown Brown
 */
class VacuumWorldGoalTest implements GoalTest {

    public Agent agent;

    /**
     * Constructor
     *
     * @param agent
     */
    public VacuumWorldGoalTest(Agent agent) {
        this.agent = agent;
    }

    /**
     * Tests whether the search has identified a goal state
     *
     * @param _state
     * @return
     */
    @Override
    public boolean isGoalState(Object _state) {
        // setup
        VacuumEnvironmentState state = (VacuumEnvironmentState) _state;
        String current_location = state.getAgentLocation(this.agent);
        String adjacent_location = (current_location.equals(VacuumEnvironment.LOCATION_A)) ? VacuumEnvironment.LOCATION_B : VacuumEnvironment.LOCATION_A;
        // test goal state
        if (VacuumEnvironment.LocationState.Clean != state.getLocationState(current_location)) {
            return false;
        } else if (VacuumEnvironment.LocationState.Clean != state.getLocationState(adjacent_location)) {
            return false;
        } else {
            return true;
        }
    }
}

/**
 * Measures step cost
 *
 * @author Andrew Brown Brown
 */
class VacuumWorldStepCost implements StepCostFunction {

    public double c(Object state, Action action, Object sDelta) {
        return 1.0;
    }
}
