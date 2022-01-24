<?php

require_once('for_php7.php');
//ビュー作成用クラス
class knjl061jForm1
{
    function main(&$model)
    {
        $db = Query::dbCheckOut();
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl061jindex.php", "", "main");
        
        //年度学期表示
        $arg["YEAR"] = $model->examyear;

        //入試区分
        $opt = array();
        $model->nonTestdiv = array();
        $result = $db->query(knjl061jQuery::GetName("L004", $model->examyear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //リンク判定の処理から帰国生を除く。
            //実際は名称マスタL004の略称２に１が立っている入試区分を除く。
            if ($row["ABBV2"] == '1') continue;
            $model->nonTestdiv[] = $row["NAMECD2"];

            $opt[]    = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            if ($model->testdiv=="" && $row["NAMESPARE2"]=='1') $model->testdiv = $row["NAMECD2"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTDIV",
                            "size"       => "1",
                            "value"      => $model->testdiv,
                            "extrahtml"  => "Onchange=\"btn_submit('main');\" ",
                            "options"    => $opt));
        $arg["TESTDIV"] = $objForm->ge("TESTDIV");

        //科目
        $opt_test = array();
        $result = $db->query(knjl061jQuery::getTestSubclasscd($model->testdiv, $model->examyear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_test[] = $row["TESTSUBCLASSCD"];
        }

        //リンク判定科目
        $result = $db->query(knjl061jQuery::GetName("L009", $model->examyear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if (!in_array($row["NAMECD2"], $opt_test)) continue;
            $fieldname = "TESTSUBCLASSCD" .$row["NAMECD2"];
            $checked = in_array($row["NAMECD2"], $model->testsubclasscd_link) ? "checked" : "";
            $objForm->ae( array("type"        => "checkbox",
                                "name"        => $fieldname,
                                "extrahtml"   => "id=\"$fieldname\" " .$checked,
                                "value"       => $row["NAMECD2"] ));
            $arg[$fieldname] = $objForm->ge($fieldname);
            $arg[$fieldname."_ID"] = $fieldname;
            $arg[$fieldname."_NAME"] = $row["NAME1"];
        }

        //確定済みリスト
        $tmp_testdiv = "";
        $opt_data2 = array();
        $result = $db->query(knjl061jQuery::getTestSubclasscdUpdated($model->examyear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");

            if ($row["TESTDIV"] != $tmp_testdiv && $tmp_testdiv != "") {
                $opt_data2["TESTSUBCLASSCD_NAME"] = ($opt_data2["TESTSUBCLASSCD_NAME"] != "") ? $opt_data2["TESTSUBCLASSCD_NAME"] : "なし";
                $arg["data2"][] = $opt_data2;//データセット
                $opt_data2["TESTDIV_NAME"] = "";
                $opt_data2["TESTSUBCLASSCD_NAME"] = "";
                $opt_data2["UPDATED"] = "";
            }
            $opt_data2["TESTDIV_NAME"] = $row["TESTDIV"] ."：" .$row["TESTDIV_NAME"];
            $opt_data2["TESTSUBCLASSCD_NAME"] .= ($row["LINK_JUDGE_DIV"] == "1" && $opt_data2["TESTSUBCLASSCD_NAME"] != "") ? ", " : "";
            $opt_data2["TESTSUBCLASSCD_NAME"] .= ($row["LINK_JUDGE_DIV"] == "1") ? $row["TESTSUBCLASSCD_NAME"] : "";
            $opt_data2["UPDATED"] = str_replace("-", "/", $row["DAY"]) ." " .$row["TIM"];
            $tmp_testdiv = $row["TESTDIV"];
        }
        if ($tmp_testdiv != "") {
            $opt_data2["TESTSUBCLASSCD_NAME"] = ($opt_data2["TESTSUBCLASSCD_NAME"] != "") ? $opt_data2["TESTSUBCLASSCD_NAME"] : "なし";
            $arg["data2"][] = $opt_data2;//データセット
        }

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

        View::toHTML($model, "knjl061jForm1.html", $arg);
    }
}
?>
