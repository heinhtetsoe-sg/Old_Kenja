<?php

require_once('for_php7.php');


class knjm503Form1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm503Form1", "POST", "knjm503index.php", "", "knjm503Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $opt = array();
        $query = knjm503Query::getSemeMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();

        $model->field["SEMESTER"] = ($model->field["SEMESTER"]) ? $model->field["SEMESTER"] : CTRL_SEMESTER;
        $extra = "onChange=\"return btn_submit('changeSem');\"";
        $arg["data"]["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $model->field["SEMESTER"], $opt, $extra, 1);

        //クラス選択コンボボックスを作成する
        $query = knjm503Query::getAuth(CTRL_YEAR, $model->field["SEMESTER"]);
        $extra = "onchange=\"return btn_submit('clschange'),AllClearList();\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1);

        //リストを作成する
        $opt1 = array();
        $opt_left = array();
        $query = knjm503Query::getSchreg($model);

        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt1[]= array('label' =>  $row["NAME"],
                           'value' => $row["SCHREGNO"]);
        }
        $result->free();

        //生徒一覧リストを作成する
        $extra = "multiple style=\"width:230px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt1, $extra, 20);
        //対象者リストを作成する
        $extra = "multiple style=\"width:230px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

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

        //住所印刷チェックボックス
        $extra = ($model->field["ADDR_PRINT"] == "1" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"ADDR_PRINT\"";
        $arg["data"]["ADDR_PRINT"] = knjCreateCheckBox($objForm, "ADDR_PRINT", "1", $extra, "");

        //送り先ラジオボタン 1:保護者 2:負担者 3:その他
        $opt_addrDiv = array(1, 2, 3);
        $model->field["ADDR_DIV"] = ($model->field["ADDR_DIV"] == "") ? "1" : $model->field["ADDR_DIV"];
        $extra = array("id=\"ADDR_DIV1\"", "id=\"ADDR_DIV2\"", "id=\"ADDR_DIV3\"");
        $radioArray = knjCreateRadio($objForm, "ADDR_DIV", $model->field["ADDR_DIV"], $extra, $opt_addrDiv, get_count($opt_addrDiv));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //日付デフォルト値
        $query = knjm503Query::getSemeSdate($model);
        $setDefSdate = $db->getOne($query);
        if ($model->cmd == "changeSem") {
            $model->field["SKIJUN"] = $setDefSdate;
            $model->field["TKIJUN"] = $setDefSdate;
        }

        //出校日数集計基準日付データ
        if ($model->field["SKIJUN"] == "") $model->field["SKIJUN"] = str_replace("-", "/", $setDefSdate);
        $arg["data"]["SKIJUN"] = View::popUpCalendar($objForm, "SKIJUN", str_replace("-", "/", $model->field["SKIJUN"]));

        //特別活動集計基準日付データ
        if ($model->field["TKIJUN"] == "") $model->field["TKIJUN"] = str_replace("-", "/" , $setDefSdate);
        $arg["data"]["TKIJUN"] = View::popUpCalendar($objForm, "TKIJUN", str_replace("-", "/", $model->field["TKIJUN"]));

        //ボタンを作成する
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $arg, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm503Form1.html", $arg); 
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    $dataFlg = false;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        $dataFlg = $value == $row["VALUE"] ? true : $dataFlg;
    }
    $result->free();

    $value = ($value) && $dataFlg ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

function makeHidden(&$objForm, &$arg, $model) {
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJM503");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
}

?>
