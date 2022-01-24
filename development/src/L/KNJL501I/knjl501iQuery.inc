<?php

require_once('for_php7.php');

class knjl501iQuery extends Query
{
    //試験科目一覧取得クエリを取得
    public function getTestClassesListQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     VALUE(NAME1,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND SETTING_CD   = 'L009' ";
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    //年度コンボ
    public function getYear($year)
    {
        $query  = "  WITH TMP(LABEL, VALUE) AS (  ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      ENTEXAMYEAR AS LABEL,";
        $query .= "      ENTEXAMYEAR AS VALUE";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_TESTDIV_MST ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR <> '{$year}' ";
        $query .= "  UNION ";
        $query .= "      VALUES('{$year}', '{$year}')";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      LABEL,";
        $query .= "      VALUE";
        $query .= "  FROM ";
        $query .= "      TMP ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE DESC ";

        return $query;
    }

    //リスト
    public function getList($year, $applicantDiv)
    {
        $query  = "  SELECT ";
        $query .= "    T1.*";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_TESTDIV_MST T1 ";
        $query .= "  WHERE ";
        $query .= "        T1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "    AND T1.APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "  ORDER BY ";
        $query .= "      T1.TESTDIV ";

        return $query;
    }

    //入学申込金納入と設備費納入関連を１レコード取得
    public function getEntryFeeAndCostRow($year, $applicantDiv, $testdiv, $seq = "001")
    {
        $query  = "  SELECT ";
        $query .= "    T1.TESTDIV AS TESTDIV, ";
        $query .= "    T1.TESTDIV_NAME AS TESTDIV_NAME, ";
        $query .= "    T1.TESTDIV_ABBV AS TESTDIV_ABBV, ";
        $query .= "    T2.REMARK1 AS TEST_DATE, ";
        $query .= "    T2.REMARK2 AS ANNOUNCEMENT_DATE, ";
        $query .= "    T2.REMARK3 AS ENTRY_FEE, ";
        $query .= "    T2.REMARK4 AS ENTRY_FEE_CLOSING_DATE, ";
        $query .= "    T2.REMARK5 AS ENTRY_FEE_CLOSING_TIME, ";
        $query .= "    T2.REMARK6 AS EQUIPMENT_COST, ";
        $query .= "    T2.REMARK7 AS EQUIPMENT_COST_CLOSING_DATE, ";
        $query .= "    T2.REMARK8 AS EQUIPMENT_COST_CLOSING_TIME, ";
        $query .= "    T2.REMARK9 AS PAYMENT_CLOSING_DATE ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_TESTDIV_MST T1 ";
        $query .= "      LEFT JOIN ENTEXAM_TESTDIV_DETAIL_SEQ_MST T2 ";
        $query .= "           ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "          AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "          AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "          AND T2.SEQ          = '{$seq}' ";
        $query .= "  WHERE ";
        $query .= "      T1.ENTEXAMYEAR      = '{$year}' ";
        $query .= "      AND T1.APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "      AND T1.TESTDIV      = '{$testdiv}' ";

        return $query;
    }

    //入試科目関連を１レコード取得
    public function getTestClasses($year, $applicantDiv, $testdiv, $seq = "002")
    {
        $query  = "  SELECT ";
        $query .= "    REMARK1  AS TEST_CLASS_1, ";
        $query .= "    REMARK2  AS TEST_CLASS_2, ";
        $query .= "    REMARK3  AS TEST_CLASS_3, ";
        $query .= "    REMARK4  AS TEST_CLASS_4, ";
        $query .= "    REMARK5  AS TEST_CLASS_5, ";
        $query .= "    REMARK6  AS TEST_CLASS_6, ";
        $query .= "    REMARK7  AS TEST_CLASS_7, ";
        $query .= "    REMARK8  AS TEST_CLASS_8, ";
        $query .= "    REMARK9  AS TEST_CLASS_9, ";
        $query .= "    REMARK10 AS TEST_CLASS_10 ";
        $query .= "  FROM ";
        $query .= "    ENTEXAM_TESTDIV_DETAIL_SEQ_MST";
        $query .= "  WHERE ";
        $query .= "    ENTEXAMYEAR        = '{$year}' ";
        $query .= "      AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "      AND TESTDIV      = '{$testdiv}' ";
        $query .= "      AND SEQ          = '{$seq}' ";

        return $query;
    }

    //入学前スケジュール１関連を１レコード取得　※内部用。共通サブルーチン。
    private function getEntryScheduleQuery($year, $applicantDiv, $testdiv, $seq, $scheduleNo)
    {
        $query  = "  SELECT ";
        $query .= "    REMARK1 AS ENTRY_SCHDL_{$scheduleNo}_EVNT_NAME, ";        //行事名
        $query .= "    REMARK2 AS ENTRY_SCHDL_{$scheduleNo}_EVNT_OPEN_DATE, ";   //日付
        $query .= "    REMARK3 AS ENTRY_SCHDL_{$scheduleNo}_EVNT_OPEN_TIME, ";   //時間From
        $query .= "    REMARK4 AS ENTRY_SCHDL_{$scheduleNo}_EVNT_CLOSE_TIME, ";  //時間To
        $query .= "    REMARK5 AS ENTRY_SCHDL_{$scheduleNo}_EVNT_PLACE, ";       //会場
        $query .= "    REMARK6 AS ENTRY_SCHDL_{$scheduleNo}_EVNT_MAIN ";         //対象者
        $query .= "  FROM ";
        $query .= "    ENTEXAM_TESTDIV_DETAIL_SEQ_MST";
        $query .= "  WHERE ";
        $query .= "    ENTEXAMYEAR        = '{$year}' ";
        $query .= "      AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "      AND TESTDIV      = '{$testdiv}' ";
        $query .= "      AND SEQ          = '{$seq}' ";

        return $query;
    }

    //入学前スケジュール１関連を１レコード取得
    public function getQueryEntrySchedule1($year, $applicantDiv, $testdiv)
    {
        return knjl501iQuery::getEntryScheduleQuery($year, $applicantDiv, $testdiv, '003', 1);
    }

    //入学前スケジュール１関連を１レコード取得
    public function getQueryEntrySchedule2($year, $applicantDiv, $testdiv)
    {
        return knjl501iQuery::getEntryScheduleQuery($year, $applicantDiv, $testdiv, '004', 2);
    }

    //入学前スケジュール１関連を１レコード取得
    public function getQueryEntrySchedule3($year, $applicantDiv, $testdiv)
    {
        return knjl501iQuery::getEntryScheduleQuery($year, $applicantDiv, $testdiv, '005', 3);
    }

    //入学前スケジュール１関連を１レコード取得
    public function getQueryEntrySchedule4($year, $applicantDiv, $testdiv)
    {
        return knjl501iQuery::getEntryScheduleQuery($year, $applicantDiv, $testdiv, '006', 4);
    }

    //入学前スケジュール１関連を１レコード取得
    public function getQueryEntrySchedule5($year, $applicantDiv, $testdiv)
    {
        return knjl501iQuery::getEntryScheduleQuery($year, $applicantDiv, $testdiv, '007', 5);
    }

    //データカウント(ENTEXAM_TESTDIV_DETAIL_MST)
    public function getCountDetailSeq($year, $applicantDiv, $testdiv, $sequence)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(ENTEXAMYEAR) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_DETAIL_SEQ_MST ";
        $query .= " WHERE ";
        $query .= "      ENTEXAMYEAR      = '{$year}' ";
        $query .= "        AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "        AND TESTDIV      = '{$testdiv}' ";
        $query .= "        AND SEQ          = '{$sequence}' ";
        return $query;
    }


