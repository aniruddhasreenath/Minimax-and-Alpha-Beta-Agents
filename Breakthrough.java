import java.util.*;
import java.util.Random;

public class Breakthrough{

    public static final int PLAYER1 = 1;
    public static final int PLAYER2 = 2;
    public static int TOTAL_PAWNS;
    public static final int OFFENSIVE_STRATEGY = 1;
    public static final int DEFENSIVE_STRATEGY = 2;
    public static final int MINIMAX = 1;
    public static final int ALPHA_BETA = 2;

    public int [][] board;

    public int [][] initBoard = {{1,1,1,1,1,1,1,1},
                                 {1,1,1,1,1,1,1,1},
                                 {0,0,0,0,0,0,0,0},
                                 {0,0,0,0,0,0,0,0},
                                 {0,0,0,0,0,0,0,0},
                                 {0,0,0,0,0,0,0,0},
                                 {2,2,2,2,2,2,2,2},
                                 {2,2,2,2,2,2,2,2}};

    public int [][] testBoard = {{0,1,1,1,1,1,1,1},
                                 {1,1,1,1,1,1,1,1},
                                 {1,0,0,0,0,0,0,0},
                                 {0,0,0,0,0,0,0,0},
                                 {0,0,0,0,0,0,0,0},
                                 {2,0,0,0,0,0,0,0},
                                 {0,2,2,2,2,2,2,2},
                                 {2,2,2,2,2,2,2,2}};

    public int [][] testBoard1 = {{0,0,0,0,0,0,0,0},
                                 {2,0,0,0,0,0,0,0},
                                 {0,0,0,0,0,0,0,0},
                                 {0,2,2,2,2,2,1,1},
                                 {0,0,0,0,2,0,1,0},
                                 {0,0,0,0,0,0,0,0},
                                 {0,0,0,0,0,0,0,0},
                                 {0,0,0,0,0,0,0,0}};

    public int nrows;
    public int ncols;
    BoardEvaluation boardEval;


    static public class PlayerStats{
        public long movesSoFar;
        public long nodesExpanded;
        public long totalMoveTimeInNS;
    }

    static public class Player{
        public int player;
        public int [][] board;
        public int evalScore;
        public int playType;
        public int strategy;
        public int searchDepth;
        public PlayerStats stats = new PlayerStats();

        Player(int p, int[][] initBoard, int type, int strategy, int depth){
            int rows = initBoard.length;
            int cols = initBoard[0].length;
            this.player = p;

            this.board = new int[rows][cols];
            for(int i=0; i<rows; i++){
                for(int j=0; j<cols; j++){
                    this.board[i][j] = initBoard[i][j];
                }
            }

            this.evalScore = 0;
            this.playType = type;
            this.strategy = strategy;
            this.searchDepth = depth;
        }
    }

    public void initBoard(){
        for(int i=0; i<ncols; i++){
            board[0][i] = 1;
            board[1][i] = 1;
            board[nrows-2][i] = 2;
            board[nrows-1][i] = 2;
        }
    }

    Breakthrough(int rows, int cols){
        nrows = rows;
        ncols = cols;
        TOTAL_PAWNS = 2*cols;
        board = new int[nrows][ncols];
        initBoard();

        boardEval = new BoardEvaluation();
    }

    public int [][] createNewBoard(int [][] board){

        int [][] newBoard = new int[nrows][ncols];
        for(int i=0; i<nrows; i++) {
            for (int j = 0; j < ncols; j++) {
                newBoard[i][j] = board[i][j];
            }
        }

        return newBoard;
    }

