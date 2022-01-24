<?php

require_once('for_php7.php');

class knjm210_2Form1
{   
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjm210_2index.php", "", "main");
        $db = Query::dbCheckOut();

        $link_flg = false;  //リンクフラグ

        //講座名（科目名）リスト：抽出
        $opt_chair = array();
        $result = $db->query(knjm210_2Query::getChairSubclassList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");

            //リンク設定（詳細一覧画面：科目別）
            $js = "wopen('knjm210_2index.php?cmd=form2&SCHNO={$model->schregno}&CHAIR={$row["CHAIRCD"]}&SUBCLASS={$row["SUBCLASSCD"]}&PROGRAMID=PROGRAMID','SUBWIN3',0,0,screen.availWidth,screen.availHeight);";
            $row["SUBCLASSNAME"] = View::alink("#", htmlspecialchars($row["SUBCLASSNAME"]), "onClick=\"$js\"");

            $opt_chair[] = array("CHAIRCD"        => $row["CHAIRCD"],
                                 "CHAIRNAME"      => $row["CHAIRNAME"],
                                 "SUBCLASSCD"     => $row["SUBCLASSCD"],
                                 "SUBCLASSNAME"   => $row["SUBCLASSNAME"]);

            $link_flg = true;  //リンクフラグ
        }

        //リンク設定（詳細一覧画面：全科目）
        if ($link_flg) {
            $js = "wopen('knjm210_2index.php?cmd=form3&SCHNO={$model->schregno}&PROGRAMID=PROGRAMID','SUBWIN3',0,0,screen.availWidth,screen.availHeight);";
            $arg["data"]["SUBCLASSALL"] = View::alink("#", "全科目詳細", "onClick=\"$js\"");
//            $arg["data"]["SUBCLASSALL"] = View::alink("#", "全科目詳細", "onClick=\"return btn_submit('main');\"");
        }

        //講座名（科目名）リスト：表示
        foreach ($opt_chair as $key => $val) {
            //規定回数
            $Row1 = knjm210_2Query::getKiteiCount($db,$val["CHAIRCD"]);
            //回数・履歴(スクーリング)
            $Row2 = $Row3 = array();
            $num2 = $num3 = 0;
            $cnt1 = $cnt2 = $cnt3 = $cnt4 = 0;
            $query = knjm210_2Query::getSchCountRireki($db,$val["CHAIRCD"],$model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                array_walk($row, "htmlspecialchars_array");

                //履歴(登校・ネット)
                if ($row["SCHOOLINGKINDCD"] == "1" || $row["SCHOOLINGKINDCD"] == "2") {
                    if ($row["SCHOOLINGKINDCD"] == "1") {
                        $cnt1++;
                        $color_s = "";
                        $color_e = "";
                    }
                    if ($row["SCHOOLINGKINDCD"] == "2") {
                        $cnt2++;
                        $color_s = "<span style=\"color:red\">";
                        $color_e = "</span>";
                    }
                    $tmp_row = array("DAYUPP".($num2+1) => $color_s.$row["E_DATE"].$color_e);
                    if ($num2 == 0) $Row2 = $tmp_row;
                    if ($num2 > 0)  $Row2 = array_merge_recursive($Row2,$tmp_row);
                    $num2++;
                }
                //履歴(放送・メディア)
                if ($row["SCHOOLINGKINDCD"] == "3" || $row["SCHOOLINGKINDCD"] == "4") {
                    if ($row["SCHOOLINGKINDCD"] == "3") {
                        $cnt3++;
                        $color_s = "<span style=\"color:green\">";
                        $color_e = "</span>";
                    }
                    if ($row["SCHOOLINGKINDCD"] == "4") {
                        $cnt4++;
                        $color_s = "<span style=\"color:purple\">";
                        $color_e = "</span>";
                    }
                    $tmp_row = array("DAYMID".($num3+1) => $color_s.$row["E_DATE"].$color_e);
                    if ($num3 == 0) $Row3 = $tmp_row;
                    if ($num3 > 0)  $Row3 = array_merge_recursive($Row3,$tmp_row);
                    $num3++;
                }
            }//while綴り
            //回数(登校・ネット・放送・メディア)
            if ($cnt1 > 0 || $cnt2 > 0 || $cnt3 > 0 || $cnt4 > 0) {
                $Row2 = array_merge_recursive($Row2,array("TOK" => $cnt1));
                $Row2 = array_merge_recursive($Row2,array("NET" => $cnt2));
                $Row3 = array_merge_recursive($Row3,array("HOU" => $cnt3));
                $Row3 = array_merge_recursive($Row3,array("MED" => $cnt4));
            }
            //回数・履歴(レポート)
            $Row4 = array();
            $num4 = 0;
            $query = knjm210_2Query::getRepCountRireki($db,$val["CHAIRCD"],$model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                array_walk($row, "htmlspecialchars_array");

                //履歴
                $tmp_row = array("DAYLOW".$row["STANDARD_SEQ"] => $row["HYOUKA"]);
                if ($num4 == 0) $Row4 = $tmp_row;
                if ($num4 > 0)  $Row4 = array_merge_recursive($Row4,$tmp_row);
                $num4++;
            }
            //回数
            if ($num4 > 0) $Row4 = array_merge_recursive($Row4,array("CNTLOW" => $num4));
            //表示
            $res_arr = array_merge_recursive($val,$Row1,$Row2,$Row3,$Row4);
            $arg["data2"][$key] = $res_arr;
        }//foreach綴り

        $header  = "";
        for ($k=1; $k<25; $k++) $header .= "<th width=\"45\" nowrap>".$k."</th>";
        $arg["header"] = $header;

        //ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_cancel",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        $arg["button"] = array("BTN_CLEAR"  => $objForm->ge("btn_cancel") );  
        
        //HIDDEN
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

		$result->free();
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm210_2Form1.html", $arg); 
    }
}
?>
