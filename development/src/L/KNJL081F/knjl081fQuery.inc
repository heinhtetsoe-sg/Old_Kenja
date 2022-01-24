<?php

require_once('for_php7.php');

class knjl081fQuery extends Query {

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

    //入試回数(2:高校のみ)
    function getTestdiv0($year, $testdiv) {
        $query  = " WITH T_DATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '1' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '2' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '3' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '4' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '5' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L059' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 || '（' || L1.DATE || '）' AS LABEL, ";
        $query .= "     T1.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     LEFT JOIN T_DATE L1 ON L1.TESTDIV0 = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.NAMECD1 = 'L034' ";
        $query .= "     AND L1.DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象者・合格者
    function GetLeftList($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     M1.ENTRANCE_FLG, "; //1:支度金利用者フラグ
        $query .= "     M1.EXP_PAY_MONEY AS PAY_MONEY, ";
        $query .= "     CASE WHEN T2.ENTDIV = '1' ";
        $query .= "          THEN M1.EXP_PAY_DATE ";
        $query .= "     END AS PAY_DATE, ";
        $query .= "     CASE WHEN T2.ENTDIV = '1' ";
        $query .= "          THEN '1' "; //対象者一覧
        $query .= "          ELSE '2' "; //合格者一覧
        $query .= "     END AS LEFT_FLG ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_MONEY_DAT M1 ";
        $query .= "              ON M1.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "             AND M1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "             AND M1.EXAMNO       = T2.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "              ON N1.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "             AND N1.NAMECD1      = 'L013' ";
        $query .= "             AND N1.NAMECD2      = T1.JUDGEDIV ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T4 ";
        $query .= "              ON T4.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T4.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T4.TESTDIV      = T1.TESTDIV ";
        $query .= "             AND T4.RECEPTNO     = T1.RECEPTNO ";
        $query .= "             AND T4.SEQ          = '003' ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        //入試回数(2:高校のみ)
        if ($model->applicantdiv == "2") {
            $query .= "     AND T4.REMARK1      = '{$model->testdiv0}' ";
        }
        if (($model->applicantdiv == "1" && $model->testdiv == "5") ||
            ($model->applicantdiv == "2" && $model->testdiv == "3")) {
            $query .= "     AND VALUE(T2.GENERAL_FLG, '') != '1' ";
        }
        $query .= "     AND N1.NAMESPARE1   = '1' ";//合格者
        //辞退者は除く
        $query .= "     AND VALUE(T2.PROCEDUREDIV, '')  != '2' "; //手続区分 1:済み 2:無し
        $query .= "     AND VALUE(T2.ENTDIV, '')        != '2' "; //入学区分 1:済み 2:辞退
        //手続者
        $query .= "     AND VALUE(T2.PROCEDUREDIV, '')   = '1' "; //手続区分 1:済み 2:無し
        $query .= " ORDER BY ";
        if ($model->applicantdiv == "1") {
            $query .= "     T1.RECEPTNO ";
        } else {
            $query .= "     T1.EXAMNO ";
        }
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
        $query .= "             AND L1.TESTDIV      = '1' ";//固定
        $query .= "             AND L1.COURSECD     = T1.SUC_COURSECD ";
        $query .= "             AND L1.MAJORCD      = T1.SUC_MAJORCD ";
        $query .= "             AND L1.EXAMCOURSECD = T1.SUC_COURSECODE ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
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
            list($receptno, $examno) = preg_split("/:/", $select);

            //ENTEXAM_MONEY_DAT
            //諸費を取得
            $query = knjl081fQuery::getPayMoney($model, $receptno);
            $setPayMoney = $db->getOne($query);
            //データチェック
            $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND EXAMNO        = '".$examno."' ";
            $getRow = $db->getRow(" SELECT * FROM ENTEXAM_MONEY_DAT " .$where, DB_FETCHMODE_ASSOC);
            //データ更新
            $data = array();
            //入金（諸費）
            $data["EXP_PAY_DIV"][TEXT]      = $model->pay_div;//入金方法 1:振込 2:窓口
            if ($getRow["EXP_PAY_DATE"] == "") {
                $data["EXP_PAY_DATE"][DATE]     = $model->pay_date;
            }
            if ($getRow["EXP_PAY_CHAK_DATE"] == "") {
                $data["EXP_PAY_CHAK_DATE"][DATE]= $model->pay_chak_date;
            }
            $data["EXP_PAY_MONEY"][NUMBER]  = $setPayMoney;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            if ($getRow["EXAMNO"] == "") {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $query = Query::insertSQL($data, "ENTEXAM_MONEY_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_MONEY_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DAT
            //手続区分をセット
            $data = array();
            $data["ENTDIV"][TEXT]           = "1"; //手続区分 1:済み 2:無し
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
            $getEnter = $db->getRow(knjl081fQuery::getEnterCourse($model, $examno), DB_FETCHMODE_ASSOC);
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
            list($receptno, $examno) = preg_split("/:/", $select);

            //ENTEXAM_MONEY_DAT
            //クリア
            $data = array();
            //入金（諸費）
            $data["EXP_PAY_DIV"][TEXT]      = "";
            $data["EXP_PAY_DATE"][DATE]     = "";
            $data["EXP_PAY_CHAK_DATE"][DATE]= "";
            $data["EXP_PAY_MONEY"][NUMBER]  = "";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND EXAMNO        = '".$examno."' ";
            $query = Query::updateSQL($data, "ENTEXAM_MONEY_DAT", $where);
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DAT
            //手続区分・手続日・入学区分をクリア
            $data = array();
            $data["ENTDIV"][TEXT]           = ""; //手続区分 1:済み 2:無し
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

    function getPayMoney($model, $receptno) {
        $query  = "";

        //免除額コードを取得(RECEPT)
        $query .= " WITH V_COMMON AS ( ";
        $query .= "     SELECT ";
        $query .= "         '1' AS LINK, ";
        $query .= "         EXEMPTION_CD, ";
        $query .= "         EXEMPTION_CD2 ";
        $query .= "     FROM ";
        $query .= "         V_ENTEXAM_RECEPT_EXEMPTION_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "         AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND TESTDIV = '{$model->testdiv}' ";
        $query .= "         AND RECEPTNO = '{$receptno}' ";
        //免除額を取得（免除額設定）
        $query .= " ), T_EXEMPTION AS ( ";
        $query .= "     SELECT ";
        $query .= "         P2.EXEMPTION_CD, ";
        $query .= "         SUM(P1.ITEM_MONEY) AS EXEMPTION_MONEY ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_PAYMENT_ITEM_MST P1 ";
        $query .= "         INNER JOIN ENTEXAM_PAYMENT_EXEMPTION_MST P2 ";
        $query .= "              ON P2.ENTEXAMYEAR = P1.ENTEXAMYEAR ";
        $query .= "             AND P2.APPLICANTDIV = P1.APPLICANTDIV ";
        $query .= "             AND P2.DIV = P1.DIV ";
        $query .= "             AND P2.ITEM_CD = P1.ITEM_CD ";
        $query .= "             AND P2.KIND_CD = '2' ";//諸費
        $query .= "     WHERE ";
        $query .= "         P1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "         AND P1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND P1.DIV = '0' ";
        $query .= "     GROUP BY ";
        $query .= "         P2.EXEMPTION_CD ";
        $query .= " ) ";
        //基本額から免除額を引いた金額を取得
        $query .= " SELECT ";
        $query .= "     VALUE(MIN(L2.EXEMPTION_MONEY, L3.EXEMPTION_MONEY), 0) AS MONEY ";
        $query .= " FROM ";
        $query .= "     V_COMMON T1 ";
        $query .= "     LEFT JOIN T_EXEMPTION L2 ON L2.EXEMPTION_CD = T1.EXEMPTION_CD ";
        $query .= "     LEFT JOIN T_EXEMPTION L3 ON L3.EXEMPTION_CD = T1.EXEMPTION_CD2 ";

        return $query;
    }
}
?>