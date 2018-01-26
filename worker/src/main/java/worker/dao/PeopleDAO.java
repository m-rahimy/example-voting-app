package dao;

import db.DatabaseConnection;
import entity.Candidate;
import entity.People;
import entity.Voting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PeopleDAO {

    private static PeopleDAO ins = null;
    private DatabaseConnection connection;

    public static synchronized PeopleDAO getInstance(DatabaseConnection connection) {
        if (ins == null) {
            ins = new PeopleDAO(connection);
        }
        return ins;
    }

    private PeopleDAO(DatabaseConnection connection) {
        this.connection = connection;
    }

    /**
     * inserting a single site visitor
     *
     * @param people
     * @return
     */
    public boolean insert(People people) {
        String id = people.getId();
        if (id == null || id.equals("")) {
            people.setId(UUID.randomUUID().toString());
        }

        try {
            Connection conn = connection.getDatabaseConnection();
            PreparedStatement statement = connection.getDatabaseConnection()
                    .prepareStatement(
                            "INSERT INTO people VALUES (?,?,?,?,?,?,?); "
                    );

            int i = 1;
            statement.setString(i++, people.getId());
            statement.setString(i++, people.getFullName());
            statement.setString(i++, people.getUserAgent());
            statement.setString(i++, people.getRemoteAddress());
            statement.setString(i++, people.getRemotePort());
            statement.setString(i++, people.getFirstVisit());
            statement.setString(i++, people.getLastVisit());

            statement.executeUpdate();

            // todo: insert every vote
            if (people.getVoteList() != null) {
                for (Voting voting : people.getVoteList()) {
                    VotingDAO.getInstance(connection).insert(voting);
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Java Worker : Caught SQL Exception: Updating People ");
            update(people);
            e.printStackTrace();
        }

        return false;
    }

    /**
     * deleting a single site visitor with their relations
     *
     * @param people
     * @return
     */
    public boolean delete(People people) {

        try {
            PreparedStatement statement = connection.getDatabaseConnection()
                    .prepareStatement(
                            "DELETE FROM people WHERE id=?; "
                    );

            int i = 1;
            statement.setString(i++, people.getId());

            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * update info about a single site visitor
     *
     * @param people
     * @return
     */
    public boolean update(People people) {

        try {
            PreparedStatement statement = connection.getDatabaseConnection()
                    .prepareStatement(
                            "UPDATE people SET " +
                                    "full_name=?, " +
                                    "user_agent=?, " +
                                    "remote_address=?, " +
                                    "remote_port=?, " +
                                    "first_visit=?, " +
                                    "last_visit=? " +
                                    "WHERE id=?; "
                    );

            int i = 1;
            statement.setString(i++, people.getFullName());
            statement.setString(i++, people.getUserAgent());
            statement.setString(i++, people.getRemoteAddress());
            statement.setString(i++, people.getRemotePort());
            statement.setString(i++, people.getFirstVisit());
            statement.setString(i++, people.getLastVisit());
            statement.setString(i++, people.getId());

            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * get all as list
     *
     * @return
     */
    public List<People> getAll() {
        List<People> peopleList = new ArrayList<>();

        try {
            PreparedStatement statement = connection.getDatabaseConnection()
                    .prepareStatement(
                            "SELECT * FROM people; "
                    );


            ResultSet result = statement.executeQuery();
            while (result.next()) {
                int i = 1;
                String id = result.getString(i++);
                People people = new People(
                        id,
                        result.getString(i++),
                        result.getString(i++),
                        result.getString(i++),
                        result.getString(i++),
                        result.getString(i++),
                        result.getString(i++),
                        null
                );

                people.setVoteList(VotingDAO.getInstance(connection).getAllForPeople(id));
                peopleList.add(people);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return peopleList;
    }

    /**
     * get a single visitor by id
     *
     * @param id
     * @return
     */
    public People get(String id) {
        People people = null;

        try {
            PreparedStatement statement = connection.getDatabaseConnection()
                    .prepareStatement(
                            "SELECT * FROM people WHERE id = ?; "
                    );

            statement.setString(1, id);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                int i = 1;
                people = new People(
                        id,
                        result.getString(i++),
                        result.getString(i++),
                        result.getString(i++),
                        result.getString(i++),
                        result.getString(i++),
                        result.getString(i++),
                        null
                );

                people.setVoteList(VotingDAO.getInstance(connection).getAllForPeople(id));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return people;
    }

    /**
     * count all site visitors
     *
     * @return
     */
    public int count() {
        return getAll().size();
    }

    /**
     * count all votes for a single visitor including changed votes
     * need to check database also
     *
     * @param people
     * @return
     */
    public int countVotes(People people, boolean fresh) {
        if (fresh)
            people.setVoteList(VotingDAO.getInstance(connection)
                    .getAllForPeople(people.getId()));

        int sum = 0;
        for (Voting voting : people.getVoteList()) {
            int c = voting.getChangesCount();
            sum += c == 0 ? 1 : c;
        }

        return sum;
    }

    /**
     * count all votes for a single visitor excluding changed votes
     * need to check database also
     *
     * @param people
     * @return
     */
    public int countDistinctVotes(People people, boolean fresh) {
        if (fresh)
            people.setVoteList(VotingDAO.getInstance(connection)
                    .getAllForPeople(people.getId()));

        return people.getVoteList().size();
    }

    /**
     * check if the visitor has any relations in voting table
     * need to check database also
     *
     * @param people
     * @return
     */
    public boolean hasAnyVotes(People people) {
        people.setVoteList(VotingDAO.getInstance(connection)
                .getAllForPeople(people.getId()));
        return people.getVoteList() != null || people.getVoteList().size() != 0;
    }

    /**
     * check if the visitor has voted to the specified candidate
     * todo: maybe need to check database also
     *
     * @param people
     * @param candidateID
     * @return
     */
    public boolean hasVoted(People people, String candidateID, boolean fresh) {
        if (fresh)
            people.setVoteList(VotingDAO.getInstance(connection)
                    .getAllForPeople(people.getId()));

        for (Voting voting : people.getVoteList()) {
            if (voting.getId().equals(candidateID)) {
                return true;
            }
        }

        return false;
    }

    /**
     * check if the visitor has voted to the specified candidate's opponent
     *
     * @param people
     * @param candidate
     * @return
     */
    public boolean hasVotedToOpponent(People people, Candidate candidate, boolean fresh) {

        if (fresh)
            people.setVoteList(VotingDAO.getInstance(connection)
                    .getAllForPeople(people.getId()));

        for (Voting voting : people.getVoteList()) {
            if (voting.getId().equals(candidate.getOpponent().getId())) {
                return true;
            }
        }

        return false;
    }
}


