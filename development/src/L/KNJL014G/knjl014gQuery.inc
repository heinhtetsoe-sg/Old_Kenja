<?php

require_once('for_php7.php');
class knjl014gquery extends Query
{
    /******************/
    /**  共通で使用  **/
    /******************/

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
        $result = $db->query(knjl014gQuery::getNameMstAll($model->ObjYear, "L007"));
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
                    list($y, $m, $d) = preg_split('/-/', $data_arr[$i][$dkey]);
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
            $base_cnt       = $db->getOne(knjl014gQuery::cntEntexamApplicantbaseDat($model, $data_arr[$i]));
            $addr_cnt       = $db->getOne(knjl014gQuery::cntEntexamApplicantaddrDat($model, $data_arr[$i]));
            $interview_cnt  = $db->getOne(knjl014gQuery::cntEntexamInterviewDat($model, $data_arr[$i]));
            $baseD001_cnt   = $db->getOne(knjl014gQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "001"));
            $baseD004_cnt   = $db->getOne(knjl014gQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "004"));
            $baseD017_cnt   = $db->getOne(knjl014gQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "017"));
            $baseD018_cnt   = $db->getOne(knjl014gQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "018"));
            $baseD019_cnt   = $db->getOne(knjl014gQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "019"));

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $data["TESTDIV"][TEXT]              = $data_arr[$i]["TESTDIV"];
            $data["SHDIV"][TEXT]                = $data_arr[$i]["SHDIV"];
            $data["DESIREDIV"][TEXT]            = '1';
            $data["RECEPTDATE"][TEXT]           = $data_arr[$i]["RECEPTDATE"];
            $data["NAME"][TEXT]                 = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]            = $data_arr[$i]["NAME_KANA"];
            $data["SEX"][TEXT]                  = $data_arr[$i]["SEX"];
            $data["ERACD"][TEXT]                = $birth_eracd;
            $data["BIRTH_Y"][TEXT]              = $birth_y;
            $data["BIRTH_M"][TEXT]              = $birth_m;
            $data["BIRTH_D"][TEXT]              = $birth_d;
            $data["BIRTHDAY"][TEXT]             = $data_arr[$i]["BIRTHDAY"];
            $data["FS_CD"][TEXT]                = $data_arr[$i]["FS_CD"];
            $data["FS_GRDYEAR"][TEXT]           = $fs_s;
            $data["FS_ERACD"][TEXT]             = $fs_eracd;
            $data["FS_Y"][TEXT]                 = $fs_y;
            $data["FS_M"][TEXT]                 = $fs_m;
            $data["FS_DAY"][TEXT]               = $data_arr[$i]["FS_DAY"];
            $data["FS_GRDDIV"][TEXT]            = $data_arr[$i]["FS_GRDDIV"];
            $data["REMARK1"][TEXT]              = $data_arr[$i]["REMARK1"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
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
            $data["RELATIONSHIP"][TEXT]    = $data_arr[$i]["RELATIONSHIP"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $addr_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_INTERVIEW_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]          = $data_arr[$i]["TESTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["INTERVIEW_REMARK"][TEXT] = $data_arr[$i]["INTERVIEW_REMARK"];
            $data["INTERVIEW_VALUE"][TEXT]  = $data_arr[$i]["INTERVIEW_VALUE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       TESTDIV       = '{$data_arr[$i]["TESTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $interview_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_INTERVIEW_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=001
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->ObjYear;
            $data["EXAMNO"][TEXT]        = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]           = "001";
            $data["REMARK8"][TEXT]       = 1;
            $data["REMARK9"][TEXT]       = $data_arr[$i]["DAI1_MAJORCD"];
            $data["REMARK10"][TEXT]      = $data_arr[$i]["DAI1_COURSECODE"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '001' ";

            if (0 == $baseD001_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=004
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->ObjYear;
            $data["EXAMNO"][TEXT]        = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]           = "004";
            $data["REMARK9"][TEXT]       = $data_arr[$i]["JIZEN_SOUDAN_CD"];
            $data["REMARK10"][TEXT]      = $data_arr[$i]["JIZEN_SOUDAN_TEXT"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '004' ";

            if (0 == $baseD004_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=017
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "017";
            $data["REMARK1"][TEXT]          = (strlen($data_arr[$i]["DAI2_COURSECODE"]) > 0) ? "1" : "";
            $data["REMARK2"][TEXT]          = $data_arr[$i]["DAI2_MAJORCD"];
            $data["REMARK3"][TEXT]          = $data_arr[$i]["DAI2_COURSECODE"];
            //柏原のみ
            if ($model->isKasiwara == "1") {
                $data["REMARK4"][TEXT]          = $data_arr[$i]["DAI2_SENPEIDIV"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '017' ";

            if (0 == $baseD017_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=018
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "018";
            $data["REMARK1"][TEXT]          = $data_arr[$i]["SINZOKU_NAME"];
            $data["REMARK2"][TEXT]          = $data_arr[$i]["SINZOKU_KYUSEI"];
            $data["REMARK3"][TEXT]          = $data_arr[$i]["SINZOKU_ZOKUGARA"];
            $data["REMARK4"][TEXT]          = $data_arr[$i]["SINZOKU_GAKKOUMEI"];
            $data["REMARK5"][TEXT]          = $data_arr[$i]["SINZOKU_GAKKA"];
            $data["REMARK6"][TEXT]          = $data_arr[$i]["SINZOKU_ZAI_GRD_YEAR"];
            $data["REMARK7"][TEXT]          = $data_arr[$i]["SINZOKU_ZAI_GRD_FLG"];
            $data["REMARK8"][TEXT]          = $data_arr[$i]["SINZOKU_FUTAGO_NAME"];
            $data["REMARK9"][TEXT]          = $data_arr[$i]["SINZOKU_FUTAGO_ZOKUGARA"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '018' ";

            if (0 == $baseD018_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=019
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "019";
            $data["REMARK1"][TEXT]          = $data_arr[$i]["CLUB_CD"];
            $data["REMARK2"][TEXT]          = $data_arr[$i]["CLUB_RANK"];
            //柏原のみ
            if ($model->isKasiwara == "1") {
                $data["REMARK3"][TEXT]          = $data_arr[$i]["HOPE_CLUB_CD"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '019' ";

            if (0 == $baseD019_cnt) {
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
        $result = $db->query(knjl014gQuery::getNameMstAll($model->ObjYear, "L008"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $class_array[] = $row["NAMECD2"];
        }

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データ件数取得
            $confRpt_cnt    = $db->getOne(knjl014gQuery::cntEntexamApplicantconfrptDat($model, $data_arr[$i]));
            $baseD020_cnt   = $db->getOne(knjl014gQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "020"));

            //ENTEXAM_APPLICANTCONFRPT_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            foreach ($class_array as $class) {
                $data["CONFIDENTIAL_RPT".$class][NUMBER] = $data_arr[$i]["CLASSCD".$class];
            }
            $data["TOTAL_ALL"][NUMBER]          = $data_arr[$i]["TOTAL_ALL"];
            $data["AVERAGE_ALL"][NUMBER]        = $data_arr[$i]["AVERAGE_ALL"];
            $data["ABSENCE_DAYS"][NUMBER]       = $data_arr[$i]["ABSENCE_DAYS"];
            $data["ABSENCE_DAYS2"][NUMBER]      = $data_arr[$i]["ABSENCE_DAYS2"];
            $data["ABSENCE_DAYS3"][NUMBER]      = $data_arr[$i]["ABSENCE_DAYS3"];
            $data["ABSENCE_REMARK"][TEXT]       = $data_arr[$i]["ABSENCE_REMARK"];
            $data["ABSENCE_REMARK2"][TEXT]      = $data_arr[$i]["ABSENCE_REMARK2"];
            $data["ABSENCE_REMARK3"][TEXT]      = $data_arr[$i]["ABSENCE_REMARK3"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $confRpt_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=020
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "020";

            $data["REMARK1"][TEXT]          = $data_arr[$i]["JITURYOKU_KOKUGO"];
            $data["REMARK2"][TEXT]          = $data_arr[$i]["JITURYOKU_EIGO"];
            $data["REMARK3"][TEXT]          = $data_arr[$i]["JITURYOKU_SUUGAKU"];
            $data["REMARK4"][TEXT]          = $data_arr[$i]["JITURYOKU_TOTAL"];
            $data["REMARK5"][TEXT]          = $data_arr[$i]["MOGI_HENSATI"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '020' ";

            if (0 == $baseD020_cnt) {
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
        $query .= "     EXAMNO      = '{$data_arr["EXAMNO"]}' ";

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
        $query .= "     EXAMNO      = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_INTERVIEW_DAT
    public function cntEntexamInterviewDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_INTERVIEW_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     TESTDIV         = '{$data_arr["TESTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' ";

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
        $query .= "     EXAMNO      = '{$data_arr["EXAMNO"]}' ";

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

    //(姉妹)姉妹中高区分名取得
    public function getSisterJHName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 || ':' || ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' AND ";
        $query .= "     NAME1 IN ('J','H') ";
        $query .= " ORDER BY ";
        $query .= "     NAME1 DESC ";

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
        $query .= "     T1.RECEPTDATE, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.SHDIV, ";
        //第１志望
        $query .= "     T1.DAI1_MAJORCD, ";
        $query .= "     T1.DAI1_COURSECODE, ";
        $query .= "     DAI1.EXAMCOURSE_NAME AS DAI1_COURSE_NAME, ";
        //第２志望
        $query .= "     T1.DAI2_MAJORCD, ";
        $query .= "     T1.DAI2_COURSECODE, ";
        $query .= "     DAI2.EXAMCOURSE_NAME AS DAI2_COURSE_NAME, ";
        //柏原のみ
        if ($model->isKasiwara == "1") {
            $query .= "     T1.DAI2_SENPEIDIV, ";
            $query .= "     T1.HOPE_CLUB_CD, ";
            $query .= "     N2.NAME1 AS HOPE_CLUB_NAME, ";
        }
        //志願者情報
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     L1.ZIPCD, ";
        $query .= "     L1.ADDRESS1, ";
        $query .= "     L1.ADDRESS2, ";
        $query .= "     L1.TELNO, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
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
        //その他
        $query .= "     T1.JIZEN_SOUDAN_CD, ";
        $query .= "     T1.JIZEN_SOUDAN_TEXT, ";
        $query .= "     T1.CLUB_CD, ";
        $query .= "     N1.NAME1 AS CLUB_NAME, ";
        $query .= "     T1.CLUB_RANK, ";
        $query .= "     T1.SINZOKU_NAME, ";
        $query .= "     T1.SINZOKU_KYUSEI, ";
        $query .= "     T1.SINZOKU_ZOKUGARA, ";
        $query .= "     T1.SINZOKU_GAKKOUMEI, ";
        $query .= "     T1.SINZOKU_GAKKA, ";
        $query .= "     T1.SINZOKU_ZAI_GRD_YEAR, ";
        $query .= "     T1.SINZOKU_ZAI_GRD_FLG, ";
        $query .= "     T1.SINZOKU_FUTAGO_NAME, ";
        $query .= "     T1.SINZOKU_FUTAGO_ZOKUGARA, ";
        $query .= "     L2.INTERVIEW_REMARK, ";
        $query .= "     L2.INTERVIEW_VALUE, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        //住所・保護者情報
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT L1 ";
        $query .= "          ON T1.ENTEXAMYEAR      = L1.ENTEXAMYEAR ";
        $query .= "         AND T1.EXAMNO           = L1.EXAMNO ";
        //面接情報
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT L2 ";
        $query .= "          ON T1.ENTEXAMYEAR  = L2.ENTEXAMYEAR ";
        $query .= "         AND T1.EXAMNO       = L2.EXAMNO ";
        //第１受験コース
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST DAI1 ";
        $query .= "          ON T1.ENTEXAMYEAR      = DAI1.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV     = DAI1.APPLICANTDIV ";
        $query .= "         AND T1.TESTDIV          = DAI1.TESTDIV ";
        $query .= "         AND T1.DAI1_COURSECD    = DAI1.COURSECD ";
        $query .= "         AND T1.DAI1_MAJORCD     = DAI1.MAJORCD ";
        $query .= "         AND T1.DAI1_COURSECODE  = DAI1.EXAMCOURSECD ";
        //第２受験コース
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST DAI2 ";
        $query .= "          ON T1.ENTEXAMYEAR      = DAI2.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV     = DAI2.APPLICANTDIV ";
        $query .= "         AND T1.TESTDIV          = DAI2.TESTDIV ";
        $query .= "         AND T1.DAI2_COURSECD    = DAI2.COURSECD ";
        $query .= "         AND T1.DAI2_MAJORCD     = DAI2.MAJORCD ";
        $query .= "         AND T1.DAI2_COURSECODE  = DAI2.EXAMCOURSECD ";
        //出身学校
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ";
        $query .= "          ON T1.FS_CD            = FIN.FINSCHOOLCD ";
        //クラブ
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "          ON T1.ENTEXAMYEAR  = N1.YEAR ";
        $query .= "         AND N1.NAMECD1      = 'L037' ";
        $query .= "         AND N1.NAMECD2      = T1.CLUB_CD ";
        //クラブ
        $query .= "     LEFT JOIN V_NAME_MST N2 ";
        $query .= "          ON T1.ENTEXAMYEAR  = N2.YEAR ";
        $query .= "         AND N2.NAMECD1      = 'L037' ";
        $query .= "         AND N2.NAMECD2      = T1.HOPE_CLUB_CD ";
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
        foreach ($class_array as $key => $label) {
            $query .= "     L1.CONFIDENTIAL_RPT{$key} AS CLASSCD{$key}, ";
        }
        $query .= "     L1.TOTAL_ALL, ";
        $query .= "     L1.AVERAGE_ALL, ";
        $query .= "     L1.ABSENCE_DAYS, ";
        $query .= "     L1.ABSENCE_REMARK, ";
        $query .= "     L1.ABSENCE_DAYS2, ";
        $query .= "     L1.ABSENCE_REMARK2, ";
        $query .= "     L1.ABSENCE_DAYS3, ";
        $query .= "     L1.ABSENCE_REMARK3, ";
        $query .= "     T1.JITURYOKU_KOKUGO, ";
        $query .= "     T1.JITURYOKU_EIGO, ";
        $query .= "     T1.JITURYOKU_SUUGAKU, ";
        $query .= "     T1.JITURYOKU_TOTAL, ";
        $query .= "     T1.MOGI_HENSATI, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT L1 ";
        $query .= "          ON T1.ENTEXAMYEAR  = L1.ENTEXAMYEAR ";
        $query .= "         AND T1.EXAMNO       = L1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "      T1.EXAMNO ";

        return $query;
    }
}
