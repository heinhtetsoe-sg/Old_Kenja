<?php

require_once('for_php7.php');

class knjh569Query extends Query {
    //学校取得
    function getZ010($model) {
        $query  = " SELECT ";
        $query .= "   NAME1 ";
        $query .= " FROM ";
        $query .= "   NAME_MST ";
        $query .= " WHERE ";
        $query .= "   NAMECD1 = 'Z010' ";
        $query .= "   AND NAMECD2 = '00' ";

        return $query;
    }

    //学期取得
    function getSemester($semester="") {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        if ($semester) {
            $query .= "     AND SEMESTER = '".$semester."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別取得
    function getDataDiv() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'H508' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト名称取得
    function getProName($model, $proficiencycd="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.PROFICIENCYNAME1, ";
        $query .= "     T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     T1.PROFICIENCYCD || ':' || T1.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST T1 ";
        $query .= "     INNER JOIN PROFICIENCY_YMST T2 ";
        $query .= "          ON T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND T2.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "         AND T2.PROFICIENCYDIV   = T1.PROFICIENCYDIV ";
        $query .= "         AND T2.PROFICIENCYCD    = T1.PROFICIENCYCD ";
        $query .= " WHERE ";
        $query .= "     T1.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        if ($proficiencycd) {
            $query .= "     AND T1.PROFICIENCYCD = '".$proficiencycd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得（権限チェック）
    function getGrade($model, $semester, $grade="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE_NAME1, ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "          ON T2.YEAR  = T1.YEAR ";
        $query .= "         AND T2.GRADE = T1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "         AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     INNER JOIN PROFICIENCY_YMST T3 ";
        $query .= "          ON T3.YEAR     = T1.YEAR ";
        $query .= "         AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T3.GRADE    = T1.GRADE ";
        $query .= "         AND T3.PROFICIENCYDIV   = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "         AND T3.PROFICIENCYCD    = '".$model->field["PROFICIENCYCD"]."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$semester."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND '".STAFFCD."' IN (T1.TR_CD1, T1.TR_CD2, T1.TR_CD3, T1.SUBTR_CD1, T1.SUBTR_CD2, T1.SUBTR_CD3) ";
        }
        if ($grade) {
            $query .= "     AND T1.GRADE = '".$grade."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     REGD.GRADE || REGD.HR_CLASS AS VALUE, ";
        $query .= "     REGD.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT REG_G ";
                $query .= "          ON REGD.YEAR   = REG_G.YEAR ";
                $query .= "         AND REGD.GRADE  = REG_G.GRADE ";
                $query .= "         AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT REG_G ";
            $query .= "          ON REGD.YEAR   = REG_G.YEAR ";
            $query .= "         AND REGD.GRADE  = REG_G.GRADE ";
            $query .= "         AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     INNER JOIN PROFICIENCY_YMST T3 ";
        $query .= "          ON T3.YEAR     = REGD.YEAR ";
        $query .= "         AND T3.SEMESTER = REGD.SEMESTER ";
        $query .= "         AND T3.GRADE    = REGD.GRADE ";
        $query .= "         AND T3.PROFICIENCYDIV   = '".$model->field["PROFICIENCYDIV"]."'";
        $query .= "         AND T3.PROFICIENCYCD    = '".$model->field["PROFICIENCYCD"]."' ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     REGD.SEMESTER   = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     REGD.GRADE      = '".$model->field["GRADE"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND '".STAFFCD."' IN (REGD.TR_CD1, REGD.TR_CD2, REGD.TR_CD3, REGD.SUBTR_CD1, REGD.SUBTR_CD2, REGD.SUBTR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース取得（権限チェック）
    function getCourse($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE || '　' || S1.COURSENAME || S2.MAJORNAME || S3.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN COURSE_MST S1 ON T1.COURSECD = S1.COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST S2 ON T1.COURSECD = S2.COURSECD AND T1.MAJORCD = S2.MAJORCD ";
        $query .= "     LEFT JOIN COURSECODE_MST S3 ON T1.COURSECODE = S3.COURSECODE, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT REG_G ";
                $query .= "          ON T2.YEAR     = REG_G.YEAR ";
                $query .= "         AND T2.GRADE    = REG_G.GRADE ";
                $query .= "         AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT REG_G ";
            $query .= "          ON T2.YEAR     = REG_G.YEAR ";
            $query .= "         AND T2.GRADE    = REG_G.GRADE ";
            $query .= "         AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     INNER JOIN PROFICIENCY_YMST T3 ";
        $query .= "          ON T3.YEAR     = T2.YEAR ";
        $query .= "         AND T3.SEMESTER = T2.SEMESTER ";
        $query .= "         AND T3.GRADE    = T2.GRADE ";
        $query .= "         AND T3.PROFICIENCYDIV   = '".$model->field["PROFICIENCYDIV"]."'";
        $query .= "         AND T3.PROFICIENCYCD    = '".$model->field["PROFICIENCYCD"]."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T1.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.GRADE    = '".$model->field["GRADE"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND '".STAFFCD."' IN (T2.TR_CD1, T2.TR_CD2, T2.TR_CD3, T2.SUBTR_CD1, T2.SUBTR_CD2, T2.SUBTR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //実力科目一覧
    function getProfSubclassMst($model, $proficiency_subclass_cd="") {
        $query  = " WITH REGD_T AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         L1.GROUP_CD ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         LEFT JOIN COURSE_GROUP_CD_DAT L1 ";
        $query .= "              ON L1.YEAR     = T1.YEAR ";
        $query .= "             AND L1.GRADE    = T1.GRADE ";
        $query .= "             AND L1.COURSECD || L1.MAJORCD || L1.COURSECODE = T1.COURSECD || T1.MAJORCD || T1.COURSECODE ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= " ), GROUP_T AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.PROFICIENCY_SUBCLASS_CD ";
        $query .= "     FROM ";
        $query .= "         PROFICIENCY_PERFECT_COURSE_DAT T1 ";
        $query .= "         LEFT JOIN REGD_T L1 ON L1.YEAR = T1.YEAR ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T1.PROFICIENCYDIV   = '".$model->field["PROFICIENCYDIV"]."' AND ";
        $query .= "         T1.PROFICIENCYCD    = '".$model->field["PROFICIENCYCD"]."' AND ";
        $query .= "         T1.GRADE    = CASE WHEN DIV = '01' ";
        $query .= "                            THEN '00' ";
        $query .= "                            ELSE L1.GRADE ";
        $query .= "                        END AND ";
        $query .= "         T1.COURSECD || T1.MAJORCD || T1.COURSECODE = CASE WHEN T1.DIV IN ('01','02') ";
        $query .= "                                                           THEN '00000000' ";
        $query .= "                                                           ELSE CASE WHEN T1.DIV = '04' ";
        $query .= "                                                                     THEN '0' || L1.GROUP_CD || '0000' ";
        $query .= "                                                                     ELSE L1.COURSECD || L1.MAJORCD || L1.COURSECODE ";
        $query .= "                                                                 END ";
        $query .= "                                                       END ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SUBCLASS_ABBV, ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD AS VALUE, ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD || ':' || T1.SUBCLASS_ABBV AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD IN (SELECT I1.PROFICIENCY_SUBCLASS_CD FROM GROUP_T I1) ";
        if ($proficiency_subclass_cd) {
            $query .= "     AND T1.PROFICIENCY_SUBCLASS_CD = '".$proficiency_subclass_cd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE   = '".$model->field["GRADE"]."' ";

        return $query;
    }

    //土佐女子のみ2019年1学期に登録されているテストをチェックするためのSQL
    function getProficiencyMstDat($model, $srchyear) {
       $query  = " SELECT ";
       $query .= "   PROFICIENCYDIV, ";
       $query .= "   PROFICIENCYCD ";
       $query .= " FROM ";
       $query .= "   PROFICIENCY_YMST  ";
       $query .= " WHERE ";
       $query .= "   YEAR = '2019' AND SEMESTER = '1' AND GRADE = '".$model->field["GRADE"]."' ";

       return $query;
    }

    //CSVデータ出力
    function selectMainQuery($model) {
        $query  = " SELECT ";
        $query .= "     P1.PROFICIENCY_SUBCLASS_CD || T1.GRADE ";
        if ($model->field["SELECT_DIV"] == '2') {
            $query .= "     || T1.HR_CLASS ";
        } else if ($model->field["SELECT_DIV"] == '3') {
            $query .= "     || T1.COURSECD || T1.MAJORCD || T1.COURSECODE ";
        }
        $query .= "     AS KEY, ";
        $query .= "     P1.PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     P2.SUBCLASS_NAME, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     BASE.NAME, ";
        $query .= "     P1.RANK, ";
        $query .= "     P1.SCORE, ";
        $query .= "     P1.DEVIATION ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT HDAT ";
        $query .= "          ON T1.YEAR         = HDAT.YEAR ";
        $query .= "         AND T1.SEMESTER     = HDAT.SEMESTER ";
        $query .= "         AND T1.GRADE        = HDAT.GRADE ";
        $query .= "         AND T1.HR_CLASS     = HDAT.HR_CLASS ";
        $query .= "     INNER JOIN PROFICIENCY_RANK_DAT P1 ";
        $query .= "          ON T1.YEAR         = P1.YEAR ";
        $query .= "         AND T1.SEMESTER     = P1.SEMESTER ";
        $query .= "         AND P1.PROFICIENCYDIV   = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "         AND P1.PROFICIENCYCD    = '".$model->field["PROFICIENCYCD"]."' ";
        $query .= "         AND T1.SCHREGNO = P1.SCHREGNO ";
        if ($model->field["PROFICIENCY_SUBCLASS_CD"]) {
            $query .= "         AND P1.PROFICIENCY_SUBCLASS_CD = '".$model->field["PROFICIENCY_SUBCLASS_CD"]."' ";
        }
        $query .= "         AND P1.PROFICIENCY_SUBCLASS_CD NOT IN ('333333', '555555', '999999') ";
        $query .= "         AND P1.RANK_DATA_DIV    = '".$model->field["RANK_DATA_DIV"]."' ";
        $query .= "         AND P1.RANK_DIV         = '".sprintf("%02d", $model->field["SELECT_DIV"])."' ";
        $query .= "     LEFT JOIN PROFICIENCY_SUBCLASS_MST P2 ON P1.PROFICIENCY_SUBCLASS_CD = P2.PROFICIENCY_SUBCLASS_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE    = '".$model->field["GRADE"]."' ";
        if ($model->field["SELECT_DIV"] == '2') {
            $query .= "     AND T1.GRADE || T1.HR_CLASS IN ('".implode(explode(',', $model->selectdata),"','")."') ";
        } else if ($model->field["SELECT_DIV"] == '3') {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE IN ('".implode(explode(',', $model->selectdata),"','")."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     P1.PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     T1.GRADE, ";
        if ($model->field["SELECT_DIV"] == '2') {
            $query .= "     T1.HR_CLASS, ";
        } else if ($model->field["SELECT_DIV"] == '3') {
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.COURSECODE, ";
        }
        $query .= "     P1.SCORE DESC, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }
}
?>
