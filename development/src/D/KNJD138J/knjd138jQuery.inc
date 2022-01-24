<?php

require_once('for_php7.php');

class knjd138jQuery extends Query {

    //学期取得
    function getSemester($model)
    {
        $setYear = $model->exp_year ? $model->exp_year : CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$setYear."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //項目取得
    function getNameMst($model, $namecd1)
    {
        $setYear = $model->exp_year ? $model->exp_year : CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$setYear."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";

        return $query;
    }

    //行動記録の取得
    function getHrepSpecial($model)
    {
        $query  = " SELECT ";
        $query .= "     CODE, ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND DIV = '01' ";
        $query .= " ORDER BY ";
        $query .= "     CODE ";

        return $query;
    }

    //１レコード取得
    function getTrainRow($model, $flg="")
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        if ($flg) {
            $query .= "     AND SEMESTER = '9' ";
        } else {
            $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //通知表詳細データ取得
    function getDetailDat($model)
    {
        $query  = " SELECT ";
        $query .= "     CODE, ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND DIV = '03' ";
        $query .= "     AND CODE IN ('03', '04') ";
        $query .= " ORDER BY ";
        $query .= "     CODE ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        /**********/
        /* 学  期 */
        /**********/
        //存在チェック
        $check = $db->getCol(knjd138jQuery::getTrainRow($model));

        if(get_count($check) == 0) {
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
            $data["ATTENDREC_REMARK"][TEXT] = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
            $data["ATTENDREC_REMARK"][TEXT] = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        /**********/
        /* 通  年 */
        /**********/
        //存在チェック
        $check2 = $db->getCol(knjd138jQuery::getTrainRow($model, "1"));

        if(get_count($check2) == 0) {
            $data2["YEAR"][TEXT]             = $model->exp_year;
            $data2["SEMESTER"][TEXT]         = '9';
            $data2["SCHREGNO"][TEXT]         = $model->schregno;
            $data2["TOTALSTUDYTIME"][TEXT]   = $model->Field["TOTALSTUDYTIME"];
            $data2["REGISTERCD"][TEXT]       = STAFFCD;
            $data2["UPDATED"][FUNC]          = "sysdate()";

            $db->query(Query::insertSQL($data2, "HREPORTREMARK_DAT"));
        } else {
            $data2["TOTALSTUDYTIME"][TEXT]   = $model->Field["TOTALSTUDYTIME"];
            $data2["REGISTERCD"][TEXT]       = STAFFCD;
            $data2["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '9' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data2, "HREPORTREMARK_DAT", $where));
        }

        //HREPORTREMARK_DETAIL_DAT削除
        $query  = " DELETE FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND (DIV = '01' OR (DIV = '03' AND CODE IN ('03','04'))) ";

        $db->query($query);

        //HREPORTREMARK_DETAIL_DAT追加
        $detail_code = array('03', '04');
        foreach ($detail_code as $key) {

            $data = array();
            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SEMESTER"][TEXT]     = '9';
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["DIV"][TEXT]          = "03";
            $data["CODE"][TEXT]         = sprintf("%02d", $key);
            $data["REMARK1"][TEXT]      = $model->Field["REMARK03_".$key];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        }

        //HREPORTREMARK_DETAIL_DAT追加
        foreach ($model->itemArray as $key => $val) {

            $data = array();
            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SEMESTER"][TEXT]     = '9';
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["DIV"][TEXT]          = "01";
            $data["CODE"][TEXT]         = sprintf("%02d", $key);
            $data["REMARK1"][TEXT]      = $model->Field["REMARK".$key];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        }

        Query::dbCheckIn($db);
        return ;
    }

}
?>
