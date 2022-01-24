<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knja121SubForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform1", "POST", "knja121index.php", "", "subform1");

        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        $db = Query::dbCheckOut();

        //年度コンボ（通知表所見）
        $opt_year = array();
        $result = $db->query(knja121Query::selectQueryYear($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_year[] = array("label" => $row["YEAR"],"value" => $row["YEAR"]);
            if ($model->year_cmb == "") $model->year_cmb = $row["YEAR"];
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "YEAR_CMB",
                            "size"        => "1",
                            "value"       => $model->year_cmb,
                            "options"     => $opt_year,
                            "extrahtml"   => "onChange=\"btn_submit('subform1')\";"
                           ));
        $arg["YEAR_CMB"] = $objForm->ge("YEAR_CMB");


        $query = knja121Query::selectQueryGuide($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //通信欄
            $objForm->ae( array("type"      => "textarea",
                                "name"      => "COMMUNICATION",
                                "rows"      => "4",
                                "cols"      => "43",
                                "extrahtml" => "style=\"height:65px;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"",
                                "value"     => $row["COMMUNICATION"]));
            
            $row["COMMUNICATION"] = $objForm->ge("COMMUNICATION");

            //総合的な学習の時間
            $objForm->ae( array("type"      => "textarea",
                                "name"      => "TOTALSTUDYTIME",
                                "rows"      => "4",
                                "cols"      => "43",
                                "extrahtml" => "style=\"height:65px;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"",
                                "value"     => $row["TOTALSTUDYTIME"]));
            
            $row["TOTALSTUDYTIME"] = $objForm->ge("TOTALSTUDYTIME");

            $row["SEMESTER"] = $model->control["学期名"][$row["SEMESTER"]];
            $arg["data"][] = $row;
        }
        Query::dbCheckIn($db);
        //終了ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => "戻る",
                            "extrahtml" => "onclick=\"return top.main_frame.right_frame.closeit()\"" ));
        $arg["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja121SubForm1.html", $arg);
    }
}
?>
