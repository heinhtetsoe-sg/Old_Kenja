<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje010SubForm2 {
    function main(&$model) {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("form4", "POST", "knje010index.php", "", "form4");

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //DB接続
        $db = Query::dbCheckOut();

        $query = knje010Query::selectQuery_Htrainremark_Hdat($model);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        /******************/
        /* テキストエリア */
        /******************/
        //活動内容
        $arg["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", 5, 89, "hard", "onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"", $row["TOTALSTUDYACT"]);
        //評価
        $arg["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", 7, 89, "hard", "onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"", $row["TOTALSTUDYVAL"]);

        /**********/
        /* ボタン */
        /**********/
        //終了
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻る", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje010SubForm2.html", $arg);
    }
}
?>