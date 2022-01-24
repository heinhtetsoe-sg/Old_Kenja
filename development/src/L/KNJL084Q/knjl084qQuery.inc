<?php

require_once('for_php7.php');

class knjl084qQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= " AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        return $query;
    }

    //対象者・合格者取得
    function GetLeftList($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     CASE WHEN T1.ENTDIV = '1' THEN '1' ELSE '0' END AS LEFT_FLG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "          ON N1.YEAR     = T1.ENTEXAMYEAR ";
        $query .= "         AND N1.NAMECD1  = 'L013' ";
        $query .= "         AND N1.NAMECD2  = T1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR              = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV             = '".$model->applicantdiv."' AND ";
        $query .= "     T1.TESTDIV                  = '".$model->testdiv."' AND ";
        //合格者が対象
        $query .= "     N1.NAMESPARE1               = '1' AND ";
        //辞退者は除く
        $query .= "     VALUE(T1.PROCEDUREDIV,'')  != '2' AND ";    //手続区分 1:済み 2:無し（辞退）
        $query .= "     VALUE(T1.ENTDIV,'')        != '2' AND ";    //入学区分 1:済み 2:辞退
        //手続者が対象
        $query .= "     VALUE(T1.PROCEDUREDIV,'')   = '1' ";        //手続区分 1:済み 2:無し（辞退）
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //入学コース取得
    function getEnterCourse($model, $examno) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     L1.ENTER_COURSECD, ";
        $query .= "     L1.ENTER_MAJORCD, ";
        $query .= "     L1.ENTER_COURSECODE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L1 ";
        $query .= "             ON  L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND L1.TESTDIV      = T1.TESTDIV ";
        $query .= "             AND L1.COURSECD     = T1.SUC_COURSECD ";
        $query .= "             AND L1.MAJORCD      = T1.SUC_MAJORCD ";
        $query .= "             AND L1.EXAMCOURSECD = T1.SUC_COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "     T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "     T1.EXAMNO       = '{$examno}' ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());

        foreach ($array as $examno) {
            //ENTEXAM_APPLICANTBASE_DAT
            //入学区分をセット
            $data = array();
            $data["ENTDIV"][TEXT]           = "1";      //入学区分 1:済み 2:辞退
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            //入学コースをクリア
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' "; 
            $query .= "   AND EXAMNO        = '{$examno}' "; 
            $query .= "   AND SEQ           = '007' ";
            $db->query($query);
            //入学コースをセット
            $getEnter = array();
            $getEnter = $db->getRow(knjl084qQuery::getEnterCourse($model, $examno), DB_FETCHMODE_ASSOC);
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $examno;
            $data["SEQ"][TEXT]              = '007';
            $data["REMARK1"][TEXT]          = $getEnter["ENTER_COURSECD"];
            $data["REMARK2"][TEXT]          = $getEnter["ENTER_MAJORCD"];
            $data["REMARK3"][TEXT]          = $getEnter["ENTER_COURSECODE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        }

        //合格者(右のリスト)
        $array2 = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());

        foreach ($array2 as $examno) {
            //ENTEXAM_APPLICANTBASE_DAT
            //入学区分をクリア
            $data = array();
            $data["ENTDIV"][TEXT]           = "";       //入学区分 1:済み 2:辞退
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            //入学コースをクリア
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' "; 
            $query .= "   AND EXAMNO        = '{$examno}' "; 
            $query .= "   AND SEQ           = '007' ";
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
