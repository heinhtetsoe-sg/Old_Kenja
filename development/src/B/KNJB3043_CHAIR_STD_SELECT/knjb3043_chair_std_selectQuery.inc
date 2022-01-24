<?php

require_once('for_php7.php');

class knjb3043_chair_std_selectQuery extends Query {

    //学期開始終了日を取得
    function getSemesterDate($model) {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";

        return $query;
    }

    // 重複講座の一覧取得
    function getOverlapChairList($model) {

        $startDate = str_replace("/", "-", $model->field["START_DATE"]);

        $query .= " WITH CHAR_STD AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR ";
        $query .= "         , T1.SEMESTER ";
        $query .= "         , T2.PRE_ORDER ";
        $query .= "         , T1.CHAIRCD ";
        $query .= "         , T1.SCHREGNO ";
        $query .= "         , T1.APPDATE ";
        $query .= "         , T1.APPENDDATE ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1 ";
        $query .= "         INNER JOIN SCH_PTRN_PRE_CHR_DAT T2 ";
        $query .= "             ON T1.YEAR = T2.YEAR ";
        $query .= "             AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "             AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "         INNER JOIN SCHREG_REGD_DAT T3 ";
        $query .= "             ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "             AND T1.YEAR = T3.YEAR ";
        $query .= "             AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "         AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "         AND '{$startDate}' BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        $query .= "         AND T2.PRESEQ = {$model->field["PRESEQ"]} ";
        $query .= " ) ";
        $query .= " , STD_CHAIR_CNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR ";
        $query .= "         , SEMESTER ";
        $query .= "         , PRE_ORDER ";
        $query .= "         , SCHREGNO ";
        $query .= "         , COUNT(DISTINCT CHAIRCD) CHAIRCNT ";
        $query .= "     FROM ";
        $query .= "         CHAR_STD ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR ";
        $query .= "         , SEMESTER ";
        $query .= "         , PRE_ORDER ";
        $query .= "         , SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD ";
        $query .= "     , T3.CHAIRNAME ";
        $query .= "     , T3.CHAIRABBV ";
        $query .= "     , T1.CHAIRCD VALUE ";
        $query .= "     , T1.CHAIRCD || ':' || T3.CHAIRNAME LABEL ";
        $query .= " FROM ";
        $query .= "     CHAR_STD T1 ";
        $query .= "     INNER JOIN STD_CHAIR_CNT T2 ";
        $query .= "         ON T1.YEAR = T2.YEAR ";
        $query .= "         AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "         AND T1.PRE_ORDER = T2.PRE_ORDER ";
        $query .= "         AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN CHAIR_DAT T3 ";
        $query .= "         ON T1.YEAR = T3.YEAR ";
        $query .= "         AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "         AND T1.CHAIRCD = T3.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T2.CHAIRCNT > 1 ";

        // 展開表画面で講座コードが選択されている場合は、追加する
        if ($model->field["SELECT_CHAIRCD"]) {
            $selectChairList = explode(",", $model->field["SELECT_CHAIRCD"]);

            // 展開表画面で選択されている講座と重複している講座のみ一覧取得
            $query .= "     AND T2.SCHREGNO IN ( SELECT ";
            $query .= "                              SCHREGNO ";
            $query .= "                          FROM ";
            $query .= "                              CHAR_STD ";
            $query .= "                          WHERE ";
            $query .= "                              CHAIRCD IN ('".implode("','", $selectChairList)."') ";
            $query .= "                          AND PRE_ORDER = '{$model->field["SELECT_PREORDER"]}' ";
            $query .= "                        ) ";
            $query .= "     AND T1.PRE_ORDER = '{$model->field["SELECT_PREORDER"]}' ";

            $query .= " UNION ";
            $query .= " SELECT DISTINCT ";
            $query .= "     T1.CHAIRCD ";
            $query .= "     , T1.CHAIRNAME ";
            $query .= "     , T1.CHAIRABBV ";
            $query .= "     , T1.CHAIRCD VALUE ";
            $query .= "     , T1.CHAIRCD || ':' || T1.CHAIRNAME LABEL ";
            $query .= " FROM ";
            $query .= "     CHAIR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
            $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
            $query .= "     AND T1.CHAIRCD IN ('".implode("','", $selectChairList)."') ";
        }

        return $query;
    }

