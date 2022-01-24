<?php

require_once('for_php7.php');

class knjl011nQuery extends Query
{

    //MAX受験番号＋１を取得
    public function getMaxExamnoQuery($model)
    {
        $db = Query::dbCheckOut();

        //受験コース
        $course_array = array();
        $course_array = explode("-", $model->field["EXAMCOURSE"]);

        $setTop1 = $setTop2 = "";
        //1:特進Ｓ 2:特進Ａ 3:こころ教養 4:体育クラブ 5:こども夢
        if ($course_array[2] == "0001") {
            $setTop1 = "1";
        } elseif ($course_array[2] == "0002") {
            $setTop1 = "2";
        } elseif ($course_array[2] == "0003") {
            $setTop1 = "3";
        } elseif ($course_array[2] == "0004") {
            $setTop1 = "5";
        } elseif ($course_array[2] == "0005") {
            $setTop1 = "6";
        } else {
            $setTop1 = "0";
        }
        //1:推薦 2:専願 3:一般（併願）
        if ($model->field["SHDIV"] == "2") {
            $setTop2 = "3";
        } elseif ($model->field["SHDIV"] == "3") {
            $setTop2 = "5";
        } elseif ($model->field["SHDIV"] == "1") {
            $setTop2 = "0";
        } else {
            $setTop2 = "0";
        }
        $setTop = $setTop1.$setTop2;

        $query  = " SELECT ";
        $query .= "     MAX(EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND EXAMNO LIKE '{$setTop}%' ";

        $maxExam = $db->getOne($query);
        if (strlen($maxExam)) {
            $maxExam++;
        } else {
            $maxExam = $setTop."01";
        }

        Query::dbCheckIn($db);
        return $maxExam;
    }

