<?php

require_once('for_php7.php');

class knjl130eQuery extends Query {
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
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象平均点チェック
    function checkAvgCnt($model) {
        $query  = " WITH TMP AS ( ";
        $query .= " SELECT ";
        $query .= "     TESTDIV, COUNT(AVARAGE_TOTAL) AS COUNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_JUDGE_AVARAGE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TESTSUBCLASSCD = 'A' "; // 推薦者を含まない
        $query .= " GROUP BY ";
        $query .= "     TESTDIV ";
        $query .= " ) , MAIN (COUNT_A, NAME_A, COUNT_B, NAME_B) AS ( ";
        $query .= " VALUES ( ";
        $query .= "    (SELECT COUNT FROM TMP WHERE TESTDIV = '2') ";
        $query .= "  , 'Ａ日程' ";
        $query .= "  , (SELECT COUNT FROM TMP WHERE TESTDIV = '3') ";
        $query .= "  , 'Ｂ日程' ";
        $query .= " ) ";
        $query .= " ) SELECT * FROM MAIN ";

        return $query;
    }

    // 一般推薦の合計点を取得
    function getCalcQuery($model, $nittei) {
        $query  = " WITH BASE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.* ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "         LEFT JOIN V_NAME_MST L004 ";
        $query .= "              ON L004.YEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND L004.NAMECD1 = 'L004' ";
        $query .= "             AND L004.NAMECD2 = T1.TESTDIV ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "         T1.APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "         L004.NAMESPARE1    = '".$nittei."' AND ";
        $query .= "         VALUE(T1.JUDGEMENT,'') != '3' "; //欠席者は除く
        $query .= " ) ";
        $query .= "     SELECT ";
        $query .= "         T1.EXAMNO, ";
        $query .= "         SUM(L1.SCORE) AS TOTAL ";
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

        return $query;
    }

    //算出
    function ExecuteQuery($model) {
        //DB接続
        $db = Query::dbCheckOut();

        //算出データ取得
        $data_flg = false;

        $nitteiList = array("2" // A日程
                          , "3" // B日程
                              );

        foreach ($nitteiList as $nittei) {
            // 一般の合計点をコピー
            $query  = " SELECT T1.EXAMNO, T1.RECEPTNO, T1.JUDGE_DEVIATION FROM ";
            $query .= "     ENTEXAM_RECEPT_DAT T1 ";
            $query .= "     INNER JOIN V_NAME_MST L004 ON L004.YEAR = ENTEXAMYEAR ";
            $query .= "         AND L004.NAMECD1 = 'L004' ";
            $query .= "         AND L004.NAMECD2 = T1.TESTDIV ";
            $query .= " WHERE ";
            $query .= "     T1.ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
            $query .= "     T1.APPLICANTDIV    = '".$model->applicantdiv."' AND ";
            $query .= "     L004.NAMESPARE1    = '".$nittei."' ";
            $result = $db->query($query);

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

                $result1 = knjl130eQuery::updateApplicantbaseDetailDat($db, $model, $row["EXAMNO"], $row["JUDGE_DEVIATION"]);

                //エラー時後処理
                if (DB::isError($result1)) {
                   $db->popExpect();
                   Query::dbCheckIn($db);
                   return $result1->getCode();
                }
                $data_flg = true;
            }
            $result->free();

            if ($nittei == '3') {

                // 推薦入試合格者は学力予備検査を実施する。偏差値は、得点と一般B日程の平均、標準偏差から算出する。

                // B日程の平均、標準偏差を取得
                $query  = " SELECT CALC_STDDEV, CALC_AVG ";
                $query .= " FROM ";
                $query .= "     ENTEXAM_JUDGE_AVARAGE_DAT ";
                $query .= " WHERE ";
                $query .= "         ENTEXAMYEAR  = '".$model->ObjYear."' ";
                $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
                $query .= "     AND TESTDIV      = '".$nittei."' ";
                $query .= "     AND TESTSUBCLASSCD = 'A' "; // 推薦者を含まない

                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $stddev = $row["CALC_STDDEV"];
                $avg = $row["CALC_AVG"];

                if ($stddev > 0) {
                    // 一般推薦の合計点を取得
                    $query = knjl130eQuery::getCalcQuery($model, "1"); // 1:推薦
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $total = $row["TOTAL"];

                        if (strlen($total) > 0) {
                            $deviation = round(10 * ($total - $avg) / $stddev + 50, 1); // B日程の平均、標準偏差で偏差値を算出
                        } else {
                            $deviation = "";
                        }

                        $result1 = knjl130eQuery::updateApplicantbaseDetailDat($db, $model, $row["EXAMNO"], $deviation);
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
            }
        }


        //対象データなし
        if (!$data_flg) {
           return "NO_DATA";
        }

        //正常
        $db->popExpect();
        Query::dbCheckIn($db);
        return DB_OK;
    } 

    function updateApplicantbaseDetailDat($db, $model, $examno, $deviation) {
        $query  = " SELECT 1 AS FLG FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     EXAMNO          = '".$examno."' AND ";
        $query .= "     SEQ             = '036' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $data = array();
        $data["REMARK1"][TEXT]              = $deviation;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        if ($row["FLG"] == 1) {
            //更新
            $where  = " WHERE ";
            $where .= "      ENTEXAMYEAR = '{$model->ObjYear}' ";
            $where .= "  AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $where .= "  AND EXAMNO = '{$examno}' ";
            $where .= "  AND SEQ = '036' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        } else {
            //追加
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
            $data["EXAMNO"][TEXT]               = $examno;
            $data["SEQ"][TEXT]                  = "036";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        }

        $result1 = $db->query($query);
        return $result1;
    }
}
?>

