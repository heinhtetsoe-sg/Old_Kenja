<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knja120aSubForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform1", "POST", "knja120aindex.php", "", "subform1");

        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        $db = Query::dbCheckOut();
        if ($model->Properties["useTuutisyoSyokenNendo"] == "1") {
            //年度コンボ（通知表所見）
            $opt_year = array();
            $result = $db->query(knja120AQuery::selectQueryYear($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
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
        }

        $field_name_array = array('HREPORTREMARK_DAT__TOTALSTUDYTIME',
                                  'HREPORTREMARK_DAT__SPECIALACTREMARK',
                                  'HREPORTREMARK_DAT__COMMUNICATION',
                                  'HREPORTREMARK_DAT__REMARK1',
                                  'HREPORTREMARK_DAT__REMARK2');
        $query = knja120aQuery::selectQueryGuide($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //通信欄
            $objForm->ae( array("type"      => "textarea",
                                "name"      => "COMMUNICATION",
                                "rows"      => "4",
                                "cols"      => "43",
                                "extrahtml" => "style=\"height:75px;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"",
                                "value"     => $row["COMMUNICATION"]));
            
            $row["COMMUNICATION"] = $objForm->ge("COMMUNICATION");

            //総合的な学習の時間
            $objForm->ae( array("type"      => "textarea",
                                "name"      => "TOTALSTUDYTIME",
                                "rows"      => "4",
                                "cols"      => "43",
                                "extrahtml" => "style=\"height:75px;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"",
                                "value"     => $row["TOTALSTUDYTIME"]));
            
            $row["TOTALSTUDYTIME"] = $objForm->ge("TOTALSTUDYTIME");

            //奉仕
            $objForm->ae( array("type"      => "textarea",
                                "name"      => "SPECIALACTREMARK",
                                "rows"      => "4",
                                "cols"      => "43",
                                "extrahtml" => "style=\"height:75px;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"",
                                "value"     => $row["SPECIALACTREMARK"]));
            
            $row["SPECIALACTREMARK"] = $objForm->ge("SPECIALACTREMARK");

            //備考1
            $objForm->ae( array("type"      => "textarea",
                                "name"      => "REMARK1",
                                "rows"      => "6",
                                "cols"      => "43",
                                "extrahtml" => "style=\"height:90px;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"",
                                "value"     => $row["REMARK1"]));
            
            $row["REMARK1"] = $objForm->ge("REMARK1");

            //備考2
            $objForm->ae( array("type"      => "textarea",
                                "name"      => "REMARK2",
                                "rows"      => "6",
                                "cols"      => "43",
                                "extrahtml" => "style=\"height:90px;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"",
                                "value"     => $row["REMARK2"]));
            
            $row["REMARK2"] = $objForm->ge("REMARK2");

            $sub_arg = array();
            $sub_arg["datas"][] = array("DATA" => $model->control["学期名"][$row["SEMESTER"]]);
            foreach ($model->Properties as $key => $val) {
                if (in_array($key, $field_name_array) && strlen($val)) {
                    list($table_name, $filed_name) = preg_split("/__/", $key);
                    $sub_arg["datas"][] = array("DATA" => $row[$filed_name]);
                }
            }

            $arg["data_array"][] = $sub_arg;
        }
        $colspan_count = 0;
        //項目名表示
        foreach ($model->Properties as $key => $val) {
            if (in_array($key, $field_name_array) && strlen($val)) {
                list($table_name, $filed_name) = preg_split("/__/", $key);
                $arg["LABELS"][] = array("LABEL" => $val);
                $colspan_count++;
            }
        }
        $arg["colspan"] = $colspan_count;

        Query::dbCheckIn($db);
        //終了ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => "戻る",
                            "extrahtml" => "onclick=\"return parent.closeit()\"" ));
        $arg["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja120aSubForm1.html", $arg);
    }
}
?>
