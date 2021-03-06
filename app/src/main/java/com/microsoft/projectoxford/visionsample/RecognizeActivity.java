//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license.
//
// Project Oxford: http://ProjectOxford.ai
//
// ProjectOxford SDK Github:
// https://github.com/Microsoft/ProjectOxfordSDK-Windows
//
// Copyright (c) Microsoft Corporation
// All rights reserved.
//
// MIT License:
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
package com.microsoft.projectoxford.visionsample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalyzeResult;
import com.microsoft.projectoxford.vision.contract.LanguageCodes;
import com.microsoft.projectoxford.vision.contract.Line;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.contract.Region;
import com.microsoft.projectoxford.vision.contract.Word;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;
import com.microsoft.projectoxford.visionsample.helper.ImageHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParserException;

import java.util.Date;
import java.text.SimpleDateFormat;

public class RecognizeActivity extends ActionBarActivity {


    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE = 0;

    // The button to select an image
    private Button mButtonSelectImage;
    private Button mButtonUpload;

    // The URI of the image selected to detect.
    private Uri mImageUri;

    // The image selected to detect.
    private Bitmap mBitmap;

    // The edit to show status and result.
    private EditText mEditText;
    private EditText mEditTextXH;
    private TextView mTitle;
    private VisionServiceClient client;

    // Flag to indicate the request of the next task to be performed
    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_SELECT_IMAGE_IN_ALBUM = 1;

    // The URI of photo taken with camera
    private Uri mUriPhotoTaken;
    private float scaleHeight;
    private Matrix matrix;

    private String xm;
    private String ccdm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);

        Intent intent=getIntent();
        ccdm=intent.getStringExtra("ccdm");
        xm=intent.getStringExtra("xm");
        mTitle=(TextView) findViewById(R.id.title);
        mTitle.setText(xm + ",欢迎您");
        if (client == null) {
            client = new VisionServiceRestClient(getString(R.string.subscription_key));
        }

        mButtonSelectImage = (Button) findViewById(R.id.buttonSelectImage);
        mEditText = (EditText) findViewById(R.id.editTextResult);
        mButtonUpload = (Button) findViewById(R.id.ButtontestInsertOracle);
        mEditTextXH = (EditText) findViewById(R.id.editTextXH);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recognize, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Called when the "Select Image" button is clicked.
    public void selectImage(View view) {
        mEditText.setText("");


        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Save the photo taken to a temporary file.
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                File file = File.createTempFile("IMG_", ".jpg", storageDir);
                mUriPhotoTaken = Uri.fromFile(file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriPhotoTaken);
                startActivityForResult(intent, REQUEST_TAKE_PHOTO);
            } catch (IOException e) {
                setInfo(e.getMessage());
            }
        }
