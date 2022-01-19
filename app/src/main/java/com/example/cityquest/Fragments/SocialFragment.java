package com.example.cityquest.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cityquest.R;

public class SocialFragment extends Fragment {

    public SocialFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_social, container, false);

        //1) Fazer motor de pesquisa para encontrar users
        //2) Ao Clicar num, vai para a pagina dele e pode dar follo ou unfollow (criar página de user para users)
        //2) Fazer pedido à base de dados dos users que segyue

        return v;
    }
}