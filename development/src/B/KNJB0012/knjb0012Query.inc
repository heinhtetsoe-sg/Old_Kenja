<?php

require_once('for_php7.php');

class knjb0012Query extends Query {

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
        $query .= "     VALUE DESC ";

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

    //基本時間割から校時取得
    function getPeriod($model) {
        list($year, $seq, $semester) = preg_split("/,/", $model->field["SCH_PTRN"]);
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     N1.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'B001' ";
        $query .= "          AND T1.PERIODCD = N1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        if ($seq) {
            $query .= "     AND BSCSEQ = {$seq} ";
        } else {
            $query .= "     AND BSCSEQ = -1 ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.PERIODCD ";

        return $query;
    }

    //対象講座
    function getChairDat($model, $periodCd) {
        list($year, $seq, $semester) = preg_split("/,/", $model->field["SCH_PTRN"]);
        $query  = " WITH SUBCLASS_C AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.RIREKI_CODE, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.GROUPCD ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.RIREKI_CODE = '{$model->field["RIREKI_CODE"]}' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregNo}' ";
        $query .= " ), SUBCLASS_G AS ( ";
        $query .= " SELECT ";
        $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T2.GROUPCD ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT T2, ";
        $query .= "     SUBCLASS_C T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "     AND T1.RIREKI_CODE = T2.RIREKI_CODE ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.SUBCLASSCD <> T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ";
        $query .= "     AND T1.GROUPCD = T2.GROUPCD ";
        $query .= " ), MAIN_CHAIR AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     SUBCLASS_C T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     SUBCLASS_G T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.DAYCD, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     L1.CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_DAT T1 ";
        $query .= "     LEFT JOIN CHAIR_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "          AND T1.CHAIRCD = L1.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$semester}' ";
        if ($seq) {
            $query .= "     AND T1.BSCSEQ = {$seq} ";
        } else {
            $query .= "     AND T1.BSCSEQ = -1 ";
        }
        $query .= "     AND T1.PERIODCD = '{$periodCd}' ";
        $query .= "     AND T1.CHAIRCD IN (SELECT CHAIRCD FROM MAIN_CHAIR) ";
        $query .= " ORDER BY ";
        $query .= "     T1.DAYCD, ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    //受講データを取得
    function getChairStd($model) {
        $query .= " SELECT ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exeYear}' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND SCHREGNO = '{$model->schregNo}' ";

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

    //UPDATE
    function &getUpdateQuery($db, $model) {
        $db->autoCommit(false);

        $query = knjb0012Query::getSemesterDate($model);
        $setDate = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $query  = " DELETE FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SEMESTER  = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregNo}' ";
        $db->query($query);

        $setZumiChair = array();
        foreach ($model->updChairArray as $setChairCd) {

            if ($setZumiChair[$setChairCd]) {
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

        $db->commit();
        return;
    }

}
?>
