<?php

require_once('for_php7.php');


class knjz235aForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjz235aForm1", "POST", "knjz235aindex.php", "", "knjz235aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        if($model->cmd == "grade") {
            unset($model->selectdata);
            unset($model->selectdataLabel);
        }

        //学期コンボ作成
        $query = knjz235aQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjz235a');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ作成
        $query = knjz235aQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('grade'),AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz235aForm1.html", $arg); 
    }
}

function makeStudentList(&$objForm, &$arg, $db, $model) {

    //出力対象一覧リストを作成する
    $opt_left = array();
    $selectdata = ($model->selectdata != "") ? explode(",",$model->selectdata) : array();
    $selectdataLabel = ($model->selectdataLabel != "") ? explode(",",$model->selectdataLabel) : array();
    if ($model->selectdata) {
        for ($i = 0; $i < get_count($selectdata); $i++) {
            $opt_left[] = array('label' => $selectdataLabel[$i],
                                'value' => $selectdata[$i]);
        }
    } else {
        $query = knjz235aQuery::getSelectTest($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $opt_left[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
        }
        $result->free();
    }
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right', 1)\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

    //対象考査リストを作成する
    $opt_right = array();
    $query = knjz235aQuery::getTestList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["VALUE"], $selectdata)) {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left', '')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', 1);\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', '');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', 1);\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', '');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg) {

    //前年度からコピーボタンを作成する
    $extra = "onclick=\"return btn_submit('copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);
    //更新ボタンを作成する
    $extra = "onclick=\"return doSubmit();\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdataLabel");
    knjCreateHidden($objForm, "cmd");
}

?>
