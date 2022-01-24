<?php

require_once('for_php7.php');


class knjp807Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp807Form1", "POST", "knjp807index.php", "", "knjp807Form1");

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コードをhiddenで送る
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //DB接続
        $db = Query::dbCheckOut();

        //校種コンボ
        $arg["schkind"] = "1";
        $query = knjp807Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCombo($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "", $model);

        //クラス一覧リスト作成する
        $query = knjp807Query::getHrClass($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();

        $extra = "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", $value, isset($row1) ? $row1 : array(), $extra, 15);

        //出力対象クラスリストを作成する
        $extra = "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", $value, array(), $extra, 15);

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

        //出力入金項目
        $query = knjp807Query::getCollectM($model);
        $extra = "";
        makeCombo($objForm, $arg, $db, $query, $model->field["COLLECT_M_CD"], "COLLECT_M_CD", $extra, 1, "BLANK", $model);

        //入金票
        $extra = " id=\"NYUUKIN\" ";
        $model->field["NYUUKIN"] = $model->cmd == "" ? "1" : $model->field["NYUUKIN"];
        $checked = $model->field["NYUUKIN"] == "1" ? " checked " : "";
        $arg["NYUUKIN"] = knjCreateCheckBox($objForm, "NYUUKIN", "1", $checked.$extra);

        //生徒入金日期From
        $arg["PAID_FDAY"] = View::popUpCalendar($objForm, "PAID_FDAY", str_replace("-", "/", $model->field["PAID_FDAY"]));

        //生徒入金日期To
        $model->field["PAID_TDAY"] = str_replace("-", "/", CTRL_DATE);
        $arg["PAID_TDAY"] = View::popUpCalendar($objForm, "PAID_TDAY", str_replace("-", "/", $model->field["PAID_TDAY"]));

        //入金伝票日付
        $model->field["PRINT_DAY"] = str_replace("-", "/", CTRL_DATE);
        $arg["PRINT_DAY"] = View::popUpCalendar($objForm, "PRINT_DAY", str_replace("-", "/", $model->field["PRINT_DAY"]));

        //生徒別内訳
        $extra = " id=\"UCHIWAKE\" ";
        $model->field["UCHIWAKE"] = $model->cmd == "" ? "1" : $model->field["UCHIWAKE"];
        $checked = $model->field["UCHIWAKE"] == "1" ? " checked " : "";
        $arg["UCHIWAKE"] = knjCreateCheckBox($objForm, "UCHIWAKE", "1", $checked.$extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJP807");
        knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp807Form1.html", $arg); 
    }
}
//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $model) {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
