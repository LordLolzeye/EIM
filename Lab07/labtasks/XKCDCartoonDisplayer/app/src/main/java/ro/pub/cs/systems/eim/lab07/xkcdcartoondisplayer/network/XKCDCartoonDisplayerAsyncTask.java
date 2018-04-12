package ro.pub.cs.systems.eim.lab07.xkcdcartoondisplayer.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import ro.pub.cs.systems.eim.lab07.xkcdcartoondisplayer.entities.XKCDCartoonInformation;
import ro.pub.cs.systems.eim.lab07.xkcdcartoondisplayer.general.Constants;

public class XKCDCartoonDisplayerAsyncTask extends AsyncTask<String, Void, XKCDCartoonInformation> {

    private TextView xkcdCartoonTitleTextView;
    private ImageView xkcdCartoonImageView;
    private TextView xkcdCartoonUrlTextView;
    private Button previousButton, nextButton;

    private class XKCDCartoonButtonClickListener implements Button.OnClickListener {

        private String xkcdComicUrl;

        public XKCDCartoonButtonClickListener(String xkcdComicUrl) {
            this.xkcdComicUrl = xkcdComicUrl;
        }

        @Override
        public void onClick(View view) {
            new XKCDCartoonDisplayerAsyncTask(xkcdCartoonTitleTextView, xkcdCartoonImageView, xkcdCartoonUrlTextView, previousButton, nextButton).execute(xkcdComicUrl);
        }

    }

    public XKCDCartoonDisplayerAsyncTask(TextView xkcdCartoonTitleTextView, ImageView xkcdCartoonImageView, TextView xkcdCartoonUrlTextView, Button previousButton, Button nextButton) {
        this.xkcdCartoonTitleTextView = xkcdCartoonTitleTextView;
        this.xkcdCartoonImageView = xkcdCartoonImageView;
        this.xkcdCartoonUrlTextView = xkcdCartoonUrlTextView;
        this.previousButton = previousButton;
        this.nextButton = nextButton;
    }

