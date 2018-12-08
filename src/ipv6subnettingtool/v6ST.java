/*
 * Copyright (c) 2010-2019, Yucel Guven
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package ipv6subnettingtool;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;

/**
 * This class provides methods for the IPv6 Subnetting Tool.
 * <br>
 * Member methods are used for IPv6 Address Checking, Formalizing, Finding
 * subnet start/end and end/start addresses, Subnetting, Inserting Colons
 * (Kolonlar).
 *
 * @author (c) Yucel Guven
 */
public final class v6ST {

    /**
     * Default constructor for v6SubnettingTool.
     */
    public v6ST() {
    }
    /**
     * These two variables is used to indicate AS number conversion type.
     * <br>
     * <b>toASplain</b>: Input will be converted from as-dot to as-plain notation.
     * Pass it like 'v6ST.toASplain' or Boolean.TRUE
     * <br><br>
     * <b>toASdot</b>: Input will be converted from as-plain to as-dot notation.
     * Pass it like 'v6ST.toASdot' or Boolean.FALSE
     */
    public static final Boolean toASplain = Boolean.TRUE;
    /**
     * These two variables is used to indicate AS number conversion type.
     * <br>
     * <b>toASplain</b>: Input will be converted from as-dot to as-plain notation.
     * Pass it like 'v6ST.toASplain' or Boolean.TRUE
     * <br><br>
     * <b>toASdot</b>: Input will be converted from as-plain to as-dot notation.
     * Pass it like 'v6ST.toASdot' or Boolean.FALSE
     */
    public static final Boolean toASdot = Boolean.FALSE;
    
    private static final long asMax = 4294967295L;
    /**
     * errmsg variable contains a message about the last status.
     */
    public static String errmsg = "";
    public static String arpa = "ip6.arpa.";
    private static BigInteger mask = BigInteger.ZERO;

    /**
     * This function is used to check the entered IPv6 address.
     * <br>
     * A boolean value true or false is returned.
     *
     * @param sin String input
     * @return boolean (true or false)
     */
    public static boolean IsAddressCorrect(String sin) {
        int nDC = 0;
        int nC = 0;

        sin = sin.trim();
        String s = sin;
        char[] chars = s.toCharArray();

        /* 0. Error: Empty */
        if (s.isEmpty()) {
            errmsg = "> Empty address";
            return false;
        }

        /* 1. Error: UNDEFINED '::' */
        if (s.equals("::")) {
            errmsg = "> Undefined";
            return false;
        }
        /* 2. Error: Triple or more colons entered */
        if ((s.length() <= 1) || (s.contains(":::"))) {
            errmsg = "> Not valid";
            return false;
        }
        /* 3. Error: Not valid hex */
        if (!Pattern.matches("^[0-9A-Fa-f:]+$", s)) {
            errmsg = "> Not valid hex-digit";
            return false;
        }
        /* 4. Error: Cannot start or end with ':' */
        if (chars[0] == ':' && chars[1] != ':') {
            errmsg = "> Not valid: ':'";
            return false;
        }
        if (chars[s.length() - 1] == ':' && chars[s.length() - 2] != ':') {
            errmsg = "> Not valid: ':'";
            return false;
        }
        /* 5. Error: More than 2 Bytes */
        String[] sa = s.split(":", -1);
        for (int j = 0; j < sa.length; j++) {
            if (sa[j].length() > 4) {
                errmsg = "> More than 2 bytes";
                return false;
            }
        }
        /* 6. Error: Number of DoubleColons and Colons */
        s = sin;
        nDC = s.split("::", -1).length - 1;
        s = s.replace("::", "**");
        nC = s.split(":", -1).length - 1;

        /* Case I. DoubleColon can only appear once - RFC4291 */
        if (nDC > 1) {
            errmsg = "> DoubleColon can only appear once";
            return false;
        }
        /* Case II. No DoubleColons means there must be 7 colons */
        if (nDC == 0 && nC != 7) {
            errmsg = "> Not valid, there must be 7 colons";
            return false;
        }
        /* Case III. If DoubleColon at start/end, max. colons must be 6 or less */
        s = sin;
        int sL = s.length();
        if ((chars[0] == ':' && chars[1] == ':')
                || (chars[sL - 1] == ':' && chars[sL - 2] == ':')) {
            if (nDC == 1 && nC > 6) {
                errmsg = "> Excessive colons";
                return false;
            }
        } /* Case IV. If DoubleColon in middle, max. colons must be 5 or less
         */ else if (nDC == 1 && nC > 5) {
            errmsg = "> Excessive colons";
            return false;
        }

        /* End of Check */
        errmsg = "> ok";
        return true;
    }

    /**
     * This function is used to formalize the entered IPv6 address into
     * BigInteger representing 16 bytes formal IPv6 address.
     * <br>
     *
     * Input is a string IPv6 address and returned value is the BigInteger of
     * the entered IPv6 address.
     *
     * @param sin String input
     * @return BigInteger
     */
    public static BigInteger FormalizeAddr(String sin) {

        String[] Resv6 = new String[]{"0000", "0000", "0000", "0000",
            "0000", "0000", "0000", "0000"};
        BigInteger Finalv6 = BigInteger.ZERO;

        sin = sin.trim();

        String s = sin;
        String[] sa = s.split(":", -1);

        int nC = 0;
        s = s.replace("::", "**");
        nC = s.split(":", -1).length - 1;

        /* Start of Building Result v6 address */
        for (int k = 0; k < sa.length; k++) {
            if (sa[k].length() == 0) {
                continue;
            } else {
                sa[k] = String.format("%4s", sa[k]).replace(' ', '0');
            }
        }

        if ((sa[sa.length - 1].length() == 0)
                && (sa[sa.length - 2].length() == 0)) {
            int t = nC + 1;

            for (int i = 0; i < t; i++) {
                Resv6[i] = sa[i];
            }
        } else if (sa[0].length() == 0 && sa[1].length() == 0) {
            int t = nC + 1;
            for (int i = 0; i < t; i++) {
                Resv6[7 - i] = sa[sa.length - 1 - i];
            }
        } else {
            int idx = Arrays.asList(sa).indexOf("");

            for (int i = 0; i < idx; i++) {
                Resv6[i] = sa[i];
            }
            for (int i = 0; i < sa.length - idx - 1; i++) {
                Resv6[7 - i] = sa[sa.length - 1 - i];
            }
        }
        /* End of Building Resultant IPv6 address */

        s = "";
        for (int i = 0; i < 8; i++) {
            s += Resv6[i];
        }

        Finalv6 = new BigInteger(s, 16);

        return Finalv6;
    }

