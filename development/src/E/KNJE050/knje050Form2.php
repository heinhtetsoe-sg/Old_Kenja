<?php

require_once('for_php7.php');


class knje050Form2
{
    function main(&$model)
    {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knje050index.php", "", "main");
        //選択学年コース
        if (isset($model->field["GRADE"]) && 
            isset($model->field["COURSENAME"])){

            $arg["HEADER"] = sprintf("%d学年 コース：%s",
                                         $model->field["GRADE"],
                                         $model->field["COURSENAME"]
                                         );

        }
        //
        $objForm->add_element(array("type"      => "checkbox",
                                    "name"      => "chk_all",
                                    "extrahtml"   => "onClick=\"return check_all();\"" ));

        $arg["CHECK_ALL"] = $objForm->ge("chk_all");

        $db = Query::dbCheckOut();

        //生徒一覧情報取得
        $query = knje050Query::selectQuery($model);
        $result = $db->query($query);

        //キーの表示
        $disabled = "disabled";
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            $row["NO"] = $row["GRADE"]."-".$row["HR_CLASS"]."-".$row["ATTENDNO"];
            $objForm->add_element(array("type"       => "checkbox",
                                         "name"      => "SCHREGNO[]",
                                         "value"     => $row["SCHREGNO"],
                                         "extrahtml" => ($row["CHG_GRADES"] > 0)? "CHECKED" : "" ));
            $row["CHECK"]     = $objForm->ge("SCHREGNO[]");

            //値変更あり

            $js = "wopen('knje050index.php?cmd=subform1&SCHREGNO=".$row["SCHREGNO"]."&NAME=".urlencode($row["NAME_SHOW"])."&YEAR=".CTRL_YEAR."','modify' ,0,0,960,520);";

            $row["NO"] = View::alink("#", htmlspecialchars($row["NO"]), "onClick=\"$js\"");

            $arg["data"][] = $row;
            //キーの表示
            $disabled = "";
        }

        $result->free();
        Query::dbCheckIn($db);

        //評価が１の場合２に置き換える
        $objForm->add_element(array("type"      => "checkbox",
                                    "name"      => "REPLACE",
                                    "value"      => "1",
                                    "extrahtml"   => ($model->field["REPLACE"] == 1)? "CHECKED" : "" ));

        $arg["REPLACE"] = $objForm->ge("REPLACE");

        //CVS出力ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_cvs",
                            "value"       => "CSV出力",
                            "extrahtml"   => "$disabled onclick=\"return btn_submit('csv');\"" ));

        $arg["btn_cvs"] = $objForm->ge("btn_cvs");


        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        $order[1]="▲";
        $order[-1]="▼";

        //ソート用のURL作成
#        $arg["SORT"] = array("CHG_GRADES"  => View::alink("knje050index.php", "<font color=\"white\">値変更".$order[$model->sort["CHG_GRADES"]]."</font>", "", array("cmd"=>"sort", "sort"=>"CHG_GRADES")) ,
#                             "ATTENDNO"    => View::alink("knje050index.php", "<font color=\"white\">出席番号".$order[$model->sort["ATTENDNO"]]."</font>", "", array("cmd"=>"sort", "sort"=>"ATTENDNO"))
#                    );

        //ソート用のURL作成
        $arg["SORT"] = array("ATTENDNO" => View::alink("knje050index.php", "<font color=\"white\">出席番号".$order[$model->sort]."</font>", "", array("cmd"=>"sort", "sort"=>"ATTENDNO")));


        $arg["IFRAME"] = VIEW::setIframeJs();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje050Form2.html", $arg); 


    }
}
?>
