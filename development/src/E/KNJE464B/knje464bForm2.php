<?php

require_once('for_php7.php');

class knje464bForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knje464bindex.php", "", "edit");

        //各フィールド取得
        if ($model->field["IPT_GROUPNAME"] == "" && $model->field["IPT_FACILITY_GRP"] != "" && !isset($model->warning)) {
            if ($model->cmd === 'edit') {
                $Row = knje464bQuery::getGroupNameYmst($model, $model->field["IPT_FACILITY_GRP"]);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //前年度からコピーボタン
        $extra = "onclick=\"return btn_submit('copy2');\"";
        $arg["button"]["btn_copy2"] = knjCreateBtn($objForm, "btn_copy2", "前年度からコピー", $extra);

        //グループコード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["IPT_FACILITY_GRP"] = knjCreateTextBox($objForm, $Row["IPT_FACILITY_GRP"], "IPT_FACILITY_GRP", 4, 4, $extra);
        
        //名称
        $extra = "";
        $arg["IPT_GROUPNAME"] = knjCreateTextBox($objForm, $Row["IPT_GROUPNAME"], "IPT_GROUPNAME", 30, 15, $extra);
        
        if ($model->field["IPT_FACILITY_GRP"] != "") {
            if ($model->cmd != 'edit2' || $model->selectStudent == "") {
                $query = knje464bQuery::getSelSchregNo($model, "GetSchK");
                $chkSchK = $db->getOne($query);
                if ($chkSchK != "" && $chkSchK != $model->schoolKind) {
                    $model->schoolKind = $chkSchK;
                }
            }
        }
        //学部
        $query = knje464bQuery::getSchoolKind();
        $extra = "onchange=\"return btn_submit('edit2');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->schoolKind, $extra, 1);

        //年組
        //クラスコンボ作成
        $query = knje464bQuery::getHrClass(CTRL_YEAR, $model);
        $extra = "onchange=\"return btn_submit('edit2');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->hr_class, $extra, 1);
        //リストToリスト作成
        //if ($Row["IPT_FACILITY_GRP"] == $model->facilityGrp) {
            makeListToList($objForm, $arg, $db, $model, $Row["IPT_FACILITY_GRP"], "CATEGORY");
            makeListToListStudent($objForm, $arg, $db, $model, $Row["IPT_FACILITY_GRP"], "NERAI");
        //} else {
        //    makeListToList($objForm, $arg, $db, $model, "", "CATEGORY");
        //    makeListToListStudent($objForm, $arg, $db, $model, "", "NERAI");
        //}

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm);

        if (VARS::post("cmd") == "update" || VARS::post("cmd") == "delete") {
            $arg["jscript"] = "window.open('knje464bindex.php?cmd=list&shori=update','left_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje464bForm2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $flg="") {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    if ($flg == "BLANK") {
        $opt[] = array('label' => "",'value' => "");
    }
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $setgroupcd, $setName) {
    $opt_right = $opt_left = array();
    $selected = array();

    $selectdata = explode(',', $model->selectdata);
    //対象支援機関一覧取得
     $query = knje464bQuery::getSupportFacilities($setgroupcd, true);
     $result = $db->query($query);
     while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
         if ($model->cmd == 'edit2') {
             if (in_array($row["VALUE"], $selectdata)) {
                 $opt_left[] = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
                 $selected[] = $row["VALUE"];
             }
         } else {
             $opt_left[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
             $selected[] = $row["VALUE"];
         }
     }
     $result->free();

    //対象支援機関一覧作成
    $extra = "id=\"{$setName}_SELECTED\" multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right', '{$setName}_SELECTED', '{$setName}_NAME', 1);\"";
    $arg["data"]["{$setName}_SELECTED"] = knjCreateCombo($objForm, "{$setName}_SELECTED", "", $opt_left, $extra, 20);

    //支援機関一覧取得
    $query = knje464bQuery::getSupportFacilities($setgroupcd, false);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $selected)) continue;
        $opt_right[] = array('label' => $row["LABEL"],
                             'value' => $row["VALUE"]);
    }
    $result->free();

    //科目一覧作成
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left','{$setName}_SELECTED','{$setName}_NAME',1);\"";
    $arg["data"]["{$setName}_NAME"] = knjCreateCombo($objForm, "{$setName}_NAME", "", $opt_right, $extra, 20);

    //選択ボタン作成（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('sel_add_all','{$setName}_SELECTED','{$setName}_NAME',1);\"";
    $arg["button"]["{$setName}btn_lefts"] = knjCreateBtn($objForm, "{$setName}btn_lefts", "<<", $extra);
    //選択ボタン作成（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('left','{$setName}_SELECTED','{$setName}_NAME',1);\"";
    $arg["button"]["{$setName}btn_left1"] = knjCreateBtn($objForm, "{$setName}btn_left1", "＜", $extra);
    //取消ボタン作成（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('right','{$setName}_SELECTED','{$setName}_NAME',1);\"";
    $arg["button"]["{$setName}btn_right1"] = knjCreateBtn($objForm, "{$setName}btn_right1", "＞", $extra);
    //取消ボタン作成（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('sel_del_all','{$setName}_SELECTED','{$setName}_NAME',1);\"";
    $arg["button"]["{$setName}btn_rights"] = knjCreateBtn($objForm, "{$setName}btn_rights", ">>", $extra);
}

