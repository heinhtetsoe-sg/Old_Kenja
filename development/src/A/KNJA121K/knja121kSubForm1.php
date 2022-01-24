<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knja121kSubForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform1", "POST", "knja121kindex.php", "", "subform1");

        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        $db = Query::dbCheckOut();

        //年度コンボ（通知表所見）
        $opt_year = array();
		$year_flg = false;
        $result = $db->query(knja121kQuery::selectQueryYear($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_year[] = array("label" => $row["YEAR"],"value" => $row["YEAR"]);
            if ($model->exp_year == $row["YEAR"]) $year_flg = true;
            if ($model->year_cmb == "") $model->year_cmb = $model->exp_year;
        }
		if (!$year_flg) $model->year_cmb = $opt_year[0]["value"];

        $objForm->ae( array("type"        => "select",
                            "name"        => "YEAR_CMB",
                            "size"        => "1",
                            "value"       => $model->year_cmb,
                            "options"     => $opt_year,
                            "extrahtml"   => "onChange=\"btn_submit('subform1')\";"
                           ));
        $arg["YEAR_CMB"] = $objForm->ge("YEAR_CMB");


        $query = knja121kQuery::selectQueryGuide($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //所見
            $objForm->ae( array("type"      => "textarea",
                                "name"      => "COMMUNICATION".$row["SEMESTER"],
                                "rows"      => "2",
                                "cols"      => "43",
                                "extrahtml" => "style=\"height:32px;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"",
                                "value"     => $row["COMMUNICATION"]));
            
            $arg["COMMUNICATION".$row["SEMESTER"]] = $objForm->ge("COMMUNICATION".$row["SEMESTER"]);

            //総合的な学習の時間
            $objForm->ae( array("type"      => "textarea",
                                "name"      => "TOTALSTUDYTIME".$row["SEMESTER"],
                                "rows"      => "2",
                                "cols"      => "43",
                                "extrahtml" => "style=\"height:32px;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"",
                                "value"     => $row["TOTALSTUDYTIME"]));
            
            $arg["TOTALSTUDYTIME".$row["SEMESTER"]] = $objForm->ge("TOTALSTUDYTIME".$row["SEMESTER"]);

            $arg["KOUMOKU_S".$row["SEMESTER"]] = $model->control["学期名"][$row["SEMESTER"]]."・特別活動";
            $arg["KOUMOKU_C".$row["SEMESTER"]] = $model->control["学期名"][$row["SEMESTER"]]."・所見";
            $arg["KOUMOKU_T".$row["SEMESTER"]] = "総合的な学習の時間";

            //特別活動
            $objForm->ae( array("type"      => "textarea",
                                "name"      => "SPECIALACTREMARK".$row["SEMESTER"],
                                "rows"      => "2",
                                "cols"      => "43",
                                "extrahtml" => "style=\"height:32px;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"",
                                "value"     => $row["SPECIALACTREMARK"]));
            
            $arg["SPECIALACTREMARK".$row["SEMESTER"]] = $objForm->ge("SPECIALACTREMARK".$row["SEMESTER"]);
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
        View::toHTML($model, "knja121kSubForm1.html", $arg);
    }
}
?>
