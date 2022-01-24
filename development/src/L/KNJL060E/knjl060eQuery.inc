<?php

require_once('for_php7.php');

class knjl060eQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2='') {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     value(NAMESPARE3, '0') as NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2 != '') {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        if ($namecd1 == "L004") {
            $query .= "     AND VALUE(NAMESPARE1,'') != '1' "; //入試区分一般（名称マスタ「L004」予備1’1：推薦’以外）が対象
        }
        if ($namecd1 == "L065") {
            $query .= "     AND VALUE(NAMESPARE1,'') = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象人数チェック
    function checkBaseCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     INNER JOIN V_NAME_MST L004 ON L004.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "                               AND L004.NAMECD1    = 'L004' ";
        $query .= "                               AND L004.NAMECD2    = BASE.TESTDIV ";
        $query .= "                               AND L004.NAMESPARE1 = '{$model->nittei}' ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR     = '".$model->ObjYear."' ";
        $query .= "     AND BASE.APPLICANTDIV    = '".$model->applicantdiv."' ";
        $query .= "     AND VALUE(BASE.JUDGEMENT,'') != '3' "; //欠席者は除く

        return $query;
    }

    //入試区分取得
    function getTestDiv($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1    = 'L004' ";
        $query .= "     AND NAMESPARE1 = '{$model->nittei}' ";

        return $query;
    }

