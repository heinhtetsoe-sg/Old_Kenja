<?php

require_once('for_php7.php');


class knjh561dQuery extends Query {

    //学校名取得
    function getZ010()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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
    function getProName($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     T1.PROFICIENCYCD || ':' || T1.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST T1 ";
        $query .= "     INNER JOIN PROFICIENCY_YMST T2 ON T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T2.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "         AND T2.PROFICIENCYCD = T1.PROFICIENCYCD ";
        $query .= "         AND T2.PROFICIENCYDIV = T1.PROFICIENCYDIV ";
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
            $query .= "     REGD.GRADE AS VALUE, ";
            $query .= "     REG_G.GRADE_NAME1 AS LABEL ";
        } else {
            $query .= "     REGD.HR_CLASS AS VALUE, ";
            $query .= "     REGD.HR_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGD ";
        $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REGD.YEAR = REG_G.YEAR ";
        $query .= "           AND REGD.GRADE = REG_G.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".$semester."' ";
        if ($div == "GRADE") {
        } else {
            $query .= "     AND REGD.GRADE = '".$model->field["GRADE"]."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   REGD.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR REGD.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR REGD.TR_CD3 = '".STAFFCD."') ";
        }
        $query .= "     AND EXISTS (SELECT 'X' ";
        $query .= "                 FROM PROFICIENCY_YMST PY ";
        $query .= "                 WHERE PY.YEAR = '".CTRL_YEAR."' ";
        $query .= "                   AND PY.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "                   AND PY.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "                   AND PY.PROFICIENCYCD = '".$model->field["PROFICIENCYCD"]."' ";
        $query .= "                   AND PY.GRADE = REGD.GRADE ";
        $query .= "     ) ";
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
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".$seme."' ";
        $query .= "     AND T2.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND T2.HR_CLASS = '".$model->field["HR_CLASS"]."' ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
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
