package cn.rongcloud.im.message.module;

import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.RongExtension;
import io.rong.imkit.emoticon.IEmoticonTab;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;
import io.rong.recognizer.RecognizePlugin;

public class SealExtensionModule extends DefaultExtensionModule {
    private RecognizePlugin recognize;

    @Override
    public void onAttachedToExtension(RongExtension extension) {
        recognize = new RecognizePlugin();
        recognize.init(extension.getContext());
        super.onAttachedToExtension(extension);
    }

    @Override
    public void onDetachedFromExtension() {
        if (recognize != null) {
            recognize.destroy();
            recognize = null;
        }
        super.onDetachedFromExtension();
    }

    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
        List<IPluginModule> pluginModules =  super.getPluginModules(conversationType);
        pluginModules.add(recognize);
        return pluginModules;
    }

    @Override
    public List<IEmoticonTab> getEmoticonTabs() {
        return super.getEmoticonTabs();
    }
}
