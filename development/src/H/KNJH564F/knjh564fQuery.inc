<?php

require_once('for_php7.php');


class knjh564fQuery extends Query {

    //学期取得
    function getNameMst($namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= "     AND NAMECD2 = '".$namecd2."' ";

        return $query;
    }

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
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別取得
    function getDataDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'H508' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト名称取得
    function getProName($semester, $model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     T1.PROFICIENCYCD || ':' || T1.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST T1 ";
        $query .= "     INNER JOIN PROFICIENCY_YMST T2 ON T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T2.SEMESTER = '".$semester."' ";
        $query .= "         AND T2.PROFICIENCYDIV = T1.PROFICIENCYDIV ";
        $query .= "         AND T2.PROFICIENCYCD = T1.PROFICIENCYCD ";
        $query .= " WHERE ";
        $query .= "     T1.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getGradeHrClass($semester, $model, $div = "")
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        if ($div == "GRADE") {
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     T1.GRADE AS LABEL ";
        } else {
            $query .= "     T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN (SELECT DISTINCT I1.GRADE, I1.HR_CLASS FROM SCHREG_REGD_DAT I1 ";
        $query .= "                 INNER JOIN SCHREG_BASE_MST I2 ON I2.SCHREGNO = I1.SCHREGNO ";
        $query .= "                 INNER JOIN (SELECT DISTINCT GRADE FROM PROFICIENCY_YMST I4 ";
        $query .= "                            WHERE I4.YEAR = '".CTRL_YEAR."' AND I4.SEMESTER = '".$semester."' ";
        $query .= "                              AND I4.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "                              AND I4.PROFICIENCYCD = '".$model->field["PROFICIENCYCD"]."' ";
        $query .= "                            ) I4 ON I4.GRADE = I1.GRADE ";
        $query .= "                 INNER JOIN (SELECT DISTINCT SCHREGNO FROM PROFICIENCY_DAT I3 ";
        $query .= "                            WHERE I3.YEAR = '".CTRL_YEAR."' AND I3.SEMESTER = '".$semester."' ";
        $query .= "                              AND I3.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "                              AND I3.PROFICIENCYCD = '".$model->field["PROFICIENCYCD"]."' ";
        $query .= "                              AND I3.SCORE IS NOT NULL ";
        $query .= "                            ) I3 ON I3.SCHREGNO = I1.SCHREGNO ";
        $query .= "                 WHERE I1.YEAR = '".CTRL_YEAR."' AND I1.SEMESTER = '".$semester."' ";
        $query .= "                ) I1 ON I1.GRADE = T1.GRADE AND I1.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        if ($div == "GRADE") {
            $query .= "     AND T1.GRADE < '11' ";
        } else {
            $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        }
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND ((TR_CD1     = '".STAFFCD."' OR ";
            $query .= "           TR_CD2     = '".STAFFCD."' OR ";
            $query .= "           TR_CD3     = '".STAFFCD."' OR ";
            $query .= "           SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "           SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "           SUBTR_CD3  = '".STAFFCD."') ";
            $query .= "       OR (T1.GRADE IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            $query .= "                  STAFF_DETAIL_MST ST ";
            $query .= "              WHERE ";
            $query .= "                  T1.YEAR = ST.YEAR ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '0200')) ";
            $query .= "         ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象生徒
    function getStudent($model, $seme)
    {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN (SELECT DISTINCT SCHREGNO FROM PROFICIENCY_DAT I2 ";
        $query .= "                 WHERE I2.YEAR = '".CTRL_YEAR."' AND I2.SEMESTER = '".$seme."' ";
        $query .= "                   AND I2.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "                   AND I2.PROFICIENCYCD = '".$model->field["PROFICIENCYCD"]."' ";
        $query .= "                   AND I2.SCORE IS NOT NULL ";
        $query .= "                ) I2 ON I2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".$seme."' ";
        $query .= "     AND T2.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND T2.HR_CLASS = '".$model->field["HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //対象外の生徒取得
    function getSchnoIdou($model,$semester)
    {
        $date = str_replace("/", "-", CTRL_DATE);

        $query  = "";
        $query .= " SELECT T1.SCHREGNO, T1.ATTENDNO ";
        $query .= " FROM   SCHREG_REGD_DAT T1, SEMESTER_MST T2 ";
        $query .= " WHERE  T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T1.SEMESTER = '".$semester."' AND ";
        $query .= "        T1.YEAR = T2.YEAR AND ";
        $query .= "        T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "        T1.GRADE = '".$model->field["GRADE"]."' AND ";
        $query .= "        T1.HR_CLASS = '".$model->field["HR_CLASS"]."' AND ( ";
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
