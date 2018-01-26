package entity;

public class Candidate {

    private String id;
    private String name;
    private Candidate opponent;

    public Candidate(String id, String name, Candidate opponent) {
        this.id = id;
        this.name = name;
        this.opponent = opponent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Candidate getOpponent() {
        return opponent;
    }

    public void setOpponent(Candidate opponent) {
        this.opponent = opponent;
    }

    @Override
    public String toString() {
        return "Candidate{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", " + opponent +
                '}';
    }
}

