<?php

require_once('for_php7.php');

class knjz290a_2Form2
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db  = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //新規
        if ($model->cmd == "new") {
            unset($model->staffcd);
            unset($model->field);
        }

        //警告メッセージを表示しない場合
        $model->iinkai = "";
        if (!isset($model->warning) && isset($model->staffcd) && ($model->cmd != "search")) {
            if ($model->cmd === 'change') {
                $Row =& $model->field;
            } else {
                $query = knjz290a_2Query::getRow($model, $model->staffcd);
                $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            }
        } elseif ($model->cmd == "search") {
            //職員マスタ存在チェック
            $query = knjz290a_2Query::getRow($model, $model->field["STAFFCD"]);
            $check = $db->getRow($query);
            if (is_array($check)) {
                $model->setWarning("この職員コードは既に登録されています。");
                $Row["STAFFCD"] = $model->field["STAFFCD"];
            } else {
                //教育委員会から表示
                $query = knjz290a_2Query::getRow($model, $model->field["STAFFCD"]);
                $Row = $db2->getRow($query, DB_FETCHMODE_ASSOC);
                if ($Row["STAFFCD"]) {
                    $model->staffcd = $Row["STAFFCD"];
                    $model->iinkai = 1;
                } else {
                    $model->setWarning("MSG303", "　　（教育委員会）");
                    $Row["STAFFCD"] = $model->field["STAFFCD"];
                }
            }
        } else {
            $Row =& $model->field;
            $Row2 = & $model->schoolKindCheck;
        }

        //教育委員会用学校コード取得
        $query = knjz290a_2Query::getEdboardSchoolcd();
        $model->edboard_schoolcd = $db->getOne($query);

        //職員区分取得
        $query = knjz290a_2Query::getStaffDiv($model->edboard_schoolcd, $model->staffcd);
        $model->staff_div = $db2->getOne($query);

        if ($model->iinkai) {
            $arg["insert"] = 1;
            $readonly = " readonly style=\"background-color:lightgray;\"";
        } elseif ($model->staffcd) {
            $arg["update"] = 1;
            $readonly = " readonly style=\"background-color:lightgray;\"";
        } else {
            $arg["insert"] = 1;
            $readonly = "";
        }

        //担当保健室表示セット
        if ($model->Properties["useNurseoffRestrict"] == '1') {
            $arg["data"]["SET_COLWITH"] = 'width="15%"';
            $arg["useNurse"] = "1";
        } else {
            $arg["data"]["SET_COLWITH"] = 'colspan="7"';
        }

        //職員コード
        $setsize = "";
        //STAFFCDフィールドサイズ変更対応
        if ($model->Properties["useStaffcdFieldSize"] === '10') {
            $setsize = 10;
        } else {
            $setsize = 8;
        }
        $extra = ($model->staffcd) ? " readonly style=\"background-color:lightgray;\"" : "onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data"]["STAFFCD"] = knjCreateTextBox($objForm, $Row["STAFFCD"], "STAFFCD", $setsize, $setsize, $extra.$readonly);

        //職員氏名
        $extra = "";
        $arg["data"]["STAFFNAME"] = knjCreateTextBox($objForm, $Row["STAFFNAME"], "STAFFNAME", 40, 60, $extra.$readonly);

        //職員氏名表示用
        $extra = "";
        $arg["data"]["STAFFNAME_SHOW"] = knjCreateTextBox($objForm, $Row["STAFFNAME_SHOW"], "STAFFNAME_SHOW", 10, 15, $extra.$readonly);

        //職員氏名かな
        $extra = "";
        $arg["data"]["STAFFNAME_KANA"] = knjCreateTextBox($objForm, $Row["STAFFNAME_KANA"], "STAFFNAME_KANA", 80, 120, $extra.$readonly);

        //職員氏名英字
        $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data"]["STAFFNAME_ENG"] = knjCreateTextBox($objForm, $Row["STAFFNAME_ENG"], "STAFFNAME_ENG", 40, 60, $extra.$readonly);

        //戸籍氏名
        $extra = "";
        $arg["data"]["STAFFNAME_REAL"] = knjCreateTextBox($objForm, $Row["STAFFNAME_REAL"], "STAFFNAME_REAL", 80, 120, $extra.$readonly);

        //戸籍氏名かな
        $extra = "";
        $arg["data"]["STAFFNAME_KANA_REAL"] = knjCreateTextBox($objForm, $Row["STAFFNAME_KANA_REAL"], "STAFFNAME_KANA_REAL", 80, 240, $extra.$readonly);

        //学校側表示項目
        makeSchoolSide($objForm, $arg, $db, $Row, $Row2, $model);

        //性別コンボボックス
        $query = knjz290a_2Query::getSTAFFSEX();
        makeCmb($objForm, $arg, $db, $query, "STAFFSEX", $Row["STAFFSEX"], "", 1, "", "BLANK");

        //職員生年月日
        $Row["STAFFBIRTHDAY"] = str_replace("-", "/", $Row["STAFFBIRTHDAY"]);
        $extra = "";
        //青山学院
        if ($model->getSchoolName == "aoyama") {
            $extra = "changeDateStr(this, 'STAFFAGE_STR', '歳', '年齢年月日', '職員生年月日');";
        }
        $arg["data"]["STAFFBIRTHDAY"] = View::popUpCalendar2($objForm, "STAFFBIRTHDAY", $Row["STAFFBIRTHDAY"], "", $extra);

        //青山学院以外 職員生年月日のCOLSPAN
        $arg["data"]["SET_COLSPAN_AOYAMA_STAFFBIRTHDAY"] = 'colspan="5"';

        //青山学院
        if ($model->getSchoolName == "aoyama") {
            $arg["aoyama"] = "1";

            //青山学院 職員生年月日のCOLSPAN
            $arg["data"]["SET_COLSPAN_AOYAMA_STAFFBIRTHDAY"] = '';

            //年齢年月日
            $staffage = $model->getDateDiffStr($Row["STAFFBIRTHDAY"], "歳");
            $arg["data"]["STAFFAGE"] = ($staffage != "") ? $staffage : "歳 ヶ月";

            //学院就任年月日
            $Row["GAKUINSYUNIN_DATE"] = str_replace("-", "/", $Row["GAKUINSYUNIN_DATE"]);
            $extra = "changeDateStr(this, 'GAKUINKINZOKU_STR', '年', '学院勤続', '学院就任年月日');";
            $arg["data"]["GAKUINSYUNIN_DATE"] = View::popUpCalendar2($objForm, "GAKUINSYUNIN_DATE", $Row["GAKUINSYUNIN_DATE"], "", $extra);

            //高等部就任年月日
            $Row["KOUTOUBUSYUNIN_DATE"] = str_replace("-", "/", $Row["KOUTOUBUSYUNIN_DATE"]);
            $extra = "changeDateStr(this, 'KOUTOUBUKINZOKU_STR', '年', '高等部勤続', '高等部就任年月日');";
            $arg["data"]["KOUTOUBUSYUNIN_DATE"]   = View::popUpCalendar2($objForm, "KOUTOUBUSYUNIN_DATE", $Row["KOUTOUBUSYUNIN_DATE"], "", $extra);

            //学院勤続
            $gakuinkinzoku = $model->getDateDiffStr($Row["GAKUINSYUNIN_DATE"], "年");
            $arg["data"]["GAKUINKINZOKU"] = ($gakuinkinzoku != "") ? $gakuinkinzoku : "年 ヶ月";

            //高等部勤続
            $koutoubukinzoku = $model->getDateDiffStr($Row["KOUTOUBUSYUNIN_DATE"], "年");
            $arg["data"]["KOUTOUBUKINZOKU"] = ($koutoubukinzoku != "") ? $koutoubukinzoku : "年 ヶ月";
        }

        //郵便番号
        if ($model->staffcd) {
            $arg["data"]["STAFFZIPCD"] = knjCreateTextBox($objForm, $Row["STAFFZIPCD"], "STAFFZIPCD", 10, 10, $readonly);
        } else {
            $arg["data"]["STAFFZIPCD"] = View::popUpZipCode($objForm, "STAFFZIPCD", $Row["STAFFZIPCD"], "STAFFADDR1");
        }

        //住所１
        $extra = "";
        $arg["data"]["STAFFADDR1"] = knjCreateTextBox($objForm, $Row["STAFFADDR1"], "STAFFADDR1", 50, 90, $extra.$readonly);

        //住所２
        $extra = "";
        $arg["data"]["STAFFADDR2"] = knjCreateTextBox($objForm, $Row["STAFFADDR2"], "STAFFADDR2", 50, 90, $extra.$readonly);

        //電話番号
        $extra = "";
        $arg["data"]["STAFFTELNO"] = knjCreateTextBox($objForm, $Row["STAFFTELNO"], "STAFFTELNO", 14, 14, $extra.$readonly);

        //FAX番号
        $extra = "";
        $arg["data"]["STAFFFAXNO"] = knjCreateTextBox($objForm, $Row["STAFFFAXNO"], "STAFFFAXNO", 14, 14, $extra.$readonly);

        //E-Mail
        $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data"]["STAFFE_MAIL"] = knjCreateTextBox($objForm, $Row["STAFFE_MAIL"], "STAFFE_MAIL", 50, 50, $extra.$readonly);

        //顔写真
        $arg["data"]["FACE_IMG"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/T".$Row["STAFFCD"].".".$model->control_data["Extension"];
        $arg["data"]["IMG_PATH"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/T".$Row["STAFFCD"].".".$model->control_data["Extension"];

        if ($model->Properties["useJpgUpload"] == "1") {
            $arg["useJpgUpload"] = "1";
            //ファイルからの取り込み
            $arg["data"]["FILE_2"] = knjCreateFile($objForm, "FILE_2", "", 102400);
            //実行ボタン
            $extra = "onclick=\"return btn_submit('execute');\"";
            $arg["button"]["BTN_IMPORT"] = knjCreateBtn($objForm, "btn_import", "実 行", $extra);
        }
        if ($model->Properties["useDispUnDispPicture"] === '1') {
            $arg["unDispPicture"] = "1";
        } else {
            $arg["dispPicture"] = "1";
        }

        //履歴有効開始日登録
        makeRirekiSdate($objForm, $arg, $db, $model);

        //資格教科登録
        makeShikaku($objForm, $arg, $db, $model);

        //届け登録
        makeTodoke($objForm, $arg, $db2, $model);

        //免許・資格登録
        makeMenkyo($objForm, $arg, $db2, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);
        
        $arg["show_csv"] = "ON";
        //CSVファイルアップロードコントロール
        makeCsv($objForm, $arg, $model, $db);

        //hidden作成
        makeHidden($objForm, $Row);

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz290a_2index.php", "", "edit");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjz290a_2index.php?cmd=list';";
        }
                                
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz290a_2Form2.html", $arg);
    }
}

