<?php

require_once('for_php7.php');

class knjxSearch4
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
        $opt2 = array();
        $opt3 = array();
        $opt[] = array("label"  => '',
                    "value" => '');
        $opt2[] = array("label"  => '',
                    "value" => '');
        $opt3[] = array("label"  => '',
                    "value" => '');
                
        $result = $db->query(knjxexpkQuery::selectBankcd($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
#            $opt[$row["BANKCD"]] = array("label"     => $row["BANKCD"] ."：" .htmlspecialchars($row["BANKNAME"]),
#                                         "value"     => $row["BANKCD"]);
            $opt[$row["BANKCD"]] = array("label"     => $row["BANKCD"] ."：" .htmlspecialchars($row["BANKNAME"]) ."(".htmlspecialchars($row["BANKNAME_KANA"]).")",
                                         "value"     => $row["BANKCD"]);

#            $opt2[$row["BANKCD"] ."-" .$row["BRANCHCD"]] 
#                                 = array("label"     => $row["BRANCHCD"] ."：" .htmlspecialchars($row["BRANCHNAME"]),
#                                         "value"     => $row["BANKCD"] ."-" .$row["BRANCHCD"]);
            $opt2[$row["BANKCD"] ."-" .$row["BRANCHCD"]] 
                                 = array("label"     => $row["BRANCHCD"] ."：" .htmlspecialchars($row["BRANCHNAME"]) ."(".htmlspecialchars($row["BRANCHNAME_KANA"]).")",
                                         "value"     => $row["BANKCD"] ."-" .$row["BRANCHCD"]);

            $opt3[$row["DEPOSIT_ITEM"]] 
                                 = array("label"     => $row["DEPOSIT_ITEM"] ."：" .htmlspecialchars($row["DEPOSIT_ITEM2"]),
                                         "value"     => $row["DEPOSIT_ITEM"]);

        }
        //銀行コード
        $objForm->ae( array("type"        => "select",
                            "name"        => "BANKCD",
                            "extrahtml"  => "onchange=\"chgBankcd(this)\"",
                            "size"        => "1",
                            "options"     => $opt ));

        $arg["BANKCD"] = $objForm->ge("BANKCD");

        //支店コード
        $objForm->ae( array("type"        => "select",
                            "name"        => "BRANCHCD",
                            "size"        => "1",
                            "options"     => $opt2 ));

        $arg["BRANCHCD"] = $objForm->ge("BRANCHCD");

        ksort($opt3);
        //預金種別
        $objForm->ae( array("type"        => "select",
                            "name"        => "DEPOSIT_ITEM",
                            "size"        => "1",
                            "options"     => $opt3 ));

        $arg["DEPOSIT_ITEM"] = $objForm->ge("DEPOSIT_ITEM");

//実行ボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_search",
                            "value"       => "検索",
                            "extrahtml"   => "onclick=\"return search_submit('search4');\"" ));

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

        View::toHTML($model, "knjxSearch4.html", $arg);
    }
}
?>