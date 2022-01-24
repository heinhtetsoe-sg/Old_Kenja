<?php

require_once('for_php7.php');
class knjl014wquery extends Query
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
    
    /******************/
    /**  画面で使用  **/
    /******************/

    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/

    //願書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //元号取得
        $result = $db->query(knjl014wQuery::getNameMstAll($model->ObjYear, "L007"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[]    = $row["NAMECD2"];
            $defyear[]  = $row["NAMESPARE1"];
            $defsdate[] = $row["NAMESPARE2"];
            $defedate[] = $row["NAMESPARE3"];
        }
        $result->free();

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //分解する日付一覧
            $date_array = array("BIRTHDAY" => "birth", "FS_DAY" => "fs");
            //部品名
            $parts = array("_eracd", "_y", "_m", "_d");

            foreach ($date_array as $dkey => $dval) {
                if (strlen($data_arr[$i][$dkey])) {
                    //日付を分解
                    list($y, $m, $d) = preg_split('/-/', str_replace("/", "-", $data_arr[$i][$dkey]));
                    //部品の変数名
                    list($e_id, $y_id, $m_id, $d_id, $s_id) = array($dval."_eracd", $dval."_y", $dval."_m", $dval."_d", $dval."_s");

                    $$e_id = $$y_id = $$m_id = $$d_id = $$s_id = "";
                    for ($ere_no=0; $ere_no < get_count($eracd); $ere_no++) {
                        if ($defsdate[$ere_no] <= $data_arr[$i][$dkey] && $data_arr[$i][$dkey] <= $defedate[$ere_no]) {
                            $$e_id = $eracd[$ere_no];
                            $$y_id = ((int)$y + 1) - ((int)$defyear[$ere_no]);
                            $$y_id = sprintf("%02d", $$y_id);
                            $$m_id = $m;
                            $$d_id = $d;
                            $$s_id = $y;
                            break;
                        }
                    }
                }
            }

            //データ件数取得
            $base_cnt       = $db->getOne(knjl014wQuery::cntEntexamApplicantbaseDat($model, $data_arr[$i]));
            $addr_cnt       = $db->getOne(knjl014wQuery::cntEntexamApplicantaddrDat($model, $data_arr[$i]));
            $baseD001_cnt   = $db->getOne(knjl014wQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "001"));
            $baseD017_cnt   = $db->getOne(knjl014wQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "017"));
            $baseD021_cnt   = $db->getOne(knjl014wQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "021"));

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $data["RECOM_EXAMNO"][TEXT]         = $data_arr[$i]["RECOM_EXAMNO"];
            $data["TESTDIV"][TEXT]              = $data_arr[$i]["TESTDIV"];
            $data["TESTDIV2"][TEXT]             = $data_arr[$i]["TESTDIV2"];
            $data["SHDIV"][TEXT]                = '1';
            $data["DESIREDIV"][TEXT]            = '1';
            $data["RECEPTDATE"][DATE]           = $data_arr[$i]["RECEPTDATE"];
            $data["NAME"][TEXT]                 = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]            = $data_arr[$i]["NAME_KANA"];
            $data["ERACD"][TEXT]                = $birth_eracd;
            $data["BIRTH_Y"][TEXT]              = $birth_y;
            $data["BIRTH_M"][TEXT]              = $birth_m;
            $data["BIRTH_D"][TEXT]              = $birth_d;
            $data["BIRTHDAY"][DATE]             = $data_arr[$i]["BIRTHDAY"];
            $data["FS_CD"][TEXT]                = $data_arr[$i]["FS_CD"];
            $data["FS_GRDYEAR"][TEXT]           = $fs_s;
            $data["FS_ERACD"][TEXT]             = $fs_eracd;
            $data["FS_Y"][TEXT]                 = $fs_y;
            $data["FS_M"][TEXT]                 = $fs_m;
            $data["FS_DAY"][DATE]               = $data_arr[$i]["FS_DAY"];
            $data["FS_GRDDIV"][TEXT]            = $data_arr[$i]["FS_GRDDIV"];
            $data["REMARK1"][TEXT]              = $data_arr[$i]["REMARK1"];
            $data["REMARK2"][TEXT]              = $data_arr[$i]["REMARK2"];
            $data["JUDGEMENT"][TEXT]            = $data_arr[$i]["JUDGEMENT"];
            $data["PROCEDUREDIV"][TEXT]         = $data_arr[$i]["ENTDIV"];
            $data["ENTDIV"][TEXT]               = $data_arr[$i]["ENTDIV"];
            $data["SUC_COURSECD"][TEXT]         = $data_arr[$i]["SUC_COURSECD"];
            $data["SUC_MAJORCD"][TEXT]          = $data_arr[$i]["SUC_MAJORCD"];
            $data["SUC_COURSECODE"][TEXT]       = $data_arr[$i]["SUC_COURSECODE"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $base_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTADDR_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["ZIPCD"][TEXT]            = $data_arr[$i]["ZIPCD"];
            $data["ADDRESS1"][TEXT]         = $data_arr[$i]["ADDRESS1"];
            $data["ADDRESS2"][TEXT]         = $data_arr[$i]["ADDRESS2"];
            $data["TELNO"][TEXT]            = $data_arr[$i]["TELNO"];
            $data["GKANA"][TEXT]            = $data_arr[$i]["GKANA"];
            $data["GNAME"][TEXT]            = $data_arr[$i]["GNAME"];
            $data["GZIPCD"][TEXT]           = $data_arr[$i]["GZIPCD"];
            $data["GADDRESS1"][TEXT]        = $data_arr[$i]["GADDRESS1"];
            $data["GADDRESS2"][TEXT]        = $data_arr[$i]["GADDRESS2"];
            $data["GTELNO"][TEXT]           = $data_arr[$i]["GTELNO"];
            $data["RELATIONSHIP"][TEXT]     = $data_arr[$i]["RELATIONSHIP"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $addr_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=001
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]  = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]        = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]           = "001";
            $data["REMARK5"][TEXT]       = $data_arr[$i]["GANHEN_DAI1_COURSECD"];
            $data["REMARK6"][TEXT]       = $data_arr[$i]["GANHEN_DAI1_MAJORCD"];
            $data["REMARK7"][TEXT]       = $data_arr[$i]["GANHEN_DAI1_COURSECODE"];
            $data["REMARK8"][TEXT]       = $data_arr[$i]["DAI1_COURSECD"];
            $data["REMARK9"][TEXT]       = $data_arr[$i]["DAI1_MAJORCD"];
            $data["REMARK10"][TEXT]      = $data_arr[$i]["DAI1_COURSECODE"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '001' ";

            if (0 == $baseD001_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=007
            $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $query .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $query .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $query .= "       SEQ           = '007' ";
            $db->query($query);
            //合格更新時に（JUDGEMENT=1,3）、入学コースもセットする。
            if ($data_arr[$i]["JUDGEMENT"] == "1" || $data_arr[$i]["JUDGEMENT"] == "3") {
                //入学コースを取得
                $query  = " SELECT ";
                $query .= "     ENTER_COURSECD, ";
                $query .= "     ENTER_MAJORCD, ";
                $query .= "     ENTER_COURSECODE ";
                $query .= " FROM ";
                $query .= "     ENTEXAM_COURSE_MST ";
                $query .= " WHERE ";
                $query .= "     ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
                $query .= "     APPLICANTDIV = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
                $query .= "     TESTDIV      = '{$data_arr[$i]["TESTDIV"]}' AND ";
                $query .= "     COURSECD     = '{$data_arr[$i]["SUC_COURSECD"]}' AND ";
                $query .= "     MAJORCD      = '{$data_arr[$i]["SUC_MAJORCD"]}' AND ";
                $query .= "     EXAMCOURSECD = '{$data_arr[$i]["SUC_COURSECODE"]}' ";
                $getEnter = array();
                $getEnter = $db->getRow($query, DB_FETCHMODE_ASSOC);

                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
                $data["SEQ"][TEXT]              = "007";
                $data["REMARK1"][TEXT]          = $getEnter["ENTER_COURSECD"];
                $data["REMARK2"][TEXT]          = $getEnter["ENTER_MAJORCD"];
                $data["REMARK3"][TEXT]          = $getEnter["ENTER_COURSECODE"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                $db->query($query);
            }

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=017
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "017";
            $data["REMARK1"][TEXT]          = $data_arr[$i]["DAI2_COURSECD"];
            $data["REMARK2"][TEXT]          = $data_arr[$i]["DAI2_MAJORCD"];
            $data["REMARK3"][TEXT]          = $data_arr[$i]["DAI2_COURSECODE"];
            $data["REMARK5"][TEXT]          = $data_arr[$i]["GANHEN_DAI2_COURSECD"];
            $data["REMARK6"][TEXT]          = $data_arr[$i]["GANHEN_DAI2_MAJORCD"];
            $data["REMARK7"][TEXT]          = $data_arr[$i]["GANHEN_DAI2_COURSECODE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '017' ";

            if (0 == $baseD017_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=021
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "021";
            $data["REMARK1"][TEXT]          = $data_arr[$i]["OBOEGAKI_SIGANSYA"];
            $data["REMARK2"][TEXT]          = $data_arr[$i]["KAIGAI_KIKOKUSEI_NADO"];
            $data["REMARK3"][TEXT]          = $data_arr[$i]["TUUGAKU_KUIKIGAI_KYOKA"];
            $data["REMARK4"][TEXT]          = $data_arr[$i]["BOSHUU_KYOUGI"];

            $data["REMARK5"][TEXT]          = $data_arr[$i]["HOSHOUNIN_TODOKE"];
            $data["REMARK6"][TEXT]          = $data_arr[$i]["KENGAI_ZAIJUU"];
            $data["REMARK8"][TEXT]          = $data_arr[$i]["KENGAI_CHUUGAKKOU_SHUSSHIN"];
            $data["REMARK10"][TEXT]         = $data_arr[$i]["KENGAI_CHUUGAKKOU_SHUSSHIN2"];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '021' ";

            if (0 == $baseD021_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //調査書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv2($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //教科取得
        $class_array = array();
        $result = $db->query(knjl014wQuery::getNameMstAll($model->ObjYear, "L008"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $class_array[$row["NAMECD2"]] = $row["ABBV1"];
        }

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データ件数取得
            $base_cnt           = $db->getOne(knjl014wQuery::cntEntexamApplicantbaseDat($model, $data_arr[$i]));
            $confRpt_cnt        = $db->getOne(knjl014wQuery::cntEntexamApplicantconfrptDat($model, $data_arr[$i]));
            $confRptD001_cnt    = $db->getOne(knjl014wQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "001"));
            $confRptD002_cnt    = $db->getOne(knjl014wQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "002"));
            $confRptD004_cnt    = $db->getOne(knjl014wQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "004"));
            $confRptD005_cnt    = $db->getOne(knjl014wQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "005"));
            $confRptD009_cnt    = $db->getOne(knjl014wQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "009"));
            $confRptD011_cnt    = $db->getOne(knjl014wQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "011"));
            $confRptD012_cnt    = $db->getOne(knjl014wQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "012"));
            $confRptD013_cnt    = $db->getOne(knjl014wQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "013"));
            $confRptD014_cnt    = $db->getOne(knjl014wQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "014"));
            $confRptD015_cnt    = $db->getOne(knjl014wQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "015"));

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $data["SHDIV"][TEXT]                = '1';
            $data["DESIREDIV"][TEXT]            = '1';
            $data["SEX"][TEXT]                  = $data_arr[$i]["SEX"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $base_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $total = 0;
            foreach ($class_array as $classcd => $label) {
                $data["CONFIDENTIAL_RPT".$classcd][NUMBER] = $data_arr[$i]["CONF3_RPT".$classcd];
                $confrpt = ($data_arr[$i]["CONF3_RPT".$classcd]) ? $data_arr[$i]["CONF3_RPT".$classcd] : 0;
                $total += $confrpt;
            }
            $data["TOTAL_ALL"][NUMBER]          = (0 < $total) ? $total : "";
            $data["ABSENCE_DAYS"][NUMBER]       = $data_arr[$i]["ABSENCE_DAYS1"];
            $data["ABSENCE_DAYS2"][NUMBER]      = $data_arr[$i]["ABSENCE_DAYS2"];
            $data["ABSENCE_DAYS3"][NUMBER]      = $data_arr[$i]["ABSENCE_DAYS3"];
            $data["ABSENCE_REMARK"][TEXT]       = $data_arr[$i]["ABSENCE_REMARK1"];
            $data["ABSENCE_REMARK2"][TEXT]      = $data_arr[$i]["ABSENCE_REMARK2"];
            $data["ABSENCE_REMARK3"][TEXT]      = $data_arr[$i]["ABSENCE_REMARK3"];
            $data["REMARK1"][TEXT]              = $data_arr[$i]["REMARK1"];
            $data["REMARK2"][TEXT]              = $data_arr[$i]["SHOKEN_SONOTA"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $confRpt_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "001";
            $total = 0;
            foreach ($class_array as $classcd => $label) {
                $cd = (int) $classcd;
                $data["REMARK".$cd][TEXT]   = $data_arr[$i]["CONF1_RPT".$classcd];
                $confrpt = ($data_arr[$i]["CONF1_RPT".$classcd]) ? $data_arr[$i]["CONF1_RPT".$classcd] : 0;
                $total += $confrpt;
            }
            $data["REMARK12"][TEXT]         = (0 < $total) ? $total : "";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '001' ";

            if (0 == $confRptD001_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "002";
            $total = 0;
            foreach ($class_array as $classcd => $label) {
                $cd = (int) $classcd;
                $data["REMARK".$cd][TEXT]   = $data_arr[$i]["CONF2_RPT".$classcd];
                $confrpt = ($data_arr[$i]["CONF2_RPT".$classcd]) ? $data_arr[$i]["CONF2_RPT".$classcd] : 0;
                $total += $confrpt;
            }
            $data["REMARK12"][TEXT]         = (0 < $total) ? $total : "";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '002' ";

            if (0 == $confRptD002_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "004";
            $data["REMARK3"][TEXT]          = $data_arr[$i]["SHOKEN_KENKOU"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '004' ";

            if (0 == $confRptD004_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "005";
            $data["REMARK1"][TEXT]          = $data_arr[$i]["CHK_TOKUBETU1"];
            $data["REMARK2"][TEXT]          = $data_arr[$i]["CHK_TOKUBETU2"];
            $data["REMARK3"][TEXT]          = $data_arr[$i]["CHK_TOKUBETU3"];
            $data["REMARK4"][TEXT]          = $data_arr[$i]["SHOKEN_SOUGAKU1"];
            $data["REMARK5"][TEXT]          = $data_arr[$i]["SHOKEN_SOUGAKU2"];
            $data["REMARK6"][TEXT]          = $data_arr[$i]["SHOKEN_SOUGAKU3"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '005' ";

            if (0 == $confRptD005_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "009";
            for ($num = 1; $num <= 10; $num++) {
                $data["REMARK{$num}"][TEXT]   = $data_arr[$i]["CHK_KOUDOU{$num}"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '009' ";

            if (0 == $confRptD009_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "011";
            foreach ($class_array as $classcd => $label) {
                $cd = (int) $classcd;
                $data["REMARK".$cd][TEXT]   = $data_arr[$i]["KANTEN1_RPT".$classcd];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '011' ";

            if (0 == $confRptD011_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "012";
            foreach ($class_array as $classcd => $label) {
                $cd = (int) $classcd;
                $data["REMARK".$cd][TEXT]   = $data_arr[$i]["KANTEN2_RPT".$classcd];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '012' ";

            if (0 == $confRptD012_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "013";
            foreach ($class_array as $classcd => $label) {
                $cd = (int) $classcd;
                $data["REMARK".$cd][TEXT]   = $data_arr[$i]["KANTEN3_RPT".$classcd];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '013' ";

            if (0 == $confRptD013_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "014";
            foreach ($class_array as $classcd => $label) {
                $cd = (int) $classcd;
                $data["REMARK".$cd][TEXT]   = $data_arr[$i]["KANTEN4_RPT".$classcd];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '014' ";

            if (0 == $confRptD014_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "015";
            foreach ($class_array as $classcd => $label) {
                if ($classcd !== '01') {
                    continue;
                }
                $cd = (int) $classcd;
                $data["REMARK".$cd][TEXT]   = $data_arr[$i]["KANTEN5_RPT".$classcd];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '015' ";

            if (0 == $confRptD015_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //得点の追加（ＣＳＶデータより読込）
    public function updateQueryCsv3($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //教科取得
        $class_array = array();
        $result = $db->query(knjl014wQuery::getNameMstAll($model->ObjYear, "L009"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $class_array[$row["NAMECD2"]] = $row["NAME1"];
        }

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            foreach ($class_array as $classcd => $label) {
                $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
                $query .= " WHERE ENTEXAMYEAR    = '{$model->ObjYear}' AND ";
                $query .= "       APPLICANTDIV   = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
                $query .= "       TESTDIV        = '{$data_arr[$i]["TESTDIV"]}' AND ";
                $query .= "       RECEPTNO       = '{$data_arr[$i]["EXAMNO"]}' AND ";
                $query .= "       TESTSUBCLASSCD = '{$classcd}' ";

                $db->query($query);

                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
                $data["TESTDIV"][TEXT]          = $data_arr[$i]["TESTDIV"];
                $data["EXAM_TYPE"][TEXT]        = "1";
                $data["RECEPTNO"][TEXT]         = $data_arr[$i]["EXAMNO"];
                $data["TESTSUBCLASSCD"][TEXT]   = $classcd;
                $data["SCORE"][NUMBER]          = $data_arr[$i]["SCORE".$classcd];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";
                $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");

                $db->query($query);
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //開示資料の追加（ＣＳＶデータより読込）
    public function updateQueryCsv4($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //教科取得
        $class_array = array();
        $result = $db->query(knjl014wQuery::getNameMstAll($model->ObjYear, "L057"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $class_array[$row["NAMECD2"]] = $row["NAME1"];
        }

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            foreach ($class_array as $classcd => $label) {
                $query  = " DELETE FROM ENTEXAM_KAIJI_DAT ";
                $query .= " WHERE ENTEXAMYEAR      = '{$model->ObjYear}' AND ";
                $query .= "       APPLICANTDIV     = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
                $query .= "       TESTDIV          = '{$data_arr[$i]["TESTDIV"]}' AND ";
                $query .= "       RECEPTNO         = '{$data_arr[$i]["EXAMNO"]}' AND ";
                $query .= "       KAIJI_SUBCLASSCD = '{$classcd}' ";

                $db->query($query);

                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
                $data["TESTDIV"][TEXT]          = $data_arr[$i]["TESTDIV"];
                $data["EXAM_TYPE"][TEXT]        = "1";
                $data["RECEPTNO"][TEXT]         = $data_arr[$i]["EXAMNO"];
                $data["KAIJI_SUBCLASSCD"][TEXT] = $classcd;
                $data["SCORE"][TEXT]            = $data_arr[$i]["SCORE".$classcd];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";
                $query = Query::insertSQL($data, "ENTEXAM_KAIJI_DAT");

                $db->query($query);
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
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

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //名称マスタ登録コードチェック
    public function nameCntSql($namecd, $setcd, $where = "")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd}' AND ";
        $query .= "     NAMECD2 = '{$setcd}' ";
        if ($where) {
            $query .= $where;
        }

        return $query;
    }

    //志望コース
    public function examcoursecdCntSql($entexamyear, $applicantdiv, $testdiv, $coursecd, $majorcd, $examcoursecd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$entexamyear}' AND ";
        $query .= "     APPLICANTDIV    = '{$applicantdiv}' AND ";
        $query .= "     TESTDIV         = '{$testdiv}' AND ";
        $query .= "     COURSECD        = '{$coursecd}' AND ";
        $query .= "     MAJORCD         = '{$majorcd}' AND ";
        $query .= "     EXAMCOURSECD    = '{$examcoursecd}' ";

        return $query;
    }

    //出身学校コード
    public function fsCdCntSql($fs_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '{$fs_cd}' ";

        return $query;
    }

    //募集競技
    public function sportCdCntSql($entexamyear, $sport_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SPORT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$entexamyear}' AND ";
        $query .= "     SPORT_CD    = '{$sport_cd}' ";

        return $query;
    }

    /********************/
    /**  更新時に使用  **/
    /********************/

    //件数取得 -- ENTEXAM_APPLICANTBASE_DAT
    public function cntEntexamApplicantbaseDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV= '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO      = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTBASE_DAT
    public function cntEntexamApplicantbaseRecome($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR    = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV   = '{$data_arr["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO        != '{$data_arr["EXAMNO"]}' ";
        $query .= "     AND RECOM_EXAMNO   = '{$data_arr["RECOM_EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTADDR_DAT
    public function cntEntexamApplicantaddrDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV= '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO      = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTBASE_DETAIL_DAT
    public function cntEntexamApplicantbaseDetailDat($model, $data_arr, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV= '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO      = '{$data_arr["EXAMNO"]}' AND ";
        $query .= "     SEQ         = '{$seq}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTCONFRPT_DAT
    public function cntEntexamApplicantconfrptDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV= '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO      = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
    public function cntEntexamApplicantconfrptDetailDat($model, $data_arr, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV= '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO      = '{$data_arr["EXAMNO"]}' AND ";
        $query .= "     SEQ         = '{$seq}' ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/

    //名称マスタより取得
    public function getNameMstAll($year, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //ヘッダ例用データを名称マスタより取得
    public function getHeaderReiNameMst($year, $namecd1, $name, $namecd2 = "", $where = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || {$name} AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        if ($where) {
            $query .= " AND ".$where;
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSVデータ出力（願書）
    public function selectMainQuery1($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     '".$model->field["DATADIV"]."' AS DATADIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.RECOM_EXAMNO, ";
        $query .= "     T1.RECEPTDATE, ";
        $query .= "     T1.TESTDIV, ";
        //第１志望
        $query .= "     T1.DAI1_COURSECD, ";
        $query .= "     T1.DAI1_MAJORCD, ";
        $query .= "     T1.DAI1_COURSECODE, ";
        //第２志望
        $query .= "     T1.DAI2_COURSECD, ";
        $query .= "     T1.DAI2_MAJORCD, ";
        $query .= "     T1.DAI2_COURSECODE, ";
        //願変_第１志望
        $query .= "     T1.GANHEN_DAI1_COURSECD, ";
        $query .= "     T1.GANHEN_DAI1_MAJORCD, ";
        $query .= "     T1.GANHEN_DAI1_COURSECODE, ";
        //願変_第２志望
        $query .= "     T1.GANHEN_DAI2_COURSECD, ";
        $query .= "     T1.GANHEN_DAI2_MAJORCD, ";
        $query .= "     T1.GANHEN_DAI2_COURSECODE, ";
        //募集競技
        $query .= "     T1.BOSHUU_KYOUGI, ";
        //追検査、各種
        $query .= "     T1.TESTDIV2, ";
        $query .= "     T1.OBOEGAKI_SIGANSYA, ";
        $query .= "     T1.KAIGAI_KIKOKUSEI_NADO, ";
        $query .= "     T1.TUUGAKU_KUIKIGAI_KYOKA, ";
        $query .= "     T1.HOSHOUNIN_TODOKE, ";
        $query .= "     T1.KENGAI_CHUUGAKKOU_SHUSSHIN, ";
        $query .= "     T1.KENGAI_CHUUGAKKOU_SHUSSHIN2, ";
        $query .= "     T1.KENGAI_ZAIJUU, ";
        //志願者情報
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     L1.ZIPCD, ";
        $query .= "     L1.ADDRESS1, ";
        $query .= "     L1.ADDRESS2, ";
        $query .= "     L1.TELNO, ";
        $query .= "     T1.FS_CD, ";
        //都道府県名+'　'+出身学校名・・・表示のみ
        $query .= "     VALUE(P1.PREF_NAME, '') || '　' || FIN.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     T1.FS_DAY, ";
        $query .= "     T1.FS_GRDDIV, ";
        //保護者情報
        $query .= "     L1.GNAME, ";
        $query .= "     L1.GKANA, ";
        $query .= "     L1.GZIPCD, ";
        $query .= "     L1.GADDRESS1, ";
        $query .= "     L1.GADDRESS2, ";
        $query .= "     L1.GTELNO, ";
        $query .= "     L1.RELATIONSHIP, ";
        //備考
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     T1.JUDGEMENT, ";
        $query .= "     T1.ENTDIV, ";
        $query .= "     T1.SUC_COURSECD, ";
        $query .= "     T1.SUC_MAJORCD, ";
        $query .= "     T1.SUC_COURSECODE, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        //住所・保護者情報
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT L1 ";
        $query .= "          ON T1.ENTEXAMYEAR      = L1.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV     = L1.APPLICANTDIV ";
        $query .= "         AND T1.EXAMNO           = L1.EXAMNO ";
        //出身学校
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ";
        $query .= "          ON T1.FS_CD            = FIN.FINSCHOOLCD ";
        //出身学校の都道府県
        $query .= "     LEFT JOIN PREF_MST P1 ON P1.PREF_CD = FIN.FINSCHOOL_PREF_CD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "      T1.EXAMNO ";

        return $query;
    }

    //CSVデータ出力（調査書）
    public function selectMainQuery2($model, $class_array)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     '".$model->field["DATADIV"]."' AS DATADIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.SEX, ";
        //１．学習の記録
        //評定1年
        foreach ($class_array as $classcd => $label) {
            $cd = (int) $classcd;
            $query .= "     L1.REMARK{$cd} AS CONF1_RPT{$classcd}, ";
        }
        //評定2年
        foreach ($class_array as $classcd => $label) {
            $cd = (int) $classcd;
            $query .= "     L2.REMARK{$cd} AS CONF2_RPT{$classcd}, ";
        }
        //評定3年
        foreach ($class_array as $classcd => $label) {
            $query .= "     L0.CONFIDENTIAL_RPT{$classcd} AS CONF3_RPT{$classcd}, ";
        }
        //観点1
        foreach ($class_array as $classcd => $label) {
            $cd = (int) $classcd;
            $query .= "     L11.REMARK{$cd} AS KANTEN1_RPT{$classcd}, ";
        }
        //観点2
        foreach ($class_array as $classcd => $label) {
            $cd = (int) $classcd;
            $query .= "     L12.REMARK{$cd} AS KANTEN2_RPT{$classcd}, ";
        }
        //観点3
        foreach ($class_array as $classcd => $label) {
            $cd = (int) $classcd;
            $query .= "     L13.REMARK{$cd} AS KANTEN3_RPT{$classcd}, ";
        }
        //観点4
        foreach ($class_array as $classcd => $label) {
            $cd = (int) $classcd;
            $query .= "     L14.REMARK{$cd} AS KANTEN4_RPT{$classcd}, ";
        }
        //観点5(01:国語のみ)
        foreach ($class_array as $classcd => $label) {
            if ($classcd !== '01') {
                continue;
            }
            $cd = (int) $classcd;
            $query .= "     L15.REMARK{$cd} AS KANTEN5_RPT{$classcd}, ";
        }
        //備考
        $query .= "     L0.REMARK1, ";
        //２．総合的な学習の時間の記録
        $query .= "     L5.REMARK4 AS SHOKEN_SOUGAKU1, ";
        $query .= "     L5.REMARK5 AS SHOKEN_SOUGAKU2, ";
        $query .= "     L5.REMARK6 AS SHOKEN_SOUGAKU3, ";
        //３．特別活動の記録及び行動の記録
        //特別活動の記録
        $query .= "     L5.REMARK1 AS CHK_TOKUBETU1, ";
        $query .= "     L5.REMARK2 AS CHK_TOKUBETU2, ";
        $query .= "     L5.REMARK3 AS CHK_TOKUBETU3, ";
        //行動の記録
        $query .= "     L9.REMARK1 AS CHK_KOUDOU1, ";
        $query .= "     L9.REMARK2 AS CHK_KOUDOU2, ";
        $query .= "     L9.REMARK3 AS CHK_KOUDOU3, ";
        $query .= "     L9.REMARK4 AS CHK_KOUDOU4, ";
        $query .= "     L9.REMARK5 AS CHK_KOUDOU5, ";
        $query .= "     L9.REMARK6 AS CHK_KOUDOU6, ";
        $query .= "     L9.REMARK7 AS CHK_KOUDOU7, ";
        $query .= "     L9.REMARK8 AS CHK_KOUDOU8, ";
        $query .= "     L9.REMARK9 AS CHK_KOUDOU9, ";
        $query .= "     L9.REMARK10 AS CHK_KOUDOU10, ";
        //４．出欠・健康の記録
        $query .= "     L0.ABSENCE_DAYS AS ABSENCE_DAYS1, ";
        $query .= "     L0.ABSENCE_REMARK AS ABSENCE_REMARK1, ";
        $query .= "     L0.ABSENCE_DAYS2, ";
        $query .= "     L0.ABSENCE_REMARK2, ";
        $query .= "     L0.ABSENCE_DAYS3, ";
        $query .= "     L0.ABSENCE_REMARK3, ";
        //健康状況
        $query .= "     L4.REMARK3 AS SHOKEN_KENKOU, ";
        //５．その他参考となる諸事項
        $query .= "     L0.REMARK2 AS SHOKEN_SONOTA, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN  ENTEXAM_APPLICANTCONFRPT_DAT L0 ";
        $query .= "          ON L0.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND L0.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L0.EXAMNO       = T1.EXAMNO ";
        for ($i = 1; $i <= 15; $i++) {
            if ($i == 3 || $i == 6 || $i == 7 || $i == 8 || $i == 10) {
                continue;
            }
            $seq = sprintf("%03d", $i);
            $query .= "     LEFT JOIN  ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT L{$i} ";
            $query .= "          ON L{$i}.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "         AND L{$i}.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "         AND L{$i}.EXAMNO       = T1.EXAMNO ";
            $query .= "         AND L{$i}.SEQ          = '{$seq}' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "      T1.EXAMNO ";

        return $query;
    }

    //CSVデータ出力（得点）
    public function selectMainQuery3($model, $class_array)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     '".$model->field["DATADIV"]."' AS DATADIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.TESTDIV, ";
        //得点
        foreach ($class_array as $classcd => $label) {
            $query .= "     L{$classcd}.SCORE AS SCORE{$classcd}, ";
        }
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        //得点
        foreach ($class_array as $classcd => $label) {
            $query .= "     LEFT JOIN  ENTEXAM_SCORE_DAT L{$classcd} ";
            $query .= "          ON L{$classcd}.ENTEXAMYEAR    = T1.ENTEXAMYEAR ";
            $query .= "         AND L{$classcd}.APPLICANTDIV   = T1.APPLICANTDIV ";
            $query .= "         AND L{$classcd}.TESTDIV        = T1.TESTDIV ";
            $query .= "         AND L{$classcd}.RECEPTNO       = T1.EXAMNO ";
            $query .= "         AND L{$classcd}.TESTSUBCLASSCD = '{$classcd}' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "      T1.EXAMNO ";

        return $query;
    }

    //CSVデータ出力（開示資料）
    public function selectMainQuery4($model, $class_array)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     '".$model->field["DATADIV"]."' AS DATADIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.TESTDIV, ";
        //得点
        foreach ($class_array as $classcd => $label) {
            $query .= "     L{$classcd}.SCORE AS SCORE{$classcd}, ";
        }
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        //得点
        foreach ($class_array as $classcd => $label) {
            $query .= "     LEFT JOIN  ENTEXAM_KAIJI_DAT L{$classcd} ";
            $query .= "          ON L{$classcd}.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
            $query .= "         AND L{$classcd}.APPLICANTDIV     = T1.APPLICANTDIV ";
            $query .= "         AND L{$classcd}.TESTDIV          = T1.TESTDIV ";
            $query .= "         AND L{$classcd}.RECEPTNO         = T1.EXAMNO ";
            $query .= "         AND L{$classcd}.KAIJI_SUBCLASSCD = '{$classcd}' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "      T1.EXAMNO ";

        return $query;
    }
}