//        Intent intent;
//        intent = new Intent(RecognizeActivity.this, com.microsoft.projectoxford.visionsample.helper.SelectImageActivity.class);
//        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }

    // Set the information panel on screen.
    private void setInfo(String info) {
        TextView textView = (TextView) findViewById(R.id.info);
        textView.setText(info);
    }

    // Called when image selection is done.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("AnalyzeActivity", "onActivityResult");
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {

//                    Uri imageUri;
//                    if (data == null || data.getData() == null) {
//                        imageUri = mUriPhotoTaken;
//                    } else {
//                        imageUri = data.getData();
//                    }
                    // If image is selected successfully, set the image URI and bitmap.
                    if (data == null || data.getData() == null) {
                        mImageUri = mUriPhotoTaken;
                    } else {
                        mImageUri = data.getData();
                    }
//                    mImageUri = data.getData();
                    mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                            mImageUri, getContentResolver());
                    if (mBitmap != null) {
                        // Show the image on screen.
                        ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                        imageView.setImageBitmap(mBitmap);

                        // Add detection log.
                        Log.d("AnalyzeActivity", "Image: " + mImageUri + " resized to " + mBitmap.getWidth()
                                + "x" + mBitmap.getHeight());

                        doRecognize();

                    }
                }
                break;
            default:
                break;
        }
    }


    public void doRecognize() {
        mButtonSelectImage.setEnabled(false);
        mEditText.setText("Analyzing...");

        try {
            new doRequest().execute();
        } catch (Exception e) {
            mEditText.setText("Error encountered. Exception is: " + e.toString());
        }
    }

    private String process() throws VisionServiceException, IOException {
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
//        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, output); // 640K
//        mBitmap.compress(Bitmap.CompressFormat.JPEG, 50, output); //588
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        OCR ocr;
        ocr = this.client.recognizeText(inputStream, LanguageCodes.AutoDetect, true);

        String result = gson.toJson(ocr);
        Log.d("result", result);

        return result;
    }

    private Bitmap imageZoom(Bitmap bitMap) {
        //图片允许最大空间   单位：KB
        Bitmap bitmapr=null;
        double maxSize = 50.00;
        //将bitmap放至数组中，意在bitmap的大小（与实际读取的原文件要大）
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        //将字节换成KB
        double mid = b.length / 1024;
        //判断bitmap占用空间是否大于允许最大空间  如果大于则压缩 小于则不压缩
        if (mid > maxSize) {
            //获取bitmap大小 是允许最大大小的多少倍
            double i = mid / maxSize;
            //开始压缩  此处用到平方根 将宽带和高度压缩掉对应的平方根倍 （1.保持刻度和高度和原bitmap比率一致，压缩后也达到了最大大小占用空间的大小）
            bitmapr = zoomImage(bitMap, bitMap.getWidth() / Math.sqrt(i),
                    bitMap.getHeight() / Math.sqrt(i));
        }
        return bitmapr;
    }

    /***
     * .         * 图片的缩放方法
     * .         *
     *
     * @param bgimage ：源图片资源
     *                .         * @param newWidth
     *                .         *            ：缩放后宽度
     *                .         * @param newHeight
     *                ：缩放后高度
     * @return
     */
    public Bitmap zoomImage(Bitmap bgimage, double newWidth,
                            double newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }


    public void doRecognizeXH() {
        mButtonUpload.setEnabled(false);
        mEditTextXH.setText("Analyzing...");

        try {
            new doRequestXH().execute();
        } catch (Exception e) {
            mEditTextXH.setText("Error encountered. Exception is: " + e.toString());
        }
    }


    private class doRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;

        public doRequest() {
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                return process();
            } catch (Exception e) {
                this.e = e;    // Store error
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            // Display based on error existence

            if (e != null) {
                mEditText.setText("Error: " + e.getMessage());
                this.e = null;
            } else {
                Gson gson = new Gson();
                OCR r = gson.fromJson(data, OCR.class);

                String result = "";
                for (Region reg : r.regions) {
                    for (Line line : reg.lines) {
                        for (Word word : line.words) {
                            result += word.text + " ";
                        }
                        result += "\n";
                    }
                    result += "\n\n";
                }

                mEditText.setText(result);
                doRecognizeXH();
            }
            mButtonSelectImage.setEnabled(true);
        }
    }

    private class doRequestXH extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;

        public doRequestXH() {
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                return process();
            } catch (Exception e) {
                this.e = e;    // Store error
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            // Display based on error existence

            if (e != null) {
                mEditTextXH.setText("Error: " + e.getMessage());
                this.e = null;
            } else {
                String Ori = mEditText.getText().toString();
                String Aft = replaceBlank(Ori);
                String[] strings = Aft.split(" ");

                String part1 = "";
                String part2 = "";
                String part3;
                String result = "";
                for (String str : strings) {
                    //part1
                    switch (str.length()) {
                        //part1
                        case 4:
                            if (part1.equals("")) {
                                if (str.substring(3).equals("U")) {
                                    part1 = str;
                                }
                            }
                            break;
                        //part2
                        case 6:
                            if (part2.equals("")) {
                                if (isNumeric(str)) {
                                    part2 = str;
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
                String tmp = part1 + part2;
                char[] chs = tmp.toCharArray();
                double it = 0;
                for (int i = 0; i < tmp.length(); i++) {
                    int tmpit = (int) chs[i];
                    // 0-9
                    if (tmpit <= 57 && tmpit >= 48) {
                        it = it + (tmpit - 48) * Math.pow(2, i);
                    }
                    //A
                    if (tmpit == 65) {
                        it = it + (tmpit - 65 + 10) * Math.pow(2, i);
                    }
                    //B-K
                    if (tmpit <= 75 && tmpit >= 66) {
                        it = it + (tmpit - 66 + 12) * Math.pow(2, i);
                    }
                    //L-U
                    if (tmpit <= 85 && tmpit >= 76) {
                        it = it + (tmpit - 76 + 23) * Math.pow(2, i);
                    }
                    //V-Z
                    if (tmpit <= 90 && tmpit >= 86) {
                        it = it + (tmpit - 85 + 34) * Math.pow(2, i);
                    }
                }
                it = it % 11;
                if (it == 10) {
                    it = 0;
                }
                int intit = (int) it;
                part3 = Integer.toString(intit);
                result = part1 + part2 + part3;
                mEditTextXH.setText(result);
            }
            mButtonUpload.setEnabled(true);
        }
    }

    // judge all numbers
    private boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
//            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Pattern p = Pattern.compile("\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll(" ");
        }
        return dest;
    }

    // 定义webservice的命名空间
    public static final String SERVICE_NAMESPACE = "http://testoracle/";
    // 定义webservice提供服务的url
    public static final String SERVICE_URL = "http://10.28.32.160:8080/testoracle/services/HelloService?wsdl";

    //testInsertOracleButton
    // Called when the "testInsertOracle" button is clicked.
    public void testInsertOracleButton(View view) {
        mEditText.setText("正在保存入数据库。");
        // 开启一个子线程，进行网络操作，等待有返回结果，使用handler通知UI
        new Thread(networkTask).start();


//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if(intent.resolveActivity(getPackageManager()) != null) {
//            // Save the photo taken to a temporary file.
//            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//            try {
//                File file = File.createTempFile("IMG_", ".jpg", storageDir);
//                mUriPhotoTaken = Uri.fromFile(file);
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriPhotoTaken);
//                startActivityForResult(intent, REQUEST_TAKE_PHOTO);
//            } catch (IOException e) {
//                setInfo(e.getMessage());
//            }
//        }
//        Intent intent;
//        intent = new Intent(RecognizeActivity.this, com.microsoft.projectoxford.visionsample.helper.SelectImageActivity.class);
//        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
//            Log.i("mylog", "请求结果为-->" + val);
            // TODO
            // UI界面的更新等相关操作
            mEditText.setText(val);
//        setInfo(msg);

        }
    };

    /**
     * 网络操作相关的子线程
     */
    Runnable networkTask = new Runnable() {

        @Override
        public void run() {
            // TODO
            // 在这里进行 http request.网络请求相关操作
            Message msg = new Message();
            Bundle data = new Bundle();

            try {
                String XH = mEditTextXH.getText().toString();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                SimpleDateFormat df1 = new SimpleDateFormat("yyyyMMddHHmmss");
                String scptdate = df.format(new Date());
                String scptdate1 = df1.format(new Date());
                String var2 = testUpload(XH, scptdate1);
                String var1="";
                String result ="";
                if (var2.equals("failed"))
                {
                    result = "上传图片失败!" +   var2   ;
                }
                else {
                     var1 = testInsertOracle(XH, scptdate, var2);
                     result = "上传图片成功!" + "图片路径为：" +   var2  + "\n" + var1;
                }

                data.putString("value", result);
                msg.setData(data);
                handler.sendMessage(msg);
//                mEditText.setText(msg);
            } catch (Exception e) {
                // TODO Auto-generated catch block
//            e.printStackTrace();
                Log.e("error", e.getMessage());
            }


        }
    };

    // 调用远程webservice获取省份列表
    public String testInsertOracle(String tpmc, String sctpdate,String filename) {
        String tmp = "";
        mEditText.setText("正在上传中，请稍候...");
        // 调用 的方法
//        String methodName = "testInsertOracle";
        String methodName = "testInsertOracle_new";
//        String methodName = "greetings";

        try {
            // 实例化SoapObject对象
            SoapObject soapObject = new SoapObject(SERVICE_NAMESPACE,
                    methodName);
            soapObject.addProperty("param1", tpmc);
            soapObject.addProperty("param2", sctpdate);
            soapObject.addProperty("param3", filename);
            soapObject.addProperty("param4", ccdm);
            // 使用SOAP1.1协议创建Envelop对象
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.bodyOut = soapObject;
            // 设置与.NET提供的webservice保持较好的兼容性

//            envelope.dotNet = true;
            envelope.dotNet = false;
            envelope.setOutputSoapObject(soapObject);
            // 创建HttpTransportSE传输对象
            HttpTransportSE ht = new HttpTransportSE(SERVICE_URL);
            ht.debug = true;
            // 调用webservice
            ht.call(SERVICE_NAMESPACE + methodName, envelope);
            Log.w(getClass().getName(), ht.requestDump);
//            ht.call(null, envelope);
            if (envelope.getResponse() != null) {
                // 获取服务器响应返回的SOAP消息
                SoapObject result = (SoapObject) envelope.bodyIn;
//                SoapObject detail = (SoapObject) result.getProperty(methodName
//                        + "Result");
                // 解析服务器响应的SOAP消息
                tmp = result.getProperty(0).toString();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
            tmp = e.getMessage();
        }
//        catch (SoapFault e) {
//            // TODO Auto-generated catch block
////            e.printStackTrace();
//            tmp=e.getMessage();
//        }
//        catch (IOException e) {
//            // TODO Auto-generated catch block
////            e.printStackTrace();
//            tmp=e.getMessage();
//        } catch (XmlPullParserException e) {
//            // TODO Auto-generated catch block
////            e.printStackTrace();
//            tmp=e.getMessage();
//        }
        return tmp;
    }

    //读取android sdcard上的图片
    public String testUpload(String XH, String scptdate) {
        String tmp = "failed";
        try {

            mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                    mImageUri, getContentResolver());
            if (mBitmap != null) {
                // Show the image on screen.
//                ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
//                imageView.setImageBitmap(mBitmap);
//
//                // Add detection log.
//                Log.d("AnalyzeActivity", "Image: " + mImageUri + " resized to " + mBitmap.getWidth()
//                        + "x" + mBitmap.getHeight());
//
//                doRecognize();


//            String srcUrl = "/sdcard/"; //路径
//
//            String fileName = "aa.jpg";  //文件名
//
//            FileInputStream fis = new FileInputStream(srcUrl + fileName);
//
                ByteArrayOutputStream baos = new ByteArrayOutputStream();


                Bitmap tmpBitmap= imageZoom(mBitmap);

//                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                tmpBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] bitmapBytes = baos.toByteArray();
                String uploadBuffer = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);

//            byte[] buffer = new byte[1024];
//
//            int count = 0;
//
//            while((count = fis.read(buffer)) >= 0){
//
//                baos.write(buffer, 0, count);
//
//            }

//            String uploadBuffer = new String(Base64.encode(baos.toByteArray()));  //进行Base64编码

                String methodName = "uploadImage";

//            String XH=mEditTextXH.getText().toString();
//            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
//            String scptdate=df.format(new Date());
                String fileName = XH + scptdate + ".jpg";
//            String fileName="test.jpg";

                tmp = connectWebService(methodName, fileName, uploadBuffer);   //调用webservice

                Log.i("connectWebService", "start");
                baos.flush();
                baos.close();
            }
//            fis.close();

        } catch (Exception e) {
//            e.printStackTrace();
            tmp = e.getMessage();
        }
        return tmp;
    }

    private String connectWebService(String methodName, String fileName, String imageBuffer) {
        String tmp = "";
        String namespace = SERVICE_NAMESPACE;  // 命名空间，即服务器端得接口，注：后缀没加 .wsdl，

        //服务器端我是用x-fire实现webservice接口的

        String url = SERVICE_URL;   //对应的url

        //以下就是 调用过程了，不明白的话 请看相关webservice文档

        SoapObject soapObject = new SoapObject(namespace, methodName);

        soapObject.addProperty("filename", fileName);  //参数1   图片名

        soapObject.addProperty("image", imageBuffer);   //参数2  图片字符串

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(

                SoapEnvelope.VER10);

        envelope.dotNet = false;

        envelope.setOutputSoapObject(soapObject);

        HttpTransportSE httpTranstation = new HttpTransportSE(url);

        try {

            httpTranstation.call(namespace, envelope);

            Object result = envelope.getResponse();
            tmp = result.toString();
            ;
            Log.i("connectWebService", result.toString());

        } catch (Exception e) {

//            e.printStackTrace();
            tmp = e.getMessage();
        }

        return tmp;

    }
}
