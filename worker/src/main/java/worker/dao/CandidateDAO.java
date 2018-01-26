package dao;

import db.DatabaseConnection;
import entity.Candidate;
import org.json.JSONArray;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CandidateDAO {


    private static CandidateDAO ins = null;
    private DatabaseConnection connection;

    public synchronized static CandidateDAO getInstance(DatabaseConnection connection) {
        if (ins == null) {
            ins = new CandidateDAO(connection);
        }
        return ins;
    }

    public CandidateDAO(DatabaseConnection connection) {
        this.connection = connection;
    }

    /**
     * insert a single candidate. This is very bad. need to change previous insertion's
     * opponent id by hand
     *
     * @param candidate
     * @return
     */
    public boolean insert(Candidate candidate) {
        try {
            PreparedStatement statement = connection.getDatabaseConnection()
                    .prepareStatement(
                            "INSERT INTO candidate (name, opponent_id) VALUES (?,?); "
                    );

            int i = 1;
            statement.setString(i++, candidate.getName());
            statement.setString(i++, candidate.getOpponent().getId());

            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean delete(Candidate candidate) {
        try {
            PreparedStatement statement = connection.getDatabaseConnection()
                    .prepareStatement(
                            "DELETE FROM candidate WHERE id=?; "
                    );

            int i = 1;
            statement.setString(i++, candidate.getId());

            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(Candidate candidate) {
        throw new RuntimeException("Not Implemented");
    }

    public List<Candidate> getAll() {
        List<Candidate> candidates = new ArrayList<>();
        try {
            PreparedStatement statement = connection.getDatabaseConnection()
                    .prepareStatement(
                            "SELECT * FROM candidate; "
                    );


            ResultSet result = statement.executeQuery();
            while (result.next()) {
                int i = 1;

                Candidate candidate = new Candidate(
                        result.getString(i++),
                        result.getString(i++),
                        null
                );

                // opponent ID
                candidate.setOpponent(getOpponent(candidate, result.getString(i++)));
                candidates.add(candidate);
            }
        } catch (
                SQLException e)

        {
            e.printStackTrace();
        }

        return candidates;
    }

    public Candidate getOpponent(Candidate candidate, String opID) throws SQLException {
        Candidate opponent = null;
        PreparedStatement statement = connection.getDatabaseConnection()
                .prepareStatement(
                        "SELECT * FROM candidate WHERE id=?; "
                );

        statement.setString(1, opID);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            int j = 1;
            opponent = new Candidate(
                    resultSet.getString(j++),
                    resultSet.getString(j++),
                    candidate
            );
        }

        return opponent;
    }

    public Candidate get(String id) {
        Candidate candidate = null;
        try {
            PreparedStatement statement = connection.getDatabaseConnection()
                    .prepareStatement(
                            "SELECT * FROM candidate WHERE id = ?; "
                    );

            statement.setString(1, id);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                int i = 1;

                candidate = new Candidate(
                        result.getString(i++),
                        result.getString(i++),
                        null
                );

                // opponent ID
                candidate.setOpponent(getOpponent(candidate, result.getString(i++)));
            }
        } catch (
                SQLException e)

        {
            e.printStackTrace();
        }

        return candidate;
    }

    public JSONArray getPollAsJsonArray(){
        JSONArray candidates = new JSONArray();
        try {
            /*PreparedStatement statement = connection.getDatabaseConnection()
                    .prepareStatement(
                            "SELECT\n" +
                                    "  candidate.name AS op1,\n" +
                                    "  A.name AS op2\n" +
                                    "FROM\n" +
                                    "  `candidate` A\n" +
                                    "JOIN\n" +
                                    "  candidate\n" +
                                    "WHERE\n" +
                                    "  candidate.id = A.`opponent_id` AND candidate.id < A.id;"
                    );*/
            CallableStatement statement = connection.getDatabaseConnection()
                    .prepareCall("{call fetch_candidates();}");


            ResultSet result = statement.executeQuery();
            while (result.next()) {
                int i = 1;
                JSONArray array = new JSONArray();
                array.put(result.getString(i++));
                array.put(result.getString(i++));

                // opponent ID
                candidates.put(array);
            }
        } catch (
                SQLException e)

        {
            e.printStackTrace();
        }

        return candidates;
    }
}
