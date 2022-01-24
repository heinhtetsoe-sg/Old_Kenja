<?php

require_once('for_php7.php');

class knja031Query extends Query
{
    //年、組コンボボックス
    public function gradeClassQuery()
    {
        $query = " SELECT ";
        $query .= "   T1.GRADE || ',' || T1.HR_CLASS AS GC, ";
        $query .= "   T1.HR_NAME, ";
        $query .= "   T2.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_HDAT T1 LEFT OUTER JOIN  ";
        $query .= "   STAFF_MST T2 ON T1.TR_CD1 = T2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR  = '" .CTRL_YEAR ."' AND ";
        $query .= "   T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "   GC ";

        return $query;
    }
    //学校マスタ
    public function selectSchoolMst()
    {
        $query = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR          = '" .CTRL_YEAR ."'";

        return $query;
    }
    //次年度の学期マスタが存在するか
    public function selectSemesterMst()
    {
        $query = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR          = '" .((int)CTRL_YEAR + 1) ."' ";

        return $query;
    }
    //コンボボックスより、生徒を検索
    public function readQuery($model)
    {
        list($grade, $hr_class) = explode(",", $model->gc_select);

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
        /* 2004/12/16 arakaki
                $query .= "   T4.GET_CREDITS, ";
                $query .= "   T5.REM_CREDITS ";
        */
        $query .= "   COALESCE(T4.GET_CREDITS, 0) AS GET_CREDITS, ";
        $query .= "   COALESCE(T5.REM_CREDITS, 0) AS REM_CREDITS ";
#        $query .= "   COALESCE(T7.ABROAD_CREDITS, 0) AS ABROAD_CREDITS ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 LEFT OUTER JOIN (SELECT ";
        $query .= "           ST4.SCHREGNO, ";
        $query .= "           SUM(COALESCE(ST4.CREDITS, 0)) AS GET_CREDITS ";
        $query .= "         FROM ";
        $query .= "               (SELECT ";
        $query .= "                     ST1.SCHREGNO, ";
        $query .= "                     CASE WHEN ST2.JUDGE_PATTERN = 'A' THEN ST2.A_PATTERN_ASSESS ";
        $query .= "                          WHEN ST2.JUDGE_PATTERN = 'B' THEN ST2.B_PATTERN_ASSESS ";
        $query .= "                          WHEN ST2.JUDGE_PATTERN = 'C' THEN ST2.C_PATTERN_ASSESS ";
        $query .= "                     END AS ASSESS, ";
        $query .= "                     ST3.CREDITS, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     ST2.CLASSCD, ";
            $query .= "                     ST2.SCHOOL_KIND, ";
            $query .= "                     ST2.CURRICULUM_CD, ";
        }
        $query .= "                     ST2.SUBCLASSCD, ";    //NO002
        $query .= "                     ST2.GRADE_RECORD ";    //NO003
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT AS ST1 ";
        $query .= "                     INNER JOIN KIN_RECORD_DAT ST2 ON ";
        $query .= "                         ST2.YEAR = ST1.YEAR AND ";
        $query .= "                         ST2.SCHREGNO = ST1.SCHREGNO ";
        $query .= "                     INNER JOIN CREDIT_MST ST3 ON ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         ST2.CLASSCD = ST3.CLASSCD AND ";
            $query .= "                         ST2.SCHOOL_KIND = ST3.SCHOOL_KIND AND ";
            $query .= "                         ST2.CURRICULUM_CD = ST3.CURRICULUM_CD AND ";
        }
        $query .= "                         ST2.SUBCLASSCD = ST3.SUBCLASSCD AND ";
        $query .= "                         ST3.YEAR = ST1.YEAR AND ";
        $query .= "                         ST3.COURSECD = ST1.COURSECD AND ";
        $query .= "                         ST3.MAJORCD = ST1.MAJORCD AND ";
        $query .= "                         ST3.GRADE = ST1.GRADE AND ";
        $query .= "                         ST3.COURSECODE = ST1.COURSECODE ";
        $query .= "                 WHERE ";
        $query .= "                     ST1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                     ST1.SEMESTER = '".CTRL_SEMESTER."'AND ";
        $query .= "                     ST3.COURSECODE = ST1.COURSECODE AND ";
        $query .= "                     ST3.CLASSCD = SUBSTR(ST3.SUBCLASSCD,1,2) AND ";                        //NO002
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     ST3.CLASSCD || '-' || ST3.SCHOOL_KIND || '-' || ST3.CURRICULUM_CD || '-' || ST3.SUBCLASSCD ";
            $query .= "                             NOT IN (SELECT ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
            $query .= "                                     FROM SUBCLASS_REPLACE_COMBINED_DAT ";
            $query .= "                                     WHERE REPLACECD = '1' ";
            $query .= "                                       AND YEAR = '".CTRL_YEAR."') ";
        } else {
            $query .= "                     ST3.SUBCLASSCD NOT IN (SELECT ATTEND_SUBCLASSCD ";                  //NO002
            $query .= "                                            FROM SUBCLASS_REPLACE_COMBINED_DAT ";                    //NO002
            $query .= "                                            WHERE REPLACECD = '1' ";                     //NO002
            $query .= "                                                  AND YEAR = '".CTRL_YEAR."') ";  //NO002
        }
        $query .= "             ) ST4 ";
        $query .= "             LEFT JOIN V_NAME_MST D065 ON ";
        $query .= "                 D065.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                 D065.NAMECD1 = 'D065' AND ";
        $query .= "                 D065.NAME1 = ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     ST4.CLASSCD || '-' || ST4.SCHOOL_KIND || '-' || ST4.CURRICULUM_CD || '-' || ST4.SUBCLASSCD ";
        } else {
            $query .= "                     ST4.SUBCLASSCD ";
        }
        $query .= "         WHERE ";
        $query .= "           (ST4.ASSESS <> '1' AND ST4.ASSESS <> '0' AND ST4.ASSESS IS NOT NULL) OR ";
        $query .= "           ((SUBSTR(ST4.SUBCLASSCD,1,2) = '90' OR D065.YEAR IS NOT NULL) AND VALUE(ST4.GRADE_RECORD,0) IN (11,22,33)) ";    //NO002 NO003
        $query .= "         GROUP BY ";
        $query .= "           ST4.SCHREGNO ";
        $query .= "         ) T4 ON T1.SCHREGNO = T4.SCHREGNO ";
        $query .= " LEFT OUTER JOIN (SELECT ";
        $query .= "           ST4.SCHREGNO, ";
        $query .= "           SUM(COALESCE(ST4.CREDITS, 0)) AS REM_CREDITS ";
        $query .= "         FROM ";
        $query .= "               (SELECT ";
        $query .= "                     ST1.SCHREGNO, ";
        $query .= "                     CASE WHEN ST2.JUDGE_PATTERN = 'A' THEN ST2.A_PATTERN_ASSESS ";
        $query .= "                          WHEN ST2.JUDGE_PATTERN = 'B' THEN ST2.B_PATTERN_ASSESS ";
        $query .= "                          WHEN ST2.JUDGE_PATTERN = 'C' THEN ST2.C_PATTERN_ASSESS ";
        $query .= "                     END AS ASSESS, ";
        $query .= "                     ST3.CREDITS, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     ST2.CLASSCD, ";
            $query .= "                     ST2.SCHOOL_KIND, ";
            $query .= "                     ST2.CURRICULUM_CD, ";
        }
        $query .= "                     ST2.SUBCLASSCD, ";    //NO003
        $query .= "                     ST2.GRADE_RECORD ";    //NO003
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT AS ST1 ";
        $query .= "                     INNER JOIN KIN_RECORD_DAT ST2 ON ";
        $query .= "                         ST2.YEAR = ST1.YEAR AND ";
        $query .= "                         ST2.SCHREGNO = ST1.SCHREGNO ";
        $query .= "                     INNER JOIN CREDIT_MST ST3 ON ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         ST2.CLASSCD = ST3.CLASSCD AND ";
            $query .= "                         ST2.SCHOOL_KIND = ST3.SCHOOL_KIND AND ";
            $query .= "                         ST2.CURRICULUM_CD = ST3.CURRICULUM_CD AND ";
        }
        $query .= "                         ST2.SUBCLASSCD = ST3.SUBCLASSCD AND ";
        $query .= "                         ST3.YEAR = ST1.YEAR AND ";
        $query .= "                         ST3.COURSECD = ST1.COURSECD AND ";
        $query .= "                         ST3.MAJORCD = ST1.MAJORCD AND ";
        $query .= "                         ST3.GRADE = ST1.GRADE AND ";
        $query .= "                         ST3.COURSECODE = ST1.COURSECODE ";
        $query .= "                     LEFT JOIN V_NAME_MST D065 ON ";
        $query .= "                         D065.YEAR = ST1.YEAR AND ";
        $query .= "                         D065.NAMECD1 = 'D065' AND ";
        $query .= "                         D065.NAME1 = ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         ST2.CLASSCD || '-' || ST2.SCHOOL_KIND || '-' || ST2.CURRICULUM_CD || '-' || ST2.SUBCLASSCD ";
        } else {
            $query .= "                         ST2.SUBCLASSCD ";
        }
        $query .= "                 WHERE ";
        $query .= "                     ST1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                     ST1.SEMESTER = '".CTRL_SEMESTER."'AND ";
        $query .= "                     ST3.CLASSCD = SUBSTR(ST3.SUBCLASSCD,1,2) AND ";                        //NO002
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     ST3.CLASSCD || '-' || ST3.SCHOOL_KIND || '-' || ST3.CURRICULUM_CD || '-' || ST3.SUBCLASSCD ";
            $query .= "                             NOT IN (SELECT ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
            $query .= "                                     FROM SUBCLASS_REPLACE_COMBINED_DAT ";
            $query .= "                                     WHERE REPLACECD = '1' ";
            $query .= "                                           AND YEAR = '".CTRL_YEAR."') ";
        } else {
            $query .= "                     ST3.SUBCLASSCD NOT IN (SELECT ATTEND_SUBCLASSCD ";                    //NO002
            $query .= "                                            FROM SUBCLASS_REPLACE_COMBINED_DAT ";                    //NO002
            $query .= "                                            WHERE REPLACECD = '1' ";                        //NO002
            $query .= "                                                  AND YEAR = '".CTRL_YEAR."') ";    //NO002
        }
        $query .= "                     AND D065.YEAR IS NULL ";  // 記号評価科目は対象外
        $query .= "                     ) ST4 ";    //NO002
        $query .= "         WHERE ";
        $query .= "           SUBSTR(ST4.SUBCLASSCD,1,2) <> '90' AND (ST4.ASSESS = '1' OR ST4.ASSESS = '0' OR ST4.ASSESS IS NULL) ";
