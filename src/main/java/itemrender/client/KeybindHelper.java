package itemrender.client;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.client.settings.KeyBinding;

import java.util.Map;

public final class KeybindHelper {
    private Multimap<KeyBinding, KeybindHandler> keyHandlers = ArrayListMultimap.create();
    private Map<KeyBinding, Boolean> keyStates = Maps.newHashMap();

    public void register(KeyBinding binding, KeybindHandler handler) {
        keyHandlers.put(binding, handler);

        if(!keyStates.containsKey(binding)) {
            keyStates.put(binding, false);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick) {
        if(tick.phase == TickEvent.Phase.END) {
            for(KeyBinding key : keyStates.keySet()) {
                boolean keyState = key.isPressed();
                boolean oldKeyState = keyStates.getOrDefault(key, Boolean.FALSE);

                if(keyState && keyState != oldKeyState) {
                    for(KeybindHandler handler : keyHandlers.get(key)) {
                        handler.onKeypress();
                    }
                }

                keyStates.put(key, keyState);
            }
        }
    }
}
