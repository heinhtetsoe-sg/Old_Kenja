<?php

require_once('for_php7.php');

class knjl233yQuery extends Query {

    //データ取得
    function selectQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.NAME1 AS APPLICANTNAME, ";
        $query .= "     T3.NAME1 AS JUDGENAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_MONEY_MST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ";
        $query .= "                  ON T2.YEAR = '".$model->ObjYear."' ";
        $query .= "                 AND T1.APPLICANTDIV = T2.NAMECD2 AND T2.NAMECD1 = 'L003' ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ";
        $query .= "                  ON T3.YEAR = '".$model->ObjYear."' ";
        $query .= "                 AND T1.JUDGE_KIND = T3.NAMECD2 AND T3.NAMECD1 = 'L025' ";
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
        $query .= "     YEAR = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得
    function getRow($applicantdiv, $judge_kind)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_MONEY_MST ";
        $query .= " WHERE ";
        $query .= "     APPLICANTDIV = '".$applicantdiv."' AND ";
        $query .= "     JUDGE_KIND   = '".$judge_kind."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        //奨学生区分取得
        $name_mst = $db->getRow(knjl233yQuery::getNameMst($model, "L025", "NAMESPARE", $fields["JUDGE_KIND"]), DB_FETCHMODE_ASSOC);
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
        $data["JUDGE_KIND_NAME"][TEXT]      = $fields["JUDGE_KIND_NAME"];
        $data["ENT_MONEY"][NUMBER]          = $fields["ENT_MONEY"];
        $data["ENT_MONEY_NAME"][TEXT]       = $fields["ENT_MONEY_NAME"];
        $data["FAC_MONEY"][NUMBER]          = $fields["FAC_MONEY"];
        $data["FAC_MONEY_NAME"][TEXT]       = $fields["FAC_MONEY_NAME"];
        $data["LESSON_MONEY"][NUMBER]       = $fields["LESSON_MONEY"];
        $data["LESSON_MONEY_NAME"][TEXT]    = $fields["LESSON_MONEY_NAME"];
        $data["FAC_MNT_MONEY"][NUMBER]      = $fields["FAC_MNT_MONEY"];
        $data["FAC_MNT_MONEY_NAME"][TEXT]   = $fields["FAC_MNT_MONEY_NAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_PAYMENT_MONEY_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        //奨学生区分取得
        $name_mst = $db->getRow(knjl233yQuery::getNameMst($model, "L025", $fields["JUDGE_KIND"]), DB_FETCHMODE_ASSOC);
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
        $data["JUDGE_KIND_NAME"][TEXT]      = $fields["JUDGE_KIND_NAME"];
        $data["ENT_MONEY"][NUMBER]          = $fields["ENT_MONEY"];
        $data["ENT_MONEY_NAME"][TEXT]       = $fields["ENT_MONEY_NAME"];
        $data["FAC_MONEY"][NUMBER]          = $fields["FAC_MONEY"];
        $data["FAC_MONEY_NAME"][TEXT]       = $fields["FAC_MONEY_NAME"];
        $data["LESSON_MONEY"][NUMBER]       = $fields["LESSON_MONEY"];
        $data["LESSON_MONEY_NAME"][TEXT]    = $fields["LESSON_MONEY_NAME"];
        $data["FAC_MNT_MONEY"][NUMBER]      = $fields["FAC_MNT_MONEY"];
        $data["FAC_MNT_MONEY_NAME"][TEXT]   = $fields["FAC_MNT_MONEY_NAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        
        $where  = " WHERE APPLICANTDIV  = '".$fields["APPLICANTDIV"]."'";
        $where .= "   AND JUDGE_KIND    = '".$fields["JUDGE_KIND"]."'";

        $query = Query::updateSQL($data, "ENTEXAM_PAYMENT_MONEY_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_PAYMENT_MONEY_MST ";
        $query .= "       WHERE APPLICANTDIV = '".$fields["APPLICANTDIV"]."'";
        $query .= "         AND JUDGE_KIND   = '".$fields["JUDGE_KIND"]."'";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }
}
?>