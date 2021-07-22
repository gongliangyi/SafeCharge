package com.example.chargemonitor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<Information>> cardInfoHolder;
    private final MutableLiveData<ChargeState> mainCardHolder;

    public MainViewModel() {
        cardInfoHolder = new MutableLiveData<>();
        cardInfoHolder.setValue(new ArrayList<>());
        mainCardHolder = new MutableLiveData<>();
    }


    public LiveData<ArrayList<Information>> getCardInfo() { return cardInfoHolder; }

    public MutableLiveData<ChargeState> getMainCard() { return mainCardHolder; }

    public void setCardInfo(ArrayList<Information> cardInfo) { cardInfoHolder.setValue(cardInfo); }

    public void setMainCard(ChargeState chargeState) { mainCardHolder.setValue(chargeState); }
}
