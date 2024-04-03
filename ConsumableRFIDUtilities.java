package com.apldbio.ce.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.zip.CRC32;

public class ConsumableRFIDUtilities {
   public static final String SEPARATOR_CHAR = ",";
   public static final String INSTALL_SEPARATOR = "/";
   public static final String CHECKSUM_SEPARATOR = "#";
   public static final String UID_TAGINFO_SEPARATOR = "::";
   public static final long DATE_DIVISOR = 60000L;
   public static final long MILLISEC_IN_A_DAY = 86400000L;
   public static final int MAX_RFID_SIZE = 256;
   private static final String SECRET_CHECKSUM_STRING = "#rF1d*d2Ta&";
   private static final String ORIGINAL_SECRET_CHECKSUM_STRING = "&rFId*dAtA%";

   public static String createArrayBlob(int numCaps, int capLen, String partNum, String lotNum, String serialNum, long expirationDate, long initialInstallDate, String initialInstallSerialNum, int numberOfInstalls, int allowedRuns, int performedRuns, long[] installDates, String[] installSerialNums) {
      String retVal = "";
      String firstPart = "";
      if (numCaps == 8) {
         firstPart = firstPart + 1;
      } else if (numCaps == 24) {
         firstPart = firstPart + 2;
      }

      if (capLen == 36) {
         firstPart = firstPart + 1;
      } else if (capLen == 50) {
         firstPart = firstPart + 2;
      }

      firstPart = firstPart + partNum + ",";
      firstPart = firstPart + lotNum + ",";
      firstPart = firstPart + serialNum + ",";
      firstPart = firstPart + expirationDate / 60000L + ",";

      String numInstalls;
      for(numInstalls = String.valueOf(numberOfInstalls); numInstalls.length() < 2; numInstalls = "0" + numInstalls) {
      }

      String totRuns = String.valueOf(allowedRuns);

      for(firstPart = firstPart + numInstalls; totRuns.length() < 4; totRuns = "0" + totRuns) {
      }

      firstPart = firstPart + totRuns;

      String perRuns;
      for(perRuns = String.valueOf(performedRuns); perRuns.length() < 4; perRuns = "0" + perRuns) {
      }

      firstPart = firstPart + perRuns + ",";
      firstPart = firstPart + initialInstallDate / 60000L + "/" + initialInstallSerialNum;
      if (installDates.length > 0) {
         firstPart = firstPart + ",";
         int startInstall = -1;

         do {
            retVal = firstPart;
            ++startInstall;

            for(int i = startInstall; i < installDates.length; ++i) {
               retVal = retVal + installDates[i] / 60000L + "/" + installSerialNums[i] + ",";
            }

            if (installDates.length > 0) {
               retVal = retVal.substring(0, retVal.length() - 1);
            }
         } while(retVal.length() > 256);
      } else {
         retVal = firstPart;
      }

      retVal = addChecksum(retVal);
      return retVal;
   }

   public static String createArrayBlob(String tagInfo) {
      String retVal = addChecksum(tagInfo);
      return retVal;
   }

   public static int getArrayTypeFromBlob(String blob) {
      int numCaps = 0;
      if (blob != null && blob.length() > 0) {
         try {
            int capCode = new Integer(blob.substring(0, 1));
            if (capCode == 1) {
               numCaps = 8;
            } else if (capCode == 2) {
               numCaps = 24;
            }
         } catch (NumberFormatException var3) {
         }
      }

      return numCaps;
   }

   public static int getArrayLengthFromBlob(String blob) {
      int capLen = 0;
      if (blob != null && blob.length() > 1) {
         try {
            int capCode = new Integer(blob.substring(1, 2));
            if (capCode == 1) {
               capLen = 36;
            } else if (capCode == 2) {
               capLen = 50;
            }
         } catch (NumberFormatException var3) {
         }
      }

      return capLen;
   }

   public static String getArrayPartNumberFromBlob(String blob) {
      String partNum = "";
      if (blob != null && blob.length() > 2) {
         int sepPos = blob.indexOf(",");
         if (sepPos > 2) {
            partNum = blob.substring(2, sepPos);
         }
      }

      return partNum;
   }

   public static String getArrayLotNumberFromBlob(String blob) {
      String lotNum = "";
      if (blob != null) {
         String[] pieces = blob.split(",");
         if (pieces.length > 1) {
            lotNum = pieces[1];
         }
      }

      return lotNum;
   }

   public static String getArraySerialNumberFromBlob(String blob) {
      String serialNum = "";
      if (blob != null) {
         String[] pieces = blob.split(",");
         if (pieces.length > 2) {
            serialNum = pieces[2];
         }
      }

      return serialNum;
   }

   public static long getArrayExpirationDateFromBlob(String blob) {
      long expDate = 0L;
      if (blob != null) {
         String[] pieces = blob.split(",");
         if (pieces.length > 3) {
            try {
               expDate = new Long(pieces[3]);
               expDate *= 60000L;
            } catch (NumberFormatException var5) {
            }
         }
      }

      return expDate;
   }

   public static long getArrayInitialInstallDateFromBlob(String blob) {
      long insDate = 0L;
      if (blob != null) {
         String[] pieces = blob.split(",");
         if (pieces.length > 5) {
            int insDatLoc = pieces[4].indexOf("/") > 0 ? 4 : 5;

            try {
               insDate = new Long(pieces[insDatLoc].split("/")[0]);
               insDate *= 60000L;
            } catch (NumberFormatException var6) {
            }
         }
      }

      return insDate;
   }

