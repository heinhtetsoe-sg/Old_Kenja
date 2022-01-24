<?php

require_once('for_php7.php');

class knjxexp_kounoukinQuery extends Query
{

    //左リスト
    public function GetStudents($model)
    {
        $tableName = "SCHREG_BASE_MST";
        if ($model->search["SEARCH_DIV"] == "1") {
            $tableName = "FRESHMAN_DAT";
        }

        $where = knjxexp_kounoukinQuery::getStudentSearch($model);
        $and = $where == "" ? "" : "AND ";

        $query  = " WITH SCH_R AS ( ";
        if ($model->search["HR_CLASS_TYPE"] == '2' && $model->Properties["useFi_Hrclass"] != "1") {
            $query .= "     SELECT DISTINCT ";
            $query .= "         T1.YEAR, ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         '' AS GRADE, ";
            $query .= "         T1.GHR_CD AS HR_CLASS, ";
            $query .= "         T1.GHR_ATTENDNO AS ATTENDNO, ";
            $query .= "         HDAT.GHR_NAME AS HR_NAME ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_GHR_DAT T1 ";
            $query .= "         INNER JOIN SCHREG_REGD_GHR_HDAT HDAT ";
            $query .= "              ON T1.YEAR     = HDAT.YEAR ";
            $query .= "             AND T1.SEMESTER = HDAT.SEMESTER ";
            $query .= "             AND T1.GHR_CD   = HDAT.GHR_CD ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            //参照・更新可（制限付き）
            if ($model->usr_auth == DEF_REFER_RESTRICT || $model->usr_auth == DEF_UPDATE_RESTRICT) {
                $query .= " AND '".STAFFCD."' IN (HDAT.TR_CD1, HDAT.TR_CD2, HDAT.TR_CD3, HDAT.SUBTR_CD1, HDAT.SUBTR_CD2, HDAT.SUBTR_CD3)  ";
            }
        } else {
            $query .= "     SELECT DISTINCT ";
            $query .= "         T1.YEAR, ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.GRADE, ";
            $query .= "         T1.HR_CLASS, ";
            $query .= "         T1.ATTENDNO, ";
            $query .= "         HDAT.HR_NAME ";
            $query .= "     FROM ";
            if ($model->search["HR_CLASS_TYPE"] == '2') {
                $query .= "         SCHREG_REGD_FI_DAT T1 ";
                $query .= "         LEFT JOIN SCHREG_REGD_FI_HDAT HDAT ";
            } else {
                $query .= "         SCHREG_REGD_DAT T1 ";
                $query .= "         LEFT JOIN SCHREG_REGD_HDAT HDAT ";
            }
            $query .= "              ON T1.YEAR     = HDAT.YEAR ";
            $query .= "             AND T1.SEMESTER = HDAT.SEMESTER ";
            $query .= "             AND T1.GRADE    = HDAT.GRADE ";
            $query .= "             AND T1.HR_CLASS = HDAT.HR_CLASS ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            if ($model->usr_auth == DEF_REFER_RESTRICT || $model->usr_auth == DEF_UPDATE_RESTRICT) {
                $query .= "     AND (   '".STAFFCD."' IN (HDAT.TR_CD1, HDAT.TR_CD2, HDAT.TR_CD3, HDAT.SUBTR_CD1, HDAT.SUBTR_CD2, HDAT.SUBTR_CD3) ";
                $query .= "          OR (T1.GRADE IN (  SELECT ";
                $query .= "                                 FIELD2 ";
                $query .= "                             FROM ";
                $query .= "                                 STAFF_DETAIL_MST ST ";
                $query .= "                             WHERE ";
                $query .= "                                 T1.YEAR     = ST.YEAR ";
                $query .= "                             AND ST.STAFFCD  = '".STAFFCD."' ";
                $query .= "                             AND ST.STAFF_SEQ IN ('005', '006', '007') ";
                $query .= "                             AND ST.FIELD1   = '0200')) ";
                $query .= "         ) ";
            }
        }
        if ($model->dispData["repayDisp"] == "1") {
            $query .= " ), REPAY_T AS ( ";
            $query .= " SELECT ";
            $query .= "     REPAY_D.SCHREGNO, ";
            $query .= "     SUM(REPAY_D.REPAY_MONEY) AS REPAY_MONEY ";
            $query .= " FROM ";
            $query .= "     COLLECT_REPAY_SLIP_DAT REPAY_D ";
            $query .= " WHERE ";
            $query .= "     VALUE(REPAY_D.CANCEL_FLG, '0') <> '1' ";
            $query .= " GROUP BY ";
            $query .= "     REPAY_D.SCHREGNO ";
        }
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "   SCH_R.GRADE || SCH_R.HR_CLASS || VALUE(SCH_R.ATTENDNO,'') AS HR_CLASS_ATTEND_ORDER, ";
        $query .= "   SCH_R.GRADE, ";
        $query .= "   SCH_R.HR_CLASS, ";
        $query .= "   SCH_R.ATTENDNO, ";
        $query .= "   MAIN.SCHREGNO, ";
        $query .= "   MAIN.NAME, ";
        $query .= "   MAIN.SEX, ";
        if ($model->dispData["repayDisp"] == "1") {
            $query .= "   REPAY_T.REPAY_MONEY, ";
        }
        if ($model->dispData["repayDisp"] == "1" && $tableName == "SCHREG_BASE_MST") {
            $query .= "   MAIN.GRD_DATE, ";
        }
        if ($model->dispData["schkind"] == "1") {
            $query .= "   L1.SCHOOL_KIND, ";
        }
        $query .= "   VALUE(SCH_R.HR_NAME,'') || ' - ' || VALUE(SCH_R.ATTENDNO,'') AS HR_ATTEND ";
        $query .= " FROM ";
        $query .= "   {$tableName} MAIN ";
        $query .= "   LEFT JOIN SCH_R ON MAIN.SCHREGNO = SCH_R.SCHREGNO ";
        if ($model->search["SEARCH_DIV"] == "1") {
            $query .= "  LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR  = MAIN.ENTERYEAR ";
            if ($model->isFreshmanGrade == "1") {
                $query .= "                           AND L1.GRADE = MAIN.GRADE ";
            }
        } else {
            $query .= "  LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR  = SCH_R.YEAR ";
            $query .= "                               AND L1.GRADE = SCH_R.GRADE ";
        }
        if ($model->dispData["repayDisp"] == "1") {
            $query .= "  LEFT JOIN REPAY_T ON MAIN.SCHREGNO = REPAY_T.SCHREGNO ";
        }
        $query .= " WHERE ".$where;
        if (!($model->search["HR_CLASS_TYPE"] == '2' && $model->Properties["useFi_Hrclass"] != "1")) {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "  AND L1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "  AND L1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }

