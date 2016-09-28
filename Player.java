
import java.util.*;

public class Player {

    /**
     * Performs a move
     *
     * @param pState the current state of the board
     * @param pDue time before which we must have returned
     * @return the next state the board is in after our move
     */
    // We stock the values of alpha beta ( repeated states )
    HashMap<Integer, Double> valuesAlphaBeta = new HashMap<>();
    int idPlayer;
    int idOpponent;
    int kingPlayer;
    int kingOpponent;

    public GameState play(final GameState pState, final Deadline pDue) {

        idOpponent = pState.getNextPlayer() == Constants.CELL_RED ? Constants.CELL_WHITE : Constants.CELL_RED;
        idPlayer = pState.getNextPlayer();

        if (idPlayer == Constants.CELL_RED) {
            kingPlayer = Constants.CELL_RED | Constants.CELL_KING;
            kingOpponent = Constants.CELL_WHITE | Constants.CELL_KING;
        } else {
            kingPlayer = Constants.CELL_WHITE | Constants.CELL_KING;
            kingOpponent = Constants.CELL_RED | Constants.CELL_KING;
        }
        Vector<GameState> lNextStates = new Vector<GameState>();
        pState.findPossibleMoves(lNextStates);

        if (lNextStates.size() == 0) {
            // Must play "pass" move if there are no other moves possible.
            return new GameState(pState, new Move());
        }

        /**
         * Here you should write your algorithms to get the best next move, i.e.
         * the best next state. This skeleton returns a random move instead.
         */
        // Random random = new Random();
        // return lNextStates.elementAt(random.nextInt(lNextStates.size()));
        int bestIndex = 0;
        int index = 0;
        int depth = 9;
        double beta = Integer.MAX_VALUE;
        double alpha = Integer.MIN_VALUE;
        double v = Integer.MIN_VALUE;
        boolean myTurnToPlay = true;

        System.err.println("Searching BEST MOVE " + idPlayer);
        /* int size = lnextStates.size();
        if (size <= 30) {
            depth = Integer.MAX_VALUE;
        }*/

        for (GameState newGameState : lNextStates) {

            if (pDue.timeUntil() < 50) {
                System.err.println("No more time exit");
                break;
            }

            double alphaBetaVal = alphaBeta(newGameState, depth, alpha, beta, !myTurnToPlay, pDue);

            if (alphaBetaVal > v) {
                System.err.println("New best move " + newGameState.getMove().toString() + " score  " + alphaBetaVal);
                v = alphaBetaVal;
                bestIndex = index;
            }

            if (v > alpha) {
                alpha = v;
            }

            if (beta <= alpha) {
                break;
            }

            index++;
        }
        return lNextStates.get(bestIndex);
    }

