<?php

require_once('for_php7.php');

class knjl691iQuery extends Query
{

    //入試区分
    public function getTestdivMst($year, $applicantdiv, $testdiv = "", $testdivName = "")
    {
        $query  = " SELECT ";
        if ($testdivName == "") {
            $query .= "     TESTDIV AS VALUE, ";
            $query .= "     TESTDIV || ':' || VALUE(TESTDIV_NAME, '') AS LABEL, ";
        }
        $query .= "     TESTDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$year."' ";
        $query .= " AND APPLICANTDIV = '".$applicantdiv."' ";
        if ($testdiv) {
            $query .= " AND TESTDIV = '".$testdiv."' ";
        }
        if ($testdivName == "") {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }
    
    //更新対象データ
    public function getSelectQuery($examyear, $applicantdiv, $testdiv0, $testdiv)
    {
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T2.EXAMNO AS BOSYU_NO, ";
        $query .= "     T3.REMARK1 AS TOKUTAI_RIYUU, ";
        $query .= "     T3.REMARK2 AS TOKUTAI_MARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     INNER JOIN ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_DAT T2 ";
        $query .= "              ON T2.FS_CD = T1.FS_CD ";
        $query .= "             AND T2.NAME = T1.NAME ";
        $query .= "     LEFT JOIN ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT T3 ";
        $query .= "              ON T3.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "             AND T3.EXAMNO = T2.EXAMNO ";
        $query .= "             AND T3.SEQ = '004' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$examyear}' AND ";
        $query .= "     T1.APPLICANTDIV = '{$applicantdiv}' AND ";
        $query .= "     T1.TESTDIV0 = '{$testdiv0}' AND ";
        $query .= "     T1.TESTDIV = '{$testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";
        
        return $query;
    }

    //存在チェック
    public function getExistsDetail($examyear, $applicantdiv, $examno, $seq, $target)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     {$target} ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$examyear}' AND ";
        $query .= "     APPLICANTDIV = '{$applicantdiv}' AND ";
        $query .= "     EXAMNO = '{$examno}' AND ";
        $query .= "     SEQ = '{$seq}' ";
            
