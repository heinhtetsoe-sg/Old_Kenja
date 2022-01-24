<?php

require_once('for_php7.php');
class knjl061jquery extends Query {

    //名称
    function GetName($namecd1, $ObjYear)
    {
        $query = "SELECT namecd2, name1, name2, namespare1, namespare2, ABBV2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2";
        return $query;
    }

    function getTestSubclasscd($testdiv, $ObjYear)
    {
        $query  = " SELECT  TESTSUBCLASSCD ";
        $query .= " FROM    ENTEXAM_SCORE_DAT ";
        $query .= " WHERE   ENTEXAMYEAR = '".$ObjYear."' ";
        $query .= "   AND   TESTDIV     = '".$testdiv."' ";
        $query .= " GROUP BY TESTSUBCLASSCD ";
        $query .= " ORDER BY TESTSUBCLASSCD ";
        return $query;
    }

    function getTestSubclasscdUpdated($ObjYear)
    {
        $query  = "";
        $query .= " SELECT  T1.TESTDIV,N1.NAME1 AS TESTDIV_NAME ";
        $query .= "        ,T1.TESTSUBCLASSCD,N2.NAME1 AS TESTSUBCLASSCD_NAME ";
        $query .= "        ,T1.LINK_JUDGE_DIV,T1.UPDATED,date(T1.UPDATED) as day,time(T1.UPDATED) as tim ";
        $query .= " FROM   (SELECT  TESTDIV,TESTSUBCLASSCD,MAX(LINK_JUDGE_DIV) AS LINK_JUDGE_DIV,MAX(UPDATED) AS UPDATED ";
        $query .= "         FROM    ENTEXAM_TESTSUBCLASSCD_DAT ";
        $query .= "         WHERE   ENTEXAMYEAR='".$ObjYear."' ";
        $query .= "         GROUP BY TESTDIV,TESTSUBCLASSCD ) T1 ";
        $query .= " LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L004' AND N1.NAMECD2=T1.TESTDIV ";
        $query .= " LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L009' AND N2.NAMECD2=T1.TESTSUBCLASSCD ";
        $query .= " ORDER BY T1.TESTDIV,T1.TESTSUBCLASSCD ";
        return $query;
    }

