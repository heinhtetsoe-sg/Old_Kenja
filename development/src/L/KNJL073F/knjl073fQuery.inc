<?php

require_once('for_php7.php');

class knjl073fQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year, $namecd2 = "") {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        if ($namecd1 == "L013") {
            $query .= "     AND NAMECD2 <> '4' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
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

    //コース区分
    function getExamcourse1() {
        $query  = " WITH T_COMMON (VALUE, LABEL) AS ( ";
        $query .= "     VALUES('1', '1:理数キャリア') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('2', '2:国際教養') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('3', '3:スポーツ科学') ";
        $query .= "     ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_COMMON ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //合格コース
    function getSucCourse($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.EXAMCOURSECD || ':' || T1.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '1' ";//固定
        $query .= "     AND T1.EXAMCOURSECD LIKE '".$model->examcourse1."%' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = "";
        $query .= "SELECT T1.RECEPTNO, ";
        $query .= "       T1.EXAMNO, ";
        $query .= "       T2.NAME, ";
        $query .= "       T2.SLIDE_FLG, ";
        $query .= "       T2.SHIFT_DESIRE_FLG, ";
        $query .= "       T1.PROCEDUREDIV1, ";
        $query .= "       T1.TOTAL2, ";
        $query .= "       T1.JUDGEDIV, ";
        $query .= "       N1.NAME1 AS JUDGE_NAME, ";
        $query .= "       T2.SUC_COURSECD || '-' || T2.SUC_MAJORCD || '-' || T2.SUC_COURSECODE AS SUC_COURSE, ";
        $query .= "       C2.EXAMCOURSE_ABBV AS SUC_COURSE_NAME, ";
        $query .= "       T3.REMARK8 || '-' || T3.REMARK9 || '-' || T3.REMARK10 AS EXAMCOURSE, ";
        $query .= "       C3.EXAMCOURSE_ABBV AS EXAMCOURSE_NAME ";
        $query .= "  FROM ";
        $query .= "     V_ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3 ";
        $query .= "              ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T3.EXAMNO       = T1.EXAMNO ";
        $query .= "             AND T3.SEQ          = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C2 ";
        $query .= "              ON C2.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "             AND C2.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "             AND C2.TESTDIV      = '1' ";//固定
        $query .= "             AND C2.COURSECD     = T2.SUC_COURSECD ";
        $query .= "             AND C2.MAJORCD      = T2.SUC_MAJORCD ";
        $query .= "             AND C2.EXAMCOURSECD = T2.SUC_COURSECODE ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C3 ";
        $query .= "              ON C3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND C3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND C3.TESTDIV      = '1' ";//固定
        $query .= "             AND C3.COURSECD     = T3.REMARK8 ";
        $query .= "             AND C3.MAJORCD      = T3.REMARK9 ";
        $query .= "             AND C3.EXAMCOURSECD = T3.REMARK10 ";
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
        $query .= " WHERE T1.ENTEXAMYEAR    = '".$model->year."'";
        $query .= "   AND T1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND T1.TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND T4.REMARK1        = '".$model->testdiv0."' ";
        $query .= "   AND T3.REMARK10 LIKE '".$model->examcourse1."%' ";
        //特別措置者のみ
        if (strlen($model->special_reason_div)) {
            $query .= "     AND T2.SPECIAL_REASON_DIV IS NOT NULL ";
        } else {
            $query .= "     AND VALUE(T1.JUDGEDIV, '0') <> '4' "; //4:欠席は対象外
        }
        if ($model->target == "2") {
            $query .= "     AND VALUE(T2.GENERAL_FLG, '') != '1' ";
        }
        if ($model->target == "3") {
            $query .= "     AND VALUE(T2.GENERAL_FLG, '')  = '1' ";
        }
        //抽出平均点
        if (strlen($model->avg_from)) {
            $query .= "   AND T1.TOTAL2 >= {$model->avg_from} ";
        }
        if (strlen($model->avg_to)) {
            $query .= "   AND T1.TOTAL2 <  {$model->avg_to} ";
        }
        $query .= " ORDER BY ";
        //表示順序ラジオボタン 1:成績順 2:受験番号順
        if ($model->sort == "1") $query .= "     value(T1.TOTAL2, -1) DESC, ";
        $query .= "     T1.RECEPTNO ";
        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //合格コース
        $course_array = array();
        $course_array = explode("-", $model->suc_course);

        $keyList = array();
        $keyList = explode(",", $model->hid_examno);
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $receptno = $tmp[0];
            $examno = $tmp[1];

            //チェックありの人を処理
            if ($model->field["CHK_DATA"][$receptno] != "on") continue;

            //合格の時、募集企画データより特待生情報をセットする。
            $query = knjl073fQuery::getJudgeKindRecruit($model, $examno);
            $judge_kind = $db->getOne($query);

            //受付データ
            $data = array();
            $data["JUDGEDIV"][TEXT]         = $model->judgediv;
            $data["HONORDIV"][TEXT]         = !strlen($model->suc_course) ? "" : $judge_kind;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND TESTDIV       = '".$model->testdiv."' ";
            $where .= "   AND RECEPTNO      = '".$receptno."' ";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

            //受付詳細データ
            //DELETE
            $query  = " DELETE FROM ENTEXAM_RECEPT_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $query .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $query .= "   AND TESTDIV       = '".$model->testdiv."' ";
            $query .= "   AND RECEPTNO      = '".$receptno."' ";
            $query .= "   AND SEQ           = '001' ";
            $db->query($query);
            //INSERT
            if (strlen($model->suc_course)) {
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAM_TYPE"][TEXT]        = "1";
                $data["RECEPTNO"][TEXT]         = $receptno;
                $data["SEQ"][TEXT]              = "001";
                $data["REMARK1"][TEXT]          = !strlen($model->suc_course) ? "" : $course_array[0];
                $data["REMARK2"][TEXT]          = !strlen($model->suc_course) ? "" : $course_array[1];
                $data["REMARK3"][TEXT]          = !strlen($model->suc_course) ? "" : $course_array[2];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
                $db->query($query);
            }

            //基礎データ
            $data = array();
            $data["JUDGEMENT"][TEXT]        = $model->judgediv;
            $data["SUC_COURSECD"][TEXT]     = !strlen($model->suc_course) ? "" : $course_array[0];
            $data["SUC_MAJORCD"][TEXT]      = !strlen($model->suc_course) ? "" : $course_array[1];
            $data["SUC_COURSECODE"][TEXT]   = !strlen($model->suc_course) ? "" : $course_array[2];
            $data["JUDGE_KIND"][TEXT]       = !strlen($model->suc_course) ? "" : $judge_kind;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND EXAMNO        = '".$examno."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            //ENTEXAM_MONEY_DAT
            //入学金を取得
            $query = knjl073fQuery::getPayMoney($model, $receptno);
            $setDueMoney = $db->getOne($query);
            //データ更新
            $data = array();
            //請求（入学金）
            $data["ENT_DUE_DATE"][DATE]     = !strlen($model->suc_course) ? "" : str_replace("/", "-", CTRL_DATE);
            $data["ENT_DUE_MONEY"][NUMBER]  = !strlen($model->suc_course) ? "" : $setDueMoney;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //データチェック
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND EXAMNO        = '".$examno."' ";
            $getRow = $db->getRow(" SELECT * FROM ENTEXAM_MONEY_DAT " .$where, DB_FETCHMODE_ASSOC);
            if ($getRow["EXAMNO"] == "") {
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $query = Query::insertSQL($data, "ENTEXAM_MONEY_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_MONEY_DAT", $where);
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //一覧表示
    function getJudgeKindRecruit($model, $examno) {
        $query  = "";
        //事前番号
        $query .= " SELECT ";
        $query .= "     V1.JUDGE_KIND ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT T1 ";
        $query .= "     LEFT JOIN RECRUIT_VISIT_DAT V1 ";
        $query .= "              ON V1.YEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND V1.RECRUIT_NO = T1.REMARK1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND T1.EXAMNO = '".$examno."' ";
        $query .= "     AND T1.SEQ = '002' ";
        return $query;
    }

    function getPayMoney($model, $receptno, $div = "") {
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
        $query .= "         ENTEXAMYEAR = '{$model->year}' ";
        $query .= "         AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND TESTDIV = '{$model->testdiv}' ";
        $query .= "         AND RECEPTNO = '{$receptno}' ";
        //基本額を取得（費目マスタ）
        $query .= " ), T_ITEM AS ( ";
        $query .= "     SELECT ";
        $query .= "         '1' AS LINK, ";
        $query .= "         SUM(ITEM_MONEY) AS ITEM_MONEY ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_PAYMENT_ITEM_MST ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '{$model->year}' ";
        $query .= "         AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND DIV = '0' ";
        $query .= "         AND REMARK1 = '1' ";//入学金対象
        //免除額を取得（免除額設定）
        $query .= " ), T_EXEMPTION AS ( ";
        $query .= "     SELECT ";
        $query .= "         P2.EXEMPTION_CD, ";
        $query .= "         SUM(P2.EXEMPTION_MONEY) AS EXEMPTION_MONEY ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_PAYMENT_ITEM_MST P1 ";
        $query .= "         INNER JOIN ENTEXAM_PAYMENT_EXEMPTION_MST P2 ";
        $query .= "              ON P2.ENTEXAMYEAR = P1.ENTEXAMYEAR ";
        $query .= "             AND P2.APPLICANTDIV = P1.APPLICANTDIV ";
        $query .= "             AND P2.DIV = P1.DIV ";
        $query .= "             AND P2.ITEM_CD = P1.ITEM_CD ";
        $query .= "             AND P2.KIND_CD = '1' ";//入学金
        $query .= "     WHERE ";
        $query .= "         P1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "         AND P1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND P1.DIV = '0' ";
        $query .= "         AND P1.REMARK1 = '1' ";//入学金対象
        $query .= "     GROUP BY ";
        $query .= "         P2.EXEMPTION_CD ";
        $query .= " ) ";
        //基本額から免除額を引いた金額を取得
        $query .= " SELECT ";
        $query .= "     VALUE(L1.ITEM_MONEY, 0) - VALUE(L2.EXEMPTION_MONEY, 0) - VALUE(L3.EXEMPTION_MONEY, 0) AS MONEY ";
        $query .= " FROM ";
        $query .= "     V_COMMON T1 ";
        $query .= "     LEFT JOIN T_ITEM L1 ON L1.LINK = T1.LINK ";
        $query .= "     LEFT JOIN T_EXEMPTION L2 ON L2.EXEMPTION_CD = T1.EXEMPTION_CD ";
        $query .= "     LEFT JOIN T_EXEMPTION L3 ON L3.EXEMPTION_CD = T1.EXEMPTION_CD2 ";

        return $query;
    }
}
?>