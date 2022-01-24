<?php

require_once('for_php7.php');

class knjp710Query extends Query {
    //年度取得
    function getYear() {
        $query  = " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
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

    //データ取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     RE_M.PREFECTURESCD, ";
        $query .= "     N1.NAME1 AS PREFECTURES_NAME, ";
        $query .= "     RE_M.GRADE, ";
        $query .= "     RE_M.MONEY_DIV1, ";
        $query .= "     RE_M.NUMERATOR1, ";
        $query .= "     RE_M.DENOMINATOR1, ";
        $query .= "     RE_M.REDUCTIONMONEY_1, ";
        $query .= "     RE_M.INCOME_LOW1, ";
        $query .= "     RE_M.INCOME_HIGH1, ";
        $query .= "     RE_M.BURDEN_CHARGE_FLG1, ";
        $query .= "     N2.NAME1 AS INCOME_RANK1, ";
        $query .= "     RE_M.MONEY_DIV2, ";
        $query .= "     RE_M.NUMERATOR2, ";
        $query .= "     RE_M.DENOMINATOR2, ";
        $query .= "     RE_M.REDUCTIONMONEY_2, ";
        $query .= "     RE_M.INCOME_LOW2, ";
        $query .= "     RE_M.INCOME_HIGH2, ";
        $query .= "     RE_M.BURDEN_CHARGE_FLG2, ";
        $query .= "     N3.NAME1 AS INCOME_RANK2, ";
        $query .= "     RE_M.REDUCTIONREMARK, ";
        $query .= "     RE_M.REDUCTION_SEQ ";
        $query .= " FROM ";
        $query .= "     REDUCTION_MST RE_M ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ON N1.YEAR    = RE_M.YEAR ";
        $query .= "                            AND N1.NAMECD2 = RE_M.PREFECTURESCD ";
        $query .= "                            AND N1.NAMECD1 = 'G202' ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ON N2.YEAR    = RE_M.YEAR ";
        $query .= "                            AND N2.NAMECD2 = RE_M.INCOME_RANK1 ";
        $query .= "                            AND N2.NAMECD1 = 'P002' ";
        $query .= "     LEFT JOIN V_NAME_MST N3 ON N3.YEAR    = RE_M.YEAR ";
        $query .= "                            AND N3.NAMECD2 = RE_M.INCOME_RANK2 ";
        $query .= "                            AND N3.NAMECD1 = 'P002' ";
        $query .= " WHERE ";
        $query .= "         RE_M.YEAR        = '".$model->year."' ";
        $query .= "     AND RE_M.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND RE_M.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND RE_M.REDUCTION_TARGET = '".$model->reductionTarget."' ";
        $query .= " ORDER BY ";
        $query .= "     RE_M.PREFECTURESCD, ";
        $query .= "     RE_M.GRADE, ";
        $query .= "     INCOME_RANK1, ";
        $query .= "     INCOME_RANK2 ";

        return $query;
    }

    //都道府県コード取得
    function getName($year, $schoolKind) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL";
        $query .= " FROM ";
        $query .= "     V_NAME_MST NM ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = 'G202' ";
        $query .= "     AND EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     REDUCTION_PREF_GRADE_MST PGM ";
        $query .= "                 WHERE ";
        $query .= "                     PGM.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "                 AND PGM.SCHOOL_KIND = '".$schoolKind."' ";
        $query .= "                 AND PGM.YEAR        = NM.YEAR ";
        $query .= "                 AND PGM.PREFECTURESCD = NM.NAMECD2) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年
    function getGrade($model, $year) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //学年別都道府県補助金マスタ取得
    function getReductionPrefGradeMst($year, $schoolKind, $prefecturescd, $grade) {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.NAME1 AS REFER_YEAR_DIV1_LABEL, ";
        $query .= "     L2.NAME1 AS REFER_YEAR_DIV2_LABEL ";
        $query .= " FROM ";
        $query .= "     REDUCTION_PREF_GRADE_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'P003' AND L1.NAMECD2 = T1.REFER_YEAR_DIV1 ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'P003' AND L2.NAMECD2 = T1.REFER_YEAR_DIV2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND T1.SCHOOL_KIND  = '".$schoolKind."' ";
        $query .= "     AND T1.YEAR         = '".$year."' ";
        $query .= "     AND T1.PREFECTURESCD = '".$prefecturescd."' ";
        $query .= "     AND T1.GRADE        = '".$grade."' ";

        return $query;
    }