    /**
     * This function is used to obtain the Starting and End subnet addresses of
     * an entered IPv6 address.
     * <br>
     * SEaddress object should be the input. Starting and End subnet addresses
     * are inside the returned SEaddress object.
     *
     * @param input SEaddress
     * @param is128Checked
     * @return SEaddress
     */
    public static SEaddress StartEndAddresses(SEaddress input, Boolean is128Checked) {

        input.subnetidx = BigInteger.ZERO;
        int delta = input.subnetslash - input.slash;

        int count = 0;
        if (!is128Checked) {
            count = 63;
        } else {
            count = 127;
        }

        for (int i = (count - input.slash); i > (count - input.subnetslash); i--) {
            if (input.Start.testBit(i) == true) {
                input.subnetidx = input.subnetidx.setBit(delta);
            } else {
                input.subnetidx = input.subnetidx.clearBit(delta);
            }
            delta--;
        }

        if (!is128Checked) {
            if (input.slash == 64) {
                input.Start = input.End = input.Resultv6;
                return input;
            }
        } else if (is128Checked) {
            if (input.slash == 128) {
                input.Start = input.End = input.Resultv6;
                return input;
            }
        }
        mask = InitializeMask();
        mask = mask.shiftRight(input.slash);
        input.End = mask.or(input.Resultv6);
        mask = mask.not();
        input.Start = mask.and(input.Resultv6);

        input.LowerLimitAddress = input.Start;
        input.UpperLimitAddress = input.End;

        return input;
    }

    /**
     * This function is used to obtain the Starting subnet address from a given
     * End subnet address.
     * <br>
     * Returned SEaddress object will contain the values.
     *
     * @param input
     * @return SEaddress
     */
    public static SEaddress EndStartAddresses(SEaddress input) {

        input.Start = BigInteger.ZERO;

        mask = InitializeMask();

        mask = mask.shiftRight(input.subnetslash);
        mask = mask.not();
        input.Start = mask.and(input.End);

        input.subnetidx = BigInteger.ZERO;
        int delta = input.subnetslash - input.slash - 1;

        for (int i = (127 - input.slash); i > (127 - input.subnetslash); i--) {
            if (input.End.testBit(i) == true) {
                input.subnetidx = input.subnetidx.setBit(delta);
            } else {
                input.subnetidx = input.subnetidx.clearBit(delta);
            }
            delta--;
        }

        return input;
    }

    /**
     * This function returns the End subnet address of a given IPv6 address.
     *
     * @param input SEaddress
     * @param is128Checked
     * @return SEaddress
     */
    public static SEaddress Subnetting(SEaddress input, Boolean is128Checked) {

        input.subnetidx = BigInteger.ZERO;
        int delta = input.subnetslash - input.slash - 1;
        int count = 127;

        for (int i = (count - input.slash); i > (count - input.subnetslash); i--) {
            if (input.Start.testBit(i) == true) {
                input.subnetidx = input.subnetidx.setBit(delta);
            } else {
                input.subnetidx = input.subnetidx.clearBit(delta);
            }
            delta--;
        }

        if (!is128Checked) {
            if (input.slash == 64) {
                input.End = input.Start;
                return input;
            }
        } else if (is128Checked) {
            if (input.subnetslash == 128) {
                input.End = input.Start;
                return input;
            }
        }

        mask = InitializeMask();
        mask = mask.shiftRight(input.subnetslash);
        input.End = mask.or(input.Start);

        return input;
    }

    /**
     * This function inserts colons into given BigInteger IPv6 address
     * <br>
     * and returns a string of formal 16 Bytes IPv6 address without zero
     * compression.
     * <br>
     * Kolonlar means Colons.
     *
     * @param bigint
     * @return String
     */
    public static String Kolonlar(BigInteger bigint) {

        String str = "";
        String stmp = bigint.toString(16);

        stmp = String.format("%32s", stmp).replace(' ', '0');

        if (stmp.equals("0")) {
            stmp = "00000000000000000000000000000000";
        }

        for (int i = 0; i < 32; i++) {
            if (i % 4 == 0) {
                str += ":";
            }
            str += stmp.substring(i, i + 1);
        }
        str = str.substring(1);

        return str;
    }

    public static BigInteger InitializeMask() {
        mask = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
        return mask;
    }