//        $query .= "           (SUBSTR(ST4.SUBCLASSCD,1,2) = '90' AND VALUE(ST4.GRADE_RECORD,0) NOT IN (11,22,33)) ";    //NO003
        $query .= "         GROUP BY ";
        $query .= "           ST4.SCHREGNO ";
        $query .= "         ) T5 ON T1.SCHREGNO = T5.SCHREGNO ";
#        $query .= "           ST1.SCHREGNO, ";
#        $query .= "           SUM(COALESCE(ST1.GET_CREDIT, 0)) AS GET_CREDITS, ";
#        $query .= "           SUM(COALESCE(ST1.ADD_CREDIT, 0)) AS ADD_CREDITS ";
#        $query .= "         FROM ";
#        $query .= "           RECORDGRADE_DAT ST1 ";
#        if ($model->school["SCHOOLDIV"] == 0 ){     //学年制
#            $query .= "         WHERE ";
#            $query .= "           ST1.YEAR = '" .CTRL_YEAR ."' ";
#        }
#        $query .= "         GROUP BY ";
#        $query .= "           ST1.SCHREGNO ";
#        $query .= "         ) T4 ON T1.SCHREGNO = T4.SCHREGNO LEFT OUTER JOIN (SELECT ";
#        $query .= "         ST2.SCHREGNO, ";
#        $query .= "         SUM(COALESCE(ST3.CREDITS, 0)) AS REM_CREDITS ";
#        $query .= "       FROM ";
#        $query .= "         SCHREG_REGD_DAT ST1, ";
#        $query .= "         RECORDGRADE_DAT ST2, ";
#        $query .= "         CREDIT_MST ST3 ";
#        $query .= "       WHERE ";
#        $query .= "         ST1.SCHREGNO = ST2.SCHREGNO AND ";
#        $query .= "         ST2.VALUATION = 1 AND ";
#        $query .= "         ST1.YEAR = '" .CTRL_YEAR ."' AND ";
#        $query .= "         ST1.YEAR = ST3.YEAR AND ";
#        $query .= "         ST1.SEMESTER = '".$model->control_data["学期数"]."' AND ";
#        if ($model->school["SCHOOLDIV"] == 0 ){     //学年制
#            $query .= "         ST2.YEAR = ST1.YEAR AND ";
#        }
#        $query .= "         ST3.GRADE = ST1.GRADE AND ";
#        $query .= "         ST3.COURSECD = ST1.COURSECD AND ";
#        $query .= "         ST3.MAJORCD = ST1.MAJORCD AND ";
#        $query .= "         ST3.COURSECODE = ST1.COURSECODE AND ";
#        $query .= "         ST3.CLASSCD = SUBSTR(ST2.GRADINGCLASSCD,1,2) AND ";
#        $query .= "         ST3.SUBCLASSCD = ST2.GRADINGCLASSCD ";
#        $query .= "       GROUP BY ";
#        $query .= "         ST2.SCHREGNO ";
#        $query .= "       ) T5 ON T1.SCHREGNO = T5.SCHREGNO  ";
        $query .= "   LEFT OUTER JOIN SCHREG_TRANSFER_DAT T6  ";
        $query .= "   ON T1.SCHREGNO = T6.SCHREGNO AND  ";
        $query .= "   DATE('".str_replace("/", "-", CTRL_DATE)."') BETWEEN T6.TRANSFER_SDATE AND T6.TRANSFER_EDATE  ";
