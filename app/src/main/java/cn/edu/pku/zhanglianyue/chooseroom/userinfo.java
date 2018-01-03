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

import cn.edu.pku.zhanglianyue.chooseroom.bean.uers;
import cn.edu.pku.zhanglianyue.chooseroom.util.NetUtil;

import static cn.edu.pku.zhanglianyue.chooseroom.MainActivity.DO_NOT_VERIFY;

/**
 * Created by zhanglianyue on 2018/1/2.
 */

public class userinfo extends Activity implements View.OnClickListener {
    private TextView useridTv;
    private TextView userNameTv;
    private TextView userSexTv;
    private TextView userCodeTv;
    private TextView userRoomTv;
    private TextView userBuildingTv;
    private TextView userLocationTv;
    private TextView userGradeTv;
    private Handler mHandler;
    private Button bQuery;
    private Button bSelect;
    private String sexQuery;//用来标志性别，查询空床位
    private String NameShare;//用来存储办理入住人名称，方便显示
    private ImageView top_exit_IV;//退出登录按钮
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.userinfo);

        bQuery = (Button)findViewById(R.id.userinfo_information_query);
        bQuery.setOnClickListener(this);
        bSelect = (Button)findViewById(R.id.userinfo_information_select);
        bSelect.setOnClickListener(this);
        top_exit_IV = (ImageView)findViewById(R.id.usual_top_exit);
        top_exit_IV.setOnClickListener(this);
        //获取主页面保存的userid信息
        sharedPreferences = getSharedPreferences("selectroom",MODE_PRIVATE);
        //SharedPreferences sharedPreferences = getSharedPreferences("selectroom",MODE_PRIVATE);
        String userid = sharedPreferences.getString("userid",null);
        Log.d("SelectRoom","userinformation:userid="+userid);

        //利用userid访问老师提供的接口，更新个人信息
        UpdateUserInfo(userid);

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 0:
                        uers userinfo_Handler = (uers) msg.obj;
                        //更新用户信息页面上的个人信息
                        useridTv = (TextView)findViewById(R.id.userinfo_information_id);
                        useridTv.setText("学    号："+userinfo_Handler.getuID());
                        userNameTv = (TextView)findViewById(R.id.userinfo_information_name);
                        userNameTv.setText("姓    名："+userinfo_Handler.getuName());
                        NameShare = userinfo_Handler.getuName();
                        userSexTv = (TextView)findViewById(R.id.userinfo_information_sex);
                        userSexTv.setText("性    别："+userinfo_Handler.getuSex());
                        if(userinfo_Handler.getuSex().equals("男")){
                            sexQuery = "1";
                        }else{
                            sexQuery = "2";
                        }
                        userCodeTv = (TextView)findViewById(R.id.userinfo_information_code);
                        userCodeTv.setText("校验码："+userinfo_Handler.getuCode());
                        userRoomTv = (TextView)findViewById(R.id.userinfo_information_room);
                        if(userinfo_Handler.getuRoom() == null){
                            userRoomTv.setText("宿舍号：尚未选择宿舍");
                        }else{
                            //如果选择了宿舍，那么就把变量设置为已选择，并且存到sharepreferences中，另一个页面也可以获取

                            userRoomTv.setText("宿舍号："+userinfo_Handler.getuRoom());
                        }
                        userBuildingTv = (TextView)findViewById(R.id.userinfo_information_building);
                        if(userinfo_Handler.getuBuilding() == null){
                            userBuildingTv.setText("楼    号：尚未选择宿舍");
                        }else{
                            userBuildingTv.setText("楼    号："+userinfo_Handler.getuBuilding());
                        }

                        userLocationTv = (TextView)findViewById(R.id.userinfo_information_location);
                        userLocationTv.setText("校    区："+userinfo_Handler.getuLocation());
                        userGradeTv = (TextView)findViewById(R.id.userinfo_information_grade);
                        userGradeTv.setText("年    级："+userinfo_Handler.getuGrade());
                        //将性别和名称存在sharedPreferences中
                        SharedPreferences sharedPreferences = getSharedPreferences("selectroom",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("sexQuery",sexQuery);
                        editor.putString("Name",NameShare);
                        editor.putString("Room",userinfo_Handler.getuRoom());
                        editor.commit();
                        break;
                    default:
                        Dialog alertDialog = new AlertDialog.Builder(userinfo.this).
                                setTitle("提示").
                                setMessage("个人信息请求失败").
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

    //个人信息页面的按钮监听事件
    @Override
    public void onClick(View view){
        //查询空床位
        if(view.getId() == R.id.userinfo_information_query){
            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                Log.d("SelectRoom","查询空床位按钮");
                //调用UserLogin函数，请求登录的接口。
                Intent i = new Intent(userinfo.this,empty.class);
                startActivity(i);
            }else{//如果网络连接不正常则显示“网络挂了”
                Toast.makeText(userinfo.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }
        }else if(view.getId() == R.id.userinfo_information_select){
            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                Log.d("SelectRoom","办理入住按钮");
                //调用UserLogin函数，请求登录的接口。
                if(sharedPreferences.getString("Room",null) == null){
                    Intent i = new Intent(userinfo.this,chooseroom.class);
                    startActivity(i);
                }else{
                    Dialog alertDialog = new AlertDialog.Builder(userinfo.this).
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
                Toast.makeText(userinfo.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }
        }else if(view.getId() == R.id.usual_top_exit){
            Dialog alertDialog = new AlertDialog.Builder(userinfo.this).
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
                            intent.setClass(userinfo.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    }).
                    setCancelable(false).
                    create();
            alertDialog.show();
        }
    }
    /**
     * 访问老师给的接口查询登录用户的个人信息
     * @param userid
     */
    private void UpdateUserInfo(String userid){
        final String address = "https://api.mysspku.com/index.php/V1/MobileCourse/getDetail?stuid="
                +userid;
        Log.d("SelectRoom",address);
        //String resultCon = null;
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
                    JSONObject jsonObject1 = new JSONObject(responseStr).getJSONObject("data");
                    uers userinformation = new uers();
                    userinformation.setuName(jsonObject1.getString("name"));
                    userinformation.setuID(jsonObject1.getString("studentid"));
                    userinformation.setuSex(jsonObject1.getString("gender"));
                    userinformation.setuCode(jsonObject1.getString("vcode"));
                    userinformation.setuLocation(jsonObject1.getString("location"));
                    userinformation.setuGrade(jsonObject1.getString("grade"));
                    if(jsonObject1.has("building")){
                        userinformation.setuBuilding(jsonObject1.getString("building"));
                    }
                    if(jsonObject1.has("room")){
                        userinformation.setuRoom(jsonObject1.getString("room"));
                    }
                    Message msg = new Message();
                    if(resultConInt == 0){
                        msg.what = 0;
                        msg.obj = userinformation;
                        mHandler.sendMessage(msg);
                        return;//如果返回的错误代码为0，说明登陆成功
                    }else{
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                        return;
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