   public static String setArrayInitialInstallDateInBlob(String blob, long insDate, String insSerialNum) {
      blob = removeChecksum(blob);
      if (blob.split(",").length < 6) {
         blob = blob + ",0/";
      }

      String[] pieces = blob.split(",");
      int datLoc = pieces[4].indexOf("/") > 0 ? 5 : 4;
      pieces[4] = "01" + pieces[datLoc].substring(2);
      pieces[5] = insDate / 60000L + "/" + insSerialNum;
      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(blob);
      return blob;
   }

   public static String getArrayInitialInstallSerialNumFromBlob(String blob) {
      String initialInstall = "";
      if (blob != null) {
         blob = removeChecksum(blob);
         String[] pieces = blob.split(",");
         if (pieces.length > 5) {
            int insDatLoc = pieces[4].indexOf("/") > 0 ? 4 : 5;
            String[] installPieces = pieces[insDatLoc].split("/");
            if (installPieces.length > 1) {
               initialInstall = installPieces[1];
            }
         }
      }

      return initialInstall;
   }

   public static int getArrayNumberOfInstallationsFromBlob(String blob) {
      int numInstalls = 0;
      if (blob != null) {
         String[] pieces = blob.split(",");
         if (pieces.length > 4) {
            int datLoc = pieces[4].indexOf("/") > 0 ? 5 : 4;

            try {
               numInstalls = new Integer(pieces[datLoc].substring(0, 2));
            } catch (NumberFormatException var5) {
            }
         }
      }

      return numInstalls;
   }

   private static String setArrayNumberOfInstallationsInBlob(String blob, int numInstalls) {
      blob = removeChecksum(blob);
      String[] pieces = blob.split(",");

      String curStr;
      for(curStr = String.valueOf(numInstalls); curStr.length() < 2; curStr = "0" + curStr) {
      }

      int datLoc = pieces[4].indexOf("/") > 0 ? 5 : 4;
      if (datLoc == 5) {
         String insDat = pieces[4];
         pieces[4] = curStr + pieces[datLoc].substring(2);
         pieces[5] = insDat;
      } else {
         pieces[4] = curStr + pieces[datLoc].substring(2);
      }

      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(blob);
      return blob;
   }

   public static int getArrayMaxRunsFromBlob(String blob) {
      int maxRuns = 0;
      if (blob != null) {
         String[] pieces = blob.split(",");
         if (pieces.length > 4) {
            int datLoc = pieces[4].indexOf("/") > 0 ? 5 : 4;

            try {
               maxRuns = new Integer(pieces[datLoc].substring(2, 6));
            } catch (NumberFormatException var5) {
            }
         }
      }

      return maxRuns;
   }

   public static int getArrayRunsPerformedFromBlob(String blob) {
      int remRuns = 0;
      if (blob != null) {
         String[] pieces = blob.split(",");
         if (pieces.length > 4) {
            int datLoc = pieces[4].indexOf("/") > 0 ? 5 : 4;

            try {
               remRuns = new Integer(pieces[datLoc].substring(6, 10));
            } catch (NumberFormatException var5) {
            }
         }
      }

      return remRuns;
   }

   public static String setArrayAdditionalRunsInBlob(String blob, int numRuns) {
      int totalRuns = getArrayRunsPerformedFromBlob(blob);
      blob = removeChecksum(blob);
      numRuns = numRuns < 0 ? 0 : numRuns;
      totalRuns += numRuns;
      String[] pieces = blob.split(",");

      String curStr;
      for(curStr = String.valueOf(totalRuns); curStr.length() < 4; curStr = "0" + curStr) {
      }

      int datLoc = pieces[4].indexOf("/") > 0 ? 5 : 4;
      if (datLoc == 5) {
         String insDat = pieces[4];
         pieces[4] = pieces[datLoc].substring(0, 6) + curStr;
         pieces[5] = insDat;
      } else {
         pieces[4] = pieces[datLoc].substring(0, 6) + curStr;
      }

      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(blob);
      return blob;
   }

   public static int getArrayRemainingRunsFromBlob(String blob) {
      int maxRuns = getArrayMaxRunsFromBlob(blob);
      int totalRuns = getArrayRunsPerformedFromBlob(blob);
      int remRuns = maxRuns - totalRuns;
      remRuns = remRuns < 0 ? 0 : remRuns;
      return remRuns;
   }

   public static long[] getArrayInstallDatesFromBlob(String blob) {
      blob = removeChecksum(blob);
      long[] retArray = new long[0];
      String[] pieces = blob.split(",");
      if (pieces.length > 5) {
         int numInstalls = pieces.length - 6;
         retArray = new long[numInstalls];

         for(int i = 0; i < numInstalls; ++i) {
            String strDate = pieces[i + 6].split("/")[0];
            long insDate = new Long(strDate);
            insDate *= 60000L;
            retArray[i] = insDate;
         }
      }

      return retArray;
   }

   public static String[] getArrayInstallSerialNumbersFromBlob(String blob) {
      blob = removeChecksum(blob);
      String[] retArray = new String[0];
      String[] pieces = blob.split(",");
      if (pieces.length > 5) {
         int numInstalls = pieces.length - 6;
         retArray = new String[numInstalls];

         for(int i = 0; i < numInstalls; ++i) {
            String dum = pieces[i + 6];
            retArray[i] = dum.split("/")[1];
         }
      }

      return retArray;
   }

