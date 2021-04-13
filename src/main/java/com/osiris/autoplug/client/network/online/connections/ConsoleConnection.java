/*
 * Copyright Osiris Team
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * AutoPlug License.  Please consult the file "LICENSE" for details.
 */

package com.osiris.autoplug.client.network.online.connections;

import com.osiris.autoplug.client.network.online.SecondaryConnection;
import com.osiris.autoplug.core.logger.AL;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import static com.osiris.autoplug.client.utils.GD.MC_SERVER_IN;

/**
 * Read the InputStreams of AutoPlug and the Minecraft server and
 * send it to the AutoPlug server when the user is online.
 */
public class ConsoleConnection extends SecondaryConnection {
    private BufferedWriter bw;
    private Thread thread2;

    public ConsoleConnection() {
        super((byte) 2);  // Each connection has its own auth_id.
    }

    @Override
    public boolean open() throws Exception {
        if (super.open()){
            try{
                if (bw==null){
                    Socket socket = getSocket();
                    socket.setSoTimeout(0);
                    bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                }
            } catch (Exception e) {
                AL.warn(e);
            }

            if (thread2==null){
                thread2 = new Thread(()->{
                    try{
                        if (MC_SERVER_IN!=null){
                            byte counter = 0;
                            InputStreamReader isr = new InputStreamReader(MC_SERVER_IN);
                            BufferedReader br = new BufferedReader(isr);
                            while(true)
                                try {
                                    send(br.readLine());
                                } catch (Exception e) {
                                    counter++;
                                    if (counter<3)
                                        AL.warn("Failed to send message to online console!", e);
                                }
                        }
                        else throw new Exception("Failed to get mc-server InputStream, because its null!");
                    } catch (Exception e) {
                        AL.warn(e);
                    }


                });
                thread2.setName("MinecraftServer-InputStreamReader-Thread");
                thread2.start();
            }
            return true;
        }
        else
            return false;
    }

    public synchronized void send(String message) throws Exception{
        bw.write(message);
        bw.flush();
        AL.info("SENT LINE: "+message);
    }
}