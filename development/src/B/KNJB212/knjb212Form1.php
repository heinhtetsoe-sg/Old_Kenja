<?php

require_once('for_php7.php');

class knjb212Form1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb212Form1", "POST", "knjb212index.php", "", "knjb212Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $opt = array();
        $opt[] = array('label' => CTRL_YEAR, 'value' => CTRL_YEAR);
        $opt[] = array('label' => CTRL_YEAR+1, 'value' => CTRL_YEAR+1);
        $model->field["YEAR"] = $model->field["YEAR"] ? $model->field["YEAR"] : CTRL_YEAR;
        $extra = "";
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt, $extra, 1);

        //CSVボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "PRGID", "KNJB212");
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb212Form1.html", $arg);
    }
}
