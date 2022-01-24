<?php

require_once('for_php7.php');

class knjl260yQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    function ExecuteQuery($model) {

        //DB接続
        $db = Query::dbCheckOut();

        //対象人数チェック
        $query  = "SELECT COUNT(*) FROM entexam_recept_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '{$model->examyear}' ";
        $query .= "   AND T1.applicantdiv = '{$model->applicantdiv}' ";
        $query .= "   AND T1.testdiv      = '{$model->testdiv}' ";
        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db);
            return false;
        }
        //得点データチェック
        $query  = "SELECT COUNT(*) FROM entexam_score_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '{$model->examyear}' ";
        $query .= "   AND T1.applicantdiv = '{$model->applicantdiv}' ";
        $query .= "   AND T1.testdiv      = '{$model->testdiv}' ";
        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db);
            return false;
        }

        //小学校４科目・・・全科目分受験フラグ用
        $cnt = 4;
        $testsub = "('1','2','3','4')";

        //エラー無視
        $db->expectError();

        /*                  */
        /* 受付データの更新 */
        /*                  */

        /* 受付データにあって得点データに1科目分もレコードがない受験番号の人は、受付データの指定した項目をNULLで更新 */

        /* 全科目分受験フラグ、合計点、平均点 */ 
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, attend_all_flg, total3, avarage3, total4, avarage4, total1, avarage1, total2, avarage2, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        $query .= "      ,TT1.attend_all_flg ";          //全科目受験フラグ
                          /* 行動観察評価を加算あり（通常システムではこちらを使用する） */ 
        $query .= "      ,TT34.total3 ";                 //重みなし
        $query .= "      ,TT34.average3 ";
        $query .= "      ,TT34.total4 ";                 //重みあり
        $query .= "      ,TT34.average4 ";
                          /* 行動観察評価を加算なし（通常システムでは使用しない） */ 
        $query .= "      ,TT12.total1 ";                 //重みなし
        $query .= "      ,TT12.average1 ";
        $query .= "      ,TT12.total2 ";                 //重みあり
        $query .= "      ,TT12.average2 ";
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
                          /* 全科目分受験フラグ */ 
        $query .= "       LEFT OUTER JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,CASE WHEN COUNT(t1.score) >= {$cnt} THEN '1' ELSE '0' END AS attend_all_flg";
        $query .= "           FROM entexam_score_dat t1";
        $query .= "          where t1.entexamyear  = '{$model->examyear}' ";
        $query .= "            AND t1.applicantdiv = '{$model->applicantdiv}'";
        $query .= "            AND t1.testdiv      = '{$model->testdiv}'";
        $query .= "            AND t1.testsubclasscd IN {$testsub}";
        $query .= "         GROUP BY t1.receptno, t1.exam_type";
        $query .= "       ) TT1 ON TT0.receptno  = TT1.receptno ";
        $query .= "            AND TT0.exam_type = TT1.exam_type ";
                          /* 行動観察評価を加算あり（通常システムではこちらを使用する） */ 
        $query .= "       LEFT OUTER JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,SUM(t1.score)                    AS total3"; //重みなし
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score)),1)  AS average3";
        $query .= "               ,SUM(t1.score2)                   AS total4"; //重みあり
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score2)),1) AS average4";
        $query .= "           FROM ";
        $query .= "            (SELECT t1.receptno";
        $query .= "                   ,t1.exam_type";
        $query .= "                   ,t1.score";
        $query .= "                   ,t1.score2";
        $query .= "               FROM entexam_score_dat t1";
        $query .= "              where t1.entexamyear  = '{$model->examyear}' ";
        $query .= "                AND t1.applicantdiv = '{$model->applicantdiv}'";
        $query .= "                AND t1.testdiv      = '{$model->testdiv}'";
        //行動観察評価を加算
        $query .= "             UNION ALL";
        $query .= "             SELECT r1.receptno";
        $query .= "                   ,r1.exam_type";
        $query .= "                   ,smallint(n1.namespare1) as score";
        $query .= "                   ,smallint(n1.namespare1) as score2";
        $query .= "               FROM entexam_recept_dat r1";
        $query .= "                    inner join entexam_interview_dat c1";
        $query .= "                        on  c1.entexamyear   = r1.entexamyear";
        $query .= "                        and c1.applicantdiv  = r1.applicantdiv";
        $query .= "                        and c1.testdiv       = r1.testdiv";
        $query .= "                        and c1.examno        = r1.examno";
        $query .= "                    left join name_mst n1";
        $query .= "                        on  n1.namecd1       = 'L030'";
        $query .= "                        and n1.namecd2       = c1.interview_value2";
        $query .= "              where r1.entexamyear  = '{$model->examyear}' ";
        $query .= "                AND r1.applicantdiv = '{$model->applicantdiv}'";
        $query .= "                AND r1.testdiv      = '{$model->testdiv}'";
        $query .= "            ) t1 ";
        $query .= "         GROUP BY t1.receptno, t1.exam_type";
        $query .= "       ) TT34 ON TT0.receptno  = TT34.receptno ";
        $query .= "             AND TT0.exam_type = TT34.exam_type ";
                          /* 行動観察評価を加算なし（通常システムでは使用しない） */ 
        $query .= "       LEFT OUTER JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,SUM(t1.score)                    AS total1"; //重みなし
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score)),1)  AS average1";
        $query .= "               ,SUM(t1.score2)                   AS total2"; //重みあり
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score2)),1) AS average2";
        $query .= "           FROM entexam_score_dat t1";
        $query .= "          where t1.entexamyear  = '{$model->examyear}' ";
        $query .= "            AND t1.applicantdiv = '{$model->applicantdiv}'";
        $query .= "            AND t1.testdiv      = '{$model->testdiv}'";
        $query .= "         GROUP BY t1.receptno, t1.exam_type";
        $query .= "       ) TT12 ON TT0.receptno  = TT12.receptno ";
        $query .= "             AND TT0.exam_type = TT12.exam_type ";
        $query .= " WHERE TT0.entexamyear  = '{$model->examyear}' ";
        $query .= "   AND TT0.applicantdiv = '{$model->applicantdiv}' ";
        $query .= "   AND TT0.testdiv      = '{$model->testdiv}' ";
        $query .= "   AND TT0.exam_type    = entexam_recept_dat.exam_type ";
        $query .= "   AND TT0.receptno     = entexam_recept_dat.receptno ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '{$model->examyear}'";
        $query .= "   AND applicantdiv = '{$model->applicantdiv}'";
        $query .= "   AND testdiv      = '{$model->testdiv}'"; 

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
//echo "合計<BR>";
//echo $query;
           $db->popExpect();
           Query::dbCheckIn($db);
           return $result->getCode();
        }


        /* 席次 */
        //入試試験の科目が1つでも未受験があれば、順位対象外(NULLで更新)
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, total_rank1, total_rank2, total_rank3, total_rank4, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        //入試区分ごと
        $query .= "      ,TT1.total_rank1 ";    //席次(満点500点)+(加点)
        $query .= "      ,TT2.total_rank2 ";    //席次(満点500点)
        $query .= "      ,TT3.total_rank3 ";    //席次(満点550点)+(加点)
        $query .= "      ,TT4.total_rank4 ";    //席次(満点550点)
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        for ($i = 1; $i <= 4; $i++) {
            //変数
            $total      = "total" .$i;
            $table      = "TT" .$i;
            $total_rank = "total_rank" .$i;
            //席次SQL
            $query .= "   LEFT OUTER JOIN ";
            $query .= "    (SELECT t1.receptno";
            $query .= "           ,t1.exam_type";
            $query .= "           ,RANK() OVER(PARTITION BY t1.testdiv ORDER BY t1.{$total} DESC) AS {$total_rank}";
            $query .= "       FROM entexam_recept_dat t1";
            $query .= "      where t1.entexamyear  = '{$model->examyear}' ";
            $query .= "        AND t1.applicantdiv = '{$model->applicantdiv}'";
            $query .= "        AND t1.testdiv      = '{$model->testdiv}'";
            //保留：仕様未確定のため、合計があれば順位をつける仕様とする。
            //$query .= "        AND t1.attend_all_flg = '1'"; //1:全科目受験
            $query .= "        AND t1.{$total} IS NOT NULL";
            $query .= "   ) {$table} ON TT0.receptno  = {$table}.receptno ";
            $query .= "             AND TT0.exam_type = {$table}.exam_type ";
        }
        $query .= " WHERE TT0.entexamyear  = '{$model->examyear}' ";
        $query .= "   AND TT0.applicantdiv = '{$model->applicantdiv}' ";
        $query .= "   AND TT0.testdiv      = '{$model->testdiv}' ";
        $query .= "   AND TT0.exam_type    = entexam_recept_dat.exam_type ";
        $query .= "   AND TT0.receptno     = entexam_recept_dat.receptno ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '{$model->examyear}'";
        $query .= "   AND applicantdiv = '{$model->applicantdiv}'";
        $query .= "   AND testdiv      = '{$model->testdiv}'"; 

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
//echo "席次<BR>";
//echo $query;
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        $db->popExpect();
        Query::dbCheckIn($db);
        return DB_OK;
    } 
}
?>
