// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.script.core.ast.expressions;

import webit.script.core.ast.Expression;
import webit.script.core.ast.ResetableValueExpression;
import webit.script.core.ast.SelfOperator;
import webit.script.util.ALU;

/**
 *
 * @author Zqq
 */
public final class SelfPlusOperator extends SelfOperator{

    public SelfPlusOperator(ResetableValueExpression leftExp, Expression rightExp, int line, int column) {
        super(leftExp, rightExp, line, column);
    }

    @Override
    protected Object doOperate(final Object right, final Object left) {
        return ALU.plus(left, right);
    }
}
