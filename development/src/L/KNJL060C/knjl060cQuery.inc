<?php

require_once('for_php7.php');

class knjl060cquery extends Query {

    //学校コードを取得：五条・和歌山を判断するため
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //名称
    function GetName($namecd1, $ObjYear)
    {
        $query = "SELECT namecd2, name1, name2, namespare1, namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2";
        return $query;
    }

    //科目
    function getSubclasscd($ObjYear, $applicantdiv, $testdiv)
    {
        $query  = " SELECT ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     L1.NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTSUBCLASSCD_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ";
        $query .= "          ON L1.YEAR     = '{$ObjYear}'  ";
        $query .= "         AND L1.NAMECD1  = 'L009'  ";
        $query .= "         AND L1.NAMECD2  = T1.TESTSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.TESTSUBCLASSCD ";

        return $query;
    }

    //満点マスタから平均点を算出する分母を取得
    function getAvgBunbo($ObjYear, $applicantdiv, $testdiv, $type = "") {
        $query  = " SELECT ";
        $query .= "     SUM(T1.PERFECT)/100 AS BUNBO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$testdiv}' ";
        //2科(国算)
        if ($type == "TESTSUBCLASSCD12") {
            $query .= "     AND T1.TESTSUBCLASSCD in ('1','2') ";
        }

        return $query;
    }

    function ExecuteQuery($model)
    {
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

        //受験科目を取得
        $subCnt = $selectSubCnt = 0;
        $subclasscd4 = array();
        //2科(国算)
        $subCnt2 = $selectSubCnt2 = 0;
        $subclasscd2 = array();
        $result = $db->query(knjl060cQuery::getSubclasscd($model->examyear, $model->applicantdiv, $model->testdiv));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $subclasscd4[] = $row["TESTSUBCLASSCD"];
            //選択受験科目は、１科目選択。
            if ($row["NAMESPARE1"] == "1") {
                $selectSubCnt = 1;
            } else {
                $subCnt++;
            }

            //2科(国算)
            if ($row["TESTSUBCLASSCD"] == "1" || $row["TESTSUBCLASSCD"] == "2") {
                $subclasscd2[] = $row["TESTSUBCLASSCD"];
                if ($row["NAMESPARE1"] == "1") {
                    $selectSubCnt2 = 1;
                } else {
                    $subCnt2++;
                }
            }
        }
        //高校・編入コースの受験科目は、３科目＋２科目の内１科目選択。
        $subclasscd4_count = $subCnt + $selectSubCnt;
        $subclasscd4_implode = implode("','",$subclasscd4);
        //2科(国算)
        $subclasscd2_count = $subCnt2 + $selectSubCnt2;
        $subclasscd2_implode = implode("','",$subclasscd2);

        //平均点を算出する分母を取得
        $avgBunbo = $db->getOne(knjl060cQuery::getAvgBunbo($model->examyear, $model->applicantdiv, $model->testdiv));
        if (!strlen($avgBunbo)) $avgBunbo = "4"; //DBエラー回避
        //2科(国算)
        $avgBunbo2 = $db->getOne(knjl060cQuery::getAvgBunbo($model->examyear, $model->applicantdiv, $model->testdiv, "TESTSUBCLASSCD12"));
        if (!strlen($avgBunbo2)) $avgBunbo2 = "3"; //DBエラー回避

        //エラー無視
        $db->expectError();

        /*                  */
        /* 受付データの更新 */
        /*                  */

