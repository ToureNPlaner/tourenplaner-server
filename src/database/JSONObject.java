package database;

/**
 * Class to bundle a json byte array and the according user id.
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 *
 */
public class JSONObject {

    private int userID;
    private byte[] jsonByteArray;
    
    public JSONObject(int userID, byte[] jsonByteArray) {
        userID = this.userID;
        this.jsonByteArray = jsonByteArray;
    }

    public int getUserID() {
        return userID;
    }

    public byte[] getJsonByteArray() {
        return jsonByteArray;
    }
}