    public int minimax(int [][] currBoard, Player rootPlayer, int currPlayer, int origDepth, int currDepth,
                       int alpha, int beta, int numPawnsAtEnemyLineForWin){

        int opponent = PLAYER1;
        int [] status;
        if(currPlayer == PLAYER1){
            opponent = PLAYER2;
        }
        int score = Integer.MIN_VALUE;
        if(currPlayer != rootPlayer.player){
            score = Integer.MAX_VALUE;
        }

        // update stats
        rootPlayer.stats.nodesExpanded++;

        //System.out.println("Inside minimax currPlayer "+currPlayer+" opponent "+opponent+" rootPlayer "+rootPlayer.player+" depth "+currDepth);

        status = boardEval.checkGameStatus(this, currBoard, numPawnsAtEnemyLineForWin);
        if((status[0] !=0) || (currDepth == 0)){
            //System.out.println("Reached leaf node currPlayer "+opponent+" opponent "+currPlayer+" rootPlayer "+rootPlayer.player);
            int evalScore = boardEval.evaluateBoardForStrategy(this, currBoard, opponent, currPlayer,
                    rootPlayer.strategy, numPawnsAtEnemyLineForWin);
            //System.out.println("Reached depth limit. Evaluation score for board is "+evalScore);
            //printBoard(currBoard);
            return evalScore;
        }

        int [][] newBoard;
        int [][] bestBoard = null;
        List<int[][]> bestBoards = new ArrayList<int[][]>();

        if(currPlayer == PLAYER1){
            for(int i=0; i<nrows; i++){
                for(int j=0; j<ncols; j++){
                    if((currBoard[i][j] == 0) || (currBoard[i][j] == PLAYER2) ||
                            ((i==nrows-1) && (currBoard[i][j] == PLAYER1))){
                        continue;
                    }

                    //System.out.println("Processing "+i+" "+j+" for player "+currPlayer+" rootPlayer "+rootPlayer.player+" at depth "+currDepth);
                    if(currBoard[i+1][j] == 0) {
                        // move ahead
                        newBoard = createNewBoard(currBoard);
                        //System.out.println("Moving ahead from "+i+" "+j+" to "+(i+1)+" "+j+"currPlayer "+currPlayer);
                        //printBoard(currBoard);
                        newBoard[i + 1][j] = currPlayer;
                        newBoard[i][j] = 0;
                        //printBoard(newBoard);
                        int tempScore = minimax(newBoard, rootPlayer, opponent, origDepth, (currDepth-1), alpha, beta, numPawnsAtEnemyLineForWin);
                        // For Max player
                        if (currPlayer == rootPlayer.player) {
                            if (tempScore > score) {
                                bestBoards.clear();
                                bestBoards.add(newBoard);
                                score = tempScore;
                                bestBoard = newBoard;
                                if(rootPlayer.playType == ALPHA_BETA) {
                                    alpha = Math.max(alpha, score);
                                    if (beta <= alpha) {
                                        //System.out.println("Pruning the tree");
                                        return score;
                                    }
                                }
                            }
                            else if(tempScore == score){
                                bestBoards.add(newBoard);
                            }
                        } else {
                            // For Min player
                            if (tempScore < score) {
                                bestBoards.clear();
                                bestBoards.add(newBoard);
                                score = tempScore;
                                bestBoard = newBoard;
                                if(rootPlayer.playType == ALPHA_BETA) {
                                    beta = Math.min(beta, score);
                                    if (beta <= alpha) {
                                        //System.out.println("Pruning the tree");
                                        return score;
                                    }
                                }
                            }
                            else if(tempScore == score){
                                bestBoards.add(newBoard);
                            }
                        }
                    }

                    if(!(j==0)){
                        if(currBoard[i+1][j-1] != currPlayer){
                            newBoard = createNewBoard(currBoard);
                            //System.out.println("Moving diagonally left from "+i+" "+j+" to "+(i+1)+" "+(j-1)+"currPlayer "+currPlayer);
                            //printBoard(currBoard);
                            newBoard[i+1][j-1] = currPlayer;
                            newBoard[i][j] = 0;
                            //printBoard(newBoard);
                            int tempScore = minimax(newBoard, rootPlayer, opponent, origDepth, (currDepth-1), alpha, beta, numPawnsAtEnemyLineForWin);
                            // For Max player
                            if (currPlayer == rootPlayer.player) {
                                if (tempScore > score) {
                                    bestBoards.clear();
                                    bestBoards.add(newBoard);
                                    score = tempScore;
                                    bestBoard = newBoard;
                                    if(rootPlayer.playType == ALPHA_BETA) {
                                        alpha = Math.max(alpha, score);
                                        if (beta <= alpha) {
                                            //System.out.println("Pruning the tree");
                                            return score;
                                        }
                                    }
                                }
                                else if(tempScore == score){
                                    bestBoards.add(newBoard);
                                }
                            } else {
                                // For Min player
                                if (tempScore < score) {
                                    bestBoards.clear();
                                    bestBoards.add(newBoard);
                                    score = tempScore;
                                    bestBoard = newBoard;
                                    if(rootPlayer.playType == ALPHA_BETA) {
                                        beta = Math.min(beta, score);
                                        if (beta <= alpha) {
                                            //System.out.println("Pruning the tree");
                                            return score;
                                        }
                                    }
                                }
                                else if(tempScore == score){
                                    bestBoards.add(newBoard);
                                }
                            }
                        }
                    }

                    if(!(j==ncols-1)){
                        if(currBoard[i+1][j+1] != currPlayer){
                            newBoard = createNewBoard(currBoard);
                            //System.out.println("Moving diagonally right from "+i+" "+j+" to "+(i+1)+" "+(j+1)+" curr player "+currPlayer);
                            //printBoard(currBoard);
                            newBoard[i+1][j+1] = currPlayer;
                            newBoard[i][j] = 0;
                            //printBoard(newBoard);
                            int tempScore = minimax(newBoard, rootPlayer, opponent, origDepth, (currDepth-1), alpha, beta, numPawnsAtEnemyLineForWin);
                            // For Max player
                            if (currPlayer == rootPlayer.player) {
                                if (tempScore > score) {
                                    bestBoards.clear();
                                    bestBoards.add(newBoard);
                                    score = tempScore;
                                    bestBoard = newBoard;
                                    if(rootPlayer.playType == ALPHA_BETA) {
                                        alpha = Math.max(alpha, score);
                                        if (beta <= alpha) {
                                            //System.out.println("Pruning the tree");
                                            return score;
                                        }
                                    }
                                }
                                else if(tempScore == score){
                                    bestBoards.add(newBoard);
                                }
                            } else {
                                // For Min player
                                if (tempScore < score) {
                                    bestBoards.clear();
                                    bestBoards.add(newBoard);
                                    score = tempScore;
                                    bestBoard = newBoard;
                                    if(rootPlayer.playType == ALPHA_BETA) {
                                        beta = Math.min(beta, score);
                                        if (beta <= alpha) {
                                            //System.out.println("Pruning the tree");
                                            return score;
                                        }
                                    }
                                }
                                else if(tempScore == score){
                                    bestBoards.add(newBoard);
                                }
                            }
                        }
                    }
                }
            }
        }
        else{ // for PLAYER2
            for(int i=nrows-1; i>=0; i--){
                for(int j=0; j<ncols; j++){
                    if((currBoard[i][j] == 0) || (currBoard[i][j] == PLAYER1) ||
                            ((i==0) && (currBoard[i][j] == PLAYER2))){
                        continue;
                    }

                    //System.out.println("Processing "+i+" "+j+" for player "+currPlayer+" rootPlayer "+rootPlayer.player+" at depth "+currDepth);
                    if(currBoard[i-1][j] == 0) {
                        // move ahead
                        newBoard = createNewBoard(currBoard);
                        //System.out.println("Moving ahead from "+i+" "+j+" to "+(i-1)+" "+j+"currPlayer "+currPlayer);
                        //printBoard(currBoard);
                        newBoard[i-1][j] = currPlayer;
                        newBoard[i][j] = 0;
                        //printBoard(newBoard);
                        int tempScore = minimax(newBoard, rootPlayer, opponent, origDepth, (currDepth-1), alpha, beta, numPawnsAtEnemyLineForWin);
                        // For Max player
                        if (currPlayer == rootPlayer.player) {
                            if (tempScore > score) {
                                bestBoards.clear();
                                bestBoards.add(newBoard);
                                score = tempScore;
                                bestBoard = newBoard;
                                if(rootPlayer.playType == ALPHA_BETA) {
                                    alpha = Math.max(alpha, score);
                                    if (beta <= alpha) {
                                        //System.out.println("Pruning the tree");
                                        return score;
                                    }
                                }
                            }
                            else if(tempScore == score){
                                bestBoards.add(newBoard);
                            }
                        } else {
                            // For Min player
                            if (tempScore < score) {
                                bestBoards.clear();
                                bestBoards.add(newBoard);
                                score = tempScore;
                                bestBoard = newBoard;
                                if(rootPlayer.playType == ALPHA_BETA) {
                                    beta = Math.min(beta, score);
                                    if (beta <= alpha) {
                                        //System.out.println("Pruning the tree");
                                        return score;
                                    }
                                }
                            }
                            else if(tempScore == score){
                                bestBoards.add(newBoard);
                            }
                        }
                    }

                    if(!(j==0)){
                        if(currBoard[i-1][j-1] != currPlayer){
                            newBoard = createNewBoard(currBoard);
                            //System.out.println("Moving diagonally left from "+i+" "+j+" to "+(i-1)+" "+(j-1)+"currPlayer "+currPlayer);
                            //printBoard(currBoard);
                            newBoard[i-1][j-1] = currPlayer;
                            newBoard[i][j] = 0;
                            //printBoard(newBoard);
                            int tempScore = minimax(newBoard, rootPlayer, opponent, origDepth, (currDepth-1), alpha, beta, numPawnsAtEnemyLineForWin);
                            // For Max player
                            if (currPlayer == rootPlayer.player) {
                                if (tempScore > score) {
                                    bestBoards.clear();
                                    bestBoards.add(newBoard);
                                    score = tempScore;
                                    bestBoard = newBoard;
                                    if(rootPlayer.playType == ALPHA_BETA) {
                                        alpha = Math.max(alpha, score);
                                        if (beta <= alpha) {
                                            //System.out.println("Pruning the tree");
                                            return score;
                                        }
                                    }
                                }
                                else if(tempScore == score){
                                    bestBoards.add(newBoard);
                                }
                            } else {
                                // For Min player
                                if (tempScore < score) {
                                    bestBoards.clear();
                                    bestBoards.add(newBoard);
                                    score = tempScore;
                                    bestBoard = newBoard;
                                    if(rootPlayer.playType == ALPHA_BETA) {
                                        beta = Math.min(beta, score);
                                        if (beta <= alpha) {
                                            //System.out.println("Pruning the tree");
                                            return score;
                                        }
                                    }
                                }
                                else if(tempScore == score){
                                    bestBoards.add(newBoard);
                                }
                            }
                        }
                    }

                    if(!(j==ncols-1)){
                        if(currBoard[i-1][j+1] != currPlayer){
                            newBoard = createNewBoard(currBoard);
                            //System.out.println("Moving diagonally right from "+i+" "+j+" to "+(i-1)+" "+(j+1)+" curr player "+currPlayer);
                            //printBoard(currBoard);
                            newBoard[i-1][j+1] = currPlayer;
                            newBoard[i][j] = 0;
                            //printBoard(newBoard);
                            int tempScore = minimax(newBoard, rootPlayer, opponent, origDepth, (currDepth-1), alpha, beta, numPawnsAtEnemyLineForWin);
                            // For Max player
                            if (currPlayer == rootPlayer.player) {
                                if (tempScore > score) {
                                    bestBoards.clear();
                                    bestBoards.add(newBoard);
                                    score = tempScore;
                                    bestBoard = newBoard;
                                    if(rootPlayer.playType == ALPHA_BETA) {
                                        alpha = Math.max(alpha, score);
                                        if (beta <= alpha) {
                                            //System.out.println("Pruning the tree");
                                            return score;
                                        }
                                    }
                                }
                                else if(tempScore == score){
                                    bestBoards.add(newBoard);
                                }
                            } else {
                                // For Min player
                                if (tempScore < score) {
                                    bestBoards.clear();
                                    bestBoards.add(newBoard);
                                    score = tempScore;
                                    bestBoard = newBoard;
                                    if(rootPlayer.playType == ALPHA_BETA) {
                                        beta = Math.min(beta, score);
                                        if (beta <= alpha) {
                                            //System.out.println("Pruning the tree");
                                            return score;
                                        }
                                    }
                                }
                                else if(tempScore == score){
                                    bestBoards.add(newBoard);
                                }
                            }
                        }
                    }
                }
            }
        }

        // overwrite the board with best board configuration
        //System.out.println(" Finished processing the moves currDepth "+currDepth+" origDepth "+origDepth);
        if(currDepth == origDepth){
//            System.out.println("OLD BOARD:");
//            printBoard(board);
//            System.out.println("NEW BOARD:");
//            printBoard(bestBoard);
            //int index = (new Random()).nextInt(bestBoards.size());
            //bestBoard = bestBoards.get(index);
            for(int i=0; i<nrows; i++){
                for(int j=0; j<ncols; j++) {
                    board[i][j] = bestBoard[i][j];
                }
            }
        }
//        if(rootPlayer.player == currPlayer) {
//            System.out.println("Best move board score for max is " + score);
//        }
//        else{
//            System.out.println("Best move board score for min is " + score);
//        }
        return score;
    }