//学校側表示項目
function makeSchoolSide(&$objForm, &$arg, $db, $Row, $Row2, $model)
{

    //画面表示セット
    $arg["data"]["SET_COLSPAN"] = "3";
    $arg["data"]["SET_BGCOLOR"] = 'bgcolor="#00BFFF"';
    $arg["data"]["SET_BGCOLOR2"]   = 'bgcolor="#99eaff"';

    //職名コンボボックス
    $query = knjz290a_2Query::getJOBNAME();
    makeCmb($objForm, $arg, $db, $query, "JOBCD", $Row["JOBCD"], "", 1, "", "BLANK");

    //所属コンボボックス
    $query = knjz290a_2Query::getSECTION();
    makeCmb($objForm, $arg, $db, $query, "SECTIONCD", $Row["SECTIONCD"], "", 1, "", "BLANK");

    //校務分掌部コンボボックス
    $query = knjz290a_2Query::getDUTYSHARE();
    $extra = " onchange=\"return chkSelSameValue()\" ";
    makeCmb($objForm, $arg, $db, $query, "DUTYSHARECD", $Row["DUTYSHARECD"], $extra, 1, "", "BLANK");
    makeCmb($objForm, $arg, $db, $query, "DUTYSHARECD2", $Row["DUTYSHARECD2"], $extra, 1, "", "BLANK");

    //授業受持ちコンボボックス
    $opt   = array();
    $opt[] = array("label" => "", "value" => "");
    $opt[] = array("label" => "0 無し", "value" => "0");
    $opt[] = array("label" => "1 有り", "value" => "1");
    $extra = "";
    $arg["data"]["CHARGECLASSCD"] = knjCreateCombo($objForm, "CHARGECLASSCD", $Row["CHARGECLASSCD"], $opt, $extra, 1);

    //肩書き1
    $extra = "onchange=\"return btn_submit('change', 1);\"";
    $query = knjz290a_2Query::getPositionCd($model);
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD1", $Row["POSITIONCD1"], $extra, 1, "", "BLANK");
    //肩書き1の学年主任、教科主任
    if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
        setPositionExtCmb($objForm, $arg, $db, $model, $Row, 1, "005");
    } else {
        setPositionCmb($objForm, $arg, $db, $model, $Row, 1, "005");
    }

    //肩書き2
    $extra = "onchange=\"return btn_submit('change', 2);\"";
    $query = knjz290a_2Query::getPositionCd($model);
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD2", $Row["POSITIONCD2"], $extra, 1, "", "BLANK");
    //肩書き2の学年主任、教科主任
    if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
        setPositionExtCmb($objForm, $arg, $db, $model, $Row, 2, "006");
    } else {
        setPositionCmb($objForm, $arg, $db, $model, $Row, 2, "006");
    }

    //肩書き3
    $extra = "onchange=\"return btn_submit('change', 3);\"";
    $query = knjz290a_2Query::getPositionCd($model);
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD3", $Row["POSITIONCD3"], $extra, 1, "", "BLANK");
    //肩書き3の学年主任、教科主任
    if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
        setPositionExtCmb($objForm, $arg, $db, $model, $Row, 3, "007");
    } else {
        setPositionCmb($objForm, $arg, $db, $model, $Row, 3, "007");
    }

    //担当保健室コンボ
    $query = knjz290a_2Query::getVNameMst("Z043", $model);
    $extra = "";
    makeCmb($objForm, $arg, $db, $query, "CHARGENURSEOFF", $Row["CHARGENURSEOFF"], $extra, 1, "BLANK");

    //担当校種チェックボックス
    $model->checkArray = array();
    $result = $db->query(knjz290a_2Query::getSchoolKind($model));

    //STAFF_DETAIL_MSTのFIELD1～5の値の配列を生成
    $staffFields = array();
    foreach (range(1, 5) as $num) {
        $val = $Row["SCHKINDFIELD".$num];
        if (strlen(!$val)) {
            continue;
        }
        $staffFields[] = $val;
    }
    if ($Row2) {
        $staffFields = $Row2;
    }
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setName = "CHK_{$row["NAME1"]}";
        $row["SETNAME"] = $setName;

        $checked = in_array($row["NAME1"], $staffFields) ? "checked" : "";
        $extra = " id=\"{$setName}\" ".$checked;
        $row["CHK_SCHKIND"] = knjCreateCheckBox($objForm, $setName, $row["NAME1"], $extra);

        $model->checkArray[] = $setName;
        $arg["schKindList"][] = $row;
    }
    knjCreateHidden($objForm, "checkArray", implode(",", $model->checkArray));

    if ($model->sendSubmit != "") {
        $arg["school"] = 1;
    }
}

