package com.llew.bytecode.fix.utils

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
/**
 * 文件处理辅助类
 * <br/><br/>
 *
 * @author llew
 * @date 2017/11/16
 */

public class FileUtils {

    public static boolean containsClass(JarFile jarFile, String className) {
        def contain = false
        if (null != jarFile) {
            Enumeration<JarEntry> entries = jarFile.entries()
            if (null != entries) {
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement()
                    if (!jarEntry.isDirectory()) {
                        String jarEntryName = jarEntry.name.replaceAll("/", ".")
                        if (jarEntryName == className) {
                            contain = true
                            break
                        }
                    }
                }
            }
        }
        return contain
    }

    public static void unzipJarFile(JarFile jarFile, File rootFile) {
        if (null == jarFile || null == rootFile) {
            Logger.e("params invalid before unzip jar file !!!")
            return
        }
        if (!rootFile.exists()) {
            rootFile.mkdirs()
        }

        Enumeration<JarEntry> entries = jarFile.entries()
        if (null == entries) {
            return
        }

        InputStream is = null
        OutputStream os = null
        FileOutputStream fos = null

        byte[] buffer = new byte[10240]

        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement()
            if (!jarEntry.isDirectory()) {
                File entityFile = new File(rootFile, jarEntry.name)
                if (!entityFile.parentFile.exists()) {
                    entityFile.parentFile.mkdirs()
                }
                if (entityFile.exists()) {
                    entityFile.delete()
                }
                entityFile.createNewFile()

                try {
                    is = jarFile.getInputStream(jarEntry)
                    fos = new FileOutputStream(entityFile)
                    os = new BufferedOutputStream(fos)

                    int readCount = 0;

                    while (-1 != (readCount = is.read(buffer))) {
                        os.write(buffer, 0, readCount)
                    }

                    os.flush()
                    fos.flush()

                    closeQuietly(os)
                    closeQuietly(fos)
                    closeQuietly(is)
                } catch (Exception ex) {
                }
            }
        }
    }

    public static void zipJarFile(File srcDir, File destFile) {
        if (null == srcDir) {
            Logger.e("srcDir is null")
            return
        }

        if (!srcDir.exists()) {
            Logger.e("srcDir is not exist")
            return
        }

        if (null == destFile) {
            Logger.e("desFile is null")
            return
        }

        if (".DS_Store".equals(srcDir.name)) {
            return
        }

        if (!destFile.exists()) {
            if (!destFile.parentFile.exists()) {
                destFile.parentFile.mkdirs()
            }
            destFile.createNewFile()
        }

        try {
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(destFile))
            if (srcDir.isDirectory()) {
                File[] files = srcDir.listFiles()
                if (null != files && files.length > 0) {
                    for (File file : files) {
                        zipInternal(zipOutputStream, file, file.name + File.separator)
                    }
                }
            }
            zipOutputStream.flush()
            closeQuietly(zipOutputStream)
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    private static void zipInternal(ZipOutputStream out, File file, String baseDir) {
        if (".DS_Store".equals(file.name)) {
            return
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles()
            if (null != files && files.length > 0) {
                for (File f : files) {
                    zipInternal(out, f, baseDir + f.name + File.separator)
                }
            }
        } else {
            byte[] buffer = new byte[10240]
            InputStream input = new FileInputStream(file)
            out.putNextEntry(new ZipEntry(baseDir.substring(0, baseDir.indexOf(file.getName())) + file.name))

            int readCount = 0
            while (-1 != (readCount = input.read(buffer))) {
                out.write(buffer, 0, readCount)
            }
            out.flush()
            closeQuietly(input)
        }
    }

    public static void clearFile(File rootFile) {
        if (null == rootFile) {
            return
        }
        if (!rootFile.exists()) {
            return
        }
        if (rootFile.isDirectory()) {
            File[] files = rootFile.listFiles()
            if (null != files && files.length > 0) {
                files.each { file ->
                        clearFile(file)
                }
            }
        }
        rootFile.delete()
    }

    public static boolean hasFiles(File rootFile) {
        if (null == rootFile || !rootFile.exists()) {
            return false
        }
        return null == rootFile.listFiles() ? false : rootFile.listFiles().length > 0
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if (null != closeable) {
                closeable.close()
                closeable = null
            }
        } catch (IOException e) {
            e.printStackTrace()
        }
    }
}
