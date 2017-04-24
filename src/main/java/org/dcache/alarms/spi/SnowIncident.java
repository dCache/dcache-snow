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

import java.io.Serializable;

/**
 * <p>Snow Incident Bean.</p>
 *
 * <p>Generated from xml configuration template.  Configured with alarm-
 * specific values by the plugin, then output as JSON.</p>
 */
public class SnowIncident implements Serializable {
    private static final long serialVersionUID = -2921782922666140850L;

    private String host;
    private String user;
    private String password;
    private String encoding;
    private String assignment_group;
    private String u_categorization;
    private String caller_id;
    private String cmdb_ci;
    private String incident_state;
    private String opened_by;
    private String u_reported_source;
    private String u_service_type;
    private String impact;
    private String urgency;
    private String short_description;
    private String description;

    public String getAssignment_group() {
        return assignment_group;
    }

    public String getCaller_id() {
        return caller_id;
    }

    public String getCmdb_ci() {
        return cmdb_ci;
    }

    public String getDescription() {
        return description;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getHost() {
        return host;
    }

    public String getImpact() {
        return impact;
    }

    public String getIncident_state() {
        return incident_state;
    }

    public String getOpened_by() {
        return opened_by;
    }

    public String getPassword() {
        return password;
    }

    public String getShort_description() {
        return short_description;
    }

    public String getU_categorization() {
        return u_categorization;
    }

    public String getU_reported_source() {
        return u_reported_source;
    }

    public String getU_service_type() {
        return u_service_type;
    }

    public String getUrgency() {
        return urgency;
    }

    public String getUser() {
        return user;
    }

    public void setAssignment_group(String assignment_group) {
        this.assignment_group = assignment_group;
    }

    public void setCaller_id(String caller_id) {
        this.caller_id = caller_id;
    }

    public void setCmdb_ci(String cmdb_ci) {
        this.cmdb_ci = cmdb_ci;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public void setIncident_state(String incident_state) {
        this.incident_state = incident_state;
    }

    public void setOpened_by(String opened_by) {
        this.opened_by = opened_by;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setShort_description(String short_description) {
        this.short_description = short_description;
    }

    public void setU_categorization(String u_categorization) {
        this.u_categorization = u_categorization;
    }

    public void setU_reported_source(String u_reported_source) {
        this.u_reported_source = u_reported_source;
    }

    public void setU_service_type(String u_service_type) {
        this.u_service_type = u_service_type;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
