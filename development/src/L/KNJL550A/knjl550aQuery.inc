<?php

require_once('for_php7.php');

class knjl550aQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namespare = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namespare) {
            $query .= " AND NAMESPARE".$namespare." = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //志望区分の取得
    public function getHopeCourse($year)
    {
        $query  = " SELECT DISTINCT";
        $query .= "   T1.HOPE_COURSECODE AS VALUE, ";
        $query .= "   T1.HOPE_COURSECODE || ':' || T1.HOPE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_HOPE_COURSE_MST T1 ";
        $query .= "   INNER JOIN ENTEXAM_HOPE_COURSE_YDAT T2 ";
        $query .= "           ON T2.HOPE_COURSECODE = T1.HOPE_COURSECODE ";
        $query .= "          AND T2.ENTEXAMYEAR     = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校名称取得
    public function getFinschoolName($year, $schoolcd)
    {
        $query  = " SELECT ";
        $query .= "     T2.FINSCHOOL_NAME_ABBV AS FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCHOOL_MST T1 ";
        $query .= "     INNER JOIN  FINSCHOOL_MST T2 ";
        $query .= "            ON T2.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR      = '".$year."' AND ";
        $query .= "     T1.ENTEXAM_SCHOOLCD = '".$schoolcd."' ";

        return $query;
    }
    
    //最初のEXAMNOを取得
    public function selectFstExamno($model)
    {
        $query  = " WITH MAXCHKTBL AS ( ";
        $query .= "   SELECT ";
        $query .= "     ROW_NUMBER() OVER(ORDER BY T1.EXAMNO) AS RENBAN, ";
        $query .= "     T1.EXAMNO ";
        $query .= "   FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T2 ";
        $query .= "       ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T2.EXAMNO = T1.EXAMNO ";
        if (strlen($model->field["DESIREDIV"])) {
            $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ";
            $query .= "             ON DETAIL.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "            AND DETAIL.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "            AND DETAIL.EXAMNO       = T1.EXAMNO ";
            $query .= "            AND DETAIL.SEQ          = '001' ";
            $query .= "            AND DETAIL.REMARK10     = '".$model->field["DESIREDIV"]."' ";
        }
        $query .= "   WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "   ORDER BY ";
        $query .= "     RENBAN ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   TX.EXAMNO ";
        $query .= " FROM ";
        $query .= "   MAXCHKTBL TX ";
        $query .= " ORDER BY ";
        $query .= "   TX.EXAMNO ";
        $query .= " FETCH FIRST ROWS ONLY ";
        return $query;
    }

    //最後のEXAMNOを取得
    public function selectLstExamno($model)
    {
        $query  = " WITH MAXCHKTBL AS ( ";
        $query .= "   SELECT ";
        $query .= "     ROW_NUMBER() OVER(ORDER BY T1.EXAMNO) AS RENBAN, ";
        $query .= "     T1.EXAMNO ";
        $query .= "   FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T2 ";
        $query .= "       ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T2.EXAMNO = T1.EXAMNO ";
        if (strlen($model->field["DESIREDIV"])) {
            $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ";
            $query .= "             ON DETAIL.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "            AND DETAIL.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "            AND DETAIL.EXAMNO       = T1.EXAMNO ";
            $query .= "            AND DETAIL.SEQ          = '001' ";
            $query .= "            AND DETAIL.REMARK10     = '".$model->field["DESIREDIV"]."' ";
        }
        $query .= "   WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "   ORDER BY ";
        $query .= "     RENBAN ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "  TW.EXAMNO ";
        $query .= " FROM ";
        $query .= "  MAXCHKTBL TW ";
        $query .= " WHERE ";
        $query .= "  TW.RENBAN = (SELECT COUNT(*) FROM MAXCHKTBL) ";
        return $query;
    }

    //一覧表示
    public function selectQuery($model, $arr_classCd)
    {
    
        $query  = " WITH ENTEXAM_SCHOOL AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.ENTEXAM_SCHOOLCD, ";
        $query .= "         T2.FINSCHOOL_NAME_ABBV AS FINSCHOOL_NAME ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SCHOOL_MST T1 ";
        $query .= "         INNER JOIN  FINSCHOOL_MST T2 ";
        $query .= "                ON T2.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR      = '".$model->ObjYear."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     R1.JUDGEDIV, ";
        foreach ($arr_classCd as $key => $cd) {
            $query .= "     S".$cd.".SCORE AS SCORE".$cd.", ";
        }
        $query .= "     D1.REMARK1, ";
        $query .= "     F1.FINSCHOOL_NAME AS FINSCHOOL_NAME1, ";
        $query .= "     D1.REMARK2, ";
        $query .= "     F2.FINSCHOOL_NAME AS FINSCHOOL_NAME2, ";
        $query .= "     D1.REMARK3, ";
        $query .= "     F3.FINSCHOOL_NAME AS FINSCHOOL_NAME3, ";
        $query .= "     D1.REMARK4,  ";
        $query .= "     F4.FINSCHOOL_NAME AS FINSCHOOL_NAME4  ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT R1 ";
        $query .= "          ON R1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND R1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND R1.TESTDIV      = B1.TESTDIV ";
        $query .= "         AND R1.EXAM_TYPE    = '1' ";
        $query .= "         AND R1.RECEPTNO     = B1.EXAMNO ";
        if (strlen($model->field["DESIREDIV"])) {
            $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ";
            $query .= "             ON DETAIL.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
            $query .= "            AND DETAIL.APPLICANTDIV = B1.APPLICANTDIV ";
            $query .= "            AND DETAIL.EXAMNO       = B1.EXAMNO ";
            $query .= "            AND DETAIL.SEQ          = '001' ";
            $query .= "            AND DETAIL.REMARK10     = '".$model->field["DESIREDIV"]."' ";
        }
        foreach ($arr_classCd as $key => $cd) {
            $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT S".$cd." ";
            $query .= "          ON S".$cd.".ENTEXAMYEAR    = B1.ENTEXAMYEAR ";
            $query .= "         AND S".$cd.".APPLICANTDIV   = B1.APPLICANTDIV ";
            $query .= "         AND S".$cd.".TESTDIV        = B1.TESTDIV ";
            $query .= "         AND S".$cd.".EXAM_TYPE      = '1' ";
            $query .= "         AND S".$cd.".RECEPTNO       = B1.EXAMNO ";
            $query .= "         AND S".$cd.".TESTSUBCLASSCD = '".$cd."' ";
        }
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D1 ";
        $query .= "          ON D1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND D1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND D1.EXAMNO       = B1.EXAMNO ";
        $query .= "         AND D1.SEQ          = '005' ";
        $query .= "     LEFT JOIN ENTEXAM_SCHOOL F1 ";
        $query .= "          ON F1.ENTEXAM_SCHOOLCD  = D1.REMARK1 ";
        $query .= "     LEFT JOIN ENTEXAM_SCHOOL F2 ";
        $query .= "          ON F2.ENTEXAM_SCHOOLCD  = D1.REMARK2 ";
        $query .= "     LEFT JOIN ENTEXAM_SCHOOL F3 ";
        $query .= "          ON F3.ENTEXAM_SCHOOLCD  = D1.REMARK3 ";
        $query .= "     LEFT JOIN ENTEXAM_SCHOOL F4 ";
        $query .= "          ON F4.ENTEXAM_SCHOOLCD  = D1.REMARK4 ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND B1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND B1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        if ($model->cmd == "search" || $model->hoji_cmd == "search") {
            if ($model->field["S_EXAMNO"] != "") {
                $query .= "   AND B1.EXAMNO >= '".$model->field["S_EXAMNO"]."' ";
                if ($model->field["E_EXAMNO"] != "") {
                    $query .= "   AND B1.EXAMNO <= '".$model->field["E_EXAMNO"]."' ";
                }
            }
        } elseif ($model->cmd == "back" || $model->hoji_cmd == "back") {
            if ($model->field["S_EXAMNO"] != "") {
                $query .= "   AND B1.EXAMNO >= '".$model->field["S_EXAMNO"]."' - 50 ";
                $query .= "   AND B1.EXAMNO < '".$model->field["S_EXAMNO"]."' ";
            }
        } elseif ($model->cmd == "next" || $model->hoji_cmd == "next") {
            $query .= "   AND B1.EXAMNO > '".$model->field["E_EXAMNO"]."' ";
        }
        $query .= " ORDER BY ";
        if ($model->field["SORT"] == "2") {
            $query .= "     B1.NAME, ";
        }
        $query .= "     B1.EXAMNO ";
        $query .= " FETCH FIRST 50 ROWS ONLY ";

        return $query;
    }

