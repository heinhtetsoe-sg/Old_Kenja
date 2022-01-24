<?php

require_once('for_php7.php');

class knjl060nQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year, $namecd2 = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        //中学は表示しない
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
        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db); 
            return false;
        }
        //得点データチェック
        $query  = "SELECT COUNT(*) FROM entexam_score_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->examyear."'";
        $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'"; 
        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db); 
            return false;
        }

        //受験科目
        $testsubArray = array();
        $query = knjl060nQuery::getNameMst("L009", $model->examyear, "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testsubArray[] = $row["VALUE"];
        }
        $result->free();

        //エラー無視
        $db->expectError();

        /*                  */
        /* 受付データの更新 */
        /*                  */

        /* 受付データにあって得点データに1科目分もレコードがない受験番号の人は、受付データの指定した項目をNULLで更新 */

        /***
        ※ 京都西山の受験科目
        -- 「1:特進S、2:特進A」は、5科目(国、数、英、社、理)
        -- 「3:こころ教養、4:体育クラブ、5:こども夢」は、3科目(国、数、英)
        ***/

        /* 全科目分受験フラグ、合計点、平均点 */ 
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, attend_all_flg, total4, avarage4, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        //全科目分受験フラグ
        $query .= "      ,TT4.attend_all_flg AS attend_all_flg ";
        //合計点
        $query .= "      ,TT4.total AS total4 ";
        //平均点
        $query .= "      ,TT4.average AS avarage4 ";
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
                          //全科目 全科目分受験フラグ、合計点、平均点
                          //科目受験フラグ(attend_flg) 1:受験 0:欠席(*) NULL:得点未入力
        $query .= "       LEFT JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,CASE WHEN min(t1.attend_flg) = '0' THEN '0' ELSE '1' END AS attend_all_flg ";
        $query .= "               ,SUM(t1.score)                    AS total ";
        $query .= "               ,ROUND(AVG(DECIMAL(t1.score)),1)  AS average ";
        $query .= "           FROM entexam_score_dat t1";
        $query .= "          WHERE t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.testsubclasscd IN ('" . implode("','", $testsubArray) . "') ";
        $query .= "         GROUP BY t1.receptno ";
        $query .= "       ) TT4 ON TT0.receptno  = TT4.receptno ";
        $query .= " WHERE TT0.entexamyear  = '".$model->examyear."' ";
        $query .= "   AND TT0.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.testdiv      = '".$model->testdiv."' ";
        $query .= "   AND TT0.exam_type    = entexam_recept_dat.exam_type ";
        $query .= "   AND TT0.receptno     = entexam_recept_dat.receptno ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'"; 

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
        /* 母集団：専併区分、志望区分毎 */
        //入試試験の科目が1つでも未受験があれば、順位対象外(NULLで更新)・・・これは保留
        //合計があれば、順位対象

        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, div_rank4, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT TT0.examno ";
        $query .= "      ,TT4.div_rank4 ";
        $query .= "      ,'".STAFFCD."' ";
        $query .= "      ,SYSDATE() ";
        $query .= "  FROM entexam_recept_dat TT0 ";
        $query .= "       LEFT JOIN ";
        $query .= "        (SELECT t1.receptno";
        $query .= "               ,t1.exam_type";
        $query .= "               ,RANK() OVER(PARTITION BY t2.shdiv, t3.remark10 ORDER BY t1.total4 DESC) AS div_rank4 ";
        $query .= "           FROM entexam_recept_dat t1";
        $query .= "                LEFT JOIN entexam_applicantbase_dat t2 ";
        $query .= "                     ON  t2.entexamyear  = t1.entexamyear ";
        $query .= "                     AND t2.applicantdiv = t1.applicantdiv";
        $query .= "                     AND t2.examno       = t1.examno";
        $query .= "                LEFT JOIN entexam_applicantbase_detail_dat t3 ";
        $query .= "                     ON  t3.entexamyear  = t1.entexamyear ";
        $query .= "                     AND t3.examno       = t1.examno";
        $query .= "                     AND t3.seq          = '001' ";
        $query .= "          where t1.entexamyear  = '".$model->examyear."' ";
        $query .= "            AND t1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "            AND t1.testdiv      = '".$model->testdiv."'";
        $query .= "            AND t1.total4 IS NOT NULL ";
        $query .= "       ) TT4 ON TT0.receptno  = TT4.receptno ";
        $query .= "            AND TT0.exam_type = TT4.exam_type ";
        $query .= " WHERE TT0.entexamyear  = '".$model->examyear."' ";
        $query .= "   AND TT0.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND TT0.testdiv      = '".$model->testdiv."' ";
        $query .= "   AND TT0.exam_type    = entexam_recept_dat.exam_type ";
        $query .= "   AND TT0.receptno     = entexam_recept_dat.receptno ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'"; 

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

