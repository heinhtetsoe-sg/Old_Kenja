<?php

require_once('for_php7.php');

class knjl083pQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     ABBV3, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     NAMECD2 ";
        }

        return $query;
    }

    //対象者・合格者取得
    function GetLeftList($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     CASE WHEN T1.PROCEDUREDIV = '1' THEN T1.PROCEDUREDATE END AS PRO_DATE, ";
        $query .= "     CASE WHEN T1.PROCEDUREDIV = '1' THEN '1' ELSE '0' END AS LEFT_FLG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "          ON N1.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "         AND N1.NAMECD1      = 'L013' ";
        $query .= "         AND N1.NAMECD2      = T1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR              = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV             = '".$model->applicantdiv."' AND ";
        $query .= "     T1.TESTDIV                  = '".$model->testdiv."' AND ";
        //合格者が対象
        $query .= "     N1.NAMESPARE1               = '1' AND ";
        //辞退者は除く
        $query .= "     VALUE(T1.PROCEDUREDIV,'')  != '2' AND ";    //手続区分 1:済み 2:無し（辞退）
        $query .= "     VALUE(T1.ENTDIV,'')        != '2' AND ";    //入学区分 1:済み 2:辞退
        //入学者は除く
        $query .= "     VALUE(T1.ENTDIV,'')        != '1' ";        //入学区分 1:済み 2:辞退
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //対象者リスト
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        //合格者リスト
        $array2 = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());

        /************************/
        /*  対象者（左リスト）  */
        /************************/
        foreach ($array as $examno) {
            //ENTEXAM_APPLICANTBASE_DAT
            //手続区分・日付をセット
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $baseRow = $db->getRow(" SELECT * FROM ENTEXAM_APPLICANTBASE_DAT " .$where, DB_FETCHMODE_ASSOC);
            $data = array();
            $data["PROCEDUREDIV"][TEXT]     = "1";      //手続区分 1:済み 2:無し
            if ($baseRow["PROCEDUREDATE"] == "") {
                $data["PROCEDUREDATE"][DATE]    = $model->pro_date;
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        /************************/
        /*  合格者（右リスト）  */
        /************************/
        foreach ($array2 as $examno) {
            //ENTEXAM_APPLICANTBASE_DAT
            //手続区分・日付をクリア
            $data = array();
            $data["PROCEDUREDIV"][TEXT]     = "";       //手続区分 1:済み 2:無し
            $data["PROCEDUREDATE"][DATE]    = "";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
