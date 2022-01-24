<?php

require_once('for_php7.php');

class knjm272nQuery extends Query {

    //担当者取得
    function selectStaff($model)
    {
        $query  = " WITH SUBCLASS_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SUBCLASS.CLASSCD, ";
        $query .= "     SUBCLASS.SCHOOL_KIND, ";
        $query .= "     SUBCLASS.CURRICULUM_CD, ";
        $query .= "     SUBCLASS.SUBCLASSCD ";
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
        $query .= "     CSTD.YEAR = '{$model->Year}' ";
        $query .= "     AND CSTD.SEMESTER = '{$model->semester}' ";
        $query .= "     AND SUBCLASS.CLASSCD < '91' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     W1.STAFFCD, ";
        $query .= "     W2.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     REP_STF_DAT W1 ";
        $query .= "     LEFT JOIN STAFF_MST W2 ON W1.STAFFCD = W2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     W1.YEAR = '{$model->Year}' ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SUBCLASS_T ";
        $query .= "         WHERE ";
        $query .= "             W1.CLASSCD = SUBCLASS_T.CLASSCD ";
        $query .= "             AND W1.SCHOOL_KIND = SUBCLASS_T.SCHOOL_KIND ";
        $query .= "             AND W1.CURRICULUM_CD = SUBCLASS_T.CURRICULUM_CD ";
        $query .= "             AND W1.SUBCLASSCD = SUBCLASS_T.SUBCLASSCD ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     W1.STAFFCD ";

        return $query;
    }

    //生徒データ
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     B1.SCHREGNO, ";
        $query .= "     B1.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST B1 ";
        $query .= " WHERE ";
        $query .= "     B1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //科目
    function getSubClass($model) {
        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCD, ";
        $query .= "     SUBCLASSNAME AS SUBNAME ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->Year}' ";
        $query .= "     AND CLASSCD < '91' ";
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     AND CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD IN ( ";
            $query .= "         SELECT ";
            $query .= "             CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ";
            $query .= "         FROM ";
            $query .= "             REP_STF_DAT ";
            $query .= "         WHERE ";
            $query .= "             YEAR = '{$model->Year}' ";
            $query .= "             AND STAFFCD = '".STAFFCD."' ";
            $query .= "     ) ";
        }
        //該当生徒の履修科目
        $query .= "     AND CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD IN ( ";
        $query .= "         SELECT ";
        $query .= "             CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ";
        $query .= "         FROM ";
        $query .= "             CHAIR_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '{$model->Year}' ";
        $query .= "             AND SEMESTER = '{$model->semester}' ";
        $query .= "             AND CHAIRCD IN ( ";
        $query .= "                 SELECT ";
        $query .= "                     CHAIRCD ";
        $query .= "                 FROM ";
        $query .= "                     CHAIR_STD_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '{$model->Year}' ";
        $query .= "                     AND SEMESTER = '{$model->semester}' ";
        $query .= "                     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "                 GROUP BY ";
        $query .= "                     CHAIRCD ";
        $query .= "             ) ";
        $query .= "         GROUP BY ";
        $query .= "             CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     SUBCD ";

        return $query;
    }

    //回数
    function getStandardSeq($model, $subcd) {
        $query  = " SELECT ";
        $query .= "     REP_SEQ_ALL, ";
        $query .= "     VALUE(REP_START_SEQ, 1) AS REP_START_SEQ ";
        $query .= " FROM ";
        $query .= "     CHAIR_CORRES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->Year}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$subcd}' ";

        return $query;
    }

    //生徒データ(レポート)
    function getSch($model, $subcd) {
        $query  = " SELECT ";
        $query .= "     STANDARD_SEQ, ";
        $query .= "     GRAD_INPUT_DATE, ";
        $query .= "     GRAD_VALUE ";
        $query .= " FROM ";
        $query .= "     REP_PRESENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->Year}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$subcd}' ";
        $query .= "     AND REPRESENT_SEQ = 0 ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     STANDARD_SEQ ";

        return $query;
    }

    //読替テーブル有無
    function useCombinedDat() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SYSIBM.TABLES ";
        $query .= " WHERE ";
        $query .= "     TABLE_NAME = 'REP_SUBCLASS_COMBINED_DAT' ";

        return $query;
    }

    //回数
    function getM003($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->Year}' ";
        $query .= "     AND NAMECD1 = 'M003' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //評価値チェック
    function getHyouka() {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     ABBV2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'M003' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

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
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $query = Query::insertSQL($data, "REP_PRESENT_DAT");
                $db->query($query);
            }
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i = 0; $i < get_count($model->updData); $i++) {
            list($subclasscd, $standard_seq, $represent_seq, $receipt_date, $grad_value) = preg_split("/,/", $model->updData[$i]);
            list($classCd, $schoolKind, $curriculum, $subclassCd) = explode("-", $subclasscd);

            $query  = " DELETE FROM ";
            $query .= "  REP_PRESENT_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$subclasscd}' ";
            $query .= "     AND STANDARD_SEQ = {$standard_seq} ";
            $query .= "     AND REPRESENT_SEQ = 0 ";
            $query .= "     AND SCHREGNO = '{$model->schregno}' ";
            $db->query($query);

            if ($grad_value != "9999" && strlen($receipt_date) == 10) {
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["CLASSCD"][TEXT]          = $classCd;
                $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculum;
                $data["SUBCLASSCD"][TEXT]       = $subclassCd;
                $data["STANDARD_SEQ"][NUMBER]   = $standard_seq;
                $data["REPRESENT_SEQ"][NUMBER]  = 0;
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["GRAD_VALUE"][TEXT]       = $grad_value;
                $data["RECEIPT_DATE"][DATE]     = $receipt_date;
                $data["GRAD_INPUT_DATE"][DATE]  = $receipt_date;
                $data["STAFFCD"][TEXT]          = $model->field["STAFF"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $query = Query::insertSQL($data, "REP_PRESENT_DAT");
                $db->query($query);
            }
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

}
?>