<?php

require_once('for_php7.php');

class knjl511fQuery extends Query 
{

    //MAX受験番号＋１を取得
    function getMaxExamnoQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     MAX(APP_B.EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT APP_B ";
        $query .= " WHERE ";
        $query .= "     APP_B.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APP_B.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND APP_B.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND APP_B.TESTDIV1     = '{$model->field["TESTDIV1"]}' ";
        $query .= "     AND APP_B.COURSECD || '-' || APP_B.MAJORCD || '-' || APP_B.EXAMCOURSECD = '{$model->field["EXAMCOURSE"]}' ";
        $maxExam = $db->getOne($query);
        if (strlen($maxExam)) {
            $maxExam++;
        } else {
            $query  = " SELECT ";
            $query .= "     MAX(APP_B.EXAMNO) AS EXAMNO ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT APP_B ";
            $query .= " WHERE ";
            $query .= "     APP_B.ENTEXAMYEAR = '".$model->year."' ";
            $query .= "     AND APP_B.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "     AND APP_B.TESTDIV      = '{$model->field["TESTDIV"]}' ";
            $query .= "     AND APP_B.TESTDIV1     = '{$model->field["TESTDIV1"]}' ";
            $query .= "     AND EXISTS (";
            $query .= "        SELECT 'X' ";
            $query .= "        FROM ENTEXAM_NO_DAT ";
            $query .= "        WHERE ENTEXAMYEAR = APP_B.ENTEXAMYEAR ";
            $query .= "          AND APPLICANTDIV = APP_B.APPLICANTDIV ";
            $query .= "          AND TESTDIV = APP_B.TESTDIV ";
            $query .= "          AND TESTDIV1 = APP_B.TESTDIV1 ";
            $query .= "          AND (COURSECD IS NULL OR MAJORCD IS NULL OR EXAMCOURSECD IS NULL) ";
            $query .= "       ) ";

            $maxExam = $db->getOne($query);
            if (strlen($maxExam)) {
                $maxExam++;
            } else {
                $query  = " SELECT ";
                $query .= "     EXAMNO_FROM ";
                $query .= " FROM ";
                $query .= "     ENTEXAM_NO_DAT ";
                $query .= " WHERE ";
                $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
                $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
                $query .= "     AND TESTDIV         = '{$model->field["TESTDIV"]}' ";
                $query .= "     AND MEMO_KISO       = '".$model->field["TESTDIV1"]."' ";
                $query .= "     AND COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD = '{$model->field["EXAMCOURSE"]}'";

                $maxExam = $db->getOne($query);
                if (strlen($maxExam)) {
                    $maxExam++;
                } else {
                    $query  = " SELECT ";
                    $query .= "     EXAMNO_FROM ";
                    $query .= " FROM ";
                    $query .= "     ENTEXAM_NO_DAT ";
                    $query .= " WHERE ";
                    $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
                    $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
                    $query .= "     AND TESTDIV         = '{$model->field["TESTDIV"]}' ";
                    $query .= "     AND MEMO_KISO       = '".$model->field["TESTDIV1"]."' ";
                    $query .= "     AND (COURSECD IS NULL OR MAJORCD IS NULL OR EXAMCOURSECD IS NULL) ";
                    $maxExam = $db->getOne($query);
                    if (!strlen($maxExam)) {
                        $maxExam = "0001";
                    }
                }
            }
        }

        Query::dbCheckIn($db);
        return sprintf("%04d", $maxExam);
    }

    //名称マスタよりデータを取得
    function get_name_cd($year, $cd, $sitei = "")
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

    //受験科目数
    function getExamType($year, $applicantdiv, $testdiv, $cntFlg = "") {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = 'L005' ";
        $query .= "     AND NAMECD2 IN ( ";
        $query .= "         SELECT ";
        $query .= "             MEMO_KISO ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_NO_DAT ";
        $query .= "         WHERE ";
        $query .= "                 ENTEXAMYEAR  = '{$year}' ";
        $query .= "             AND APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "             AND TESTDIV      = '{$testdiv}' ";
        $query .= "         GROUP BY ";
        $query .= "             MEMO_KISO ";
        $query .= "     ) ";
        return $query;
    }

