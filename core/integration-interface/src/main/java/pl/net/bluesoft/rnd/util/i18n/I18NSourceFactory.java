package pl.net.bluesoft.rnd.util.i18n;

import pl.net.bluesoft.rnd.util.i18n.impl.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2012-07-27
 * Time: 13:51
 */
public class I18NSourceFactory {
	private static final Map<Locale, I18NSource> i18NSources = new HashMap<Locale, I18NSource>();

	public static synchronized I18NSource createI18NSource(Locale locale) {
		I18NSource i18NSource = i18NSources.get(locale);
		if (i18NSource == null) {
			i18NSource = new ThreadSafeCachingI18NSource(
					new DefaultI18NSource(locale));
			i18NSources.put(locale, i18NSource);
		}
		return new CachingI18NSource(i18NSource);
	}

	public static synchronized void invalidateCache() {
		i18NSources.clear();
	}
}
