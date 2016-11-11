package cn.rongcloud.im.message.module;

import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.emoticon.IEmoticonTab;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;

public class SealExtensionModule extends DefaultExtensionModule {

    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
        return super.getPluginModules(conversationType);
    }

    @Override
    public List<IEmoticonTab> getEmoticonTabs() {
        return super.getEmoticonTabs();
    }
}