    public void play(Player p1, Player p2, int numPawnsAtEnemyLineForWin){

        int score;
        long startTime;
        long endTime;
        int turn = PLAYER2;
        int player;
        Player p;
        int [] status;

        do {

            if (turn == PLAYER1) {
                player = PLAYER2;
                p = p2;
                turn = PLAYER2;
            }
            else {
                player = PLAYER1;
                p = p1;
                turn = PLAYER1;
            }

            //System.out.println("PLAYER-"+player+" PLAYING AT ROOT");
            p.stats.movesSoFar++;

            startTime = System.nanoTime();
            score = minimax(board, p, player, p.searchDepth, p.searchDepth, Integer.MIN_VALUE, Integer.MAX_VALUE,
                    numPawnsAtEnemyLineForWin);
            endTime = System.nanoTime();
            p.stats.totalMoveTimeInNS += (endTime - startTime);

            //System.out.println("BEST MOVE BELOW !!!");
            //printBoard(board);

            status = boardEval.checkGameStatus(this, board, numPawnsAtEnemyLineForWin);

            if(status[0] != 0){
                int winner = status[0];
                int loser = PLAYER2;
                if(winner == PLAYER2){
                    loser = PLAYER1;
                }

                //boardEval.countEnemyPawnsCaptured(this, board, player);
                System.out.println("RESULT: Player-"+status[0]+" wins in "+p.stats.movesSoFar+" moves.");
                System.out.println();
                System.out.println("Final board configuration:");
                printBoard(board);
                System.out.println();
                System.out.println("Player-"+p1.player+" game statistics:");
                System.out.println("\t- Number of moves played = "+p1.stats.movesSoFar);
                System.out.println("\t- Number of game tree nodes expanded = "+p1.stats.nodesExpanded);
                System.out.println("\t- Average nodes expanded per move = "+p1.stats.nodesExpanded/p1.stats.movesSoFar);
                System.out.println("\t- Average time taken per move = "+p1.stats.totalMoveTimeInNS/(p1.stats.movesSoFar*1000)+" microseconds");
                System.out.println("\t- Total opponent workers captured = "+(TOTAL_PAWNS-status[loser]));
                System.out.println("\t- Total self workers captured = "+(TOTAL_PAWNS-status[winner]));
                System.out.println();
                System.out.println("Player-"+p2.player+" game statistics:");
                System.out.println("\t- Number of moves played = "+p2.stats.movesSoFar);
                System.out.println("\t- Number of game tree nodes expanded = "+p2.stats.nodesExpanded);
                System.out.println("\t- Average nodes expanded per move = "+p2.stats.nodesExpanded/p1.stats.movesSoFar);
                System.out.println("\t- Average time taken per move = "+p2.stats.totalMoveTimeInNS/(p1.stats.movesSoFar*1000)+" microseconds");
                System.out.println("\t- Total opponent workers captured = "+(TOTAL_PAWNS-status[loser]));
                System.out.println("\t- Total self workers captured = "+(TOTAL_PAWNS-status[winner]));
                System.out.println();
                return;
            }
        }while(true);
    }

