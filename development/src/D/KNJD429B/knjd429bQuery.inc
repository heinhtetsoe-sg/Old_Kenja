<?php

require_once('for_php7.php');

class knjd429bQuery extends Query {

    //学期取得
    function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchoolKind() {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //実クラス取得
    function getGhr($model, $ghrCd = '') {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GHR_NAME AS LABEL, ";
        $query .= "     T1.GHR_CD AS VALUE, ";
        $query .= "     MAX(T4.SCHOOL_KIND) AS SCHOOL_KIND, ";
        $query .= "     CASE WHEN MIN(T4.SCHOOL_KIND) = MAX(T4.SCHOOL_KIND) THEN 1 END AS SCHOOL_KIND_FLG ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GHR_DAT T2 ";
        $query .= "          ON T2.YEAR     = T1.YEAR ";
        $query .= "         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T2.GHR_CD   = T1.GHR_CD ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T3 ";
        $query .= "          ON T3.YEAR     = T1.YEAR ";
        $query .= "         AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T3.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T4 ";
        $query .= "          ON T4.YEAR     = T3.YEAR ";
        $query .= "         AND T4.GRADE    = T3.GRADE ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ";
        $query .= "          ON L1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L2 ";
        $query .= "          ON L2.SCHREGNO = L1.SCHREGNO ";
        $query .= "         AND L2.YEAR     = T1.YEAR ";
        $query .= "         AND L2.BASE_SEQ = '004' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= " AND T1.SEMESTER = '". CTRL_SEMESTER ."' ";
        } else {
            $query .= " AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        if ($ghrCd) {
            $query .= " AND T1.GHR_CD = '". $ghrCd."' ";
        }
        //更新・参照可（制限付き）
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= " AND (T1.TR_CD1    = '". STAFFCD ."' OR ";
            $query .= "      T1.TR_CD2    = '". STAFFCD ."' OR ";
            $query .= "      T1.TR_CD3    = '". STAFFCD ."' OR ";
            $query .= "      T1.SUBTR_CD1 = '". STAFFCD ."' OR ";
            $query .= "      T1.SUBTR_CD2 = '". STAFFCD ."' OR ";
            $query .= "      T1.SUBTR_CD3 = '". STAFFCD ."') ";
        }
        $query .= "     AND CASE WHEN L2.BASE_REMARK1 IS NULL THEN L1.HANDICAP ELSE L2.BASE_REMARK1 END ";
        $query .= "         IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 = '1') ";
        $query .= " GROUP BY ";
        $query .= "     T1.GHR_CD, T1.GHR_NAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($year, $semester, $model)
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL, ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T3 ";
        $query .= "          ON T3.YEAR     = T1.YEAR ";
        $query .= "         AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T3.GRADE    = T1.GRADE ";
        $query .= "         AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ";
        $query .= "          ON L1.SCHREGNO = T3.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L2 ";
        $query .= "          ON L2.SCHREGNO = L1.SCHREGNO ";
        $query .= "         AND L2.YEAR     = T1.YEAR ";
        $query .= "         AND L2.BASE_SEQ = '004' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        $query .= "     AND T2.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= "     AND CASE WHEN L2.BASE_REMARK1 IS NULL THEN L1.HANDICAP ELSE L2.BASE_REMARK1 END ";
        $query .= "         IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 = '1') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象生徒 法廷クラス
    function getHreportConditionDat($year, $model, $seq)
    {
        $query  = " SELECT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORT_CONDITION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND GRADE = '".sprintf("%02d", "")."' ";
        $query .= "     AND COURSECD = '".sprintf("%01d", "")."' ";
        $query .= "     AND MAJORCD = '".sprintf("%03d", "")."' ";
        $query .= "     AND COURSECODE = '".sprintf("%04d", "")."' ";
        $query .= "     AND SEQ = '".$seq."' ";

        return $query;
    }

