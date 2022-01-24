<?php

require_once('for_php7.php');

class knjxtoke2Query extends Query {
    //V_科目担任データ取得
    function SQLGet_Main($model)
    {
        $query = "SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T3.CLASSCD || '-' || T3.SCHOOL_KIND AS CLASSCD, ";
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
        $query .= "    T6.STAFFCD, ";               //2004-07/30 naka
        $query .= "    T7.GRADE || T7.HR_CLASS, ";          //2004-08-24 naka sort
        $query .= "    T5.CHAIRNAME AS CLASSALIAS, ";
        $query .= "    T6.CHARGEDIV ";
        $query .= "FROM ";
        $query .= "    (SELECT ";
        $query .= "        TT1.CHAIRCD, ";
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
        $query .= "             YEAR     = '" .$model->year ."' AND ";
        $query .= "             SEMESTER = '" .$model->semester ."'";
        $query .= "         ) TT2";
        $query .= "    WHERE ";
        $query .= "        TT1.PERIODCD > '0' AND ";
        $query .= "         TT1.EXECUTEDATE BETWEEN TT2.SDATE AND TT2.EDATE ";
//2004-07-29 naka
//        $query .= "        TT1.EXECUTEDATE BETWEEN '" .str_replace("/","-",$model->semi_start ) ."'";  //学期開始日
//        $query .= "        AND '" .str_replace("/","-",$model->semi_end ) ."'";       //学期終了日
        $query .= "    GROUP BY ";
        $query .= "        TT1.CHAIRCD ";
        $query .= "    ) T1 ";
        $query .= "    INNER JOIN CHAIR_DAT T5 ON T5.YEAR = '" .$model->year ."' AND T5.SEMESTER   = '" .$model->semester ."' AND T5.CHAIRCD = T1.CHAIRCD , ";     //講座データ
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
        $query .= "        TT3.YEAR = '" .$model->year ."' AND ";
        $query .= "        TT3.SEMESTER   = '" .$model->semester ."' AND ";
        //制限付き参照・更新可は自分が受け持つクラスのみ
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT){
            $query .= "  TT3.STAFFCD  = '" .$model->staffcd ."' AND ";      //2004/03/24 nakamoto add
        }elseif ($model->auth == DEF_NOAUTH){
            $query .= "     TT3.STAFFCD    IS NULL AND";
        }
        $query .= "        TT3.STAFFCD    = TT4.STAFFCD  ";
        $query .= "    ) T6, ";
//------------------2004-07-30 naka
        $query .= "    (SELECT ";
        $query .= "        WK1.chaircd, ";
        $query .= "        MIN(WK1.trgtgrade || WK1.trgtclass) AS trgtgradeclass ";
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
        $query .= "        SUBSTR(MIN(TRGTGRADE || TRGTCLASS),3,3) TRGTCLASS ";     //  04/07/23  yamauchi
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
        $query .= "    CLASS_MST T3, ";     //教科マスタ
        $query .= "    SUBCLASS_MST T4, ";      //科目マスタ
        $query .= "    SCHREG_REGD_HDAT T7 "; //学籍在籍ヘッダデータ
        $query .= "WHERE ";
        $query .= "    T2.CHAIRCD    = T1.CHAIRCD AND ";
        $query .= "    T6.CHAIRCD    = T1.CHAIRCD AND ";
        if (isset($model->checked_attend) && $model->cmd == "left"){
            $query .= " T1.CHAIRCD IN ('" .implode($model->checked_attend,"','") ."') AND ";
        }elseif($model->cmd == "main" && $model->disp == "TEST" ){ //
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || T5.SUBCLASSCD = '" .$model->subclasscd[0] ."' AND ";
            } else {
                $query .= "    T5.SUBCLASSCD = '" .$model->subclasscd[0] ."' AND ";
            }
        }
        if ($model->Properties["useCurriculumcd"] == '1') {
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
//2004/04/01 nakamoto 教科コード仕様の変更に伴う修正
        $query .= "    substr(T5.SUBCLASSCD,1,2) < '90' AND ";
        $query .= "    T7.YEAR       = '" .$model->year ."' AND ";
        $query .= "    T7.SEMESTER   = '" .$model->semester ."' AND ";
        $query .= "    T2.TRGTGRADECLASS  = T7.GRADE || T7.HR_CLASS ";
        $query .= "ORDER BY ";
        $query .= "    1, ";
        $query .= "    2, ";
        $query .= "    13, ";       //2004-08-24 naka
        $query .= "    7 ";
        
        return $query;
    }
}
?>