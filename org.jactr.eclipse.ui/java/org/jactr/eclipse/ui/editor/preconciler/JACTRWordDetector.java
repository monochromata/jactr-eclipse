/*
 * Created on Apr 18, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jactr.eclipse.ui.editor.preconciler;

import org.eclipse.jface.text.rules.IWordDetector;

public class JACTRWordDetector implements IWordDetector
{

  public boolean isWordPart(char c)
  {
    return Character.isLetter(c)
        || Character.getType(c) == Character.CONNECTOR_PUNCTUATION || c == '-';
  }

  public boolean isWordStart(char c)
  {
    return Character.isLetter(c);
  }

}
