package com.gathin.tvhome;

import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContract;
import android.media.tv.TvInputManager;

/** Switches the TV to an input, shared by input tiles and the remote shortcut button. */
final class InputSwitch {

    private InputSwitch() {}

    /** Takes comma-separated input ids; returns false when nothing could be opened. */
    static boolean open(Context ctx, String ids) {
        Intent tv = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(TvContract.buildChannelUriForPassthroughInput(pick(ctx, ids)), "vnd.android.cursor.item/channel")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            ctx.startActivity(tv);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * First id the TV currently lists. List the HDMI-CEC device input first: selecting it makes the TV
     * send CEC routing to the console, which can wake it from rest mode. It disappears while the
     * console is fully off, so the raw port belongs last as the fallback.
     */
    private static String pick(Context ctx, String ids) {
        String[] all = ids.split(",");
        TvInputManager tim = (TvInputManager) ctx.getSystemService(Context.TV_INPUT_SERVICE);
        if (tim != null) {
            for (String id : all) {
                if (tim.getTvInputInfo(id.trim()) != null) return id.trim();
            }
        }
        return all[all.length - 1].trim();
    }
}
