package com.tinybox.animlogindemo.demo

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.tinybox.animlogindemo.R
import kotlinx.android.synthetic.main.activity_demo.*
import java.util.*


val Number.dp2px get() = (toInt() * Resources.getSystem().displayMetrics.density).toInt()
val Number.px2dp get() = (toInt() / Resources.getSystem().displayMetrics.density).toInt()

fun View.getScreenWidth(): Int {
    return this.context.resources.displayMetrics.widthPixels
}

fun Activity.getScreenWidth(): Int {
    return this.resources.displayMetrics.widthPixels
}


class DemoActivity : AppCompatActivity() {

    companion object {
        private const val DURATION_ANIM: Long = 800
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_demo)

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val login = findViewById<Button>(R.id.login)
        val loading = findViewById<ProgressBar>(R.id.loading)

        initVM()
        initListener()
        initListener()
        /** init */
        btn_clear.visibility = View.GONE
        checkbox.isChecked = false
        val defStr = "默认显示标题"
        et_room.setText(defStr)
        et_room.setOnEditorActionListener { v, actionId, event ->
            event.keyCode == KeyEvent.KEYCODE_ENTER
        }

    }
    val handler: Handler = Handler()

    private lateinit var viewModel: CreateViewModel
    private fun initVM() {
        viewModel = ViewModelProvider(this).get(CreateViewModel::class.java)
        viewModel.createResult.observe(this, Observer {
            when (it) {
                EnumStatus.BEFORE -> {
                    disableBtn()
                }
                EnumStatus.LOADING -> {
//                    loadingBtn()
                    startLoadingAnim()
                }
                EnumStatus.SUCCESS -> {
                    if (animatorSet != null && animatorSet?.isRunning!!) {
                        handler.postDelayed({
                            onCreateSuccess(it)
                        }, DURATION_ANIM)
                    } else {
                        onCreateSuccess(it)
                    }
                }
                EnumStatus.ERROR -> {
                    Toast.makeText(this,"创建失败，请重试",Toast.LENGTH_SHORT).show()
                    reverseAnim()
                }
                null -> {
                }
            }
        })
        viewModel.inputName.observe(this, Observer {
            it?.let { btn_clear.visibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE }
        })
        viewModel.checkEnable.observe(this, Observer {
            if (it) enableBtn() else disableBtn()
        })
    }

    private fun onCreateSuccess(it: EnumStatus) {
        animatorSet?.let { it.end() }
        hideBtn()
        // TODO
        Toast.makeText(this,"创建成功",Toast.LENGTH_SHORT).show()
    }

    private fun initListener() {
        btn_close.setOnClickListener {
            reverseAnim()
        }
        et_room.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.inputName.postValue(s.toString())
            }
        })
        et_room.setOnFocusChangeListener { v, hasFocus -> if (hasFocus) et_room.isCursorVisible = true }
        btn_clear.setOnClickListener { et_room.setText("") }
        checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.checkAgree.postValue(isChecked)
        }
        tv_what.setOnClickListener {
        }
        btn_confirm.setOnClickListener {
//            KeyboardUtils.hideKeyboard(et_room, {
//                viewModel.reqCreate(et_room.text.toString())
//            }, 250L)
            viewModel.reqCreate(et_room.text.toString())
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
        btn_text?.handler?.removeCallbacksAndMessages(null)
    }

    var animatorSet: AnimatorSet? = null
    fun startLoadingAnim() {
        var screenWidth = getScreenWidth() ?: 0
        if (screenWidth <= 0)
            screenWidth = 377.dp2px
        val et_width = screenWidth - 16.dp2px * 2
        var target_et_width = et_width
        if (et_room.text?.toString()?.length ?: 0 <= 16) {
            val measureText = et_room.paint.measureText(et_room.text?.toString() ?: "").toInt()
            target_et_width = 13.dp2px * 2 + measureText + 20
        }
        val transX = if (et_width > target_et_width) (et_width - target_et_width) / 2f else 0f

//        val etWrapper = ViewWrapper(et_room)
//        val objectAnimator = ObjectAnimator.ofInt(etWrapper, "width", et_width, target_et_width)
        val objectAnimator = ObjectAnimator.ofFloat(et_room, "translationX", 0f, transX);
        val objectAnimator1 = ObjectAnimator.ofFloat(layout_et_room, "alpha", 1f, 0f)
        val objectAnimator2 = ObjectAnimator.ofArgb(btn_confirm, "backgroundColor", getColor(R.color.GBL01A), getColor(R.color.GBK06A));
        val objectAnimator3 = ObjectAnimator.ofFloat(iv_avatar, "translationY", 0f, 1f * 48.dp2px);
        val objectAnimator4 = ObjectAnimator.ofFloat(iv_back, "alpha", 0f, 1f);
        val objectAnimator5 = ObjectAnimator.ofFloat(checkbox, "alpha", 1f, 0f);
        val objectAnimator6 = ObjectAnimator.ofFloat(tv_what, "alpha", 1f, 0f);
        val objectAnimator7 = ObjectAnimator.ofFloat(btn_clear, "alpha", 1f, 0f);
        val objectAnimator8 = ObjectAnimator.ofFloat(tv_tip, "alpha", 1f, 0f);
        val objectAnimator9 = ObjectAnimator.ofArgb(btn_text, "textColor", getColor(R.color.GBK99B), getColor(R.color.GBK05A));

        if (animatorSet == null) animatorSet = AnimatorSet()
        animatorSet?.let {
            it.playTogether(
                objectAnimator, objectAnimator1, objectAnimator2,
                objectAnimator3, objectAnimator4, objectAnimator5,
                objectAnimator6, objectAnimator7, objectAnimator8,
                objectAnimator9
            )
            it.duration = Companion.DURATION_ANIM
            it.removeAllListeners()
            it.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
//                    et_room.gravity = Gravity.CENTER
                }

                override fun onAnimationCancel(animation: Animator?) {
                    btn_confirm.isEnabled = true
                    btn_text.handler.removeCallbacksAndMessages(null)
                    btn_text.setText("创建一个房间")

                    et_room.setPaddingRelative(13.dp2px, 13.dp2px, 36.dp2px, 13.dp2px)
                }

                override fun onAnimationStart(animation: Animator?) {
                    btn_confirm.isEnabled = false

                    btn_text.postDelayed(textLoadingRunnable, 500)
                    btn_text.setText("创建房间中   ")

                    et_room.clearFocus()
                    et_room.isCursorVisible = false;
                    et_room.setPaddingRelative(13.dp2px,13.dp2px,13.dp2px,13.dp2px)
//                    et_room.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                }
            })
            it.start()
        }
    }

    fun reverseAnim() {
        btn_confirm.isEnabled = true
        btn_confirm.visibility = View.VISIBLE
        btn_text.visibility = View.VISIBLE
        btn_text.handler.removeCallbacksAndMessages(null)
        btn_text.setText("创建一个房间")
        animatorSet?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.removeAllListeners()
                it.reverse()
            } else {
                enableBtn()
            }
        }
    }


    var loadingCount = 0
    private val textLoadingRunnable: Runnable = object : Runnable {
        override fun run() {
            var txt = "创建房间中"
            when (loadingCount % 3) {
                0 -> {
                    txt = "创建房间中.  "
                }
                1 -> {
                    txt = "创建房间中.. "
                }
                2 -> {
                    txt = "创建房间中..."
                }
            }
            btn_text?.text = txt
            loadingCount++
            if (loadingCount >= 3) loadingCount = 0
            btn_text?.postDelayed(this, 500)
        }
    }

    fun enableBtn() {
        btn_confirm.isEnabled = true
        btn_confirm.alpha = 1f
        btn_confirm.setBackgroundColor(getColor(R.color.GBL01A))
        btn_confirm.visibility = View.VISIBLE
        btn_text.visibility = View.VISIBLE
        btn_text.setText("创建一个房间")
        btn_text.setTextColor(getColor(R.color.GBK99B))
    }

    fun disableBtn() {
        btn_confirm.isEnabled = false
        btn_confirm.alpha = 0.5f
        btn_confirm.setBackgroundColor(getColor(R.color.GBL01A))
        btn_confirm.visibility = View.VISIBLE
        btn_text.visibility = View.VISIBLE
        btn_text.setText("创建一个房间")
        btn_text.setTextColor(getColor(R.color.GBK99B))
    }

    fun loadingBtn() {
        btn_confirm.isEnabled = false
        btn_confirm.alpha = 0.08f
        btn_confirm.setBackgroundColor(getColor(R.color.GBK06A))
        btn_confirm.visibility = View.VISIBLE
        btn_text.visibility = View.VISIBLE
        btn_text.setText("创建房间中...")
        btn_text.setTextColor(getColor(R.color.GBK05A))
    }

    fun hideBtn() {
        btn_confirm.isEnabled = false
        btn_confirm.visibility = View.GONE
        btn_text.visibility = View.GONE
    }

}

