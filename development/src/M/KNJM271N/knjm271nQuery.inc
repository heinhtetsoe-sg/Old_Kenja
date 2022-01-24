<?php

require_once('for_php7.php');

class knjm271nQuery extends Query {

    //科目
    function getSubClass($model)
    {
        $query  = " SELECT ";
        $query .= "     SUBCLASS.CLASSCD || '-' || SUBCLASS.SCHOOL_KIND || '-' || SUBCLASS.CURRICULUM_CD || '-' || SUBCLASS.SUBCLASSCD AS VALUE, ";
        $query .= "     SUBCLASS.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT CSTD ";
        $query .= "     INNER JOIN CHAIR_DAT CHAIRD ON CSTD.YEAR = CHAIRD.YEAR ";
        $query .= "           AND CSTD.SEMESTER = CHAIRD.SEMESTER ";
        $query .= "           AND CSTD.CHAIRCD = CHAIRD.CHAIRCD ";
        $query .= "     INNER JOIN SUBCLASS_MST SUBCLASS ON CHAIRD.CLASSCD = SUBCLASS.CLASSCD ";
        $query .= "           AND CHAIRD.SCHOOL_KIND = SUBCLASS.SCHOOL_KIND ";
        $query .= "           AND CHAIRD.CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ";
        $query .= "           AND CHAIRD.SUBCLASSCD = SUBCLASS.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     CSTD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CSTD.SEMESTER = '{$model->semester}' ";
        $query .= "     AND SUBCLASS.CLASSCD < '91' ";
        $query .= " GROUP BY ";
        $query .= "     SUBCLASS.CLASSCD, ";
        $query .= "     SUBCLASS.SCHOOL_KIND, ";
        $query .= "     SUBCLASS.CURRICULUM_CD, ";
        $query .= "     SUBCLASS.SUBCLASSCD, ";
        $query .= "     SUBCLASS.SUBCLASSNAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //担当者取得
    function selectStaff($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     W1.STAFFCD, ";
        $query .= "     W2.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     REP_STF_DAT W1 ";
        $query .= "     LEFT JOIN V_STAFF_MST W2 ON W1.STAFFCD = W2.STAFFCD ";
        $query .= "          AND W1.YEAR = W2.YEAR ";
        $query .= " WHERE ";
        $query .= "     W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || W1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     W1.STAFFCD ";

        return $query;
    }

    //回数
    function getM003()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'M003' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //最大回数
    function getStandardSeq($model)
    {
        $query  = " SELECT ";
        $query .= "     REP_SEQ_ALL, ";
        $query .= "     VALUE(REP_START_SEQ, 1) AS REP_START_SEQ ";
        $query .= " FROM ";
        $query .= "     CHAIR_CORRES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";

        return $query;
    }

    //登録済み生徒データ
    function getStdData($model) {

        $sortDiv = $model->sort[$model->getSort] == 1 ? "" : " DESC ";
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     CSTD.YEAR, ";
        $query .= "     CSTD.SEMESTER, ";
        $query .= "     CSTD.SCHREGNO, ";
        $query .= "     CHAIRD.CLASSCD || '-' || CHAIRD.SCHOOL_KIND || '-' || CHAIRD.CURRICULUM_CD || '-' || CHAIRD.SUBCLASSCD AS SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT CSTD ";
        $query .= "     INNER JOIN CHAIR_DAT CHAIRD ON CSTD.YEAR = CHAIRD.YEAR ";
        $query .= "           AND CSTD.SEMESTER = CHAIRD.SEMESTER ";
        $query .= "           AND CSTD.CHAIRCD = CHAIRD.CHAIRCD ";
        $query .= "           AND CHAIRD.CLASSCD || '-' || CHAIRD.SCHOOL_KIND || '-' || CHAIRD.CURRICULUM_CD || '-' || CHAIRD.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        $query .= "           AND CHAIRD.CLASSCD < '91' ";
        $query .= " WHERE ";
        $query .= "     CSTD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CSTD.SEMESTER = '{$model->semester}' ";
        $query .= " GROUP BY ";
        $query .= "     CSTD.YEAR, ";
        $query .= "     CSTD.SEMESTER, ";
        $query .= "     CSTD.SCHREGNO, ";
        $query .= "     CHAIRD.CLASSCD, ";
        $query .= "     CHAIRD.SCHOOL_KIND, ";
        $query .= "     CHAIRD.CURRICULUM_CD, ";
        $query .= "     CHAIRD.SUBCLASSCD ";
        $query .= " ) ";

        $query .= " SELECT  ";
        $query .= "     MAIN_T.SCHREGNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGDH.HR_NAME, ";
        $query .= "     PRESENT.GRAD_INPUT_DATE, ";
        $query .= "     PRESENT.RECEIPT_TIME, ";
        $query .= "     PRESENT.CLASSCD, ";
        $query .= "     PRESENT.SCHOOL_KIND, ";
        $query .= "     PRESENT.CURRICULUM_CD, ";
        $query .= "     PRESENT.SUBCLASSCD, ";
        $query .= "     PRESENT.CHAIRCD, ";
        $query .= "     PRESENT.RECEIPT_DATE, ";
        $query .= "     PRESENT.STANDARD_SEQ, ";
        $query .= "     PRESENT.REPRESENT_SEQ, ";
        $query .= "     PRESENT.GRAD_VALUE, ";
        $query .= "     PRESENT.GRAD_TIME, ";
        $query .= "     M003.NAME1 AS GRADMARK ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= "     LEFT JOIN REP_PRESENT_DAT PRESENT ON MAIN_T.YEAR = PRESENT.YEAR ";
        $query .= "          AND MAIN_T.SUBCLASSCD = PRESENT.CLASSCD || '-' || PRESENT.SCHOOL_KIND || '-' || PRESENT.CURRICULUM_CD || '-' || PRESENT.SUBCLASSCD ";
        $query .= "          AND MAIN_T.SCHREGNO = PRESENT.SCHREGNO ";
        $query .= "          AND PRESENT.REPRESENT_SEQ = 0 ";
        if (AUTHORITY != DEF_UPDATABLE){
            $query .= "      AND PRESENT.CLASSCD || PRESENT.SCHOOL_KIND  || PRESENT.CURRICULUM_CD || PRESENT.SUBCLASSCD ";
            $query .= "      IN (SELECT ";
            $query .= "              I1.CLASSCD || I1.SCHOOL_KIND  || I1.CURRICULUM_CD || I1.SUBCLASSCD ";
            $query .= "          FROM ";
            $query .= "              REP_STF_DAT I1 ";
            $query .= "          WHERE ";
            $query .= "              YEAR = '".CTRL_YEAR."' ";
            $query .= "              AND STAFFCD ='".STAFFCD."' ";
            $query .= "      ) ";
        }
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON MAIN_T.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON MAIN_T.YEAR = REGD.YEAR ";
        $query .= "          AND MAIN_T.SEMESTER = REGD.SEMESTER ";
        $query .= "          AND MAIN_T.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ";
        $query .= "          AND REGD.SEMESTER = REGDH.SEMESTER ";
        $query .= "          AND REGD.GRADE = REGDH.GRADE ";
        $query .= "          AND REGD.HR_CLASS = REGDH.HR_CLASS ";
        $query .= "     LEFT JOIN NAME_MST M003 ON M003.NAMECD1 = 'M003' ";
        $query .= "          AND PRESENT.GRAD_VALUE = M003.NAMECD2 ";
        $query .= " ORDER BY ";
        if ($model->getSort == "SRT_CLASS") {
            $query .= "     REGD.GRADE{$sortDiv}, ";
            $query .= "     REGD.HR_CLASS{$sortDiv}, ";
            $query .= "     REGD.ATTENDNO{$sortDiv}, ";
        }
        if ($model->getSort == "SRT_SCHREGNO") {
            $query .= "     MAIN_T.SCHREGNO{$sortDiv}, ";
        }
        if ($model->getSort == "SRT_NAME") {
            $query .= "     BASE.NAME_KANA{$sortDiv}, ";
        }
        $query .= "     PRESENT.SCHREGNO ";

        return $query;
    }

    /**
     * INSERT
     */
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($classCd, $schoolKind, $curriculum, $subclassCd) = explode("-", $model->field["SUBCLASSCD"]);
        foreach ($model->updData as $schregNo => $val) {
            $query  = " DELETE FROM ";
            $query .= "  REP_PRESENT_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
            $query .= "     AND STANDARD_SEQ = {$model->field["STANDARD_SEQ"]} ";
            $query .= "     AND REPRESENT_SEQ = 0 ";
            $query .= "     AND SCHREGNO = '{$schregNo}' ";
            $db->query($query);

            if ($val["GRAD_VALUE"] != "9999") {
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["CLASSCD"][TEXT]          = $classCd;
                $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculum;
                $data["SUBCLASSCD"][TEXT]       = $subclassCd;
                $data["STANDARD_SEQ"][NUMBER]   = $model->field["STANDARD_SEQ"];
                $data["REPRESENT_SEQ"][NUMBER]  = 0;
                $data["SCHREGNO"][TEXT]         = $schregNo;
                $data["RECEIPT_DATE"][DATE]     = $model->Date;
                $data["GRAD_VALUE"][TEXT]       = $val["GRAD_VALUE"];
                $data["GRAD_INPUT_DATE"][DATE]  = $model->Date;
                $data["STAFFCD"][TEXT]          = $model->field["STAFF"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $query = Query::insertSQL($data, "REP_PRESENT_DAT");
                $db->query($query);
            }
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }
}
?>