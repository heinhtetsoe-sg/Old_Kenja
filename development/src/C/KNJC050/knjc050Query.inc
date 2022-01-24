<?php

require_once('for_php7.php');

class knjc050Query extends Query {

    //V_科目担任データ取得
    function SQLGet_Main($model)
    {
        $query = "SELECT DISTINCT ";
        $query .= "    SUBSTR(T5.SUBCLASSCD,1,2) CLASSCD, ";
        $query .= "    T5.SUBCLASSCD, ";
        $query .= "    T5.CHAIRCD ATTENDCLASSCD, ";
        $query .= "    T3.CLASSNAME, ";
        $query .= "    T4.SUBCLASSNAME, ";
        $query .= "    (CASE WHEN T5.GROUPCD='0000' THEN (T7.HR_NAME) ELSE (T7.HR_NAME || '*') END )TARGETCLASS, ";
        $query .= "    T1.STARTDAY, ";
        $query .= "    T1.ENDDAY, ";
        $query .= "    T5.GROUPCD, ";
        $query .= "    T5.CHAIRNAME GROUPNAME, ";
        $query .= "    T6.STAFFCD, ";							//2004-08-11 naka
        $query .= "    T8.appdate, ";							//2004-08-11 naka
        $query .= "    T6.STAFFNAME_SHOW ";
//        $query .= "    T2.CLASSALIAS, ";
//      $query .= "    T5.GROUPSEQ ";  2004/05/24 delete by teruya
        $query .= "FROM ";
        $query .= "    (SELECT ";
        $query .= "        TT1.CHAIRCD, ";
        $query .= "        TT2.STAFFCD, ";
        $query .= "        TT1.YEAR, ";
        $query .= "        MIN(DATE(TT1.EXECUTEDATE)) STARTDAY, ";
        $query .= "        MAX(DATE(TT1.EXECUTEDATE)) ENDDAY ";
        $query .= "    FROM ";
        $query .= "        SCH_CHR_DAT   TT1,  ";    //時間割データ
        $query .= "        CHAIR_STF_DAT TT2,   ";    //講座担当職員データ
        $query .= "         (SELECT";
        $query .= "             SDATE,";
        $query .= "             EDATE";
        $query .= "         FROM";
        $query .= "             SEMESTER_MST";
        $query .= "         WHERE";
        $query .= "             YEAR     = '" .$model->year ."' AND ";
        $query .= "             SEMESTER = '" .$model->semester ."'";
        $query .= "         ) TT3";
        $query .= "    WHERE ";
        $query .= "        TT1.YEAR = '" .$model->year ."' AND ";
        $query .= "        TT1.YEAR = TT2.YEAR AND ";
        $query .= "        TT2.SEMESTER = '" .$model->semester ."' AND ";
        $query .= "        TT1.CHAIRCD = TT2.CHAIRCD AND ";
        $query .= "        TT1.PERIODCD > '0' AND ";
        $query .= "         TT1.EXECUTEDATE BETWEEN TT3.SDATE AND TT3.EDATE ";
//2004-07-30 naka
//        $query .= "        TT1.EXECUTEDATE BETWEEN DATE(REPLACE('" .$model->semi_start ."','/','-')) ";  //学期開始日
//        $query .= "        AND DATE(REPLACE('" .$model->semi_end ."','/','-')) ";       //学期終了日
        	//制限付き参照・更新可は自分が受け持つクラスのみ	2004/03/24 nakamoto add
	        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT){
    	        $query .= "  AND TT2.STAFFCD  = '" .STAFFCD ."' ";        
        	}elseif (AUTHORITY == DEF_NOAUTH){        //権限なし(表示不可)
            	$query .= "  AND TT2.STAFFCD  IS NULL ";
	        }
        $query .= "    GROUP BY ";
        $query .= "        TT1.CHAIRCD,TT2.STAFFCD,TT1.YEAR ";
        $query .= "    ) T1 ";
        $query .= "    INNER JOIN CHAIR_DAT T5 ON T5.YEAR = T1.YEAR AND T5.SEMESTER   = '" .$model->semester ."' AND T5.CHAIRCD = T1.CHAIRCD , ";     //講座データ
//------------------2004-07-30 naka
        $query .= "    (SELECT ";
        $query .= "        WK1.chaircd, ";
        $query .= "        SUBSTR(MIN(WK1.trgtgrade || WK1.trgtclass),1,2) AS trgtgrade, ";
        $query .= "        SUBSTR(MIN(WK1.trgtgrade || WK1.trgtclass),3,3) AS trgtclass ";
        $query .= "    FROM ";
        $query .= "        (SELECT ";
        $query .= "            k1.year, k1.semester, k2.chaircd, k1.trgtgrade, k1.trgtclass ";
        $query .= "        FROM ";
        $query .= "            chair_cls_dat k1, ";
        $query .= "            chair_dat k2 ";
        $query .= "        WHERE ";
        $query .= "            k1.year       = k2.year AND ";
        $query .= "            k1.semester   = k2.semester AND ";
        $query .= "            k1.chaircd    = '0000000' AND ";
        $query .= "            k1.year       = '" .$model->year ."' AND ";
        $query .= "            k1.semester   = '" .$model->semester ."' AND ";
        $query .= "            k1.groupcd    = k2.groupcd ";
        $query .= "        UNION   ";
        $query .= "        SELECT ";
        $query .= "            t1.year, t1.semester, t2.chaircd, t1.trgtgrade, t1.trgtclass ";
        $query .= "        FROM ";
        $query .= "            chair_cls_dat t1, ";
        $query .= "            chair_dat t2 ";
        $query .= "        WHERE ";
        $query .= "            t1.year       = t2.year AND ";
        $query .= "            t1.semester   = t2.semester AND ";
        $query .= "            t1.groupcd    = t2.groupcd AND ";
        $query .= "            t1.year       = '" .$model->year ."' AND ";
        $query .= "            t1.semester   = '" .$model->semester ."' AND ";
        $query .= "            t1.chaircd    = t2.chaircd ";
        $query .= "        ) WK1 ";
        $query .= "    GROUP BY ";
        $query .= "        WK1.chaircd ";
        $query .= "    ) T2, ";
/********************2004-07-30 naka
        $query .= "    (SELECT DISTINCT ";
        $query .= "        CHAIRCD, ";
        $query .= "        SUBSTR(MIN(TRGTGRADE || TRGTCLASS),1,2) TRGTGRADE, ";
        $query .= "        SUBSTR(MIN(TRGTGRADE || TRGTCLASS),3,3) TRGTCLASS ";		//  04/07/21  yamauchi
//        $query .= "        SUBSTR(MIN(TRGTGRADE || TRGTCLASS),3,2) TRGTCLASS ";
        $query .= "    FROM ";
        $query .= "        CHAIR_CLS_DAT  ";       //講座受講クラスデータ
        $query .= "    WHERE ";
        $query .= "        YEAR = '" .$model->year ."' AND ";
        $query .= "        SEMESTER   = '" .$model->semester ."' ";
        $query .= "    GROUP BY ";
        $query .= "        CHAIRCD ";
        $query .= "    ) T2, ";
********************/
//------------------2004-08-11 naka 適用開始日付を追加
        $query .= "    (SELECT ";
        $query .= "        chaircd,appdate ";
        $query .= "    FROM ";
        $query .= "        chair_std_dat ";
        $query .= "    WHERE ";
        $query .= "        year='" .$model->year ."' AND semester='" .$model->semester ."'  ";
        $query .= "    GROUP BY ";
        $query .= "        chaircd,appdate ";
        $query .= "    ) T8, ";
//---------------------------------
        $query .= "    CLASS_MST T3, ";       //教科マスタ
        $query .= "    SUBCLASS_MST T4, ";    //科目マスタ
        $query .= "    STAFF_MST T6, ";       //職員マスタ
        $query .= "    SCHREG_REGD_HDAT T7 "; //学籍在籍ヘッダデータ
        $query .= "WHERE ";
        $query .= "    T2.CHAIRCD    = T1.CHAIRCD AND ";
        $query .= "    T1.CHAIRCD IN ('" .implode($model->checked_attend,"','") ."') AND ";
        $query .= "    T8.CHAIRCD    = T1.CHAIRCD AND ";		//2004-08-11 naka
        if ($model->cmd == "main" && isset($model->subclasscd) && $model->disp != "TEST"){
        $query .= "    T5.SUBCLASSCD = '" .$model->subclasscd[0] ."' AND ";
        }
        $query .= "    T3.CLASSCD    = SUBSTR(T5.SUBCLASSCD,1,2) AND ";
        $query .= "    T4.SUBCLASSCD = T5.SUBCLASSCD AND ";
        $query .= "    T1.STAFFCD    = '" .$model->checked_staff ."' AND ";		//2004-08-11 naka
        $query .= "    T8.APPDATE    = '" .$model->checked_appd ."' AND ";		//2004-08-11 naka
        $query .= "    T6.STAFFCD    = T1.STAFFCD AND ";
        $query .= "    T7.YEAR       = '" .$model->year ."' AND ";
        $query .= "    T7.SEMESTER   = '" .$model->semester ."' AND ";
        $query .= "    T2.TRGTGRADE  = T7.GRADE AND ";
        $query .= "    T2.TRGTCLASS  = T7.HR_CLASS ";
        $query .= "ORDER BY ";
        $query .= "    1, ";
        $query .= "    2, ";
        $query .= "    6, ";
        $query .= "    7 ";

        return $query;
    }
}
?>