   public static String addArrayInstallInformationToBlob(String blob, long installDate, String serialNumber) {
      blob = removeChecksum(blob);
      if (installDate > 0L && serialNumber.length() > 0) {
         int numInstalls = getArrayNumberOfInstallationsFromBlob(blob);
         if (numInstalls == 0) {
            blob = setArrayInitialInstallDateInBlob(blob, installDate, serialNumber);
         } else if (numInstalls < 4) {
            blob = blob + "," + installDate / 60000L + "/" + serialNumber;
            blob = setArrayNumberOfInstallationsInBlob(blob, numInstalls + 1);
         } else {
            String[] pieces = blob.split(",");
            pieces[6] = pieces[7];
            pieces[7] = pieces[8];
            pieces[8] = installDate / 60000L + "/" + serialNumber;
            blob = "";

            for(int i = 0; i < pieces.length - 1; ++i) {
               blob = blob + pieces[i] + ",";
            }

            blob = blob + pieces[pieces.length - 1];
            blob = setArrayNumberOfInstallationsInBlob(blob, numInstalls + 1);
         }
      }

      return blob;
   }

   public static String createPolymerBlob(String tagUID, String polymerType, String partNum, String lotNum, long expirationDate, long installationDate, long lifeOnInstrument, int allowedRuns, int allowedSamples, int remainingRuns, int remainingSamples, int bubbleWizardExecutions, int arrayFillExecutions, int microLitersRemaining) {
      String retVal = "";
      retVal = retVal + polymerType + ",";
      retVal = retVal + partNum + ",";
      retVal = retVal + lotNum + ",";
      retVal = retVal + expirationDate / 60000L + ",";
      retVal = retVal + installationDate / 60000L + ",";
      retVal = retVal + lifeOnInstrument / 60000L + ",";

      String totRuns;
      for(totRuns = String.valueOf(allowedRuns); totRuns.length() < 4; totRuns = "0" + totRuns) {
      }

      retVal = retVal + totRuns;

      String totSamp;
      for(totSamp = String.valueOf(allowedSamples); totSamp.length() < 4; totSamp = "0" + totSamp) {
      }

      retVal = retVal + totSamp;

      String remRuns;
      for(remRuns = String.valueOf(remainingRuns); remRuns.length() < 4; remRuns = "0" + remRuns) {
      }

      retVal = retVal + remRuns;

      String remSamp;
      for(remSamp = String.valueOf(remainingSamples); remSamp.length() < 4; remSamp = "0" + remSamp) {
      }

      retVal = retVal + remSamp;
      if (bubbleWizardExecutions >= 0 && bubbleWizardExecutions <= 9) {
         retVal = retVal + String.valueOf(bubbleWizardExecutions);
      } else {
         retVal = retVal + "9";
      }

      if (arrayFillExecutions >= 0 && arrayFillExecutions <= 9) {
         retVal = retVal + String.valueOf(arrayFillExecutions);
      } else {
         retVal = retVal + "9";
      }

      String volRemaining;
      for(volRemaining = String.valueOf(microLitersRemaining); volRemaining.length() < 5; volRemaining = "0" + volRemaining) {
      }

      retVal = retVal + volRemaining;
      retVal = addChecksum(tagUID, retVal);
      return retVal;
   }

   public static String createPolymerBlob(String tagInfo) {
      String retVal = addChecksum(tagInfo);
      return retVal;
   }

   public static String getPolymerTypeFromBlob(String blob) {
      String polymerType = "";
      String[] pieces = blob.split(",");
      if (pieces.length > 0) {
         polymerType = pieces[0];
      }

      return polymerType;
   }

   public static String getPolymerPartNumberFromBlob(String blob) {
      String partNum = "";
      String[] pieces = blob.split(",");
      if (pieces.length > 1) {
         partNum = pieces[1];
      }

      return partNum;
   }

   public static String getPolymerLotNumberFromBlob(String blob) {
      String lotNum = "";
      String[] pieces = blob.split(",");
      if (pieces.length > 2) {
         lotNum = pieces[2];
      }

      return lotNum;
   }

   public static long getPolymerExpirationDateFromBlob(String blob) {
      long expDate = 0L;
      String[] pieces = blob.split(",");
      if (pieces.length > 3) {
         try {
            expDate = new Long(pieces[3]);
            expDate *= 60000L;
         } catch (NumberFormatException var5) {
         }
      }

      return expDate;
   }

   public static long getPolymerInstallationDateFromBlob(String blob) {
      long insDate = 0L;
      String[] pieces = blob.split(",");
      if (pieces.length > 4) {
         try {
            insDate = new Long(pieces[4]);
            insDate *= 60000L;
         } catch (NumberFormatException var5) {
         }
      }

      return insDate;
   }

   public static String setPolymerInstallationDateInBlob(String tagUID, String blob, long insDate) {
      blob = removeChecksum(blob);
      String[] pieces = blob.split(",");
      pieces[4] = String.valueOf(insDate / 60000L);
      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(tagUID, blob);
      return blob;
   }

   public static long getPolymerLifeOnInstrumentFromBlob(String blob) {
      long loi = 0L;
      String[] pieces = blob.split(",");
      if (pieces.length > 5) {
         try {
            loi = new Long(pieces[5]);
            loi *= 60000L;
         } catch (NumberFormatException var5) {
         }
      }

      return loi;
   }