    public static String PrintBin(SEaddress StartEnd, int border, Boolean is128Checked) {
        BigInteger n = StartEnd.Start;

        String sOut = "";
        int count = 0;

        if (!is128Checked) {
            count = 61;

            if (border == 64) {
                n = StartEnd.Resultv6;
            }

            if (n.equals(BigInteger.ZERO)) {
                sOut = "0000 0000 0000 0000:0000 0000 0000 0000:"
                        + "0000 0000 0000 0000:0000 0000 0000 0000";
                return sOut;
            }
            for (int bitIndex = 127; bitIndex >= 64; bitIndex--) {
                sOut += ((n.shiftRight(bitIndex)).and(BigInteger.ONE)).equals(BigInteger.ZERO) ? "0" : "1";
                if (bitIndex % 4 == 0) {
                    sOut += " ";
                }
            }
        }

        if (is128Checked) {
            count = 141;

            if (border == 128) {
                n = StartEnd.Resultv6;
            }

            if (n.equals(BigInteger.ZERO)) {
                sOut = "0000 0000 0000 0000:0000 0000 0000 0000:"
                        + "0000 0000 0000 0000:0000 0000 0000 0000:"
                        + "0000 0000 0000 0000:0000 0000 0000 0000:"
                        + "0000 0000 0000 0000:0000 0000 0000 0000";

                return sOut;
            }
            for (int bitIndex = 127; bitIndex >= 0; bitIndex--) {
                sOut += ((n.shiftRight(bitIndex)).and(BigInteger.ONE)).equals(BigInteger.ZERO) ? "0" : "1";
                if (bitIndex % 4 == 0) {
                    sOut += " ";
                }
            }
        }
        // and add colons:
        StringBuilder buf = new StringBuilder(String.valueOf(sOut));
        for (int pos = 0; pos < count; pos++) {
            if (pos % 20 == 0 && pos > 0) {
                buf = buf.replace(pos - 1, pos, ":");
            }
        }

        return buf.toString();
    }

    public static SEaddress ListFirstPage(SEaddress input, Boolean is128Checked, Boolean isEndChecked) {

        SEaddress subnets = new SEaddress();
        subnets.Start = input.Start;
        subnets.slash = input.slash;
        subnets.subnetslash = input.subnetslash;

        subnets.liste.clear();
        subnets.End = BigInteger.ZERO;
        subnets.subnetidx = BigInteger.ZERO;

        String ss = "", se = "";
        int count = 0;

        if (input.slash == input.subnetslash) {
            input.upto = 1;
        }

        if (!is128Checked && input.slash == 64) {
            subnets.Start = subnets.End = input.Start = input.End = input.Resultv6;
            ss = Kolonlar(subnets.Start);
            ss = CompressAddress(ss);
            ss = "p" + subnets.subnetidx + "> " + ss + "/" + input.slash;

            subnets.liste.add(ss);
            return subnets;
        } else if (is128Checked && input.slash == 128) {
            subnets.Start = subnets.End = input.Start = input.End = input.Resultv6;
            ss = Kolonlar(subnets.Start);
            ss = CompressAddress(ss);
            ss = "p" + subnets.subnetidx + "> " + ss + "/" + input.slash;

            subnets.liste.add(ss);
            return subnets;
        }
        for (count = 0; count < input.upto; count++) {
            subnets = Subnetting(subnets, is128Checked);

            if (!is128Checked) {
                ss = Kolonlar(subnets.Start);
                ss = CompressAddress(ss);
                ss = "p" + subnets.subnetidx + "> " + ss + "/" + input.subnetslash;

                if (input.subnetslash == 64) {
                    subnets.liste.add(ss);
                } else {
                    subnets.liste.add(ss);
                    
                    if (isEndChecked) {
                        se = Kolonlar(subnets.End);
                        se = se.substring(0, 19) + "::";
                        se = CompressAddress(se);
                        se = "e" + subnets.subnetidx + "> " + se + "/" + input.subnetslash;
                        subnets.liste.add(se);
                        subnets.liste.add("");
                    }
                }
            } else if (is128Checked) {
                ss = Kolonlar(subnets.Start);
                ss = CompressAddress(ss);
                ss = "p" + subnets.subnetidx + "> " + ss + "/" + input.subnetslash;

                if (input.subnetslash == 128) {
                    subnets.liste.add(ss);
                } else {
                    subnets.liste.add(ss);
                    
                    if (isEndChecked) {
                        se = Kolonlar(subnets.End);
                        se = CompressAddress(se);
                        se = "e" + subnets.subnetidx + "> " + se + "/" + input.subnetslash;
                        subnets.liste.add(se);
                        subnets.liste.add("");
                    }
                }
            }

            if (subnets.End.equals(input.UpperLimitAddress)) {
                break;
            } else {
                subnets.Start = subnets.End.add(BigInteger.ONE);
            }
        }

        return subnets;
    }

    public static SEaddress ListPageBackward(SEaddress input, Boolean is128Checked, Boolean isEndChecked) {
        SEaddress subnets = new SEaddress();
        subnets = input;

        subnets.liste.clear();

        String ss = "", se = "";
        int count = 0;

        for (count = 0; count < input.upto; count++) {
            subnets = EndStartAddresses(subnets, is128Checked);

            if (!is128Checked) {
                ss = Kolonlar(subnets.Start);
                ss = CompressAddress(ss);
                ss = "p" + subnets.subnetidx + "> " + ss + "/"
                        + input.subnetslash;

                if (input.subnetslash == 64) {
                    subnets.liste.add(ss);
                } else {
                    if (isEndChecked) {
                        se = Kolonlar(subnets.End);
                        se = se.substring(0, 19) + "::";
                        se = CompressAddress(se);
                        se = "e" + subnets.subnetidx + "> " + se + "/"
                                + input.subnetslash;
                        subnets.liste.add("");
                        subnets.liste.add(se);
                    }
                    subnets.liste.add(ss);
                }

            } else if (is128Checked) {
                ss = Kolonlar(subnets.Start);
                ss = CompressAddress(ss);
                ss = "p" + subnets.subnetidx + "> " + ss + "/"
                        + input.subnetslash;

                if (input.subnetslash == 128) {
                    subnets.liste.add(ss);
                } else {
                    if (isEndChecked) {
                        se = Kolonlar(subnets.End);
                        se = CompressAddress(se);
                        se = "e" + subnets.subnetidx + "> " + se + "/"
                                + input.subnetslash;
                        subnets.liste.add("");
                        subnets.liste.add(se);
                    }
                    subnets.liste.add(ss);
                }
            }
            subnets.End = subnets.Start.subtract(BigInteger.ONE);

            if (subnets.Start.equals(input.LowerLimitAddress)) {
                break;
            }
        }
        javafx.collections.FXCollections.reverse(subnets.liste);
        return subnets;
    }

