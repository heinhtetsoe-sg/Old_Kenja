<?php

require_once('for_php7.php');


class knjb3052Form1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjb3052Form1", "POST", "knjb3052index.php", "", "knjb3052Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度　学期コンボ作成
        $query = knjb3052Query::getYearSemester("");
        $extra = "onchange=\"return btn_submit('changeSeme'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR_SEMESTER", $model->field["YEAR_SEMESTER"], $extra, 1);

        //学期の日付範囲取得
        $getDate = array();
        $query = knjb3052Query::getYearSemester($model);
        $getDate = $db->getRow($query, DB_FETCHMODE_ASSOC);
        knjCreateHidden($objForm, "SDATE", str_replace("-", "/", $getDate["SDATE"]));
        knjCreateHidden($objForm, "EDATE", str_replace("-", "/", $getDate["EDATE"]));
        $model->field["SDATE"] = $getDate["SDATE"];
        $model->field["EDATE"] = $getDate["EDATE"];

        //対象日付作成
        $model->field["EXECUTEDATE"] = $model->field["EXECUTEDATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["EXECUTEDATE"];
        if ($model->field["YEAR_SEMESTER"] == CTRL_YEAR."-".CTRL_SEMESTER && $model->cmd != "" && $model->cmd != "changeDate") {
            $model->field["EXECUTEDATE"] = str_replace("-", "/", CTRL_DATE);
        } else if ($model->cmd == "changeSeme") {
            $model->field["EXECUTEDATE"] = str_replace("-", "/", $getDate["SDATE"]);
        }
        if (!$model->checkCtrlDay($getDate["SDATE"], $getDate["EDATE"], $model->field["EXECUTEDATE"])) {
            $model->field["EXECUTEDATE"] = str_replace("-", "/", $getDate["SDATE"]);
        }
        $arg["data"]["EXECUTEDATE"] = View::popUpCalendar2($objForm, "EXECUTEDATE", $model->field["EXECUTEDATE"], "reload=true", "btn_submit('changeDate')","");

        //リストToリスト作成
        makeChairCdList($objForm, $arg, $db, $model);

        //出力順ラジオボタン 1:年組番号 2:男女 2:学籍番号
        $opt_group = array(1, 2, 3);
        $model->field["ORDER_DIV"] = ($model->field["ORDER_DIV"] == "") ? "1" : $model->field["ORDER_DIV"];
        $extra = array("id=\"ORDER_DIV1\"", "id=\"ORDER_DIV2\"", "id=\"ORDER_DIV3\"");
        $radioArray = knjCreateRadio($objForm, "ORDER_DIV", $model->field["ORDER_DIV"], $extra, $opt_group, get_count($opt_group));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb3052Form1.html", $arg);
    }
}

function makeChairCdList(&$objForm, &$arg, $db, $model) {

    //対象講座を作成する
    $query = knjb3052Query::getChairCdQuery($model, "");
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[] = array('label' => $row["VALUE"].":".$row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt1, $extra, 20);

    //生徒一覧リストを作成する//
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", array(), $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
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

    if ($name == "YEAR_SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR.'-'.CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg) {
    //実行ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "実 行", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectData");
}

?>
