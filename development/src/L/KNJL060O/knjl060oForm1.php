<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjl060oForm1
{
    function main(&$model)
    {
        $db = Query::dbCheckOut();
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl060oindex.php", "", "main");
        
        //年度学期表示
        $arg["YEAR"] = $model->examyear;

        //入試制度
        $opt = array();
        $result = $db->query(knjl060oQuery::GetName("L003", $model->examyear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
        }
        
        $objForm->ae( array("type"       => "select",
                            "name"       => "APPLICANTDIV",
                            "size"       => "1",
                            "value"      => $model->applicantdiv,
                            "options"    => $opt));
        $arg["APPLICANTDIV"] = $objForm->ge("APPLICANTDIV");

        //入試区分
        $opt = array();
        $result = $db->query(knjl060oQuery::getTestdivMst($model->examyear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if($row["NAMESPARE2"]=='1') {
                $opt[]    = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            }
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTDIV",
                            "size"       => "1",
                            "value"      => $model->testdiv,
                            "options"    => $opt));
        $arg["TESTDIV"] = $objForm->ge("TESTDIV");

        Query::dbCheckIn($db);

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_exec",
                            "value"       => "実 行",
                            "extrahtml"   => "onclick=\"return btn_submit('exec');\"" ));

        $arg["btn_exec"] = $objForm->ge("btn_exec");

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl060oForm1.html", $arg);
    }
}
?>