private class ViewWrapper(private val rView: View) {
    var width: Int
        get() = rView.layoutParams.width
        set(width) {
            rView.layoutParams.width = width
            rView.requestLayout()
        }

    var height: Int
        get() = rView.layoutParams.height
        set(height) {
            rView.layoutParams.height = height
            rView.requestLayout()
        }
}

enum class EnumStatus {
    BEFORE, LOADING, SUCCESS, ERROR
}

class CreateViewModel : ViewModel() {
    val inputName: MutableLiveData<String> = MutableLiveData()
    val checkAgree: MutableLiveData<Boolean> = MutableLiveData()
    val checkEnable: MediatorLiveData<Boolean> = MediatorLiveData()
    val createResult: MutableLiveData<EnumStatus> = MutableLiveData()
    val avatarUrl: MutableLiveData<String> = MutableLiveData()

    init {
        val observer = Observer<Any> {
            if (inputName.value?.length ?: 0 > 0 && checkAgree.value == true) {
                checkEnable.postValue(true)
            } else {
                checkEnable.postValue(false)
            }
        }
        checkEnable.addSource(inputName, observer)
        checkEnable.addSource(checkAgree, observer)

    }

    @SuppressLint("CheckResult")
    fun reqCreate(roomName: String?) {
        val map = mapOf(
            "title" to roomName
        )
        createResult.postValue(EnumStatus.LOADING)

        Thread(Runnable {
            Thread.sleep(5000)
            val randSuccess = Random().nextBoolean()
            if(randSuccess) {
                createResult.postValue(
                    EnumStatus.SUCCESS
                )
            }else {
                createResult.postValue(
                    EnumStatus.ERROR
                )
            }
        }).start()
    }
}