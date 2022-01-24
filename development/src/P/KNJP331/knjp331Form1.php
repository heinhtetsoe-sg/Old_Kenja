<?php

require_once('for_php7.php');


class knjp331Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp331Form1", "POST", "knjp331index.php", "", "knjp331Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //今学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTER;

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //切替ラジオ（1:クラス, 2:個人）
        $opt_data = array(1, 2);
        $model->output = ($model->output == "") ? "1" : $model->output;
        $extra = array("onclick =\" return btn_submit('knjp331');\"",
                       "onclick =\" return btn_submit('knjp331');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->output, $extra, $opt_data, get_count($opt_data));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->output == 1) $arg["classno"] = $model->output;
        if ($model->output == 2) $arg["schno"]   = $model->output;

        if ($model->output != 1) {
            //クラスコンボ
            $query = knjp331Query::GetClass($model, "COMB");
            $extra = "onchange =\" return btn_submit('knjp331');\"";
            makeCombo($objForm, $arg, $db, $query, $model->hrclass, "HRCLASS", $extra, 1);
        }

        //リストToリスト
        if ($model->output == 1) $query = knjp331Query::GetClass($model, "LIST");
        if ($model->output == 2) $query = knjp331Query::GetSchreg($model);
        $extra = "multiple style=\"width:200px\" width:\"200px\" ondblclick=\"move1('left','up')\"";
        makeCombo($objForm, $arg, $db, $query, $dummy, "SELECT_NAME", $extra, 20);

        //出力対象クラスリスト
        $extra = "multiple style=\"width:200px\" width:\"200px\" ondblclick=\"move1('right','up')\"";
        $arg["data"]["SELECT_SELECTED"] = knjCreateCombo($objForm, "SELECT_SELECTED", "", array(), $extra, 20);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right','up');\"";
        $arg["button"]["select_rights"] = knjCreateBtn($objForm, "select_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left','up');\"";
        $arg["button"]["select_lefts"] = knjCreateBtn($objForm, "select_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right','up');\"";
        $arg["button"]["select_right1"] = knjCreateBtn($objForm, "select_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left','up');\"";
        $arg["button"]["select_left1"] = knjCreateBtn($objForm, "select_left1", "＜", $extra);

        //印刷対象日付1
        if ($model->field["DATE1"] == "") $model->field["DATE1"] = str_replace("-","/",CTRL_DATE);
        $arg["data"]["DATE1"] = View::popUpCalendar($objForm    ,"DATE1"    ,$model->field["DATE1"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $arg);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp331Form1.html", $arg); 
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    } else if ($blank == "ALL") {
        $opt[] = array ("label" => "全て",
                        "value" => "99");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    if ($name != "SELECT_NAME" && $name != "SELECT_SELECTED") {
        $value = ($value) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //CSV
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "ＣＳＶ出力", $extra);

    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, &$arg)
{
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJP331");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}
?>
