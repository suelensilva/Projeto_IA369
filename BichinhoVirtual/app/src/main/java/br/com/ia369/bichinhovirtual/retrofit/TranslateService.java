package br.com.ia369.bichinhovirtual.retrofit;

import java.util.Map;

import br.com.ia369.bichinhovirtual.model.TranslationResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface TranslateService {
    @GET("translate")
    Call<TranslationResponse> translate(@QueryMap Map<String, String> options);
}
