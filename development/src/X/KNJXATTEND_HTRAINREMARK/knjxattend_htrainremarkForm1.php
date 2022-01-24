<?php

require_once('for_php7.php');
class knjxattend_htrainremarkForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("detail", "POST", "index.php", "", "detail");

        //DB OPEN
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjxattend_htrainremarkQuery::getYear($model->schregno, $model);
        $extra = "onchange=\"return btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1);

        //学籍番号
        $arg["data"]["SCHREGNO"] = $model->schregno;

        //氏名
        $query = knjxattend_htrainremarkQuery::getName($model->schregno);
        $schName = $db->getOne($query);
        $arg["data"]["NAME"] = $schName;

        //タイトル
        if ($model->output_field == "TOTALREMARK") {
            $arg["data"]["TITLE"] = "総合所見";
        } else {
            $arg["data"]["TITLE"] = $model->attendTitle."の記録備考";
        }

        //備考
        $query = knjxattend_htrainremarkQuery::getRemark($model->schregno, $model->year, $model->output_field);
        $remark = $db->getOne($query);
        $extra = "style=\"height:{$model->output_height}px;width:{$model->output_width}px;\"";
        $arg["data"]["REMARK"] = knjCreateTextArea($objForm, "REMARK", "", "", "", $extra, $remark);

        //終了ボタンを作成する
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJXATTEND_HTRAINREMARK");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "OUTPUT_FIELD", $model->output_field);
        knjCreateHidden($objForm, "OUTPUT_HEIGHT", $model->output_height);
        knjCreateHidden($objForm, "OUTPUT_WIDTH", $model->output_width);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを$arg経由で渡す
        View::toHTML($model, "knjxattend_htrainremarkForm1.html", $arg);
    }
}
/********************************************** 以下関数 ******************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $value_flg_year = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
        if (CTRL_YEAR == $row["VALUE"]) $value_flg_year = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else if ($name == "YEAR") {
        if (!($value && $value_flg)){
            $value = (CTRL_YEAR && $value_flg_year) ? CTRL_YEAR : $opt[0]["value"];
        }
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
