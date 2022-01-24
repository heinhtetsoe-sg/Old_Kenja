<?php

require_once('for_php7.php');

class knjd624mForm1 {
    function main(&$model){
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjd624mForm1", "POST", "knjd624mindex.php", "", "knjd624mForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボボックスを作成する
        $query = knjd624mQuery::getSemester();
        $model->field["SEMESTER"] = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : CTRL_SEMESTER;
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], "onChange=\"return btn_submit('knjd624m');\"", 1);

        //学年コンボ
        $query = knjd624mQuery::getSelectGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], "", 1);

        //テスト種別コンボ
        $query  = knjd624mQuery::getName($model->field["SEMESTER"]);
        makeCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], "", 1, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd624mForm1.html", $arg);
    }
}
/************************************************ 以下関数 ************************************************/
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

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJD624M");
    knjCreateHidden($objForm, "useCurriculumcd",   $model->Properties["useCurriculumcd"]);
}
?>
