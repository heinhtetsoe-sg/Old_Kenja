<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjxSearch9.php 56591 2017-10-22 13:04:39Z maeshiro $
class knjxSearch9
{
    function main(&$model){

        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjxSearch9", "POST", "index.php", "", "knjxSearch9");

        //会社名
        $objForm->ae( array("type"        => "text",
                            "size"        => 52,
                            "maxlength"   => 80,
                            "extrahtml"  => "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"",
                            "name"        => "COMPANY_NAME"));
        $arg["data"]["COMPANY_NAME"] = $objForm->ge("COMPANY_NAME");
    
        //就業場所
        $objForm->ae( array("type"        => "text",
                            "size"        => 52,
                            "maxlength"   => 80,
                            "extrahtml"  => "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"",
                            "name"        => "SHUSHOKU_ADDR"));
        $arg["data"]["SHUSHOKU_ADDR"] = $objForm->ge("SHUSHOKU_ADDR");
        
        //実行ボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "BTN_OK",
                            "value"       => "実行",
                            "extrahtml"   => "onclick=\"return search_submit();\"" ));
        
        $arg["button"]["BTN_OK"] = $objForm->ge("BTN_OK");
        
        //閉じるボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "BTN_END",
                            "value"       => "閉じる",
                            "extrahtml"   => "onclick=\"closeWin(); window.opener.close()\"" ));
        
        $arg["button"]["BTN_END"] = $objForm->ge("BTN_END");
        
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
        
        $arg["finish"]  = $objForm->get_finish();
        
        $js = "var w;\n";
        $js .= "w = setInterval('window.focus()', 50);\n";
        $js .= "setInterval('observeDisp()', 5000);\n";
        $arg["JAVASCRIPT"] = $js;
        View::toHTML($model, "knjxSearch9.html", $arg);
    }
}
?>