<?php

require_once('for_php7.php');

class knjxattend_htrainremark_detailForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("detail", "POST", "index.php", "", "detail");

        //DB OPEN
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjxattend_htrainremark_detailQuery::getYear($model->schregno);
        $extra = "onchange=\"return btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1);

        //学籍番号
        $arg["data"]["SCHREGNO"] = $model->schregno;

        //氏名
        $query = knjxattend_htrainremark_detailQuery::getName($model->schregno);
        $schName = $db->getOne($query);
        $arg["data"]["NAME"] = $schName;

        //備考
        $query = knjxattend_htrainremark_detailQuery::getRemark($model->schregno, $model->year, $model->output_field);
        $train_ref = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $extra = "style=\"height:{$model->output_height}px;width:{$model->output_width}px;\"";
        $arg["data"]["TRAIN_REF1"] = knjCreateTextArea($objForm, "TRAIN_REF1", "7", "20", "", $extra, $train_ref["TRAIN_REF1"]);
        $arg["data"]["TRAIN_REF2"] = knjCreateTextArea($objForm, "TRAIN_REF2", "7", "20", "", $extra, $train_ref["TRAIN_REF2"]);
        $arg["data"]["TRAIN_REF3"] = knjCreateTextArea($objForm, "TRAIN_REF3", "7", "20", "", $extra, $train_ref["TRAIN_REF3"]);

        //終了ボタンを作成する
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJXATTEND_HTRAINREMARK_DETAIL");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "OUTPUT_FIELD", $model->output_field);
        knjCreateHidden($objForm, "OUTPUT_HEIGHT", $model->output_height);
        knjCreateHidden($objForm, "OUTPUT_WIDTH", $model->output_width);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを$arg経由で渡す
        View::toHTML($model, "knjxattend_htrainremark_detailForm1.html", $arg);
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