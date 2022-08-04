import java.util.*;
import java.lang.Thread;
public class ProbabilityCalculator {
    /**
     * Written by: David Bogoslavsky
     * This program runs the probability of winning a certain scenario written by the customer in the legendary game
     * called "Warlords" (dos, 1990). The probability check is based on the algorithm of battle in Warlords,
     * that goes like this: First, there can be 1-8 warriors in the attacking team, and 1-32 warriors in the defending
     * team (when a castle if being attacked). The warriors are positioned from left to right. every warrior has 2
     * lives in a single overall-battle. when a warrior reaches 0 lives, he dies. The battle is always between the left
     * attacker against the left defender. when a warrior on either team dies, the next one to fight the opponent's left
     * warrior is
     * the warrior who becomes the far left next to the dead one, and so forth. The winner of a battle is the army who
     * has warriors still standing.
     * So, how do we determine a hit that decreases 1 life? Via a die. A die can have either 10 equal sides, or 12.
     * Every warrior has strength (1-9). In order to hit another enemy, your warrior needs to draw
     * a number in the die bigger than the strength of the opponent, while the opponent draws a smaller or equal number
     * to your warriors' strength. In order to get hit by the opponent, your warrior needs to draw exactly the opposite,
     * meaning:  your warrior needs to draw a number in the die smaller or equal to the strength of the opponent,
     * while the opponent draws a bigger number than your warrior's strength. Other cases cause another drop of the die,
     * so they are meaningless to our calculations.
     * As mentioned before, a warrior needs to be hit *twice* in order to die, so there are multiple options for a win.
     * For now, it's the user's responsibility to calculate the warriors real strength via the Warlords strength system.
     * This program for now simply calculates with raw strength. Later versions may take that task on themselves.
     * @param args is not being worked with.
     */
    public static void main(String[] args) {
        //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        RunningCalculations probabilityCalculator1 = new RunningCalculations();
        probabilityCalculator1.run();
        ThreadedProbabilityCalculator probabilityCalculator = new ThreadedProbabilityCalculator();
        probabilityCalculator.start();
    }
}
