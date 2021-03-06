package com.qbix.tkinfo.model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.qbix.tkinfo.R;
import com.qbix.tkinfo.model.CardDescriptor.LastUsageInfo;
import com.qbix.tkinfo.model.CardDescriptor.RechargeInfo;

public class TranskartManager {

	private static final String URL = "http://81.23.146.8/default.aspx";
	private static final String IMAGE_FORMAT = ".png";
	private static final String TAG = "transkart_manager";
	
	private Context context;
	private Resources resources;

	public TranskartManager(Context context) {
		this.context = context;
		this.resources = context.getResources();
	}

	public class TranskartSession {
		private static final String MONEY_VALUE_SPLITTER = "тар.ед.";
		private static final String SRC_TAG = "src";
		private static final String IMAGE_TAG = "img";
		private static final String VIEW_STATE = "__VIEWSTATE";
		private static final String EVENT_VALIDATION = "__EVENTVALIDATION";
		private static final int REQUEST_DELAY = 5000;

		private final Document initialDocument;
		private final String eventValidation;
		private final String viewState;
		
		private final long nextRequestTime;

		public TranskartSession(Document initialDocument) {
			this.initialDocument = initialDocument;
			this.eventValidation = initialDocument.select("#"+EVENT_VALIDATION)
					.val();
			this.viewState = initialDocument.select("#"+VIEW_STATE).val();
			this.nextRequestTime = System.currentTimeMillis() + REQUEST_DELAY;
		}

		public Drawable getCaptcha() throws IOException {
			File captcha = grabCaptcha();
			Drawable drawable = Drawable.createFromPath(captcha.getAbsolutePath());
			captcha.delete();
			return drawable;
		}

		private File grabCaptcha() throws IOException {
			Elements img = initialDocument.getElementsByTag(IMAGE_TAG);
			if (img.size() != 1) {
				throw new RuntimeException(
						"the format of the page has changed. contact the developer");
			}

			String absUrl = img.get(0).absUrl(SRC_TAG);
			File downloadImage = downloadImage(absUrl);
			return downloadImage;
		}

		private CardDescriptor getGeneralCard(Element table, String cardNumber) {
			CardDescriptor cd = new CardDescriptor();
			String cardType = getValueOfRow(table, 0);
			String balance = getValueOfRow(table, 2);
			String lastUsedDateString = getValueOfRow(table, 3);
			String transportNumber = getValueOfRow(table, 4);
			transportNumber = transportNumber.substring(0,
					transportNumber.length() - 8);
			String transportType = getValueOfRow(table, 5);
			String operationType = getValueOfRow(table, 6);
			String rechargeDateString = getValueOfRow(table, 7);
			String rechargeLocation = getValueOfRow(table, 8);
			String rechargeAmount = getValueOfRow(table, 9);

			cd.setCardType(cardType);
			cd.setCardNumber(cardNumber);

			cd.setBalance(getMoneyValue(balance));

			cd.setActivationDate(parseDate(rechargeDateString));
			LastUsageInfo lastUsageInfo = new LastUsageInfo();
			lastUsageInfo.setOperationType(operationType);
			lastUsageInfo.setDate(parseDate(lastUsedDateString));
			lastUsageInfo.setTransportNumber(transportNumber);
			lastUsageInfo.setTransportType(transportType);
			cd.setLastUsageInfo(lastUsageInfo);

			RechargeInfo rechargeInfo = new RechargeInfo();
			rechargeInfo.setRechargeAmount(getMoneyValue(rechargeAmount));
			rechargeInfo.setRechargeDate(parseDate(rechargeDateString));
			rechargeInfo.setRechargeLocation(rechargeLocation);
			cd.setRechargeInfo(rechargeInfo);
			return cd;

		}

