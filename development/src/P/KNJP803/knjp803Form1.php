<?php

require_once('for_php7.php');

class knjp803Form1 {
    function main(&$model) {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp803Form1", "POST", "knjp803index.php", "", "knjp803Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //入金月
        $monthlist = array();
        $monthlist[] = array("value"=>"04", "label"=>"4月");
        $monthlist[] = array("value"=>"05", "label"=>"5月");
        $monthlist[] = array("value"=>"06", "label"=>"6月");
        $monthlist[] = array("value"=>"07", "label"=>"7月");
        $monthlist[] = array("value"=>"08", "label"=>"8月");
        $monthlist[] = array("value"=>"09", "label"=>"9月");
        $monthlist[] = array("value"=>"10", "label"=>"10月");
        $monthlist[] = array("value"=>"11", "label"=>"11月");
        $monthlist[] = array("value"=>"12", "label"=>"12月");
        $monthlist[] = array("value"=>"01", "label"=>"1月");
        $monthlist[] = array("value"=>"02", "label"=>"2月");
        $monthlist[] = array("value"=>"03", "label"=>"3月");
        $arg["INCOMEMONTH"] = knjCreateCombo($objForm, "INCOMEMONTH", $model->field["INCOMEMONTH"], $monthlist, "", 1);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        // フォーム選択
        $opt = array(1, 2);
        if (!$model->field["FORM"]) $model->field["FORM"] = "2";
        $extra = array("id=\"FORM1\" " , "id=\"FORM2\" ");
        $radioArray = knjCreateRadio($objForm, "FORM", $model->field["FORM"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /**********/
        /* ボタン */
        /**********/
        //実行ボタン
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
        //閉じるボタン
        $arg["button"]["btn_end"]   = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJP803");
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp803Form1.html", $arg); 
    }
}
/*****************************************************************************************************************/
/***************************************** 以下関数 **************************************************************/
/*****************************************************************************************************************/
//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧
    $rightList = array();
    $query = knjp803Query::getClassName($model);

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:200px\" width=\"200px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $rightList, $extra, 20);

    //出力対象生徒作成
    $extra = "multiple style=\"width:200px\" width=\"200px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}
?>
