package com.gnivol.game.system.minigame;

import java.util.*;

public class SlidingLogic {
    public final int N = 10;
    public int[][] grid; // 0: Trống, 1: Tường, 2: Lỗ
    public List<Marble> marbles;
    public int totalHoles;
    public List<Marble> initialMarbles;

    public static class Marble {
        public int x, y;
        public boolean locked;

        public Marble(int x, int y) {
            this.x = x; this.y = y; this.locked = false;
        }

        public Marble clone() {
            Marble m = new Marble(this.x, this.y);
            m.locked = this.locked;
            return m;
        }
    }

    private static class BFSNode {
        List<Marble> state;
        int steps;
        BFSNode(List<Marble> state, int steps) {
            this.state = state;
            this.steps = steps;
        }
    }

    public SlidingLogic() {
        grid = new int[N][N];
        marbles = new ArrayList<>();
    }

    public void generateValidMap(int wallCount, int marbleCount, int minSteps) {
        Random rand = new Random();
        boolean valid = false;

        while (!valid) {
            grid = new int[N][N];
            marbles.clear();

            int wCount = 0;
            while (wCount < wallCount) {
                int rx = rand.nextInt(N);
                int ry = rand.nextInt(N);
                if (grid[rx][ry] == 0) {
                    grid[rx][ry] = 1;
                    wCount++;
                }
            }

            List<Marble> startPositions = new ArrayList<>();
            int mCount = 0;
            while (mCount < marbleCount) {
                int rx = rand.nextInt(N);
                int ry = rand.nextInt(N);
                if (grid[rx][ry] == 0 && !hasMarbleAt(startPositions, rx, ry)) {
                    startPositions.add(new Marble(rx, ry));
                    mCount++;
                }
            }

            List<Marble> tempState = cloneState(startPositions);
            int scrambleCount = minSteps + 5;
            for (int i = 0; i < scrambleCount; i++) {
                int isRow = rand.nextInt(2);
                int lineIdx = rand.nextInt(N);
                int dir = rand.nextInt(2) == 0 ? -1 : 1;
                if (isRow == 1) pullRowLogic(tempState, lineIdx, dir, false);
                else pullColLogic(tempState, lineIdx, dir, false);
            }

            boolean hasOverlap = false;
            for (Marble endM : tempState) {
                for (Marble startM : startPositions) {
                    if (endM.x == startM.x && endM.y == startM.y) {
                        hasOverlap = true;
                        break;
                    }
                }
                if (hasOverlap) break;
            }
            if (hasOverlap) continue;

            totalHoles = 0;
            for (Marble m : tempState) {
                grid[m.x][m.y] = 2;
                totalHoles++;
            }

            marbles = cloneState(startPositions);

            int shortestPath = solveBFS();

            if (shortestPath >= minSteps) {
                valid = true;
                this.initialMarbles = cloneState(startPositions);
            }
        }
    }

    private int solveBFS() {
        Queue<BFSNode> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(new BFSNode(cloneState(this.marbles), 0));
        visited.add(getStateString(this.marbles));

        while (!queue.isEmpty()) {
            BFSNode current = queue.poll();

            if (isWinState(current.state)) return current.steps;

            if (current.steps > 10) return -1;

            for (int i = 0; i < N; i++) {
                for (int dir : new int[]{-1, 1}) {
                    List<Marble> nextRowState = cloneState(current.state);
                    pullRowLogic(nextRowState, i, dir, true);
                    String rowStr = getStateString(nextRowState);
                    if (!visited.contains(rowStr)) {
                        visited.add(rowStr);
                        queue.add(new BFSNode(nextRowState, current.steps + 1));
                    }

                    List<Marble> nextColState = cloneState(current.state);
                    pullColLogic(nextColState, i, dir, true);
                    String colStr = getStateString(nextColState);
                    if (!visited.contains(colStr)) {
                        visited.add(colStr);
                        queue.add(new BFSNode(nextColState, current.steps + 1));
                    }
                }
            }
        }
        return -1;
    }

    public void pullRow(int row, int dir) { pullRowLogic(marbles, row, dir, true); }
    public void pullCol(int col, int dir) { pullColLogic(marbles, col, dir, true); }

    public boolean isWin() { return isWinState(marbles); }

    private void pullRowLogic(List<Marble> state, int row, int dir, boolean checkHoles) {
        List<Marble> activeMarbles = new ArrayList<>();
        for (Marble m : state) if (m.y == row && !m.locked) activeMarbles.add(m);

        if (dir == -1) Collections.sort(activeMarbles, Comparator.comparingInt(m -> m.x));
        else Collections.sort(activeMarbles, (m1, m2) -> Integer.compare(m2.x, m1.x));

        for (Marble m : activeMarbles) {
            while (true) {
                int nx = m.x + dir;
                if (nx < 0 || nx >= N || grid[nx][m.y] == 1 || hasUnlockedMarbleAt(state, nx, m.y)) break;
                m.x = nx;
            }
            if (checkHoles && grid[m.x][m.y] == 2 && !isHoleOccupiedByLocked(state, m.x, m.y)) m.locked = true;
        }
    }

    private void pullColLogic(List<Marble> state, int col, int dir, boolean checkHoles) {
        List<Marble> activeMarbles = new ArrayList<>();
        for (Marble m : state) if (m.x == col && !m.locked) activeMarbles.add(m);

        if (dir == -1) Collections.sort(activeMarbles, Comparator.comparingInt(m -> m.y));
        else Collections.sort(activeMarbles, (m1, m2) -> Integer.compare(m2.y, m1.y));

        for (Marble m : activeMarbles) {
            while (true) {
                int ny = m.y + dir;
                if (ny < 0 || ny >= N || grid[m.x][ny] == 1 || hasUnlockedMarbleAt(state, m.x, ny)) break;
                m.y = ny;
            }
            if (checkHoles && grid[m.x][m.y] == 2 && !isHoleOccupiedByLocked(state, m.x, m.y)) m.locked = true;
        }
    }

    private List<Marble> cloneState(List<Marble> source) {
        List<Marble> copy = new ArrayList<>();
        for (Marble m : source) copy.add(m.clone());
        return copy;
    }

    private String getStateString(List<Marble> state) {
        StringBuilder sb = new StringBuilder();
        for (Marble m : state) sb.append(m.x).append(",").append(m.y).append(m.locked ? "L|" : "U|");
        return sb.toString();
    }

    private boolean isWinState(List<Marble> state) {
        for (Marble m : state) if (!m.locked) return false;
        return true;
    }

    private boolean hasMarbleAt(List<Marble> state, int x, int y) {
        for (Marble m : state) if (m.x == x && m.y == y) return true;
        return false;
    }

    private boolean hasUnlockedMarbleAt(List<Marble> state, int x, int y) {
        for (Marble m : state) if (m.x == x && m.y == y && !m.locked) return true;
        return false;
    }

    private boolean isHoleOccupiedByLocked(List<Marble> state, int x, int y) {
        for (Marble m : state) if (m.x == x && m.y == y && m.locked) return true;
        return false;
    }

    public void resetBoard() {
        if (initialMarbles != null) {
            this.marbles = cloneState(initialMarbles);
        }
    }
}