        return $query;
    }

    //-----------------------------------
    //実行
    //-----------------------------------
    public function exec($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
      
        //処理件数取得
        $model->match = $db->getOne(knjl691iQuery::getMatchCount($model->examyear, $model->applicantdiv, $model->testdiv0, $model->testdiv));
        $model->unmatch1 = $db->getOne(knjl691iQuery::getUnMatchCount1($model->examyear, $model->applicantdiv, $model->testdiv0, $model->testdiv));
        $model->unmatch2 = $db->getOne(knjl691iQuery::getUnMatchCount2($model->examyear, $model->applicantdiv, $model->testdiv0, $model->testdiv));

        if ($model->match > 0) {
            $query = knjl691iQuery::getSelectQuery($model->examyear, $model->applicantdiv, $model->testdiv0, $model->testdiv);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

                // ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ:002
                $seq = "002";

                $data = array();
                $data["ENTEXAMYEAR"][TEXT]    = $row["ENTEXAMYEAR"];
                $data["APPLICANTDIV"][TEXT]   = $row["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]         = $row["EXAMNO"];
                $data["SEQ"][TEXT]            = $seq;
                $data["REMARK1"][TEXT]        = $row["BOSYU_NO"]; //募集番号

                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "SYSDATE()";

                $getCount = $db->getOne(knjl691iQuery::getExistsDetail($model->examyear, $model->applicantdiv, $row["EXAMNO"], $seq, "ENTEXAM_APPLICANTBASE_DETAIL_DAT"));
                if ($getCount > 0) {
                    $where  = " WHERE ";
                    $where .= "     ENTEXAMYEAR   = '".$row["ENTEXAMYEAR"]."' AND ";
                    $where .= "     APPLICANTDIV  = '".$row["APPLICANTDIV"]."' AND ";
                    $where .= "     EXAMNO        = '".$row["EXAMNO"]."' AND ";
                    $where .= "     SEQ           = $seq ";

                    //更新
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                } else {
                    //追加
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                }
                $db->query($query);

                // ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT SEQ:009
                $seq = "009";

                $data = array();
                $data["ENTEXAMYEAR"][TEXT]    = $row["ENTEXAMYEAR"];
                $data["APPLICANTDIV"][TEXT]   = $row["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]         = $row["EXAMNO"];
                $data["SEQ"][TEXT]            = $seq;
                $data["REMARK2"][TEXT]        = $row["TOKUTAI_MARK"];  //特待記号
                $data["REMARK3"][TEXT]        = $row["TOKUTAI_RIYUU"]; //特待理由

                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "SYSDATE()";

                $getCount = $db->getOne(knjl691iQuery::getExistsDetail($model->examyear, $model->applicantdiv, $row["EXAMNO"], $seq, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT"));
                if ($getCount > 0) {
                    $where  = " WHERE ";
                    $where .= "     ENTEXAMYEAR   = '".$row["ENTEXAMYEAR"]."' AND ";
                    $where .= "     APPLICANTDIV  = '".$row["APPLICANTDIV"]."' AND ";
                    $where .= "     EXAMNO        = '".$row["EXAMNO"]."' AND ";
                    $where .= "     SEQ           = $seq ";

                    //更新
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
                } else {
                    //追加
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
                }
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //マッチ件数取得
    public function getMatchCount($examyear, $applicantdiv, $testdiv0, $testdiv)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     INNER JOIN ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_DAT T2 ";
        $query .= "              ON T2.FS_CD = T1.FS_CD ";
        $query .= "             AND T2.NAME = T1.NAME ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$examyear}' AND ";
        $query .= "     T1.APPLICANTDIV = '{$applicantdiv}' AND ";
        $query .= "     T1.TESTDIV0     = '{$testdiv0}' AND ";
        $query .= "     T1.TESTDIV      = '{$testdiv}' ";
    
        return $query;
    }

    //アンマッチ件数取得（願書）
    public function getUnMatchCount1($examyear, $applicantdiv, $testdiv0, $testdiv)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$examyear}' AND ";
        $query .= "     T1.APPLICANTDIV = '{$applicantdiv}' AND ";
        $query .= "     T1.TESTDIV0     = '{$testdiv0}' AND ";
        $query .= "     T1.TESTDIV      = '{$testdiv}' AND ";
        $query .= "     T1.EXAMNO NOT IN ( ";
        $query .= "                       SELECT ";
        $query .= "                           T1.EXAMNO ";
        $query .= "                       FROM ";
        $query .= "                           ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "                           INNER JOIN ";
        $query .= "                               ENTEXAM_RECRUIT_ADVICE_DAT T2 ";
        $query .= "                                    ON T2.FS_CD = T1.FS_CD ";
        $query .= "                                   AND T2.NAME = T1.NAME ";
        $query .= "                       WHERE ";
        $query .= "                           T1.ENTEXAMYEAR  = '{$examyear}' AND ";
        $query .= "                           T1.APPLICANTDIV = '{$applicantdiv}' AND ";
        $query .= "                           T1.TESTDIV0     = '{$testdiv0}' AND ";
        $query .= "                           T1.TESTDIV      = '{$testdiv}' ";
        $query .= "                      ) ";
        
        return $query;
    }

    //アンマッチ件数取得（募集）
    public function getUnMatchCount2($examyear, $applicantdiv, $testdiv0, $testdiv)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECRUIT_ADVICE_DAT T1  ";
        $query .= "     INNER JOIN ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ001 ";
        $query .= "              ON SEQ001.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND SEQ001.EXAMNO = T1.EXAMNO ";
        $query .= "             AND SEQ001.SEQ = '001' ";
        $query .= "             AND SEQ001.REMARK1 = '{$testdiv}' ";
        $query .= "     INNER JOIN ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ002 ";
        $query .= "              ON SEQ002.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND SEQ002.EXAMNO = T1.EXAMNO ";
        $query .= "             AND SEQ002.SEQ = '002' ";
        $query .= "     INNER JOIN ";
        $query .= "         ENTEXAM_GENERAL_MST GENE02 ";
        $query .= "              ON GENE02.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND GENE02.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "             AND GENE02.TESTDIV = '0' ";
        $query .= "             AND GENE02.GENERAL_DIV = '02' ";
        $query .= "             AND GENE02.GENERAL_CD = SEQ002.REMARK1 ";
        $query .= "             AND GENE02.REMARK1 = '{$testdiv0}' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$examyear}' ";
        $query .= "     AND T1.EXAMNO NOT IN ( ";
        $query .= "             SELECT ";
        $query .= "                 T2.EXAMNO ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "                 INNER JOIN ";
        $query .= "                     ENTEXAM_RECRUIT_ADVICE_DAT T2 ";
        $query .= "                          ON T2.FS_CD = T1.FS_CD ";
        $query .= "                         AND T2.NAME = T1.NAME ";
        $query .= "             WHERE ";
        $query .= "                 T1.ENTEXAMYEAR = '{$examyear}' AND ";
        $query .= "                 T1.TESTDIV = '{$testdiv}' AND ";
        $query .= "                 T1.TESTDIV0 = '{$testdiv0}' ";
        $query .= "         ) ";
        return $query;
    }

    //願書から募集データ取得
    public function getCsvData1($examyear, $applicantdiv, $testdiv0, $testdiv)
    {
        $query .= " SELECT ";
        $query .= "     T1.EXAMNO AS SHIGAN_EXAMNO, ";
        $query .= "     T1.NAME AS SHIGAN_NAME, ";
        $query .= "     T1.NAME_KANA AS SHIGAN_KANA, ";
        $query .= "     T1.FS_CD AS SHIGAN_FS_CD, ";
        $query .= "     SHIGAN_SCHOOL.FINSCHOOL_NAME_ABBV AS SHIGAN_SCHOOL, ";
        $query .= "     CASE WHEN T1.SEX = '1' THEN '男' ELSE '女' END AS SHIGAN_SEX, ";
        $query .= "     T2.EXAMNO AS BOSYU_EXAMNO, ";
        $query .= "     GENE04.GENERAL_NAME AS TOKUTAI, ";
        $query .= "     T2.NAME AS BOSYU_NAME, ";
        $query .= "     SEQ002.REMARK1 AS SOUDAN, ";
        $query .= "     T2.FS_CD AS BOSYU_FS_CD, ";
        $query .= "     BOSYU_SCHOOL.FINSCHOOL_NAME_ABBV AS BOSYU_SCHOOL, ";
        $query .= "     SEQ003.REMARK1 AS COMMON_TEST, ";
        $query .= "     SEQ008.REMARK1 AS KESSEKI, ";
        $query .= "     SEQ008.REMARK2 AS KESSEKI_RIYUU, ";
        $query .= "     SEQ005.REMARK1 AS BUKATSUNO, ";
        $query .= "     SEQ006.REMARK1 AS BIKOU ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ";
        $query .= "         FINSCHOOL_MST SHIGAN_SCHOOL ";
        $query .= "              ON SHIGAN_SCHOOL.FINSCHOOLCD = T1.FS_CD ";
        $query .= "     LEFT JOIN ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_DAT T2 ";
        $query .= "              ON T2.FS_CD = T1.FS_CD ";
        $query .= "             AND T2.NAME = T1.NAME ";
        $query .= "     LEFT JOIN ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ004 ON SEQ004.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "             AND SEQ004.EXAMNO = T2.EXAMNO ";
        $query .= "             AND SEQ004.SEQ = '004' ";
        $query .= "     LEFT JOIN ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ002 ON SEQ002.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "             AND SEQ002.EXAMNO = T2.EXAMNO ";
        $query .= "             AND SEQ002.SEQ = '002' ";
        $query .= "     LEFT JOIN ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ003 ON SEQ003.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "             AND SEQ003.EXAMNO = T2.EXAMNO ";
        $query .= "             AND SEQ003.SEQ = '003' ";
        $query .= "     LEFT JOIN ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ008 ON SEQ008.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "             AND SEQ008.EXAMNO = T2.EXAMNO ";
        $query .= "             AND SEQ008.SEQ = '008' ";
        $query .= "     LEFT JOIN ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ005 ON SEQ005.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "             AND SEQ005.EXAMNO = T2.EXAMNO ";
        $query .= "             AND SEQ005.SEQ = '005' ";
        $query .= "     LEFT JOIN ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ006 ON SEQ006.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "             AND SEQ006.EXAMNO = T2.EXAMNO ";
        $query .= "             AND SEQ006.SEQ = '006' ";
        $query .= "     LEFT JOIN ";
        $query .= "         ENTEXAM_GENERAL_MST GENE02 ";
        $query .= "              ON GENE02.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND GENE02.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND GENE02.TESTDIV = '0' ";
        $query .= "             AND GENE02.GENERAL_DIV = '02' ";
        $query .= "             AND GENE02.GENERAL_CD = SEQ002.REMARK1 ";
        $query .= "     LEFT JOIN ";
        $query .= "         ENTEXAM_GENERAL_MST GENE04 ";
        $query .= "              ON GENE04.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND GENE04.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND GENE04.TESTDIV = '0' ";
        $query .= "             AND GENE04.GENERAL_DIV = '04' ";
        $query .= "             AND GENE04.GENERAL_CD = SEQ004.REMARK2 ";
        $query .= "     LEFT JOIN ";
        $query .= "         ENTEXAM_GENERAL_MST GENE05 ";
        $query .= "              ON GENE05.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND GENE05.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND GENE05.TESTDIV = '0' ";
        $query .= "             AND GENE05.GENERAL_DIV = '05' ";
        $query .= "             AND GENE05.GENERAL_CD = SEQ004.REMARK1 ";
        $query .= "     LEFT JOIN ";
        $query .= "         FINSCHOOL_MST BOSYU_SCHOOL ";
        $query .= "              ON BOSYU_SCHOOL.FINSCHOOLCD = T2.FS_CD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$examyear}' AND ";
        $query .= "     T1.APPLICANTDIV = '{$applicantdiv}' AND ";
        $query .= "     T1.TESTDIV0 = '{$testdiv0}' AND ";
        $query .= "     T1.TESTDIV = '{$testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //紐づかない募集データ
    public function getCsvData2($examyear, $applicantdiv, $testdiv0, $testdiv)
    {
        $query .= " SELECT ";
        $query .= "     '' AS EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.FS_CD AS FS_CD, ";
        $query .= "     SCHOOL.FINSCHOOL_NAME_ABBV AS SCHOOL, ";
        $query .= "     '' AS SEX, ";
        $query .= "     GENE04.GENERAL_NAME AS TOKUTAI, ";
        $query .= "     SEQ002.REMARK1 AS SOUDAN, ";
        $query .= "     SEQ003.REMARK1 AS BOSYU_TEST, ";
        $query .= "     SEQ008.REMARK1 AS BOSYU_KESSEKI, ";
        $query .= "     SEQ008.REMARK2 AS BOSYU_KESSEKI_RIYUU, ";
        $query .= "     SEQ005.REMARK1 AS BOSYU_BUKATSUNO, ";
        $query .= "     SEQ006.REMARK1 AS BOSYU_BIKOU ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECRUIT_ADVICE_DAT T1  ";
        $query .= "     INNER JOIN ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ001 ";
        $query .= "              ON SEQ001.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND SEQ001.EXAMNO = T1.EXAMNO ";
        $query .= "             AND SEQ001.SEQ = '001' ";
        $query .= "             AND SEQ001.REMARK1 = '{$testdiv}' ";
        $query .= "     INNER JOIN ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ002 ";
        $query .= "              ON SEQ002.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND SEQ002.EXAMNO = T1.EXAMNO ";
        $query .= "             AND SEQ002.SEQ = '002' ";
        $query .= "     INNER JOIN ";
        $query .= "         ENTEXAM_GENERAL_MST GENE02 ";
        $query .= "              ON GENE02.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND GENE02.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "             AND GENE02.TESTDIV = '0' ";
        $query .= "             AND GENE02.GENERAL_DIV = '02' ";
        $query .= "             AND GENE02.GENERAL_CD = SEQ002.REMARK1 ";
        $query .= "             AND GENE02.REMARK1 = '{$testdiv0}' ";
        $query .= "     LEFT JOIN ";
        $query .= "         FINSCHOOL_MST SCHOOL ";
        $query .= "              ON SCHOOL.FINSCHOOLCD = T1.FS_CD  ";
        $query .= "     LEFT JOIN ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ004 ";
        $query .= "              ON SEQ004.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND SEQ004.EXAMNO = T1.EXAMNO ";
        $query .= "             AND SEQ004.SEQ = '004' ";
        $query .= "     LEFT JOIN ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ003 ";
        $query .= "              ON SEQ003.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND SEQ003.EXAMNO = T1.EXAMNO ";
        $query .= "             AND SEQ003.SEQ = '003' ";
        $query .= "     LEFT JOIN ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ008 ";
        $query .= "              ON SEQ008.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND SEQ008.EXAMNO = T1.EXAMNO ";
        $query .= "             AND SEQ008.SEQ = '008' ";
        $query .= "     LEFT JOIN ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ005 ";
        $query .= "              ON SEQ005.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND SEQ005.EXAMNO = T1.EXAMNO ";
        $query .= "             AND SEQ005.SEQ = '005' ";
        $query .= "     LEFT JOIN ";
        $query .= "         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ006 ";
        $query .= "              ON SEQ006.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND SEQ006.EXAMNO = T1.EXAMNO ";
        $query .= "             AND SEQ006.SEQ = '006' ";
        $query .= "     LEFT JOIN ";
        $query .= "         ENTEXAM_GENERAL_MST GENE04 ";
        $query .= "             ON GENE04.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "            AND GENE04.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "            AND GENE04.TESTDIV = '0' ";
        $query .= "            AND GENE04.GENERAL_DIV = '04' ";
        $query .= "            AND GENE04.GENERAL_CD = SEQ004.REMARK2 ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST GENE05 ";
        $query .= "             ON GENE05.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "            AND GENE05.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "            AND GENE05.TESTDIV = '0' ";
        $query .= "            AND GENE05.GENERAL_DIV = '05' ";
        $query .= "            AND GENE05.GENERAL_CD = SEQ004.REMARK1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$examyear}' ";
        $query .= "     AND T1.EXAMNO NOT IN ( ";
        $query .= "             SELECT ";
        $query .= "                 T2.EXAMNO ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "                 INNER JOIN ";
        $query .= "                     ENTEXAM_RECRUIT_ADVICE_DAT T2 ";
        $query .= "                          ON T2.FS_CD = T1.FS_CD ";
        $query .= "                         AND T2.NAME = T1.NAME ";
        $query .= "             WHERE ";
        $query .= "                 T1.ENTEXAMYEAR = '{$examyear}' AND ";
        $query .= "                 T1.TESTDIV = '{$testdiv}' AND ";
        $query .= "                 T1.TESTDIV0 = '{$testdiv0}' ";
        $query .= "         ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";
        return $query;
    }
}