   public static String setPolymerLifeOnInstrumentInBlob(String tagUID, String blob, long loi) {
      blob = removeChecksum(blob);
      String[] pieces = blob.split(",");
      pieces[5] = String.valueOf(loi / 60000L);
      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(tagUID, blob);
      return blob;
   }

   public static int getPolymerAllowedRunsFromBlob(String blob) {
      int maxRuns = 0;
      String[] pieces = blob.split(",");
      if (pieces.length > 6) {
         try {
            maxRuns = new Integer(pieces[6].substring(0, 4));
         } catch (NumberFormatException var4) {
         }
      }

      return maxRuns;
   }

   public static String setPolymerAllowedRunsInBlob(String tagUID, String blob, int maxRuns) {
      blob = removeChecksum(blob);
      maxRuns = maxRuns < 0 ? 0 : maxRuns;
      String[] pieces = blob.split(",");

      String curStr;
      for(curStr = String.valueOf(maxRuns); curStr.length() < 4; curStr = "0" + curStr) {
      }

      pieces[6] = curStr + pieces[6].substring(4);
      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(tagUID, blob);
      return blob;
   }

   public static int getPolymerAllowedSamplesFromBlob(String blob) {
      int maxSamp = 0;
      String[] pieces = blob.split(",");
      if (pieces.length > 6 && pieces[6].length() > 8) {
         try {
            maxSamp = new Integer(pieces[6].substring(4, 8));
         } catch (NumberFormatException var4) {
         }
      }

      return maxSamp;
   }

   public static int getPolymerRemainingRunsFromBlob(String blob) {
      int remRuns = 0;
      String[] pieces = blob.split(",");
      if (pieces.length > 6 && pieces[6].length() > 12) {
         try {
            remRuns = new Integer(pieces[6].substring(8, 12));
         } catch (NumberFormatException var4) {
         }
      }

      return remRuns;
   }

   public static String setPolymerRemainingRunsInBlob(String tagUID, String blob, int remRuns) {
      blob = removeChecksum(blob);
      remRuns = remRuns < 0 ? 0 : remRuns;
      String[] pieces = blob.split(",");

      String curStr;
      for(curStr = String.valueOf(remRuns); curStr.length() < 4; curStr = "0" + curStr) {
      }

      pieces[6] = pieces[6].substring(0, 8) + curStr + pieces[6].substring(12);
      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(tagUID, blob);
      return blob;
   }

   public static int getPolymerRemainingSamplesFromBlob(String blob) {
      int remSamp = 0;
      String[] pieces = blob.split(",");
      if (pieces.length > 6 && pieces[6].length() > 16) {
         try {
            remSamp = new Integer(pieces[6].substring(12, 16));
         } catch (NumberFormatException var4) {
         }
      }

      return remSamp;
   }

   public static String setPolymerRemainingSamplesInBlob(String tagUID, String blob, int remSamp) {
      blob = removeChecksum(blob);
      remSamp = remSamp < 0 ? 0 : remSamp;
      String[] pieces = blob.split(",");

      String curStr;
      for(curStr = String.valueOf(remSamp); curStr.length() < 4; curStr = "0" + curStr) {
      }

      pieces[6] = pieces[6].substring(0, 12) + curStr + pieces[6].substring(16);
      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(tagUID, blob);
      return blob;
   }

   public static int getPolymerBubbleRemovalRunsFromBlob(String blob) {
      int bubRuns = 0;
      String[] pieces = blob.split(",");
      if (pieces.length > 6 && pieces[6].length() > 17) {
         try {
            bubRuns = new Integer(pieces[6].substring(16, 17));
         } catch (NumberFormatException var4) {
         }
      }

      return bubRuns;
   }

   public static String setPolymerBubbleRemovalRunsInBlob(String tagUID, String blob, int bubRuns) {
      blob = removeChecksum(blob);
      bubRuns = bubRuns < 0 ? 0 : bubRuns;
      bubRuns = bubRuns > 9 ? 9 : bubRuns;
      String[] pieces = blob.split(",");
      String curStr = String.valueOf(bubRuns);
      pieces[6] = pieces[6].substring(0, 16) + curStr + pieces[6].substring(17);
      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(tagUID, blob);
      return blob;
   }

   public static int getPolymerArrayFillsFromBlob(String blob) {
      int fills = 0;
      String[] pieces = blob.split(",");
      if (pieces.length > 6 && pieces[6].length() > 18) {
         try {
            fills = new Integer(pieces[6].substring(17, 18));
         } catch (NumberFormatException var4) {
         }
      }

      return fills;
   }

   public static String setPolymerArrayFillsInBlob(String tagUID, String blob, int fills) {
      blob = removeChecksum(blob);
      fills = fills < 0 ? 0 : fills;
      fills = fills > 9 ? 9 : fills;
      String[] pieces = blob.split(",");
      String curStr = String.valueOf(fills);
      pieces[6] = pieces[6].substring(0, 17) + curStr + pieces[6].substring(18);
      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(tagUID, blob);
      return blob;
   }

   public static int getPolymerRemainingVolumeFromBlob(String blob) {
      int remVol = 0;
      String[] pieces = blob.split(",");
      if (pieces.length > 6 && pieces[6].length() > 23) {
         try {
            remVol = new Integer(pieces[6].substring(18, 23));
         } catch (NumberFormatException var4) {
         }
      }

      return remVol;
   }

