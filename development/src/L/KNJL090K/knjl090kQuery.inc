<?php

require_once('for_php7.php');
class knjl090kquery extends Query {

    //名称
    function GetName($namecd1, $year)
    {
        $query = "SELECT namecd2, name1, name2, namespare1 FROM v_name_mst
                   WHERE year    = '".$year."'
                     AND namecd1 = '".$namecd1."'";
        return $query;
    }

    /* 実行 */
    function ExecuteQuery(&$model)
    {
        $db = Query::dbCheckOut();

        //エラー無視
        $db->expectError();

        //更新対象人数チェック
        $query  = "SELECT COUNT(*) FROM entexam_applicantbase_dat ";
        $query .= " WHERE entexamyear = '".$model->examyear."'";
        $query .= "   AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "   AND testdiv     = '".$model->testdiv."'";

        if ($db->getOne($query) == "0") {
            $db->popExpect();
            Query::dbCheckIn($db); 
            $model->setMessage("MSG303","対象者がいません。");
            return false;
        }

        if ($model->procedurecd == "1") {
            $result = knjl090kquery::UpdateBaseQuery($db, $model);    //基礎データ

        } else {
            $result = knjl090kquery::UpdateScoreQuery($db, $model);   //得点データ
        }

        if ($result != DB_OK) {
            $this->setWarning("MSG203", "\\nエラーが発生しました。しばらくして処理を再開してください。\\n( それでも解決しない場合は管理者へ連絡して下さい。)");
            return false;
        }

        $db->popExpect();
        Query::dbCheckIn($db);
        return true;
    }

    /*                  */
    /* 基礎データの更新 */
    /*                  */
    /* 基礎データにあって得点データに1科目分もレコードがない受験番号の人は、基礎データの指定項目をNULLで更新 */
    /* 配点Ｂ試験合計点は満点マスタのアラカルトが１の科目の中の最高点と、それ以外の科目の得点を合計する */

