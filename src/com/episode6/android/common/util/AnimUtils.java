package com.episode6.android.common.util;

import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class AnimUtils {
	
	public static Animation createSlideAnimation(boolean relToParent, boolean horizontal, boolean fromLeftOrTop, boolean exiting, int duration) {
		int rel = (relToParent ? Animation.RELATIVE_TO_PARENT : Animation.RELATIVE_TO_SELF);
		float movingFrom = (exiting ? 0f : (fromLeftOrTop ? -1f : 1f));
		float movingTo = (exiting ? (fromLeftOrTop ? 1f : -1f) : 0f);
		TranslateAnimation anim;
		if (horizontal) {
			anim = new TranslateAnimation(rel, movingFrom, rel, movingTo, rel, 0, rel, 0);
		} else {
			anim = new TranslateAnimation(rel, 0, rel, 0, rel, movingFrom, rel, movingTo);
		}
		anim.setDuration(duration);
		return anim;
	}

}