    /************ コピー ***************/
    //ある年度にデータが存在するのかチェック
    public function getCheckTestDivMstSql($year, $applicantDiv, $testdiv)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(ENTEXAMYEAR) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "      ENTEXAMYEAR      = '{$year}' ";
        $query .= "     AND APPLICANTDIV  = '{$applicantDiv}' ";
        if (!is_null($testdiv)) {
            $query .= " AND TESTDIV       = '{$testdiv}' ";
        }

        return $query;
    }

    //ある年度にデータが存在するのかチェック(ENTEXAM_TESTDIV_DETAIL_MST)
    public function getCheckTestDivDetailMstSql($year, $applicantDiv, $testdiv)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(ENTEXAMYEAR) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_DETAIL_SEQ_MST ";
        $query .= " WHERE ";
        $query .= "      ENTEXAMYEAR        = '{$year}' ";
        $query .= "        AND APPLICANTDIV = '{$applicantDiv}' ";
        if (!is_null($testdiv)) {
            $query .= "    AND TESTDIV      = '{$testdiv}' ";
        }

        return $query;
    }

    //対象年度からのコピー(ENTEXAM_TESTDIV_MST)
    public function copyTestDivMstQuery($copyFrom, $copyTo, $applicantDiv)
    {
        //DB接続を開始
        $db = Query::dbCheckOut();

        //自動コミットを無効（コミット実行まで１つのトランザクションにまとめる）
        $db->autoCommit(false);

        //ENTEXAM_TESTDIV_MST
        $query  = " INSERT INTO ENTEXAM_TESTDIV_MST(ENTEXAMYEAR,APPLICANTDIV,TESTDIV,TESTDIV_NAME,TESTDIV_ABBV,CAPACITY,TEST_DATE,REGISTERCD,UPDATED) ";
        $query .= " SELECT ";
        $query .= "     '{$copyTo}' AS ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     TESTDIV_ABBV, ";
        $query .= "     CAPACITY, ";
        $query .= "     TEST_DATE, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST T1 ";
        $query .= "     INNER JOIN V_NAME_MST T2 ";
        $query .= "        ON T2.YEAR    = '{$copyFrom}' ";
        $query .= "       AND T2.NAMECD1 = 'L003' ";
        $query .= "       AND T2.NAMECD2 = T1.APPLICANTDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$copyFrom}' ";
        $query .= "       AND T1.APPLICANTDIV = '{$applicantDiv}'";

        //コピークエリを実行
        $db->query($query);

        // 重複回避のためInsert前に既存レコードを削除
        $query  = " DELETE FROM ";
        $query .= "   ENTEXAM_TESTDIV_DETAIL_SEQ_MST ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR = '{$copyTo}' ";

        //既存削除クエリを実行
        $db->query($query);

        //ENTEXAM_TESTDIV_DETAIL_MST
        //INSERT
        $query  = " INSERT INTO ENTEXAM_TESTDIV_DETAIL_SEQ_MST(ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ, REMARK1, REMARK2, REMARK3, REMARK4, REMARK5, REMARK6, REMARK7, REMARK8, REMARK9, REMARK10, REGISTERCD, UPDATED) ";
        $query .= " SELECT ";
        $query .= "     '{$copyTo}', ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     SEQ, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2, ";
        $query .= "     REMARK3, ";
        $query .= "     REMARK4, ";
        $query .= "     REMARK5, ";
        $query .= "     REMARK6, ";
        $query .= "     REMARK7, ";
        $query .= "     REMARK8, ";
        $query .= "     REMARK9, ";
        $query .= "     REMARK10, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_DETAIL_SEQ_MST T1 ";
        $query .= "     INNER JOIN V_NAME_MST T2 ";
        $query .= "        ON T2.YEAR    = '{$copyFrom}' ";
        $query .= "       AND T2.NAMECD1 = 'L003' ";
        $query .= "       AND T2.NAMECD2 = T1.APPLICANTDIV ";
        $query .= " WHERE ";
        $query .= "      T1.ENTEXAMYEAR  = '{$copyFrom}' ";
        $query .= "  AND T1.APPLICANTDIV = '{$applicantDiv}'";

        //コピークエリを実行
        $db->query($query);

        //追加削除を確定
        $db->commit();

        // DB接続を切断
        Query::dbCheckIn($db);
    }


    /******** 追加更新処理  ********/
    //--- INSERT
    public function insertTestDivAndDetailSeq($year, $fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_TESTDIV_MST
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $year;
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["TESTDIV_NAME"][TEXT]     = $fields["TESTDIV_NAME"];
        $data["TESTDIV_ABBV"][TEXT]     = $fields["TESTDIV_ABBV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_TESTDIV_MST");
        $db->query($query);

        //ENTEXAM_TESTDIV_DETAIL_SEQ_MST　※追加または更新を実行
        $query = knjl501iQuery::getDetailSeqInsOrUpdQuery($db, $year, $fields, '001');
        $db->query($query);
        $query = knjl501iQuery::getDetailSeqInsOrUpdQuery($db, $year, $fields, '002');
        $db->query($query);
        $query = knjl501iQuery::getDetailSeqInsOrUpdQuery($db, $year, $fields, '003');
        $db->query($query);
        $query = knjl501iQuery::getDetailSeqInsOrUpdQuery($db, $year, $fields, '004');
        $db->query($query);
        $query = knjl501iQuery::getDetailSeqInsOrUpdQuery($db, $year, $fields, '005');
        $db->query($query);
        $query = knjl501iQuery::getDetailSeqInsOrUpdQuery($db, $year, $fields, '006');
        $db->query($query);
        $query = knjl501iQuery::getDetailSeqInsOrUpdQuery($db, $year, $fields, '007');
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    //--- UPDATE
    public function updateTestDivAndDetailSeq($year, $fields)
    {
        $testDev = $fields["TESTDIV"];
        $applicantDiv = $fields["APPLICANTDIV"];
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_TESTDIV_MST
        $data = array();
        $data["TESTDIV_NAME"][TEXT]     = $fields["TESTDIV_NAME"];
        $data["TESTDIV_ABBV"][TEXT]     = $fields["TESTDIV_ABBV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "   ENTEXAMYEAR       = '{$year}' ";
        $where .= "   AND APPLICANTDIV  = '{$applicantDiv}'";
        $where .= "   AND TESTDIV       = '{$testDev}' ";

        $query = Query::updateSQL($data, "ENTEXAM_TESTDIV_MST", $where);
        $db->query($query);

        //ENTEXAM_TESTDIV_DETAIL_MST
        $query = knjl501iQuery::getDetailSeqInsOrUpdQuery($db, $year, $fields, '001');
        $db->query($query);
        $query = knjl501iQuery::getDetailSeqInsOrUpdQuery($db, $year, $fields, '002');
        $db->query($query);
        $query = knjl501iQuery::getDetailSeqInsOrUpdQuery($db, $year, $fields, '003');
        $db->query($query);
        $query = knjl501iQuery::getDetailSeqInsOrUpdQuery($db, $year, $fields, '004');
        $db->query($query);
        $query = knjl501iQuery::getDetailSeqInsOrUpdQuery($db, $year, $fields, '005');
        $db->query($query);
        $query = knjl501iQuery::getDetailSeqInsOrUpdQuery($db, $year, $fields, '006');
        $db->query($query);
        $query = knjl501iQuery::getDetailSeqInsOrUpdQuery($db, $year, $fields, '007');
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    //指定された年度に合致する入試区分詳細シーケンスマスタを更新するためのSQLクエリを取得
    public function &getDetailSeqInsOrUpdQuery($db, $year, $fields, $seq)
    {
        $testDiv = $fields["TESTDIV"];
        $applicantDiv = $fields["APPLICANTDIV"];
        //ENTEXAM_TESTDIV_DETAIL_MST
        //cnt
        $getCount = (int)$db->getOne(knjl501iQuery::getCountDetailSeq($year, $applicantDiv, $testDiv, $seq));

        // 更新対象共通項目
        $data = array();
        switch ($seq) {
            case "001": //入学試験日、合格発表日、入学申込金納入および施設設備費納入関連
                $data["REMARK1"][TEXT]          = $fields["TEST_DATE"];
                $data["REMARK2"][TEXT]          = $fields["ANNOUNCEMENT_DATE"];
                $data["REMARK3"][TEXT]          = $fields["ENTRY_FEE"];
                $data["REMARK4"][TEXT]          = $fields["ENTRY_FEE_CLOSING_DATE"];
                $data["REMARK5"][TEXT]          = $fields["ENTRY_FEE_CLOSING_TIME"];
                $data["REMARK6"][TEXT]          = $fields["EQUIPMENT_COST"];
                $data["REMARK7"][TEXT]          = $fields["EQUIPMENT_COST_CLOSING_DATE"];
                $data["REMARK8"][TEXT]          = $fields["EQUIPMENT_COST_CLOSING_TIME"];
                $data["REMARK9"][TEXT]          = $fields["PAYMENT_CLOSING_DATE"];
                $data["REMARK10"][TEXT]         = ""; // DB NULLを設定
                break;

            case "002": //入試科目
                $data["REMARK1"][TEXT]          = $fields["TEST_CLASS_1"];
                $data["REMARK2"][TEXT]          = $fields["TEST_CLASS_2"];
                $data["REMARK3"][TEXT]          = $fields["TEST_CLASS_3"];
                $data["REMARK4"][TEXT]          = $fields["TEST_CLASS_4"];
                $data["REMARK5"][TEXT]          = $fields["TEST_CLASS_5"];
                $data["REMARK6"][TEXT]          = $fields["TEST_CLASS_6"];
                $data["REMARK7"][TEXT]          = $fields["TEST_CLASS_7"];
                $data["REMARK8"][TEXT]          = $fields["TEST_CLASS_8"];
                $data["REMARK9"][TEXT]          = $fields["TEST_CLASS_9"];
                $data["REMARK10"][TEXT]         = $fields["TEST_CLASS_10"];
                break;

            case "003": //入学式までのスケジュール1
                $data["REMARK1"][TEXT]          = $fields["ENTRY_SCHDL_1_EVNT_NAME"];
                $data["REMARK2"][TEXT]          = $fields["ENTRY_SCHDL_1_EVNT_OPEN_DATE"];
                $data["REMARK3"][TEXT]          = $fields["ENTRY_SCHDL_1_EVNT_OPEN_TIME"];
                $data["REMARK4"][TEXT]          = $fields["ENTRY_SCHDL_1_EVNT_CLOSE_TIME"];
                $data["REMARK5"][TEXT]          = $fields["ENTRY_SCHDL_1_EVNT_PLACE"];
                $data["REMARK6"][TEXT]          = $fields["ENTRY_SCHDL_1_EVNT_MAIN"];
                $data["REMARK7"][TEXT]          = ""; // DB NULLを設定
                $data["REMARK8"][TEXT]          = ""; // DB NULLを設定
                $data["REMARK9"][TEXT]          = ""; // DB NULLを設定
                $data["REMARK10"][TEXT]         = ""; // DB NULLを設定
                break;

            case "004": //入学式までのスケジュール2
                $data["REMARK1"][TEXT]          = $fields["ENTRY_SCHDL_2_EVNT_NAME"];
                $data["REMARK2"][TEXT]          = $fields["ENTRY_SCHDL_2_EVNT_OPEN_DATE"];
                $data["REMARK3"][TEXT]          = $fields["ENTRY_SCHDL_2_EVNT_OPEN_TIME"];
                $data["REMARK4"][TEXT]          = $fields["ENTRY_SCHDL_2_EVNT_CLOSE_TIME"];
                $data["REMARK5"][TEXT]          = $fields["ENTRY_SCHDL_2_EVNT_PLACE"];
                $data["REMARK6"][TEXT]          = $fields["ENTRY_SCHDL_2_EVNT_MAIN"];
                $data["REMARK7"][TEXT]          = ""; // DB NULLを設定
                $data["REMARK8"][TEXT]          = ""; // DB NULLを設定
                $data["REMARK9"][TEXT]          = ""; // DB NULLを設定
                $data["REMARK10"][TEXT]         = ""; // DB NULLを設定
                break;

            case "005": //入学式までのスケジュール3
                $data["REMARK1"][TEXT]          = $fields["ENTRY_SCHDL_3_EVNT_NAME"];
                $data["REMARK2"][TEXT]          = $fields["ENTRY_SCHDL_3_EVNT_OPEN_DATE"];
                $data["REMARK3"][TEXT]          = $fields["ENTRY_SCHDL_3_EVNT_OPEN_TIME"];
                $data["REMARK4"][TEXT]          = $fields["ENTRY_SCHDL_3_EVNT_CLOSE_TIME"];
                $data["REMARK5"][TEXT]          = $fields["ENTRY_SCHDL_3_EVNT_PLACE"];
                $data["REMARK6"][TEXT]          = $fields["ENTRY_SCHDL_3_EVNT_MAIN"];
                $data["REMARK7"][TEXT]          = ""; // DB NULLを設定
                $data["REMARK8"][TEXT]          = ""; // DB NULLを設定
                $data["REMARK9"][TEXT]          = ""; // DB NULLを設定
                $data["REMARK10"][TEXT]         = ""; // DB NULLを設定
                break;

            case "006": //入学式までのスケジュール4
                $data["REMARK1"][TEXT]          = $fields["ENTRY_SCHDL_4_EVNT_NAME"];
                $data["REMARK2"][TEXT]          = $fields["ENTRY_SCHDL_4_EVNT_OPEN_DATE"];
                $data["REMARK3"][TEXT]          = $fields["ENTRY_SCHDL_4_EVNT_OPEN_TIME"];
                $data["REMARK4"][TEXT]          = $fields["ENTRY_SCHDL_4_EVNT_CLOSE_TIME"];
                $data["REMARK5"][TEXT]          = $fields["ENTRY_SCHDL_4_EVNT_PLACE"];
                $data["REMARK6"][TEXT]          = $fields["ENTRY_SCHDL_4_EVNT_MAIN"];
                $data["REMARK7"][TEXT]          = ""; // DB NULLを設定
                $data["REMARK8"][TEXT]          = ""; // DB NULLを設定
                $data["REMARK9"][TEXT]          = ""; // DB NULLを設定
                $data["REMARK10"][TEXT]         = ""; // DB NULLを設定
                break;

            case "007": //入学式までのスケジュール5
                $data["REMARK1"][TEXT]          = $fields["ENTRY_SCHDL_5_EVNT_NAME"];
                $data["REMARK2"][TEXT]          = $fields["ENTRY_SCHDL_5_EVNT_OPEN_DATE"];
                $data["REMARK3"][TEXT]          = $fields["ENTRY_SCHDL_5_EVNT_OPEN_TIME"];
                $data["REMARK4"][TEXT]          = $fields["ENTRY_SCHDL_5_EVNT_CLOSE_TIME"];
                $data["REMARK5"][TEXT]          = $fields["ENTRY_SCHDL_5_EVNT_PLACE"];
                $data["REMARK6"][TEXT]          = $fields["ENTRY_SCHDL_5_EVNT_MAIN"];
                $data["REMARK7"][TEXT]          = ""; // DB NULLを設定
                $data["REMARK8"][TEXT]          = ""; // DB NULLを設定
                $data["REMARK9"][TEXT]          = ""; // DB NULLを設定
                $data["REMARK10"][TEXT]         = ""; // DB NULLを設定
                break;

            default:
                //シーケンスNo指定エラー
                return null;
        }

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        // 既存の有無に合わせて発行するクエリ内容を選別
        if ($getCount < 1) {
            $data["ENTEXAMYEAR"][TEXT]       = $year;
            $data["APPLICANTDIV"][TEXT]      = $applicantDiv;
            $data["TESTDIV"][TEXT]           = $testDiv;
            $data["SEQ"][TEXT]               = $seq;

            //追加クエリを生成
            $query = Query::insertSQL($data, "ENTEXAM_TESTDIV_DETAIL_SEQ_MST");
        } else {
            $where  = " WHERE ";
            $where .= "   ENTEXAMYEAR       = '{$year}' ";
            $where .= "   AND APPLICANTDIV  = '{$applicantDiv}'";
            $where .= "   AND TESTDIV       = '{$testDiv}' ";
            $where .= "   AND SEQ           = '{$seq}' ";

            //更新クエリを生成
            $query = Query::updateSQL($data, "ENTEXAM_TESTDIV_DETAIL_SEQ_MST", $where);
        }
        return $query;
    }

    //指定された年度に合致する入試区分マスタとそれに紐づく詳細シーケンスマスタを削除
    public function deleteTestDivAndDetailSeq($year, $fields)
    {
        $testDiv = $fields["TESTDIV"];
        $applicantDiv = $fields["APPLICANTDIV"];

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //入学試験日、合格発表日、入学申込金納入および施設設備費納入関連、入学前スケジュールを削除
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_TESTDIV_DETAIL_SEQ_MST ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR       = '{$year}' ";
        $query .= "   AND APPLICANTDIV  = '{$applicantDiv}'";
        $query .= "   AND TESTDIV       = '{$testDiv}' ";
        $db->query($query);

        //「ENTEXAM_TESTDIV_DETAIL_SEQ_MST」削除により紐づけを失い孤立したヘッダを削除
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR       = '{$year}' ";
        $query .= "   AND APPLICANTDIV  = '{$applicantDiv}'";
        $query .= "   AND TESTDIV       = '{$testDiv}' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }
}