		public CardDescriptor getCardDescriptor(String captcha, String cardNumber)
				throws IOException, DocumentValidationException, InterruptedException {
			long sleepTime = nextRequestTime - System.currentTimeMillis();
			Log.i(TAG, "sleep time " +sleepTime);
			if(sleepTime > 0){
				Thread.sleep(sleepTime);
			}
			Document document = getCardDocument(captcha, cardNumber);
			CardDescriptor cd = new CardDescriptor();

			Element table = document.select("table").get(2).select("tbody").get(0);
			Elements select = table.select(".FieldHeader");
//			Log.i("MANAGER", select.size()+"");
			try {
				if(select.size() == 10){
					cd = getGeneralCard(table,cardNumber);
				}else if(select.size() == 8){
					cd = getUnlimitedCard(table,cardNumber);
				}else{
					throw new RuntimeException();
				}
			} catch (Exception ex) {
				throw new DocumentValidationException(resources.getString(
						R.string.error_fetching_data, cardNumber));
			}			
			cd.setLastUpdated(new Date());
			return cd;
		}
		private CardDescriptor getUnlimitedCard(Element table, String cardNumber) {
			CardDescriptor cd = new CardDescriptor();
			String cardType = getValueOfRow(table, 0);
//			String balance = getValueOfRow(table, 2);
			String lastUsedDateString = getValueOfRow(table, 2);
			String transportNumber = getValueOfRow(table, 3);
			transportNumber = transportNumber.substring(0,
					transportNumber.length() - 8);
			String transportType = getValueOfRow(table, 4);
			String operationType = getValueOfRow(table, 5);
			String rechargeDateString = getValueOfRow(table,6);
			String rechargeLocation = getValueOfRow(table, 7);
//			String rechargeAmount = getValueOfRow(table, 9);

			cd.setCardType(cardType);
			cd.setCardNumber(cardNumber);

			cd.setBalance(-1);//TODO support unlimited

			cd.setActivationDate(parseDate(rechargeDateString));
			LastUsageInfo lastUsageInfo = new LastUsageInfo();
			lastUsageInfo.setOperationType(operationType);
			lastUsageInfo.setDate(parseDate(lastUsedDateString));
			lastUsageInfo.setTransportNumber(transportNumber);
			lastUsageInfo.setTransportType(transportType);
			cd.setLastUsageInfo(lastUsageInfo);

			RechargeInfo rechargeInfo = new RechargeInfo();
			rechargeInfo.setRechargeAmount(-1);//TODO support unlimited;
			rechargeInfo.setRechargeDate(parseDate(rechargeDateString));
			rechargeInfo.setRechargeLocation(rechargeLocation);
			cd.setRechargeInfo(rechargeInfo);
			return cd;
		}

		private int getMoneyValue(String moneyString){
			String[] values = moneyString.split(MONEY_VALUE_SPLITTER);
			return Integer.parseInt(values[0].trim());
		}
		private String getValueOfRow(Element table,int row){
			return table.select("tr").get(row)
					.select("td").get(1).select("b").html();
		}
		
		private Date parseDate(String dateString) {
			SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			try {
				return formatter.parse(dateString);
			} catch (ParseException e) {
				Log.d(TAG,"SUDDENLY PARSING FAILED");
				// never gonna happen
			}
			return null;
		}
		
		private Document getCardDocument(String captcha, String cardNumber)
				throws IOException, DocumentValidationException {
			Document doc = Jsoup.connect(URL).data("checkcode", captcha)
					.data("cardnum", cardNumber).data(VIEW_STATE, viewState)
					.data(EVENT_VALIDATION, eventValidation).post();
			validateDocument(doc, cardNumber);

			return doc;
		}

		private File downloadImage(String srcUrl) throws IOException {
			System.out.println(srcUrl);
			URL url = new URL(srcUrl);
			File captchaFile = File.createTempFile("TKM", IMAGE_FORMAT,
					context.getCacheDir());
			InputStream in = url.openStream();
			OutputStream out = new BufferedOutputStream(new FileOutputStream(
					captchaFile));

			for (int b; (b = in.read()) != -1;) {
				out.write(b);
			}
			out.close();
			in.close();
			return captchaFile;
		}

		private void validateDocument(Document doc, String cardNumber)
				throws DocumentValidationException {
			Elements errors = doc.select(".ErrorMessage");
			if (!errors.isEmpty()) {
				// throw new RuntimeException(errors.html());
				throw new DocumentValidationException(resources.getString(
						R.string.no_such_card, cardNumber));
			}
			Elements error2 = doc.select("#CustomValidator1");
			if (!error2.isEmpty()) {
				throw new DocumentValidationException(
						resources.getString(R.string.incorrect_captcha));
				// throw new RuntimeException(error2.select("font").html());
			}
		}

	}

	public TranskartSession startSession() throws IOException {
		Document doc = Jsoup.connect(URL).get();
		return new TranskartSession(doc);
	}
	
	public class DocumentValidationException extends Exception {
		public DocumentValidationException(String message) {
			super(message);
		}
	}
	
}