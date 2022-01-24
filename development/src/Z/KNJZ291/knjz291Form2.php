<?php

require_once('for_php7.php');

class knjz291Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db  = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //新規
        if ($model->cmd == "new") {
            unset($model->staffcd);
            unset($model->field);
        }

        $query = knjz291Query::getKyouikuIinkai();
        $model->kyouikuIinkai = $db->getOne($query);

        //警告メッセージを表示しない場合
        $model->iinkai = "";
        if (!isset($model->warning) && isset($model->staffcd) && ($model->cmd != "search")) {
            if ($model->cmd === 'change') {
                $Row =& $model->field;
            } else {
                $query = knjz291Query::getRow($model, $model->staffcd);
                $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $query = knjz291Query::getEdWork($model, $model->staffcd);
                $setEdWork = $db2->getRow($query, DB_FETCHMODE_ASSOC);
                if (is_array($Row) && is_array($setEdWork)) {
                    $Row = array_merge($Row, $setEdWork);
                }
            }
        } else if ($model->cmd == "search") {
            //職員マスタ存在チェック
            $query = knjz291Query::getRow($model, $model->field["STAFFCD"]);
            $check = $db->getRow($query);
            if (is_array($check)){
                $model->setWarning("この職員コードは既に登録されています。");
                $Row["STAFFCD"] = $model->field["STAFFCD"];
            } else {
                //教育委員会から表示
                $query = knjz291Query::getRow($model, $model->field["STAFFCD"]);
                $Row = $db2->getRow($query, DB_FETCHMODE_ASSOC);
                $query = knjz291Query::getEdWork($model, $model->staffcd);
                $setEdWork = $db2->getRow($query, DB_FETCHMODE_ASSOC);
                $Row = $Row + $setEdWork;
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
        }
        knjCreateHidden($objForm, "EDBOARD_TORIKOMI_FLG", $Row["EDBOARD_TORIKOMI_FLG"]);
        
        //職員コード桁数セット
        $setsize = "";
        //STAFFCDフィールドサイズ変更対応
        if ($model->Properties["useStaffcdFieldSize"] === '10') {
            $setsize = 10;
        } else {
            $setsize = 8;
        }

        //県職員コード(学校側のみ)
        if ($model->sendSubmit === '1') {
            $arg["EDBOARD_STAFFCD_HYOUJI"] = "1";
            $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
            if ($Row["EDBOARD_TORIKOMI_FLG"] === '1') {
                $extra .= " readonly style=\"background-color:lightgray;\"";
            }
            $arg["data"]["EDBOARD_STAFFCD"] = knjCreateTextBox($objForm, $Row["EDBOARD_STAFFCD"], "EDBOARD_STAFFCD", $setsize, $setsize, $extra);
            if ($model->cmd == "search_ken") {
                //教育委員会から表示
                $query = knjz291Query::getRow($model, $model->field["EDBOARD_STAFFCD"]);
                $RowKen = $db2->getRow($query, DB_FETCHMODE_ASSOC);
                if ($RowKen["STAFFCD"]) {
                    $Row["STAFFCD"] = $RowKen["STAFFCD"];
                    $Row["STAFFNAME"] = $RowKen["STAFFNAME"];
                    $Row["STAFFNAME_SHOW"] = $RowKen["STAFFNAME_SHOW"];
                    $Row["STAFFNAME_KANA"] = $RowKen["STAFFNAME_KANA"];
                    $Row["STAFFNAME_ENG"] = $RowKen["STAFFNAME_ENG"];
                    $Row["STAFFNAME_REAL"] = $RowKen["STAFFNAME_REAL"];
                    $Row["STAFFNAME_KANA_REAL"] = $RowKen["STAFFNAME_KANA_REAL"];
                    $Row["STAFFSEX"] = $RowKen["STAFFSEX"];
                    $Row["STAFFBIRTHDAY"] = str_replace("-","/",$RowKen["STAFFBIRTHDAY"]);
                    $Row["STAFFZIPCD"] = $RowKen["STAFFZIPCD"];
                    $Row["STAFFADDR1"] = $RowKen["STAFFADDR1"];
                    $Row["STAFFADDR2"] = $RowKen["STAFFADDR2"];
                    $Row["STAFFTELNO"] = $RowKen["STAFFTELNO"];
                    $Row["STAFFFAXNO"] = $RowKen["STAFFFAXNO"];
                    $Row["STAFFE_MAIL"] = $RowKen["STAFFE_MAIL"];
                } else {
                    $model->setWarning("MSG303", "　　（教育委員会）");
                    $Row =& $model->field;
                }
            }
        }

        //教育委員会用学校コード取得
        $query = knjz291Query::getEdboardSchoolcd();
        $model->edboard_schoolcd = $db->getOne($query);

        //職員区分取得
        $query = knjz291Query::getStaffDiv($model->edboard_schoolcd, $model->staffcd);
        $model->staff_div = $db2->getOne($query);

        if ($model->iinkai) {
            $arg["insert"] = 1;
            $readonly = " readonly style=\"background-color:lightgray;\"";
        } else if ($model->staffcd) {
            $arg["update"] = 1;
            $readonly = " readonly style=\"background-color:lightgray;\"";
        } else {
            $arg["insert"] = 1;
            $readonly = "";
        }

        //職員コード
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
        makeSchoolSide($objForm, $arg, $db, $Row, $model);

        //性別コンボボックス
        $query = knjz291Query::getSTAFFSEX();
        makeCmb($objForm, $arg, $db, $query, "STAFFSEX", $Row["STAFFSEX"], "", 1, "", "BLANK");

        //職員生年月日
        $Row["STAFFBIRTHDAY"] = str_replace("-","/",$Row["STAFFBIRTHDAY"]);
        $arg["data"]["STAFFBIRTHDAY"] = View::popUpCalendar($objForm, "STAFFBIRTHDAY" ,$Row["STAFFBIRTHDAY"]);

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

        //開始日付
        $Row["FROM_DATE"] = str_replace("-", "/", $Row["FROM_DATE"]);
        $arg["data"]["FROM_DATE"] = View::popUpCalendar($objForm, "FROM_DATE", $Row["FROM_DATE"], "", "");

        //学校コード
        $query = knjz291Query::getEdSchool($model, $model->kyouikuIinkai);
        $extra = "";
        makeCmb($objForm, $arg, $db2, $query, "FROM_SCHOOLCD", $Row["FROM_SCHOOLCD"], $extra, 1, "", "BLANK");

        //所属区分
        $query = knjz291Query::getNameMst("Z041", "SITEI");
        $extra = "";
        makeCmb($objForm, $arg, $db2, $query, "FROM_DIV", $Row["FROM_DIV"], $extra, 1, "", "BLANK");

        //課程
        $query = knjz291Query::getCouser();
        $extra = "";
        if ($model->kyouikuIinkai == 0) {
            makeCmb($objForm, $arg, $db, $query, "FROM_COURSECD", $Row["FROM_COURSECD"], $extra, 1, "", "BLANK");
        } else {
            makeCmb($objForm, $arg, $db2, $query, "FROM_COURSECD", $Row["FROM_COURSECD"], $extra, 1, "", "BLANK");
        }

        //開始日付
        $Row["TO_DATE"] = str_replace("-", "/", $Row["TO_DATE"]);
        $arg["data"]["TO_DATE"] = View::popUpCalendar($objForm, "TO_DATE", $Row["TO_DATE"], "", "");

        //学校コード
        $query = knjz291Query::getEdSchool($model, 1);
        $extra = "";
        makeCmb($objForm, $arg, $db2, $query, "TO_SCHOOLCD", $Row["TO_SCHOOLCD"], $extra, 1, "", "BLANK");

        //所属区分
        $query = knjz291Query::getNameMst("Z041", "SITEI");
        $extra = "";
        makeCmb($objForm, $arg, $db2, $query, "TO_DIV", $Row["TO_DIV"], $extra, 1, "", "BLANK");

        //課程
        $query = knjz291Query::getCouser();
        $extra = "";
        makeCmb($objForm, $arg, $db2, $query, "TO_COURSECD", $Row["TO_COURSECD"], $extra, 1, "", "BLANK");

        //資格教科登録
        makeShikaku($objForm, $arg, $db, $model);

        //届け登録
        makeTodoke($objForm, $arg, $db2, $model);

        //免許・資格登録
        makeMenkyo($objForm, $arg, $db2, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $Row);

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz291index.php", "", "edit");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz291index.php?cmd=list';";
        }
                                
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz291Form2.html", $arg);
    }
}

