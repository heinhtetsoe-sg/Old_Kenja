<?php

require_once('for_php7.php');


class knjl041oForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $result = $db->query(knjl041oQuery::GetName("L003",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
        }
        if (!strlen($model->applicantdiv)) {
            $model->applicantdiv = $opt[0]["value"];
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "APPLICANTDIV",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"  tabindex=-1",
                            "value"      => $model->applicantdiv,
                            "options"    => $opt));
        $arg["TOP"]["APPLICANTDIV"] = $objForm->ge("APPLICANTDIV");

        //入試区分
        $opt = array();
        $result = $db->query(knjl041oQuery::getTestdivMst($model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMESPARE2"] == '1') {
                $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            }
        }
        if (!strlen($model->testdiv)) {
            $model->testdiv = $opt[0]["value"];
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTDIV",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"  tabindex=-1",
                            "value"      => $model->testdiv,
                            "options"    => $opt));
        $arg["TOP"]["TESTDIV"] = $objForm->ge("TESTDIV");

        //受験型
        $model->exam_type = "2";
        $exam_type = $db->getRow(knjl041oQuery::GetName("L005", $model->ObjYear, $model->exam_type), DB_FETCHMODE_ASSOC);
        knjCreateHidden($objForm, "EXAM_TYPE", $model->exam_type);
        $arg["TOP"]["EXAM_TYPE"] = $exam_type["NAME1"];

        //会場
        $opt = array();
        $opt[] = array("label" => "", "value" => "");
        $result = $db->query(knjl041oQuery::getHall($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["EXAMHALL_NAME"], "value" => $row["EXAMHALLCD"]);
        }
        if (!strlen($model->hallcd)) {
            $model->hallcd = $opt[0]["value"];
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "HALLCD",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"  tabindex=-1",
                            "value"      => $model->hallcd,
                            "options"    => $opt));
        $arg["TOP"]["HALLCD"] = $objForm->ge("HALLCD");

        //ソート順ラジオ（1:座席番号、2:受験番号）
        $opt = array(1, 2);
        $model->sort = ($model->sort == "") ? "1" : $model->sort;
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"SORT{$val}\" onClick=\"btn_submit('main');\" ");
        }
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;


        //一覧表示
        $arr_receptno = array();
        if (strlen($model->hallcd)) {
            //データ取得
            $result    = $db->query(knjl041oQuery::SelectQuery($model));

            if ($result->numRows() == 0 ){
               $model->setMessage("MSG303","\\n座席番号登録が行われていません。");
            }

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"];

                //対象者
                $extra  = "OnChange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\"";
                $extra .= ($row["TARGET_FLG"] == "1") ? " checked": "";
                $row["TARGET_FLG"] = knjCreateCheckBox($objForm, "TARGET_FLG-".$row["RECEPTNO"], "1", $extra);

                $arg["data"][] = $row;
            }
        }

        Query::dbCheckIn($db);


        //ボタン作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onClick=\"btn_submit('update');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onClick=\"btn_submit('reset');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["btn_update"] = $objForm->ge("btn_update");
        $arg["btn_reset"]  = $objForm->ge("btn_reset");
        $arg["btn_end"]    = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_RECEPTNO",
                            "value"     => implode(",",$arr_receptno)) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_APPLICANTDIV",
                            "value"     => "") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_TESTDIV",
                            "value"     => "") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_EXAM_TYPE",
                            "value"     => "") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_HALLCD",
                            "value"     => "") );
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl041oindex.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl041oForm1.html", $arg); 
    }
}
?>
