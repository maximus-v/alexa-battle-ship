package alexa.battleship;

import java.util.ResourceBundle;

public class ResponseHelper {

    private ResourceBundle messages;

    public ResponseHelper(ResourceBundle messages) {
        this.messages = messages;
    }

    public String handlePlayerShotEvent(String shotevent) {
        String result = "";
        switch (shotevent) {
            case "WINNER":
                result = messages.getString("alexa.battleship.player.winner.text");
                break;
            case "DESTROYED":
                result = messages.getString("alexa.battleship.player.destroy.text");
                break;
            case "HIT":
                result = messages.getString("alexa.battleship.player.hit.text");
                break;
            case "WATER":
                result = messages.getString("alexa.battleship.player.water.text");
                break;
            case "ALLREADYSHOT":
                result = messages.getString("alexa.battleship.player.allready.shot.coordinates.text");
                break;
            case "NOTEXISTS":
                result = messages.getString("alexa.battleship.player.coordinates.notexists.text");
                break;
            case "GAMEOVER":
                result = messages.getString("alexa.battleship.player.gameover.text");
                break;
            default:
                result = messages.getString("alexa.battleship.error ");
                break;
        }
        return result;
    }

    public String handleComputerShotEvent(String shotevent) {
        String result = "";
        switch (shotevent) {
            case "WINNER":
                result = messages.getString("alexa.battleship.computer.winner.text");
                break;
            case "DESTROYED":
                result = messages.getString("alexa.battleship.computer.destroy.text");
                break;
            case "HIT":
                result = messages.getString("alexa.battleship.computer.hit.text");
                break;
            case "WATER":
                result = messages.getString("alexa.battleship.computer.water.text");
                break;
            default:
                result = messages.getString("alexa.battleship.error");
                break;
        }
        return result;
    }
}
