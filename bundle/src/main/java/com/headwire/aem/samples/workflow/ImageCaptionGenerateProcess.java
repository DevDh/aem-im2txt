package com.headwire.aem.samples.workflow;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.headwire.aem.samples.config.Im2txtConfigService;
import org.apache.felix.scr.annotations.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.jackrabbit.oak.commons.IOUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Workflow to generate the image captions
 */
@Component(label = "Image Caption Generate Step", metatype = false, immediate = true)
@Properties({
		@Property(name = Constants.SERVICE_DESCRIPTION, value = "Generate Image Captions Workflow"),
		@Property(name = Constants.SERVICE_VENDOR, value = "Headwire Inc."),
		@Property(name = "process.label", value = "Image Caption Generate Step") })
@Service(value = WorkflowProcess.class)
public class ImageCaptionGenerateProcess implements WorkflowProcess {

	private static final Logger LOG = LoggerFactory.getLogger(ImageCaptionGenerateProcess.class);

	@Reference
	private ResourceResolverFactory resolverFactory;

	@Reference
	private Im2txtConfigService im2txtConfigService;

	public void execute(WorkItem workItem, WorkflowSession wfSession, MetaDataMap args) throws WorkflowException {

		try {

			final Map<String, Object> map = new HashMap<String, Object>();
			map.put("user.jcr.session", wfSession.getSession());

			final Asset asset = getAssetFromPayload(workItem, wfSession.getSession());
			String[] captions = generateCaption(asset);

			//Add Captions to JCR Node
			addToAssetMetadata(asset, wfSession.getSession(), "captions", captions);

		} catch (Exception e) {

			LOG.error("Something wrong happened with exception", e);

		}
	}

	private void addToAssetMetadata(Asset asset, Session session, String captionKey, String[] values) {

		try {

			// Target Node
			Node assetNode = asset.adaptTo(Node.class);
			Node targetNode = assetNode.getNode("jcr:content/metadata");
			targetNode.setProperty(captionKey, values);

			// Save the session changes and log out
			session.save();
			session.logout();

		} catch (RepositoryException e) {

			LOG.error("Something wrong happened with RepositoryException", e);
		}

	}

	/**
	 *
	 * @param asset
	 * @return the generated captions returned by the im2txt service
	 */
	public String[] generateCaption(Asset asset) {

		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost(im2txtConfigService.getIm2txtURL());

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

		InputStream is = asset.getOriginal().getStream();
		File f = createTempFileFromStream(is, asset.getName());

		builder.addBinaryBody("file", f, ContentType.DEFAULT_BINARY, asset.getName());
		HttpEntity entity = builder.build();
		post.setEntity(entity);

		String responseString = "";
		String[] arr = null;

		try {

			HttpResponse response = client.execute(post);
			HttpEntity resEntity = response.getEntity();
			responseString = EntityUtils.toString(resEntity, "UTF-8");
			responseString = responseString.replaceAll("\"", "");
			arr = responseString.split(",");

			LOG.info("Generate Caption received : [{}]", responseString);

		} catch (IOException e) {
			LOG.error("Generate Caption Failed with exception", e);
		}

		return arr;

	}

	private File createTempFileFromStream(InputStream is, String name) {

		File tmpDir = null;
		FileOutputStream fos = null;
		File tmpFile = null;

		try {
			// creating temp directory
			tmpDir = createTempDir(null);
			// streaming file to temp directory
			tmpFile = new File(tmpDir, name.replace(' ', '_'));
			fos = new FileOutputStream(tmpFile);
			IOUtils.copy(is, fos);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return tmpFile;
	}

	protected final File createTempDir(File parentDir) throws IOException {
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

	private Asset getAssetFromPayload(WorkItem item, Session session) {
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


}
