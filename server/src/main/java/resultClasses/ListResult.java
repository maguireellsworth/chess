package resultClasses;

import models.GameModel;

import java.util.List;

public class ListResult extends Result{
    private List<GameModel> games;

    public ListResult(String message, List<GameModel> games){
        super(message);
        this.games = games;
    }
}
