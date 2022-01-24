<?php

require_once('for_php7.php');

class knjl060kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl060kindex.php", "", "main");
        $db           = Query::dbCheckOut();

        //ヘッダ
        $result = $db->query(knjl060kQuery::getName($model->year, "L003"));
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]), "value"  => $row["NAMECD2"]);
            if (!isset($model->testdiv)) $model->testdiv = $row["NAMECD2"];
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTDIV",
                            "size"       => "1",
                            "extrahtml"  => "",
                            "value"      => $model->testdiv,
                            "options"    => $opt));

        $arg["TESTDIV"] = $objForm->ge("TESTDIV");
        
        $arg["CTRL_YEAR"] = $model->year ."年度";

        //更新ボタン作成
        $objForm->ae( array("type"        => "button",
                                "name"        => "btn_exec",
                                "value"       => "実 行",
                                "extrahtml"   => "onclick=\"return btn_submit('exec')\"" ) );

        $arg["btn_exec"]  = $objForm->ge("btn_exec");

        //終了ボタン作成
        $objForm->ae( array("type"        => "button",
                                "name"        => "btn_end",
                                "value"       => "終 了",
                                "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["btn_end"]  = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl060kForm1.html", $arg); 
    }
}
?>
