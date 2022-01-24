<?php

require_once('for_php7.php');
class knjl580fQuery extends Query {

    //名称
    function GetName($namecd1, $ObjYear) {
        $query  = " SELECT ";
        $query .= "     namecd2, ";
        $query .= "     name1, ";
        $query .= "     name2, ";
        $query .= "     namespare1, ";
        $query .= "     namespare2, ";
        $query .= "     ABBV3, ";
        $query .= "     NAMESPARE3, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     v_name_mst ";
        $query .= " WHERE ";
        $query .= "     year = '".$ObjYear."' ";
        $query .= "     AND namecd1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        if ($namecd1 == "L024") {
            $query .= "     INT(namecd2) ";
        } else {
            $query .= "     namecd2 ";
        }
        return $query;
    }

    //対象者・合格者
    function GetLeftList($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     RECEPT.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     CASE WHEN BASE.PROCEDUREDIV = '1' ";
        $query .= "          THEN BASE.PROCEDUREDATE ";
        $query .= "     END AS PRO_DATE, ";
        $query .= "     CASE WHEN BASE.PROCEDUREDIV = '1' ";
        $query .= "          THEN '1' "; //対象者一覧
        $query .= "          ELSE '2' "; //合格者一覧
        $query .= "     END AS LEFT_FLG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "              ON BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "             AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "             AND BASE.EXAMNO       = RECEPT.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST L013 ";
        $query .= "              ON L013.YEAR         = RECEPT.ENTEXAMYEAR ";
        $query .= "             AND L013.NAMECD1      = 'L013' ";
        $query .= "             AND L013.NAMECD2      = RECEPT.JUDGEDIV ";
        $query .= " WHERE ";
        $query .= "         RECEPT.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND RECEPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND RECEPT.TESTDIV      = '{$model->testdiv}' ";
        if (($model->applicantdiv == "1" && $model->testdiv == "5") ||
            ($model->applicantdiv == "2" && $model->testdiv == "3")) {
            $query .= "     AND VALUE(BASE.GENERAL_FLG, '') != '1' ";
        }
        $query .= "     AND L013.NAMESPARE1   = '1' ";//合格者
        //辞退者は除く
        $query .= "     AND VALUE(BASE.PROCEDUREDIV, '')  != '2' "; //手続区分 1:済み 2:無し
        $query .= "     AND VALUE(BASE.ENTDIV, '')        != '2' "; //入学区分 1:済み 2:辞退
        $query .= " ORDER BY ";
        $query .= "     RECEPT.EXAMNO ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());

        foreach ($array as $select) {
            list($receptno, $examno) = preg_split("{:}", $select);

            //ENTEXAM_APPLICANTBASE_DAT
            //手続区分をセット
            //データチェック
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $baseRow = $db->getRow(" SELECT * FROM ENTEXAM_APPLICANTBASE_DAT " .$where, DB_FETCHMODE_ASSOC);
            $data = array();
            $data["PROCEDUREDIV"][TEXT]     = "1"; //手続区分 1:済み 2:無し
            if ($baseRow["PROCEDUREDATE"] == "") {
                $data["PROCEDUREDATE"][DATE]    = $model->pro_date;
            }
            $data["ENTDIV"][TEXT]           = "1"; // 入学区分 1:入学
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            //ENTEXAM_RECEPT_DAT
            //手続区分をセット
            //データチェック
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND TESTDIV       = '{$model->testdiv}' ";
            $where .= "   AND RECEPTNO      = '{$receptno}' ";
            $recRow = $db->getRow(" SELECT * FROM V_ENTEXAM_RECEPT_DAT " .$where, DB_FETCHMODE_ASSOC);
            $data = array();
            $data["PROCEDUREDIV1"][TEXT]    = "1"; //手続区分 1:済み 2:無し
            if ($recRow["PROCEDUREDATE1"] == "") {
                $data["PROCEDUREDATE1"][DATE]   = $model->pro_date;
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);
        }


        //合格者(右のリスト)
        $array2 = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());

        foreach ($array2 as $select) {
            list($receptno, $examno) = preg_split("{:}", $select);

            //ENTEXAM_APPLICANTBASE_DAT
            //手続区分・手続日・入学区分をクリア
            $data = array();
            $data["PROCEDUREDIV"][TEXT]     = ""; //手続区分 1:済み 2:無し
            $data["PROCEDUREDATE"][DATE]    = "";
            $data["ENTDIV"][TEXT]           = "";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            //ENTEXAM_RECEPT_DAT
            //手続区分をセット
            $data = array();
            $data["PROCEDUREDIV1"][TEXT]    = ""; //手続区分 1:済み 2:無し
            $data["PROCEDUREDATE1"][DATE]   = "";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND TESTDIV       = '{$model->testdiv}' ";
            $where .= "   AND RECEPTNO      = '{$receptno}' ";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);
        }


        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
