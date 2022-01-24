<?php

require_once('for_php7.php');

class knjl011hQuery extends Query 
{

    //出身学校マスタよりデータを取得
    function getFinschoolName($cd)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT  T1.FINSCHOOL_NAME, T1.DISTRICTCD ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //出身学校マスタよりデータを取得
    function getFinschoolcd($year, $cd2)
    {
        $query  = " SELECT  T1.FINSCHOOLCD, T1.FINSCHOOL_NAME, ";
        $query .= "         T1.FINSCHOOL_DISTCD, N1.NAME1 AS FINSCHOOL_DISTCD_NAME ";
        $query .= "   FROM  FINSCHOOL_YDAT T2, ";
        $query .= "         FINSCHOOL_MST T1 ";
        $query .= "         LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L001' AND N1.NAMECD2=T1.FINSCHOOL_DISTCD ";
        $query .= "  WHERE  T2.YEAR = '".$year."' ";
        $query .= "    AND  T2.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= "    AND  T2.FINSCHOOLCD LIKE '".$cd2."%' ";
        $query .= " ORDER BY T1.FINSCHOOLCD ";
        return $query;
    }

    //塾マスタよりデータを取得
    function getPrischoolcd($year)
    {
        $query  = " SELECT  T1.PRISCHOOLCD, T1.PRISCHOOL_NAME ";
        $query .= "   FROM  PRISCHOOL_MST T1, ";
        $query .= "         PRISCHOOL_YDAT T2 ";
        $query .= "  WHERE  T2.YEAR = '".$year."' ";
        $query .= "    AND  T2.PRISCHOOLCD = T1.PRISCHOOLCD ";
        $query .= " ORDER BY T1.PRISCHOOLCD ";
        return $query;
    }

    //名称マスタよりデータを取得
    function get_name_cd($year, $cd)
    {
        $query = "  SELECT
                        NAMECD2,
                        NAME1
                       ,NAMESPARE2
                    FROM
                        V_NAME_MST
                    WHERE
                        YEAR = '".$year."' AND
                        NAMECD1 = '".$cd."'
                 ";
        return $query;
    }

