<?php

require_once('for_php7.php');

class knjl033bQuery extends Query {

    //入試年度一覧取得
    function selectYearQuery()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_MONEY_YMST ";
        $query .= " ORDER BY ";
        $query .= "     ENTEXAMYEAR DESC ";

        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model)
    {
        $query  = " SELECT ";
        $query .= "     MIN(ENTEXAMYEAR) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR >= '{$model->year}' ";

        return $query;
    }

    //データ取得
    function selectQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.NAME1 AS APPLICANTNAME, ";
        $query .= "     T3.NAME1 AS JUDGENAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_MONEY_YMST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ";
        $query .= "                  ON T1.ENTEXAMYEAR  = T2.YEAR ";
        $query .= "                 AND T1.APPLICANTDIV = T2.NAMECD2 AND T2.NAMECD1 = 'L003' ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ";
        $query .= "                  ON T1.ENTEXAMYEAR = T3.YEAR ";
        $query .= "                 AND T1.JUDGE_KIND  = T3.NAMECD2 AND T3.NAMECD1 = 'L025' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.JUDGE_KIND, ";
        $query .= "     T1.SCHOLARSHIP_DIV ";

        return $query;
    }

    //名称マスタより取得
    function getNameMst($model, $namecd1, $name, $namecd2="") {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        if($name == "JUDGE_KIND") {
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     '0' AS VALUE, ";
            $query .= "     '0:基準' AS LABEL, ";
            $query .= "     '', ";
            $query .= "     '' ";
            $query .= " FROM ";
            $query .= "     V_NAME_MST ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$model->year."' AND ";
            $query .= "     NAMECD1 = '".$namecd1."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得
    function getRow($year, $applicantdiv, $judge_kind)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_MONEY_YMST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$year."' ";
        $query .= " AND APPLICANTDIV = '".$applicantdiv."' ";
        $query .= " AND JUDGE_KIND   = '".$judge_kind."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        //奨学生区分取得
        $name_mst = $db->getRow(knjl033bQuery::getNameMst($model, "L025", "NAMESPARE", $fields["JUDGE_KIND"]), DB_FETCHMODE_ASSOC);
        if($name_mst["NAMESPARE1"] == "1") {
            $scholarship_div = "2";
        } else if($name_mst["NAMESPARE2"] == "1") {
            $scholarship_div = "3";
        } else if($fields["JUDGE_KIND"] == "0") {
            $scholarship_div = "1";
        } else {
            $scholarship_div = "";
        }
        $data["ENTEXAMYEAR"][TEXT]          = $model->year;
        $data["APPLICANTDIV"][TEXT]         = $fields["APPLICANTDIV"];
        $data["JUDGE_KIND"][TEXT]           = $fields["JUDGE_KIND"];
        $data["SCHOLARSHIP_DIV"][TEXT]      = $scholarship_div;
        $data["ENT_MONEY"][NUMBER]          = $fields["ENT_MONEY"];
        $data["ENT_MONEY_NAME"][TEXT]       = $fields["ENT_MONEY_NAME"];
        $data["FAC_MONEY"][NUMBER]          = $fields["FAC_MONEY"];
        $data["FAC_MONEY_NAME"][TEXT]       = $fields["FAC_MONEY_NAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_PAYMENT_MONEY_YMST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        //奨学生区分取得
        $name_mst = $db->getRow(knjl033bQuery::getNameMst($model, "L025", $fields["JUDGE_KIND"]), DB_FETCHMODE_ASSOC);
        if($name_mst["NAMESPARE1"] == "1") {
            $scholarship_div = "2";
        } else if($name_mst["NAMESPARE2"] == "1") {
            $scholarship_div = "3";
        } else if($fields["JUDGE_KIND"] == "0") {
            $scholarship_div = "1";
        } else {
            $scholarship_div = "";
        }

        $data["APPLICANTDIV"][TEXT]         = $fields["APPLICANTDIV"];
        $data["JUDGE_KIND"][TEXT]           = $fields["JUDGE_KIND"];
        $data["SCHOLARSHIP_DIV"][TEXT]      = $scholarship_div;
        $data["ENT_MONEY"][NUMBER]          = $fields["ENT_MONEY"];
        $data["ENT_MONEY_NAME"][TEXT]       = $fields["ENT_MONEY_NAME"];
        $data["FAC_MONEY"][NUMBER]          = $fields["FAC_MONEY"];
        $data["FAC_MONEY_NAME"][TEXT]       = $fields["FAC_MONEY_NAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        
        $where  = "   WHERE ENTEXAMYEAR  = '{$model->year}' ";
        $where .= "     AND APPLICANTDIV = '".$fields["APPLICANTDIV"]."'";
        $where .= "     AND JUDGE_KIND   = '".$fields["JUDGE_KIND"]."'";

        $query = Query::updateSQL($data, "ENTEXAM_PAYMENT_MONEY_YMST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_PAYMENT_MONEY_YMST ";
        $query .= "     WHERE  ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '".$fields["APPLICANTDIV"]."'";
        $query .= "     AND JUDGE_KIND   = '".$fields["JUDGE_KIND"]."'";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }

    //ある年度にデータが存在するのかチェック
    function getCheckPaymentMoneyYmstSql($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_MONEY_YMST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //COPY
    function getCopyQuery($thisYear, $nextYear) {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_PAYMENT_MONEY_YMST ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     JUDGE_KIND, ";
        $query .= "     SCHOLARSHIP_DIV, ";
        $query .= "     JUDGE_KIND_NAME, ";
        $query .= "     ENT_MONEY, ";
        $query .= "     ENT_MONEY_NAME, ";
        $query .= "     FAC_MONEY, ";
        $query .= "     FAC_MONEY_NAME, ";
        $query .= "     LESSON_MONEY, ";
        $query .= "     LESSON_MONEY_NAME, ";
        $query .= "     FAC_MNT_MONEY, ";
        $query .= "     FAC_MNT_MONEY_NAME, ";        
        $query .= "     '". STAFFCD ."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_PAYMENT_MONEY_YMST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$thisYear}' ";

        return $query;
    }
}
?>
