<?php

require_once('for_php7.php');

class knjz091kQuery extends Query {
    //データ取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.REDUCTIONMONEY, ";
        $query .= "     T1.REDUCTION_ADD_MONEY, ";
        $query .= "     T1.INCOME_LOW, ";
        $query .= "     T1.INCOME_HIGH, ";
        $query .= "     T1.REDUCTIONREMARK, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.REDUCTION_SEQ ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.INCOME_LOW, ";
        $query .= "     T1.INCOME_LOW ";

        return $query;
    }

    //学年
    function getGrade($year) {
        $query .= " SELECT DISTINCT GRADE ";
        $query .= "   FROM SCHREG_REGD_HDAT ";
        $query .= "  WHERE YEAR     = '".$year."' ";
        $query .= "    AND SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $flg) {
        $db = Query::dbCheckOut();

        $query  = " SELECT * ";
        $query .= "   FROM REDUCTION_COUNTRY_MST ";
        $query .= "  WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        if($flg == 1) {
            $query .= "    AND GRADE = '".$model->grade."' ";
            if ($model->reduction_seq)   $query .= "    AND reduction_seq  = ".$model->reduction_seq ;
        } elseif($flg == 2) {
            $query .= "    AND GRADE = '".$model->field["GRADE"]."' ";
            if($this->cmd == "update" || $this->cmd == "delete") {
                $query .= "    AND REDUCTION_SEQ = ".$model->reduction_seq ;
            } else {
                $query .= "    AND REDUCTION_ADD_MONEY = ".$model->field["REDUCTION_ADD_MONEY"] ;
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
        $query .= "     REDUCTION_COUNTRY_MST ";

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

        $query  = " INSERT INTO REDUCTION_COUNTRY_MST ";
        $query .= "                ( ";
        $query .= "                 YEAR, ";
        $query .= "                 GRADE, ";
        $query .= "                 REDUCTIONMONEY, ";
        $query .= "                 REDUCTION_ADD_MONEY, ";
        $query .= "                 INCOME_LOW, ";
        $query .= "                 INCOME_HIGH, ";
        $query .= "                 REDUCTIONREMARK, ";
        $query .= "                 REGISTERCD, ";
        $query .= "                 UPDATED ";
        $query .= "                ) ";
        $query .= "    (SELECT ";
        $query .= "         '".$year."', ";
        $query .= "         GRADE, ";
        $query .= "         REDUCTIONMONEY, ";
        $query .= "         REDUCTION_ADD_MONEY, ";
        $query .= "         INCOME_LOW, ";
        $query .= "         INCOME_HIGH, ";
        $query .= "         REDUCTIONREMARK, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         SYSDATE() ";
        $query .= "     FROM ";
        $query .= "         REDUCTION_COUNTRY_MST ";
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
        $reductionAddMoney = is_null($model->field["REDUCTION_ADD_MONEY"]) ? 0 : $model->field["REDUCTION_ADD_MONEY"];

        $query  = "";
        $query .= "  SELECT";
        $query .= "      COUNT(*)";
        $query .= "  FROM";
        $query .= "      REDUCTION_COUNTRY_MST";
        $query .= "  WHERE";
        $query .= "      YEAR = '{$model->year}' AND";
        $query .= "      GRADE = '{$model->field["GRADE"]}' AND";
        $query .= "      REDUCTION_ADD_MONEY = {$reductionAddMoney} ";

        if($model->cmd == "update") {
            $query .= "   AND REDUCTION_SEQ != ".$model->reduction_seq;
        }

        $cntch = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cntch;
    }

    //INSERT
    function &getInsertQuery($fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["YEAR"][TEXT]                     = $fields["YEAR"];
        $data["GRADE"][TEXT]                    = $fields["GRADE"];
        $data["REDUCTIONMONEY"][NUMBER]         = is_null($fields["REDUCTIONMONEY"])? 0 : $fields["REDUCTIONMONEY"];
        $data["REDUCTION_ADD_MONEY"][NUMBER]    = is_null($fields["REDUCTION_ADD_MONEY"])? 0 : $fields["REDUCTION_ADD_MONEY"];
        $data["INCOME_LOW"][NUMBER]             = $fields["INCOME_LOW"];
        $data["INCOME_HIGH"][NUMBER]            = $fields["INCOME_HIGH"];
        $data["REDUCTIONREMARK"][TEXT]          = $fields["REDUCTIONREMARK"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "sysdate()";

        $query = Query::insertSQL($data, "REDUCTION_COUNTRY_MST");
        $db->query($query);

        $db->commit();

        //INSERT後右側画面の表示のための連番(reduction_seq)取得
        $query  = " SELECT reduction_seq ";
        $query .= "   FROM REDUCTION_COUNTRY_MST ";
        $query .= "  WHERE YEAR           = '".CTRL_YEAR."' ";
        $query .= "    AND GRADE          = '".$fields["GRADE"]."' ";
        $query .= "    AND REDUCTION_ADD_MONEY =  ".$fields["REDUCTION_ADD_MONEY"] ;

        $reduction_seq = $db->getOne($query);

        Query::dbCheckIn($db);
        return $reduction_seq;
    }

    //UPDATE
    function &getUpdateQuery($fields,$reduction_seq) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["REDUCTIONMONEY"][NUMBER]         = $fields["REDUCTIONMONEY"];
        $data["REDUCTION_ADD_MONEY"][NUMBER]    = $fields["REDUCTION_ADD_MONEY"];
        $data["INCOME_LOW"][NUMBER]             = $fields["INCOME_LOW"];
        $data["INCOME_HIGH"][NUMBER]            = $fields["INCOME_HIGH"];
        $data["REDUCTIONREMARK"][TEXT]          = $fields["REDUCTIONREMARK"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "sysdate()";

        $where  = " WHERE YEAR          = '".$fields["YEAR"]."' ";
        $where .= "   AND REDUCTION_SEQ =  ".$reduction_seq ;
        $where .= "   AND GRADE         = '".$fields["GRADE"]."' ";

        $query = Query::updateSQL($data, "REDUCTION_COUNTRY_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields,$reduction_seq) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM REDUCTION_COUNTRY_MST ";
        $query .= " WHERE YEAR           = '".$fields["YEAR"]."' ";
        $query .= "   AND GRADE          = '".$fields["GRADE"]."' ";
        $query .= "   AND REDUCTION_SEQ  =  ".$reduction_seq ;

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

    function Income_Check1($model,$kind) {
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
        $query .= "                  max(income_high) as income_high";
        $query .= "              FROM";
        $query .= "                  REDUCTION_COUNTRY_MST";
        $query .= "              WHERE";
        $query .= "                  YEAR = '".$model->field["YEAR"]."'  AND";
        $query .= "                  GRADE = '".$model->field["GRADE"]."' AND";
        if($kind == "update") {
            $query .= "                  reduction_seq!=".$model->field["REDUCTION_SEQ"]." AND";
        }
        $query .= "                  income_low < ".$model->field["INCOME_LOW"]."";
        $query .= "              ) st1";
        $query .= "          WHERE";
        $query .= "              income_high >= ".$model->field["INCOME_LOW"]."";
        $query .= "  UNION  ";
        $query .= "          SELECT";
        $query .= "              0 as ng_low,";
        $query .= "              COUNT(*) as ng_high";
        $query .= "          FROM";
        $query .= "              (SELECT";
        $query .= "                  min(income_low) as income_low";
        $query .= "              FROM";
        $query .= "                  REDUCTION_COUNTRY_MST";
        $query .= "              WHERE";
        $query .= "                  YEAR = '".$model->field["YEAR"]."'  AND";
        $query .= "                  GRADE = '".$model->field["GRADE"]."' AND";
        if($kind == "update") {
            $query .= "                  reduction_seq!=".$model->field["REDUCTION_SEQ"]." AND";
        }
        $query .= "                  income_low > ".$model->field["INCOME_LOW"]."";
        $query .= "              ) st2";
        $query .= "          WHERE";
        $query .= "              income_low <= ".$model->field["INCOME_HIGH"]."";
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
