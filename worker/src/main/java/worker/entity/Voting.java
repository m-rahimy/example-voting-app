package entity;

public class Voting {

    private String id;
    private People voter;
    private Candidate candidate;
    private String firstVoted;
    private String lastChange;
    private int changesCount;

    public Voting(String id, People voter, Candidate candidate, String firstVoted, String lastChange, int changesCount) {
        this.id = id;
        this.voter = voter;
        this.candidate = candidate;
        this.firstVoted = firstVoted;
        this.lastChange = lastChange;
        this.changesCount = changesCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public People getVoter() {
        return voter;
    }

    public void setVoter(People voter) {
        this.voter = voter;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public String getFirstVoted() {
        return firstVoted;
    }

    public void setFirstVoted(String firstVoted) {
        this.firstVoted = firstVoted;
    }

    public String getLastChange() {
        return lastChange;
    }

    public void setLastChange(String lastChange) {
        this.lastChange = lastChange;
    }

    public int getChangesCount() {
        return changesCount;
    }

    public void setChangesCount(int changesCount) {
        this.changesCount = changesCount;
    }

    @Override
    public String toString() {
        return "Voting{" +
                "id='" + id + '\'' +
                ", " + voter +
                ", " + candidate +
                ", firstVoted='" + firstVoted + '\'' +
                ", lastChange='" + lastChange + '\'' +
                ", changesCount=" + changesCount +
                '}';
    }
}
