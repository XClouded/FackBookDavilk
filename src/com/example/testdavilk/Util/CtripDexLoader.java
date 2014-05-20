package com.example.testdavilk.Util;

import android.content.Context;
import dalvik.system.DexClassLoader;

public class CtripDexLoader extends DexClassLoader
{
    private boolean mDPSigned;
    private final String mDexPath;
    private Context mContext;
    private boolean mSignatureChecked;
    private static CtripDexLoader ctripDexLoader;

    public static CtripDexLoader getInstance(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent,Context context)
    {
        if (ctripDexLoader == null)
        {
            ctripDexLoader = new CtripDexLoader(dexPath, optimizedDirectory, libraryPath, parent);
            ctripDexLoader.mContext=context;
        }
        return ctripDexLoader;
    }

    private CtripDexLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent)
    {
        super(dexPath, optimizedDirectory, libraryPath, parent);
        // TODO Auto-generated constructor stub
        this.mDexPath = dexPath;
    }
    /**
     * Ð£ÑéÇ©Ãû
     * @return
     */
    public boolean checkSignature(){
    	if (!((this.mDexPath == null) || (this.mDexPath.length() <= 0)))
        {
            boolean isSample = new CtripCheckSign(this.mDexPath).checkSign(mContext);
            this.mSignatureChecked = true;
            this.mDPSigned=isSample;
            return isSample;
        }
        return false; 
    }
    public Class<?> loadSelfClass(String paramString) throws ClassNotFoundException
    {
		// if (!this.mSignatureChecked)
		// checkSignature();
		// if (!this.mDPSigned)
		// throw new ClassNotFoundException("invalid signature");
        return super.loadClass(paramString);
    }
}