   public static String setPolymerRemainingVolumeInBlob(String tagUID, String blob, int remVol) {
      blob = removeChecksum(blob);
      remVol = remVol < 0 ? 0 : remVol;
      String[] pieces = blob.split(",");

      String curStr;
      for(curStr = String.valueOf(remVol); curStr.length() < 5; curStr = "0" + curStr) {
      }

      pieces[6] = pieces[6].substring(0, 18) + curStr;
      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(tagUID, blob);
      return blob;
   }

   public static String reducePolymerRemainingVolumeInBlob(String tagUID, String blob, int redVol) {
      blob = removeChecksum(blob);
      String[] pieces = blob.split(",");
      int remVol = new Integer(pieces[6].substring(18, 23));
      remVol -= redVol;
      remVol = remVol < 0 ? 0 : remVol;

      String curStr;
      for(curStr = String.valueOf(remVol); curStr.length() < 5; curStr = "0" + curStr) {
      }

      pieces[6] = pieces[6].substring(0, 18) + curStr;
      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(tagUID, blob);
      return blob;
   }

   public static String createAnodeBufferBlob(String tagUID, String bufferType, String partNum, String lotNum, long expirationDate, long installationDate, long lifeOnInstrument, int runsAllowed, int runsRemaining) {
      String retVal = "";
      retVal = retVal + bufferType + ",";
      retVal = retVal + partNum + ",";
      retVal = retVal + lotNum + ",";
      retVal = retVal + expirationDate / 60000L + ",";
      retVal = retVal + installationDate / 60000L + ",";
      retVal = retVal + lifeOnInstrument / 60000L + ",";

      String curStr;
      for(curStr = String.valueOf(runsAllowed); curStr.length() < 4; curStr = "0" + curStr) {
      }

      retVal = retVal + curStr;

      for(curStr = String.valueOf(runsRemaining); curStr.length() < 4; curStr = "0" + curStr) {
      }

      retVal = retVal + curStr;
      retVal = addChecksum(tagUID, retVal);
      return retVal;
   }

   public static String createAnodeBufferBlob(String tagInfo) {
      String retVal = addChecksum(tagInfo);
      return retVal;
   }

   public static String getAnodeBufferTypeFromBlob(String blob) {
      String bufferType = "";
      String[] pieces = blob.split(",");
      if (pieces.length > 0) {
         bufferType = pieces[0];
      }

      return bufferType;
   }

   public static String getAnodeBufferPartNumberFromBlob(String blob) {
      String partNum = "";
      String[] pieces = blob.split(",");
      if (pieces.length > 1) {
         partNum = pieces[1];
      }

      return partNum;
   }

   public static String getAnodeBufferLotNumberFromBlob(String blob) {
      String lotNum = "";
      String[] pieces = blob.split(",");
      if (pieces.length > 2) {
         lotNum = pieces[2];
      }

      return lotNum;
   }

   public static long getAnodeBufferExpirationDateFromBlob(String blob) {
      long expDate = 0L;
      String[] pieces = blob.split(",");
      if (pieces.length > 3) {
         try {
            expDate = new Long(pieces[3]);
            expDate *= 60000L;
         } catch (NumberFormatException var5) {
         }
      }

      return expDate;
   }

   public static long getAnodeBufferInstallationDateFromBlob(String blob) {
      long insDate = 0L;
      String[] pieces = blob.split(",");
      if (pieces.length > 4) {
         try {
            insDate = new Long(pieces[4]);
            insDate *= 60000L;
         } catch (NumberFormatException var5) {
         }
      }

      return insDate;
   }

   public static String setAnodeBufferInstallationDateInBlob(String tagUID, String blob, long insDate) {
      blob = removeChecksum(blob);
      String[] pieces = blob.split(",");
      pieces[4] = String.valueOf(insDate / 60000L);
      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(tagUID, blob);
      return blob;
   }

   public static long getAnodeBufferLifeOnInstrumentFromBlob(String blob) {
      long loi = 0L;
      String[] pieces = blob.split(",");
      if (pieces.length > 5) {
         try {
            loi = new Long(pieces[5]);
            loi *= 60000L;
         } catch (NumberFormatException var5) {
         }
      }

      return loi;
   }

   public static String setAnodeBufferLifeOnInstrumentInBlob(String tagUID, String blob, long loi) {
      blob = removeChecksum(blob);
      String[] pieces = blob.split(",");
      pieces[5] = String.valueOf(loi / 60000L);
      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(tagUID, blob);
      return blob;
   }

   public static int getAnodeBufferAllowedRunsFromBlob(String blob) {
      int maxRuns = 0;
      String[] pieces = blob.split(",");
      if (pieces.length > 6 && pieces[6].length() > 4) {
         try {
            maxRuns = new Integer(pieces[6].substring(0, 4));
         } catch (NumberFormatException var4) {
         }
      }

      return maxRuns;
   }

   public static String setAnodeBufferAllowedRunsInBlob(String tagUID, String blob, int maxRuns) {
      blob = removeChecksum(blob);
      maxRuns = maxRuns < 0 ? 0 : maxRuns;
      String[] pieces = blob.split(",");

      String curStr;
      for(curStr = String.valueOf(maxRuns); curStr.length() < 4; curStr = "0" + curStr) {
      }

      pieces[6] = curStr + pieces[6].substring(4);
      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(tagUID, blob);
      return blob;
   }

