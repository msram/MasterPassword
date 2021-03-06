//==============================================================================
// This file is part of Master Password.
// Copyright (c) 2011-2017, Maarten Billemont.
//
// Master Password is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Master Password is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You can find a copy of the GNU General Public License in the
// LICENSE file.  Alternatively, see <http://www.gnu.org/licenses/>.
//==============================================================================

package com.lyndir.masterpassword;

import static org.testng.Assert.*;

import com.google.common.base.Charsets;
import com.lyndir.lhunath.opal.system.CodeUtils;
import com.lyndir.lhunath.opal.system.logging.Logger;
import java.util.Random;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class MPMasterKeyTest {

    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = Logger.get( MPMasterKeyTest.class );

    private MPTestSuite testSuite;

    @BeforeMethod
    public void setUp()
            throws Exception {

        testSuite = new MPTestSuite();
    }

    @Test
    public void testMasterKey()
            throws Exception {

        testSuite.forEach( "testMasterKey", new MPTestSuite.TestCase() {
            @Override
            public boolean run(final MPTests.Case testCase)
                    throws Exception {
                char[]      masterPassword = testCase.getMasterPassword().toCharArray();
                MPMasterKey masterKey      = new MPMasterKey( testCase.getFullName(), masterPassword );

                // Test key
                assertEquals(
                        CodeUtils.encodeHex( masterKey.getKeyID( testCase.getAlgorithm() ) ),
                        testCase.getKeyID(),
                        "[testMasterKey] keyID mismatch: " + testCase );

                // Test invalidation
                masterKey.invalidate();
                try {
                    masterKey.getKeyID( testCase.getAlgorithm() );
                    fail( "[testMasterKey] invalidate ineffective: " + testCase );
                }
                catch (final MPInvalidatedException ignored) {
                }
                assertNotEquals(
                        masterPassword,
                        testCase.getMasterPassword().toCharArray(),
                        "[testMasterKey] masterPassword not wiped: " + testCase );

                return true;
            }
        } );
    }

    @Test
    public void testSiteResult()
            throws Exception {

        testSuite.forEach( "testSiteResult", new MPTestSuite.TestCase() {
            @Override
            public boolean run(final MPTests.Case testCase)
                    throws Exception {
                char[]      masterPassword = testCase.getMasterPassword().toCharArray();
                MPMasterKey masterKey      = new MPMasterKey( testCase.getFullName(), masterPassword );

                // Test site result
                assertEquals(
                        masterKey.siteResult( testCase.getSiteName(), testCase.getSiteCounter(), testCase.getKeyPurpose(),
                                              testCase.getKeyContext(), testCase.getResultType(),
                                              null, testCase.getAlgorithm() ),
                        testCase.getResult(),
                        "[testSiteResult] result mismatch: " + testCase );

                return true;
            }
        } );
    }

    @Test
    public void testSiteState()
            throws Exception {

        MPTests.Case testCase       = testSuite.getTests().getDefaultCase();
        char[]       masterPassword = testCase.getMasterPassword().toCharArray();
        MPMasterKey  masterKey      = new MPMasterKey( testCase.getFullName(), masterPassword );

        String password = randomString( 8 );
        for (final MPMasterKey.Version version : MPMasterKey.Version.values()) {
            MPResultType resultType = MPResultType.StoredPersonal;

            // Test site state
            String state = masterKey.siteState( testCase.getSiteName(), testCase.getSiteCounter(), testCase.getKeyPurpose(),
                                                testCase.getKeyContext(), resultType, password, version );
            String result = masterKey.siteResult( testCase.getSiteName(), testCase.getSiteCounter(), testCase.getKeyPurpose(),
                                                  testCase.getKeyContext(), resultType, state, version );

            assertEquals(
                    result,
                    password,
                    "[testSiteState] state mismatch: " + testCase );
        }
    }

    public static String randomString(int length) {
        Random        random  = new Random();
        StringBuilder builder = new StringBuilder();

        while (length > 0) {
            int codePoint = random.nextInt( Character.MAX_CODE_POINT - Character.MIN_CODE_POINT ) + Character.MIN_CODE_POINT;
            if (!Character.isDefined( codePoint ) || (Character.getType( codePoint ) == Character.PRIVATE_USE) || Character.isSurrogate(
                    (char) codePoint ))
                continue;

            builder.appendCodePoint( codePoint );
            length--;
        }

        return builder.toString();
    }
}
