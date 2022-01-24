<?php

require_once('for_php7.php');

class knjd129jForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;
        
        $setAuth = AUTHORITY;

        $requestroot = REQUESTROOT;
        $arg["jump"] = "Page_jumper('{$requestroot}', '{$setAuth}');";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd129jForm1.html", $arg);
    }
}

?>
