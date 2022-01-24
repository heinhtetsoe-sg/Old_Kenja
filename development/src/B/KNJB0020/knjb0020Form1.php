<?php

require_once('for_php7.php');

class knjb0020Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $db      = Query::dbCheckOut();
        
        $arg["start"] = $objForm->get_start("edit", "POST", "knjb0020index.php", "", "edit");

        $arg["FUSE_STAFFCD"] = $model->fusestaffcd;

        $arg["NAME"]    = $model->name;

        $colhead = array("99" => "校時/曜日",
                         "2"  => "月曜日",
                         "3"  => "火曜日",
                         "4"  => "水曜日",
                         "5"  => "木曜日",
                         "6"  => "金曜日",
                         "7"  => "土曜日",
                         "1"  => "日曜日");

        //稼動不可情報
        $data = array();
        $result = $db->query(knjb0020Query::GetOpeimposs($model->staffcd));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $data[$row["DAYCD"]][$row["PERIODCD"]] = isset($row["PERIODCD"]);
        }

        $result  = $db->query(knjb0020Query::GetPeriod());

        $table = "<table width=\"100%\" border=\"1\" cellpadding=\"3\" cellspacing=\"0\">";

        //曜日ヘッダ（校時軸、曜日軸のセルID番号を99とする）
        $head = "<tr class=\"no_search\">";
        foreach ($colhead as $key => $val) {
            $head .= "<td id=\"".$key."-99\" 
                          onclick=\"switchCell('".$key."-99');\" 
                          style=\"height:10px;\" align=\"center\" value=\"1\">".$val."</td>";
        }
        $head .= "</tr>";

        //テーブル作成
        $period = array();
        $num = $result->numRows();
        while ($r = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $period[] = $r["PERIODCD"];

            $row .= "<tr class=\"no_search\">";

            foreach ($colhead as $key => $val) {
                if ($key == "99") {
                    $id = "99-".$r["PERIODCD"];
                    $col .= "<td id=\"".$id."\" onclick=\"switchCell('$id');\" value=\"1\">".$r["PERIODNAME"]."</td>";
                } else {
                    $id = $key."-".$r["PERIODCD"];
                    if (isset($data[$key][$r["PERIODCD"]])) {
                        $col .= "<td style=\"height:40px; width=50px;\" 
                                     bgcolor=\"#ff0099\" align=\"center\"
                                     id=\"".$id."\" value=\"false\" onclick=\"switchCell('$id');\">稼動<br>不可</td>";
                        $col .= "<input type=\"hidden\" name=\"HIDDEN_".$id."\" id=\"HIDDEN_".$id."\" value=\"false\">";
                    } else {
                        $col .= "<td style=\"height:40px; width=50px;\" 
                                     bgcolor=\"#3399ff\" align=\"center\"
                                     id=\"".$id."\" value=\"true\" onclick=\"switchCell('$id');\">稼動可</td>";
                        $col .= "<input type=\"hidden\" name=\"HIDDEN_".$id."\" id=\"HIDDEN_".$id."\" value=\"true\">";
                    }
                }
            }

            $row .= $col;
            $row .= "</tr>";
            $col = "";
        }

        $table .= $head.$row."</table>";
        $arg["timeTable"] = $table;

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_delete",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ));
        $arg["button"]["btn_delete"] = $objForm->ge("btn_delete");

        $objForm->ae( array("type"        => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "STAFFCD", $model->staffcd);
        knjCreateHidden($objForm, "data");
        knjCreateHidden($objForm, "period", implode($period,","));//校時
        knjCreateHidden($objForm, "FUSE_STAFFCD", $model->fusestaffcd);

        $result->free();
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjb0020Form1.html", $arg);
    }
}
?>
