package util;

public class ErrorMessages {

    public static final String CRYPTO_EXPORT_RESTRICTIONS = "Our efforts to use 256-bit keys were thwarped by Java's export restrictions on cryptography. You need to install the Cryptographic Policy Extensions from Oracle.";
    public static final String INTERNAL_FAILURE = "Internal failure! ";
    public static final String CLIPBOARD_FAILURE = "Internal failure! Clipboard probably does not contain your password right now.";
    public static final String INVALID_OLD_MASTER_PASSWORD = "Unable to decrypt file with given master password!";
    public static final String PASSWORDS_DO_NOT_MATCH = "Passwords do not match!";
    public static final String KEYFILE_LOAD_FAILED = "File not recognized as a valid BaoPass keyfile!";

}
