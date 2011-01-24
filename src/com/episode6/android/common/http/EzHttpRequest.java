package com.episode6.android.common.http;

import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;

import com.episode6.android.common.util.Base64;
import com.episode6.android.common.util.DataUtils;

public class EzHttpRequest implements DataUtils.ProgressListener {
	
	public static final int REQ_GET = 1;
	public static final int REQ_PUT = 2;
	public static final int REQ_DEL = 3;
	public static final int REQ_HEAD = 4;
	public static final int REQ_POST = 5;
	public static final int REQ_POST_MULTIPART = 6;
	public static final int REQ_POST_STRING_ENT = 7;
	
	private static final String VAL_LAST_MOD_HEADER = "Last-Modified";
	
	private static final String VAL_AUTHORIZATION = "Authorization";
	private static final String VAL_BASIC = "Basic ";
	
	
	//post file string constants
	private static final String VAL_HTTP_POST = "POST";
	private static final String VAL_LINE_END = "\r\n";
	private static final String VAL_TWO_HYPHENS = "--";
	private static final String VAL_BOUNDRY = "***MIMEFileUpload***";
	private static final String VAL_FILE_CONTENT_DISPOSITION_FORMATER = "Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"\r\n";
	private static final String VAL_PARAM_CONTENT_DISPOSITION_FORMATER = "Content-Disposition: form-data; name=\"%s\"\r\n";
	private static final String VAL_POST_SEPERATOR = VAL_TWO_HYPHENS + VAL_BOUNDRY + VAL_LINE_END;
	private static final String VAL_POST_CLOSE = VAL_TWO_HYPHENS + VAL_BOUNDRY + VAL_TWO_HYPHENS + VAL_LINE_END;
	private static final String VAL_FILE_POST_CONTENT_TYPE = "multipart/form-data;boundary=" + VAL_BOUNDRY;

	
	public interface EzHttpRequestListener {
		public void onHttpRequestSucceeded(EzHttpResponse response);
		public void onHttpRequestFailed(EzHttpResponse response);
	}
	
	public interface EzHttpRequestProgressListener {
		public void onHttpProgressUpdated(EzHttpRequest request, int percentComplete, int currentFile, int totalFiles);
	}
	
	public interface EzHttpPostUploadEntity {
		public String getParamName();
		public String getPostFileName();
		public InputStream getInputStream();
	}
	
	public static class RequestFactory {
		public static EzHttpRequest createGetRequest(Context c, String url, boolean isRaw, int requestCode) {
			EzHttpRequest req = new EzHttpRequest(c, url, REQ_GET, isRaw, requestCode);
			return req;
		}
		
		public static EzHttpRequest createPutRequest(Context c, String url, boolean isRaw, int requestCode) {
			EzHttpRequest req = new EzHttpRequest(c, url, REQ_PUT, isRaw, requestCode);
			return req;
		}

		public static EzHttpRequest createDeleteRequest(Context c, String url, boolean isRaw, int requestCode) {
			EzHttpRequest req = new EzHttpRequest(c, url, REQ_DEL, isRaw, requestCode);
			return req;
		}
		
		public static EzHttpRequest createHeadRequest(Context c, String url, boolean isRaw, int requestCode) {
			EzHttpRequest req = new EzHttpRequest(c, url, REQ_HEAD, isRaw, requestCode);
			return req;
		}
		
		public static EzHttpRequest createPostRequest(Context c, String url, boolean isRaw, int requestCode) {
			EzHttpRequest req = new EzHttpRequest(c, url, REQ_POST, isRaw, requestCode);
			return req;
		}
		
		public static EzHttpRequest createMultipartPostRequest(Context c, String url, boolean isRaw, int requestCode) {
			EzHttpRequest req = new EzHttpRequest(c, url, REQ_POST_MULTIPART, isRaw, requestCode);
			req.addHeader(HTTP.CONTENT_TYPE, VAL_FILE_POST_CONTENT_TYPE);
			req.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
			return req;
		}

		public static EzHttpRequest createPostStringEntityRequest(Context c, String url, boolean isRaw, int requestCode, String entity, String entityType, String entityEncoding) {
			EzHttpRequest req = new EzHttpRequest(c, url, REQ_POST_STRING_ENT, isRaw, requestCode);
			req.setStringEntity(entity, entityType, entityEncoding);
			return req;
		}
	}
	
