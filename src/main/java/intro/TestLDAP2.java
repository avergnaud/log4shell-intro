package intro;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Hashtable;

public class TestLDAP2 {

    public static void main(String[] args) {

        try {
            DirContext dirContext = new InitialDirContext();

            // Perform search using URL
            NamingEnumeration answer = dirContext.search(
                    "ldap://localhost:389/ou=users,dc=brochain",
                    "uid=adrien", null);

            // Print the answer
            printSearchEnumeration(answer);

            dirContext.close();
        } catch (NamingException e) {
            System.err.println("Erreur lors de l'acces au serveur LDAP" + e);
            e.printStackTrace();
        }
    }
    public static void printSearchEnumeration(NamingEnumeration namingEnum) {
        try {
            while (namingEnum.hasMore()) {
                SearchResult sr = (SearchResult)namingEnum.next();
                System.out.println(">>>" + sr.getName());
                System.out.println(sr.getAttributes());
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
}