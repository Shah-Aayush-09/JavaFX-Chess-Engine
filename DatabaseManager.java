package org.example;

import java.sql.*;
import java.util.List;

public class DatabaseManager {
    static final String DB_URL = "jdbc:mysql://localhost:3306/chess_db";
    static final String DB_USER = "root";
    static final String DB_PASS = "";

    static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL driver not found in project paths!");
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static void saveGame(String type, int whitePts, int blackPts, String result, String winner, List<String> moves) {
        String addGameQuery = type.equals("PVP")
                ? "INSERT INTO pvp_games (white_score, black_score, outcome, winner) VALUES (?, ?, ?, ?)"
                : "INSERT INTO pve_games (white_score, black_score, outcome, winner) VALUES (?, ?, ?, ?)";

        String addMoveQuery = "INSERT INTO game_moves (game_id, game_type, move_number, notation) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement gameCmd = conn.prepareStatement(addGameQuery, Statement.RETURN_GENERATED_KEYS)) {
                gameCmd.setInt(1, whitePts);
                gameCmd.setInt(2, blackPts);
                gameCmd.setString(3, result);
                gameCmd.setString(4, winner);
                gameCmd.executeUpdate();

                int gameId = -1;
                try (ResultSet keys = gameCmd.getGeneratedKeys()) {
                    if (keys.next()) {
                        gameId = keys.getInt(1);
                    }
                }

                if (gameId != -1) {
                    try (PreparedStatement moveCmd = conn.prepareStatement(addMoveQuery)) {
                        for (int i = 0; i < moves.size(); i++) {
                            moveCmd.setInt(1, gameId);
                            moveCmd.setString(2, type);
                            moveCmd.setInt(3, i + 1);
                            moveCmd.setString(4, moves.get(i));
                            moveCmd.addBatch();
                        }
                        moveCmd.executeBatch();
                    }
                }

                conn.commit();
                System.out.println(type + " match cleanly saved into its dedicated table!");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Failed to save match data: " + e.getMessage());
        }
    }

    public static String fetchProfileStats() {
        String pvpQuery = "SELECT COUNT(*) as total, SUM(winner='Player 1') as p1, SUM(winner='Player 2') as p2," +
                " SUM(outcome='STALEMATE') as draws FROM pvp_games";
        String pveQuery = "SELECT COUNT(*) as total, SUM(winner='Player') as p_win, SUM(winner='Bot') as b_win," +
                " SUM(outcome='STALEMATE') as draws FROM pve_games";

        int pvpTotal = 0, p1Wins = 0, p2Wins = 0, pvpDraws = 0;
        int pveTotal = 0, pveWins = 0, botWins = 0, pveDraws = 0;

        try (Connection conn = getConnection()) {
            // pvp stats
            try (PreparedStatement stmt = conn.prepareStatement(pvpQuery); ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt("total") > 0) {
                    pvpTotal = rs.getInt("total");
                    p1Wins = rs.getInt("p1");
                    p2Wins = rs.getInt("p2");
                    pvpDraws = rs.getInt("draws");
                }
            }
            // pve stats
            try (PreparedStatement stmt = conn.prepareStatement(pveQuery); ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt("total") > 0) {
                    pveTotal = rs.getInt("total");
                    pveWins = rs.getInt("p_win");
                    botWins = rs.getInt("b_win");
                    pveDraws = rs.getInt("draws");
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to read profile data: " + e.getMessage());
            return "Error loading profile details.";
        }

        if (pvpTotal == 0 && pveTotal == 0) {
            return "No games recorded yet! Play a match to build your profile.";
        }

        double botWinPct = pveTotal > 0 ? (Math.round(((double) pveWins / pveTotal) * 100 * 10.0) / 10.0) : 0.0;
        int puzzlesSolvedCount = fetchSolvedPuzzlesCount();

        return "---------- PROFILE STATS -----------\n\n" +
                "----- PVP STATS -----\n" +
                "Total Matches: " + pvpTotal + "\n" +
                "Player 1 Wins: " + p1Wins + "  |  Player 2 Wins: " + p2Wins + "\n" +
                "Draws/Stalemates: " + pvpDraws + "\n\n" +
                "----- PVE STATS -----\n" +
                "Total Matches: " + pveTotal + "\n" +
                "Your Wins: " + pveWins + "  |  Bot Wins: " + botWins + "\n" +
                "Draws/Stalemates: " + pveDraws + "\n" +
                "Win Rate vs Bot: " + botWinPct + "%\n\n" +
                "----- PUZZLE STATS -----\n" +
                "Unique Puzzles Solved: " + puzzlesSolvedCount;
    }

    public static void savePuzzleSolve(int puzzleId) {
        String query = "INSERT INTO solved_puzzles (puzzle_id) VALUES (?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, puzzleId);
            stmt.executeUpdate();
            System.out.println("Puzzle #" + puzzleId + " completion logged into database!");
        } catch (SQLException e) {
            System.err.println("Failed to save puzzle progress: " + e.getMessage());
        }
    }

    public static int fetchSolvedPuzzlesCount() {
        String query = "SELECT COUNT(DISTINCT puzzle_id) as total_solved FROM solved_puzzles";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total_solved");
            }
        } catch (SQLException e) {
            System.err.println("Error reading solved puzzles: " + e.getMessage());
        }
        return 0;
    }
}