   public static int getAnodeBufferRemainingRunsFromBlob(String blob) {
      int remRuns = 0;
      String[] pieces = blob.split(",");
      if (pieces.length > 6 && pieces[6].length() > 8) {
         try {
            remRuns = new Integer(pieces[6].substring(4, 8));
         } catch (NumberFormatException var4) {
         }
      }

      return remRuns;
   }

   public static String setAnodeBufferRemainingRunsInBlob(String tagUID, String blob, int remRuns) {
      blob = removeChecksum(blob);
      remRuns = remRuns < 0 ? 0 : remRuns;
      String[] pieces = blob.split(",");

      String curStr;
      for(curStr = String.valueOf(remRuns); curStr.length() < 4; curStr = "0" + curStr) {
      }

      pieces[6] = pieces[6].substring(0, 4) + curStr;
      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(tagUID, blob);
      return blob;
   }

   public static String createCathodeBufferBlob(String tagUID, String bufferType, String partNum, String lotNum, long expirationDate, long installationDate, long lifeOnInstrument, int runsAllowed, int runsRemaining) {
      String retVal = "";
      retVal = retVal + bufferType + ",";
      retVal = retVal + partNum + ",";
      retVal = retVal + lotNum + ",";
      retVal = retVal + expirationDate / 60000L + ",";
      retVal = retVal + installationDate / 60000L + ",";
      retVal = retVal + lifeOnInstrument / 60000L + ",";

      String curStr;
      for(curStr = String.valueOf(runsAllowed); curStr.length() < 4; curStr = "0" + curStr) {
      }

      retVal = retVal + curStr;

      for(curStr = String.valueOf(runsRemaining); curStr.length() < 4; curStr = "0" + curStr) {
      }

      retVal = retVal + curStr;
      retVal = addChecksum(tagUID, retVal);
      return retVal;
   }

   public static String createCathodeBufferBlob(String tagInfo) {
      String retVal = addChecksum(tagInfo);
      return retVal;
   }

   public static String getCathodeBufferTypeFromBlob(String blob) {
      String bufferType = "";
      String[] pieces = blob.split(",");
      if (pieces.length > 0) {
         bufferType = pieces[0];
      }

      return bufferType;
   }

   public static String getCathodeBufferPartNumberFromBlob(String blob) {
      String partNum = "";
      String[] pieces = blob.split(",");
      if (pieces.length > 1) {
         partNum = pieces[1];
      }

      return partNum;
   }

   public static String getCathodeBufferLotNumberFromBlob(String blob) {
      String lotNum = "";
      String[] pieces = blob.split(",");
      if (pieces.length > 2) {
         lotNum = pieces[2];
      }

      return lotNum;
   }

   public static long getCathodeBufferExpirationDateFromBlob(String blob) {
      long expDate = 0L;
      String[] pieces = blob.split(",");
      if (pieces.length > 3) {
         try {
            expDate = new Long(pieces[3]);
            expDate *= 60000L;
         } catch (NumberFormatException var5) {
         }
      }

      return expDate;
   }

   public static long getCathodeBufferInstallationDateFromBlob(String blob) {
      long insDate = 0L;
      String[] pieces = blob.split(",");
      if (pieces.length > 4) {
         try {
            insDate = new Long(pieces[4]);
            insDate *= 60000L;
         } catch (NumberFormatException var5) {
         }
      }

      return insDate;
   }

   public static String setCathodeBufferInstallationDateInBlob(String tagUID, String blob, long insDate) {
      blob = removeChecksum(blob);
      String[] pieces = blob.split(",");
      pieces[4] = String.valueOf(insDate / 60000L);
      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(tagUID, blob);
      return blob;
   }

   public static long getCathodeBufferLifeOnInstrumentFromBlob(String blob) {
      long loi = 0L;
      String[] pieces = blob.split(",");
      if (pieces.length > 5) {
         try {
            loi = new Long(pieces[5]);
            loi *= 60000L;
         } catch (NumberFormatException var5) {
         }
      }

      return loi;
   }

   public static String setCathodeBufferLifeOnInstrumentInBlob(String tagUID, String blob, long loi) {
      blob = removeChecksum(blob);
      String[] pieces = blob.split(",");
      pieces[5] = String.valueOf(loi / 60000L);
      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(tagUID, blob);
      return blob;
   }

   public static int getCathodeBufferAllowedRunsFromBlob(String blob) {
      int maxRuns = 0;
      String[] pieces = blob.split(",");
      if (pieces.length > 6 && pieces[6].length() > 4) {
         try {
            maxRuns = new Integer(pieces[6].substring(0, 4));
         } catch (NumberFormatException var4) {
         }
      }

      return maxRuns;
   }

   public static String setCathodeBufferAllowedRunsInBlob(String tagUID, String blob, int maxRuns) {
      blob = removeChecksum(blob);
      maxRuns = maxRuns < 0 ? 0 : maxRuns;
      String[] pieces = blob.split(",");

      String curStr;
      for(curStr = String.valueOf(maxRuns); curStr.length() < 4; curStr = "0" + curStr) {
      }

      pieces[6] = curStr + pieces[6].substring(4);
      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(tagUID, blob);
      return blob;
   }

   public static int getCathodeBufferRemainingRunsFromBlob(String blob) {
      int remRuns = 0;
      String[] pieces = blob.split(",");
      if (pieces.length > 6 && pieces[6].length() > 8) {
         try {
            remRuns = new Integer(pieces[6].substring(4, 8));
         } catch (NumberFormatException var4) {
         }
      }

      return remRuns;
   }

