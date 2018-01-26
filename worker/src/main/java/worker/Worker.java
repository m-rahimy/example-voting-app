package worker;

import db.DatabaseConnection;
import db.DatabaseUtils;
import entity.People;
import redis.clients.jedis.Jedis;
import org.json.JSONObject;

import java.sql.*;

class Worker {
    public static void main(String[] args) {
        System.out.println("Starting Java Worker - Modified By Local");

        //try {
        Jedis redis = DatabaseConnection.getInstance().connectToRedis("redis");
        //Connection dbConn = DatabaseConnection.getInstance().connectToDB();

        System.err.println("Watching vote queue");
        boolean exit = false;
        while (!exit) {
            // this is a blocking pop.
            String voteJSON = redis.blpop(0, "votes").get(1);
            System.err.printf("We have json file : %s \n", voteJSON);
            JSONObject voteData = new JSONObject(voteJSON);
            String voterID = voteData.getString("voter_id");
            //todo: get a json object of vote full of data
            String vote = voteData.getString("vote");

            System.err.printf("Processing vote for '%s' by '%s'\n", vote, voterID);
            People people = new People(
                    voterID, "", "", "",
                    "", "", "", null);
            if (DatabaseUtils.getInstance().insertPeople(people)) {
                System.err.printf("Insert people (id= %s) is done ", people.getId());
                // TODO: INSERT vote
            } /*else {
                    throw new SQLException();
                }*/
            //updateVote(dbConn, voterID, vote);
        }
        /*} catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }*/
    }

    /**
     *    We update vote in another way:
     *    PeopleDAO INSERTS every vote in insert() method
     */
    static void updateVote(Connection dbConn, String voterID, String vote) throws SQLException {
        // todo: update
        PreparedStatement insert = dbConn.prepareStatement(
                "INSERT INTO votes (id, vote) VALUES (?, ?)");
        insert.setString(1, voterID);
        insert.setString(2, vote);

        try {
            insert.executeUpdate();
        } catch (SQLException e) {
            PreparedStatement update = dbConn.prepareStatement(
                    "UPDATE votes SET vote = ? WHERE id = ?");
            update.setString(1, vote);
            update.setString(2, voterID);
            update.executeUpdate();
        }
    }

}
