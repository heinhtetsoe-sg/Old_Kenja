<?php

require_once('for_php7.php');

class knjl080gQuery extends Query {

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
        $query .= "     namecd2 ";
        return $query;
    }

    //対象者・合格者
    function GetLeftList($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.PAY_MONEY, ";
        $query .= "     L1.EXAMCOURSE_ABBV, ";
        $query .= "     CASE WHEN T1.PROCEDUREDIV = '1' ";
        $query .= "          THEN T1.PROCEDUREDATE ";
        $query .= "     END AS PRODATE, ";
        $query .= "     CASE WHEN T1.PROCEDUREDIV = '1' ";
        $query .= "          THEN '1' "; //対象者一覧
        $query .= "          ELSE '2' "; //合格者一覧
        $query .= "     END AS LEFT_FLG ";
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
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.SHDIV        = '{$model->shdiv}' ";
        $query .= "     AND T1.JUDGEMENT    IN ('1','3') "; //合否区分 1:合格 3:まわし合格
        //辞退者は除く
        $query .= "     AND VALUE(T1.PROCEDUREDIV, '')  != '2' "; //手続区分 1:済み 2:無し
        $query .= "     AND VALUE(T1.ENTDIV, '')        != '2' "; //入学区分 1:済み 2:辞退
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";
        return $query;
    }

    //入学コース取得
    function getEnterCourse($model, $examno) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.EXAMNO, ";
        //入学コース
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
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.EXAMNO       = '{$examno}' ";
        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());

        foreach ($array as $select) {
            list($examno, $proDate) = preg_split("/:/", $select);

            //手続区分・手続日・入学区分をセット
            $data = array();
            $data["PROCEDUREDIV"][TEXT]     = "1"; //手続区分 1:済み 2:無し
            //手続日は未設定のみ上書き更新する
            if ($proDate == "") {
                $data["PROCEDUREDATE"][DATE]    = $model->pro_date;
            }
            $data["ENTDIV"][TEXT]           = "1";  //入学区分 1:済み 2:辞退
            //入金額を取得・・・仕様未定のため、保留
            //$query = knjl080gQuery::getPayMoney($model, $examno);
            //$setPayMoney = $db->getOne($query);
            //$data["PAY_MONEY"][NUMBER]      = $setPayMoney;   //入金
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            //入学コースをクリア
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $query .= "   AND EXAMNO        = '{$examno}' "; 
            $query .= "   AND SEQ           = '007' ";
            $db->query($query);
            //入学コースをセット
            $getEnter = array();
            $getEnter = $db->getRow(knjl080gQuery::getEnterCourse($model, $examno), DB_FETCHMODE_ASSOC);
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
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

        foreach ($array2 as $select) {
            list($examno, $proDate) = preg_split("/:/", $select);

            //手続区分・手続日・入学区分をクリア
            $data = array();
            $data["PROCEDUREDIV"][TEXT]     = ""; //手続区分 1:済み 2:無し
            $data["PROCEDUREDATE"][DATE]    = "";
            $data["ENTDIV"][TEXT]           = ""; //入学区分 1:済み 2:辞退
            //$data["PAY_MONEY"][NUMBER]      = ""; //入金
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            //入学コースをクリア
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $query .= "   AND EXAMNO        = '{$examno}' "; 
            $query .= "   AND SEQ           = '007' ";
            $db->query($query);
        }


        $db->commit();
        Query::dbCheckIn($db);
    }

    function getPayMoney($model, $examno) {
        $query  = " WITH JUDGEKIND_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.ENTEXAMYEAR, ";
        $query .= "         T1.APPLICANTDIV, ";
        $query .= "         T1.EXAMNO, ";
        $query .= "         CASE WHEN T1.JUDGE_KIND IS NOT NULL THEN T1.JUDGE_KIND ";
        $query .= "              WHEN T1.CLUB_RANK IS NOT NULL THEN T1.CLUB_RANK ";
        $query .= "              ELSE '0' ";
        $query .= "         END AS JUDGE_KIND ";
        $query .= "     FROM ";
        $query .= "         V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "             T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "         AND T1.EXAMNO = '{$examno}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     VALUE(L1.ENT_MONEY, 0) + VALUE(L1.FAC_MONEY, 0) + VALUE(L1.REMARK1_MONEY, 0) + VALUE(L1.REMARK2_MONEY, 0) + VALUE(L1.REMARK3_MONEY, 0) AS SET_MONEY ";
        $query .= " FROM ";
        $query .= "     JUDGEKIND_T T1 ";
        $query .= "     LEFT JOIN ENTEXAM_PAYMENT_MONEY_YMST L1 ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "          AND T1.APPLICANTDIV = L1.APPLICANTDIV ";
        $query .= "          AND T1.JUDGE_KIND = L1.JUDGE_KIND ";
        return $query;
    }
}
?>
