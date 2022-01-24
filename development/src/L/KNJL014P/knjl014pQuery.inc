<?php

require_once('for_php7.php');
class knjl014pquery extends Query
{
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
        $result = $db->query(knjl014pQuery::getNameMstAll($model->ObjYear, "L007"));
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
                    list($e_id, $y_id, $m_id, $d_id) = array($dval."_eracd", $dval."_y", $dval."_m", $dval."_d");

                    $$e_id = $$y_id = $$m_id = $$d_id = "";
                    for ($ere_no=0; $ere_no < get_count($eracd); $ere_no++) {
                        if ($defsdate[$ere_no] <= $data_arr[$i][$dkey] && $data_arr[$i][$dkey] <= $defedate[$ere_no]) {
                            $$e_id = $eracd[$ere_no];
                            $$y_id = ((int)$y + 1) - ((int)$defyear[$ere_no]);
                            $$y_id = sprintf("%02d", $$y_id);
                            $$m_id = $m;
                            $$d_id = $d;
                            break;
                        }
                    }
                }
            }

            //志望コース　ENTEXAM_COURSE_MSTから取得
            $examCourse = $db->getRow(knjl014pQuery::getExamcourse($model->ObjYear, $data_arr), DB_FETCHMODE_ASSOC);

            //データ件数取得
            $base_cnt       = $db->getOne(knjl014pQuery::cntEntexamApplicantbaseDat($model, $data_arr[$i]));
            $addr_cnt       = $db->getOne(knjl014pQuery::cntEntexamApplicantaddrDat($model, $data_arr[$i]));
            $baseD001_cnt   = $db->getOne(knjl014pQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "001"));
            $baseD008_cnt   = $db->getOne(knjl014pQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "008"));
            $baseD014_cnt   = $db->getOne(knjl014pQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "014"));
            $baseD024_cnt   = $db->getOne(knjl014pQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "024"));

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $data["TESTDIV"][TEXT]              = $data_arr[$i]["TESTDIV"];
            $data["SHDIV"][TEXT]                = '1';
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
            $data["FS_ERACD"][TEXT]             = $fs_eracd;
            $data["FS_Y"][TEXT]                 = $fs_y;
            $data["FS_M"][TEXT]                 = $fs_m;
            $data["FS_DAY"][TEXT]               = $data_arr[$i]["FS_DAY"];
            $data["FS_GRDDIV"][TEXT]            = $data_arr[$i]["FS_GRDDIV"];
            $data["DORMITORY_FLG"][TEXT]        = $data_arr[$i]["DORMITORY_FLG"];
            $data["REMARK1"][TEXT]              = $data_arr[$i]["REMARK1"];
            $data["REMARK2"][TEXT]              = $data_arr[$i]["REMARK2"];
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
            $data["GTELNO2"][TEXT]          = $data_arr[$i]["GTELNO2"];
            $data["RELATIONSHIP"][TEXT]    = $data_arr[$i]["RELATIONSHIP"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $addr_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=001
            $seq = '001';
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = $seq;
            $data["REMARK8"][TEXT]          = $examCourse["COURSECD"];
            $data["REMARK9"][TEXT]          = $examCourse["MAJORCD"];
            $data["REMARK10"][TEXT]         = $examCourse["EXAMCOURSECD"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '{$seq}' ";

            $cntdata = "baseD".$seq."_cnt";
            if (0 == $$cntdata) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=008
            $seq = '008';
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = $seq;
            $data["REMARK1"][TEXT]          = $data_arr[$i]["PRISCHOOLCD"];
            $data["REMARK3"][TEXT]          = $data_arr[$i]["PRISCHOOL_CLASS_CD"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '{$seq}' ";

            $cntdata = "baseD".$seq."_cnt";
            if (0 == $$cntdata) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=014
            $seq = '014';
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = $seq;
            $data["REMARK2"][TEXT]          = $data_arr[$i]["SIMAI_NAME"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '{$seq}' ";

            $cntdata = "baseD".$seq."_cnt";
            if (0 == $$cntdata) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=024
            $seq = '024';
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = $seq;
            $data["REMARK1"][TEXT]          = $data_arr[$i]["SIKEN_KAIJOU_CD1"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '{$seq}' ";

            $cntdata = "baseD".$seq."_cnt";
            if (0 == $$cntdata) {
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
        $result = $db->query(knjl014pQuery::getNameMstAll($model->ObjYear, "L008"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $class_array[] = $row["NAMECD2"];
        }

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データ件数取得
            $confRpt_cnt        = $db->getOne(knjl014pQuery::cntEntexamApplicantconfrptDat($model, $data_arr[$i]));
            $confRptD001_cnt    = $db->getOne(knjl014pQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "001"));
            $confRptD002_cnt    = $db->getOne(knjl014pQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "002"));
            $confRptD004_cnt    = $db->getOne(knjl014pQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "004"));
            $confRptD005_cnt    = $db->getOne(knjl014pQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "005"));
            $confRptD006_cnt    = $db->getOne(knjl014pQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "006"));
            $confRptD007_cnt    = $db->getOne(knjl014pQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "007"));
            $confRptD008_cnt    = $db->getOne(knjl014pQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "008"));

            //ENTEXAM_APPLICANTCONFRPT_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            foreach ($class_array as $class) {
                $confrpt = ($data_arr[$i]["CONFRPT".$class."_3"]) ? $data_arr[$i]["CONFRPT".$class."_3"] : 0;
                $data["CONFIDENTIAL_RPT".$class][NUMBER] = $confrpt;
            }
            $data["REMARK1"][TEXT]              = $data_arr[$i]["REMARK1"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $confRpt_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT  SEQ=001,002
            for ($grade = 1; $grade <= 2; $grade++) {
                $seq = sprintf("%03d", $grade);
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
                $data["SEQ"][TEXT]                  = $seq;
                foreach ($class_array as $class) {
                    $num = (int)$class;
                    $confrpt = ($data_arr[$i]["CONFRPT".$class."_".$grade]) ? $data_arr[$i]["CONFRPT".$class."_".$grade] : 0;
                    $data["REMARK".$num][TEXT]         = $confrpt;
                }

                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "SYSDATE()";

                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
                $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
                $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
                $where .= "       SEQ           = '{$seq}' ";

                $cntdata = "confRptD".$seq."_cnt";
                if (0 == $$cntdata) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
                }
                $db->query($query);
            }

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT  SEQ=004
            $seq = '004';
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]                  = $seq;
            $data["REMARK1"][TEXT]              = $data_arr[$i]["CLASS_CNT"];
            $data["REMARK2"][TEXT]              = $data_arr[$i]["RANK"];
            $data["REMARK3"][TEXT]              = $data_arr[$i]["HEALTH"];
            $data["REMARK4"][TEXT]              = $data_arr[$i]["TOTAKREMARK"];
            $data["REMARK5"][TEXT]              = $data_arr[$i]["OTHERACT"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '{$seq}' ";

            $cntdata = "confRptD".$seq."_cnt";
            if (0 == $$cntdata) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT  SEQ=005
            $seq = '005';
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]                  = $seq;
            $data["REMARK1"][TEXT]              = $data_arr[$i]["SPECIALACT1"];
            $data["REMARK2"][TEXT]              = $data_arr[$i]["SPECIALACT2"];
            $data["REMARK3"][TEXT]              = $data_arr[$i]["SPECIALACT3"];
            $data["REMARK4"][TEXT]              = $data_arr[$i]["TOTALSTUDY1"];
            $data["REMARK5"][TEXT]              = $data_arr[$i]["TOTALSTUDY2"];
            $data["REMARK6"][TEXT]              = $data_arr[$i]["TOTALSTUDY3"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '{$seq}' ";

            $cntdata = "confRptD".$seq."_cnt";
            if (0 == $$cntdata) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT  SEQ=006,007,008
            $seq_array = array("006" => "1", "007" => "2", "008" => "3");
            foreach ($seq_array as $seq => $grade) {
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
                $data["SEQ"][TEXT]                  = $seq;
                $data["REMARK1"][TEXT]              = $data_arr[$i]["ATTEND_PRESENT".$grade];
                $data["REMARK2"][TEXT]              = $data_arr[$i]["ATTEND_KESSEKI".$grade];
                $data["REMARK3"][TEXT]              = $data_arr[$i]["ATTEND_LATE".$grade];
                $data["REMARK4"][TEXT]              = $data_arr[$i]["ATTEND_EARLY".$grade];
                $data["REMARK5"][TEXT]              = $data_arr[$i]["ATTEND_REMARK".$grade];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "SYSDATE()";

                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
                $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
                $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
                $where .= "       SEQ           = '{$seq}' ";

                $cntdata = "confRptD".$seq."_cnt";
                if (0 == $$cntdata) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
                }
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

    //受験番号範囲取得
    public function getEntexamNoDat($entexamyear, $applicantdiv)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$entexamyear}' AND ";
        $query .= "     APPLICANTDIV    = '{$applicantdiv}' ";

        return $query;
    }

    //試験会場
    public function hallCntSql($year, $applicantdiv, $testdiv, $examhallcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$year."' AND ";
        $query .= "     APPLICANTDIV    = '".$applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$testdiv."' AND ";
        $query .= "     EXAM_TYPE       = '1' AND ";
        $query .= "     EXAMHALLCD      = '".$examhallcd."' ";

        return $query;
    }

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

    //塾コード
    public function prischoolCnt($prischoolcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD = '{$prischoolcd}' ";

        return $query;
    }

    //教室コード
    public function prischoolClassCnt($prischoolcd, $prischool_class_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD         = '{$prischoolcd}' AND ";
        $query .= "     PRISCHOOL_CLASS_CD  = '{$prischool_class_cd}' ";

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
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '".sprintf("%06d", $data_arr["EXAMNO"])."' ";

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
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
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
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' AND ";
        $query .= "     SEQ             = '{$seq}' ";

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
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' ";

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
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' AND ";
        $query .= "     SEQ             = '{$seq}' ";

        return $query;
    }

    //志望コース取得
    public function getExamcourse($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     TESTDIV         = '{$data_arr["TESTDIV"]}' ";
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

    //試験会場取得
    public function getHall($year, $applicantdiv, $testdiv)
    {
        $query  = " SELECT ";
        $query .= "     EXAMHALLCD || ':' ||  EXAMHALL_NAME AS LABEL, ";
        $query .= "     EXAMHALLCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$year."' AND ";
        $query .= "     APPLICANTDIV    = '".$applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$testdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSVデータ出力（願書）
    public function selectMainQuery2_1($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '".$model->field["DATADIV"]."' AS DATADIV, ";
        $query .= "     T1.EXAMNO, ";
        //試験会場
        $query .= "     T1.SIKEN_KAIJOU_CD1, ";
        $query .= "     L1.EXAMHALL_NAME, ";
        //受付日付
        $query .= "     T1.RECEPTDATE, ";
        //志願者情報
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     L2.ZIPCD, ";
        $query .= "     L2.ADDRESS1, ";
        $query .= "     L2.ADDRESS2, ";
        $query .= "     L2.TELNO, ";
        //出身学校
        $query .= "     T1.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     T1.FS_DAY, ";
        $query .= "     T1.FS_GRDDIV, ";
        //塾
        $query .= "     T1.JUKUCD AS PRISCHOOLCD, ";
        $query .= "     PRI.PRISCHOOL_NAME, ";
        $query .= "     T1.KYOUSHITSU_CD AS PRISCHOOL_CLASS_CD, ";
        $query .= "     PRI_C.PRISCHOOL_NAME AS PRISCHOOL_CLASS_NAME, ";
        //保護者情報
        $query .= "     L2.GNAME, ";
        $query .= "     L2.GKANA, ";
        $query .= "     L2.GZIPCD, ";
        $query .= "     L2.GADDRESS1, ";
        $query .= "     L2.GADDRESS2, ";
        $query .= "     L2.GTELNO, ";
        $query .= "     L2.GTELNO2, ";
        $query .= "     L2.RELATIONSHIP, ";
        //通学方法
        $query .= "     T1.DORMITORY_FLG, ";
        //本校在籍又は卒業の保護者・兄弟・姉妹
        $query .= "     T1.SIMAI_NAME, ";
        //志願理由
        $query .= "     T1.REMARK1, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_HALL_YDAT L1 ";
        $query .= "              ON T1.ENTEXAMYEAR      = L1.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV     = L1.APPLICANTDIV ";
        $query .= "             AND T1.TESTDIV          = L1.TESTDIV ";
        $query .= "             AND T1.SIKEN_KAIJOU_CD1 = L1.EXAMHALLCD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT L2 ";
        $query .= "              ON T1.ENTEXAMYEAR      = L2.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV     = L2.APPLICANTDIV ";
        $query .= "             AND T1.EXAMNO           = L2.EXAMNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON T1.FS_CD = FIN.FINSCHOOLCD ";
        $query .= "     LEFT JOIN PRISCHOOL_MST PRI ON T1.JUKUCD = PRI.PRISCHOOLCD ";
        $query .= "     LEFT JOIN PRISCHOOL_CLASS_MST PRI_C ";
        $query .= "              ON T1.JUKUCD           = PRI_C.PRISCHOOLCD ";
        $query .= "             AND T1.KYOUSHITSU_CD    = PRI_C.PRISCHOOL_CLASS_CD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //CSVデータ出力（高校・調査書）
    public function selectMainQuery2_2($model, $class_array)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '".$model->field["DATADIV"]."' AS DATADIV, ";
        $query .= "     T1.EXAMNO, ";
        //学習成績
        for ($grade = 1; $grade <= 3; $grade++) {
            foreach ($class_array as $key => $label) {
                $num = (int)$key;
                if ($grade == "3") {
                    $query .= "     L1.CONFIDENTIAL_RPT{$key} AS CONFRPT{$key}_{$grade}, ";
                } else {
                    $query .= "     D{$grade}.REMARK{$num} AS CONFRPT{$key}_{$grade}, ";
                }
            }
        }
        //学習成績順位
        $query .= "     D4.REMARK1 AS CLASS_CNT, ";
        $query .= "     D4.REMARK2 AS RANK, ";
        //出欠状況
        $query .= "     D6.REMARK1 AS ATTEND_PRESENT1, ";
        $query .= "     D6.REMARK2 AS ATTEND_KESSEKI1, ";
        $query .= "     D6.REMARK3 AS ATTEND_LATE1, ";
        $query .= "     D6.REMARK4 AS ATTEND_EARLY1, ";
        $query .= "     D6.REMARK5 AS ATTEND_REMARK1, ";
        $query .= "     D7.REMARK1 AS ATTEND_PRESENT2, ";
        $query .= "     D7.REMARK2 AS ATTEND_KESSEKI2, ";
        $query .= "     D7.REMARK3 AS ATTEND_LATE2, ";
        $query .= "     D7.REMARK4 AS ATTEND_EARLY2, ";
        $query .= "     D7.REMARK5 AS ATTEND_REMARK2, ";
        $query .= "     D8.REMARK1 AS ATTEND_PRESENT3, ";
        $query .= "     D8.REMARK2 AS ATTEND_KESSEKI3, ";
        $query .= "     D8.REMARK3 AS ATTEND_LATE3, ";
        $query .= "     D8.REMARK4 AS ATTEND_EARLY3, ";
        $query .= "     D8.REMARK5 AS ATTEND_REMARK3, ";
        //備考
        $query .= "     L1.REMARK1, ";
        //特別活動の記録
        $query .= "     D5.REMARK1 AS SPECIALACT1, ";
        $query .= "     D5.REMARK2 AS SPECIALACT2, ";
        $query .= "     D5.REMARK3 AS SPECIALACT3, ";
        //総合的な学習の時間の記録
        $query .= "     D5.REMARK4 AS TOTALSTUDY1, ";
        $query .= "     D5.REMARK5 AS TOTALSTUDY2, ";
        $query .= "     D5.REMARK6 AS TOTALSTUDY3, ";
        //健康状況
        $query .= "     D4.REMARK3 AS HEALTH, ";
        //総合所見
        $query .= "     D4.REMARK4 AS TOTAKREMARK, ";
        //その他の活動の記録
        $query .= "     D4.REMARK5 AS OTHERACT, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT L1 ";
        $query .= "          ON T1.ENTEXAMYEAR  = L1.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = L1.APPLICANTDIV ";
        $query .= "         AND T1.EXAMNO       = L1.EXAMNO ";
        for ($i = 1; $i <= 8; $i++) {
            if ($i == 3) {
                continue;
            }
            $seq = sprintf("%03d", $i);
            $query .= "     LEFT JOIN  ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT D{$i} ";
            $query .= "          ON D{$i}.ENTEXAMYEAR   = T1.ENTEXAMYEAR ";
            $query .= "         AND D{$i}.APPLICANTDIV  = T1.APPLICANTDIV ";
            $query .= "         AND D{$i}.EXAMNO        = T1.EXAMNO ";
            $query .= "         AND D{$i}.SEQ           = '{$seq}' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "      T1.EXAMNO ";

        return $query;
    }
}
