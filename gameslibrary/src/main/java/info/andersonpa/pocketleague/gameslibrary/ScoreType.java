package info.andersonpa.pocketleague.gameslibrary;

public enum ScoreType {
    BINARY("Win or Loss."),
    POINTS("Highest score wins."),
    POINTS_INVERSE("Lowest score wins."),
    TIME("Shortest time wins."),
    TIME_INVERSE("Longest time wins.");

    private String scoretype_label;

    ScoreType(String label) {
        scoretype_label = label;
    }

    @Override
    public String toString() {
        return scoretype_label;
    }
}
