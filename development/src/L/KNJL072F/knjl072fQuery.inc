<?php

require_once('for_php7.php');

class knjl072fQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year, $namecd2 = "") {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE1, ";
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
        if ($namecd1 == "L024") {
            $query .= "     INT(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }
        return $query;
    }

    //受験型
    function getExamType($year, $testdiv) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = 'L005' ";
        $query .= "     AND VALUE(ABBV2, '') IN ( ";
        $query .= "         SELECT ";
        $query .= "             VALUE(ABBV2, '') ";
        $query .= "         FROM ";
        $query .= "             V_NAME_MST ";
        $query .= "         WHERE ";
        $query .= "                 YEAR = '".$year."' ";
        $query .= "             AND NAMECD1 = 'L024' ";
        $query .= "             AND NAMECD2 = '".$testdiv."' ";
        $query .= "     ) ";
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
        $query .= "       T1.PROCEDUREDIV1, ";
        $query .= "       T1.TOTAL4, ";
        $query .= "       T1.JUDGEDIV, ";
        $query .= "       N1.NAME1 AS JUDGE_NAME, ";
        $query .= "       N2.NAME1 AS EXAM_TYPE_NAME, ";
        $query .= "       N3.NAME1 AS EIKEN_SHUTOKU_KYUU ";
        $query .= "  FROM ";
        $query .= "     V_ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT T4 ";
        $query .= "              ON T4.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T4.EXAMNO       = T1.EXAMNO ";
        $query .= "             AND T4.SEQ          = '011' ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "              ON N1.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "             AND N1.NAMECD1      = 'L013' ";
        $query .= "             AND N1.NAMECD2      = T1.JUDGEDIV ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ";
        $query .= "              ON N2.YEAR         = T4.ENTEXAMYEAR ";
        $query .= "             AND N2.NAMECD1      = 'L005' ";
        $query .= "             AND N2.NAMECD2      = T4.REMARK{$model->testdiv} ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_014 ";
        $query .= "              ON BASE_D_014.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND BASE_D_014.EXAMNO      = T1.EXAMNO ";
        $query .= "             AND BASE_D_014.SEQ         = '014' ";
        $query .= "     LEFT JOIN V_NAME_MST N3 ";
        $query .= "              ON N3.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "             AND N3.NAMECD1      = 'L055' ";
        $query .= "             AND N3.NAMECD2      = BASE_D_014.REMARK9 ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '".$model->year."'";
        $query .= "   AND T1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND T1.TESTDIV        = '".$model->testdiv."' ";
        //特別措置者のみ
        if (strlen($model->special_reason_div)) {
            $query .= "     AND T2.SPECIAL_REASON_DIV IS NOT NULL ";
        } else {
            $query .= "     AND VALUE(T1.JUDGEDIV, '0') <> '4' "; //4:欠席は対象外
        }
        //特別入試対象者は、合否判定処理をしないため、母集団から除く。
        if ($model->testdiv == "5") {
            $query .= "   AND VALUE(T2.GENERAL_FLG,'') != '1' ";
        }
        //受験型(EXAM_TYPE)
        if (strlen($model->exam_type)) {
            $query .= "   AND T4.REMARK{$model->testdiv} = '".$model->exam_type."' ";
        }
        //抽出平均点
        if (strlen($model->avg_from)) {
            $query .= "   AND T1.TOTAL4 >= {$model->avg_from} ";
        }
        if (strlen($model->avg_to)) {
            $query .= "   AND T1.TOTAL4 <  {$model->avg_to} ";
        }
        $query .= " ORDER BY ";
        //表示順序ラジオボタン 1:成績順 2:受験番号順
        if ($model->sort == "1") $query .= "     value(T1.TOTAL4, -1) DESC, ";
        $query .= "     T1.RECEPTNO ";
        return $query;
    }

    //名称マスタより取得
    function getJudgeFlg($year, $namecd1, $namecd2) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= "     AND NAMECD2 = '".$namecd2."' ";
        return $query;
    }

    //未来の入試区分を取得
    function getFutureTestdiv($model) {
        $query  = "";
        $query .= " WITH COMMON AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMECD2 AS TESTDIV, ";
        $query .= "         NAME1 AS TESTDIV_NAME, ";
        $query .= "         ABBV2 AS KIKOKU, ";
        $query .= "         NAMESPARE1 || '-' || ABBV3 AS TESTDAY, ";
        $query .= "         NAMESPARE1 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".$model->year."' ";
        $query .= "         AND NAMECD1 = 'L024' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     TESTDIV ";
        $query .= " FROM ";
        $query .= "     COMMON ";
        $query .= " WHERE ";
        $query .= "     TESTDIV != '5' ";
        $query .= "     AND VALUE(KIKOKU,'') != '1' ";
        $query .= "     AND TESTDAY > (SELECT TESTDAY FROM COMMON WHERE TESTDIV = '".$model->testdiv."') ";
        $query .= "     AND NAMESPARE1 = (SELECT NAMESPARE1 FROM COMMON WHERE TESTDIV = '".$model->testdiv."') ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //合格コース
        $course_array = array();
        $course_array = explode("-", $model->suc_course);

        //合格フラグ
        $query = knjl072fQuery::getJudgeFlg($model->year, "L013", $model->judgediv);
        $judgeFlg = $db->getOne($query);

        //未来の入試区分を取得
        $fTestdiv = array();
        $result = $db->query(knjl072fQuery::getFutureTestdiv($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $fTestdiv[] = $row["TESTDIV"];
        }

        $keyList = array();
        $keyList = explode(",", $model->hid_examno);
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $receptno = $tmp[0];
            $examno = $tmp[1];

            //チェックありの人を処理
            if ($model->field["CHK_DATA"][$receptno] != "on") continue;

            //合格の時、募集企画データより特待生情報をセットする。
            $query = knjl072fQuery::getJudgeKindRecruit($model, $examno);
            $judge_kind = $db->getOne($query);

            //受付データ
            $data = array();
            $data["JUDGEDIV"][TEXT]         = $model->judgediv;
            //合格の時、募集企画データより特待生情報をセットする。
            if (strlen($judgeFlg) && strlen($judge_kind)) {
                //既に登録済みの時は、セットしない。
                $query  = "SELECT HONORDIV FROM V_ENTEXAM_RECEPT_DAT";
                $query .= " WHERE ENTEXAMYEAR   = '".$model->year."' ";
                $query .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
                $query .= "   AND TESTDIV       = '".$model->testdiv."' ";
                $query .= "   AND RECEPTNO      = '".$receptno."' ";
                if (!strlen($db->getOne($query))) {
                    $data["HONORDIV"][TEXT] = $judge_kind;
                }
            }
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
            if (strlen($judgeFlg) && strlen($model->suc_course)) {
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAM_TYPE"][TEXT]        = "1";
                $data["RECEPTNO"][TEXT]         = $receptno;
                $data["SEQ"][TEXT]              = "001";
                $data["REMARK1"][TEXT]          = $course_array[0];
                $data["REMARK2"][TEXT]          = $course_array[1];
                $data["REMARK3"][TEXT]          = $course_array[2];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
                $db->query($query);
            }

            //基礎データ
            $data = array();
            $data["JUDGEMENT"][TEXT]        = $model->judgediv;
            $data["SUC_COURSECD"][TEXT]     = !strlen($judgeFlg) || !strlen($model->suc_course) ? "" : $course_array[0];
            $data["SUC_MAJORCD"][TEXT]      = !strlen($judgeFlg) || !strlen($model->suc_course) ? "" : $course_array[1];
            $data["SUC_COURSECODE"][TEXT]   = !strlen($judgeFlg) || !strlen($model->suc_course) ? "" : $course_array[2];
            //合格の時、募集企画データより特待生情報をセットする。
            if (strlen($judgeFlg) && strlen($judge_kind)) {
                //既に登録済みの時は、セットしない。
                $query  = "SELECT JUDGE_KIND FROM ENTEXAM_APPLICANTBASE_DAT";
                $query .= " WHERE ENTEXAMYEAR   = '".$model->year."' ";
                $query .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
                $query .= "   AND EXAMNO        = '".$examno."'";
                if (!strlen($db->getOne($query))) {
                    $data["JUDGE_KIND"][TEXT] = $judge_kind;
                }
            }
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
            $query = knjl072fQuery::getPayMoney($model, $receptno);
            $setDueMoney = $db->getOne($query);
            //データ更新
            $data = array();
            //請求（入学金）
            $data["ENT_DUE_DATE"][DATE]     = !strlen($judgeFlg) ? "" : str_replace("/", "-", CTRL_DATE);
            $data["ENT_DUE_MONEY"][NUMBER]  = !strlen($judgeFlg) ? "" : $setDueMoney;
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

            if (0 < get_count($fTestdiv)) {
                //未来の受付データに不合格（JUDGEDIV = '2'）をセットする
                $data = array();
                $data["JUDGEDIV"][TEXT]         = !strlen($judgeFlg) ? "" : "2";
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";
                //条件
                $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
                $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
                $where .= "   AND EXAMNO        = '".$examno."' ";
                $where .= "   AND TESTDIV       IN ('".implode("','", $fTestdiv)."') ";
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
                $db->query($query);
            }
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