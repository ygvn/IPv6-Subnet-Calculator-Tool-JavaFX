/*
 * Copyright (c) 2010-2018, Yucel Guven
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * SEaddress is a storage object consisting of:
 * <br>
 * IPv6 address, start/end subnet addresses,
 * <br>
 * prefix slash and subnet slash values, etc.
 * @author (c) Yucel Guven
 */

public class SEaddress {
    public BigInteger Resultv6 = BigInteger.ZERO;
    public BigInteger LowerLimitAddress = BigInteger.ZERO;
    public BigInteger Start = BigInteger.ZERO;
    public BigInteger End = BigInteger.ZERO;
    public BigInteger UpperLimitAddress = BigInteger.ZERO;
    public int slash = 0;
    public int subnetslash = 0;
    public BigInteger subnetidx = BigInteger.ZERO;
    public int upto = 0;
    public ObservableList<String> liste = FXCollections.observableArrayList();
    public int ID = 0;
    /**
     * A simple method to initialize all values.
     */
    public void Initialize() {
        this.Resultv6 = BigInteger.ZERO;
        this.LowerLimitAddress = BigInteger.ZERO;
        this.Start = BigInteger.ZERO;
        this.End = BigInteger.ZERO;
        this.UpperLimitAddress = BigInteger.ZERO;
        this.slash = 0;
        this.subnetslash = 0;
        this.subnetidx = BigInteger.ZERO;
        this.upto = 0;
        this.ID = 0;
        this.liste.clear();
    }
}
