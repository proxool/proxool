/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000,2002,2003 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Contact: Eric.Bruneton@rd.francetelecom.com
 *
 * Author: Eric Bruneton
 */

package org.logicalcobwebs.asm;

/**
 * A non standard class, field, method or code attribute.
 */

public class Attribute {

  /**
   * The type of this attribute.
   */

  public String type;

  /**
   * The byte array that contains the value of this attribute. <i>The content of
   * this array must not be modified, although the array itself can be changed
   * (i.e. attr.b[...] = ...; is forbidden, but attr.b = ...; is allowed)</i>.
   */

  public byte[] b;

  /**
   * Index of the first byte of this attribute in {@link #b b}.
   */

  public int off;

  /**
   * Length of this attributes, in bytes.
   */

  public int len;

  /**
   * The next attribute in this attribute list. May be <tt>null</tt>.
   */

  public Attribute next;

  /**
   * Constructs a new {@link Attribute}.
   *
   * @param type the type of the attribute.
   * @param b byte array that contains the value of the attribute.
   * @param off index of the first byte of the attribute in <tt>b</tt>.
   * @param len length of the attributes, in bytes.
   */

  public Attribute (
    final String type,
    final byte[] b,
    final int off,
    final int len)
  {
    this.type = type;
    this.b = b;
    this.off = off;
    this.len = len;
  }

  /**
   * Constructs a new empty attribute.
   *
   * @param type the type of the attribute.
   */

  public Attribute (final String type) {
    this(type, null, 0, 0);
  }

  /**
   * Returns the length of the attribute list that begins with this attribute.
   *
   * @return the length of the attribute list that begins with this attribute.
   */

  final int getCount () {
    int count = 0;
    Attribute attr = this;
    while (attr != null) {
      count += 1;
      attr = attr.next;
    }
    return count;
  }

  /**
   * Returns the size of all the attributes in this attribute list.
   *
   * @param cw the class writer to be used to convert the attributes into byte
   *      arrays, with the {@link ClassWriter#writeAttribute writeAttribute}
   *      method.
   * @return the size of all the attributes in this attribute list. This size
   *      includes the size of the attribute headers.
   */

  final int getSize (final ClassWriter cw) {
    int size = 0;
    Attribute attr = this;
    while (attr != null) {
      cw.newUTF8(attr.type);
      size += cw.writeAttribute(attr).length + 6;
      attr = attr.next;
    }
    return size;
  }

  /**
   * Writes all the attributes of this attribute list in the given byte vector.
   *
   * @param cw the class writer to be used to convert the attributes into byte
   *      arrays, with the {@link ClassWriter#writeAttribute writeAttribute}
   *      method.
   * @param out where the attributes must be written.
   */

  final void put (final ClassWriter cw, final ByteVector out) {
    Attribute attr = this;
    while (attr != null) {
      byte[] b = cw.writeAttribute(attr);
      out.put2(cw.newUTF8(attr.type)).put4(b.length);
      out.putByteArray(b, 0, b.length);
      attr = attr.next;
    }
  }
}
