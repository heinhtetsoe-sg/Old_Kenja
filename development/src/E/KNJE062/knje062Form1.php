<?php

require_once('for_php7.php');

class knje062form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knje062index.php", "", "right_list");

        $db = Query::dbCheckOut();

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = htmlspecialchars($model->name);
        
        $objForm->ae(array("type"   => "checkbox",
                        "name"      => "CHECKALL",
                        "extrahtml" => "onClick=\"return check_all(this);\"" ));

        $arg["CHECKALL"] = $objForm->ge("CHECKALL");

        $order[1] = "▲";
        $order[-1] = "▼";

        $arg["YEAR"] = View::alink("knje062index.php", "<font color=\"white\">年度</font>", "",
                        array("cmd"=>"sort", "sort"=>"YEAR")) .$order[$model->sort["YEAR"]];

        $arg["SUBCLASSCD"] = View::alink("knje062index.php", "<font color=\"white\">科目名</font>", "",
                        array("cmd"=>"sort", "sort"=>"SUBCLASSCD")) .$order[$model->sort["SUBCLASSCD"]];

        //学籍賞罰データよりデータを取得
        if($model->schregno)
        {        
            $result = $db->query(knje062Query::selectQuery($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $objForm->ae( array("type"        => "checkbox",
                                    "name"        => "CHECKED",
                                    "value"       => $row["SCHOOLCD"] ."," .$row["ANNUAL"] ."," .$row["SUBCLASSCD"],
                                    "multiple"    => "1" ));
                                                      
                $row["CHECKED"] = $objForm->ge("CHECKED");
                $row["SCHOOLCD"] = ($row["SCHOOLCD"] == 1)? "前":"";

                $row["CREDIT"] = $row["GET_CREDIT"] +  $row["ADD_CREDIT"];
                $arg["data"][] = $row;
            }
        }
        Query::dbCheckIn($db);

        //削除ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_del",
                            "value"     => "削 除",
                            "extrahtml" => "onclick=\"return btn_submit('delete');\""));
        $arg["btn_del"] = $objForm->ge("btn_del");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "clear",
                            "value"     => "0"));

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje062Form1.html", $arg);
    }
}
?>
