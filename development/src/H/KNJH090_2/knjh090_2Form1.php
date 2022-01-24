<?php

require_once('for_php7.php');

class knjh090_2Form1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjh090_2Form1", "POST", "knjh090_2index.php", "", "knjh090_2Form1");

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //クラスコンボボックスを作成する
        $query = knjh090_2Query::getHrClass($model);
        $extra = "onChange=\"return btn_submit('knjh090_2');\"";
        $model->field["GRADE_HR_CLASS"] = ($model->field["GRADE_HR_CLASS"]) ? $model->field["GRADE_HR_CLASS"] : $model->grade.$model->hr_class;
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, "1");

        //生徒一覧リスト
        makeStudentList($objForm, $arg, $db, $model);

        //登録日付
        $date_ymd = ($model->field["DETAIL_SDATE"]) ? $model->field["DETAIL_SDATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DETAIL_SDATE"] = View::popUpCalendar($objForm, "DETAIL_SDATE", $date_ymd);

        //詳細区分 (賞データのみ)
        $opt_detaildiv = array();
        $opt_detaildiv[] = array("label" => "賞データ","value" => "1");
        $extra = "";
        $arg["data"]["DETAIL_DIV"] = knjCreateCombo($objForm, "DETAIL_DIV", $model->field["DETAIL_DIV"], $opt_detaildiv, $extra, 1);

        //詳細種類 (賞データのみ)
        $query = knjh090_2Query::getNameMst("H303");
        makeCmb($objForm, $arg, $db, $query, "DETAILCD", $model->field["DETAILCD"], "", "1");

        //詳細内容
        $extra = "";
        $arg["data"]["CONTENT"] = knjCreateTextArea($objForm, "CONTENT", 6, 100, "soft", $extra, $model->field["CONTENT"]);

        //備考
        $extra = "";
        $arg["data"]["REMARK"] = knjCreateTextArea($objForm, "REMARK", 2, 50, "soft", $extra, $model->field["REMARK"]);

        //ボタンを作成する
        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh090_2Form1.html", $arg);
    }
}

//生徒一覧リスト作成
function makeStudentList(&$objForm, &$arg, $db, &$model)
{
    $row1 = $row2 = array();
    //一覧リストを作成する
    $query = knjh090_2Query::getStudent($model, "1");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["STUDENT_NAME"] = knjCreateCombo($objForm, "STUDENT_NAME", "", $row1, $extra, 20);

    //出力対象リストを作成する
    $query = knjh090_2Query::getStudent($model, "2");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row2[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
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
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;

    if ($name != "GRADE_HR_CLASS") {
        $opt[] = array('label' => "", 'value' => "");
    }

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = (($value != "") && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //追加ボタン
    $extra = (AUTHORITY == DEF_UPDATABLE) ? "onclick=\"return doSubmit();\"" : "disabled";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "更 新", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '戻 る', $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
}
