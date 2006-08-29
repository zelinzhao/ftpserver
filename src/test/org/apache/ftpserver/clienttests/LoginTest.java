/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */  

package org.apache.ftpserver.clienttests;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

public class LoginTest extends ClientTestTemplate {
    private static final String TESTUSER_PASSWORD = "password";

    public void testLogin() throws Exception {
        assertTrue(client.login("admin", "admin"));
    }

    public void testLoginWithAccount() throws Exception {
        assertTrue(client.login("admin", "admin", "foo"));
    }
    
    public void testLoginIncorrectPassword() throws Exception {
        assertFalse(client.login("admin", "foo"));
    }

    public void testReLogin() throws Exception {
        assertFalse(client.login("admin", "foo"));
        assertTrue(client.login("admin", "admin"));
    }

    public void testReLoginWithOnlyPass() throws Exception {
        assertFalse(client.login("admin", "foo"));
        
        int reply = client.pass("admin");
        assertTrue(FTPReply.isNegativePermanent(reply));
    }

    public void testLoginUnknownUser() throws Exception {
        assertFalse(client.login("foo", "foo"));
    }
    
    private String[] getHostAddresses() throws Exception {
        Enumeration nifs = NetworkInterface.getNetworkInterfaces();
        
        List hostIps = new ArrayList();
        while (nifs.hasMoreElements()) {
            NetworkInterface nif = (NetworkInterface) nifs.nextElement();
            Enumeration ips = nif.getInetAddresses();
            
            while (ips.hasMoreElements()) {
                InetAddress ip = (InetAddress) ips.nextElement();
                hostIps.add(ip.getHostAddress());
            }
        }
        
        return (String[]) hostIps.toArray(new String[0]);
    }

    public void testLoginWithMaxConnectionsPerIp() throws Exception {
        String[] ips = getHostAddresses();
        
        if(ips.length > 1) {
            FTPClient client2 = new FTPClient();
            client2.connect(ips[0], port);
            FTPClient client3 = new FTPClient();
            client3.connect(ips[0], port);
            FTPClient client4 = new FTPClient();
            client4.connect(ips[1], port);
            FTPClient client5 = new FTPClient();
            client5.connect(ips[1], port);
            FTPClient client6 = new FTPClient();
            client6.connect(ips[1], port);

            assertTrue(client2.login("testuser2", TESTUSER_PASSWORD));
            assertTrue(client3.login("testuser2", TESTUSER_PASSWORD));
            assertTrue(client4.login("testuser2", TESTUSER_PASSWORD));
            assertTrue(client5.login("testuser2", TESTUSER_PASSWORD));

            try{
                assertTrue(client6.login("testuser2", TESTUSER_PASSWORD));
                fail("Must throw FTPConnectionClosedException");
            } catch(FTPConnectionClosedException e) {
                // expected
            }
        } else {
            // ignore test
        }
    }
    
    public void testLoginWithMaxConnectionsMulti() throws Exception {
        for(int i = 0; i<50; i++) {
            testLoginWithMaxConnections();
        }
    }
    
    public void testLoginWithMaxConnections() throws Exception {
        FTPClient client1 = new FTPClient();
        FTPClient client2 = new FTPClient();
        FTPClient client3 = new FTPClient();
        FTPClient client4 = new FTPClient();
        
        try{
            client1.connect("localhost", port);
            client2.connect("localhost", port);
            client3.connect("localhost", port);
            client4.connect("localhost", port);
            
            assertTrue(client1.login("testuser1", TESTUSER_PASSWORD));
            assertTrue(client2.login("testuser1", TESTUSER_PASSWORD));
            assertTrue(client3.login("testuser1", TESTUSER_PASSWORD));
            
            try{
                assertTrue(client4.login("testuser1", TESTUSER_PASSWORD));
                fail("Must throw FTPConnectionClosedException");
            } catch(FTPConnectionClosedException e) {
                // expected
            }
        } finally {
            closeQuitely(client1);
            closeQuitely(client2);
            closeQuitely(client3);
            closeQuitely(client4);
        }
    }
    
    private void closeQuitely(FTPClient client) {
        try{
            client.logout();
        } catch(Exception e) {
            // ignore
        }
        try{
            client.disconnect();
        } catch(Exception e) {
            // ignore
        }
        
    }
    
}