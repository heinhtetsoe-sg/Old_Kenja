<?php

require_once('for_php7.php');

class knjxtoke4Query extends Query {
    //V_科目担任データ取得
    function SQLGet_Main($data)
    {
        $query = "SELECT DISTINCT ";
        if ($data->Properties["useCurriculumcd"] == '1') {
            $query .= "    T3.CLASSCD || '-' || T3.SCHOOL_KIND  AS CLASSCD, ";
            $query .= "    T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || T5.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "    T3.CLASSCD, ";
            $query .= "    T5.SUBCLASSCD, ";
        }
        $query .= "    T5.CHAIRCD ATTENDCLASSCD, ";
        $query .= "    T3.CLASSNAME, ";
        $query .= "    T4.SUBCLASSNAME, ";
        $query .= "    (CASE WHEN T5.GROUPCD='0000' THEN (T7.HR_NAME) ELSE (T7.HR_NAME || '*') END )TARGETCLASS, ";
        $query .= "    T1.STARTDAY, ";
        $query .= "    T1.ENDDAY, ";
        $query .= "    T5.GROUPCD, ";
        $query .= "    T5.CHAIRNAME GROUPNAME, ";
        $query .= "    T6.STAFFNAME_SHOW, ";
        $query .= "    T6.STAFFCD, ";				//2004-07/30 naka
        $query .= "    T7.GRADE || T7.HR_CLASS, ";			//2004-08-24 naka sort
        $query .= "    T5.CHAIRNAME AS CLASSALIAS, ";
        $query .= "    T6.CHARGEDIV ";
//      $query .= "    T5.GROUPSEQ ";  2004/05/24 delete by teruya
        $query .= "FROM ";
        $query .= "    (SELECT ";
        $query .= "        TT1.CHAIRCD, ";
        $query .= "        TT1.YEAR, ";
        $query .= "        MIN(DATE(TT1.EXECUTEDATE)) STARTDAY, ";
        $query .= "        MAX(DATE(TT1.EXECUTEDATE)) ENDDAY ";
        $query .= "    FROM ";
        $query .= "        SCH_CHR_DAT   TT1  ,";    //時間割データ
        $query .= "         (SELECT";
        $query .= "             SDATE,";
        $query .= "             EDATE";
        $query .= "         FROM";
        $query .= "             SEMESTER_MST";
        $query .= "         WHERE";
        $query .= "             YEAR     = '" .$data->year ."' AND ";
        $query .= "             SEMESTER = '" .$data->semester ."'";
        $query .= "         ) TT2";
        $query .= "    WHERE ";
        $query .= "        TT1.YEAR = '" .$data->year ."' AND ";
        $query .= "        TT1.PERIODCD > '0' AND ";
        $query .= "         TT1.EXECUTEDATE BETWEEN TT2.SDATE AND TT2.EDATE ";
//2004-07-29 naka
//        $query .= "        TT1.EXECUTEDATE BETWEEN '" .str_replace("/","-",$data->semi_start ) ."'";  //学期開始日
//        $query .= "        AND '" .str_replace("/","-",$data->semi_end ) ."'";       //学期終了日
        $query .= "    GROUP BY ";
        $query .= "        TT1.CHAIRCD,TT1.YEAR ";
        $query .= "    ) T1 ";
        $query .= "    INNER JOIN CHAIR_DAT T5 ON T5.YEAR = T1.YEAR AND T5.SEMESTER   = '" .$data->semester ."' AND T5.CHAIRCD = T1.CHAIRCD , ";     //講座データ
        $query .= "    (SELECT DISTINCT ";
        $query .= "        TT4.STAFFNAME_SHOW, ";
        $query .= "        TT3.STAFFCD, ";
        $query .= "        TT3.CHARGEDIV, ";
        $query .= "        TT3.CHAIRCD ";
//2004-07-30 naka
//        $query .= "        * ";
        $query .= "     FROM   ";    
        $query .= "        CHAIR_STF_DAT TT3,   ";    //講座担当職員データ
        $query .= "        STAFF_MST TT4 ";      //職員マスタ
        $query .= "    WHERE ";
        $query .= "        TT3.YEAR = '" .$data->year ."' AND ";
        $query .= "        TT3.SEMESTER   = '" .$data->semester ."' AND ";
        //制限付き参照・更新可は自分が受け持つクラスのみ
        if ($data->auth == DEF_REFER_RESTRICT || $data->auth == DEF_UPDATE_RESTRICT){
            $query .= "  TT3.STAFFCD  = '" .$data->staffcd ."' AND ";		//2004/03/24 nakamoto add
/* 2004/03/24 nakamoto del
            $query .= "    (TT3.STAFFCD    =   '" .$data->staffcd ."' OR ";
            $query .= "     TT3.CHAIRCD IN (SELECT DISTINCT ";
            $query .= "                      CHAIRCD ";
            $query .= "                     FROM ";
            $query .= "                      CHAIR_CLS_DAT TT5, ";       //講座受講クラスデータ
            $query .= "                      SCHREG_REGD_HDAT TT6 "; //学籍在籍ヘッダデータ
            $query .= "                     WHERE ";
            $query .= "                      TT5.YEAR = '" .$data->year ."' AND ";
            $query .= "                      TT5.SEMESTER   = '" .$data->semester ."' AND ";
            $query .= "                      TT5.YEAR = TT6.YEAR AND ";
            $query .= "                      TT5.SEMESTER   = TT6.SEMESTER AND ";
            $query .= "                      TT5.TRGTGRADE      = TT6.GRADE AND ";
            $query .= "                      TT5.TRGTCLASS   = TT6.HR_CLASS AND ";
            $query .= "                     (TT6.TR_CD1   = '" .$data->staffcd ."' OR ";
            $query .= "                      TT6.TR_CD1   = '" .$data->staffcd ."' OR ";
            $query .= "                      TT6.TR_CD1   = '" .$data->staffcd ."' OR ";
            $query .= "                      TT6.SUBTR_CD1   = '" .$data->staffcd ."' OR ";
            $query .= "                      TT6.SUBTR_CD1   = '" .$data->staffcd ."' OR ";
            $query .= "                      TT6.SUBTR_CD1   = '" .$data->staffcd ."') ";
            $query .= "                    ) ";
            $query .= "    ) AND ";
*/
        }elseif ($data->auth == DEF_NOAUTH){		//権限なし(表示不可)
            $query .= "     TT3.STAFFCD    IS NULL AND";
        }
        $query .= "        TT3.STAFFCD    = TT4.STAFFCD  ";
        $query .= "    ) T6, ";
//------------------2004-07-29 naka
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
        $query .= "            k1.year       = '" .$data->year ."' AND ";
        $query .= "            k1.semester   = '" .$data->semester ."' AND ";
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
        $query .= "            t1.year       = '" .$data->year ."' AND ";
        $query .= "            t1.semester   = '" .$data->semester ."' AND ";
        $query .= "            t1.chaircd    = t2.chaircd ";
        $query .= "        ) WK1 ";
        $query .= "    GROUP BY ";
        $query .= "        WK1.chaircd ";
        $query .= "    ) T2, ";
/********************2004-07-29 naka
        $query .= "    (SELECT DISTINCT ";
        $query .= "        CHAIRCD, ";
        $query .= "        SUBSTR(MIN(TRGTGRADE || TRGTCLASS),1,2) TRGTGRADE, ";
        $query .= "        SUBSTR(MIN(TRGTGRADE || TRGTCLASS),3,3) TRGTCLASS ";		//  04/07/23  yamauchi
//        $query .= "        SUBSTR(MIN(TRGTGRADE || TRGTCLASS),3,2) TRGTCLASS ";
        $query .= "    FROM ";
        $query .= "        CHAIR_CLS_DAT  ";       //講座受講クラスデータ
        $query .= "    WHERE ";
        $query .= "        YEAR = '" .$data->year ."' AND ";
        $query .= "        SEMESTER   = '" .$data->semester ."' ";
        $query .= "    GROUP BY ";
        $query .= "        CHAIRCD ";
        $query .= "    ) T2, ";
********************/
        $query .= "    CLASS_MST T3, ";     //教科マスタ
        $query .= "    SUBCLASS_MST T4, ";      //科目マスタ
        $query .= "    SCHREG_REGD_HDAT T7 "; //学籍在籍ヘッダデータ
        $query .= "WHERE ";
        $query .= "    T2.CHAIRCD    = T1.CHAIRCD AND ";
        $query .= "    T6.CHAIRCD    = T1.CHAIRCD AND ";
        if (isset($data->checked_attend) && $data->cmd == "left"){
            $query .= " T1.CHAIRCD IN ('" .implode($data->checked_attend,"','") ."') AND ";
        } elseif($data->cmd == "main" && $data->disp == "TEST" ){ //得点入力            
            if ($data->Properties["useCurriculumcd"] == '1') {
                $query .= "    T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || T5.SUBCLASSCD = '" .$data->subclasscd[0] ."' AND ";
            } else {
                $query .= "    T5.SUBCLASSCD = '" .$data->subclasscd[0] ."' AND ";
            }
            $query .= "    T1.CHAIRCD IN (SELECT DISTINCT";
            $query .= "                            CHAIRCD ";
            $query .= "                        FROM ";
            $query .= "                            TESTSCORE_HDAT ";
            $query .= "                        WHERE ";
            $query .= "                            YEAR         = '" .$data->year ."' AND ";
            $query .= "                            SEMESTER     = '" .$data->semester ."' AND ";
            $query .= "                            TESTKINDCD = '" .$data->testkindcd ."' AND ";
            $query .= "                            TESTITEMCD = '" .$data->testitemcd ."' AND ";
            $query .= "                            OPERATION_FLG = '1' ";
            $query .= "                        ) AND ";
        //学期末成績処理
        } else if ($data->cmd == "main" && isset($data->subclasscd) && $data->disp == "CLASS"){
            if ($data->Properties["useCurriculumcd"] == '1') {
                $query .= "    T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || T5.SUBCLASSCD = '" .$data->subclasscd[0] ."' AND ";
            } else {
                $query .= "    T5.SUBCLASSCD = '" .$data->subclasscd[0] ."' AND ";
            }
        }
//2004/04/01 nakamoto 教科コード仕様の変更に伴う修正
        $query .= "    substr(T5.SUBCLASSCD,1,2) < '90' AND ";

        if ($data->Properties["useCurriculumcd"] == '1') {
            $query .= "    T3.CLASSCD       = T5.CLASSCD AND ";
            $query .= "    T3.SCHOOL_KIND   = T5.SCHOOL_KIND AND ";
            $query .= "    T4.CLASSCD       = T5.CLASSCD AND ";
            $query .= "    T4.SCHOOL_KIND   = T5.SCHOOL_KIND AND ";
            $query .= "    T4.CURRICULUM_CD = T5.CURRICULUM_CD AND ";
            $query .= "    T4.SUBCLASSCD    = T5.SUBCLASSCD AND ";
        } else {
            $query .= "    T3.CLASSCD    = SUBSTR(T5.SUBCLASSCD,1,2) AND ";
            $query .= "    T4.SUBCLASSCD = T5.SUBCLASSCD AND ";
        }
        $query .= "    T7.YEAR       = '" .$data->year ."' AND ";
        $query .= "    T7.SEMESTER   = '" .$data->semester ."' AND ";
        $query .= "    T2.TRGTGRADE  = T7.GRADE AND ";
        $query .= "    T2.TRGTCLASS  = T7.HR_CLASS ";
        $query .= "ORDER BY ";
        $query .= "    1, ";
        $query .= "    2, ";
        $query .= "    13, ";		//2004-08-24 naka
        $query .= "    7 ";

//		print($query);
        return $query;
    }



