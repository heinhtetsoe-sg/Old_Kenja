<?php

require_once('for_php7.php');
class knja032Query extends Query {

    function getGradeInfo($model, $grade) {

        $query  = " SELECT ";
        $query .= "     T1.NAME1, ";
        $query .= "     T1.NAMESPARE2 AS GRD_GRADE_F, ";
        $query .= "     T1.NAMESPARE3 AS GRD_GRADE_T, ";
        $query .= "     CASE WHEN L1.MAXCD2 IS NOT NULL ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '0' ";
        $query .= "     END AS SAISYU_GRADE ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= "     LEFT JOIN (SELECT ";
        if ($model->A023Cnt > 1) {
            $query .= "                    MAX(LT1.ABBV2) AS MAXCD2 ";
        } else {
            $query .= "                    '1' AS MAXCD2 ";
        }
        $query .= "                FROM ";
        $query .= "                    NAME_MST LT1 ";
        $query .= "                WHERE ";
        $query .= "                    LT1.NAMECD1 = 'A023' ";
        if ($model->A023Cnt > 1) {
            $query .= "     ) L1 ON T1.ABBV2 = L1.MAXCD2 ";
        } else {
            $query .= "     ) L1 ON VALUE(T1.ABBV2, '1') = L1.MAXCD2 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'A023' ";
        $query .= "     AND '{$grade}' BETWEEN T1.NAME2 AND T1.NAME3 ";

        return $query;
    }

    // 成績テーブルを判断するためのフラグを取得(法政・自修館・その他)
    function getNameMst() {
        $query  = "";
        $query .= "SELECT ";
        $query .= "    NAMECD1, ";
        $query .= "    NAMECD2, ";
        $query .= "    NAME1, ";
        $query .= "    NAMESPARE1, ";
        $query .= "    NAMESPARE2 ";
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1='Z010' AND ";
        $query .= "    NAMECD2='00' ";
        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $rtnRow;
    }

    //年、組コンボボックス
    function GradeClassQuery($model)
    {
        $query = " SELECT ";
        $query .= "   T1.GRADE || ',' || T1.HR_CLASS AS GC, ";
        $query .= "   T1.HR_NAME, ";
        $query .= "   GDAT.SCHOOL_KIND, ";
        $query .= "   T2.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_HDAT T1 LEFT OUTER JOIN  ";
        $query .= "   STAFF_MST T2 ON T1.TR_CD1 = T2.STAFFCD ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ";
        $query .= "        AND GDAT.GRADE = T1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ";
                $query .= " AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ";
            $query .= " AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "   T1.YEAR  = '" .CTRL_YEAR ."' AND ";
        $query .= "   T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "   GC ";

        return $query;
    }
    //学校マスタ
    function selectSchool_Mst($model, $schoolKind)
    {
        $query = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR          = '" .CTRL_YEAR ."'";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SCHOOL_KIND = '" . $schoolKind ."' ";
        }
        return $query;
    }
    //次年度の学期マスタが存在するか
    function selectSemester_Mst()
    {
        $query = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR          = '" .((int) CTRL_YEAR + 1) ."' ";

        return $query;
    }
    //名称マスタ「A023」のカウントを取得１件の場合はABBV2=1として動かす
    function selectA023() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '" .((int) CTRL_YEAR + 1) ."' AND ";
        $query .= "     NAMECD1 = 'A023' ";

        return $query;
    }
    //名称マスタ「A023」略称2が存在するか
    function selectA023Abbv2() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '" .((int) CTRL_YEAR + 1) ."' AND ";
        $query .= "     NAMECD1 = 'A023' AND ";
        $query .= "     (ABBV2 IS NULL OR ABBV2 = '') ";