    //名称マスタより和暦の元号を取得
    function get_calendarno($year)
    {
        $query = "  SELECT
                        NAMECD2,
                        NAME1,
                        NAMESPARE1,
                        NAMESPARE2,
                        NAMESPARE3
                    FROM
                        V_NAME_MST
                    WHERE
                        YEAR = '".$year."' AND
                        NAMECD1 = 'L007'
                 ";

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        T1.APPLICANTDIV,
                        T1.EXAMNO,
                        T1.TESTDIV1,
                        T1.TESTDIV2,
                        T1.TESTDIV3,
                        T1.TESTDIV4,
                        T1.NAME,
                        T1.NAME_KANA,
                        T1.SEX,
                        T1.ERACD,
                        T1.BIRTH_Y,
                        T1.BIRTH_M,
                        T1.BIRTH_D,
                        T1.BIRTHDAY,
                        T1.FS_CD,
                        T1.FS_NAME,
                        T1.FS_AREA_CD,
                        T1.FS_NATPUBPRIDIV,
                        T1.FS_GRDYEAR,
                        T1.PRISCHOOLCD,
                        T1.REMARK1,
                        T1.REMARK2,
                        T1.RECOM_EXAMNO,
                        T2.ZIPCD,
                        T2.ADDRESS1,
                        T2.ADDRESS2,
                        T2.TELNO,
                        T2.GNAME,
                        T2.GKANA,
                        T2.GZIPCD,
                        T2.GADDRESS1,
                        T2.GADDRESS2,
                        T2.GTELNO,
                        T3.NAME1,
                        T4.confidential_rpt01,T4.confidential_rpt02,T4.confidential_rpt03,T4.confidential_rpt04,T4.confidential_rpt05,T4.confidential_rpt06,
                        T4.confidential_rpt07,T4.confidential_rpt08,T4.confidential_rpt09,T4.confidential_rpt10,T4.confidential_rpt11,T4.confidential_rpt12,
                        T4.TOTAL_ALL,T4.KASANTEN_ALL,
                        T4.average5,T4.average_all,T4.absence_days
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT T1
                    LEFT OUTER JOIN
                        ENTEXAM_APPLICANTADDR_DAT T2
                    ON
                        T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND
                        T1.EXAMNO = T2.EXAMNO
                    LEFT OUTER JOIN
                        V_NAME_MST T3
                    ON
                        T1.ENTEXAMYEAR = T3.YEAR AND
                        T1.ERACD = T3.NAMECD2 AND
                        T3.NAMECD1 = 'L007'
                    LEFT OUTER JOIN 
                        entexam_applicantconfrpt_dat T4
                    ON
                        T1.entexamyear = T4.entexamyear AND
                        T1.examno      = T4.examno
                    WHERE
                        T1.ENTEXAMYEAR = '".$model->year."' AND ";

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= " T1.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= " T1.EXAMNO > '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO ASC ";
        } else {
            $query.= " T1.EXAMNO = '".$model->examno."'";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //データ重複チェック
    function CheckData($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                       T1.EXAMNO,
                       T2.NAME1
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT T1
                    LEFT OUTER JOIN
                        V_NAME_MST T2
                    ON
                        T1.ENTEXAMYEAR = T2.YEAR AND
                        T1.APPLICANTDIV = T2.NAMECD2 AND
                        T2.NAMECD1 = 'L003'
                    WHERE
                        ENTEXAMYEAR = '".$model->year."' AND
                        EXAMNO = '".$model->examno."' AND
                        APPLICANTDIV <> '".$model->applicantdiv."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //存在チェック
    function Existdata($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        EXAMNO
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT
                    WHERE
                        ENTEXAMYEAR = '".$model->year."' AND     
                        EXAMNO = '".$model->examno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //該当日付取得
    function CheckDate($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        NAME1,
                        NAMESPARE1,
                        NAMESPARE2,
                        NAMESPARE3
                    FROM
                        V_NAME_MST
                    WHERE
                        YEAR = '".$model->year."' AND
                        NAMECD1 = 'L007' AND
                        NAMECD2 = '".$model->field["ERACD"]. "'";
        
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //推薦受験番号の存在チェック
    function checkRecomExamno($model, $recomExamno)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT COUNT(*) as CNT ";
        $query .= "  FROM entexam_applicantbase_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->year."' ";
        $query .= "   AND T1.applicantdiv = '3' ";
        $query .= "   AND T1.EXAMNO = '".$recomExamno."' ";
        $retCnt = $db->getOne($query);

        Query::dbCheckIn($db);
        return $retCnt;
    }

    //推薦受験番号の重複チェック（既に同じ推薦受験番号が登録されていないか？）
    function checkRecomExamno2($model, $recomExamno, $examno)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT COUNT(*) as CNT ";
        $query .= "  FROM entexam_applicantbase_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->year."' ";
        $query .= "   AND T1.applicantdiv = '2' ";
        $query .= "   AND T1.EXAMNO != '".$examno."' ";
        $query .= "   AND T1.RECOM_EXAMNO = '".$recomExamno."' ";
        $retCnt = $db->getOne($query);

        Query::dbCheckIn($db);
        return $retCnt;
    }



    //--- INSERT
    //--- UPDATE
    function &getInsertUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //高校一般のみ。
        //推薦受験番号を入力あるいは削除した場合、加算点を再計算。
        //この処理は、内申点入力、加算計算後に再度推薦受験番号をメンテナンスすることの対応。
        if ($model->field["APPLICANTDIV"] == "2") {
            //推薦受験番号を取得
            $query  = " SELECT ";
            $query .= "     RECOM_EXAMNO ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '".$model->year."' AND ";
            $query .= "     EXAMNO = '".$model->examno."' ";
            $recomExamno = $db->getOne($query);
            //修正前後の推薦受験番号を比較
            if (($recomExamno == "" && $model->field["RECOM_EXAMNO"] != "") || 
                ($recomExamno != "" && $model->field["RECOM_EXAMNO"] == "")) {
                //加算点を取得
                $query  = " SELECT ";
                $query .= "     KASANTEN_ALL ";
                $query .= " FROM ";
                $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT ";
                $query .= " WHERE ";
                $query .= "     ENTEXAMYEAR = '".$model->year."' AND ";
                $query .= "     EXAMNO = '".$model->examno."' ";
                $kasan = $db->getOne($query);
                //加算点を再計算
                if ($kasan != null) {
                    $tmpKasan = ($model->field["RECOM_EXAMNO"] != "") ? ($kasan + 10) : ($kasan - 10);

                    $data = array();
                    $data["KASANTEN_ALL"][NUMBER] = $tmpKasan;

                    $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND";
                    $where .= "       EXAMNO = '".$model->examno."'";

                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
                    $db->query($query);
                }
            }
        }


        $result = $db->query(knjl011hQuery::get_calendarno($model->year));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $eracd[] = $row["NAMECD2"];
            $defyear[] = $row["NAMESPARE1"];
            $defsdate[] = $row["NAMESPARE2"];
            $defedate[] = $row["NAMESPARE3"];
        }
        $result->free();

        $w_eracd = $w_year = $mon = $day = "";
        if (strlen($model->field["BIRTHDAY"])) {
            list($s_year, $mon, $day) = explode("/", $model->field["BIRTHDAY"]);
            for ($i=0; $i<get_count($eracd); $i++) {
                if ($defsdate[$i] <= $model->field["BIRTHDAY"] && $model->field["BIRTHDAY"] <= $defedate[$i]) {
                    $w_eracd = $eracd[$i];
                    $w_year = ((int)$s_year + 1) - ((int)$defyear[$i]);
                    $w_year = sprintf("%02d",$w_year);
                    break;
                }
            }
        }

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["TESTDIV"][TEXT]       = '1';
        $data["SHDIV"][TEXT]         = '1';
        $data["DESIREDIV"][TEXT]     = '1';
        $data["TESTDIV1"][TEXT]      = (strlen($model->field["TESTDIV1"]) ? $model->field["TESTDIV1"] : "");
        $data["TESTDIV2"][TEXT]      = (strlen($model->field["TESTDIV2"]) ? $model->field["TESTDIV2"] : "");
        $data["TESTDIV3"][TEXT]      = (strlen($model->field["TESTDIV3"]) ? $model->field["TESTDIV3"] : "");
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["ERACD"][TEXT]         = $w_eracd;
        $data["BIRTH_Y"][TEXT]       = $w_year;
        $data["BIRTH_M"][TEXT]       = $mon;
        $data["BIRTH_D"][TEXT]       = $day;
        $data["BIRTHDAY"][TEXT]      = str_replace("/","-",$model->field["BIRTHDAY"]);
        $data["FS_CD"][TEXT]         = $model->field["FS_CD"];
        $data["FS_NAME"][TEXT]       = $model->field["FS_NAME"];
        $data["FS_AREA_CD"][TEXT]    = $model->field["FS_AREA_CD"];
        $data["FS_NATPUBPRIDIV"][TEXT] = $model->field["FS_NATPUBPRIDIV"];
        $data["FS_GRDYEAR"][TEXT]    = $model->field["FS_GRDYEAR"];
        $data["PRISCHOOLCD"][TEXT]   = $model->field["PRISCHOOLCD"];
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]       = $model->field["REMARK2"];
        $data["RECOM_EXAMNO"][TEXT]  = $model->field["RECOM_EXAMNO"];
        if ($model->field["APPLICANTDIV"] == "4") {
            $data["JUDGEMENT"][TEXT]    = "1";// 4:高校（内部生）は自動的に合格
            $data["PROCEDUREDIV"][TEXT] = "1";// 4:高校（内部生）は自動的に済み
            $data["ENTDIV"][TEXT]       = "1";// 4:高校（内部生）は自動的に済み
        }
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $where .= "       EXAMNO = '".$model->examno."'";

