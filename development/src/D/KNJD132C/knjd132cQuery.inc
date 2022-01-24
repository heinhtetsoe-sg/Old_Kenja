<?php

require_once('for_php7.php');

class knjd132cQuery extends Query {
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

    //名称取得
    function getNameMst($model, $namecd1) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->exp_year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD1 ";

        return $query;
    }

    //観点マスタ
    function getBehaviorSemesMst($model) {
        $query  = " SELECT ";
        $query .= "     CODE AS VALUE, ";
        $query .= "     CODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->exp_year."' AND ";
        $query .= "     GRADE   = '".$model->grade."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //行動の記録取得
    function getBehaviorDat($model) {
        $query  = " SELECT ";
        $query .= "     CODE, ";
        $query .= "     RECORD ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     CODE ";

        return $query;
    }

    //所見取得
    function getHreportremarkDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    //INSERT -- BEHAVIOR_SEMES_DAT
    function &getInsertQuery2($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除 -- BEHAVIOR_SEMES_DAT
        $query  = " DELETE FROM ";
        $query .= "     BEHAVIOR_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        $db->query($query);

        //追加 -- BEHAVIOR_SEMES_DAT
        if (is_array($this->itemArray)) {
            foreach ($this->itemArray as $key => $val) {
                if ($model->Properties["knjdBehaviorsd_UseText"] != "1" && $model->record["RECORD"][$key] != "1") {
                    continue;
                }

                $data = array();
                $data["YEAR"][TEXT]         = $model->exp_year;
                $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["CODE"][TEXT]         = sprintf("%02d", $key);
                $data["RECORD"][TEXT]       = $model->record["RECORD"][$key];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "BEHAVIOR_SEMES_DAT");
                $db->query($query);
            }
        }

        //道徳
        //存在チェック
        $chk = $db->getCol(knjd132cQuery::getHreportremarkDat($model));

        if (get_count($chk) == 0) {
            $data = array();
            $data["YEAR"][TEXT]            = $model->exp_year;
            $data["SEMESTER"][TEXT]        = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]        = $model->schregno;
            $data["REMARK2"][TEXT]         = $model->field["REMARK2"];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";

            $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
        } else {
            $data = array();
            $data["REMARK2"][TEXT]         = $model->field["REMARK2"];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' ";

            $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
        }
        $db->query($query);
        $db->commit();

        Query::dbCheckIn($db);
        return ;
    }

    //定型文マスタ一覧取得
    function getHtrainRemarkTempDatIkkatsu($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE       = '".$model->grade."' ";
        if ($model->field["TEIKEI_CMD"] == "teikei1") {
            $query .= "     AND DATA_DIV    = '05' ";
        } else if ($model->field["TEIKEI_CMD"] == "teikei2") {
            $query .= "     AND DATA_DIV    = '06' ";
        } else if ($model->field["TEIKEI_CMD"] == "teikei3") {
            $query .= "     AND DATA_DIV    = '07' ";
        }
        $query .= " ORDER BY ";
        $query .= "     PATTERN_CD ";
        return $query;
    }
}
?>
