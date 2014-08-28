/*
 *   Copyright 2008, Maarten Billemont
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */


package com.lyndir.masterpassword;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.lyndir.lhunath.opal.system.CodeUtils;
import com.lyndir.lhunath.opal.system.MessageDigests;
import com.lyndir.lhunath.opal.system.logging.Logger;
import com.lyndir.lhunath.opal.system.util.TypeUtils;
import java.io.*;
import java.net.URI;
import java.net.URL;
import javax.swing.*;


/**
 * <p> <i>Jun 10, 2008</i> </p>
 *
 * @author mbillemo
 */
public class GUI implements UnlockFrame.SignInCallback {

    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = Logger.get( GUI.class );

    private UnlockFrame unlockFrame = new UnlockFrame( this );
    private PasswordFrame passwordFrame;

    public static void main(final String[] args)
            throws IOException {

        try {
            byte[] manifest = ByteStreams.toByteArray( GUI.class.getClassLoader().getResourceAsStream( "META-INF/MANIFEST.MF" ) );
            String manifestHash = CodeUtils.encodeHex( CodeUtils.digest( MessageDigests.SHA1, manifest ) );
            InputStream upstream = URI.create( "http://masterpasswordapp.com/masterpassword-gui.jar.mf.sha1" ).toURL().openStream();
            String upstreamHash = CharStreams.toString( new InputStreamReader( upstream, Charsets.UTF_8 ) );
            logger.inf( "Local Manifest Hash:    %s", manifestHash );
            logger.inf( "Upstream Manifest Hash: %s", upstreamHash );
            if (!manifestHash.equalsIgnoreCase( upstreamHash )) {
                logger.wrn( "You are not running the current official version.  Please update from:\n"
                            + "http://masterpasswordapp.com/masterpassword-gui.jar" );
                JOptionPane.showMessageDialog( null, "A new version of Master Password is available.\n"
                                                     + "Please download the latest version from http://masterpasswordapp.com",
                                               "Update Available", JOptionPane.WARNING_MESSAGE );
            }
        }
        catch (IOException e) {
            logger.wrn( e, "Couldn't check for version update." );
        }

        GUI gui;
        try {
            gui = TypeUtils.newInstance( AppleGUI.class );
        }
        catch (RuntimeException e) {
            gui = new GUI();
        }
        gui.open();
    }

    void open() {
        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                if (passwordFrame == null) {
                    unlockFrame.setVisible( true );
                } else {
                    passwordFrame.setVisible( true );
                }
            }
        } );
    }

    @Override
    public boolean signedIn(final User user) {
        if (!user.hasKey()) {
            return false;
        }
        user.getKey();

        passwordFrame = newPasswordFrame( user );

        open();
        return true;
    }

    protected PasswordFrame newPasswordFrame(final User user) {
        PasswordFrame frame = new PasswordFrame( user );
        frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );

        return frame;
    }
}
