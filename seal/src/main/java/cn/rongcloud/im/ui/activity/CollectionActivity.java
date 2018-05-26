package cn.rongcloud.im.ui.activity;

import android.os.Bundle;

import com.dbcapp.club.R;

/**
 * Created by star1209 on 2018/5/22.
 */

public class CollectionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collections);

        setTitle(R.string.baojia_collections_title, false);
    }
}
