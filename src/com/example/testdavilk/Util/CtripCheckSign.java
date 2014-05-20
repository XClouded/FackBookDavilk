package com.example.testdavilk.Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
public class CtripCheckSign
{
    private final String mDexPath;
    public CtripCheckSign(String mDexPath)
    {
        this.mDexPath = mDexPath;
    }
    /**
     * 对比签名
     * @param context
     * @return
     */
    public boolean checkSign(Context context){
    	if(getSign()!=null&&getSign().equals(getSingInfo(context))){
    		return true;
    	}
    	return false;
    }
    /**
     * 获取 增量包签名
     * @return
     */
    private String getSign()
    {
        JarFile localJarFile;
        try
        {
            localJarFile = new JarFile(mDexPath);
            JarEntry localJarEntry = localJarFile.getJarEntry("classes.dex");
            if (localJarEntry != null)
            {
                Certificate[] arrayOfCertificate = loadCertificates(localJarFile, localJarEntry, new byte[8192]);
                if(arrayOfCertificate.length>0){
                    Certificate cert = arrayOfCertificate[0];
                    if(cert!=null){
                        String publickey = cert.getPublicKey().toString();
                        System.out.println(publickey);
                        System.out.println(cert.getPublicKey().getAlgorithm());
                        System.out.println(cert.getType());
                        return publickey;
                    } 
                }
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 获取本包 签名
     * @param context
     * @return
     */
    private String getSingInfo(Context context)
    {
        try
        {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            Signature sign = signs[0];
            return parseSignature(sign.toByteArray());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析签名
     * @param signature
     * @return
     */
    private String parseSignature(byte[] signature)
    {
        try
        {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(signature));
            String pubKey = cert.getPublicKey().toString();
            String signNumber = cert.getSerialNumber().toString();
            return pubKey;
        }
        catch (CertificateException e)
        {
            e.printStackTrace();
        }
        return null;
    }
//    @SuppressWarnings("unchecked")
//    public PackageInfo getPackageArchiveInfo(int flags)
//    {
//        try
//        {
//            Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
//            Class[] innerClasses = packageParserClass.getDeclaredClasses();
//            Class packageParserPackageClass = null;
//            for (Class innerClass : innerClasses)
//            {
//                if (0 == innerClass.getName().compareTo("android.content.pm.PackageParser$Package"))
//                {
//                    packageParserPackageClass = innerClass;
//                    break;
//                }
//            }
//            Constructor<?> packageParserConstructor = packageParserClass.getConstructor(String.class);
//            Method parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage", File.class, String.class, DisplayMetrics.class, int.class);
//            Method collectCertificatesMethod = packageParserClass.getDeclaredMethod("collectCertificates", packageParserPackageClass, int.class);
//            Method generatePackageInfoMethod = packageParserClass.getDeclaredMethod("generatePackageInfo", packageParserPackageClass, int[].class, int.class, long.class, long.class);
//            packageParserConstructor.setAccessible(true);
//            parsePackageMethod.setAccessible(true);
//            collectCertificatesMethod.setAccessible(true);
//            generatePackageInfoMethod.setAccessible(true);
//            Object packageParser = packageParserConstructor.newInstance(mDexPath);
//            DisplayMetrics metrics = new DisplayMetrics();
//            metrics.setToDefaults();
//            final File sourceFile = new File(mDexPath);
//            Object pkg = parsePackageMethod.invoke(packageParser, sourceFile, mDexPath, metrics, 0);
//            if (pkg == null)
//            {
//                return null;
//            }
//            if ((flags & android.content.pm.PackageManager.GET_SIGNATURES) != 0)
//            {
//                collectCertificatesMethod.invoke(packageParser, pkg, 0);
//            }
//            return (PackageInfo) generatePackageInfoMethod.invoke(null, pkg, null, flags, 0, 0);
//        }
//        catch (Exception e)
//        {
//
//        }
//
//        return null;
//    }
    private java.security.cert.Certificate[] loadCertificates(JarFile jarFile, JarEntry je, byte[] readBuffer)
    {
        try
        {
            InputStream is = jarFile.getInputStream(je);
            while (is.read(readBuffer, 0, readBuffer.length) != -1)
            {
            }
            is.close();

            return je != null ? je.getCertificates() : null;
        }
        catch (IOException e)
        {
            System.err.println("Exception reading " + je.getName() + " in " + jarFile.getName() + ": " + e);
        }
        return null;
    }

}
