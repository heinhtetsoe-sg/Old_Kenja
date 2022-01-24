<?php

require_once('for_php7.php');

class knjxtoke3Query extends Query {
    //V_科目担任データ取得
    function SQLGet_Main($model)
    {
        $query = "SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T3.CLASSCD || '-' || T3.SCHOOL_KIND  AS CLASSCD, ";
            $query .= "    T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || T5.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "    T3.CLASSCD, ";
            $query .= "    T5.SUBCLASSCD, ";
        }
        $query .= "    T3.CLASSNAME, ";
        $query .= "    T4.SUBCLASSNAME ";
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
        $query .= "             YEAR     = '" .$model->year ."' AND ";
        $query .= "             SEMESTER = '" .$model->semester ."'";
        $query .= "         ) TT2";
        $query .= "    WHERE ";
        $query .= "        TT1.YEAR = '" .$model->year ."' AND ";
        $query .= "        TT1.PERIODCD > '0' AND ";
        $query .= "         TT1.EXECUTEDATE BETWEEN TT2.SDATE AND TT2.EDATE ";
//2004-07-29 naka
//        $query .= "        TT1.EXECUTEDATE BETWEEN '" .str_replace("/","-",$model->semi_start ) ."'";  //学期開始日
//        $query .= "        AND '" .str_replace("/","-",$model->semi_end ) ."'";       //学期終了日
        $query .= "    GROUP BY ";
        $query .= "        TT1.CHAIRCD,TT1.YEAR ";
        $query .= "    ) T1 ";
        $query .= "    INNER JOIN CHAIR_DAT T5 ON T5.YEAR = T1.YEAR AND T5.SEMESTER   = '" .$model->semester ."' AND T5.CHAIRCD = T1.CHAIRCD , ";     //講座データ
        $query .= "    (SELECT DISTINCT ";
        $query .= "        * ";
        $query .= "     FROM   ";    
        $query .= "        CHAIR_STF_DAT TT3,   ";    //講座担当職員データ
        $query .= "        STAFF_MST TT4 ";      //職員マスタ
        $query .= "    WHERE ";
        $query .= "        TT3.YEAR = '" .$model->year ."' AND ";
        $query .= "        TT3.SEMESTER   = '" .$model->semester ."' AND ";
        //制限付き参照・更新可は自分が受け持つクラスのみ
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT){
            $query .= "  TT3.STAFFCD  = '" .$model->staffcd ."' AND ";		//2004/03/24 nakamoto add
/* 2004/03/24 nakamoto del
            $query .= "    (TT3.STAFFCD    =   '" .$model->staffcd ."' OR ";
            $query .= "     TT3.CHAIRCD IN (SELECT DISTINCT ";
            $query .= "                      CHAIRCD ";
            $query .= "                     FROM ";
            $query .= "                      CHAIR_CLS_DAT TT5, ";       //講座受講クラスデータ
            $query .= "                      SCHREG_REGD_HDAT TT6 "; //学籍在籍ヘッダデータ
            $query .= "                     WHERE ";
            $query .= "                      TT5.YEAR = '" .$model->year ."' AND ";
            $query .= "                      TT5.SEMESTER   = '" .$model->semester ."' AND ";
            $query .= "                      TT5.YEAR = TT6.YEAR AND ";
            $query .= "                      TT5.SEMESTER   = TT6.SEMESTER AND ";
            $query .= "                      TT5.TRGTGRADE      = TT6.GRADE AND ";
            $query .= "                      TT5.TRGTCLASS   = TT6.HR_CLASS AND ";
            $query .= "                     (TT6.TR_CD1   = '" .$model->staffcd ."' OR ";
            $query .= "                      TT6.TR_CD1   = '" .$model->staffcd ."' OR ";
            $query .= "                      TT6.TR_CD1   = '" .$model->staffcd ."' OR ";
            $query .= "                      TT6.SUBTR_CD1   = '" .$model->staffcd ."' OR ";
            $query .= "                      TT6.SUBTR_CD1   = '" .$model->staffcd ."' OR ";
            $query .= "                      TT6.SUBTR_CD1   = '" .$model->staffcd ."') ";
            $query .= "                    ) ";
            $query .= "    ) AND ";
*/
        }elseif ($model->auth == DEF_NOAUTH){
            $query .= "     TT3.STAFFCD    IS NULL AND";
        }
        $query .= "        TT3.STAFFCD    = TT4.STAFFCD  ";
        $query .= "    ) T6, ";
        $query .= "    CLASS_MST T3, ";     //教科マスタ
        $query .= "    SUBCLASS_MST T4 ";      //科目マスタ
        $query .= "WHERE ";
        $query .= "    T6.CHAIRCD    = T1.CHAIRCD AND ";
        if (isset($model->classcd) && $model->cmd == "main" ){
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    T5.CLASSCD || '-' || T5.SCHOOL_KIND = '" .$model->classcd ."' AND ";
            } else {
                $query .= "    SUBSTR(T5.SUBCLASSCD,1,2) = '" .$model->classcd ."' AND ";
            }
        }
        else {
//2004/04/01 nakamoto 教科コード仕様の変更に伴う修正
        $query .= "    substr(T5.SUBCLASSCD,1,2) <= '99' AND ";//---2005.05.17---2005.05.18
//        $query .= "    SUBSTR(T5.SUBCLASSCD,1,2) BETWEEN '01' AND '49' AND ";
        }
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T3.CLASSCD       = T5.CLASSCD AND ";
            $query .= "    T3.SCHOOL_KIND   = T5.SCHOOL_KIND AND ";
            $query .= "    T4.CLASSCD       = T5.CLASSCD AND ";
            $query .= "    T4.SCHOOL_KIND   = T5.SCHOOL_KIND AND ";
            $query .= "    T4.CURRICULUM_CD = T5.CURRICULUM_CD AND ";
            $query .= "    T4.SUBCLASSCD    = T5.SUBCLASSCD ";
        } else {
            $query .= "    T3.CLASSCD    = SUBSTR(T5.SUBCLASSCD,1,2) AND ";
            $query .= "    T4.SUBCLASSCD = T5.SUBCLASSCD ";
        }
        $query .= "ORDER BY ";
        $query .= "    1, ";
        $query .= "    2 ";
        
        return $query;
    }
}
?>