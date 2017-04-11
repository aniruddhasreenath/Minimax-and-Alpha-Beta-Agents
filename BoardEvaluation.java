import java.util.*;

public class BoardEvaluation{

    public int evaluateBoardForStrategy(Breakthrough bt, int[][] currBoard, int player, int opponent, int strategy,
                                        int numPawnsAtEnemyLineForWin) {

        /* Board evaluation has following criteria
        OFFENSIVE:
            +10 for each enemy pawn captured
            +(rowid) for how deep I am into the enemy territory
            //+5 for each own pawn left
            +1000 for winning the game
            -1000 for losing the game

        DEFENSIVE
            +10 for each own pawn left
            -(rowid) for how deep enemy is into my territory
            //+5 for each enemy pawn captured
            +1000 for winning the game
            -1000 for losing the game
        */

        int score = 0;
        int attackingScore = 0;
        int territoryScore = 0;
        int totalEnemyPawnsRemaining = 0;
        int totalSelfPawnsRemaining = 0;
        int myGoalLine = 0;
        int opponentGoalLine = bt.nrows-1;
        int numSelfPawnsAtEnemyLine = 0;
        int numEnemyPawnsAtSelfLine = 0;
        //System.out.println("Calculating board score for player "+player+" opponent "+opponent+" strategy"+strategy);

        if(player == bt.PLAYER2){
            myGoalLine = bt.nrows-1;
            opponentGoalLine = 0;
        }

        for(int i=0; i<bt.nrows; i++) {
            for (int j = 0; j < bt.ncols; j++) {
                if(currBoard[i][j] == 0){
                    continue;
                }

                // for opponent
                if (currBoard[i][j] == opponent) {
                    totalEnemyPawnsRemaining++;

                    // opponent reached the last row - defeat
                    if (i == myGoalLine) {
                        numEnemyPawnsAtSelfLine++;
                        //if(numEnemyPawnsAtSelfLine >= numPawnsAtEnemyLineForWin) {
                        if(numEnemyPawnsAtSelfLine > 0) {
                            //System.out.println("Sub 1000 for losing the board " + i + " " + j);
                            score -= numEnemyPawnsAtSelfLine*1000;
                        }
                    }

                    // opponent in my territory
                    if(player == bt.PLAYER1) {
                        //System.out.println("Adding " + i + " for deeper territory " + i + " " + j);
                        territoryScore += bt.nrows -1 - i;
                    }
                    else if (player == bt.PLAYER2){
                        //System.out.println("Adding " + (bt.nrows-1-i) + " for deeper territory " + i + " " + j);
                        territoryScore += i;
                    }

                } else { // for player
                    totalSelfPawnsRemaining++;

                    // player in enemy territory
                    if(player == bt.PLAYER1) {
                        //System.out.println("Adding " + i + " for deeper territory " + i + " " + j);
                        attackingScore += i;
                    }
                    else {
                        //System.out.println("Adding " + (bt.nrows-1-i) + " for deeper territory " + i + " " + j);
                        attackingScore += (bt.nrows-1-i);
                    }

                    // player reached the final row - victory
                    if (i == opponentGoalLine) {
                        numSelfPawnsAtEnemyLine++;
                        //if(numSelfPawnsAtEnemyLine >= numPawnsAtEnemyLineForWin) {
                        if(numSelfPawnsAtEnemyLine > 0) {
                            //System.out.println("i "+i+" j "+j+" value "+currBoard[i][j]);
                            //System.out.println("Adding 1000 for winning the board " + i + " " + j);
                            score += numSelfPawnsAtEnemyLine*1000;
                        }
                    }
                }
            }
        }

        if(totalSelfPawnsRemaining < numPawnsAtEnemyLineForWin){
            //System.out.println("Subtracting 1000 for no self pawns remaining");
            score -= (totalSelfPawnsRemaining+1)*1000;
        }

        if(totalEnemyPawnsRemaining < numPawnsAtEnemyLineForWin){
            //System.out.println("Adding 1000 for no enemy pawns remaining");
            score += (totalEnemyPawnsRemaining+1)*1000;
        }

        if(strategy == bt.OFFENSIVE_STRATEGY) {
            //System.out.println("Adding " + ((bt.TOTAL_PAWNS - totalEnemyPawnsRemaining) * 10) + " for captured enemy pawns");
            score += (bt.TOTAL_PAWNS - totalEnemyPawnsRemaining) * 10;
            score += 3*attackingScore;
            //score += totalSelfPawnsRemaining * 2;
        }
        else{
            //System.out.println("Adding " + totalSelfPawnsRemaining*10 + " for remaining self pawns");
            score += totalSelfPawnsRemaining * 10;
            score -= 3*territoryScore;
            //score += (bt.TOTAL_PAWNS - totalEnemyPawnsRemaining) * 2;
        }

        //System.out.println("Score is "+score);
        return score;
    }

    public int[] checkGameStatus(Breakthrough bt, int [][] board, int numPawnsAtEnemyLineForWin) {

        int [] results = new int[3]; // results struct stores, winner player, num pawns remaining for p1, num pawns remaining for p2
        int numP1PawnsAtEnemyLine = 0;
        int numP2PawnsAtEnemyLine = 0;

        for(int i=0; i<bt.nrows; i++){
            for(int j=0; j<bt.ncols; j++){
                if(board[i][j] != 0)
                {
                    results[board[i][j]]++;
                    if(i==0 && board[i][j] == bt.PLAYER2){
                        numP2PawnsAtEnemyLine++;
                        if(numP2PawnsAtEnemyLine >= numPawnsAtEnemyLineForWin) {
                            results[0] = bt.PLAYER2;
                        }
                    }

                    if((i==bt.nrows-1) && board[i][j] == bt.PLAYER1){
                        numP1PawnsAtEnemyLine++;
                        if(numP1PawnsAtEnemyLine >= numPawnsAtEnemyLineForWin) {
                            results[0] = bt.PLAYER1;
                        }
                    }
                }
            }
        }

        if(results[0] == 0){
            if(results[1] < numPawnsAtEnemyLineForWin){
                results[0] = bt.PLAYER2;
            }
            if(results[2] < numPawnsAtEnemyLineForWin){
                results[0] = bt.PLAYER1;
            }
        }

        return results;

    }

    public int countEnemyPawnsCaptured(Breakthrough bt, int [][] board, int player){

        int count = 0;

        for(int i=0; i<bt.nrows; i++){
            for(int j=0; j<bt.ncols; j++){
                if(board[i][j] != 0 && board[i][j] != player)
                {
                    count++;
                }
            }
        }
        return (bt.TOTAL_PAWNS - count);
    }

    public int countSelfPawnsCaptured(Breakthrough bt, int [][] board, int player){

        int count = 0;

        for(int i=0; i<bt.nrows; i++){
            for(int j=0; j<bt.ncols; j++){
                if(board[i][j] == player)
                {
                    count++;
                }
            }
        }
        return (bt.TOTAL_PAWNS - count);
    }
}