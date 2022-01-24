<?php

require_once('for_php7.php');

class knjj196Form1 {

    function main(&$model) {

        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //校種ラジオボタン
        $opt = array("J", "H");
        $radioArray = array();
        $model->field["SCHOOL_KIND"] = ($model->field["SCHOOL_KIND"] == "") ? SCHOOLKIND : $model->field["SCHOOL_KIND"];
        for ($i = 0; $i < get_count($opt); $i++) {
            $objForm->ae( array("type"      => "radio",
                                "name"      => "SCHOOL_KIND",
                                "value"     => $model->field["SCHOOL_KIND"],
                                "extrahtml" => "id=\"SCHOOL_KIND{$opt[$i]}\"",
                                "multiple"  => $opt));
            $radioArray["SCHOOL_KIND".$opt[$i]] = $objForm->ge("SCHOOL_KIND", $opt[$i]);
        }
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //CSV出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjj196index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj196Form1.html", $arg);
    }
}
?>