    function ExecuteQuery($model)
    {
        $db = Query::dbCheckOut();

        //対象人数チェック
        $query  = "SELECT COUNT(*) FROM entexam_recept_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->examyear."'";
        $query .= "   AND T1.testdiv      = '".$model->testdiv."'";

        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db); 
            return false;
        }

        //エラー無視
        $db->expectError();

        //リンク判定科目
        $testsubclasscd_link = "('" .implode("','", $model->testsubclasscd_link) ."')";

        //帰国生を除いた入試区分・・・リンク判定科目の算出処理対象の入試区分です
        $nonTestdiv = "('" .implode("','", $model->nonTestdiv) ."')";

        /*                  */
        /* 受付データの更新 */
        /*                  */
        /* リンク判定偏差値・リンク判定偏差値区分 入試区分ごとに算出 */
        $query  = "UPDATE entexam_recept_dat ";
        $query .= "   SET (examno, link_judge_deviation, link_judge_deviation_div, registercd, updated) ";
        $query .= "     = ( ";
        $query .= "SELECT  TT0.EXAMNO ";
        $query .= "       ,CASE WHEN TT0.EXAM_TYPE = '2' AND TT1.LINK_JUDGE_DEVIATION < TT2.LINK_JUDGE_DEVIATION ";
        $query .= "             THEN TT2.LINK_JUDGE_DEVIATION ELSE TT1.LINK_JUDGE_DEVIATION END AS LINK_JUDGE_DEVIATION ";//リンク判定偏差値
        $query .= "       ,CASE WHEN TT0.EXAM_TYPE = '2' AND TT1.LINK_JUDGE_DEVIATION < TT2.LINK_JUDGE_DEVIATION ";
        $query .= "             THEN TT2.LINK_JUDGE_DEVIATION_DIV ELSE TT1.LINK_JUDGE_DEVIATION_DIV END AS LINK_JUDGE_DEVIATION_DIV ";//リンク判定偏差値区分
        $query .= "       ,'".STAFFCD."' ";
        $query .= "       ,SYSDATE() ";
        $query .= "  FROM  ENTEXAM_RECEPT_DAT TT0 ";
                           /* ４科平均偏差値 ４科型・２科型・３科型を含む */
        $query .= "        LEFT JOIN ( ";
        $query .= "            SELECT  EXAMNO,ROUND(AVG(STD_SCORE),1) AS LINK_JUDGE_DEVIATION,'2' AS LINK_JUDGE_DEVIATION_DIV ";
                                       /* リンク判定科目 */
        $query .= "              FROM (SELECT  T1.EXAMNO,T2.TESTSUBCLASSCD,MAX(T2.STD_SCORE) AS STD_SCORE ";
        $query .= "                      FROM  ENTEXAM_RECEPT_DAT T1 ";
        $query .= "                            INNER JOIN ENTEXAM_SCORE_DAT T2 ";
        $query .= "                                             ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                            AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                            AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "                                            AND T2.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "                                            AND T2.RECEPTNO     = T1.RECEPTNO ";
        $query .= "                                            AND T2.TESTSUBCLASSCD IN ".$testsubclasscd_link." ";
        $query .= "                     WHERE  T1.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "                       AND  T1.TESTDIV     <= '".$model->testdiv."' ";
        $query .= "                       AND  T1.TESTDIV     IN ".$nonTestdiv." ";
        $query .= "                    GROUP BY T1.EXAMNO,T2.TESTSUBCLASSCD ";
                                       /* リンク判定科目以外 */
        $query .= "                    UNION ALL ";
        $query .= "                    SELECT  T1.EXAMNO,T2.TESTSUBCLASSCD,T2.STD_SCORE ";
        $query .= "                      FROM  ENTEXAM_RECEPT_DAT T1 ";
        $query .= "                            INNER JOIN ENTEXAM_SCORE_DAT T2 ";
        $query .= "                                             ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                            AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                            AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "                                            AND T2.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "                                            AND T2.RECEPTNO     = T1.RECEPTNO ";
        $query .= "                                            AND T2.TESTSUBCLASSCD NOT IN ".$testsubclasscd_link." ";
        $query .= "                     WHERE  T1.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "                       AND  T1.TESTDIV      = '".$model->testdiv."' ) T0 ";
        $query .= "            GROUP BY EXAMNO ";
        $query .= "        ) TT1 ON TT1.EXAMNO = TT0.EXAMNO ";
                           /* ３科平均偏差値 ４科型のみ */
        $query .= "        LEFT JOIN ( ";
        $query .= "            SELECT  EXAMNO,ROUND(AVG(STD_SCORE),1) AS LINK_JUDGE_DEVIATION,'1' AS LINK_JUDGE_DEVIATION_DIV ";
        $query .= "              FROM (SELECT  EXAMNO,'9' AS TESTSUBCLASSCD,MAX(STD_SCORE) AS STD_SCORE ";
                                               /* リンク判定科目 */
        $query .= "                      FROM (SELECT  T1.EXAMNO,T2.TESTSUBCLASSCD,MAX(T2.STD_SCORE) AS STD_SCORE ";
        $query .= "                              FROM  ENTEXAM_RECEPT_DAT T1 ";
        $query .= "                                    INNER JOIN ENTEXAM_SCORE_DAT T2 ";
        $query .= "                                                     ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                                    AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                                    AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "                                                    AND T2.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "                                                    AND T2.RECEPTNO     = T1.RECEPTNO ";
        $query .= "                                                    AND T2.TESTSUBCLASSCD IN ".$testsubclasscd_link." ";
        $query .= "                             WHERE  T1.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "                               AND  T1.TESTDIV     <= '".$model->testdiv."' ";
        $query .= "                               AND  T1.TESTDIV     IN ".$nonTestdiv." ";
        $query .= "                            GROUP BY T1.EXAMNO,T2.TESTSUBCLASSCD ";
                                               /* リンク判定科目以外 */
        $query .= "                            UNION ALL ";
        $query .= "                            SELECT  T1.EXAMNO,T2.TESTSUBCLASSCD,T2.STD_SCORE ";
        $query .= "                              FROM  ENTEXAM_RECEPT_DAT T1 ";
        $query .= "                                    INNER JOIN ENTEXAM_SCORE_DAT T2 ";
        $query .= "                                                     ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                                    AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                                    AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "                                                    AND T2.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "                                                    AND T2.RECEPTNO     = T1.RECEPTNO ";
        $query .= "                                                    AND T2.TESTSUBCLASSCD NOT IN ".$testsubclasscd_link." ";
        $query .= "                             WHERE  T1.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "                               AND  T1.TESTDIV      = '".$model->testdiv."' ) T0 ";
        $query .= "                     WHERE  TESTSUBCLASSCD IN ('4','5') ";
        $query .= "                    GROUP BY EXAMNO ";
        $query .= "                    UNION ALL ";
        $query .= "                    SELECT  EXAMNO,TESTSUBCLASSCD,STD_SCORE ";
                                               /* リンク判定科目 */
        $query .= "                      FROM (SELECT  T1.EXAMNO,T2.TESTSUBCLASSCD,MAX(T2.STD_SCORE) AS STD_SCORE ";
        $query .= "                              FROM  ENTEXAM_RECEPT_DAT T1 ";
        $query .= "                                    INNER JOIN ENTEXAM_SCORE_DAT T2 ";
        $query .= "                                                     ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                                    AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                                    AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "                                                    AND T2.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "                                                    AND T2.RECEPTNO     = T1.RECEPTNO ";
        $query .= "                                                    AND T2.TESTSUBCLASSCD IN ".$testsubclasscd_link." ";
        $query .= "                             WHERE  T1.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "                               AND  T1.TESTDIV     <= '".$model->testdiv."' ";
        $query .= "                               AND  T1.TESTDIV     IN ".$nonTestdiv." ";
        $query .= "                            GROUP BY T1.EXAMNO,T2.TESTSUBCLASSCD ";
                                               /* リンク判定科目以外 */
        $query .= "                            UNION ALL ";
        $query .= "                            SELECT  T1.EXAMNO,T2.TESTSUBCLASSCD,T2.STD_SCORE ";
        $query .= "                              FROM  ENTEXAM_RECEPT_DAT T1 ";
        $query .= "                                    INNER JOIN ENTEXAM_SCORE_DAT T2 ";
        $query .= "                                                     ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                                    AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                                    AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "                                                    AND T2.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "                                                    AND T2.RECEPTNO     = T1.RECEPTNO ";
        $query .= "                                                    AND T2.TESTSUBCLASSCD NOT IN ".$testsubclasscd_link." ";
        $query .= "                             WHERE  T1.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "                               AND  T1.TESTDIV      = '".$model->testdiv."' ) T0 ";
        $query .= "                     WHERE  TESTSUBCLASSCD IN ('2','3') ) T1 ";
        $query .= "            GROUP BY EXAMNO ";
        $query .= "        ) TT2 ON TT2.EXAMNO = TT0.EXAMNO ";
        $query .= " WHERE  TT0.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "   AND  TT0.TESTDIV      = '".$model->testdiv."' ";
        $query .= "   AND  TT0.applicantdiv = entexam_recept_dat.applicantdiv ";
        $query .= "   AND  TT0.exam_type    = entexam_recept_dat.exam_type ";
        $query .= "   AND  TT0.receptno     = entexam_recept_dat.receptno ";
        $query .= "       )";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'"; 

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
           $db->popExpect();
           Query::dbCheckIn($db);
           return $result->getCode();
        }

        /*                      */
        /* 試験科目データの更新 */
        /*                      */
        /* リンク判定区分 クリア */
