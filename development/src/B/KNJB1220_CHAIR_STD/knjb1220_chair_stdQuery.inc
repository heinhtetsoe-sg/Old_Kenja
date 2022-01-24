<?php

require_once('for_php7.php');

class knjb1220_chair_stdQuery extends Query {

    //学期を取得
    function getSemester($model) {
        $query .= " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exeYear}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //履修履歴
    function getRirekiCode($model)
    {
        $query  = " SELECT ";
        $query .= "     RIREKI_CODE AS VALUE, ";
        $query .= "     SELECT_NAME || ' ' || CHAR(REPLACE(CHAR(SELECT_DATE), '-', '/')) AS LABEL ";
        $query .= " FROM ";
        $query .= "     STUDY_SELECT_DATE_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exeYear}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期開始終了日を取得
    function getSemesterDate($model) {
        $query .= " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exeYear}' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";

        return $query;
    }

    //生徒情報を取得
    function getSchregInfo($model) {
        $query .= " SELECT ";
        $query .= "     BASE.SCHREGNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     REGD.COURSECODE, ";
        $query .= "     REGD_H.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST BASE ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "          AND REGD.YEAR = '{$model->exeYear}' ";
        $query .= "          AND REGD.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON REGD.YEAR = REGD_H.YEAR ";
        $query .= "          AND REGD.SEMESTER = REGD_H.SEMESTER ";
        $query .= "          AND REGD.GRADE = REGD_H.GRADE ";
        $query .= "          AND REGD.HR_CLASS = REGD_H.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     BASE.SCHREGNO = '{$model->schregNo}' ";

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
        $query .= "     T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.RIREKI_CODE = '{$model->field["RIREKI_CODE"]}' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregNo}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //基本時間割
    function getSchPatternH($model) {
        $query  = " SELECT ";
        $query .= "     T2.SEMESTERNAME, ";
        $query .= "     T1.BSCSEQ, ";
        $query .= "     T1.TITLE, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_HDAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.BSCSEQ ";

        return $query;
    }

    //メイン講座
    function getMainChair($model) {
        list($year, $seq, $semester) = preg_split("/,/", $model->field["SCH_PTRN"]);
        $query  = " WITH CHAIR_STD AS ( ";
        $query .= " SELECT ";
        $query .= "     STD.CHAIRCD, ";
        $query .= "     COUNT(*) AS STD_CNT ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "          T1.CHAIRCD, ";
        $query .= "          T1.SCHREGNO ";
        $query .= "      FROM ";
        $query .= "          CHAIR_STD_DAT T1 ";
        $query .= "      WHERE ";
        $query .= "          T1.YEAR = '{$year}' ";
        $query .= "          AND T1.SEMESTER = '{$semester}' ";
        $query .= "      GROUP BY ";
        $query .= "          T1.CHAIRCD, ";
        $query .= "          T1.SCHREGNO ";
        $query .= "     ) STD ";
        $query .= " GROUP BY ";
        $query .= "     STD.CHAIRCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     L1.CHAIRNAME, ";
        $query .= "     T1.DAYCD, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     VALUE(PERI.ABBV1, '') AS PERIODNAME, ";
        $query .= "     MAX(VALUE(L3.CAPACITY, 0)) AS CAPACITY, ";
        $query .= "     VALUE(STD.STD_CNT, 0) AS STD_CNT ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_DAT T1 ";
        $query .= "     LEFT JOIN CHAIR_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "          AND T1.CHAIRCD = L1.CHAIRCD ";
        $query .= "     LEFT JOIN CHAIR_FAC_DAT L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.SEMESTER = L2.SEMESTER ";
        $query .= "          AND T1.CHAIRCD = L2.CHAIRCD ";
        $query .= "     LEFT JOIN FACILITY_MST L3 ON L2.FACCD = L3.FACCD ";
        $query .= "     LEFT JOIN CHAIR_STD STD ON T1.CHAIRCD = STD.CHAIRCD ";
        $query .= "     LEFT JOIN NAME_MST PERI ON PERI.NAMECD1 = 'B001' ";
        $query .= "          AND T1.PERIODCD = PERI.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        if ($seq) {
            $query .= "     AND T1.BSCSEQ = {$seq} ";
        }
        $query .= "     AND T1.SEMESTER = '{$semester}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.DAYCD, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     PERI.ABBV1, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     L1.CHAIRNAME, ";
        $query .= "     STD.STD_CNT ";
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.DAYCD, ";
        $query .= "     T1.PERIODCD ";

        return $query;
    }

    //STD講座
    function getMainStdChair($model) {
        list($year, $seq, $semester) = preg_split("/,/", $model->field["SCH_PTRN"]);
        $query  = " WITH CHAIR_STD AS ( ";
        $query .= " SELECT ";
        $query .= "     STD.CHAIRCD, ";
        $query .= "     COUNT(*) AS STD_CNT ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "          T1.CHAIRCD, ";
        $query .= "          T1.SCHREGNO ";
        $query .= "      FROM ";
        $query .= "          CHAIR_STD_DAT T1 ";
        $query .= "      WHERE ";
        $query .= "          T1.YEAR = '{$model->exeYear}' ";
        $query .= "          AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "          AND T1.SCHREGNO = '{$model->schregNo}' ";
        $query .= "      GROUP BY ";
        $query .= "          T1.CHAIRCD, ";
        $query .= "          T1.SCHREGNO ";
        $query .= "     ) STD ";
        $query .= " GROUP BY ";
        $query .= "     STD.CHAIRCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CHAIRNAME, ";
        $query .= "     L1.DAYCD, ";
        $query .= "     L1.PERIODCD, ";
        $query .= "     VALUE(PERI.ABBV1, '') AS PERIODNAME, ";
        $query .= "     MAX(VALUE(L3.CAPACITY, 0)) AS CAPACITY, ";
        $query .= "     VALUE(STD.STD_CNT, 0) AS STD_CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     LEFT JOIN SCH_PTRN_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SEMESTER = L1.SEMESTER ";
        if ($seq) {
            $query .= "          AND L1.BSCSEQ = {$seq} ";
        }
        $query .= "          AND T1.CHAIRCD = L1.CHAIRCD ";
        $query .= "     LEFT JOIN CHAIR_FAC_DAT L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.SEMESTER = L2.SEMESTER ";
        $query .= "          AND T1.CHAIRCD = L2.CHAIRCD ";
        $query .= "     LEFT JOIN FACILITY_MST L3 ON L2.FACCD = L3.FACCD ";
        $query .= "     LEFT JOIN CHAIR_STD STD ON T1.CHAIRCD = STD.CHAIRCD ";
        $query .= "     LEFT JOIN NAME_MST PERI ON PERI.NAMECD1 = 'B001' ";
        $query .= "          AND L1.PERIODCD = PERI.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= " GROUP BY ";
        $query .= "     L1.DAYCD, ";
        $query .= "     L1.PERIODCD, ";
        $query .= "     PERI.ABBV1, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CHAIRNAME, ";
        $query .= "     STD.STD_CNT ";
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     L1.DAYCD, ";
        $query .= "     L1.PERIODCD ";

        return $query;
    }

    //選択済み講座
    function getChairStd($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregNo}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    //対象講座
    function getChairDat($model, $leftInstate) {
        $query  = " WITH CHIR_CLASS_C AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND VALUE(T1.GROUPCD, '0000') = '0000' ";
        $query .= " ), CHIR_CLASS_G AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.GROUPCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND VALUE(T1.GROUPCD, '0000') <> '0000' ";
        $query .= " ), CHAIR_D AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.GROUPCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN {$model->subClassInState} ";
        $query .= " ), CHAIR_G AS ( ";
        $query .= " SELECT ";
        $query .= "     T2.CHAIRCD, ";
        $query .= "     T2.GROUPCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_D T1, ";
        $query .= "     CHAIR_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "     AND T1.CHAIRCD <> T2.CHAIRCD ";
        $query .= "     AND T1.GROUPCD <> '0000' ";
        $query .= "     AND T1.GROUPCD = T2.GROUPCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     CASE WHEN VALUE(GROUPCD, '0000') <> '0000' ";
        $query .= "          THEN GROUPCD || ' ' ";
        $query .= "          ELSE VALUE(GROUPCD, '0000') ";
        $query .= "     END AS GROUPCD, ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_D ";
        $query .= " WHERE ";
        $query .= "     CHAIRCD NOT IN {$leftInstate} ";
        $query .= "     AND (CHAIRCD IN (SELECT NI1.CHAIRCD FROM CHIR_CLASS_C NI1) ";
        $query .= "          OR ";
        $query .= "          GROUPCD IN (SELECT NI1.GROUPCD FROM CHIR_CLASS_G NI1)) ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     VALUE(GROUPCD, '0000') || '+' AS GROUPCD, ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_G ";
        $query .= " WHERE ";
        $query .= "     CHAIRCD NOT IN {$leftInstate} ";
        $query .= "     AND GROUPCD IN (SELECT NI1.GROUPCD FROM CHIR_CLASS_G NI1) ";
        $query .= " ORDER BY ";
        $query .= "     GROUPCD, ";
        $query .= "     CHAIRCD ";

        return $query;
    }

    //対象講座
    function getChairDat2($model, $leftInstate) {
        $query  = " WITH CHIR_CLASS AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND VALUE(T1.GROUPCD, '0000') = '0000' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND VALUE(T1.GROUPCD, '0000') <> '0000' ";
        $query .= " ), CHAIR_MAIN AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.GROUPCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN {$model->subClassInState} ";
        $query .= " ), SUBCLASS_C AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.GROUPCD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SUBCLASS_COMP_SELECT_DAT T1 ON REGD.YEAR = T1.YEAR ";
        $query .= "           AND REGD.GRADE = T1.GRADE ";
        $query .= "           AND REGD.COURSECD = T1.COURSECD ";
        $query .= "           AND REGD.MAJORCD = T1.MAJORCD ";
        $query .= "           AND REGD.COURSECODE = T1.COURSECODE ";
        $query .= "           AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN {$model->subClassInState} ";
        $query .= " WHERE ";
        $query .= "     REGD.SCHREGNO = '{$model->schregNo}' ";
        $query .= "     AND REGD.YEAR = '{$model->exeYear}' ";
        $query .= "     AND REGD.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= " ), SUBCLASS_G AS ( ";
        $query .= " SELECT ";
        $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T2.GROUPCD ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT T2, ";
        $query .= "     SUBCLASS_C T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.GRADE = T2.GRADE ";
        $query .= "     AND T1.COURSECD = T2.COURSECD ";
        $query .= "     AND T1.MAJORCD = T2.MAJORCD ";
        $query .= "     AND T1.COURSECODE = T2.COURSECODE ";
        $query .= "     AND T1.SUBCLASSCD <> T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ";
        $query .= "     AND T1.GROUPCD = T2.GROUPCD ";
        $query .= " ), CHAIR_D AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     '000' AS GROUPCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_MAIN T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.SUBCLASSCD NOT IN (SELECT T2.SUBCLASSCD FROM SUBCLASS_C T2) ";
        $query .= "     AND T1.SUBCLASSCD NOT IN (SELECT T3.SUBCLASSCD FROM SUBCLASS_G T3) ";
        $query .= " ), CHAIR_G AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T2.GROUPCD || ' ' AS GROUPCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     SUBCLASS_C T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T2.GROUPCD || '+' AS GROUPCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     SUBCLASS_G T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN (SELECT NI1.SUBCLASSCD FROM SUBCLASS_C NI1) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     GROUPCD, ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_D ";
        $query .= " WHERE ";
        $query .= "     CHAIRCD NOT IN {$leftInstate} ";
        $query .= "     AND CHAIRCD IN (SELECT NI1.CHAIRCD FROM CHIR_CLASS NI1) ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     GROUPCD, ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_G ";
        $query .= " WHERE ";
        $query .= "     CHAIRCD NOT IN {$leftInstate} ";
        $query .= "     AND CHAIRCD IN (SELECT NI1.CHAIRCD FROM CHIR_CLASS NI1) ";
        $query .= " ORDER BY ";
        $query .= "     GROUPCD, ";
        $query .= "     CHAIRCD ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($db, $model) {
        $db->autoCommit(false);

        //対象講座
        $selectChairData = array();
        $selectChair = explode(",", $model->selectChair);

        $query = knjb1220_chair_stdQuery::getSemesterDate($model);
        $setDate = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //収入生徒割り振りデータをセット(Delete Insert)
        $query  = " DELETE FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SEMESTER  = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregNo}' ";
        $db->query($query);

        //生徒データをInsert
        $setZumiChair = array();
        if ($model->selectChair) {
            foreach ($selectChair as $setChairCd) {

                if ($setZumiChair["$setChairCd"]) {
                    continue;
                }
                $data = array();
                $data["YEAR"][TEXT]         = $model->exeYear;
                $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                $data["CHAIRCD"][TEXT]      = $setChairCd;
                $data["SCHREGNO"][TEXT]     = $model->schregNo;
                $data["APPDATE"][DATE]      = $setDate["SDATE"];
                $data["APPENDDATE"][DATE]   = $setDate["EDATE"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER]    = "sysdate()";
                $query = Query::insertSQL($data, "CHAIR_STD_DAT");
                $db->query($query);
                $setZumiChair[$setChairCd] = $setChairCd;
            }
        }

        $db->commit();
        return;
    }
}
?>