   public static String setCathodeBufferRemainingRunsInBlob(String tagUID, String blob, int remRuns) {
      blob = removeChecksum(blob);
      remRuns = remRuns < 0 ? 0 : remRuns;
      String[] pieces = blob.split(",");

      String curStr;
      for(curStr = String.valueOf(remRuns); curStr.length() < 4; curStr = "0" + curStr) {
      }

      pieces[6] = pieces[6].substring(0, 4) + curStr;
      blob = "";

      for(int i = 0; i < pieces.length - 1; ++i) {
         blob = blob + pieces[i] + ",";
      }

      blob = blob + pieces[pieces.length - 1];
      blob = addChecksum(tagUID, blob);
      return blob;
   }

   private static String calculateChecksum(String blob) {
      CRC32 a32 = new CRC32();
      long cksum = 0L;

      try {
         a32.reset();
         a32.update(blob.getBytes("UTF-8"));
         cksum = a32.getValue();
      } catch (UnsupportedEncodingException var6) {
         var6.printStackTrace();
      }

      String checksum = Long.toString(cksum, 36);
      return checksum;
   }

   private static String calculateV1_0_Checksum(String blob) {
      CRC32 a32 = new CRC32();
      long cksum = 0L;

      try {
         a32.reset();
         a32.update(blob.getBytes("UTF-8"));
         cksum = a32.getValue();
      } catch (UnsupportedEncodingException var6) {
         var6.printStackTrace();
      }

      String checksum = (new Long(cksum)).toString();
      return checksum;
   }

   private static String calculateChecksum(String tagUID, String strStartCode, String blob) {
      CRC32 a32 = new CRC32();
      long cksum = 0L;

      try {
         a32.reset();
         if (tagUID != null) {
            a32.update(tagUID.toUpperCase().getBytes("UTF-8"));
         }

         a32.update(strStartCode.getBytes("UTF-8"));
         a32.update(blob.getBytes("UTF-8"));
         cksum = a32.getValue();
      } catch (UnsupportedEncodingException var8) {
         var8.printStackTrace();
      }

      String checksum = Long.toString(cksum, 36);
      return checksum;
   }

   private static String calculateV1_0_Checksum(String tagUID, String strStartCode, String blob) {
      CRC32 a32 = new CRC32();
      long cksum = 0L;

      try {
         a32.reset();
         if (tagUID != null) {
            a32.update(tagUID.getBytes("UTF-8"));
         }

         a32.update(strStartCode.getBytes("UTF-8"));
         a32.update(blob.getBytes("UTF-8"));
         cksum = a32.getValue();
      } catch (UnsupportedEncodingException var8) {
         var8.printStackTrace();
      }

      String checksum = (new Long(cksum)).toString();
      return checksum;
   }

   public static String addChecksum(String tagUID, String blob) {
      String checksum = tagUID == null ? calculateChecksum(blob) : calculateChecksum(tagUID, "#rF1d*d2Ta&", blob);
      String timeStamp = Long.toString(System.currentTimeMillis(), 36);
      blob = blob + "#" + checksum + "#" + timeStamp + "#";
      return blob;
   }

   public static String addChecksum(String blob) {
      return addChecksum((String)null, blob);
   }

   private static String removeChecksum(String blob) {
      String[] pieces = blob.split("#");
      return pieces[0];
   }

   public static boolean checkChecksum(String tagUID, String blob) {
      boolean checksumValid = false;
      String[] pieces = blob.split("#");
      String testChecksum = tagUID == null ? calculateChecksum(pieces[0]) : calculateChecksum(tagUID, "#rF1d*d2Ta&", pieces[0]);

      assert pieces.length >= 2;

      if (pieces.length < 2) {
         return false;
      } else {
         checksumValid = testChecksum.equals(pieces[1]);
         return checksumValid;
      }
   }

   public static boolean checkV1_0_Checksum(String tagUID, String blob) {
      boolean checksumValid = false;
      String[] pieces = blob.split("#");
      String testChecksum = tagUID == null ? calculateV1_0_Checksum(pieces[0]) : calculateV1_0_Checksum(tagUID, "&rFId*dAtA%", pieces[0]);

      assert pieces.length >= 2;

      if (pieces.length < 2) {
         return false;
      } else {
         checksumValid = testChecksum.equals(pieces[1]);
         return checksumValid;
      }
   }

   public static boolean checkChecksum(String blob) {
      return checkChecksum((String)null, blob);
   }

   public static boolean checkV1_0_Checksum(String blob) {
      return checkV1_0_Checksum((String)null, blob);
   }

   public static boolean checkTimestamp(String blob) {
      String[] pieces = blob.split("#");
      if (pieces.length < 3) {
         return true;
      } else {
         long timeStamp = Long.parseLong(pieces[2], 36);
         return System.currentTimeMillis() >= timeStamp;
      }
   }

