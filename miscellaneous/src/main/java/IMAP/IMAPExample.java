package IMAP;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

/**
 * Created by cahillt on 6/29/15.
 */
public class IMAPExample {

  public static void main(String[] args) throws MessagingException {
    Properties properties = new Properties();
    properties.setProperty("imap.host", "imap.gmail.com"); //Currently connects to Google
    properties.setProperty("imap.port", "993"); //Per email service
    properties.setProperty("mail.store.protocol", "imap");
    properties.setProperty("mail.debug", "true");  //Debug gives lots of output

    Session session = Session.getInstance(properties);
    Store store = session.getStore("imaps"); //Or imap, depending on if it uses SSL
    System.out.println("Connecting");
    store.connect("imap.gmail.com", args[0], args[1]);
    System.out.print("Connected");

    //List All top level folders and their first depth children
    Folder[] folders = store.getDefaultFolder().list();
    for (Folder f : folders) {
      if (f.list().length > 0) {
        for (Folder child : f.list()) {
          System.out.println(f.getFullName() + ":" + child.getFullName());
        }
      }
      System.out.println(f.getFullName());
    }
  }
}
