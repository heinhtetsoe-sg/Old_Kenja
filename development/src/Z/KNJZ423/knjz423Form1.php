<?php

require_once('for_php7.php');

class knjz423Form1 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz423index.php", "", "main");

        //radio(1:産業分類, 2:職業分類)
        $opt = array(1, 2);
        $model->field["TAISYOU"] = ($model->field["TAISYOU"] == "") ? "1" : $model->field["TAISYOU"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"TAISYOU{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "TAISYOU", $model->field["TAISYOU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力取込種別ラジオボタン(1:データ取込 2:エラー出力)
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", 1024000);

        //CSV取込みボタンを作成する
        $extra = " onclick=\"return btn_submit('execute');\"";
        $arg["button"]["btn_execute"] = knjCreateBtn($objForm, "btn_execute", " 実行 ", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz423Form1.html", $arg);
    }
}
?>
