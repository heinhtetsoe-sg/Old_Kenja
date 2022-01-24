<?php

require_once('for_php7.php');

class knjl308hForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl308hForm1", "POST", "knjl308hindex.php", "", "knjl308hForm1");

        $db = Query::dbCheckOut();

        //年度テキスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR + 1;
        //生徒一覧リストを作成する/
        $name = "CATEGORY_NAME";
        $value = '';
        $options = array();
        $options[] = array("label" => "氏名",     "value" => "name");
        $options[] = array("label" => "氏名かな", "value" => "name_kana");
        $options[] = array("label" => "性別",     "value" => "sex");
        $options[] = array("label" => "生年月日", "value" => "birthday");
        $options[] = array("label" => "出身学校", "value" => "fs_cd");
        $extra = "multiple style=\"width:240px\" width:\"240px\" ondblclick=\"move1('left')\"";
        $size = "20";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, $name, $value, $options, $extra, $size);


        //対象者一覧リストを作成する
        $name = "category_selected";
        $value = '';
        $options = array();
        $extra = "multiple style=\"width:240px\" width:\"240px\" ondblclick=\"move1('right')\"";
        $size = "20";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, $name, $value, $options, $extra, $size);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, 'btn_rights', '>>', $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, 'btn_lefts', '<<', $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, 'btn_right1', '＞', $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, 'btn_left1', '＜', $extra);

        //印刷と終了ボタンの作成
        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm);

        Query::dbCheckIn($db);

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl308hForm1.html", $arg); 
    }
}





//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size)
{
    $opt = array();
    $flag = false;
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        $flag = ($row["VALUE"] == $value) ? true : $flag;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $flag) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, 'btn_print', 'プレビュー／印刷', $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);
}
//hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL308H");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR + 1);//年度データ
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CHECK_ITEM", "name,name_kana");
    knjCreateHidden($objForm, "DATE", str_replace("-","/",CTRL_DATE));
}

?>
