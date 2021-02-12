/*
 *
 *   Copyright [ 2020 - 2021 ] [Matthew Buckton]
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
 *
 */

package org.maps.messaging.engine.selector.operators.functions;

import org.maps.messaging.api.message.Message;
import org.maps.messaging.engine.selector.ParseException;
import org.maps.messaging.engine.selector.operators.ComparisonOperator;
import org.maps.messaging.engine.selector.operators.FunctionOperator;
import org.maps.messaging.engine.selector.operators.Operation;
import org.maps.messaging.engine.selector.operators.comparison.GreaterOrEqualOperator;
import org.maps.messaging.engine.selector.operators.comparison.LessOrEqualOperator;

public class BetweenOperator extends FunctionOperator {

  private final Object lhs;
  private final ComparisonOperator bottomOperator;
  private final ComparisonOperator topOperator;


  public BetweenOperator(Object lhs, Object lowest, Object highest){
    if(lowest instanceof Operation){
      lowest = ((Operation)lowest).compile();
    }
    if(highest instanceof Operation){
      highest = ((Operation)highest).compile();
    }

    this.lhs = lhs;
    bottomOperator = new GreaterOrEqualOperator(lhs, lowest);
    topOperator = new LessOrEqualOperator(lhs, highest);
  }

  public Object compile(){
    bottomOperator.compile();
    if(lhs instanceof Number &&
        bottomOperator.getRHS() instanceof Number &&
        topOperator.getRHS() instanceof Number){
      boolean bottom  = (Boolean) bottomOperator.evaluate(lhs, bottomOperator.getRHS());
      boolean top  = (Boolean) topOperator.evaluate(lhs, topOperator.getRHS());
      return top && bottom;
    }
    return this;
  }

  @Override
  public Object evaluate(Message message) throws ParseException {
    boolean bottom = ((Boolean)bottomOperator.evaluate(message));
    if(bottom) {
      return topOperator.evaluate(message);
    }
    return false;
  }

  public String toString(){
    return "("+lhs.toString() +") BETWEEN ("+ bottomOperator.getRHS()+" AND "+topOperator.getRHS()+")";
  }

  @Override
  public boolean equals(Object test){
    if(test instanceof BetweenOperator){
      return (lhs.equals(((BetweenOperator) test).lhs) &&
          bottomOperator.equals(((BetweenOperator) test).bottomOperator) &&
          topOperator.equals(((BetweenOperator) test).topOperator));
    }
    return false;
  }

  @Override
  public int hashCode(){
    return lhs.hashCode() | bottomOperator.hashCode() ^ topOperator.hashCode();
  }

}