        $query .= " ORDER BY ";
        if ($model->search["HR_CLASS_HYOUJI_FLG"] == "1" || $model->dispData["sort"] == "1") {
            $query .= "   HR_CLASS_ATTEND_ORDER ";
        } else {
            $query .= "   MAIN.SCHREGNO ";
        }

        return $query;
    }

    public function getStudentSearch($model)
    {
        $and = "";
        $where = "";

        if ($model->search["SEARCH_DIV"] == "1") {
            $where .= "   ".$and."MAIN.ENTERYEAR = '".(CTRL_YEAR + 1)."' ";
            $and = "AND ";
        } elseif ($model->dispData["select_grd"] != "1") {
            $where .= "   ".$and."VALUE(MAIN.GRD_DIV, '0') NOT IN ('1', '2', '3', '9') ";
            $and = "AND ";
        }

        if ($model->search["SEARCH_TENHEN"] == "1") {
            $where .= "   ".$and."MAIN.ENT_DIV IN ('4', '5') ";
            $and = "AND ";
        }

        if ($model->search["GRD_CHECK"] == "1") {
            $where .= "   ".$and."MAIN.GRD_DIV IN ('2', '3', '6') ";
            $and = "AND ";
            $where .= "   ".$and."SCH_R.YEAR = '".CTRL_YEAR."' ";
            $and = "AND ";
        }

        //※保護者連絡履歴管理(KNJP850)用条件
        //メモ保有者
        if ($model->search["CONTACT_EXISTS_CHK"] == "1") {
            $where .= "   ".$and." MAIN.SCHREGNO IN ( ";
            $where .= "             SELECT DISTINCT ";
            $where .= "                 CNTCT.SCHREGNO ";
            $where .= "             FROM ";
            $where .= "                 COLLECT_CONTACT_HISTORY_DAT CNTCT ";
            $where .= "           ) ";
            $and = "AND ";
            $where .= "   ".$and."SCH_R.YEAR = '".CTRL_YEAR."' ";
            $and = "AND ";
        }
        //対応完了除く
        if ($model->search["EXCEPT_CONTACT_END_CHK"] == "1") {
            $where .= "    ".$and." NOT EXISTS ( ";
            $where .= "                 SELECT 'X' FROM ";
            $where .= "                     COLLECT_CONTACT_HISTORY_DAT CNTCT ";
            $where .= "                 WHERE ";
            $where .= "                     CNTCT.SCHREGNO =  MAIN.SCHREGNO ";
            $where .= "                 GROUP BY ";
            $where .= "                     CNTCT.SCHREGNO ";
            $where .= "                 HAVING COUNT(*) = COUNT(CASE WHEN CNTCT.CONTACT_EDATE IS NOT NULL THEN 1 ELSE NULL END) ";
            $where .= "             ) ";
            $and = "AND ";
            $where .= "   ".$and."SCH_R.YEAR = '".CTRL_YEAR."' ";
            $and = "AND ";
        }

        //入学年度
        if ($model->search["ENT_YEAR"]) {
            if ($model->search["SEARCH_DIV"] == "1") {
                $where .= "   ".$and."MAIN.ENTERYEAR = '".$model->search["ENT_YEAR"]."' ";
                $and = "AND ";
            } else {
                $where .= "   ".$and."FISCALYEAR(ENT_DATE) = '".$model->search["ENT_YEAR"]."' ";
                $and = "AND ";
            }
        }

        //卒業予定年度
        if ($model->search["GRD_YEAR"]) {
            if ($model->search["SEARCH_DIV"] == "1") {
                $where .= "   ".$and."(CAST(MAIN.ENTERYEAR AS SMALLINT) + 3) = ".$model->search["GRD_YEAR"]." ";
                $and = "AND ";
            } else {
                $where .= "   ".$and."FISCALYEAR(GRD_DATE) = '".$model->search["GRD_YEAR"]."' ";
                $and = "AND ";
            }
        }

        //課程・学科
        if ($model->search["COURSE_MAJOR"]) {
            $where .= "   ".$and."SCH_R.COURSECD || '-' || SCH_R.MAJORCD = '".$model->search["COURSE_MAJOR"]."' ";
            $and = "AND ";
        }

        //コース
        if ($model->search["COURSECODE"]) {
            $where .= "   ".$and."SCH_R.COURSECODE = '".$model->search["COURSECODE"]."' ";
            $and = "AND ";
        }

        //校種
        if ($model->search["SCHOOL_KIND"]) {
            $where .= "   ".$and."L1.SCHOOL_KIND = '".$model->search["SCHOOL_KIND"]."' ";
            $and = "AND ";
        }

        //学年
        if ($model->search["GRADE"]) {
            $where .= "   ".$and."SCH_R.GRADE = '".$model->search["GRADE"]."' ";
            $and = "AND ";
        }

        //年組
        if ($model->search["HR_CLASS"]) {
            if ($model->search["HR_CLASS_TYPE"] == '1' && $model->search["GAKUNEN_KONGOU"] == "1") {
                $where .= "   ".$and."L1.SCHOOL_KIND || '-' || SCH_R.HR_CLASS = '".$model->search["HR_CLASS"]."' ";
            } elseif ($model->search["HR_CLASS_TYPE"] == '2' && $model->Properties["useFi_Hrclass"] != "1") {
                $where .= "   ".$and."SCH_R.HR_CLASS = '".$model->search["HR_CLASS"]."' ";
            } else {
                $where .= "   ".$and."SCH_R.GRADE || SCH_R.HR_CLASS = '".$model->search["HR_CLASS"]."' ";
            }
            $and = "AND ";
        }

        //学籍番号
        if ($model->search["SCHREGNO"]) {
            $where .= "   ".$and."MAIN.SCHREGNO = '".$model->search["SCHREGNO"]."' ";
            $and = "AND ";
        }

        //氏名
        if ($model->search["NAME"]) {
            $where .= "   ".$and."MAIN.NAME LIKE '%" .str_replace("'", "''", $model->search["NAME"]). "%' ";
            $and = "AND ";
        }

        //氏名かな
        if ($model->search["NAME_KANA"]) {
            $where .= "   ".$and."MAIN.NAME_KANA LIKE '%" .str_replace("'", "''", $model->search["NAME_KANA"]). "%' ";
            $and = "AND ";
        }

        return $where;
    }

    //入学年度
    public function getEntYear()
    {
        $query  = " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //名称マスタ
    public function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //有無
    public function getUmu()
    {
        $query  = " WITH PAID_UMU (LABEL, VALUE) AS ( ";
        $query .= "     VALUES('有', '1') ";
        $query .= "     UNION ";
        $query .= "     VALUES('無', '2') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PAID_UMU ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //卒業予定年度
    public function getGrdYear()
    {
        $query  = " WITH MIRAI_3YEAR (LABEL, VALUE) AS ( ";
        $query .= "     VALUES ";
        $query .= "         ('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."'), ";
        $query .= "         ('".(CTRL_YEAR + 2)."', '".(CTRL_YEAR + 2)."'), ";
        $query .= "         ('".(CTRL_YEAR + 3)."', '".(CTRL_YEAR + 3)."') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     MIRAI_3YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //課程・学科
    public function getCourseMajor()
    {
        $query  = " SELECT ";
        $query .= "     REGD.COURSECD || '-' || REGD.MAJORCD AS VALUE, ";
        $query .= "     CCD.COURSENAME || ' ' || MAJOR.MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN COURSE_MST CCD ON REGD.COURSECD = CCD.COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST MAJOR ON REGD.COURSECD = MAJOR.COURSECD ";
        $query .= "          AND REGD.MAJORCD = MAJOR.MAJORCD ";
        $query .= " GROUP BY ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     CCD.COURSENAME, ";
        $query .= "     MAJOR.MAJORNAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース
    public function getCourseCode()
    {
        $query  = " SELECT ";
        $query .= "     REGD.COURSECODE AS VALUE, ";
        $query .= "     CCODE.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN COURSECODE_MST CCODE ON REGD.COURSECODE = CCODE.COURSECODE ";
        $query .= " GROUP BY ";
        $query .= "     REGD.COURSECODE, ";
        $query .= "     CCODE.COURSECODENAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    public function getSchkind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //学年
    public function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "  AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組
    public function getHrClass($model)
    {
        if ($model->search["HR_CLASS_TYPE"] == '1' && $model->search["GAKUNEN_KONGOU"] == "1") {
            $query  = " SELECT ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS || ':' || T1.HR_CLASS_NAME1 AS LABEL ";
            $query .= " FROM ";
            $query .= "     V_STAFF_HR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            //参照・更新可（制限付き）
            if ($model->usr_auth == DEF_REFER_RESTRICT || $model->usr_auth == DEF_UPDATE_RESTRICT) {
                $query .= "     AND T1.STAFFCD = '".STAFFCD."' ";
            }
            $query .= "     AND T1.HR_CLASS_NAME1 IN (  SELECT ";
            $query .= "                                    MAX(M1.HR_CLASS_NAME1) AS MAX_NAME  ";
            $query .= "                                 FROM ";
            $query .= "                                     V_STAFF_HR_DAT M1 ";
            $query .= "                                 WHERE ";
            $query .= "                                     T1.YEAR         = M1.YEAR AND ";
            $query .= "                                     T1.SEMESTER     = M1.SEMESTER AND ";
            $query .= "                                     T1.HR_CLASS     = M1.HR_CLASS AND ";
            $query .= "                                     T1.SCHOOL_KIND  = M1.SCHOOL_KIND AND ";
            $query .= "                                     T1.STAFFCD      = M1.STAFFCD ";
            $query .= "                                 ) ";
            $query .= " GROUP BY ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.HR_CLASS_NAME1, ";
            $query .= "     T1.SCHOOL_KIND ";
            $query .= " ORDER BY ";
            $query .= "     T1.SCHOOL_KIND DESC, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     LABEL ";
        } elseif ($model->search["HR_CLASS_TYPE"] == '2' && $model->Properties["useFi_Hrclass"] != "1") {
            $query  = " SELECT ";
            $query .= "     T1.GHR_CD AS VALUE, ";
            $query .= "     T1.GHR_NAME LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GHR_HDAT T1 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            //参照・更新可（制限付き）
            if ($model->usr_auth == DEF_REFER_RESTRICT || $model->usr_auth == DEF_UPDATE_RESTRICT) {
                $query .= " AND '".STAFFCD."' IN (T1.TR_CD1, T1.TR_CD2, T1.TR_CD3, T1.SUBTR_CD1, T1.SUBTR_CD2, T1.SUBTR_CD3)  ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            if ($model->search["HR_CLASS_TYPE"] == '2') {
                $query .= "     SCHREG_REGD_FI_HDAT T1";
            } else {
                $query .= "     SCHREG_REGD_HDAT T1 ";
            }
            $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = T1.YEAR ";
            $query .= "           AND GDAT.GRADE = T1.GRADE ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "           AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "           AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            //参照・更新可（制限付き）
            if ($model->usr_auth == DEF_REFER_RESTRICT || $model->usr_auth == DEF_UPDATE_RESTRICT) {
                $query .= " AND (   '".STAFFCD."' IN (T1.TR_CD1, T1.TR_CD2, T1.TR_CD3, T1.SUBTR_CD1, T1.SUBTR_CD2, T1.SUBTR_CD3) ";
                $query .= "      OR (T1.GRADE IN (  SELECT ";
                $query .= "                             FIELD2 ";
                $query .= "                         FROM ";
                $query .= "                             STAFF_DETAIL_MST ST ";
                $query .= "                         WHERE ";
                $query .= "                             T1.YEAR         = ST.YEAR ";
                $query .= "                             AND ST.STAFFCD  = '".STAFFCD."' ";
                $query .= "                             AND ST.STAFF_SEQ IN ('005', '006', '007') ";
                $query .= "                             AND ST.FIELD1   = '0200')) ";
                $query .= "     ) ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //フィールド有無チェック
    public function checkTableColumn($tableName, $columnName)
    {
        $query  = " WITH TMP AS (";
        $query .= " SELECT ";
        $query .= "     NAME ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME  = '{$tableName}' ";
        $query .= " AND NAME  = '{$columnName}' ";

        $query .= " ) SELECT COUNT(*) FROM TMP ";

        return $query;
    }
}
