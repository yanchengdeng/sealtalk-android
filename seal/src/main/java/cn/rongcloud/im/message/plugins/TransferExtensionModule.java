package cn.rongcloud.im.message.plugins;


import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.RongIM;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;

/**
 * Created by star1209 on 2018/5/15.
 */

public class TransferExtensionModule extends DefaultExtensionModule {

    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {

        List<IPluginModule> pluginModules = new ArrayList<>();
        if (conversationType.equals(Conversation.ConversationType.PRIVATE)){
            pluginModules.add(new TransferPlugin());
        }
        return pluginModules;
    }

    @Override
    public void onInit(String appKey) {
        super.onInit(appKey);
        RongIM.registerMessageType(TransferMessage.class);
        RongIM.registerMessageTemplate(new TransferMessageProvider());
    }
}
