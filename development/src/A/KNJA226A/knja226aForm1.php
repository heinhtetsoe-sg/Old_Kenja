<?php

require_once('for_php7.php');

class knja226aForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knja226aForm1", "POST", "knja226aindex.php", "", "knja226aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        //校種
        $query = knja226aQuery::getSchoolKind($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);

        //学年
        $query = knja226aQuery::getGrade($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //ふりがなを出力する
        $extra  = " id='PRINT_FURIGANA' ";
        $extra .= ($model->field["PRINT_FURIGANA"] == "1" || $model->cmd == "") ? " checked " : "";
        $arg["data"]["PRINT_FURIGANA"] = knjCreateCheckBox($objForm, "PRINT_FURIGANA", 1, $extra);

        //学年毎に改ページする
        $extra  = " id='KAIPAGE_GRADE' ";
        $extra .= ($model->field["KAIPAGE_GRADE"] == "1" || $model->cmd == "") ? " checked " : "";
        $arg["data"]["KAIPAGE_GRADE"] = knjCreateCheckBox($objForm, "KAIPAGE_GRADE", 1, $extra);

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden
        makeHidden($objForm, $model, $db);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja226aForm1.html", $arg); 
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

//Hidden作成
function makeHidden(&$objForm, $model, $db) {
    knjCreateHidden($objForm, "PRGID", "KNJA226A");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "useFamilyDat", $model->Properties["useFamilyDat"]);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
