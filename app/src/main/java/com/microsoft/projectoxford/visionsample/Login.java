package com.microsoft.projectoxford.visionsample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.microsoft.projectoxford.visionsample.R;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Login extends ActionBarActivity {
    private Button but;//创建一个按钮对象，导入包
//    private Button btn;
    private EditText et1;
    private EditText et2;
    private TextView TVstatus;
    private String xm;
    private String ccdm;
    // 定义webservice的命名空间
    public static final String SERVICE_NAMESPACE = "http://testoracle/";
    // 定义webservice提供服务的url
    public static final String SERVICE_URL = "http://10.28.32.160:8080/testoracle/services/HelloService?wsdl";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //对but进行实例化
        but=(Button)findViewById(R.id.button1);
//        btn=(Button)findViewById(R.id.button2);
        //创建but的单击事件，参数要传匿名内部类
        et1=(EditText)findViewById(R.id.editText1);
        et2=(EditText)findViewById(R.id.editText2);
        TVstatus=(TextView)findViewById(R.id.TVstatus);
        but.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                //测试使用System.out.println("hello..........");
                //要成功进行跳转到intent这个对象
                //第一个参数是原来的类，出发站以.this结尾
                //第二个参数是要跳转的类，结束站以class结尾
                //Intent in=new Intent(MainActivity.this,SecActivity.class);
                //startActivity(in);
                TVstatus.setText("正在登录");

                new Thread(networkTask).start();
//                String tmp=TVstatus.getText().toString();

//                Login_ButtonClick(username, pwd);
            }



        });
//        btn.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//                // TODO Auto-generated method stub
//                Intent in2 = new Intent();//创建意图对象
//			/* 打电话
//			in2.setAction(Intent.ACTION_CALL);指定意图动作
//			in2.setData(Uri.parse("tel:5556"));
//			startActivity(in2);启动意图*/
//                //发短信
//                in2.setAction(Intent.ACTION_SENDTO);
//                in2.setData(Uri.parse("smsto:5556"));
//                in2.putExtra("sms_body", "password");
//                startActivity(in2);//=MainActivity.this.startActivity(in2)
//
//            }

//        });
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
            TVstatus.setText(val);
//        setInfo(msg);
            if (!xm.equals("")) {
                //密码确认
                Intent in = new Intent(Login.this, RecognizeActivity.class);
                in.putExtra("ccdm",ccdm);
                in.putExtra("xm",xm);
                startActivity(in);
            }
            else
            {
                TVstatus.setText("登录失败，用户或密码错误。");
//                    tmp="登录失败，用户或密码错误。";
            }
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
            xm = "";
            try {
                String username = et1.getText().toString();
                String pwd = et2.getText().toString();
//                String XH = mEditTextXH.getText().toString();
//                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
//                SimpleDateFormat df1 = new SimpleDateFormat("yyyyMMddHHmmss");
//                String scptdate = df.format(new Date());
//                String scptdate1 = df1.format(new Date());
//                String var2 = testUpload(XH, scptdate1);
                String var1="";
                String result ="";
//                if (var2.equals("failed"))
//                {
//                    result = "上传图片失败!" +   var2   ;
//                }
//                else {
                    var1 = Login_ButtonClick(username, pwd);
                    result =  var1;
//                }

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
    private String Login_ButtonClick(String username, String pwd) {
        String tmp="";
        ccdm = username;
        // 调用 的方法
        String methodName = "login";
//        String methodName = "greetings";
//        String tmp="";
        try {
            // 实例化SoapObject对象
            SoapObject soapObject = new SoapObject(SERVICE_NAMESPACE,
                    methodName);
            soapObject.addProperty("param1", username);
            soapObject.addProperty("param2", pwd);
//                    soapObject.addProperty("param3", filename);
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
                xm=tmp;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
            tmp = e.getMessage();
        }
        return  tmp;
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
}
