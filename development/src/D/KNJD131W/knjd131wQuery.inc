<?php

require_once('for_php7.php');

class knjd131wQuery extends Query {

    //校種取得
    function getSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出欠備考参照データ取得
    function getAttendRemark($model) {
        $query  = " SELECT ";
        $query .= "     RDAT.YEAR, ";
        $query .= "     CASE WHEN LOCATE('0', RDAT.MONTH) = 1 THEN REPLACE(RDAT.MONTH, '0', '') ELSE RDAT.MONTH END AS MONTH, ";
        $query .= "     RDAT.SEMESTER, ";
        $query .= "     RDAT.SCHREGNO, ";
        $query .= "     RDAT.REMARK1, ";
        $query .= "     SMST.SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_REMARK_DAT RDAT";
        $query .= "     LEFT JOIN ";
        $query .= "     SEMESTER_MST SMST";
        $query .= "         ON RDAT.YEAR        = SMST.YEAR ";
        $query .= "         AND RDAT.SEMESTER   = SMST.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     RDAT.YEAR               = '".CTRL_YEAR."' ";
        $query .= "     AND RDAT.SCHREGNO       = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     RDAT.SEMESTER , ";
        $query .= "     CASE WHEN MONTH IN ('1', '2', '3') THEN 2 ELSE 1 END, ";
        $query .= "     RDAT.MONTH ";
        
        return $query;
    }

    //１レコード取得
    function getTrainRow($schregno, $model) {
        $query  = " SELECT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER             = '9' ";
        $query .= "     AND SCHREGNO             = '".$schregno."' ";

        return $query;
    }

    function getHreportDetailDat($schregno, $model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '{$schregno}'   ";
        $query .= "     AND DIV = '06' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER, ";
        $query .= "     CODE ";

        return $query;
    }

    function getAttendSemes($schregno, $semester) {
        $query  = " SELECT ";
        $query .= "     SUM(VALUE(MOURNING, 0)) AS ATTEND01,  ";
        $query .= "     SUM(VALUE(SUSPEND, 0)) AS ATTEND02,  ";
        $query .= "     SUM(VALUE(SICK, 0) + VALUE(NOTICE, 0) + VALUE(NONOTICE, 0)) AS ATTEND03,  ";
        $query .= "     SUM(VALUE(LATE, 0)) AS ATTEND04,  ";
        $query .= "     SUM(VALUE(EARLY, 0)) AS ATTEND05  ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";
        if ($semester) {
            $query .= "     AND SEMESTER = '{$semester}' ";
        }

        return $query;
    }

    /********/
    /* 更新 */
    /********/
    //INSERT
    function &getInsertQuery($fields, $schregno, $model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["SEMESTER"][TEXT]     = "9";
        $data["SCHREGNO"][TEXT]     = $schregno;
        $data["REMARK1"][TEXT]      = $fields["REMARK1"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $db->query(Query::insertSQL($data, "hreportremark_dat"));

        foreach ($model->koumoku as $key => $val) {
            $data = array();
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SEMESTER"][TEXT]     = $model->field["DETAIL_SEME"];
            $data["SCHREGNO"][TEXT]     = $schregno;
            $data["DIV"][TEXT]          = "06";
            $data["CODE"][TEXT]         = $key;
            $data["REMARK1"][TEXT]      = $model->detailField["DETAIL_REMARK1{$key}"];
            $data["REMARK2"][TEXT]      = $model->detailField["DETAIL_CHECK{$key}"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));

            $data = array();
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SEMESTER"][TEXT]     = "9";
            $data["SCHREGNO"][TEXT]     = $schregno;
            $data["DIV"][TEXT]          = "06";
            $data["CODE"][TEXT]         = $key;
            $data["REMARK1"][TEXT]      = $model->detailField["DETAIL_REMARK1_ALL{$key}"];
            $data["REMARK2"][TEXT]      = $model->detailField["DETAIL_CHECK_ALL{$key}"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));

        }

        Query::dbCheckIn($db);
        return ;
    }

   //DELETE
   function &getDeleteQuery($schregno, $model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";

        $db->query($query);

        $query  = " DELETE FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER IN ('{$model->field["DETAIL_SEME"]}', '9') ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";
        $query .= "     AND DIV = '06'";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
   }

}
?>
