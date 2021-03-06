/**
 *  ServingXML
 *  
 *  Copyright (C) 2006  Daniel Parker
 *    daniel.parker@servingxml.com 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/

package com.servingxml.components.recordio;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.components.regex.PatternMatcher;
import com.servingxml.app.Flow;
import com.servingxml.util.record.Value;

/**
 *
 * 
 * @author  Daniel A. Parker
 */


public class FieldRestriction implements Restriction {
  private final Name fieldName;
  private final PatternMatcher matcher;

  public FieldRestriction(Name fieldName, PatternMatcher matcher) {
    this.fieldName = fieldName;
    this.matcher = matcher;
  }

  public boolean accept(ServiceContext context, Flow flow, Value value) {
    boolean accept = false;

    Record record = flow.getRecord();
    String s = record.getString(fieldName);
    if (s != null) {
      accept = matcher.match(s);
    }
    return accept;
  }
}
