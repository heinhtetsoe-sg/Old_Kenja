<?php

require_once('for_php7.php');

class knjs351Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjs351Form1", "POST", "knjs351index.php", "", "knjs351Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjs351Query::getSemester($model);
        $extra = " onChange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

        if ($model->Properties["useFi_Hrclass"] == "1" || $model->Properties["useSpecial_Support_Hrclass"] == "1") {
            $arg["HR_CLASS_TYPE_SELECT"] = 1;
            //クラス方式選択    1:法定クラス 2:複式クラス/実クラス
            $opt = array(1, 2);
            $model->field["HR_CLASS_TYPE"] = ($model->field["HR_CLASS_TYPE"] == "") ? "1" : $model->field["HR_CLASS_TYPE"];
            $extra = array("id=\"HR_CLASS_TYPE1\" onclick=\"return btn_submit('main');\"", "id=\"HR_CLASS_TYPE2\" onclick=\"return btn_submit('main');\"");
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

            if ($model->Properties["useFi_Hrclass"] == "1") {
                $arg["data"]["HR_CLASS_TYPE2_LABEL"] = "複式クラス";
            } else if ($model->Properties["useSpecial_Support_Hrclass"] == "1") {
                $arg["data"]["HR_CLASS_TYPE2_LABEL"] = "実クラス";

                //学年混合チェックボックス
                $extra  = $model->field["GAKUNEN_KONGOU"] == "1" ? "checked" : "";
                $extra .= " onclick=\"return btn_submit('main');\" id=\"GAKUNEN_KONGOU\"";
                $arg["data"]["GAKUNEN_KONGOU"] = knjCreateCheckBox($objForm, "GAKUNEN_KONGOU", "1", $extra, "");
            }
        }

        //年組コンボボックス
        $query = knjs351Query::getGradeHrClass($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", "", 1);

        //帳票選択  1:15行 2:45行
        $opt = array(1, 2);
        $model->field["FORM_SELECT"] = ($model->field["FORM_SELECT"]) ? $model->field["FORM_SELECT"] : 1;
        $extra = array("id=\"FORM_SELECT1\"", "id=\"FORM_SELECT2\"");
        $radioArray = knjCreateRadio($objForm, "FORM_SELECT", $model->field["FORM_SELECT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden
        makeHidden($objForm, $model, $db);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjs351Form1.html", $arg); 
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $db) {
    knjCreateHidden($objForm, "PRGID", PROGRAMID);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useFi_Hrclass", $model->Properties["useFi_Hrclass"]);
    knjCreateHidden($objForm, "useSpecial_Support_Hrclass", $model->Properties["useSpecial_Support_Hrclass"]);
    knjCreateHidden($objForm, "RESTRICT_FLG", (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) ? "1" : "0");
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $result->free();

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