//学校側表示項目
function makeSchoolSide(&$objForm, &$arg, $db, $Row, $model) {

    //画面表示セット
    $arg["data"]["SET_COLSPAN"] = "3";
    $arg["data"]["SET_BGCOLOR"] = 'bgcolor="#00BFFF"';
    $arg["data"]["SET_BGCOLOR2"]   = 'bgcolor="#99eaff"';

    //担当保健室表示セット
    if ($model->Properties["useNurseoffRestrict"] == '1') {
        $arg["data"]["SET_COLWITH"] = 'width="20%"';
        $arg["useNurse"] = "1";
    } else {
        $arg["data"]["SET_COLWITH"] = 'colspan="4"';
    }

    //職名コンボボックス
    $query = knjz291Query::getJOBNAME();
    makeCmb($objForm, $arg, $db, $query, "JOBCD", $Row["JOBCD"], "", 1, "", "BLANK");

    //所属コンボボックス
    $query = knjz291Query::getSECTION();
    makeCmb($objForm, $arg, $db, $query, "SECTIONCD", $Row["SECTIONCD"], "", 1, "", "BLANK");

    //校務分掌部コンボボックス
    $query = knjz291Query::getDUTYSHARE();
    $extra = " onchange=\"chkSelSameValue()\" ";
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
    $query = knjz291Query::getPositionCd($model);
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD1", $Row["POSITIONCD1"], $extra, 1, "", "BLANK");
    //肩書き1の学年主任、教科主任
    if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
        setPositionExtCmb($objForm, $arg, $db, $model, $Row, 1, "005");
    } else {
        setPositionCmb($objForm, $arg, $db, $model, $Row, 1, "005");
    }

    //肩書き2
    $extra = "onchange=\"return btn_submit('change', 2);\"";
    $query = knjz291Query::getPositionCd($model);
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD2", $Row["POSITIONCD2"], $extra, 1, "", "BLANK");
    //肩書き2の学年主任、教科主任
    if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
        setPositionExtCmb($objForm, $arg, $db, $model, $Row, 2, "006");
    } else {
        setPositionCmb($objForm, $arg, $db, $model, $Row, 2, "006");
    }

    //肩書き3
    $extra = "onchange=\"return btn_submit('change', 3);\"";
    $query = knjz291Query::getPositionCd($model);
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD3", $Row["POSITIONCD3"], $extra, 1, "", "BLANK");
    //肩書き3の学年主任、教科主任
    if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
        setPositionExtCmb($objForm, $arg, $db, $model, $Row, 3, "007");
    } else {
        setPositionCmb($objForm, $arg, $db, $model, $Row, 3, "007");
    }

    //担当保健室コンボ
    $query = knjz291Query::getVNameMst("Z043", $model);
    $extra = "";
    makeCmb($objForm, $arg, $db, $query, "CHARGENURSEOFF", $Row["CHARGENURSEOFF"], $extra, 1, "", "BLANK");

    if ($model->sendSubmit != "") $arg["school"] = 1;
}

