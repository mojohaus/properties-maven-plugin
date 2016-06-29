package org.codehaus.mojo.properties;

import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.json.JSONObject;

/**
 * Writes properties of all active profiles to a file.
 *
 * @author <a href="mailto:zarars@gmail.com">Zarar Siddiqi</a>
 * @version $Id$
 */
@Mojo(name = "write-server-keys-as-json", defaultPhase = LifecyclePhase.NONE, threadSafe = true)
public class WriteServerKeysAsJson extends AbstractWritePropertiesMojo {
	@Parameter(defaultValue = "${settings}", readonly = true, required = true)
	Settings settings;
	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	MavenProject project;

	public void execute() throws MojoExecutionException {
		validateOutputFile();
		List<Server> list = settings.getServers();
		if (getLog().isInfoEnabled()) {
			getLog().debug(list.size() + " servers found");
		}
		JSONObject servers = new JSONObject();
		servers.put("version", WritePropertiesPluginData.version);
		for (Server server : list) {
			JSONObject o = asJSON(server);
			if (getLog().isInfoEnabled())
				getLog().debug("Got -> " + o.toString(2));
			servers.put(server.getId(), o);
		}

		writeJson(servers, getOutputFile());
	}

	private JSONObject asJSON(Server server) {
		
		// Object d = server.getConfiguration();  // Spec'd of type DOM.  Must currently be ignored because it is under-documented
		return new JSONObject().put("id", server.getId()).put("directoryPermissions", server.getDirectoryPermissions())
				.put("filePermissions", server.getFilePermissions()).put("passphrase", server.getPassphrase())
				.put("password", server.getPassword()).put("privateKey", server.getPrivateKey())
				.put("sourceLevel", server.getSourceLevel().toString()).put("username", server.getUsername());
				
	}
}