        if ($model->cmd != "add") {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        } else {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
        }
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT ---DELETE
        if ($model->cmd != "add") {
            $query = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
            $query.= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
            $query.= "       EXAMNO = '".$model->examno."'";
            $db->query($query);
        }

        //ENTEXAM_APPLICANTADDR_DAT ---INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["ZIPCD"][TEXT]         = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]      = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]      = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]         = $model->field["TELNO"];
        $data["GNAME"][TEXT]         = $model->field["GNAME"];
        $data["GKANA"][TEXT]         = $model->field["GKANA"];
        $data["GZIPCD"][TEXT]        = $model->field["GZIPCD"];
        $data["GADDRESS1"][TEXT]     = $model->field["GADDRESS1"];
        $data["GADDRESS2"][TEXT]     = $model->field["GADDRESS2"];
        $data["GTELNO"][TEXT]        = $model->field["GTELNO"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_SCORE_DAT
        $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE, RECEPTNO ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "                EXAMNO      = '".$model->examno."'";
        $query .= "       )";
        $db->query($query);

        //ENTEXAM_JUDGE_TMP
        $query  = " DELETE FROM ENTEXAM_JUDGE_TMP ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_RECEPT_DAT
        $query  = "DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_DESIRE_DAT
        $query  = "DELETE FROM ENTEXAM_DESIRE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>