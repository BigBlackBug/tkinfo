package com.testwidget.dataprovider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.testwidget.CardDescriptor;

import android.content.Context;
import android.util.Log;

public class DataProvider {

	private static final String EXTENTION = ".card";

	private TraversableMap<String, CardDescriptor> data;
	private File cardDirectory;

	public DataProvider(Context context) {
		this.data = new TraversableMap<String, CardDescriptor>();
		cardDirectory = context.getFilesDir();
		Log.i("provider", cardDirectory.getAbsolutePath());
		loadAllCardDescriptors(cardDirectory);
	}

	private void loadAllCardDescriptors(File filesDir) {
		for (File file : filesDir.listFiles()) {
			if (file.getName().endsWith(EXTENTION)) {
				try {
					CardDescriptor cardInfo = loadCardDescriptor(file);
					Log.i("provider", "found "+cardInfo.getCardNumber());
					data.put(cardInfo.getCardNumber(), cardInfo);
				} catch (CardLoadingException e) {
					Log.i("dp", "error loading card "+file.getName());
					// TODO handler
				} catch (IOException e) {
					Log.i("dp", " io error loading card "+file.getName());
					// TODO handler
				}
			}
		}
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public CardDescriptor next() throws NoDataException {
		if (isEmpty()) {
			throw new NoDataException();
		}
		return data.next();
	}

	public CardDescriptor previous() throws NoDataException {
		if (isEmpty()) {
			throw new NoDataException();
		}
		return data.previous();
	}

	public CardDescriptor getCurrent() throws NoDataException {
		if (isEmpty()) {
			throw new NoDataException();
		}
		return data.current();
	}

	public int size() {
		return data.size();
	}

	public CardDescriptor getByNumber(String cardNumber) {
		return data.get(cardNumber);
	}

	private String getNewCardFile(String cardNumber) {
		return cardDirectory.getAbsolutePath() + "/" + cardNumber + EXTENTION;
	}

	public void saveOrUpdateCard(CardDescriptor descriptor) {
		data.put(descriptor.getCardNumber(), descriptor);
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(getNewCardFile(descriptor.getCardNumber())));
			Log.i("widget", "saving file under ("
					+ getNewCardFile(descriptor.getCardNumber()) + ")");
			oos.writeObject(descriptor);
		} catch (Exception ex) {
			// TODO handler. unable to persist blablabla
		}
	}
	
	public void saveCard(CardDescriptor descriptor) throws DuplicateCardException{
		if(data.containsKey(descriptor.getCardNumber())){
			throw new DuplicateCardException();
		}
		saveOrUpdateCard(descriptor);
	}

	private CardDescriptor loadCardDescriptor(File cardFile)
			throws IOException, CardLoadingException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				cardFile));
		try {
			Object cardInfo = in.readObject();
			return (CardDescriptor) cardInfo;
		} catch (Exception e) {
			Log.i("dp", "error loading card ",e);
			throw new CardLoadingException();
		}
	}

	public static class CardLoadingException extends RuntimeException {

	}

	public static class NoDataException extends RuntimeException {

	}
	
	public static class DuplicateCardException extends RuntimeException {

	}
}
