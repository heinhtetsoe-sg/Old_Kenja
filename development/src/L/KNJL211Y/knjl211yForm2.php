<?php

require_once('for_php7.php');

class knjl211yForm2
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl211yindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //基礎データ
        $query = knjl211yQuery::getExamName($model);
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //受験番号・氏名
        $arg["EXAMNO"]  = $Row["EXAMNO"];
        $arg["NAME"]    = $Row["NAME"];

        //理由コンボ
        $optReason   = array();
        $optReason[] = array("label" => "", "value" => "");
        $result    = $db->query(knjl211yQuery::get_name_cd($model->year, "L017"));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optReason[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                                 "value" => $row["NAMECD2"]);
        }
        $result->free();

        //受付データを参照し、入試区分のレコードがある分の欠席チェックボツクスを表示
        $dis_button = " disabled";
        if (strlen($Row["EXAMNO"])) {
            $query = knjl211yQuery::getAttendFlg($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $name   = "ATTEND_FLG" .$row["TESTDIV"];
                $extraReason = ($row["ATTEND_FLG"] == "4") ? "" : " disabled"; //理由
                $extra  = ($row["ATTEND_FLG"] == "4") ? " checked" : ""; //欠席
                $extra .= ($row["NAMESPARE1"] == "1") ? " disabled" : ""; //合格の場合、disabledにする
                $extra .= " id=\"".$name."\" onclick=\"disReason(".$row["TESTDIV"].");\" ";
                $objForm->ae( array("type"      => "checkbox",
                                    "name"      => $name,
                                    "value"     => "4",
                                    "extrahtml" => $extra,
                                    "multiple"  => ""));
                $row["ATTEND_FLG"] = $objForm->ge($name);
                $row["ATTEND_LABEL"] = "<LABEL for=\"".$name."\">欠席</LABEL>";
                //理由コンボ
                $name  = "ATTEND_REASON" .$row["TESTDIV"];
                $value = $row["ATTEND_REASON"];
                $objForm->ae( array("type"      => "select",
                                    "name"      => $name,
                                    "size"      => "1",
                                    "extrahtml" => $extraReason,
                                    "value"     => $value,
                                    "options"   => $optReason ) );
                $row["ATTEND_REASON"] = $objForm->ge($name);

                $arg["data2"][] = $row;
                $dis_button = "";
            }
            $result->free();
        }

        //更新ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('attend_update')\"" .$dis_button ) );
        $arg["btn_update"]  = $objForm->ge("btn_update");

        //戻るボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"top.main_frame.closeit()\"" ) );
        $arg["btn_back"]  = $objForm->ge("btn_back");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ATTEND_APPLICANTDIV",
                            "value"     => $Row["APPLICANTDIV"]) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ATTEND_EXAMNO",
                            "value"     => $Row["EXAMNO"]) );

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl211yForm2.html", $arg); 
    }
}
?>
