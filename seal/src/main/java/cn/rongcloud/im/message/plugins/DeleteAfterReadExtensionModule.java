package cn.rongcloud.im.message.plugins;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;

/**
 * Created by star1209 on 2018/5/18.
 */

public class DeleteAfterReadExtensionModule extends DefaultExtensionModule {

    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
        List<IPluginModule> pluginModules = new ArrayList<>();
        if (conversationType.equals(Conversation.ConversationType.PRIVATE)){
            pluginModules.add(new DeleteAfterReadPlugin());
        }
        return pluginModules;
    }

    @Override
    public void onInit(String appKey) {
        super.onInit(appKey);
    }
}
