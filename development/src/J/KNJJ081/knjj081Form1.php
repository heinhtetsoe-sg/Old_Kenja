<?php

require_once('for_php7.php');

class knjj081Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjj081Form1", "POST", "knjj081index.php", "", "knjj081Form1");

        $db = Query::dbCheckOut();

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
        }
        $query = knjj081Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('knjj081Form1');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);

        //委員会
        $opt_committeecd = array();
        $query = knjj081Query::getCommiMst($model, CTRL_YEAR);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_committeecd[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        }
        $result->free();
        if ($model->field["COMMITTEECD"] == "") $model->field["COMMITTEECD"] = $opt_committeecd[0]["value"];
        $extra = "onchange=\"return btn_submit('knjj081Form1'),AllClearList();\"";
        $arg["data"]["COMMITTEECD"] = knjCreateCombo($objForm, "COMMITTEECD", $model->field["COMMITTEECD"], $opt_committeecd, $extra, 1);

        //委員会顧問一覧
        $result = $db->query(knjj081Query::selectQuery($model));   
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_left[] = array("label" => $row["STAFFCD"]."  ".$row["STAFFNAME_SHOW"], "value" => $row["STAFFCD"]);
            $opt_left_id[] = $row["STAFFCD"];
        }
        $opt_right = array();

        //職員一覧
        if (is_array($opt_left_id)){
            $result = $db->query(knjj081Query::selectNoGroupQuery($opt_left_id,$model));   
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_right[] = array("label" => $row["STAFFCD"]."  ".$row["STAFFNAME_SHOW"], "value" => $row["STAFFCD"]);
            }
        }

        $result->free();

        //年度設定
        $result    = $db->query(knjj081Query::selectYearQuery());   
        $opt       = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["YEAR"], 
                           "value" => $row["YEAR"]);
            if ($row["YEAR"] == $model->year)
                $no_year = 1;
        }
        if ($no_year==0) $model->year = $opt[0]["value"];
        
        //年度コンボボックスを作成する
        $objForm->ae( array("type"        => "select",
                            "name"        => "YEAR",
                            "size"        => "1",
                            "value"       => $model->year,
                            "extrahtml"   => "onchange=\"return btn_submit('');\"",
                            "options"     => $opt));    

                            
        $objForm->ae( array("type"        => "text",
                            "name"        => "year_add",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value);\"",
                             )); 
                        
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_year_add",
                            "value"       => "年度追加",
                            "extrahtml"   => "onclick=\"return add('');\"" ));                           
                                              
        $arg["year"] = array( "VAL"       => $objForm->ge("YEAR")."&nbsp;&nbsp;".
                                             $objForm->ge("year_add")."&nbsp;".$objForm->ge("btn_year_add"));


        //職員一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "isGroup",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','isGroup','noGroup',1)\"",
                            "options"     => $opt_left)); 
        //職員年度
        $objForm->ae( array("type"        => "select",
                            "name"        => "noGroup",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','isGroup','noGroup',1)\"",
                            "options"     => $opt_right));  
        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"return move('sel_add_all','isGroup','noGroup',1);\"" ) );
        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"return move('left','isGroup','noGroup',1);\"" ) );
        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"return move('right','isGroup','noGroup',1);\"" ) );
        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"return move('sel_del_all','isGroup','noGroup',1);\"" ) ); 
                                        
        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("isGroup"),
                                   "RIGHT_PART"  => $objForm->ge("noGroup"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));                    

        //保存ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit();\"" ) );
        //取消ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );
        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"] = array("BTN_OK"     =>$objForm->ge("btn_keep"),
                               "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                               "BTN_END"    =>$objForm->ge("btn_end"));  
                               
        $arg["info"]    = array("TOP"        => "対象年度：");
        
        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        //年度
        $arg["data"]["YEAR"] = $model->year;
        
        //学期
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjj081Form1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
