import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadedProbabilityCalculator extends Thread{ //This class was created to slightly enhance the performance
    //of the calculator with big amount of warriors using threads. for example: in my computer,
    // loosing 7 of 8 of my warriors in a battle against 13 warriors would take about 1 minute to calculate in the
    // original program, compared to 30 seconds with this threaded calculator!
    private static ArrayList<Integer> myForces;
    private static ArrayList<Integer> enemyForces;
    private static Map<String, Float> lookup;
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
        maxNumOfThreads = 32;
        acceptParamsOnce = true;
        announceOnce = true;
        lookup = new ConcurrentHashMap<>(16, 0.75f, maxNumOfThreads);
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

    public static synchronized String keyFactory (int myTroop, int enemyTroop, int myLives, int enemyLives) {
        return (String.valueOf(myTroop) + "-" + String.valueOf(enemyTroop) + "-" +
                String.valueOf(myLives) + "-" + String.valueOf(enemyLives));
    }

    private void putValueInKey(String key, float value) {
        lookup.put(key, value);
    }

    public static void setMaxNumOfThreads(int newMax) { //every computer has it's own capabilities regarding
        //number of max threads ran on the computer
        maxNumOfThreads = newMax;
    }
    private synchronized boolean addThreads(int addedThreads) { // we want to encapsulate the process of adding a
        //number to the static variable of number of threads. We don't want concurrency problems, therefore we make it
        //a synchronized method, that only one thread can use at a time. We check whether we can have another threads,
        //and if we do, we update the number of threads in the static variable
        if ((numberOfThreads + addedThreads) <= maxNumOfThreads) {
            numberOfThreads += addedThreads;
            return true;
        }
        return false;
    }

    public synchronized void reduceThreads(int reducedThreads) {
        numberOfThreads -= reducedThreads;
    }

    public synchronized void setReturnProbability(float newReturn) {
        returnProbability = newReturn;
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
        String key = keyFactory(numberOfTroop, numberOfEnemy, myTroopLives, enemyTroopLives);
        if ((maxLosesNumber + 1) <= numberOfTroop) {
            // when we allow a certain num of loses, it means we can have a probability of winning and losing with the
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
        float recursiveCalc;
        if ((myTroopLives == 2) && (enemyTroopLives == 2)) {
            if (!addThreads(3)) { //we check whether we are allowed to create more threads. If not, we use
                //our regular recursion in that same thread
                if (!lookup.containsKey(key)) {
                    recursiveCalc = RecursiveProbabilityCalculator(numberOfTroop, numberOfEnemy,
                            myTroopLives, enemyTroopLives);
                    putValueInKey(key, recursiveCalc);
                }
                returnProbability = lookup.get(key);
                return;
            }
            String winWinKey = keyFactory(numberOfTroop, (numberOfEnemy + 1), 2, 2);
            String winLoseWinKey = keyFactory(numberOfTroop, (numberOfEnemy + 1), 1, 2);
            String loseLoseKey = keyFactory((numberOfTroop + 1), numberOfEnemy, 2, 2);
            String loseWinLoseKey = keyFactory((numberOfTroop + 1), numberOfEnemy, 2, 1);
            ThreadedProbabilityCalculator winWin = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
                    numberOfTroop, (numberOfEnemy + 1), 2, 2);
            ThreadedProbabilityCalculator winLoseWin = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
                    numberOfTroop, (numberOfEnemy + 1), 1, 2);
            ThreadedProbabilityCalculator loseLose = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
                    (numberOfTroop + 1), numberOfEnemy, 2, 2);
            //ThreadedProbabilityCalculator loseWinLose = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
            //        (numberOfTroop + 1), numberOfEnemy, 2, 1);
            float loseWinLose = -1;
            try {
                //loseWinLose.start();
                if (!lookup.containsKey(winWinKey)) {
                    winWin.start();
                } else {
                    winWin.setReturnProbability(lookup.get(winWinKey));
                }
                if (!lookup.containsKey(winLoseWinKey)) {
                    winLoseWin.start();
                } else {
                    winLoseWin.setReturnProbability(lookup.get(winLoseWinKey));
                }
                if (!lookup.containsKey(loseLoseKey)) {
                    loseLose.start();
                } else {
                    loseLose.setReturnProbability(lookup.get(loseLoseKey));
                }
                if (!lookup.containsKey(loseWinLoseKey)) {
                    loseWinLose = RecursiveProbabilityCalculator((numberOfTroop + 1), numberOfEnemy,
                            2, 1);
                    putValueInKey(loseWinLoseKey, loseWinLose);
                } else {
                    loseWinLose = lookup.get(loseWinLoseKey);
                }
                if (!lookup.containsKey(winWinKey)) {
                    winWin.join();
                    putValueInKey(winWinKey, winWin.getReturnProbability());
                }
                if (!lookup.containsKey(winLoseWinKey)) {
                    winLoseWin.join();
                    putValueInKey(winLoseWinKey, winLoseWin.getReturnProbability());
                }
                if (!lookup.containsKey(loseLoseKey)) {
                    loseLose.join();
                    putValueInKey(loseLoseKey, loseLose.getReturnProbability());
                }
                //loseWinLose.join();
            }
            catch (InterruptedException e) { }
            while (loseWinLose == -1) {
                continue;
            }
                    retProbability = (winInSingleBattleProbability * winInSingleBattleProbability
                    * winWin.getReturnProbability())
                    + (2 * (winInSingleBattleProbability * loseInSingleBattleProbability * winInSingleBattleProbability
                    * winLoseWin.getReturnProbability())) +
                    (loseInSingleBattleProbability * loseInSingleBattleProbability
                            * loseLose.getReturnProbability()) +
                    (2 * (loseInSingleBattleProbability * loseInSingleBattleProbability * winInSingleBattleProbability
                            * loseWinLose));
        }
        if ((myTroopLives == 2) && (enemyTroopLives == 1)) {
            if (!addThreads(2)) { //we check whether we are allowed to create more threads. If not, we use
                //our regular recursion in that same thread
                if (!lookup.containsKey(key)) {
                    recursiveCalc = RecursiveProbabilityCalculator(numberOfTroop, numberOfEnemy,
                            myTroopLives, enemyTroopLives);
                    putValueInKey(key, recursiveCalc);
                }
                returnProbability = lookup.get(key);
                return;
            }
            String winKey = keyFactory(numberOfTroop, (numberOfEnemy + 1), 2, 2);
            String winLoseKey = keyFactory(numberOfTroop, (numberOfEnemy + 1), 1, 2);
            String loseLoseKey = keyFactory((numberOfTroop + 1), numberOfEnemy, 2, 1);
            ThreadedProbabilityCalculator win = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
                    numberOfTroop, (numberOfEnemy + 1), 2, 2);
            ThreadedProbabilityCalculator winLose = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
                    numberOfTroop, (numberOfEnemy + 1), 1, 2);
            //ThreadedProbabilityCalculator loseLose = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
            //        (numberOfTroop + 1), numberOfEnemy, 2, 1);
            float loseLose = -1;
            try {
                if (!lookup.containsKey(winKey)) {
                    win.start();
                } else {
                    win.setReturnProbability(lookup.get(winKey));
                }
                if (!lookup.containsKey(winLoseKey)) {
                    winLose.start();
                } else {
                    winLose.setReturnProbability(lookup.get(winLoseKey));
                }
                //loseLose.start();
                if (!lookup.containsKey(loseLoseKey)) {
                    loseLose = RecursiveProbabilityCalculator((numberOfTroop + 1), numberOfEnemy,
                            2, 1);
                    putValueInKey(loseLoseKey, loseLose);
                } else {
                    loseLose = lookup.get(loseLoseKey);
                }
                if (!lookup.containsKey(winKey)) {
                    win.join();
                    putValueInKey(winKey, win.getReturnProbability());
                }
                if (!lookup.containsKey(winLoseKey)) {
                    winLose.join();
                    putValueInKey(winLoseKey, winLose.getReturnProbability());
                }
                //loseLose.join();
            }
            catch (InterruptedException e) { }
            while (loseLose == -1) {
                continue;
            }
            retProbability = (winInSingleBattleProbability
                    * win.getReturnProbability())
                    + (loseInSingleBattleProbability * winInSingleBattleProbability
                    * winLose.getReturnProbability()) +
                    (loseInSingleBattleProbability * loseInSingleBattleProbability * loseLose);
        }
        if ((myTroopLives == 1) && (enemyTroopLives == 2)) {
            if (key.equals("0112")) {
                String le = "1";
            }
            if (!addThreads(2)) { //we check whether we are allowed to create more threads. If not, we use
                //our regular recursion in that same thread
                if (!lookup.containsKey(key)) {
                    recursiveCalc = RecursiveProbabilityCalculator(numberOfTroop, numberOfEnemy,
                            myTroopLives, enemyTroopLives);
                    putValueInKey(key, recursiveCalc);
                }
                returnProbability = lookup.get(key);
                return;
            }
            String winWinKey = keyFactory(numberOfTroop, (numberOfEnemy + 1), 1, 2);
            //0212
            String loseWinKey = keyFactory((numberOfTroop + 1), numberOfEnemy, 2, 1);
            //1121
            String loseKey = keyFactory((numberOfTroop + 1), numberOfEnemy, 2, 2); // enemy changed: 2
            //1122
            ThreadedProbabilityCalculator winWin = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
                    numberOfTroop, (numberOfEnemy + 1), 1, 2);
            ThreadedProbabilityCalculator loseWin = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
                    (numberOfTroop + 1), numberOfEnemy, 2, 1);
            //ThreadedProbabilityCalculator lose = new ThreadedProbabilityCalculator(dieGameSize, maxLosesNumber,
            //        (numberOfTroop + 1), numberOfEnemy, 2, 2);
            float lose = -1;
            try {
                //lose.start();
                if (!lookup.containsKey(winWinKey)) {
                    winWin.start();
                } else {
                    winWin.setReturnProbability(lookup.get(winWinKey));
                }
                if (!lookup.containsKey(loseWinKey)) {
                    loseWin.start();
                } else {
                    loseWin.setReturnProbability(lookup.get(loseWinKey));
                }
                //loseLose.start();
                if (!lookup.containsKey(loseKey)) {
                    lose = RecursiveProbabilityCalculator((numberOfTroop + 1), numberOfEnemy,
                            2, 2);
                    putValueInKey(loseKey, lose);
                } else {
                    lose = lookup.get(loseKey);
                }
                if (!lookup.containsKey(winWinKey)) {
                    winWin.join();
                    putValueInKey(winWinKey, winWin.getReturnProbability());
                }
                if (!lookup.containsKey(loseWinKey)) {
                    loseWin.join();
                    putValueInKey(loseWinKey, loseWin.getReturnProbability());
                }
                //lose.join();
            }
            catch (InterruptedException e) { }
            while (lose == -1) {
                continue;
            }
            retProbability = (winInSingleBattleProbability * winInSingleBattleProbability
                    * winWin.getReturnProbability())
                    + (loseInSingleBattleProbability * winInSingleBattleProbability
                    * loseWin.getReturnProbability()) +
                    (loseInSingleBattleProbability * lose);
            if (key.equals("0112")) {
                System.out.print(Thread.currentThread().getName() + " \n");
                System.out.print(retProbability + "\n");
            }
        }
        returnProbability = retProbability;
    }

    public float RecursiveProbabilityCalculator(int numberOfTroop, int numberOfEnemy,
                                                int myTroopLives, int enemyTroopLives) {
        float retProbability = 0;
        String key = keyFactory(numberOfTroop, numberOfEnemy, myTroopLives, enemyTroopLives);
        if ((maxLosesNumber + 1) <= numberOfTroop) {
            // when we allow a certain num of loses, it means we can have a probability of winning and losing with the
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
            String winWinKey = keyFactory(numberOfTroop, (numberOfEnemy + 1), 2, 2);
            String winLoseWinKey = keyFactory(numberOfTroop, (numberOfEnemy + 1), 1, 2);
            String loseWinLoseKey = keyFactory((numberOfTroop + 1), numberOfEnemy, 2, 1);
            String loseLoseKey = keyFactory((numberOfTroop + 1), numberOfEnemy, 2, 2);
            if (!lookup.containsKey(winWinKey)) {
                float recursiveCalcWinWin = RecursiveProbabilityCalculator(numberOfTroop, (numberOfEnemy + 1),
                        2, 2);
                putValueInKey(winWinKey, recursiveCalcWinWin);
            }
            if (!lookup.containsKey(winLoseWinKey)) {
                float recursiveCalcWinLoseWin = RecursiveProbabilityCalculator(numberOfTroop, (numberOfEnemy + 1),
                        1, 2);
                putValueInKey(winLoseWinKey, recursiveCalcWinLoseWin);
            }
            if (!lookup.containsKey(loseWinLoseKey)) {
                float recursiveLoseWinLose = RecursiveProbabilityCalculator((numberOfTroop + 1), numberOfEnemy,
                        2, 1);
                putValueInKey(loseWinLoseKey, recursiveLoseWinLose);
            }
            if (!lookup.containsKey(loseLoseKey)) {
                float recursiveLoseLose = RecursiveProbabilityCalculator((numberOfTroop + 1), numberOfEnemy,
                        2, 2);
                putValueInKey(loseLoseKey, recursiveLoseLose);
            }
            retProbability = (winInSingleBattleProbability * winInSingleBattleProbability
                    * lookup.get(winWinKey))
                    + (2 * (winInSingleBattleProbability * loseInSingleBattleProbability * winInSingleBattleProbability
                    * lookup.get(winLoseWinKey))) +
                    (loseInSingleBattleProbability * loseInSingleBattleProbability
                            * lookup.get(loseLoseKey)) +
                    (2 * (loseInSingleBattleProbability * loseInSingleBattleProbability * winInSingleBattleProbability
                            * lookup.get(loseWinLoseKey)));
        }
        if ((myTroopLives == 2) && (enemyTroopLives == 1)) {
            String winKey = keyFactory(numberOfTroop, (numberOfEnemy + 1), 2, 2);
            String loseWinKey = keyFactory(numberOfTroop, (numberOfEnemy + 1), 1, 2);
            String loseLoseKey = keyFactory((numberOfTroop + 1), numberOfEnemy, 2, 1);
            if (!lookup.containsKey(winKey)) {
                float recursiveWin = RecursiveProbabilityCalculator(numberOfTroop, (numberOfEnemy + 1),
                        2, 2);
                putValueInKey(winKey, recursiveWin);
            }
            if (!lookup.containsKey(loseWinKey)) {
                float recursiveLoseWin = RecursiveProbabilityCalculator(numberOfTroop, (numberOfEnemy + 1),
                        1, 2);
                putValueInKey(loseWinKey, recursiveLoseWin);
            }
            if (!lookup.containsKey(loseLoseKey)) {
                float recursiveLoseLose = RecursiveProbabilityCalculator((numberOfTroop + 1), numberOfEnemy,
                        2, 1);
                putValueInKey(loseLoseKey, recursiveLoseLose);
            }
            retProbability = (winInSingleBattleProbability
                    * lookup.get(winKey))
                    + (loseInSingleBattleProbability * winInSingleBattleProbability
                    * lookup.get(loseWinKey)) +
                    (loseInSingleBattleProbability * loseInSingleBattleProbability
                            * lookup.get(loseLoseKey));
        }
        if ((myTroopLives == 1) && (enemyTroopLives == 2)) {
            String winWinKey = keyFactory(numberOfTroop, (numberOfEnemy + 1), 1, 2);
            String winLoseKey = keyFactory((numberOfTroop + 1), numberOfEnemy, 2, 1);
            String loseKey = keyFactory((numberOfTroop + 1), numberOfEnemy, 2, 2);
            if (!lookup.containsKey(winWinKey)) {
                float recursiveWinWin = RecursiveProbabilityCalculator(numberOfTroop, (numberOfEnemy + 1),
                        1, 2);
                putValueInKey(winWinKey, recursiveWinWin);
            }
            if (!lookup.containsKey(winLoseKey)) {
                float recursiveWinLose = RecursiveProbabilityCalculator((numberOfTroop + 1), numberOfEnemy,
                        2, 1);
                putValueInKey(winLoseKey, recursiveWinLose);
            }
            if (!lookup.containsKey(loseKey)) {
                float recursiveLose = RecursiveProbabilityCalculator((numberOfTroop + 1), numberOfEnemy,
                        2, 2);
                putValueInKey(loseKey, recursiveLose);
            }
            retProbability = (winInSingleBattleProbability * winInSingleBattleProbability
                    * lookup.get(winWinKey))
                    + (loseInSingleBattleProbability * winInSingleBattleProbability
                    * lookup.get(winLoseKey)) +
                    (loseInSingleBattleProbability
                            * lookup.get(loseKey));
        }
        if (key.equals("0112")) {
            System.out.print(Thread.currentThread().getName() + " \n");
            System.out.print(retProbability + "\n");
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
        //System.out.print(lookup.values());
        System.out.print("The chances of a battle with these results or better are: " + returnProbability + "%\n");
    }
}