    //名称取得
    function getNameMst($year, $namecd, $namecd2="") {
        $query  = " SELECT ";
        $query .= "      NAMECD2 AS VALUE, ";
        $query .= "      NAME1 AS LABEL";
        $query .= " FROM  ";
        $query .= "      V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' ";
        $query .= " AND NAMECD1 = '".$namecd."' ";
        if ($namecd2){
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        return $query;
    }

    //１レコード取得
    function getRow($model, $flg) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     REDUCTION_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR             = '".$model->year."' ";
        $query .= "     AND SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND      = '".$model->schoolKind."' ";
        $query .= "     AND REDUCTION_TARGET = '".$model->reductionTarget."' ";
        if ($flg == 1) {
            $query .= "    AND PREFECTURESCD = '".$model->prefecturescd."' ";
            $query .= "    AND GRADE         = '".$model->grade."' ";
            if($model->reduction_seq) {
                $query .= "    AND REDUCTION_SEQ  = ".$model->reduction_seq." ";
            }
        } else if($flg == 2) {
            $query .= "    AND PREFECTURESCD = '".$model->field["PREFECTURESCD"]."' ";
            $query .= "    AND GRADE         = '".$model->field["GRADE"]."' ";
            if($this->cmd == "update" || $this->cmd == "delete") {
                $query .= "    AND REDUCTION_SEQ = ".$model->reduction_seq." ";
            } else {
                if ($model->field["MONEY_DIV1"] == "1") {
                    $query .= "    AND NUMERATOR1   = ".$model->field["NUMERATOR1"] ;
                    $query .= "    AND DENOMINATOR1 = ".$model->field["DENOMINATOR1"] ;
                } else if ($model->field["MONEY_DIV1"] == "2" || $model->field["MONEY_DIV1"] == "3") {
                    $query .= "    AND REDUCTIONMONEY_1 = ".$model->field["REDUCTIONMONEY_1"] ;
                }
                if ($model->field["MONEY_DIV2"] == "1") {
                    $query .= "    AND NUMERATOR2   = ".$model->field["NUMERATOR2"] ;
                    $query .= "    AND DENOMINATOR2 = ".$model->field["DENOMINATOR2"] ;
                } else if ($model->field["MONEY_DIV2"] == "2" || $model->field["MONEY_DIV2"] == "3") {
                    $query .= "    AND REDUCTIONMONEY_2 = ".$model->field["REDUCTIONMONEY_2"] ;
                }
            }
        }

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

/***** 前年度コピー処理 *****/
    //今年度のデータが存在するかチェック
    function CheckData($year, $model) {
        $Lyear = $year - 1; //前年度チェック用

        $db = Query::dbCheckOut();

        $query  = " SELECT DISTINCT ";
        $query .= "     CASE WHEN YEAR = '".$year."'  THEN 'X' ";
        $query .= "          WHEN YEAR = '".$Lyear."' THEN 'Y' END  AS CNT";
        $query .= " FROM ";
        $query .= "     REDUCTION_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND      = '".$model->schoolKind."' ";
        $query .= "     AND REDUCTION_TARGET = '".$model->reductionTarget."' ";

        $result = $db->query($query);
        $opt_year = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year[] = $row["CNT"];
        }

        Query::dbCheckIn($db);
        return $opt_year;
    }

