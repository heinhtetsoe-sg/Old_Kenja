<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：一覧表示されない為、表示処理修正         山城 2004/11/17 */
/* ･NO002：委員会コード表示を変更                   山城 2004/11/26 */
/********************************************************************/

class knjj080Form1
{
    function main(&$model)
    {

    $objForm = new form;

    //フォーム作成
    $arg["start"]   = $objForm->get_start("sel", "POST", "knjj080index.php", "", "sel");
    $db             = Query::dbCheckOut();
    $no_year        = 0;

    //校種コンボ
    if ($model->Properties["use_prg_schoolkind"] == "1") {
        $arg["schkind"] = "1";
        $query = knjj080Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('sel');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->schKind, $extra, 1);
    }

    //年度設定
    $result    = $db->query(knjj080Query::selectYearQuery($model));
    $opt       = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt[] = array("label" => $row["YEAR"], 
                       "value" => $row["YEAR"]);
        if ($row["YEAR"] == $model->year)
            $no_year = 1;
    }
    if ($no_year==0) $model->year = $opt[0]["value"];

    //委員会年度一覧取得
    $result      = $db->query(knjj080Query::selectQuery($model));
    $opt_left_id = $opt_left = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt_left[]    = array("label" => $row["COMMITTEE_FLG"]."-".$row["COMMITTEECD"]."　".$row["COMMITTEENAME"], /* NO002 */
                               "value" => $row["COMMITTEE_FLG"].$row["COMMITTEECD"]);
        $opt_left_id[] = $row["COMMITTEE_FLG"].$row["COMMITTEECD"];
    }
    $opt_right = array();
    
    //委員会一覧取得
    $result = $db->query(knjj080Query::selectClassQuery($opt_left_id,$model));   /* NO001↓ */
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt_right[] = array("label" => $row["COMMITTEE_FLG"]."-".$row["COMMITTEECD"]."　".$row["COMMITTEENAME"], /* NO002 */
                             "value" => $row["COMMITTEE_FLG"].$row["COMMITTEECD"]);
    }   /* NO001↑ */

    $result->free();
    Query::dbCheckIn($db);
    
    //年度コンボボックスを作成する
    $objForm->ae( array("type"        => "select",
                        "name"        => "year",
                        "size"        => "1",
                        "value"       => $model->year,
                        "extrahtml"   => "onchange=\"return btn_submit('');\"",
                        "options"     => $opt));    

                        
    //年度追加テキストボックスを作成する
    $objForm->ae( array("type"        => "text",
                        "name"        => "year_add",
                        "size"        => 5,
                        "maxlength"   => 4,
                        "extrahtml"   => "onblur=\"this.value=toInteger(this.value);\"",
                        "value"       => $year_add )); 
                    
    //年度追加ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_year_add",
                        "value"       => "年度追加",
                        "extrahtml"   => "onclick=\"return add('');\"" ));                           
                                          
    $arg["year"] = array( "VAL"       => $objForm->ge("year")."&nbsp;".$objForm->ge("btn_def")."&nbsp;&nbsp;&nbsp;".
                                         $objForm->ge("year_add")."&nbsp;".$objForm->ge("btn_year_add"));

    //委員会年度
    $objForm->ae( array("type"        => "select",
                        "name"        => "classyear",
                        "size"        => "20",
                        "value"       => "left",
                        "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','classyear','classmaster','value')\"",
                        "options"     => $opt_left)); 
                    
    //委員会マスタ
    $objForm->ae( array("type"        => "select",
                        "name"        => "classmaster",
                        "size"        => "20",
                        "value"       => "right",
                        "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','classyear','classmaster','value')\"",
                        "options"     => $opt_right));  
                    
    //追加ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_add_all",
                        "value"       => "≪",
                        "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"return move('sel_add_all','classyear','classmaster','value');\"" ) );

    //追加ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_add",
                        "value"       => "＜",
                        "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"return move('left','classyear','classmaster','value');\"" ) );

    //削除ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_del",
                        "value"       => "＞",
                        "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"return move('right','classyear','classmaster','value');\"" ) );

    //削除ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_del_all",
                        "value"       => "≫",
                        "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"return move('sel_del_all','classyear','classmaster','value');\"" ) );

    $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("classyear"),
                               "RIGHT_PART"  => $objForm->ge("classmaster"),
                               "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                               "SEL_ADD"     => $objForm->ge("sel_add"),
                               "SEL_DEL"     => $objForm->ge("sel_del"),
                               "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));                    


    //委員会マスタボタンを作成する
    $link  = REQUESTROOT."/J/KNJJ080_2/knjj080_2index.php?mode=1";
    $link .= "&SEND_selectSchoolKind=".$model->selectSchoolKind;

    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_master",
                        "value"       => " 委員会マスタ ",
                        "extrahtml"   => "onclick=\"document.location.href='$link'\"") ); 

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

    $arg["info"]    = array("TOP"        => "対象年度　",
                            "LEFT_LIST"  => "委員会年度一覧",
                            "RIGHT_LIST" => "委員会一覧");
    
    $arg["TITLE"]   = "マスタメンテナンス - 委員会マスタ";
    $arg["finish"]  = $objForm->get_finish();

    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "sel.html", $arg); 
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
