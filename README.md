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

This plugin expresses two dependencies on dcache modules.  In order
to build locally, the procedure must currently be to clone locally
the main dcache-server repository, install the .jars in the local
maven repository, then switch back to this plugin repository and run the
build.  

Assuming maven and git are installed locally, the steps would then be:


1.  In an appropriate local directory, checkout dCache from GitHub (here we use https, read-only):

    git clone https://github.com/dCache/dcache.git
    
2.  cd to the dcache directory created by the clone, and checkout 
    the version/branch (master, 3.1, 3.0, etc.) corresponding to the
    desired version of this plugin; e.g.,
    
    mvn checkout -b 3.0 origin/3.0
    
    then do:

    mvn install -DskipTests
    
3.  switch to your checked out dcache-snow repository, and do (continuing with
    the version above, for instance):

    mvn checkout 3.0
    mvn clean package

This should produce a .zip file for the plugin.

A pre-built .zip is included in this repository in the lib directory.

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
