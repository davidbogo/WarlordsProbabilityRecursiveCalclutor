import java.util.ArrayList;
import java.util.Scanner;

public class RunningCalculations {
    private ArrayList<Integer> myForces;
    private ArrayList<Integer> enemyForces;
    private int dieGameSize;
    private int maxLosesNumber;
    private float totalWinProbability;
    public RunningCalculations() {
        myForces = new ArrayList<Integer>();
        enemyForces = new ArrayList<Integer>();
        dieGameSize = 10;
        maxLosesNumber = 0;
    }

    public float RecursiveProbabilityCalculator(int numberOfTroop, int numberOfEnemy,
                                                int myTroopLives, int enemyTroopLives) {
        float retProbability = 0;
        if ((maxLosesNumber + 1) <= numberOfTroop) {
            // when we allow a certain num of loses, it means we can have a porobability of winning and losing with the
            // troops up that number, and, that we can have all three possibilities of winning (3 of them) of the troop
            // above the max number of loses, but we can't have him lose!
            return retProbability;
        }
        if (numberOfEnemy >= enemyForces.size()) {
            retProbability = 1;
            return retProbability;
        }
        if (numberOfTroop >= myForces.size()) {
            retProbability = 1;
            return  retProbability;
        }
        float winInSingleBattleProbability;
        float loseInSingleBattleProbability;
        float dieShowWin;
        float dieShowLose;
        int powerOfMyTroop = myForces.get(numberOfTroop);
        int powerOfEnemy = enemyForces.get(numberOfEnemy);
        dieShowWin = (dieGameSize - powerOfEnemy) * (powerOfMyTroop);
        dieShowLose = (dieGameSize - powerOfMyTroop) * (powerOfEnemy);
        winInSingleBattleProbability = (dieShowWin) / (dieShowWin + dieShowLose);
        loseInSingleBattleProbability = 1 - winInSingleBattleProbability;
        if ((myTroopLives == 2) && (enemyTroopLives == 2)){
            retProbability = (winInSingleBattleProbability * winInSingleBattleProbability
                    * RecursiveProbabilityCalculator(numberOfTroop, (numberOfEnemy + 1), 2, 2))
                    + (2 * (winInSingleBattleProbability * loseInSingleBattleProbability * winInSingleBattleProbability
                            * RecursiveProbabilityCalculator(numberOfTroop, (numberOfEnemy + 1),
                    1, 2))) +
                    (loseInSingleBattleProbability * loseInSingleBattleProbability
                            * RecursiveProbabilityCalculator((numberOfTroop + 1), numberOfEnemy,
                            2, 2)) +
                    (2 * (loseInSingleBattleProbability * loseInSingleBattleProbability * winInSingleBattleProbability
                            * RecursiveProbabilityCalculator((numberOfTroop + 1), numberOfEnemy,
                            2, 1)));
        }
        if ((myTroopLives == 2) && (enemyTroopLives == 1)) {
            retProbability = (winInSingleBattleProbability
                    * RecursiveProbabilityCalculator(numberOfTroop, (numberOfEnemy + 1), 2, 2))
                    + (loseInSingleBattleProbability * winInSingleBattleProbability
                    * RecursiveProbabilityCalculator(numberOfTroop, (numberOfEnemy + 1),
                    1, 2)) +
                    (loseInSingleBattleProbability * loseInSingleBattleProbability
                            * RecursiveProbabilityCalculator((numberOfTroop + 1), numberOfEnemy,
                            2, 1));
        }
        if ((myTroopLives == 1) && (enemyTroopLives == 2)) {
            retProbability = (winInSingleBattleProbability * winInSingleBattleProbability
                    * RecursiveProbabilityCalculator(numberOfTroop, (numberOfEnemy + 1), 1, 2))
                    + (loseInSingleBattleProbability * winInSingleBattleProbability
                    * RecursiveProbabilityCalculator((numberOfTroop + 1), numberOfEnemy,
                    2, 1)) +
                    (loseInSingleBattleProbability
                            * RecursiveProbabilityCalculator((numberOfTroop + 1), numberOfEnemy,
                            2, 2));
        }
            return retProbability;
    }

    public void run() {
        Scanner console = new Scanner(System.in);
        System.out.print("Enter 10 or 12, depending on die system (regular or intense): ");
        int dieSystem = console.nextInt();
        dieGameSize = dieSystem;
        int run = 0;
        int numberOfTroop = 1;
        while(run < 1) {
            System.out.print("Enter strength of your troop " + numberOfTroop
                    + "(from left to right). If you're done, press 0: ");
            int troopStrenth = console.nextInt();
            if (troopStrenth == 0) {
                run = 1;
                numberOfTroop = 1;
                continue;
            }
            myForces.add(troopStrenth);
            numberOfTroop += 1;
        }
        while(run < 2) {
            System.out.print("Enter strength of enemy troop " + numberOfTroop
                    + "(from left to right). If you're done, press 0: ");
            int troopStrenth = console.nextInt();
            if (troopStrenth == 0) {
                run = 2;
                numberOfTroop = 1;
                continue;
            }
            enemyForces.add(troopStrenth);
            numberOfTroop += 1;
        }
        System.out.print("How many troops are you ready to lose? ");
        int losesNumber = console.nextInt();
        maxLosesNumber = losesNumber;
        probabilityCalculationAnnouncer();
    }

    public void probabilityCalculationAnnouncer() {
        totalWinProbability = RecursiveProbabilityCalculator(0, 0,
                2, 2);
        totalWinProbability *= 100; // we want to show percentage
        System.out.print("The chances of a battle with these results or better are: " + totalWinProbability + "%");
    }
}
