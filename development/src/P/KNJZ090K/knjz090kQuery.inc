<?php

require_once('for_php7.php');

class knjz090kQuery extends Query {
    //データ取得
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

    //データ取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.PREFECTURESCD, ";
        $query .= "     T2.NAME1, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.REDUCTIONMONEY_1, ";
        $query .= "     T1.INCOME_LOW1, ";
        $query .= "     T1.INCOME_HIGH1, ";
        $query .= "     T1.INCOME_SIBLINGS1, ";
        $query .= "     CASE WHEN (T1.YEAR < '2016') OR (T1.YEAR = '2016' AND T1.GRADE > '01') OR (T1.YEAR = '2017' AND T1.GRADE = '03') ";
        $query .= "          THEN G213_1.NAME1 ";
        $query .= "          ELSE G218_1.NAME1 ";
        $query .= "     END AS INCOME_RANK1, ";
        $query .= "     T1.REDUCTIONMONEY_2, ";
        $query .= "     T1.INCOME_LOW2, ";
        $query .= "     T1.INCOME_HIGH2, ";
        $query .= "     T1.INCOME_SIBLINGS2, ";
        $query .= "     CASE WHEN (T1.YEAR < '2016') OR (T1.YEAR = '2016' AND T1.GRADE > '01') OR (T1.YEAR = '2017' AND T1.GRADE = '03') ";
        $query .= "          THEN G213_2.NAME1 ";
        $query .= "          ELSE G218_2.NAME1 ";
        $query .= "     END AS INCOME_RANK2, ";
        $query .= "     T1.REDUCTIONREMARK, ";
        $query .= "     T1.REDUCTION_SEQ ";
        $query .= " FROM ";
        $query .= "     REDUCTION_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST G213_1  ON G213_1.NAMECD2 = T1.INCOME_RANK1 ";
        $query .= "          AND G213_1.NAMECD1 = 'G213' ";
        $query .= "     LEFT JOIN NAME_MST G218_1  ON G218_1.NAMECD2 = T1.INCOME_RANK1 ";
        $query .= "          AND G218_1.NAMECD1 = 'G218' ";
        $query .= "     LEFT JOIN NAME_MST G213_2  ON G213_2.NAMECD2 = T1.INCOME_RANK2 ";
        $query .= "          AND G213_2.NAMECD1 = 'G213' ";
        $query .= "     LEFT JOIN NAME_MST G218_2  ON G218_2.NAMECD2 = T1.INCOME_RANK2 ";
        $query .= "          AND G218_2.NAMECD1 = 'G218', ";
        $query .= "     NAME_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR          = '".$model->year."' AND ";
        $query .= "     T1.PREFECTURESCD = T2.NAMECD2 AND ";
        $query .= "     T2.NAMECD1       = 'G202' ";
        $query .= " ORDER BY ";
        $query .= "     T1.PREFECTURESCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     INCOME_RANK1, ";
        $query .= "     T1.INCOME_HIGH1 ";

