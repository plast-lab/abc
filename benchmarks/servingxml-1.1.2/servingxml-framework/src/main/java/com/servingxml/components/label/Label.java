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

package com.servingxml.components.label;

import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.common.ValueEvaluator;
import com.servingxml.components.common.StringLiteralValueEvaluator;
import com.servingxml.app.Flow;

public class Label {
  private final ValueEvaluator valueEvaluator;

  public Label(ValueEvaluator valueEvaluator) {
    this.valueEvaluator = valueEvaluator;
  }

  public Label(String s) {
    this.valueEvaluator = new StringLiteralValueEvaluator(s);
  }

  public String getText(ServiceContext context, Flow flow) {
    String value = valueEvaluator.evaluateString(context, flow);
    return value;
  }
}

