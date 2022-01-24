<?php

require_once('for_php7.php');

class knjd234Form1
{
    function main(&$model){

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd234Form1", "POST", "knjd234index.php", "", "knjd234Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //処理学期
        $arg["data"]["NOW_SEM"] = $model->control["学期名"][CTRL_SEMESTER];

        //学年コンボボックスを作成する
        $query = knjd234Query::getSelectGrade($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", "", 1);

        //学期コンボボックスを作成する
        $query = knjd234Query::getSelectSeme();
        $extra = "onchange=\"return btn_submit('knjd234');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["COMBO_SEM"], "COMBO_SEM", $extra, 1);

        //成績優良者・評定平均
        $value = ($model->field["ASSESS1"]) ? $model->field["ASSESS1"] : "4.3";
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toFloat(this.value)\"";
        $arg["data"]["ASSESS1"] = knjCreateTextBox($objForm, $value, "ASSESS1", 5, 5, $extra);

        //成績優良者・詳細リスト
        $extra  = ($model->field["ASSESS1_DETAIL"] == "1") ? "checked " : "";
        $extra .= " id=\"ASSESS1_DETAIL\"";
        $arg["data"]["ASSESS1_DETAIL"] = knjCreateCheckBox($objForm, "ASSESS1_DETAIL", "1", $extra);

        //成績不振者・評定
        $value = ($model->field["VALUE"]) ? $model->field["VALUE"] : "1";
        $extra = " STYLE=\"text-align: right\"; onBlur=\"return toInteger(this.value);\"";
        $arg["data"]["VALUE"] = knjCreateTextBox($objForm, $value, "VALUE", 3, 3, $extra);

        //成績不振者・科目数
        $value = ($model->field["COUNT"]) ? $model->field["COUNT"] : "1";
        $extra = " STYLE=\"text-align: right\"; onBlur=\"return toInteger(this.value);\"";
        $arg["data"]["COUNT"] = knjCreateTextBox($objForm, $value, "COUNT", 3, 2, $extra);

        //成績不振者・未履修
        $value = ($model->field["UNSTUDY"]) ? $model->field["UNSTUDY"] : "1";
        $extra = " STYLE=\"text-align: right\"; onBlur=\"return toInteger(this.value);\"";
        $arg["data"]["UNSTUDY"] = knjCreateTextBox($objForm, $value, "UNSTUDY", 3, 2, $extra);

        //成績不振者・評定平均
        $value = ($model->field["ASSESS2"]) ? $model->field["ASSESS2"] : "2.2";
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toFloat(this.value)\"";
        $arg["data"]["ASSESS2"] = knjCreateTextBox($objForm, $value, "ASSESS2", 5, 5, $extra);

        //成績不振者・詳細リスト
        $extra  = ($model->field["ASSESS2_DETAIL"] == "1") ? "checked " : "";
        $extra .= " id=\"ASSESS2_DETAIL\"";
        $arg["data"]["ASSESS2_DETAIL"] = knjCreateCheckBox($objForm, "ASSESS2_DETAIL", "1", $extra);

        //カレンダーコントロール
        $value = isset($model->field["DATE"]) ? $model->field["DATE"] : $model->control["学籍処理日"];
        $arg["el"]["DATE"] = View::popUpCalendar($objForm, "DATE", $value);

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd234Form1.html", $arg); 
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //終了ボタン
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $db, $model)
{
    knjCreateHidden($objForm, "PRGID", "KNJD234");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "NOW_SEM", CTRL_SEMESTER);
    knjCreateHidden($objForm, "SDATE", $model->control["学期開始日付"][$model->field["COMBO_SEM"]]);
    knjCreateHidden($objForm, "EDATE", $model->control["学期終了日付"][$model->field["COMBO_SEM"]]);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useCurriculumcd",   $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();

    if ($query) {
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }

        if ($name == "COMBO_SEM") {
            $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
        } else {
            $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        }

        $result->free();
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
