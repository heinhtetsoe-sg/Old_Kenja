<?php

require_once('for_php7.php');

class knjf074Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjf074Form1", "POST", "knjf074index.php", "", "knjf074Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $extra = "";
        $query = knjf074Query::getSelectYear($model);
        $result = $db->query($query);
        $chkDefaultYear = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[]= array('label' => $row["LABEL"]."年度",
                           'value' => $row["VALUE"]);
            if ($row["VALUE"] == CTRL_YEAR) $chkDefaultYear = true;
        }
        if ($model->field["YEAR"] == "" && get_count($opt) > 0) {
            if ($chkDefaultYear) {
                $model->field["YEAR"] = CTRL_YEAR;
            } else {
                $model->field["YEAR"] = $opt[0];
            }
        }

        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt, $extra, 1);

        //プレビュー／印刷ボタン
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");

        //csvボタン
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", "onclick=\"return btn_submit('csv');\"");
        
        //終了ボタン
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //hidden作成
        knjCreateHidden($objForm, "PRGID", "KNJF074");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "useSpecial_Support_School", $model->Properties["useSpecial_Support_School"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf074Form1.html", $arg);
    }
}
?>