//肩書詳細
function setPositionCmb(&$objForm, &$arg, $db, $model, $Row, $positionNum, $staffSeq)
{
    //肩書きの学年主任、教科主任
    if ($Row["POSITIONCD{$positionNum}"] === '0200' || $Row["POSITIONCD{$positionNum}"] === '1050') {
        $arg["MANAGER{$positionNum}_SET"] = "1";
        //学年主任　教科主任
        if ($Row["POSITIONCD{$positionNum}"] === '0200') {
            $query = knjz290a_2Query::getGrade($model);
        } else {
            $query = knjz290a_2Query::getClass($model);
        }
    } else {
        return true;
    }
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD{$positionNum}_MANAGER", $Row["POSITIONCD{$positionNum}_MANAGER"], "", 1, "", "BLANK");
}

//肩書詳細EXT
function setPositionExtCmb(&$objForm, &$arg, $db, $model, $Row, $positionNum, $staffSeq)
{

    //肩書きの学年主任、教科主任
    $setClass = "";
    $setSize = "1";
    $positionNo = $Row["POSITIONCD{$positionNum}"];
    if ($positionNo === '0200' || $positionNo === '1050') {
        $arg["MANAGER{$positionNum}_SET"] = "1";
        //学年主任　教科主任
        if ($positionNo === '0200') {
            $query = knjz290a_2Query::getGrade($model);
        } else {
            if ($model->cmd == "change") {
                $Row["POSITIONCD{$positionNum}_MANAGER"] = array();
                $Row["POSITIONCD{$positionNum}_MANAGER"] = explode(",", $model->field["POSITION{$positionNum}_EXT"]);
            } else {
                $query = knjz290a_2Query::getStaffExt($model, $staffSeq);
                $result = $db->query($query);
                $Row["POSITIONCD{$positionNum}_MANAGER"] = array();
                while ($extRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $Row["POSITIONCD{$positionNum}_MANAGER"][] = $extRow["VALUE"];
                }
                $result->free();
            }

            $query = knjz290a_2Query::getClass($model);
            $arg["setBR{$positionNum}"] = "<BR>";
            $setMultiple = "1";
            $setSize = "10";
            $setBtnText = "教科登録";
            if ($model->field["POSITIONCD_NUM"] == $positionNum) {
                $setClass .= " style=\"width:300px\" ";
                $setBtnText = "閉じる";
            } else {
                $setClass .= " style=\"display:none; width:300px\" ";
            }

            //表示/非表示ボタン
            $extra = " id=\"BTN_POSITION{$positionNum}\" onclick=\"changeDisplay('{$positionNum}')\"";
            $arg["BTN_POSITION{$positionNum}"] = knjCreateBtn($objForm, "BTN_POSITION{$positionNum}", $setBtnText, $extra);
        }
    } else {
        return true;
    }
    $extra = $setClass." id=\"POSITIONCD{$positionNum}_MANAGER\" ";
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD{$positionNum}_MANAGER", $Row["POSITIONCD{$positionNum}_MANAGER"], $extra, $setSize, $setMultiple, "BLANK");
}