    function UpdateBaseQuery(&$db, $model) 
    {
        $db->autoCommit(false);
        set_time_limit(0);

        //合計、平均、席次の項目(配点ＡＢ)クリア    2005.08.17 minei
        $query  = "UPDATE entexam_applicantbase_dat ";
        $query .= "   SET attend_all_flg = CAST(NULL AS VARCHAR(1))";   //全科目受験フラグ
        $query .= "      ,a_total        = CAST(NULL AS SMALLINT)";     //配点Ａ試験合計点
        $query .= "      ,a_average      = CAST(NULL AS DECIMAL(4,1))"; //配点Ａ試験平均点
        $query .= "      ,a_total_rank   = CAST(NULL AS SMALLINT)";     //配点Ａ全体席次
        $query .= "      ,a_div_rank     = CAST(NULL AS SMALLINT)";     //配点Ａ志願区分別席次
        $query .= "      ,b_total        = CAST(NULL AS SMALLINT)";     //配点Ｂ試験合計点
        $query .= "      ,b_average      = CAST(NULL AS DECIMAL(4,1))"; //配点Ｂ試験平均点
        $query .= "      ,b_total_rank   = CAST(NULL AS SMALLINT)";     //配点Ｂ全体席次
        $query .= "      ,b_div_rank     = CAST(NULL AS SMALLINT)";     //配点Ｂ志願区分別席次
        $query .= "      ,registercd     = '".STAFFCD."' ";
        $query .= "      ,updated        = SYSDATE() ";
        $query .= " WHERE entexamyear = '".$model->examyear."' ";
        $query .= "   AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "   AND testdiv     = '".$model->testdiv."' ";
        $result2 = $db->query($query);
        if (DB::isError($result2)) {
            return $result2->getCode();
        }

        //合計、平均等を算出 ===============================================================================================================
        $query  = "SELECT T1.examno ";
        //受験科目数と受験すべき科目数が同じなら全科目受験フラグを１に更新 2005.08.09
        $query .= "      ,CASE WHEN SUM(SMALLINT(T4.attend_flg)) = COUNT(T2.testsubclasscd) THEN '1' ELSE '0' END AS attend_all_flg "; 

        //配点Ａ試験合計点 2005.08.12 minei 集計フラグが「1:集計する」場合のみ対象
        $query .= "      ,SUM(CASE T2.a_total_flg WHEN '1' THEN T4.a_score ELSE 0 END)          AS a_total ";

        //配点Ａ試験平均点 2005.08.12 minei 集計フラグが「1:集計する」場合のみ対象
        $query .= "      ,ROUND(DECIMAL(SUM(CASE T2.a_total_flg WHEN '1' THEN T4.a_score ELSE 0 END)) ";
        $query .= "                   /(SUM(CASE T2.a_total_flg WHEN '1' THEN 1 ELSE 0 END)),1) AS a_average";

        #2005/08/12 arakaki edit strat
        //配点Ｂ試験合計点
        $query .= "      ,SUM(CASE WHEN T2.b_total_flg = '1' AND T2.adoptiondiv = '0' THEN T4.b_score ELSE 0 END) ";
        $query .= "      +MAX(CASE WHEN T2.b_total_flg = '1' AND T2.adoptiondiv = '1' THEN T4.b_score ELSE 0 END)           AS b_total ";

        //配点Ｂ試験平均点
        $query .= "      ,ROUND(DECIMAL(SUM(CASE WHEN T2.b_total_flg = '1' AND T2.adoptiondiv = '0' THEN T4.b_score ELSE 0 END) ";
        $query .= "                    +MAX(CASE WHEN T2.b_total_flg = '1' AND T2.adoptiondiv = '1' THEN T4.b_score ELSE 0 END)) ";
        $query .= "                   /(SUM(CASE WHEN T2.b_total_flg = '1' AND T2.adoptiondiv = '0' THEN 1 ELSE 0 END) ";
        $query .= "                    +MAX(CASE WHEN T2.b_total_flg = '1' AND T2.adoptiondiv = '1' THEN 1 ELSE 0 END)),1)  AS b_average";
        #2005/08/12 arakaki edit end

        $query .= "  FROM entexam_applicantbase_dat T1";
        $query .= "      ,entexam_perfect_mst T2";
        $query .= "      ,entexam_wishdiv_mst T3";
        $query .= "      ,entexam_score_dat   T4";
        $query .= " WHERE T1.entexamyear    = T2.entexamyear";
        $query .= "   AND T1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "   AND T1.entexamyear    = T3.entexamyear";
        $query .= "   AND T1.entexamyear    = T4.entexamyear";
        $query .= "   AND T1.testdiv        = T2.testdiv";
        $query .= "   AND T1.testdiv        = T3.testdiv";
        $query .= "   AND T1.testdiv        = T4.testdiv";
        $query .= "   AND T1.examno         = T4.examno";
        $query .= "   AND T1.desirediv      = T3.desirediv";
        $query .= "   AND T2.coursecd       = T3.coursecd";
        $query .= "   AND T2.majorcd        = T3.majorcd";
        $query .= "   AND T2.examcoursecd   = T3.examcoursecd";
        $query .= "   AND T2.testsubclasscd = T4.testsubclasscd";
        $query .= "   AND T1.entexamyear    = '".$model->examyear."' ";
        $query .= "   AND T1.testdiv        = '".$model->testdiv."' ";  //前期、後期
        $query .= "   AND T3.wishno         = '1'";
        $query .= "   GROUP BY T1.examno";
        #echo $query;
        $result = $db->query($query); 
        if (DB::isError($result)) {
           return $result->getCode();
        }

        //合計、平均等を更新
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $query  = "UPDATE entexam_applicantbase_dat ";
            $query .= "   SET attend_all_flg = '".$row["ATTEND_ALL_FLG"]."'";
            $query .= "      ,a_total        = ".($row["A_TOTAL"]       != "" ? $row["A_TOTAL"]   : "NULL");
            $query .= "      ,a_average      = ".($row["A_AVERAGE"]     != "" ? $row["A_AVERAGE"] : "NULL");
            $query .= "      ,b_total        = ".($row["B_TOTAL"]       != "" ? $row["B_TOTAL"]   : "NULL");
            $query .= "      ,b_average      = ".($row["B_AVERAGE"]     != "" ? $row["B_AVERAGE"] : "NULL");
            $query .= "      ,registercd     = '".STAFFCD."' ";
            $query .= "      ,updated        = SYSDATE() ";
            $query .= " WHERE entexamyear = '".$model->examyear."' ";
            $query .= "   AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
            $query .= "   AND testdiv     = '".$model->testdiv."' ";
            $query .= "   AND examno      = '".$row["EXAMNO"]."'";
            $result2 = $db->query($query);
            if (DB::isError($result2)) {
                return $result2->getCode();
            }
        }