    public static SEaddress ListPageForward(SEaddress input, Boolean is128Checked, Boolean isEndChecked) {
        SEaddress subnets = new SEaddress();
        subnets = input;

        subnets.liste.clear();
        String ss, se;
        int count = 0;

        for (count = 0; count < input.upto; count++) {
            subnets = Subnetting(subnets, is128Checked);

            if (!is128Checked) {
                ss = Kolonlar(subnets.Start);
                ss = CompressAddress(ss);
                ss = "p" + subnets.subnetidx + "> " + ss + "/"
                        + input.subnetslash;

                if (input.subnetslash == 64) {
                    subnets.liste.add(ss);
                } else {
                    subnets.liste.add(ss);
                    if (isEndChecked) {
                        se = Kolonlar(subnets.End);
                        se = se.substring(0, 19) + "::";
                        se = CompressAddress(se);
                        se = "e" + subnets.subnetidx + "> " + se + "/"
                                + input.subnetslash;
                        subnets.liste.add(se);
                        subnets.liste.add("");
                    }
                }
            } else if (is128Checked) {
                ss = Kolonlar(subnets.Start);
                ss = CompressAddress(ss);
                ss = "p" + subnets.subnetidx + "> " + ss + "/"
                        + input.subnetslash;

                if (input.subnetslash == 128) {
                    subnets.liste.add(ss);
                } else {
                    subnets.liste.add(ss);
                    if (isEndChecked) {
                        se = Kolonlar(subnets.End);
                        se = CompressAddress(se);
                        se = "e" + subnets.subnetidx + "> " + se + "/"
                                + input.subnetslash;
                        subnets.liste.add(se);
                        subnets.liste.add("");                        
                    }
                }
            }

            if (subnets.End.equals(input.UpperLimitAddress)
                    || subnets.subnetidx == BigInteger.ZERO) {
                break;
            }
            subnets.Start = subnets.End.add(BigInteger.ONE);
        }

        return subnets;
    }

    public static SEaddress ListLastPage(SEaddress input, Boolean is128Checked, Boolean isEndChecked) {

        SEaddress subnets = new SEaddress();
        subnets = input;

        subnets.liste.clear();

        String ss = "", se = "";
        int count = 0;

        subnets.subnetidx = BigInteger.ZERO;

        for (count = 0; count < input.upto; count++) {
            subnets = EndStartAddresses(subnets, is128Checked);

            if (!is128Checked) {
                ss = Kolonlar(subnets.Start);
                ss = CompressAddress(ss);
                ss = "p" + subnets.subnetidx + "> " + ss + "/"
                        + input.subnetslash;

                if (input.subnetslash == 64) {
                    subnets.liste.add(ss);
                } else {
                    if (isEndChecked) {
                        se = Kolonlar(subnets.End);
                        se = se.substring(0, 19) + "::";
                        se = CompressAddress(se);
                        se = "e" + subnets.subnetidx + "> " + se + "/"
                                + input.subnetslash;
                        subnets.liste.add("");
                        subnets.liste.add(se);                        
                    }
                    subnets.liste.add(ss);
                }
            } else if (is128Checked) {
                ss = Kolonlar(subnets.Start);
                ss = CompressAddress(ss);
                ss = "p" + subnets.subnetidx + "> " + ss + "/"
                        + input.subnetslash;

                if (input.subnetslash == 128) {
                    subnets.liste.add(ss);
                } else {
                    if (isEndChecked) {
                        se = Kolonlar(subnets.End);
                        se = CompressAddress(se);
                        se = "e" + subnets.subnetidx + "> " + se + "/"
                                + input.subnetslash;
                        subnets.liste.add("");
                        subnets.liste.add(se);                        
                    }
                    subnets.liste.add(ss);
                }
            }
            subnets.End = subnets.Start.subtract(BigInteger.ONE);
            if (subnets.subnetidx == BigInteger.ZERO) {
                break;
            }
        }
        javafx.collections.FXCollections.reverse(subnets.liste);

        return subnets;
    }

    public static SEaddress EndStartAddresses(SEaddress input, Boolean is128Checked) {

        input.subnetidx = BigInteger.ZERO;
        int delta = input.subnetslash - input.slash - 1;
        
        int count = 127;
        
        for (int i = (count - input.slash); i > (count - input.subnetslash); i--) {
            if (input.End.testBit(i)) {
                input.subnetidx = input.subnetidx.setBit(delta);
            } else {
                input.subnetidx = input.subnetidx.clearBit(delta);
            }
            delta--;
        }

        if (!is128Checked) {
            if (input.slash == 64) {
                input.Start = input.End;
                return input;
            }
        } else if (is128Checked) {
            if (input.slash == 128) {
                input.Start = input.End;
                return input;
            }
        }

        input.Start = BigInteger.ZERO;
        mask = InitializeMask();
        mask = mask.shiftRight(input.subnetslash);
        mask = mask.not();
        input.Start = input.End.and(mask);

        return input;
    }

