<?php

require_once('for_php7.php');

class knjl030hForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl030hindex.php", "", "main");
        $db           = Query::dbCheckOut();

        //ヘッダ
        $result = $db->query(knjl030hQuery::getName("L004",$model->year));
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]), "value"  => $row["NAMECD2"]);
//            if (!isset($model->testdiv)) $model->testdiv = $row["NAMECD2"];
            if ($model->testdiv=="" && $row["NAMESPARE2"]=='1') $model->testdiv = $row["NAMECD2"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTDIV",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"",
                            "value"      => $model->testdiv,
                            "options"    => $opt));

        $arg["TOP"]["TESTDIV"] = $objForm->ge("TESTDIV");

//        //受験型
//        $result = $db->query(knjl030hQuery::getName("L005",$model->year,"DESC"));
//        $opt = array();
//        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
//        {
//            $opt[] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]), "value"  => $row["NAMECD2"]);
//            if (!isset($model->exam_type)) $model->exam_type = $row["NAMECD2"];
//        }
//
//        $objForm->ae( array("type"       => "select",
//                            "name"       => "EXAM_TYPE",
//                            "size"       => "1",
//                            "extrahtml"  => "Onchange=\"btn_submit('main');\"",
//                            "value"      => $model->exam_type,
//                            "options"    => $opt));
//
//        $arg["TOP"]["EXAM_TYPE"] = $objForm->ge("EXAM_TYPE");
        $arg["TOP"]["YEAR"] = $model->year ."年度";

        $objForm->ae(array("type"      => "checkbox",
                           "name"      => "CHECKALL",
                           "extrahtml" => "onClick=\"return check_all(this);\"" ));

        $arg["CHECKALL"] = $objForm->ge("CHECKALL");

        //受験型
        $result = $db->query(knjl030hQuery::selectQuery($model));
        $arg["data"] = array();
        unset($model->max_examhallcd);
        unset($model->e_receptno);
        $disabled = "disabled";
        if (!strlen($model->s_receptno) || (int)$model->s_receptno == 0) {
            $model->s_receptno = "0001";
        }
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
                            "onclick=\"loadwindow('knjl030hindex.php?cmd=edit&mode=update&examhallcd=".$row["EXAMHALLCD"] ."',event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(),370,100);\"");
            $arg["data"][] = $row;
        }

        Query::dbCheckIn($db);

        //開始座席番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "S_RECEPTNO",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "extrahtml"   => " style=\"text-align:center\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => $model->s_receptno
                            ));
        $arg["TOP"]["S_RECEPTNO"] = $objForm->ge("S_RECEPTNO");

        //削除ボタン作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_delete",
                            "value"       => "削 除",
                            "extrahtml"   => "$disabled onclick=\"return btn_submit('delete');\"" ) );

        $arg["btn_delete"]  = $objForm->ge("btn_delete");

        //割振り実行ボタン作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_exec",
                            "value"       => "割振り実行",
                            "extrahtml"   => "$disabled onclick=\"return btn_submit('exec');\"" ) );

        $arg["btn_exec"]  = $objForm->ge("btn_exec");

        //会場追加ボタン作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "会場追加",
#                            "extrahtml"   => "onclick=\"loadwindow('knjl030hindex.php?cmd=edit&mode=insert',body.clientWidth/2-200,body.clientHeight/2-100,370,100);\"" ) );
                            "extrahtml"   => "onclick=\"return btn_submit('halladd');\"" ) );

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
        View::toHTML($model, "knjl030hForm1.html", $arg); 
    }
}
?>