    //主に使用する様式
    function getPatternUseMainly() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= "     , NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A035' ";
        $query .= "     AND NAMECD2 IN ('101', '102', '103') ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //対象生徒 法廷クラス
    function getStudent1($model, $seme)
    {
        $query  = " SELECT ";
        $query .= "     T2.ATTENDNO || '-' || T2.SCHREGNO AS VALUE, ";
        $query .= "     T2.SCHREGNO AS SCHREGNO_SHOW, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW, ";
        $query .= "     T3.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR  = T2.YEAR";
        $query .= "                                  AND T3.GRADE = T2.GRADE";
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L2 ";
        $query .= "          ON L2.SCHREGNO = T2.SCHREGNO ";
        $query .= "         AND L2.YEAR     = T2.YEAR ";
        $query .= "         AND L2.BASE_SEQ = '004' ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".$seme."' ";
        $query .= "     AND T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND CASE WHEN L2.BASE_REMARK1 IS NULL THEN T1.HANDICAP ELSE L2.BASE_REMARK1 END ";
        $query .= "         IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T2.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 = '1') ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }
    
    //対象生徒 実クラス
    function getStudent2($model, $seme)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T4.GHR_NAME || T1.GHR_ATTENDNO || '番　' || T1.SCHREGNO || '　' || L1.NAME_SHOW AS LABEL, ";
        //$query .= "     '1-' || T1.GHR_CD || T1.GHR_ATTENDNO || '-' || T1.SCHREGNO AS VALUE ";
        $query .= "     T1.GHR_ATTENDNO || '-' || T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST L1 ";
        $query .= "          ON L1.SCHREGNO     = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "          ON T2.SCHREGNO     = T1.SCHREGNO ";
        $query .= "         AND T2.YEAR         = T1.YEAR ";
        $query .= "         AND T2.SEMESTER     = T1.SEMESTER ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "          ON T3.YEAR         = T2.YEAR ";
        $query .= "         AND T3.GRADE        = T2.GRADE ";
        $query .= "         AND T3.SCHOOL_KIND  = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "     INNER JOIN SCHREG_REGD_GHR_HDAT T4 ";
        $query .= "          ON T4.YEAR         = T1.YEAR ";
        $query .= "         AND T4.SEMESTER     = T1.SEMESTER ";
        $query .= "         AND T4.GHR_CD       = T1.GHR_CD ";
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L2 ";
        $query .= "          ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND L2.YEAR     = T1.YEAR ";
        $query .= "         AND L2.BASE_SEQ = '004' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$seme."' ";
        $query .= "     AND T1.GHR_CD   = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND CASE WHEN L2.BASE_REMARK1 IS NULL THEN L1.HANDICAP ELSE L2.BASE_REMARK1 END ";
        $query .= "         IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 = '1') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象外の生徒取得
    function getSchnoIdou($model,$semester)
    {
        $date = str_replace("/","-",$model->field["DATE"]);

        $query  = "";
        $query .= " SELECT T1.SCHREGNO, T1.ATTENDNO ";
        $query .= " FROM   SCHREG_REGD_DAT T1, V_SEMESTER_GRADE_MST T2 ";
        $query .= " WHERE  T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T1.SEMESTER = '".$semester."' AND ";
        $query .= "        T1.YEAR = T2.YEAR AND ";
        $query .= "        T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "        T2.GRADE = '".substr($grade_hr_class, 0, 2)."' AND ";
        $query .= "        T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ( ";
                           //転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
                           //転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                    ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE <  ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END) OR ";
        $query .= "                     (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE >  ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END)) ) OR ";
                           //留学(1)・休学(2)者
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                      S1.TRANSFERCD IN ('1','2') AND  ";
        $query .= "                      CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                           THEN T2.EDATE ELSE '".$date."' END  ";
        $query .= "                      BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ) ";
        $query .= " ORDER BY T1.ATTENDNO ";

        return $query;
    }

}
?>