    //存在チェック ENTEXAM_APPLICANTBASE_DETAIL_DAT
    public function checkEntexamApplicantbaseDetailDat($model, $seq)
    {
        $query  = " SELECT ";
        $query .= "     EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     SEQ             = '".$seq."' ";

        return $query;
    }

    //存在チェック ENTEXAM_APPLICANTBASE_DETAIL_DAT
    public function checkEntexamScoreDat($model, $examno, $classcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$model->field["TESTDIV"]."' AND ";
        $query .= "     EXAM_TYPE       = '1' AND ";
        $query .= "     RECEPTNO        = '".$examno."' AND ";
        $query .= "     TESTSUBCLASSCD  = '".$classcd."' ";

        return $query;
    }

    //更新
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //存在チェック ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $D005      = $db->getCol(knjl550aQuery::checkEntexamApplicantbaseDetailDat($model, "005"));

        //更新
        $array = explode(",", $model->hid_examno);
        foreach ($array as $key => $examno) {
            // ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
            $where .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
            $where .= "     EXAMNO          = '".$examno."' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);


            // ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ:005
            $data = array();
            if (!in_array($examno, $D005)) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $data["SEQ"][TEXT]              = '005';
            }
            $data["REMARK1"][TEXT]          = !strlen($model->data["REMARK1"][$examno]) ? "" : $model->data["REMARK1"][$examno];
            $data["REMARK2"][TEXT]          = !strlen($model->data["REMARK2"][$examno]) ? "" : $model->data["REMARK2"][$examno];
            $data["REMARK3"][TEXT]          = !strlen($model->data["REMARK3"][$examno]) ? "" : $model->data["REMARK3"][$examno];
            $data["REMARK4"][TEXT]          = !strlen($model->data["REMARK4"][$examno]) ? "" : $model->data["REMARK4"][$examno];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if (!in_array($examno, $D005)) {
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
                $where .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
                $where .= "     EXAMNO          = '".$examno."' AND ";
                $where .= "     SEQ             = '005' ";

                //更新
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);