    public static String CompressAddress(String sin) {
        String s = "";
        if (IsAddressCorrect(sin)) {
            sin = Kolonlar(FormalizeAddr(sin));

            String p = "";
            int[] pref = new int[8];
            int[] idx = new int[8];

            String[] sprfx = sin.split(":");

            for (int i = 0; i < 8; i++) {
                pref[i] = Integer.valueOf(sprfx[i], 16);
            }
            //Sıfır OLMAYAN bölümler: non-zero parts
            for (int i = 0; i < 8; i++) {
                if (pref[i] > 0) {
                    idx[i] = 1;
                }
            }
            // Sıfır OLAN bölümlerin uzunlukları: Length of the zero parts
            int n = 0, z = 0, fark = 0, max = 0;
            int[] aralik = new int[2];
            for (int i = 0; i < 8; i++) {
                if (idx[i] != 0) {
                    n = z = i;
                    n++;
                } else {
                    z = i;
                    fark = z - n;
                    if (fark > max) {
                        max = fark;
                        aralik[0] = n;
                        aralik[1] = z;
                    }
                }
            }

            if (aralik[0] == aralik[1]) {
                String st = "";
                for (int nn : pref) {
                    if (nn == 0) {
                        st += "*" + ":";
                    } else {

                        st += Integer.toHexString(nn) + ":";
                    }
                }
                st = st.replaceAll("[:]+$", "");

                int c = st.split("[*]").length - 1;

                if (c == 1) {
                    st = st.replaceAll("[*]", ":");
                } else if (c > 1) {
                    st = st.replaceFirst("[*]", ":");
                }
                st = st.replaceAll(":::", "::");
                st = st.replaceAll("[*]", "0");

                return st;
            }

            for (int i = 0; i < 8; i++) {
                if (i == aralik[0]) {
                    p += "::";
                }
                if (i < aralik[0] || i > aralik[1]) {
                    if (i != 7) {
                        p += Integer.toHexString(pref[i]) + ":";
                    } else {
                        p += Integer.toHexString(pref[i]);
                    }
                }
            }
            p = p.replaceAll(":::", "::");

            return p;
        } else {
            return s;
        }
    }

    /// <summary>
    /// Goes to previous address space when user clicks on PrevSpace button.
    /// </summary>
    /// <param name="input">SEaddress</param>
    /// <param name="is128Checked">Boolean</param>
    /// <returns>SEaddress</returns>
    public static SEaddress PrevSpace(SEaddress input, Boolean is128Checked) {
        input.subnetidx = AddressSpaceNo(input.End, input.slash, is128Checked);

        mask = InitializeMask();
        mask = mask.shiftRight(input.slash);
        mask = mask.not();
        input.Start = input.End.and(mask);

        input.LowerLimitAddress = input.Start;
        input.UpperLimitAddress = input.End;

        return input;
    }

    public static SEaddress NextSpace(SEaddress input, Boolean is128Checked) {
        input.subnetidx = AddressSpaceNo(input.Start, input.slash, is128Checked);

        mask = InitializeMask();
        mask = mask.shiftRight(input.slash);
        input.End = input.Start.or(mask);

        input.LowerLimitAddress = input.Start;
        input.UpperLimitAddress = input.End;

        return input;
    }

    /// <summary>
    /// Finds the address space number (or index number).
    /// </summary>
    /// <param name="address">BigInteger</param>
    /// <param name="slash">prefix</param>
    /// <param name="is128Checked">Boolean</param>
    /// <returns></returns>
    private static BigInteger AddressSpaceNo(BigInteger address, int slash, Boolean is128Checked) {
        BigInteger spaceno = BigInteger.ZERO;
        int delta = slash - 1;

        int count = 127;

        for (int i = count; i > (count - slash); i--) {
            if (address.testBit(i) == true) {
                spaceno = spaceno.setBit(delta);
            } else {
                spaceno = spaceno.clearBit(delta);
            }
            delta--;
        }

        return spaceno;
    }

    /**
     * Go to user-entered subnet number (or index number).
     *
     * @param input SEaddress
     * @param is128Checked Boolean
     * @return SEaddress
     */
    public static SEaddress GoToSubnet(SEaddress input, Boolean is128Checked) {

        int count = 128;

        int bitStart = count - input.subnetslash;
        int bitUpto = count - input.slash;

        for (int i = bitStart; i < bitUpto; i++) {
            if ((input.subnetidx.and(BigInteger.ONE)).compareTo(BigInteger.ONE) == 0) {
                input.Start = input.Start.setBit(i);
            } else {
                input.Start = input.Start.clearBit(i);
            }
            input.subnetidx = input.subnetidx.shiftRight(1);
        }

        if (!is128Checked) {
            if (input.slash == 64) {
                input.End = input.Start;
                return input;
            }
        } else if (is128Checked) {
            if (input.slash == 128) {
                input.End = input.Start;
                return input;
            }
        }

        return input;
    }

    public static SEaddress ListSubRangeFirstPage(SEaddress input, Boolean is128Checked) {
        BigInteger shiftAdd = BigInteger.ONE.shiftLeft(64);
        SEaddress subnets = new SEaddress();
        subnets = input;

        BigInteger TotalSubs = BigInteger.ZERO;
        if (!is128Checked) {
            TotalSubs = (BigInteger.ONE.shiftLeft(64 - input.subnetslash)).subtract(BigInteger.ONE);
        } else {
            TotalSubs = (BigInteger.ONE.shiftLeft(128 - input.subnetslash)).subtract(BigInteger.ONE);
        }
        //
        subnets.liste = FXCollections.observableArrayList();

        subnets.LowerLimitAddress = input.LowerLimitAddress;
        subnets.UpperLimitAddress = input.UpperLimitAddress;

        String ss = "";
        int count = 0;

        for (count = 0; count < input.upto; count++) {
            //
            subnets = RangeIndex(subnets, is128Checked);

            if (!is128Checked) {
                ss = Kolonlar(subnets.Start);
                ss = ss.substring(0, 19) + "::";
                ss = CompressAddress(ss);
                ss = "p" + subnets.subnetidx + "> " + ss + "/64";
            } else if (is128Checked) {
                ss = Kolonlar(subnets.Start);
                ss = CompressAddress(ss);
                ss = "p" + subnets.subnetidx + "> " + ss + "/128";
            }

            if (subnets.subnetidx.equals(TotalSubs)) {
                subnets.liste.add(ss);
                break;
            } else {
                subnets.liste.add(ss);
            }

            if (!is128Checked) {
                subnets.Start = subnets.Start.add(shiftAdd);
            } else if (is128Checked) {
                subnets.Start = subnets.Start.add(BigInteger.ONE);
            }
        }

        return subnets;
    }

