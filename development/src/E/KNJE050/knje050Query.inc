<?php

require_once('for_php7.php');

class knje050query extends Query {


    //学期名を取得
    function getThisSemester($year,$semes)
    {
        $query  = "SELECT SEMESTERNAME FROM SEMESTER_MST ";
        $query .= "WHERE YEAR     = '".$year."' AND ";
        $query .= "      SEMESTER = '".$semes."'";
        return $query;
    }

    //学年取得
    function selectGradeQuery($model)
    {
        $query = "SELECT DISTINCT ";
        $query .= "    GRADE ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR     = '" .CTRL_YEAR ."' AND ";
        $query .= "    SEMESTER = '" .CTRL_SEMESTER ."' ";
        $query .= "ORDER BY GRADE ";
        return $query;
    }

    //学籍在籍ヘッダデータより
    function selectLeftQuery($model)
    {
        $query = "SELECT DISTINCT ";
        $query .= "    T1.GRADE, ";
        $query .= "    T1.COURSECODE, ";
        $query .= "    T2.COURSECODENAME NAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    LEFT OUTER JOIN V_COURSECODE_MST T2 ON (T1.YEAR = T2.YEAR) AND (T1.COURSECODE = T2.COURSECODE) ";
        $query .= "WHERE ";
        $query .= "    T1.COURSECODE IS NOT NULL  AND ";
        $query .= "    T1.YEAR       = '" .CTRL_YEAR ."' AND ";
        $query .= "    T1.SEMESTER   = '" .CTRL_SEMESTER ."' AND ";
        $query .= "    T1.GRADE      = '" .sprintf("%02d", $model->field["GRADE"]) ."' ";
        $query .= "ORDER BY ";
        $query .= "    T1.GRADE, T1.COURSECODE ";

        return $query;
    }

    //生徒一覧
    function selectQuery($model)
    {
        $query  = "SELECT DISTINCT";
        $query .= "       T1.SCHREGNO";
        $query .= "      ,T1.GRADE";
        $query .= "      ,T1.HR_CLASS";
        $query .= "      ,T1.ATTENDNO";
        $query .= "      ,T3.NAME_SHOW";
        $query .= "      ,T3.NAME_KANA";
        $query .= "  FROM SCHREG_REGD_DAT T1";
        $query .= "      ,SCHREG_BASE_MST T3 ";
        $query .= " WHERE T1.YEAR        = '" .CTRL_YEAR ."'";
        $query .= "   AND T1.SEMESTER    = '" .CTRL_SEMESTER ."'";
        $query .= "   AND T1.GRADE       = '" .$model->field["GRADE"]  ."'";
        $query .= "   AND T1.COURSECODE = '" .sprintf("%04d", $model->field["COURSECODE"]) ."'";
        $query .= "   AND T1.SCHREGNO    = T3.SCHREGNO ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO , ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T3.NAME_SHOW, ";
        $query .= "     T3.NAME_KANA ";

#        if (isset($model->sort)){
#            $query .= " ORDER BY ";
#            foreach ($model->sort as $field => $order){
#                switch($field){
#            case "ATTENDNO":    //番号順
#                if($order > 0){
#                    $query .= "T1.HR_CLASS,T1.ATTENDNO ";
#                }else{
#                    $query .=" T1.HR_CLASS DESC,T1.ATTENDNO DESC ";
#                }
#                break 2;
#            case "CHG_GRADES":   //値変更
#                if ($order > 0){
#                    $query .= "CHG_GRADES DESC,T1.HR_CLASS,T1.ATTENDNO ";
#                }else{
#                    $query .= "CHG_GRADES,T1.HR_CLASS ,T1.ATTENDNO";
#                }
#                break 2;
#                }
#            }
#        }

        if (isset($model->sort)){
            $query .= " ORDER BY ";
            if($model->sort > 0){
                $query .= "T1.HR_CLASS,T1.ATTENDNO ";
            }else{
                $query .=" T1.HR_CLASS DESC,T1.ATTENDNO DESC ";
            }
        }


        //echo $query;
        return $query;

    }