    public double gama2(GameState gameState, boolean myTurnToPlay, boolean verbose) {

        if (gameState.isRedWin()) {
            if (idPlayer == Constants.CELL_RED) {
                return Integer.MAX_VALUE;
            } else if (idPlayer == Constants.CELL_WHITE) {
                return Integer.MIN_VALUE;
            }
        } else if (gameState.isWhiteWin()) {
            if (idPlayer == Constants.CELL_WHITE) {
                return Integer.MAX_VALUE;
            } else if (idPlayer == Constants.CELL_RED) {
                return Integer.MIN_VALUE;
            }
        } else {
            double safetyPlayer = 0;
            double safetyOpponent = 0;
            double positionValuePlayer = 0;
            double positionValueOppo = 0;
            double nbCheckersPlayer = 0;
            double nbCheckersOpponent = 0;
            double valueKing = 5;
            double valueCheckers = 3;
            int distanceToKingPlayer = 7 * 12;
            int distanceToKingOppo = 7 * 12;

            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (gameState.get(i, j) == idPlayer) {
                        positionValuePlayer += positionValue(i, j);
                        nbCheckersPlayer += valueCheckers;

                        if (idPlayer == Constants.CELL_RED) {
                            distanceToKingPlayer -= i;
                        } else {
                            distanceToKingPlayer -= (7 - i);
                        }

                    } else if (gameState.get(i, j) == kingPlayer) {
                        if (verbose) {
                            System.err.println("King P  " + i + "," + j);
                        }
                        positionValuePlayer += positionValue(i, j);

                        nbCheckersPlayer += valueKing;
                        distanceToKingPlayer -= 7;
                    } else if (gameState.get(i, j) == idOpponent) {
                        positionValueOppo += positionValue(i, j);
                        nbCheckersOpponent += valueCheckers;
                        if (idOpponent == Constants.CELL_RED) {
                            distanceToKingOppo -= i;
                        } else {
                            distanceToKingOppo -= (7 - i);
                        }

                    } else if (gameState.get(i, j) == kingOpponent) {
                        if (verbose) {
                            System.err.println("King O  " + i + "," + j);
                        }
                        positionValueOppo += positionValue(i, j);
                        nbCheckersOpponent += valueKing;
                        distanceToKingOppo -= 7;
                    }

                }
            }
            return (nbCheckersPlayer - nbCheckersOpponent) + positionValuePlayer - positionValueOppo + distanceToKingOppo - distanceToKingPlayer;//+ safetyPlayer -safetyOpponent;
        }
        return 0;

    }

    public double positionValue(int row, int col) {

        double[][] caseValue
                = {{0, 1, 0, 1, 0, 1, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 1, 0, 1, 0, 1, 0}};

        return caseValue[row][col];
    }

    public double checkSafety(int row, int col, int checkers, int checkersKings, GameState gameState) {
        int safetyValue = 0;
        double weight1 = 1;
        double weight2 = 10;
        // If we are on the side of the board we are protect
        if (row - 1 < 0 || row + 1 > 7 || col - 1 < 0 || col + 1 > 7) {
            return 1;
        }

        // Check first diagonal
        if (gameState.get(row - 1, col - 1) == checkers || gameState.get(row - 1, col - 1) == checkersKings) {
            safetyValue += weight1;
        }

        if (gameState.get(row + 1, col + 1) == checkers || gameState.get(row + 1, col + 1) == checkersKings) {
            safetyValue += weight1;
        }
        // Check the second
        if (gameState.get(row - 1, col + 1) == checkers || gameState.get(row - 1, col + 1) == checkersKings) {
            safetyValue += weight2;
        }
        if (gameState.get(row + 1, col - 1) == checkers || gameState.get(row + 1, col - 1) == checkersKings) {
            safetyValue += weight2;
        }

        if (safetyValue == 11 || safetyValue == 21 || safetyValue == 12 || safetyValue == 22) {
            return 1;
        } else if (safetyValue == 1 || safetyValue == 2 || safetyValue == 10 || safetyValue == 20) {
            return 0.5;
        }
        return 0;
    }

    public double alphaBeta(GameState gameState, int depth, double alpha, double beta, boolean myTurnToPlay, Deadline deadline) {
        depth--;
        Vector<GameState> nextStates = new Vector<GameState>();
        gameState.findPossibleMoves(nextStates);

        if (nextStates.isEmpty() || gameState.isEOG() || depth == 0 || deadline.timeUntil() < 1000) {
            return gama2(gameState, myTurnToPlay, false);
        }
        double v;

        if (myTurnToPlay) {
            v = Integer.MIN_VALUE;

            for (GameState newGameState : nextStates) {

                double alphaBetaVal;
                if (valuesAlphaBeta.containsKey(newGameState.hashCode())) {
                    alphaBetaVal = valuesAlphaBeta.get(newGameState.hashCode());
                } else {
                    alphaBetaVal = alphaBeta(newGameState, depth, alpha, beta, !myTurnToPlay, deadline);
                    valuesAlphaBeta.put(newGameState.hashCode(), alphaBetaVal);
                }
                if (alphaBetaVal > v) {
                    v = alphaBetaVal;
                }

                if (v > alpha) {
                    alpha = v;
                }

                if (beta <= alpha) {
                    break;
                }

            }
        } else {
            v = Integer.MAX_VALUE;

            for (GameState newGameState : nextStates) {
                double alphaBetaVal;

                if (valuesAlphaBeta.containsKey(newGameState.hashCode())) {
                    alphaBetaVal = valuesAlphaBeta.get(newGameState.hashCode());
                } else {
                    alphaBetaVal = alphaBeta(newGameState, depth, alpha, beta, !myTurnToPlay, deadline);
                    valuesAlphaBeta.put(newGameState.hashCode(), alphaBetaVal);
                }
                if (alphaBetaVal < v) {
                    v = alphaBetaVal;
                }

                if (v < beta) {
                    beta = v;
                }

                if (beta <= alpha) {
                    break;
                }

            }

        }
        return v;
    }

}
