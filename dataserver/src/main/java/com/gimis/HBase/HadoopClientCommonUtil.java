package com.gimis.HBase;

public class HadoopClientCommonUtil
{

	public static final String SEPARATOR_ROW_KEY = "-";

	public static final String ROWKEY_PATTERN_HISTORY_NUMBER = "[0-9]{10}\\-[0-9]{19}";

	public static final String ROWKEY_PATTERN_HISTORY_CHAR = "[A-Z,0-9]{10}\\-[0-9]{19}";

	public static final String ROWKEY_PATTERN_HISTORY_REPORT = "[0-9]{19}\\-[0-9]{10}";

	public static final String ROWKEY_PATTERN_DAILY_REPORT = "[0-9]{8}\\-[0-9]{10}";

	public static final String ROWKEY_PATTERN_WEEKLY_REPORT = "[0-9]{6}\\-[0-9]{10}";

	public static final String ROWKEY_PATTERN_MONTHLY_REPORT = "[0-9]{6}\\-[0-9]{10}";

	public static final String ROWKEY_PATTERN_YEARLY_REPORT = "[0-9]{4}\\-[0-9]{10}";

	public static final String PATTERN_NUMBER_ID = "[0-9]{10}";

	public static final String PATTERN_CHAR_ID = "[A-Z,0-9]{10}";

	public static final String PATTERN_NUMBER_SIMPLE_ID = "[0-9]{1,10}";

	public static final String PATTERN_CHAR_SIMPLE_ID = "[A-Z,0-9]{1,10}";

	public static final String MIN_ID_STRING = "00000000000000000000000000000000";

	public static final String MAX_ID_STRING = "ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ";

	public static final long MAX_ID_NUMBER = 9999999999L;

	public static final int ID_LEN = "00000000000000000000000000000000".length();

	public static final int TIMESTAMP_LEN = 19;

	private static final char[] NUMBER_ALPHABET_TABLE = { '0', '1', '2', '3', '4', '5', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

	private static final int NUMBER_ALPHABET_TABLE_LEN = NUMBER_ALPHABET_TABLE.length;

	public static String getReportDescRowKeyRegexString(String id)
	{
		String result = getReverseId( id );

		return result;
	}


	public static String getReverseId(String id)
	{
		String tmp = id.toUpperCase();
		tmp = fillId( tmp );
		if (tmp.matches( "[0-9]{10}" ))
		{
			long longId = Long.parseLong( tmp );
			longId = 9999999999L - longId;
			String result = "00000000000000000000000000000000" + longId;
			result = result.substring( result.length() - ID_LEN );
			return result;
		}
		if (tmp.matches( "[A-Z,0-9]{32}" ))
		{
			String result = "";
			for ( int i = 0; i < tmp.length(); i++ )
			{
				char c = tmp.charAt( i );
				int index = 0;
				for ( int j = 0; j < NUMBER_ALPHABET_TABLE_LEN; j++ )
				{
					if (c == NUMBER_ALPHABET_TABLE[j])
					{
						index = j;
						break;
					}
				}
				c = NUMBER_ALPHABET_TABLE[(NUMBER_ALPHABET_TABLE_LEN - 1 - index)];
				result = result + c;
			}
			return result;
		}

		throw new RuntimeException( "不合法的ID" );
	}


	public static String fillId(String id)
	{
		String result = "00000000000000000000000000000000" + id;
		result = result.substring( result.length() - ID_LEN );
		if (false == result.matches( "[A-Z,0-9]{32}" ))
		{
			throw new RuntimeException( "ID[" + id + "]不符合格式" );
		}
		return result;
	}

}