        /* 受付データにあって得点データに1科目分もレコードがない受験番号の人は、受付データの指定した項目をNULLで更新 */
        if ($model->isCollege && $model->applicantdiv == "1" && $model->testdiv == "1") {
            $query  = "UPDATE entexam_recept_dat ";
            $query .= "   SET (examno, attend_all_flg, total4, avarage4, total2, avarage2, avarage1, registercd, updated) ";
            $query .= "     = ( ";
            $query .= "SELECT TT0.examno ";
            $query .= "      ,CASE WHEN B1.SUB_ORDER = '1' THEN TT1.attend_all_flg ";
            $query .= "            WHEN B1.SUB_ORDER = '2' THEN TT2.attend_all_flg ";
            $query .= "       END AS attend_all_flg";   //全科目受験フラグ
            $query .= "      ,TT1.total4 ";             //全科目合計
            $query .= "      ,TT1.average4 ";           //全科目平均
            $query .= "      ,TT2.total2 ";             //２科目合計
            $query .= "      ,TT2.average2 ";           //２科目平均
            $query .= "      ,TT3.average1";            //得点率平均
            $query .= "      ,'".STAFFCD."' ";
            $query .= "      ,SYSDATE() ";
            $query .= "  FROM entexam_recept_dat TT0 ";
            $query .= "       LEFT JOIN entexam_applicantbase_dat B1 ON B1.entexamyear  = TT0.entexamyear AND B1.applicantdiv = TT0.applicantdiv AND B1.examno = TT0.examno ";
            $query .= "       LEFT JOIN ( ";
            $query .= "             SELECT ";
            $query .= "                 R1.RECEPTNO, ";
            $query .= "                 R1.EXAM_TYPE, ";
            $query .= "                 CASE WHEN SUM(SMALLINT(S1.ATTEND_FLG)) >= {$subclasscd4_count} THEN '1' ELSE '0' END AS attend_all_flg, ";
            $query .= "                 SUM(S1.SCORE) AS total4, ";
            $query .= "                 CASE WHEN SUM(SMALLINT(S1.ATTEND_FLG)) >= {$subclasscd4_count} THEN ROUND(DECIMAL(SUM(S1.SCORE))/{$avgBunbo},1) END AS average4 ";
            $query .= "             FROM ";
            $query .= "                 ENTEXAM_RECEPT_DAT R1 ";
            $query .= "                 LEFT JOIN ENTEXAM_APPLICANTBASE_DAT B1 ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR AND B1.EXAMNO = R1.EXAMNO ";
            $query .= "                 LEFT JOIN ENTEXAM_SCORE_DAT S1 ";
            $query .= "                     ON  S1.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
            $query .= "                     AND S1.APPLICANTDIV = R1.APPLICANTDIV ";
            $query .= "                     AND S1.TESTDIV = R1.TESTDIV ";
            $query .= "                     AND S1.EXAM_TYPE = R1.EXAM_TYPE ";
            $query .= "                     AND S1.RECEPTNO = R1.RECEPTNO ";
            $query .= "             WHERE ";
            $query .= "                 R1.ENTEXAMYEAR = '".$model->examyear."' ";
            $query .= "                 AND R1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "                 AND R1.TESTDIV = '".$model->testdiv."' ";
            $query .= "                 AND B1.SUB_ORDER IN ('1') "; //1:Ⅰ型(国算理)
            $query .= "                 AND S1.TESTSUBCLASSCD IN ('".$subclasscd4_implode."') "; //3科
            $query .= "             GROUP BY ";
            $query .= "                 R1.RECEPTNO, ";
            $query .= "                 R1.EXAM_TYPE ";
            $query .= "       ) TT1 ON TT0.receptno  = TT1.receptno AND TT0.exam_type = TT1.exam_type ";
            $query .= "       LEFT JOIN ( ";
            $query .= "             SELECT ";
            $query .= "                 R1.RECEPTNO, ";
            $query .= "                 R1.EXAM_TYPE, ";
            $query .= "                 CASE WHEN SUM(SMALLINT(S1.ATTEND_FLG)) >= {$subclasscd2_count} THEN '1' ELSE '0' END AS attend_all_flg, ";
            $query .= "                 SUM(S1.SCORE) AS total2, ";
            $query .= "                 CASE WHEN SUM(SMALLINT(S1.ATTEND_FLG)) >= {$subclasscd2_count} THEN ROUND(DECIMAL(SUM(S1.SCORE))/{$avgBunbo2},1) END AS average2 ";
            $query .= "             FROM ";
            $query .= "                 ENTEXAM_RECEPT_DAT R1 ";
            $query .= "                 LEFT JOIN ENTEXAM_APPLICANTBASE_DAT B1 ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR AND B1.EXAMNO = R1.EXAMNO ";
            $query .= "                 LEFT JOIN ENTEXAM_SCORE_DAT S1 ";
            $query .= "                     ON  S1.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
            $query .= "                     AND S1.APPLICANTDIV = R1.APPLICANTDIV ";
            $query .= "                     AND S1.TESTDIV = R1.TESTDIV ";
            $query .= "                     AND S1.EXAM_TYPE = R1.EXAM_TYPE ";
            $query .= "                     AND S1.RECEPTNO = R1.RECEPTNO ";
            $query .= "             WHERE ";
            $query .= "                 R1.ENTEXAMYEAR = '".$model->examyear."' ";
            $query .= "                 AND R1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "                 AND R1.TESTDIV = '".$model->testdiv."' ";
            $query .= "                 AND B1.SUB_ORDER IN ('1','2') "; //1:Ⅰ型(国算理) 2:Ⅱ型(国算)
            $query .= "                 AND S1.TESTSUBCLASSCD IN ('".$subclasscd2_implode."') "; //2科
            $query .= "             GROUP BY ";
            $query .= "                 R1.RECEPTNO, ";
            $query .= "                 R1.EXAM_TYPE ";
            $query .= "       ) TT2 ON TT0.receptno  = TT2.receptno AND TT0.exam_type = TT2.exam_type ";
            $query .= "       LEFT JOIN ( ";
            $query .= "         SELECT ";
            $query .= "             T1.RECEPTNO, ";
            $query .= "             T1.EXAM_TYPE, ";
            $query .= "             MAX(T1.AVERAGE) AS average1 ";
            $query .= "         FROM ";
            $query .= "             ( ";
            $query .= "             SELECT ";
            $query .= "                 R1.RECEPTNO, ";
            $query .= "                 R1.EXAM_TYPE, ";
            $query .= "                 CASE WHEN SUM(SMALLINT(S1.ATTEND_FLG)) >= {$subclasscd4_count} THEN ROUND(DECIMAL(SUM(S1.SCORE))/{$avgBunbo},1) END AS AVERAGE ";
            $query .= "             FROM ";
            $query .= "                 ENTEXAM_RECEPT_DAT R1 ";
            $query .= "                 LEFT JOIN ENTEXAM_APPLICANTBASE_DAT B1 ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR AND B1.EXAMNO = R1.EXAMNO ";
            $query .= "                 LEFT JOIN ENTEXAM_SCORE_DAT S1 ";
            $query .= "                     ON  S1.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
            $query .= "                     AND S1.APPLICANTDIV = R1.APPLICANTDIV ";
            $query .= "                     AND S1.TESTDIV = R1.TESTDIV ";
            $query .= "                     AND S1.EXAM_TYPE = R1.EXAM_TYPE ";
            $query .= "                     AND S1.RECEPTNO = R1.RECEPTNO ";
            $query .= "             WHERE ";
            $query .= "                 R1.ENTEXAMYEAR = '".$model->examyear."' ";
            $query .= "                 AND R1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "                 AND R1.TESTDIV = '".$model->testdiv."' ";
            $query .= "                 AND B1.SUB_ORDER IN ('1') "; //1:Ⅰ型(国算理)
            $query .= "                 AND S1.TESTSUBCLASSCD IN ('".$subclasscd4_implode."') "; //3科
            $query .= "             GROUP BY ";
            $query .= "                 R1.RECEPTNO, ";
            $query .= "                 R1.EXAM_TYPE ";
            $query .= "             UNION ALL ";
            $query .= "             SELECT ";
            $query .= "                 R1.RECEPTNO, ";
            $query .= "                 R1.EXAM_TYPE, ";
            $query .= "                 CASE WHEN SUM(SMALLINT(S1.ATTEND_FLG)) >= {$subclasscd2_count} THEN ROUND(DECIMAL(SUM(S1.SCORE))/{$avgBunbo2},1) END AS AVERAGE ";
            $query .= "             FROM ";
            $query .= "                 ENTEXAM_RECEPT_DAT R1 ";
            $query .= "                 LEFT JOIN ENTEXAM_APPLICANTBASE_DAT B1 ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR AND B1.EXAMNO = R1.EXAMNO ";
            $query .= "                 LEFT JOIN ENTEXAM_SCORE_DAT S1 ";
            $query .= "                     ON  S1.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
            $query .= "                     AND S1.APPLICANTDIV = R1.APPLICANTDIV ";
            $query .= "                     AND S1.TESTDIV = R1.TESTDIV ";
            $query .= "                     AND S1.EXAM_TYPE = R1.EXAM_TYPE ";
            $query .= "                     AND S1.RECEPTNO = R1.RECEPTNO ";
            $query .= "             WHERE ";
            $query .= "                 R1.ENTEXAMYEAR = '".$model->examyear."' ";
            $query .= "                 AND R1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "                 AND R1.TESTDIV = '".$model->testdiv."' ";
            $query .= "                 AND B1.SUB_ORDER IN ('1','2') "; //1:Ⅰ型(国算理) 2:Ⅱ型(国算)
            $query .= "                 AND S1.TESTSUBCLASSCD IN ('".$subclasscd2_implode."') "; //2科
            $query .= "             GROUP BY ";
            $query .= "                 R1.RECEPTNO, ";
            $query .= "                 R1.EXAM_TYPE ";
            $query .= "             ) T1 ";
            $query .= "         GROUP BY ";
            $query .= "             T1.RECEPTNO, ";
            $query .= "             T1.EXAM_TYPE ";
            $query .= "       ) TT3 ON TT0.receptno  = TT3.receptno AND TT0.exam_type = TT3.exam_type ";
            $query .= " WHERE TT0.entexamyear  = '".$model->examyear."' ";
            $query .= "   AND TT0.applicantdiv = '".$model->applicantdiv."' ";
            $query .= "   AND TT0.testdiv      = '".$model->testdiv."' ";
            $query .= "   AND TT0.exam_type    = entexam_recept_dat.exam_type ";
            $query .= "   AND TT0.receptno     = entexam_recept_dat.receptno ";
            $query .= "       )";
            $query .= " WHERE entexamyear  = '".$model->examyear."'";
            $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
            $query .= "   AND testdiv      = '".$model->testdiv."'"; 
        } else {
            $query  = "UPDATE entexam_recept_dat ";
            $query .= "   SET (examno, attend_all_flg, total4, avarage4, registercd, updated) ";
            $query .= "     = ( ";
            $query .= "SELECT TT0.examno ";
            $query .= "      ,TT1.attend_all_flg ";     //全科目受験フラグ
            $query .= "      ,TT1.total4 ";             //全科目合計
            $query .= "      ,TT1.average4 ";           //全科目平均
            $query .= "      ,'".STAFFCD."' ";
            $query .= "      ,SYSDATE() ";
            $query .= "  FROM entexam_recept_dat TT0 ";
                              /* 全科目分の出欠フラグ、合計点、平均点 */ 
            $query .= "       LEFT JOIN ";
            $query .= "        (SELECT t1.receptno";
            $query .= "               ,t1.exam_type";
            $query .= "               ,CASE WHEN SUM(SMALLINT(t1.attend_flg)) >= {$subclasscd4_count} THEN '1' ELSE '0' END AS attend_all_flg";
            $query .= "               ,SUM(t1.score)                    AS total4";
            //五條中学の場合、
            //・全科目に満たない受験者の平均点と順位は、NULLで更新
            //・前期：平均点＝合計点÷４で更新
            //・後期：平均点＝合計点÷３で更新
            if ($model->isGojou && $model->applicantdiv == "1") {
                $query .= "           ,CASE WHEN SUM(SMALLINT(t1.attend_flg)) >= {$subclasscd4_count} ";
                $query .= "                 THEN ROUND(DECIMAL(SUM(t1.score))/{$avgBunbo},1) END AS average4";
            } else {
                $query .= "           ,ROUND(AVG(DECIMAL(t1.score)),1)  AS average4";
            }
            $query .= "           FROM (";
            $query .= "             SELECT W0.receptno";
            $query .= "                   ,W0.exam_type";
            $query .= "                   ,W0.attend_flg";
            $query .= "                   ,W0.score";
            $query .= "               FROM entexam_score_dat W0";
            $query .= "              where W0.entexamyear  = '".$model->examyear."' ";
            $query .= "                AND W0.applicantdiv = '".$model->applicantdiv."'";
            $query .= "                AND W0.testdiv      = '".$model->testdiv."'";
            $query .= "                and W0.testsubclasscd IN ('".$subclasscd4_implode."')";
            $query .= "                 ) t1";
            $query .= "         GROUP BY t1.receptno, t1.exam_type";
            $query .= "       ) TT1 ON TT0.receptno  = TT1.receptno ";
            $query .= "            AND TT0.exam_type = TT1.exam_type ";
            $query .= " WHERE TT0.entexamyear  = '".$model->examyear."' ";
            $query .= "   AND TT0.applicantdiv = '".$model->applicantdiv."' ";
            $query .= "   AND TT0.testdiv      = '".$model->testdiv."' ";
            $query .= "   AND TT0.exam_type    = entexam_recept_dat.exam_type ";
            $query .= "   AND TT0.receptno     = entexam_recept_dat.receptno ";
            $query .= "       )";
            $query .= " WHERE entexamyear  = '".$model->examyear."'";
            $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
            $query .= "   AND testdiv      = '".$model->testdiv."'"; 
        }

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
           $db->popExpect();
           Query::dbCheckIn($db);
           return $result->getCode();
        }


        /* 席次を求める */
        if ($model->isCollege && $model->applicantdiv == "1" && $model->testdiv == "1") {
            $query  = "UPDATE entexam_recept_dat ";
            $query .= "   SET (examno, total_rank4, div_rank4, total_rank2, div_rank2, total_rank1, div_rank1, registercd, updated) ";
            $query .= "     = ( ";
            $query .= "         SELECT TT0.examno, TT1.total_rank4, TT1.div_rank4, TT2.total_rank2, TT2.div_rank2, TT3.total_rank1, TT3.div_rank1, '".STAFFCD."',SYSDATE() ";
            $query .= "           FROM entexam_recept_dat TT0 ";
            $query .= "                LEFT JOIN ( ";
            $query .= "                 SELECT ";
            $query .= "                     R1.RECEPTNO, ";
            $query .= "                     R1.EXAM_TYPE, ";
            $query .= "                     RANK() OVER(PARTITION BY R1.TESTDIV ORDER BY R1.AVARAGE4 DESC) AS total_rank4, ";
            $query .= "                     RANK() OVER(PARTITION BY R1.TESTDIV, B1.SHDIV ORDER BY R1.AVARAGE4 DESC) AS div_rank4 ";
            $query .= "                 FROM ";
            $query .= "                     ENTEXAM_RECEPT_DAT R1 ";
            $query .= "                     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT B1 ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR AND B1.EXAMNO = R1.EXAMNO ";
            $query .= "                 WHERE ";
            $query .= "                     R1.ENTEXAMYEAR = '".$model->examyear."' ";
            $query .= "                     AND R1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "                     AND R1.TESTDIV = '".$model->testdiv."' ";
            $query .= "                     AND B1.SUB_ORDER IN ('1') "; //1:Ⅰ型(国算理)
            $query .= "                     AND R1.AVARAGE4 IS NOT NULL ";
            $query .= "                ) TT1 ON TT0.receptno  = TT1.receptno AND TT0.exam_type = TT1.exam_type ";
            $query .= "                LEFT JOIN ( ";
            $query .= "                 SELECT ";
            $query .= "                     R1.RECEPTNO, ";
            $query .= "                     R1.EXAM_TYPE, ";
            $query .= "                     RANK() OVER(PARTITION BY R1.TESTDIV ORDER BY R1.AVARAGE2 DESC) AS total_rank2, ";
            $query .= "                     RANK() OVER(PARTITION BY R1.TESTDIV, B1.SHDIV ORDER BY R1.AVARAGE2 DESC) AS div_rank2 ";
            $query .= "                 FROM ";
            $query .= "                     ENTEXAM_RECEPT_DAT R1 ";
            $query .= "                     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT B1 ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR AND B1.EXAMNO = R1.EXAMNO ";
            $query .= "                 WHERE ";
            $query .= "                     R1.ENTEXAMYEAR = '".$model->examyear."' ";
            $query .= "                     AND R1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "                     AND R1.TESTDIV = '".$model->testdiv."' ";
            $query .= "                     AND B1.SUB_ORDER IN ('1','2') "; //1:Ⅰ型(国算理) 2:Ⅱ型(国算)
            $query .= "                     AND R1.AVARAGE2 IS NOT NULL ";
            $query .= "                ) TT2 ON TT0.receptno  = TT2.receptno AND TT0.exam_type = TT2.exam_type ";
            $query .= "                LEFT JOIN ( ";
            $query .= "                 SELECT ";
            $query .= "                     R1.RECEPTNO, ";
            $query .= "                     R1.EXAM_TYPE, ";
            $query .= "                     RANK() OVER(PARTITION BY R1.TESTDIV ORDER BY R1.AVARAGE1 DESC) AS total_rank1, ";
            $query .= "                     RANK() OVER(PARTITION BY R1.TESTDIV, B1.SHDIV ORDER BY R1.AVARAGE1 DESC) AS div_rank1 ";
            $query .= "                 FROM ";
            $query .= "                     ENTEXAM_RECEPT_DAT R1 ";
            $query .= "                     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT B1 ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR AND B1.EXAMNO = R1.EXAMNO ";
            $query .= "                 WHERE ";
            $query .= "                     R1.ENTEXAMYEAR = '".$model->examyear."' ";
            $query .= "                     AND R1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "                     AND R1.TESTDIV = '".$model->testdiv."' ";
            $query .= "                     AND B1.SUB_ORDER IN ('1','2') "; //1:Ⅰ型(国算理) 2:Ⅱ型(国算)
            $query .= "                     AND R1.AVARAGE1 IS NOT NULL ";
            $query .= "                ) TT3 ON TT0.receptno  = TT3.receptno AND TT0.exam_type = TT3.exam_type ";
            $query .= "          WHERE TT0.entexamyear  = '".$model->examyear."' ";
            $query .= "            AND TT0.applicantdiv = '".$model->applicantdiv."' ";
            $query .= "            AND TT0.testdiv      = '".$model->testdiv."' ";
            $query .= "            AND TT0.exam_type    = entexam_recept_dat.exam_type ";
            $query .= "            AND TT0.receptno     = entexam_recept_dat.receptno ";
            $query .= "       ) ";
            $query .= " WHERE entexamyear  = '".$model->examyear."' ";
            $query .= "   AND applicantdiv = '".$model->applicantdiv."' ";
            $query .= "   AND testdiv      = '".$model->testdiv."' ";
        } else {
            $total_rank4 = ($model->isGojou && $model->applicantdiv == "1" || $model->isCollege && $model->applicantdiv == "2") ? "TT4.total_rank4" : "TT5.div_rank4";
            $query  = "UPDATE entexam_recept_dat ";
            $query .= "   SET (examno, total_rank4, div_rank4, registercd, updated) ";
            $query .= "     = ( ";
            $query .= "         SELECT TT0.examno, {$total_rank4}, TT5.div_rank4, '".STAFFCD."',SYSDATE() ";
            $query .= "           FROM entexam_applicantbase_dat TT9 ";
            $query .= "                INNER JOIN entexam_recept_dat TT0 ";
            $query .= "                        ON TT9.entexamyear  = TT0.entexamyear AND ";
            $query .= "                           TT9.examno       = TT0.examno  ";
            $query .= "                 LEFT OUTER JOIN  ";
            $query .= "                ( ";
            $query .= "                 SELECT T1.examno ";
            $query .= "                       ,CASE WHEN t1.total4 IS NOT NULL THEN COUNT(t2.total4)+1 END as total_rank4 ";
            $query .= "                   FROM (SELECT examno, total4 ";
            $query .= "                           FROM entexam_recept_dat ";
            $query .= "                          WHERE entexamyear  = '".$model->examyear."' ";
            $query .= "                            AND applicantdiv = '".$model->applicantdiv."'";
            $query .= "                            AND testdiv      = '".$model->testdiv."' ";
            //五條中学の場合、全科目に満たない受験者の平均点と順位は、NULLで更新
            if ($model->isGojou && $model->applicantdiv == "1") {
                $query .= "                        AND attend_all_flg = '1' ";
            }
            $query .= "                        ) t1  ";
            $query .= "                        LEFT OUTER JOIN  ";
            $query .= "                        (SELECT examno, total4 ";
            $query .= "                           FROM entexam_recept_dat ";
            $query .= "                          WHERE entexamyear  = '".$model->examyear."' ";
            $query .= "                            AND applicantdiv = '".$model->applicantdiv."'";
            $query .= "                            AND testdiv      = '".$model->testdiv."' ";
            //五條中学の場合、全科目に満たない受験者の平均点と順位は、NULLで更新
            if ($model->isGojou && $model->applicantdiv == "1") {
                $query .= "                        AND attend_all_flg = '1' ";
            }
            $query .= "                        ) t2 ON t1.total4 < t2.total4 ";
            $query .= "                  GROUP BY T1.examno, T1.total4 ";
            $query .= "                ) TT4 ON TT0.examno = TT4.examno  ";
            $query .= "                 LEFT JOIN   ";
            $query .= "                (  ";
            $query .= "                 SELECT T1.examno  ";
            $query .= "                       ,CASE WHEN t1.total4 IS NOT NULL THEN COUNT(t3.total4)+1 END as div_rank4  ";
            $query .= "                   FROM (SELECT w1.examno, w1.total4, w2.shdiv ";
            $query .= "                           FROM entexam_recept_dat w1 ";
            $query .= "                                inner join entexam_applicantbase_dat w2 ";
            $query .= "                                        on w2.entexamyear=w1.entexamyear ";
            $query .= "                                       and w2.examno=w1.examno ";
            $query .= "                          WHERE w1.entexamyear  = '".$model->examyear."'  ";
            $query .= "                            AND w1.applicantdiv = '".$model->applicantdiv."' ";
            $query .= "                            AND w1.testdiv      = '".$model->testdiv."'  ";
            //五條中学の場合、全科目に満たない受験者の平均点と順位は、NULLで更新
            if ($model->isGojou && $model->applicantdiv == "1") {
                $query .= "                        AND w1.attend_all_flg = '1' ";
            }
            $query .= "                        ) t1   ";
            $query .= "                        LEFT JOIN  ";
            $query .= "                        (SELECT w1.examno, w1.total4, w2.shdiv ";
            $query .= "                           FROM entexam_recept_dat w1 ";
            $query .= "                                inner join entexam_applicantbase_dat w2 ";
            $query .= "                                        on w2.entexamyear=w1.entexamyear ";
            $query .= "                                       and w2.examno=w1.examno ";
            $query .= "                          WHERE w1.entexamyear  = '".$model->examyear."'  ";
            $query .= "                            AND w1.applicantdiv = '".$model->applicantdiv."' ";
            $query .= "                            AND w1.testdiv      = '".$model->testdiv."'  ";
            //五條中学の場合、全科目に満たない受験者の平均点と順位は、NULLで更新
            if ($model->isGojou && $model->applicantdiv == "1") {
                $query .= "                        AND w1.attend_all_flg = '1' ";
            }
            $query .= "                        ) t3 ON t1.total4 < t3.total4 and t1.shdiv = t3.shdiv ";
            $query .= "                  GROUP BY T1.examno, T1.total4  ";
            $query .= "                ) TT5 ON TT0.examno = TT5.examno   ";
            $query .= "          WHERE TT0.entexamyear  = '".$model->examyear."' ";
            $query .= "            AND TT0.applicantdiv = '".$model->applicantdiv."' ";
            $query .= "            AND TT0.testdiv      = '".$model->testdiv."' ";
            $query .= "            AND TT0.examno       = entexam_recept_dat.examno ";
            $query .= "       ) ";
            $query .= " WHERE entexamyear  = '".$model->examyear."' ";
            $query .= "   AND applicantdiv = '".$model->applicantdiv."' ";
            $query .= "   AND testdiv      = '".$model->testdiv."' ";
        }

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }


        /*                  */
        /* 得点データの更新 */
        /*                  */
        /* 偏差値 受験科目ごとに算出 */
        $query  = "UPDATE entexam_score_dat  ";
        $query .= "   SET std_score =  ";
        $query .= "     (SELECT ROUND(50 + 10 * (T1.score - T2.avg_score) / CASE WHEN T2.std_score = 0 THEN 1 ELSE T2.std_score END,1) ";   //偏差値(小数第2位で四捨五入)
        $query .= "        FROM entexam_score_dat T1 ";
        $query .= "            ,(SELECT testsubclasscd ";
        $query .= "                    ,ROUND(STDDEV(score),1)       AS std_score ";   //標準偏差
        $query .= "                    ,ROUND(AVG(DECIMAL(score)),1) AS avg_score ";   //平均
        $query .= "                FROM entexam_score_dat ";
        $query .= "               WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                 AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "                 AND testdiv      = '".$model->testdiv."' ";
        $query .= "             GROUP BY testsubclasscd) T2 ";
        $query .= "       WHERE T1.testsubclasscd = T2.testsubclasscd ";
        $query .= "         AND T1.entexamyear    = '".$model->examyear."' ";
        $query .= "         AND T1.applicantdiv   = '".$model->applicantdiv."' ";
        $query .= "         AND T1.testdiv        = '".$model->testdiv."' ";
        $query .= "         AND T1.exam_type      = entexam_score_dat.exam_type ";
        $query .= "         AND T1.receptno       = entexam_score_dat.receptno ";
        $query .= "         AND T1.testsubclasscd = entexam_score_dat.testsubclasscd ";
        $query .= "     ), ";
        $query .= "      registercd = '".STAFFCD."', ";
        $query .= "      updated    = SYSDATE() ";
        $query .= " WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND testdiv      = '".$model->testdiv."' ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        /* 受験科目ごとの順位 */
        $query  = "UPDATE entexam_score_dat ";
        $query .= "   SET rank = (";
        $query .= "                 SELECT CASE WHEN T1.score IS NOT NULL THEN COUNT(T2.score)+1 END AS rank ";
        $query .= "                   FROM (SELECT * ";
        $query .= "                           FROM entexam_score_dat ";
        $query .= "                          WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                            AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "                            AND testdiv      = '".$model->testdiv."' ";
        $query .= "                        ) T1   ";
        $query .= "                          LEFT OUTER JOIN   ";
        $query .= "                        (SELECT * ";
        $query .= "                           FROM entexam_score_dat ";
        $query .= "                          WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "                            AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "                            AND testdiv      = '".$model->testdiv."' ";
        $query .= "                        ) T2 ON T1.testsubclasscd = T2.testsubclasscd   ";
        $query .= "                            AND T1.score          < T2.score  ";
        $query .= "                  WHERE T1.exam_type      = entexam_score_dat.exam_type ";
        $query .= "                    AND T1.receptno       = entexam_score_dat.receptno ";
        $query .= "                    AND T1.testsubclasscd = entexam_score_dat.testsubclasscd ";
        $query .= "                  GROUP BY T1.receptno, T1.score, t1.testsubclasscd ";
        $query .= "              ), ";
        $query .= "       registercd = '".STAFFCD."', ";
        $query .= "       updated    = SYSDATE() ";
        $query .= " WHERE entexamyear = '".$model->examyear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND testdiv     = '".$model->testdiv."'";

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

