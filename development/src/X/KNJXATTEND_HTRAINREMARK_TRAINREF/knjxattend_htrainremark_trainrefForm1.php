<?php

require_once('for_php7.php');

class knjxattend_htrainremark_trainrefForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("detail", "POST", "index.php", "", "detail");

        //DB OPEN
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjxattend_htrainremark_trainrefQuery::getYear($model->schregno);
        $extra = "onchange=\"return btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1);

        //学籍番号
        $arg["data"]["SCHREGNO"] = $model->schregno;

        //氏名
        $query = knjxattend_htrainremark_trainrefQuery::getName($model->schregno);
        $schName = $db->getOne($query);
        $arg["data"]["NAME"] = $schName;

        // 項目名
        foreach ($model->itemname as $item => $name) {
            $arg[$item."NAME"] = $name;
        }

        $seqName = array();
        for ($i = 1; $i <= 6; $i++) {
            $trainSeq = sprintf("%03d", 100 + $i);
            $seqName[$trainSeq] = "TRAIN_REF".$i;
        }

        //備考
        $query = knjxattend_htrainremark_trainrefQuery::getRemark($model->schregno, $model->year);
        $result = $db->query($query);
        $row = array();
        while ($getRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row[$seqName[$getRow["TRAIN_SEQ"]]] = $getRow["REMARK"];
        }
        $height = $model->gyou * 13.5 + ($model->gyou -1 ) * 3 + 5;
        $extra = "style=\"height:{$height}px;\"";
        foreach ($model->itemname as $item => $name) {
            $arg[$item] = knjCreateTextArea($objForm, $item, $model->gyou, $model->moji * 2 + 1, "", $extra, $row[$item]);
        }

        //終了ボタンを作成する
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJXATTEND_HTRAINREMARK_TRAINREF");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "OUTPUT_HEIGHT", $model->output_height);
        knjCreateHidden($objForm, "OUTPUT_WIDTH", $model->output_width);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを$arg経由で渡す
        View::toHTML($model, "knjxattend_htrainremark_trainrefForm1.html", $arg);
    }
}
/********************************************** 以下関数 ******************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
