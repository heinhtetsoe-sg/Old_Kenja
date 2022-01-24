<?php

require_once('for_php7.php');

class knjc039Form1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjc039Form1", "POST", "knjc039index.php", "", "knjc039Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学年コンボ
        $query = knjc039Query::getGrade();
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], "", 1);

        //対象日作成
        $query = knjc039Query::getCheckDate();
        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        $model->field["SDATE"] = $model->field["SDATE"] == "" ? str_replace("-", "/", $row["SDATE"]) : $model->field["SDATE"];
        $arg["data"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", $model->field["SDATE"]);

        $model->field["EDATE"] = $model->field["EDATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["EDATE"];
        $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $model->field["EDATE"]);

        //出欠状況出力範囲(1:個人別 2:講座別)
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //公欠
        $query = knjc039Query::getNameMst("1");
        $arg["data"]["NAME1"] = $db->getOne($query);
        $extra = "checked id=\"DI_CD1\"";
        $arg["data"]["DI_CD1"] = knjCreateCheckBox($objForm, "DI_CD1", 'on', $extra);

        //出停
        $query = knjc039Query::getNameMst("2");
        $arg["data"]["NAME2"] = $db->getOne($query);
        $extra = "checked id=\"DI_CD2\"";
        $arg["data"]["DI_CD2"] = knjCreateCheckBox($objForm, "DI_CD2", 'on', $extra);

        //忌引
        $query = knjc039Query::getNameMst("3");
        $arg["data"]["NAME3"] = $db->getOne($query);
        $extra = "checked id=\"DI_CD3\"";
        $arg["data"]["DI_CD3"] = knjCreateCheckBox($objForm, "DI_CD3", 'on', $extra);

        //保健室欠課
        $query = knjc039Query::getNameMst("14");
        $arg["data"]["NAME14"] = $db->getOne($query);
        $extra = "checked id=\"DI_CD14\"";
        $arg["data"]["DI_CD14"] = knjCreateCheckBox($objForm, "DI_CD14", 'on', $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $db);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc039Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"].'学年',
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $opt[] = array('label' => '全て',
                   'value' => 99);

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
function makeHidden(&$objForm, $model, $db) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJC039");

    //日付チェック用
    $query = knjc039Query::getCheckDate();
    $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
    knjCreateHidden($objForm, "CHK_LDATE", str_replace("-", "/", CTRL_DATE));
    knjCreateHidden($objForm, "CHK_SDATE", $row["SDATE"]);
    knjCreateHidden($objForm, "CHK_EDATE", $row["EDATE"]);
}
?>
