<?php

require_once('for_php7.php');

    $objForm = new form;
    $arg = array();
    //�ե��������
    $arg["start"]   = $objForm->get_start("left", "POST", "index.php", "", "left");


    //���̾
    $objForm->ae( array("type"        => "text",
                        "size"        => 52,
                        "maxlength"   => 80,
                        "extrahtml"  => "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"",
                        "name"        => "COMPANY_NAME"));
    $arg["data"]["COMPANY_NAME"] = $objForm->ge("COMPANY_NAME");

    //���Ⱦ��
    $objForm->ae( array("type"        => "text",
                        "size"        => 52,
                        "maxlength"   => 80,
                        "extrahtml"  => "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"",
                        "name"        => "SHUSHOKU_ADD"));
    $arg["data"]["SHUSHOKU_ADD"] = $objForm->ge("SHUSHOKU_ADD");


    //�¹ԥܥ���
    $objForm->ae( array("type" 		  => "button",
                        "name"        => "BTN_OK",
                        "value"       => "�¹�",
                        "extrahtml"   => "onclick=\"return search_submit();\"" ));

    $arg["button"]["BTN_OK"] = $objForm->ge("BTN_OK");

    //�Ĥ���ܥ���
    $objForm->ae( array("type" 		  => "button",
                        "name"        => "BTN_END",
                        "value"       => "�Ĥ���",
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
    View::toHTML($model, "search.html", $arg);
?>