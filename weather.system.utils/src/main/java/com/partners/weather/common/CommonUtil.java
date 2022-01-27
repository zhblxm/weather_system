package com.partners.weather.common;

public final class CommonUtil {
    private static float getRatio(int width, int height, int maxWidth, int maxHeight) {
        float Ratio = 1.0f;
        float widthRatio;
        float heightRatio;
        widthRatio = (float) maxWidth / width;
        heightRatio = (float) maxHeight / height;
        if (widthRatio < 1.0 || heightRatio < 1.0) {
            Ratio = widthRatio <= heightRatio ? widthRatio : heightRatio;
        }
        return Ratio;
    }
    private static boolean imageCompress(String path, String fileName, String outPath, String outFileName, float scale, float quality, int width, int height) throws Exception {
            /*Image image = javax.imageio.ImageIO.read(new File(path + fileName));
            int imageWidth = image.getWidth(null);
            int imageHeight = image.getHeight(null);
            if (scale > 0.5)
                scale = 0.5f;
            
            if(width < 1){
            	width = imageWidth;
            }
            if(height < 1){
            	height = imageHeight;
            }
            float realscale = getRatio(imageWidth, imageHeight, width, height);
            
            float finalScale = Math.min(scale, realscale);
            imageWidth = (int) (finalScale * imageWidth);
            imageHeight = (int) (finalScale * imageHeight);
            image = image.getScaledInstance(imageWidth, imageHeight, Image.SCALE_AREA_AVERAGING);
            BufferedImage mBufferedImage = new BufferedImage(imageWidth,imageHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = mBufferedImage.createGraphics();
			try {
				  g2.drawImage(image, 0, 0, imageWidth, imageHeight, Color.white,null);
			      g2.dispose();
				
			} catch (Exception e) {
				return false;
			}
			finally
			{
				if(g2!=null)
				{
					g2.dispose();
				}
			}
            float[] kernelData2 = { -0.125f, -0.125f, -0.125f, -0.125f, 2, -0.125f, -0.125f, -0.125f, -0.125f };
            Kernel kernel = new Kernel(3, 3, kernelData2);
            ConvolveOp cOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
            mBufferedImage = cOp.filter(mBufferedImage, null);
            FileOutputStream out = new FileOutputStream(outPath + outFileName);
            try {
                JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
                JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(mBufferedImage);
                param.setQuality(quality, true);
                encoder.setJPEGEncodeParam(param);
                encoder.encode(mBufferedImage);
			} catch (Exception e) {
				return false;
			}
			finally
			{
				out.close();
			}*/
			return true;

    }
    public synchronized static boolean ImageCompress(String path,String fileName, float scale, int width, int height) {
        try {
			boolean compressResult=imageCompress(path, fileName, path, fileName, scale, 0.75f, width, height);
			return compressResult;
		} catch (Exception e) {
			return false;
		}
    }

    public synchronized static boolean ImageCompress(String path,String fileName, String outPath,String outFileName, float scale, int width, int height) {
        try {
			boolean compressResult=imageCompress(path, fileName, outPath, outFileName, scale, 0.75f, width, height);
			return compressResult;
		} catch (Exception e) {
			return false;
		}
    }
}
