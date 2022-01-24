<?php

require_once('for_php7.php');

class knjd135jQuery extends Query {

    //学校名取得
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1     = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";
        return $query;
    }

    //学期取得
    function getSemester($model) {
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

    //学期制取得
    function getMaxSemester() {
        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";

        return $query;
    }

    // HREPORTREMARK_DETAIL_DAT取得
    function getHreportremarkDetailDat($model, $semester, $div, $code) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$model->exp_year."'";
        $query .= "     AND SEMESTER    ='{$semester}'";
        $query .= "     AND SCHREGNO    = '".$model->schregno."' ";
        $query .= "     AND DIV         = '{$div}' ";
        $query .= "     AND CODE        = '{$code}' ";
        return $query;
    }

    // HREPORTREMARK_DAT取得
    function getHreportremarkDat($model, $semes = "") {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        if($semes == ""){
            $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        } else {
            $query .= "     SEMESTER    = '$semes' AND ";
        }
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    //まとめ出欠備考取得
    function getAttendSemesRemarkDat($model) {
        $query  = "  SELECT ";
        $query .= "      REMARK1, ";
        $query .= "      CASE WHEN MONTH = '01' THEN '13' WHEN MONTH = '02' THEN '14' WHEN MONTH = '03' THEN '15' ELSE MONTH END AS SORT_MONTH ";
        $query .= "  FROM ";
        $query .= "      ATTEND_SEMES_REMARK_DAT ";
        $query .= "  WHERE ";
        $query .= "     COPYCD      = '0' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";
        $query .= "  ORDER BY ";
        $query .= "      YEAR, ";
        $query .= "      SEMESTER, ";
        $query .= "      SORT_MONTH ";

        return $query;
    }

    // HREPORTREMARK_DETAIL_DAT件数取得
    function getHreportremarkDetailDatCnt($model, $semester, $div, $code) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     SEMESTER    = '".$semester."' AND ";
        $query .= "     DIV         = '".$div."' AND ";
        $query .= "     CODE        = '".$code."' ";

        return $query;
    }

    //更新処理
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        // HREPORTREMARK_DETAIL_DAT
        foreach ($model->detail as $key) {
            list ($semester, $div, $code, $field, $default_moji, $default_gyou, $comment) = $key;
            $semester = ($semester == "") ? $model->field["SEMESTER"] : $semester;
            $name = 'DETAIL_'.$div.'_'.$code.'_'.$field;

            //存在チェック
            $chk_detail = $db->getOne(knjd135jQuery::getHreportremarkDetailDatCnt($model, $semester, $div, $code));

            if ($chk_detail == 0) {
                $data = array();
                $data["YEAR"][TEXT]         = $model->exp_year;
                $data["SEMESTER"][TEXT]     = $semester;
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["DIV"][TEXT]          = $div;
                $data["CODE"][TEXT]         = $code;
                $data[$field][TEXT]         = $model->field[$name];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
            } else {
                $data = array();
                $data["YEAR"][TEXT]         = $model->exp_year;
                $data["SEMESTER"][TEXT]     = $semester;
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["DIV"][TEXT]          = $div;
                $data["CODE"][TEXT]         = $code;
                $data[$field][TEXT]         = $model->field[$name];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $where  = " WHERE ";
                $where .= "     YEAR        = '".$model->exp_year."' AND ";
                $where .= "     SEMESTER    = '".$semester."' AND ";
                $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
                $where .= "     DIV         = '".$div."' AND ";
                $where .= "     CODE        = '".$code."' ";

                $db->query(Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where));
            }
        }

        // HREPORTREMARK_DAT
        //存在チェック
        $chk = $db->getCol(knjd135jQuery::getHreportremarkDat($model));

        if (get_count($chk) == 0) {
            $data = array();
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data = array();
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        if ($model->schoolName == "kyoto") {
            // HREPORTREMARK_DAT 9学期
            //存在チェック
            $chk = $db->getCol(knjd135jQuery::getHreportremarkDat($model, '9'));

            if (get_count($chk) == 0) {
                $data = array();
                $data["YEAR"][TEXT]                 = $model->exp_year;
                $data["SEMESTER"][TEXT]             = '9';
                $data["SCHREGNO"][TEXT]             = $model->schregno;
                $data["REMARK1"][TEXT]              = $model->field["MORAL"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "sysdate()";

                $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
            } else {
                $data = array();
                $data["REMARK1"][TEXT]              = $model->field["MORAL"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "sysdate()";

                $where  = " WHERE ";
                $where .= "     YEAR     = '".$model->exp_year."' AND ";
                $where .= "     SEMESTER = '9' AND ";
                $where .= "     SCHREGNO = '".$model->schregno."' ";

                $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
            }
        }

        Query::dbCheckIn($db);
        return ;
    }
}
?>