            // ENTEXAM_SCORE_DAT
            $arr_classCd = explode(",", $model->hid_classcd);
            foreach ($arr_classCd as $key => $classcd) {
                //存在チェック ENTEXAM_SCORE_DAT
                $chkCnt = $db->getOne(knjl550aQuery::checkEntexamScoreDat($model, $examno, $classcd));

                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
                $data["EXAM_TYPE"][TEXT]        = '1';
                $data["RECEPTNO"][TEXT]         = $examno;
                $data["TESTSUBCLASSCD"][TEXT]   = $classcd;
                $data["SCORE"][TEXT]            = !strlen($model->data["SCORE".$classcd][$examno]) ? "" : $model->data["SCORE".$classcd][$examno];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";
                
                if ($chkCnt == 0) {
                    //追加
                    $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
                } else {
                    $where  = " WHERE ";
                    $where .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
                    $where .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
                    $where .= "     TESTDIV         = '".$model->field["TESTDIV"]."' AND ";
                    $where .= "     EXAM_TYPE       = '1' AND ";
                    $where .= "     RECEPTNO        = '".$examno."' AND ";
                    $where .= "     TESTSUBCLASSCD  = '".$classcd."' ";
                    
                    //更新
                    $query = Query::updateSQL($data, "ENTEXAM_SCORE_DAT", $where);
                }
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //併願校情報データ取込
    public function updateQueryCsvHeigan($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //存在チェック ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $D005      = $db->getCol(knjl550aQuery::checkEntexamApplicantbaseDetailDat($model, "005"));

        $cnt = 0;   //処理件数
        for ($i = 0; $i < count($data_arr); $i++) {
            $examno = $data_arr[$i]["EXAMNO"];

            // ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ:005
            $data = array();
            if (!in_array($examno, $D005)) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $data["SEQ"][TEXT]              = '005';
            }
            for ($j = 1; $j <= 4; $j++) {
                if (!$data_arr[$i]["SCHOOLCD_ERROR"][$j]) {
                    $data["REMARK{$j}"][TEXT]          = !strlen($data_arr[$i]["SCHOOLCD{$j}"]) ? "" : $data_arr[$i]["SCHOOLCD{$j}"];
                }
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if (!in_array($examno, $D005)) {
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
                $where .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
                $where .= "     EXAMNO          = '".$examno."' AND ";
                $where .= "     SEQ             = '005' ";

                //更新
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }

            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //受験番号
    public function checkExamNo($model, $examNo)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND EXAMNO       = '".$examNo."' ";

        return $query;
    }

    //学校コード
    public function checkSchoolcd($model, $schoolcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCHOOL_MST T1 ";
        $query .= "     INNER JOIN  FINSCHOOL_MST T2 ";
        $query .= "         ON T2.FINSCHOOLCD = T1.FINSCHOOLCD  ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR       = '".$model->ObjYear."' ";
        $query .= "     AND T1.ENTEXAM_SCHOOLCD  = '".$schoolcd."' ";

        return $query;
    }

    //欠席
    public function checkKesseki($model, $examNo)
    {
        $query  = " SELECT ";
        $query .= "     R1.JUDGEDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     INNER JOIN  ENTEXAM_RECEPT_DAT R1 ";
        $query .= "          ON R1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND R1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND R1.TESTDIV      = B1.TESTDIV ";
        $query .= "         AND R1.EXAM_TYPE    = '1' ";
        $query .= "         AND R1.RECEPTNO     = B1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND B1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND B1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND B1.EXAMNO       = '".$examNo."' ";

        return $query;
    }

    /********************/
    /**  エラー処理用  **/
    /********************/

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query  = " SELECT ";
        $query .= "     MSGROW, ";
        $query .= "     MSGREMARK ";
        $query .= " FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }
}
