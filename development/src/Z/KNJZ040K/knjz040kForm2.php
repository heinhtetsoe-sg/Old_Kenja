<?php

require_once('for_php7.php');

class knjz040kForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz040kindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if(!isset($model->warning)){
            $Row = knjz040kQuery::getRow($model,1);
        }else{
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //受験コースコンボ
        $result = $db->query(knjz040kQuery::getExamCourse($model->year));
        $opt = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"].":".$row["EXAMCOURSE_NAME"],
                           "value" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]);
        }
        $result->free();

        //(編集)コース
        $objForm->ae( array("type"        => "select",
                            "name"        => "TOTALCD",
                            "size"        => "1",
                            "value"       => $Row["TOTALCD"],
                            "options"     => $opt));
        $arg["data"]["TOTALCD"] = $objForm->ge("TOTALCD");

        //補完設定コース
        $objForm->ae( array("type"        => "select",
                            "name"        => "CMP_TOTALCD",
                            "size"        => "1",
                            "value"       => $Row["CMP_TOTALCD"],
                            "options"     => $opt));
        $arg["data"]["CMP_TOTALCD"] = $objForm->ge("CMP_TOTALCD");


        //専併区分取得
        $result = $db->query(knjz040kQuery::getName($model->year, 'L006'));
        $opt = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();

        //(編集)専併区分
        $objForm->ae( array("type"        => "select",
                            "name"        => "SHDIV",
                            "size"        => "1",
                            "value"       => $Row["SHDIV"],
                            "options"     => $opt));
        $arg["data"]["SHDIV"] = $objForm->ge("SHDIV");


        //判定区分取得
        $result = $db->query(knjz040kQuery::getName($model->year, 'L002'));
        $opt = array();
        $opt_judge = array();
        $opt_judge[] = array("label" => "", "value" => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAME1"],
                           "value" => $row["NAMECD2"]);

            $opt_judge[] = array("label" => $row["NAME1"],
                                 "value" => $row["NAMECD2"]);
        }
        $result->free();

        //(編集)事前判定
        $objForm->ae( array("type"        => "select",
                            "name"        => "JUDGEMENT",
                            "size"        => "1",
                            "value"       => $Row["JUDGEMENT"],
                            "options"     => $opt));
        $arg["data"]["JUDGEMENT"] = $objForm->ge("JUDGEMENT");

        //補完専願判定
        $objForm->ae( array("type"        => "select",
                            "name"        => "S_JUDGEMENT",
                            "size"        => "1",
                            "value"       => $Row["S_JUDGEMENT"],
                            "options"     => $opt_judge));
        $arg["data"]["S_JUDGEMENT"] = $objForm->ge("S_JUDGEMENT");

        //補完併願判定
        $objForm->ae( array("type"        => "select",
                            "name"        => "H_JUDGEMENT",
                            "size"        => "1",
                            "value"       => $Row["H_JUDGEMENT"],
                            "options"     => $opt_judge));
        $arg["data"]["H_JUDGEMENT"] = $objForm->ge("H_JUDGEMENT");

        //ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year ) );

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjz040kindex.php?cmd=list','left_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz040kForm2.html", $arg);
    }
}
?>
