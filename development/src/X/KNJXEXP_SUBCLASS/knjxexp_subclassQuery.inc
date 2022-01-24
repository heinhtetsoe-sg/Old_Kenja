<?php
class knjxexp_subclassQuery extends Query
{

    //左リスト
    public function GetStudents($model)
    {
//★このSQLでリストを取得する。
//        $tableName = "SCHREG_BASE_MST";
//        if ($model->search["SEARCH_DIV"] == "1") {
//            $tableName = "FRESHMAN_DAT";
//        }
//★$whereは別関数にて設定。有効な項目ごとに条件設定する。->細かいことはあまりしないので、不要。
//        $where = knjxexp_subclassQuery::getStudentSearch($model);
//        $and = $where == "" ? "" : "AND ";
//
//        $query = " SELECT DISTINCT ";
//        $query .= "   SCH_R.GRADE || SCH_R.HR_CLASS || SCH_R.ATTENDNO AS HR_CLASS_ATTEND_ORDER, ";
//        $query .= "   SCH_R.GRADE, ";
//        $query .= "   SCH_R.HR_CLASS, ";
//        $query .= "   SCH_R.ATTENDNO, ";
//        $query .= "   MAIN.SCHREGNO, ";
//        $query .= "   MAIN.NAME, ";
//        $query .= "   MAIN.SEX, ";
//        $query .= "   SCH_RH.HR_NAME || ' - ' || SCH_R.ATTENDNO AS HR_ATTEND ";
//        $query .= " FROM ";
//        $query .= "   {$tableName} MAIN ";
//        $query .= "   LEFT JOIN SCHREG_REGD_DAT SCH_R ON MAIN.SCHREGNO = SCH_R.SCHREGNO ";
//        $query .= "        AND SCH_R.YEAR = '".CTRL_YEAR."' ";
//        $query .= "        AND SCH_R.SEMESTER = '".CTRL_SEMESTER."' ";
//        $query .= "   LEFT JOIN SCHREG_REGD_HDAT SCH_RH ON SCH_R.YEAR = SCH_RH.YEAR ";
//        $query .= "        AND SCH_R.SEMESTER = SCH_RH.SEMESTER ";
//        $query .= "        AND SCH_R.GRADE = SCH_RH.GRADE ";
//        $query .= "        AND SCH_R.HR_CLASS = SCH_RH.HR_CLASS ";
//        $query .= "  LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR  = SCH_R.YEAR ";
//        $query .= "                               AND L1.GRADE = SCH_R.GRADE ";
//        $query .= " WHERE ".$where;
//        if ($model->Properties["use_prg_schoolkind"] == "1") {
//            if ($model->selectSchoolKind) {
//                $query .= "  AND L1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
//            }
//        } else if ($model->Properties["useSchool_KindField"] == "1") {
//            $query .= "  AND L1.SCHOOL_KIND = '".SCHOOLKIND."' ";
//        }
//
//        $query .= " ORDER BY ";
//        if ($model->search["HR_CLASS_HYOUJI_FLG"] == "1") {
//            $query .= "   SCH_R.GRADE || SCH_R.HR_CLASS || SCH_R.ATTENDNO ";
//        } else {
//            $query .= "   MAIN.SCHREGNO ";
//        }

//★$whereは別関数にて設定。有効な項目ごとに条件設定する。->細かいことはあまりしないので、不要。
        $where = knjxexp_subclassQuery::getStudentSearch($model);
        $and = $where == "" ? "" : "AND ";
        $query  = " SELECT ";
        $query .= "     T4.GRADE, ";
        $query .= "     T5.GRADE_CD, ";
        $query .= "     T4.HR_CLASS, ";
        $query .= "     T4.ATTENDNO, ";
        $query .= "     T3.NAME_SHOW AS NAME, ";
        $query .= "     T3.NAME_KANA, ";
        $query .= "     T2.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T3 ON T2.SCHREGNO = T3.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T3.SCHREGNO ";
        $query .= "       AND T4.YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND T4.SEMESTER = '".$model->search["SEMESTER"]."' ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T5 ON T5.GRADE = T4.GRADE ";
        $query .= "       AND T5.YEAR = T4.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$model->search["SEMESTER"]."' AND ";
        $query .= "     T1.CHAIRCD  = T2.CHAIRCD ";

        $query .= $and.$where;

        $query .= " ORDER BY ";
        $query .= "     T4.GRADE,T4.HR_CLASS,T4.ATTENDNO ";

        return $query;
    }

