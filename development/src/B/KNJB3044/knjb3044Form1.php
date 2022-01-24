<?php

require_once('for_php7.php');

class knjb3044Form1
{
    function main(&$model)
    {

        $objForm = new form;

        // フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjb3044index.php", "", "main");

        // 権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        // DB接続
        $db = Query::dbCheckOut();

        $arg["YEAR"] = CTRL_YEAR;

        // 年度学期コンボ
        $query = knjb3044Query::getYearSemester($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR_SEME", $model->field["YEAR_SEME"], $extra, 1);

        // 科目展開表SEQコンボ
        $query = knjb3044Query::getSchPtrnPreHdat($model);
        $opt = array();
        $opt[] = array("label" => "", "value" => "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dateTime = explode('.', $row["UPDATED"]);
            list($setDate, $setTime) = explode(' ', $dateTime[0]);
            $setWeek = $model->week[date('w', $row["UPDATED"])];
            $dispDate = str_replace("-", "/", $setDate)."({$setWeek["WEEK_JP"]}) {$setTime}";
            $row["PRESEQ"] = sprintf('%02d', $row["PRESEQ"]);

            $opt[] = array('label' => "{$row["PRESEQ"]} {$dispDate} {$row["TITLE"]}",
                           'value' => $row["PRESEQ"]);
        }
        $result->free();
        $model->field['PRESEQ'] = strlen($model->field['PRESEQ']) > 0 ? $model->field['PRESEQ'] : $opt[0]["value"];
        $extra = "id=\"PRESEQ\" onChange=\"btn_submit('main')\"";
        $arg['data']['PRESEQ'] = knjCreateCombo($objForm, 'PRESEQ', $model->field['PRESEQ'], $opt, $extra, $size);

        // 時間割の存在チェック
        $query = knjb3044Query::getSchChrCnt($model);
        $schChairCnt = $db->getOne($query);
        $setMessage = "";
        $disabled = "";
        if ($schChairCnt > 0) {
            $setMessage = "※指定年度学期の時間割は作成済みです。";
            $disabled = " disabled ";
        }
        $arg['data']['NOT_UPD_MESSAGE'] = $setMessage;

        //実行ボタン
        $extra = "onclick=\"btn_submit('exec');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $disabled.$extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);


        // 講座の存在チェック
        $query = knjb3044Query::getChairDatCnt($model);
        $chairCnt = $db->getOne($query);
        $setMessage = "";
        if ($chairCnt > 0) {
            $model->chairCntFlg = "1";
        }

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb3044Form1.html", $arg);
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
        $value = $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

// hidden項目作成
function makeHidden(&$objForm, $model) {

    //hiddenを作成する
    knjCreateHidden($objForm, "cmd");

    // 講座存在チェックフラグ
    knjCreateHidden($objForm, "CHAIR_CNT_FLG", $model->chairCntFlg);

}

?>
