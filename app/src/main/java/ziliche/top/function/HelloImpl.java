package ziliche.top.function;

import android.view.View;

import ziliche.top.function.recyclerview.SingleClick;

/**
 * @author eddie
 * @date 2019/9/3
 */
public class HelloImpl implements Hello, View.OnClickListener {
    @Override
    public void sayHello() {

    }

    @SingleClick
    @Override
    public void onClick(View v) {
    }
}
