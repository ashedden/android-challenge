package guidebook.allisonshedden.android_challenge;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import guidebook.allisonshedden.android_challenge.models.DataModel;


public class MainActivity extends AppCompatActivity {

    private TextView tvData;
    private ListView lvData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);

        lvData = (ListView)findViewById(R.id.lvData);

        //Make buttons and Text View
        Button btnHit = (Button)findViewById(R.id.btnGetInfo);
//        tvData = (TextView)findViewById(R.id.tvJsonItem);
//        tvData.setMovementMethod(new ScrollingMovementMethod());

        btnHit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start JSON read-in
                new JSONTask().execute("https://guidebook.com/service/v2/upcomingGuides/");
            }
        });
    }

    public class JSONTask extends AsyncTask<String, String, List<DataModel>> {
        @Override
        protected List<DataModel> doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                //Open and start reading in information, and storing in buffers
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String finalJson = buffer.toString();

                //Make JSONObject and array
                JSONObject parentObject = new JSONObject(finalJson);
                JSONArray parentArray = parentObject.getJSONArray("data");

                List<DataModel> dataModelList = new ArrayList<>();
                for(int i=0; i<parentArray.length(); i++) {
                    JSONObject finalObject = parentArray.getJSONObject(i);
                    DataModel dataModel = new DataModel();
                    dataModel.setStartDate(finalObject.getString("startDate"));
                    dataModel.setEndDate(finalObject.getString("endDate"));
                    dataModel.setName(finalObject.getString("name"));
                    dataModel.setUrl(finalObject.getString("url"));
                    dataModel.setIcon(finalObject.getString("icon"));

                    dataModelList.add(dataModel);
                }
                return dataModelList;

                //Catch all the exceptions
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(List<DataModel> result){
            super.onPostExecute(result);
//            tvData.setText(result);
            DataAdapter adapter = new DataAdapter(getApplicationContext(), R.layout.eventwithicon, result);
            lvData.setAdapter(adapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_get_info){
            //Start JSON read-in
            new JSONTask().execute("https://guidebook.com/service/v2/upcomingGuides/");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class DataAdapter extends ArrayAdapter {

        public List<DataModel> dataModelList;
        private int resource;
        private LayoutInflater inflater;
        public DataAdapter(Context context, int resource, List<DataModel> objects) {
            super(context, resource, objects);
            dataModelList = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){

            if(convertView == null){
                convertView = inflater.inflate(resource, null);
            }

            ImageView ivIcon;
            TextView tvName;
            TextView tvDate;
            TextView tvUrl;

            ivIcon = (ImageView)convertView.findViewById(R.id.ivIcon);
            tvName = (TextView)convertView.findViewById(R.id.tvName);
            tvDate = (TextView)convertView.findViewById(R.id.tvDate);
            tvUrl = (TextView)convertView.findViewById(R.id.tvUrl);

            ImageLoader.getInstance().displayImage(dataModelList.get(position).getIcon(), ivIcon);

            tvName.setText(dataModelList.get(position).getName());
            tvDate.setText("Dates:  " + dataModelList.get(position).getStartDate() + " - " + dataModelList.get(position).getEndDate());

//            link.setText(Html.fromHtml(linkText));
            tvUrl.setText("URL:  " + Html.fromHtml("https://guidebook.com" + dataModelList.get(position).getUrl()));
//            link.setMovementMethod(LinkMovementMethod.getInstance());
            tvUrl.setMovementMethod(LinkMovementMethod.getInstance());

            return convertView;
        }
    }

}
