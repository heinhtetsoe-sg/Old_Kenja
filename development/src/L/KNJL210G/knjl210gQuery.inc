<?php

require_once('for_php7.php');

class knjl210gQuery extends Query
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
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";//入試制度
        $query .= "     AND TESTDIV      = '{$model->field["TESTDIV"]}' ";//入試区分

        $maxExam = $db->getOne($query);
        if (strlen($maxExam)) {
            $maxExam++;
        } else {
            $maxExam = '00000001';
        }

        Query::dbCheckIn($db);
        return sprintf("%08d", $maxExam);
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
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //学年取得
    public function getGrade($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    T1.GRADE AS VALUE, ";
        $query .= "    T1.GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".$model->year."' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

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
        $query .= "     VALUE DESC ";
        return $query;
    }

    //学科
    public function getMajorMst()
    {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.MAJORCD AS VALUE, ";
        $query .= "     T1.MAJORCD || ':' || T1.MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     MAJOR_MST T1 ";
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
        $query .= "     BASE.APPLICANTDIV, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.TESTDIV, ";
        $query .= "     S021.REMARK1 AS GRADE, ";
        $query .= "     S001.REMARK8 || '-' || S001.REMARK9 || '-' || S001.REMARK10 AS EXAMCOURSE, ";
        $query .= "     S017.REMARK1 || '-' || S017.REMARK2 || '-' || S017.REMARK3 AS EXAMCOURSE2, ";
        $query .= "     BASE.RECEPTDATE, ";
        //受験料
        $query .= "     MONEY.EXAM_PAY_DIV, ";
        $query .= "     MONEY.EXAM_PAY_DATE, ";
        $query .= "     MONEY.EXAM_PAY_CHAK_DATE, ";
        //志願者情報
        $query .= "     BASE.SPECIAL_REASON_DIV, ";
        $query .= "     BASE.TESTDIV1 AS GANSHO_YUUSOU, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BASE.SEX, ";
        $query .= "     BASE.BIRTHDAY, ";
        $query .= "     BASE.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     case ";
        $query .= "         when FIN.FINSCHOOLCD is null then '　　　　　' || '（' || value(S005.REMARK1, '') || '）' ";
        $query .= "         else '' ";
        $query .= "     end as CSV_FS_NAME, ";
        $query .= "     BASE.FS_GRDYEAR, ";
        $query .= "     BASE.FS_ERACD, ";
        $query .= "     L007.NAME1 AS FS_WNAME, ";
        $query .= "     BASE.FS_Y, ";
        $query .= "     BASE.FS_M, ";
        $query .= "     BASE.FS_GRDDIV, ";
        $query .= "     BASE.FS_DAY, ";
        $query .= "     ADDR.ZIPCD, ";
        $query .= "     ADDR.ADDRESS1, ";
        $query .= "     ADDR.ADDRESS2, ";
        $query .= "     ADDR.TELNO, ";
        //本校第一志望等情報
        $query .= "     BASE.SHDIV, ";
        $query .= "     S016.REMARK1 as REMARK1_016, ";
        $query .= "     S016.REMARK2 as REMARK2_016, ";
        $query .= "     S016.REMARK3 as REMARK3_016, ";
        $query .= "     S029.REMARK1 as REMARK1_029, ";
        $query .= "     S029.REMARK2 as REMARK2_029, ";
        $query .= "     S029.REMARK10 as REMARK10_029, ";
        //保護者情報
        $query .= "     ADDR.GNAME, ";
        $query .= "     ADDR.GKANA, ";
        $query .= "     ADDR.RELATIONSHIP, ";
        $query .= "     ADDR.GZIPCD, ";
        $query .= "     ADDR.GADDRESS1, ";
        $query .= "     ADDR.GADDRESS2, ";
        $query .= "     ADDR.GTELNO, ";
        $query .= "     ADDR.EMERGENCYCALL, ";
        //本校第一志望等情報
        $query .= "     S018.REMARK1 as REMARK1_018, ";
        $query .= "     S018.REMARK3 as REMARK3_018, ";
        $query .= "     S018.REMARK5 as REMARK5_018, ";
        $query .= "     S018.REMARK6 as REMARK6_018, ";
        $query .= "     S018.REMARK7 as REMARK7_018, ";
        //内申情報
        foreach ($model->l008Arr as $key => $abbv) {
            $query .= "     CONF.CONFIDENTIAL_RPT{$key}, ";
        }
        $query .= "     CONF.TOTAL5, ";
        $query .= "     CONF.TOTAL_ALL, ";
        $query .= "     CONF.ABSENCE_DAYS3, ";
        //志望理由
        $query .= "     S032.REMARK10 as REMARK10_032, ";
        //欠席
        $query .= "     L013.NAME1 AS JUDGEMENT_INFO ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                             AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                             AND ADDR.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON BASE.FS_CD = FIN.FINSCHOOLCD ";
        $query .= "     LEFT JOIN V_NAME_MST L013 ON L013.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "                              AND L013.NAMECD1 = 'L013' ";
        $query .= "                              AND L013.NAMECD2 = BASE.JUDGEMENT ";
        $query .= "                              AND L013.NAMECD2 = '4' "; //欠席者
        $query .= "     LEFT JOIN V_NAME_MST L007 ON L007.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "                              AND L007.NAMECD1 = 'L007' ";
        $query .= "                              AND L007.NAMECD2 = BASE.FS_ERACD ";
        //受験料
        $query .= "     LEFT JOIN ENTEXAM_MONEY_DAT MONEY ON MONEY.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                      AND MONEY.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                      AND MONEY.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S001 ON S001.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                    AND S001.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                    AND S001.EXAMNO       = BASE.EXAMNO ";
        $query .= "                                                    AND S001.SEQ          = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S005 ON S005.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                    AND S005.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                    AND S005.EXAMNO       = BASE.EXAMNO ";
        $query .= "                                                    AND S005.SEQ          = '005' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S016 ON S016.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                    AND S016.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                    AND S016.EXAMNO       = BASE.EXAMNO ";
        $query .= "                                                    AND S016.SEQ          = '016' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S017 ON S017.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                    AND S017.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                    AND S017.EXAMNO       = BASE.EXAMNO ";
        $query .= "                                                    AND S017.SEQ          = '017' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S018 ON S018.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                    AND S018.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                    AND S018.EXAMNO       = BASE.EXAMNO ";
        $query .= "                                                    AND S018.SEQ          = '018' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S021 ON S021.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                    AND S021.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                    AND S021.EXAMNO       = BASE.EXAMNO ";
        $query .= "                                                    AND S021.SEQ          = '021' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S029 ON S029.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                    AND S029.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                    AND S029.EXAMNO       = BASE.EXAMNO ";
        $query .= "                                                    AND S029.SEQ          = '029' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S032 ON S032.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                    AND S032.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                    AND S032.EXAMNO       = BASE.EXAMNO ";
        $query .= "                                                    AND S032.SEQ          = '032' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ON CONF.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                AND CONF.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                AND CONF.EXAMNO       = BASE.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->applicantdiv."' ";
        if ($model->field["TESTDIV"] != '') {
            $query .= "     AND TESTDIV      = '{$model->field["TESTDIV"]}' ";
        }

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= "      AND BASE.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     BASE.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= "      AND BASE.EXAMNO > '".sprintf("%04d", $model->examno)."'";
            $query.= " ORDER BY ";
            $query.= "     BASE.EXAMNO ASC ";
        } else {
            $query.= "      AND BASE.EXAMNO = '".$model->examno."'";
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

    /************/
    /* 更新処理 */
    /************/
    //存在チェック
    public function existdata($model)
    {
        $query  = " SELECT ";
        $query .= "     EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND EXAMNO       = '".$model->examno."' ";

        return $query;
    }

    //カウント取得
    public function getCntData($table, $year, $applicantDiv, $examNo, $seq = '')
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR   = '{$year}' ";
        $query .= "     AND APPLICANTDIV  = '{$applicantDiv}' ";
        $query .= "     AND EXAMNO        = '{$examNo}' ";
        if ($seq != '') {
            $query .= " AND SEQ           = '{$seq}' ";
        }

        return $query;
    }

    //追加・更新
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $result = $db->query(knjl210gQuery::getCalendarno($model->year));
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

        $setYear   = $model->year;
        $setAppDiv = $model->applicantdiv;
        $setExamNo = $model->examno;

        //件数取得
        $baseCnt  = $db->getOne(knjl210gQuery::getCntData('ENTEXAM_APPLICANTBASE_DAT', $setYear, $setAppDiv, $setExamNo));
        $addrCnt  = $db->getOne(knjl210gQuery::getCntData('ENTEXAM_APPLICANTADDR_DAT', $setYear, $setAppDiv, $setExamNo));
        $moneyCnt = $db->getOne(knjl210gQuery::getCntData('ENTEXAM_MONEY_DAT', $setYear, $setAppDiv, $setExamNo));
        $s001Cnt  = $db->getOne(knjl210gQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $setYear, $setAppDiv, $setExamNo, '001'));
        $s016Cnt  = $db->getOne(knjl210gQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $setYear, $setAppDiv, $setExamNo, '016'));
        $s017Cnt  = $db->getOne(knjl210gQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $setYear, $setAppDiv, $setExamNo, '017'));
        $s018Cnt  = $db->getOne(knjl210gQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $setYear, $setAppDiv, $setExamNo, '018'));
        $s021Cnt  = $db->getOne(knjl210gQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $setYear, $setAppDiv, $setExamNo, '021'));
        $s029Cnt  = $db->getOne(knjl210gQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $setYear, $setAppDiv, $setExamNo, '029'));
        $s032Cnt  = $db->getOne(knjl210gQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $setYear, $setAppDiv, $setExamNo, '032'));

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]          = $setYear;
        $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
        $data["EXAMNO"][TEXT]               = $model->examno;
        $data["TESTDIV"][TEXT]              = $model->field["TESTDIV"];
        $data["SHDIV"][TEXT]                = $model->field["SHDIV"];
        $data["DESIREDIV"][TEXT]            = "1";
        $data["RECEPTDATE"][TEXT]           = str_replace("/", "-", $model->field["RECEPTDATE"]);
        $data["SPECIAL_REASON_DIV"][TEXT]   = $model->field["SPECIAL_REASON_DIV"];
        $data["TESTDIV1"][TEXT]             = $model->field["GANSHO_YUUSOU"];
        $data["NAME"][TEXT]                 = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]            = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]                  = $model->field["SEX"];
        $data["BIRTHDAY"][TEXT]             = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["FS_CD"][TEXT]                = (strlen($model->field["FS_CD"]) ? sprintf("%07d", $model->field["FS_CD"]) : "");
        $data["FS_ERACD"][TEXT]             = $model->field["FS_ERACD"];
        $data["FS_Y"][TEXT]                 = $model->field["FS_Y"]? sprintf("%02d", $model->field["FS_Y"]) : "";
        $data["FS_M"][TEXT]                 = $model->field["FS_M"]? sprintf("%02d", $model->field["FS_M"]) : "";
        $data["FS_GRDDIV"][TEXT]            = $model->field["FS_GRDDIV"];
        $data["FS_GRDYEAR"][TEXT]           = $fs_grdyear;
        $data["FS_DAY"][TEXT]               = $fs_date;

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        if ($baseCnt == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$setYear."' ";
            $where .= "   AND APPLICANTDIV  = '".$setAppDiv."'";
            $where .= "   AND EXAMNO        = '".$setExamNo."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        }
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $setYear;
        $data["APPLICANTDIV"][TEXT]  = $model->applicantdiv;
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
        $data["EMERGENCYCALL"][TEXT] = $model->field["EMERGENCYCALL"];
        $data["RELATIONSHIP"][TEXT]  = $model->field["RELATIONSHIP"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        if ($addrCnt == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$setYear."' ";
            $where .= "   AND APPLICANTDIV  = '".$setAppDiv."'";
            $where .= "   AND EXAMNO        = '".$setExamNo."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
        }
        $db->query($query);

        //ENTEXAM_MONEY_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]          = $setYear;
        $data["APPLICANTDIV"][TEXT]         = $setAppDiv;
        $data["EXAMNO"][TEXT]               = $setExamNo;
        $data["EXAM_PAY_DIV"][TEXT]         = $model->field["EXAM_PAY_DIV"];
        $data["EXAM_PAY_DATE"][DATE]        = str_replace('/', '-', $model->field["EXAM_PAY_DATE"]);
        $data["EXAM_PAY_CHAK_DATE"][DATE]   = str_replace('/', '-', $model->field["EXAM_PAY_CHAK_DATE"]);
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "SYSDATE()";

        if ($moneyCnt == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_MONEY_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$setYear."' ";
            $where .= "   AND APPLICANTDIV  = '".$setAppDiv."'";
            $where .= "   AND EXAMNO        = '".$setExamNo."'";
            $query = Query::updateSQL($data, "ENTEXAM_MONEY_DAT", $where);
        }
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT(001)第１希望コース
        list($coursecd1, $majorcd1, $coursecode1) = explode('-', $model->field["EXAMCOURSE"]);
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $setYear;
        $data["APPLICANTDIV"][TEXT]     = $setAppDiv;
        $data["EXAMNO"][TEXT]           = $setExamNo;
        $data["SEQ"][TEXT]              = '001';
        $data["REMARK8"][TEXT]          = $coursecd1;
        $data["REMARK9"][TEXT]          = $majorcd1;
        $data["REMARK10"][TEXT]         = $coursecode1;

        if ($s001Cnt == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$setYear."' ";
            $where .= "   AND APPLICANTDIV  = '".$setAppDiv."'";
            $where .= "   AND EXAMNO        = '".$setExamNo."'";
            $where .= "   AND SEQ           = '001'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        //(016)併願校情報
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $setYear;
        $data["APPLICANTDIV"][TEXT]     = $setAppDiv;
        $data["EXAMNO"][TEXT]           = $setExamNo;
        $data["SEQ"][TEXT]              = '016';
        $data["REMARK1"][TEXT]          = $model->field["REMARK1_016"]; //併願校名
        $data["REMARK2"][TEXT]          = $model->field["REMARK2_016"]; //合格発表日
        $data["REMARK3"][TEXT]          = $model->field["REMARK3_016"]; //延期願

        if ($s016Cnt == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$setYear."' ";
            $where .= "   AND APPLICANTDIV  = '".$setAppDiv."'";
            $where .= "   AND EXAMNO        = '".$setExamNo."'";
            $where .= "   AND SEQ           = '016'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        //(017)第２希望コース
        list($coursecd2, $majorcd2, $coursecode2) = explode('-', $model->field["EXAMCOURSE2"]);
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $setYear;
        $data["APPLICANTDIV"][TEXT]     = $setAppDiv;
        $data["EXAMNO"][TEXT]           = $setExamNo;
        $data["SEQ"][TEXT]              = '017';
        $data["REMARK1"][TEXT]          = $coursecd2;
        $data["REMARK2"][TEXT]          = $majorcd2;
        $data["REMARK3"][TEXT]          = $coursecode2;

        if ($s001Cnt == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$setYear."' ";
            $where .= "   AND APPLICANTDIV  = '".$setAppDiv."'";
            $where .= "   AND EXAMNO        = '".$setExamNo."'";
            $where .= "   AND SEQ           = '017'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        //(018)本校卒業・在校生情報
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $setYear;
        $data["APPLICANTDIV"][TEXT]     = $setAppDiv;
        $data["EXAMNO"][TEXT]           = $setExamNo;
        $data["SEQ"][TEXT]              = '018';
        $data["REMARK1"][TEXT]          = $model->field["REMARK1_018"];
        $data["REMARK3"][TEXT]          = $model->field["REMARK3_018"];
        $data["REMARK5"][TEXT]          = $model->field["REMARK5_018"];
        $data["REMARK6"][TEXT]          = $model->field["REMARK6_018"];
        $data["REMARK7"][TEXT]          = $model->field["REMARK7_018"];

        if ($s018Cnt == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$setYear."' ";
            $where .= "   AND APPLICANTDIV  = '".$setAppDiv."'";
            $where .= "   AND EXAMNO        = '".$setExamNo."'";
            $where .= "   AND SEQ           = '018'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        if ($model->field["TESTDIV"] == "31" || $model->field["TESTDIV"] == "32") {
            //(021)学年情報
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $setYear;
            $data["APPLICANTDIV"][TEXT]     = $setAppDiv;
            $data["EXAMNO"][TEXT]           = $setExamNo;
            $data["SEQ"][TEXT]              = '021';
            $data["REMARK1"][TEXT]          = $model->field["GRADE"]; //学年

            if ($s021Cnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '".$setYear."' ";
                $where .= "   AND APPLICANTDIV  = '".$setAppDiv."'";
                $where .= "   AND EXAMNO        = '".$setExamNo."'";
                $where .= "   AND SEQ           = '021'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);
        }

        //(029)特待希望情報
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $setYear;
        $data["APPLICANTDIV"][TEXT]     = $setAppDiv;
        $data["EXAMNO"][TEXT]           = $setExamNo;
        $data["SEQ"][TEXT]              = '029';
        $data["REMARK1"][TEXT]          = $model->field["REMARK1_029"]; //特待希望
        $data["REMARK2"][TEXT]          = $model->field["REMARK2_029"];
        $data["REMARK10"][TEXT]         = $model->field["REMARK10_029"];

        if ($s029Cnt == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$setYear."' ";
            $where .= "   AND APPLICANTDIV  = '".$setAppDiv."'";
            $where .= "   AND EXAMNO        = '".$setExamNo."'";
            $where .= "   AND SEQ           = '029'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        //(032)志望理由
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $setYear;
        $data["APPLICANTDIV"][TEXT]     = $setAppDiv;
        $data["EXAMNO"][TEXT]           = $setExamNo;
        $data["SEQ"][TEXT]              = '032';
        $data["REMARK10"][TEXT]         = $model->field["REMARK10_032"];

        if ($s032Cnt == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$setYear."' ";
            $where .= "   AND APPLICANTDIV  = '".$setAppDiv."'";
            $where .= "   AND EXAMNO        = '".$setExamNo."'";
            $where .= "   AND SEQ           = '032'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
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
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."' ";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."' ";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."' ";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."' ";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."' ";
        $db->query($query);

        //ENTEXAM_RECEPT_DAT
        $query  = "DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."' ";
        $db->query($query);

        //ENTEXAM_MONEY_DAT
        $query  = "DELETE FROM ENTEXAM_MONEY_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