    //CVS作成用のQUERY
    function selectCvsQuery($model)
    {

        $query = "SELECT DISTINCT";
        $query .= "    '0'               AS SCHOOLCD, ";
        $query .= "    T1.GRADE, ";
        $query .= "    T1.HR_CLASS, ";
        $query .= "    T1.ATTENDNO, ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    T1.NAME_SHOW, ";
        $query .= "    T1.SEX            AS SEXCD, ";
        $query .= "    T1.SEXNAME, ";
        $query .= "    T1.YEAR, ";
        $query .= "    T1.GRADE          AS RGRADE, ";
        $query .= "    T1.COURSECD, ";
        $query .= "    T1.MAJORCD, ";
        $query .= "    T1.COURSECODE, ";
        $query .= "    T1.COURSECODENAME, ";
        $query .= "    T2.CLASSCD, ";
        $query .= "    T2.CLASSNAME, ";
        $query .= "    T2.CLASSABBV, ";
        $query .= "    T2.CLASSNAME_ENG, ";
        $query .= "    T2.CLASSABBV_ENG, ";
        $query .= "    T2.SUBCLASSCD, ";
        $query .= "    T2.SUBCLASSNAME, ";
        $query .= "    T2.SUBCLASSABBV, ";
        $query .= "    T2.SUBCLASSNAME_ENG, ";
        $query .= "    T2.SUBCLASSABBV_ENG, ";
        if (isset($model->field["REPLACE"])){
            $query .= "    CASE WHEN T2.GRAD_VALUE = 1 THEN 2 ELSE T2.GRAD_VALUE END AS GRAD_VALUE, ";
            $query .= "    T4.CREDITS, ";
        }else{
            $query .= "    T2.GRAD_VALUE, ";
            $query .= "    T2.GET_CREDIT AS CREDITS, ";
        }
        $query .= "    T2.ADD_CREDIT     AS ADDCREDITCD, ";
        $query .= "    T3.REMARK ";
        $query .= "FROM ";

        $query .= "  (SELECT ";
        $query .= "    TBL1.SCHREGNO , ";
        $query .= "    TBL1.YEAR, ";
        $query .= "    TBL1.SEMESTER, ";
        $query .= "    TBL1.GRADE, ";
        $query .= "    TBL1.HR_CLASS, ";
        $query .= "    TBL1.ATTENDNO, ";
        $query .= "    TBL1.COURSECD, ";
        $query .= "    TBL1.MAJORCD, ";
        $query .= "    TBL1.COURSECODE, ";
        $query .= "    TBL2.NAME_SHOW, ";
        $query .= "    TBL2.SEX, ";
        $query .= "    TBL3.COURSECODENAME, ";
        $query .= "    MEISYOU_GET(TBL2.SEX,'Z002',1) SEXNAME ";
        $query .= "  FROM ";

        if ($model->control["学校区分"] == "0") {           //学年制
            $query .= "    V_REGDYEAR_GRADE_DAT TBL1 ";
        }else{                                              //単位制
            $query .= "    V_REGDYEAR_UNIT_DAT TBL1 ";
        }

        $query .= "        LEFT OUTER JOIN V_COURSECODE_MST TBL3 ON TBL1.YEAR = TBL3.YEAR AND TBL1.COURSECODE = TBL3.COURSECODE, ";
        $query .= "    SCHREG_BASE_MST TBL2 ";
        $query .= "  WHERE ";
        $query .= "    TBL1.SCHREGNO IN ('" .implode($model->field["CHECKD_SCHREGNO"], "','") ."') AND ";
        $query .= "    TBL1.SCHREGNO = TBL2.SCHREGNO ";
        $query .= "    ) T1 ";

        $query .= "    LEFT OUTER JOIN ( ";
        $query .= "    SELECT ";
        $query .= "       TBL1.YEAR, ";
        $query .= "       TBL1.TAKESEMES, ";
        $query .= "       TBL1.SCHREGNO, ";
        $query .= "       SUBSTR(TBL1.SUBCLASSCD,1,2) CLASSCD, ";
        $query .= "       TBL2.CLASSNAME, ";
        $query .= "       TBL2.CLASSABBV, ";
        $query .= "       TBL2.CLASSNAME_ENG, ";
        $query .= "       TBL2.CLASSABBV_ENG, ";
        $query .= "       TBL1.SUBCLASSCD, ";
        $query .= "       TBL3.SUBCLASSNAME, ";
        $query .= "       TBL3.SUBCLASSABBV, ";
        $query .= "       TBL3.SUBCLASSNAME_ENG, ";
        $query .= "       TBL3.SUBCLASSABBV_ENG, ";
        $query .= "       TBL1.GRAD_VALUE, ";
        $query .= "       TBL1.GET_CREDIT, ";
        $query .= "       TBL1.ADD_CREDIT ";
        $query .= "    FROM ";
#        $query .= "       RECORDGRADE_DAT TBL1 ";
        $query .= "       RECORD_DAT TBL1 ";
        $query .= "           LEFT OUTER JOIN V_CLASS_MST    TBL2 ON TBL1.YEAR = TBL2.YEAR AND SUBSTR(TBL1.SUBCLASSCD,1,2) = TBL2.CLASSCD ";
        $query .= "           LEFT OUTER JOIN V_SUBCLASS_MST TBL3 ON TBL1.YEAR = TBL3.YEAR AND TBL1.SUBCLASSCD             = TBL3.SUBCLASSCD ";
        $query .= "    ) T2 ON T2.YEAR = T1.YEAR AND T2.SCHREGNO = T1.SCHREGNO ";


        $query .= "    LEFT OUTER JOIN ( ";
        $query .= "    SELECT ";
        $query .= "       YEAR,";
        $query .= "       SCHREGNO,";
        $query .= "       CLASSCD,";
        $query .= "       SUBCLASSCD,";
        $query .= "       REMARK ";
        $query .= "    FROM ";
        $query .= "       STUDYCLASSREMARK_DAT ";
        $query .= "    ) T3 ON T3.YEAR = T1.YEAR AND T3.CLASSCD = T2.CLASSCD AND T3.SUBCLASSCD = T2.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO ";

        $query .= "    LEFT OUTER JOIN ( ";
        $query .= "    SELECT ";
        $query .= "       YEAR, ";
        $query .= "       COURSECD, ";
        $query .= "       MAJORCD, ";
        $query .= "       GRADE, ";
        $query .= "       COURSECODE, ";
        $query .= "       CLASSCD, ";
        $query .= "       SUBCLASSCD, ";
        $query .= "       CREDITS ";
        $query .= "    FROM ";
        $query .= "       CREDIT_MST ";
        $query .= "    ) T4 ON ";
        $query .= "            T4.YEAR        = T1.YEAR       AND ";
        $query .= "            T4.COURSECD    = T1.COURSECD   AND ";
        $query .= "            T4.MAJORCD     = T1.MAJORCD    AND ";
        $query .= "            T4.GRADE       = T1.GRADE      AND ";
        $query .= "            T4.COURSECODE  = T1.COURSECODE AND ";
        $query .= "            T4.CLASSCD     = T2.CLASSCD    AND ";
        $query .= "            T4.SUBCLASSCD  = T2.SUBCLASSCD ";

        $query .= "ORDER BY ";
        $query .= "    T1.GRADE, ";
        $query .= "    T1.HR_CLASS, ";
        $query .= "    T1.ATTENDNO, ";
        $query .= "    T2.CLASSCD, ";
        $query .= "    T2.SUBCLASSCD ";

        //echo $query;
        return $query;
    }


