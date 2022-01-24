<?php

require_once('for_php7.php');

class knjb0058Query extends Query {

    //学期を取得
    function getSemester($model) {
        $query .= " SELECT ";
        $query .= "     YEAR || ':' || SEMESTER AS VALUE, ";
        $query .= "     YEAR || '年度 ' || SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR IN ('".CTRL_YEAR."', '".(CTRL_YEAR + 1)."') ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //履修履歴
    function getRirekiCode($model)
    {
        list($year, $semester) = explode(':', $model->field["YEAR_SEMESTER"]);
        $query  = " SELECT ";
        $query .= "     RIREKI_CODE AS VALUE, ";
        $query .= "     SELECT_NAME || ' ' || CHAR(REPLACE(CHAR(SELECT_DATE), '-', '/')) AS LABEL ";
        $query .= " FROM ";
        $query .= "     STUDY_SELECT_DATE_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース
    function getCourseCode($model) {

        list($year, $semester) = explode(':', $model->field["YEAR_SEMESTER"]);
        $query  = " SELECT DISTINCT ";
        $query .= "     T5.GRADE_NAME1 || '(' || T4.COURSECD || T4.MAJORCD || ') ' || T2.COURSENAME || T3.MAJORNAME || ' (' || T4.COURSECODE || ') ' || T1.COURSECODENAME AS LABEL, ";
        $query .= "     T4.GRADE || '-' || T4.COURSECD || '-' || T4.MAJORCD || '-' || T4.COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     COURSECODE_MST T1, ";
        $query .= "     COURSE_MST T2, ";
        $query .= "     MAJOR_MST T3, ";
        $query .= "     SCHREG_REGD_DAT T4, ";
        $query .= "     SCHREG_REGD_GDAT T5 ";
        $query .= " WHERE ";
        $query .= "     T4.YEAR       ='{$year}' AND ";
        $query .= "     T1.COURSECODE = T4.COURSECODE AND ";
        $query .= "     T2.COURSECD   = T4.COURSECD AND ";
        $query .= "     T3.COURSECD   = T4.COURSECD AND ";
        $query .= "     T4.MAJORCD    = T3.MAJORCD AND ";
        $query .= "     T4.YEAR       = T5.YEAR AND ";
        $query .= "     T4.GRADE      = T5.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T5.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T5.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目
    function getSubclassStdSelect($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS LABEL, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ";
        $query .= "          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "          AND T1.SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || ':' || T1.SEMESTER= '{$model->field["YEAR_SEMESTER"]}' ";
        $query .= "     AND T1.RIREKI_CODE = '{$model->field["RIREKI_CODE"]}' ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'X' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT E1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "             INNER JOIN SCHREG_REGD_GDAT G1 ";
                $query .= "                      ON G1.YEAR         = E1.YEAR ";
                $query .= "                     AND G1.GRADE        = E1.GRADE ";
                $query .= "                     AND G1.SCHOOL_KIND  IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "             INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "                      ON G1.YEAR         = E1.YEAR ";
            $query .= "                     AND G1.GRADE        = E1.GRADE ";
            $query .= "                     AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = E1.YEAR ";
        $query .= "             AND T1.SEMESTER = E1.SEMESTER ";
        $query .= "             AND E1.GRADE || '-' || E1.COURSECD || '-' || E1.MAJORCD || '-' || E1.COURSECODE = '{$model->field["COURSECODE"]}' ";
        $query .= "             AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "         ) ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座
    function getChairCmb($model) {
        $query  = " SELECT ";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRCD || ':' || T1.CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || ':' || T1.SEMESTER = '{$model->field["YEAR_SEMESTER"]}' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    //選択済み講座
    function getChairStd($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     REGD.COURSECODE, ";
        $query .= "     REGD_H.HR_NAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "          AND T1.YEAR = REGD.YEAR ";
        $query .= "          AND T1.SEMESTER = REGD.SEMESTER ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON REGD.YEAR = REGD_H.YEAR ";
        $query .= "          AND REGD.SEMESTER = REGD_H.SEMESTER ";
        $query .= "          AND REGD.GRADE = REGD_H.GRADE ";
        $query .= "          AND REGD.HR_CLASS = REGD_H.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || ':' || T1.SEMESTER = '{$model->field["YEAR_SEMESTER"]}' ";
        $query .= "     AND T1.CHAIRCD = '{$model->field["CHAIRCD"]}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND EXISTS (SELECT ";
                $query .= "                 'X' ";
                $query .= "             FROM ";
                $query .= "                 SCHREG_REGD_DAT S1, ";
                $query .= "                 SCHREG_REGD_GDAT S2 ";
                $query .= "             WHERE ";
                $query .= "                 T1.YEAR         = S1.YEAR AND ";
                $query .= "                 T1.YEAR         = S2.YEAR AND ";
                $query .= "                 T1.SEMESTER     = S1.SEMESTER AND ";
                $query .= "                 T1.SCHREGNO     = S1.SCHREGNO AND ";
                $query .= "                 S1.GRADE        = S2.GRADE AND ";
                $query .= "                 S2.SCHOOL_KIND  IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "             ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 SCHREG_REGD_DAT S1, ";
            $query .= "                 SCHREG_REGD_GDAT S2 ";
            $query .= "             WHERE ";
            $query .= "                 T1.YEAR         = S1.YEAR AND ";
            $query .= "                 T1.YEAR         = S2.YEAR AND ";
            $query .= "                 T1.SEMESTER     = S1.SEMESTER AND ";
            $query .= "                 T1.SCHREGNO     = S1.SCHREGNO AND ";
            $query .= "                 S1.GRADE        = S2.GRADE AND ";
            $query .= "                 S2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "             ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    //選択済み科目
    function getSubclassStd($model, $leftInstate) {
        $query  = " WITH CHAIR_STD AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_DAT CHAIR ON T1.YEAR = CHAIR.YEAR ";
        $query .= "          AND T1.SEMESTER = CHAIR.SEMESTER ";
        $query .= "          AND CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        $query .= "          AND T1.CHAIRCD = CHAIR.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || ':' || T1.SEMESTER = '{$model->field["YEAR_SEMESTER"]}' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     REGD.COURSECODE, ";
        $query .= "     REGD_H.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "          AND T1.YEAR = REGD.YEAR ";
        $query .= "          AND T1.SEMESTER = REGD.SEMESTER ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON REGD.YEAR = REGD_H.YEAR ";
        $query .= "          AND REGD.SEMESTER = REGD_H.SEMESTER ";
        $query .= "          AND REGD.GRADE = REGD_H.GRADE ";
        $query .= "          AND REGD.HR_CLASS = REGD_H.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || ':' || T1.SEMESTER = '{$model->field["YEAR_SEMESTER"]}' ";
        $query .= "     AND T1.RIREKI_CODE = '{$model->field["RIREKI_CODE"]}' ";
        $query .= "     AND T1.SCHREGNO NOT IN (SELECT CHAIR_STD.SCHREGNO FROM CHAIR_STD) ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        $query .= "     AND REGD.GRADE || '-' || REGD.COURSECD || '-' || REGD.MAJORCD || '-' || REGD.COURSECODE = '{$model->field["COURSECODE"]}' ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'X' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT E1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "             INNER JOIN SCHREG_REGD_GDAT E2 ";
                $query .= "                      ON E1.YEAR         = E2.YEAR ";
                $query .= "                     AND E1.GRADE        = E2.GRADE ";
                $query .= "                     AND E2.SCHOOL_KIND  IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "             INNER JOIN SCHREG_REGD_GDAT E2 ";
            $query .= "                      ON E1.YEAR         = E2.YEAR ";
            $query .= "                     AND E1.GRADE        = E2.GRADE ";
            $query .= "                     AND E2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = E1.YEAR ";
        $query .= "             AND T1.SEMESTER = E1.SEMESTER ";
        $query .= "             AND E1.GRADE || '-' || E1.COURSECD || '-' || E1.MAJORCD || '-' || E1.COURSECODE = '{$model->field["COURSECODE"]}' ";
        $query .= "             AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "         ) ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    //学期情報
    function getSemesterDate($year, $semester) {
        $query .= " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";

        return $query;
    }

    //DELETE
    function &getDeleteQuery($model) {

        list($year, $semester) = preg_split("/:/", $model->field["YEAR_SEMESTER"]);

        //収入生徒割り振りデータをセット(Delete Insert)
        $query  = " DELETE FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$semester}' ";
        $query .= "     AND T1.CHAIRCD IN ( ";
        $query .= "         SELECT ";
        $query .= "             T1.CHAIRCD ";
        $query .= "         FROM ";
        $query .= "             CHAIR_DAT T1 ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR || ':' || T1.SEMESTER = '{$model->field["YEAR_SEMESTER"]}' ";
        $query .= "             AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        $query .= "     ) ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($db, $model) {
        $db->autoCommit(false);

        list($year, $semester) = preg_split("/:/", $model->field["YEAR_SEMESTER"]);

        $query = knjb0058Query::getSemesterDate($year, $semester);
        $setDate = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //収入生徒割り振りデータをセット(Delete Insert)
        $query  = " DELETE FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$semester}' ";
        $query .= "     AND T1.CHAIRCD = '{$model->field["CHAIRCD"]}' ";
        $db->query($query);

        //対象生徒
        $selectStdData = array();
        $selectStd = explode(",", $model->selectStd);

        //生徒データをInsert
        if ($model->selectStd) {
            foreach ($selectStd as $schregNo) {
                $data = array();
                $data["YEAR"][TEXT]         = $year;
                $data["SEMESTER"][TEXT]     = $semester;
                $data["CHAIRCD"][TEXT]      = $model->field["CHAIRCD"];
                $data["SCHREGNO"][TEXT]     = $schregNo;
                $data["APPDATE"][DATE]      = $setDate["SDATE"];
                $data["APPENDDATE"][DATE]   = $setDate["EDATE"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER]    = "sysdate()";
                $query = Query::insertSQL($data, "CHAIR_STD_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        return $setDate;
    }
}
?>
