package com.example.coderescue.navar.onboarding;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;
import com.example.coderescue.R;

public class DefaultIntro extends AppIntro {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        SliderPage page1 = new SliderPage();
        page1.setTitle(getResources().getString(R.string.slide1_title));
        page1.setDescription(getResources().getString(R.string.slide1_desc));
        page1.setImageDrawable(R.drawable.slide1);
        page1.setBgColor(getResources().getColor(R.color.colorPrimary));
        addSlide(AppIntroFragment.newInstance(page1));

        SliderPage page2 = new SliderPage();
        page2.setTitle(getResources().getString(R.string.slide2_title));
        page2.setDescription(getResources().getString(R.string.slide2_desc));
        page2.setImageDrawable(R.drawable.slide2);
        page2.setBgColor(getResources().getColor(R.color.colorPrimary));
        addSlide(AppIntroFragment.newInstance(page2));

        SliderPage page3 = new SliderPage();
        page3.setTitle(getResources().getString(R.string.slide3_title));
        page3.setDescription(getResources().getString(R.string.slide3_desc));
        page3.setImageDrawable(R.drawable.slide3);
        page3.setBgColor(getResources().getColor(R.color.colorPrimary));
        addSlide(AppIntroFragment.newInstance(page3));

        setBarColor(getResources().getColor(R.color.colorAccent));
        showSkipButton(false);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }
}
