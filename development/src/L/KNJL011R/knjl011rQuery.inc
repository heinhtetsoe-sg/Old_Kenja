<?php

require_once('for_php7.php');
class knjl011rQuery extends Query 
{

    //MAX受験番号＋１を取得
    function getMaxExamnoQuery($model)
    {
        $db = Query::dbCheckOut();
        if ($model->field["TESTDIV"] == "1") {
            $examArray = array("1" => "S1",
                               "2" => "S1",
                               "3" => "S2",
                               "4" => "S3"
                              );
            $getLen = "2";
        } else {
            $examArray = array("1" => "1",
                               "2" => "1",
                               "3" => "2",
                               "4" => "3"
                              );
            $getLen = "1";
        }
        $retMinWish = "";
        for ($code = 1; $code <= 4; $code++) {
            if (strlen($model->field["TESTDIV".$code])) {
                $retMinWish = $examArray[$code];
                break;
            }
        }

        $query  = " SELECT ";
        $query .= "     MAX(EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND SUBSTR(EXAMNO, 1, {$getLen}) = '{$retMinWish}' ";

        $maxExam = $db->getOne($query);
        if ($model->field["TESTDIV"] == "1") {
            if (strlen($maxExam)) {
                $maxExam = str_replace($retMinWish, "", $maxExam);
                $maxExam++;
                $maxExam = $retMinWish.sprintf("%03d", $maxExam);
            } else {
                $maxExam = $retMinWish."001";
            }
        } else {
            if (strlen($maxExam)) {
                $maxExam++;
            } else {
                $maxExam = $retMinWish."0001";
            }
        }

        Query::dbCheckIn($db);
        return $maxExam;
    }

    //名称マスタよりデータを取得
    function get_name_cd($model, $cd, $namecd2="")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        if ($namecd2 != '') {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分マスタ
    function getCourseMst($ObjYear)
    {
        $query .= " SELECT ";
        $query .= "     EXAMCOURSECD, ";
        $query .= "     MAX(EXAMCOURSE_NAME) AS EXAMCOURSE_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '1' ";
        $query .= " GROUP BY ";
        $query .= "     EXAMCOURSECD ";
        $query .= " ORDER BY ";
        $query .= "     EXAMCOURSECD ";
        return $query;
    }

    //名称
    function testDayCheck($ObjYear,$testdivArray)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     TESTDIV AS NAMECD2, ";
        $query .= "     NAME AS NAME1, ";
        $query .= "     TESTDAY AS NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$ObjYear."' AND ";
        $query .= "     TESTDAY in (SELECT ";
        $query .= "                         TESTDAY ";
        $query .= "                     FROM ";
        $query .= "                         ENTEXAM_TESTDIV_MST ";
        $query .= "                     WHERE ";
        $query .= "                         ENTEXAMYEAR = '".$ObjYear."' AND ";
        $query .= "                         TESTDIV IN ('" . implode("','", $testdivArray) . "') ";
        $query .= "                     GROUP BY ";
        $query .= "                         TESTDAY ";
        $query .= "                     HAVING ";
        $query .= "                         1 < COUNT(*) ";
        $query .= "                     ) ";
        $query .= " ORDER BY ";
        $query .= "     TESTDIV ";

        $testDayCheck = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $testDayCheck[$row["NAMECD2"]] = $row["NAME1"];
        }