    public static SEaddress ListSubRangePageBackward(SEaddress input, Boolean is128Checked) {

        SEaddress subnets = new SEaddress();
        subnets = input;
        subnets.liste = FXCollections.observableArrayList();

        BigInteger shiftSub = BigInteger.ONE.shiftLeft(64);
        String ss = "";
        int count = 0;

        for (count = 0; count < input.upto; count++) {
            subnets = RangeIndex(subnets, is128Checked);

            if (is128Checked) {
                ss = Kolonlar(subnets.Start);
                ss = CompressAddress(ss);
                ss = "p" + subnets.subnetidx + "> " + ss + "/128";
            } else if (!is128Checked) {
                ss = Kolonlar(subnets.Start);
                ss = ss.substring(0, 19) + "::";
                ss = CompressAddress(ss);
                ss = "p" + subnets.subnetidx + "> " + ss + "/64";
            }

            if (subnets.subnetidx.equals(BigInteger.ZERO)) {
                subnets.liste.add(ss);
                break;
            } else {
                subnets.liste.add(ss);
            }

            if (!is128Checked) {
                subnets.Start = subnets.Start.subtract(shiftSub);
            } else if (is128Checked) {
                subnets.Start = subnets.Start.subtract(BigInteger.ONE);
            }

        }
        javafx.collections.FXCollections.reverse(subnets.liste);

        return subnets;
    }

    public static SEaddress ListSubRangePageForward(SEaddress input, Boolean is128Checked) {
        BigInteger shiftAdd = BigInteger.ONE.shiftLeft(64);
        SEaddress subnets = new SEaddress();
        subnets = input;

        subnets.liste = FXCollections.observableArrayList();

        String ss = "";
        int count = 0;
        BigInteger NumberOfSubnets = BigInteger.ZERO;
        if (is128Checked)
            NumberOfSubnets = BigInteger.ONE.shiftLeft(128 - input.subnetslash);
        else if (!is128Checked)
            NumberOfSubnets = BigInteger.ONE.shiftLeft(64 - input.subnetslash);

        for (count = 0; count < input.upto; count++) {
            subnets = RangeIndex(subnets, is128Checked);

            if (!is128Checked) {
                ss = Kolonlar(subnets.Start);
                ss = ss.substring(0, 19) + "::";
                ss = CompressAddress(ss);
                ss = "p" + subnets.subnetidx + "> " + ss + "/64";
            } else if (is128Checked) {
                ss = Kolonlar(subnets.Start);
                ss = CompressAddress(ss);
                ss = "p" + subnets.subnetidx + "> " + ss + "/128";
            }

            subnets.liste.add(ss);

            if (subnets.subnetidx.equals(NumberOfSubnets.subtract(BigInteger.ONE))) {
                break;
            }

            if (!is128Checked) {
                subnets.Start = subnets.Start.add(shiftAdd);
            } else if (is128Checked) {
                subnets.Start = subnets.Start.add(BigInteger.ONE);
            }
        }

        return subnets;
    }

    public static SEaddress ListSubRangeLastPage(SEaddress input, Boolean is128Checked) {
        SEaddress subnets = new SEaddress();
        subnets = input;
        BigInteger shiftSub = BigInteger.ONE.shiftLeft(64);

        String ss = "";
        int count = 0;

        for (count = 0; count < input.upto; count++) {
            subnets = RangeIndex(subnets, is128Checked);

            if (is128Checked) {
                ss = Kolonlar(subnets.Start);
                ss = CompressAddress(ss);
                ss = "p" + subnets.subnetidx + "> " + ss + "/128";
            } else if (!is128Checked) {
                ss = Kolonlar(subnets.Start);
                ss = ss.substring(0, 19) + "::";
                ss = CompressAddress(ss);
                ss = "p" + subnets.subnetidx + "> " + ss + "/64";
            }

            subnets.liste.add(ss);

            if (subnets.Start.equals(input.LowerLimitAddress)) {
                break;
            } else {
                if (!is128Checked) {
                    subnets.Start = subnets.Start.subtract(shiftSub);
                } else if (is128Checked) {
                    subnets.Start = subnets.Start.subtract(BigInteger.ONE);
                }
            }
        }
        javafx.collections.FXCollections.reverse(subnets.liste);

        return subnets;
    }

    public static SEaddress RangeIndex(SEaddress input, Boolean is128Checked) {
        
        input.subnetidx = BigInteger.ZERO;
        int downto = 0;

        if (!is128Checked) {
            downto = 64;
        } else if (is128Checked) {
            downto = 0;
        }

        int delta = 127 - input.subnetslash;

        for (int i = delta; i >= downto; i--) {
            if (input.Start.testBit(i)) {
                input.subnetidx = input.subnetidx.setBit(delta);
            } else {
                input.subnetidx = input.subnetidx.clearBit(delta);
            }
            delta--;
        }
        if (!is128Checked) {
            input.subnetidx = input.subnetidx.shiftRight(64);
        }

        return input;
    }

