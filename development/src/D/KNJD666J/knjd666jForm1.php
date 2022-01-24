<?php

require_once('for_php7.php');

class knjd666jForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjd666jForm1", "POST", "knjd666jindex.php", "", "knjd666jForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $extra = "onChange=\"return btn_submit('knjd666j');\"";
        $query = knjd666jQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ
        $extra = "onChange=\"return btn_submit('knjd666j');\"";
        $query = knjd666jQuery::getGrade();
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);
        
        //クラス別、コース別
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学校校種を取得
        $model->schoolkind = $db->getOne(knjd666jQuery::getSchoolKind($model));

        //テスト種別コンボ
        $extra = "onChange=\"return btn_submit('knjd666j');\"";
        $query = knjd666jQuery::getTestitem($model);
        makeCmb($objForm, $arg, $db, $query, "TESTKIND_ITEMCD", $model->field["TESTKIND_ITEMCD"], $extra, 1);

        //順位
        $opt = array(1, 2);
        $model->field["RANKING"] = ($model->field["RANKING"] == "") ? "1" : $model->field["RANKING"];
        $extra = array("id=\"RANKING1\"", "id=\"RANKING2\"");
        $radioArray = knjCreateRadio($objForm, "RANKING", $model->field["RANKING"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $db);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd666jForm1.html", $arg); 
    }
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
    if ($name != "SEMESTER") {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    } else {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    }

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
function makeHidden(&$objForm, $model, $db) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJD666J");
}
?>
