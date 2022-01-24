<?php

require_once('for_php7.php');

class knjd616hForm1 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd616hForm1", "POST", "knjd616hindex.php", "", "knjd616hForm1");
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        /******************/
        /* コンボボックス */
        /******************/
        //学期
        $query = knjd616hQuery::getSelectSeme();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1, "");

        //学年
        $query = knjd616hQuery::getSelectGrade($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "");

        /****************/
        /* ラジオボタン */
        /****************/
        //出力区分(1:成績上位者 2:成績下位者)
        $opt = array(1, 2);
        $model->field["DATA_DIV"] = ($model->field["DATA_DIV"] == "") ? "1" : $model->field["DATA_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"DATA_DIV{$val}\" onClick=\"useOption()\"");
        }
        $radioArray = knjCreateRadio($objForm, "DATA_DIV", $model->field["DATA_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /********************/
        /* テキストボックス */
        /********************/
        $disabled1 = ($model->field["DATA_DIV"] == "1") ? " disabled": "";
        $disabled2 = ($model->field["DATA_DIV"] == "2") ? " disabled": "";

        //順位
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"".$disabled2;
        $model->field["RANK"] = ($model->field["RANK"] != "") ? $model->field["RANK"]: "40";
        $arg["data"]["RANK"] = knjCreateTextBox($objForm, $model->field["RANK"], "RANK", 3, 3, $extra);

        //評定平均（上位）
        $extra = "style=\"text-align:right\" onblur=\"this.value=toFloat(this.value);\"".$disabled2;
        $model->field["HYOUTEI"] = ($model->field["HYOUTEI"] != "") ? $model->field["HYOUTEI"]: "4.25";
        $arg["data"]["HYOUTEI"] = knjCreateTextBox($objForm, $model->field["HYOUTEI"], "HYOUTEI", 4, 4, $extra);

        //評定２の個数（下位）
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"".$disabled1;
        $model->field["HYOUTEI_2"] = ($model->field["HYOUTEI_2"] != "") ? $model->field["HYOUTEI_2"]: "4";
        $arg["data"]["HYOUTEI_2"] = knjCreateTextBox($objForm, $model->field["HYOUTEI_2"], "HYOUTEI_2", 2, 2, $extra);

        /**********/
        /* ボタン */
        /**********/
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        //印刷
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "CTRL_YEAR",  CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJD616H");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd616hForm1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "SEMESTER") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
