<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：一覧表示がされるよう修正                 山城 2004/11/17 */
/********************************************************************/

class knjh210Form1 {
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjh210index.php", "", "sel");
        $db             = Query::dbCheckOut();
        $no_year        = 0;
        //年度設定
        $result    = $db->query(knjh210Query::selectYearQuery());   
        $opt       = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["YEAR"], 
                           "value" => $row["YEAR"]);
            if ($row["YEAR"] == $model->year)
                $no_year = 1;
        }
        if ($no_year==0) $model->year = $opt[0]["value"];

        //寮年度一覧取得
        $opt_left = array();
        $result      = $db->query(knjh210Query::selectQuery($model));   
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[]    = array("label" => $row["DOMI_CD"]."　".$row["DOMI_NAME"], 
                                   "value" => $row["DOMI_CD"]);
        }
        $opt_right = array();

        //寮一覧取得
        $result = $db->query(knjh210Query::selectClassQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array("label" => $row["DOMI_CD"]."　".$row["DOMI_NAME"], 
                                 "value" => $row["DOMI_CD"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        //年度コンボボックスを作成する
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
                             )); 
                        
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_year_add",
                            "value"       => "年度追加",
                            "extrahtml"   => "onclick=\"return add('');\"" ));                           
                                              
        $arg["year"] = array( "VAL"       => $objForm->ge("year")."&nbsp;&nbsp;".
                                             $objForm->ge("year_add")."&nbsp;".$objForm->ge("btn_year_add"));
        //寮年度
        $objForm->ae( array("type"        => "select",
                            "name"        => "classyear",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','classyear','classmaster','value')\"",
                            "options"     => $opt_left)); 
                        
        //寮マスタ
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
                                          
        //寮マスタボタンを作成する
        $link  = REQUESTROOT."/H/KNJH210_2/knjh210_2index.php?mode=1";

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_master",
                            "value"       => " 寮マスタ ",
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
                                "LEFT_LIST"  => "寮年度一覧",
                                "RIGHT_LIST" => "寮一覧");

        $arg["TITLE"]   = "マスタメンテナンス - 寮マスタ";
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