        //2005.08.09 minei 未受験者の抽出 ===============================================================================================================
        //8:未受験者の合否区分クリア    2005/08/12 arakaki
        $query  = "UPDATE entexam_applicantbase_dat ";
        $query .= "   SET judgement   = CAST(NULL AS VARCHAR(1)) ";
        $query .= "      ,registercd  = '".STAFFCD."' ";
        $query .= "      ,updated     = SYSDATE() ";
        $query .= " WHERE entexamyear = '".$model->examyear."' ";
        $query .= "   AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "   AND testdiv     = '".$model->testdiv."' ";
        $query .= "   AND judgement   = '8'";
        $query .= "   AND applicantdiv NOT IN('1','2')  "; // 1:附属推薦、2:中高一貫
        $result2 = $db->query($query);
        if (DB::isError($result2)) {
            return $result2->getCode();
        }

        //下記の条件に当てはまる受験者は「8:未受験」扱いとする
        //1.第１志望の受験科目の得点データが１件もない
        //2.第１志望の受験科目の得点データが全て未受験
        $query  = "SELECT T1.examno ";
        $query .= "      ,SUM(SMALLINT(COALESCE(T4.attend_flg,'0')))";
        $query .= "  FROM entexam_applicantbase_dat T1";
        $query .= "       LEFT OUTER JOIN entexam_perfect_mst T2 ";
        $query .= "               ON T1.entexamyear  = T2.entexamyear AND ";
        $query .= "                  T1.testdiv      = T2.testdiv";
        $query .= "       LEFT OUTER JOIN entexam_wishdiv_mst T3 ";
        $query .= "               ON T3.wishno       = '1' AND ";
        $query .= "                  T1.entexamyear  = T3.entexamyear AND ";
        $query .= "                  T1.testdiv      = T3.testdiv AND ";
        $query .= "                  T1.desirediv    = T3.desirediv AND ";
        $query .= "                  T2.coursecd     = T3.coursecd AND ";
        $query .= "                  T2.majorcd      = T3.majorcd AND ";
        $query .= "                  T2.examcoursecd = T3.examcoursecd ";
        $query .= "       LEFT OUTER JOIN entexam_score_dat   T4";
        $query .= "               ON T1.entexamyear    = T4.entexamyear AND ";
        $query .= "                  T1.testdiv        = T4.testdiv AND ";
        $query .= "                  T1.examno         = T4.examno AND ";
        $query .= "                  T2.testsubclasscd = T4.testsubclasscd ";
        $query .= " WHERE T1.entexamyear = '".$model->examyear."' ";
        $query .= "   AND T1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "   AND T1.testdiv     = '".$model->testdiv."' "; //前期、後期
        $query .= "   AND T1.applicantdiv NOT IN('1','2')  ";       // 1:附属推薦、2:中高一貫 #2006/01/25 alp m-yama
        $query .= "   GROUP BY T1.examno";
        $query .= "   HAVING SUM(SMALLINT(COALESCE(T4.attend_flg,'0'))) = 0";//全科目未受験の人

        $result = $db->query($query); 
        if (DB::isError($result)) {
           return $result->getcode();
        }