    //名称マスタよりデータを取得
    public function getNameCd($year, $cd, $sitei = "")
    {
        $query  = " SELECT ";
        if ($cd !== 'L008') {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE ";
        } else {
            $query .= "     ABBV1, ";
            $query .= "     NAMECD2 AS VALUE ";
        }
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

    //受験科目数(表示のみ)
    public function getJukenKamokuCnt($year, $applicantdiv, $testdiv, $examcourse)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(TESTSUBCLASSCD) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND TESTDIV = '{$testdiv}' ";
        $query .= "     AND COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD = '{$examcourse}' ";

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
        $query .= "     T1.SHDIV, ";
        $query .= "     T5.REMARK8 || '-' || T5.REMARK9 || '-' || T5.REMARK10 AS EXAMCOURSE, ";
        $query .= "     T6.REMARK1 || '-' || T6.REMARK2 || '-' || T6.REMARK3 AS EXAMCOURSE2, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.RECEPTDATE, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     T1.FS_AREA_CD, ";
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
        $query .= "     T4.REMARK1, ";
        $query .= "     T4.REMARK2, ";
        $query .= "     T4.REMARK3, ";
        $query .= "     T4.REMARK4, ";
        $query .= "     T4.REMARK5, ";
        $query .= "     T4.REMARK6, ";
        $query .= "     T4.REMARK7 AS SHOUGAKU1, ";
        $query .= "     T4.REMARK8 AS SHOUGAKU5, ";
        $query .= "     T4.REMARK9 AS SOUDAN, ";
        $query .= "     L1.CONFIDENTIAL_RPT01, ";
        $query .= "     L1.CONFIDENTIAL_RPT02, ";
        $query .= "     L1.CONFIDENTIAL_RPT03, ";
        $query .= "     L1.CONFIDENTIAL_RPT04, ";
        $query .= "     L1.CONFIDENTIAL_RPT05, ";
        $query .= "     L1.CONFIDENTIAL_RPT06, ";
        $query .= "     L1.CONFIDENTIAL_RPT07, ";
        $query .= "     L1.CONFIDENTIAL_RPT08, ";
        $query .= "     L1.CONFIDENTIAL_RPT09, ";
        $query .= "     L1.CONFIDENTIAL_RPT10, ";
        $query .= "     L1.TOTAL_ALL, ";
        $query .= "     L1.TOTAL5, ";
        $query .= "     L1.KASANTEN_ALL, ";
        $query .= "     L1.ABSENCE_DAYS, ";
        $query .= "     L1.ABSENCE_DAYS2, ";
        $query .= "     L1.ABSENCE_DAYS3, ";
        $query .= "     L1.ABSENCE_REMARK, ";
        $query .= "     L1.ABSENCE_REMARK2, ";
        $query .= "     L1.ABSENCE_REMARK3, ";
        $query .= "     L1.REMARK1 AS CONFRPT_REMARK1, ";
        $query .= "     D4.REMARK1 AS DETAIL4_REMARK1, ";
        $query .= "     L013.NAME1 AS JUDGEMENT_INFO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT T2 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ";
        $query .= "     T1.EXAMNO = T2.EXAMNO ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     V_NAME_MST T3 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = T3.YEAR AND ";
        $query .= "     T1.ERACD = T3.NAMECD2 AND ";
        $query .= "     T3.NAMECD1 = 'L007' ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT T4 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = T4.ENTEXAMYEAR AND ";
        $query .= "     T1.EXAMNO = T4.EXAMNO AND ";
        $query .= "     T4.SEQ = '009' ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT T5 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = T5.ENTEXAMYEAR AND ";
        $query .= "     T1.EXAMNO = T5.EXAMNO AND ";
        $query .= "     T5.SEQ = '001' ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT T6 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = T6.ENTEXAMYEAR AND ";
        $query .= "     T1.EXAMNO = T6.EXAMNO AND ";
        $query .= "     T6.SEQ = '017' ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT D4 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = D4.ENTEXAMYEAR AND ";
        $query .= "     T1.EXAMNO = D4.EXAMNO AND ";
        $query .= "     D4.SEQ = '004' ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     FINSCHOOL_MST FIN ";
        $query .= " ON ";
        $query .= "     T1.FS_CD = FIN.FINSCHOOLCD ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT L1 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = L1.ENTEXAMYEAR AND ";
        $query .= "     T1.EXAMNO = L1.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST L013 ";
        $query .= "                  ON  L013.YEAR = T1.ENTEXAMYEAR ";
        $query .= "                  AND L013.NAMECD1 = 'L013' ";
        $query .= "                  AND L013.NAMECD2 = T1.JUDGEMENT ";
        $query .= "                  AND L013.NAMECD2 IN ('4','5') ";
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
    public function existData($model)
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

    //--- INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
        $data["SHDIV"][TEXT]         = $model->field["SHDIV"];
        $data["DESIREDIV"][TEXT]     = "1";
        $data["RECEPTDATE"][DATE]    = $model->field["RECEPTDATE"];
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["ERACD"][TEXT]         = $model->field["ERACD"];
        $data["BIRTHDAY"][DATE]      = $model->field["BIRTHDAY"];
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"]) ? sprintf("%07d", $model->field["FS_CD"]) : "");
        $data["FS_AREA_CD"][TEXT]    = (strlen($model->field["FS_AREA_CD"]) ? sprintf("%02d", $model->field["FS_AREA_CD"]) : ""); //2005.12.28 minei
        $data["FS_DAY"][DATE]        = $model->field["FS_DAY"];
        $data["FS_GRDDIV"][TEXT]     = $model->field["FS_GRDDIV"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
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

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "009";
        $data["REMARK1"][TEXT]      = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]      = $model->field["REMARK2"];
        $data["REMARK3"][TEXT]      = $model->field["REMARK3"];
        $data["REMARK4"][TEXT]      = $model->field["REMARK4"];
        $data["REMARK5"][TEXT]      = $model->field["REMARK5"];
        $data["REMARK6"][TEXT]      = $model->field["REMARK6"];
        $data["REMARK7"][TEXT]      = $model->field["SHOUGAKU1"];
        $data["REMARK8"][TEXT]      = $model->field["SHOUGAKU5"];
        $data["REMARK9"][TEXT]      = $model->field["SOUDAN"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);


        //受験コース
        $course_array = array();
        $course_array = explode("-", $model->field["EXAMCOURSE"]);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
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

        //受験コース（第二志望）
        $course_array2 = array();
        $course_array2 = explode("-", $model->field["EXAMCOURSE2"]);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "017";
        $data["REMARK1"][TEXT]      = $course_array2[0];
        $data["REMARK2"][TEXT]      = $course_array2[1];
        $data["REMARK3"][TEXT]      = $course_array2[2];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);

