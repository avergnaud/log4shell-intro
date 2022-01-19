package intro;

import javax.naming.*;
import javax.naming.directory.*;
import java.util.*;

public class TestDNS {

    com.sun.jndi.dns.DnsContextFactory a;

    public static void main(String[] args) {
        try {
            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY,
                    "com.sun.jndi.dns.DnsContextFactory");
            env.put(Context.PROVIDER_URL, "dns://8.8.8.8/");

            DirContext ctx = new InitialDirContext(env);
            Object o = ctx.lookup("jmdoudoux.fr");
            Attributes attrs = ctx.getAttributes("jmdoudoux.fr",
                    new String[]{"A", "CNAME"});

            for (NamingEnumeration ae = attrs.getAll(); ae.hasMoreElements(); ) {
                Attribute attr = (Attribute) ae.next();
                String attrId = attr.getID();
                for (Enumeration vals = attr.getAll();
                     vals.hasMoreElements();
                     System.out.println(attrId + ": " + vals.nextElement())
                )
                    ;
            }
            ctx.close();
        } catch (Exception e) {
            System.err.println("Probleme lors de l'interrogation du DNS: " + e);
            e.printStackTrace();
        }
    }
}
