package lucas.vegi.coletapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import lucas.vegi.coletapp.utils.BancoDados;

public class MapaFragment extends Fragment implements OnMapReadyCallback {
	private static View rootView;
    private GoogleMap mapa;
    public BancoDados bd;
    private MapFragment mapFragment;
    private LatLng POSICAO_ATUAL;

    public void plotarMarcadoresCheckIn(){
        try {
            bd = BancoDados.getINSTANCE(getActivity());
            Cursor c = bd.buscar("Ponto", new String[]{"nome", "latitude", "longitude","dt_coleta", "hr_coleta"}, "", "");

            if (c.getCount() > 0) {
                LatLng posicao;
                while (c.moveToNext()) {
                    int indexLocal = c.getColumnIndex("nome");
                    int indexLatitude = c.getColumnIndex("latitude");
                    int indexLongitude = c.getColumnIndex("longitude");
                    int indexDtColeta = c.getColumnIndex("dt_coleta");
                    int indexHrColeta = c.getColumnIndex("hr_coleta");

                    double latitude = Double.parseDouble(c.getString(indexLatitude));
                    double longitude = Double.parseDouble(c.getString(indexLongitude));
                    String titulo = c.getString(indexLocal);
                    String dtColeta = c.getString(indexDtColeta);
                    String hrColeta = c.getString(indexHrColeta);

                    posicao = new LatLng(latitude, longitude);

                    mapa.addMarker(new MarkerOptions().position(posicao).title(titulo).snippet("Data: " + dtColeta + " Hora: " + hrColeta));
                }
            }
            c.close();
        }catch (Exception e){
            Log.i("ERRO","Erro ao tentar plotar os pontos no mapa "+e.getMessage());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        Log.i("ERRO", "PASSEI AQUI 4");
        if(mapa != null) {
            Log.i("MAPA", "Obteve o mapa");

            mapa.setMyLocationEnabled(true);
            mapa.setBuildingsEnabled(true);

            if(Principal.loc != null){
                //usa variavel global
                POSICAO_ATUAL = new LatLng(Principal.loc.getLatitude(),Principal.loc.getLongitude());

                //centraliza camera na posição atual
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(POSICAO_ATUAL, 16);
                mapa.animateCamera(update);
            }

            plotarMarcadoresCheckIn();

            //adiciona marrcador
            /*mapa.addMarker(new MarkerOptions().position(POSICAO_ATUAL).title("Posição Atual")
                 .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));*/
        }
        Log.i("ERRO", "PASSEI AQUI 5");
    }

	public MapaFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView != null) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
                parent.removeView(rootView);
        }
        try {
            rootView = inflater.inflate(R.layout.fragment_mapa, container, false);

        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //tentar obter o mapa de forma assíncrona
        //mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.mapa);

        mapFragment = getMapFragment();

        if(mapFragment != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            onMapReadyAntigo();
        }else if(mapFragment != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mapFragment.getMapAsync(this);
        }

    }

    private void onMapReadyAntigo(){
        mapa = mapFragment.getMap();
        if(mapa != null) {
            Log.i("MAPA", "Obteve o mapa");

            mapa.setMyLocationEnabled(true);
            mapa.setBuildingsEnabled(true);

            if(Principal.loc != null){
                //usa variavel global
                POSICAO_ATUAL = new LatLng(Principal.loc.getLatitude(),Principal.loc.getLongitude());

                //centraliza camera na posição atual
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(POSICAO_ATUAL, 16);
                mapa.animateCamera(update);
            }

            plotarMarcadoresCheckIn();
        }
    }

    //Decide a forma de Recuperar o mapFragment baseado na versão do android
    private MapFragment getMapFragment() {
        FragmentManager fm = null;

        Log.d("MAPA", "sdk: " + Build.VERSION.SDK_INT);
        Log.d("MAPA", "release: " + Build.VERSION.RELEASE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.d("MAPA", "using getFragmentManager");
            fm = getFragmentManager();
        } else {
            Log.d("MAPA", "using getChildFragmentManager");
            fm = getChildFragmentManager();
        }

        return (MapFragment) fm.findFragmentById(R.id.mapa);
    }

}