//        $query  = "UPDATE ENTEXAM_TESTSUBCLASSCD_DAT ";
//        $query .= "   SET (LINK_JUDGE_DIV, registercd, updated) ";
//        $query .= "     = (NULL, '".STAFFCD."', SYSDATE())";
//        $query .= " WHERE entexamyear  = '".$model->examyear."'";
//        $query .= "   AND testdiv      = '".$model->testdiv."'";
//        $query .= "   AND TESTSUBCLASSCD NOT IN ".$testsubclasscd_link;
        $query  = " DELETE  FROM    ENTEXAM_TESTSUBCLASSCD_DAT ";
        $query .= " WHERE   ENTEXAMYEAR='".$model->examyear."' ";
        $query .= "   AND   TESTDIV='".$model->testdiv."' ";
        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
           $db->popExpect();
           Query::dbCheckIn($db);
           return $result->getCode();
        }

        $query  = " INSERT INTO ENTEXAM_TESTSUBCLASSCD_DAT(ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,TESTSUBCLASSCD,REGISTERCD,UPDATED)  ";
        $query .= " SELECT  ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,TESTSUBCLASSCD,'".STAFFCD."',sysdate() ";
        $query .= " FROM    ENTEXAM_SCORE_DAT ";
        $query .= " WHERE   ENTEXAMYEAR='".$model->examyear."' ";
        $query .= "   AND   TESTDIV='".$model->testdiv."' ";
        $query .= " GROUP BY ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,TESTSUBCLASSCD ";
        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
           $db->popExpect();
           Query::dbCheckIn($db);
           return $result->getCode();
        }

        /* リンク判定区分 入試区分・試験科目ごとに更新 */
        $query  = "UPDATE ENTEXAM_TESTSUBCLASSCD_DAT ";
        $query .= "   SET (LINK_JUDGE_DIV, registercd, updated) ";
        $query .= "     = ('1', '".STAFFCD."', SYSDATE())";
        $query .= " WHERE entexamyear  = '".$model->examyear."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'";
        $query .= "   AND TESTSUBCLASSCD IN ".$testsubclasscd_link;
        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
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