    public void printBoard(int [][] tempBoard){

        System.out.print(" \t");
        for(int j=0; j<ncols; j++){
            System.out.print((j+1)+"\t");
        }
        System.out.println();

        for(int i=0; i<nrows; i++){
            System.out.print((i+1)+"\t");
            for(int j=0; j<ncols; j++){
                switch(tempBoard[i][j]){
                    case 1:
                        System.out.print("B\t");
                        break;
                    case 2:
                        System.out.print("W\t");
                        break;
                    default:
                        System.out.print(" \t");
                        break;
                }
            }
            System.out.println();
        }
    }

    public static void main(String[] args){

        Breakthrough bt;
        Player p1, p2;
        int gameCount = 1;

        /*
        // Player 1 Minimax (offensive) vs Player 2 Minimax (defensive)
        System.out.println("GAME-"+(gameCount++)+": minimax offensive (Player-1) vs minimax defensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, MINIMAX, OFFENSIVE_STRATEGY, 3);
        p2 = new Player(PLAYER2, bt.board, MINIMAX, DEFENSIVE_STRATEGY, 3);
        bt.play(p1, p2, 1);

        // Player 1 Minimax (defensive) vs Player 2 Minimax (offensive)
        System.out.println("GAME-"+(gameCount++)+": minimax defensive (Player-1) vs minimax offensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, MINIMAX, DEFENSIVE_STRATEGY, 3);
        p2 = new Player(PLAYER2, bt.board, MINIMAX, OFFENSIVE_STRATEGY, 3);
        bt.play(p1, p2, 1);

        // Player 1 Minimax (offensive) vs Player 2 Minimax (offensive)
        System.out.println("GAME-"+(gameCount++)+": minimax offensive (Player-1) vs minimax offensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, MINIMAX, OFFENSIVE_STRATEGY, 3);
        p2 = new Player(PLAYER2, bt.board, MINIMAX, OFFENSIVE_STRATEGY, 3);
        bt.play(p1, p2, 1);

        // Player 1 Minimax (defensive) vs Player 2 Minimax (defensive)
        System.out.println("GAME-"+(gameCount++)+": minimax defensive (Player-1) vs minimax defensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, MINIMAX, DEFENSIVE_STRATEGY, 3);
        p2 = new Player(PLAYER2, bt.board, MINIMAX, DEFENSIVE_STRATEGY, 3);
        bt.play(p1, p2, 1);

        // Player 1 Alpha-beta (offensive) vs Alpha-beta (defensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta offensive (Player-1) vs alpha-beta defensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        bt.play(p1, p2, 1);

        // Player 1 Alpha-beta (defensive) vs Alpha-beta (offensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta defensive (Player-1) vs alpha-beta offensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        bt.play(p1, p2, 1);

        // Player 1 Alpha-beta (offensive) vs Alpha-beta (offensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta offensive (Player-1) vs alpha-beta offensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        bt.play(p1, p2, 1);

        // Player 1 Alpha-beta (defensive) vs Alpha-beta (defensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta defensive (Player-1) vs alpha-beta defensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        bt.play(p1, p2, 1);

        // Player 1 Minimax (offensive) vs Alpha-beta (defensive)
        System.out.println("GAME-"+(gameCount++)+": minimax offensive (Player-1) vs alpha-beta defensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, MINIMAX, OFFENSIVE_STRATEGY, 3);
        p2 = new Player(PLAYER2, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        bt.play(p1, p2, 1);

        // Player 1 Minimax (defensive) vs Alpha-beta (offensive)
        System.out.println("GAME-"+(gameCount++)+": minimax defensive (Player-1) vs alpha-beta offensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, MINIMAX, DEFENSIVE_STRATEGY, 3);
        p2 = new Player(PLAYER2, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        bt.play(p1, p2, 1);

        // Player 1 Minimax (offensive) vs Alpha-beta (offensive)
        System.out.println("GAME-"+(gameCount++)+": minimax offensive (Player-1) vs alpha-beta offensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, MINIMAX, OFFENSIVE_STRATEGY, 3);
        p2 = new Player(PLAYER2, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        bt.play(p1, p2, 1);

        // Player 1 Minimax (defensive) vs Alpha-beta (defensive)
        System.out.println("GAME-"+(gameCount++)+": minimax defensive (Player-1) vs alpha-beta defensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, MINIMAX, DEFENSIVE_STRATEGY, 3);
        p2 = new Player(PLAYER2, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        bt.play(p1, p2, 1);

        // Player 1 Alpha-beta (offensive) vs Minimax (defensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta offensive (Player-1) vs minimax defensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, MINIMAX, DEFENSIVE_STRATEGY, 3);
        bt.play(p1, p2, 1);

        // Player 1 Alpha-beta (defensive) vs Minimax (offensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta defensive (Player-1) vs minimax offensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, MINIMAX, OFFENSIVE_STRATEGY, 3);
        bt.play(p1, p2, 1);

        // Player 1 Alpha-beta (offensive) vs Minimax (offensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta offensive (Player-1) vs minimax offensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, MINIMAX, OFFENSIVE_STRATEGY, 3);
        bt.play(p1, p2, 1);

        // Player 1 Alpha-beta (defensive) vs Minimax (defensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta defensive (Player-1) vs minimax defensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, MINIMAX, DEFENSIVE_STRATEGY, 3);
        bt.play(p1, p2, 1);
        */

        /* For Bonus points */
        System.out.println("For bonus points - 3 pawn wins (Player-2)");

        // Player 1 Alpha-beta (offensive) vs Alpha-beta (defensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta offensive (Player-1) vs minimax defensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, MINIMAX, DEFENSIVE_STRATEGY, 3);
        bt.play(p1, p2, 3);

        // Player 1 Alpha-beta (defensive) vs Alpha-beta (offensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta defensive (Player-1) vs minimax offensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, MINIMAX, OFFENSIVE_STRATEGY, 3);
        bt.play(p1, p2, 3);

        // Player 1 Alpha-beta (offensive) vs Alpha-beta (offensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta offensive (Player-1) vs minimax offensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, MINIMAX, OFFENSIVE_STRATEGY, 3);
        bt.play(p1, p2, 3);

        // Player 1 Alpha-beta (defensive) vs Alpha-beta (defensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta defensive (Player-1) vs minimax defensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, MINIMAX, DEFENSIVE_STRATEGY, 3);
        bt.play(p1, p2, 3);

        // Player 1 Alpha-beta (offensive) vs Alpha-beta (defensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta offensive (Player-1) vs alpha-beta defensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        bt.play(p1, p2, 3);

        // Player 1 Alpha-beta (defensive) vs Alpha-beta (offensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta defensive (Player-1) vs alpha-beta offensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        bt.play(p1, p2, 3);

        // Player 1 Alpha-beta (offensive) vs Alpha-beta (offensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta offensive (Player-1) vs alpha-beta offensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        bt.play(p1, p2, 3);

        // Player 1 Alpha-beta (defensive) vs Alpha-beta (defensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta defensive (Player-1) vs alpha-beta defensive (Player-2)");
        bt = new Breakthrough(8, 8);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        bt.play(p1, p2, 3);

        System.out.println("For bonus points - 5x10 board (Player-2)");

        // Player 1 Alpha-beta (offensive) vs Alpha-beta (defensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta offensive (Player-1) vs minimax defensive (Player-2)");
        bt = new Breakthrough(5, 10);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, MINIMAX, DEFENSIVE_STRATEGY, 3);
        bt.play(p1, p2, 1);

