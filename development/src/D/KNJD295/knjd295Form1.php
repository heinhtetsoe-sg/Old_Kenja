<?php

require_once('for_php7.php');

class knjd295Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd295Form1", "POST", "knjd295index.php", "", "knjd295Form1");

        $db = Query::dbCheckOut();

        $arg["data"]["YEAR"] = $model->control["年度"];
        $arg["data"]["SEMESTERNAME"] = $model->control["学期名"][CTRL_SEMESTER];

        $query = knjd295Query::getGrade();
        $extra = " onchange=\"return submit('dtGrade')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["DTGRADE"], "DTGRADE", $extra, 1, "");

        //サイン欄出力
        $extra ="";
        if ($model->field["PRINT_SIGN"] == "1") {
            $extra .= "checked";
        }
        $extra .= " id=\"PRINT_SIGN\" ";
        $arg["data"]["PRINT_SIGN"] = knjCreateCheckBox($objForm, "PRINT_SIGN", "1", $extra, "");

        //検証欄出力
        $extra = "";
        if ($model->field["PRINT_VERIFICATION"] == "1") {
            $extra .= "checked";
        }
        $extra .= " id=\"PRINT_VERIFICATION\" ";
        $arg["data"]["PRINT_VERIFICATION"] = knjCreateCheckBox($objForm, "PRINT_VERIFICATION", "1", $extra, "");

        $query = knjd295Query::getHrClass($model);
        $result = $db->query($query);
        $opt_right = $opt_left = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
        $result->free();

        //対象
        $value = "";
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", $value, $opt_right, $extra, 20);

        //生徒一覧
        $value = "";
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", $value, $opt_left, $extra, 20);

        //移動ボタン
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "NEW_YEAR", CTRL_YEAR+1);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJD295");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "IMAGEPATH", $model->control["LargePhotoPath"]);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd295Form1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
