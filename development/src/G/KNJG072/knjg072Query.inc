<?php

require_once('for_php7.php');


class knjg072Query extends Query {

    //年組取得
    function getHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "    HDAT.GRADE || '-' || HDAT.HR_CLASS AS VALUE, ";
        $query .= "    HDAT.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_GDAT GDAT ";
        $query .= "    INNER JOIN SCHREG_REGD_HDAT HDAT ON GDAT.YEAR = HDAT.YEAR ";
        $query .= "          AND GDAT.GRADE = HDAT.GRADE ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "          AND (HDAT.TR_CD1 = '" .STAFFCD ."' ";
            $query .= "            OR HDAT.TR_CD2 = '" .STAFFCD ."' ";
            $query .= "            OR HDAT.TR_CD3 = '" .STAFFCD ."' ";
            $query .= "            OR HDAT.SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "            OR HDAT.SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "            OR HDAT.SUBTR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= " WHERE ";
        $query .= "     GDAT.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    function getSchreg($model) {
        $query  = " SELECT ";
        $query .= "    REGD.ATTENDNO, ";
        $query .= "    REGD.SCHREGNO AS VALUE, ";
        $query .= "    REGD.ATTENDNO || '番　' || BASE.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.GRADE || '-' || REGD.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        $query .= " ORDER BY ";
        $query .= "    REGD.ATTENDNO ";

        return $query;
    }

    //生徒取得
    function getSportPrintHist() {
        $query  = " SELECT ";
        $query .= "    PRINT_HIST.SCHREGNO, ";
        $query .= "    PRINT_HIST.SEQ, ";
        $query .= "    BASE.NAME, ";
        $query .= "    PRINT_HIST.SEND_DATE, ";
        $query .= "    PRINT_HIST.SEND_TO1, ";
        $query .= "    PRINT_HIST.SEND_TO2 ";
        $query .= " FROM ";
        $query .= "     HEALTH_SPORT_PRINT_HIST_DAT PRINT_HIST ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON PRINT_HIST.SCHREGNO = BASE.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     PRINT_HIST.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND PRINT_HIST.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "     AND PRINT_HIST.SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= " ORDER BY ";
        $query .= "    PRINT_HIST.SEND_DATE, ";
        $query .= "    PRINT_HIST.SCHREGNO, ";
        $query .= "    PRINT_HIST.SEQ ";

        return $query;
    }

    //MAXSEQ
    function getMaxSeq($model) {
        $query  = " SELECT ";
        $query .= "    VALUE(MAX(SEQ), 0) + 1 AS MAX_SEQ ";
        $query .= " FROM ";
        $query .= "     HEALTH_SPORT_PRINT_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= "     AND SCHREGNO = '{$model->field["SCHREGNO"]}' ";

        return $query;
    }

    //更新
    function updatePrintHist($model, $maxSeq) {
        $data = array();
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
        $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
        $data["SCHREGNO"][TEXT]     = $model->field["SCHREGNO"];
        $data["SEQ"][NUMBER]        = $maxSeq;
        $data["SEND_TO1"][TEXT]     = $model->field["SEND_TO1"];
        $data["SEND_TO2"][TEXT]     = $model->field["SEND_TO2"];
        $data["SEND_DATE"][DATE]    = $model->field["SEND_DATE"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "HEALTH_SPORT_PRINT_HIST_DAT");

        return $query;
    }
}
?>