        Query::dbCheckIn($db);
        return $testDayCheck;
    }

    //名称マスタより和暦の元号を取得
    function get_calendarno($year)
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

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model)
    {
        $db = Query::dbCheckOut();

        $query .= " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.SHDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T5.REMARK1 AS TESTDIV1, ";
        $query .= "     T5.REMARK2 AS TESTDIV2, ";
        $query .= "     T5.REMARK3 AS TESTDIV3, ";
        $query .= "     T5.REMARK4 AS TESTDIV4, ";
        $query .= "     T6.REMARK1 AS JIZEN_PAGE, ";
        $query .= "     T6.REMARK2 AS JIZEN_SEQ, ";
        $query .= "     T6.REMARK1 || '-' || T6.REMARK2 AS JIZEN, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.ERACD, ";
        $query .= "     T1.BIRTH_Y, ";
        $query .= "     T1.BIRTH_M, ";
        $query .= "     T1.BIRTH_D, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     T1.FS_AREA_CD, ";
        $query .= "     T1.FS_GRDYEAR, ";
        $query .= "     T1.FS_Y, ";
        $query .= "     T1.FS_M, ";
        $query .= "     T1.DESIREDIV, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
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
        $query .= "     T4.TOTAL_ALL, ";
        $query .= "     T4.KASANTEN_ALL, ";
        $query .= "     T4.ABSENCE_DAYS3 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT T2 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR  = T2.ENTEXAMYEAR AND ";
        $query .= "     T1.APPLICANTDIV = T2.APPLICANTDIV AND ";
        $query .= "     T1.EXAMNO       = T2.EXAMNO ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     V_NAME_MST T3 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = T3.YEAR AND ";
        $query .= "     T1.ERACD = T3.NAMECD2 AND ";
        $query .= "     T3.NAMECD1 = 'L007' ";
        $query .= " LEFT OUTER JOIN  ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT T4 ";
        $query .= " ON ";
        $query .= "     T1.entexamyear  = T4.entexamyear AND ";
        $query .= "     T1.APPLICANTDIV = T4.APPLICANTDIV AND ";
        $query .= "     T1.examno       = T4.examno ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT T5 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR  = T5.ENTEXAMYEAR AND ";
        $query .= "     T1.APPLICANTDIV = T5.APPLICANTDIV AND ";
        $query .= "     T1.EXAMNO       = T5.EXAMNO AND ";
        $query .= "     T5.SEQ          = '001' ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT T6 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR  = T6.ENTEXAMYEAR AND ";
        $query .= "     T1.APPLICANTDIV = T6.APPLICANTDIV AND ";
        $query .= "     T1.EXAMNO       = T6.EXAMNO AND ";
        $query .= "     T6.SEQ          = '002' ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     FINSCHOOL_MST FIN ";
        $query .= " ON ";
        $query .= "     T1.FS_CD = FIN.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";

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

    //出身学校マスタよりデータを取得
    function getFinschoolName($cd)
    {
        $query  = " SELECT  T1.FINSCHOOL_NAME, T1.DISTRICTCD ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }

    //事前データ取得
    function getEntexamApplicantBeforeDat($model, $year, $applicantdiv, $testdiv, $jizen_page, $jizen_seq)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     L1.ABBV3 AS JIZEN_TEST_CD, ";
        $query .= "     L2.EXAMCOURSE_MARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANT_BEFORE_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ON L1.NAMECD1 = 'L004' ";
        $query .= "                            AND L1.NAMECD2 = T1.TESTDIV ";
        $query .= "                            AND L1.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L2 ON L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                    AND L2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                    AND L2.TESTDIV = T1.TESTDIV ";
        $query .= "                                    AND L2.COURSECD = T1.BEFORE_COURSECD ";
        $query .= "                                    AND L2.MAJORCD = T1.BEFORE_MAJORCD ";
        $query .= "                                    AND L2.EXAMCOURSECD = T1.BEFORE_EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR   = '".$year."' ";
        $query .= " AND T1.APPLICANTDIV  = '".$applicantdiv."' ";
        $query .= " AND T1.TESTDIV       = '".$testdiv."' ";
        $query .= " AND T1.BEFORE_PAGE   = '".$jizen_page."' ";
        $query .= " AND T1.BEFORE_SEQ    = '".$jizen_seq."' ";

        return $query;
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
    function CheckDate($db, $model)
    {
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
    function getWishMst($model)
    {
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

    //--- INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $result = $db->query(knjl011rQuery::get_calendarno($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
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

        //志望区分
        $desirediv = "E";
        $wishArray = array();
        $result = $db->query(knjl011rQuery::getWishMst($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $wishArray[$row["DESIREDIV"]][$row["EXAMCOURSECD"]] = $row["EXAMCOURSECD"] * 1;
        }
        $result->free();
        foreach ($wishArray as $key => $val) {
            $wishFlg = true;
            for ($code = 1; $code <= 4; $code++) {
                if ($model->field["TESTDIV".$code]) {
                    if (!array_search($model->field["TESTDIV".$code], $val)) {
                        $wishFlg = false;
                    }
                } else {
                    if (array_search($code, $val)) {
                        $wishFlg = false;
                    }
                }
            }
            if ($wishFlg) {
                $desirediv = $key;
                break;
            }
        }

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
        $data["SHDIV"][TEXT]         = '1';
        $data["DESIREDIV"][TEXT]     = $desirediv;
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
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
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
        //半角を全角変換
        $model->field["GKANA"] = mb_convert_kana($model->field["GKANA"], "K", "UTF-8");
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
        $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "001";
        for ($code = 1; $code <= 4; $code++) {
            $data["REMARK".$code][TEXT] = strlen($model->field["TESTDIV".$code]) ? "1" : "";
        }
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DAT
        $setKansan = round(($model->field["TOTAL_ALL"] * 90 / 135), 0);
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["TOTAL_ALL"][NUMBER]      = $model->field["TOTAL_ALL"];
        $data["KASANTEN_ALL"][NUMBER]   = $setKansan;
        $data["ABSENCE_DAYS3"][NUMBER]  = $model->field["ABSENCE_DAYS3"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $result = $db->query(knjl011rQuery::get_calendarno($model->year));
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

        //志望区分
        $desirediv = "E";
        $wishArray = array();
        $result = $db->query(knjl011rQuery::getWishMst($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $wishArray[$row["DESIREDIV"]][$row["EXAMCOURSECD"]] = $row["EXAMCOURSECD"] * 1;
        }
        $result->free();
        foreach ($wishArray as $key => $val) {
            $wishFlg = true;
            for ($code = 1; $code <= 4; $code++) {
                if ($model->field["TESTDIV".$code]) {
                    if (!array_search($model->field["TESTDIV".$code], $val)) {
                        $wishFlg = false;
                    }
                } else {
                    if (array_search($code, $val)) {
                        $wishFlg = false;
                    }
                }
            }
            if ($wishFlg) {
                $desirediv = $key;
                break;
            }
        }

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
        $data["SHDIV"][TEXT]         = '1';
        $data["DESIREDIV"][TEXT]     = $desirediv;
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
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["ZIPCD"][TEXT]         = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]      = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]      = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]         = $model->field["TELNO"];
        $data["GNAME"][TEXT]         = $model->field["GNAME"];
        //半角を全角変換
        $model->field["GKANA"] = mb_convert_kana($model->field["GKANA"], "K", "UTF-8");
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

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT ---DELETE
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."'";
        $query .= "   AND SEQ          = '001'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "001";
        for ($code = 1; $code <= 4; $code++) {
            $data["REMARK".$code][TEXT] = strlen($model->field["TESTDIV".$code]) ? "1" : "";
        }
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DAT ---DELETE
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DAT
        $setKansan = round(($model->field["TOTAL_ALL"] * 90 / 135), 0);
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["TOTAL_ALL"][NUMBER]      = $model->field["TOTAL_ALL"];
        $data["KASANTEN_ALL"][NUMBER]   = $setKansan;
        $data["ABSENCE_DAYS3"][NUMBER]  = $model->field["ABSENCE_DAYS3"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
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