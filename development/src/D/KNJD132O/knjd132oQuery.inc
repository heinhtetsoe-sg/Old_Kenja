<?php

require_once('for_php7.php');

class knjd132oQuery extends Query {

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // HREPORTREMARK_DAT取得
    function getHreportremarkDat($model, $semes) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$semes."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";
        return $query;
    }
    
    // HREPORTREMARKDETAIL_DATをCODEの値をもとに取得
    function getHreportremarkDetailDatByCode($model, $semes, $code) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".$model->exp_year."' AND ";
        $query .= "     T1.SEMESTER    = '".$semes."' AND ";
        $query .= "     T1.SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     T1.DIV = '01' AND ";
        $query .= "     T1.CODE = '".$code."' ";
        return $query;
    }

    // HREPORTREMARKDETAIL_DAT取得
    function getHreportremarkDetailDat($model, $semes) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".$model->exp_year."' AND ";
        $query .= "     T1.SEMESTER    = '".$semes."' AND ";
        $query .= "     T1.SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     T1.DIV = '01' AND ";
        $query .= "     T1.CODE = '01' ";

        return $query;
    }

    //まとめ出欠備考
    function getSemesRemark($db, $year, $schregno) {

        $query  = "  SELECT ";
        $query .= "      REMARK1, ";
        $query .= "      CASE WHEN MONTH = '01' THEN '13' WHEN MONTH = '02' THEN '14' WHEN MONTH = '03' THEN '15' ELSE MONTH END AS SORT_MONTH ";
        $query .= "  FROM ";
        $query .= "      ATTEND_SEMES_REMARK_DAT ";
        $query .= "  WHERE ";
        $query .= "      COPYCD = '0' ";
        $query .= "  AND YEAR = '{$year}' ";
        $query .= "  AND SCHREGNO = '{$schregno}' ";
        $query .= "  ORDER BY ";
        $query .= "      YEAR, ";
        $query .= "      SEMESTER, ";
        $query .= "      SORT_MONTH ";

        $result = $db->query($query);
        $set_remark = "";
        $count = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($count == 0) {
                $set_remark .= $row["REMARK1"];
            } else {
                if ($row["REMARK1"] != "") {
                    $set_remark .= "／".$row["REMARK1"];
                }
            }
            $count++;
        }

        return $set_remark;
    }


    //更新処理
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // HREPORTREMARK_DAT(所見)
        //存在チェック
        $setsems = $model->field["SEMESTER"];
        $chk = $db->getCol(knjd132oQuery::getHreportremarkDat($model, $setsems));
        if (get_count($chk) == 0) {
            $data = array();
            $data["YEAR"][TEXT]               = $model->exp_year;
            $data["SEMESTER"][TEXT]           = $setsems;
            $data["SCHREGNO"][TEXT]           = $model->schregno;
            $data["COMMUNICATION"][TEXT]      = $model->field["COMMUNICATION"];
            $data["ATTENDREC_REMARK"][TEXT]   = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][FUNC]            = "sysdate()";

            $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
        } else {
            $data = array();
            $data["COMMUNICATION"][TEXT]      = $model->field["COMMUNICATION"];
            $data["ATTENDREC_REMARK"][TEXT]   = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][FUNC]            = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR              = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER          = '".$setsems."' AND ";
            $where .= "     SCHREGNO          = '".$model->schregno."' ";
            
            $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
        }
        $db->query($query);
        

        //HREPORTREMARK_DETAIL_DAT
        $divval = "01";
        
        //CODE列が01～05のREMARK1を更新
        $remark_arr=array(
        "REMARK1" => "01",
        "REMARK2" => "03",
        "REMARK3" => "05",
        "REMARK4" => "06",
        );

        
        foreach ($remark_arr as $remark_name => $code) {
            $chk = $db->getCol(knjd132oQuery::getHreportremarkDetailDatByCode($model, $setsems, $code));
            if (get_count($chk) == 0) {
                $data = array();
                $data["YEAR"][TEXT]         = $model->exp_year;
                $data["SEMESTER"][TEXT]     = $setsems;
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["DIV"][TEXT]          = $divval;
                $data["CODE"][TEXT]         = $code;
                $data["REMARK1"][TEXT]      = $model->field[$remark_name];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
            } else {
                $data = array();
                $data["REMARK1"][TEXT]      = $model->field[$remark_name];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $where  = " WHERE ";
                $where .= "     YEAR     = '".$model->exp_year."' AND ";
                $where .= "     SEMESTER = '".$setsems."' AND ";
                $where .= "     SCHREGNO = '".$model->schregno."' AND ";
                $where .= "     DIV = '".$divval."' AND ";
                $where .= "     CODE = '".$code."' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
            }
            $db->query($query);
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return ;
    }
}
?>
