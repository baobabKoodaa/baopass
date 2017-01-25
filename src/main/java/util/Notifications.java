package util;

public class Notifications {

    public static final String ABOUT = "<html>BaoPass by Baobab, unreleased<br>developer version. For updates,<br>visit https://baobab.fi/baopass";
    public static final String SUCCESSFUL_NEW_KEY_ENCRYPTION = "<html>Your keyfile has been encrypted<br>succesfully and saved under filename<br>";
    public static final String SUCCESSFUL_KEY_GENERATION =
            "<html>Your random keyfile has been<br>" +
            "generated succesfully. Next you<br>" +
            "will be asked to choose a master<br>" +
            "password to encrypt the keyfile.";

    public static String MPWchange(String oldKeyFile, String newKeyFile) {
        return "<html>Master password changed<br>" +
                "succesfully. You should<br>" +
                "backup the updated keyfile<br>" +
                newKeyFile + "<br>" +
                "Your old keyfile was renamed<br>" +
                oldKeyFile;
    }

}