    //学年（年次）コンボボックス用(Subform1)
    function selectQueryAnnual($model)
    {
#        $query = " SELECT DISTINCT ";
#        $query .= "   MAX(T1.YEAR) AS ANNUAL_YEAR, ";
#        $query .= "   T1.ANNUAL ";
#        $query .= " FROM ";
#        $query .= "   RECORDGRADE_DAT T1, ";
#        $query .= "   SCHOOL_MST T2 ";
#        $query .= " WHERE ";
#        $query .= "   T1.SCHREGNO = '" .$model->schregno ."' AND ";
#        $query .= "   T2.YEAR = '" .$model->year ."' AND ";
#        $query .= "   T2.SCHOOLDIV = '0' ";  //学年制
#        $query .= " GROUP BY ";
#        $query .= "   T1.ANNUAL ";
#        $query .= " UNION    ";
#        $query .= " SELECT DISTINCT ";
#        $query .= "   T1.YEAR AS ANNUAL_YEAR, ";
#        $query .= "   T1.ANNUAL ";
#        $query .= " FROM ";
#        $query .= "   RECORDGRADE_DAT T1, ";
#        $query .= "   SCHOOL_MST T2 ";
#        $query .= " WHERE ";
#        $query .= "   T1.SCHREGNO = '" .$model->schregno ."' AND ";
#        $query .= "   T2.YEAR = '" .$model->year ."' AND ";
#        $query .= "   T2.SCHOOLDIV = '1' ";  //単位制
#        $query .= " ORDER BY ";
#        $query .= "   1, ";
#        $query .= "   2 ";

        $query = " SELECT DISTINCT ";
        $query .= "   MAX(T1.YEAR) AS ANNUAL_YEAR, ";
        $query .= "   T1.ANNUAL ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.SCHREGNO = '" .$model->schregno ."' AND ";
        $query .= "   T1.YEAR = '" .$model->year ."' ";
        $query .= " GROUP BY ";
        $query .= "   T1.ANNUAL ";


        //echo $query;
        return $query;
    }