//履歴有効開始日登録
function makeRirekiSdate(&$objForm, &$arg, $db, $model)
{

    //氏名有効開始日
    $arg["data"]["NAME_SDATE"] = View::popUpCalendar($objForm, "NAME_SDATE", $model->field["NAME_SDATE"]);

    //住所有効開始日
    $arg["data"]["ADDRESS_SDATE"] = View::popUpCalendar($objForm, "ADDRESS_SDATE", $model->field["ADDRESS_SDATE"]);

    if (!$model->staffcd && !$model->iinkai) {
        $arg["rireki"] = 1;
    }
}

//資格教科登録
function makeShikaku(&$objForm, &$arg, $db, $model)
{
    $disable = ($model->staffcd && !$model->iinkai) ? "" : " disabled";

    //資格教科登録ボタン
    $extra = "onclick=\"return btn_submit('subform1');\"";
    $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "資格教科登録", $extra.$disable);

    //資格科教科表示
    $query = knjz290a_2Query::getStaffClass($model);
    $arg["data"]["STF_SUBCLASS"] = implode(',', $db->getCol($query));

    if ($model->sendSubmit != "") {
        $arg["shikaku"] = 1;
    }
}

//届け登録
function makeTodoke(&$objForm, &$arg, $db2, $model)
{

    //届け登録ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ291_STAFF_REQUESTFORM/knjz291_staff_requestformindex.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ290A_2&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $extra .= ($model->staffcd && $model->staff_div != '2' && !$model->iinkai) ? "" : " disabled";
    $arg["button"]["btn_requestform"] = knjCreateBtn($objForm, "btn_requestform", "届け登録", $extra);

    //届け表示
    $query = knjz290a_2Query::getStaffRequestform($model);
    $arg["data"]["STF_REQFORM"] = implode(',', $db2->getCol($query));
}

