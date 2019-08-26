package com.gmail.afonsotrepa.pocketgopher.gopherclient;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection
{
    private Socket socket;
    private PrintWriter os; //output stream
    private BufferedReader is; //input stream

    /**
     * Opens a connection with the server
     *
     * @param server ip or DNS address of the server
     * @param port   port the server and the client listen to
     */
    public Connection(String server, Integer port) throws IOException
    {
        socket = new Socket(server, port);

        os = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
        is = new BufferedReader((new InputStreamReader((socket.getInputStream()))));
    }

    /**
     * Sends/writes a string to the server
     *
     * @param message string to send to the server
     */
    private void write(String message)
    {
        os.write(message + "\r\n");
        os.flush();
    }

    /**
     * Receives/reads from the server
     *
     * @return a message sent by the server
     */
    private String read()
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            //read until the end of the message (EOF or ".")
            String line;
            while ((line = is.readLine()) != null && ! line.equals("."))
            {
                sb.append(line).append('\n');
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return sb.toString();
    }


    /**
     * Sends the selector to the server and returns the response (expects a directory/menu)
     *
     * @param selector selector (see RFC 1436)
     *
     * @return the response from the server (as Page objects)
     */
    public SpannableString getMenu(String selector, Context context)
    {
        this.write(selector); //send the selector
        String[] lines = this.read().split("\n"); //read the response by the server

        SpannableStringBuilder spannableStringContent = new SpannableStringBuilder("");
        for (String line : lines)
        {
            Page newPage;
            //skip empty line
            if (line.equals(""))
            {
                continue;
            }

            String[] lineSplit = line.split("\t");
            if (lineSplit.length < 2)
            {
                newPage = Page.makePage(
                        line.charAt(0), //type
                        "",
                        "",
                        0,
                        lineSplit[0].substring(1)); //remove the type tag

            }
            else if (lineSplit.length < 4)
            {
                newPage = new UnknownPage(line);
            }
            else
            {
                try
                {
                    int port = Integer.parseInt(lineSplit[3]);

                    newPage = Page.makePage(
                            line.charAt(0), //type
                            lineSplit[1],
                            lineSplit[2],
                            port,
                            lineSplit[0].substring(1) //remove the type tag
                    );
                }
                catch (Exception ex)
                {
                    newPage = new UnknownPage(line);
                }
            }

            SpannableString pageLine = newPage.render(context, newPage.line);
            spannableStringContent.append(pageLine);
        }

        return SpannableString.valueOf(spannableStringContent);
    }

    /**
     * Sends the selector to the server and returns the response (expects text)
     *
     * @param selector selector (see RFC 1436)
     *
     * @return the response from the server (as strings)
     */
    public String getText(String selector)
    {
        this.write(selector); //send the selector
        return this.read();
    }

    /**
     * Sends the selector to the server and writes its response to the file
     *
     * @param selector selector (see RFC 1436)
     * @param file     file to store the response from the server
     */
    public void getBinary(String selector, File file)
    {
        this.write(selector); //send the selector

        try
        {
            InputStream is = this.socket.getInputStream();
            FileOutputStream os = new FileOutputStream(file);

            int read;
            byte[] buf = new byte[4096]; //pretty small rn

            while ((read = is.read(buf)) != - 1)
            {
                os.write(buf, 0, read);
            }

        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            os.flush(); //flush the buffer
            os.close(); //close the stream
        }
    }
}
