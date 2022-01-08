package attaquant.v1;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

public class JNDIBind {

    public static void main(String[] args) {

        Hashtable env = new Hashtable();
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "cn=admin,dc=brochain");
        env.put(Context.SECURITY_CREDENTIALS, "admin");
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                        "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://localhost:389");

        try {
            Context context = new InitialContext(env);
            context.bind("cn=malicious,dc=brochain", "malicious valeur");
            context.close();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
}
