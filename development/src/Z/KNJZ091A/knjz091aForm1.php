<?php

require_once('for_php7.php');

class knjz091aForm1
{
    function main(&$model)
    {
    //権限チェック
    if (AUTHORITY != DEF_UPDATABLE){
        $arg["jscript"] = "OnAuthError();";
    }

    $objForm = new form;

    //フォーム作成
    $arg["start"]   = $objForm->get_start("sel", "POST", "knjz091aindex.php", "", "sel");
    $db             = Query::dbCheckOut();
    $no_year        = 0;

    //年度コンボ設定
    $result    = $db->query(knjz091aQuery::selectYearQuery());
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


    //出身塾年度一覧取得
    $result      = $db->query(knjz091aQuery::selectQuery($model));   
    $opt_left_id = $opt_left = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt_left[]    = array("label" => $row["PRISCHOOLCD"]."  ".$row["PRISCHOOL_NAME"], 
                               "value" => $row["PRISCHOOLCD"]);
        $opt_left_id[] = $row["PRISCHOOLCD"];
    }
    $opt_right = array();

    //出身塾一覧取得
    $result = $db->query(knjz091aQuery::selectJuniorQuery($opt_left_id,$model));
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt_right[] = array("label" => $row["PRISCHOOLCD"]."  ".$row["PRISCHOOL_NAME"], 
                             "value" => $row["PRISCHOOLCD"]);
    }

    $result->free();
    Query::dbCheckIn($db);

    //年度コンボボックスを作成する
    $objForm->ae( array("type"        => "select",
                        "name"        => "year",
                        "size"        => "1",
                        "extrahtml"   => "onchange=\"return btn_submit('');\"",
                        "value"       => $model->year,
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
                                          
    $arg["year"] = array( "VAL"       => $objForm->ge("year")."&nbsp;&nbsp;".
                                         $objForm->ge("year_add")."&nbsp;".$objForm->ge("btn_year_add"));

    //出身塾年度
    $objForm->ae( array("type"        => "select",
                        "name"        => "finschoolyear",
                        "size"        => "20",
                        "value"       => "left",
                        "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','finschoolyear','finschoolmaster',1)\"",
                        "options"     => $opt_left)); 

    //出身塾マスタ
    $objForm->ae( array("type"        => "select",
                        "name"        => "finschoolmaster",
                        "size"        => "20",
                        "value"       => "left",
                        "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','finschoolyear','finschoolmaster',1)\"",
                        "options"     => $opt_right));  

    //追加ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_add_all",
                        "value"       => "≪",
                        "extrahtml"   => "onclick=\"return move('sel_add_all','finschoolyear','finschoolmaster',1);\"" ) );

    //追加ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_add",
                        "value"       => "＜",
                        "extrahtml"   => "onclick=\"return move('left','finschoolyear','finschoolmaster',1);\"" ) );

    //削除ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_del",
                        "value"       => "＞",
                        "extrahtml"   => "onclick=\"return move('right','finschoolyear','finschoolmaster',1);\"" ) );

    //削除ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_del_all",
                        "value"       => "≫",
                        "extrahtml"   => "onclick=\"return move('sel_del_all','finschoolyear','finschoolmaster',1);\"" ) ); 
                                        
    $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("finschoolyear"),
                               "RIGHT_PART"  => $objForm->ge("finschoolmaster"),
                               "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                               "SEL_ADD"     => $objForm->ge("sel_add"),
                               "SEL_DEL"     => $objForm->ge("sel_del"),
                               "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));                    
                                      
    //出身塾マスタボタンを作成する
    $link = REQUESTROOT."/Z/KNJZ091A_2/knjz091a_2index.php?mode=1";

    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_master",
                        "value"       => "出身塾マスタ",
                        "extrahtml"   => "onclick=\"document.location.href='$link'\"") ); 

    //保存ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_keep",
                        "value"       => "更新",
                        "extrahtml"   => "onclick=\"return doSubmit();\"" ) );

    //取消ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_clear",
                        "value"       => "取消",
                        "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );

    //終了ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_end",
                        "value"       => "終了",
                        "extrahtml"   => "onclick=\"closeWin();\"" ) );

    $arg["button"] = array("BTN_MASTER" =>$objForm->ge("btn_master"),
                           "BTN_OK"     =>$objForm->ge("btn_keep"),
                           "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                           "BTN_END"    =>$objForm->ge("btn_end"));  

    //hiddenを作成する
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );  

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "selectdata"
                        ) );  

    $arg["info"]    = array("TOP"        => "対象年度",
                            "LEFT_LIST"  => "出身塾年度一覧",
                            "RIGHT_LIST" => "出身塾一覧");

    $arg["TITLE"]   = "マスタメンテナンス - 出身塾マスタ";
    $arg["finish"]  = $objForm->get_finish();

    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, TMPLDIRECTORY."/sel.html", $arg); 
    }
}
?>
