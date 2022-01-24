<?php

require_once('for_php7.php');

class knjz160Form1
{
    function main(&$model)
    {
        $arg["jscript"] = "";
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
    
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz160index.php", "", "sel");
        $db             = Query::dbCheckOut();
        $no_year        = 0;
        //年度設定
        $result    = $db->query(knjz160Query::selectYearQuery());   
        $opt       = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["YEAR"], 
                          "value" => $row["YEAR"]);
            if ($row["YEAR"] == $model->year)
               $no_year = 1;
        }
        if ($no_year == 0)
            $model->year = $opt[0]["value"];

        //年度科目一覧取得
        $result      = $db->query(knjz160Query::selectQuery($model));   
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_left[]    = array("label" => $row["FACCD"]."　".$row["FACILITYNAME"], 
                                   "value" => $row["FACCD"]);
            $opt_left_id[] = $row["FACCD"];
        }
        $opt_right = array();
    
        //科目一覧取得
        $result = $db->query(knjz160Query::selectFacQuery($opt_left_id,$model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_right[] = array("label" => $row["FACCD"]."　".$row["FACILITYNAME"], 
                                 "value" => $row["FACCD"]);
        }

        $result->free();
        Query::dbCheckIn($db);
    
        //年度コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "year",
                            "size"        => "1",
                            "value"       => $model->year,
                            "extrahtml"   => "onchange=\"return btn_submit('');\"",
                            "options"     => $opt));    

        $objForm->ae( array("type"        => "text",
                            "name"        => "year_add",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => "" )); 
                    
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_year_add",
                            "value"       => "年度追加",
                            "extrahtml"   => "onclick=\"return add('');\"" ));                           
                                          
        $arg["year"] = array( "VAL"       => $objForm->ge("year")."&nbsp;&nbsp;".
                                             $objForm->ge("year_add")."&nbsp;".$objForm->ge("btn_year_add"));
        //施設年度
        $objForm->ae( array("type"        => "select",
                            "name"        => "fac_year",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\" ",
                            "options"     => $opt_left)); 
                    
        //施設マスタ
        $objForm->ae( array("type"        => "select",
                            "name"        => "fac_master",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"",
                            "options"     => $opt_right));  
                    
        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','fac_year','fac_master',1);\"" ) );

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','fac_year','fac_master',1);\"" ) );

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','fac_year','fac_master',1);\"" ) );

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','fac_year','fac_master',1);\"" ) ); 
                                        
        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("fac_year"),
                                   "RIGHT_PART"  => $objForm->ge("fac_master"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));                    
                                      
        //施設マスタボタン
        $link = REQUESTROOT."/Z/KNJZ160_2/knjz160_2index.php?mode=1";

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_master",
                            "value"       => " 施設マスタ ",
                            "extrahtml"   => "onclick=\"document.location.href='$link'\"") ); 

        //保存ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更新",
                            "extrahtml"   => "onclick=\"return doSubmit();\"" ) );

        //取消ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear",
                            "value"       => "取消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"] = array("BTN_MASTER" =>$objForm->ge("btn_master"),
                               "BTN_OK"     =>$objForm->ge("btn_keep"),
                               "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                               "BTN_END"    =>$objForm->ge("btn_end"));  

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));  

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata"));  

        $arg["info"]    = array("TOP"        => "対象年度",
                                "LEFT_LIST"  => "施設年度一覧",
                                "RIGHT_LIST" => "施設一覧");
        
        $arg["year"]["BUTTON"] = "";
        $arg["TITLE"]   = "マスタメンテナンス - 施設マスタ";
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, TMPLDIRECTORY."/sel.html", $arg); 
    }
}
?>
