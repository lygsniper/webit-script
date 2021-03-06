// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.script.core.ast.statments;

import webit.script.core.ast.Expression;
import webit.script.core.ast.Position;
import webit.script.core.ast.Statment;
import webit.script.core.ast.loop.LoopInfo;
import webit.script.util.StatmentUtil;

/**
 *
 * @author Zqq
 */
public final class WhileStatmentPart extends Position {

    private Expression whileExpr;
    private IBlockStatment bodyStatment;
    private boolean doWhileAtFirst;

    public WhileStatmentPart(Expression whileExpr, IBlockStatment bodyStatment, boolean doWhileAtFirst, int line, int column) {
        super(line, column);
        this.whileExpr = whileExpr;
        this.bodyStatment = bodyStatment;
        this.doWhileAtFirst = doWhileAtFirst;
    }

    public Statment pop(int label) {
        if (bodyStatment.hasLoops()) {
            LoopInfo[] loopInfos = StatmentUtil.collectPossibleLoopsInfoForWhileStatments(bodyStatment, null, label);
            return doWhileAtFirst
                    ? new WhileStatment(whileExpr, bodyStatment.getVarMap(), bodyStatment.getStatments(), loopInfos, label, line, column)
                    : new DoWhileStatment(whileExpr, bodyStatment.getVarMap(), bodyStatment.getStatments(), loopInfos, label, line, column);
        } else {
            return doWhileAtFirst
                    ? new WhileStatmentNoLoops(whileExpr, bodyStatment.getVarMap(), bodyStatment.getStatments(), line, column)
                    : new DoWhileStatmentNoLoops(whileExpr, bodyStatment.getVarMap(), bodyStatment.getStatments(), line, column);
        }
    }

    public Statment pop() {
        return pop(0);  //default label is zero;
    }
}