    public function getStudentSearch($model)
    {
//★where句の記載のみ。有効な項目ごとに条件設定する。
        $and = "";
        $where = "";

        //受験資格者のみの場合、条件追加
        //★変数あるのか、要確認
        if ($model->dispData["req_flg"] != "" && $model->search["REQ_FLG"]) {
            if ($model->dispData["testtype"] == "2" && $model->search["TESTTYPE"] == "990008") {
                $where .= "     EXISTS (SELECT ";
                $where .= "                 'X' ";
                $where .= "             FROM ";
                $where .= "                 RECORD_SCORE_HIST_DAT S1 ";
                $where .= "                 INNER JOIN NAME_MST M002 ON M002.NAMECD1 = 'M002' ";
                $where .= "             WHERE ";
                $where .= "                 T1.YEAR         = S1.YEAR AND ";
                $where .= "                 T1.SEMESTER     = S1.SEMESTER AND ";
                $where .= "                 S1.SEMESTER || '-' || S1.TESTKINDCD || '-' || S1.TESTITEMCD || '-' || S1.SCORE_DIV = M002.NAMESPARE1 AND ";
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $where .= "                 T1.CLASSCD       = S1.CLASSCD AND ";
                    $where .= "                 T1.SCHOOL_KIND   = S1.SCHOOL_KIND AND ";
                    $where .= "                 T1.CURRICULUM_CD = S1.CURRICULUM_CD AND ";
                }
                $where .= "                 T1.SUBCLASSCD   = S1.SUBCLASSCD AND ";
                $where .= "                 T2.SCHREGNO     = S1.SCHREGNO AND ";
                $where .= "                 S1.SEQ = 1 AND ";
                $where .= "                 S1.SCORE IS NOT NULL ) AND ";
            } else {
                $where .= "     EXISTS (SELECT ";
                $where .= "                 'X' ";
                $where .= "             FROM ";
                $where .= "                 SUBCLASS_STD_PASS_SDIV_DAT S1 ";
                $where .= "             WHERE ";
                $where .= "                 T1.YEAR         = S1.YEAR AND ";
                $where .= "                 T1.SEMESTER     = S1.SEMESTER AND ";
                if ($model->dispData["testtype"] != "" && $model->search["TESTTYPE"] != "") {
                    $where .= "                 S1.TESTKINDCD || S1.TESTITEMCD || S1.SCORE_DIV = '".$model->search["TESTTYPE"]."' AND ";
                }
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $where .= "                 T1.CLASSCD       = S1.CLASSCD AND ";
                    $where .= "                 T1.SCHOOL_KIND   = S1.SCHOOL_KIND AND ";
                    $where .= "                 T1.CURRICULUM_CD = S1.CURRICULUM_CD AND ";
                }
                $where .= "                 T1.SUBCLASSCD   = S1.SUBCLASSCD AND ";
                $where .= "                 T2.SCHREGNO     = S1.SCHREGNO AND ";
                $where .= "                 S1.SEM_PASS_FLG = '1' ) AND ";
            }
        }
        if ($model->dispData["subclass"] != "" && $model->dispData["chair"] != "" && $model->search["CHAIR"] != "") {
            $where .= "                 T1.CHAIRCD = '".$model->search["CHAIR"]."' AND ";
        }
        if ($model->dispData["subclass"] != "") {
            if ($model->Properties["useCurriculumcd"] == "1") {
                $where .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->search["SUBCLASS"]."' ";
            } else {
                $where .= "     T1.SUBCLASSCD = '".$model->search["SUBCLASS"]."' ";
            }
        }

