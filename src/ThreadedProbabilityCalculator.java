import java.util.ArrayList;
import java.util.Scanner;

public class ThreadedProbabilityCalculator extends Thread{ //This class was created to slightly enhance the performance
    //of the calculator with big amount of warriors using threads. for example: in my computer,
    // loosing 7 of 8 of my warriors in a battle against 13 warriors would take about 1 minute to calculate in the
    // original program, compared to 30 seconds with this threaded calculator!
    private static ArrayList<Integer> myForces;
    private static ArrayList<Integer> enemyForces;
    private static int maxNumOfThreads;
    private static int numberOfThreads;
    private static boolean acceptParamsOnce;
    private boolean announceOnce;
    private int dieGameSize;
    private int maxLosesNumber;
    private int numOfTroop;
    private int numOfEnemy;
    private int mTroopLives;
    private int eTroopLives;
    private float returnProbability;
    public ThreadedProbabilityCalculator() {
        myForces = new ArrayList<Integer>();
        enemyForces = new ArrayList<Integer>();
        dieGameSize = 10;
        maxLosesNumber = 0;
        numOfEnemy = 0;
        numOfTroop = 0;
        mTroopLives = 2;
        eTroopLives = 2;
        returnProbability = 0;
        numberOfThreads = 1;
        maxNumOfThreads = 100;
        acceptParamsOnce = true;
        announceOnce = true;
    }

    public ThreadedProbabilityCalculator(int die, int maxLose,
                                         int numOfTroop, int numOfEnemy, int mTroopLives, int eTroopLives) {
        dieGameSize = die;
        maxLosesNumber = maxLose;
        this.numOfTroop = numOfTroop;
        this.numOfEnemy = numOfEnemy;
        this.mTroopLives = mTroopLives;
        this.eTroopLives = eTroopLives;
        returnProbability = 0;
        announceOnce = false;
    }

    public static void setMaxNumOfThreads(int newMax) { //every computer has it's own capabilities regarding
        //number of max threads ran on the computer
        maxNumOfThreads = newMax;
    }
    public synchronized boolean addThreads(int addedThreads) { // we want to encapsulate the process of adding a
        //number to the static variable of number of threads. We don't want concurrency problems, therefore we make it
        //a synchronized method, that only one thread can use at a time. We check whether we can have another threads,
        //and if we do, we update the number of threads in the static variable
        if ((numberOfThreads + addedThreads) <= maxNumOfThreads) {
            numberOfThreads += addedThreads;
            return true;
        }
        return false;
    }

    public float getReturnProbability() {
        return returnProbability;
    }

    public void run() { //run the threads via a Template design pattern
        if (acceptParamsOnce) {
            acceptArgs();
            acceptParamsOnce = false;
        }
        threadRecursiveProbabilityCalculator(numOfTroop, numOfEnemy, mTroopLives, eTroopLives);
        if (announceOnce) {
            probabilityCalculationAnnouncer();
        }
    }

