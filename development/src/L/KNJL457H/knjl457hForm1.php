<?php

require_once('for_php7.php');

class knjl457hForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->examYear;

        //入試制度コンボ
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl457hQuery::getNameMst($model, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //回数コンボ
        $extra = "";
        $query = knjl457hQuery::getSettingMst($model, "L004");
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1, "ALL", $model->cmd);

        //奨学区分コンボ
        $optSyogakudiv = array();
        foreach ($model->syogakudivList as $key => $val) {
            if ($model->field["APPLICANTDIV"] == "1" && $key == "2") {
                continue;
            }
            $optSyogakudiv[] = array(
                "label" => $key.":".$val,
                "value" => $key
            );
        }
        $extra = "";
        $arg["data"]["SYOGAKUDIV"] = knjCreateCombo($objForm, "SYOGAKUDIV", $model->field["SYOGAKUDIV"], $optSyogakudiv, $extra, 1);

        //決定発行通知日
        if ($model->field["APPLICANTDIV"] == "1") {
            $arg["showNoticeDate"] = 1;
            $noticeDate = strlen($model->field["NOTICE_DATE"]) ? str_replace("-", "/", $model->field["NOTICE_DATE"]) : "";
            $model->field["NOTICE_DATE"] = $noticeDate;
            $arg["data"]["NOTICE_DATE"] = View::popUpCalendar2($objForm, "NOTICE_DATE", $noticeDate);
        }

        //実行
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "CSV出力", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl457hindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl457hForm1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $cmd = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "-- 全て --", "value" => "ALL");
    }
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = ($cmd == "" && $blank == "ALL") ? 1 : 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
