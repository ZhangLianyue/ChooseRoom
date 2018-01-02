package cn.edu.pku.zhanglianyue.chooseroom;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import cn.edu.pku.zhanglianyue.chooseroom.util.NetUtil;
import cn.edu.pku.zhanglianyue.chooseroom.bean.emptyinfo;

import static cn.edu.pku.zhanglianyue.chooseroom.MainActivity.DO_NOT_VERIFY;

/**
 * Created by zhanglianyue on 2018/1/2.
 */

public class empty extends Activity implements View.OnClickListener {

    private Button bSelect;
    private Handler mHandler;
    private TextView query_5Tv;
    private TextView query_8Tv;
    private TextView query_9Tv;
    private TextView query_13Tv;
    private TextView query_14Tv;
    private ImageView top_home_IV;
    private ImageView top_exit_IV;

    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty);

        sharedPreferences = getSharedPreferences("selectroom",MODE_PRIVATE);
        bSelect = (Button)findViewById(R.id.querybed_select);
        bSelect.setOnClickListener(this);
        top_home_IV = (ImageView)findViewById(R.id.usual_top_home);
        top_home_IV.setOnClickListener(this);
        top_exit_IV = (ImageView)findViewById(R.id.usual_top_exit);
        top_exit_IV.setOnClickListener(this);
        //SharedPreferences sharedPreferences = getSharedPreferences("selectroom",MODE_PRIVATE);
        String userid = sharedPreferences.getString("userid",null);
        String querySex = sharedPreferences.getString("sexQuery",null);//获取到查询空床位的性别
        Log.d("SelectRoom","QueryBed:userid="+userid);
        Log.d("SelectRoom","QueryBed:sexQuery="+querySex);
        //更新空床位信息
        UpdateQueryBed(querySex);

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 0:
                        emptyinfo bedInformation = (emptyinfo) msg.obj;
                        //更新空床位信息
                        query_5Tv = (TextView)findViewById(R.id.querybed_5);
                        query_8Tv = (TextView)findViewById(R.id.querybed_8);
                        query_9Tv = (TextView)findViewById(R.id.querybed_9);
                        query_13Tv = (TextView)findViewById(R.id.querybed_13);
                        query_14Tv = (TextView)findViewById(R.id.querybed_14);
                        query_5Tv.setText("5号楼剩余空床数"+bedInformation.getQuery_5());
                        query_8Tv.setText("8号楼剩余空床数"+bedInformation.getQuery_8());
                        query_9Tv.setText("9号楼剩余空床数"+bedInformation.getQuery_9());
                        query_13Tv.setText("13号楼剩余空床数"+bedInformation.getQuery_13());
                        query_14Tv.setText("14号楼剩余空床数"+bedInformation.getQuery_14());
                        break;
                    default:
                        Dialog alertDialog = new AlertDialog.Builder(empty.this).
                                setTitle("提示").
                                setMessage("空床位信息请求失败").
                                setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //确定按钮，点击可取消提示框
                                    }
                                }).
                                setCancelable(false).
                                create();
                        alertDialog.show();
                        break;
                }
            }
        };
    }

    @Override
    public void onClick(View view){
        if(view.getId() == R.id.querybed_select){
            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                Log.d("SelectRoom","办理入住按钮");
                //调用UserLogin函数，请求登录的接口。
                if(sharedPreferences.getString("Room",null) == null){
                    Intent i = new Intent(empty.this,chooseroom.class);
                    startActivity(i);
                }else{
                    Dialog alertDialog = new AlertDialog.Builder(empty.this).
                            setTitle("提示").
                            setMessage("您已经办理入住，无需重复办理！").
                            setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //确定按钮，点击可取消提示框
                                }
                            }).
                            setCancelable(false).
                            create();
                    alertDialog.show();
                }

            }else{//如果网络连接不正常则显示“网络挂了”
                Toast.makeText(empty.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }
        }else if(view.getId() == R.id.usual_top_home){
            Intent intent = new Intent();
            intent.setClass(empty.this, userinfo.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if(view.getId() == R.id.usual_top_exit){
            Dialog alertDialog = new AlertDialog.Builder(empty.this).
                    setTitle("提示").
                    setMessage("您确认要退出登录吗？").
                    setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //确定按钮，点击可取消提示框
                        }
                    }).
                    setPositiveButton("确定",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(empty.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    }).
                    setCancelable(false).
                    create();
            alertDialog.show();
        }
    }

    private void UpdateQueryBed(String querySex){
        final String address = "https://api.mysspku.com/index.php/V1/MobileCourse/getRoom?gender="
                +querySex;
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpsURLConnection con = null;
                try{
                    URL url = new URL(address);
                    //trustAllHosts函数用来给权限相信所有的网站证书
                    MainActivity.trustAllHosts();
                    con = (HttpsURLConnection)url.openConnection();
                    con.setHostnameVerifier(DO_NOT_VERIFY);
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                        Log.d("SelectRoom",str);
                    }
                    String responseStr = response.toString();
                    Log.d("SelectRoom",responseStr);
                    //获取json中的错误信息，以及数据信息
                    int resultConInt = new JSONObject(responseStr).getInt("errcode");
                    String resultCon = Integer.toString(resultConInt);
                    Log.d("SelectRoom",resultCon);
                    Message msg = new Message();
                    if(resultConInt != 0){  //如果有错误，那么直接返回错误代码
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                        return;
                    }else{
                        JSONObject jsonObject1 = new JSONObject(responseStr).getJSONObject("data");
                        emptyinfo bedinformation = new emptyinfo();
                        bedinformation.setQuery_5(jsonObject1.getInt("5"));
                        bedinformation.setQuery_8(jsonObject1.getInt("8"));
                        bedinformation.setQuery_9(jsonObject1.getInt("9"));
                        bedinformation.setQuery_13(jsonObject1.getInt("13"));
                        bedinformation.setQuery_14(jsonObject1.getInt("14"));
                        msg.what = 0;
                        msg.obj = bedinformation;
                        mHandler.sendMessage(msg);
                        return;//如果返回的错误代码为0，说明登陆成功
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(con != null){
                        con.disconnect();
                    }
                }
            }
        }).start();
    }
}