        return $query;
    }

    //都道府県コード取得
    function getName($year) {
        $query  = " SELECT namecd2,name1 ";
        $query .= "   FROM v_name_mst ";
        $query .= "  WHERE year    = '".$year."' ";
        $query .= "    AND namecd1 = 'G202' ";

        return $query;
    }

    //学年
    function getGrade($year) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }
    
    //ランク
    function getNameMst($year, $namecd, $namecd2) {
        $query  = " SELECT ";
        if ($namecd === 'G213' || $namecd === 'G218') {
            $query .= "      NAMECD2 AS VALUE, ";
            $query .= "      NAMECD2 || ':' || NAME1 AS LABEL";
        } else if ($namecd === 'G202') {
            $query .= "      ABBV1 ";
        }
        $query .= " FROM  ";
        $query .= "      V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' ";
        $query .= " AND NAMECD1 = '".$namecd."' ";
        if ($namecd === 'G202'){
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        return $query;
    }

    //１レコード取得
    function getRow($model,$flg) {
        $db = Query::dbCheckOut();

        $query  = " SELECT * ";
        $query .= "   FROM reduction_mst ";
        $query .= "  WHERE year           = '".$model->year."' ";

        if($flg == 1) {
            $query .= "    AND prefecturescd  = '".$model->prefecturescd."' ";
            $query .= "    AND grade          = '".$model->grade."' ";

            if($model->reduction_seq)   $query .= "    AND reduction_seq  = ".$model->reduction_seq ;

        } elseif($flg == 2) {
            $query .= "    AND prefecturescd = '".$model->field["PREFECTURESCD"]."' ";
            $query .= "    AND grade         = '".$model->field["GRADE"]."' ";

            if($this->cmd == "update" || $this->cmd == "delete") {
                $query .= "    AND reduction_seq = ".$model->reduction_seq ;
            } else {
                $query .= "    AND reductionmoney_1 = ".$model->field["REDUCTIONMONEY_1"] ;
                $query .= "    AND reductionmoney_2 = ".$model->field["REDUCTIONMONEY_2"] ;
            }
        }

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    //今年度のデータが存在するかチェック
    function CheckData($year) {
        $Lyear = $year - 1; //前年度チェック用

        $db = Query::dbCheckOut();

        $query  = " SELECT DISTINCT ";
        $query .= "     CASE WHEN year = '".$year."'  THEN 'X' ";
        $query .= "          WHEN year = '".$Lyear."' THEN 'Y' end  AS CNT";
        $query .= " FROM ";
        $query .= "     reduction_mst ";

        $result = $db->query($query);
        $opt_year = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year[] = $row["CNT"];
        }

        Query::dbCheckIn($db);
        return $opt_year;
    }

    //前年度のデータをコピー
    function &getCopyQuery($year) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO REDUCTION_MST ";
        $query .= "                ( ";
        $query .= "                 YEAR, ";
        $query .= "                 PREFECTURESCD, ";
        $query .= "                 GRADE, ";
        $query .= "                 REDUCTIONMONEY_1, ";
        $query .= "                 INCOME_LOW1, ";
        $query .= "                 INCOME_HIGH1, ";
        $query .= "                 INCOME_SIBLINGS1, ";
        $query .= "                 INCOME_RANK1, ";
        $query .= "                 REDUCTIONMONEY_2, ";
        $query .= "                 INCOME_LOW2, ";
        $query .= "                 INCOME_HIGH2, ";
        $query .= "                 INCOME_SIBLINGS2, ";
        $query .= "                 INCOME_RANK2, ";
        $query .= "                 REDUCTIONREMARK, ";
        $query .= "                 REGISTERCD, ";
        $query .= "                 UPDATED ";
        $query .= "                ) ";
        $query .= "    (SELECT ";
        $query .= "         '".$year."', ";
        $query .= "         PREFECTURESCD, ";
        $query .= "         GRADE, ";
        $query .= "         REDUCTIONMONEY_1, ";
        $query .= "         INCOME_LOW1, ";
        $query .= "         INCOME_HIGH1, ";
        $query .= "         INCOME_SIBLINGS1, ";
        $query .= "         INCOME_RANK1, ";
        $query .= "         REDUCTIONMONEY_2, ";
        $query .= "         INCOME_LOW2, ";
        $query .= "         INCOME_HIGH2, ";
        $query .= "         INCOME_SIBLINGS2, ";
        $query .= "         INCOME_RANK2, ";
        $query .= "         REDUCTIONREMARK, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         SYSDATE() ";
        $query .= "     FROM ";
        $query .= "         REDUCTION_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".($year - 1)."'";
        $query .= "     ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //整合性チェック
    function getCntch($model) {
        $db = Query::dbCheckOut();
        $reductionmoney_1 = is_null($model->field["REDUCTIONMONEY_1"]) ? 0 : $model->field["REDUCTIONMONEY_1"];
        $reductionmoney_2 = is_null($model->field["REDUCTIONMONEY_2"]) ? 0 : $model->field["REDUCTIONMONEY_2"];

        $query  = "";
        $query .= "  SELECT";
        $query .= "      COUNT(*)";
        $query .= "  FROM";
        $query .= "      reduction_mst";
        $query .= "  WHERE";
        $query .= "      year             = '{$model->year}' AND";
        $query .= "      prefecturescd    = '{$model->field["PREFECTURESCD"]}' AND";
        $query .= "      grade            = '{$model->field["GRADE"]}' AND";
        $query .= "      reductionmoney_1 = {$reductionmoney_1} AND ";
        $query .= "      reductionmoney_2 = {$reductionmoney_2}";

        if($model->cmd == "update") {
            $query .= "   AND reduction_seq != ".$model->reduction_seq;
        }

        $cntch = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cntch;
    }

    //INSERT
    function &getInsertQuery($fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["YEAR"][TEXT]               = $fields["YEAR"];
        $data["PREFECTURESCD"][TEXT]      = $fields["PREFECTURESCD"];
        $data["GRADE"][TEXT]              = $fields["GRADE"];
        $data["REDUCTIONMONEY_1"][NUMBER] = is_null($fields["REDUCTIONMONEY_1"])? 0 : $fields["REDUCTIONMONEY_1"];
        $data["INCOME_LOW1"][NUMBER]      = $fields["INCOME_LOW1"];
        $data["INCOME_HIGH1"][NUMBER]     = $fields["INCOME_HIGH1"];
        $data["INCOME_SIBLINGS1"][NUMBER] = $fields["INCOME_SIBLINGS1"];
        $data["INCOME_RANK1"][TEXT]       = $fields["INCOME_RANK1"];
        $data["REDUCTIONMONEY_2"][NUMBER] = is_null($fields["REDUCTIONMONEY_2"])? 0 : $fields["REDUCTIONMONEY_2"];
        $data["INCOME_LOW2"][NUMBER]      = $fields["INCOME_LOW2"];
        $data["INCOME_HIGH2"][NUMBER]     = $fields["INCOME_HIGH2"];
        $data["INCOME_SIBLINGS2"][NUMBER] = $fields["INCOME_SIBLINGS2"];
        $data["INCOME_RANK2"][TEXT]       = $fields["INCOME_RANK2"];
        $data["REDUCTIONREMARK"][TEXT]    = $fields["REDUCTIONREMARK"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][FUNC]            = "sysdate()";

        $query = Query::insertSQL($data, "REDUCTION_MST");
        $db->query($query);

        $db->commit();

        //INSERT後右側画面の表示のための連番(reduction_seq)取得
        $query  = " SELECT REDUCTION_SEQ ";
        $query .= "   FROM REDUCTION_MST ";
        $query .= "  WHERE YEAR             = '".$fields["YEAR"]."' ";
        $query .= "    AND PREFECTURESCD    = '".$fields["PREFECTURESCD"]."' ";
        $query .= "    AND GRADE            = '".$fields["GRADE"]."' ";
        $query .= "    AND REDUCTIONMONEY_1 =  ".$fields["REDUCTIONMONEY_1"] ;
        $query .= "    AND REDUCTIONMONEY_2 =  ".$fields["REDUCTIONMONEY_2"] ;
        $query .= "    AND INCOME_RANK1     = '".$fields["INCOME_RANK1"]."' ";

        $reduction_seq = $db->getOne($query);

        Query::dbCheckIn($db);
        return $reduction_seq;
    }

    //UPDATE
    function &getUpdateQuery($fields,$reduction_seq) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["REDUCTIONMONEY_1"][NUMBER] = $fields["REDUCTIONMONEY_1"];
        $data["INCOME_LOW1"][NUMBER]      = $fields["INCOME_LOW1"];
        $data["INCOME_HIGH1"][NUMBER]     = $fields["INCOME_HIGH1"];
        $data["INCOME_SIBLINGS1"][NUMBER] = $fields["INCOME_SIBLINGS1"];
        $data["INCOME_RANK1"][TEXT]       = $fields["INCOME_RANK1"];
        $data["REDUCTIONMONEY_2"][NUMBER] = $fields["REDUCTIONMONEY_2"];
        $data["INCOME_LOW2"][NUMBER]      = $fields["INCOME_LOW2"];
        $data["INCOME_HIGH2"][NUMBER]     = $fields["INCOME_HIGH2"];
        $data["INCOME_SIBLINGS2"][NUMBER] = $fields["INCOME_SIBLINGS2"];
        $data["INCOME_RANK2"][TEXT]       = $fields["INCOME_RANK2"];
        $data["REDUCTIONREMARK"][TEXT]    = $fields["REDUCTIONREMARK"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][FUNC]            = "sysdate()";

        $where  = " WHERE YEAR          = '".$fields["YEAR"]."' ";
        $where .= "   AND PREFECTURESCD = '".$fields["PREFECTURESCD"]."' ";
        $where .= "   AND GRADE         = '".$fields["GRADE"]."' ";
        $where .= "   AND REDUCTION_SEQ =  ".$reduction_seq ;

        $query = Query::updateSQL($data, "REDUCTION_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields,$reduction_seq) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM REDUCTION_MST ";
        $query .= " WHERE YEAR           = '".$fields["YEAR"]."' ";
        $query .= "   AND PREFECTURESCD  = '".$fields["PREFECTURESCD"]."' ";
        $query .= "   AND GRADE          = '".$fields["GRADE"]."' ";
        $query .= "   AND REDUCTION_SEQ  =  ".$reduction_seq ;

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
        $query .= "  WHERE YEAR           = '".$model->year."' ";
        $query .= "    AND PREFECTURESCD  = '".$fields["PREFECTURESCD"]."' ";
        $query .= "    AND GRADE          = '".$fields["GRADE"]."' ";
        
        $reduction_seq = $db->getOne($query);
        
        Query::dbCheckIn($db);
        return $reduction_seq;
    }


    function Income_Check($model, $kind, $num) {
        $db = Query::dbCheckOut();
        
        $query  = "  SELECT";
        $query .= "      sum(ng_low)  as ng_low,";
        $query .= "      sum(ng_high) as ng_high";
        $query .= "  FROM";
        $query .= "  (";
        $query .= "          SELECT";
        $query .= "              COUNT(*) as ng_low,";
        $query .= "              0 as ng_high";
        $query .= "          FROM";
        $query .= "              (SELECT";
        $query .= "                  max(INCOME_HIGH{$num}) as INCOME_HIGH{$num}";
        $query .= "              FROM";
        $query .= "                  REDUCTION_MST";
        $query .= "              WHERE";
        $query .= "                  year='".$model->field["YEAR"]."'  AND";
        $query .= "                  prefecturescd='".$model->field["PREFECTURESCD"]."' AND";
        $query .= "                  grade='".$model->field["GRADE"]."' AND";
        if($kind == "update") {
            $query .= "                  reduction_seq!=".$model->field["REDUCTION_SEQ"]." AND";
        }
        $query .= "                  INCOME_LOW{$num} < ".$model->field["INCOME_LOW{$num}"]."";
        $query .= "              ) st1";
        $query .= "          WHERE";
        $query .= "              INCOME_HIGH{$num} >= ".$model->field["INCOME_LOW{$num}"]."";
        $query .= "  UNION  ";
        $query .= "          SELECT";
        $query .= "              0 as ng_low,";
        $query .= "              COUNT(*) as ng_high";
        $query .= "          FROM";
        $query .= "              (SELECT";
        $query .= "                  min(INCOME_LOW{$num}) as INCOME_LOW{$num}";
        $query .= "              FROM";
        $query .= "                  REDUCTION_MST";
        $query .= "              WHERE";
        $query .= "                  year='".$model->field["YEAR"]."'  AND";
        $query .= "                  prefecturescd='".$model->field["PREFECTURESCD"]."' AND";
        $query .= "                  grade='".$model->field["GRADE"]."' AND";
        if($kind == "update") {
            $query .= "                  reduction_seq!=".$model->field["REDUCTION_SEQ"]." AND";
        }
        $query .= "                  INCOME_LOW{$num} > ".$model->field["INCOME_LOW{$num}"]."";
        $query .= "              ) st2";
        $query .= "          WHERE";
        $query .= "              INCOME_LOW{$num} <= ".$model->field["INCOME_HIGH{$num}"]."";
        $query .= "      ) t1";

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if($Row["NG_LOW"]==1) {
            return "low";
        } elseif($Row["NG_HIGH"]==1) {
            return "high";
        } else {
            return "OK";
        }

        Query::dbCheckIn($db);
    }
}
?>
