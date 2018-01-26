package db;

import dao.PeopleDAO;
import dao.CandidateDAO;
import dao.VotingDAO;
import entity.People;
import entity.Candidate;
import entity.Voting;

public class DatabaseUtils {

    private static DatabaseConnection connection = DatabaseConnection.getInstance();
    private static DatabaseUtils instance = null;

    public static DatabaseUtils getInstance() {
        if (instance == null)
            instance = new DatabaseUtils();

        return instance;
    }

    public boolean insertPeople(People people) {
        System.err.printf("MY_DEBUG (%s.java) : inserting people with id= %s\n", getClass().getSimpleName(), people.getId());
        boolean success = false;
        if (connection.connect()) {
            success = PeopleDAO.getInstance(connection).insert(people);
            connection.close();
        }

        System.err.printf("MY_DEBUG (%s.java) : (DONE) inserting people with id= %s\n", getClass().getSimpleName(), people.getId());
        return success;

    }

    public boolean insertVote(Candidate candidate) {
        return CandidateDAO.getInstance(connection).insert(candidate);
    }

    public boolean insertVoting(Voting voting) {

        return VotingDAO.getInstance(connection).insert(voting);
    }
}