        //8:未受験者更新処理
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $query  = "UPDATE entexam_applicantbase_dat ";
            $query .= "   SET judgement   = '8'";
            $query .= "      ,registercd  = '".STAFFCD."' ";
            $query .= "      ,updated     = SYSDATE() ";
            $query .= " WHERE entexamyear = '".$model->examyear."' ";
            $query .= "   AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
            $query .= "   AND testdiv     = '".$model->testdiv."' ";
            $query .= "   AND examno      = '".$row["EXAMNO"]."'";
            $result2 = $db->query($query);
            if (DB::isError($result2)) {
                return $result2->getCode();
            }
        }

        //席次等を算出 ===============================================================================================================
        //附属推薦、中高一貫、未受験は、除く
        $query  = "with t_base as ( ";
        $query .= " SELECT * ";
        $query .= "   FROM entexam_applicantbase_dat ";
        $query .= "  WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "    AND testdiv      = '".$model->testdiv."' ";
        $query .= "    AND examno   NOT IN (SELECT examno ";
        $query .= "                           FROM entexam_applicantbase_dat ";
        $query .= "                          WHERE entexamyear     = '".$model->examyear."' AND ";
        $query .= "                                SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "                                testdiv         = '".$model->testdiv."'     AND ";
        $query .= "                                (applicantdiv   IN('1','2')      OR  ";
        $query .= "                                 judgement      = '8' ) ";
        $query .= "                        ) ";
        $query .= "    ) ";

        $query .= " SELECT T0.examno ";
        $query .= "       ,T2.a_total_rank ";
        $query .= "       ,T3.a_div_rank ";
        $query .= "       ,T4.b_total_rank ";
        $query .= "       ,T5.b_div_rank ";
        $query .= "   FROM t_base T0 ";

        /* Ａ全体席次 */
        $query .= "       LEFT OUTER JOIN ";
        $query .= "       (SELECT T1.examno, CASE WHEN T1.a_total IS NOT NULL THEN COUNT(T2.a_total)+1 END as a_total_rank ";
        $query .= "          FROM ";
        $query .= "              ( SELECT examno ";
        $query .= "                      ,a_total ";
        $query .= "                  FROM t_base ";
        $query .= "                 WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                   AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "                   AND testdiv      = '".$model->testdiv."') T1 ";
        $query .= "              LEFT OUTER JOIN ";
        $query .= "              ( SELECT examno ";
        $query .= "                      ,a_total ";
        $query .= "                  FROM t_base ";
        $query .= "                 WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                   AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "                   AND testdiv      = '".$model->testdiv."') T2 ";
        $query .= "              ON T1.a_total < T2.a_total ";
        $query .= "         GROUP BY T1.examno, T1.a_total) T2 ON T0.examno = T2.examno ";

        /* Ａ志願区分別席次 */
        $query .= "       LEFT OUTER JOIN ";
        $query .= "      (SELECT T1.examno, CASE WHEN T1.a_total IS NOT NULL THEN COUNT(T2.a_total)+1 END as a_div_rank ";
        $query .= "          FROM ";
        $query .= "              ( SELECT examno ";
        $query .= "                      ,desirediv ";
        $query .= "                      ,a_total ";
        $query .= "                  FROM t_base ";
        $query .= "                 WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                   AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "                   AND testdiv      = '".$model->testdiv."') T1 ";
        $query .= "              LEFT OUTER JOIN ";
        $query .= "              ( SELECT examno ";
        $query .= "                      ,desirediv ";
        $query .= "                      ,a_total ";
        $query .= "                  FROM t_base ";
        $query .= "                 WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                   AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "                   AND testdiv      = '".$model->testdiv."') T2 ";
        $query .= "              ON T1.desirediv = T2.desirediv  ";  //試験区分(志望パターン)
        $query .= "             AND T1.a_total < T2.a_total ";
        $query .= "         GROUP BY T1.examno, T1.a_total) T3 ON T0.examno = T3.examno ";

        /* Ｂ全体席次 */
        $query .= "       LEFT OUTER JOIN ";
        $query .= "      (SELECT T1.examno, CASE WHEN T1.b_total IS NOT NULL THEN COUNT(T2.b_total)+1 END AS b_total_rank ";
        $query .= "          FROM ";
        $query .= "              ( SELECT examno ";
        $query .= "                      ,b_total ";
        $query .= "                  FROM t_base ";
        $query .= "                 WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                   AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "                   AND testdiv      = '".$model->testdiv."') T1 ";
        $query .= "              LEFT OUTER JOIN ";
        $query .= "              ( SELECT examno ";
        $query .= "                      ,b_total ";
        $query .= "                  FROM t_base ";
        $query .= "                 WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                   AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "                   AND testdiv      = '".$model->testdiv."') T2 ";
        $query .= "              ON T1.b_total < T2.b_total ";
        $query .= "         GROUP BY T1.examno, T1.b_total) T4 ON T0.examno = T4.examno ";

        /* Ｂ志願区分別席次 */
        $query .= "       LEFT OUTER JOIN ";
        $query .= "      ( SELECT T1.examno, CASE WHEN T1.b_total IS NOT NULL THEN COUNT(T2.b_total)+1 END AS b_div_rank ";
        $query .= "          FROM ";
        $query .= "              ( SELECT examno ";
        $query .= "                      ,desirediv ";
        $query .= "                      ,b_total ";
        $query .= "                  FROM t_base ";
        $query .= "                 WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                   AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "                   AND testdiv      = '".$model->testdiv."') T1 ";
        $query .= "              LEFT OUTER JOIN ";
        $query .= "              ( SELECT examno ";
        $query .= "                      ,desirediv ";
        $query .= "                      ,b_total ";
        $query .= "                  FROM t_base ";
        $query .= "                 WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                   AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "                   AND testdiv      = '".$model->testdiv."') T2 ";
        $query .= "              ON T1.desirediv = T2.desirediv  ";
        $query .= "             AND T1.b_total < T2.b_total ";
        $query .= "         GROUP BY T1.examno, T1.b_total) T5 ON T0.examno = T5.examno ";

        $query .= " WHERE T0.entexamyear = '".$model->examyear."' ";
        $query .= "   AND T0.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "   AND T0.testdiv     = '".$model->testdiv."' ";

        #echo $query;
        $result = $db->query($query); 
        if (DB::isError($result)) {
           return $result->getcode();
        }

        //席次項目の更新
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $query  = "UPDATE entexam_applicantbase_dat ";
            $query .= "   SET a_total_rank   = ".($row["A_TOTAL_RANK"]!= "" ? $row["A_TOTAL_RANK"] : "NULL");
            $query .= "      ,a_div_rank     = ".($row["A_DIV_RANK"]  != "" ? $row["A_DIV_RANK"]   : "NULL");
            $query .= "      ,b_total_rank   = ".($row["B_TOTAL_RANK"]!= "" ? $row["B_TOTAL_RANK"] : "NULL");
            $query .= "      ,b_div_rank     = ".($row["B_DIV_RANK"]  != "" ? $row["B_DIV_RANK"]   : "NULL");
            $query .= "      ,registercd     = '".STAFFCD."' ";
            $query .= "      ,updated        = SYSDATE() ";
            $query .= " WHERE entexamyear = '".$model->examyear."' ";
            $query .= "   AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
            $query .= "   AND testdiv     = '".$model->testdiv."' ";
            $query .= "   AND examno      = '".$row["EXAMNO"]."'";
            $result2 = $db->query($query);
            if (DB::isError($result2)) {
                return $result2->getCode();
            }
        }

        $db->commit();
        return DB_OK;
    }

    /*                  */
    /* 得点データの更新 */
    /*                  */
    /* 偏差値 受験科目ごとに算出 データ件数が多いと時間がかかるので席次はPHP側で算出し、UPDATE文を発行する */

    function UpdateScoreQuery(&$db, $model)
    {

        $a_std   = $b_std   = array();
        $a_score = $b_score = $array = array();

        //偏差値、席次の項目クリア 2005.08.17 minei
        $query  = "UPDATE entexam_score_dat ";
        $query .= "   SET a_std_score = CAST(NULL AS DECIMAL(4,1))";    //配点Ａ偏差値
        $query .= "      ,a_rank      = CAST(NULL AS SMALLINT)";        //配点Ａ全体席次
        $query .= "      ,b_std_score = CAST(NULL AS DECIMAL(4,1))";    //配点Ｂ偏差値
        $query .= "      ,b_rank      = CAST(NULL AS SMALLINT)";        //配点Ｂ全体席次
        $query .= "      ,registercd  = '".STAFFCD."' ";
        $query .= "      ,updated     = SYSDATE() ";
        $query .= " WHERE entexamyear = '".$model->examyear."' ";
        $query .= "   AND testdiv     = '".$model->testdiv."' ";
        $result2 = $db->query($query);
        if (DB::isError($result2)) {
            return $result2->getCode();
        }

        //各生徒の偏差値の算出
        //附属推薦、中高一貫、未受験は、除く
        $query  = "with t_score as ( ";
        $query .= " SELECT * ";
        $query .= "   FROM entexam_score_dat ";
        $query .= "  WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "    AND testdiv      = '".$model->testdiv."' ";
        $query .= "    AND examno   NOT IN (SELECT examno ";
        $query .= "                           FROM entexam_applicantbase_dat ";
        $query .= "                          WHERE entexamyear     = '".$model->examyear."' AND ";
        $query .= "                                SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "                                testdiv         = '".$model->testdiv."'     AND ";
        $query .= "                                (applicantdiv   IN('1','2')      OR  ";
        $query .= "                                 judgement      = '8' ) ";
        $query .= "                        ) ";
        $query .= "    ) ";

        $query .= "SELECT T0.examno ";
        $query .= "      ,T0.testsubclasscd ";
        $query .= "      ,T1.a_std_score ";
        $query .= "      ,T2.b_std_score ";
        $query .= "  FROM t_score T0 ";

                         /* Ａ得点の偏差値 */
        $query .= "   LEFT OUTER JOIN ";
        $query .= "     (SELECT T1.examno ";
        $query .= "            ,T2.testsubclasscd ";
        $query .= "            ,ROUND(50 + 10 * (T1.a_score - T2.avg_score) / CASE WHEN T2.std_score = 0 THEN 1 ELSE T2.std_score END,1) AS a_std_score";   //偏差値(小数第2位で四捨五入)
        $query .= "        FROM t_score T1 ";
        $query .= "            ,(SELECT testsubclasscd ";
        $query .= "                    ,ROUND(STDDEV(a_score),1)       AS std_score ";   //標準偏差
        $query .= "                    ,ROUND(AVG(DECIMAL(a_score)),1) AS avg_score ";   //平均
        $query .= "                FROM t_score ";
        $query .= "               WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                 AND testdiv      = '".$model->testdiv."' ";
        $query .= "             GROUP BY testsubclasscd) T2 ";
        $query .= "       WHERE T1.testsubclasscd = T2.testsubclasscd ";
        $query .= "         AND T1.entexamyear    = '".$model->examyear."' ";
        $query .= "         AND T1.testdiv        = '".$model->testdiv."' ";
        $query .= "     ) T1 ON T0.examno = T1.examno AND T0.testsubclasscd = T1.testsubclasscd";

                         /* Ｂ得点の偏差値 */
        $query .= "     LEFT OUTER JOIN ";
        $query .= "     (SELECT T1.examno ";
        $query .= "            ,T2.testsubclasscd ";
        $query .= "            ,ROUND(50 + 10 * (T1.b_score - T2.avg_score) / CASE WHEN T2.std_score = 0 THEN 1 ELSE T2.std_score END,1) AS b_std_score";   //偏差値(小数第2位で四捨五入)
        $query .= "        FROM t_score T1 ";
        $query .= "            ,(SELECT testsubclasscd ";
        $query .= "                    ,ROUND(STDDEV(b_score),1)       AS std_score ";   //標準偏差
        $query .= "                    ,ROUND(AVG(DECIMAL(b_score)),1) AS avg_score ";   //平均
        $query .= "                FROM t_score ";
        $query .= "               WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                 AND testdiv      = '".$model->testdiv."' ";
        $query .= "             GROUP BY testsubclasscd) T2 ";
        $query .= "       WHERE T1.testsubclasscd = T2.testsubclasscd ";
        $query .= "         AND T1.entexamyear    = '".$model->examyear."' ";
        $query .= "         AND T1.testdiv        = '".$model->testdiv."' ";
        $query .= "     ) T2 ON T0.examno = T2.examno AND T0.testsubclasscd = T2.testsubclasscd ";
        $query .= " WHERE T0.entexamyear  = '".$model->examyear."' ";
        $query .= "   AND T0.testdiv      = '".$model->testdiv."' ";

        $result = $db->query($query); 
        if (DB::isError($result)) {
           return $result->getCode();
        }

        set_time_limit(0);

        //変数に一時保存
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $a_std[$row["TESTSUBCLASSCD"]][$row["EXAMNO"]] = $row["A_STD_SCORE"];
            $b_std[$row["TESTSUBCLASSCD"]][$row["EXAMNO"]] = $row["B_STD_SCORE"];
        }

        //得点を取得
        $query  = "SELECT T0.examno, T0.testsubclasscd, T0.a_score, T0.b_score ";
        $query .= "  FROM entexam_score_dat T0";
        $query .= " WHERE T0.entexamyear  = '".$model->examyear."'";
        $query .= "   AND T0.testdiv      = '".$model->testdiv."' ";
        //附属推薦、中高一貫、未受験は、除く        2005.08.17 minei
        $query .= "   AND T0.examno NOT IN (SELECT examno ";
        $query .= "                           FROM entexam_applicantbase_dat ";
        $query .= "                          WHERE entexamyear     = T0.entexamyear AND ";
        $query .= "                                SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "                                testdiv         = T0.testdiv     AND ";
        $query .= "                                (applicantdiv   IN('1','2')      OR  ";     // 1:附属推薦、2:中高一貫    #2005/10/19 minei
