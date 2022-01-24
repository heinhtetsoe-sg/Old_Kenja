<?php

require_once('for_php7.php');

class knjd139lQuery extends Query {

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //所見取得
    function getHreportremarkDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->exp_semester}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //所見明細取得
    function getHreportremarkDetailDat($model, $div)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        if ($div == "02") {
            $query .= "     AND SEMESTER = '9' ";
        } else {
            $query .= "     AND SEMESTER = '{$model->exp_semester}' ";
        }
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND CODE = '01' ";
        $query .= "     AND DIV = '{$div}' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //道徳
    function getMoralText($model) {
        $query  = " SELECT ";
        $query .= "   D1.REMARK1 AS TEXT01 ";
        //$query .= "   D2.REMARK2 AS TITLE ";
        $query .= " FROM ";
        $query .= "   HREPORTREMARK_DAT D1 ";
        $query .= "   LEFT JOIN HREPORTREMARK_DETAIL_DAT D2 ";
        $query .= "     ON D2.YEAR = D1.YEAR ";
        $query .= "    AND D2.SEMESTER = D1.SEMESTER ";
        $query .= "    AND D2.SCHREGNO = D1.SCHREGNO";
        $query .= "    AND D2.DIV  = '01' ";
        $query .= "    AND D2.CODE = '01' ";
        $query .= " WHERE ";
        $query .= "   D1.YEAR = '{$model->exp_year}' ";
        $query .= "   AND D1.SEMESTER = '{$model->exp_semester}' ";
        $query .= "   AND D1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //出欠・特記事項
    function getRemarkText($model) {
        $query  = " SELECT ";
        //$query .= "   D1.ATTENDREC_REMARK AS TEXT01, ";
        $query .= "   D1.COMMUNICATION    AS TEXT02 ";
        //$query .= "   D2.REMARK3          AS TEXT03 ";
        $query .= " FROM ";
        $query .= "   HREPORTREMARK_DAT D1 ";
        $query .= "   LEFT JOIN HREPORTREMARK_DETAIL_DAT D2 ";
        $query .= "     ON D2.YEAR = D1.YEAR ";
        $query .= "    AND D2.SEMESTER = D1.SEMESTER ";
        $query .= "    AND D2.SCHREGNO = D1.SCHREGNO";
        $query .= "    AND D2.DIV  = '01' ";
        $query .= "    AND D2.CODE = '01' ";
        $query .= " WHERE ";
        $query .= "   D1.YEAR = '{$model->exp_year}' ";
        $query .= "   AND D1.SEMESTER = '{$model->exp_semester}' ";
        $query .= "   AND D1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }


    //INSERT
    function &getInsertQuery($model)
    {
        $retval = false;
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knjd139lQuery::getHreportremarkDat($model));

        if(get_count($check) == 0) {
            $data["YEAR"][TEXT]              = $model->exp_year;
            $data["SEMESTER"][TEXT]          = $model->exp_semester;
            $data["SCHREGNO"][TEXT]          = $model->schregno;
            //$data["TOTALSTUDYTIME"][TEXT]    = $model->field["TOTALSTUDY_EVAL"];
            $data["REMARK1"][TEXT]           = $model->field["MORAL_EVAL"];
            //$data["ATTENDREC_REMARK"][TEXT]  = $model->field["REMARK_ATTEND"];
            $data["COMMUNICATION"][TEXT]     = $model->field["REMARK_TEACHERCOMMENT"];
            $data["REGISTERCD"][TEXT]        = STAFFCD;
            $data["UPDATED"][FUNC]           = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            //$data["TOTALSTUDYTIME"][TEXT]    = $model->field["TOTALSTUDY_EVAL"];
            $data["REMARK1"][TEXT]           = $model->field["MORAL_EVAL"];
            //$data["ATTENDREC_REMARK"][TEXT]  = $model->field["REMARK_ATTEND"];
            $data["COMMUNICATION"][TEXT]     = $model->field["REMARK_TEACHERCOMMENT"];
            $data["REGISTERCD"][TEXT]        = STAFFCD;
            $data["UPDATED"][FUNC]           = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '{$model->exp_semester}' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }
        if ($db->getmessage == "") {
            $retval = true;
        }

        Query::dbCheckIn($db);
        return $retval;
    }
}
?>