#        $query .= "   LEFT OUTER JOIN ( SELECT ";
#        $query .= "   ST1.SCHREGNO, ";
#        $query .= "   SUM(COALESCE(ST1.ABROAD_CREDITS, 0)) AS ABROAD_CREDITS ";
#        $query .= " FROM ";
#        $query .= "   SCHREG_TRANSFER_DAT ST1 ";
#        if ($model->school["SCHOOLDIV"] == 0 ){     //学年制
#            $query .= " WHERE ";
#            $query .= "   FISCALYEAR(ST1.TRANSFER_SDATE) = '" .CTRL_YEAR ."' ";
#        }
#        $query .= " GROUP BY ";
#        $query .= "   ST1.SCHREGNO ";
#        $query .= " ) T7 ON T1.SCHREGNO = T7.SCHREGNO ";
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
        $query .= "   ((T3.GRD_DIV IS NOT NULL AND T3.GRD_DIV NOT IN ('2','3')) OR ";
        $query .= "   (T3.GRD_DIV IS NULL OR T3.GRD_DIV = '')) ";
        if (is_array($model->sort)) {
            $query .= " ORDER BY ";
            foreach ($model->sort as $field => $order) {
                switch ($field) {
                    case "ATTENDNO":    //番号順
                        $query .= "T2.HR_NAMEABBV,INTEGER(T1.ATTENDNO) ";
                        $query .= ($order > 0)? "ASC " : "DESC ";
                        break 2;
                    case "TARGET":       //席次順
                        if ($order > 0) {
                            $query .= "T8.REMAINGRADE_FLG,T3.GRD_DIV DESC,T2.HR_NAMEABBV,INTEGER(T1.ATTENDNO) ";
                        } else {
                            $query .= "T8.REMAINGRADE_FLG,T3.GRD_DIV,T2.HR_NAMEABBV,INTEGER(T1.ATTENDNO) ";
                        }
                        break 2;
                }
            }
        } else {
            $query .= "ORDER BY ";
            $query .= "    T2.HR_NAMEABBV, ";
            $query .= "    INTEGER(T1.ATTENDNO) ASC ";
        }

        return $query;
    }

    public function &getUpdateQuery($model)
    {
        list($grade, $hr_class) = explode(",", $model->gc_select);

        $db = Query::dbCheckOut();

        $schoolKind = $db->getOne("SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '".CTRL_YEAR."' AND GRADE = '{$grade}'");

        $db->autoCommit(false);
        switch ($model->trans) {
            case 1://進級
            case 3://留年
            case 9://取消
                $query = " DELETE FROM ";
                $query .= "   CLASS_FORMATION_DAT T1 ";
                $query .= " WHERE ";
                $query .= "   T1.SCHREGNO IN ('" .implode($model->checked, "','") ."') AND ";
                $query .= "   (T1.YEAR,T1.SEMESTER) IN (SELECT ";
                $query .= "                         YEAR, ";
                $query .= "                         MIN(SEMESTER) ";
                $query .= "                       FROM ";
                $query .= "                         SEMESTER_MST ";
                $query .= "                       WHERE ";
                $query .= "                         YEAR = '" .((int)CTRL_YEAR+1) ."' ";
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
                //取消でチェックされている生徒が「卒業」の場合
                if ($model->trans == 3 || $model->trans == 9) {
                    $query = " UPDATE ";
                    $query .= "   SCHREG_BASE_MST ";
                    $query .= " SET ";
                    $query .= "   (GRD_DIV, GRD_NO, GRD_DATE) = (NULL,NULL,NULL) ";
                    $query .= " WHERE ";
                    $query .= "   SCHREGNO IN ('" .implode($model->checked, "','") ."') AND ";
                    $query .= "   GRD_DIV = '1' ";

                    $result = $db->query($query);

                    $query  = " UPDATE ";
                    $query .= "   SCHREG_ENT_GRD_HIST_DAT ";
                    $query .= " SET ";
                    $query .= "   (GRD_DATE, GRD_DIV) = (NULL, NULL) ";
                    $query .= " WHERE ";
                    $query .= "   SCHREGNO IN ('" .implode($model->checked, "','") ."') ";
                    $query .= "   AND SCHOOL_KIND = '{$schoolKind}' ";

                    $result = $db->query($query);
                }
                if ($model->trans == 1 || $model->trans == 3) {
                    $query = " INSERT INTO CLASS_FORMATION_DAT( ";
                    $query .= " SCHREGNO, ";
                    $query .= " YEAR, ";
                    $query .= " SEMESTER, ";
                    $query .= " GRADE, ";
                    $query .= " OLD_SCHREGNO, ";    //NO001
                    $query .= " OLD_GRADE, ";
                    $query .= " OLD_HR_CLASS, ";
                    $query .= " OLD_ATTENDNO, ";
                    $query .= " COURSECD, ";
                    $query .= " MAJORCD, ";
                    $query .= " COURSECODE, ";
                    $query .= " REMAINGRADE_FLG, ";
                    $query .= " REGISTERCD ";
                    $query .= " ) ";
                    $query .= "   (SELECT ";
                    $query .= "     ST1.SCHREGNO, ";
                    $query .= "     CHAR(INTEGER(ST1.YEAR) + 1) AS YEAR, ";
                    $query .= "     '1', ";
                    if ($model->trans == 1) {        //進級
                        $query .= "     SUBSTR(DIGITS(INTEGER(ST1.GRADE)+1),9,2)  AS GRADE, ";
                    } else {
                        $query .= "     ST1.GRADE, ";
                    }
                    $query .= "     ST1.SCHREGNO, ";    //NO001
                    $query .= "     ST1.GRADE, ";
                    $query .= "     ST1.HR_CLASS, ";
                    $query .= "     ST1.ATTENDNO, ";
                    $query .= "     ST1.COURSECD, ";
                    $query .= "     ST1.MAJORCD, ";
                    $query .= "     ST1.COURSECODE, ";
                    if ($model->trans == 3) {        //留年
                        $query .= "     '1',";
                    } else {
                        $query .= "     '0',";
                    }
                    $query .= "     '".STAFFCD ."' ";
                    $query .= "   FROM ";
                    $query .= "     SCHREG_REGD_DAT ST1 ";
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
                break;
            case 2://卒業
                $query = " DELETE FROM ";
                $query .= "   CLASS_FORMATION_DAT T1 ";
                $query .= " WHERE ";
                $query .= "   T1.SCHREGNO IN ('" .implode($model->checked, "','") ."') AND ";
                $query .= "   (T1.YEAR,T1.SEMESTER) IN (SELECT ";
                $query .= "                         YEAR, ";
                $query .= "                         MIN(SEMESTER) ";
                $query .= "                       FROM ";
                $query .= "                         SEMESTER_MST ";
                $query .= "                       WHERE ";
                $query .= "                         YEAR = '" .((int)CTRL_YEAR+1) ."' ";
                $query .= "                       GROUP BY ";
                $query .= "                         YEAR ";
                $query .= "                       ) ";

                $result = $db->query($query);

                $query = " UPDATE ";
                $query .= "   SCHREG_BASE_MST ";
                $query .= " SET ";
                $query .= "   (GRD_DATE,GRD_DIV) = (SELECT ";
                $query .= "                           ST1.GRADUATE_DATE, ";
                $query .= "                           '1' ";
                $query .= "                         FROM ";
                $query .= "                           SCHOOL_MST ST1 ";
                $query .= "                         WHERE ";
                $query .= "                           ST1.YEAR = '" .CTRL_YEAR ."' ";
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
