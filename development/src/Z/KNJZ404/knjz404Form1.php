<?php

require_once('for_php7.php');

class knjz404Form1 {

    function main(&$model) {

        //権限チェック
        $arg["jscript"] = "";
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("sel", "POST", "knjz404index.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        /***********/
        /*  年 度  */
        /***********/
        //年度取得
        $opt = array();
        $no_year = 0;
        $query = knjz404Query::getYearQuery();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            if ($row["VALUE"] == $model->year)  $no_year = 1;
        }
        $model->year = ($model->year && $no_year) ? $model->year : CTRL_YEAR;
        //年度コンボ
        $extra = "onchange=\"return btn_submit('');\"";
        $arg["year"] = knjCreateCombo($objForm, "year", $model->year, $opt, $extra, 1);
        //年度追加テキスト
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $arg["year_add"] = knjCreateTextBox($objForm, "", "year_add", 5, 4, $extra);
        //年度追加ボタン
        $extra = "onclick=\"return add('');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra);

        //学年
        $extra = "onchange=\"return btn_submit('');\"";
        $query = knjz404Query::getGrade($model);
        makeCombo($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1, "BLANK", $model);

        //クラス形態
        $opt = array(1, 2);
        $model->field["RECORD_DIV"] = ($model->field["RECORD_DIV"] == "") ? "1" : $model->field["RECORD_DIV"];
        $extra = array("id=\"RECORD_DIV1\" onclick=\"return btn_submit('');\"", "id=\"RECORD_DIV2\" onclick=\"return btn_submit('');\"");
        $radioArray = knjCreateRadio($objForm, "RECORD_DIV", $model->field["RECORD_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //特殊科目フラグ
        $extra  = "id=\"SENMON_FLG\"";
        if ($model->field["SENMON_FLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $extra .= "onclick=\"return btn_submit('');\"";
        $arg["data"]["SENMON_FLG"] = knjCreateCheckBox($objForm, "SENMON_FLG", "1", $extra);

        //文言評価フラグ
        $extra  = "id=\"TEXT_HYOKA_FLG\"";
        if ($model->field["TEXT_HYOKA_FLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $extra .= "onclick=\"return btn_submit('');\"";
        $arg["data"]["TEXT_HYOKA_FLG"] = knjCreateCheckBox($objForm, "TEXT_HYOKA_FLG", "1", $extra);
        
        /********************/
        /*  リストToリスト  */
        /********************/
        //設定済み科目一覧取得
        $opt_left = array();
        $query = knjz404Query::getSubclassGradeQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
        }
        $result->free();
        //科目一覧リスト
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

        //科目一覧取得
        $opt_right = array();
        $query = knjz404Query::getSubclassQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
        $result->free();
        //科目一覧リスト
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

        //追加ボタン（全部）
        $extra = " onclick=\"moves('left');\"";
        $arg["button"]["sel_add_all"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
        //追加ボタン（一部）
        $extra = " onclick=\"move1('left');\"";
        $arg["button"]["sel_add"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
        //削除ボタン（一部）
        $extra = " onclick=\"move1('right');\"";
        $arg["button"]["sel_del"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
        //削除ボタン（全部）
        $extra = " onclick=\"moves('right');\"";
        $arg["button"]["sel_del_all"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz404Form1.html", $arg); 
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

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //更新ボタン
    $extra = "onclick=\"return doSubmit();\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
