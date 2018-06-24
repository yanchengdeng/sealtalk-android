package cn.rongcloud.im.message.module;

import android.content.Context;
import android.content.res.Resources;
import android.view.KeyEvent;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import io.rong.common.RLog;
import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongExtension;
import io.rong.imkit.emoticon.EmojiTab;
import io.rong.imkit.emoticon.IEmojiItemClickListener;
import io.rong.imkit.emoticon.IEmoticonTab;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.plugin.ImagePlugin;
import io.rong.imkit.widget.provider.FilePlugin;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

public class AmapAndGoogleExtentionsModule
implements IExtensionModule

    {
        private static final String TAG = DefaultExtensionModule.class.getSimpleName();
        private EditText mEditText;
        private Stack<EditText> stack;
        String[] types = null;

    public AmapAndGoogleExtentionsModule() {
        }

        public void onInit(String appKey) {
            this.stack = new Stack();
        }

        public void onConnect(String token) {
        }

        public void onAttachedToExtension(RongExtension extension) {
            this.mEditText = extension.getInputEditText();
            Context context = extension.getContext();
            RLog.i(TAG, "attach " + this.stack.size());
            this.stack.push(this.mEditText);
            Resources resources = context.getResources();

            try {
                this.types = resources.getStringArray(resources.getIdentifier("rc_realtime_support_conversation_types", "array", context.getPackageName()));
            } catch (Resources.NotFoundException var5) {
                RLog.i(TAG, "not config rc_realtime_support_conversation_types in rc_config.xml");
            }

        }

        public void onDetachedFromExtension() {
            RLog.i(TAG, "detach " + this.stack.size());
            if (this.stack.size() > 0) {
                this.stack.pop();
                this.mEditText = this.stack.size() > 0 ? (EditText)this.stack.peek() : null;
            }

        }

        public void onReceivedMessage(Message message) {
        }

        public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
            List<IPluginModule> pluginModuleList = new ArrayList();
            IPluginModule image = new ImagePlugin();
            IPluginModule file = new FilePlugin();
            IPluginModule locationPlugin = new AmapAndGoogleMapLocationPlugin();
           /* pluginModuleList.add(image);

            try {
                String clsName = "com.amap.api.netlocation.AMapNetworkLocationClient";
                Class<?> locationCls = Class.forName(clsName);
                if (locationCls != null) {
                    IPluginModule combineLocation = new CombineLocationPlugin();
                    IPluginModule locationPlugin = new AmapAndGoogleMapLocationPlugin();
                    boolean typesDefined = false;
                    if (this.types != null && this.types.length > 0) {
                        String[] var10 = this.types;
                        int var11 = var10.length;

                        for(int var12 = 0; var12 < var11; ++var12) {
                            String type = var10[var12];
                            if (conversationType.getName().equals(type)) {
                                typesDefined = true;
                                break;
                            }
                        }
                    }

//                    if (typesDefined) {
//                        pluginModuleList.add(combineLocation);
//                    } else if (this.types == null && conversationType.equals(Conversation.ConversationType.PRIVATE)) {
//                        pluginModuleList.add(combineLocation);
//                    } else {
//                        pluginModuleList.add(locationPlugin);
//                    }
                }
            } catch (Exception var14) {
                RLog.i(TAG, "Not include AMap");
                var14.printStackTrace();
            }

            if (conversationType.equals(Conversation.ConversationType.GROUP) || conversationType.equals(Conversation.ConversationType.DISCUSSION) || conversationType.equals(Conversation.ConversationType.PRIVATE)) {
                pluginModuleList.addAll(InternalModuleManager.getInstance().getExternalPlugins(conversationType));
            }

            pluginModuleList.add(file);*/
            pluginModuleList.add(locationPlugin);
            return pluginModuleList;
        }

        public List<IEmoticonTab> getEmoticonTabs() {
            EmojiTab emojiTab = new EmojiTab();
            emojiTab.setOnItemClickListener(new IEmojiItemClickListener() {
                public void onEmojiClick(String emoji) {
                    int start = AmapAndGoogleExtentionsModule.this.mEditText.getSelectionStart();
                    AmapAndGoogleExtentionsModule.this.mEditText.getText().insert(start, emoji);
                }

                public void onDeleteClick() {
                    AmapAndGoogleExtentionsModule.this.mEditText.dispatchKeyEvent(new KeyEvent(0, 67));
                }
            });
            List<IEmoticonTab> list = new ArrayList();
            list.add(emojiTab);
            return list;
        }

        public void onDisconnect() {
        }
    }
