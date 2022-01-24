<?php

require_once('for_php7.php');

class knjm262wForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjm262wForm1", "POST", "knjm262windex.php", "", "knjm262wForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //開始日付初期値
        if ($model->field["SDATE"] == "") {
            $model->field["SDATE"] = CTRL_YEAR."/04/01"; //0401固定
        }
        //開始日（テキスト）
        $disabled = "";
        $extra = "onblur=\"tmp_list('knjm262w', 'on')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["SDATE"], "SDATE", 12, 12, $extra);
        //開始日（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('knjm262w', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=SDATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['SDATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //開始日付
        $arg["data"]["SDATE"] = View::setIframeJs().$date_textbox.$date_button;
        
        //終了日付初期値
        if ($model->field["EDATE"] == "") {
            $model->field["EDATE"] = str_replace("-", "/", CTRL_DATE);
        }
        //終了日（テキスト）
        $disabled = "";
        $extra = "onblur=\"tmp_list('knjm262w', 'on')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["EDATE"], "EDATE", 12, 12, $extra);
        //終了日（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('knjm262w', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=EDATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['EDATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //終了日付
        $arg["data"]["EDATE"] = View::setIframeJs().$date_textbox.$date_button;

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //CSVボタン
        $extra = "onclick=\"btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "csv", "CSV出力", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJM262W");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm262wForm1.html", $arg);
    }
}