    public static SEaddress GoToAddrSpace(SEaddress input, Boolean is128Checked) {
        int count = 128;

        int bitStart = count - input.slash;
        int bitUpto = count;

        for (int i = bitStart; i < bitUpto; i++) {
            if (input.subnetidx.and(BigInteger.ONE).compareTo(BigInteger.ONE) == 0) {
                input.Start = input.Start.setBit(i);
            } else {
                input.Start = input.Start.clearBit(i);
            }
            input.subnetidx = input.subnetidx.shiftRight(1);
        }

        if (!is128Checked) {
            if (input.slash == 64) {
                input.End = input.Start;
                return input;
            }
        } else if (is128Checked) {
            if (input.slash == 128) {
                input.End = input.Start;
                return input;
            }
        }

        mask = InitializeMask();
        mask = mask.shiftRight(input.slash);
        input.End = input.Start.or(mask);

        input.subnetidx = AddressSpaceNo(input.Start, input.slash, is128Checked);

        input.LowerLimitAddress = input.Start;
        input.UpperLimitAddress = input.End;

        return input;
    }

    /// <summary>
    /// Find the prefix of a given (SEaddress) IPv6 address
    /// 
    /// </summary>
    /// <param name="input">SEaddress</param>
    /// <param name="is128Checked">Boolean</param>
    /// <returns>SEaddress</returns>
    public static SEaddress FindPrefixIndex(SEaddress input, Boolean is128Checked) {

        if (input.slash == input.subnetslash) {
            return input;
        }
        input.subnetidx = BigInteger.ZERO;
        int delta = input.subnetslash - input.slash - 1;
        int count = 127;

        for (int i = (count - input.slash); i > (count - input.subnetslash); i--) {
            if (input.Start.testBit(i) == true) {
                input.subnetidx = input.subnetidx.setBit(delta);
            } else {
                input.subnetidx = input.subnetidx.clearBit(delta);
            }
            delta--;
        }

        return input;
    }

    public static String[] DnsRev(BigInteger inv6, int subnetslash, Boolean is128Checked) {

        String s = v6ST.Kolonlar(inv6).replaceAll(":", "");
        int count = 0, countarray = 0;

        if (!is128Checked) {
            count = 16;
            countarray = 8;

            if (s.length() > 16) {
                s = s.substring(0, 16);
            }
        } else if (is128Checked) {
            count = 32;
            countarray = 16;
        }

        String[] sa = new String[countarray];

        int remainder = subnetslash % 4;
        int len = (subnetslash + (4 - remainder)) / 4;
        int nzones = (1 << (4 - remainder));

        if (subnetslash % 4 == 0) // it's nibble-boundary / 4'un TAM KATI. 
        {                         // we can work with sa[0] only.
            sa[0] = s.substring(0, len - 1);
            String stmp = "";

            for (int j = sa[0].length() - 1; j >= 0; j--) {
                stmp += sa[0].charAt(j) + ".";
            }
            stmp += arpa;
            sa[0] = stmp;
        } else // non-nibble boundary
        {
            s = s.substring(0, len);
            BigInteger zones = new BigInteger(s, 16);
            for (int i = 0; i < nzones; i++) {
                sa[i] = String.format("%x", zones);

                if (sa[i].length() < len) {
                    sa[i] = String.format("%1$" + len + "s", sa[i]).replace(" ", "0");
                }

                String stmp = "";

                for (int j = sa[i].length() - 1; j >= 0; j--) {
                    stmp += sa[i].charAt(j) + ".";
                }
                stmp += arpa;
                sa[i] = stmp;
                zones = zones.add(BigInteger.ONE);
            }
        }

        return sa;
    }

    public static SEaddress ListDnsRevFirstPage(SEaddress input, Boolean is128Checked) {
        
        SEaddress subnets = new SEaddress();
        subnets = input;
        subnets.subnetidx = BigInteger.ZERO;

        int count = 0;
        int spaces = 0;

        String[] sa;
        String sf;

        for (count = 0; count < subnets.upto; count++) {
            subnets = Subnetting(subnets, is128Checked);

            sa = DnsRev(subnets.Start, subnets.subnetslash, is128Checked);
            sf = "p" + subnets.subnetidx + "> " + sa[0];
            subnets.liste.add(sf);

            String[] sr = sf.split(" ");
            spaces = sr[0].length() + 1;

            for (int i = 1; i < 8; i++) {
                if (sa[i] == null) {
                    break;
                }
                
                sa[i] = String.format("%1$" + Integer.valueOf(sa[i].length() + spaces) + "s", sa[i]);
                    
                subnets.liste.add(sa[i]);
            }

            if (subnets.End.equals(input.UpperLimitAddress)) {
                break;
            } else {
                subnets.Start = subnets.End.add(BigInteger.ONE);
            }
        }

        return subnets;
    }

    public static SEaddress ListDnsRevPageBackward(SEaddress input, Boolean is128Checked) {
        SEaddress subnets = new SEaddress();
        subnets = input;
        subnets.subnetidx = BigInteger.ZERO;

        String[] sa;
        int count = 0;
        int spaces = 0;

        for (count = 0; count < input.upto; count++) {
            subnets = EndStartAddresses(subnets, is128Checked);

            sa = DnsRev(subnets.Start, subnets.subnetslash, is128Checked);

            String sf = "p" + subnets.subnetidx + "> " + sa[0];

            String[] sr = sf.split(" ");
            spaces = sr[0].length() + 1;

            for (int i = 7; i > 0; i--) {
                if (sa[i] == null) {
                    continue;
                }
                sa[i] = String.format("%1$" + Integer.valueOf(sa[i].length() + spaces) + "s", sa[i]);
                subnets.liste.add(sa[i]);
            }
            subnets.liste.add(sf);

            subnets.End = subnets.Start.subtract(BigInteger.ONE);

            if (subnets.Start.equals(input.LowerLimitAddress)) {
                break;
            }
        }
        javafx.collections.FXCollections.reverse(subnets.liste);

        return subnets;
    }