    //前年度のデータをコピー
    function &getCopyQuery($year, $model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO REDUCTION_MST ";
        $query .= "                ( ";
        $query .= "                 SCHOOLCD, ";
        $query .= "                 SCHOOL_KIND, ";
        $query .= "                 YEAR, ";
        $query .= "                 REDUCTION_TARGET, ";
        $query .= "                 PREFECTURESCD, ";
        $query .= "                 GRADE, ";
        $query .= "                 MONEY_DIV1, ";
        $query .= "                 NUMERATOR1, ";
        $query .= "                 DENOMINATOR1, ";
        $query .= "                 REDUCTIONMONEY_1, ";
        $query .= "                 INCOME_LOW1, ";
        $query .= "                 INCOME_HIGH1, ";
        $query .= "                 BURDEN_CHARGE_FLG1, ";
        $query .= "                 INCOME_RANK1, ";
        $query .= "                 MONEY_DIV2, ";
        $query .= "                 NUMERATOR2, ";
        $query .= "                 DENOMINATOR2, ";
        $query .= "                 REDUCTIONMONEY_2, ";
        $query .= "                 INCOME_LOW2, ";
        $query .= "                 INCOME_HIGH2, ";
        $query .= "                 BURDEN_CHARGE_FLG2, ";
        $query .= "                 INCOME_RANK2, ";
        $query .= "                 MONTH_MONEY_4, ";
        $query .= "                 MONTH_MONEY_5, ";
        $query .= "                 MONTH_MONEY_6, ";
        $query .= "                 MONTH_MONEY_7, ";
        $query .= "                 MONTH_MONEY_8, ";
        $query .= "                 MONTH_MONEY_9, ";
        $query .= "                 MONTH_MONEY_10, ";
        $query .= "                 MONTH_MONEY_11, ";
        $query .= "                 MONTH_MONEY_12, ";
        $query .= "                 MONTH_MONEY_1, ";
        $query .= "                 MONTH_MONEY_2, ";
        $query .= "                 MONTH_MONEY_3, ";
        $query .= "                 REDUCTIONREMARK, ";
        $query .= "                 REGISTERCD, ";
        $query .= "                 UPDATED ";
        $query .= "                ) ";
        $query .= "    (SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         '".$year."', ";
        $query .= "         REDUCTION_TARGET, ";
        $query .= "         PREFECTURESCD, ";
        $query .= "         GRADE, ";
        $query .= "         MONEY_DIV1, ";
        $query .= "         NUMERATOR1, ";
        $query .= "         DENOMINATOR1, ";
        $query .= "         REDUCTIONMONEY_1, ";
        $query .= "         INCOME_LOW1, ";
        $query .= "         INCOME_HIGH1, ";
        $query .= "         BURDEN_CHARGE_FLG1, ";
        $query .= "         INCOME_RANK1, ";
        $query .= "         MONEY_DIV2, ";
        $query .= "         NUMERATOR2, ";
        $query .= "         DENOMINATOR2, ";
        $query .= "         REDUCTIONMONEY_2, ";
        $query .= "         INCOME_LOW2, ";
        $query .= "         INCOME_HIGH2, ";
        $query .= "         BURDEN_CHARGE_FLG2, ";
        $query .= "         INCOME_RANK2, ";
        $query .= "         MONTH_MONEY_4, ";
        $query .= "         MONTH_MONEY_5, ";
        $query .= "         MONTH_MONEY_6, ";
        $query .= "         MONTH_MONEY_7, ";
        $query .= "         MONTH_MONEY_8, ";
        $query .= "         MONTH_MONEY_9, ";
        $query .= "         MONTH_MONEY_10, ";
        $query .= "         MONTH_MONEY_11, ";
        $query .= "         MONTH_MONEY_12, ";
        $query .= "         MONTH_MONEY_1, ";
        $query .= "         MONTH_MONEY_2, ";
        $query .= "         MONTH_MONEY_3, ";
        $query .= "         REDUCTIONREMARK, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         SYSDATE() ";
        $query .= "     FROM ";
        $query .= "         REDUCTION_MST ";
        $query .= "     WHERE ";
        $query .= "         SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND      = '".$model->schoolKind."' ";
        $query .= "     AND YEAR             = '".($year - 1)."'";
        $query .= "     AND REDUCTION_TARGET = '".$model->reductionTarget."'";
        $query .= "     ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]          = $model->schoolKind;
        $data["YEAR"][TEXT]                 = $fields["YEAR"];
        $data["REDUCTION_TARGET"][TEXT]     = $model->reductionTarget;
        $data["PREFECTURESCD"][TEXT]        = $fields["PREFECTURESCD"];
        $data["GRADE"][TEXT]                = $fields["GRADE"];
        $data["MONEY_DIV1"][TEXT]           = $fields["MONEY_DIV1"];
        $data["NUMERATOR1"][NUMBER]         = $fields["NUMERATOR1"];
        $data["DENOMINATOR1"][NUMBER]       = $fields["DENOMINATOR1"];
        $data["REDUCTIONMONEY_1"][NUMBER]   = is_null($fields["REDUCTIONMONEY_1"])? 0 : $fields["REDUCTIONMONEY_1"];
        $data["INCOME_LOW1"][NUMBER]        = $fields["INCOME_LOW1"];
        $data["INCOME_HIGH1"][NUMBER]       = $fields["INCOME_HIGH1"];
        $data["BURDEN_CHARGE_FLG1"][TEXT]   = $fields["BURDEN_CHARGE_FLG1"];
        $data["INCOME_RANK1"][TEXT]         = $fields["INCOME_RANK1"];
        $data["MONEY_DIV2"][TEXT]           = $fields["MONEY_DIV2"];
        $data["NUMERATOR2"][NUMBER]         = $fields["NUMERATOR2"];
        $data["DENOMINATOR2"][NUMBER]       = $fields["DENOMINATOR2"];
        $data["REDUCTIONMONEY_2"][NUMBER]   = is_null($fields["REDUCTIONMONEY_2"])? 0 : $fields["REDUCTIONMONEY_2"];
        $data["INCOME_LOW2"][NUMBER]        = $fields["INCOME_LOW2"];
        $data["INCOME_HIGH2"][NUMBER]       = $fields["INCOME_HIGH2"];
        $data["BURDEN_CHARGE_FLG2"][TEXT]   = $fields["BURDEN_CHARGE_FLG2"];
        $data["INCOME_RANK2"][TEXT]         = $fields["INCOME_RANK2"];
        $data["REDUCTIONREMARK"][TEXT]      = $fields["REDUCTIONREMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "reduction_mst");
        $db->query($query);

        $db->commit();

        //INSERT後右側画面の表示のための連番(reduction_seq)取得
        $query  = " SELECT MAX(REDUCTION_SEQ) ";
        $query .= "   FROM REDUCTION_MST ";
        $query .= "  WHERE YEAR             = '".$fields["YEAR"]."' ";
        $query .= "    AND SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "    AND SCHOOL_KIND      = '".$model->schoolKind."' ";
        $query .= "    AND REDUCTION_TARGET = '".$model->reductionTarget."' ";
        $query .= "    AND PREFECTURESCD    = '".$fields["PREFECTURESCD"]."' ";
        $query .= "    AND GRADE            = '".$fields["GRADE"]."' ";
        $query .= "    AND MONEY_DIV1       = '".$fields["MONEY_DIV1"]."' ";
        if ($fields["MONEY_DIV1"] == "1") {
            $query .= "    AND NUMERATOR1   =  ".$fields["NUMERATOR1"] ;
            $query .= "    AND DENOMINATOR1 =  ".$fields["DENOMINATOR1"] ;
        } else if ($fields["MONEY_DIV1"] == "2" || $fields["MONEY_DIV1"] == "3") {
            $query .= "    AND REDUCTIONMONEY_1 =  ".$fields["REDUCTIONMONEY_1"] ;
        }
        $query .= "    AND INCOME_RANK1     = '".$fields["INCOME_RANK1"]."' ";
        if ($model->reductionTarget == "1") {
            if ($fields["MONEY_DIV2"] == "1") {
                $query .= "    AND NUMERATOR2   =  ".$fields["NUMERATOR2"] ;
                $query .= "    AND DENOMINATOR2 =  ".$fields["DENOMINATOR2"] ;
            } else if ($fields["MONEY_DIV2"] == "2" || $fields["MONEY_DIV2"] == "3") {
                $query .= "    AND REDUCTIONMONEY_2 =  ".$fields["REDUCTIONMONEY_2"] ;
            }
            $query .= "    AND INCOME_RANK2     = '".$fields["INCOME_RANK2"]."' ";
        }

        $reduction_seq = $db->getOne($query);

        Query::dbCheckIn($db);
        return $reduction_seq;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields, $reduction_seq) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["MONEY_DIV1"][TEXT]           = $fields["MONEY_DIV1"];
        $data["NUMERATOR1"][NUMBER]         = $fields["NUMERATOR1"];
        $data["DENOMINATOR1"][NUMBER]       = $fields["DENOMINATOR1"];
        $data["REDUCTIONMONEY_1"][NUMBER]   = $fields["REDUCTIONMONEY_1"];
        $data["INCOME_LOW1"][NUMBER]        = $fields["INCOME_LOW1"];
        $data["INCOME_HIGH1"][NUMBER]       = $fields["INCOME_HIGH1"];
        $data["BURDEN_CHARGE_FLG1"][TEXT]   = $fields["BURDEN_CHARGE_FLG1"];
        $data["INCOME_RANK1"][TEXT]         = $fields["INCOME_RANK1"];
        $data["MONEY_DIV2"][TEXT]           = $fields["MONEY_DIV2"];
        $data["NUMERATOR2"][NUMBER]         = $fields["NUMERATOR2"];
        $data["DENOMINATOR2"][NUMBER]       = $fields["DENOMINATOR2"];
        $data["REDUCTIONMONEY_2"][NUMBER]   = $fields["REDUCTIONMONEY_2"];
        $data["INCOME_LOW2"][NUMBER]        = $fields["INCOME_LOW2"];
        $data["INCOME_HIGH2"][NUMBER]       = $fields["INCOME_HIGH2"];
        $data["BURDEN_CHARGE_FLG2"][TEXT]   = $fields["BURDEN_CHARGE_FLG2"];
        $data["INCOME_RANK2"][TEXT]         = $fields["INCOME_RANK2"];
        $data["REDUCTIONREMARK"][TEXT]      = $fields["REDUCTIONREMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE YEAR             = '".$fields["YEAR"]."' ";
        $where .= "   AND SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "   AND SCHOOL_KIND      = '".$model->schoolKind."' ";
        $where .= "   AND REDUCTION_TARGET =  '".$model->reductionTarget."' ";
        $where .= "   AND PREFECTURESCD    = '".$fields["PREFECTURESCD"]."' ";
        $where .= "   AND GRADE            = '".$fields["GRADE"]."' ";
        $where .= "   AND REDUCTION_SEQ    =  ".$reduction_seq ;

        $query = Query::updateSQL($data, "reduction_mst", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields, $reduction_seq) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM REDUCTION_MST ";
        $query .= " WHERE YEAR             = '".$fields["YEAR"]."' ";
        $query .= "   AND SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "   AND SCHOOL_KIND      = '".$model->schoolKind."' ";
        $query .= "   AND REDUCTION_TARGET = '".$model->reductionTarget."' ";
        $query .= "   AND PREFECTURESCD    = '".$fields["PREFECTURESCD"]."' ";
        $query .= "   AND GRADE            = '".$fields["GRADE"]."' ";
        $query .= "   AND REDUCTION_SEQ    =  ".$reduction_seq ;

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

    //reduction_seqがNULLのときにセット
    function getReduction_seq($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " SELECT REDUCTION_SEQ ";
        $query .= "   FROM REDUCTION_MST ";
        $query .= "  WHERE YEAR               = '".$model->year."' ";
        $query .= "    AND SCHOOLCD           = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "    AND SCHOOL_KIND        = '".$model->schoolKind."' ";
        $query .= "    AND REDUCTION_TARGET   = '".$model->reductionTarget."' ";
        $query .= "    AND PREFECTURESCD      = '".$fields["PREFECTURESCD"]."' ";
        $query .= "    AND GRADE              = '".$fields["GRADE"]."' ";

        $reduction_seq = $db->getOne($query);

        Query::dbCheckIn($db);
        return $reduction_seq;
    }

}
?>
