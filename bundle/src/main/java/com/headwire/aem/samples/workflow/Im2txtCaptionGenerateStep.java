package com.headwire.aem.samples.workflow;

import com.day.cq.dam.api.Asset;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.headwire.aem.samples.config.Im2txtConfigService;
import com.headwire.aem.samples.util.RequestHelper;
import org.apache.felix.scr.annotations.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Workflow to generate the image captions
 */
@Component(label = "Im2txt Image Caption Generate Step", metatype = false, immediate = true)
@Properties({
		@Property(name = Constants.SERVICE_DESCRIPTION, value = "Im2txt Generate Image Captions Step"),
		@Property(name = Constants.SERVICE_VENDOR, value = "Headwire Inc."),
		@Property(name = "process.label", value = "Im2txt Image Caption Generate Step") })
@Service(value = WorkflowProcess.class)
public class Im2txtCaptionGenerateStep implements WorkflowProcess {

	private static final Logger LOG = LoggerFactory.getLogger(Im2txtCaptionGenerateStep.class);

	@Reference
	private ResourceResolverFactory resolverFactory;

	@Reference
	private Im2txtConfigService im2txtConfigService;

	private static final String IMG_FORMAT = "JPG";

	public void execute(WorkItem workItem, WorkflowSession wfSession, MetaDataMap args) throws WorkflowException {

		try {

			final Map<String, Object> map = new HashMap<String, Object>();
			map.put("user.jcr.session", wfSession.getSession());

			final Asset asset = RequestHelper.getAssetFromPayload(resolverFactory, workItem, wfSession.getSession());
			String[] captions = generateCaption(asset);

			//Add Captions to JCR Node
			addToAssetMetadata(asset, wfSession.getSession(), "dam:captions", captions);

		} catch (Exception e) {

			LOG.error("Something wrong happened with exception", e);

		}
	}

	/**
	 *
	 * @param asset
	 * @param session
	 * @param captionKey
	 * @param values
	 *
	 * It will add to Asset Metadata if it is not already added.
	 */
	private void addToAssetMetadata(Asset asset, Session session, String captionKey, String[] values) {

		try {

			// Target Node
			Node assetNode = asset.adaptTo(Node.class);
			Node targetNode = assetNode.getNode("jcr:content/metadata");

			if(targetNode.hasProperty(captionKey)){
				//No Action Needed
				return;
			}

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

		String imageProcessEndpoint = im2txtConfigService.getIm2txtURL() + im2txtConfigService.getIm2txtProcessImage();
		HttpPost post = new HttpPost(imageProcessEndpoint);

		InputStream is = asset.getOriginal().getStream();
		File f = RequestHelper.createTempFileFromStream(is, asset.getName(), IMG_FORMAT);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addBinaryBody("file", f, ContentType.DEFAULT_BINARY, asset.getName());
		HttpEntity entity = builder.build();
		post.setEntity(entity);

		String responseString = RequestHelper.postHttpClient(post);
		responseString = responseString.replaceAll("\"", "");
		String[] arr = responseString.split(",");

		LOG.info("Generate Caption received : [{}]", responseString);

		return arr;

	}

}
