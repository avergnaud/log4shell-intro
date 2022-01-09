package intro;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestLog4j2 {

    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        logger.info("hello");
        logger.info("${jndi:ldap://localhost:389/uid=adrien,ou=users,dc=brochain}");
    }
}
