<?php

require_once('for_php7.php');

class knjtx009Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //ファイルからの取り込み
        $objForm->ae( array("type"          => "file",
                            "name"          => "csvfile",
                            "size"          => 1024000,
                            "extrahtml"     => "style=\"width:300px\"" ));

        $arg["data"]["csvfile"] = $objForm->ge("csvfile");

        //CSV取込みボタンを作成する
        $objForm->ae( array("type"          => "button",
                            "name"          => "btn_execute",
                            "value"         => " 実行 ",
                            "extrahtml"     => "$disabled onclick=\"return btn_submit('execute');\"" ));

        $arg["data"]["btn_execute"] = $objForm->ge("btn_execute");

        //終了ボタンを作成する
        $objForm->ae( array("type"          => "button",
                            "name"          => "btn_end",
                            "value"         => "終 了",
                            "extrahtml"     => "onclick=\"closeWin();\"" ) );

        $arg["data"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"          => "hidden",
                            "name"          => "cmd"
                            ) );

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjtx009index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjtx009Form1.html", $arg);
    }
}
?>
