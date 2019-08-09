package com.cahill;

import java.util.Arrays;

/**
 * Created by cahillt on 6/5/16.
 *
 */
public class Console {
  private static final String NEW_LINE = System.getProperty("line.separator");

  public static void main(String[] args) {
    boolean exit = false;

    java.io.Console console = System.console();

    //TODO Name
    console.printf("Peer to Peer Lending Console for <NAME>");
    console.printf(NEW_LINE);
    String username = console.readLine("Username: ");
    char[] password = console.readPassword("Password: ");
    Arrays.fill(password, ' ');
    console.printf("Welcome, %s", username);
    console.printf(NEW_LINE);

    //TODO list commands
    console.printf("Commands: ");
    console.printf(NEW_LINE);
    console.printf("\tlist: will list current holdings");
    console.printf(NEW_LINE);

    while (!exit) {

      String input = console.readLine().toLowerCase();

      if (input.equals("exit")) {
        exit = true;
      } else if (input.equals("list")) {

      }
    }
  }


}
