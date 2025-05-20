import java.util.HashSet;
import java.util.Set;

public class MaxNumberFromMatrix {

    private static final int SIZE = 3;
    private static int[][] matrix;
    private static boolean[][] visited;
    private static long maxNumber = 0;

    public static void main(String[] args) {
        matrix = new int[][]{
                {5, 8, 3},
                {9, 1, 7},
                {4, 6, 2}
        };

        if (!isValidMatrix(matrix)) {
            System.out.println("The matrix is invalid.  It must be 3x3 in size and contain the digits from 1 to 9 exactly once.");
            return;
        }

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                visited = new boolean[SIZE][SIZE];
                visited[row][col] = true;
                findMaxNumber(row, col, String.valueOf(matrix[row][col]));
            }
        }

        System.out.println("Maximum number: " + maxNumber);
    }

    private static boolean isValidMatrix(int[][] matrix) {
        if (matrix == null || matrix.length != SIZE) {
            return false;
        }
        for (int i = 0; i < SIZE; i++) {
            if (matrix[i] == null || matrix[i].length != SIZE) {
                return false;
            }
        }

        Set<Integer> numbers = new HashSet<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (matrix[i][j] < 1 || matrix[i][j] > 9) {
                    return false;
                }
                if (!numbers.add(matrix[i][j])) {
                    return false;
                }
            }
        }
        return numbers.size() == 9;
    }

    private static void findMaxNumber(int row, int col, String currentNumber) {
        maxNumber = Math.max(maxNumber, Long.parseLong(currentNumber));

        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (isValid(newRow, newCol) && !visited[newRow][newCol]) {
                visited[newRow][newCol] = true;
                findMaxNumber(newRow, newCol, currentNumber + matrix[newRow][newCol]);
                visited[newRow][newCol] = false;
            }
        }
    }


    private static boolean isValid(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }
}