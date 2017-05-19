package com.ilya.ivanov.data.model;

/**
 * Created by ilya on 5/18/17.
 */
public enum Role {
    GUEST(Role.read),
    USER(GUEST.getPermissions() | Role.execute | Role.write),
    ADMIN(USER.getPermissions() | Role.edit);

    // allows USER to browse the data
    private static final int read = 0x1;
    //allows USER to open files
    private static final int execute = 0x2;
    // allows USER to add files; rename and remove his own files
    private static final int write = 0x4;
    // allows USER to remove all files
    private static final int edit = 0x8;

    private final int permissions;

    public int getPermissions() {
        return permissions;
    }

    Role(int permissions) {
        this.permissions = permissions;
    }
}
