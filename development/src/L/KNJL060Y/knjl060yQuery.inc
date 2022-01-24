<?php

require_once('for_php7.php');

class knjl060yquery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year, $namecd2 = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        //中学全ておよび高校推薦入試は表示しない
        if (strlen($namecd2)) {
            $query .= " AND NAMECD2 != '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    function ExecuteQuery($model) {

        //DB接続
        $db = Query::dbCheckOut();

        //対象人数チェック
        $query  = "SELECT COUNT(*) FROM entexam_recept_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->examyear."'";
        $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'";
        //帰国生対応(高校のみ) 1:帰国生除く 2:帰国生のみ
        if ($model->applicantdiv == "2") {
            if ($model->kikoku == "2") {
                $inSql = "IN";
            } else {
                $inSql = "NOT IN";
            }
            $query .= " AND T1.RECEPTNO {$inSql} ( ";
            $query .= "     SELECT ";
            $query .= "         K1.RECEPTNO ";
            $query .= "     FROM ";
            $query .= "         ENTEXAM_RECEPT_DAT K1 ";
            $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT K2 ";
            $query .= "                 ON  K2.ENTEXAMYEAR = K1.ENTEXAMYEAR ";
            $query .= "                 AND K2.APPLICANTDIV = K1.APPLICANTDIV ";
            $query .= "                 AND K2.EXAMNO = K1.EXAMNO ";
            $query .= "                 AND K2.INTERVIEW_ATTEND_FLG = '1' ";
            $query .= "     WHERE ";
            $query .= "             K1.ENTEXAMYEAR  = '{$model->examyear}' ";
            $query .= "         AND K1.APPLICANTDIV = '{$model->applicantdiv}' ";
            $query .= "         AND K1.TESTDIV      = '{$model->testdiv}' ";
            $query .= " ) ";
        }
        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db); 
            return false;
        }
        //得点データチェック
        $query  = "SELECT COUNT(*) FROM entexam_score_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->examyear."'";
        $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'"; 
        //帰国生対応(高校のみ) 1:帰国生除く 2:帰国生のみ
        if ($model->applicantdiv == "2") {
            if ($model->kikoku == "2") {
                $inSql = "IN";
            } else {
                $inSql = "NOT IN";
            }
            $query .= " AND T1.RECEPTNO {$inSql} ( ";
            $query .= "     SELECT ";
            $query .= "         K1.RECEPTNO ";
            $query .= "     FROM ";
            $query .= "         ENTEXAM_RECEPT_DAT K1 ";
            $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT K2 ";
            $query .= "                 ON  K2.ENTEXAMYEAR = K1.ENTEXAMYEAR ";
            $query .= "                 AND K2.APPLICANTDIV = K1.APPLICANTDIV ";
            $query .= "                 AND K2.EXAMNO = K1.EXAMNO ";
            $query .= "                 AND K2.INTERVIEW_ATTEND_FLG = '1' ";
            $query .= "     WHERE ";
            $query .= "             K1.ENTEXAMYEAR  = '{$model->examyear}' ";
            $query .= "         AND K1.APPLICANTDIV = '{$model->applicantdiv}' ";
            $query .= "         AND K1.TESTDIV      = '{$model->testdiv}' ";
            $query .= " ) ";
        }
        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db); 
            return false;
        }

        //中学２科目、高校５科目・・・全科目分受験フラグ用
        $cnt = ($model->applicantdiv == "1") ? 2 : 5;
        $testsub = ($model->applicantdiv == "1") ? "('1','2')" : "('1','2','3','4','5')";
        //帰国生対応(高校のみ) 1:帰国生除く 2:帰国生のみ
        if ($model->applicantdiv == "2" && $model->kikoku == "2") {
            $cnt = 3;
            $testsub = "('1','2','5')";
        }
        //中学適性検査型入試　科目「3:適性」
        if ($model->applicantdiv == "1" && $model->testdiv == "5") {
            $cnt = 1;
            $testsub = "('3')";
        }

        //エラー無視
        $db->expectError();

        /*                  */
        /* 受付データの更新 */
        /*                  */

        /* 受付データにあって得点データに1科目分もレコードがない受験番号の人は、受付データの指定した項目をNULLで更新 */

        /* 全科目分受験フラグ、合計点、平均点 */ 
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, attend_all_flg, total2, avarage2, total4, avarage4, total1, avarage1, total3, avarage3, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        $query .= "      ,TT1.attend_all_flg ";          //全科目受験フラグ
        $query .= "      ,TT24.total2 ";                 //合計(満点500点)
        $query .= "      ,TT24.average2 ";               //平均(満点500点)
        $query .= "      ,TT24.total4 ";                 //合計(満点550点)
        $query .= "      ,TT24.average4 ";               //平均(満点550点)
        $query .= "      ,TT13.total1 ";                 //合計(満点500点)+(加点)
        $query .= "      ,TT13.average1 ";               //平均(満点500点)+(加点)
        $query .= "      ,TT13.total3 ";                 //合計(満点550点)+(加点)
        $query .= "      ,TT13.average3 ";               //平均(満点550点)+(加点)
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
                          /* 全科目分受験フラグ */ 
        $query .= "       LEFT OUTER JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,CASE WHEN COUNT(t1.score) >= {$cnt} THEN '1' ELSE '0' END AS attend_all_flg";
        $query .= "           FROM entexam_score_dat t1";
        $query .= "          where t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.testsubclasscd IN {$testsub}"; //主要5科目
        $query .= "         GROUP BY t1.receptno, t1.exam_type";
        $query .= "       ) TT1 ON TT0.receptno  = TT1.receptno ";
        $query .= "            AND TT0.exam_type = TT1.exam_type ";
                          /* 加点なし */ 
        $query .= "       LEFT OUTER JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,SUM(t1.score)                    AS total2";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score)),1)  AS average2";
        $query .= "               ,SUM(t1.score2)                   AS total4";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score2)),1) AS average4";
        $query .= "           FROM ";
        $query .= "            (SELECT t1.receptno";
        $query .= "                   ,t1.exam_type";
        $query .= "                   ,t1.score";
        $query .= "                   ,t1.score2";
        $query .= "               FROM entexam_score_dat t1";
        $query .= "              where t1.entexamyear  = '".$model->examyear."' ";
        $query .= "                AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "                AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "                AND t1.testsubclasscd NOT IN ('9')"; //加点を除く
        //高校一般の場合、合計には内申点（評定平均）も加算する
        if ($model->applicantdiv == "2" && $model->testdiv == "3") {
            $query .= "         UNION ALL";
            $query .= "         SELECT r1.receptno";
            $query .= "               ,r1.exam_type";
            $query .= "               ,smallint(c1.average_all) as score";
            $query .= "               ,smallint(c1.average_all) as score2";
            $query .= "           FROM entexam_recept_dat r1";
            $query .= "                inner join entexam_applicantconfrpt_dat c1";
            $query .= "                    on  c1.entexamyear  = r1.entexamyear";
            $query .= "                    and c1.examno       = r1.examno";
            $query .= "          where r1.entexamyear  = '".$model->examyear."' ";
            $query .= "            AND r1.applicantdiv = '".$model->applicantdiv."'";
            $query .= "            AND r1.testdiv      = '".$model->testdiv."'";
        }
        $query .= "            ) t1 ";
        $query .= "         GROUP BY t1.receptno, t1.exam_type";
        $query .= "       ) TT24 ON TT0.receptno  = TT24.receptno ";
        $query .= "             AND TT0.exam_type = TT24.exam_type ";
                          /* 加点あり */ 
        $query .= "       LEFT OUTER JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,SUM(t1.score)                    AS total1";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score)),1)  AS average1";
        $query .= "               ,SUM(t1.score2)                   AS total3";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score2)),1) AS average3";
        $query .= "           FROM entexam_score_dat t1";
        $query .= "          where t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "         GROUP BY t1.receptno, t1.exam_type";
        $query .= "       ) TT13 ON TT0.receptno  = TT13.receptno ";
        $query .= "             AND TT0.exam_type = TT13.exam_type ";
        $query .= " WHERE TT0.entexamyear  = '".$model->examyear."' ";
        $query .= "   AND TT0.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.testdiv      = '".$model->testdiv."' ";
        $query .= "   AND TT0.exam_type    = entexam_recept_dat.exam_type ";
        $query .= "   AND TT0.receptno     = entexam_recept_dat.receptno ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'"; 
        //帰国生対応(高校のみ) 1:帰国生除く 2:帰国生のみ
        if ($model->applicantdiv == "2") {
            if ($model->kikoku == "2") {
                $inSql = "IN";
            } else {
                $inSql = "NOT IN";
            }
            $query .= " AND ENTEXAM_RECEPT_DAT.RECEPTNO {$inSql} ( ";
            $query .= "     SELECT ";
            $query .= "         K1.RECEPTNO ";
            $query .= "     FROM ";
            $query .= "         ENTEXAM_RECEPT_DAT K1 ";
            $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT K2 ";
            $query .= "                 ON  K2.ENTEXAMYEAR = K1.ENTEXAMYEAR ";
            $query .= "                 AND K2.APPLICANTDIV = K1.APPLICANTDIV ";
            $query .= "                 AND K2.EXAMNO = K1.EXAMNO ";
            $query .= "                 AND K2.INTERVIEW_ATTEND_FLG = '1' ";
            $query .= "     WHERE ";
            $query .= "             K1.ENTEXAMYEAR  = '{$model->examyear}' ";
            $query .= "         AND K1.APPLICANTDIV = '{$model->applicantdiv}' ";
            $query .= "         AND K1.TESTDIV      = '{$model->testdiv}' ";
            $query .= " ) ";
        }

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
        $query .= "   SET (examno, total_rank1, total_rank2, total_rank3, total_rank4, div_rank1, div_rank2, div_rank3, div_rank4, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        //入試区分ごと
        $query .= "      ,TT1.total_rank1 ";    //席次(満点500点)+(加点)
        $query .= "      ,TT2.total_rank2 ";    //席次(満点500点)
        $query .= "      ,TT3.total_rank3 ";    //席次(満点550点)+(加点)
        $query .= "      ,TT4.total_rank4 ";    //席次(満点550点)
        //入試区分＋志望区分（学科・コース）ごと
        $query .= "      ,TT1.inout_rank1 ";      //席次(満点500点)+(加点)
        $query .= "      ,TT2.inout_rank2 ";      //席次(満点500点)
        $query .= "      ,TT3.inout_rank3 ";      //席次(満点550点)+(加点)
        $query .= "      ,TT4.inout_rank4 ";      //席次(満点550点)
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        for ($i = 1; $i <= 4; $i++) {
            //変数
            $total      = "total" .$i;
            $table      = "TT" .$i;
            $total_rank = "total_rank" .$i;
            $div_rank   = "div_rank" .$i;
            $inout_rank = "inout_rank" .$i;
            //席次SQL
            $query .= "   LEFT OUTER JOIN ";
            $query .= "    (SELECT t1.receptno";
            $query .= "           ,t1.exam_type";
            $query .= "           ,RANK() OVER(PARTITION BY t2.testdiv               ORDER BY t1.{$total} DESC) AS {$total_rank}";
            $query .= "           ,RANK() OVER(PARTITION BY t2.testdiv, t2.desirediv ORDER BY t1.{$total} DESC) AS {$div_rank}";
            $query .= "           ,RANK() OVER(PARTITION BY CASE WHEN t1.examno LIKE '6%' THEN '6' ELSE '1' END ORDER BY t1.{$total} DESC) AS {$inout_rank}";
            $query .= "       FROM entexam_recept_dat t1";
            $query .= "            inner join entexam_applicantdesire_dat t2 ";
            $query .= "                 ON  t2.entexamyear  = t1.entexamyear ";
            $query .= "                 AND t2.applicantdiv = t1.applicantdiv";
            $query .= "                 AND t2.testdiv      = t1.testdiv";
            $query .= "                 AND t2.examno       = t1.examno";
            $query .= "      where t1.entexamyear  = '".$model->examyear."' ";
            $query .= "        AND t1.applicantdiv = '".$model->applicantdiv."'";
            $query .= "        AND t1.testdiv      = '".$model->testdiv."'";
            $query .= "        AND t1.attend_all_flg = '1'"; //1:全科目受験
            $query .= "        AND t1.{$total} IS NOT NULL";
            //帰国生対応(高校のみ) 1:帰国生除く 2:帰国生のみ
            if ($model->applicantdiv == "2") {
                if ($model->kikoku == "2") {
                    $inSql = "IN";
                } else {
                    $inSql = "NOT IN";
                }
                $query .= " AND T1.RECEPTNO {$inSql} ( ";
                $query .= "     SELECT ";
                $query .= "         K1.RECEPTNO ";
                $query .= "     FROM ";
                $query .= "         ENTEXAM_RECEPT_DAT K1 ";
                $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT K2 ";
                $query .= "                 ON  K2.ENTEXAMYEAR = K1.ENTEXAMYEAR ";
                $query .= "                 AND K2.APPLICANTDIV = K1.APPLICANTDIV ";
                $query .= "                 AND K2.EXAMNO = K1.EXAMNO ";
                $query .= "                 AND K2.INTERVIEW_ATTEND_FLG = '1' ";
                $query .= "     WHERE ";
                $query .= "             K1.ENTEXAMYEAR  = '{$model->examyear}' ";
                $query .= "         AND K1.APPLICANTDIV = '{$model->applicantdiv}' ";
                $query .= "         AND K1.TESTDIV      = '{$model->testdiv}' ";
                $query .= " ) ";
            }
            $query .= "   ) {$table} ON TT0.receptno  = {$table}.receptno ";
            $query .= "             AND TT0.exam_type = {$table}.exam_type ";
        }
        $query .= " WHERE TT0.entexamyear  = '".$model->examyear."' ";
        $query .= "   AND TT0.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.testdiv      = '".$model->testdiv."' ";
        $query .= "   AND TT0.exam_type    = entexam_recept_dat.exam_type ";
        $query .= "   AND TT0.receptno     = entexam_recept_dat.receptno ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'"; 
        //帰国生対応(高校のみ) 1:帰国生除く 2:帰国生のみ
        if ($model->applicantdiv == "2") {
            if ($model->kikoku == "2") {
                $inSql = "IN";
            } else {
                $inSql = "NOT IN";
            }
            $query .= " AND ENTEXAM_RECEPT_DAT.RECEPTNO {$inSql} ( ";
            $query .= "     SELECT ";
            $query .= "         K1.RECEPTNO ";
            $query .= "     FROM ";
            $query .= "         ENTEXAM_RECEPT_DAT K1 ";
            $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT K2 ";
            $query .= "                 ON  K2.ENTEXAMYEAR = K1.ENTEXAMYEAR ";
            $query .= "                 AND K2.APPLICANTDIV = K1.APPLICANTDIV ";
            $query .= "                 AND K2.EXAMNO = K1.EXAMNO ";
            $query .= "                 AND K2.INTERVIEW_ATTEND_FLG = '1' ";
            $query .= "     WHERE ";
            $query .= "             K1.ENTEXAMYEAR  = '{$model->examyear}' ";
            $query .= "         AND K1.APPLICANTDIV = '{$model->applicantdiv}' ";
            $query .= "         AND K1.TESTDIV      = '{$model->testdiv}' ";
            $query .= " ) ";
        }

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

