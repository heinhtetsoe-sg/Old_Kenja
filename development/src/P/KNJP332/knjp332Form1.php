<?php

require_once('for_php7.php');


class knjp332Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp332Form1", "POST", "knjp332index.php", "", "knjp332Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //今学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTER;

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //切替ラジオ（1:都道府県, 2:クラス, 3:個人）
        $opt_data = array(1, 2, 3);
        $model->output = ($model->output == "") ? "1" : $model->output;
        $extra = array("id=\"OUTPUT1\" onclick =\" return btn_submit('knjp332');\"", "id=\"OUTPUT2\" onclick =\" return btn_submit('knjp332');\"", "id=\"OUTPUT3\" onclick =\" return btn_submit('knjp332');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->output, $extra, $opt_data, get_count($opt_data));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->output == 1) $arg["todouno"] = $model->output;
        if ($model->output == 2) $arg["classno"] = $model->output;
        if ($model->output == 3) $arg["schno"]   = $model->output;

        if ($model->output != 1) {
            //都道府県コンボ
            $query = knjp332Query::GetPrefecturescd("COMB");
            $extra = "onchange =\" return btn_submit('knjp332ken');\"";
            makeCombo($objForm, $arg, $db, $query, $model->todoufuken, "TODOUFUKEN", $extra, 1, "ALL");

            if ($model->output == 3){
                if ($model->cmd == "knjp332ken"){
                    $model->hrclass = "";
                    $model->cmd = "knjp332";
                }
                //クラスコンボ
                $query = knjp332Query::GetClass($model, "COMB");
                $extra = "onchange =\" return btn_submit('knjp332');\"";
                makeCombo($objForm, $arg, $db, $query, $model->hrclass, "HRCLASS", $extra, 1);
            }
        }

        //リストToリスト
        if ($model->output == 1) $query = knjp332Query::GetPrefecturescd("LIST");
        if ($model->output == 2) $query = knjp332Query::GetClass($model, "LIST");
        if ($model->output == 3) $query = knjp332Query::GetSchreg($model);
        $extra = "multiple style=\"width:200px\" ondblclick=\"move1('left','up')\"";
        makeCombo($objForm, $arg, $db, $query, $dummy, "SELECT_NAME", $extra, 20);

        //出力対象クラスリスト
        $extra = "multiple style=\"width:200px\" ondblclick=\"move1('right','up')\"";
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
        
        //印刷対象日付2
        if ($model->field["DATE2"] == "") $model->field["DATE2"] = str_replace("-","/",CTRL_DATE);
        $arg["data"]["DATE2"] = View::popUpCalendar($objForm    ,"DATE2"    ,$model->field["DATE2"]);

        $model->field["YOUSIKI"] = ($model->field["YOUSIKI"] == "") ? "2" : $model->field["YOUSIKI"];
        knjCreateHidden($objForm, "YOUSIKI", $model->field["YOUSIKI"]);

        //異動日付
        $model->grdDate = $model->grdDate ? $model->grdDate : CTRL_DATE;
        $arg["data"]["GRD_DATE"] = View::popUpCalendar($objForm, "GRD_DATE", str_replace("-", "/", $model->grdDate),"");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $arg);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp332Form1.html", $arg); 
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
    knjCreateHidden($objForm, "PRGID", "KNJP332");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}
?>
