package ir.internship.logmonitor;

import ir.internship.logmonitor.api.Api;

import java.io.IOException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws IOException, SQLException {
        Api x = new Api();
        x.start();
    }
}