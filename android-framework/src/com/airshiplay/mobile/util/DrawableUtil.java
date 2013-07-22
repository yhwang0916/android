package com.airshiplay.mobile.util;

import java.io.File;
import java.io.FileFilter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer.DrawableContainerState;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;

/**
 * @author airshiplay
 * @Create 2013-7-20
 * @version 1.0
 * @since 1.0
 */
public class DrawableUtil {

	/**
	 * @param res
	 * @param bitmap
	 * @param bounds
	 * @return
	 */
	public static BitmapDrawable getBitmapDrawable(Resources res, Bitmap bitmap, Rect bounds) {
		BitmapDrawable bitmapDrawable = new BitmapDrawable(res, bitmap);
		if (bounds != null)
			bitmapDrawable.setBounds(bounds);
		else if (bitmap != null)
			bitmapDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
		return bitmapDrawable;
	}

	/**
	 * @param bitmap
	 * @return
	 */
	public static BitmapDrawable getBitmapDrawable(Bitmap bitmap) {
		return getBitmapDrawable(null, bitmap, null);
	}

	/**
	 * drawable 复制
	 * 
	 * @param drawable
	 * @return
	 */
	public static Drawable getNewDrawable(Drawable drawable) {
		return drawable.getConstantState().newDrawable();
	}

	/**
	 * 获取新的drawable,并根据resid设置其bound。
	 * 
	 * @param drawable
	 * @param res
	 * @param resId
	 * @return
	 */
	public static Drawable getNewDrawable(Drawable drawable, Resources res, int resId) {
		Drawable localDrawable = res.getDrawable(resId);
		Rect bounds = new Rect();
		localDrawable.copyBounds(bounds);
		if (localDrawable instanceof BitmapDrawable) {
			Bitmap bit = ((BitmapDrawable) localDrawable).getBitmap();
			if (bit != null && !bit.isRecycled())
				bit.recycle();
		}
		Drawable newDrawable = getNewDrawable(drawable);
		newDrawable.setBounds(bounds);
		return newDrawable;
	}

	/**
	 * 根据resid 将bitmap转换为相应类型的Drawable<br/>
	 * 如resid为Nine Patch File,则转换为{@link NinePatchDrawable}<br/>
	 * 否则转换为{@link BitmapDrawable}
	 * 
	 * @param bitmap
	 * @param res
	 * @param resId
	 *            bitmap图片 资源id
	 * @return
	 */
	public static Drawable getDrawable(Bitmap bitmap, Resources res, int resId) {
		Rect padding = new Rect();
		Rect bounds = new Rect();
		Drawable localDrawable = res.getDrawable(resId);
		localDrawable.getPadding(padding);
		localDrawable.copyBounds(bounds);
		byte[] chunk;
		Bitmap localBitmap = null;
		if (localDrawable instanceof BitmapDrawable) {
			localBitmap = ((BitmapDrawable) localDrawable).getBitmap();
		} else if (localDrawable instanceof StateListDrawable) {
			StateListDrawable stateListDrawable = (StateListDrawable) localDrawable;
			DrawableContainerState state = (DrawableContainerState) stateListDrawable.getConstantState();
			if (state.getChildCount() > 0) {
				localDrawable = state.getChildren()[0];
				if (localDrawable instanceof NinePatchDrawable)
					localBitmap = BitmapFactory.decodeResource(res, resId);
				else
					localBitmap = ((BitmapDrawable) localDrawable).getBitmap();
			}
		} else if (localDrawable instanceof NinePatchDrawable) {
			localBitmap = BitmapFactory.decodeResource(res, resId);
		} else {
			localBitmap = BitmapFactory.decodeResource(res, resId);
		}
		Drawable drawable = null;
		if (localBitmap != null) {
			chunk = localBitmap.getNinePatchChunk();
			if (localBitmap != null && !localBitmap.isRecycled())
				localBitmap.recycle();
			if (chunk != null && NinePatch.isNinePatchChunk(chunk)) {
				drawable = new NinePatchDrawable(res, bitmap, chunk, padding, null);
			} else {
				drawable = getBitmapDrawable(res, bitmap, bounds);
			}
		} else
			drawable = getBitmapDrawable(res, bitmap, bounds);
		return drawable;
	}