    @Override
    public XKCDCartoonInformation doInBackground(String... urls) {
        XKCDCartoonInformation xkcdCartoonInformation = new XKCDCartoonInformation();

        // TODO exercise 5a)
        // 1. obtain the content of the web page (whose Internet address is stored in urls[0])
        // - create an instance of a HttpClient object
        // - create an instance of a HttpGet object
        // - create an instance of a ResponseHandler object
        // - execute the request, thus obtaining the web page source code
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGetXKCD = new HttpGet(urls[0]);

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String dom = null;
        try {
            dom = httpClient.execute(httpGetXK CD, responseHandler);
        } catch (ClientProtocolException clientProtocolException) {
            // do nada
        } catch (IOException ioException) {
            // do nada
        }

        // 2. parse the web page source code
        // - cartoon title: get the tag whose id equals "ctitle"
        // - cartoon url
        //   * get the first tag whose id equals "comic"
        //   * get the embedded <img> tag
        //   * get the value of the attribute "src"
        //   * prepend the protocol: "http:"
        // - cartoon bitmap (only if using Apache HTTP Components)
        //   * create the HttpGet object
        //   * execute the request and obtain the HttpResponse object
        //   * get the HttpEntity object from the response
        //   * get the bitmap from the HttpEntity stream (obtained by getContent()) using Bitmap.decodeStream() method
        // - previous cartoon address
        //   * get the first tag whole rel attribute equals "prev"
        //   * get the href attribute of the tag
        //   * prepend the value with the base url: http://www.xkcd.com
        //   * attach the previous button a click listener with the address attached
        // - next cartoon address
        //   * get the first tag whole rel attribute equals "next"
        //   * get the href attribute of the tag
        //   * prepend the value with the base url: http://www.xkcd.com
        //   * attach the next button a click listener with the address attached

        if (dom != null) {
            Document document = Jsoup.parse(dom);
            Element htmlTag = document.child(0);

            Element divTagIdCtitle = htmlTag.getElementsByAttributeValue(Constants.ID_ATTRIBUTE, Constants.CTITLE_VALUE).first();
            xkcdCartoonInformation.setCartoonTitle(divTagIdCtitle.ownText());

            Element divTagIdComic = htmlTag.getElementsByAttributeValue(Constants.ID_ATTRIBUTE, Constants.COMIC_VALUE).first();
            String cartoonInternetAddress = divTagIdComic.getElementsByTag(Constants.IMG_TAG).attr(Constants.SRC_ATTRIBUTE);

            String cartoonUrl = Constants.HTTP_PROTOCOL + cartoonInternetAddress;
            Log.d(Constants.TAG, cartoonUrl);
            xkcdCartoonInformation.setCartoonUrl(cartoonUrl);

            try {
                HttpGet httpGetCartoon = new HttpGet(cartoonUrl);
                HttpResponse httpResponse = httpClient.execute(httpGetCartoon);
                HttpEntity httpEntity = httpResponse.getEntity();
                if (httpEntity != null) {
                    xkcdCartoonInformation.setCartoonBitmap(BitmapFactory.decodeStream(httpEntity.getContent()));
                }
            } catch (ClientProtocolException clientProtocolException) {
                // do nada
            } catch (IOException ioException) {
                // do nada
            }

            Element aTagRelPrev = htmlTag.getElementsByAttributeValue(Constants.REL_ATTRIBUTE, Constants.PREVIOUS_VALUE).first();
            String previousCartoonInternetAddress = Constants.XKCD_INTERNET_ADDRESS + aTagRelPrev.attr(Constants.HREF_ATTRIBUTE);
            xkcdCartoonInformation.setPreviousCartoonUrl(previousCartoonInternetAddress);

            Element aTagRelNext = htmlTag.getElementsByAttributeValue(Constants.REL_ATTRIBUTE, Constants.NEXT_VALUE).first();
            String nextCartoonInternetAddress = Constants.XKCD_INTERNET_ADDRESS + aTagRelNext.attr(Constants.HREF_ATTRIBUTE);
            xkcdCartoonInformation.setNextCartoonUrl(nextCartoonInternetAddress);
        }

        return  xkcdCartoonInformation;
    }

    @Override
    protected void onPostExecute(final XKCDCartoonInformation xkcdCartoonInformation) {

        // TODO exercise 5b)
        // map each member of xkcdCartoonInformation object to the corresponding widget
        // cartoonTitle -> xkcdCartoonTitleTextView
        // cartoonBitmap -> xkcdCartoonImageView (only if using Apache HTTP Components)
        // cartoonUrl -> xkcdCartoonUrlTextView
        // based on cartoonUrl fetch the bitmap using Volley (using an ImageRequest object added to the queue)
        // and put it into xkcdCartoonImageView
        // previousCartoonUrl, nextCartoonUrl -> set the XKCDCartoonUrlButtonClickListener for previousButton, nextButton
        if (xkcdCartoonInformation != null) {
            String cartoonTitle = xkcdCartoonInformation.getCartoonTitle();
            if (cartoonTitle != null) {
                xkcdCartoonTitleTextView.setText(cartoonTitle);
            }
            Bitmap cartoonBitmap = xkcdCartoonInformation.getCartoonBitmap();
            if (cartoonBitmap != null) {
                xkcdCartoonImageView.setImageBitmap(cartoonBitmap);
            }
            String cartoonUrl = xkcdCartoonInformation.getCartoonUrl();
            if (cartoonUrl != null) {
                xkcdCartoonUrlTextView.setText(cartoonUrl);
            }
            String previousCartoonUrl = xkcdCartoonInformation.getPreviousCartoonUrl();
            if (previousCartoonUrl != null) {
                previousButton.setOnClickListener(new XKCDCartoonButtonClickListener(previousCartoonUrl));
            }
            String nextCartoonUrl = xkcdCartoonInformation.getNextCartoonUrl();
            if (nextCartoonUrl != null) {
                nextButton.setOnClickListener(new XKCDCartoonButtonClickListener(nextCartoonUrl));
            }
        }


    }

}
