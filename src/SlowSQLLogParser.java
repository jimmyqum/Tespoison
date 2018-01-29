import java.io.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.String;

/**
 * 
 * @author quming
 * @context SQL������־����
 */
public class SlowSQLLogParser {

	public static String dataPattern = "[0-9]{4}[-][0-9]{1,2}[-][0-9]{1,2}[-][0-9]{1,2}[.][0-9]{1,2}[.][0-9]{1,2}";
	public static String dataMinuPattern = "[0-9]{4}[-][0-9]{1,2}[-][0-9]{1,2}[-][0-9]{1,2}[.][0-9]{1,2}";
	public static String datePattern = "[0-9]{4}[-][0-9]{1,2}[-][0-9]{1,2}";
	public static String timePattern = "[0-9]{1,2}[.][0-9]{1,2}";


	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		String matchSql = "";

		//SQLƥ�俪ʼ---------------------------------

		matchSql = getNewSql("ƾ���ص�����");



		//SQLƥ�����---------------------------------


		List sqlList = new ArrayList();
		//�������
		sqlList.add(matchSql);
		//��־����·��

		String basePath = publicLog.sys.basePath;
		String logPath = publicLog.sys.logPath;



		try {
			List<String> listFilePath = filereadSQL(basePath + logPath, null);
			//System.out.println(listFilePath.size());
			List resultSql = new ArrayList();
			List tmpList = null;
			int count = 1;
			int totalCount = listFilePath.size();
			for(Iterator iterator = listFilePath.iterator(); iterator.hasNext();) {
				String item = (String) iterator.next();
				System.out.println(count + " of " + totalCount);
				tmpList = readFileByLines(item, sqlList);
				System.out.println(tmpList.size());
				if(tmpList != null && tmpList.size() != 0) {
					resultSql.addAll(tmpList);
				}
				count++;
			}

			int size = resultSql.size();

			System.out.println(size);
			createCSVFile(resultSql, basePath,
					"ԭʼ����");
			resultSql = getNewList(resultSql);


			Map<String,Integer[]> statMap = new HashMap();
			String headStr = "head";
			boolean hasHead=false;
			DecimalFormat df = new DecimalFormat("00");
			for (int i = 0; i < resultSql.size(); i++) {
				Map item = (Map) resultSql.get(i);
				Set<String> keySet = item.keySet();

				String tmpCount = (String)item.get("count");
				String tmpTimeMin =(String)item.get("timeMin");
				String tmpDate = SlowSQLLogParser.getMatchData(tmpTimeMin, datePattern);
				String tmpTime = SlowSQLLogParser.getMatchData(tmpTimeMin, timePattern);
				int tmpCount1 = Integer.parseInt(tmpCount);
				Integer[] dataAry = null;
				if(statMap.containsKey(tmpDate)){
					dataAry = statMap.get(tmpDate);
				}else {
					dataAry = new Integer[1440];
					statMap.put(tmpDate,dataAry );
				}

				if(headStr.equals("head")){
					hasHead = false;
				}else {
					hasHead = true;
				}

				for(int j = 0; j< 24; j++) {
					for(int k = 0; k< 60; k++) {
						String tmpStr = df.format(j) +"."+ df.format(k);
						String head = df.format(j) +":"+ df.format(k);
						if (!hasHead) {
							headStr+=","+head;
						}
						System.out.println(j*60+k +"---"+tmpStr+"--|--"+tmpTime+"="+tmpCount1);
						if(tmpStr.equals(tmpTime)) {
							dataAry[j*60+k]=tmpCount1;
							System.out.println(dataAry[j*60+k]);

						}else if( dataAry[j*60+k]==null){
							dataAry[j*60+k] = 0;
						}

					}
				}

				for (String key : keySet) {
					System.out.println(item.get(key));
				}


			}

			createCSVFile2(statMap,headStr, basePath,"�������");


			for(int i = 0 ; i < resultSql.size(); i++){
				System.out.println(resultSql.get(i));
			}

			System.out.println(size);
			System.out.println("������ɣ�");

			//System.out.println(resultSql);
			//System.out.println(getMatchData(testStr, "select  SAFECODE,CORPID,CORPNAME,CHECKSTS,PAYLIST"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}









	/**
	 * read all file
	 */
	public static List<String> filereadSQL(String filepath, List filePath)
			throws FileNotFoundException, IOException {
		List<String> filePathList = filePath;
		if(filePathList == null) {
			filePathList = new ArrayList();
		}
		try {
			File file = new File(filepath);
			if (!file.isDirectory()) {

				filePathList.add(file.getAbsolutePath());
				System.out.println("name=" + file.getName());

			} else if (file.isDirectory()) {

				String[] filelist = file.list();
				for (int i = 0; i < filelist.length; i++) {
					File readfile = new File(filepath + "//" + filelist[i]);
					if (!readfile.isDirectory()) {

						filePathList.add(readfile.getAbsolutePath());
						System.out.println("name=" + readfile.getName());

					} else if (readfile.isDirectory()) {
						filereadSQL(filepath + "//" + filelist[i], filePathList);
					}
				}

			}

		} catch (FileNotFoundException e) {
			System.out.println("readfile()   Exception:" + e.getMessage());
		}
		return filePathList;
	}




	/**
	 * ����Ϊ��λ��ȡ�ļ��������ڶ������еĸ�ʽ���ļ�
	 */
	public static List readFileByLines(String fileName, List sqlList) {
		File file = new File(fileName);
		List result = new ArrayList();
		
		BufferedReader reader = null;
		try {
			//System.out.println("����Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���У�");
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			// һ�ζ���һ�У�ֱ������nullΪ�ļ�����

			InputStreamReader osw = new InputStreamReader(new FileInputStream(file), "UTF-8");

            FileInputStream fis = new FileInputStream(fileName);
			DataInputStream in = new DataInputStream(fis);
			BufferedReader d = new BufferedReader(new InputStreamReader(in, "GBK"));

			while ((tempString = d.readLine()) != null) {

				//byte[] t_utf8 = tempString.getBytes("GBK");

				String gStr= new String(tempString.getBytes(), "UTF-8");

				//System.out.println(gStr);


				String matchResult = matchResult(tempString, sqlList);




				if(matchResult!=null) {
					Map itemMap = new HashMap();
					itemMap.put("time", getMatchData(tempString, dataPattern));
					itemMap.put("timemin", getMatchData(tempString, dataMinuPattern));
					itemMap.put("fileName", fileName);
					itemMap.put("str", tempString);
					result.add(itemMap);
				}
				line++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @param fileStr
	 * @param sqlList
	 * @return
	 */
	public static String matchResult(String fileStr,List sqlList) {
		for(int i = 0; i < sqlList.size(); i++){
			String matchResult = getMatchData(fileStr, (String)sqlList.get(i));
			if(matchResult!= null) {
				return matchResult;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param strSource
	 * @param patternStr
	 * @return
	 */
	public static String getMatchData(String strSource, String patternStr){		
		Pattern pattern = Pattern.compile(patternStr);  
        Matcher matcher = pattern.matcher(strSource);             
        String dateStr = null;  
        if(matcher.find()){  
          dateStr = matcher.group(0);  
        }  
        String str = dateStr == null ? null : dateStr.toString();  
        //System.out.println(str);
        return str;
	}
	
	/** 
     * ����ΪCVS�ļ�  
     * @param exportData 
     *              Դ����List 

     * @param outPutPath
     *              �ļ�·�� 
     * @param fileName 
     *              �ļ����� 
     * @return 
     */  
    @SuppressWarnings("rawtypes")  
    public static File createCSVFile(List<Map<String, String>> exportData, String outPutPath,  
                                     String fileName) {  
        File csvFile = null;  
        BufferedWriter csvFileOutputStream = null;  
        try {  
            File file = new File(outPutPath);  
            if (!file.exists()) {  
                file.mkdir();  
            }  
            //�����ļ�����ʽ������  
            csvFile = File.createTempFile(fileName, ".csv", new File(outPutPath));  
            System.out.println("csvFile��" + csvFile);  
            // UTF-8ʹ��ȷ��ȡ�ָ���","    
            csvFileOutputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(  
                csvFile), "GBK"), 1024);  
        //    System.out.println("csvFileOutputStream��" + csvFileOutputStream);
            for(int i = 0 ; i < exportData.size(); i++){
            	Map<String, String> item = (Map<String, String>)exportData.get(i);
//            	
            	String rest = "";
            	for (Map.Entry<String, String> entry : item.entrySet()) {
            		System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
            		rest = rest + "\""+entry.getValue()+"\",";
            		
            	}
            	rest = rest.substring(0, rest.length() -1);
            	csvFileOutputStream.write(rest);
            	csvFileOutputStream.write("\r\n");  
            }
                       

            csvFileOutputStream.flush();  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            try {  
                csvFileOutputStream.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
        return csvFile;  
    }
    
    /**
     * ����ΪCVS�ļ�  
     * @param exportData 
     *              Դ����List 
     * @param headStr
     *              CSV�ļ����б�ͷMAP
     * @param outPutPath 
     *              �ļ�·�� 
     * @param fileName 
     *              �ļ����� 
     * @return 
     */  
    @SuppressWarnings("rawtypes")  
    public static File createCSVFile2(Map<String, Integer[]> exportData,String headStr, String outPutPath,  
                                     String fileName) {  
        File csvFile = null;  
        BufferedWriter csvFileOutputStream = null;
        try {  
            File file = new File(outPutPath);  
            if (!file.exists()) {  
                file.mkdir();  
            }  
            //�����ļ�����ʽ������  
            csvFile = File.createTempFile(fileName, ".csv", new File(outPutPath));  
            System.out.println("csvFile��" + csvFile);  
            // UTF-8ʹ��ȷ��ȡ�ָ���","    
            csvFileOutputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(  
                csvFile), "GBK"), 1024);  
            System.out.println("csvFileOutputStream��" + csvFileOutputStream);  
            
        	csvFileOutputStream.write(headStr);
        	csvFileOutputStream.write("\r\n");         	
			Set<Map.Entry<String, Integer[]>> entryseSet = exportData.entrySet();
			for (Map.Entry<String, Integer[]> entry : entryseSet) {
				
				String tmpStr=entry.getKey();				
				Integer[] dataAry = entry.getValue();
				for (int i = 0; i < dataAry.length; i++) {
					
					tmpStr+=","+dataAry[i];
					
				}				
            	csvFileOutputStream.write(tmpStr);
            	csvFileOutputStream.write("\r\n"); 
			}
			
            csvFileOutputStream.flush();  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            try {  
                csvFileOutputStream.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
        return csvFile;  
    }
    
    /**
     * 
     * @param sourceList
     * @return
     */
    public static List getNewList(List sourceList) {
    	List newList =new ArrayList();
    
    	 for(int i = 0 ; i < sourceList.size(); i++){
         	Map item = (Map)sourceList.get(i);
         	String aleadyCount = (String)item.get("encount");
         	if(aleadyCount != null){
         		continue;
         	}
         	int count = 1;
         	String timeMin = (String)item.get("timemin");
         	
         	if(i < sourceList.size()-1){
	         	 for(int j = i +1 ; j < sourceList.size(); j++){
	             	Map subItem = (Map)sourceList.get(j);
	             	String ec = (String)subItem.get("encount");
	             	if(ec != null) continue;
	             	String stimeMin = (String)subItem.get("timemin");
	             	if(timeMin.equals(stimeMin)){
	             		subItem.put("encount", "1");
	             		count++;
	             	}
	             }
         	}
         	Map newItem = new HashMap();
         	newItem.put("count", String.valueOf(count));
         	newItem.put("timeMin", timeMin);
         	//newItem.put("str", (String)item.get("str"));
         	newList.add(newItem);
         }
    	return newList;
    }
    

	
	public static Date string2Date(String strDate, String pattern) throws ParseException  {
		if (strDate == null || strDate.equals("")) {
			return null;
		}
		
		if (pattern == null || pattern.equals("")) {
			pattern = "yyyy-MM-dd HH:mm";
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		Date date = null;

		date = sdf.parse(strDate);
		return date;
	}
	
	private static String getNewSql(String patternStr){
		patternStr = patternStr.replaceAll("\\(", "\\\\(");
		patternStr = patternStr.replaceAll("\\)", "\\\\)");		
		patternStr = patternStr.replaceAll("\\*", "\\\\*");
		patternStr = patternStr.replaceAll("\\+", "\\\\+");
		patternStr = patternStr.replaceAll("\\?", "\\\\?");
		patternStr = patternStr.replaceAll("\\|", "\\\\|");
		return patternStr;
	}
}