//免許・資格登録
function makeMenkyo(&$objForm, &$arg, $db2, $model)
{

    //免許・資格登録ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ291_STAFF_QUALIFIED/knjz291_staff_qualifiedindex.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ290A_2&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $extra .= ($model->staffcd && $model->staff_div != '2' && !$model->iinkai) ? "" : " disabled";
    $arg["button"]["btn_qualified"] = knjCreateBtn($objForm, "btn_qualified", "免許・資格登録", $extra);

    //免許・資格表示
    $data = array();
    $query = knjz290a_2Query::getStaffQualified($model);
    $result = $db2->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $sep = "";
        if ($row["QUALIFIED_CD"] != "" && $row["QUALIFIED_NAME"] != "") {
            $sep = "/";
        }

        $data[] = $row["QUALIFIED_CD"].$sep.$row["QUALIFIED_NAME"];
    }
    $arg["data"]["STF_QUALIFIED"] = implode(',', $data);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    $disable = ($model->auth == DEF_UPDATABLE) ? "" : " disabled";

    //新規ボタン
    $extra = "onclick=\"return btn_submit('new');\"";
    $arg["button"]["btn_new"] = knjCreateBtn($objForm, "btn_new", "新 規", $extra);

    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra.$disable);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disable);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra.$disable);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    if ($model->sendSubmit == "1") {
        $link = REQUESTROOT."/Z/KNJZ290A/knjz290aindex.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
        $name = "戻 る";
    } else {
        $extra = "onclick=\"return closeWin();\"";
        $name = "終 了";
    }
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", $name, $extra);

    //赴任履歴修正ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ291_STAFF_WORKHIST/knjz291_staff_workhistindex.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ290A_2&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $extra .= ($model->staffcd && $model->staff_div != '2' && !$model->iinkai) ? "" : " disabled";
    $arg["button"]["btn_workhist"] = knjCreateBtn($objForm, "btn_workhist", "赴任履歴修正", $extra);

    //氏名履歴登録ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ291_STAFF_NAME_HIST/knjz291_staff_name_histindex.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ290A_2&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd."&SEND_STAFF_DIV=".$model->staff_div;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_namehist"] = ($model->staffcd && !$model->iinkai) ? knjCreateBtn($objForm, "btn_namehist", "※職員氏名", $extra) : "※職員氏名";

    //住所履歴登録ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ291_STAFF_ADDRESS/knjz291_staff_addressindex.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ290A_2&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd."&SEND_STAFF_DIV=".$model->staff_div;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_address"] = ($model->staffcd && !$model->iinkai) ? knjCreateBtn($objForm, "btn_address", "住所1", $extra) : "住所1";

    //校長履歴登録ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ290S1/knjz290s1index.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ290A_2&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $extra .= ($model->staffcd && !$model->iinkai) ? "" : " disabled";
    $arg["button"]["btn_prihist"] = knjCreateBtn($objForm, "btn_prihist", "校長履歴登録", $extra);
    if ($model->sendSubmit != "") {
        $arg["koutyou"] = 1;
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $multiple, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    if ($multiple != "1") {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size, $multiple);
}