//リストToリスト作成
function makeListToListStudent(&$objForm, &$arg, $db, $model, $setgroupcd) {
    $opt_right = $opt_left = array();
    $selected = array();

    $opt_chk = array();
    $opt_chkdat = array();
    $query = knje464bQuery::getSelSchregNo($model, "GetNot");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_chk[] = $row["VALUE"];
        $opt_chkdat[] = $row;
    }
    $result->free();

    //生徒選択リストの処理では、選択生徒が居る状態なら、画面状態を維持する処理を行う。
    if ($model->field["IPT_FACILITY_GRP"] != "" && $model->hr_class != "") {
        if ($model->cmd == 'edit2') {
            $selectStudent = ($model->selectStudent != "") ? explode(",",$model->selectStudent) : array();
            $selectStudentLabel = ($model->selectStudentLabel != "") ? explode(",",$model->selectStudentLabel) : array();

            for ($i = 0; $i < get_count($selectStudent); $i++) {
                $opt_left[] = array('label' => $selectStudentLabel[$i],
                                    'value' => $selectStudent[$i]);
                $selected[] = $selectStudent[$i];
            }
        } else {
            //対象生徒一覧取得
            $query = knje464bQuery::getSelSchregNo($model, "DataGet");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_left[] = array('label' => "　".$row["LABEL"],
                                    'value' => $row["VALUE"]);
                $selected[] = $row["VALUE"];
            }
            $result->free();
        }
    }

    //対象生徒一覧作成
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right', 'SCHREG_SELECTED', 'SCHREG_NAME', 1);\"";
    $arg["data"]["SCHREG_SELECTED"] = knjCreateCombo($objForm, "SCHREG_SELECTED", "", $opt_left, $extra, 20);

    //生徒一覧取得
    $query = knje464bQuery::getSelSchregNo($model, "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $selected, true)) continue;
        if (in_array($row["VALUE"], $opt_chk, true)) {
            $mark = "■";
            for ($ii = 0;$ii < get_count($opt_chkdat);$ii++) {
                if ($opt_chkdat[$ii]["VALUE"] == $row["VALUE"]) {
                    $behindStr = "(グループ:".$opt_chkdat[$ii]["LABEL"].")";
                }
            }
        } else {
            $mark = "　";
            $behindStr = "";
        }
        $opt_right[] = array('label' => $mark.$row["LABEL"].$behindStr,
                             'value' => $row["VALUE"]);
    }
    $result->free();

    //科目一覧作成
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left','SCHREG_SELECTED','SCHREG_NAME',1);\"";
    $arg["data"]["SCHREG_NAME"] = knjCreateCombo($objForm, "SCHREG_NAME", "", $opt_right, $extra, 20);

    //選択ボタン作成（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('sel_add_all','SCHREG_SELECTED','SCHREG_NAME',1);\"";
    $arg["button"]["SCHREGbtn_lefts"] = knjCreateBtn($objForm, "SCHREGbtn_lefts", "<<", $extra);
    //選択ボタン作成（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('left','SCHREG_SELECTED','SCHREG_NAME',1);\"";
    $arg["button"]["SCHREGbtn_left1"] = knjCreateBtn($objForm, "SCHREGbtn_left1", "＜", $extra);
    //取消ボタン作成（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('right','SCHREG_SELECTED','SCHREG_NAME',1);\"";
    $arg["button"]["SCHREGbtn_right1"] = knjCreateBtn($objForm, "SCHREGbtn_right1", "＞", $extra);
    //取消ボタン作成（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('sel_del_all','SCHREG_SELECTED','SCHREG_NAME',1);\"";
    $arg["button"]["SCHREGbtn_rights"] = knjCreateBtn($objForm, "SCHREGbtn_rights", ">>", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //追加ボタン1
    $extra = (AUTHORITY < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return doSubmit('1');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //追加ボタン2
    $extra = (AUTHORITY < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return doSubmit('2');\"";
    $arg["button"]["btn_update2"] = knjCreateBtn($objForm, "btn_update2", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdata2");
    knjCreateHidden($objForm, "selectStudent");
    knjCreateHidden($objForm, "selectStudentLabel");
    
}
?>
