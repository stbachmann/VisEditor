/*
 * Copyright 2014-2016 See AUTHORS file.
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

package com.kotcrab.vis.editor.util;

import com.kotcrab.vis.editor.Editor;
import com.kotcrab.vis.ui.util.OsUtils;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import java.io.File;

/**
 * @author Kotcrab
 */
public abstract class GLFWIconSetter {
	public abstract void setIcon (String path);

	public static GLFWIconSetter newInstance () {
		if (OsUtils.isWindows()) return new WinGLFWIconSetter();
		return new DefaultGLFWIconSetter();
	}

	private static class WinGLFWIconSetter extends GLFWIconSetter {
		@Override
		public void setIcon (String path) {
			try {
				HANDLE hImage = User32.INSTANCE.LoadImage(Kernel32.INSTANCE.GetModuleHandle(""),
						new File(Editor.class.getResource(path).toURI()).getAbsolutePath(),
						WinUser.IMAGE_ICON, 0, 0, WinUser.LR_LOADFROMFILE);
				User32.INSTANCE.SendMessageW(User32.INSTANCE.GetActiveWindow(), User32.WM_SETICON, new WPARAM(User32.BIG_ICON), hImage);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public interface User32 extends StdCallLibrary, WinDef {
			User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

			int WM_SETICON = 0x0080;
			int BIG_ICON = 1;

			HWND GetActiveWindow ();

			HANDLE LoadImage (HINSTANCE hinst, String name, int type, int xDesired, int yDesired, int load);

			LPARAM SendMessageW (HWND hWnd, int msg, WPARAM wParam, HANDLE lParam);

		}
	}

	private static class DefaultGLFWIconSetter extends GLFWIconSetter {
		@Override
		public void setIcon (String path) {

		}
	}
}