        //調査書
        //ENTEXAM_APPLICANTCONFRPT_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]          = $model->year;
        $data["EXAMNO"][TEXT]               = $model->examno;
        $data["CONFIDENTIAL_RPT01"][NUMBER] = $model->field["CONFIDENTIAL_RPT01"];
        $data["CONFIDENTIAL_RPT02"][NUMBER] = $model->field["CONFIDENTIAL_RPT02"];
        $data["CONFIDENTIAL_RPT03"][NUMBER] = $model->field["CONFIDENTIAL_RPT03"];
        $data["CONFIDENTIAL_RPT04"][NUMBER] = $model->field["CONFIDENTIAL_RPT04"];
        $data["CONFIDENTIAL_RPT05"][NUMBER] = $model->field["CONFIDENTIAL_RPT05"];
        $data["CONFIDENTIAL_RPT06"][NUMBER] = $model->field["CONFIDENTIAL_RPT06"];
        $data["CONFIDENTIAL_RPT07"][NUMBER] = $model->field["CONFIDENTIAL_RPT07"];
        $data["CONFIDENTIAL_RPT08"][NUMBER] = $model->field["CONFIDENTIAL_RPT08"];
        $data["CONFIDENTIAL_RPT09"][NUMBER] = $model->field["CONFIDENTIAL_RPT09"];
        $data["TOTAL_ALL"][NUMBER]          = $model->field["TOTAL_ALL"];
        $data["TOTAL5"][NUMBER]             = $model->field["TOTAL5"];
        $data["KASANTEN_ALL"][NUMBER]       = $model->field["KASANTEN_ALL"];
        $data["ABSENCE_DAYS"][NUMBER]       = $model->field["ABSENCE_DAYS"];
        $data["ABSENCE_DAYS2"][NUMBER]      = $model->field["ABSENCE_DAYS2"];
        $data["ABSENCE_DAYS3"][NUMBER]      = $model->field["ABSENCE_DAYS3"];
        $data["ABSENCE_REMARK"][TEXT]       = $model->field["ABSENCE_REMARK"];
        $data["ABSENCE_REMARK2"][TEXT]      = $model->field["ABSENCE_REMARK2"];
        $data["ABSENCE_REMARK3"][TEXT]      = $model->field["ABSENCE_REMARK3"];
        $data["REMARK1"][TEXT]              = $model->field["CONFRPT_REMARK1"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
        $db->query($query);
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "004";
        $data["REMARK1"][TEXT]      = $model->field["DETAIL4_REMARK1"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);


        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
        $data["SHDIV"][TEXT]         = $model->field["SHDIV"];
        $data["DESIREDIV"][TEXT]     = "1";
        $data["RECEPTDATE"][DATE]    = $model->field["RECEPTDATE"];
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["ERACD"][TEXT]         = $model->field["ERACD"];
        $data["BIRTHDAY"][DATE]      = $model->field["BIRTHDAY"];
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"]) ? sprintf("%07d", $model->field["FS_CD"]) : "");
        $data["FS_AREA_CD"][TEXT]    = (strlen($model->field["FS_AREA_CD"]) ? sprintf("%02d", $model->field["FS_AREA_CD"]) : ""); //2005.12.28 minei
        $data["FS_DAY"][DATE]        = $model->field["FS_DAY"];
        $data["FS_GRDDIV"][TEXT]     = $model->field["FS_GRDDIV"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $where .= "       EXAMNO = '".$model->examno."'";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT ---DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query.= "       EXAMNO = '".$model->examno."'";
        $db->query($query);

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
        $data["RELATIONSHIP"][TEXT]  = $model->field["RELATIONSHIP"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);


        //受験コース
        $course_array = array();
        $course_array = explode("-", $model->field["EXAMCOURSE"]);
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT ---DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "       AND EXAMNO = '".$model->examno."'";
        $query.= "       AND SEQ = '001'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
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

        //受験コース（第二志望）
        $course_array2 = array();
        $course_array2 = explode("-", $model->field["EXAMCOURSE2"]);
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT ---DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "       AND EXAMNO = '".$model->examno."'";
        $query.= "       AND SEQ = '017'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "017";
        $data["REMARK1"][TEXT]      = $course_array2[0];
        $data["REMARK2"][TEXT]      = $course_array2[1];
        $data["REMARK3"][TEXT]      = $course_array2[2];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);


        //ENTEXAM_APPLICANTBASE_DETAIL_DAT ---DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "       AND EXAMNO = '".$model->examno."'";
        $query.= "       AND SEQ = '009'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "009";
        $data["REMARK1"][TEXT]      = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]      = $model->field["REMARK2"];
        $data["REMARK3"][TEXT]      = $model->field["REMARK3"];
        $data["REMARK4"][TEXT]      = $model->field["REMARK4"];
        $data["REMARK5"][TEXT]      = $model->field["REMARK5"];
        $data["REMARK6"][TEXT]      = $model->field["REMARK6"];
        $data["REMARK7"][TEXT]      = $model->field["SHOUGAKU1"];
        $data["REMARK8"][TEXT]      = $model->field["SHOUGAKU5"];
        $data["REMARK9"][TEXT]      = $model->field["SOUDAN"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

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
}
