<?php

require_once('for_php7.php');

class knjd667vQuery extends Query {

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchoolKind() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHOOL_KIND AS VALUE, ";
        $query .= "     A023.NAME1, ";
        $query .= "     A023.ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query.= "      LEFT JOIN NAME_MST A023 ";
        $query .= "       ON A023.NAMECD1 = 'A023' ";
        $query .= "       AND A023.NAME1 = T1.SCHOOL_KIND";
        $query .= " WHERE ";
        $query .= "     T1.YEAR    = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "   A023.NAME1 ASC ";

        return $query;
    }

    //年組取得（権限チェック）
    function getGradeHrClass($semester, $model, $div = "")  {
        if ($semester == "9") {
            $semester = CTRL_SEMESTER;
        }

        //参照・更新可
        $query  = " SELECT DISTINCT ";
        if ($div == "GRADE") {
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     T2.GRADE_NAME1 AS LABEL ";
        } else {
            $query .= "     T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                                  AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        if (is_null($model->field["SCHOOLKIND"]) || $model->field["SCHOOLKIND"] == "") {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        } else {
            $query .= " AND T2.SCHOOL_KIND = '".$model->field["SCHOOLKIND"]."' ";
        }
        if ($div == "GRADE") {
        } else {
            $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        }
        $query .= "     AND EXISTS ( ";
        $query .= "                 SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT T3 ";
        $query .= "                 WHERE ";
        $query .= "                     T3.YEAR = T1.YEAR ";
        $query .= "                     AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "                     AND T3.GRADE = T1.GRADE ";
        $query .= "                     AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= "                 ) ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト取得
    function getTest($model) {
        $testCdIn = $sep = "";
        foreach ($model->testCd as $key => $testCd) {
            $testCdIn .= $sep.$testCd;
            $sep = "', '";
        }

        $query  = " SELECT ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= " INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ";
        $query .= "    AND T11.SEMESTER = T1.SEMESTER ";
        $query .= "    AND T11.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "    AND T11.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "    AND T11.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "    AND T11.CLASSCD      = '00' ";
        $query .= "    AND T11.CURRICULUM_CD  = '00' ";
        $query .= "    AND T11.SUBCLASSCD  = '000000' ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T11.SCHOOL_KIND = '".$model->field["SCHOOLKIND"]."' ";
        //$query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV IN ('{$testCdIn}') ";
        $query .= "     AND T1.SCORE_DIV IN ('01') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース取得
    function getCourse($model) {
        $query  = " SELECT ";
        $query .= "   SRD.COURSECODE, ";
        $query .= "   CM.COURSECODENAME ";
        $query .= " FROM ";
        $query .= " SCHREG_REGD_DAT SRD ";
        $query .= " LEFT JOIN COURSECODE_MST CM ";
        $query .= "   ON CM.COURSECODE = SRD.COURSECODE ";
        $query .= " WHERE ";
        $query .= "   SRD.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "   AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= " GROUP BY ";
        $query .= "   SRD.COURSECODE, ";
        $query .= "   CM.COURSECODENAME ";
        $query .= " ORDER BY ";
        $query .= "   SRD.COURSECODE ";

        return $query;
    }

    //対象生徒
    function getStudent($model, $seme) {
        if ($seme == "9") {
            $seme = CTRL_SEMESTER;
        }

        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "         T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".$seme."' ";
        $query .= "     AND T2.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "     AND T2.HR_CLASS = '".$model->field["HR_CLASS"]."' ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //対象外の生徒取得
    function getSchnoIdou($model,$semester) {
        $date = str_replace("/", "-", CTRL_DATE);
        if ($semester == "9") {
            $semester = CTRL_SEMESTER;
        }

        $query  = "";
        $query .= " SELECT T1.SCHREGNO, T1.ATTENDNO ";
        $query .= " FROM   SCHREG_REGD_DAT T1, SEMESTER_MST T2 ";
        $query .= " WHERE  T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "        T1.SEMESTER = '".$semester."' AND ";
        $query .= "        T1.YEAR     = T2.YEAR AND ";
        $query .= "        T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "        T1.GRADE    = '".$model->field["GRADE"]."' AND ";
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

    //学年カラー指定取得
    function getColor($model) {
        $query  = " SELECT  ";
        $query .= "   NM.NAMESPARE1 AS VALUE, ";
        $query .= "   NM.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "   NAME_YDAT NY ";
        $query .= "   LEFT JOIN NAME_MST NM ";
        $query .= "     ON  NY.NAMECD1 = NM.NAMECD1 ";
        $query .= "     AND NY.NAMECD2 = NM.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "   NY.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND NY.NAMECD1 = 'D074' ";
        $query .= " ORDER BY ";
        $query .= "   NY.NAMECD2 ASC ";

        return $query;
    }
}
?>
