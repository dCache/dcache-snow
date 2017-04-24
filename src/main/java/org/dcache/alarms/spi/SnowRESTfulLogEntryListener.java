/*
COPYRIGHT STATUS:
Dec 1st 2001, Fermi National Accelerator Laboratory (FNAL) documents and
software are sponsored by the U.S. Department of Energy under Contract No.
DE-AC02-76CH03000. Therefore, the U.S. Government retains a  world-wide
non-exclusive, royalty-free license to publish or reproduce these documents
and software for U.S. Government purposes.  All documents and software
available from this server are protected under the U.S. and Foreign
Copyright Laws, and FNAL reserves all rights.

Distribution of the software available from this server is free of
charge subject to the user following the terms of the Fermitools
Software Legal Information.

Redistribution and/or modification of the software shall be accompanied
by the Fermitools Software Legal Information  (including the copyright
notice).

The user is asked to feed back problems, benefits, and/or suggestions
about the software to the Fermilab Software Providers.

Neither the name of Fermilab, the  URA, nor the names of the contributors
may be used to endorse or promote products derived from this software
without specific prior written permission.

DISCLAIMER OF LIABILITY (BSD):

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED  WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED  WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL FERMILAB,
OR THE URA, OR THE U.S. DEPARTMENT of ENERGY, OR CONTRIBUTORS BE LIABLE
FOR  ANY  DIRECT, INDIRECT,  INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
OF SUBSTITUTE  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY  OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT  OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE  POSSIBILITY OF SUCH DAMAGE.

Liabilities of the Government:

This software is provided by URA, independent from its Prime Contract
with the U.S. Department of Energy. URA is acting independently from
the Government and in its own private capacity and is not acting on
behalf of the U.S. Government, nor as its contractor nor its agent.
Correspondingly, it is understood and agreed that the U.S. Government
has no connection to this software and in no manner whatsoever shall
be liable for nor assume any responsibility or obligation for any claim,
cost, or damages arising out of or resulting from the use of the software
available from this server.

Export Control:

All documents and software available from this server are subject to U.S.
export control laws.  Anyone downloading information from this server is
obligated to secure any necessary Government licenses before exporting
documents or software obtained from this server.
 */
package org.dcache.alarms.spi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.dcache.alarms.AlarmPriority;
import org.dcache.alarms.LogEntry;
import org.dcache.alarms.file.FileBackedAlarmPriorityMap;

/**
 * <p>Generates an INC[ident] ticket for an alarm
 * via the Service Now (SNOW) RESTful interface.</p>
 */