//        if ($model->search["SEARCH_DIV"] == "1") {
//            $where .= "   ".$and."MAIN.ENTERYEAR = '".(CTRL_YEAR + 1)."' ";
//            $and = "AND ";
//        } else if ($model->dispData["select_grd"] != "1") {
//            $where .= "   ".$and."VALUE(MAIN.GRD_DIV, '0') NOT IN ('1', '2', '3', '9') ";
//            $and = "AND ";
//        }
//
//        if ($model->search["SEARCH_TENHEN"] == "1") {
//            $where .= "   ".$and."MAIN.ENT_DIV IN ('4', '5') ";
//            $and = "AND ";
//        }
//
//        //入学年度
//        if ($model->search["ENT_YEAR"]) {
//            if ($model->search["SEARCH_DIV"] == "1") {
//                $where .= "   ".$and."MAIN.ENTERYEAR = '".$model->search["ENT_YEAR"]."' ";
//                $and = "AND ";
//            } else {
//                $where .= "   ".$and."FISCALYEAR(ENT_DATE) = '".$model->search["ENT_YEAR"]."' ";
//                $and = "AND ";
//            }
//        }
//
//        //卒業予定年度
//        if ($model->search["GRD_YEAR"]) {
//            if ($model->search["SEARCH_DIV"] == "1") {
//                $where .= "   ".$and."(CAST(MAIN.ENTERYEAR AS SMALLINT) + 3) = ".$model->search["GRD_YEAR"]." ";
//                $and = "AND ";
//            } else {
//                $where .= "   ".$and."FISCALYEAR(GRD_DATE) = '".$model->search["GRD_YEAR"]."' ";
//                $and = "AND ";
//            }
//        }
//
//        //学年
//        if ($model->search["GRADE"]) {
//            $where .= "   ".$and."SCH_R.GRADE = '".$model->search["GRADE"]."' ";
//            $and = "AND ";
//        }
//
//        //年組
//        if ($model->search["HR_CLASS"]) {
//            $where .= "   ".$and."SCH_R.GRADE || SCH_R.HR_CLASS = '".$model->search["HR_CLASS"]."' ";
//            $and = "AND ";
//        }
//
//        //学籍番号
//        if ($model->search["SCHREGNO"]) {
//            $where .= "   ".$and."MAIN.SCHREGNO = '".$model->search["SCHREGNO"]."' ";
//            $and = "AND ";
//        }
//
//        //氏名
//        if ($model->search["NAME"]) {
//            $where .= "   ".$and."MAIN.NAME LIKE '%" .str_replace("'","''",$model->search["NAME"]). "%' ";
//            $and = "AND ";
//        }
//
//        //氏名かな
//        if ($model->search["NAME_KANA"]) {
//            $where .= "   ".$and."MAIN.NAME_KANA LIKE '%" .str_replace("'","''",$model->search["NAME_KANA"]). "%' ";
//            $and = "AND ";
//        }
//
        return $where;
    }

