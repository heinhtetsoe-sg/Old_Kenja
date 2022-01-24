<?php

require_once('for_php7.php');

class knjl010rQuery extends Query {
    //名称マスタよりデータを取得
    function get_name_cd($year, $cd, $namecd2='') {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$cd}' ";
        if ($namecd2 != '') {
            $query .= "     AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより和暦の元号を取得
    function get_calendarno($year) {
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

        return $query;
    }

    //塾名
    function getPriSchoolName($priSchoolCd) {
        $query  = " SELECT ";
        $query .= "     PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD = '{$priSchoolCd}' ";
        return $query;
    }
    //教室名
    function getPriSchoolClassName($priSchoolCd, $priSchoolClassCd) {
        $query  = " SELECT ";
        $query .= "     PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "         PRISCHOOLCD        = '{$priSchoolCd}' ";
        $query .= "     AND PRISCHOOL_CLASS_CD = '{$priSchoolClassCd}' ";
        return $query;
    }

    //メインデータ取得
    function get_edit_data($model) {
        $db = Query::dbCheckOut();

        $query .= " SELECT ";
        $query .= "     BASE.APPLICANTDIV, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.SHDIV, ";
        $query .= "     BASE.TESTDIV, ";
        $query .= "     BUN010.REMARK1 AS TESTDIV_1, ";
        $query .= "     BUN010.REMARK2 AS TESTDIV_2, ";
        $query .= "     BUN012.REMARK1 AS EXAMNO_1, ";
        $query .= "     BUN012.REMARK2 AS EXAMNO_2, ";
        $query .= "     BUN013.REMARK1 AS SHDIV1, ";
        $query .= "     BUN013.REMARK2 AS SHDIV2, ";
        $query .= "     BUN014.REMARK1 AS INNER_PROMISE_1, ";
        $query .= "     BUN014.REMARK2 AS INNER_PROMISE_2, ";
        $query .= "     BD002.REMARK1 AS JIZEN_PAGE, ";
        $query .= "     BD002.REMARK2 AS JIZEN_SEQ, ";
        $query .= "     BD002.REMARK1 || '-' || BD002.REMARK2 AS JIZEN, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BASE.SEX, ";
        $query .= "     BASE.ERACD, ";
        $query .= "     BASE.BIRTH_Y, ";
        $query .= "     BASE.BIRTH_M, ";
        $query .= "     BASE.BIRTH_D, ";
        $query .= "     BASE.BIRTHDAY, ";
        $query .= "     BASE.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     BD008.REMARK1 as PRISCHOOLCD1, ";
        $query .= "     BASE.FS_AREA_CD, ";
        $query .= "     BASE.FS_GRDYEAR, ";
        $query .= "     BASE.FS_Y, ";
        $query .= "     BASE.FS_M, ";
        $query .= "     BASE.DESIREDIV, ";
        $query .= "     BASE.REMARK1, ";
        $query .= "     BASE.REMARK2, ";
        $query .= "     ADDR.ZIPCD, ";
        $query .= "     ADDR.ADDRESS1, ";
        $query .= "     ADDR.ADDRESS2, ";
        $query .= "     ADDR.TELNO, ";
        $query .= "     ADDR.GNAME, ";
        $query .= "     ADDR.GKANA, ";
        $query .= "     ADDR.RELATIONSHIP, ";
        $query .= "     ADDR.GZIPCD, ";
        $query .= "     ADDR.GADDRESS1, ";
        $query .= "     ADDR.GADDRESS2, ";
        $query .= "     ADDR.GTELNO, ";
        $query .= "     ADDR.EMERGENCYTELNO, ";
        $query .= "     L007.NAME1, ";
        $query .= "     CF006.REMARK5 as ABSENCE_5, ";
        $query .= "     CF006.REMARK6 as ABSENCE_6 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON BASE.ENTEXAMYEAR  = ADDR.ENTEXAMYEAR ";
        $query .= "                                             AND BASE.APPLICANTDIV = ADDR.APPLICANTDIV ";
        $query .= "                                             AND BASE.EXAMNO       = ADDR.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST L007 ON BASE.ENTEXAMYEAR = L007.YEAR ";
        $query .= "                              AND BASE.ERACD       = L007.NAMECD2 ";
        $query .= "                              AND L007.NAMECD1     = 'L007' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CF006 ON BASE.ENTEXAMYEAR  = CF006.ENTEXAMYEAR ";
        $query .= "                                                        AND BASE.APPLICANTDIV = CF006.APPLICANTDIV ";
        $query .= "                                                        AND BASE.EXAMNO       = CF006.EXAMNO ";
        $query .= "                                                        AND CF006.SEQ         = '006' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD002 ON BASE.ENTEXAMYEAR  = BD002.ENTEXAMYEAR ";
        $query .= "                                                     AND BASE.APPLICANTDIV = BD002.APPLICANTDIV ";
        $query .= "                                                     AND BASE.EXAMNO       = BD002.EXAMNO ";
        $query .= "                                                     AND BD002.SEQ         = '002' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD008 ON BASE.ENTEXAMYEAR  = BD008.ENTEXAMYEAR ";
        $query .= "                                                     AND BASE.APPLICANTDIV = BD008.APPLICANTDIV ";
        $query .= "                                                     AND BASE.EXAMNO       = BD008.EXAMNO ";
        $query .= "                                                     AND BD008.SEQ         = '008' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN010 ON BUN010.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                          AND BUN010.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                          AND BUN010.EXAMNO       = BASE.EXAMNO ";
        $query .= "                                                          AND BUN010.SEQ          = '010' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN012 ON BUN012.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                          AND BUN012.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                          AND BUN012.EXAMNO       = BASE.EXAMNO ";
        $query .= "                                                          AND BUN012.SEQ          = '012' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN013 ON BUN013.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                          AND BUN013.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                          AND BUN013.EXAMNO       = BASE.EXAMNO ";
        $query .= "                                                          AND BUN013.SEQ          = '013' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN014 ON BUN014.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                          AND BUN014.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                          AND BUN014.EXAMNO       = BASE.EXAMNO ";
        $query .= "                                                          AND BUN014.SEQ          = '014' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON BASE.FS_CD = FIN.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "     BASE.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= " BASE.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     BASE.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= " BASE.EXAMNO > '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     BASE.EXAMNO ASC ";
        } else {
            $query.= " BASE.EXAMNO = '".$model->examno."'";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //出身学校マスタよりデータを取得
    function getFinschoolName($cd) {
        $query  = " SELECT  T1.FINSCHOOL_NAME, T1.DISTRICTCD ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }

    //データ重複チェック
    function CheckData($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T2.NAME1 ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "  LEFT OUTER JOIN ";
        $query .= "      V_NAME_MST T2 ";
        $query .= "  ON ";
        $query .= "      T1.ENTEXAMYEAR = T2.YEAR AND ";
        $query .= "      T1.APPLICANTDIV = T2.NAMECD2 AND ";
        $query .= "      T2.NAMECD1 = 'L003' ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "      EXAMNO = '".$model->examno."' AND ";
        $query .= "      APPLICANTDIV <> '".$model->applicantdiv."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //存在チェック
    function Existdata($model) {

        $query = "  SELECT
                        EXAMNO
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT
                    WHERE
                            ENTEXAMYEAR  = '".$model->year."'
                        AND APPLICANTDIV = '".$model->applicantdiv."'
                        AND EXAMNO = '".$model->examno."'";

        return $query;
    }

    //該当日付取得
    function CheckDate($db, $model) {
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

        return $row;
    }

    //志望区分マスタ
    function getWishMst($model) {
        $query  = " SELECT ";
        $query .= "     DESIREDIV, ";
        $query .= "     EXAMCOURSECD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_WISHDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     DESIREDIV, ";
        $query .= "     WISHNO ";

        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $result = $db->query(knjl010rQuery::get_calendarno($model->year));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $eracd[] = $row["NAMECD2"];
            $defyear[] = $row["NAMESPARE1"];
        }
        $result->free();

        $birthday = "";
        if (isset($model->field["ERACD"])) {
            for ($i=0; $i<get_count($eracd); $i++) {
                if ($model->field["ERACD"] == $eracd[$i]) {
                    $syear = ((int)$defyear[$i]) + ((int)$model->field["BIRTH_Y"] - 1);
                    $birthday = $syear. "-".sprintf("%02d",$model->field["BIRTH_M"])."-".sprintf("%02d",$model->field["BIRTH_D"]);
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
                    $fs_date = $fs_grdyear. "-".sprintf("%02d",$model->field["FS_M"])."-01";
                    break;
                }
            }
        }

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["NAME"][TEXT]          = $model->field["NAME"];
        //半角を全角変換
        $model->field["NAME_KANA"] = mb_convert_kana($model->field["NAME_KANA"], "K", "UTF-8");
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["ERACD"][TEXT]         = $model->field["ERACD"];
        $data["BIRTH_Y"][TEXT]       = $model->field["BIRTH_Y"]? sprintf("%02d",$model->field["BIRTH_Y"]) : "";
        $data["BIRTH_M"][TEXT]       = $model->field["BIRTH_M"]? sprintf("%02d",$model->field["BIRTH_M"]) : "";
        $data["BIRTH_D"][TEXT]       = $model->field["BIRTH_D"]? sprintf("%02d",$model->field["BIRTH_D"]) : "";
        $data["BIRTHDAY"][TEXT]      = $birthday;
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"]) ? sprintf("%07d",$model->field["FS_CD"]) : "");
        $data["FS_AREA_CD"][TEXT]    = (strlen($model->field["FS_AREA_CD"]) ? sprintf("%02d",$model->field["FS_AREA_CD"]) : ""); //2005.12.28 minei
        $data["FS_ERACD"][TEXT]      = $model->field["FS_ERACD"];
        $data["FS_Y"][TEXT]          = $model->field["FS_Y"]? sprintf("%02d",$model->field["FS_Y"]) : "";
        $data["FS_M"][TEXT]          = $model->field["FS_M"]? sprintf("%02d",$model->field["FS_M"]) : "";
        $data["FS_GRDYEAR"][TEXT]    = $fs_grdyear;
        $data["FS_DAY"][TEXT]        = $fs_date;
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]       = $model->field["REMARK2"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $where .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "   AND EXAMNO       = '".$model->examno."'";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT ---DELETE
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT ---INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]       = $model->year;
        $data["APPLICANTDIV"][TEXT]      = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]            = $model->examno;
        $data["ZIPCD"][TEXT]             = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]          = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]          = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]             = $model->field["TELNO"];
        $data["GNAME"][TEXT]             = $model->field["GNAME"];
        //半角を全角変換
        $model->field["GKANA"]           = mb_convert_kana($model->field["GKANA"], "K", "UTF-8");
        $data["GKANA"][TEXT]             = $model->field["GKANA"];
        $data["GZIPCD"][TEXT]            = $model->field["GZIPCD"];
        $data["GADDRESS1"][TEXT]         = $model->field["GADDRESS1"];
        $data["GADDRESS2"][TEXT]         = $model->field["GADDRESS2"];
        $data["GTELNO"][TEXT]            = $model->field["GTELNO"];
        $data["RELATIONSHIP"][TEXT]      = $model->field["RELATIONSHIP"];
        $data["EMERGENCYTELNO"][TEXT]    = $model->field["EMERGENCYTELNO"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][FUNC]           = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT ---DELETE
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."'";
        $query .= "   AND SEQ          = '008'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "008";
        $data["REMARK1"][TEXT]      = $model->field["PRISCHOOLCD1"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT
        $seqArr = array('010' => 'TESTDIV_',
                        '012' => 'EXAMNO_',
                        '013' => 'SHDIV',
                        '014' => 'INNER_PROMISE_'
                        );

        foreach ($seqArr as $seq => $fieldName) {
            $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT ";
            $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
            $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
            $query .= "   AND EXAMNO       = '".$model->examno."'";
            $query .= "   AND SEQ          = '{$seq}'";
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = $seq;
            foreach ($model->testDivArr as $key => $num) {
                $data["REMARK{$num}"][TEXT]      = ($model->field["TESTDIV_{$num}"] == $num) ? $model->field[$fieldName.$num]: '';
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT");
            $db->query($query);
        }

        //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ---DELETE
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."' ";
        $query .= "   AND SEQ          = '006' ";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["SEQ"][TEXT]              = "006";
        $data["REMARK5"][TEXT]          = $model->field["ABSENCE_5"];
        $data["REMARK6"][TEXT]          = $model->field["ABSENCE_6"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_SCORE_DAT
        $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "            AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "            AND EXAMNO       = '".$model->examno."'";
        $query .= "       )";
        $db->query($query);

        //ENTEXAM_JUDGE_TMP
        $query  = " DELETE FROM ENTEXAM_JUDGE_TMP ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_RECEPT_DAT
        $query  = "DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_DESIRE_DAT
        $query  = "DELETE FROM ENTEXAM_DESIRE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO      = '".$model->examno."'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>