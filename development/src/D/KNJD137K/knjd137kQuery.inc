<?php

require_once('for_php7.php');

class knjd137kQuery extends Query {
    //学年コード取得
    function getGradeCd($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->exp_year."' AND ";
        $query .= "     GRADE   = '".$model->grade."' AND ";
        $query .= "     SCHOOL_KIND = 'P' ";

        return $query;
    }

    //印刷学期
    function getPrintSemester() {
        $query  = " SELECT ";
        $query .= "   NM.NAME1 AS LABEL, ";
        $query .= "   NM.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "   NAME_MST NM ";
        $query .= " WHERE ";
        $query .= "   NM.NAMECD1 = 'DP78' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //項目取得
    function getNameMst($model, $namecd1) {
        $setYear = $model->exp_year ? $model->exp_year : CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$setYear."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "D034") {
            $query .= "     AND NAME2 IS NOT NULL ";
            $query .= " ORDER BY ";
            $query .= "     VALUE(NAME2, '99'), ";
            $query .= "     NAMECD2 ";
        }

        return $query;
    }

    //DETAILの取得
    function getHrepSpecial($model) {
        $query  = " SELECT ";
        $query .= "     DIV, ";
        $query .= "     CODE, ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '".$model->field["PRINT_SEMESTER"]."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     DIV, ";
        $query .= "     CODE ";

        return $query;
    }

    //１レコード取得
    function getTrainRow($model) {
        $query  = " SELECT ";
        $query .= "     H1.* ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT H1 ";
        $query .= " WHERE ";
        $query .= "         H1.YEAR     = '".$model->exp_year."' ";
        $query .= "     AND H1.SEMESTER = '".$model->field["PRINT_SEMESTER"]."' ";
        $query .= "     AND H1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knjd137kQuery::getTrainRow($model));

        $grade_cd = $db->getOne(knjd137kQuery::getGradeCd($model));
        $chkGrade = ($grade_cd == '01' || $grade_cd == '02') ? "": "1";

        /*********************/
        /* HREPORTREMARK_DAT */
        /*********************/
        $data = array();
        $data["YEAR"][TEXT]                 = $model->exp_year;
        $data["SEMESTER"][TEXT]             = $model->field["PRINT_SEMESTER"];
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        if ($chkGrade == "1") {
            $data["TOTALSTUDYTIME"][TEXT]       = $model->field["TOTALSTUDYTIME"];  //総合（活動の様子）
        }
        $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];   //総合所見
        $data["REMARK1"][TEXT]              = $model->field["REMARK1"];         //道徳
        $data["REMARK3"][TEXT]              = $model->field["REMARK3"];         //自立活動（学習の様子）
        $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        if (get_count($check) == 0) {
            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "         YEAR     = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '".$model->field["PRINT_SEMESTER"]."' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        /****************************/
        /* HREPORTREMARK_DETAIL_DAT */
        /****************************/
        // 1年、2年
        if ($chkGrade == "") {
            $setCode = "'";
            $sep     = "";
            foreach ($model->itemArrayD034 as $key => $val) {
                $setCode .= $sep.$val["NAMECD2"];
                $sep      = "', '";
            }
            $setCode .= "'";
            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".$model->exp_year."' ";
            $query .= "     AND SEMESTER = '".$model->field["PRINT_SEMESTER"]."' ";
            $query .= "     AND SCHREGNO = '".$model->schregno."' ";
            $query .= "     AND (   (DIV = '01' AND CODE IN ({$setCode})) ";
            $query .= "          OR (DIV = '03' AND CODE IN ('01'))) ";
            $db->query($query);

            //自立活動(学習内容)
            $data = array();
            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SEMESTER"][TEXT]     = $model->field["PRINT_SEMESTER"];
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["DIV"][TEXT]          = "03";
            $data["CODE"][TEXT]         = "01";
            $data["REMARK1"][TEXT]      = $model->field["REMARK1_03_01"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));

            //特別活動
            foreach ($model->itemArrayD034 as $key => $val) {
                list($div, $code) = explode("_", $key);
                $data = array();
                $data["YEAR"][TEXT]         = $model->exp_year;
                $data["SEMESTER"][TEXT]     = $model->field["PRINT_SEMESTER"];
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["DIV"][TEXT]          = sprintf("%02d", $div);
                $data["CODE"][TEXT]         = sprintf("%02d", $code);
                $data["REMARK1"][TEXT]      = $model->field["REMARK1_".$key];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
            }

        } else {
        // 1年、2年以外
            $setCode01 = $setCode02 = "'";
            $sep01     = $sep02     = "";
            foreach ($model->itemArrayD034 as $key => $val) {
                $setCode01 .= $sep01.$val["NAMECD2"];
                $sep01      = "', '";
            }
            $setCode01 .= "'";

            foreach ($model->itemArrayD038 as $key => $val) {
                $setCode02 .= $sep02.$val["NAMECD2"];
                $sep02      = "', '";
            }
            $setCode02 .= "'";

            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".$model->exp_year."' ";
            $query .= "     AND SEMESTER = '".$model->field["PRINT_SEMESTER"]."' ";
            $query .= "     AND SCHREGNO = '".$model->schregno."' ";
            $query .= "     AND (   (DIV = '01' AND CODE IN ({$setCode01})) ";
            $query .= "          OR (DIV = '02' AND CODE IN ({$setCode02})) ";
            $query .= "          OR (DIV = '03' AND CODE IN ('01')) ";
            $query .= "          OR (DIV = '04' AND CODE IN ('01'))) ";
            $db->query($query);

            $detail_div  = array('03', '04');
            foreach ($detail_div as $div) {

                $data = array();
                $data["YEAR"][TEXT]         = $model->exp_year;
                $data["SEMESTER"][TEXT]     = $model->field["PRINT_SEMESTER"];
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["DIV"][TEXT]          = sprintf("%02d", $div);
                $data["CODE"][TEXT]         = "01";
                $data["REMARK1"][TEXT]      = $model->field["REMARK1_".sprintf("%02d", $div)."_01"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
            }

            //外国語
            foreach ($model->itemArrayD038 as $key => $val) {
                list($div, $code) = explode("_", $key);
                $data = array();
                $data["YEAR"][TEXT]         = $model->exp_year;
                $data["SEMESTER"][TEXT]     = $model->field["PRINT_SEMESTER"];
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["DIV"][TEXT]          = sprintf("%02d", $div);
                $data["CODE"][TEXT]         = sprintf("%02d", $code);
                $data["REMARK1"][TEXT]      = $model->field["REMARK1_".$key];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
            }
            //特別活動
            foreach ($model->itemArrayD034 as $key => $val) {
                list($div, $code) = explode("_", $key);
                $data = array();
                $data["YEAR"][TEXT]         = $model->exp_year;
                $data["SEMESTER"][TEXT]     = $model->field["PRINT_SEMESTER"];
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["DIV"][TEXT]          = sprintf("%02d", $div);
                $data["CODE"][TEXT]         = sprintf("%02d", $code);
                $data["REMARK1"][TEXT]      = $model->field["REMARK1_".$key];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
            }
        }

        Query::dbCheckIn($db);
        return ;
    }
}
?>