    public static SEaddress ListDnsRevPageForward(SEaddress input, Boolean is128Checked) {
        SEaddress subnets = new SEaddress();
        subnets = input;
        subnets.subnetidx = BigInteger.ZERO;
        
        int count = 0;
        int spaces = 0;
        String[] sa;

        for (count = 0; count < input.upto; count++) {
            subnets = Subnetting(subnets, is128Checked);

            sa = DnsRev(subnets.Start, subnets.subnetslash, is128Checked);

            String sf = "p" + subnets.subnetidx + "> " + sa[0];
            subnets.liste.add(sf);

            String[] sr = sf.split(" ");
            spaces = sr[0].length() + 1;

            for (int i = 1; i < 8; i++) {
                if (sa[i] == null) {
                    break;
                }

                sa[i] = String.format("%1$" + Integer.valueOf(sa[i].length() + spaces) + "s", sa[i]);
                subnets.liste.add(sa[i]);
            }

            if (subnets.End.equals(input.UpperLimitAddress)
                    || subnets.subnetidx.equals(BigInteger.ZERO)) {
                break;
            }
            subnets.Start = subnets.End.add(BigInteger.ONE);
        }

        return subnets;
    }

    public static SEaddress ListDnsRevLastPage(SEaddress input, Boolean is128Checked) {
        SEaddress subnets = new SEaddress();
        subnets = input;

        subnets.subnetidx = BigInteger.ZERO;

        String[] sa;
        int count = 0;
        int spaces = 0;

        for (count = 0; count < input.upto; count++) {
            subnets = EndStartAddresses(subnets, is128Checked);

            sa = DnsRev(subnets.Start, subnets.subnetslash, is128Checked);

            String sf = "p" + subnets.subnetidx + "> " + sa[0];

            String[] sr = sf.split(" ");
            spaces = sr[0].length() + 1;

            for (int i = 7; i > 0; i--) {
                if (sa[i] == null) {
                    continue;
                }
                sa[i] = String.format("%1$" + Integer.valueOf(sa[i].length() + spaces) + "s", sa[i]);
                subnets.liste.add(sa[i]);
            }
            subnets.liste.add(sf);

            subnets.End = subnets.Start.subtract(BigInteger.ONE);
            if (subnets.subnetidx == BigInteger.ZERO) {
                break;
            }
        }
        javafx.collections.FXCollections.reverse(subnets.liste);

        return subnets;
    }

    public static String FindParentNet(String prefix, short parentpflen, Boolean is128Checked) {
        mask = PrepareMask(parentpflen);

        BigInteger Resultv6 = BigInteger.ZERO;

        if (IsAddressCorrect(prefix)) {
            Resultv6 = FormalizeAddr(prefix).and(mask);
            String s = "";

            if (is128Checked) {
                s = Kolonlar(Resultv6);
                s = CompressAddress(s) + "/" + parentpflen;
                return s;
            } else { /* unchecked 64 bits*/
                s = Kolonlar(Resultv6);
                s = s.substring(0, 19) + "::";
                s = CompressAddress(s) + "/" + parentpflen;
                return s;
            }
        } else {
            return null;
        }
    }

    public static BigInteger PrepareMask(short pflen) {
        if (pflen == 128) {
            return (InitializeMask());
        }

        if (pflen > 127 || pflen < 0) {
            return BigInteger.ZERO;
        }

        int delta = 127 - pflen;
        mask = BigInteger.ZERO;

        for (int i = 127; i > delta; i--) {
            mask = (mask.or(BigInteger.ONE.shiftLeft(i)));
        }
        return mask;
    }

    /**
     * This function is used to convert given AS number to requested notation,
     * as-plain or as-dot.
     * <br> 'null' is returned in case of exception/error.
     * <br> Maximum value for as-plain and as-dot is 4294967295 and 65535.65535, respectively.
     * <br><br>
     * <b>toASplain</b>: Input will be converted from as-dot to as-plain notation.
     * Pass it like 'v6ST.toASplain'.
     * <br>
     * <b>toASdot</b>: Input will be converted from as-plain to as-dot notation.
     * Pass it like 'v6ST.toASdot'.
     * 
     * @param asin String AS number input.
     * @param b Boolean toASplain(which is true) or toASdot(which is false).
     * @return String as-plain or as-dot or null.
     */
    public static String ConvertASnum(String asin, Boolean b) {
        if (b) {  //v6ST.toASplain / true
            char[] chars = asin.trim().toCharArray();
            if (chars.length != 0) {
                int c = asin.trim().split("[.]", -1).length - 1;
                if (chars[0] == '.' || chars[chars.length - 1] == '.' || c != 1) {
                    v6ST.errmsg = "Input Error!";
                    return null;
                } else {
                    try {
                        long result
                                = Long.parseLong(asin.trim().split("[.]")[0]) * 65536
                                + Long.parseLong(asin.trim().split("[.]")[1]);
                        if (result > asMax) {
                            v6ST.errmsg = "Exceeded max! 65535.65535";
                            return null;
                        } else {
                            return String.valueOf(result);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(IPv6SubnettingTool.class.getName()).log(Level.SEVERE, null, ex);
                        v6ST.errmsg = ex.toString();
                        return null;
                    }
                }
            } else {
                v6ST.errmsg = "Error in input string length!";
                return null;
            }
        } else { //v6ST.toASdot / false
            if (asin.trim().length() > 0) {
                try {
                    long asplain = Long.parseLong(asin.trim());
                    if (asplain <= asMax) {
                        return (String.valueOf(asplain >> 16)
                                + "."
                                + String.valueOf(asplain % 65536));
                    } else {
                        v6ST.errmsg = "Exceeded max! 4294967295";
                        return null;
                    }
                } catch (Exception ex) {
                    Logger.getLogger(IPv6SubnettingTool.class.getName()).log(Level.SEVERE, null, ex);
                    v6ST.errmsg = ex.toString();
                    return null;
                }
            } else {
                v6ST.errmsg = "Error in input string length!";
                return null;
            }
        }
    }
    
} //END of Class