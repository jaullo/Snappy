/*
 * The MIT License (MIT)
 * 
 * Copyright � 2013 Clover Studio Ltd. All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.snappy.lazy;

import android.content.Context;
import android.graphics.Typeface;

/**
 * SetFontFace
 * 
 * Creates typeface from assets.
 */

public class SetFontFace {
	
	static Typeface face;
	static Typeface faceBold;
	
	public SetFontFace(){
		
	}
	
	public void setFont(String font, Context context){
		SetFontFace.face = Typeface.createFromAsset(context.getAssets(),
				font);
	}
	
	public void setFontBold(String fontBold, Context context){
		SetFontFace.faceBold = Typeface.createFromAsset(context.getAssets(),
				fontBold);
	}
	
	public static Typeface getFont(){
		return face;
	}
	
	public static Typeface getFontBold(){
		return faceBold;
	}

}
