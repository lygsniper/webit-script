// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.script.web.resolvers;

import webit.script.resolvers.GetResolver;
import webit.script.resolvers.MatchMode;
import webit.script.resolvers.SetResolver;
import webit.script.web.HttpServletRequestAttributes;

/**
 *
 * @author Zqq
 */
public class HttpServletRequestAttributesResolver implements GetResolver, SetResolver {

    public Object get(Object bean, Object property) {
        return ((HttpServletRequestAttributes) bean).get(property.toString());
    }

    public boolean set(Object bean, Object property, Object value) {
        ((HttpServletRequestAttributes) bean).set(property.toString(), value);
        return true;
    }

    public MatchMode getMatchMode() {
        return MatchMode.EQUALS;
    }

    public Class<?> getMatchClass() {
        return HttpServletRequestAttributes.class;
    }
}
