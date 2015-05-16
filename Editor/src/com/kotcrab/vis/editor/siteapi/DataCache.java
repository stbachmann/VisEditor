/*
 * Copyright 2014-2015 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kotcrab.vis.editor.siteapi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;
import com.kotcrab.vis.editor.siteapi.APIClient.SetCallback;

public class DataCache {
	private APIClient apiClient;

	private Json json;

	private ContentSet content;
	private NewsSet news;
	private GdxReleaseSet gdx;
	private VersionSet version;

	private SetCallback<ContentSet> contentCallback;
	private SetCallback<NewsSet> newsCallback;
	private SetCallback<GdxReleaseSet> gdxCallback;
	private SetCallback<VersionSet> versionCallback;

	private FileHandle contentCacheFile;
	private FileHandle newsCacheFile;
	private FileHandle gdxCacheFile;
	private FileHandle versionCacheFile;

	private boolean refreshInProgress;

	public DataCache () {
		apiClient = new APIClient();

		FileHandle apiCache = Gdx.files.absolute(".");
		apiCache.mkdirs();

		contentCacheFile = apiCache.child("content.json");
		newsCacheFile = apiCache.child("news.json");
		gdxCacheFile = apiCache.child("gdx.json");
		versionCacheFile = apiCache.child("version.json");

		json = new Json();
	}

	public void init () {
		try {
			if (contentCacheFile.exists()) {
				content = json.fromJson(ContentSet.class, contentCacheFile);
				contentCallback.reload(content);
			}
		} catch (SerializationException e) {
			e.printStackTrace();
		}

		try {
			if (newsCacheFile.exists()) {
				news = json.fromJson(NewsSet.class, newsCacheFile);
				newsCallback.reload(news);
			}
		} catch (SerializationException e) {
			e.printStackTrace();
		}

		try {
			if (gdxCacheFile.exists()) {
				gdx = json.fromJson(GdxReleaseSet.class, gdxCacheFile);
				gdxCallback.reload(gdx);
			}
		} catch (SerializationException e) {
			e.printStackTrace();
		}

		try {
			if (versionCacheFile.exists()) {
				version = json.fromJson(VersionSet.class, versionCacheFile);
				versionCallback.reload(version);
			}
		} catch (SerializationException e) {
			e.printStackTrace();
		}

		refresh();
	}

	public void refresh () {
		if(refreshInProgress) return;

		new Thread(() -> {
			refreshInProgress = true;

			apiClient.readContent(new SetCallback<ContentSet>() {
				@Override
				public void failed (Throwable cause) {
					contentCallback.failed(cause);
				}

				@Override
				public void reload (ContentSet set) {
					content = set;
					contentCallback.reload(content);
					json.toJson(content, contentCacheFile);
				}
			});

			apiClient.readNews(new SetCallback<NewsSet>() {
				@Override
				public void failed (Throwable cause) {
					newsCallback.failed(cause);
				}

				@Override
				public void reload (NewsSet set) {
					news = set;
					newsCallback.reload(news);
					json.toJson(news, newsCacheFile);
				}
			});

			apiClient.readGdx(new SetCallback<GdxReleaseSet>() {
				@Override
				public void failed (Throwable cause) {
					gdxCallback.failed(cause);
				}

				@Override
				public void reload (GdxReleaseSet set) {
					gdx = set;
					gdxCallback.reload(gdx);
					json.toJson(gdx, gdxCacheFile);
				}
			});

			apiClient.readVersion(new SetCallback<VersionSet>() {
				@Override
				public void failed (Throwable cause) {
					versionCallback.failed(cause);
				}

				@Override
				public void reload (VersionSet set) {
					version = set;
					versionCallback.reload(version);
					json.toJson(version, versionCacheFile);
				}
			});

			refreshInProgress = false;
		}, "APIClient").start();
	}

	public void setContentCallback (SetCallback<ContentSet> contentCallback) {
		this.contentCallback = contentCallback;
	}

	public void setNewsCallback (SetCallback<NewsSet> newsCallback) {
		this.newsCallback = newsCallback;
	}

	public void setGdxCallback (SetCallback<GdxReleaseSet> gdxCallback) {
		this.gdxCallback = gdxCallback;
	}

	public void setVersionCallback (SetCallback<VersionSet> versionCallback) {
		this.versionCallback = versionCallback;
	}
}