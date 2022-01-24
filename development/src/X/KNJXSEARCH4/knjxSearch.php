<?php

require_once('for_php7.php');

class knjxSearch {
    function main(&$model) {

        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjxSearch", "POST", "knjxsearch4index.php", "", "knjxSearch");

        //DB接続
        $db = Query::dbCheckOut();

        //卒業年度コンボ
        $result = $db->query(knjxsearch4Query::GetYear());
        $opt[] = array("label" => "　　　　", "value" => "");

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["GRADUATE_YEAR"], "value" => $row["GRADUATE_YEAR"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "GRADUATE_YEAR",
                            "size"        => 1,
                            "extrahtml"   => "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\" onchange=\"return btn_submit('search_view2');\"",
                            "value"       => $model->search_fields["graduate_year"],
                            "options"     => $opt));

        $arg["data"]["GRADUATE_YEAR"] = $objForm->ge("GRADUATE_YEAR");

        //卒業時組コンボボックス
        $query = knjxsearch4Query::getHrclass($model, $model->search_fields["graduate_year"]);
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"";
        makeCmb($objForm, $arg, $db, $query, "GRADUATE_CLASS", $model->search_fields["graduate_class"], $extra, 1);

        //漢字姓名
        $objForm->ae( array("type"        => "text",
                            "size"        => 20,
                            "maxlength"   => 10,
                            "extrahtml"  => "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"",
                            "name"        => "LKANJI"));
        $arg["data"]["LKANJI"] = $objForm->ge("LKANJI");

        //かな姓名
        $objForm->ae( array("type"        => "text",
                            "size"        => 20,
                            "maxlength"   => 20,
                            "extrahtml"  => "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"",
                            "name"        => "LKANA"));
        $arg["data"]["LKANA"] = $objForm->ge("LKANA");

        //実行ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "BTN_OK",
                            "value"       => "実行",
                            "extrahtml"   => "onclick=\"return search_submit();\"" ));

        $arg["button"]["BTN_OK"] = $objForm->ge("BTN_OK");

        //閉じるボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "BTN_END",
                            "value"       => "閉じる",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        $arg["button"]["BTN_END"] = $objForm->ge("BTN_END");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        $js = "var w;\n";
        $js .= "w = setInterval('window.focus()', 50);\n";
        $js .= "setInterval('observeDisp()', 5000);\n";
        $arg["JAVASCRIPT"] = $js;
        View::toHTML($model, "knjxSearch.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $opt[] = array("label" => "   ", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
