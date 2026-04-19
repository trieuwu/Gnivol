package com.gnivol.game.system.minigame;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class LaserLogic {
    public int N;
    public int[][] grid;

    private static class Node {
        int x, y, t, steps;
        Node(int x, int y, int t, int steps) {
            this.x = x;
            this.y = y;
            this.t = t;
            this.steps = steps;
        }
    }

    public LaserLogic() {}

    public void generateValidMap(int size, int wallCount, int laserCount) {
        this.N = size;
        boolean mapIsValid = false;
        Random rand = new Random();

        while (!mapIsValid) {
            grid = new int[N][N];

            for (int i = 0; i < wallCount; i++) {
                int rx = rand.nextInt(N);
                int ry = rand.nextInt(N);
                if ((rx==0 && ry==0) || (rx == N-1 && ry==N-1)) continue;
                grid[rx][ry] = 1;
            }

            for(int i = 0; i < laserCount; i++) {
                int rx = rand.nextInt(N);
                int ry = rand.nextInt(N);
                if((rx==0 && ry==0) || (rx == N-1 && ry==N-1)) continue;

                if (grid[rx][ry] == 0) {
                    grid[rx][ry] = 2 + rand.nextInt(4);

                }
            }

            int steps = botPlayBFS();
            int manhattan = (N - 1) * 2;

            if (steps != -1 && steps <= manhattan + 5) {
                mapIsValid = true;
            }
        }
    }

    public int botPlayBFS() {
        boolean[][][] visited = new boolean[N][N][4];
        Queue<Node> queue = new LinkedList<>();
        queue.add(new Node(0, 0, 0, 0));
        visited[0][0][0] = true;

        int[] dx = {0, 0, -1, 1};
        int[] dy = {1, -1, 0, 0};

        while (!queue.isEmpty()) {
            Node curr =  queue.poll();
            if (curr.x == N - 1 && curr.y == N - 1) return curr.steps;
            int nextTime = (curr.t + 1) % 4;

            for (int i = 0; i < 4; i++) {
                int nx = curr.x + dx[i];
                int ny = curr.y + dy[i];
                if(nx >= 0 && nx < N && ny >= 0 && ny < N) {
                    if(grid[nx][ny] != 0) continue;

                    if(!isTileDangerous(nx, ny, nextTime)) {
                        if(!visited[nx][ny][nextTime]) {
                            visited[nx][ny][nextTime] = true;
                            queue.add(new Node(nx, ny, nextTime, curr.steps + 1));
                        }
                    }
                }
            }
        }
        return -1;
    }

    public boolean isTileDangerous(int x, int y, int time) {
        for (int i = y - 1; i >= 0; i--) {
            if (grid[x][i] == 1) break;
            if (grid[x][i] >= 2) {
                if (getLaserDirection(grid[x][i], time) == 2) return true;
                break;
            }
        }

        for (int i = x - 1; i >= 0; i--) {
            if (grid[i][y] == 1) break;
            if (grid[i][y] >= 2) {
                if (getLaserDirection(grid[i][y], time) == 3) return true;
                break;
            }
        }

        for (int i = y + 1; i < N; i++) {
            if (grid[x][i] == 1) break;
            if (grid[x][i] >= 2) {
                if (getLaserDirection(grid[x][i], time) == 4) return true;
                break;
            }
        }

        for (int i = x + 1; i < N; i++) {
            if (grid[i][y] == 1) break;
            if (grid[i][y] >= 2) {
                if (getLaserDirection(grid[i][y], time) == 5) return true;
                break;
            }
        }
        return false;
    }

    private int getLaserDirection(int baseType, int time) {
        int offset = baseType - 2;
        int currentOffset = (offset + time) % 4;

        return 2 + currentOffset;
    }
}
