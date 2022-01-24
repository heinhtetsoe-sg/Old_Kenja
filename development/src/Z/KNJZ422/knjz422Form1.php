<?php

require_once('for_php7.php');

class knjz422form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz422index.php", "", "main");

        $db = Query::dbCheckOut();

        //アンケート件名取得
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjz422Query::getQuestionNairecd();
        makeCombo($objForm, $arg, $db, $query, $model->field["QUESTIONNAIRECD"], "QUESTIONNAIRECD", $extra, 1, "", $model);
        
        //設問数
        $model->koumoku = "10";

        //情報取得
        if (!isset($model->warning) && $model->field["QUESTIONNAIRECD"] != "") {
            if ($model->cmd != "change") {
                $query = knjz422Query::getQuestionFormatDat($model);
                $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row = $model->field;
            }
        } else {
            $Row = $model->field;
        }

        for ($i = 1; $i <= $model->koumoku; $i++ ) {
            //設問
            $extra = "";
            $arg["data"]["QUESTION_CONTENTS".$i] = KnjCreateTextArea($objForm, "QUESTION_CONTENTS".$i, 10, 61, "soft", $extra, $Row["QUESTION_CONTENTS".$i]);
        
            //回答入力方式
            $extra = "onchange=\"return btn_submit('change');\"";
            $query = knjz422Query::getPattern();
            makeCombo($objForm, $arg, $db, $query, $Row["ANSWER_PATTERN".$i], "ANSWER_PATTERN".$i, $extra, 1, "BLANK", $model);
            
            //回答の選択数
            if ($Row["ANSWER_PATTERN".$i] === '1' || $Row["ANSWER_PATTERN".$i] === '2') {
                $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
            } else {
                $extra = "disabled";
            }
            $arg["data"]["ANSWER_SELECT_COUNT".$i] = knjCreateTextBox($objForm, $Row["ANSWER_SELECT_COUNT".$i], "ANSWER_SELECT_COUNT".$i, 2, 2, $extra);
        }
        //プレビュー/印刷
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);
        
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        //終了ボタン
        $extra = "onclick=\"return closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJZ422");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        Query::dbCheckIn($db);        
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz422Form1.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $model) {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    if ($name != "QUESTIONNAIRECD") {
        $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        $arg["top"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    }
}

?>
