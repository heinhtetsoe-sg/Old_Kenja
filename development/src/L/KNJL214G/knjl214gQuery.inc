<?php

require_once('for_php7.php');

class knjl214gquery extends Query
{
    public function getNameMst($model, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //願書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_AID_RISSHI_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";

        $db->query($query);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //ENTEXAM_AID_RISSHI_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]              = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]             = $model->field["APPLICANTDIV"];
            $data["AID_TESTDIV"][TEXT]              = $data_arr[$i]["AID_TESTDIV"];
            $data["AID_TESTDIV_NAME"][TEXT]         = $data_arr[$i]["AID_TESTDIV_NAME"];
            $data["EXAMCD"][TEXT]                   = $data_arr[$i]["EXAMCD"];
            $data["EXAM_NAME"][TEXT]                = $data_arr[$i]["EXAM_NAME"];
            $data["EXAMNO"][TEXT]                   = $data_arr[$i]["EXAMNO"];
            $data["HOPECOURSE1"][TEXT]              = $data_arr[$i]["HOPECOURSE1"];
            $data["HOPECOURSE2"][TEXT]              = $data_arr[$i]["HOPECOURSE2"];
            $data["NAME_SEI"][TEXT]                 = $data_arr[$i]["NAME_SEI"];
            $data["NAME_MEI"][TEXT]                 = $data_arr[$i]["NAME_MEI"];
            $data["GAIJI_CD"][TEXT]                 = $data_arr[$i]["GAIJI_CD"];
            $data["NAME_KANA_SEI"][TEXT]            = $data_arr[$i]["NAME_KANA_SEI"];
            $data["NAME_KANA_MEI"][TEXT]            = $data_arr[$i]["NAME_KANA_MEI"];
            $data["SEX"][TEXT]                      = $data_arr[$i]["SEX"];
            $data["BIRTHDAY"][TEXT]                 = $data_arr[$i]["BIRTHDAY"];
            $data["ZIPCD1"][TEXT]                   = $data_arr[$i]["ZIPCD1"];
            $data["ZIPCD2"][TEXT]                   = $data_arr[$i]["ZIPCD2"];
            $data["PREF_CITY_BANCHI_NAME"][TEXT]    = $data_arr[$i]["PREF_CITY_BANCHI_NAME"];
            $data["ADDRESS2_1"][TEXT]               = $data_arr[$i]["ADDRESS2_1"];
            $data["ADDRESS2_2"][TEXT]               = $data_arr[$i]["ADDRESS2_2"];
            $data["PREF_CD"][TEXT]                  = $data_arr[$i]["PREF_CD"];
            $data["TELNO"][TEXT]                    = $data_arr[$i]["TELNO"];
            $data["EMAIL"][TEXT]                    = $data_arr[$i]["EMAIL"];
            $data["SHIGAN_DATE"][TEXT]              = $data_arr[$i]["SHIGAN_DATE"];
            $data["GNAME_SEI"][TEXT]                = $data_arr[$i]["GNAME_SEI"];
            $data["GNAME_MEI"][TEXT]                = $data_arr[$i]["GNAME_MEI"];
            $data["GKANA_SEI"][TEXT]                = $data_arr[$i]["GKANA_SEI"];
            $data["GKANA_MEI"][TEXT]                = $data_arr[$i]["GKANA_MEI"];
            $data["RELATIONSHIP"][TEXT]             = $data_arr[$i]["RELATIONSHIP"];
            $data["GZIPCD1"][TEXT]                  = $data_arr[$i]["GZIPCD1"];
            $data["GZIPCD2"][TEXT]                  = $data_arr[$i]["GZIPCD2"];
            $data["GPREF_CITY_BANCHI_NAME"][TEXT]   = $data_arr[$i]["GPREF_CITY_BANCHI_NAME"];
            $data["GADDRESS2_1"][TEXT]              = $data_arr[$i]["GADDRESS2_1"];
            $data["GADDRESS2_2"][TEXT]              = $data_arr[$i]["GADDRESS2_2"];
            $data["GPREF_CD"][TEXT]                 = $data_arr[$i]["GPREF_CD"];
            $data["GTELNO"][TEXT]                   = $data_arr[$i]["GTELNO"];
            $data["EMERGENCYCALL"][TEXT]            = $data_arr[$i]["EMERGENCYCALL"];
            $data["AID_FS_CD"][TEXT]                = $data_arr[$i]["AID_FS_CD"];
            $data["AID_FS_NAME"][TEXT]              = $data_arr[$i]["AID_FS_NAME"];
            $data["FS_GRDYEAR"][TEXT]               = $data_arr[$i]["FS_GRDYEAR"];
            $data["REASON"][TEXT]                   = $data_arr[$i]["REASON"];
            $data["SH_SCHOOL_NAME"][TEXT]           = $data_arr[$i]["SH_SCHOOL_NAME"];
            $data["SH_PASS_DATE"][TEXT]             = $data_arr[$i]["SH_PASS_DATE"];
            $data["SCHOLARSHIP"][TEXT]              = $data_arr[$i]["SCHOLARSHIP"];
            $data["SCHOLARSHIP_REASON"][TEXT]       = $data_arr[$i]["SCHOLARSHIP_REASON"];
            $data["ENT_MONEY_EXEMPT"][TEXT]         = $data_arr[$i]["ENT_MONEY_EXEMPT"];
            $data["P_REGD_DIV"][TEXT]               = $data_arr[$i]["P_REGD_DIV"];
            $data["P_GRD_YEAR"][TEXT]               = $data_arr[$i]["P_GRD_YEAR"];
            $data["P_GRD_COURSE"][TEXT]             = $data_arr[$i]["P_GRD_COURSE"];
            $data["P_NAME"][TEXT]                   = $data_arr[$i]["P_NAME"];
            $data["P_RELATIONSHIP"][TEXT]           = $data_arr[$i]["P_RELATIONSHIP"];
            $data["PRISCHOOL_NAME"][TEXT]           = $data_arr[$i]["PRISCHOOL_NAME"];
            $data["PRISCHOOL_CLASS_NAME"][TEXT]     = $data_arr[$i]["PRISCHOOL_CLASS_NAME"];
            $data["REGISTERCD"][TEXT]               = STAFFCD;
            $data["UPDATED"][NUMBER]                = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_AID_RISSHI_DAT");
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    public function getAidCsv($model)
    {
        $not = ($model->field["TESTDIV"] == '02') ? ' NOT ': '';

        $query  = " SELECT ";
        $query .= "     AID.* ";
        $query .= "     ,substr(AID.AID_TESTDIV, 2, 1) || substr(AID.EXAMCD, 4, 1) as TESTDIV ";
        $query .= "     ,L007.NAMECD2 AS ERACD ";
        $query .= "     ,YEAR(DATE(REPLACE(AID.BIRTHDAY, '/', '-'))) - INT(L007.NAMESPARE1) + 1 AS BIRTH_Y ";
        $query .= "     ,RIGHT('00' || RTRIM(CAST(MONTH(DATE(REPLACE(AID.BIRTHDAY, '/', '-'))) AS CHAR(2))), 2) AS BIRTH_M ";
        $query .= "     ,RIGHT('00' || RTRIM(CAST(DAY(DATE(REPLACE(AID.BIRTHDAY, '/', '-'))) AS CHAR(2))), 2)  AS BIRTH_D ";
        $query .= "     ,AID.FS_GRDYEAR || '-03-31' AS FS_DAY ";
        $query .= "     ,L007_FS.NAMECD2 AS FS_ERACD ";
        $query .= "     ,INT(AID.FS_GRDYEAR) - INT(L007_FS.NAMESPARE1) + 1 AS FS_Y ";
        $query .= "     ,'03' AS FS_M ";
        $query .= "     ,'31' AS FS_D ";
        $query .= "     , AID.REASON ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_AID_RISSHI_DAT AID ";
        $query .= "     LEFT JOIN NAME_MST L007 ON L007.NAMECD1 = 'L007' ";
        $query .= "         AND DATE(REPLACE(AID.BIRTHDAY, '/', '-')) BETWEEN DATE(REPLACE(L007.NAMESPARE2, '/', '-')) ";
        $query .= "                                                       AND DATE(REPLACE(L007.NAMESPARE3, '/', '-')) ";
        $query .= "     LEFT JOIN NAME_MST L007_FS ON L007_FS.NAMECD1 = 'L007' ";
        $query .= "         AND DATE(AID.FS_GRDYEAR || '-03-31') BETWEEN DATE(REPLACE(L007_FS.NAMESPARE2, '/', '-')) ";
        $query .= "                                                  AND DATE(REPLACE(L007_FS.NAMESPARE3, '/', '-')) ";
        $query .= " WHERE ";
        $query .= "         AID.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND AID.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND substr(AID.AID_TESTDIV, 2, 1) || ";
        $query .= "         substr(AID.EXAMCD, 4, 1) IN ( ";
        $query .= "                                     SELECT ";
        $query .= "                                         NAMECD2 ";
        $query .= "                                     FROM ";
        $query .= "                                         V_NAME_MST ";
        $query .= "                                     WHERE ";
        $query .= "                                             YEAR       = '{$model->ObjYear}' ";
        $query .= "                                         AND NAMECD1    = 'L004' ";
        $query .= "                                         AND value(NAMESPARE3, '0') {$not} IN '1' ";
        $query .= "                                     ) ";

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

    //更新
    public function updateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //続柄変換設定(本番機に合わせて変換)
        $h201Arr = array('01' => '01',
                         '02' => '02',
                         '03' => '07',
                         '04' => '08',
                         '05' => '12',
                         '06' => '14',
                         '07' => '15',
                         '08' => '09',
                         '09' => '10',
                         '10' => '16',
                         '11' => '11'
                         );

        //続柄変換設定2(本番機に合わせて変換)
        $h201Arr2 = array('01' => '01',
                          '02' => '02',
                          '03' => '12',
                          '04' => '13'
                         );

        //在籍区分変換
        $regdDivArr = array('01' => '1',
                            '02' => '1',
                            '03' => '2',
                            '04' => '2'
                            );

        //卒業コース変換
        $grdCdArr = array('01' => '102',
                          '02' => '101'
                          );

        //希望コース設定
        $cmcCdArr = array('01' => '1-102-1024', //平日コース特進クラス
                          '02' => '1-102-1023', //平日コース進学クラス
                          '03' => '1-102-1022', //平日コース普通クラス
                          '04' => '1-101-1011'  //土曜コース
                          );

        $query = knjl214gQuery::getAidCsv($model);
        $result = $db->query($query);
        while ($rowAid = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setYear   = $rowAid["ENTEXAMYEAR"];
            $setAppDiv = $rowAid["APPLICANTDIV"];
            $setExamNo = $rowAid["EXAMNO"];

            //件数取得
            $baseCnt  = $db->getOne(knjl214gQuery::getCntData('ENTEXAM_APPLICANTBASE_DAT', $setYear, $setAppDiv, $setExamNo));
            $addrCnt  = $db->getOne(knjl214gQuery::getCntData('ENTEXAM_APPLICANTADDR_DAT', $setYear, $setAppDiv, $setExamNo));
            $moneyCnt = $db->getOne(knjl214gQuery::getCntData('ENTEXAM_MONEY_DAT', $setYear, $setAppDiv, $setExamNo));
            $s001Cnt  = $db->getOne(knjl214gQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $setYear, $setAppDiv, $setExamNo, '001'));
            $s005Cnt  = $db->getOne(knjl214gQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $setYear, $setAppDiv, $setExamNo, '005'));
            $s016Cnt  = $db->getOne(knjl214gQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $setYear, $setAppDiv, $setExamNo, '016'));
            $s017Cnt  = $db->getOne(knjl214gQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $setYear, $setAppDiv, $setExamNo, '017'));
            $s018Cnt  = $db->getOne(knjl214gQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $setYear, $setAppDiv, $setExamNo, '018'));
            $s029Cnt  = $db->getOne(knjl214gQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $setYear, $setAppDiv, $setExamNo, '029'));
            $s032Cnt  = $db->getOne(knjl214gQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $setYear, $setAppDiv, $setExamNo, '032'));

            //志願日セット
            $receptDate = str_replace('/', '-', $rowAid["SHIGAN_DATE"]);

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]        = $setYear;
            $data["APPLICANTDIV"][TEXT]       = $setAppDiv;
            $data["EXAMNO"][TEXT]             = $setExamNo;
            $data["TESTDIV"][TEXT]            = $rowAid["TESTDIV"];
            $data["SHDIV"][TEXT]              = ($rowAid["SH_SCHOOL_NAME"] != '') ? '2': '1';
            $data["DESIREDIV"][TEXT]          = '1';
            $data["RECEPTDATE"][DATE]         = $receptDate;
            $data["NAME"][TEXT]               = $rowAid["NAME_SEI"].'　'.$rowAid["NAME_MEI"];
            $data["NAME_KANA"][TEXT]          = $rowAid["NAME_KANA_SEI"].'　'.$rowAid["NAME_KANA_MEI"];
            $data["SEX"][TEXT]                = ($rowAid["SEX"] == '1') ? '1': '2';
            if ($rowAid["BIRTHDAY"]) {
                $data["ERACD"][VARCHAR]       = $rowAid["ERACD"];
                $data["BIRTH_Y"][VARCHAR]     = $rowAid["BIRTH_Y"];
                $data["BIRTH_M"][VARCHAR]     = $rowAid["BIRTH_M"];
                $data["BIRTH_D"][VARCHAR]     = $rowAid["BIRTH_D"];
            }
            $data["BIRTHDAY"][DATE]           = str_replace('/', '-', $rowAid["BIRTHDAY"]);
            if ($rowAid["AID_FS_CD"]) {
                $data["FS_CD"][TEXT]              = '3'.$rowAid["AID_FS_CD"];//頭に'3'を付属
            }
            if ($rowAid["FS_DAY"]) {
                $data["FS_GRDYEAR"][TEXT]     = $rowAid["FS_GRDYEAR"];
                $data["FS_ERACD"][TEXT]       = $rowAid["FS_ERACD"];
                $data["FS_Y"][TEXT]           = $rowAid["FS_Y"];
                $data["FS_M"][TEXT]           = $rowAid["FS_M"];
            }
            $data["FS_DAY"][DATE]             = $rowAid["FS_DAY"];
            $data["FS_GRDDIV"][TEXT]          = $rowAid["FS_GRDYEAR"] >= $setYear ? '1' : '2';
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][NUMBER]          = "SYSDATE()";

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
            $data["ENTEXAMYEAR"][TEXT]      = $setYear;
            $data["APPLICANTDIV"][TEXT]     = $setAppDiv;
            $data["EXAMNO"][TEXT]           = $setExamNo;
            $data["ZIPCD"][TEXT]            = $rowAid["ZIPCD1"].'-'.$rowAid["ZIPCD2"];
            $data["ADDRESS1"][TEXT]         = $rowAid["PREF_CITY_BANCHI_NAME"];
            $data["ADDRESS2"][TEXT]         = $rowAid["ADDRESS2_1"].$rowAid["ADDRESS2_2"];
            $data["TELNO"][TEXT]            = $rowAid["TELNO"];
            $data["GNAME"][TEXT]            = $rowAid["GNAME_SEI"].'　'.$rowAid["GNAME_MEI"];
            $data["GKANA"][TEXT]            = $rowAid["GKANA_SEI"].'　'.$rowAid["GKANA_MEI"];
            $data["GZIPCD"][TEXT]           = $rowAid["GZIPCD1"].'-'.$rowAid["GZIPCD2"];
            $data["GADDRESS1"][TEXT]        = $rowAid["GPREF_CITY_BANCHI_NAME"];
            $data["GADDRESS2"][TEXT]        = $rowAid["GADDRESS2_1"].$rowAid["GADDRESS2_2"];
            $data["GTELNO"][TEXT]           = $rowAid["GTELNO"];
            $data["EMERGENCYCALL"][TEXT]    = $rowAid["EMERGENCYCALL"];
            $data["RELATIONSHIP"][TEXT]     = $h201Arr[$rowAid["RELATIONSHIP"]];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

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
            $data["ENTEXAMYEAR"][TEXT]      = $setYear;
            $data["APPLICANTDIV"][TEXT]     = $setAppDiv;
            $data["EXAMNO"][TEXT]           = $setExamNo;
            $data["EXAM_PAY_DIV"][TEXT]     = ($receptDate != '') ? '1': '';
            $data["EXAM_PAY_DATE"][DATE]    = $receptDate;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

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
            list($coursecd1, $majorcd1, $coursecode1) = explode('-', $cmcCdArr[$rowAid["HOPECOURSE1"]]);
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

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT(005)出身学校名
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $setYear;
            $data["APPLICANTDIV"][TEXT]     = $setAppDiv;
            $data["EXAMNO"][TEXT]           = $setExamNo;
            $data["SEQ"][TEXT]              = '005';
            $data["REMARK1"][TEXT]          = $rowAid["AID_FS_NAME"];

            if ($s005Cnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '".$setYear."' ";
                $where .= "   AND APPLICANTDIV  = '".$setAppDiv."'";
                $where .= "   AND EXAMNO        = '".$setExamNo."'";
                $where .= "   AND SEQ           = '005'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //(016)併願校情報
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $setYear;
            $data["APPLICANTDIV"][TEXT]     = $setAppDiv;
            $data["EXAMNO"][TEXT]           = $setExamNo;
            $data["SEQ"][TEXT]              = '016';
            $data["REMARK1"][TEXT]          = $rowAid["SH_SCHOOL_NAME"];//併願校名
            $data["REMARK2"][TEXT]          = $rowAid["SH_PASS_DATE"];  //合格発表日

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
            list($coursecd2, $majorcd2, $coursecode2) = explode('-', $cmcCdArr[$rowAid["HOPECOURSE2"]]);
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $setYear;
            $data["APPLICANTDIV"][TEXT]     = $setAppDiv;
            $data["EXAMNO"][TEXT]           = $setExamNo;
            $data["SEQ"][TEXT]              = '017';
            $data["REMARK1"][TEXT]          = $coursecd2;
            $data["REMARK2"][TEXT]          = $majorcd2;
            $data["REMARK3"][TEXT]          = $coursecode2;

            if ($s017Cnt == 0) {
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
            $data["REMARK1"][TEXT]          = $rowAid["P_NAME"];
            $data["REMARK3"][TEXT]          = $h201Arr2[$rowAid["P_RELATIONSHIP"]];
            $data["REMARK5"][TEXT]          = $grdCdArr[$rowAid["P_GRD_COURSE"]];
            $data["REMARK6"][TEXT]          = $rowAid["P_GRD_YEAR"];
            $data["REMARK7"][TEXT]          = $regdDivArr[$rowAid["P_REGD_DIV"]];

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

            //(029)特待希望情報
            $scholaFlg = '';
            if ($rowAid["SCHOLARSHIP"] == '00') {
                $scholaFlg = '';
            } elseif ($rowAid["SCHOLARSHIP"] != '') {
                $scholaFlg = '1';
            }
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $setYear;
            $data["APPLICANTDIV"][TEXT]     = $setAppDiv;
            $data["EXAMNO"][TEXT]           = $setExamNo;
            $data["SEQ"][TEXT]              = '029';
            $data["REMARK1"][TEXT]          = $scholaFlg;
            $data["REMARK2"][TEXT]          = $rowAid["SCHOLARSHIP"];
            $data["REMARK10"][TEXT]         = $rowAid["SCHOLARSHIP_REASON"];

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

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT(032)志望理由
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $setYear;
            $data["APPLICANTDIV"][TEXT]     = $setAppDiv;
            $data["EXAMNO"][TEXT]           = $setExamNo;
            $data["SEQ"][TEXT]              = '032';
            $data["REMARK10"][TEXT]         = $rowAid["REASON"];

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
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }
}
