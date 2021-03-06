// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.script;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;
import webit.script.core.Parser;
import webit.script.core.ast.TemplateAST;
import webit.script.exceptions.ParseException;
import webit.script.exceptions.ScriptRuntimeException;
import webit.script.io.Out;
import webit.script.io.impl.OutputStreamOut;
import webit.script.io.impl.WriterOut;
import webit.script.loaders.Resource;
import webit.script.util.EncodingPool;
import webit.script.util.ExceptionUtil;

/**
 *
 * @author Zqq
 */
public final class Template {

    public final Engine engine;
    public final String name;
    public final Resource resource;
    private TemplateAST templateAst;
    private long lastModified;
    //
    private final Object reloadLock = new Object();

    public Template(Engine engine, String name, Resource resource) {
        this.engine = engine;
        this.name = name;
        this.resource = resource;
    }

    /**
     *
     * @return TemplateAST
     * @throws ParseException
     */
    public TemplateAST prepareTemplate() throws ParseException {
        final TemplateAST tmpl;
        if ((tmpl = this.templateAst) != null && !resource.isModified()) {
            return tmpl;
        } else {
            return parseAST();
        }
    }

    private TemplateAST parseAST() throws ParseException {
        TemplateAST tmpl;
        synchronized (reloadLock) {
            if ((tmpl = this.templateAst) == null || resource.isModified()) {
                try {
                    this.templateAst = tmpl = new Parser()
                            .parseTemplate(
                            resource.openReader(), //Parser will close reader when finish
                            this);
                } catch (IOException e) {
                    throw ExceptionUtil.castToParseException(e);
                }
                lastModified = System.currentTimeMillis();
            }
        }
        return tmpl;
    }

    /**
     *
     * @param root
     * @param outputStream
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context merge(final Map<String, Object> root, final OutputStream outputStream) throws ScriptRuntimeException, ParseException {
        return merge(root, new OutputStreamOut(outputStream, engine.getEncoding(), engine.getCoderFactory()));
    }

    /**
     *
     * @param root
     * @param outputStream
     * @param encoding
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context merge(final Map<String, Object> root, final OutputStream outputStream, final String encoding) throws ScriptRuntimeException, ParseException {
        return merge(root, new OutputStreamOut(outputStream, encoding != null ? EncodingPool.intern(encoding) : engine.getEncoding(), engine.getCoderFactory()));
    }

    /**
     *
     * @param root
     * @param writer
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context merge(final Map<String, Object> root, final Writer writer) throws ScriptRuntimeException, ParseException {
        return merge(root, new WriterOut(writer, engine.getEncoding(), engine.getCoderFactory()));
    }

    /**
     *
     * @param root
     * @param out
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context merge(final Map<String, Object> root, final Out out) throws ScriptRuntimeException, ParseException {
        try {
            TemplateAST tmpl;
            if ((tmpl = this.templateAst) == null || resource.isModified()) {
                tmpl = parseAST();
            }
            return tmpl.execute(new Context(this, out), root);
        } catch (Throwable e) {
            throw wrapThrowable(e);
        }
    }

    public void reset() {
        templateAst = null;
        lastModified = 0;
    }

    public long getLastModified() {
        return lastModified;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != Template.class) {
            return false;
        }
        Template other = (Template) obj;
        return (this.engine == other.engine) && (this.name.equals(other.name));
    }

    private RuntimeException wrapThrowable(final Throwable exception) throws ScriptRuntimeException, ParseException {
        if (exception instanceof ScriptRuntimeException) {
            return ((ScriptRuntimeException) exception).setTemplate(this);
        } else if (exception instanceof ParseException) {
            return ((ParseException) exception).registTemplate(this);
        } else {
            return new ScriptRuntimeException(exception).setTemplate(this);
        }
    }
}
