package com.headwire.aem.samples.util;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.workflow.exec.WorkItem;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.jcr.Session;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by puneetdhanda on 1/17/17.
 */
public class RequestHelper {

	private static final Logger LOG = LoggerFactory.getLogger(RequestHelper.class);

	public static String postHttpClient(HttpPost post) {

		CloseableHttpClient client = HttpClients.createDefault();

		String responseString = "";

		try {

			HttpResponse response = client.execute(post);
			HttpEntity resEntity = response.getEntity();
			responseString = EntityUtils.toString(resEntity, "UTF-8");
			responseString = responseString.replaceAll("\"", "");

			LOG.info("Http Response : [{}]", responseString);

		} catch (IOException e) {

			LOG.error("Some exception occured !!!", e);
		}

		return responseString;
	}

	public static Asset getAssetFromPayload(ResourceResolverFactory resolverFactory, WorkItem item, Session session) {
		Asset asset = null;
		Resource resource = null;

		if (item.getWorkflowData().getPayloadType().equals("JCR_PATH")) {

			String path = item.getWorkflowData().getPayload().toString();
			final Map<String, Object> map = new HashMap<String, Object>();
			map.put("user.jcr.session", session);

			try {

				ResourceResolver rr = resolverFactory.getResourceResolver(map);
				resource = rr.getResource(path);

			} catch (LoginException e) {
				LOG.error("Login Exception while retrieving the resource resolver.", path, item.getWorkflow().getId());
			}

			if (null != resource) {
				asset = DamUtil.resolveToAsset(resource);

			} else {
				LOG.error("getAssetFromPaylod: asset [{}] in payload of workflow [{}] does not exist.", path,
						item.getWorkflow().getId());
			}
		}

		return asset;
	}

	public static File createTempFileFromStream(InputStream is, String name, String type) {

		File tmpFile = null;

		try {

			// creating temp directory
			File tmpDir = createTempDir(null);
			// streaming file to temp directory
			tmpFile = new File(tmpDir, name.replace(' ', '_'));

			ImageInputStream iis = ImageIO.createImageInputStream(is);
			BufferedImage image = ImageIO.read(iis);
			OutputStream os = new FileOutputStream(tmpFile);
			ImageOutputStream ios = ImageIO.createImageOutputStream(os);
			ImageIO.write(image, type, ios);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return tmpFile;
	}

	public static final File createTempDir(File parentDir) throws IOException {
		File tempDir = null;
		try {

			tempDir = File.createTempFile("cqdam", null, parentDir);

			if (!tempDir.delete()) {
				throw new IOException("Unable to delete temp directory.");
			}
			if (!tempDir.mkdir()) {
				throw new IOException("Unable to create temp directory.");
			}
		} catch (IOException e) {
			LOG.warn("could not create temp directory in the [{}] with the exception", parentDir, e);
		}
		return tempDir;
	}

}
