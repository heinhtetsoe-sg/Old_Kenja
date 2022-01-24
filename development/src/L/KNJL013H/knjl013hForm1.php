<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjl013hForm1
{
    function main(&$model)
    {
        $db = Query::dbCheckOut();
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl013hindex.php", "", "main");
        
        //年度学期表示
        $arg["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $result = $db->query(knjl013hQuery::GetName("L003",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            if ($model->applicantdiv=="" && $row["NAMESPARE2"]=='1') $model->applicantdiv = $row["NAMECD2"];
        }
        
        $objForm->ae( array("type"       => "select",
                            "name"       => "APPLICANTDIV",
                            "size"       => "1",
                            "value"      => $model->applicantdiv,
                            "options"    => $opt));
        $arg["APPLICANTDIV"] = $objForm->ge("APPLICANTDIV");

        //出願区分
        $opt = array();
        $result = $db->query(knjl013hQuery::GetName("L004",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[]    = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            if ($model->testdiv=="" && $row["NAMESPARE2"]=='1') $model->testdiv = $row["NAMECD2"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTDIV",
                            "size"       => "1",
                            "value"      => $model->testdiv,
                            "options"    => $opt));
        $arg["TESTDIV"] = $objForm->ge("TESTDIV");

        Query::dbCheckIn($db);

        //受付番号の値
        $opt = array();
        $opt[]  = array("label" => "1:受験番号をコピー", "value" => "1");
        $opt[]  = array("label" => "2:連番(受験番号順)", "value" => "2");
        $objForm->ae( array("type"       => "select",
                            "name"       => "RECEPT_DIV",
                            "size"       => "1",
                            "value"      => $model->recept_div,
                            "options"    => $opt));
        $arg["RECEPT_DIV"] = $objForm->ge("RECEPT_DIV");


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

        View::toHTML($model, "knjl013hForm1.html", $arg);
    }
}
?>
