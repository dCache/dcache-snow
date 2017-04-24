Snow Alarm Plugin
======

This is a plugin providing a listener to the dCache alarms system.
The alarm is converted into a ServiceNow incident (JSON) and sent
to a ServiceNow instance.  It is to be used in connection with an
instance of the dCache alarm service.

Contributors
============
[Fermi National Accelerator Laboratory] (http://www.fnal.gov)

License
=======
The project is licensed under under __BSD__ and __LGPL__. See the source code for details.

Building the plugin
===================
Run:

    mvn clean package -am -pl plugins/snow

This should produce a .zip file for the plugin.

Installing the plugin
=====================
The zip file should be unzipped in /usr/local/share/dcache/plugins.

The directory will contain the necessary .jars, plus the following files:

    keygen.sh  -- to create a public/private RSA key pair in a form usable by
                  the plugin

    encrypt.sh -- to generate an encrypted password from the public key;
                  this should be included as the <password></password> in
                  the snow-incident.xml configuration

    snow-listener.properties -- the standard dCache properties file for this
                  this plugin

    snow-incident.xml -- the "template" for the ticket.  Global information
                  (the same for every such ticket) should be set in this file.

Run the provided bash scripts to generate the keys and to encrypt the password.
Make sure the properties for the private key path are correctly set to whereever
you place the private key (used to decrypt the password).  Add the password
to snow-incident.xml.  Then restart the alarms domain.
