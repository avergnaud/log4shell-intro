package org.example;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public class TestLDAP {

    public static void main(String[] args) {
        Hashtable env = new Hashtable();
        env
                .put(Context.INITIAL_CONTEXT_FACTORY,
                        "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://localhost:389");
        /*
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "cn=admin,dc=brochain");
        env.put(Context.SECURITY_CREDENTIALS, "admin");
        */

        DirContext dirContext;

        try {
            dirContext = new InitialDirContext(env);
            Attributes attributs = dirContext.getAttributes("uid=adrien,ou=users,dc=brochain");
            Attribute attribut = attributs.get("homeDirectory");
            System.out.println(attribut.get());/* "/home/adrien" */
            dirContext.close();
        } catch (NamingException e) {
            System.err.println("Erreur lors de l'acces au serveur LDAP" + e);
            e.printStackTrace();
        }
    }
}