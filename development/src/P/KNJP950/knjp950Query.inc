<?php

require_once('for_php7.php');

class knjp950Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //年度を取得
    function getYear() {
        $setYear = CTRL_YEAR;
        $setNextYear = CTRL_YEAR + 1;

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR AS VALUE, ";
        $query .= "     T1.YEAR || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_YOSAN_DAT T1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '{$setYear}' AS VALUE, ";
        $query .= "     '{$setYear}' || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '{$setNextYear}' AS VALUE, ";
        $query .= "     '{$setNextYear}' || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";
        return $query;
    }
    
    //予算区分(収入、支出)を取得
    function getYosanSetDiv() {
        $query  = " SELECT ";
        $query .= "     '収入' AS LABEL, ";
        $query .= "     '01' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '支出' AS LABEL, ";
        $query .= "     '03' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
        
    //一覧取得(全体の履歴)
    function getList($model) {
        $setYear = ($model->field["YEAR"]) ? $model->field["YEAR"] : CTRL_YEAR;
        $beforeYear = $setYear - 1;

        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.YOSAN_DIV, ";
        $query .= "     T1.YOSAN_L_CD, ";
        $query .= "     T1.YOSAN_M_CD, ";
        $query .= "     T1.YOSAN_L_CD || T1.YOSAN_M_CD AS YOSAN_L_M_CD, ";
        $query .= "     T1.REQUEST_NO, ";
        $query .= "     VALUE(T1.REQUEST_GK, 0) AS REQUEST_GK, ";
        $query .= "     T1.REQUEST_REASON, ";
        $query .= "     VALUE(L1.REQUEST_GK, 0) AS BOFORE_REQUEST_GK ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_YOSAN_DAT T1 ";
        $query .= "     LEFT JOIN LEVY_REQUEST_YOSAN_DAT L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                                        AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                        AND L1.YEAR        = '{$beforeYear}' ";
        $query .= "                                        AND L1.YOSAN_DIV   = T1.YOSAN_DIV ";
        $query .= "                                        AND L1.YOSAN_L_CD  = T1.YOSAN_L_CD ";
        $query .= "                                        AND L1.YOSAN_M_CD  = T1.YOSAN_M_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= " AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " AND T1.YEAR = '{$setYear}' ";
        $query .= " AND T1.YOSAN_DIV  = '{$model->field["SET_DIV"]}' ";
        $query .= " AND T1.YOSAN_L_CD = '{$model->field["YOSAN_L_CD"]}' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.YOSAN_DIV, ";
        $query .= "     T1.YOSAN_L_CD, ";
        $query .= "     T1.YOSAN_M_CD, ";
        $query .= "     T1.YOSAN_L_CD || T1.YOSAN_M_CD AS YOSAN_L_M_CD, ";
        $query .= "     T1.REQUEST_NO, ";
        $query .= "     VALUE(T1.REQUEST_GK, 0) AS REQUEST_GK, ";
        $query .= "     T1.REQUEST_REASON, ";
        $query .= "     VALUE(L1.REQUEST_GK, 0) AS BOFORE_REQUEST_GK ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_YOSAN_DAT T1 ";
        $query .= "     LEFT JOIN LEVY_REQUEST_YOSAN_DAT L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                                        AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                        AND L1.YEAR        = '{$beforeYear}' ";
        $query .= "                                        AND L1.YOSAN_DIV   = T1.YOSAN_DIV ";
        $query .= "                                        AND L1.YOSAN_L_CD  = T1.YOSAN_L_CD ";
        $query .= "                                        AND L1.YOSAN_M_CD  = T1.YOSAN_M_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= " AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " AND T1.YEAR = '{$setYear}' ";
        //収入
        if ($model->field["SET_DIV"] == "01") {
            $query .= " AND T1.YOSAN_DIV  = '02' ";
        //支出
        } else {
            $query .= " AND T1.YOSAN_DIV  = '04' ";
        }
        $query .= " AND T1.YOSAN_L_CD = '{$model->field["YOSAN_L_CD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     YOSAN_DIV, ";
        $query .= "     YOSAN_L_M_CD ";
        return $query;
    }
    
    //精算科目
    function getLevyLDiv($model, $cd="") {
        $setYear = ($model->getYear) ? $model->getYear : $model->field["YEAR"];
        $query  = "";
        $query .= " SELECT ";
        $query .= "     LEVY_L_CD || ':' || LEVY_L_NAME AS LABEL, ";
        $query .= "     LEVY_L_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     LEVY_L_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= " AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " AND YEAR = '{$setYear}' ";
        if ($cd) {
            $query .= " AND LEVY_L_CD = '{$cd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    
    //精算項目
    function getLevyMDiv($model) {
        $setYear = ($model->getYear) ? $model->getYear : $model->field["YEAR"];
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || ':' || T1.LEVY_M_NAME AS LABEL, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= "     INNER JOIN LEVY_L_MST L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                             AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                             AND L1.YEAR        = T1.YEAR ";
        $query .= "                             AND L1.LEVY_L_CD   = T1.LEVY_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= " AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " AND T1.YEAR = '{$setYear}' ";
        $query .= " AND T1.LEVY_L_CD = '{$model->field["YOSAN_L_CD"]}' ";
        if ($model->field["SET_DIV"] === '01') {
            $query .= " AND T1.LEVY_IN_OUT_DIV = '1' ";
        } else {
            $query .= " AND T1.LEVY_IN_OUT_DIV = '2' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //精算項目名取得
    function getLevyMDivName($model, $yosanlcd, $yosanmcd) {
        $setYear = ($model->getYear) ? $model->getYear : $model->field["YEAR"];
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.LEVY_M_NAME ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= "     INNER JOIN LEVY_L_MST L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                             AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                             AND L1.YEAR        = T1.YEAR ";
        $query .= "                             AND L1.LEVY_L_CD   = T1.LEVY_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= " AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " AND T1.YEAR = '{$setYear}' ";
        $query .= " AND T1.LEVY_L_CD = '{$yosanlcd}' ";
        $query .= " AND T1.LEVY_M_CD = '{$yosanmcd}' ";
        if ($model->field["SET_DIV"] === '01') {
            $query .= " AND T1.LEVY_IN_OUT_DIV = '1' ";
        } else {
            $query .= " AND T1.LEVY_IN_OUT_DIV = '2' ";
        }
        return $query;
    }

    //精算項目の雑収入、予備費の項目かどうかをチェック
    function getLevyMZatuYobiFlg($model, $yosanlcd, $yosanmcd) {
        $setYear = ($model->getYear) ? $model->getYear : $model->field["YEAR"];
        $query  = "";
        $query .= " SELECT ";
        if ($model->field["SET_DIV"] === '01') {
            $query .= "     T1.ZATU_FLG ";
        } else {
            $query .= "     T1.YOBI_FLG ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= "     INNER JOIN LEVY_L_MST L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                             AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                             AND L1.YEAR        = T1.YEAR ";
        $query .= "                             AND L1.LEVY_L_CD   = T1.LEVY_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= " AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " AND T1.YEAR = '{$setYear}' ";
        $query .= " AND T1.LEVY_L_CD = '{$yosanlcd}' ";
        $query .= " AND T1.LEVY_M_CD = '{$yosanmcd}' ";
        if ($model->field["SET_DIV"] === '01') {
            $query .= " AND T1.LEVY_IN_OUT_DIV = '1' ";
        } else {
            $query .= " AND T1.LEVY_IN_OUT_DIV = '2' ";
        }
        return $query;
    }

    //データ有無チェック
    function getYosanDataCheck($db, $model) {
        $yosanlcd = substr($model->field["YOSAN_L_M_CD"], 0, 2);
        $yosanmcd = substr($model->field["YOSAN_L_M_CD"], 2, 2);
        //雑収入、予備費の場合は予算区分を変更
        $zatuYobiFlg = $db->getOne(knjp950Query::getLevyMZatuYobiFlg($model, $yosanlcd, $yosanmcd));
        
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_YOSAN_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= " AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " AND T1.YEAR = '{$model->field["YEAR"]}' ";
        if ($model->field["SET_DIV"] === '01' && $zatuYobiFlg === '1') {
            $query .= " AND T1.YOSAN_DIV  = '02' ";
        } else if ($model->field["SET_DIV"] === '03' && $zatuYobiFlg === '1') {
            $query .= " AND T1.YOSAN_DIV  = '04' ";
        } else {
            $query .= " AND T1.YOSAN_DIV  = '{$model->field["SET_DIV"]}' ";
        }
        $query .= " AND T1.YOSAN_L_CD || T1.YOSAN_M_CD = '{$model->field["YOSAN_L_M_CD"]}' ";
        return $query;
    }

    //選択データを取得
    function getYosanData($model, $flg="") {
        $beforeYear = $model->getYear - 1;
        
        $query  = " SELECT ";
        if (!$flg) {
            $query .= "     T1.YEAR, ";
            $query .= "     T1.YOSAN_DIV, ";
            $query .= "     T1.YOSAN_L_CD, ";
            $query .= "     T1.YOSAN_M_CD, ";
            $query .= "     T1.YOSAN_L_CD || T1.YOSAN_M_CD AS YOSAN_L_M_CD, ";
            $query .= "     T1.REQUEST_NO, ";
            $query .= "     T1.REQUEST_DATE, ";
            $query .= "     VALUE(T1.REQUEST_GK, 0) AS REQUEST_GK, ";
            $query .= "     T1.REQUEST_REASON, ";
            $query .= "     VALUE(L1.REQUEST_GK, 0) AS BOFORE_REQUEST_GK ";
        } else {
            $query .= "     COUNT(*) AS CNT ";
        }
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_YOSAN_DAT T1 ";
        $query .= "     LEFT JOIN LEVY_REQUEST_YOSAN_DAT L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                                        AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                        AND L1.YEAR        = '{$beforeYear}' ";
        $query .= "                                        AND L1.YOSAN_DIV   = T1.YOSAN_DIV ";
        $query .= "                                        AND L1.YOSAN_L_CD  = T1.YOSAN_L_CD ";
        $query .= "                                        AND L1.YOSAN_M_CD  = T1.YOSAN_M_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= " AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " AND T1.YEAR = '{$model->getYear}' ";
        $query .= " AND T1.YOSAN_DIV  = '{$model->getYosanDiv}' ";
        $query .= " AND T1.YOSAN_L_CD || T1.YOSAN_M_CD = '{$model->getYosanLMcd}' ";
        return $query;
    }
    
    //管理番号を取得
    function getMaxRequestNo($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     MAX(REQUEST_NO) AS NO ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_YOSAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= " AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " AND YEAR = '{$model->field["YEAR"]}' ";

        return $query;
    }
    
    //UPDATE
    function &getUpdateQuery($db, $model) {
        $db->autoCommit(false);

        $yosanlcd = substr($model->field["YOSAN_L_M_CD"], 0, 2);
        $yosanmcd = substr($model->field["YOSAN_L_M_CD"], 2, 2);
        
        //予算データチェック
        $countYosan = $db->getOne(knjp950Query::getYosanData($model, "COUNT"));

        $data = array();
        $data["REQUEST_NO"][TEXT]     = $model->getRequestNo;
        $data["REQUEST_DATE"][DATE]   = str_replace("/","-",$model->field["REQUEST_DATE"]);
        $data["REQUEST_GK"][NUMBER]   = $model->field["REQUEST_GK"];
        $data["REQUEST_REASON"][TEXT] = $model->field["REQUEST_REASON"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER] = "sysdate()";
        if ($countYosan == 0) {
            $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
            $data["YEAR"][TEXT] = $model->field["YEAR"];
            //雑収入、予備費の場合は予算区分を変更
            $zatuYobiFlg = $db->getOne(knjp950Query::getLevyMZatuYobiFlg($model, $yosanlcd, $yosanmcd));
            if ($model->field["SET_DIV"] === '01' && $zatuYobiFlg === '1') {
                $data["YOSAN_DIV"][TEXT]   = "02";
            } else if ($model->field["SET_DIV"] === '03' && $zatuYobiFlg === '1') {
                $data["YOSAN_DIV"][TEXT]   = "04";
            } else {
                $data["YOSAN_DIV"][TEXT]   = $model->field["SET_DIV"];
            }
            $data["YOSAN_L_CD"][TEXT]  = $yosanlcd;
            $data["YOSAN_M_CD"][TEXT]  = $yosanmcd;
            $query = Query::insertSQL($data, "LEVY_REQUEST_YOSAN_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ";
            $where .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= " AND SCHOOL_KIND = '".$model->schoolKind."' ";
            $where .= " AND YEAR = '{$model->getYear}' ";
            $where .= " AND YOSAN_DIV = '{$model->getYosanDiv}' ";
            $where .= " AND YOSAN_L_CD || YOSAN_M_CD = '{$model->getYosanLMcd}' ";
            $query = Query::updateSQL($data, "LEVY_REQUEST_YOSAN_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        return;
    }

    //UPDATE
    function &getDeleteQuery($db, $model) {
        $db->autoCommit(false);


        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_YOSAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= " AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " AND YEAR = '{$model->getYear}' ";
        $query .= " AND YOSAN_DIV = '{$model->getYosanDiv}' ";
        $query .= " AND YOSAN_L_CD || YOSAN_M_CD = '{$model->getYosanLMcd}' ";

        $db->query($query);

        $db->commit();
        return;
    }

}
?>