public class SnowRESTfulLogEntryListener implements StandardLogEntryListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(
                    SnowRESTfulLogEntryListener.class);

    private static final String CONF_FILE = "snow.conf";
    private static final String PLUGINS_DIR = "plugins";
    private static final String PRIVATE_KEY = "snow.private-key";
    private static final String PKEY_ENCRYPT = "snow.private-key.encryption";

    private static final String SNOW_HIGH = "2-HIGH";
    private static final String SNOW_MEDIUM = "3-MEDIUM";
    private static final String SNOW_LOW = "4-LOW";

    private static final String SNOW_SIGNIFICANT = "2-Significant/Large";
    private static final String SNOW_MODERATE = "3-Moderate/Limited";
    private static final String SNOW_MINOR= "4-Minor/Localized";

    private static final String DESC_FORMAT = "%-28s%s%s\n";

    private static final String PROTOCOL = "https://";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String CONTENT_HEADER = "Content-Type";
    private static final String JSON_APP_TYPE = "application/json";
    private static final String JSON_PATH = "/api/now/table/incident";

    private static String configurationItemOf(LogEntry entry) {
        return entry.getHost().toUpperCase();
    }

    /**
     * <p>Breadth-first search for matching file. (The snow listener
     * conf file should be found in one of the top-level plugin directories
     * rather than in a nested directory, so it will be faster to check those
     * rather than recurring depth-first, in case some plugins actually
     * do have sub directories).</p>
     *
     * @return first file which matches the conf file name.
     */
    private static File findConfFile(Queue<File> dirs, String name) {
        while (!dirs.isEmpty()) {
            File dir = dirs.remove();
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    dirs.add(file);
                } else if (file.getName().equals(name)) {
                    return file;
                }
            }
        }

        return null;
    }

    private static String impactOf(AlarmPriority priority) {
        switch (priority) {
            case HIGH:
                return SNOW_MODERATE;
            case CRITICAL:
                return SNOW_SIGNIFICANT;
            default:
                return SNOW_MINOR;
        }
    }

    private static String nameValueString(LogEntry entry,
                    AlarmPriority priority) {
        StringBuilder nvString = new StringBuilder();
        nvString.append(String.format(DESC_FORMAT, "First arrived", "\t\t",
                        entry.getDateOfFirstArrival()));
        nvString.append(String.format(DESC_FORMAT, "Last updated", "\t\t",
                        entry.getDateOfLastUpdate()));
        nvString.append(String.format(DESC_FORMAT, "Number of times received",
                        "\t", entry.getReceived()));
        nvString.append(String.format(DESC_FORMAT, "Type", "\t\t\t",
                        entry.getType()));
        nvString.append(String.format(DESC_FORMAT, "Priority", "\t\t\t",
                        priority));
        nvString.append(String.format(DESC_FORMAT, "Host", "\t\t\t",
                        entry.getHost()));
        nvString.append(String.format(DESC_FORMAT, "Service", "\t\t",
                        entry.getService()));
        nvString.append(String.format(DESC_FORMAT, "Domain", "\t\t",
                        entry.getDomain()));
        nvString.append(String.format(DESC_FORMAT, "Unique ID", "\t\t",
                        entry.getKey()));
        nvString.append(String.format(DESC_FORMAT, "Info", "\t\t\t",
                        entry.getInfo()));
        return nvString.toString();
    }

    private static String shortDescription(LogEntry entry) {
        StringBuilder builder = new StringBuilder("dCache Alarm: ");
        builder.append(entry.getType()).append(" ")
               .append(entry.getDomain()).append(" ")
               .append(entry.getService());
        return builder.toString();
    }

    private static String urgencyOf(AlarmPriority priority) {
        switch (priority) {
            case CRITICAL:
                return SNOW_HIGH;
            case HIGH:
                return SNOW_MEDIUM;
            default:
                return SNOW_LOW;
        }
    }

    private Map<String, AlarmPriority> priorityMap;
    private String                     pluginsDir;
    private File                       privateKey;
    private File                       confFile;
    private boolean                    isEncryptedKey;

    @Override
    public void configure(Map<String, String> configuration) {
        pluginsDir = configuration.get(PLUGINS_DIR);

        Preconditions.checkNotNull(pluginsDir,
                        "No base directory path(s) for plugins defined.");

        String name = Strings.emptyToNull(configuration.get(CONF_FILE));

        Preconditions.checkNotNull(name, "No name for snow incident "
                        + "configuration file defined.");

        Queue<File> queue = new LinkedList<>();

        /*
         *  The path property is actually a colon-separated list of possible
         *  locations for plugins.
         */
        String[] paths = pluginsDir.split("[:]");

        for (String path: paths) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                queue.add(new File(path));
            }
        }

        confFile = findConfFile(queue, name);

        Preconditions.checkNotNull(confFile, "No configuration file for "
                        + "snow incident plugin matching " + name
                        + " in the plugins directories " + pluginsDir
                        + " was found");

        privateKey = new File(configuration.get(PRIVATE_KEY));
        isEncryptedKey = Boolean.parseBoolean(configuration.get(PKEY_ENCRYPT));
    }

    @Override
    public void handleLogEntry(LogEntry entry) {
        String data = null;
        CloseableHttpClient httpclient = null;
        try {
            SnowIncident incident = newSnowIncident(entry);
            String host = Preconditions.checkNotNull(incident.getHost(),
                            "host was undefined");
            String user = Preconditions.checkNotNull(incident.getUser(),
                            "user was undefined");
            String password = PasswordManager.decrypt(incident.getPassword(),
                                                      privateKey,
                                                      isEncryptedKey);

            AuthScope authScope = new AuthScope(new HttpHost(host));
            UsernamePasswordCredentials credentials
                            = new UsernamePasswordCredentials(user, password);
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(authScope, credentials);

            httpclient = HttpClients.custom()
                                    .setDefaultCredentialsProvider(credsProvider)
                                    .build();

            HttpPost httpPost = new HttpPost(PROTOCOL + host + JSON_PATH);
            httpPost.setHeader(ACCEPT_HEADER, JSON_APP_TYPE);
            httpPost.setHeader(CONTENT_HEADER, JSON_APP_TYPE);

            data = generateDataString(incident);
            LOGGER.trace(data);

            HttpEntity entity = new ByteArrayEntity(data.getBytes(incident.getEncoding()));
            httpPost.setEntity(entity);

            LOGGER.trace("Executing request {}.", httpPost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httpPost);

            try {
                LOGGER.info(String.valueOf(response.getStatusLine()));
                LOGGER.trace(EntityUtils.toString(response.getEntity()));
            } finally {
                response.close();
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Could not encode request {}: {}.", data, e);
        } catch (IOException e) {
            LOGGER.error("Could not post request {}: {}.", data, e);
        } catch (NoSuchPaddingException
                        | NoSuchAlgorithmException
                        | InvalidKeyException
                        | IllegalBlockSizeException
                        | BadPaddingException
                        | InvalidKeySpecException
                        | InvalidAlgorithmParameterException e) {
            LOGGER.error("Could not configure user account: {}.", e);
        } finally {
            try {
                if (httpclient != null) {
                    httpclient.close();
                }
            } catch (IOException e) {
                LOGGER.trace("Trouble closing httpclient: {}.", e);
            }
        }
    }

    @Override
    public void setPriorityMap(FileBackedAlarmPriorityMap priorityMap) {
        this.priorityMap = priorityMap.getPriorityMap();
    }

    private String generateDataString(SnowIncident incident)
                    throws JsonProcessingException {
        return new ObjectMapper().writerWithDefaultPrettyPrinter()
                                 .writeValueAsString(incident);
    }

    private SnowIncident newSnowIncident(LogEntry entry) throws IOException {
        ObjectMapper mapper = new XmlMapper();
        SnowIncident incident = mapper.readValue(confFile,
                                                 SnowIncident.class);
        AlarmPriority priority = priorityMap.get(entry.getType());
        incident.setCmdb_ci(configurationItemOf(entry));
        incident.setImpact(impactOf(priority));
        incident.setUrgency(urgencyOf(priority));
        incident.setDescription(nameValueString(entry, priority));
        incident.setShort_description(shortDescription(entry));
        return incident;
    }
}