    public void threadRecursiveProbabilityCalculator(int numberOfTroop, int numberOfEnemy,
                                                int myTroopLives, int enemyTroopLives) {
        float retProbability = 0;
        if ((maxLosesNumber + 1) <= numberOfTroop) {
            // when we allow a certain num of loses, it means we can have a porobability of winning and losing with the
            // troops up that number, and, that we can have all three possibilities of winning (3 of them) of the troop
            // above the max number of loses, but we can't have him lose!
            returnProbability = retProbability;
            return;
        }
        if (numberOfEnemy >= enemyForces.size()) {
            retProbability = 1;
            returnProbability = retProbability;
            return;
        }
        if (numberOfTroop >= myForces.size()) {
            retProbability = 1;
            returnProbability = retProbability;
            return;
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
        if ((myTroopLives == 2) && (enemyTroopLives == 2)) {
            if (!addThreads(4)) { //we check whether we are allowed to create more threads. If not, we use
                //our regular recursion in that same thread
                returnProbability = RecursiveProbabilityCalculator(numberOfTroop, numberOfEnemy,
                        myTroopLives, enemyTroopLives);
                return;
            }
            ThreadedProbabilityCalculator winWin = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
                    numberOfTroop, (numberOfEnemy + 1), 2, 2);
            ThreadedProbabilityCalculator winLoseWin = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
                    numberOfTroop, (numberOfEnemy + 1), 1, 2);
            ThreadedProbabilityCalculator loseLose = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
                    (numberOfTroop + 1), numberOfEnemy, 2, 2);
            ThreadedProbabilityCalculator loseWinLose = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
                    (numberOfTroop + 1), numberOfEnemy, 2, 1);
            try {
                winWin.start();
                winLoseWin.start();
                loseLose.start();
                loseWinLose.start();
                winWin.join();
                winLoseWin.join();
                loseLose.join();
                loseWinLose.join();
            }
            catch (InterruptedException e) { }
            retProbability = (winInSingleBattleProbability * winInSingleBattleProbability
                    * winWin.getReturnProbability())
                    + (2 * (winInSingleBattleProbability * loseInSingleBattleProbability * winInSingleBattleProbability
                    * winLoseWin.getReturnProbability())) +
                    (loseInSingleBattleProbability * loseInSingleBattleProbability
                            * loseLose.getReturnProbability()) +
                    (2 * (loseInSingleBattleProbability * loseInSingleBattleProbability * winInSingleBattleProbability
                            * loseWinLose.getReturnProbability()));
        }
        if ((myTroopLives == 2) && (enemyTroopLives == 1)) {
            if (!addThreads(3)) { //we check whether we are allowed to create more threads. If not, we use
                //our regular recursion in that same thread
                returnProbability = RecursiveProbabilityCalculator(numberOfTroop, numberOfEnemy,
                        myTroopLives, enemyTroopLives);
                return;
            }
            ThreadedProbabilityCalculator win = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
                    numberOfTroop, (numberOfEnemy + 1), 2, 2);
            ThreadedProbabilityCalculator winLose = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
                    numberOfTroop, (numberOfEnemy + 1), 1, 2);
            ThreadedProbabilityCalculator loseLose = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
                    (numberOfTroop + 1), numberOfEnemy, 2, 1);
            try {
                win.start();
                winLose.start();
                loseLose.start();
                win.join();
                winLose.join();
                loseLose.join();
            }
            catch (InterruptedException e) { }
            retProbability = (winInSingleBattleProbability
                    * win.getReturnProbability())
                    + (loseInSingleBattleProbability * winInSingleBattleProbability
                    * winLose.getReturnProbability()) +
                    (loseInSingleBattleProbability * loseInSingleBattleProbability
                            * loseLose.getReturnProbability());
        }
        if ((myTroopLives == 1) && (enemyTroopLives == 2)) {
            if (!addThreads(3)) { //we check whether we are allowed to create more threads. If not, we use
                //our regular recursion in that same thread
                returnProbability = RecursiveProbabilityCalculator(numberOfTroop, numberOfEnemy,
                        myTroopLives, enemyTroopLives);
                return;
            }
            ThreadedProbabilityCalculator winWin = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
                    numberOfTroop, (numberOfEnemy + 1), 1, 2);
            ThreadedProbabilityCalculator loseWin = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
                    (numberOfTroop + 1), numberOfEnemy, 2, 1);
            ThreadedProbabilityCalculator lose = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
                    (numberOfTroop + 1), numberOfEnemy, 2, 2);
            try {
                winWin.start();
                loseWin.start();
                lose.start();
                winWin.join();
                loseWin.join();
                lose.join();
            }
            catch (InterruptedException e) { }
            retProbability = (winInSingleBattleProbability * winInSingleBattleProbability
                    * winWin.getReturnProbability())
                    + (loseInSingleBattleProbability * winInSingleBattleProbability
                    * loseWin.getReturnProbability()) +
                    (loseInSingleBattleProbability
                            * lose.getReturnProbability());
        }
        returnProbability = retProbability;
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

    public void acceptArgs() {
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
    }

    public void probabilityCalculationAnnouncer() {
        returnProbability *= 100; // we want to show percentage
        System.out.print("The chances of a battle with these results or better are: " + returnProbability + "%\n");
    }
}
