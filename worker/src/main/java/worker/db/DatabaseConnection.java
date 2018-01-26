package db;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import utils.ThreadUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseConnection {

    private boolean isConnected = false;
    private boolean isTablesInitialized = false;
    private Connection databaseConnection;

    private static DatabaseConnection instance = null;

    public synchronized static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private DatabaseConnection() {

    }

    public void close() {
        if (!isConnected) return;

        try {
            databaseConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        databaseConnection = null;
        isConnected = false;
    }

    public Connection getDatabaseConnection() {
        return databaseConnection;
    }

    public Jedis connectToRedis(String host) {
        Jedis conn = new Jedis(host);

        while (true) {
            try {
                conn.keys("*");
                break;
            } catch (JedisConnectionException e) {
                System.err.println("Waiting for redis");
                ThreadUtils.sleep(1000);
            }
        }

        System.err.println("Connected to redis");
        return conn;
    }

    public boolean connect() {
        if (isConnected) return true;

        try {

            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://" + // base
                    "db" + // host name alias
                    "/postgres"; // database name

            while (databaseConnection == null) {
                try {
                    databaseConnection = DriverManager.getConnection(url, "postgres", "theCamelsHateUs");
                } catch (SQLException e) {
                    long s = 1000;
                    System.err.println("Error in init, Waiting for db in " + s + " ms");
                    ThreadUtils.sleep(s);
                }
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            isConnected = false;
            System.exit(1);

        }

        isConnected = true;
        if (!isTablesInitialized)
            try {
                initTables(databaseConnection);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        System.err.println("Connected to db");
        return true;
    }

    /**
     * todo: make sure to delete DROP lines
     *
     * @param conn
     * @throws SQLException
     */
    private void initTables(Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(
                "DROP TABLE IF EXISTS voting; \n" +
                        "DROP TABLE IF EXISTS people; \n" +
                        "DROP TABLE IF EXISTS candidate; \n" +

                        "CREATE TABLE IF NOT EXISTS  people (  \n" +
                        "id VARCHAR(36) PRIMARY KEY NOT NULL,  \n" +
                        "full_name VARCHAR(50)  DEFAULT NULL,  \n" +
                        "user_agent VARCHAR(255) DEFAULT NULL,  \n" +
                        "remote_address VARCHAR(20) DEFAULT NULL,  \n" +
                        "remote_port VARCHAR(4) DEFAULT NULL,  \n" +
                        "first_visit VARCHAR(20) DEFAULT NULL,  \n" +
                        "last_visit VARCHAR(20) DEFAULT NULL) ; \n" +

                        "CREATE TABLE IF NOT EXISTS  voting ( \n" +
                        "id SERIAL PRIMARY KEY  NOT NULL,  \n" +
                        "voter_id VARCHAR(36)  NOT NULL,  \n" +
                        "candidate_id INT  NOT NULL,  \n" +
                        "first_voted VARCHAR(20) DEFAULT NULL,  \n" +
                        "last_change VARCHAR(20) DEFAULT NULL,  \n" +
                        "changes_count INT DEFAULT 1) ; \n" +

                        "CREATE TABLE IF NOT EXISTS candidate (  \n" +
                        "id SERIAL PRIMARY KEY  NOT NULL,  \n" +
                        "name VARCHAR(20)  NOT NULL,  \n" +
                        "opponent_id INT  NOT NULL) ; \n" +

                        "DO $$BEGIN  BEGIN    \n" +
                        "ALTER TABLE voting       \n" +
                        "ADD CONSTRAINT voting_ibfk_1 FOREIGN KEY (voter_id) REFERENCES people (id); \n" +
                        "  EXCEPTION    \n" +
                        "WHEN duplicate_object\n" +
                        "  THEN \n" +
                        "RAISE NOTICE 'Table constraint voting_ibfk_1 already exists'; \n" +
                        "  END; \n" +
                        "END $$; \n" +

                        "DO $$BEGIN  BEGIN    \n" +
                        "ALTER TABLE voting       \n" +
                        "ADD CONSTRAINT voting_ibfk_2 FOREIGN KEY (candidate_id) REFERENCES candidate (id); \n" +
                        "  EXCEPTION    \n" +
                        "WHEN duplicate_object \n" +
                        "THEN RAISE NOTICE 'Table constraint voting_ibfk_2 already exists'; \n" +
                        "  END; \n" +
                        "END $$; \n" +

                        "DO $$BEGIN  BEGIN    \n" +
                        "ALTER TABLE candidate \n" +
                        "ADD CONSTRAINT candidate_ibfk_1 FOREIGN KEY (opponent_id) REFERENCES candidate (id); \n" +
                        "  EXCEPTION   \n" +
                        "WHEN duplicate_object\n" +
                        "THEN RAISE NOTICE 'Table constraint voting_ibfk_2 already exists'; \n" +
                        "  END; \n" +
                        "END $$; \n" +

                        "CREATE OR REPLACE FUNCTION fetch_candidates() \n" +
                        "RETURNS TABLE(op1ID INTEGER, op1Name VARCHAR(20), op2ID INTEGER, op2Name VARCHAR(20)) AS $$ \n" +
                        "    BEGIN      \n" +
                        "RETURN QUERY            \n" +
                        "SELECT         \n" +
                        "candidate.id AS op1ID,      \n" +
                        "candidate.name AS op1Name,            \n" +
                        "A.id AS op2ID,   \n" +
                        "A.name AS op2Name           \n" +
                        "FROM              \n" +
                        "candidate A           \n" +
                        "JOIN candidate           \n" +
                        "ON candidate.id = A.opponent_id        \n" +
                        "WHERE candidate.id < A.id; \n" +
                        "   END; \n" +
                        "$$ LANGUAGE plpgsql; \n" +

                        "CREATE OR REPLACE FUNCTION fetch_votes_for_voter(id TEXT) \n" +
                        "\tRETURNS TABLE(voteID INTEGER, voterID VARCHAR(36), voterName VARCHAR(36), candidateID INTEGER, firstVoted VARCHAR(20), lastChange VARCHAR(20) , changesCount INTEGER) AS $$ \n" +
                        "    BEGIN      \n" +
                        "\tRETURN QUERY            \n" +
                        "\t\tSELECT         \n" +
                        "\t\t\tvoting.id AS voteID,      \n" +
                        "\t\t\tpeople.id AS voterID,            \n" +
                        "                        people.full_name AS voterName,\n" +
                        "\t\t\tcandidate_id AS candidateID,   \n" +
                        "\t\t\tfirst_voted AS firstVoted,           \n" +
                        "\t\t\tlast_change AS lastChange,           \n" +
                        "\t\t\tchanges_count AS changesCount           \n" +
                        "\t\tFROM              \n" +
                        "\t\t\tvoting \n" +
                        "                JOIN candidate ON voting.candidate_id=candidate.id\n" +
                        "\t\tJOIN people ON voter_id = people.id\n" +
                        "\t\tWHERE candidate.id = $1; \n" +
                        "   END; \n" +
                        "$$ LANGUAGE plpgsql; "+

                        "INSERT INTO candidate (name, opponent_id) values ('iOS','1'); \n" +
                        "INSERT INTO candidate (name, opponent_id) values ('Android','1'); \n" +
                        "UPDATE candidate SET opponent_id=2 WHERE id=1; \n" +

                        "INSERT INTO candidate (name, opponent_id) values ('Cats','3'); \n" +
                        "INSERT INTO candidate (name, opponent_id) values ('Dogs','3'); \n" +
                        "UPDATE candidate SET opponent_id=4 WHERE id=3; \n" +

                        "INSERT INTO candidate (name, opponent_id) values ('Pepsi','5'); \n" +
                        "INSERT INTO candidate (name, opponent_id) values ('Coca Cola','5'); \n" +
                        "UPDATE candidate SET opponent_id=6 WHERE id=5; \n" +

                        "INSERT INTO candidate (name, opponent_id) values ('Blue Pen','7'); \n" +
                        "INSERT INTO candidate (name, opponent_id) values ('Black Pen','7'); \n" +
                        "UPDATE candidate SET opponent_id=8 WHERE id=7; \n" +

                        "INSERT INTO candidate (name, opponent_id) values ('Regular Pen','9'); \n" +
                        "INSERT INTO candidate (name, opponent_id) values ('Fountain Pen','9'); \n" +
                        "UPDATE candidate SET opponent_id=10 WHERE id=9; \n" +

                        "INSERT INTO candidate (name, opponent_id) values ('Pencil','11'); \n" +
                        "INSERT INTO candidate (name, opponent_id) values ('Mechanical Pencil','11'); \n" +
                        "UPDATE candidate SET opponent_id=12 WHERE id=11; \n"
                // todo: insert another rows here

        );

        st.executeUpdate();
        isTablesInitialized = true;

    }

}
