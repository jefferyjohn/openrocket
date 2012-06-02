package net.sf.openrocket.android;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.database.ComponentPresetDatabase;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.file.openrocket.OpenRocketSaver;
import net.sf.openrocket.l10n.DebugTranslator;
import net.sf.openrocket.l10n.ResourceBundleTranslator;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.Rocket;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.preference.PreferenceManager;

public class Application extends android.app.Application {

	private OpenRocketDocument rocketDocument;
	private Uri fileUri;

	private WarningSet warnings;

	// Big B boolean so I can synchronize on it.
	private static Boolean initialized = false;

	public void initialize() {
		synchronized (initialized) {
			if ( initialized == true ) {
				return;
			}

			// Android does not have a default sax parser set.  This needs to be defined first.
			System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");

			net.sf.openrocket.startup.Application.setLogger( new AndroidLogWrapper.LogHelper() );

			net.sf.openrocket.startup.Application.setPreferences( new PreferencesAdapter() );

			net.sf.openrocket.startup.Application.setComponentPresetDao( new ComponentPresetDatabase() );

			MotorDatabaseAdapter db = new MotorDatabaseAdapter(this);

			net.sf.openrocket.startup.Application.setMotorSetDatabase(db);

			Translator t;
			t = new ResourceBundleTranslator("l10n.messages");
			if (Locale.getDefault().getLanguage().equals("xx")) {
				t = new DebugTranslator(t);
			}

			net.sf.openrocket.startup.Application.setBaseTranslator(t);

			initialized = true;
		}
	}

	public Application() {
	}

	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		initialize();
		boolean isDebuggable = (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
		AndroidLogWrapper.setLogEnabled(isDebuggable);
		PreferencesActivity.initializePreferences(this, PreferenceManager.getDefaultSharedPreferences(this));
	}

	private RocketChangedEventHandler handler;
	
	public void setHandler( RocketChangedEventHandler handler ) {
		this.handler = handler;
	}
	
	/**
	 * @return the rocketDocument
	 */
	public OpenRocketDocument getRocketDocument() {
		return rocketDocument;
	}

	public void addNewSimulation() {
		Rocket rocket = rocketDocument.getRocket();
		Simulation newSim = new Simulation(rocket);
		newSim.setName(rocketDocument.getNextSimulationName());
		rocketDocument.addSimulation(newSim);
		if ( handler != null ) {
			handler.simsChangedMessage();
		}
	}
	
	public void deleteSimulation( int simulationPos ) {
		rocketDocument.removeSimulation( simulationPos );
		if ( handler != null ) {
			handler.simsChangedMessage();
		}
	}
	
	public String addNewMotorConfig() {
		String configId = rocketDocument.getRocket().newMotorConfigurationID();
		if ( handler != null ) {
			handler.configsChangedMessage();
		}
		return configId;
	}
	/**
	 * @param rocketDocument the rocketDocument to set
	 */
	public void setRocketDocument(OpenRocketDocument rocketDocument) {
		this.rocketDocument = rocketDocument;
	}

	public WarningSet getWarnings() {
		return warnings;
	}

	public void setWarnings(WarningSet warnings) {
		this.warnings = warnings;
	}

	public Uri getFileUri() {
		return fileUri;
	}

	public void setFileUri(Uri fileUri) {
		this.fileUri = fileUri;
	}

	public void saveOpenRocketDocument() throws IOException {
		OpenRocketSaver saver = new OpenRocketSaver();
		saver.save(new File(fileUri.getPath()),rocketDocument);

	}
}
