<?php

require_once('for_php7.php');

class knjl011wQuery extends Query
{
    //権限チェック（入試管理者）
    public function getAdminFlg()
    {
        $query  = " SELECT ";
        $query .= "     FIELD1 ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND STAFFCD = '".STAFFCD."' ";
        $query .= "     AND STAFF_SEQ = '009' ";

        $db = Query::dbCheckOut();
        $rtnFlg = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtnFlg;
    }
    
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

    //MAX受付番号＋１を取得
    public function getMaxExamnoQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     MAX(EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";

        $maxExam = $db->getOne($query);
        if (strlen($maxExam)) {
            $maxExam++;
        } else {
            $maxExam = 1;
        }

        Query::dbCheckIn($db);
        return sprintf("%05d", $maxExam);
    }

    //MAX受付番号＋１を取得
    public function getMaxRecomeExamnoQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     MAX(RECOM_EXAMNO) AS RECOM_EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";

        $maxExam = $db->getOne($query);
        if (strlen($maxExam)) {
            $maxExam++;
        } else {
            $maxExam = 1;
        }

        Query::dbCheckIn($db);
        return sprintf("%05d", $maxExam);
    }

    //検索ボタンを押した時は、受検番号で受付番号を取得
    public function getReferenceExamno($model)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "     RECOM_EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND EXAMNO = '".$model->examno."' ";
        $rtnNo = $db->getOne($query);
        Query::dbCheckIn($db);
        return $rtnNo;
    }

    //子画面から戻ってきた時、RECOM_EXAMNOを取得
    public function getSendRecomExamno($model)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "     RECOM_EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND EXAMNO = '".$model->examno."' ";
        $rtnNo = $db->getOne($query);
        Query::dbCheckIn($db);
        return $rtnNo;
    }

    //名称マスタよりデータを取得
    public function getNameCd($year, $cd, $sitei = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1, ";
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

    //募集競技
    public function getSportCd($year)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     SPORT_CD AS VALUE, ";
        $query .= "     SPORT_CD || ':' || SPORT_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SPORT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //志望区分
    public function getExamcourse($year, $applicantdiv, $testdiv, $course = "")
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD || ':' || L1.COURSENAME || L1.MAJORNAME || T1.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ";
        $query .= "                  ON T1.ENTEXAMYEAR      = L1.YEAR ";
        $query .= "                 AND T1.COURSECD         = L1.COURSECD ";
        $query .= "                 AND T1.MAJORCD          = L1.MAJORCD ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$testdiv}' ";
        if ($course) {
            $query .= "     AND T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD <> '".$course."' ";
        }
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
        $query .= "     T1.RECOM_EXAMNO, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.TESTDIV2, ";
        $query .= "     T1.OBOEGAKI_SIGANSYA, ";
        $query .= "     T1.KAIGAI_KIKOKUSEI_NADO, ";
        $query .= "     T1.TUUGAKU_KUIKIGAI_KYOKA, ";
        $query .= "     T1.HOSHOUNIN_TODOKE, ";
        $query .= "     T1.KENGAI_ZAIJUU, ";
        $query .= "     T1.KENGAI_CHUUGAKKOU_SHUSSHIN, ";
        $query .= "     T1.KENGAI_CHUUGAKKOU_SHUSSHIN2, ";
        $query .= "     T1.DAI1_COURSECD || '-' || T1.DAI1_MAJORCD || '-' || T1.DAI1_COURSECODE AS EXAMCOURSE, ";
        $query .= "     T1.DAI2_COURSECD || '-' || T1.DAI2_MAJORCD || '-' || T1.DAI2_COURSECODE AS EXAMCOURSE2, ";
        $query .= "     BD001.REMARK5 || '-' || BD001.REMARK6 || '-' || BD001.REMARK7 AS EXAMCOURSE_HENKOU, ";
        $query .= "     BD017.REMARK5 || '-' || BD017.REMARK6 || '-' || BD017.REMARK7 AS EXAMCOURSE_HENKOU2, ";
        $query .= "     BD021.REMARK4 AS SPORT_CD, ";
        $query .= "     T1.RECEPTDATE, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.ERACD, ";
        $query .= "     T3.NAME1 AS WNAME, ";
        $query .= "     T1.BIRTH_Y, ";
        $query .= "     T1.BIRTH_M, ";
        $query .= "     T1.BIRTH_D, ";
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
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     T4.CONFIDENTIAL_RPT01,T4.CONFIDENTIAL_RPT02,T4.CONFIDENTIAL_RPT03,T4.CONFIDENTIAL_RPT04,T4.CONFIDENTIAL_RPT05,T4.CONFIDENTIAL_RPT06, ";
        $query .= "     T4.CONFIDENTIAL_RPT07,T4.CONFIDENTIAL_RPT08,T4.CONFIDENTIAL_RPT09,T4.CONFIDENTIAL_RPT10,T4.CONFIDENTIAL_RPT11,T4.CONFIDENTIAL_RPT12, ";
        $query .= "     T4.TOTAL_ALL AS TOTAL9, ";
        $query .= "     T4.ABSENCE_DAYS  AS ABSENCE_DAYS1, ";
        $query .= "     T4.ABSENCE_DAYS2 AS ABSENCE_DAYS2, ";
        $query .= "     T4.ABSENCE_DAYS3 AS ABSENCE_DAYS3, ";
        $query .= "     L013.NAME1 AS JUDGEMENT_INFO ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD001 ";
        $query .= "          ON BD001.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND BD001.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND BD001.EXAMNO = T1.EXAMNO ";
        $query .= "         AND BD001.SEQ = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD017 ";
        $query .= "          ON BD017.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND BD017.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND BD017.EXAMNO = T1.EXAMNO ";
        $query .= "         AND BD017.SEQ = '017' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD021 ";
        $query .= "          ON BD021.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND BD021.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND BD021.EXAMNO = T1.EXAMNO ";
        $query .= "         AND BD021.SEQ = '021' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T2 ";
        $query .= "          ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ";
        $query .= "          ON T3.YEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T3.NAMECD1 = 'L007' ";
        $query .= "         AND T3.NAMECD2 = T1.ERACD ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "          ON N1.YEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND N1.NAMECD1 = 'L007' ";
        $query .= "         AND N1.NAMECD2 = T1.FS_ERACD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T4 ";
        $query .= "          ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T4.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T4.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ";
        $query .= "         ON T1.FS_CD = FIN.FINSCHOOLCD ";
        $query .= "     LEFT JOIN V_NAME_MST L013 ";
        $query .= "                  ON  L013.YEAR = T1.ENTEXAMYEAR ";
        $query .= "                  AND L013.NAMECD1 = 'L013' ";
        $query .= "                  AND L013.NAMECD2 = T1.JUDGEMENT ";
        $query .= "                  AND L013.NAMECD2 IN ('4','5') ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";

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

    //前の志願者の出身学校をコピー
    public function getBackFinCopy($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.FS_CD ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T1.EXAMNO < '".$model->examno."'";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO DESC ";
        return $query;
    }

    //出身学校マスタよりデータを取得
    public function getFinschoolName($cd)
    {
        $query  = " SELECT  T1.FINSCHOOL_NAME, T1.DISTRICTCD, L1.PREF_NAME ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "         LEFT JOIN PREF_MST L1 ON L1.PREF_CD = T1.FINSCHOOL_PREF_CD ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }

    //存在チェック
    public function existdata($model)
    {
        $query = "  SELECT
                        COUNT(*) 
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT
                    WHERE
                        ENTEXAMYEAR = '".$model->year."' AND     
                        APPLICANTDIV = '".$model->applicantdiv."' AND
                        EXAMNO = '".$model->examno."'";

        return $query;
    }

    //重複チェック(RECOM_EXAMNO)
    public function existdataRecomExamno($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        if ($model->cmd == "update" || $model->cmd == "back" || $model->cmd == "next") {
            $query .= "     AND EXAMNO = '".$model->examno."' ";
        }
        $query .= "     AND RECOM_EXAMNO = '".$model->recomExamno."' ";

        return $query;
    }

    //追加・更新
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $result = $db->query(knjl011wQuery::getCalendarno($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[] = $row["NAMECD2"];
            $defyear[] = $row["NAMESPARE1"];
        }
        $result->free();

        //生年月日の変換(和暦→西暦)
        $birthday = "";
        if (strlen($model->field["ERACD"]) && strlen($model->field["BIRTH_Y"]) && strlen($model->field["BIRTH_M"]) && strlen($model->field["BIRTH_D"])) {
            for ($i=0; $i<get_count($eracd); $i++) {
                if ($model->field["ERACD"] == $eracd[$i]) {
                    $syear = ((int)$defyear[$i]) + ((int)$model->field["BIRTH_Y"] - 1);
                    $birthday = $syear. "-".sprintf("%02d", $model->field["BIRTH_M"])."-".sprintf("%02d", $model->field["BIRTH_D"]);
                    break;
                }
            }
        }

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
        $data["RECOM_EXAMNO"][TEXT]  = $model->recomExamno;
        $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
        $data["TESTDIV2"][TEXT]      = $model->field["TESTDIV2"];//追検査
        $data["SHDIV"][TEXT]         = "1";
        $data["DESIREDIV"][TEXT]     = "1";
        $data["RECEPTDATE"][TEXT]    = str_replace("/", "-", $model->field["RECEPTDATE"]);
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["ERACD"][TEXT]         = $model->field["ERACD"];
        $data["BIRTH_Y"][TEXT]       = $model->field["BIRTH_Y"]? sprintf("%02d", $model->field["BIRTH_Y"]) : "";
        $data["BIRTH_M"][TEXT]       = $model->field["BIRTH_M"]? sprintf("%02d", $model->field["BIRTH_M"]) : "";
        $data["BIRTH_D"][TEXT]       = $model->field["BIRTH_D"]? sprintf("%02d", $model->field["BIRTH_D"]) : "";
        $data["BIRTHDAY"][TEXT]      = $birthday;
//        $data["BIRTHDAY"][TEXT]      = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"]) ? sprintf("%07d", $model->field["FS_CD"]) : "");
        $data["FS_ERACD"][TEXT]      = $model->field["FS_ERACD"];
        $data["FS_Y"][TEXT]          = $model->field["FS_Y"]? sprintf("%02d", $model->field["FS_Y"]) : "";
        $data["FS_M"][TEXT]          = $model->field["FS_M"]? sprintf("%02d", $model->field["FS_M"]) : "";
        $data["FS_GRDDIV"][TEXT]     = $model->field["FS_GRDDIV"];
        $data["FS_GRDYEAR"][TEXT]    = $fs_grdyear;
        $data["FS_DAY"][TEXT]        = $fs_date;
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]       = $model->field["REMARK2"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        if ($model->cmd == "add") {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND";
            $where .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
            $where .= "       EXAMNO = '".$model->examno."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        //ENTEXAM_APPLICANTADDR_DAT
        //DELETE
        if ($model->cmd != "add") {
            $query = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
            $query.= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
            $query.= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
            $query.= "       EXAMNO = '".$model->examno."'";
            $db->query($query);
        }
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
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

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        //cnt
        $getCount001 = $db->getOne(knjl011wQuery::getCountDetail($model, "001"));
        $getCount017 = $db->getOne(knjl011wQuery::getCountDetail($model, "017"));
        $getCount021 = $db->getOne(knjl011wQuery::getCountDetail($model, "021"));

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT 001:第１志望コース
        $course_array = array();
        $course_array = explode("-", $model->field["EXAMCOURSE"]);
        $course_arrayH = array();
        $course_arrayH = explode("-", $model->field["EXAMCOURSE_HENKOU"]);
        //INSERT
        $data = array();
        $data["REMARK8"][TEXT]      = $course_array[0];
        $data["REMARK9"][TEXT]      = $course_array[1];
        $data["REMARK10"][TEXT]     = $course_array[2];
        $data["REMARK5"][TEXT]      = $course_arrayH[0];
        $data["REMARK6"][TEXT]      = $course_arrayH[1];
        $data["REMARK7"][TEXT]      = $course_arrayH[2];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if ($getCount001 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "001";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
            $where .= "   AND EXAMNO        = '".$model->examno."'";
            $where .= "   AND SEQ           = '001' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT 017:第２志望コース
        $course_array = array();
        $course_array = explode("-", $model->field["EXAMCOURSE2"]);
        $course_arrayH = array();
        $course_arrayH = explode("-", $model->field["EXAMCOURSE_HENKOU2"]);
        //INSERT
        $data = array();
        $data["REMARK1"][TEXT]      = $course_array[0];
        $data["REMARK2"][TEXT]      = $course_array[1];
        $data["REMARK3"][TEXT]      = $course_array[2];
        $data["REMARK5"][TEXT]      = $course_arrayH[0];
        $data["REMARK6"][TEXT]      = $course_arrayH[1];
        $data["REMARK7"][TEXT]      = $course_arrayH[2];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if ($getCount017 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "017";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
            $where .= "   AND EXAMNO        = '".$model->examno."'";
            $where .= "   AND SEQ           = '017' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT 021:覚書志願者、海外帰国生等、通学区域外許可
        //INSERT
        $data = array();
        $data["REMARK1"][TEXT]      = $model->field["OBOEGAKI_SIGANSYA"];
        $data["REMARK2"][TEXT]      = $model->field["KAIGAI_KIKOKUSEI_NADO"];
        $data["REMARK3"][TEXT]      = $model->field["TUUGAKU_KUIKIGAI_KYOKA"];
        $data["REMARK4"][TEXT]      = $model->field["SPORT_CD"];
        $data["REMARK5"][TEXT]      = $model->field["HOSHOUNIN_TODOKE"];
        $data["REMARK6"][TEXT]      = $model->field["KENGAI_ZAIJUU"];
        $data["REMARK8"][TEXT]      = $model->field["KENGAI_CHUUGAKKOU_SHUSSHIN"];
        $data["REMARK10"][TEXT]     = $model->field["KENGAI_CHUUGAKKOU_SHUSSHIN2"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if ($getCount021 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "021";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
            $where .= "   AND EXAMNO        = '".$model->examno."'";
            $where .= "   AND SEQ           = '021' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    public function getCountDetail($model, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO          = '{$model->examno}' ";
        $query .= "     AND SEQ             = '{$seq}' ";
        return $query;
    }

    //--- DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' AND";
        $query .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "       EXAMNO       = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' AND";
        $query .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "       EXAMNO       = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' AND";
        $query .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "       EXAMNO       = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' AND";
        $query .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "       EXAMNO       = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' AND";
        $query .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "       EXAMNO       = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_KAIJI_DAT
        $query  = " DELETE FROM ENTEXAM_KAIJI_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' AND";
        $query .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "       RECEPTNO     = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_SCORE_DAT
        $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' AND";
        $query .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "       RECEPTNO     = '".$model->examno."'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- 調査書データ
    public function &getDelInsConfrptQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTCONFRPT_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' AND";
        $query .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "       EXAMNO       = '".$model->examno."'";
        $db->query($query);

        $query  = " INSERT INTO ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " ( ";
        $query .= " SELECT ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     '{$model->examno}' AS EXAMNO, ";
        $query .= "     CONFIDENTIAL_RPT01, ";
        $query .= "     CONFIDENTIAL_RPT02, ";
        $query .= "     CONFIDENTIAL_RPT03, ";
        $query .= "     CONFIDENTIAL_RPT04, ";
        $query .= "     CONFIDENTIAL_RPT05, ";
        $query .= "     CONFIDENTIAL_RPT06, ";
        $query .= "     CONFIDENTIAL_RPT07, ";
        $query .= "     CONFIDENTIAL_RPT08, ";
        $query .= "     CONFIDENTIAL_RPT09, ";
        $query .= "     CONFIDENTIAL_RPT10, ";
        $query .= "     CONFIDENTIAL_RPT11, ";
        $query .= "     CONFIDENTIAL_RPT12, ";
        $query .= "     ABSENCE_DAYS, ";
        $query .= "     ABSENCE_DAYS2, ";
        $query .= "     ABSENCE_DAYS3, ";
        $query .= "     AVERAGE3, ";
        $query .= "     AVERAGE5, ";
        $query .= "     AVERAGE_ALL, ";
        $query .= "     TOTAL3, ";
        $query .= "     TOTAL5, ";
        $query .= "     TOTAL_ALL, ";
        $query .= "     KASANTEN_ALL, ";
        $query .= "     ABSENCE_REMARK, ";
        $query .= "     ABSENCE_REMARK2, ";
        $query .= "     ABSENCE_REMARK3, ";
        $query .= "     BASE_FLG, ";
        $query .= "     HEALTH_FLG, ";
        $query .= "     ACTIVE_FLG, ";
        $query .= "     RESPONSIBLE_FLG, ";
        $query .= "     ORIGINAL_FLG, ";
        $query .= "     MIND_FLG, ";
        $query .= "     NATURE_FLG, ";
        $query .= "     WORK_FLG, ";
        $query .= "     JUSTICE_FLG, ";
        $query .= "     PUBLIC_FLG, ";
        $query .= "     SPECIALACTREC, ";
        $query .= "     TOTALSTUDYTIME, ";
        $query .= "     SPECIALREPORT, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->defVal["APPLICANTDIV"]."' ";
        $query .= "     AND EXAMNO = '".$model->defVal["EXAMNO"]."' ";
        $query .= " ) ";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' AND";
        $query .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "       EXAMNO       = '".$model->examno."'";
        $db->query($query);

        $query  = " INSERT INTO ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
        $query .= " ( ";
        $query .= " SELECT ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     '{$model->examno}' AS EXAMNO, ";
        $query .= "     SEQ, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2, ";
        $query .= "     REMARK3, ";
        $query .= "     REMARK4, ";
        $query .= "     REMARK5, ";
        $query .= "     REMARK6, ";
        $query .= "     REMARK7, ";
        $query .= "     REMARK8, ";
        $query .= "     REMARK9, ";
        $query .= "     REMARK10, ";
        $query .= "     REMARK11, ";
        $query .= "     REMARK12, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
        $query .= "  ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->defVal["APPLICANTDIV"]."' ";
        $query .= "     AND EXAMNO = '".$model->defVal["EXAMNO"]."' ";
        $query .= " ) ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
