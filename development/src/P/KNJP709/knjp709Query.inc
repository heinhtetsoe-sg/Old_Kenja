<?php

require_once('for_php7.php');

class knjp709Query extends Query {

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
        $query .= "     REDM.SCHOOLDIV, ";
        $query .= "     REDM.SCHOOLDIV || ':' || N3.NAME1 AS SCHOOLDIV_NAME, ";
        $query .= "     REDM.GRADE, ";
        $query .= "     REDM.REDUCTIONMONEY1, ";
        $query .= "     REDM.REDUCTION_ADD_MONEY1, ";
        $query .= "     REDM.INCOME_LOW1, ";
        $query .= "     REDM.INCOME_HIGH1, ";
        $query .= "     N1.NAME1 AS INCOME_RANK1, ";
        $query .= "     REDM.REDUCTIONMONEY2, ";
        $query .= "     REDM.REDUCTION_ADD_MONEY2, ";
        $query .= "     REDM.INCOME_LOW2, ";
        $query .= "     REDM.INCOME_HIGH2, ";
        $query .= "     N2.NAME1 AS INCOME_RANK2, ";
        $query .= "     REDM.REDUCTIONREMARK, ";
        $query .= "     REDM.REDUCTION_SEQ ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_MST REDM ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ON N1.YEAR    = REDM.YEAR ";
        $query .= "                            AND N1.NAMECD1 = 'P001' ";
        $query .= "                            AND N1.NAMECD2 = REDM.INCOME_RANK1 ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ON N2.YEAR    = REDM.YEAR ";
        $query .= "                            AND N2.NAMECD1 = 'P001' ";
        $query .= "                            AND N2.NAMECD2 = REDM.INCOME_RANK2 ";
        $query .= "     LEFT JOIN V_NAME_MST N3 ON N3.YEAR    = REDM.YEAR ";
        $query .= "                            AND N3.NAMECD1 = 'Z001' ";
        $query .= "                            AND N3.NAMECD2 = REDM.SCHOOLDIV ";
        $query .= " WHERE ";
        $query .= "         REDM.YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND REDM.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND REDM.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " ORDER BY ";
        $query .= "     REDM.SCHOOLDIV, ";
        $query .= "     REDM.GRADE, ";
        $query .= "     REDM.INCOME_LOW1 ";