        return $query;
    }
    //名称マスタ「A003」5:修了が存在するか
    function selectA003() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '" .((int) CTRL_YEAR + 1) ."' AND ";
        $query .= "     NAMECD1 = 'A003' AND ";
        $query .= "     NAMECD2 = '5' ";

        return $query;
    }

    //卒業可能学年
    function getGraduate($model, $grade)
    {
        $query .= " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     CASE WHEN T1.GRADE BETWEEN T2.NAMESPARE2 AND T2.NAMESPARE3 THEN 1 END AS IS_GRD, ";
        $query .= "     CASE WHEN L1.ABBV2 IS NOT NULL THEN 1 END AS MAX_SK ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= "     INNER JOIN NAME_MST T2 ";
        $query .= "          ON T2.NAMECD1  = 'A023' ";
        $query .= "         AND T2.NAME1    = T1.SCHOOL_KIND ";
        $query .= "     LEFT JOIN ( SELECT ";
        if ($model->A023Cnt > 1) {
            $query .= "                     MAX(ABBV2) AS ABBV2 ";
        } else {
            $query .= "                    '1' AS ABBV2 ";
        }
        $query .= "                 FROM ";
        $query .= "                     V_NAME_MST ";
        $query .= "                 WHERE ";
        $query .= "                     NAMECD1 = 'A023') L1 ";
        if ($model->A023Cnt > 1) {
            $query .= "          ON T2.ABBV2 = L1.ABBV2 ";
        } else {
            $query .= "          ON VALUE(T2.ABBV2, '1') = L1.ABBV2 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE    = '".$grade."' ";

        return $query;
    }

    //コンボボックスより、生徒を検索
    function ReadQuery($model)
    {
        list($grade, $hr_class) = explode(",",$model->gc_select);

        $query = " SELECT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   INTEGER(T1.ATTENDNO) AS ATTENDNO, ";
        $query .= "   T2.HR_NAMEABBV, ";
        $query .= "   T3.NAME_SHOW, ";
        $query .= "   T3.NAME_KANA, ";
        $query .= "   T3.GRD_DATE, ";
        $query .= "   T3.GRD_DIV, ";
        $query .= "   T8.REMAINGRADE_FLG, ";
        $query .= "   T3.GRD_NO, ";
        $query .= "   MEISYOU_GET(T6.TRANSFERCD,'A004',1) AS TRANS_STATUS, ";
        $query .= "   COALESCE(T7.ABROAD_CREDITS, 0) AS ABROAD_CREDITS ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   LEFT OUTER JOIN SCHREG_TRANSFER_DAT T6  ";
        $query .= "   ON T1.SCHREGNO = T6.SCHREGNO AND  ";
        $query .= "   DATE('".str_replace("/","-",CTRL_DATE)."') BETWEEN T6.TRANSFER_SDATE AND T6.TRANSFER_EDATE  ";
        $query .= "   LEFT OUTER JOIN ( SELECT ";
        $query .= "   ST1.SCHREGNO, ";
        $query .= "   SUM(COALESCE(ST1.ABROAD_CREDITS, 0)) AS ABROAD_CREDITS ";
        $query .= " FROM ";
        $query .= "   SCHREG_TRANSFER_DAT ST1 ";
        if ($model->school["SCHOOLDIV"] == 0 ){     //学年制
            $query .= " WHERE ";
            $query .= "   FISCALYEAR(ST1.TRANSFER_SDATE) = '" .CTRL_YEAR ."' ";
        }
        $query .= " GROUP BY ";
        $query .= "   ST1.SCHREGNO ";
        $query .= " ) T7 ON T1.SCHREGNO = T7.SCHREGNO ";
        $query .= "   LEFT OUTER JOIN CLASS_FORMATION_DAT T8  ";
        $query .= "   ON T1.SCHREGNO = T8.SCHREGNO AND ";
        $query .= "   INTEGER(T1.YEAR) + 1 = INTEGER(T8.YEAR) AND ";
        $query .= "   T8.SEMESTER = '1',";
        $query .= "   SCHREG_REGD_HDAT T2, ";
        $query .= "   SCHREG_BASE_MST T3 ";
        $query .= " WHERE ";
        $query .= "   T1.SCHREGNO = T3.SCHREGNO AND ";
        $query .= "   T1.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "   T1.YEAR = T2.YEAR AND ";
        $query .= "   T1.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        $query .= "   T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "   T2.GRADE = '" .$grade ."' AND ";
        $query .= "   T1.GRADE = T2.GRADE AND ";
        $query .= "   T2.HR_CLASS = '" .$hr_class ."' AND ";
        $query .= "   T1.HR_CLASS = T2.HR_CLASS AND ";
        $query .= "   ((T3.GRD_DIV IS NOT NULL AND T3.GRD_DIV NOT IN ('2','3','6','7')) OR ";
        $query .= "   (T3.GRD_DIV IS NULL OR T3.GRD_DIV = '')) ";
        if (is_array($model->sort)){
            $query .= " ORDER BY ";
            foreach ($model->sort as $field => $order){
                switch($field){
                    case "ATTENDNO":    //番号順
                        $query .= "T2.HR_NAMEABBV,INTEGER(T1.ATTENDNO) ";
                        $query .= ($order > 0)? "ASC " : "DESC ";
                        break 2;
                    case "TARGET":       //席次順
                        if ($order > 0){
                            $query .= "T8.REMAINGRADE_FLG,T3.GRD_DIV DESC,T2.HR_NAMEABBV,INTEGER(T1.ATTENDNO) ";
                        }else{
                            $query .= "T8.REMAINGRADE_FLG,T3.GRD_DIV,T2.HR_NAMEABBV,INTEGER(T1.ATTENDNO) ";
                        }
                        break 2;
                }
            }
        }else{
            $query .= "ORDER BY ";
            $query .= "    T2.HR_NAMEABBV, ";
            $query .= "    INTEGER(T1.ATTENDNO) ASC ";
        }

        return $query;
    }

    //修得単位(学年制)
    function getGetCredits0($schregno, $model)
    {
        $query  = "WITH RECORD_CREDIT AS ( ";
        $query .= "SELECT ";
        $query .= "    SUM(VALUE(GET_CREDIT, 0)) AS RECORD_CREDITS ";
        $query .= "FROM ";
        if ($model->recordTableDiv == "1") {
            $query .= "    RECORD_SCORE_DAT ";
        } else {
            $query .= "    RECORD_DAT ";
        }
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR ."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ";
            $query .= "             NOT IN (SELECT DISTINCT ";
            $query .= "                         ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
        } else {
            $query .= "    AND SUBCLASSCD NOT IN (SELECT DISTINCT ";
            $query .= "                               ATTEND_SUBCLASSCD ";
        }
        $query .= "                           FROM ";
        $query .= "                               SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                           WHERE ";
        $query .= "                               REPLACECD = '1' ";
        $query .= "                               AND YEAR = '".CTRL_YEAR ."' ";
        $query .= "                          ) ";
        $query .= "    AND SCHREGNO = '".$schregno."' ";
        if ($model->recordTableDiv == "1") {
            $query .= "   AND  SEMESTER = '9' ";
            $query .= "   AND  TESTKINDCD = '99' ";
            $query .= "   AND  TESTITEMCD = '00' ";
            if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                $query .= "   AND  SCORE_DIV = '09' ";
            } else {
                $query .= "   AND  SCORE_DIV = '00' ";
            }
        }
        $query .= "), TRANSFER_CREDIT AS ( ";
        $query .= "SELECT ";
        $query .= "    SUM(VALUE(ABROAD_CREDITS, 0)) AS TRANSFER_CREDITS ";
        $query .= "FROM ";
        $query .= "    SCHREG_TRANSFER_DAT ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$schregno."' ";
        $query .= "    AND TRANSFER_SDATE BETWEEN '".str_replace("/", "-", $model->control_data["学期開始日付"][9])."' ";
        $query .= "                           AND '".str_replace("/", "-", $model->control_data["学期終了日付"][9])."' ";
        $query .= "), QUALIFIED_CREDIT AS ( ";
        $query .= "SELECT ";
        $query .= "    SUM(VALUE(CREDITS, 0)) AS QUALIFIED_CREDITS ";
        $query .= "FROM ";
        $query .= "    SCHREG_QUALIFIED_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR ."' ";
        $query .= "    AND SCHREGNO = '".$schregno."' ";
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    VALUE(RECORD_CREDITS, 0) + ";
        $query .= "    VALUE(TRANSFER_CREDITS, 0) + ";
        $query .= "    VALUE(QUALIFIED_CREDITS, 0) AS GET_CREDITS ";
        $query .= "FROM ";
        $query .= "    RECORD_CREDIT, ";
        $query .= "    TRANSFER_CREDIT, ";
        $query .= "    QUALIFIED_CREDIT ";

        return $query;
    }

    //習得単位(単位制)
    function getGetCredits1($schregno, $model)
    {
        $query  = "WITH RECORD_CREDIT AS ( ";
        $query .= "SELECT ";
        $query .= "    SUM(VALUE(GET_CREDIT, 0)) AS RECORD_CREDITS ";
        $query .= "FROM ";
        if ($model->recordTableDiv == "1") {
            $query .= "    RECORD_SCORE_DAT ";
        } else {
            $query .= "    RECORD_DAT ";
        }
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR ."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ";
            $query .= "             NOT IN (SELECT DISTINCT ";
            $query .= "                         ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
        } else {
            $query .= "    AND SUBCLASSCD NOT IN (SELECT DISTINCT ";
            $query .= "                               ATTEND_SUBCLASSCD ";
        }
        $query .= "                           FROM ";
        $query .= "                               SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                           WHERE ";
        $query .= "                               REPLACECD = '1' ";
        $query .= "                               AND YEAR = '".CTRL_YEAR ."' ";
        $query .= "                          ) ";
        $query .= "    AND SCHREGNO = '".$schregno."' ";
        if ($model->recordTableDiv == "1") {
            $query .= "   AND  SEMESTER = '9' ";
            $query .= "   AND  TESTKINDCD = '99' ";
            $query .= "   AND  TESTITEMCD = '00' ";
            if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                $query .= "   AND  SCORE_DIV = '09' ";
            } else {
                $query .= "   AND  SCORE_DIV = '00' ";
            }
        }
        $query .= "), TRANSFER_CREDIT AS ( ";
        $query .= "SELECT ";
        $query .= "    SUM(VALUE(ABROAD_CREDITS, 0)) AS TRANSFER_CREDITS ";
        $query .= "FROM ";
        $query .= "    SCHREG_TRANSFER_DAT ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$schregno."' ";
        $query .= "    AND TRANSFER_SDATE BETWEEN '".str_replace("/", "-", $model->control_data["学期開始日付"][9])."' ";
        $query .= "                           AND '".str_replace("/", "-", $model->control_data["学期終了日付"][9])."' ";
        $query .= "), QUALIFIED_CREDIT AS ( ";
        $query .= "SELECT ";
        $query .= "    SUM(VALUE(CREDITS, 0)) AS QUALIFIED_CREDITS ";
        $query .= "FROM ";
        $query .= "    SCHREG_QUALIFIED_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR ."' ";
        $query .= "    AND SCHREGNO = '".$schregno."' ";
        $query .= "), STUDYREC_CREDIT AS ( ";
        $query .= "SELECT ";
        $query .= "    SUM(VALUE(GET_CREDIT, 0)) + SUM(VALUE(ADD_CREDIT, 0)) AS STUDYREC_CREDIT ";
        $query .= "FROM ";
        $query .= "    SCHREG_STUDYREC_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR < '".CTRL_YEAR."' ";
        $query .= "    AND SCHREGNO = '".$schregno."' ";
        $query .= ") ";
        if ("1" == $model->field["GRADUATE"]) {
            $query .= "SELECT ";
            $query .= "    VALUE(RECORD_CREDITS, 0) ";
            $query .= "    + VALUE(TRANSFER_CREDITS, 0) ";
            $query .= "    + VALUE(QUALIFIED_CREDITS, 0) ";
            $query .= "    + VALUE(STUDYREC_CREDIT, 0) ";
            $query .= "      AS GET_CREDITS ";
            $query .= "FROM ";
            $query .= "    RECORD_CREDIT, ";
            $query .= "    TRANSFER_CREDIT, ";
            $query .= "    QUALIFIED_CREDIT, ";
            $query .= "    STUDYREC_CREDIT ";
        } else {
            $query .= "SELECT ";
            $query .= "    VALUE(RECORD_CREDITS, 0) ";
            $query .= "    + VALUE(TRANSFER_CREDITS, 0) ";
            $query .= "    + VALUE(QUALIFIED_CREDITS, 0) ";
            $query .= "      AS GET_CREDITS ";
            $query .= "FROM ";
            $query .= "    RECORD_CREDIT, ";
            $query .= "    TRANSFER_CREDIT, ";
            $query .= "    QUALIFIED_CREDIT ";
        }

        return $query;
    }

    //修得単位(学年制)
    function getGetCredits2($schregno, $model)
    {
        list($grade, $hr_class) = explode(",",$model->gc_select);

        $query  = " SELECT ";
        $query .= "       SUM(COALESCE(ST4.CREDITS, 0)) AS GET_CREDITS ";
        $query .= " FROM ";
        $query .= "      (SELECT ";
        $query .= "           ST1.SCHREGNO, ";
        $query .= "           CASE WHEN ST2.JUDGE_PATTERN = 'A' THEN ST2.A_PATTERN_ASSESS ";
        $query .= "                WHEN ST2.JUDGE_PATTERN = 'B' THEN ST2.B_PATTERN_ASSESS ";
        $query .= "                WHEN ST2.JUDGE_PATTERN = 'C' THEN ST2.C_PATTERN_ASSESS ";
        $query .= "           END AS ASSESS, ";
        $query .= "           ST3.CREDITS, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           ST2.CLASSCD, ";
            $query .= "           ST2.SCHOOL_KIND, ";
            $query .= "           ST2.CURRICULUM_CD, ";
        }
        $query .= "           ST2.SUBCLASSCD, ";
        $query .= "           ST2.GRADE_RECORD ";
        $query .= "       FROM ";
        $query .= "           SCHREG_REGD_DAT AS ST1, ";
        $query .= "           KIN_RECORD_DAT AS ST2, ";
        $query .= "           CREDIT_MST AS ST3 ";
        $query .= "       WHERE ";
        $query .= "           ST1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "           ST1.SEMESTER = '".CTRL_SEMESTER."'AND ";
        $query .= "           ST1.SCHREGNO = '".$schregno."'AND ";
        $query .= "           ST2.YEAR = ST1.YEAR AND ";
        $query .= "           ST2.SCHREGNO = ST1.SCHREGNO AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     ST2.CLASSCD = ST3.CLASSCD AND ";
            $query .= "                     ST2.SCHOOL_KIND = ST3.SCHOOL_KIND AND ";
            $query .= "                     ST2.CURRICULUM_CD = ST3.CURRICULUM_CD AND ";
        }
        $query .= "                     ST2.SUBCLASSCD = ST3.SUBCLASSCD AND ";
        $query .= "                     ST3.YEAR = ST1.YEAR AND ";
        $query .= "                     ST3.COURSECD = ST1.COURSECD AND ";
        $query .= "                     ST3.MAJORCD = ST1.MAJORCD AND ";
        $query .= "                     ST3.GRADE = ST1.GRADE AND ";
        $query .= "                     ST3.COURSECODE = ST1.COURSECODE AND ";
        $query .= "                     ST3.CLASSCD = SUBSTR(ST3.SUBCLASSCD,1,2) AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     ST3.CLASSCD || '-' || ST3.SCHOOL_KIND || '-' || ST3.CURRICULUM_CD || '-' || ST3.SUBCLASSCD ";
            $query .= "                             NOT IN (SELECT ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
            $query .= "                                     FROM SUBCLASS_REPLACE_DAT ";
            $query .= "                                     WHERE REPLACECD = '1' ";
            $query .= "                                       AND YEAR = '".CTRL_YEAR."' ";
            $query .= "                                       AND ANNUAL = '" .$grade ."') ) ST4 ";
        } else {
            $query .= "                     ST3.SUBCLASSCD NOT IN (SELECT ATTEND_SUBCLASSCD ";
            $query .= "                                            FROM SUBCLASS_REPLACE_DAT ";
            $query .= "                                            WHERE REPLACECD = '1' ";
            $query .= "                                                  AND YEAR = '".CTRL_YEAR."' ";
            $query .= "                                                  AND ANNUAL = '" .$grade ."') ) ST4 ";
        }
        $query .= "         WHERE ";
        $query .= "           (ST4.ASSESS <> '1' AND ST4.ASSESS IS NOT NULL) OR ";
        $query .= "           (SUBSTR(ST4.SUBCLASSCD,1,2) = '90' AND VALUE(ST4.GRADE_RECORD,0) IN (11,22,33)) ";
        $query .= "         GROUP BY ";
        $query .= "           ST4.SCHREGNO ";

        return $query;
    }

    //保留単位(学年制)
    function getRemCredits0($schregno, $model)
    {
        $query  = "SELECT ";
        $query .= "    SUM(CASE WHEN VALUE(COMP_CREDIT, 0) > VALUE(GET_CREDIT, 0) ";
        $query .= "         THEN VALUE(COMP_CREDIT, 0) - VALUE(GET_CREDIT, 0) ";
        $query .= "         ELSE 0 END ";
        $query .= "       ) AS RECORD_CREDITS ";
        $query .= "FROM ";
        if ($model->recordTableDiv == "1") {
            $query .= "    RECORD_SCORE_DAT ";
        } else {
            $query .= "    RECORD_DAT ";
        }
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR ."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ";
            $query .= "             NOT IN (SELECT DISTINCT ";
            $query .= "                               ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
        } else {
            $query .= "    AND SUBCLASSCD NOT IN (SELECT DISTINCT ";
            $query .= "                               ATTEND_SUBCLASSCD ";
        }
        $query .= "                           FROM ";
        $query .= "                               SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                           WHERE ";
        $query .= "                               REPLACECD = '1' ";
        $query .= "                               AND YEAR = '".CTRL_YEAR ."' ";
        $query .= "                          ) ";
        $query .= "    AND SCHREGNO = '".$schregno ."' ";
        if ($model->recordTableDiv == "1") {
            $query .= "   AND  SEMESTER = '9' ";
            $query .= "   AND  TESTKINDCD = '99' ";
            $query .= "   AND  TESTITEMCD = '00' ";
            if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                $query .= "   AND  SCORE_DIV = '09' ";
            } else {
                $query .= "   AND  SCORE_DIV = '00' ";
            }
        }

        return $query;
    }

    //保留単位(学年制)
    function getRemCredits2($schregno, $model)
    {
        list($grade, $hr_class) = explode(",",$model->gc_select);

        $query  = " SELECT ";
        $query .= "       ST4.SCHREGNO, ";
        $query .= "       SUM(COALESCE(ST4.CREDITS, 0)) AS REM_CREDITS ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "           ST1.SCHREGNO, ";
        $query .= "           CASE WHEN ST2.JUDGE_PATTERN = 'A' THEN ST2.A_PATTERN_ASSESS ";
        $query .= "                WHEN ST2.JUDGE_PATTERN = 'B' THEN ST2.B_PATTERN_ASSESS ";
        $query .= "                WHEN ST2.JUDGE_PATTERN = 'C' THEN ST2.C_PATTERN_ASSESS ";
        $query .= "           END AS ASSESS, ";
        $query .= "           ST3.CREDITS, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           ST2.CLASSCD, ";
            $query .= "           ST2.SCHOOL_KIND, ";
            $query .= "           ST2.CURRICULUM_CD, ";
        }
        $query .= "           ST2.SUBCLASSCD, ";	//NO003
        $query .= "           ST2.GRADE_RECORD ";	//NO003
        $query .= "       FROM ";
        $query .= "           SCHREG_REGD_DAT AS ST1, ";
        $query .= "           KIN_RECORD_DAT AS ST2, ";
        $query .= "           CREDIT_MST AS ST3 ";
        $query .= "       WHERE ";
        $query .= "           ST1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "           ST1.SEMESTER = '".CTRL_SEMESTER."'AND ";
        $query .= "           ST1.SCHREGNO = '".$schregno."'AND ";
        $query .= "           ST2.YEAR = ST1.YEAR AND ";
        $query .= "           ST2.SCHREGNO = ST1.SCHREGNO AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           ST2.CLASSCD = ST3.CLASSCD AND ";
            $query .= "           ST2.SCHOOL_KIND = ST3.SCHOOL_KIND AND ";
            $query .= "           ST2.CURRICULUM_CD = ST3.CURRICULUM_CD AND ";
        }
        $query .= "           ST2.SUBCLASSCD = ST3.SUBCLASSCD AND ";
        $query .= "           ST3.YEAR = ST1.YEAR AND ";
        $query .= "           ST3.COURSECD = ST1.COURSECD AND ";
        $query .= "           ST3.MAJORCD = ST1.MAJORCD AND ";
        $query .= "           ST3.GRADE = ST1.GRADE AND ";
        $query .= "           ST3.COURSECODE = ST1.COURSECODE AND ";
        $query .= "           ST3.CLASSCD = SUBSTR(ST3.SUBCLASSCD,1,2) AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 ST3.CLASSCD || '-' || ST3.SCHOOL_KIND || '-' || ST3.CURRICULUM_CD || '-' || ST3.SUBCLASSCD ";
            $query .= "                         NOT IN (SELECT ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
            $query .= "                                 FROM SUBCLASS_REPLACE_DAT ";
            $query .= "                                 WHERE REPLACECD = '1' ";
            $query .= "                                       AND YEAR = '".CTRL_YEAR."' ";
            $query .= "                                       AND ANNUAL = '" .$grade ."') ) ST4 ";
        } else {
            $query .= "                 ST3.SUBCLASSCD NOT IN (SELECT ATTEND_SUBCLASSCD ";
            $query .= "                                        FROM SUBCLASS_REPLACE_DAT ";
            $query .= "                                        WHERE REPLACECD = '1' ";
            $query .= "                                              AND YEAR = '".CTRL_YEAR."' ";
            $query .= "                                              AND ANNUAL = '" .$grade ."') ) ST4 ";
        }
        $query .= " WHERE ";
        $query .= "     SUBSTR(ST4.SUBCLASSCD,1,2) <> '90' AND (ST4.ASSESS = '1' OR ST4.ASSESS IS NULL) ";
        $query .= " GROUP BY ";
        $query .= "     ST4.SCHREGNO ";

        return $query;
    }

    function &getUpdateQuery($model)
    {
        list($grade, $hr_class) = explode(",", $model->gc_select);

        $db = Query::dbCheckOut();

        $schoolKind = $db->getOne("SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '".CTRL_YEAR."' AND GRADE = '{$grade}'");

        $db->autoCommit(false);
        switch($model->trans){
            case 1://進級
            case 3://留年
            case 4://修了
            case 9://取消
                $query  = " DELETE FROM ";
                $query .= "   CLASS_FORMATION_DAT T1 ";
                $query .= " WHERE ";
                $query .= "   T1.SCHREGNO IN ('" .implode($model->checked, "','") ."') AND ";
                $query .= "   (T1.YEAR,T1.SEMESTER) IN (SELECT ";
                $query .= "                         YEAR, ";
                $query .= "                         MIN(SEMESTER) ";
                $query .= "                       FROM ";
                $query .= "                         SEMESTER_MST ";
                $query .= "                       WHERE ";
                $query .= "                         YEAR = '" .((int) CTRL_YEAR+1) ."' ";
                $query .= "                       GROUP BY ";
                $query .= "                         YEAR ";
                $query .= "                       ) AND ";
                $query .= "   NOT EXISTS(SELECT ";
                $query .= "               * ";
                $query .= "             FROM ";
                $query .= "               SCHREG_BASE_MST W ";
                $query .= "             WHERE ";
                $query .= "               W.SCHREGNO = T1.SCHREGNO AND ";
                $query .= "               W.GRD_DIV  = '1' ";
                $query .= "             ) ";

                $result = $db->query($query);

                $query  = " DELETE FROM ";
                $query .= "   SCHREG_BASE_YEAR_DETAIL_MST T1 ";
                $query .= " WHERE ";
                $query .= "   T1.SCHREGNO IN ('" .implode($model->checked, "','") ."') ";
                $query .= "   AND T1.YEAR = '".CTRL_YEAR."' ";
                $query .= "   AND T1.BASE_SEQ = '007' ";

                $result = $db->query($query);

                //取消でチェックされている生徒が「卒業」の場合
                if ($model->trans == 3 || $model->trans == 9){
                    $query  = " UPDATE ";
                    $query .= "   SCHREG_BASE_MST ";
                    $query .= " SET ";
                    $query .= "   (GRD_DIV, GRD_NO, GRD_DATE) = (NULL,NULL,NULL) ";
                    $query .= " WHERE ";
                    $query .= "   SCHREGNO IN ('" .implode($model->checked, "','") ."') ";
                    if ($model->isIkkanUseSchKind && $model->field["GRADUATE"] == "1" && $model->field["MAX_SCHOOLKIND"] != "1") {
                        $query .= "   AND GRD_DIV IN ('1', '5') ";
                    } else {
                        $query .= "   AND GRD_DIV = '1' ";
                    }

                    $result = $db->query($query);

                    $query  = " UPDATE ";
                    $query .= "   SCHREG_ENT_GRD_HIST_DAT ";
                    $query .= " SET ";
                    $query .= "   (GRD_DIV, GRD_NO, GRD_DATE) = (NULL,NULL,NULL) ";
                    $query .= " WHERE ";
                    $query .= "   SCHREGNO IN ('" .implode($model->checked, "','") ."') ";
                    $query .= "   AND SCHOOL_KIND = '{$schoolKind}' ";
                    if ($model->isIkkanUseSchKind && $model->field["GRADUATE"] == "1" && $model->field["MAX_SCHOOLKIND"] != "1") {
                        $query .= "   AND GRD_DIV IN ('1', '5') ";
                    } else {
                        $query .= "   AND GRD_DIV = '1' ";
                    }

                    $result = $db->query($query);
                }
                if ($model->trans == 1 || $model->trans == 3 || $model->trans == 4){
                    $query  = " INSERT INTO CLASS_FORMATION_DAT( ";
                    $query .= " SCHREGNO, ";
                    $query .= " YEAR, ";
                    $query .= " SEMESTER, ";
                    $query .= " GRADE, ";
                    $query .= " OLD_GRADE, ";
                    $query .= " OLD_HR_CLASS, ";
                    $query .= " OLD_ATTENDNO, ";
                    $query .= " COURSECD, ";
                    $query .= " MAJORCD, ";
                    $query .= " COURSECODE, ";
                    $query .= " REMAINGRADE_FLG, ";
                    $query .= " REGISTERCD ";
                    $query .= " ) ";

                    $query .= " WITH GRADE_LIST AS ( ";
                    $query .= "     SELECT ";
                    if ($model->A023Cnt > 1) {
                        $query .= "         ROW_NUMBER() OVER(ORDER BY N1.ABBV2, T1.GRADE) AS ROW, ";
                        $query .= "         N1.ABBV2, ";
                    } else {
                        $query .= "         ROW_NUMBER() OVER(ORDER BY VALUE(N1.ABBV2, '1'), T1.GRADE) AS ROW, ";
                        $query .= "         '1' AS ABBV2, ";
                    }
                    $query .= "         T1.GRADE ";
                    $query .= "     FROM ";
                    $query .= "         SCHREG_REGD_GDAT T1, ";
                    $query .= "         V_NAME_MST N1 ";
                    $query .= "     WHERE ";
                    $query .= "         T1.YEAR         = N1.YEAR AND ";
                    $query .= "         T1.YEAR         = '".CTRL_YEAR."' AND ";
                    $query .= "         N1.NAMECD1      = 'A023' AND ";
                    $query .= "         T1.SCHOOL_KIND  = N1.NAME1 AND ";
                    $query .= "         T1.GRADE BETWEEN N1.NAME2 AND N1.NAME3 ";
                    $query .= " ), GRADE_T AS ( ";
                    $query .= "     SELECT ";
                    $query .= "         T1.ROW, ";
                    $query .= "         T1.GRADE AS OLD_GRADE, ";
                    $query .= "         L1.GRADE ";
                    $query .= "     FROM ";
                    $query .= "         GRADE_LIST T1 ";
                    $query .= "         LEFT JOIN GRADE_LIST L1 ON INTEGER(T1.ROW) = (INTEGER(L1.ROW) - 1) ";
                    $query .= " ) ";

                    $query .= "   (SELECT ";
                    $query .= "     ST1.SCHREGNO, ";
                    $query .= "     CHAR(INTEGER(ST1.YEAR) + 1) AS YEAR, ";
                    $query .= "     '1', ";
                    if ($model->trans == 1 || $model->trans == 4){      //進級・修了
                        //単位制かつ学年upなし
                        if ($model->school["SCHOOLDIV"] == "1" && $model->Properties["useKeepGrade"] == "1") {
                            $query .= "     ST1.GRADE, ";
                        } else {
                            $query .= "     L1.GRADE AS GRADE, ";
                        }
                    }else{
                        $query .= "     ST1.GRADE, ";
                    }
                    $query .= "     ST1.GRADE, ";
                    $query .= "     ST1.HR_CLASS, ";
                    $query .= "     ST1.ATTENDNO, ";
                    $query .= "     ST1.COURSECD, ";
                    $query .= "     ST1.MAJORCD, ";
                    $query .= "     ST1.COURSECODE, ";
                    if ($model->trans == 3){        //留年
                        $query .= "     '1',";
                    }else{
                        $query .= "     '0',";
                    }
                    $query .= "     '".STAFFCD ."' ";
                    $query .= "   FROM ";
                    $query .= "     SCHREG_REGD_DAT ST1 ";
                    $query .= "     LEFT JOIN GRADE_T L1 ON ST1.GRADE = L1.OLD_GRADE ";
                    $query .= "   WHERE ";
                    $query .= "     ST1.YEAR = '" .CTRL_YEAR ."' AND ";
                    $query .= "     ST1.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
                    $query .= "     ST1.SCHREGNO IN ('" .implode($model->checked, "','") ."') AND ";
                    $query .= "     NOT EXISTS(SELECT ";
                    $query .= "               * ";
                    $query .= "             FROM ";
                    $query .= "               SCHREG_BASE_MST W ";
                    $query .= "             WHERE ";
                    $query .= "               W.SCHREGNO = ST1.SCHREGNO AND ";
                    $query .= "               W.GRD_DIV  = '1' ";
                    $query .= "             ) ";
                    $query .= "   ) ";

                    $result = $db->query($query);
                }
                if ($model->trans == 1 && $model->sinkyuSyuuryouFlg == '1') {
                    $query  = " INSERT INTO SCHREG_BASE_YEAR_DETAIL_MST ( ";
                    $query .= " SCHREGNO, ";
                    $query .= " YEAR, ";
                    $query .= " BASE_SEQ, ";
                    $query .= " BASE_REMARK1, ";
                    $query .= " REGISTERCD, ";
                    $query .= " UPDATED ";
                    $query .= " ) ";
                    $query .= "   (SELECT ";
                    $query .= "     ST1.SCHREGNO, ";
                    $query .= "     '".CTRL_YEAR."' AS YEAR, ";
                    $query .= "     '007', ";
                    $query .= "     '1', ";
                    $query .= "     '".STAFFCD ."', ";
                    $query .= "     sysdate() ";
                    $query .= "   FROM ";
                    $query .= "     SCHREG_REGD_DAT ST1 ";
                    $query .= "   WHERE ";
                    $query .= "     ST1.YEAR = '" .CTRL_YEAR ."' AND ";
                    $query .= "     ST1.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
                    $query .= "     ST1.SCHREGNO IN ('" .implode($model->checked, "','") ."') ";
                    $query .= "   ) ";

                    $result = $db->query($query);
                }
                if ($model->trans == 4) {
                    $query  = " UPDATE ";
                    $query .= "   SCHREG_BASE_MST ";
                    $query .= " SET ";
                    $query .= "   (GRD_DATE,GRD_DIV) = (SELECT ";
                    $query .= "                           ST1.GRADUATE_DATE, ";
                    $query .= "                           '5' ";
                    $query .= "                         FROM ";
                    $query .= "                           SCHOOL_MST ST1 ";
                    $query .= "                         WHERE ";
                    $query .= "                           ST1.YEAR = '" .CTRL_YEAR ."' ";
                    if ($model->Properties["useSchool_KindField"] == "1") {
                        $query .= "                       AND ST1.SCHOOL_KIND = '" . $model->schoolKind[$model->gc_select]."' ";
                    }
                    $query .= "                         ) ";
                    $query .= " WHERE ";
                    $query .= "   SCHREGNO IN ('" .implode($model->checked, "','") ."') ";

                    $result = $db->query($query);

                    foreach ($model->checked as $val) {
                        $egCnt = $db->getOne("SELECT COUNT(*) FROM SCHREG_ENT_GRD_HIST_DAT WHERE SCHREGNO = '{$val}' AND SCHOOL_KIND = '{$schoolKind}'");
                        if ($egCnt > 0) {
                            $query  = " UPDATE ";
                            $query .= "   SCHREG_ENT_GRD_HIST_DAT ";
                            $query .= " SET ";
                            $query .= "   (GRD_DATE, GRD_DIV) = (SELECT ";
                            $query .= "                              ST1.GRADUATE_DATE, ";
                            $query .= "                              '5' ";
                            $query .= "                          FROM ";
                            $query .= "                              SCHOOL_MST ST1 ";
                            $query .= "                          WHERE ";
                            $query .= "                              ST1.YEAR = '" .CTRL_YEAR ."' ";
                            if ($model->Properties["useSchool_KindField"] == "1") {
                                $query .= "                       AND ST1.SCHOOL_KIND = '" . $model->schoolKind[$model->gc_select]."' ";
                            }
                            $query .= "                         ) ";
                            $query .= " WHERE ";
                            $query .= "   SCHREGNO = '{$val}' ";
                            $query .= "   AND SCHOOL_KIND = '{$schoolKind}' ";
                        } else {
                            $query  = " INSERT INTO ";
                            $query .= "   SCHREG_ENT_GRD_HIST_DAT ";
                            $query .= "   (SCHREGNO, SCHOOL_KIND, GRD_DATE, GRD_DIV) ( ";
                            $query .= "                          SELECT ";
                            $query .= "                              '{$val}' AS SCHREGNO, ";
                            $query .= "                              '{$schoolKind}' AS SCHOOL_KIND, ";
                            $query .= "                              ST1.GRADUATE_DATE, ";
                            $query .= "                              '5' ";
                            $query .= "                          FROM ";
                            $query .= "                              SCHOOL_MST ST1 ";
                            $query .= "                          WHERE ";
                            $query .= "                              ST1.YEAR = '" .CTRL_YEAR ."' ";
                            if ($model->Properties["useSchool_KindField"] == "1") {
                                $query .= "                       AND ST1.SCHOOL_KIND = '" . $model->schoolKind[$model->gc_select]."' ";
                            }
                            $query .= "                         ) ";
                        }
                        $result = $db->query($query);
                    }
                }

                break;
            case 2://卒業
                $query  = " DELETE FROM ";
                $query .= "   CLASS_FORMATION_DAT T1 ";
                $query .= " WHERE ";
                $query .= "   T1.SCHREGNO IN ('" .implode($model->checked, "','") ."') AND ";
                $query .= "   (T1.YEAR,T1.SEMESTER) IN (SELECT ";
                $query .= "                         YEAR, ";
                $query .= "                         MIN(SEMESTER) ";
                $query .= "                       FROM ";
                $query .= "                         SEMESTER_MST ";
                $query .= "                       WHERE ";
                $query .= "                         YEAR = '" .((int) CTRL_YEAR+1) ."' ";
                $query .= "                       GROUP BY ";
                $query .= "                         YEAR ";
                $query .= "                       ) ";

                $result = $db->query($query);

                $query  = " UPDATE ";
                $query .= "   SCHREG_BASE_MST ";
                $query .= " SET ";
                $query .= "   (GRD_DATE,GRD_DIV) = (SELECT ";
                $query .= "                           ST1.GRADUATE_DATE, ";
                $query .= "                           '1' ";
                $query .= "                         FROM ";
                $query .= "                           SCHOOL_MST ST1 ";
                $query .= "                         WHERE ";
                $query .= "                           ST1.YEAR = '" .CTRL_YEAR ."' ";
                if ($model->Properties["useSchool_KindField"] == "1") {
                    $query .= "                       AND ST1.SCHOOL_KIND = '" . $model->schoolKind[$model->gc_select]."' ";
                }
                $query .= "                         ) ";
                $query .= " WHERE ";
                $query .= "   SCHREGNO IN ('" .implode($model->checked, "','") ."') ";

                $result = $db->query($query);

                foreach ($model->checked as $val) {
                    $egCnt = $db->getOne("SELECT COUNT(*) FROM SCHREG_ENT_GRD_HIST_DAT WHERE SCHREGNO = '{$val}' AND SCHOOL_KIND = '{$schoolKind}'");
                    if ($egCnt > 0) {
                        $query  = " UPDATE ";
                        $query .= "   SCHREG_ENT_GRD_HIST_DAT ";
                        $query .= " SET ";
                        $query .= "   (GRD_DATE, GRD_DIV) = (SELECT ";
                        $query .= "                              ST1.GRADUATE_DATE, ";
                        $query .= "                              '1' ";
                        $query .= "                          FROM ";
                        $query .= "                              SCHOOL_MST ST1 ";
                        $query .= "                          WHERE ";
                        $query .= "                              ST1.YEAR = '" .CTRL_YEAR ."' ";
                        if ($model->Properties["useSchool_KindField"] == "1") {
                            $query .= "                       AND ST1.SCHOOL_KIND = '" . $model->schoolKind[$model->gc_select]."' ";
                        }
                        $query .= "                         ) ";
                        $query .= " WHERE ";
                        $query .= "   SCHREGNO = '{$val}' ";
                        $query .= "   AND SCHOOL_KIND = '{$schoolKind}' ";
                    } else {
                        $query  = " INSERT INTO ";
                        $query .= "   SCHREG_ENT_GRD_HIST_DAT ";
                        $query .= "   (SCHREGNO, SCHOOL_KIND, GRD_DATE, GRD_DIV) ( ";
                        $query .= "                          SELECT ";
                        $query .= "                              '{$val}' AS SCHREGNO, ";
                        $query .= "                              '{$schoolKind}' AS SCHOOL_KIND, ";
                        $query .= "                              ST1.GRADUATE_DATE, ";
                        $query .= "                              '1' ";
                        $query .= "                          FROM ";
                        $query .= "                              SCHOOL_MST ST1 ";
                        $query .= "                          WHERE ";
                        $query .= "                              ST1.YEAR = '" .CTRL_YEAR ."' ";
                        if ($model->Properties["useSchool_KindField"] == "1") {
                            $query .= "                       AND ST1.SCHOOL_KIND = '" . $model->schoolKind[$model->gc_select]."' ";
                        }
                        $query .= "                         ) ";
                    }
                    $result = $db->query($query);
                }

        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
