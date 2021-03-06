// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.script.core.text;

import webit.script.Template;
import webit.script.core.ast.Statment;

/**
 *
 * @author Zqq
 */
public interface TextStatmentFactory {

    void startTemplateParser(Template template);

    void finishTemplateParser(Template template);

    Statment getTextStatment(Template template, char[] text, int line, int column);
}