#        $query .= "                                (applicantdiv   IN('1','2','3')      OR  ";     // 1:附属推薦、2:中高一貫、3:スポーツ推薦       #2005/09/12 arakaki
        $query .= "                                 judgement      = '8' ) ";                  // 8:未受験
        $query .= "                        ) ";

        $result = $db->query($query); 
        if (DB::isError($result)) {
           return $result->getCode();
        }

        //変数に一時保存
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $a_score[$row["TESTSUBCLASSCD"]][] = $row["A_SCORE"];
            $b_score[$row["TESTSUBCLASSCD"]][] = $row["B_SCORE"];
            $array[$row["TESTSUBCLASSCD"]][] = $row;
        }

        $db->autoCommit(false);

        foreach ($array as $subclasscd => $val)
        {
            //席次を求める
            array_multisort($a_score[$subclasscd], SORT_NUMERIC, SORT_DESC);
            array_multisort($b_score[$subclasscd], SORT_NUMERIC, SORT_DESC);
            foreach($val as $row)
            {
                $arr    = array_keys($a_score[$subclasscd], $row["A_SCORE"]);
                $a_rank = $arr[0] + 1;

                $arr    = array_keys($b_score[$subclasscd], $row["B_SCORE"]);
                $b_rank = $arr[0] + 1;

                $query  = "UPDATE entexam_score_dat  ";
                $query .= "   SET a_std_score = ".($a_std[$subclasscd][$row["EXAMNO"]] != "" ? $a_std[$subclasscd][$row["EXAMNO"]] : "NULL");
                $query .= "      ,b_std_score = ".($b_std[$subclasscd][$row["EXAMNO"]] != "" ? $b_std[$subclasscd][$row["EXAMNO"]] : "NULL");
                $query .= "      ,a_rank      = ".($a_rank != "" ? $a_rank : "NULL");
                $query .= "      ,b_rank      = ".($b_rank != "" ? $b_rank : "NULL");
                $query .= "      ,registercd = '".STAFFCD."' ";
                $query .= "      ,updated    = SYSDATE() ";
                $query .= " WHERE entexamyear    = '".$model->examyear."' ";
                $query .= "   AND testdiv        = '".$model->testdiv."' ";
                $query .= "   AND examno         = '".$row["EXAMNO"]."'";
                $query .= "   AND testsubclasscd = '".$subclasscd."'";

                //エラー時後処理
                $result = $db->query($query);
                if (DB::isError($result)) {
                   return $result->getCode();
                }
            }
        }

        $db->commit();
        unset($a_std, $b_std, $a_score, $b_score, $array);
        return DB_OK;
    } 
} 
?>