	public static final int DEFAULT_TIMEOUT_SECS = 30;
	
	private static final String TMP_FILE_PREFIX = "ez_http_response";
	
	private Context mContext;
	private String mUrl;
	private int mReqType;
	private int mTimeoutSecs;
	private boolean mIsRaw;
	
	private String mStringEntity;
	private String mStringEntityType;
	private String mStringEntityEncoding;

	private int mRequestCode;
	private Object mTag;
	
	private ArrayList<NameValuePair> mPostParams;
	private ArrayList<EzHttpPostUploadEntity> mPostFiles;
	private HashMap<String, String> mHeaders;
	
	private EzHttpRequestListener mFinishedListener;
	private EzHttpRequestProgressListener mProgressListener;
	
	
	private int mTotalBytes;
	private int mCurrentFile;
	
	private EzHttpRequest(Context c, String url, int reqType, boolean isRaw, int requestCode) {
		mUrl = url;
		mReqType = reqType;
		mIsRaw = isRaw;
		mTimeoutSecs = DEFAULT_TIMEOUT_SECS;
		
		mStringEntity = null;
		mStringEntityType = null;
		mStringEntityEncoding = null;
		
		mRequestCode = requestCode;
		mTag = null;

		mHeaders = null;
		mPostParams = null;
		mPostFiles = null;
		
		mFinishedListener = null;
		mProgressListener = null;
		
		mTotalBytes = 1;
		mCurrentFile = 0;
	}
	
	
	
	public void setContext(Context c) {
		mContext = c;
	}
	public void setUrl(String url) {
		mUrl = url;
	}
	public void setRequestType(int requestType) {
		mReqType = requestType;
	}
	public void setTimeoutSecs(int timeoutSecs) {
		mTimeoutSecs = timeoutSecs;
	}
	public void setIsRaw(boolean isRaw) {
		mIsRaw = isRaw;
	}
	
	public Context getContext() {
		return mContext;
	}
	public String getUrl() {
		return mUrl;
	}
	public int getRequestType() {
		return mReqType;
	}
	public int getTimeoutSecs() {
		return mTimeoutSecs;
	}
	public boolean isRaw() {
		return mIsRaw;
	}
	
	
	public void setStringEntity(String entity, String type, String encoding) {
		mStringEntity = entity;
		mStringEntityType = type;
		mStringEntityEncoding = encoding;
	}
	
	public String getStringEntity() {
		return mStringEntity;
	}
	public String getStringEntityType() {
		return mStringEntityType;
	}
	public String getStringEntityEncoding() {
		return mStringEntityEncoding;
	}
	
	
	public void setHeaders(HashMap<String, String> headers) {
		mHeaders = headers;
	}
	public void setPostParams(ArrayList<NameValuePair> postParams) {
		mPostParams = postParams;
	}
	public void setPostFiles(ArrayList<EzHttpPostUploadEntity> postFiles) {
		mPostFiles = postFiles;
	}
	
	public HashMap<String, String> getHeaders() {
		return mHeaders;
	}
	public ArrayList<NameValuePair> getPostParams() {
		return mPostParams;
	}
	public ArrayList<EzHttpPostUploadEntity> getPostFiles() {
		return mPostFiles;
	}
	
	public void addHeader(String name, String value) {
		if (mHeaders == null) {
			mHeaders = new HashMap<String, String>();
		}
		mHeaders.put(name, value);
	}
	public void addPostParam(String name, String value) {
		if (mPostParams == null) {
			mPostParams = new ArrayList<NameValuePair>();
		}
		mPostParams.add(new BasicNameValuePair(name, value));
	}
	public void addPostFile(EzHttpPostUploadEntity postFile) {
		if (mPostFiles == null) {
			mPostFiles = new ArrayList<EzHttpRequest.EzHttpPostUploadEntity>();
		}
		mPostFiles.add(postFile);
	}
	public void addBasicAuth(String username, String password) {
		String auth = Base64.encodeString(username + ":" + password);
		addHeader(VAL_AUTHORIZATION, VAL_BASIC + auth);
	}

	
	
	
	public void setRequestCode(int requestCode) {
		mRequestCode = requestCode;
	}
	public void setTag(Object tag) {
		mTag = tag;
	}
	
