<?php

require_once('for_php7.php');

class knjl030fQuery extends Query
{

    //名称マスタより取得
    public function getNameMst($year, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        if ($namecd1 == "L024") {
            $query .= "     INT(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }

        return $query;
    }

    //入試回数(2:高校のみ)
    public function getTestdiv0($year, $testdiv)
    {
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

    //--- UPDATE(1:中学)
    public function getUpdateJQuery($model)
    {
        $query  = "";

        //過去に合格した人。
        $query .= " WITH T_JUDGE AS ( ";
        $query .= "     SELECT ";
        $query .= "         MIN(N1.NAMESPARE1) AS NAMESPARE1, ";
        $query .= "         MIN(T1.PROCEDUREDIV1) AS PROCEDUREDIV1, ";
        $query .= "         T1.EXAMNO ";
        $query .= "     FROM ";
        $query .= "         V_ENTEXAM_RECEPT_DAT T1 ";
        $query .= "         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L013' AND N1.NAMECD2 = T1.JUDGEDIV ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "         AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "         AND T1.TESTDIV <> '{$model->field["TESTDIV"]}' "; //過去の入試区分
        $query .= "         AND N1.NAMESPARE1 = '1' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.EXAMNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     J1.NAMESPARE1, ";
        $query .= "     J1.PROCEDUREDIV1, ";
        $query .= "     L2.REMARK{$model->field["TESTDIV"]} AS RECEPTNO, ";
        $query .= "     T1.EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        //入試区分
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT L0 ";
        $query .= "              ON L0.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND L0.EXAMNO = T1.EXAMNO ";
        $query .= "             AND L0.SEQ = '010' ";
        $query .= "             AND L0.REMARK{$model->field["TESTDIV"]} = '{$model->field["TESTDIV"]}' ";
        //受験番号(RECEPTNO)
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT L2 ";
        $query .= "              ON L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND L2.EXAMNO = T1.EXAMNO ";
        $query .= "             AND L2.SEQ = '012' ";
        $query .= "             AND L2.REMARK{$model->field["TESTDIV"]} IS NOT NULL ";
        //過去に合格した人。
        $query .= "     LEFT JOIN T_JUDGE J1 ON J1.EXAMNO = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        //合格者は、生成しない（合格後は受験不可）
        //但し、第５回の時は、特別入試対象者（合格で手続済み）も生成する。
        if ($model->field["TESTDIV"] == "5") {
            //$query .= "     AND ((J1.NAMESPARE1 is null) OR (J1.NAMESPARE1 = '1' AND J1.PROCEDUREDIV1 = '1')) ";
        } else {
            $query .= "     AND J1.NAMESPARE1 is null ";
        }
        $query .= " ORDER BY ";
        $query .= "     RECEPTNO ";

        return $query;
    }

    //--- UPDATE(1:中学)
    public function &getUpdateJ($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //配列
        $recList = array();
        $query = knjl030fQuery::getUpdateJQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $recList[] = array('RECEPTNO' => $row["RECEPTNO"], 'EXAMNO' => $row["EXAMNO"], 'NAMESPARE1' => $row["NAMESPARE1"], 'PROCEDUREDIV1' => $row["PROCEDUREDIV1"]);
        }
        $result->free();

        //DELETE
        //ENTEXAM_RECEPT_DAT
        $query  = " DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $db->query($query);

        //配列ループ
        foreach ($recList as $key => $row) {
            //INSERT
            //ENTEXAM_RECEPT_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]      = $model->field["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]    = "1";
            $data["RECEPTNO"][TEXT]     = $row["RECEPTNO"];
            $data["EXAMNO"][TEXT]       = $row["EXAMNO"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
            $db->query($query);
            //第５回の時、過去に合格で手続済みの人は、特別入試対象者とする。
            if ($model->field["TESTDIV"] == "5" && $row["NAMESPARE1"] == "1" && $row["PROCEDUREDIV1"] == "1") {
                //UPDATE
                $data = array();
                $data["GENERAL_FLG"][TEXT]      = "1";
                //条件
                $where  = " WHERE ENTEXAMYEAR   = '{$model->year}' ";
                $where .= "   AND EXAMNO        = '{$row["EXAMNO"]}' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
                $db->query($query);
            }
        }//foreach

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE(2:高校)
    public function getUpdateHQuery($model)
    {
        //基本母集団
        $query  = " SELECT ";
        $query .= "     '0' AS GAKU_TEST_FLG, ";
        $query .= "     EXAMNO AS RECEPTNO, ";
        $query .= "     EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";//1:A推薦 2:B推薦 3:一般（併願優遇含む） 4:帰国生A 5:帰国生B
        $query .= "     AND TESTDIV0 = '{$model->field["TESTDIV0"]}' ";//1:第１回 2:第２回
        //特殊母集団
        //3:一般、2:第２回の時、学力診断テスト対象者も生成する。
        if ($model->field["TESTDIV"] == "3" && $model->field["TESTDIV0"] == "2") {
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     '1' AS GAKU_TEST_FLG, ";
            $query .= "     T1.RECEPTNO, ";
            $query .= "     T1.EXAMNO ";
            $query .= " FROM ";
            $query .= "     V_ENTEXAM_RECEPT_DAT T1 ";
            $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L013' AND N1.NAMECD2 = T1.JUDGEDIV ";
            $query .= " WHERE ";
            $query .= "     T1.ENTEXAMYEAR = '{$model->year}' ";
            $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "     AND T1.TESTDIV IN ('1','4') ";  //1:A推薦 4:帰国生A
            $query .= "     AND N1.NAMESPARE1 = '1' ";      //1:合格者扱いフラグ
            $query .= "     AND T1.PROCEDUREDIV1 = '1' ";   //1:手続済み
        }
        $query .= " ORDER BY ";
        $query .= "     GAKU_TEST_FLG, ";
        $query .= "     RECEPTNO ";

        return $query;
    }

    //--- UPDATE(2:高校)
    public function &getUpdateH($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //配列
        $recList = array();
        $query = knjl030fQuery::getUpdateHQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $recList[] = array('RECEPTNO' => $row["RECEPTNO"], 'EXAMNO' => $row["EXAMNO"], 'GAKU_TEST_FLG' => $row["GAKU_TEST_FLG"]);
        }
        $result->free();

        //DELETE
        //ENTEXAM_RECEPT_DAT
        $query  = " DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND RECEPTNO IN ( ";
        $query .= "         SELECT RECEPTNO ";
        $query .= "         FROM ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= "         WHERE ";
        $query .= "             ENTEXAMYEAR = '{$model->year}' ";
        $query .= "             AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "             AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "             AND SEQ = '003' ";
        $query .= "             AND REMARK1 = '{$model->field["TESTDIV0"]}' ";
        $query .= "     ) ";
        $db->query($query);
        //ENTEXAM_RECEPT_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND SEQ = '003' ";
        $query .= "     AND REMARK1 = '{$model->field["TESTDIV0"]}' ";
        $db->query($query);

        //配列ループ
        foreach ($recList as $key => $row) {
            //INSERT
            //ENTEXAM_RECEPT_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]      = $model->field["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]    = "1";
            $data["RECEPTNO"][TEXT]     = $row["RECEPTNO"];
            $data["EXAMNO"][TEXT]       = $row["EXAMNO"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
            $db->query($query);
            //ENTEXAM_RECEPT_DETAIL_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]      = $model->field["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]    = "1";
            $data["RECEPTNO"][TEXT]     = $row["RECEPTNO"];
            $data["SEQ"][TEXT]          = "003";
            $data["REMARK1"][TEXT]      = $model->field["TESTDIV0"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
            $db->query($query);
            //学力診断テスト対象者
            if ($row["GAKU_TEST_FLG"] == "1") {
                //UPDATE
                $data = array();
                $data["GENERAL_FLG"][TEXT]      = "1";
                //条件
                $where  = " WHERE ENTEXAMYEAR   = '{$model->year}' ";
                $where .= "   AND EXAMNO        = '{$row["EXAMNO"]}' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
                $db->query($query);
            }
        }//foreach

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
