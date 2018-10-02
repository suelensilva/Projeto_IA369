package br.com.ia369.bichinhovirtual.retrofit;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface IbmNluService {
    @POST("analyze")
    Call<ResponseBody> analyse(@QueryMap Map<String, String> options,
                               @Body RequestBody requestBody);
}
