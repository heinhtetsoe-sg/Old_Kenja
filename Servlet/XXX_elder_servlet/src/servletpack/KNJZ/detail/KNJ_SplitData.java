package servletpack.KNJZ.detail;

import org.apache.commons.lang.StringUtils;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *         ＜KNJ_SPLITDATA＞ SPLITと同等の処理
 *
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJ_SplitData{

	/**
     *  セパレータ区切りのデータを配列化
     * @deprecated 代わりに {@link org.apache.commons.lang.StringUtils#split(String, String)} を使って下さい。
	 */
	public static String[] ParseData(String line,String sep) {
//		int sepa = line.indexOf(sep);
//		if (sepa < 0){
//			return null;
//		}
//
//		List sepadata = new ArrayList();
//		while (sepa > -1){
//			sepadata.add(line.substring(0,sepa));
//			line = line.substring(sepa + 1);
//			sepa = line.indexOf(sep);
//            if (line.length() > 0 && sepa < 0) {
//                sepadata.add(line);
//            }
//		}
//		return (String[])sepadata.toArray(new String[sepadata.size()]);
        return StringUtils.split(line, sep);
	}


}//クラスの括り
