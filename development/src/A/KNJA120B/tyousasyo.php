<?php

require_once('for_php7.php');

class tyousasyo {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("detail", "POST", "knja120bindex.php", "", "detail");

        //DB OPEN
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knja120bQuery::getYear_tyousasyo($model);
        $extra = "onchange=\"return btn_submit('tyousasyo');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1);

        //学籍番号
        $arg["data"]["SCHREGNO"] = $model->schregno;

        //氏名
        $query = knja120bQuery::getName($model->schregno);
        $schName = $db->getOne($query);
        $arg["data"]["NAME"] = $schName;

        //備考
        $query = knja120bQuery::getAttendrecRemark($model);
        $attendrec_remark = $db->getOne($query);
        $extra = "style=\"height:75px;width:200px;\"";
        $arg["data"]["ATTENDREC_REMARK"] = knjCreateTextArea($objForm, "ATTENDREC_REMARK", "", "", "", $extra, $attendrec_remark);

        //終了ボタンを作成する
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "knja120b");
        knjCreateHidden($objForm, "SCHREGNO", "$model->schregno");

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを$arg経由で渡す
        View::toHTML($model, "tyousasyo.html", $arg);
    }
}
/********************************************** 以下関数 ******************************************************/
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

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>