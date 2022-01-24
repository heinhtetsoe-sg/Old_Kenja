<?php

require_once('for_php7.php');

class knjp070kForm4
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjp070kindex.php", "", "sel");
        $arg["jscript"] = "";

        $db = Query::dbCheckOut();

        //年組コンボ
        $opt = array();
        $result = $db->query(knjp070kQuery::GetHrclass());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
               $opt[] = array("label" => $row["HR_NAME"],
                              "value" => $row["GRADE"]."-".$row["HR_CLASS"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "HRCLASS",
                            "size"        => 1,
                            "extrahtml"   => " OnChange=\"btn_submit('all_edit');\"",
                            "value"       => $model->hrclass,
                            "options"     => $opt ));
        $arg["HRCLASS"]  = $objForm->ge("HRCLASS");

        //性別コンボ NO001
        $opt_sex = array();
		$opt_sex[] = array( "label" => "男女",
							"value" => "99");
        $result = $db->query(knjp070kQuery::getNamecd(CTRL_YEAR,"Z002"));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_sex[] = array("label" => $row["NAME1"],
                    	       "value" => $row["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "SEX",
                            "size"        => 1,
                            "extrahtml"   => " OnChange=\"btn_submit('change_sex');\"",
                            "value"       => $model->sex,
                            "options"     => $opt_sex ));
        $arg["SEX"]  = $objForm->ge("SEX");

        if ($model->sex == "") {
            $model->sex = $opt_sex[0]["value"];
        }

        //生徒一覧
        $opt_left = $opt_right = array();
        
        $selectdata = explode(",", $model->selectdata);
        //NO001
        $result   = $db->query(knjp070kQuery::GetStudent($model->hrclass,$model->sex));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["HR_NAME"]."  ".$row["ATTENDNO"]."  ".$row["SCHREGNO"]."  ".$row["NAME_SHOW"], 
                                                         "value" => $row["SCHREGNO"]);

            if (!in_array($row["SCHREGNO"], $selectdata)){
                $opt_right[]  = array("label" => $row["HR_NAME"]."  ".$row["ATTENDNO"]."  ".$row["SCHREGNO"]."  ".$row["NAME_SHOW"], 
                                     "value" => $row["SCHREGNO"]);
            }
        }

        //左リストで選択されたものを再セット
        foreach ($model->select_opt as $key => $val)
        {
            if (in_array($key, $selectdata))
                $opt_left[] = $val;
        }

        if ($model->div == "M") {
            $arg["checked1"] = "checked";

            //中分類
            $opt = array();
            $result = $db->query(knjp070kQuery::GetMcd());
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label" => $row["EXPENSE_L_CD"].$row["EXPENSE_M_CD"]."：".$row["EXPENSE_M_NAME"],
                               "value" => $row["EXPENSE_M_CD"]);
                              
                //小分類存在フラグをセットし、金額テキストの有無効を変更
                $arg["data2"][] = array("idx" => $row["EXPENSE_M_CD"], "flg" => $row["EXPENSE_S_EXIST_FLG"]); 

                //中分類コードごとの金額を設定
                $arg["data3"][] = array("idx" => $row["EXPENSE_M_CD"],  "money" => $row["EXPENSE_M_MONEY"]); 
            }
            $objForm->ae( array("type"        => "select",
                                "name"        => "EXPENSE_M_CD",
                                "size"        => 1,
                                "extrahtml"   => "onChange=\"EnableMoney(), SetMoney1();\"",
                                "value"       => $model->field["EXPENSE_M_CD"],
                                "options"     => $opt ));
            $arg["data"][] = array("TITLE" => "中分類コード",
                                   "ITEM"  => $objForm->ge("EXPENSE_M_CD"));

            //納入必要金額
            $objForm->ae( array("type"        => "text",
                                "name"        => "MONEY_DUE",
                                "size"        => 10,
                                "maxlength"   => 8,
                                "extrahtml"   => " style=\"text-align:right\" onBlur=\"this.value=toInteger(this.value);\"",
                                "value"       => $model->field["MONEY_DUE"] ));
            $arg["data"][] = array("TITLE" => "納入必要金額",
                                   "ITEM"  => $objForm->ge("MONEY_DUE"));

            //減免事由
            $opt = array();
            $opt[0] = array("label" => "", "value" => "");
            $result = $db->query(knjp070kQuery::nameGet());
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                   $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"],
                                  "value" => $row["NAMECD2"]);
            }
            $objForm->ae( array("type"        => "select",
                                "name"        => "REDUCTION_REASON",
                                "size"        => 1,
                                "value"       => $model->field["REDUCTION_REASON"],
                                "options"     => $opt ));
            $arg["data"][] = array("TITLE" => "減免事由",
                                   "ITEM"  => $objForm->ge("REDUCTION_REASON"));

            $arg["num"] = 1;

        } else {
            $arg["checked2"] = "checked";

            //小分類
            $opt = array();
            $result = $db->query(knjp070kQuery::GetScd2());
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label" => $row["EXPENSE_L_CD"].$row["EXPENSE_M_CD"].$row["EXPENSE_S_CD"]."：".$row["EXPENSE_S_NAME"],
                               "value" => $row["EXPENSE_S_CD"].$row["EXPENSE_M_CD"]);

                //小分類コードごとの金額を設定
                $arg["data3"][] = array("idx" => $row["EXPENSE_S_CD"].$row["EXPENSE_M_CD"],  "money" => $row["EXPENSE_S_MONEY"]); 
            }
            $objForm->ae( array("type"        => "select",
                                "name"        => "EXPENSE_S_CD",
                                "size"        => 1,
                                "extrahtml"   => "onChange=\"SetMoney2();\"",
                                "value"       => $model->field["EXPENSE_S_CD"],
                                "options"     => $opt ));
            $arg["data"][] = array("TITLE" => "小分類コード",
                                   "ITEM"  => $objForm->ge("EXPENSE_S_CD"));

            //納入必要金額
            $objForm->ae( array("type"        => "text",
                                "name"        => "MONEY_DUE",
                                "size"        => 10,
                                "maxlength"   => 8,
                                "extrahtml"   => " style=\"text-align:right\" onBlur=\"this.value=toInteger(this.value);\"",
                                "value"       => $model->field["MONEY_DUE"] ));
            $arg["data"][] = array("TITLE" => "納入必要金額",
                                   "ITEM"  => $objForm->ge("MONEY_DUE"));

            $arg["num"] = 2;
        }
        Query::dbCheckIn($db);

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return doSubmit('all_add')\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit('all_update')\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_delete",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return doSubmit('all_delete')\"" ) );
        //戻るボタン
        $link = "knjp070kindex.php?cmd=back&radiodiv=".$model->div;
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"window.open('$link','_self');\"" ) );
                    
        $arg["BUTTONS"] = $objForm->ge("btn_add").$objForm->ge("btn_update").$objForm->ge("btn_delete").$objForm->ge("btn_back");

        //対象生徒
        $objForm->ae( array("type"        => "select",
                            "name"        => "left_select",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','left_select','right_select',1)\" ",
                            "options"     => $opt_left));                    
        //その他の生徒
        $objForm->ae( array("type"        => "select",
                            "name"        => "right_select",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','left_select','right_select',1)\" ",
                            "options"     => $opt_right));  
                    
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','left_select','right_select',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','left_select','right_select',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','left_select','right_select',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','left_select','right_select',1);\"" ) ); 
                            
        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("left_select"),
                                   "RIGHT_PART"  => $objForm->ge("right_select"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));                        

        $arg["info"] = array("TOP"        =>  sprintf("%d年度  %s  対象クラス  %s",
                                                CTRL_YEAR,$model->control_data["学期名"][CTRL_SEMESTER],$hr_name),
                             "LEFT_LIST"  => "対象者一覧",
                             "RIGHT_LIST" => "生徒一覧");
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );  
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata") );  
        
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp070kForm4.html", $arg); 
    }
}
?>

