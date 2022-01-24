<?php

require_once('for_php7.php');

class knjxattend_entremarkForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("detail", "POST", "index.php", "", "detail");

        //DB OPEN
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjxattend_entremarkQuery::getYear($model->schregno);
        // Edit by PP for PC-Talker(voice) 2020-01-20 start
        $extra = "id=\"YEAR\" onchange=\"current_cursor('YEAR'); return btn_submit('');\" aria-label='年度'";
        // Edit by PP for PC-Talker(voice) 2020-01-31 end
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1);

        //学籍番号
        $arg["data"]["SCHREGNO"] = $model->schregno;

        //氏名
        $query = knjxattend_entremarkQuery::getName($model->schregno);
        $schName = $db->getOne($query);
        $arg["data"]["NAME"] = $schName;

        //備考
        $query = knjxattend_entremarkQuery::getRemark($model->schregno, $model->year);
        $remark = $db->getOne($query);
        $remark = preg_replace("/\r\n/","",$remark);
        $remark = preg_replace("/\n/","",$remark);
        $remark = preg_replace("/\r/","",$remark);
        // Edit by PP for PC-Talker(voice) 2020-01-20 start
        $extra = "style=\"height:75px;width:200px;\" aria-label='出欠の記録'";
        // Edit by PP for PC-Talker(voice) 2020-01-31 end
        $arg["data"]["REMARK"] = knjCreateTextArea($objForm, "REMARK", "", "", "", $extra, $remark);

        //終了ボタンを作成する
        // Edit by PP for PC-Talker(voice) and current focus 2020-01-20 start
        $extra = "onclick=\"return parent.closeit()\" aria-label='戻る'";
        // Edit by PP for PC-Talker(voice) and current focus 2020-01-31 end
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJXATTEND_HTRAINREMARK");
        knjCreateHidden($objForm, "cmd", "");
        knjCreateHidden($objForm, "SCHREGNO", "$model->schregno");

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを$arg経由で渡す
        View::toHTML($model, "knjxattend_entremarkForm1.html", $arg);
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
