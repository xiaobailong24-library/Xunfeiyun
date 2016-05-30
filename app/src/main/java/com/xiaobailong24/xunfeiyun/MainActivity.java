package com.xiaobailong24.xunfeiyun;

import android.app.Activity;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;


public class MainActivity extends Activity {

    private static String TAG = "MainActivity";
    // 函数调用返回值
    int ret = 0;
    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog iatDialog;
    // 听写结果内容
    private EditText mResultText;
    // 语音合成对象
    private SpeechSynthesizer mTts;

    // 默认发音人
    private String voicer = "xiaoyan";

    // 缓冲进度
    private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;

    private Toast mToast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
        super.onCreate(savedInstanceState);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        // 用于验证应用的key,将XXXXXXXX改为你申请的APPID
        SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID + "=XXXXXXXX");
        // 创建语音听写对象
        mIat = SpeechRecognizer.createRecognizer(this, mInitListener);
        // 初始化听写Dialog,如果只使用有UI听写功能,无需创建SpeechRecognizer
        // 创建语音听写UI
        iatDialog = new RecognizerDialog(MainActivity.this, mInitListener);
        // 创建语音合成对象
        mTts = SpeechSynthesizer.createSynthesizer(this, mInitListener);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mResultText = ((EditText) findViewById(R.id.content_rec));
    }

    public void startRec(View view) {
        mResultText.setText(null);
        setParam();
        boolean isShowDialog = true;
        if (isShowDialog) {
            // 显示听写对话框
            iatDialog.setListener(recognizerDialogListener);
            iatDialog.show();
            // showTip("begin");
        } else {
            // 不显示听写对话框
            ret = mIat.startListening(recognizerListener);
            if (ret != ErrorCode.SUCCESS) {
                // showTip("听写失败,错误码：" + ret);
            } else {
                // showTip("begin");
            }
        }
    }

    public void read(View view) {
        String text = mResultText.getText().toString();
        // 设置参数
        setParam2();
        //朗读
        int code = mTts.startSpeaking(text, mTtsListener);
        if (code != ErrorCode.SUCCESS) {
            if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                // 未安装则跳转到提示安装页面
            } else {
                showTip("语音合成失败,错误码: " + code);
            }
        }
    }

    /**
     * 参数设置
     *
     * @param
     * @return
     */
    private void setParam2() {
        Log.e(TAG, "setParam2");
        // 设置合成
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);

        // 设置语速
        mTts.setParameter(SpeechConstant.SPEED, "50");

        // 设置音调
        mTts.setParameter(SpeechConstant.PITCH, "50");

        // 设置音量
        mTts.setParameter(SpeechConstant.VOLUME, "50");

        // 设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
    }

    public void setParam() {
        Log.e(TAG, "setParam");
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);

        // 设置语言
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");

        // 设置语音前端点
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
        // 设置语音后端点
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
        // 设置标点符号 1为有标点 0为没标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "0");
        // 设置音频保存路径
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                Environment.getExternalStorageDirectory()
                        + "/xiaobailong24/xunfeiyun");
    }

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            Log.e(TAG, "mTtsListener-->onSpeakBegin");
            showTip("开始播放");
        }

        @Override
        public void onSpeakPaused() {
            Log.e(TAG, "mTtsListener-->onSpeakPaused");
            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            Log.e(TAG, "mTtsListener-->onSpeakResumed");
            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            Log.e(TAG, "mTtsListener-->onBufferProgress");
            mPercentForBuffering = percent;
            showTip(String.format("缓冲进度为%d%%，播放进度为%d%%",
                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            Log.e(TAG, "mTtsListener-->onSpeakProgress");
            mPercentForPlaying = percent;
            showTip(String.format("缓冲进度为%d%%，播放进度为%d%%",
                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError error) {
            Log.e(TAG, "mTtsListener-->onCompleted");
            if (error == null) {
                showTip("播放完成");
            } else if (error != null) {
                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            Log.e(TAG, "mTtsListener-->onEvent");
        }
    };
    /**
     * 听写监听器。
     */
    private RecognizerListener recognizerListener = new RecognizerListener() {

        @Override
        public void onVolumeChanged(int i, byte[] bytes) {
            Log.e(TAG, "recognizerListener-->onVolumeChanged");
            showTip("当前正在说话，音量大小：" + i);
        }

        @Override
        public void onBeginOfSpeech() {
            Log.e(TAG, "recognizerListener-->onBeginOfSpeech");
            showTip("开始说话");
        }

        @Override
        public void onEndOfSpeech() {
            Log.e(TAG, "recognizerListener-->onEndOfSpeech");
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.e(TAG, "recognizerListener-->onResult");
            String text = JsonParser.parseIatResult(results.getResultString());
            mResultText.append(text);
            mResultText.setSelection(mResultText.length());
            if (isLast) {
                // TODO 最后的结果
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            Log.e(TAG, "recognizerListener-->onEvent");
        }

        @Override
        public void onError(SpeechError arg0) {
            Log.e(TAG, "recognizerListener-->onError");
            // TODO Auto-generated method stub
        }
    };
    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener recognizerDialogListener = new RecognizerDialogListener() {

        public void onResult(RecognizerResult results, boolean isLast) {
            Log.e(TAG, "recognizerDialogListener-->onResult");
            String text = JsonParser.parseIatResult(results.getResultString());
            mResultText.append(text);
            mResultText.setSelection(mResultText.length());
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            Log.e(TAG, "recognizerDialogListener-->onError");
            showTip(error.getPlainDescription(true));
        }
    };
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.e(TAG, "mInitListener-->onInit");
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码：" + code);
            }
        }
    };

    private void showTip(final String str) {
        Log.e(TAG, "showTip-->" + str);
        mToast.setText(str);
        mToast.show();
    }


    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        // 退出时释放连接
        mIat.cancel();
        mIat.destroy();
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
    }

}
