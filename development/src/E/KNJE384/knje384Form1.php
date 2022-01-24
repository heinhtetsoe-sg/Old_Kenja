<?php

require_once('for_php7.php');

class knje384Form1 {
    function main(&$model) {

        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //タイトルの表示
        $arg["data"]["TITLE"] = "TOEFL基準点登録";

        if (!isset($model->warning)) {
            $query = knje384Query::getToeflMst(CTRL_YEAR);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //基準点
        $extra = "";
        $arg["data"]["BASE_SCORE"] = knjCreateTextBox($objForm, $Row["BASE_SCORE"], "BASE_SCORE", 4, 4, $extra);

        //範囲(FROM)
        $extra = "";
        $arg["data"]["RANGE_F"] = knjCreateTextBox($objForm, $Row["RANGE_F"], "RANGE_F", 4, 4, $extra);
        //範囲(TO)
        $extra = "";
        $arg["data"]["RANGE_T"] = knjCreateTextBox($objForm, $Row["RANGE_T"], "RANGE_T", 4, 4, $extra);

        //ボタン作成
        //登録ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //前年度コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJE384");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knje384index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje384Form1.html", $arg);
    }
}
?>