//ＣＳＶ作成
function makeCsv(&$objForm, &$arg, $model, $db)
{
    //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー書出 4:見本
    $opt_shubetsu = array(1, 2, 3, 4);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $click = " onclick=\"return changeRadio(this);\"";
    $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click);
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["data"][$key] = $val;
    }

    //ファイルからの取り込み
    $extra = ($model->field["OUTPUT"] == "1") ? "" : "disabled";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //ヘッダ有チェックボックス
    $check_header = "checked id=\"HEADER\"";
    $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");

    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["data"]["CSV_XLS_NAME"] = "ＣＳＶ出力<BR>／ＣＳＶ取込";

    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}

//hidden作成
function makeHidden(&$objForm, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJZ290A_2");
    knjCreateHidden($objForm, "TEMPLATE_PATH");
    knjCreateHidden($objForm, "POSITIONCD_NUM");
    knjCreateHidden($objForm, "POSITION1_EXT");
    knjCreateHidden($objForm, "POSITION2_EXT");
    knjCreateHidden($objForm, "POSITION3_EXT");
    knjCreateHidden($objForm, "HID_DUTYSHARECD", $Row["DUTYSHARECD"]);
    knjCreateHidden($objForm, "HID_DUTYSHARECD_2", $Row["DUTYSHARECD2"]);
}