//肩書詳細
function setPositionCmb(&$objForm, &$arg, $db, $model, $Row, $positionNum, $staffSeq) {
    //肩書きの学年主任、教科主任
    if ($Row["POSITIONCD{$positionNum}"] === '0200' || $Row["POSITIONCD{$positionNum}"] === '1050') {
        $arg["MANAGER{$positionNum}_SET"] = "1";
        //学年主任　教科主任
        if ($Row["POSITIONCD{$positionNum}"] === '0200') {
            $query = knjz291Query::getGrade($model);
        } else {
            $query = knjz291Query::getClass($model);
        }
    } else {
        return true;
    }
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD{$positionNum}_MANAGER", $Row["POSITIONCD{$positionNum}_MANAGER"], "", 1, "", "BLANK");
}

//肩書詳細EXT
function setPositionExtCmb(&$objForm, &$arg, $db, $model, $Row, $positionNum, $staffSeq) {

    //肩書きの学年主任、教科主任
    $setClass = "";
    $setSize = "1";
    $positionNo = $Row["POSITIONCD{$positionNum}"];
    if ($positionNo === '0200' || $positionNo === '1050') {
        $arg["MANAGER{$positionNum}_SET"] = "1";
        //学年主任　教科主任
        if ($positionNo === '0200') {
            $query = knjz291Query::getGrade($model);
        } else {
            if ($model->cmd == "change") {
                $Row["POSITIONCD{$positionNum}_MANAGER"] = array();
                $Row["POSITIONCD{$positionNum}_MANAGER"] = explode(",", $model->field["POSITION{$positionNum}_EXT"]);
            } else {
                $query = knjz291Query::getStaffExt($model, $staffSeq);
                $result = $db->query($query);
                $Row["POSITIONCD{$positionNum}_MANAGER"] = array();
                while ($extRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $Row["POSITIONCD{$positionNum}_MANAGER"][] = $extRow["VALUE"];
                }
                $result->free();
            }

            $query = knjz291Query::getClass($model);
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
function makeRirekiSdate(&$objForm, &$arg, $db, $model) {

    //氏名有効開始日
    $arg["data"]["NAME_SDATE"] = View::popUpCalendar($objForm, "NAME_SDATE" ,$model->field["NAME_SDATE"]);

    //住所有効開始日
    $arg["data"]["ADDRESS_SDATE"] = View::popUpCalendar($objForm, "ADDRESS_SDATE" ,$model->field["ADDRESS_SDATE"]);

    if (!$model->staffcd && !$model->iinkai) $arg["rireki"] = 1;
}

//資格教科登録
function makeShikaku(&$objForm, &$arg, $db, $model) {

    $disable = ($model->staffcd && !$model->iinkai) ? "" : " disabled";

    //資格教科登録ボタン
    $extra = "onclick=\"return btn_submit('subform1');\"";
    $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "資格教科登録", $extra.$disable);

    //資格科教科表示
    $query = knjz291Query::getStaffClass($model);
    $arg["data"]["STF_SUBCLASS"] = implode(',',$db->getCol($query));

    if ($model->sendSubmit != "") $arg["shikaku"] = 1;
}

//届け登録
function makeTodoke(&$objForm, &$arg, $db2, $model) {

    //届け登録ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ291_STAFF_REQUESTFORM/knjz291_staff_requestformindex.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ291&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $extra .= ($model->staffcd && $model->staff_div != '2' && !$model->iinkai) ? "" : " disabled";
    $arg["button"]["btn_requestform"] = knjCreateBtn($objForm, "btn_requestform", "届け登録", $extra);

    //届け表示
    $query = knjz291Query::getStaffRequestform($model);
    $arg["data"]["STF_REQFORM"] = implode(',',$db2->getCol($query));
}

//免許・資格登録
function makeMenkyo(&$objForm, &$arg, $db2, $model) {

    //免許・資格登録ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ291_STAFF_QUALIFIED/knjz291_staff_qualifiedindex.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ291&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $extra .= ($model->staffcd && $model->staff_div != '2' && !$model->iinkai) ? "" : " disabled";
    $arg["button"]["btn_qualified"] = knjCreateBtn($objForm, "btn_qualified", "免許・資格登録", $extra);

    //免許・資格表示
    $data = array();
    $query = knjz291Query::getStaffQualified($model);
    $result = $db2->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $sep = "";
        if($row["QUALIFIED_CD"] != "" && $row["QUALIFIED_NAME"] != "") {
            $sep = "/";
        }

        $data[] = $row["QUALIFIED_CD"].$sep.$row["QUALIFIED_NAME"];
    }
    $arg["data"]["STF_QUALIFIED"] = implode(',',$data);

}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {

    $disable = ($model->auth == DEF_UPDATABLE) ? "" : " disabled";

    //学校側のみ
    if ($model->sendSubmit === '1') {
        //県職員情報取込ボタン
        $extra = "onclick=\"return btn_submit('search_ken');\"";
        $arg["button"]["btn_search_ken"] = knjCreateBtn($objForm, "btn_search_ken", "県職員情報取込", $extra);
    }
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
        $link = REQUESTROOT."/Z/KNJZ291A/knjz291aindex.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
        $name = "戻 る";
    } else {
        $extra = "onclick=\"return closeWin();\"";
        $name = "終 了";
    }
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", $name, $extra);

    //赴任履歴修正ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ291_STAFF_WORKHIST/knjz291_staff_workhistindex.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ291&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    if ($model->Properties["knjz291NotShowWorkHistButton"] != '1') {
	    $arg["button"]["btn_workhist"] = knjCreateBtn($objForm, "btn_workhist", "赴任履歴修正", $extra);
	}

    //氏名履歴登録ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ291_STAFF_NAME_HIST/knjz291_staff_name_histindex.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ291&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd."&SEND_STAFF_DIV=".$model->staff_div;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_namehist"] = ($model->staffcd && !$model->iinkai) ? knjCreateBtn($objForm, "btn_namehist", "※職員氏名", $extra) : "※職員氏名";

    //住所履歴登録ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ291_STAFF_ADDRESS/knjz291_staff_addressindex.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ291&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd."&SEND_STAFF_DIV=".$model->staff_div;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_address"] = ($model->staffcd && !$model->iinkai) ? knjCreateBtn($objForm, "btn_address", "住所1", $extra) : "住所1";

    //校長履歴登録ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ290S1/knjz290s1index.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ291&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $extra .= ($model->staffcd && !$model->iinkai) ? "" : " disabled";
    $arg["button"]["btn_prihist"] = knjCreateBtn($objForm, "btn_prihist", "校長履歴登録", $extra);
    if ($model->sendSubmit != "") $arg["koutyou"] = 1;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $multiple, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    if ($multiple != "1") {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size, $multiple);
}

//hidden作成
function makeHidden(&$objForm, $Row) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "POSITIONCD_NUM");
    knjCreateHidden($objForm, "POSITION1_EXT");
    knjCreateHidden($objForm, "POSITION2_EXT");
    knjCreateHidden($objForm, "POSITION3_EXT");
    knjCreateHidden($objForm, "HID_DUTYSHARECD", $Row["DUTYSHARECD"]);
    knjCreateHidden($objForm, "HID_DUTYSHARECD_2", $Row["DUTYSHARECD2"]);
}
?>
