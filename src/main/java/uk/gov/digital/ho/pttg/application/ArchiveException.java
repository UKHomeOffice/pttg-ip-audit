package uk.gov.digital.ho.pttg.application;

public class ArchiveException extends RuntimeException {

    public ArchiveException(Throwable cause) {
        super(cause);
    }

    public ArchiveException(String message) {
        super(message);
    }

    public ArchiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