        // Player 1 Alpha-beta (defensive) vs Alpha-beta (offensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta defensive (Player-1) vs minimax offensive (Player-2)");
        bt = new Breakthrough(5, 10);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, MINIMAX, OFFENSIVE_STRATEGY, 3);
        bt.play(p1, p2, 1);

        // Player 1 Alpha-beta (offensive) vs Alpha-beta (offensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta offensive (Player-1) vs minimax offensive (Player-2)");
        bt = new Breakthrough(5, 10);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, MINIMAX, OFFENSIVE_STRATEGY, 3);
        bt.play(p1, p2, 1);

        // Player 1 Alpha-beta (defensive) vs Alpha-beta (defensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta defensive (Player-1) vs minimax defensive (Player-2)");
        bt = new Breakthrough(5, 10);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, MINIMAX, DEFENSIVE_STRATEGY, 3);
        bt.play(p1, p2, 1);

        // Player 1 Alpha-beta (offensive) vs Alpha-beta (defensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta offensive (Player-1) vs alpha-beta defensive (Player-2)");
        bt = new Breakthrough(5, 10);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        bt.play(p1, p2, 1);

        // Player 1 Alpha-beta (defensive) vs Alpha-beta (offensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta defensive (Player-1) vs alpha-beta offensive (Player-2)");
        bt = new Breakthrough(5, 10);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        bt.play(p1, p2, 1);

        // Player 1 Alpha-beta (offensive) vs Alpha-beta (offensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta offensive (Player-1) vs alpha-beta offensive (Player-2)");
        bt = new Breakthrough(5, 10);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, ALPHA_BETA, OFFENSIVE_STRATEGY, 5);
        bt.play(p1, p2, 1);

        // Player 1 Alpha-beta (defensive) vs Alpha-beta (defensive)
        System.out.println("GAME-"+(gameCount++)+": alpha-beta defensive (Player-1) vs alpha-beta defensive (Player-2)");
        bt = new Breakthrough(5, 10);
        p1 = new Player(PLAYER1, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        p2 = new Player(PLAYER2, bt.board, ALPHA_BETA, DEFENSIVE_STRATEGY, 5);
        bt.play(p1, p2, 1);
    }
}