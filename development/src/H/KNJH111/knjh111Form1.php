<?php

require_once('for_php7.php');

class knjh111form1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh111index.php", "", "edit");

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //学籍資格データよりデータを取得
        $db = Query::dbCheckOut();
        if($model->schregno) {
            $result = $db->query(knjh111Query::getAward($model, $model->schregno));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["REGDDATE"]  = str_replace("-","/",$row["REGDDATE"]);

                //今年度以外は、リンクなしにする
                if ($row["YEAR"] == CTRL_YEAR) {
                    $row["URL"] = View::alink("knjh111index.php", $row["REGDDATE"], "target=edit_frame",
                                                array("cmd"         => "edit",
                                                      "REGDDATE"    => $row["REGDDATE"],
                                                      "subclasscd"  => $row["SUBCLASSCD"],
                                                      "seq"         => $row["SEQ"],
                                                      "condition"   => $row["CONDITION_DIV"]
                                                      ));
                } else {
                    $row["URL"] = $row["REGDDATE"];
                }

                $arg["data"][] = $row;
            }
        }
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") == "right_list"){ 
                $arg["reload"]  = "parent.edit_frame.location.href='knjh111index.php?cmd=edit'";
        }

        View::toHTML($model, "knjh111Form1.html", $arg);
    }
}

?>
