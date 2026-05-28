package main.java;

import main.java.dao.UserDAO;
import main.java.database.DatabaseInitializer;
import main.java.model.User;
import main.java.ui.LoginUI;

public class Test {
    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        LoginUI.main(args); // Run Login UI
    }
}