    //科目ツリー用のSQL
    function selectQuerySubclass($model)	//2004/04/01 処理時間の改善のためのＳＱＬ修正版
    {
        $query = "";

        $query .= "SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  T3.CLASSCD || '-' || T3.SCHOOL_KIND AS CLASSCD, ";
            $query .= "  T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "  T3.CLASSCD, ";
            $query .= "  T4.SUBCLASSCD, ";
        }
        $query .= "  T3.CLASSNAME, ";
        $query .= "  T4.SUBCLASSNAME, ";
        $query .= "  T6.TESTITEMCD, ";
        $query .= "  T6.TESTKINDCD, ";
        $query .= "  T6.TESTITEMNAME ";
        $query .= "FROM ";
        $query .= "     (";
        $query .= "       SELECT";
        $query .= "           YEAR,";
        $query .= "           SEMESTER,";
        $query .= "           CHAIRCD,";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           CLASSCD,";
            $query .= "           SCHOOL_KIND,";
            $query .= "           CURRICULUM_CD,";
        }
        $query .= "           SUBCLASSCD";
        $query .= "       FROM";
        $query .= "           CHAIR_DAT";
        $query .= "       WHERE";
        $query .= "           YEAR       = '" .CTRL_YEAR ."' AND";
        $query .= "           SEMESTER   = '" .$model->semester ."' AND";
        $query .= "           SUBSTR(SUBCLASSCD,1,2) < '90'";
        $query .= "     ) T1 LEFT OUTER JOIN (SELECT ";
        $query .= "            ST1.YEAR, ";
        $query .= "            ST1.SEMESTER, ";
        $query .= "            ST1.CHAIRCD, ";
        $query .= "            ST1.TESTITEMCD, ";
        $query .= "            ST1.TESTKINDCD, ";
        $query .= "            ST2.TESTITEMNAME, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           ST2.CLASSCD,";
            $query .= "           ST2.SCHOOL_KIND,";
            $query .= "           ST2.CURRICULUM_CD,";
        }
        $query .= "            ST2.SUBCLASSCD ";
        $query .= "          FROM ";
        $query .= "            TESTSCORE_HDAT ST1, ";
        $query .= "            TESTITEM_MST ST2 ";
        $query .= "          WHERE ";
        $query .= "            ST1.YEAR       = '" .CTRL_YEAR ."' AND ";
        $query .= "            ST1.SEMESTER   = '" .$model->semester ."' AND ";
        $query .= "            ST1.TESTITEMCD = ST2.TESTITEMCD AND ";
        $query .= "            ST1.TESTKINDCD = ST2.TESTKINDCD ";
        $query .= "          ) T6 ON T1.YEAR       = T6.YEAR AND  ";
        $query .= "                  T1.SEMESTER   = T6.SEMESTER AND  ";
        $query .= "                  T1.CHAIRCD    = T6.CHAIRCD AND  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                  T1.CLASSCD         = T6.CLASSCD AND";
            $query .= "                  T1.SCHOOL_KIND     = T6.SCHOOL_KIND AND ";
            $query .= "                  T1.CURRICULUM_CD   = T6.CURRICULUM_CD AND ";
        }
        $query .= "                  T1.SUBCLASSCD = T6.SUBCLASSCD, ";
        $query .= "     (SELECT";
        $query .= "         YEAR,";
        $query .= "         SEMESTER,";
        $query .= "         CHAIRCD,";
        $query .= "         STAFFCD";
        $query .= "      FROM";
        $query .= "         CHAIR_STF_DAT ";
        $query .= "      WHERE";
        $query .= "           YEAR     = '" .CTRL_YEAR ."' AND";
        $query .= "           SEMESTER = '" .$model->semester ."'";
        $query .= "     ) T2,";
        $query .= "     CLASS_MST     T3, ";
        $query .= "     SUBCLASS_MST  T4, ";
        $query .= "     (SELECT";
        $query .= "         SST1.YEAR,";
        $query .= "         SST1.CHAIRCD,";
        $query .= "         MIN(EXECUTEDATE) SDATE,";
        $query .= "         MAX(EXECUTEDATE) EDATE";
        $query .= "     FROM";
        $query .= "         SCH_CHR_DAT SST1    ,";
        $query .= "         (SELECT";
        $query .= "             SDATE,";
        $query .= "             EDATE";
        $query .= "         FROM";
        $query .= "             SEMESTER_MST";
        $query .= "         WHERE";
        $query .= "             YEAR     = '" .CTRL_YEAR ."' AND ";
        $query .= "             SEMESTER = '" .$model->semester ."'";
        $query .= "         ) SST2";
        $query .= "     WHERE";
        $query .= "         SST1.PERIODCD > '0' AND";
        $query .= "         SST1.EXECUTEDATE BETWEEN SST2.SDATE AND";
        $query .= "         SST2.EDATE";
        $query .= "     GROUP BY";
        $query .= "         SST1.CHAIRCD,";
        $query .= "         SST1.YEAR";
        $query .= "     ) T5 ";
        $query .= "WHERE ";
        $query .= "  T1.YEAR     = T2.YEAR AND ";
        $query .= "  T1.SEMESTER = T2.SEMESTER AND ";
        //制限付き参照・更新可は自分が受け持つクラスのみ
        if ($model->auth == DEF_UPDATE_RESTRICT || $model->auth == DEF_REFER_RESTRICT){
            $query .= "  T2.STAFFCD  = '" .STAFFCD ."' AND ";        
        }elseif ($model->auth == DEF_NOAUTH){        //権限なし(表示不可)
            $query .= "  T2.STAFFCD  IS NULL AND ";
        }
        $query .= "  T1.CHAIRCD  = T2.CHAIRCD AND ";
        $query .= "  T1.CHAIRCD  = T5.CHAIRCD AND ";

        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  T1.CLASSCD         = T3.CLASSCD AND ";
            $query .= "  T1.SCHOOL_KIND     = T3.SCHOOL_KIND AND ";
            $query .= "  T1.CLASSCD         = T4.CLASSCD AND ";
            $query .= "  T1.SCHOOL_KIND     = T4.SCHOOL_KIND AND ";
            $query .= "  T1.CURRICULUM_CD   = T4.CURRICULUM_CD AND ";
            $query .= "  T1.SUBCLASSCD      = T4.SUBCLASSCD ";
        } else {
            $query .= "  SUBSTR(T1.SUBCLASSCD,1,2) = T3.CLASSCD AND ";
            $query .= "  T1.SUBCLASSCD             = T4.SUBCLASSCD ";
        }
        $query .= "ORDER BY ";
        $query .= "  CLASSCD, ";
        $query .= "  SUBCLASSCD, ";
        $query .= "  T6.TESTITEMCD, ";
        $query .= "  T6.TESTKINDCD ";

        return $query;
    }
}
?>