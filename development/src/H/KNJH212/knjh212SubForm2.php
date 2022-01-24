<?php

require_once('for_php7.php');


class knjh212SubForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2", "POST", "knjh212index.php", "", "subform2");

        //クラスコンボボックスを作成する
        $query = knjh212Query::getHrClass($model);
        $extra = "onChange=\"return btn_submit('subform2');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, "1");

        //部活動コンボボックスを作成する
        if ($model->Properties["useClubCombo"] == "1") {
            $arg['useClubCombo'] = '1';
            $query = knjh212Query::getClub($model);
            $extra = "onChange=\"return btn_submit('subform2');\"";
            makeCmb($objForm, $arg, $db, $query, "CLUB", $model->field["CLUB"], $extra, "1", "BLANK");
        }

        //生徒一覧リスト
        makeStudentList($objForm, $arg, $db, $model);

        //退寮日
        $model->field["SUB_EDATE"] = ($model->field["SUB_EDATE"]) ? $model->field["SUB_EDATE"] : str_replace("-","/",CTRL_DATE);
        $arg["data"]["SUB_EDATE"] = View::popUpCalendar($objForm, "SUB_EDATE", $model->field["SUB_EDATE"]);

        //印刷ボタンを作成する
        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::post("cmd") == "sub_update") {
            $arg["reload"]  = "window.open('knjh212index.php?cmd=list&RELOADDOMI=".$selectdomi."','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh212SubForm2.html", $arg);
    }
}

//生徒一覧リスト作成
function makeStudentList(&$objForm, &$arg, $db, &$model) {
    $row1 = $row2 = array();
    //生徒一覧リストを作成する
    $query = knjh212Query::getDomiStudent($model, "1");
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    $extra = "multiple style=\"width:200px\" width:\"200px\" ondblclick=\"move1('left')\"";
    $arg["data"]["STUDENT_NAME"] = knjCreateCombo($objForm, "STUDENT_NAME", "", $row1, $extra, 20);

    //対象者一覧リストを作成する
    $query = knjh212Query::getDomiStudent($model, "2");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row2[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    $extra = "multiple style=\"width:200px\" width:\"200px\" ondblclick=\"move1('right')\"";
    $arg["data"]["STUDENT_SELECTED"] = knjCreateCombo($objForm, "STUDENT_SELECTED", "", $row2, $extra, 20);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //追加ボタン
    $auth = common::SecurityCheck(STAFFCD, PROGRAMID);
    $extra = ($auth == DEF_UPDATABLE || $auth == DEF_UPDATE_RESTRICT) ? "onclick=\"return doSubmit();\"" : "disabled";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('edit');\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '戻 る', $extra);
}

//hidden作成
function makeHidden(&$objForm, $model){
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DOMI_CODEALL", $model->field["DOMI_CODEALL"]);
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "CHK_SDATE", CTRL_YEAR.'/04/01');
    knjCreateHidden($objForm, "CHK_EDATE", (CTRL_YEAR+1).'/03/31');
}
?>