	public int getRequestCode() {
		return mRequestCode;
	}
	public Object getTag() {
		return mTag;
	}
	
	
	public EzHttpRequestListener getRequestFinishedListener() {
		return mFinishedListener;
	}
	
	
	
	
	public void setFinishedListened(EzHttpRequestListener listener) {
		mFinishedListener = listener;
	}
	public void setProgressListener(EzHttpRequestProgressListener listener) {
		mProgressListener = listener;
	}
	
	
	
	
	
	public EzHttpResponse execute() {
		if (mReqType == REQ_POST_MULTIPART)
			return executeMultipartPostRequest();
		
		EzHttpResponse ezResponse = new EzHttpResponse(this);
		
		HttpParams connParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(connParams, mTimeoutSecs*1000);
		HttpConnectionParams.setSoTimeout(connParams, mTimeoutSecs*1000);
		HttpUriRequest message = null;
		
		try {
			switch(mReqType) {
				case REQ_GET: {
					message = new HttpGet(mUrl);
					break;
				}
				case REQ_PUT: {
					message = new HttpPost(mUrl);
					break;
				}
				case REQ_DEL: {
					message = new HttpDelete(mUrl);
					break;
				}
				case REQ_HEAD: {
					message = new HttpHead(mUrl);
					break;
				}
				case REQ_POST: {
					message = new HttpPost(mUrl);
					if (mPostParams != null)
						((HttpPost)message).setEntity(new UrlEncodedFormEntity(mPostParams, HTTP.UTF_8));
					break;
				}
				case REQ_POST_STRING_ENT: {
					message = new HttpPost(mUrl);
					StringEntity ent = new StringEntity(mStringEntity, mStringEntityEncoding);
					ent.setContentType(mStringEntityType);
					((HttpPost)message).setEntity(ent);
					break;
				}
			}
			
			
			if (mHeaders != null) {
				for (String headerName : mHeaders.keySet()) {
					message.addHeader(headerName, mHeaders.get(headerName));
				}
			}
			
			HttpClient client = new DefaultHttpClient(connParams);
			HttpResponse response = client.execute(message);
			HttpEntity entity = response.getEntity();
			
			ezResponse.mResponseCode = response.getStatusLine().getStatusCode();
			ezResponse.mResponseReasonPhrase = response.getStatusLine().getReasonPhrase();
			ezResponse.mResponseContentLength = entity.getContentLength();
			if (entity.getContentEncoding() != null)
				ezResponse.mResponseContentEncoding = entity.getContentEncoding().getValue();
			if (entity.getContentType() != null)
				ezResponse.mResponseContentType = entity.getContentType().getValue();
			mTotalBytes = Math.max((int)ezResponse.mResponseContentLength, 1);


			
			if (ezResponse.mResponseCode != 200 && ezResponse.mResponseCode != 206) {
				
				//REQUEST FAILED
				ezResponse.mSuccess = false;
				ezResponse.mResponseText = DataUtils.getTextFromStream(entity.getContent(), true, null);
				
			} else {
				
				//REQUEST SUCCEEDED
				ezResponse.mSuccess = true;
				if (mIsRaw) {
					File tmpFile = File.createTempFile(TMP_FILE_PREFIX, null, mContext.getCacheDir());
					DataUtils.copyInputStreamToFile(entity.getContent(), tmpFile, DataUtils.DEFAULT_BUFFER_SIZE, true, true, this);
					ezResponse.mResponseFile = tmpFile;
				} else {
					ezResponse.mResponseText = DataUtils.getTextFromStream(entity.getContent(), true, null);
				}
				
				Header lastMod = response.getFirstHeader(VAL_LAST_MOD_HEADER);
				if (lastMod != null) {
					try {
						ezResponse.mResponseLastModTime = DateUtils.parseDate(lastMod.getValue()).getTime();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			ezResponse.mSuccess = false;
			ezResponse.mResponseCode = -1;
			ezResponse.mResponseReasonPhrase = e.getMessage();
		}
		
		return ezResponse;
	}
	
	private EzHttpResponse executeMultipartPostRequest() {
		EzHttpResponse ezResponse = new EzHttpResponse(this);
		
		try {
			//setup connection
			HttpURLConnection conn = (HttpURLConnection)(new URL(mUrl).openConnection());
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setConnectTimeout(mTimeoutSecs*1000);
			conn.setRequestMethod(VAL_HTTP_POST);
			
			if (mHeaders != null) {
				for (String headerName : mHeaders.keySet()) {
					conn.setRequestProperty(headerName, mHeaders.get(headerName));
				}
			}
			
			boolean hasFiles = mPostFiles != null && mPostFiles.size() > 0;
			
			//start output
			DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
			outputStream.writeBytes(VAL_POST_SEPERATOR);
			
			if (mPostParams != null) {
				for (int i = 0; i<mPostParams.size(); i++) {
					NameValuePair param = mPostParams.get(i);
					outputStream.writeBytes(String.format(VAL_PARAM_CONTENT_DISPOSITION_FORMATER, param.getName()));
					outputStream.writeBytes(param.getValue());
					outputStream.writeBytes(VAL_LINE_END);
					
					if (hasFiles || i < mPostParams.size()-1) {
						outputStream.writeBytes(VAL_POST_SEPERATOR);
					}
				}
			}
			
			if (hasFiles) {
				for (int i = 0; i<mPostFiles.size(); i++) {
					EzHttpPostUploadEntity uploadFile = mPostFiles.get(i);
					mCurrentFile = i;
					
					outputStream.writeBytes(String.format(VAL_FILE_CONTENT_DISPOSITION_FORMATER, uploadFile.getParamName(), uploadFile.getPostFileName());
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			ezResponse.mSuccess = false;
			ezResponse.mResponseCode = -1;
			ezResponse.mResponseReasonPhrase = e.getMessage();
		}
		
		return ezResponse;
	}
	
	
	
	@Override
	public void onProgressUpdate(int totalBytesRead) {
		if (mProgressListener != null) {
			if (mReqType == REQ_POST_MULTIPART && mPostFiles != null)
				mProgressListener.onHttpProgressUpdated(this, (int)(((float)totalBytesRead / (float)mTotalBytes) * 100f), mCurrentFile, mPostFiles.size());
			else 
				mProgressListener.onHttpProgressUpdated(this, (int)(((float)totalBytesRead / (float)mTotalBytes) * 100f), 0, 1);
		}
	}
	
	
	
	public static class EzHttpResponse {
		private EzHttpRequest mRequest;
		private boolean mSuccess;
		private int mResponseCode;
		private String mResponseReasonPhrase;
		private String mResponseContentType;
		private String mResponseContentEncoding;
		private long mResponseLastModTime;
		private long mResponseContentLength;
		private String mResponseText;
		private File mResponseFile;

		
		private EzHttpResponse(EzHttpRequest request) {
			mRequest = request;
			mSuccess = false;
			mResponseCode = 0;
			mResponseReasonPhrase = null;
			mResponseContentType = null;
			mResponseContentEncoding = null;
			mResponseLastModTime = 0;
			mResponseContentLength = 0;
			mResponseText = null;
			mResponseFile = null;
		}
		
		public EzHttpRequest getRequest() {
			return mRequest;
		}
		public boolean wasSuccess() {
			return mSuccess;
		}
		public int getResponseCode() {
			return mResponseCode;
		}
		public String getResponseReasonPhrase() {
			return mResponseReasonPhrase;
		}
		public String getResponseContentType() {
			return mResponseContentType;
		}
		public String getResponseContentEncoding() {
			return mResponseContentEncoding;
		}
		public long getResponseLastModTime() {
			return mResponseLastModTime;
		}
		public long getResponseContentLength() {
			return mResponseContentLength;
		}
		public String getResponseText() {
			return mResponseText;
		}
		public File getResponseFile() {
			return mResponseFile;
		}
		public boolean isRaw() {
			return mRequest.isRaw();
		}
		public void deleteRawFile() {
			if (wasSuccess() && isRaw() && mResponseFile != null) {
				mResponseFile.delete();
			}
		}
		public int getRequestCode() {
			return mRequest.getRequestCode();
		}
		public Object getTag() {
			return mRequest.getTag();
		}
	}










	
	

}
