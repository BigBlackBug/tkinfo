package com.qbix.tkinfo.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CardDescriptor implements Serializable {// 10+2 fields
	private static final String CURRENCY = "рублей";

	private static final long serialVersionUID = 7040643686085096126L;

	private static final SimpleDateFormat FORMATTER = new SimpleDateFormat(
			"dd.MM.yyyy");
	private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat(
			"HH:mm:ss");
	private static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat(
			"HH:mm:ss dd.MM.yyyy");

	private int balance;
	private DateWrapper lastUpdated;

	private DateWrapper activationDate;
	private DateWrapper validUntil;

	private String cardName;

	private String cardNumber;
	private String cardType;

	private LastUsageInfo lastUsageInfo;
	private RechargeInfo rechargeInfo;

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	public String getBalanceString() {
		return balance + " " + CURRENCY;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = new DateWrapper(lastUpdated);
	}

	public DateWrapper getLastUpdated() {
		return lastUpdated;
	}

	public DateWrapper getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(Date activationDate) {
		this.activationDate = new DateWrapper(activationDate);
		Calendar c = Calendar.getInstance();
		c.setTime(activationDate);
		c.add(Calendar.YEAR, 2);
		this.validUntil = new DateWrapper(c.getTime());
	}

	public DateWrapper getValidUntil() {
		return validUntil;
	}

	public String getCardName() {
		return cardName;
	}

	public void setCardName(String cardName) {
		this.cardName = cardName;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public LastUsageInfo getLastUsageInfo() {
		return lastUsageInfo;
	}

	public void setLastUsageInfo(LastUsageInfo lastUsageInfo) {
		this.lastUsageInfo = lastUsageInfo;
	}

	public RechargeInfo getRechargeInfo() {
		return rechargeInfo;
	}

	public void setRechargeInfo(RechargeInfo rechargeInfo) {
		this.rechargeInfo = rechargeInfo;
	}

	public static class LastUsageInfo implements Serializable {
		private static final long serialVersionUID = -2767015955392136972L;

		private DateWrapper date;

		public void setDate(Date date) {
			this.date = new DateWrapper(date);
		}

		public DateWrapper getDate() {
			return date;
		}

		public String getTransportType() {
			return transportType;
		}

		public void setTransportType(String transportType) {
			this.transportType = transportType;
		}

		public String getTransportNumber() {
			return transportNumber;
		}

		public void setTransportNumber(String transportNumber) {
			this.transportNumber = transportNumber;
		}

		public String getOperationType() {
			return operationType;
		}

		public void setOperationType(String operationType) {
			this.operationType = operationType;
		}

		private String transportType;
		private String transportNumber;
		private String operationType;
	}

	public static class RechargeInfo implements Serializable {
		private static final long serialVersionUID = 9029396777518844009L;

		private DateWrapper rechargeDate;

		public DateWrapper getRechargeDate() {
			return rechargeDate;
		}

		public void setRechargeDate(Date rechargeDate) {
			this.rechargeDate = new DateWrapper(rechargeDate);
		}

		public String getRechargeLocation() {
			return rechargeLocation;
		}

		public void setRechargeLocation(String rechargeLocation) {
			this.rechargeLocation = rechargeLocation;
		}

		public int getRechargeAmount() {
			return rechargeAmount;
		}

		public void setRechargeAmount(int rechargeAmount) {
			this.rechargeAmount = rechargeAmount;
		}
		
		public String getRechargeAmountString() {
			return rechargeAmount + " " + CURRENCY;
		}

		private String rechargeLocation;
		private int rechargeAmount;
	}

	public static class DateWrapper implements Serializable {
		private static final long serialVersionUID = 5595140810231500757L;

		private final Date date;

		public DateWrapper(Date date) {
			this.date = date;
		}

		public Date getDate() {
			return date;
		}

		public String getFormattedString() {
			return DATE_TIME_FORMATTER.format(date);
		}
	}
//
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((cardNumber == null) ? 0 : cardNumber.hashCode());
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj) {
//			return true;
//		}
//		if (obj == null) {
//			return false;
//		}
//		if (getClass() != obj.getClass()) {
//			return false;
//		}
//		CardDescriptor other = (CardDescriptor) obj;
//		if (cardNumber == null) {
//			if (other.cardNumber != null) {
//				return false;
//			}
//		} else if (!cardNumber.equals(other.cardNumber)) {
//			return false;
//		}
//		return true;
//	}
	
	

}
