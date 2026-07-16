package com.simplebank.bank.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

/**
 * Opens the frontend after a local IntelliJ run is ready to receive requests.
 *
 * The shared IntelliJ run configuration enables this component. It stays disabled for tests,
 * packaged deployments, and other environments unless app.browser.open-on-startup is explicitly true.
 */
@Component
@ConditionalOnProperty(name = "app.browser.open-on-startup", havingValue = "true")
public class BrowserLauncher {

	private static final Logger LOGGER = LoggerFactory.getLogger(BrowserLauncher.class);
	private static final String OPENED_MARKER = "simplebank.browser.opened";

	private final int serverPort;

	public BrowserLauncher(@Value("${server.port:8080}") int serverPort) {
		this.serverPort = serverPort;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void openFrontend() {
		// DevTools can restart the Spring context; this JVM marker prevents opening duplicate tabs.
		if (Boolean.getBoolean(OPENED_MARKER)) {
			return;
		}

		URI frontendUri = URI.create("http://localhost:" + serverPort + "/");
		try {
			if (openBrowser(frontendUri)) {
				System.setProperty(OPENED_MARKER, Boolean.TRUE.toString());
				LOGGER.info("Opened the Simple Bank frontend at {}", frontendUri);
			} else {
				LOGGER.warn("The frontend is ready at {}, but this environment cannot open a browser.", frontendUri);
			}
		} catch (IOException | RuntimeException exception) {
			LOGGER.warn("The frontend is ready at {}, but the browser could not be opened.", frontendUri, exception);
		}
	}

	private boolean openBrowser(URI frontendUri) throws IOException {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			Desktop.getDesktop().browse(frontendUri);
			return true;
		}

		// Some Windows launchers report Desktop as unsupported even though the URL handler is available.
		if (System.getProperty("os.name", "").startsWith("Windows")) {
			new ProcessBuilder(
					"rundll32.exe",
					"url.dll,FileProtocolHandler",
					frontendUri.toString()
			).start();
			return true;
		}

		return false;
	}
}
