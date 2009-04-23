package br.com.caelum.vraptor.vraptor2;

import br.com.caelum.vraptor.InterceptionException;
import br.com.caelum.vraptor.Interceptor;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.resource.ResourceMethod;
import br.com.caelum.vraptor.vraptor2.outject.JsonOutjecter;
import br.com.caelum.vraptor.vraptor2.outject.Outjecter;
import org.vraptor.annotations.Remotable;
import org.vraptor.remote.json.JSONSerializer;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * VRaptor2 based ajax interceptor.
 *
 * @author Guilherme Silveira
 */
public class AjaxInterceptor implements Interceptor {

    private static final String UTF8 = "UTF-8";

    private final Outjecter outjecter;

    private final ComponentInfoProvider info;

    private final HttpServletResponse response;

    public AjaxInterceptor(Outjecter outjecter, HttpServletResponse response, ComponentInfoProvider info) {
        this.outjecter = outjecter;
        this.response = response;
        this.info = info;

    }

    public boolean accepts(ResourceMethod method) {
        // TODO this is not invoked as automatically loaded thorugh
        // RequestExecution
        // it should be included on the ExtractorList so would not be invoked?
        return info.isAjax();
    }

    public void intercept(InterceptorStack stack, ResourceMethod method, Object resourceInstance) throws InterceptionException {
        if (info.isAjax()) {
            if (!method.getMethod().isAnnotationPresent(Remotable.class)) {
                throw new InterceptionException("Unable to make an ajax result in a non-remotable method.");
            }
            int depth = method.getMethod().getAnnotation(Remotable.class).depth();
            JsonOutjecter outjecter = (JsonOutjecter) this.outjecter;
            CharSequence output = new JSONSerializer(depth).serialize(outjecter.contents());
            response.setCharacterEncoding(UTF8);
            response.setContentType("application/json");

            PrintWriter writer = null;
            try {
                writer = response.getWriter();
                writer.append(output);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                throw new InterceptionException(e);
            }
        } else {
            stack.next(method, resourceInstance);
        }
    }

}