//    //入学年度
//    function getEntYear()
//    {
//        $query  = " SELECT ";
//        $query .= "     YEAR AS LABEL, ";
//        $query .= "     YEAR AS VALUE ";
//        $query .= " FROM ";
//        $query .= "     SCHOOL_MST ";
//        $query .= " GROUP BY ";
//        $query .= "     YEAR ";
//        $query .= " ORDER BY ";
//        $query .= "     VALUE DESC ";
//
//        return $query;
//    }
//
//    //名称マスタ
//    function getNameMst($namecd1)
//    {
//        $query  = " SELECT ";
//        $query .= "     NAME1 AS LABEL, ";
//        $query .= "     NAMECD2 AS VALUE ";
//        $query .= " FROM ";
//        $query .= "     NAME_MST ";
//        $query .= " WHERE ";
//        $query .= "     NAMECD1 = '{$namecd1}' ";
//        $query .= " ORDER BY ";
//        $query .= "     VALUE ";
//
//        return $query;
//    }
//
//    //有無
//    function getUmu()
//    {
//        $query  = " WITH PAID_UMU (LABEL, VALUE) AS ( ";
//        $query .= "     VALUES('有', '1') ";
//        $query .= "     UNION ";
//        $query .= "     VALUES('無', '2') ";
//        $query .= " ) ";
//        $query .= " SELECT ";
//        $query .= "     * ";
//        $query .= " FROM ";
//        $query .= "     PAID_UMU ";
//        $query .= " ORDER BY ";
//        $query .= "     VALUE ";
//
//        return $query;
//    }
//
//    //卒業予定年度
//    function getGrdYear()
//    {
//        $query  = " WITH MIRAI_3YEAR (LABEL, VALUE) AS ( ";
//        $query .= "     VALUES ";
//        $query .= "         ('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."'), ";
//        $query .= "         ('".(CTRL_YEAR + 2)."', '".(CTRL_YEAR + 2)."'), ";
//        $query .= "         ('".(CTRL_YEAR + 3)."', '".(CTRL_YEAR + 3)."') ";
//        $query .= " ) ";
//        $query .= " SELECT ";
//        $query .= "     YEAR AS LABEL, ";
//        $query .= "     YEAR AS VALUE ";
//        $query .= " FROM ";
//        $query .= "     SCHOOL_MST ";
//        $query .= " UNION ";
//        $query .= " SELECT ";
//        $query .= "     LABEL, ";
//        $query .= "     VALUE ";
//        $query .= " FROM ";
//        $query .= "     MIRAI_3YEAR ";
//        $query .= " ORDER BY ";
//        $query .= "     VALUE DESC ";
//
//        return $query;
//    }
//
//    //学年
//    function getGrade($model)
//    {
//        $query  = " SELECT ";
//        $query .= "     GRADE_NAME1 AS LABEL, ";
//        $query .= "     GRADE AS VALUE ";
//        $query .= " FROM ";
//        $query .= "     SCHREG_REGD_GDAT ";
//        $query .= " WHERE ";
//        $query .= "     YEAR = '".CTRL_YEAR."' ";
//        if ($model->Properties["use_prg_schoolkind"] == "1") {
//            if ($model->selectSchoolKind) {
//                $query .= "  AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
//            }
//        } else if ($model->Properties["useSchool_KindField"] == "1") {
//            $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
//        }
//        $query .= " ORDER BY ";
//        $query .= "     VALUE ";
//
//        return $query;
//    }
//
//    //学年
//    function getHrClass($model)
//    {
//        $query  = " SELECT ";
//        $query .= "     T1.HR_NAME AS LABEL, ";
//        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE ";
//        $query .= " FROM ";
//        $query .= "     SCHREG_REGD_HDAT T1 ";
//        if ($model->Properties["use_prg_schoolkind"] == "1") {
//            if ($model->selectSchoolKind) {
//                $query .= "     INNER JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR  = T1.YEAR ";
//                $query .= "           AND L1.GRADE = T1.GRADE ";
//                $query .= "           AND L1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
//            }
//        } else if ($model->Properties["useSchool_KindField"] == "1") {
//            $query .= "     INNER JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR  = T1.YEAR ";
//            $query .= "           AND L1.GRADE = T1.GRADE ";
//            $query .= "           AND L1.SCHOOL_KIND = '".SCHOOLKIND."' ";
//        }
//        $query .= " WHERE ";
//        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
//        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
//        if ($model->usr_auth == DEF_UPDATE_RESTRICT || $model->usr_auth == DEF_REFER_RESTRICT) {
//            $query .= " AND ((T1.TR_CD1 = '". STAFFCD ."' OR";
//            $query .= "  T1.TR_CD2 = '". STAFFCD ."' OR";
//            $query .= "  T1.TR_CD3 = '". STAFFCD ."' OR";
//            $query .= "  T1.SUBTR_CD1 = '". STAFFCD ."' OR";
//            $query .= "  T1.SUBTR_CD2 = '". STAFFCD ."' OR";
//            $query .= "  T1.SUBTR_CD3 = '". STAFFCD ."') ";
//            $query .= " OR (T1.GRADE IN ";
//            $query .= "       (SELECT ";
//            $query .= "            FIELD2 ";
//            $query .= "        FROM ";
//            $query .= "            STAFF_DETAIL_MST ST ";
//            $query .= "        WHERE ";
//            $query .= "            T1.YEAR = ST.YEAR ";
//            $query .= "            AND ST.STAFFCD = '".STAFFCD."' ";
//            $query .= "            AND ST.STAFF_SEQ IN ('005', '006', '007') ";
//            $query .= "            AND ST.FIELD1 = '0200')) ";
//            $query .= "   ) ";
//        }
//        $query .= " ORDER BY ";
//        $query .= "     VALUE ";
//
//        return $query;
//    }

    //テスト種別
    public function getTestcd($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS VALUE ";
        $query .= "   , T2.TESTITEMNAME AS LABEL ";
        $query .= " FROM NAME_MST T1 ";
        $query .= " INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ON T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".$model->search["SEMESTER"]."' ";
        $query .= "     AND T2.SEMESTER || '-' || T2.TESTKINDCD || '-' || T2.TESTITEMCD || '-' || T2.SCORE_DIV = T1.NAMESPARE1 ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'M002' ";
        if ($model->dispData["testtype"] == "2") {
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     '990008' AS VALUE ";
            $query .= "   , '学期評価' AS LABEL ";
            $query .= " FROM SYSIBM.SYSDUMMY1 T1 ";
            $query .= " WHERE '990008' NOT IN ( ";
            $query .= "     SELECT ";
            $query .= "         T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS VALUE ";
            $query .= "     FROM NAME_MST T1 ";
            $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ON T2.YEAR = '".CTRL_YEAR."' ";
            $query .= "         AND T2.SEMESTER = '".$model->search["SEMESTER"]."' ";
            $query .= "         AND T2.SEMESTER || '-' || T2.TESTKINDCD || '-' || T2.TESTITEMCD || '-' || T2.SCORE_DIV = T1.NAMESPARE1 ";
            $query .= "     WHERE ";
            $query .= "         NAMECD1 = 'M002' ";
            $query .= " ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //科目取得
    public function getSubclassList($model)
    {
        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T2.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     CHAIR_DAT T2 ";
        $query .= "     LEFT JOIN SUBCLASS_MST T3 ON T2.SUBCLASSCD      = T3.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                          AND T2.CLASSCD         = T3.CLASSCD ";
            $query .= "                          AND T2.SCHOOL_KIND     = T3.SCHOOL_KIND ";
            $query .= "                          AND T2.CURRICULUM_CD   = T3.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        if ($model->search["SEMESTER"] == "2") {
            $query .= "     (T1.SEMESTER = '".$model->search["SEMESTER"]."' OR ";
            $query .= "      T1.SEMESTER = '1') AND ";
        } else {
            $query .= "     T1.SEMESTER = '".$model->search["SEMESTER"]."' AND ";
        }
        $query .= "     T1.CHAIRCD = T2.CHAIRCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座取得
    public function getChairList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.CHAIRCD AS VALUE, ";
        $query .= "     T2.CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' AND ";
        if ($model->dispData["subclass"] == 1 && $model->search["SUBCLASS"] != "") {
            $spl = explode("-", $model->search["SUBCLASS"]);
            if ($spl[3]) {
                $query .= "     T2.SUBCLASSCD = '".$spl[3]."' AND ";
            }
        }
        if ($model->search["SEMESTER"] == "2") {
            $query .= "     (T2.SEMESTER = '".$model->search["SEMESTER"]."' OR ";
            $query .= "      T2.SEMESTER = '1') ";
        } else {
            $query .= "     T2.SEMESTER = '".$model->search["SEMESTER"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getSemesterName($model)
    {
        $query  = " SELECT ";
        $query .= "   SEMESTER, SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "   SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$model->search["YEAR"]."' ";
        $query .= "   AND SEMESTER = '".$model->search["SEMESTER"]."' ";
        return $query;
    }
}
