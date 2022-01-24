<?php

require_once('for_php7.php');

class knjl011gQuery extends Query
{
    //学校コードを取得
    public function getNameMstZ010()
    {
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

    //MAX受験番号＋１を取得
    public function getMaxExamnoQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     MAX(EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";//入試制度
        if ($model->isKasiwara == "1") {
            $course_array = array();
            $course_array = explode("-", $model->field["EXAMCOURSE"]);
            $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";//入試区分
            $query .= "     AND SHDIV = '{$model->field["SHDIV"]}' ";//専併区分
            if ($model->field["TESTDIV"] == "1" && $model->field["SHDIV"] == "1") {
                $query .= "     AND DAI1_COURSECODE = '".$course_array[2]."' ";//第１志望
            }
        } else {
            $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";//入試区分
            $query .= "     AND SHDIV = '{$model->field["SHDIV"]}' ";//専併区分
        }

        $maxExam = $db->getOne($query);
        if (strlen($maxExam)) {
            $maxExam++;
        } else {
            $maxExam = knjl011gQuery::getDefNumber($model);
        }

        Query::dbCheckIn($db);
        return sprintf("%04d", $maxExam);
    }

    //敬愛)受験番号(初期値)
    public function getDefNumber($model)
    {
        $check = array();

        if ($model->isKasiwara == "1") {
            $check[] = array("TESTDIV" => "1", "SHDIV" => "1", "EXAMCOURSE" => "3000", "MIN_NO" => "0001", "MAX_NO" => "0099"); //アシスト
            $check[] = array("TESTDIV" => "1", "SHDIV" => "1", "EXAMCOURSE" => "2000", "MIN_NO" => "0101", "MAX_NO" => "0299"); //キャリアアップ
            $check[] = array("TESTDIV" => "1", "SHDIV" => "1", "EXAMCOURSE" => "4000", "MIN_NO" => "0301", "MAX_NO" => "0499"); //スポーツ
            $check[] = array("TESTDIV" => "1", "SHDIV" => "1", "EXAMCOURSE" => "1000", "MIN_NO" => "0501", "MAX_NO" => "0599"); //アドバンスト
            $check[] = array("TESTDIV" => "2", "SHDIV" => "1", "EXAMCOURSE" => "NO",   "MIN_NO" => "0601", "MAX_NO" => "0699");
            $check[] = array("TESTDIV" => "3", "SHDIV" => "1", "EXAMCOURSE" => "NO",   "MIN_NO" => "0701", "MAX_NO" => "0799");
            $check[] = array("TESTDIV" => "1", "SHDIV" => "2", "EXAMCOURSE" => "NO",   "MIN_NO" => "1001", "MAX_NO" => "2999");
            $check[] = array("TESTDIV" => "2", "SHDIV" => "2", "EXAMCOURSE" => "NO",   "MIN_NO" => "3001", "MAX_NO" => "3999");

            $course_array = array();
            $course_array = explode("-", $model->field["EXAMCOURSE"]);

            foreach ($check as $key => $val) {
                if ($model->field["TESTDIV"] == $val["TESTDIV"] && $model->field["SHDIV"] == $val["SHDIV"] && ($course_array[2] == $val["EXAMCOURSE"] || "NO" == $val["EXAMCOURSE"])) {
                    return $val["MIN_NO"];
                }
            }
        } else {
            $check[] = array("TESTDIV" => "1", "SHDIV" => "1", "MIN_NO" => "1001", "MAX_NO" => "1999");
            $check[] = array("TESTDIV" => "1", "SHDIV" => "2", "MIN_NO" => "2001", "MAX_NO" => "2999");
            $check[] = array("TESTDIV" => "2", "SHDIV" => "1", "MIN_NO" => "6001", "MAX_NO" => "6999");
            $check[] = array("TESTDIV" => "2", "SHDIV" => "2", "MIN_NO" => "7001", "MAX_NO" => "7999");
            $check[] = array("TESTDIV" => "3", "SHDIV" => "1", "MIN_NO" => "8001", "MAX_NO" => "8999");
            $check[] = array("TESTDIV" => "3", "SHDIV" => "2", "MIN_NO" => "8001", "MAX_NO" => "8999");

            foreach ($check as $key => $val) {
                if ($model->field["TESTDIV"] == $val["TESTDIV"] && $model->field["SHDIV"] == $val["SHDIV"]) {
                    return $val["MIN_NO"];
                }
            }
        }

        return "0001";
    }

    //名称マスタよりデータを取得
    public function getNameCd($year, $cd, $sitei = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //志望区分
    public function getExamcourse($year, $applicantdiv, $testdiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.EXAMCOURSECD || ':' || T1.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //名称マスタより和暦の元号を取得
    public function getCalendarno($year, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = 'L007' ";
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }

        return $query;
    }

    //メインデータ取得
    public function getEditData($model)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.SHDIV, ";
        $query .= "     T1.DAI1_COURSECD || '-' || T1.DAI1_MAJORCD || '-' || T1.DAI1_COURSECODE AS EXAMCOURSE, ";
        $query .= "     T1.DAI2_COURSECD || '-' || T1.DAI2_MAJORCD || '-' || T1.DAI2_COURSECODE AS EXAMCOURSE2, ";
        $query .= "     T1.DAI2_SENPEIDIV AS SHDIV2, ";
        $query .= "     T1.RECEPTDATE, ";
        $query .= "     T1.SPECIAL_REASON_DIV, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     T1.FS_GRDYEAR, ";
        $query .= "     T1.FS_ERACD, ";
        $query .= "     N1.NAME1 AS FS_WNAME, ";
        $query .= "     T1.FS_Y, ";
        $query .= "     T1.FS_M, ";
        $query .= "     T1.FS_GRDDIV, ";
        $query .= "     T1.FS_DAY, ";
        $query .= "     T2.ZIPCD, ";
        $query .= "     T2.ADDRESS1, ";
        $query .= "     T2.ADDRESS2, ";
        $query .= "     T2.TELNO, ";
        $query .= "     T2.GNAME, ";
        $query .= "     T2.GKANA, ";
        $query .= "     T2.RELATIONSHIP, ";
        $query .= "     T2.GZIPCD, ";
        $query .= "     T2.GADDRESS1, ";
        $query .= "     T2.GADDRESS2, ";
        $query .= "     T2.GTELNO, ";
        $query .= "     T3.NAME1, ";
        $query .= "     T1.JIZEN_SOUDAN_CD, ";
        $query .= "     T1.JIZEN_SOUDAN_TEXT, ";
        $query .= "     T1.CLUB_CD, ";
        $query .= "     T1.CLUB_RANK, ";
        $query .= "     T1.HOPE_CLUB_CD, ";
        $query .= "     T1.SINZOKU_NAME, ";
        $query .= "     T1.SINZOKU_KYUSEI, ";
        $query .= "     T1.SINZOKU_ZOKUGARA, ";
        $query .= "     T1.SINZOKU_GAKKOUMEI, ";
        $query .= "     T1.SINZOKU_GAKKA, ";
        $query .= "     T1.SINZOKU_ZAI_GRD_YEAR, ";
        $query .= "     T1.SINZOKU_ZAI_GRD_FLG, ";
        $query .= "     T1.SINZOKU_FUTAGO_NAME, ";
        $query .= "     T1.SINZOKU_FUTAGO_ZOKUGARA, ";
        $query .= "     T4.INTERVIEW_VALUE, ";
        $query .= "     T4.INTERVIEW_REMARK, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     L013.NAME1 AS JUDGEMENT_INFO ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T2 ";
        $query .= "          ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ";
        $query .= "          ON T3.YEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T3.NAMECD1 = 'L007' ";
        $query .= "         AND T3.NAMECD2 = T1.ERACD ";
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT T4 ";
        $query .= "          ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T4.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ";
        $query .= "         ON T1.FS_CD = FIN.FINSCHOOLCD ";
        $query .= "     LEFT JOIN V_NAME_MST L013 ";
        $query .= "                  ON  L013.YEAR = T1.ENTEXAMYEAR ";
        $query .= "                  AND L013.NAMECD1 = 'L013' ";
        $query .= "                  AND L013.NAMECD2 = T1.JUDGEMENT ";
        $query .= "                  AND L013.NAMECD2 = '4' ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "          ON N1.YEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND N1.NAMECD1 = 'L007' ";
        $query .= "         AND N1.NAMECD2 = T1.FS_ERACD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' AND ";

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= " T1.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= " T1.EXAMNO > '".sprintf("%04d", $model->examno)."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO ASC ";
        } else {
            $query.= " T1.EXAMNO = '".$model->examno."'";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //出身学校マスタよりデータを取得
    public function getFinschoolName($cd)
    {
        $query  = " SELECT  T1.FINSCHOOL_NAME, T1.DISTRICTCD ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }

    //データ重複チェック
    public function checkData($model)
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
    public function existdata($model)
    {
        $query = "  SELECT
                        EXAMNO
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT
                    WHERE
                        ENTEXAMYEAR = '".$model->year."' AND     
                        EXAMNO = '".$model->examno."'";

        return $query;
    }

    //追加・更新
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $result = $db->query(knjl011gQuery::getCalendarno($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[] = $row["NAMECD2"];
            $defyear[] = $row["NAMESPARE1"];
        }
        $result->free();

        //卒業日の変換(和暦→西暦)
        $fs_date = $fs_grdyear = "";
        if (strlen($model->field["FS_ERACD"]) && strlen($model->field["FS_Y"]) && strlen($model->field["FS_M"])) {
            for ($i=0; $i<get_count($eracd); $i++) {
                if ($model->field["FS_ERACD"] == $eracd[$i]) {
                    $fs_grdyear = ((int)$defyear[$i]) + ((int)$model->field["FS_Y"] - 1);
                    $fs_date = $fs_grdyear. "-".sprintf("%02d", $model->field["FS_M"])."-01";
                    break;
                }
            }
        }

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
        $data["SHDIV"][TEXT]         = $model->field["SHDIV"];
        $data["DESIREDIV"][TEXT]     = "1";
        $data["RECEPTDATE"][TEXT]    = str_replace("/", "-", $model->field["RECEPTDATE"]);
        $data["SPECIAL_REASON_DIV"][TEXT]   = $model->field["SPECIAL_REASON_DIV"];
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["BIRTHDAY"][TEXT]      = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"]) ? sprintf("%07d", $model->field["FS_CD"]) : "");
        $data["FS_ERACD"][TEXT]      = $model->field["FS_ERACD"];
        $data["FS_Y"][TEXT]          = $model->field["FS_Y"]? sprintf("%02d", $model->field["FS_Y"]) : "";
        $data["FS_M"][TEXT]          = $model->field["FS_M"]? sprintf("%02d", $model->field["FS_M"]) : "";
        $data["FS_GRDDIV"][TEXT]     = $model->field["FS_GRDDIV"];
        $data["FS_GRDYEAR"][TEXT]    = $fs_grdyear;
        $data["FS_DAY"][TEXT]        = $fs_date;
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];//監督者情報
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        if ($model->cmd == "add") {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND";
            $where .= "       EXAMNO = '".$model->examno."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        //ENTEXAM_APPLICANTADDR_DAT
        //DELETE
        if ($model->cmd != "add") {
            $query = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
            $query.= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
            $query.= "       EXAMNO = '".$model->examno."'";
            $db->query($query);
        }
        //INSERT
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
        $data["RELATIONSHIP"][TEXT]  = $model->field["RELATIONSHIP"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);

        //ENTEXAM_INTERVIEW_DAT 面接
        //DELETE
        $query = " DELETE FROM ENTEXAM_INTERVIEW_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "       AND EXAMNO = '".$model->examno."'";
        $db->query($query);
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["INTERVIEW_REMARK"][TEXT] = $model->field["INTERVIEW_REMARK"];
        $data["INTERVIEW_VALUE"][TEXT]  = $model->field["INTERVIEW_VALUE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT 001:第１志望コース
        $course_array = array();
        $course_array = explode("-", $model->field["EXAMCOURSE"]);
        //DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "       AND EXAMNO = '".$model->examno."'";
        $query.= "       AND SEQ = '001'";
        $db->query($query);
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "001";
        $data["REMARK8"][TEXT]      = $course_array[0];
        $data["REMARK9"][TEXT]      = $course_array[1];
        $data["REMARK10"][TEXT]     = $course_array[2];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT 004:事前相談
        //DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "   AND EXAMNO = '".$model->examno."' ";
        $query.= "   AND SEQ = '004' ";
        $db->query($query);
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["SEQ"][TEXT]              = "004";
        $data["REMARK9"][TEXT]          = $model->field["JIZEN_SOUDAN_CD"];
        $data["REMARK10"][TEXT]         = $model->field["JIZEN_SOUDAN_TEXT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT 017:第２志望コース・専併区分
        $course_array = array();
        $course_array = explode("-", $model->field["EXAMCOURSE2"]);
        //DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "       AND EXAMNO = '".$model->examno."'";
        $query.= "       AND SEQ = '017'";
        $db->query($query);
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "017";
        $data["REMARK1"][TEXT]      = $course_array[0];
        $data["REMARK2"][TEXT]      = $course_array[1];
        $data["REMARK3"][TEXT]      = $course_array[2];
        $data["REMARK4"][TEXT]      = $model->field["SHDIV2"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT 018:親族・双生児
        //DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "   AND EXAMNO = '".$model->examno."' ";
        $query.= "   AND SEQ = '018' ";
        $db->query($query);
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["SEQ"][TEXT]              = "018";
        $data["REMARK1"][TEXT]          = $model->field["SINZOKU_NAME"];
        $data["REMARK2"][TEXT]          = $model->field["SINZOKU_KYUSEI"];
        $data["REMARK3"][TEXT]          = $model->field["SINZOKU_ZOKUGARA"];
        $data["REMARK4"][TEXT]          = $model->field["SINZOKU_GAKKOUMEI"];
        $data["REMARK5"][TEXT]          = $model->field["SINZOKU_GAKKA"];
        $data["REMARK6"][TEXT]          = $model->field["SINZOKU_ZAI_GRD_YEAR"];
        $data["REMARK7"][TEXT]          = $model->field["SINZOKU_ZAI_GRD_FLG"];
        $data["REMARK8"][TEXT]          = $model->field["SINZOKU_FUTAGO_NAME"];
        $data["REMARK9"][TEXT]          = $model->field["SINZOKU_FUTAGO_ZOKUGARA"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT 019:クラブ推薦
        //DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "   AND EXAMNO = '".$model->examno."' ";
        $query.= "   AND SEQ = '019' ";
        $db->query($query);
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["SEQ"][TEXT]              = "019";
        $data["REMARK1"][TEXT]          = $model->field["CLUB_CD"];
        $data["REMARK2"][TEXT]          = $model->field["CLUB_RANK"];
        $data["REMARK3"][TEXT]          = $model->field["HOPE_CLUB_CD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
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

        //ENTEXAM_INTERVIEW_DAT
        $query  = " DELETE FROM ENTEXAM_INTERVIEW_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_SCORE_DAT
        $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "                EXAMNO      = '".$model->examno."'";
        $query .= "       )";
        $db->query($query);

        //ENTEXAM_RECEPT_DAT
        $query  = "DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    public function getPayMoney($model, $examno)
    {
        $query  = "";

        //免除額コードを取得(BASE)
        $query .= " WITH V_COMMON AS ( ";
        $query .= "     SELECT ";
        $query .= "         '1' AS LINK, ";
        $query .= "         EXEMPTION_CD ";
        $query .= "     FROM ";
        $query .= "         V_ENTEXAM_APPLICANTBASE_EXEMPTION_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '{$model->year}' ";
        $query .= "         AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "         AND EXAMNO = '{$examno}' ";
        //基本額を取得（費目マスタ）
        $query .= " ), T_ITEM AS ( ";
        $query .= "     SELECT ";
        $query .= "         '1' AS LINK, ";
        $query .= "         SUM(ITEM_MONEY) AS ITEM_MONEY ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_PAYMENT_ITEM_MST ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '{$model->year}' ";
        $query .= "         AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "         AND DIV = '0' ";
        $query .= "         AND REMARK4 = '1' ";//受験料
        $query .= " ) ";
        //基本額から免除額を引いた金額を取得
        $query .= " SELECT ";
        $query .= "     VALUE(L1.ITEM_MONEY, 0) AS MONEY ";
        $query .= " FROM ";
        $query .= "     V_COMMON T1 ";
        $query .= "     LEFT JOIN T_ITEM L1 ON L1.LINK = T1.LINK ";

        return $query;
    }
}
