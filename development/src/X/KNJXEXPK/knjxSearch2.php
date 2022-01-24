<?php

require_once('for_php7.php');

class knjxSearch2
{
    function main(&$model){
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("search", "POST", "index.php", "right_frame");

        //年度と学期
        $arg["CTRL_YEAR"] = CTRL_YEAR;
        $arg["CTRL_SEMESTER"] = CTRL_SEMESTERNAME;

        $db = Query::dbCheckOut();
        //入学区分、異動区分、卒業区分のコンボ
        $opt['A002'] = array();
        $opt['A003'] = array();
        $opt['A004'] = array();
        $opt['A002'][] = array("label"  => '',
                        "value" => '');
        $opt['A003'][] = array("label"  => '',
                        "value" => '');
        $opt['A004'][] = array("label"  => '',
                        "value" => '');
                
        $result = $db->query(knjxexpkQuery::selectSearchDiv($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[$row["NAMECD1"]][] = array("label"     => $row["DIV"] ."：" .htmlspecialchars($row["NAME1"]),
                                            "value"     => $row["DIV"]);

        }
        //入学区分
        $objForm->ae( array("type"        => "select",
                            "name"        => "DIV1",
                            "size"        => "1",
                            "options"     => $opt['A002'] ));

        $arg["DIV1"] = $objForm->ge("DIV1");
        //異動区分
        $objForm->ae( array("type"        => "select",
                            "name"        => "DIV2",
                            "size"        => "1",
                            "options"     => $opt['A004'] ));

        $arg["DIV2"] = $objForm->ge("DIV2");
        //卒業区分
        $objForm->ae( array("type"        => "select",
                            "name"        => "DIV3",
                            "size"        => "1",
                            "options"     => $opt['A003'] ));

        $arg["DIV3"] = $objForm->ge("DIV3");

        //検索基準日
        $arg["BASEDATE1"] = View::popUpCalendar($objForm, "BASEDATE1");
        $arg["BASEDATE2"] = View::popUpCalendar($objForm, "BASEDATE2");
        $arg["BASEDATE3"] = View::popUpCalendar($objForm, "BASEDATE3");

        //実行ボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_search",
                            "value"       => "検索",
                            "extrahtml"   => "onclick=\"return search_submit('search2');\"" ));

        $arg["btn_search"] = $objForm->ge("btn_search");

        //閉じるボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_end",
                            "value"       => "戻る",
                            "extrahtml"   => "onclick=\"return btn_back();\"" ));

        $arg["btn_end"] = $objForm->ge("btn_end");
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjxSearch2.html", $arg);
    }
}
?>