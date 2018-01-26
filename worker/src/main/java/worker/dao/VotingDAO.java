package dao;

import db.DatabaseConnection;
import entity.Candidate;
import entity.People;
import entity.Voting;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VotingDAO {


    private static VotingDAO ins = null;
    private DatabaseConnection connection;

    public synchronized static VotingDAO getInstance(DatabaseConnection connection) {
        if (ins == null) {
            ins = new VotingDAO(connection);
        }
        return ins;
    }

    private VotingDAO(DatabaseConnection connection) {
        this.connection = connection;
    }


    /**
     * insert a vote into database
     * if already voted -> just change count and change update time
     * if already voted to the other option -> change it
     *
     * @param voting
     * @return
     */
    public boolean insert(Voting voting) {
        // TODO: check for previous votes
        People people = voting.getVoter();
        Candidate candidate = voting.getCandidate();

        // Already voted for this ... no need to change?
        if (PeopleDAO.getInstance(connection).hasVoted(people, candidate.getId(), true)) {
            try {
                PreparedStatement statement = connection.getDatabaseConnection()
                        .prepareStatement(
                                "UPDATE voting SET " +
                                        "last_change = ? " +
                                        "changes_count=changes_count+1 " +
                                        "WHERE id = ?;"
                        );

                String date = LocalDate.now().toString();
                int i = 1;
                statement.setString(i++, date);
                statement.setString(i++, voting.getId());

                statement.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        if (PeopleDAO.getInstance(connection).hasVotedToOpponent(people, candidate, true)) {
            // TODO: change vote and set opponent

            voting.setCandidate(candidate.getOpponent());

            // persist changes
            try {
                PreparedStatement statement = connection.getDatabaseConnection()
                        .prepareStatement(
                                "UPDATE voting SET " +
                                        "candidate_id = ? , " +
                                        "last_change = ? " +
                                        "changes_count=changes_count+1 " +
                                        "WHERE id = ?;"
                        );

                String date = LocalDate.now().toString();
                int i = 1;
                statement.setString(i++, voting.getCandidate().getId());
                statement.setString(i++, date);
                statement.setString(i++, voting.getId());
                statement.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        }


        try {
            PreparedStatement statement = connection.getDatabaseConnection()
                    .prepareStatement(
                            "INSERT INTO voting (" +
                                    "voter_id ," +
                                    "candidate_id, " +
                                    "first_voted," +
                                    "last_change," +
                                    "changes_count" +
                                    ") " +
                                    "VALUES (?,?,?,?,1); "
                    );

            String date = LocalDate.now().toString();
            int i = 1;
            statement.setString(i++, people.getId());
            statement.setString(i++, candidate.getId());
            statement.setString(i++, date);
            statement.setString(i++, date);

            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * remove a voting from database
     * checks for keys?
     * @param voting
     * @return
     */
    public boolean delete(Voting voting) {
        try {
            PreparedStatement statement = connection.getDatabaseConnection()
                    .prepareStatement(
                            "DELETE FROM voting WHERE id=?; "
                    );

            statement.setString(1, voting.getId());
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * updates a voting
     *
     * @param voting
     * @return
     */
    public boolean update(Voting voting) {
        try {
            PreparedStatement statement = connection.getDatabaseConnection()
                    .prepareStatement(
                            "UPDATE voting SET " +
                                    "voter_id=? ," +
                                    "candidate_id=? ," +
                                    "first_voted=? ," +
                                    "last_change=? ," +
                                    "changes_count=? ," +
                                    "WHERE id = ?;"
                    );

            int i = 1;
            statement.setString(i++, voting.getVoter().getId());
            statement.setString(i++, voting.getCandidate().getId());
            statement.setString(i++, voting.getFirstVoted());
            statement.setString(i++, voting.getLastChange());
            statement.setInt(i++, voting.getChangesCount());
            statement.setString(i++, voting.getId());

            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * get all the votes by everyone
     *
     * @return
     */
    public List<Voting> getAll() {
        List<Voting> votingList = new ArrayList<>();

        try {
            PreparedStatement statement = connection.getDatabaseConnection()
                    .prepareStatement(
                            "SELECT * FROM voting; "
                    );


            ResultSet result = statement.executeQuery();
            while (result.next()) {
                int i = 1;
                String id = result.getString(i++);
                Voting voting = new Voting(
                        id,
                        PeopleDAO.getInstance(connection).get(result.getString(i++)),
                        CandidateDAO.getInstance(connection).get(result.getString(i++)),
                        result.getString(i++),
                        result.getString(i++),
                        result.getInt(i++)
                );

                votingList.add(voting);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return votingList;
    }


    public List<Voting> getAllForPeople(String peopleID) {
        List<Voting> votingList = new ArrayList<>();

        try {
            PreparedStatement statement = connection.getDatabaseConnection()
                    .prepareStatement(
                            "SELECT * FROM voting WHERE voter_id=?; "
                    );

            statement.setString(1, peopleID);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                int i = 1;
                String id = result.getString(i++);
                Voting voting = new Voting(
                        id,
                        PeopleDAO.getInstance(connection).get(result.getString(i++)),
                        CandidateDAO.getInstance(connection).get(result.getString(i++)),
                        result.getString(i++),
                        result.getString(i++),
                        result.getInt(i++)
                );

                votingList.add(voting);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return votingList;
    }

    public Voting get(String id) {
        Voting voting = null;
        try {
            PreparedStatement statement = connection.getDatabaseConnection()
                    .prepareStatement(
                            "SELECT * FROM voting WHERE id=?; "
                    );

            statement.setString(1, id);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                int i = 1;
                voting = new Voting(
                        id,
                        PeopleDAO.getInstance(connection).get(result.getString(i++)),
                        CandidateDAO.getInstance(connection).get(result.getString(i++)),
                        result.getString(i++),
                        result.getString(i++),
                        result.getInt(i++)
                );

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return voting;
    }

}
