
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
        int depth = 8;
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

    public double gama2(GameState gameState, boolean myTurnToPlay) {

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
            int safetyPlayer = 0;
            int safetyOpponent = 0;
            int nbCheckersPlayer = 0;
            int nbCheckersOpponent = 0;
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 4; j++) {
                    if (gameState.get(i, j) == idPlayer) {
                        safetyPlayer += checkSafety(i, j, idPlayer, kingPlayer, gameState, myTurnToPlay);
                        nbCheckersPlayer++;
                    } else if (gameState.get(i, j) == kingPlayer) {
                        safetyPlayer += checkSafety(i, j, idPlayer, kingPlayer, gameState, myTurnToPlay);
                        nbCheckersPlayer += 10;
                    } else if (gameState.get(i, j) == idOpponent) {
                        safetyOpponent += checkSafety(i, j, idOpponent, kingOpponent, gameState, !myTurnToPlay);
                        nbCheckersOpponent++;
                    } else if (gameState.get(i, j) == kingOpponent) {
                        safetyOpponent += checkSafety(i, j, idOpponent, kingOpponent, gameState, !myTurnToPlay);
                        nbCheckersOpponent += 10;
                    }

                }
            }
            return ((nbCheckersPlayer - nbCheckersOpponent) + (safetyPlayer - safetyOpponent));
        }
        return 0;

    }

    public double checkSafety(int row, int col, int checkers, int checkersKings, GameState gameState, boolean myTurnToPlay) {
        int safetyValue = 0;
        double weight1 = 1;
        double weight2 = 10;
        // If we are on the side of the board we are protect
        if (row - 1 < 0 || row + 1 > 7 || col - 1 < 0 || col + 1 > 3) {
            return 1;
        }

        // If row impair
        if (row % 2 != 0) {
            // Check first diagonal
            if (gameState.get(row - 1, col - 1) == checkers || gameState.get(row - 1, col - 1) == checkersKings) {
                safetyValue += weight1;
            } else if (!(gameState.get(row - 1, col - 1) == Constants.CELL_EMPTY) && !myTurnToPlay) {
                return 0;
            }

            if (gameState.get(row + 1, col) == checkers || gameState.get(row + 1, col) == checkersKings) {
                safetyValue += weight1;
            } else if (!(gameState.get(row + 1, col) == Constants.CELL_EMPTY) && !myTurnToPlay) {
                return 0;
            }
            // Check the second
            if (gameState.get(row + 1, col - 1) == checkers || gameState.get(row + 1, col - 1) == checkersKings) {
                safetyValue += weight2;
            } else if (!(gameState.get(row + 1, col - 1) == Constants.CELL_EMPTY) && !myTurnToPlay) {
                return 0;
            }

            if (gameState.get(row - 1, col) == checkers || gameState.get(row - 1, col) == checkersKings) {
                safetyValue += weight2;
            } else if (!(gameState.get(row - 1, col) == Constants.CELL_EMPTY) && !myTurnToPlay) {
                return 0;
            }

        } else {
            // Check first diagonal
            if (gameState.get(row - 1, col) == checkers || gameState.get(row - 1, col) == checkersKings) {
                safetyValue += weight1;
            } else if (!(gameState.get(row - 1, col) == Constants.CELL_EMPTY) && !myTurnToPlay) {
                return 0;
            }

            if (gameState.get(row + 1, col + 1) == checkers || gameState.get(row + 1, col + 1) == checkersKings) {
                safetyValue += weight1;
            } else if (!(gameState.get(row + 1, col + 1) == Constants.CELL_EMPTY) && !myTurnToPlay) {
                return 0;
            }
            // Check the second
            if (gameState.get(row - 1, col + 1) == checkers || gameState.get(row - 1, col + 1) == checkersKings) {
                safetyValue += weight2;
            } else if (!(gameState.get(row - 1, col + 1) == Constants.CELL_EMPTY) && !myTurnToPlay) {
                return 0;
            }
            if (gameState.get(row + 1, col) == checkers || gameState.get(row + 1, col) == checkersKings) {
                safetyValue += weight2;
            } else if (!(gameState.get(row + 1, col) == Constants.CELL_EMPTY) && !myTurnToPlay) {
                return 0;
            }
        }
        return safetyValue >= 10 ? 1 : safetyValue > 0 ? 0.5 : 0;
    }

    public double alphaBeta(GameState gameState, int depth, double alpha, double beta, boolean myTurnToPlay, Deadline deadline) {
        depth--;
        Vector<GameState> nextStates = new Vector<GameState>();
        gameState.findPossibleMoves(nextStates);

        if (nextStates.isEmpty() || gameState.isEOG() || depth == 0 || deadline.timeUntil() < 1000) {
            return gama2(gameState, myTurnToPlay);
        }
        double v;

        if (myTurnToPlay) {
            v = Integer.MIN_VALUE;

            // Ordering moves -> that way we have more chance to prune the tree
            HashMap<Double, ArrayList<GameState>> cache = new HashMap<>();
            for (GameState newGameState : nextStates) {
                double val = gama2(newGameState, myTurnToPlay);
                if (cache.get(Integer.MAX_VALUE - val) == null) {
                    // we use Max_Value - val in order that the maxmimum value of alpha will be the first element of the set list
                    // se we can iterate from maximimum to minimum
                    cache.put(Integer.MAX_VALUE - val, new ArrayList<GameState>());
                }
                cache.get(Integer.MAX_VALUE - val).add(newGameState);
            }

            Iterator<Double> it = cache.keySet().iterator();

            while (it.hasNext()) {
                Double itValue = it.next();
                for (GameState newGameState : cache.get(itValue)) {
                    double alphaBetaVal;
                    if (valuesAlphaBeta.containsKey(newGameState.hashCode())) {
                        alphaBetaVal = valuesAlphaBeta.get(newGameState.hashCode());
                    } else {
                        alphaBetaVal = alphaBeta(newGameState, depth, alpha, beta, !myTurnToPlay, deadline);
                        valuesAlphaBeta.put(newGameState.hashCode(), alphaBetaVal);
                        valuesAlphaBeta.put(newGameState.reversed().hashCode(), alphaBetaVal);
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
            }
        } else {
            v = Integer.MAX_VALUE;
            // Ordering moves
            HashMap<Double, ArrayList<GameState>> cache = new HashMap<>();
            for (GameState newGameState : nextStates) {
                Double val = gama2(newGameState, myTurnToPlay);
                if (cache.get(val) == null) {
                    cache.put(val, new ArrayList<GameState>());
                }
                cache.get(val).add(newGameState);
            }
            Iterator<Double> it = cache.keySet().iterator();
            while (it.hasNext()) {
                Double itValue = it.next();
                for (GameState newGameState : cache.get(itValue)) {
                    double alphaBetaVal;

                    if (valuesAlphaBeta.containsKey(newGameState.hashCode())) {
                        alphaBetaVal = valuesAlphaBeta.get(newGameState.hashCode());
                    } else {
                        alphaBetaVal = alphaBeta(newGameState, depth, alpha, beta, !myTurnToPlay, deadline);
                        valuesAlphaBeta.put(newGameState.hashCode(), alphaBetaVal);
                        valuesAlphaBeta.put(newGameState.reversed().hashCode(), alphaBetaVal);
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

        }
        return v;
    }

}
