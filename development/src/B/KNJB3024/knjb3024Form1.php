<?php

require_once('for_php7.php');

class knjb3024Form1
{
    function main(&$model)
    {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjb3024index.php", "", "main");

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $arg["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjb3024Query::getSemester($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //科目時間割SEQコンボ
        $query = knjb3024Query::getSchPtrnSubHdat($model);
        $opt = array();
        $opt[] = array("label" => "", "value" => "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dateTime = explode('.', $row["UPDATED"]);
            list($setDate, $setTime) = explode(' ', $dateTime[0]);
            $setWeek = $model->week[date('w', $row["UPDATED"])];
            $dispDate = str_replace("-", "/", $setDate)."({$setWeek["WEEK_JP"]}) {$setTime}";
            $row["SEQ"] = sprintf('%02d', $row["SEQ"]);

            $opt[] = array('label' => "{$row["SEQ"]} {$dispDate} {$row["TITLE"]}",
                           'value' => $row["SEQ"]);
        }
        $result->free();
        $model->field['SEQ'] = strlen($model->field['SEQ']) > 0 ? $model->field['SEQ'] : $opt[0]["value"];
        $extra = "id=\"SEQ\" onChange=\"btn_submit('main')\"";
        $arg['data']['SEQ'] = knjCreateCombo($objForm, 'SEQ', $model->field['SEQ'], $opt, $extra, $size);

        //基本時間割SEQコンボ
        $query = knjb3024Query::getSchPtrnHdat($model);
        $opt = array();
        $opt[] = array("label" => "(新規)", "value" => "0");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dateTime = explode('.', $row["UPDATED"]);
            list($setDate, $setTime) = explode(' ', $dateTime[0]);
            $setWeek = $model->week[date('w', $row["UPDATED"])];
            $dispDate = str_replace("-", "/", $setDate)."({$setWeek["WEEK_JP"]}) {$setTime}";
            $row["BSCSEQ"] = sprintf('%02d', $row["BSCSEQ"]);

            $opt[] = array('label' => "{$row["BSCSEQ"]} {$dispDate} {$row["TITLE"]}",
                           'value' => $row["BSCSEQ"]);
        }
        $result->free();
        $model->field['BSCSEQ'] = strlen($model->field['BSCSEQ']) > 0 ? $model->field['BSCSEQ'] : $opt[0]["value"];
        $extra = "id=\"BSCSEQ\" onChange=\"btn_submit('main')\"";
        $arg['data']['BSCSEQ'] = knjCreateCombo($objForm, 'BSCSEQ', $model->field['BSCSEQ'], $opt, $extra, $size);

        $query = knjb3024Query::getSchChrCnt($model);
        $schChrCnt = $db->getOne($query);
        $setMessage = "";
        $disabled = "";
        if ($schChrCnt > 0) {
            $setMessage = "※指定学期の時間割が作成済みです。";
            $disabled = " disabled ";
        }
        $arg['data']['NOT_UPD_MESSAGE'] = $setMessage;

        //実行ボタン
        $extra = "onclick=\"btn_submit('exec');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $disabled.$extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb3024Form1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array('label' => "全　て",
                       'value' => "");
    }
    if ($blank == "BLANK") {
        $opt[] = array('label' => "",
                       'value' => "");
    }
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

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "SEMESTER") ? CTRL_SEMESTER : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
