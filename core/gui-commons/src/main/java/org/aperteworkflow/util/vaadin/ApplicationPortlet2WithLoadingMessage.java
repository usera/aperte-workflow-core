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

	public static void hideLoadingMessage(Window window, PortletApplicationContext2 context) {
		String js = "if (document.getElementsByName) {" +
				"	var elems = document.getElementsByName('%s');" +
				"	for (var i = 0; i < elems.length; ++i) {" +
				"		elems[i].style.display = 'none';" +
				"	}" +
				"}";
		window.executeJavaScript(String.format(js, getLoaderTagId(context.getPortletConfig())));
	}

	private static String getLoaderTagId(PortletConfig config) {
		return ("vaadinLoader_" + config.getPortletName() + "_" + config.getPortletContext().getPortletContextName())
				.replaceAll("[^\\w-]","_");
	}
}