	/**
	 * path 路径下只存在一个prefix图片时返回{@link BitmapDrawable}；存在多张图片时返回
	 * {@link StateListDrawable} 。其目录下存放的 图片命名规则为
	 * <p>
	 * 1、当图片存在多种状态时采用 {prefix}_{state}.png
	 * </p>
	 * <p>
	 * 2、当只存在一种状态时 命名规则为{prefix}.png
	 * </p>
	 * 实例如下：
	 * 
	 * <pre>
	 * 按下状态图片:<b>{prefix}_pressed.png</b>
	 * 选中状态图片:<b>{prefix}_checked.png</b>
	 * 选择状态图片:<b>{prefix}_selected.png</b>
	 * 默认状态图片:<b>{prefix}_normal.png</b>
	 * </pre>
	 * 
	 * @param path
	 *            图片存放文件夹
	 * @param prefix
	 *            图片文件名前缀
	 * @return
	 */
	public static Drawable getDrawable(String path, final String prefix) {
		File[] files = new File(path).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String fileName = pathname.getName();
				return fileName.startsWith(prefix) && (fileName.endsWith(".png") || fileName.endsWith(".PNG"));
			}
		});
		if (files == null || files.length == 0)
			return null;
		if (files.length == 1)
			return getBitmapDrawable(BitmapUtil.getBitmap(files[0].getAbsolutePath()));
		StateListDrawable stateListDrawable = new StateListDrawable();
		for (int i = 0; i < files.length; i++) {
			BitmapDrawable bitmapDrawable = getBitmapDrawable(BitmapUtil.getBitmap(files[i].getAbsolutePath()));
			String fileName = files[i].getName();
			if (fileName.contains("pressed")) {
				stateListDrawable.addState(new int[] { android.R.attr.state_pressed }, bitmapDrawable);
			} else if (fileName.contains("checked")) {
				stateListDrawable.addState(new int[] { android.R.attr.state_checked }, bitmapDrawable);
			} else if (fileName.contains("selected")) {
				stateListDrawable.addState(new int[] { android.R.attr.state_selected }, bitmapDrawable);
			} else if (fileName.contains("expanded")) {
				stateListDrawable.addState(new int[] { android.R.attr.state_expanded }, bitmapDrawable);
			} else if (fileName.contains("enabled")) {
				stateListDrawable.addState(new int[] { android.R.attr.state_enabled }, bitmapDrawable);
			} else if (fileName.contains("disabled")) {
				stateListDrawable.addState(new int[] { -android.R.attr.state_enabled }, bitmapDrawable);
			} else if (fileName.contains("normal")) {
				stateListDrawable.addState(new int[] {}, bitmapDrawable);
			} else {
				stateListDrawable.addState(new int[] {}, bitmapDrawable);
			}
		}
		return stateListDrawable;
	}

	/**
	 * 支持Nine Patch File ;其他参考{@link DrawableUtil#getDrawable(String, String)}
	 * 
	 * @param path
	 *            图片存放文件夹
	 * @param prefix
	 *            图片文件名前缀
	 * @param res
	 *            参考 {@link android.content.res.Resources}
	 * @param resId
	 *            本地图片资源id,判断是否为Nine Patch File,必须是单状态图片
	 * @return
	 */
	public static Drawable getDrawable(String path, final String prefix, Resources res, int resId) {
		File[] files = new File(path).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String fileName = pathname.getName();
				return fileName.startsWith(prefix) && (fileName.endsWith(".png") || fileName.endsWith(".PNG"));
			}
		});
		if (files == null || files.length == 0)
			return null;
		// 获取本地drawable padding和bound
		Rect padding = new Rect();
		Rect bounds = new Rect();
		Drawable localDrawable = res.getDrawable(resId);
		localDrawable.getPadding(padding);
		localDrawable.copyBounds(bounds);
		// 判断程序本地文件是否为Nine Patch File
		Bitmap localBitmap = null;
		byte[] chunk = null;
		boolean isNineDrawable = false;
		if (localDrawable instanceof NinePatchDrawable) {
			localBitmap = BitmapFactory.decodeResource(res, resId);
			chunk = localBitmap.getNinePatchChunk();
		} else if (localDrawable instanceof StateListDrawable) {
			throw new RuntimeException("resId 必须是 PNG文件，不能是xml文件等复合状态资源");
		}
		if (chunk != null && NinePatch.isNinePatchChunk(chunk))
			isNineDrawable = true;
		// 待增加确认是否释放localbitmap
		if (files.length == 1) {
			Bitmap bitmap = BitmapUtil.getBitmap(files[0].getAbsolutePath(), res.getDisplayMetrics().density);
			if (isNineDrawable) {
				NinePatchDrawable drawable = new NinePatchDrawable(res, bitmap, chunk, padding, files[0].getAbsolutePath());
				drawable.setBounds(bounds);
				return drawable;
			}
			return getBitmapDrawable(res, bitmap, bounds);
		}
		StateListDrawable stateListDrawable = new StateListDrawable();
		for (int i = 0; i < files.length; i++) {
			Bitmap bitmap = BitmapUtil.getBitmap(files[i].getAbsolutePath(), res.getDisplayMetrics().density);
			Drawable drawable;
			if (isNineDrawable) {
				drawable = new NinePatchDrawable(res, bitmap, chunk, padding, files[0].getAbsolutePath());
				drawable.setBounds(bounds);
			} else
				drawable = getBitmapDrawable(res, bitmap, bounds);
			String fileName = files[i].getName();
			if (fileName.contains("pressed")) {
				stateListDrawable.addState(new int[] { android.R.attr.state_pressed }, drawable);
			} else if (fileName.contains("checked")) {
				stateListDrawable.addState(new int[] { android.R.attr.state_checked }, drawable);
			} else if (fileName.contains("selected")) {
				stateListDrawable.addState(new int[] { android.R.attr.state_selected }, drawable);
			} else if (fileName.contains("expanded")) {
				stateListDrawable.addState(new int[] { android.R.attr.state_expanded }, drawable);
			} else if (fileName.contains("enabled")) {
				stateListDrawable.addState(new int[] { android.R.attr.state_enabled }, drawable);
			} else if (fileName.contains("disabled")) {
				stateListDrawable.addState(new int[] { -android.R.attr.state_enabled }, drawable);
			} else if (fileName.contains("normal")) {
				stateListDrawable.addState(new int[] {}, drawable);
			} else {
				stateListDrawable.addState(new int[] {}, drawable);
			}
		}
		return stateListDrawable;
	}
}
