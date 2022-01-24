<?php

require_once('for_php7.php');

class knjj144hForm1 {
    function main(&$model) {
        $objForm = new form;
        //タイトル
        $arg["data"]["YEAR"] = CTRL_YEAR."年度　ＣＳＶ出力／取込";
        //DB接続
        $db = Query::dbCheckOut();

        /********************/
        /* チェックボックス */
        /********************/
        //ヘッダ有
        if($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header);

        /****************/
        /* ラジオボタン */
        /****************/
        //出力取込種別 1:データ取込 2:エラー出力
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /********/
        /* FILE */
        /********/
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

        /**********/
        /* コンボ */
        /**********/
        //処理名
        $opt_shori   = array();
        $opt_shori[] = array("label" => "新規","value" => "1");
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, "style=\"width:60px;\"", 1);

        /**********/
        /* ボタン */
        /**********/
        //実行
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", "onclick=\"return btn_submit('exec');\"");
        //終了
        $arg["btn_end"]  = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjj144hindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj144hForm1.html", $arg);
    }
}
?>
