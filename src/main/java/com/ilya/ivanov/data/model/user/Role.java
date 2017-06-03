package com.ilya.ivanov.data.model.user;

/**
 * Created by ilya on 5/18/17.
 */
public enum Role {
    USER(Role.READ | Role.EXECUTE | Role.WRITE | Role.EDIT, "User-mode"),
    ADMIN(USER.getPermissions() | Role.ADMINISTRATE, "Admin-mode");

    // allows USER to browse the data
    private static final int READ = 0x1;
    // allows USER to add files; rename and remove his own files
    private static final int WRITE = 0x2;
    //allows USER to open files
    private static final int EXECUTE = 0x4;
    // allows USER to remove all files
    private static final int EDIT = 0x8;
    // application admin
    private static final int ADMINISTRATE = 0x10;

    private final int permissions;

    private final String description;

    private int getPermissions() {
        return permissions;
    }

    Role(int permissions, String description) {
        this.permissions = permissions;
        this.description = description;
    }

    public boolean hasReadPermission() {
        return (permissions & READ) != 0;
    }

    public boolean hasWritePermission() {
        return (permissions & WRITE) != 0;
    }

    public boolean hasExecutePermission() {
        return (permissions & EXECUTE) != 0;
    }

    public boolean hasEditPermission() {
        return (permissions & EDIT) != 0;
    }

    public boolean hasAdminPermission() {
        return (permissions & ADMINISTRATE) != 0;
    }

    public boolean hasRole(Role role) {
        return permissions >= role.getPermissions();
    }

    public String getDescription() {
        return description;
    }
}
