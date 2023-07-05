package ravioli.gravioli.customitem.action;

import ravioli.gravioli.customitem.action.event.CustomItemClickAirEvent;
import ravioli.gravioli.customitem.action.event.CustomItemClickBlockEvent;
import ravioli.gravioli.customitem.action.event.CustomItemEvent;

public final class CustomItemAction<T extends CustomItemEvent> {
    public static final CustomItemAction<CustomItemClickBlockEvent> CLICK_BLOCK = new CustomItemAction<>();
    public static final CustomItemAction<CustomItemClickAirEvent> CLICK_AIR = new CustomItemAction<>();
}