        return $query;
    }

    //授業料区分
    function getSchooldiv($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'Z001' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年
    function getGrade($model, $year) {
        $query  = " SELECT DISTINCT ";
        $query .= "     HDAT.GRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT HDAT ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON HDAT.YEAR  = GDAT.YEAR ";
        $query .= "                                    AND HDAT.GRADE = GDAT.GRADE ";
        $query .= " WHERE ";
        $query .= "         HDAT.YEAR        = '".$year."' ";
        $query .= "     AND HDAT.SEMESTER    = '".CTRL_SEMESTER."' ";
        $query .= "     AND GDAT.SCHOOL_KIND = '".$model->schoolKind."' ";

        return $query;
    }

    //ランク
    function getNameMst($year, $namecd, $namecd2) {
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
        $query .= "     REDUCTION_COUNTRY_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";

        if ($flg == 1) {
            $query .= "    AND SCHOOLDIV = '".$model->schooldiv."' ";
            $query .= "    AND GRADE = '".$model->grade."' ";
            if ($model->reduction_seq) {
                $query .= "    AND reduction_seq  = ".$model->reduction_seq ;
            }
        } else if ($flg == 2) {
            $query .= "    AND SCHOOLDIV = '".$model->field["SCHOOLDIV"]."' ";
            $query .= "    AND GRADE = '".$model->field["GRADE"]."' ";
            $query .= "    AND REDUCTION_SEQ = ".$model->reduction_seq ;
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
        $query .= "     CASE WHEN year = '".$year."'  THEN 'X' ";
        $query .= "          WHEN year = '".$Lyear."' THEN 'Y' end  AS CNT";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";

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

        $query  = " INSERT INTO REDUCTION_COUNTRY_MST ";
        $query .= "     ( ";
        $query .= "      SCHOOLCD, ";
        $query .= "      SCHOOL_KIND, ";
        $query .= "      YEAR, ";
        $query .= "      SCHOOLDIV, ";
        $query .= "      GRADE, ";
        $query .= "      REDUCTIONMONEY1, ";
        $query .= "      REDUCTION_ADD_MONEY1, ";
        $query .= "      INCOME_LOW1, ";
        $query .= "      INCOME_HIGH1, ";
        $query .= "      INCOME_RANK1, ";
        $query .= "      REDUCTIONMONEY2, ";
        $query .= "      REDUCTION_ADD_MONEY2, ";
        $query .= "      INCOME_LOW2, ";
        $query .= "      INCOME_HIGH2, ";
        $query .= "      INCOME_RANK2, ";
        $query .= "      REDUCTIONREMARK, ";
        $query .= "      REGISTERCD, ";
        $query .= "      UPDATED ";
        $query .= "     ) ";
        $query .= " (SELECT ";
        $query .= "      SCHOOLCD, ";
        $query .= "      SCHOOL_KIND, ";
        $query .= "      '".$year."', ";
        $query .= "      SCHOOLDIV, ";
        $query .= "      GRADE, ";
        $query .= "      REDUCTIONMONEY1, ";
        $query .= "      REDUCTION_ADD_MONEY1, ";
        $query .= "      INCOME_LOW1, ";
        $query .= "      INCOME_HIGH1, ";
        $query .= "      INCOME_RANK1, ";
        $query .= "      REDUCTIONMONEY2, ";
        $query .= "      REDUCTION_ADD_MONEY2, ";
        $query .= "      INCOME_LOW2, ";
        $query .= "      INCOME_HIGH2, ";
        $query .= "      INCOME_RANK2, ";
        $query .= "      REDUCTIONREMARK, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() ";
        $query .= "  FROM ";
        $query .= "      REDUCTION_COUNTRY_MST ";
        $query .= "  WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND YEAR        = '".($year - 1)."' ";
        $query .= "  ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHOOLCD"][TEXT]                 = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]              = $model->schoolKind;
        $data["YEAR"][TEXT]                     = $fields["YEAR"];
        $data["SCHOOLDIV"][TEXT]                = $fields["SCHOOLDIV"];
        $data["GRADE"][TEXT]                    = $fields["GRADE"];
        $data["REDUCTIONMONEY1"][NUMBER]        = is_null($fields["REDUCTIONMONEY1"])? 0 : $fields["REDUCTIONMONEY1"];
        $data["REDUCTION_ADD_MONEY1"][NUMBER]   = is_null($fields["REDUCTION_ADD_MONEY1"])? 0 : $fields["REDUCTION_ADD_MONEY1"];
        $data["INCOME_LOW1"][NUMBER]            = $fields["INCOME_LOW1"];
        $data["INCOME_HIGH1"][NUMBER]           = $fields["INCOME_HIGH1"];
        $data["INCOME_RANK1"][TEXT]             = $fields["INCOME_RANK1"];
        $data["REDUCTIONMONEY2"][NUMBER]        = is_null($fields["REDUCTIONMONEY2"])? 0 : $fields["REDUCTIONMONEY2"];
        $data["REDUCTION_ADD_MONEY2"][NUMBER]   = is_null($fields["REDUCTION_ADD_MONEY2"])? 0 : $fields["REDUCTION_ADD_MONEY2"];
        $data["INCOME_LOW2"][NUMBER]            = $fields["INCOME_LOW2"];
        $data["INCOME_HIGH2"][NUMBER]           = $fields["INCOME_HIGH2"];
        $data["INCOME_RANK2"][TEXT]             = $fields["INCOME_RANK2"];
        $data["REDUCTIONREMARK"][TEXT]          = $fields["REDUCTIONREMARK"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "sysdate()";

        $query = Query::insertSQL($data, "REDUCTION_COUNTRY_MST");
        $db->query($query);

        $db->commit();

        //INSERT後右側画面の表示のための連番(reduction_seq)取得
        $query  = " SELECT REDUCTION_SEQ ";
        $query .= "   FROM REDUCTION_COUNTRY_MST ";
        $query .= "  WHERE SCHOOLCD             = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "    AND SCHOOL_KIND          = '".$model->schoolKind."' ";
        $query .= "    AND YEAR                 = '".CTRL_YEAR."' ";
        $query .= "    AND SCHOOLDIV            = '".$fields["SCHOOLDIV"]."' ";
        $query .= "    AND GRADE                = '".$fields["GRADE"]."' ";
        $query .= "    AND REDUCTIONMONEY1      =  ".$fields["REDUCTIONMONEY1"] ;
        $query .= "    AND REDUCTION_ADD_MONEY1 =  ".$fields["REDUCTION_ADD_MONEY1"] ;
        $query .= "    AND REDUCTIONMONEY2      =  ".$fields["REDUCTIONMONEY2"] ;
        $query .= "    AND REDUCTION_ADD_MONEY2 =  ".$fields["REDUCTION_ADD_MONEY2"] ;

        $reduction_seq = $db->getOne($query);

        Query::dbCheckIn($db);
        return $reduction_seq;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields, $reduction_seq) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["REDUCTIONMONEY1"][NUMBER]        = $fields["REDUCTIONMONEY1"];
        $data["REDUCTION_ADD_MONEY1"][NUMBER]   = $fields["REDUCTION_ADD_MONEY1"];
        $data["INCOME_LOW1"][NUMBER]            = $fields["INCOME_LOW1"];
        $data["INCOME_HIGH1"][NUMBER]           = $fields["INCOME_HIGH1"];
        $data["INCOME_RANK1"][TEXT]             = $fields["INCOME_RANK1"];
        $data["REDUCTIONMONEY2"][NUMBER]        = $fields["REDUCTIONMONEY2"];
        $data["REDUCTION_ADD_MONEY2"][NUMBER]   = $fields["REDUCTION_ADD_MONEY2"];
        $data["INCOME_LOW2"][NUMBER]            = $fields["INCOME_LOW2"];
        $data["INCOME_HIGH2"][NUMBER]           = $fields["INCOME_HIGH2"];
        $data["INCOME_RANK2"][TEXT]             = $fields["INCOME_RANK2"];
        $data["REDUCTIONREMARK"][TEXT]          = $fields["REDUCTIONREMARK"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "sysdate()";

        $where  = " WHERE SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "   AND SCHOOL_KIND   = '".$model->schoolKind."' ";
        $where .= "   AND YEAR          = '".$fields["YEAR"]."' ";
        $where .= "   AND REDUCTION_SEQ =  ".$reduction_seq ;
        $where .= "   AND SCHOOLDIV     = '".$fields["SCHOOLDIV"]."' ";
        $where .= "   AND GRADE         = '".$fields["GRADE"]."' ";

        $query = Query::updateSQL($data, "REDUCTION_COUNTRY_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields, $reduction_seq) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM REDUCTION_COUNTRY_MST ";
        $query .= " WHERE SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "   AND SCHOOL_KIND   = '".$model->schoolKind."' ";
        $query .= "   AND YEAR          = '".$fields["YEAR"]."' ";
        $query .= "   AND SCHOOLDIV     = '".$fields["SCHOOLDIV"]."' ";
        $query .= "   AND GRADE         = '".$fields["GRADE"]."' ";
        $query .= "   AND REDUCTION_SEQ =  ".$reduction_seq ;

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

    function Income_Check($model, $kind, $fieldNum) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     sum(NG_LOW)  AS NG_LOW, ";
        $query .= "     sum(NG_HIGH) AS NG_HIGH ";
        $query .= " FROM ";
        $query .= " ( ";
        $query .= "         SELECT ";
        $query .= "             COUNT(*) AS NG_LOW, ";
        $query .= "             0 AS NG_HIGH ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                  max(INCOME_HIGH{$fieldNum}) AS INCOME_HIGH ";
        $query .= "              FROM";
        $query .= "                  REDUCTION_COUNTRY_MST ";
        $query .= "              WHERE";
        $query .= "                      SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "                  AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "                  AND YEAR        = '".$model->field["YEAR"]."' ";
        $query .= "                  AND SCHOOLDIV   = '".$model->field["SCHOOLDIV"]."' ";
        $query .= "                  AND GRADE       = '".$model->field["GRADE"]."' ";
        if($kind == "update") {
            $query .= "                  AND REDUCTION_SEQ != ".$model->field["REDUCTION_SEQ"]." ";
        }
        $query .= "                  AND INCOME_LOW{$fieldNum} < ".$model->field["INCOME_LOW".$fieldNum]." ";
        $query .= "             ) st1 ";
        $query .= "         WHERE ";
        $query .= "             INCOME_HIGH >= ".$model->field["INCOME_LOW".$fieldNum]." ";
        $query .= " UNION ";
        $query .= "         SELECT ";
        $query .= "             0 AS NG_LOW, ";
        $query .= "             COUNT(*) AS NG_HIGH ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                  min(INCOME_LOW{$fieldNum}) AS INCOME_LOW ";
        $query .= "              FROM ";
        $query .= "                  REDUCTION_COUNTRY_MST ";
        $query .= "              WHERE ";
        $query .= "                      SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "                  AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "                  AND YEAR        = '".$model->field["YEAR"]."' ";
        $query .= "                  AND SCHOOLDIV   = '".$model->field["SCHOOLDIV"]."' ";
        $query .= "                  AND GRADE       = '".$model->field["GRADE"]."' ";
        if($kind == "update") {
            $query .= "                  AND REDUCTION_SEQ != ".$model->field["REDUCTION_SEQ"]." ";
        }
        $query .= "                  AND INCOME_LOW{$fieldNum} > ".$model->field["INCOME_LOW".$fieldNum]." ";
        $query .= "             ) st2 ";
        $query .= "         WHERE ";
        $query .= "             INCOME_LOW <= ".$model->field["INCOME_HIGH".$fieldNum]." ";
        $query .= "     ) t1 ";

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
