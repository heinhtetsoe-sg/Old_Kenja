<?php

require_once('for_php7.php');

class knjl030cForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl030cindex.php", "", "main");
        $db           = Query::dbCheckOut();
        //ヘッダ
        $result = $db->query(knjl030cQuery::getName($model->year, "L004"));
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]), "value"  => $row["NAMECD2"]);
            if ($model->testdiv=="" && $row["NAMESPARE2"]=='1') $model->testdiv = $row["NAMECD2"];
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTDIV",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"",
                            "value"      => $model->testdiv,
                            "options"    => $opt));

        $arg["TOP"]["TESTDIV"]  = $objForm->ge("TESTDIV");
        
        $arg["TOP"]["YEAR"]     = $model->year ."年度";

        $objForm->ae(array("type"      => "checkbox",
                           "name"      => "CHECKALL",
                           "extrahtml" => "onClick=\"return check_all(this);\"" ));

        $arg["CHECKALL"] = $objForm->ge("CHECKALL");

        //会場一覧
        $result = $db->query(knjl030cQuery::selectQuery($model));
        $arg["data"] = array();
        unset($model->max_examhallcd);
        unset($model->e_receptno);
        $disabled = "disabled";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $disabled = "";
            $objForm->ae( array("type"        => "checkbox",
                                "name"        => "CHECKED",
                                "value"       => $row["EXAMHALLCD"],
                                "extrahtml"   => "tabindex=\"-1\"",
                                "multiple"    => "1" ));

            $row["CHECKED"]     = $objForm->ge("CHECKED");
            $row["EXAMHALL_NAME"] = View::alink("#",htmlspecialchars($row["EXAMHALL_NAME"]),
                            "onclick=\"loadwindow('knjl030cindex.php?cmd=edit&mode=update&examhallcd=".$row["EXAMHALLCD"] ."',event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(),400,200);\"");
            $arg["data"][] = $row;
        }

        Query::dbCheckIn($db);


        //削除ボタン作成
        $objForm->ae( array("type"        => "button",
                                "name"        => "btn_delete",
                                "value"       => "削 除",
                                "extrahtml"   => "$disabled onclick=\"return btn_submit('delete');\"" ) );

        $arg["btn_delete"]  = $objForm->ge("btn_delete");

        //会場追加ボタン作成
        $objForm->ae( array("type"        => "button",
                                "name"        => "btn_add",
                                "value"       => "会場追加",
                                "extrahtml"   => "onclick=\"loadwindow('knjl030cindex.php?cmd=edit&mode=insert',body.clientWidth/2-200,body.clientHeight/2-100,400,200);\"" ) );

        $arg["btn_add"]  = $objForm->ge("btn_add");

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
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjl030cForm1.html", $arg); 
    }
}
?>
