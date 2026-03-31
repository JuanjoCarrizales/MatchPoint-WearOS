package com.mypadelapp.matchpoint.ui;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.mypadelapp.matchpoint.logic.PartidoPadel;

public class PagerAdapter extends FragmentStateAdapter {

    private final PartidoPadel partido;

    public PagerAdapter(FragmentActivity activity, PartidoPadel partido) {
        super(activity);
        this.partido = partido;
    }

    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> MarcadorFragment.newInstance(partido);
            case 1 -> CronometroFragment.newInstance(partido);
            case 2 -> EstadisticasFragment.newInstance(partido);
            default -> MarcadorFragment.newInstance(partido);
        };
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}