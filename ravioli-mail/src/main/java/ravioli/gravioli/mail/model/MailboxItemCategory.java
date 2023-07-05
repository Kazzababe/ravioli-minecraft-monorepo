package ravioli.gravioli.mail.model;

public enum MailboxItemCategory {
    GENERAL(0, 0),
    PATCH_NOTES(0, 0),
    ANNOUNCEMENTS(0, 0);

    private final int unreadModelData;
    private final int readModelData;

    MailboxItemCategory(final int unreadModelData, final int readModelData) {
        this.unreadModelData = unreadModelData;
        this.readModelData = readModelData;
    }

    public int unreadModelData() {
        return this.unreadModelData;
    }

    public int readModelData() {
        return this.readModelData;
    }
}