   public static void generateRFIDTagInfoFromFile() {
      String inLine = "";
      File inFile = null;
      File outFile = null;
      File logFile = null;

      try {
         inFile = new File("RFIDInput.txt");
         outFile = new File("RFIDTag.txt");
         if (outFile.exists()) {
            outFile.delete();
         }

         logFile = new File("RFID_Tag_Programming.log");
         if (!logFile.exists()) {
            logFile.createNewFile();
         }

         BufferedReader bin = new BufferedReader(new FileReader(inFile));
         inLine = bin.readLine();
         bin.close();
      } catch (FileNotFoundException var26) {
         var26.printStackTrace();
      } catch (IOException var27) {
         var27.printStackTrace();
      }

      String tagStr = "";
      String[] tok = inLine.split(",");
      if (tok.length < 7) {
         System.exit(1);
      }

      String tagUID = tok[0];
      String consumableType = tok[1];
      String type;
      String part;
      long loi;
      String lot;
      String serial;
      Date exp;
      long expVal;
      SimpleDateFormat df;
      GregorianCalendar ed;
      if (consumableType.equals("Ano")) {
         type = tok[2];
         part = tok[3];
         loi = new Long(tok[4]);
         loi *= 86400000L;
         lot = tok[5];
         serial = tok[6];
         exp = null;
         expVal = 0L;

         try {
            df = new SimpleDateFormat("dd-MMM-yyyy");
            exp = df.parse(serial);
            ed = new GregorianCalendar();
            ed.setTime(exp);
            ed.setTimeZone(TimeZone.getTimeZone("GMT"));
            ed.set(11, 12);
            ed.set(12, 0);
            expVal = ed.getTimeInMillis();
         } catch (ParseException var25) {
            var25.printStackTrace();
         }

         tagStr = createAnodeBufferBlob(tagUID, type, part, lot, expVal, 0L, loi, 0, 0);
      } else if (consumableType.equals("Cat")) {
         type = tok[2];
         part = tok[3];
         loi = new Long(tok[4]);
         loi *= 86400000L;
         lot = tok[5];
         serial = tok[6];
         exp = null;
         expVal = 0L;

         try {
            df = new SimpleDateFormat("dd-MMM-yyyy");
            exp = df.parse(serial);
            ed = new GregorianCalendar();
            ed.setTime(exp);
            ed.setTimeZone(TimeZone.getTimeZone("GMT"));
            ed.set(11, 12);
            ed.set(12, 0);
            expVal = ed.getTimeInMillis();
         } catch (ParseException var24) {
            var24.printStackTrace();
         }

         tagStr = createCathodeBufferBlob(tagUID, type, part, lot, expVal, 0L, loi, 0, 0);
      } else {
         String lot;
         String expDate;
         if (consumableType.equals("Pol")) {
            if (tok.length < 9) {
               System.exit(1);
            }

            type = tok[2];
            part = tok[3];
            loi = new Long(tok[4]);
            loi *= 86400000L;
            int numSamp = new Integer(tok[5]);
            int fillVol = new Integer(tok[6]);
            lot = tok[7];
            expDate = tok[8];
            Date exp = null;
            long expVal = 0L;

            try {
               SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
               exp = df.parse(expDate);
               GregorianCalendar ed = new GregorianCalendar();
               ed.setTime(exp);
               ed.setTimeZone(TimeZone.getTimeZone("GMT"));
               ed.set(11, 12);
               ed.set(12, 0);
               expVal = ed.getTimeInMillis();
            } catch (ParseException var23) {
               var23.printStackTrace();
            }

            if (type.equals("Conditioner")) {
               tagStr = createPolymerBlob(tagUID, type, part, lot, expVal, 0L, loi, 1, numSamp, 1, numSamp, 0, 0, fillVol);
            } else {
               tagStr = createPolymerBlob(tagUID, type, part, lot, expVal, 0L, loi, 0, numSamp, 0, numSamp, 0, 0, fillVol);
            }
         } else if (consumableType.equals("Cap")) {
            if (tok.length < 9) {
               System.exit(1);
            }

            int caps = new Integer(tok[2]);
            int len = new Integer(tok[3]);
            String part = tok[4];
            int runs = new Integer(tok[5]);
            lot = tok[6];
            serial = tok[7];
            lot = tok[8];
            expDate = null;
            long expVal = 0L;

            try {
               SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
               Date exp = df.parse(lot);
               GregorianCalendar ed = new GregorianCalendar();
               ed.setTime(exp);
               ed.setTimeZone(TimeZone.getTimeZone("GMT"));
               ed.set(11, 12);
               ed.set(12, 0);
               expVal = ed.getTimeInMillis();
            } catch (ParseException var22) {
               var22.printStackTrace();
            }

            long[] noDates = new long[0];
            String[] noSer = new String[0];
            tagStr = createArrayBlob(caps, len, part, lot, serial, expVal, 0L, "", 0, runs, 0, noDates, noSer);
         }
      }

      try {
         BufferedWriter bout = new BufferedWriter(new FileWriter(outFile, false));
         bout.write(tagStr);
         bout.newLine();
         bout.close();
         BufferedWriter lout = new BufferedWriter(new FileWriter(logFile, true));
         DateFormat dateFormatter = new SimpleDateFormat("yyyy MMM dd HH:mm:ss.SSS");
         String ts = dateFormatter.format(new Timestamp(System.currentTimeMillis()));
         ts = ts + " -- Tag ID:  " + tagUID;
         lout.write(ts);
         lout.newLine();
         lout.write("  Input line:   " + inLine);
         lout.newLine();
         lout.write("  Output line:  " + tagStr);
         lout.newLine();
         lout.newLine();
         lout.close();
         inFile.delete();
      } catch (IOException var21) {
         var21.printStackTrace();
      }

   }
}