    //合計・平均・順位算出
    function getCulcQuery($model, $testDiv) {
        $query  = " WITH BASE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         L1.EXAMNO AS EXAMNO030, "; //レコード存在確認用
        $query .= "         CASE WHEN VALUE(L004.NAMESPARE3, '') = '2' AND VALUE(L061.NAMESPARE1, '') <> '1' THEN '1' END AS KAMOKU2 "; // 2科目のみ
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L1 ";
        $query .= "              ON T1.ENTEXAMYEAR  = L1.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV = L1.APPLICANTDIV ";
        $query .= "             AND T1.EXAMNO       = L1.EXAMNO ";
        $query .= "             AND L1.SEQ          = '030' ";
        $query .= "         LEFT JOIN V_NAME_MST L004 ";
        $query .= "              ON L004.YEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND L004.NAMECD1 = 'L004' ";
        $query .= "             AND L004.NAMECD2 = T1.TESTDIV ";
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD33 ";
        $query .= "              ON BD33.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND BD33.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND BD33.EXAMNO = T1.EXAMNO ";
        $query .= "             AND BD33.SEQ = '033' ";
        $query .= "         LEFT JOIN V_NAME_MST L061 ";
        $query .= "              ON L061.YEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND L061.NAMECD1 = 'L061' ";
        $query .= "             AND L061.NAMECD2 = BD33.REMARK3 ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "         T1.APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "         L004.NAMESPARE1    = '".$model->nittei."' AND ";
        $query .= "         VALUE(T1.JUDGEMENT,'') != '3' "; //欠席者は除く
        $query .= " ), SCORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXAMNO, ";
        $query .= "         SUM(L1.SCORE) AS TOTAL, ";
        $query .= "         CASE WHEN MAX(T1.KAMOKU2) IS NULL AND SUM(L1.SCORE) IS NOT NULL THEN 0 ELSE 1 END AS FLG, ";
        $query .= "         ROUND(AVG(DECIMAL(L1.SCORE)),1) AS AVG, ";
        $query .= "         SUM(CASE WHEN L009.NAMESPARE2 = '1' THEN L1.SCORE END) AS TOTAL2, ";
        $query .= "         CASE WHEN SUM(CASE WHEN L009.NAMESPARE2 = '1' THEN L1.SCORE END) IS NOT NULL THEN 0 ELSE 1 END AS FLG2, ";
        $query .= "         ROUND(AVG(DECIMAL(CASE WHEN L009.NAMESPARE2 = '1' THEN L1.SCORE END)),1) AS AVG2 ";
        $query .= "     FROM ";
        $query .= "         BASE T1 ";
        $query .= "         LEFT JOIN ENTEXAM_SCORE_DAT L1 ";
        $query .= "              ON T1.ENTEXAMYEAR  = L1.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV = L1.APPLICANTDIV ";
        $query .= "             AND T1.TESTDIV      = L1.TESTDIV ";
        $query .= "             AND L1.EXAM_TYPE    = '".$model->exam_type."' ";
        $query .= "             AND T1.EXAMNO       = L1.RECEPTNO ";
        $query .= "         LEFT JOIN V_NAME_MST L009 ";
        $query .= "              ON L009.YEAR       = L1.ENTEXAMYEAR ";
        $query .= "             AND L009.NAMECD1    = 'L009' ";
        $query .= "             AND L009.NAMECD2    = L1.TESTSUBCLASSCD ";
        $query .= "     GROUP BY ";
        $query .= "         T1.EXAMNO ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXAMNO, ";
        $query .= "         T1.TESTDIV, ";
        $query .= "         T1.TESTDIV1, ";
        $query .= "         T1.DESIREDIV, ";
        $query .= "         T1.EXAMNO030, ";
        //2教科
        $query .= "         L2.TOTAL2 AS TOTAL2, ";
        $query .= "         L2.AVG2 AS AVARAGE2, ";
        $query .= "         L2.FLG2 AS TOTAL_FLG2, ";
        $query .= "         RANK() OVER(PARTITION BY L2.FLG2 ORDER BY L2.TOTAL2 DESC) AS TOTAL_RANK2, ";
        //全教科
        $query .= "         L2.TOTAL AS TOTAL3, ";
        $query .= "         L2.AVG AS AVARAGE3, ";
        $query .= "         L2.FLG AS TOTAL_FLG3, ";
        $query .= "         RANK() OVER(PARTITION BY L2.FLG ORDER BY L2.TOTAL DESC) AS TOTAL_RANK3, ";
        //全教科・・・面接点も加算するかも？今は、TOTAL3と同じのをセットして下さい（宮城さん）
        $query .= "         L2.TOTAL AS TOTAL4, ";
        $query .= "         L2.AVG AS AVARAGE4, ";
        $query .= "         L2.FLG AS TOTAL_FLG4, ";
        $query .= "         RANK() OVER(PARTITION BY L2.FLG ORDER BY L2.TOTAL DESC) AS TOTAL_RANK4 ";
        $query .= "     FROM ";
        $query .= "         BASE T1 ";
        $query .= "         LEFT JOIN SCORE L2 ON T1.EXAMNO = L2.EXAMNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= " WHERE ";
        $query .= "     T1.TESTDIV = '{$testDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //算出
    function ExecuteQuery($model) {
        //DB接続
        $db = Query::dbCheckOut();

        //対象入試区分取得
        $testDivArr = array();
        $inTestDiv  = "'";
        $sep = '';
        $query = knjl060eQuery::getTestDiv($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testDivArr[$row["NAMECD2"]] = $row["NAMECD2"];
            $inTestDiv .= $sep.$row["NAMECD2"];
            $sep = "','";
        }
        $inTestDiv .= "'";

        //エラー無視
        $db->expectError();

        foreach ($testDivArr as $testDiv) {
            //削除
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_RECEPT_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
            $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
            $query .= "     TESTDIV         = '".$testDiv."' AND ";
            $query .= "     EXAM_TYPE       = '".$model->exam_type."' ";
            $db->query($query);

            //算出データ取得
            $data_flg = false;
            $query = knjl060eQuery::getCulcQuery($model, $testDiv);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //追加
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
                $data["TESTDIV"][TEXT]              = $testDiv;
                $data["EXAM_TYPE"][TEXT]            = $model->exam_type;
                $data["RECEPTNO"][TEXT]             = $row["EXAMNO"];
                $data["EXAMNO"][TEXT]               = $row["EXAMNO"];
                $data["TOTAL2"][NUMBER]             = $row["TOTAL2"];
                $data["AVARAGE2"][NUMBER]           = $row["AVARAGE2"];
                $data["TOTAL_RANK2"][NUMBER]        = ($row["TOTAL_FLG2"] == 1) ? "" : $row["TOTAL_RANK2"];
                $data["TOTAL3"][NUMBER]             = $row["TOTAL3"];
                $data["AVARAGE3"][NUMBER]           = $row["AVARAGE3"];
                $data["TOTAL_RANK3"][NUMBER]        = ($row["TOTAL_FLG3"] == 1) ? "" : $row["TOTAL_RANK3"];
                $data["TOTAL4"][NUMBER]             = $row["TOTAL4"];
                $data["AVARAGE4"][NUMBER]           = $row["AVARAGE4"];
                $data["TOTAL_RANK4"][NUMBER]        = ($row["TOTAL_FLG4"] == 1) ? "" : $row["TOTAL_RANK4"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "sysdate()";

                $result1 = $db->query(Query::insertSQL($data, "ENTEXAM_RECEPT_DAT"));
                //エラー時後処理
                if (DB::isError($result1)) {
                   $db->popExpect();
                   Query::dbCheckIn($db);
                   return $result1->getCode();
                }
                $data_flg = true;
            }
            $result->free();
        }

        //対象データなし
        if (!$data_flg) {
           return "NO_DATA";
        }

        //--------------------【（得点・合計）別　平均・標準偏差など】--------------------
        //DELETE
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_JUDGE_AVARAGE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV      = '{$model->nittei}' ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        $query  = " INSERT INTO ENTEXAM_JUDGE_AVARAGE_DAT ( ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     EXAM_TYPE, ";
        $query .= "     TESTSUBCLASSCD, ";
        $query .= "     AVARAGE_MEN, ";
        $query .= "     AVARAGE_WOMEN, ";
        $query .= "     AVARAGE_TOTAL, ";
        $query .= "     MAX_SCORE, ";
        $query .= "     MIN_SCORE, ";
        $query .= "     COUNT, ";
        $query .= "     CALC_STDDEV, ";
        $query .= "     CALC_AVG, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        //得点
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     '{$model->nittei}' as TESTDIV, ";
        $query .= "     '{$model->exam_type}' AS EXAM_TYPE, ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '1' THEN T1.SCORE END))*10,0)/10,5,1) AS AVARAGE_MEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '2' THEN T1.SCORE END))*10,0)/10,5,1) AS AVARAGE_WOMEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.SCORE))*10,0)/10,5,1) AS AVARAGE_TOTAL, ";
        $query .= "     MAX(T1.SCORE) AS MAX_SCORE, ";
        $query .= "     MIN(T1.SCORE) AS MIN_SCORE, ";
        $query .= "     COUNT(T1.SCORE) AS COUNT, ";
        $query .= "     DECIMAL(ROUND(STDDEV(FLOAT(T1.SCORE))*100000,0)/100000,8,5) AS CALC_STDDEV, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.SCORE))*100000,0)/100000,8,5) AS CALC_AVG, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT T2 ";
        $query .= "          ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND T2.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "         AND T2.RECEPTNO     = T1.RECEPTNO ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T3 ";
        $query .= "          ON T3.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "         AND T3.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND T3.EXAMNO       = T2.EXAMNO ";
        $query .= "     INNER JOIN V_NAME_MST L004 ON L004.YEAR       = T1.ENTEXAMYEAR ";
        $query .= "                               AND L004.NAMECD1    = 'L004' ";
        $query .= "                               AND L004.NAMECD2    = T1.TESTDIV ";
        $query .= "                               AND L004.NAMESPARE1 = '{$model->nittei}' ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTSUBCLASSCD ";
        //合計(A)(TOTAL4)推薦者含まない
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     '{$model->nittei}' as TESTDIV, ";
        $query .= "     '{$model->exam_type}' AS EXAM_TYPE, ";
        $query .= "     'A' AS TESTSUBCLASSCD, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '1' THEN T1.TOTAL4 END))*10,0)/10,5,1) AS AVARAGE_MEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '2' THEN T1.TOTAL4 END))*10,0)/10,5,1) AS AVARAGE_WOMEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL4))*10,0)/10,5,1) AS AVARAGE_TOTAL, ";
        $query .= "     MAX(T1.TOTAL4) AS MAX_SCORE, ";
        $query .= "     MIN(T1.TOTAL4) AS MIN_SCORE, ";
        $query .= "     COUNT(T1.TOTAL4) AS COUNT, ";
        $query .= "     DECIMAL(ROUND(STDDEV(FLOAT(T1.TOTAL4))*100000,0)/100000,8,5) AS CALC_STDDEV, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL4))*100000,0)/100000,8,5) AS CALC_AVG, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T3 ";
        $query .= "          ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T3.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D002 ";
        $query .= "          ON D002.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND D002.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND D002.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND D002.SEQ          = '002' ";
        $query .= "     INNER JOIN V_NAME_MST L004 ON L004.YEAR       = T1.ENTEXAMYEAR ";
        $query .= "                               AND L004.NAMECD1    = 'L004' ";
        $query .= "                               AND L004.NAMECD2    = T1.TESTDIV ";
        $query .= "                               AND L004.NAMESPARE1 = '{$model->nittei}' ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND D002.REMARK1 not in (SELECT ";
        $query .= "                                 D002.REMARK1 ";//CKIPのログインＩＤ
        $query .= "                             FROM ";
        $query .= "                                 ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                                 LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D002 ";
        $query .= "                                      ON D002.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "                                     AND D002.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "                                     AND D002.EXAMNO       = B1.EXAMNO ";
        $query .= "                                     AND D002.SEQ          = '002' ";
        $query .= "                                 LEFT JOIN V_NAME_MST L004_2 ";
        $query .= "                                      ON L004_2.YEAR    = B1.ENTEXAMYEAR ";
        $query .= "                                     AND L004_2.NAMECD1 = 'L004' ";
        $query .= "                                     AND L004_2.NAMECD2 = B1.TESTDIV ";
        $query .= "                             WHERE ";
        $query .= "                                     B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                                 AND B1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "                                 AND value(L004_2.NAMESPARE1, '0') = '1' ";// 1:推薦
        $query .= "                             ) ";
        $query .= " GROUP BY ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV ";
        //合計(B)(TOTAL4)推薦者含む
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     '{$model->nittei}' as TESTDIV, ";
        $query .= "     '{$model->exam_type}' AS EXAM_TYPE, ";
        $query .= "     'B' AS TESTSUBCLASSCD, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '1' THEN T1.TOTAL4 END))*10,0)/10,5,1) AS AVARAGE_MEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T3.SEX = '2' THEN T1.TOTAL4 END))*10,0)/10,5,1) AS AVARAGE_WOMEN, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL4))*10,0)/10,5,1) AS AVARAGE_TOTAL, ";
        $query .= "     MAX(T1.TOTAL4) AS MAX_SCORE, ";
        $query .= "     MIN(T1.TOTAL4) AS MIN_SCORE, ";
        $query .= "     COUNT(T1.TOTAL4) AS COUNT, ";
        $query .= "     DECIMAL(ROUND(STDDEV(FLOAT(T1.TOTAL4))*100000,0)/100000,8,5) AS CALC_STDDEV, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL4))*100000,0)/100000,8,5) AS CALC_AVG, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T3 ";
        $query .= "          ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T3.EXAMNO       = T1.EXAMNO ";
        $query .= "     INNER JOIN V_NAME_MST L004 ON L004.YEAR       = T1.ENTEXAMYEAR ";
        $query .= "                               AND L004.NAMECD1    = 'L004' ";
        $query .= "                               AND L004.NAMECD2    = T1.TESTDIV ";
        $query .= "                               AND L004.NAMESPARE1 = '{$model->nittei}' ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        /* 偏差値 */
        $query  = "UPDATE ENTEXAM_RECEPT_DAT ";
        $query .= "   SET (EXAMNO, ";
        $query .= "        JUDGE_DEVIATION, ";
        $query .= "        LINK_JUDGE_DEVIATION, ";
        $query .= "        REGISTERCD, ";
        $query .= "        UPDATED) ";
        $query .= "     = ( ";
        $query .= " SELECT TT0.EXAMNO ";
        $query .= "       ,TT4.JUDGE_DEVIATION ";        //入試区分偏差値(TOTAL4)推薦者含まない
        $query .= "       ,TT4.LINK_JUDGE_DEVIATION ";   //入試区分偏差値(TOTAL4)推薦者含む
        $query .= "       ,'".STAFFCD."' ";
        $query .= "       ,SYSDATE() ";
        $query .= "  FROM ENTEXAM_RECEPT_DAT TT0 ";
        $query .= "       INNER JOIN V_NAME_MST L004_0 ON L004_0.YEAR       = TT0.ENTEXAMYEAR ";
        $query .= "                                   AND L004_0.NAMECD1    = 'L004' ";
        $query .= "                                   AND L004_0.NAMECD2    = TT0.TESTDIV ";
        $query .= "                                   AND L004_0.NAMESPARE1 = '{$model->nittei}' ";
        $query .= "       LEFT JOIN ( ";
        $query .= "         SELECT  R1.RECEPTNO";
        $query .= "                ,R1.EXAM_TYPE";
        $query .= "                ,case when S1.EXAMNO is not null then null else ";//推薦はJUDGE_DEVIATIONをセットしない
        $query .= "                 CASE WHEN 0 < R2.CALC_STDDEV THEN DECIMAL(ROUND((10*(R1.TOTAL4-R2.CALC_AVG)/R2.CALC_STDDEV+50)*10,0)/10,5,1) END end AS JUDGE_DEVIATION ";
        $query .= "                ,CASE WHEN 0 < R3.CALC_STDDEV THEN DECIMAL(ROUND((10*(R1.TOTAL4-R3.CALC_AVG)/R3.CALC_STDDEV+50)*10,0)/10,5,1) END AS LINK_JUDGE_DEVIATION ";
        $query .= "           FROM  ENTEXAM_RECEPT_DAT R1";
        $query .= "                 LEFT JOIN ENTEXAM_JUDGE_AVARAGE_DAT R2 ";
        $query .= "                      ON R2.ENTEXAMYEAR    = R1.ENTEXAMYEAR ";
        $query .= "                     AND R2.APPLICANTDIV   = R1.APPLICANTDIV ";
        $query .= "                     AND R2.TESTDIV        = '{$model->nittei}' ";
        $query .= "                     AND R2.EXAM_TYPE      = '{$model->exam_type}' ";
        $query .= "                     AND R2.TESTSUBCLASSCD = 'A' ";
        $query .= "                 LEFT JOIN ENTEXAM_JUDGE_AVARAGE_DAT R3 ";
        $query .= "                      ON R3.ENTEXAMYEAR    = R1.ENTEXAMYEAR ";
        $query .= "                     AND R3.APPLICANTDIV   = R1.APPLICANTDIV ";
        $query .= "                     AND R3.TESTDIV        = '{$model->nittei}' ";
        $query .= "                     AND R3.EXAM_TYPE      = '{$model->exam_type}' ";
        $query .= "                     AND R3.TESTSUBCLASSCD = 'B' ";
        $query .= "                 INNER JOIN V_NAME_MST L004 ON L004.YEAR       = R1.ENTEXAMYEAR ";
        $query .= "                                           AND L004.NAMECD1    = 'L004' ";
        $query .= "                                           AND L004.NAMECD2    = R1.TESTDIV ";
        $query .= "                                           AND L004.NAMESPARE1 = '{$model->nittei}' ";
        $query .= "                 LEFT JOIN ( SELECT "; // 推薦の人
        $query .= "                                 B1.EXAMNO ";
        $query .= "                             FROM ";
        $query .= "                                 ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                                 LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D002 ";
        $query .= "                                      ON D002.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "                                     AND D002.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "                                     AND D002.EXAMNO       = B1.EXAMNO ";
        $query .= "                                     AND D002.SEQ          = '002' ";
        $query .= "                                 INNER JOIN V_NAME_MST L004_1 ON L004_1.YEAR       = B1.ENTEXAMYEAR ";
        $query .= "                                                             AND L004_1.NAMECD1    = 'L004' ";
        $query .= "                                                             AND L004_1.NAMECD2    = B1.TESTDIV ";
        $query .= "                                                             AND L004_1.NAMESPARE1 = '{$model->nittei}' ";
        $query .= "                             WHERE ";
        $query .= "                                     B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                                 AND B1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "                                 AND D002.REMARK1 in (SELECT ";
        $query .= "                                                         D002.REMARK1 ";//CKIPのログインＩＤ
        $query .= "                                                     FROM ";
        $query .= "                                                         ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                                                         LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D002 ";
        $query .= "                                                              ON D002.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "                                                             AND D002.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "                                                             AND D002.EXAMNO       = B1.EXAMNO ";
        $query .= "                                                             AND D002.SEQ          = '002' ";
        $query .= "                                                         LEFT JOIN V_NAME_MST L004_2 ";
        $query .= "                                                              ON L004_2.YEAR       = B1.ENTEXAMYEAR ";
        $query .= "                                                             AND L004_2.NAMECD1    = 'L004' ";
        $query .= "                                                             AND L004_2.NAMECD2    = B1.TESTDIV ";
        $query .= "                                                     WHERE ";
        $query .= "                                                             B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                                                         AND B1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "                                                         AND value(L004_2.NAMESPARE1, '0') = '1' ";// 1:推薦
        $query .= "                                                     ) ";
        $query .= "                             ) S1 ON R1.EXAMNO = S1.EXAMNO ";
        $query .= "          WHERE  R1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "            AND  R1.APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "            AND  R1.TOTAL4 IS NOT NULL ";
        $query .= "       ) TT4 ON TT0.RECEPTNO  = TT4.RECEPTNO ";
        $query .= "            AND TT0.EXAM_TYPE = TT4.EXAM_TYPE ";
        $query .= " WHERE TT0.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "   AND TT0.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.EXAM_TYPE    = ENTEXAM_RECEPT_DAT.EXAM_TYPE ";
        $query .= "   AND TT0.RECEPTNO     = ENTEXAM_RECEPT_DAT.RECEPTNO ";
        $query .= "       )"; 
        $query .= " WHERE ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND TESTDIV     IN ({$inTestDiv}) "; 

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        /* 席次(偏差値) */
        $query  = "UPDATE ENTEXAM_RECEPT_DAT ";
        $query .= "   SET (EXAMNO, ";
        $query .= "        JUDGE_DEVIATION_RANK, ";
        $query .= "        LINK_JUDGE_DEVIATION_RANK, ";
        $query .= "        REGISTERCD, UPDATED) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno, ";
        $query .= "       TT4.JUDGE_DEVIATION_RANK, ";
        $query .= "       TT2.LINK_JUDGE_DEVIATION_RANK, ";
        $query .= "       '".STAFFCD."', ";
        $query .= "       SYSDATE() ";
        $query .= "  FROM ENTEXAM_RECEPT_DAT TT0 ";
        $query .= "       LEFT JOIN ";
        $query .= "        (SELECT T1.RECEPTNO ";
        $query .= "               ,T1.EXAM_TYPE ";
        $query .= "               ,RANK() OVER(ORDER BY T1.JUDGE_DEVIATION DESC) AS JUDGE_DEVIATION_RANK ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT T1 ";
        $query .= "          where T1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "            AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "            AND T1.TESTDIV     IN ({$inTestDiv}) ";
        $query .= "            AND T1.JUDGE_DEVIATION IS NOT NULL ";
        $query .= "       ) TT4 ON TT0.RECEPTNO  = TT4.RECEPTNO ";
        $query .= "            AND TT0.EXAM_TYPE = TT4.EXAM_TYPE ";
        $query .= "       LEFT JOIN ";
        $query .= "        (SELECT T1.RECEPTNO ";
        $query .= "               ,T1.EXAM_TYPE ";
        $query .= "               ,RANK() OVER(ORDER BY T1.LINK_JUDGE_DEVIATION DESC) AS LINK_JUDGE_DEVIATION_RANK ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT T1";
        $query .= "          where T1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "            AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "            AND T1.TESTDIV     IN ({$inTestDiv}) ";
        $query .= "            AND T1.LINK_JUDGE_DEVIATION IS NOT NULL ";
        $query .= "       ) TT2 ON TT0.RECEPTNO  = TT2.RECEPTNO ";
        $query .= "            AND TT0.EXAM_TYPE = TT2.EXAM_TYPE ";
        $query .= " WHERE TT0.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "   AND TT0.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.TESTDIV     IN ({$inTestDiv}) ";
        $query .= "   AND TT0.EXAM_TYPE    = ENTEXAM_RECEPT_DAT.EXAM_TYPE ";
        $query .= "   AND TT0.RECEPTNO     = ENTEXAM_RECEPT_DAT.RECEPTNO ";
        $query .= "       )";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND TESTDIV     IN ({$inTestDiv}) "; 

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        //正常
        $db->popExpect();
        Query::dbCheckIn($db);
        return DB_OK;
    } 
}
?>

