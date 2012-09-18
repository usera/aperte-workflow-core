package org.aperteworkflow.util.vaadin;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.ApplicationPortlet2;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.ui.Window;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * User: POlszewski
 * Date: 2012-02-02
 * Time: 14:33
 */
public class ApplicationPortlet2WithLoadingMessage extends ApplicationPortlet2 {
	@Override
	protected void writeAjaxPageHtmlVaadinScripts(RenderRequest request, RenderResponse response, BufferedWriter writer, Application application, String themeName) throws IOException, PortletException {
		response.createResourceURL().setParameter("img", "loader");

		I18NSource i18NSource = I18NSourceFactory.createI18NSource(request.getLocale());
		writer.write(String.format("<div name='%s'>%s</div>",
				getLoaderTagId(getPortletConfig()),
				i18NSource.getMessage("loader.message")));

		super.writeAjaxPageHtmlVaadinScripts(request, response,	writer,	application, themeName);
	}	

	public static void hideLoadingMessage(Window window, PortletApplicationContext2 context) 
	{
		/* Moved to theme */
		/* We do not use getElementsByName becouse it doesn't work on IE */
//		String js = "if (document.getElementsByTagName) { " +
//						"var elements = document.getElementsByTagName('div'); " +
//						"for(i = 0,iarr = 0; i < elements.length; i++){ " +
//							"var att = elements[i].getAttribute('name'); " +
//							"if(att == '%s'){ " +
//								"elements[i].style.display = 'none'; " +
//								"return; " +
//							"} " +
//						"} " +
//					"} ";
//		window.executeJavaScript(String.format(js, getLoaderTagId(context.getPortletConfig())));
		window.executeJavaScript("hideLoadingMessage('"+getLoaderTagId(context.getPortletConfig())+"');");
	}

	private static String getLoaderTagId(PortletConfig config) {
		return ("vaadinLoader_" + config.getPortletName() + "_" + config.getPortletContext().getPortletContextName())
				.replaceAll("[^\\w-]","_");
	}
}