    //志望コース
    function getExamcourse($year, $applicantdiv, $testdiv, $examcoursecd = "") {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.EXAMCOURSECD || ':' || T1.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '1' ";//固定
        if (strlen($examcoursecd)) {
            $query .= "     AND T1.EXAMCOURSECD {$examcoursecd} ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //名称マスタより和暦の元号を取得
    function get_calendarno($year, $namecd2 = "") {
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

    //奨学区分
    function getShougaku($year, $applicantdiv) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = 'L025' ";
        $query .= "     AND NAMESPARE1 = '{$applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= " SELECT ";
        $query .= "     APP_B.APPLICANTDIV, ";
        $query .= "     APP_B.EXAMNO, ";
        $query .= "     BASE_D_001.REMARK8 || '-' || BASE_D_001.REMARK9 || '-' || BASE_D_001.REMARK10 AS EXAMCOURSE, ";
        $query .= "     BASE_D_017.REMARK1 || '-' || BASE_D_017.REMARK2 || '-' || BASE_D_017.REMARK3 AS EXAMCOURSE2, ";
        $query .= "     APP_B.TESTDIV, ";
        $query .= "     BASE_D_005.REMARK1 AS SEQ005_R1, ";
        $query .= "     BASE_D_005.REMARK2 AS SEQ005_R2, ";
        $query .= "     BASE_D_005.REMARK5 AS SEQ005_R5, ";
        $query .= "     BASE_D_005.REMARK6 AS SEQ005_R6, ";
        $query .= "     BASE_D_005.REMARK7 AS SEQ005_R7, ";
        $query .= "     APP_B.SHDIV, ";
        //併願校名、併願校合格発表日
        $query .= "     APP_B.RECEPTDATE, ";
        $query .= "     APP_B.TESTDIV1, ";
        $query .= "     APP_B.SPECIAL_MEASURES, ";
        $query .= "     APP_B.NAME, ";
        $query .= "     APP_B.NAME_KANA, ";
        $query .= "     APP_B.SEX, ";
        $query .= "     APP_B.BIRTHDAY, ";
        $query .= "     APP_B.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     APP_B.FS_GRDYEAR, ";
        $query .= "     APP_B.FS_Y, ";
        $query .= "     APP_B.FS_M, ";
        $query .= "     APP_B.FS_GRDDIV, ";
        $query .= "     APP_B.FS_DAY, ";
        $query .= "     APP_ADDR.ZIPCD, ";
        $query .= "     APP_ADDR.ADDRESS1, ";
        $query .= "     APP_ADDR.ADDRESS2, ";
        $query .= "     APP_ADDR.TELNO, ";
        $query .= "     APP_ADDR.GNAME, ";
        $query .= "     APP_ADDR.GKANA, ";
        $query .= "     APP_ADDR.RELATIONSHIP, ";
        $query .= "     APP_ADDR.GZIPCD, ";
        $query .= "     APP_ADDR.GADDRESS1, ";
        $query .= "     APP_ADDR.GADDRESS2, ";
        $query .= "     APP_ADDR.GTELNO, ";
        $query .= "     L007.NAME1, ";
        $query .= "     APP_CONF.CONFIDENTIAL_RPT01, ";
        $query .= "     APP_CONF.CONFIDENTIAL_RPT02, ";
        $query .= "     APP_CONF.CONFIDENTIAL_RPT03, ";
        $query .= "     APP_CONF.CONFIDENTIAL_RPT04, ";
        $query .= "     APP_CONF.CONFIDENTIAL_RPT05, ";
        $query .= "     APP_CONF.CONFIDENTIAL_RPT06, ";
        $query .= "     APP_CONF.CONFIDENTIAL_RPT07, ";
        $query .= "     APP_CONF.CONFIDENTIAL_RPT08, ";
        $query .= "     APP_CONF.CONFIDENTIAL_RPT09, ";
        $query .= "     APP_CONF.CONFIDENTIAL_RPT10, ";
        $query .= "     APP_CONF.TOTAL3, ";
        $query .= "     APP_CONF.TOTAL5, ";
        $query .= "     APP_CONF.TOTAL_ALL, ";
        $query .= "     APP_CONF.KASANTEN_ALL, ";
        $query .= "     APP_CONF.ABSENCE_DAYS, ";
        $query .= "     APP_CONF.ABSENCE_DAYS2, ";
        $query .= "     APP_CONF.ABSENCE_DAYS3, ";
        $query .= "     APP_CONF.ABSENCE_REMARK, ";
        $query .= "     APP_CONF.ABSENCE_REMARK2, ";
        $query .= "     APP_CONF.ABSENCE_REMARK3, ";
        $query .= "     APP_CONF.REMARK1 AS CONFRPT_REMARK1, ";
        $query .= "     BASE_D_004.REMARK1 AS DETAIL4_REMARK1, ";
        $query .= "     BASE_D_004.REMARK9 AS SEQ004_R9, ";
        $query .= "     BASE_D_004.REMARK10 AS SEQ004_R10, ";
        $query .= "     L013.NAME1 AS JUDGEMENT_INFO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT APP_B ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT APP_ADDR ON APP_B.ENTEXAMYEAR = APP_ADDR.ENTEXAMYEAR ";
        $query .= "     AND APP_B.APPLICANTDIV = APP_ADDR.APPLICANTDIV ";
        $query .= "     AND APP_B.EXAMNO = APP_ADDR.EXAMNO ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     V_NAME_MST L007 ON APP_B.ENTEXAMYEAR = L007.YEAR ";
        $query .= "     AND APP_B.ERACD = L007.NAMECD2 ";
        $query .= "     AND L007.NAMECD1 = 'L007' ";
        $query .= " LEFT OUTER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_001 ";
        $query .= "       ON APP_B.ENTEXAMYEAR = BASE_D_001.ENTEXAMYEAR ";
        $query .= "      AND APP_B.APPLICANTDIV = BASE_D_001.APPLICANTDIV ";
        $query .= "      AND APP_B.EXAMNO = BASE_D_001.EXAMNO ";
        $query .= "      AND BASE_D_001.SEQ = '001' ";
        $query .= " LEFT OUTER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_017 ";
        $query .= "       ON APP_B.ENTEXAMYEAR = BASE_D_017.ENTEXAMYEAR ";
        $query .= "      AND APP_B.APPLICANTDIV = BASE_D_017.APPLICANTDIV ";
        $query .= "      AND APP_B.EXAMNO = BASE_D_017.EXAMNO ";
        $query .= "      AND BASE_D_017.SEQ = '017' ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_005 ";
        $query .= "       ON BASE_D_005.ENTEXAMYEAR = APP_B.ENTEXAMYEAR ";
        $query .= "      AND BASE_D_005.APPLICANTDIV = APP_B.APPLICANTDIV ";
        $query .= "      AND BASE_D_005.EXAMNO = APP_B.EXAMNO ";
        $query .= "      AND BASE_D_005.SEQ = '005' ";
        $query .= " LEFT OUTER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_004 ";
        $query .= "       ON APP_B.ENTEXAMYEAR = BASE_D_004.ENTEXAMYEAR ";
        $query .= "      AND APP_B.APPLICANTDIV = BASE_D_004.APPLICANTDIV ";
        $query .= "      AND APP_B.EXAMNO = BASE_D_004.EXAMNO ";
        $query .= "      AND BASE_D_004.SEQ = '004' ";
        $query .= " LEFT OUTER JOIN FINSCHOOL_MST FIN ";
        $query .= "      ON APP_B.FS_CD = FIN.FINSCHOOLCD ";
        $query .= " LEFT OUTER JOIN ENTEXAM_APPLICANTCONFRPT_DAT APP_CONF ";
        $query .= "       ON APP_B.ENTEXAMYEAR = APP_CONF.ENTEXAMYEAR ";
        $query .= "      AND APP_B.APPLICANTDIV = APP_CONF.APPLICANTDIV ";
        $query .= "      AND APP_B.EXAMNO = APP_CONF.EXAMNO ";
        $query .= " LEFT JOIN V_NAME_MST L013 ";
        $query .= "      ON  L013.YEAR = APP_B.ENTEXAMYEAR ";
        $query .= "      AND L013.NAMECD1 = 'L013' ";
        $query .= "      AND L013.NAMECD2 = APP_B.JUDGEMENT ";
        $query .= "      AND L013.NAMECD2 = '4' ";
        $query .= " WHERE ";
        $query .= "     APP_B.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APP_B.APPLICANTDIV = '2' AND ";//2:高校

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= " APP_B.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     APP_B.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= " APP_B.EXAMNO > '".sprintf("%04d", $model->examno)."'";
            $query.= " ORDER BY ";
            $query.= "     APP_B.EXAMNO ASC ";
        } else {
            $query.= " APP_B.EXAMNO = '".$model->examno."'";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //英語見なし得点
    function getEiken($year, $applicantdiv) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = 'L055' ";
        $query .= "     AND NAMESPARE1 = '{$applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出身学校マスタよりデータを取得
    function getFinschoolName($cd)
    {
        $query  = " SELECT  T1.FINSCHOOL_NAME, T1.DISTRICTCD, VALUE(L3.NAME1, '') AS FINSCHOOL_DISTCD_NAME, ";
        $query .= "         case when L4.NAME1 is not null then '(' || L4.NAME1 || ')' else '' end FINSCHOOL_TYPE_NAME ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "         LEFT JOIN NAME_MST L3 ON  L3.NAMECD1 = 'L001' ";
        $query .= "                               AND L3.NAMECD2 = T1.FINSCHOOL_DISTCD ";
        $query .= "         LEFT JOIN NAME_MST L4 ON  L4.NAMECD1 = 'L019' ";
        $query .= "                               AND L4.NAMECD2 = T1.FINSCHOOL_TYPE ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }

    //存在チェック
    function Existdata($model)
    {

        $query  = " SELECT ";
        $query .= "      EXAMNO ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR = '".$model->year."' ";
        $query .= "      AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "      AND EXAMNO = '".$model->examno."'";

        return $query;
    }

    //受験番号範囲取得
    function getEntexamNoDatCnt($db, $model) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV         = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND MEMO_KISO       = '".$model->field["TESTDIV1"]."' ";
        $query .= "     AND '{$model->examno}' BETWEEN EXAMNO_FROM AND EXAMNO_TO ";
        $query .= "     AND COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD = '{$model->field["EXAMCOURSE"]}' ";
        $count = $db->getOne($query);
        if ($count) {
            return $count;
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV         = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND MEMO_KISO       = '".$model->field["TESTDIV1"]."' ";
        $query .= "     AND '{$model->examno}' BETWEEN EXAMNO_FROM AND EXAMNO_TO ";
        $query .= "     AND COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD IS NULL ";
        $query .= "     AND NOT EXISTS ( ";
        $query .= "        SELECT 'X' ";
        $query .= "        FROM ENTEXAM_NO_DAT ";
        $query .= "        WHERE ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "          AND APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "          AND TESTDIV = T1.TESTDIV ";
        $query .= "          AND MEMO_KISO = T1.MEMO_KISO ";
        $query .= "          AND COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD = '{$model->field["EXAMCOURSE"]}' ";
        $query .= "     ) ";
        return $db->getOne($query);
    }

    //--- INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $result = $db->query(knjl511fQuery::get_calendarno($model->year));
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
                    $fs_date = $fs_grdyear. "-".sprintf("%02d",$model->field["FS_M"])."-01";
                    break;
                }
            }
        }

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
        $data["SHDIV"][TEXT]            = $model->field["SHDIV"];
        $data["DESIREDIV"][TEXT]        = "1";
        $data["RECEPTDATE"][TEXT]       = str_replace("/", "-", $model->field["RECEPTDATE"]);
        $data["TESTDIV1"][TEXT]         = $model->field["TESTDIV1"];
        $data["SPECIAL_MEASURES"][TEXT] = $model->field["SPECIAL_MEASURES"];
        $data["NAME"][TEXT]             = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]        = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]              = $model->field["SEX"];
        $data["BIRTHDAY"][TEXT]         = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["FS_CD"][TEXT]            = (strlen($model->field["FS_CD"]) ? sprintf("%07d",$model->field["FS_CD"]) : "");
        $data["FS_ERACD"][TEXT]         = $model->field["FS_ERACD"];
        $data["FS_Y"][TEXT]             = $model->field["FS_Y"]? sprintf("%02d",$model->field["FS_Y"]) : "";
        $data["FS_M"][TEXT]             = $model->field["FS_M"]? sprintf("%02d",$model->field["FS_M"]) : "";
        $data["FS_GRDDIV"][TEXT]        = $model->field["FS_GRDDIV"];
        $data["FS_GRDYEAR"][TEXT]       = $fs_grdyear;
        $data["FS_DAY"][TEXT]           = $fs_date;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
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

        //志望コース1
        knjl511fQuery::insertBaseDetailCourse($db, "001", $model, $model->field["EXAMCOURSE"]);

        //志望コース2
        knjl511fQuery::insertBaseDetailCourse($db, "017", $model, $model->field["EXAMCOURSE2"]);

        //SEQ004(事前相談)
        knjl511fQuery::insertBaseDetail($db, "004", $model, array("9", "10"));
        //SEQ005(英語見なし得点,奨学区分,特別条件,注意を要する,別室受験)
        knjl511fQuery::insertBaseDetail($db, "005", $model, array("1", "2", "5", "6", "7"));

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    function insertBaseDetailCourse($db, $seq, $model, $courseCd) {
        //受験コース
        $course_array = array();
        $course_array = explode("-", $courseCd);
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT ---DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "       AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query.= "       AND EXAMNO = '".$model->examno."'";
        $query.= "       AND SEQ = '{$seq}'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = $seq;
        if ($seq == "001") {
            $data["REMARK8"][TEXT]      = $course_array[0];
            $data["REMARK9"][TEXT]      = $course_array[1];
            $data["REMARK10"][TEXT]     = $course_array[2];
        } else {
            $data["REMARK1"][TEXT]      = $course_array[0];
            $data["REMARK2"][TEXT]      = $course_array[1];
            $data["REMARK3"][TEXT]      = $course_array[2];
        }
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);
    }

    function insertBaseDetail($db, $seq, $model, $nums) {
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT 
        $query = " SELECT * FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "       AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query.= "       AND EXAMNO = '".$model->examno."'";
        $query.= "       AND SEQ = '{$seq}'";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT ---DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "       AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query.= "       AND EXAMNO = '".$model->examno."'";
        $query.= "       AND SEQ = '{$seq}'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT 
        $data = array();
        if (is_array($row)) {
            for ($n = 1; $n <= 10; $n++) {
                $data["REMARK{$n}"][TEXT] = $row["REMARK{$n}"];
            }
        }
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = $seq;

        foreach ($nums as $n) {
            $data["REMARK{$n}"][TEXT]      = $model->field["SEQ{$seq}_R{$n}"];
        }

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);
    }


    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $result = $db->query(knjl511fQuery::get_calendarno($model->year));
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
                    $fs_date = $fs_grdyear. "-".sprintf("%02d",$model->field["FS_M"])."-01";
                    break;
                }
            }
        }

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
        $data["SHDIV"][TEXT]            = $model->field["SHDIV"];
        $data["DESIREDIV"][TEXT]        = "1";
        $data["RECEPTDATE"][TEXT]       = str_replace("/", "-", $model->field["RECEPTDATE"]);
        $data["TESTDIV1"][TEXT]         = $model->field["TESTDIV1"];
        $data["SPECIAL_MEASURES"][TEXT] = $model->field["SPECIAL_MEASURES"];
        $data["NAME"][TEXT]             = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]        = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]              = $model->field["SEX"];
        $data["BIRTHDAY"][TEXT]         = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["FS_CD"][TEXT]            = (strlen($model->field["FS_CD"]) ? sprintf("%07d",$model->field["FS_CD"]) : "");
        $data["FS_ERACD"][TEXT]         = $model->field["FS_ERACD"];
        $data["FS_Y"][TEXT]             = $model->field["FS_Y"]? sprintf("%02d",$model->field["FS_Y"]) : "";
        $data["FS_M"][TEXT]             = $model->field["FS_M"]? sprintf("%02d",$model->field["FS_M"]) : "";
        $data["FS_GRDDIV"][TEXT]        = $model->field["FS_GRDDIV"];
        $data["FS_GRDYEAR"][TEXT]       = $fs_grdyear;
        $data["FS_DAY"][TEXT]           = $fs_date;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $where .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND";
        $where .= "       EXAMNO = '".$model->examno."'";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT ---DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query.= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND";
        $query.= "       EXAMNO = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
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

        //志望コース1
        knjl511fQuery::insertBaseDetailCourse($db, "001", $model, $model->field["EXAMCOURSE"]);

        //志望コース2
        knjl511fQuery::insertBaseDetailCourse($db, "017", $model, $model->field["EXAMCOURSE2"]);

        //SEQ004(事前相談)
        knjl511fQuery::insertBaseDetail($db, "004", $model, array("9", "10"));
        //SEQ005(英語見なし得点,奨学区分,特別条件,注意を要する,別室受験)
        knjl511fQuery::insertBaseDetail($db, "005", $model, array("1", "2", "5", "6", "7"));

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
        $query .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_SCORE_DAT
        $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "                APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND";
        $query .= "                EXAMNO      = '".$model->examno."'";
        $query .= "       )";
        $db->query($query);

        //ENTEXAM_RECEPT_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "                APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND";
        $query .= "                EXAMNO      = '".$model->examno."'";
        $query .= "       )";
        $db->query($query);

        //ENTEXAM_RECEPT_DAT
        $query  = "DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>