<?php

require_once('for_php7.php');

class knjl016fQuery extends Query
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
        $result = $db->query(knjl016fQuery::getNameMstAll($model->ObjYear, "L007"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[]    = $row["NAMECD2"];
            $defyear[]  = $row["NAMESPARE1"];
            $defsdate[] = $row["NAMESPARE2"];
            $defedate[] = $row["NAMESPARE3"];
        }
        $result->free();
        
        //変換用出身学校コード格納
        $fscd = array();
        $query = knjl016fQuery::getFsCd();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $fscd[$row["MIRAI_FS_CD"]] = $row["FS_CD"];
        }
        $result->free();

        //賢者コードに変換
        //中学の英検（級）取得
        $eiken = array();
        $result = $db->query(knjl016fQuery::getNameMstAll($model->ObjYear, "L055"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eiken[$row["NAME1"]] = $row["NAMECD2"];
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
                    list($y, $m, $d) = preg_split("/-/", $data_arr[$i][$dkey]);
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
            $data_arr[$i]["FS_CD"] = $fscd[$data_arr[$i]["MIRAI_FS_CD"]];

            //データ件数取得
            $base_cnt       = $db->getOne(knjl016fQuery::cntEntexamApplicantbaseDat($model, $data_arr[$i]));
            $addr_cnt       = $db->getOne(knjl016fQuery::cntEntexamApplicantaddrDat($model, $data_arr[$i]));
            $baseD001_cnt   = $db->getOne(knjl016fQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "001"));
            $baseD009_cnt   = $db->getOne(knjl016fQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "009"));
            if ($model->field["APPLICANTDIV"] == "1") {
                $baseD010_cnt   = $db->getOne(knjl016fQuery::cntEntexamApplicantbaseDetailBunDat($model, $data_arr[$i], "010"));
                $baseD011_cnt   = $db->getOne(knjl016fQuery::cntEntexamApplicantbaseDetailBunDat($model, $data_arr[$i], "011"));
                $baseD012_cnt   = $db->getOne(knjl016fQuery::cntEntexamApplicantbaseDetailBunDat($model, $data_arr[$i], "012"));
            }
            $baseD014_cnt   = $db->getOne(knjl016fQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "014"));
            $baseD015_cnt   = $db->getOne(knjl016fQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "015"));
            if ($model->field["APPLICANTDIV"] == "2") {
                $baseD016_cnt   = $db->getOne(knjl016fQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "016"));
            }
            $money_cnt      = $db->getOne(knjl016fQuery::cntEntexamMoneyDat($model, $data_arr[$i]));

            //志望コース　ENTEXAM_COURSE_MSTから取得
            $examCourse = $db->getRow(knjl016fQuery::getExamcourse($model, $data_arr[$i]), DB_FETCHMODE_ASSOC);

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];//入試制度
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            if ($model->field["APPLICANTDIV"] == "1") {
                $data["TESTDIV"][TEXT]              = $data_arr[$i]["TEST_L_DIV"];//入試種類
                $data["SHDIV"][TEXT]                = "1";//1:第１志望 2:その他
            } elseif ($model->field["APPLICANTDIV"] == "2") {
                $data["TESTDIV"][TEXT]              = $data_arr[$i]["TESTDIV"];//入試区分
                $data["TESTDIV0"][TEXT]             = $data_arr[$i]["TESTDIV0"];//入試回数
                //第一志望校名が文京学院以外なら「併願校名」に取込み、専願・併願に「2:併願」をセット。
                $data["SHDIV"][TEXT]                = strlen($data_arr[$i]["SH_SCHOOLNAME"]) && (strpos($data_arr[$i]["SH_SCHOOLNAME"], "文京学院") === false) ? "2" : "1";//1:第１志望 2:その他
                //「入学手続の延納願いは…」欄が「はい(受験…」の場合は、願書画面の「入学手続の延期願い」の「提出する」にチェックを入れる。
                $data["SHIFT_DESIRE_FLG"][TEXT]     = strlen($data_arr[$i]["SHIFT_DESIRE_FLG"]) && (strpos($data_arr[$i]["SHIFT_DESIRE_FLG"], "はい") !== false) ? "1" : "";//1:提出する
                //「Ｔクラスへの変更について」欄が「希望する」の場合は、願書画面の「Ｔクラスへの変更」欄の「希望する」にチェックを入れる。
                $data["SLIDE_FLG"][TEXT]            = strlen($data_arr[$i]["SLIDE_FLG"]) && (strpos($data_arr[$i]["SLIDE_FLG"], "希望する") !== false) ? "1" : "";//1:希望する
                //高校特別入試の受験科目（SELECT_SUBCLASS_DIV）の登録。試験名欄を参照
                if ($data_arr[$i]["TESTDIV"] == "7") {
                    if (strpos($data_arr[$i]["EXAM_NAME"], "数学１科型") !== false) {
                        $data["SELECT_SUBCLASS_DIV"][TEXT] = "2";
                    } elseif (strpos($data_arr[$i]["EXAM_NAME"], "英語１科型") !== false) {
                        $data["SELECT_SUBCLASS_DIV"][TEXT] = "5";
                    } elseif (strpos($data_arr[$i]["EXAM_NAME"], "プレゼン") !== false) {
                        $data["SELECT_SUBCLASS_DIV"][TEXT] = "9";
                    }
                }
            }
            $data["DESIREDIV"][TEXT]            = '1';
            $data["RECEPTDATE"][TEXT]           = $data_arr[$i]["RECEPTDATE"];
            $data["NAME"][TEXT]                 = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]            = mb_convert_kana($data_arr[$i]["NAME_KANA"], "c");
            $data["SEX"][TEXT]                  = "2";//固定
            $data["ERACD"][TEXT]                = $birth_eracd;
            $data["BIRTH_Y"][TEXT]              = $birth_y;
            $data["BIRTH_M"][TEXT]              = $birth_m;
            $data["BIRTH_D"][TEXT]              = $birth_d;
            $data["BIRTHDAY"][TEXT]             = $data_arr[$i]["BIRTHDAY"];
            $data["FS_CD"][TEXT]                = $data_arr[$i]["FS_CD"];
            $data["FS_ERACD"][TEXT]             = $fs_eracd;
            $data["FS_Y"][TEXT]                 = $fs_y;
            $data["FS_M"][TEXT]                 = $fs_m;
            $data["FS_GRDDIV"][TEXT]            = "1";//1:見込
            $data["FS_DAY"][TEXT]               = $data_arr[$i]["FS_DAY"];//入試年度."-03-01"
            $data["FS_GRDYEAR"][TEXT]           = $model->ObjYear;
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
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["ZIPCD"][TEXT]            = $data_arr[$i]["ZIPCD"];
            $data["ADDRESS1"][TEXT]         = $data_arr[$i]["ADDRESS1"];
            $data["ADDRESS2"][TEXT]         = $data_arr[$i]["ADDRESS2"];
            $data["TELNO"][TEXT]            = $data_arr[$i]["TELNO"];
            $data["GNAME"][TEXT]            = $data_arr[$i]["GNAME"];
            $data["GKANA"][TEXT]            = mb_convert_kana($data_arr[$i]["GKANA"], "c");
            $data["GZIPCD"][TEXT]           = $data_arr[$i]["ZIPCD"];
            $data["GADDRESS1"][TEXT]        = $data_arr[$i]["ADDRESS1"];
            $data["GADDRESS2"][TEXT]        = $data_arr[$i]["ADDRESS2"];
            $data["GTELNO"][TEXT]           = $data_arr[$i]["TELNO"];
            $data["RELATIONSHIP"][TEXT]     = $data_arr[$i]["RELATIONSHIP"];//rep
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
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
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = $seq;
            $data["REMARK8"][TEXT]          = $examCourse["COURSECD"];
            $data["REMARK9"][TEXT]          = $examCourse["MAJORCD"];
            $data["REMARK10"][TEXT]         = $examCourse["EXAMCOURSECD"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '{$seq}' ";

            $cntdata = "baseD".$seq."_cnt";
            if (0 == $$cntdata) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DAT SEQ=009
            $seq = "009";

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = $seq;
            $data["REMARK2"][TEXT]          = $data_arr[$i]["MIRAI_FS_CD"];
            $data["REMARK3"][TEXT]          = $data_arr[$i]["MIRAI_FS_NAME"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '{$seq}' ";

            $cntdata = "baseD".$seq."_cnt";
            if (0 == $$cntdata) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
        
            $db->query($query);

            if ($model->field["APPLICANTDIV"] == "1") {
                $testdiv = $data_arr[$i]["TESTDIV"];

                //ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT SEQ=010
                $seq = '010';
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
                $data["SEQ"][TEXT]              = $seq;
                $data["REMARK".$testdiv][TEXT]  = $data_arr[$i]["TESTDIV"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
                $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
                $where .= "       SEQ           = '{$seq}' ";

                $cntdata = "baseD".$seq."_cnt";
                if (0 == $$cntdata) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT", $where);
                }
                $db->query($query);

                //ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT SEQ=011
                $seq = '011';
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
                $data["SEQ"][TEXT]              = $seq;
                $data["REMARK".$testdiv][TEXT]  = $data_arr[$i]["EXAM_TYPE"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
                $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
                $where .= "       SEQ           = '{$seq}' ";

                $cntdata = "baseD".$seq."_cnt";
                if (0 == $$cntdata) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT", $where);
                }
                $db->query($query);

                //ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT SEQ=012
                $seq = '012';
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
                $data["SEQ"][TEXT]              = $seq;
                $data["REMARK".$testdiv][TEXT]  = $data_arr[$i]["RECEPTNO"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
                $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
                $where .= "       SEQ           = '{$seq}' ";

                $cntdata = "baseD".$seq."_cnt";
                if (0 == $$cntdata) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT", $where);
                }
                $db->query($query);
            }

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=014:姉妹
            $seq = '014';
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = $seq;
            //中学の英検（級）
            if ($model->field["APPLICANTDIV"] == "1") {
                $index = rtrim(ltrim($data_arr[$i]["EIKEN"], "　"), "　"); //入力値の左右に全角スペースがついている場合、値のみを取得
                $value = isset($eiken[$index]) ? $eiken[$index] : "";
                $data["REMARK9"][TEXT]          = $value; //賢者コードに変換
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '{$seq}' ";

            $cntdata = "baseD".$seq."_cnt";
            if (0 == $$cntdata) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=015:校友会
            $seq = '015';
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = $seq;
            //「減免制度利用について」列が”利用する（卒業生）”なら「入力欄」列を賢者の「校友会の氏名」に取込
            if (strpos($data_arr[$i]["GENMEN_RIYOU"], "利用する") !== false) {
                $data["REMARK1"][TEXT]          = (strlen($data_arr[$i]["SISTER_NAME"]) > 120) ? mb_strcut($data_arr[$i]["SISTER_NAME"], 0, 120) : $data_arr[$i]["SISTER_NAME"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '{$seq}' ";

            $cntdata = "baseD".$seq."_cnt";
            if (0 == $$cntdata) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            if ($model->field["APPLICANTDIV"] == "2") {
                //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=016
                $seq = '016';
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
                $data["SEQ"][TEXT]              = $seq;
                //第一志望校名が文京学院以外なら「併願校名」に取込み、専願・併願に「2:併願」をセット。
                if (strpos($data_arr[$i]["SH_SCHOOLNAME"], "文京学院") === false) {
                    $data["REMARK1"][TEXT]          = strlen($data_arr[$i]["SH_SCHOOLNAME"]) ? $data_arr[$i]["SH_SCHOOLNAME"] : "";
                    $data["REMARK2"][TEXT]          = strlen($data_arr[$i]["SH_JUDGEMENT_DATE"]) ? str_replace('/', '-', $data_arr[$i]["SH_JUDGEMENT_DATE"]) : "";
                }
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
                $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
                $where .= "       SEQ           = '{$seq}' ";

                $cntdata = "baseD".$seq."_cnt";
                if (0 == $$cntdata) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                }
                $db->query($query);
            }

            //ENTEXAM_MONEY_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];//入試制度
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $data["EXAM_PAY_DIV"][TEXT]         = !strlen($data_arr[$i]["EXAM_PAY_DATE"]) ? "" : "1"; //1:振込
            $data["EXAM_PAY_DATE"][TEXT]        = !strlen($data_arr[$i]["EXAM_PAY_DATE"]) ? "" : $data_arr[$i]["EXAM_PAY_DATE"];
            //受験料を取得
            $query = knjl016fQuery::getPayMoney($model, $data_arr[$i]);
            $data["EXAM_PAY_MONEY"][NUMBER]     = !strlen($data_arr[$i]["EXAM_PAY_DATE"]) ? "" : $db->getOne($query);
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $money_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_MONEY_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_MONEY_DAT", $where);
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
    /**  賢者コード変換  **/
    /**********************/

    //続柄コード取得
    public function getNameMstH201($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = 'H201' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        return $query;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //受験番号範囲取得
    public function getEntexamNoDatCnt($model, $data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV    = '{$data["APPLICANTDIV"]}' ";
        $query .= "     AND REMARK_DIV      = '1' ";//1:Web出願　2:窓口・郵送
        if ($model->field["APPLICANTDIV"] == "1") {
            $query .= "     AND TESTDIV         = '{$data["TESTDIV"]}' ";
            $query .= "     AND MEMO_KISO       = '{$data["EXAM_TYPE"]}' ";
            $query .= "     AND '{$data["RECEPTNO"]}' BETWEEN EXAMNO_FROM AND EXAMNO_TO ";
        }
        if ($model->field["APPLICANTDIV"] == "2") {
            $query .= "     AND TESTDIV         = '{$data["TESTDIV"]}' ";
            $query .= "     AND TESTDIV0        = '{$data["TESTDIV0"]}' ";
            $query .= "     AND EXAMCOURSECD    = '{$data["EXAMCOURSECD"]}' ";
            $query .= "     AND '{$data["EXAMNO"]}' BETWEEN EXAMNO_FROM AND EXAMNO_TO ";
        }

//        echo $query."<BR>";
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
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' ";

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
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' AND ";
        $query .= "     SEQ             = '{$seq}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT
    public function cntEntexamApplicantbaseDetailBunDat($model, $data_arr, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
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
        $query .= "     TESTDIV         = '1' ";//固定
        if ($model->field["APPLICANTDIV"] == "2") {
            $query .= "     AND EXAMCOURSECD    = '{$data_arr["EXAMCOURSECD"]}' ";
        }
        return $query;
    }

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

    //件数取得 -- ENTEXAM_MONEY_DAT
    public function cntEntexamMoneyDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MONEY_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //受験料を取得
    public function getPayMoney($model, $data_arr)
    {
        $query  = "";

        //免除額コードを取得(BASE)
        $query .= " WITH V_COMMON AS ( ";
        $query .= "     SELECT ";
        $query .= "         '1' AS LINK, ";
        $query .= "         EXEMPTION_CD ";
        $query .= "     FROM ";
        $query .= "         V_ENTEXAM_APPLICANTBASE_EXEMPTION_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "         AND APPLICANTDIV = '{$data_arr["APPLICANTDIV"]}' ";
        $query .= "         AND EXAMNO = '{$data_arr["EXAMNO"]}' ";
        //基本額を取得（費目マスタ）
        $query .= " ), T_ITEM AS ( ";
        $query .= "     SELECT ";
        $query .= "         '1' AS LINK, ";
        $query .= "         SUM(ITEM_MONEY) AS ITEM_MONEY ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_PAYMENT_ITEM_MST ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "         AND APPLICANTDIV = '{$data_arr["APPLICANTDIV"]}' ";
        $query .= "         AND DIV = '0' ";
        $query .= "         AND REMARK4 = '1' ";//受験料
        $query .= " ) ";
        //基本額から免除額を引いた金額を取得
        $query .= " SELECT ";
        $query .= "     VALUE(L1.ITEM_MONEY, 0) AS MONEY ";
        $query .= " FROM ";
        $query .= "     V_COMMON T1 ";
        $query .= "     LEFT JOIN T_ITEM L1 ON L1.LINK = T1.LINK ";

        return $query;
    }

    /********************/
    /**  データ変換用  **/
    /********************/
    //賢者の出身学校コード取得
    public function getFsCd()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_FS_REP_DAT ";

        return $query;
    }
}
