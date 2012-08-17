package pl.net.bluesoft.rnd.processtool.plugins;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.plugins.util.UserProcessQueuesSizeProvider;

import com.thoughtworks.xstream.XStream;

/**
 * Servlet which provides logic to get all avaiable user process queues
 * 
 * @author Maciej Pawlak
 *
 */
public class UserProcessQueuesServlet extends HttpServlet 
{
	public enum Format {
		JSON, XML
	}

	private static Logger				logger			= Logger.getLogger(UserProcessQueuesServlet.class.getName());
	private static final ObjectMapper	mapper			= new ObjectMapper();
	private static final XStream		xstream			= new XStream();
	private static final Format			DEFAULT_FORMAT	= Format.JSON;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		ProcessToolRegistry reg = (ProcessToolRegistry) getServletContext().getAttribute(ProcessToolRegistry.class.getName());
		resp.setContentType("text/plain");
		
		PrintWriter out = resp.getWriter();
		String formatString = req.getParameter("format");
		Format format;
		if (formatString == null) {
			format = DEFAULT_FORMAT;
		} else {
			format = Format.valueOf(formatString.toUpperCase());
		}
		
		String userLogin = req.getParameter("userLogin");
		if(userLogin == null)
		{
			out.write("No user login specified. Please run servlet with 'userLogin' parameter");
			return;
		}
		
		UserProcessQueuesSizeProvider userQueuesSizeProvider = new UserProcessQueuesSizeProvider(reg, userLogin);
		Map<String, Integer> userProcessQueueSize = userQueuesSizeProvider.getUserProcessQueueSize();


		switch (format) {
			case XML: {
				out.write(xstream.toXML(userProcessQueueSize));
			}
			case JSON: {
				mapper.configure(Feature.INDENT_OUTPUT, true);
				mapper.writeValue(out, userProcessQueueSize);
			}
		}

		out.close();

		logger.info(this.getClass().getSimpleName() + " GET");
	}
	


	@Override
	public void init() throws ServletException {
		super.init();
		logger.info(this.getClass().getSimpleName() + " INITIALIZED: " + getServletContext().getContextPath());
	}

	@Override
	public void destroy() {
		super.destroy();
		logger.info(this.getClass().getSimpleName() + " DESTROYED");
	}
}