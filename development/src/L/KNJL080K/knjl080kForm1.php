<?php

require_once('for_php7.php');
class knjl080kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $db           = Query::dbCheckOut();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl080kindex.php", "", "main");

        //年度
        $arg["TOP"]["YEAR"] = CTRL_YEAR + 1;

        //試験区分
        $opt = array();
        $result = $db->query(knjl080kQuery::GetName("L003",$model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTDIV",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"",
                            "value"      => $model->testdiv,
                            "options"    => $opt));
        $arg["TOP"]["TESTDIV"] = $objForm->ge("TESTDIV");

        //受験科目
        $opt = array();
        $result = $db->query(knjl080kQuery::GetName("L009",$model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[]    = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTSUBCLASSCD",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"",
                            "value"      => $model->testsubclasscd,
                            "options"    => $opt));
        $arg["TOP"]["TESTSUBCLASSCD"] = $objForm->ge("TESTSUBCLASSCD");

        //受験番号自
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value);\" tabindex=-1",
                            "value"       => $model->examno));

        //受験番号至
        if ((int)$model->examno + 19 >= 9999) {
            $end_examno = "9999";   
        } elseif (strlen($model->examno)){
            $end_examno = sprintf("%04d", (int)$model->examno + 19);
        }
        $arg["TOP"]["END_EXAMNO"] = (strlen($end_examno) ? $end_examno : "     ");
        
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_read",
                            "value"       => "読込み",
                            "extrahtml"   => "onClick=\"btn_submit('read');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => " << ",
                            "extrahtml"   => "onClick=\"btn_submit('back2');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_next",
                            "value"       => " >> ",
                            "extrahtml"   => "onClick=\"btn_submit('next2');\"" ) );

        $arg["TOP"]["EXAMNO"] = $objForm->ge("EXAMNO");
        $arg["TOP"]["button"] = $objForm->ge("btn_read")."　　".$objForm->ge("btn_back").$objForm->ge("btn_next");

        //データ取得
        if ($model->cmd == "read" || $model->cmd == "back" || $model->cmd == "next" || $model->cmd == "back2" || $model->cmd == "next2")
        {
            $tmp = array();
            $result    = $db->query(knjl080kQuery::SelectQuery($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                //コース名をスラッシュ区切りで表示するために一時保持
                $tmp[$row["EXAMNO"]]["EXAMNO"]            = $row["EXAMNO"];
                $tmp[$row["EXAMNO"]]["NAME"]              = $row["NAME"];
                $tmp[$row["EXAMNO"]]["SEX"]               = $row["SEX"];
                $tmp[$row["EXAMNO"]]["DESIREDIV"]         = $row["DESIREDIV"];
                $tmp[$row["EXAMNO"]]["A_SCORE"]           = $row["A_SCORE"];
                $tmp[$row["EXAMNO"]]["B_SCORE"]           = $row["B_SCORE"];
                $tmp[$row["EXAMNO"]]["AUTOCALC"]          = $row["AUTOCALC"];
                $tmp[$row["EXAMNO"]]["A_PERFECT"]         = $row["A_PERFECT"];
                $tmp[$row["EXAMNO"]]["B_PERFECT"]         = $row["B_PERFECT"];              #2005/09/06
                $tmp[$row["EXAMNO"]]["INC_MAGNIFICATION"] = $row["INC_MAGNIFICATION"];
                $tmp[$row["EXAMNO"]]["EXAMCOURSE_NAME"][] = $row["EXAMCOURSE_NAME"];
            }

            //HTML出力用に配置し直す
            foreach ($tmp as $examno => $row)
            {
                $objForm->ae( array("type"        => "text",
                                    "name"        => "A_SCORE",
                                    "extrahtml"   => "style=\"text-align:center;\" id=\"".$row["EXAMNO"]."\" OnChange=\"Setflg();\" onblur=\"CheckScore(this);\"",
                                    "size"        => "3",
                                    "maxlength"   => "3",
                                    "multiple"    => "1",
                                    "value"       => $row["A_SCORE"]));
                $row["A_SCORE"] = $objForm->ge("A_SCORE");

                //B_SCORE手入力
                if ($row["AUTOCALC"] == "0") {

                    $objForm->ae( array("type"        => "text",
                                        "name"        => "B_SCORE".$row["EXAMNO"],
                                        #"extrahtml"   => "style=\"text-align:center;\"  OnChange=\"Setflg();\" onblur=\"this.value=toInteger(this.value);\"",  //2005/09/06
                                        "extrahtml"   => "style=\"text-align:center;\" id=\"".$row["EXAMNO"]."\" OnChange=\"Setflg();\" onblur=\"CheckScore(this);\"",
                                        "size"        => "3",
                                        "maxlength"   => "3",
                                        "value"       => $row["B_SCORE"]));
                    $row["B_SCORE"] = $objForm->ge("B_SCORE".$row["EXAMNO"]);

                } else {
                    //自動計算場合はHIDDENにセットするため
                    $objForm->ae( array("type"      => "hidden",
                                        "name"      => "B_SCORE".$row["EXAMNO"],
                                        "value"     => "") );
                }

                $row["B_ID"] = "B_".$row["EXAMNO"];

                //コース名をスラッシュ区切り
                $row["EXAMCOURSE_NAME"] = "&nbsp;".$row["DESIREDIV"]."：".implode(" / ", $row["EXAMCOURSE_NAME"]);
                //満点チェック用
                #$arg["data2"][] = array("key" => $row["EXAMNO"], "perf" => (int)$row["A_PERFECT"].",".$row["INC_MAGNIFICATION"]);      #2005/09/06
                $arg["data2"][] = array("key" => $row["EXAMNO"], "perf" => (int)$row["A_PERFECT"].",".$row["INC_MAGNIFICATION"].",".(int)$row["B_PERFECT"]);
                $arg["data"][]  = $row;
            }

            //表示される受験番号をセット
            if (get_count($tmp) > 0) {
                $objForm->ae( array("type"      => "hidden",
                                    "name"      => "HID_EXAMNO",
                                    "value"     => implode(",",array_keys($tmp))) );
            }
        }

        Query::dbCheckIn($db);
        //ボタン作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onClick=\"btn_submit('update');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update_back",
                            "value"       => "更新後前の20名",
                            "extrahtml"   => "onClick=\"btn_submit('back');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update_next",
                            "value"       => "更新後次の20名",
                            "extrahtml"   => "onClick=\"btn_submit('next');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onClick=\"btn_submit('reset');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        
        $arg["btn_update"]  = $objForm->ge("btn_update");
        $arg["update_back"] = $objForm->ge("btn_update_back");
        $arg["update_next"] = $objForm->ge("btn_update_next");
        $arg["btn_reset"]   = $objForm->ge("btn_reset");
        $arg["btn_end"]     = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_TESTDIV",
                            "value"     => "") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_TESTSUBCLASSCD",
                            "value"     => "") );

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl080kForm1.html", $arg); 
    }
}
?>