    // 講座情報取得
    function getChairInfo($model) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "     , T1.SEMESTER ";
        $query .= "     , T1.CHAIRCD ";
        $query .= "     , T1.GROUPCD ";
        $query .= "     , T1.CLASSCD ";
        $query .= "     , T1.SCHOOL_KIND ";
        $query .= "     , T1.CURRICULUM_CD ";
        // $query .= "     , T1.SUBCLASSCD ";
        $query .= "     , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ";
        $query .= "     , T1.CHAIRNAME ";
        $query .= "     , T1.CHAIRABBV ";
        $query .= "     , T2.SUBCLASSNAME ";
        $query .= "     , T2.SUBCLASSABBV ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     INNER JOIN SUBCLASS_MST T2 ";
        $query .= "         ON T1.CLASSCD = T2.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.CHAIRCD = '{$model->field["OVERLAP_CHAIR"]}' ";

        return $query;
    }

    // 講座担当者取得
    function getChairStaffList($model, $chairCd) {
        $startDate = str_replace("/", "-", $model->field["START_DATE"]);

        $query  = " WITH STAFF AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD ";
        $query .= "         , STAFFCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STF_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->field["YEAR"]}' ";
        $query .= "         AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "         AND CHARGEDIV = 1 ";
        $query .= "         AND CHAIRCD = '{$chairCd}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.CHAIRCD ";
        $query .= "     , T1.STAFFCD ";
        $query .= "     , T2.STAFFNAME ";
        $query .= "     , T2.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     STAFF T1 ";
        $query .= "     INNER JOIN STAFF_MST T2 ";
        $query .= "         ON T1.STAFFCD = T2.STAFFCD ";

        return $query;
    }

    // 在籍生徒
    function getStd($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= "     , T1.YEAR ";
        $query .= "     , T1.SEMESTER ";
        $query .= "     , T1.GRADE ";
        $query .= "     , T1.HR_CLASS ";
        $query .= "     , T1.ATTENDNO ";
        $query .= "     , T1.COURSECD ";
        $query .= "     , T1.MAJORCD ";
        $query .= "     , T1.COURSECODE ";
        $query .= "     , T2.NAME ";
        $query .= "     , T2.NAME_SHOW ";
        $query .= "     , T3.HR_NAMEABBV ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "         ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "         ON T1.YEAR = T3.YEAR ";
        $query .= "         AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "         AND T1.GRADE = T3.GRADE ";
        $query .= "         AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";

        return $query;
    }

    // 講座生徒
    function getChairStd($model, $chairCd) {
        $startDate = str_replace("/", "-", $model->field["START_DATE"]);

        $query  = " WITH STD AS ( ";
        $query .= knjb3043_chair_std_selectQuery::getStd($model);
        $query .= " ) ";
        $query .= " , CHAR_STD AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR ";
        $query .= "         , T1.SEMESTER ";
        $query .= "         , T2.PRESEQ ";
        $query .= "         , T2.PRE_ORDER ";
        $query .= "         , T1.CHAIRCD ";
        $query .= "         , T1.SCHREGNO ";
        $query .= "         , T1.APPDATE ";
        $query .= "         , T1.APPENDDATE ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1 ";
        $query .= "         INNER JOIN SCH_PTRN_PRE_CHR_DAT T2 ";
        $query .= "             ON T1.YEAR = T2.YEAR ";
        $query .= "             AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "             AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "         INNER JOIN STD T3 ";
        $query .= "             ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "             AND T1.YEAR = T3.YEAR ";
        $query .= "             AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "         AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "         AND '{$startDate}' BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        $query .= "         AND T2.PRESEQ = {$model->field["PRESEQ"]} ";
        $query .= " ) ";
        $query .= " , STD_CHAIR_CNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR ";
        $query .= "         , SEMESTER ";
        $query .= "         , PRE_ORDER ";
        $query .= "         , SCHREGNO ";
        $query .= "         , COUNT(DISTINCT CHAIRCD) CHAIRCNT ";
        $query .= "     FROM ";
        $query .= "         CHAR_STD ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR ";
        $query .= "         , SEMESTER ";
        $query .= "         , PRE_ORDER ";
        $query .= "         , SCHREGNO ";
        $query .= " ) ";
        $query .= " , STD_CHAIR_MAX AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.CHAIRCD ";
        $query .= "         , T1.SCHREGNO ";
        $query .= "         , MAX(T2.CHAIRCNT) CHAIRCNT ";
        $query .= "     FROM ";
        $query .= "         CHAR_STD T1 ";
        $query .= "         LEFT JOIN STD_CHAIR_CNT T2 ";
        $query .= "             ON T1.YEAR = T2.YEAR ";
        $query .= "             AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "             AND T1.PRE_ORDER = T2.PRE_ORDER ";
        $query .= "             AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     GROUP BY ";
        $query .= "         T1.CHAIRCD ";
        $query .= "         , T1.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD ";
        $query .= "     , T1.SCHREGNO ";
        $query .= "     , T3.NAME ";
        $query .= "     , T3.NAME_SHOW ";
        $query .= "     , T3.GRADE ";
        $query .= "     , T3.HR_CLASS ";
        $query .= "     , VALUE(T2.CHAIRCNT, 0) CHAIRCNT ";
        $query .= "     , T3.HR_NAMEABBV ";
        $query .= "     , T3.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     LEFT JOIN STD_CHAIR_MAX T2 ";
        $query .= "         ON T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "         AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN STD T3 ";
        $query .= "         ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "         AND T1.YEAR = T3.YEAR ";
        $query .= "         AND T1.SEMESTER = T3.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.CHAIRCD = '{$chairCd}' ";
        $query .= "     AND '{$startDate}' BETWEEN T1.APPDATE AND T1.APPENDDATE ";

        $query .= " ORDER BY ";
        if ($model->Properties["studentListDispSchregNo"] == "1") {
            $query .= "     T1.SCHREGNO ";
        } else {
            $query .= "     T3.GRADE, T3.HR_CLASS, T3.ATTENDNO ";
        }

        return $query;
    }

    // クラス情報取得
    function getHrInfo($model, $allChairCd) {
        $startDate = str_replace("/", "-", $model->field["START_DATE"]);

        $query  = " SELECT ";
        $query .= "     HR_NAME ";
        $query .= "     , GRADE ";
        $query .= "     , HR_CLASS ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "         ( ";
        $query .= "             SELECT DISTINCT ";
        $query .= "                 T3.HR_NAME ";
        $query .= "                 , T1.CHAIRCD ";
        $query .= "                 , T2.GRADE ";
        $query .= "                 , T2.HR_CLASS ";
        $query .= "             FROM ";
        $query .= "                 CHAIR_STD_DAT T1 ";
        $query .= "                 INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "                     ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "                     AND T1.YEAR = T2.YEAR ";
        $query .= "                     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "                 INNER JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "                     ON T1.YEAR = T3.YEAR ";
        $query .= "                     AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "                     AND T2.GRADE || T2.HR_CLASS = T3.GRADE || T3.HR_CLASS ";
        $query .= "             WHERE ";
        $query .= "                 T1.CHAIRCD IN ('".implode("','", $allChairCd)."') ";
        $query .= "                 AND T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "                 AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "                 AND '{$startDate}' BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        $query .= "             UNION ALL ";
        $query .= "             SELECT ";
        $query .= "                 T2.HR_NAME ";
        $query .= "                 , T1.CHAIRCD ";
        $query .= "                 , T1.TRGTGRADE AS GRADE ";
        $query .= "                 , T1.TRGTCLASS AS HR_CLASS ";
        $query .= "             FROM ";
        $query .= "                 CHAIR_CLS_DAT T1 ";
        $query .= "                 INNER JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "                     ON T2.YEAR = T1.YEAR ";
        $query .= "                     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "                     AND T2.GRADE = T1.TRGTGRADE ";
        $query .= "                     AND T2.HR_CLASS = T1.TRGTCLASS ";
        $query .= "             WHERE ";
        $query .= "                 T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "                 AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "                 AND T1.CHAIRCD IN ('".implode("','", $allChairCd)."') ";
        $query .= "         ) ";
        $query .= "         ORDER BY ";
        $query .= "             CHAIRCD ";
        $query .= "     ) AS HR ";
        $query .= " GROUP BY ";
        $query .= "     HR_NAME ";
        $query .= "     , GRADE ";
        $query .= "     , HR_CLASS ";

        return $query;
    }

    // クラス生徒
    function getHrStd($model, $grade, $hrClass, $schregnoList) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO || ':' || T3.NAME AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.NAME, ";
        $query .= "     T2.HR_NAMEABBV ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " INNER JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.GRADE = T1.GRADE ";
        $query .= "     AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_BASE_MST T3 ";
        $query .= "     ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR='{$model->field['YEAR']}' ";
        $query .= "     AND T1.SEMESTER='{$model->field['SEMESTER']}' ";
        $query .= "     AND T1.GRADE='{$grade}' ";
        $query .= "     AND T1.HR_CLASS='{$hrClass}' ";
        $query .= "     AND T1.SCHREGNO NOT IN ('".implode("','", $schregnoList)."') ";

        $query .= " ORDER BY ";
        if ($model->Properties["studentListDispSchregNo"] == "1") {
            $query .= "     T1.SCHREGNO ";
        } else {
            $query .= "     T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ";
        }

        return $query;

    }

    // 指定された講座と同じ科目、または群の講座情報取得
    function getChairGroup($model, $chairCd) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "     , T1.SEMESTER ";
        $query .= "     , T1.CHAIRCD ";
        $query .= "     , T1.GROUPCD ";
        $query .= "     , T1.CLASSCD ";
        $query .= "     , T1.SCHOOL_KIND ";
        $query .= "     , T1.CURRICULUM_CD ";
        $query .= "     , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ";
        $query .= "     , T1.CHAIRNAME ";
        $query .= "     , T1.CHAIRABBV ";
        $query .= "     , T2.SUBCLASSNAME ";
        $query .= "     , T2.SUBCLASSABBV ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     INNER JOIN SUBCLASS_MST T2 ";
        $query .= "         ON T1.CLASSCD = T2.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "     LEFT JOIN CHAIR_DAT T3 ";
        $query .= "         ON T1.YEAR = T3.YEAR ";
        $query .= "         AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "         AND T1.CLASSCD = T3.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T3.CHAIRCD = '{$chairCd}' ";
        $query .= "     AND T1.CHAIRCD != '{$chairCd}' ";

        $query .= " ORDER BY ";
        $query .= "     T1.YEAR ";
        $query .= "     , T1.SEMESTER ";
        // $query .= "     , T1.GROUPCD ";
        $query .= "     , T1.CLASSCD ";
        $query .= "     , T1.SCHOOL_KIND ";
        $query .= "     , T1.CURRICULUM_CD ";
        $query .= "     , T1.SUBCLASSCD ";
        $query .= "     , T1.CHAIRCD ";

        return $query;
    }

    // 指定された講座と同じ履修科目の講座情報を取得
    function getChairCompGroup($model, $chairCd) {
        $query  = " WITH COMP AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T2.YEAR ";
        $query .= "         , T2.GRADE ";
        $query .= "         , T2.COURSECD ";
        $query .= "         , T2.MAJORCD ";
        $query .= "         , T2.COURSECODE ";
        $query .= "         , T2.GROUPCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1 ";
        $query .= "         INNER JOIN SUBCLASS_COMP_SELECT_DAT T2 ";
        $query .= "             ON T1.YEAR = T2.YEAR ";
        $query .= "             AND T1.CLASSCD = T2.CLASSCD ";
        $query .= "             AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "             AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "             AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "         AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "         AND T1.CHAIRCD = '{$chairCd}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $query .= " ) ";
        $query .= " , COMP_SUBCLASS AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR ";
        $query .= "         , T1.CLASSCD ";
        $query .= "         , T1.SCHOOL_KIND ";
        $query .= "         , T1.CURRICULUM_CD ";
        $query .= "         , T1.SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_COMP_SELECT_DAT T1 ";
        $query .= "         INNER JOIN COMP T2 ";
        $query .= "             ON T1.YEAR = T2.YEAR ";
        $query .= "             AND T1.GRADE = T2.GRADE ";
        $query .= "             AND T1.COURSECD = T2.COURSECD ";
        $query .= "             AND T1.MAJORCD = T2.MAJORCD ";
        $query .= "             AND T1.COURSECODE = T2.COURSECODE ";
        $query .= "             AND T1.GROUPCD = T2.GROUPCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "     , T1.SEMESTER ";
        $query .= "     , T1.CHAIRCD ";
        $query .= "     , T1.GROUPCD ";
        $query .= "     , T1.CLASSCD ";
        $query .= "     , T1.SCHOOL_KIND ";
        $query .= "     , T1.CURRICULUM_CD ";
        $query .= "     , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ";
        $query .= "     , T1.CHAIRNAME ";
        $query .= "     , T1.CHAIRABBV ";
        $query .= "     , T2.SUBCLASSNAME ";
        $query .= "     , T2.SUBCLASSABBV ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     INNER JOIN SUBCLASS_MST T2 ";
        $query .= "         ON T1.CLASSCD = T2.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "     INNER JOIN COMP_SUBCLASS T3 ";
        $query .= "         ON T1.YEAR = T3.YEAR ";
        $query .= "         AND T1.CLASSCD = T3.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.CHAIRCD NOT IN ('{$chairCd}') ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR ";
        $query .= "     , T1.SEMESTER ";
        // $query .= "     , T1.GROUPCD ";
        $query .= "     , T1.CLASSCD ";
        $query .= "     , T1.SCHOOL_KIND ";
        $query .= "     , T1.CURRICULUM_CD ";
        $query .= "     , T1.SUBCLASSCD ";
        $query .= "     , T1.CHAIRCD ";

        return $query;
    }


    // 受講生の重複チェック(Ajax用)
    function getStdOverlap($model) {
        $startDate = str_replace("/", "-", $model->field["START_DATE"]);

        $query  = " WITH STD AS ( ";
        $query .= knjb3043_chair_std_selectQuery::getStd($model);
        $query .= " ) ";
        $query .= " , CHAIR_STD_IDOU( ";
        $query .= "     YEAR, SEMESTER, CHAIRCD, SCHREGNO, APPDATE, APPENDDATE ";
        $query .= " ) AS ( ";
        // 受講生のリスト作成(移動先講座)
        $chairList = array();
        $chairList[] = $model->ajaxParam["CHAIRCD"];
        $union = "";
        for ($i=0; $i < get_count($model->ajaxParam["STDLIST"]); $i++) {
            $stdCd = $model->ajaxParam["STDLIST"][$i];
            $chairStd = " VALUES (";
            $chairStd .= "'{$model->field["YEAR"]}'";
            $chairStd .= ",'{$model->field["SEMESTER"]}'";
            $chairStd .= ",'{$model->ajaxParam["CHAIRCD"]}'";
            $chairStd .= ",'{$stdCd}'";
            $chairStd .= ",'{$startDate}'";
            $chairStd .= ",'{$model->field["SEMESTER_END"]}'";
            $chairStd .= ") ";
            $query .= $union.$chairStd;
            $union = " UNION ";
        }

        // 受講生のリスト作成(表示用講座)
        for ($i=0; $i < get_count($model->ajaxParam["UPDATE_CHAIRLIST"]); $i++) {
            $updateChairList = $model->ajaxParam["UPDATE_CHAIRLIST"][$i];
            $chairList[] = $updateChairList["CHAIRCD"];

            for ($j=0; $j < get_count($updateChairList["STDLIST"]); $j++) {
                $stdCd = $updateChairList["STDLIST"][$j];
                $chairStd = " VALUES (";
                $chairStd .= "'{$model->field["YEAR"]}'";
                $chairStd .= ",'{$model->field["SEMESTER"]}'";
                $chairStd .= ",'{$updateChairList["CHAIRCD"]}'";
                $chairStd .= ",'{$stdCd}'";
                $chairStd .= ",'{$startDate}'";
                $chairStd .= ",'{$model->field["SEMESTER_END"]}'";
                $chairStd .= ") ";
                $query .= $union.$chairStd;
                $union = " UNION ";
            }
        }

        $query .= " ) ";
        $query .= " , CHAIR_STD_PULL AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR ";
        $query .= "         , SEMESTER ";
        $query .= "         , CHAIRCD ";
        $query .= "         , SCHREGNO ";
        $query .= "         , APPDATE ";
        $query .= "         , APPENDDATE ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->field["YEAR"]}' ";
        $query .= "         AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "         AND CHAIRCD NOT IN ('".implode("','", $chairList)."') ";
        $query .= "         AND '{$startDate}' BETWEEN APPDATE AND APPENDDATE ";
        $query .= " ) ";
        $query .= " , CHAIR_STD_FULL AS ( ";
        $query .= "     SELECT * FROM CHAIR_STD_PULL ";
        $query .= "     UNION ";
        $query .= "     SELECT * FROM CHAIR_STD_IDOU ";
        $query .= " ) ";
        $query .= " , CHAIR_STD AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR ";
        $query .= "         , T1.SEMESTER ";
        $query .= "         , T2.PRE_ORDER ";
        $query .= "         , T1.CHAIRCD ";
        $query .= "         , T1.SCHREGNO ";
        $query .= "         , T1.APPDATE ";
        $query .= "         , T1.APPENDDATE ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_FULL T1 ";
        $query .= "         INNER JOIN SCH_PTRN_PRE_CHR_DAT T2 ";
        $query .= "             ON T1.YEAR = T2.YEAR ";
        $query .= "             AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "             AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "         INNER JOIN SCHREG_REGD_DAT T3 ";
        $query .= "             ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "             AND T1.YEAR = T3.YEAR ";
        $query .= "             AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "         AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "         AND '{$startDate}' BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        $query .= "         AND T2.PRESEQ = {$model->field["PRESEQ"]} ";
        $query .= " ) ";
        $query .= " , STD_CHAIR_CNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR ";
        $query .= "         , SEMESTER ";
        $query .= "         , PRE_ORDER ";
        $query .= "         , SCHREGNO ";
        $query .= "         , COUNT(DISTINCT CHAIRCD) CHAIRCNT ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR ";
        $query .= "         , SEMESTER ";
        $query .= "         , PRE_ORDER ";
        $query .= "         , SCHREGNO ";
        $query .= " ) ";
        $query .= " , STD_CHAIR_MAX AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.CHAIRCD ";
        $query .= "         , T1.SCHREGNO ";
        $query .= "         , MAX(T2.CHAIRCNT) CHAIRCNT ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD T1 ";
        $query .= "         LEFT JOIN STD_CHAIR_CNT T2 ";
        $query .= "             ON T1.YEAR = T2.YEAR ";
        $query .= "             AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "             AND T1.PRE_ORDER = T2.PRE_ORDER ";
        $query .= "             AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     GROUP BY ";
        $query .= "         T1.CHAIRCD ";
        $query .= "         , T1.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD ";
        $query .= "     , T1.SCHREGNO ";
        $query .= "     , T2.CHAIRCNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD T1 ";
        $query .= "     INNER JOIN STD_CHAIR_MAX T2 ";
        $query .= "         ON T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "         AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN CHAIR_DAT T3 ";
        $query .= "         ON T1.YEAR = T3.YEAR ";
        $query .= "         AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "         AND T1.CHAIRCD = T3.CHAIRCD ";
        $query .= "     INNER JOIN STD T4 ";
        $query .= "         ON T1.SCHREGNO = T4.SCHREGNO ";
        $query .= "         AND T1.YEAR = T4.YEAR ";
        $query .= "         AND T1.SEMESTER = T4.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.CHAIRCD = '{$model->ajaxParam["CHAIRCD"]}' ";
        $query .= "     AND '{$startDate}' BETWEEN T1.APPDATE AND T1.APPENDDATE ";

        return $query;
    }


    //UPDATE
    function &getUpdateQuery($db, $model) {
        $db->autoCommit(false);
        //学期開始日～学期終了日取得
        $query = knjb3043_chair_std_selectQuery::getSemesterDate($model);
        $result = $db->query($query);
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        $model->field["SEMESTER_START"] = $row["SDATE"];
        $model->field["SEMESTER_END"] = $row["EDATE"];

        $startDate = str_replace("/", "-", $model->field["START_DATE"]);
        $newEndDate = date('Y-m-d', strtotime($startDate)-60*60*24);

        // CHAIR_STD_DATの更新(重複元講座)  ※学期開始日以降を全て削除
        $query  = " DELETE FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '{$model->field["YEAR"]}' ";
        $query .= " AND T1.SEMESTER     = '{$model->field["SEMESTER"]}' ";
        $query .= " AND T1.APPDATE     >= '{$startDate}' ";
        $query .= " AND T1.CHAIRCD      = '{$model->updateChairList["CHAIRCD"]}' ";
        $db->query($query);

        // CHAIR_STD_DATへ追加(重複元講座)
        for ($i=0; $i < get_count($model->updateChairList["STDLIST"]); $i++) { 
            $stdCd = $model->updateChairList["STDLIST"][$i];

            $data = array();
            $data["YEAR"][TEXT]           = $model->field["YEAR"];
            $data["SEMESTER"][TEXT]       = $model->field["SEMESTER"];
            $data["CHAIRCD"][TEXT]        = $model->updateChairList["CHAIRCD"];
            $data["SCHREGNO"][TEXT]       = $stdCd;
            $data["APPDATE"][TEXT]        = $startDate;
            $data["APPENDDATE"][TEXT]     = $model->field["SEMESTER_END"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "SYSDATE()";

            $query = Query::insertSQL($data, "CHAIR_STD_DAT");
            $db->query($query);
        }

        // 移動先講座
        for ($i=0; $i < get_count($model->updateChairList["UPDATE_CHAIRLIST"]); $i++) {
            $updateChairList = $model->updateChairList["UPDATE_CHAIRLIST"][$i];

            // CHAIR_STD_DATの更新(移動先講座)
            $query  = " DELETE FROM ";
            $query .= "     CHAIR_STD_DAT T1 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR         = '{$model->field["YEAR"]}' ";
            $query .= " AND T1.SEMESTER     = '{$model->field["SEMESTER"]}' ";
            $query .= " AND T1.APPDATE     >= '{$startDate}' ";
            $query .= " AND T1.CHAIRCD      = '{$updateChairList["CHAIRCD"]}' ";
            $db->query($query);
    
            for ($j=0; $j < get_count($updateChairList["STDLIST"]); $j++) {
                // CHAIR_STD_DATへ追加(移動先講座)
                $stdCd = $updateChairList["STDLIST"][$j];

                $data = array();
                $data["YEAR"][TEXT]           = $model->field["YEAR"];
                $data["SEMESTER"][TEXT]       = $model->field["SEMESTER"];
                $data["CHAIRCD"][TEXT]        = $updateChairList["CHAIRCD"];
                $data["SCHREGNO"][TEXT]       = $stdCd;
                $data["APPDATE"][TEXT]        = $startDate;
                $data["APPENDDATE"][TEXT]     = $model->field["SEMESTER_END"];
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "SYSDATE()";

                $query = Query::insertSQL($data, "CHAIR_STD_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        return;
    }
}
?>
