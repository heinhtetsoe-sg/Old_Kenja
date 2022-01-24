<?php

require_once('for_php7.php');

class knjxSearch3
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
        $opt = array();
        $opt[] = array("label"  => '',
                    "value" => '');
                
        $result = $db->query(knjxexpkQuery::selectGrantcd($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label"     => $row["GRANTCD"] ."：" .htmlspecialchars($row["NAME1"]),
                                            "value"     => $row["GRANTCD"]);

        }
        //交付コード
        $objForm->ae( array("type"        => "select",
                            "name"        => "GRANTCD",
                            "size"        => "1",
                            "options"     => $opt ));

        $arg["GRANTCD"] = $objForm->ge("GRANTCD");

        //交付年度
        $objForm->ae( array("type"        => "text",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"  => "onblur=\"this.value=toInteger(this.value)\"",
                            "name"        => "YEAR"));

        $arg["YEAR"] = $objForm->ge("YEAR");

        //検索基準日
        $arg["BASEDATE"] = View::popUpCalendar($objForm, "BASEDATE");

        //実行ボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_search",
                            "value"       => "検索",
                            "extrahtml"   => "onclick=\"return search_submit('search3');\"" ));

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

        View::toHTML($model, "knjxSearch3.html", $arg);
    }
}
?>