    //成績参照画面(Subform1)
    function selectQuerySubForm1($model)
    {
#        $query = " SELECT ";
#        $query .= "   T1.ANNUAL, ";
#        $query .= "   SUBSTR(T1.GRADINGCLASSCD,1,2) AS CLASSCD, ";
#        $query .= "   T2.CLASSNAME, ";
#        $query .= "   T1.GRADINGCLASSCD AS SUBCLASSCD, ";
#        $query .= "   T3.SUBCLASSNAME, ";
#        $query .= "   T1.OLD_TMPVAL, ";
#        $query .= "   T1.NEW_TMPVAL, ";
#        $query .= "   T1.VALUATION, ";
#        $query .= "   T1.GET_CREDIT, ";
#        $query .= "   T1.ADD_CREDIT, ";
#        $query .= "   T4.REMARK ";
#        $query .= " FROM ";
#        $query .= "   RECORDGRADE_DAT T1 ";
#        $query .= "   LEFT OUTER JOIN V_CLASS_MST T2 ON  ";          //教科名称取得
#        $query .= "   T2.YEAR = T1.YEAR AND  ";
#        $query .= "   T2.CLASSCD = SUBSTR(T1.GRADINGCLASSCD,1,2) ";
#        $query .= "   LEFT OUTER JOIN V_SUBCLASS_MST T3 ON  ";       //科目名称取得
#        $query .= "   T3.YEAR = T1.YEAR AND  ";
#        $query .= "   T3.SUBCLASSCD = T1.GRADINGCLASSCD ";
#        $query .= "   LEFT OUTER JOIN STUDYCLASSREMARK_DAT T4 ON  ";   //学籍履修科目備考データ取得
#        $query .= "   T4.YEAR = T1.YEAR AND ";
#        $query .= "   T4.SCHREGNO = T1.SCHREGNO AND ";
#        $query .= "   T4.CLASSCD = T2.CLASSCD AND ";
#        $query .= "   T4.SUBCLASSCD = T3.SUBCLASSCD ";
#        $query .= " WHERE ";
#        if ($model->annual["YEAR"] != "0" || $model->annual["ANNUAL"] != "0"){
#            $query .= "   T1.YEAR = '" .$model->annual["YEAR"] ."' AND ";
#            $query .= "   T1.ANNUAL = '" .$model->annual["ANNUAL"]."' AND";
#        }
#        $query .= "   T1.SCHREGNO = '" .$model->schregno ."' ";
    
    
        $query = " SELECT ";
        $query .= "   SUBSTR(T1.SUBCLASSCD,1,2) AS CLASSCD, ";
        $query .= "   T2.CLASSNAME, ";
        $query .= "   T1.SUBCLASSCD, ";
        $query .= "   T3.SUBCLASSNAME, ";
        $query .= "   T1.GRAD_VALUE, ";
        $query .= "   T1.GET_CREDIT, ";
        $query .= "   T1.ADD_CREDIT, ";
        $query .= "   T4.REMARK, ";
        $query .= "   T5.ANNUAL ";
        $query .= " FROM ";
        $query .= "   RECORD_DAT T1 ";
        $query .= "   LEFT OUTER JOIN V_CLASS_MST T2 ON  ";          //教科名称取得
        $query .= "   T2.YEAR = T1.YEAR AND  ";
        $query .= "   T2.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ";
        $query .= "   LEFT OUTER JOIN V_SUBCLASS_MST T3 ON  ";       //科目名称取得
        $query .= "   T3.YEAR = T1.YEAR AND  ";
        $query .= "   T3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "   LEFT OUTER JOIN STUDYCLASSREMARK_DAT T4 ON  ";   //学籍履修科目備考データ取得
        $query .= "   T4.YEAR = T1.YEAR AND ";
        $query .= "   T4.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "   T4.CLASSCD = T2.CLASSCD AND ";
        $query .= "   T4.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= "   LEFT OUTER JOIN ";  
        $query .= "     (SELECT DISTINCT YEAR, SCHREGNO, ANNUAL ";
        $query .= "        FROM SCHREG_REGD_DAT ";
        $query .= "       WHERE SCHREGNO = '" .$model->schregno ."'";
#        $query .= "                 YEAR = '" .$model->year ."' ";
        $query .= "     ) T5";
        $query .= " ON ";
        $query .= "   T5.YEAR = T1.YEAR AND ";
        $query .= "   T5.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        if ($model->annual["YEAR"] != "0" || $model->annual["ANNUAL"] != "0"){
            $query .= "   T1.YEAR = '" .$model->annual["YEAR"] ."' AND ";
            $query .= "   T5.ANNUAL = '" .$model->annual["ANNUAL"]."' AND";
        }
        $query .= "   T1.SCHREGNO = '" .$model->schregno ."' ";
        $query .= " ORDER BY ";
        $query .= "     T5.ANNUAL, ";
        $query .= "     T1.SUBCLASSCD ";

        //echo $query;
        return $query;
    }

}
?>
