<?php

require_once('for_php7.php');

class knjz260Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz260index.php", "", "sel");
        $db = Query::dbCheckOut();

        //年度設定
        $result = $db->query(knjz260Query::selectYearQuery());
        $opt = array();
        $j=0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => $row["YEAR"], "value" => $row["YEAR"]);
            if ($model->year==$row["YEAR"]) $j++;
        }
        if ($j==0) {
            $model->year = $opt[0]["value"];
        }

        //年度科目一覧取得
        $result = $db->query(knjz260Query::selectQuery($model));   
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_left[] = array("label" => $row["DUTYSHARECD"]."　".$row["SHARENAME"], "value" => $row["DUTYSHARECD"]);
            $opt_left_id[] = $row["DUTYSHARECD"];
        }
        $opt_right = array();

        //科目一覧取得
        $result = $db->query(knjz260Query::selectdutyshareQuery($opt_left_id,$model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_right[] = array("label" => $row["DUTYSHARECD"]."　".$row["SHARENAME"], "value" => $row["DUTYSHARECD"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        //年度コンボボックスを作成する
        $objForm->ae( array("type"        => "select",
                            "name"        => "year",
                            "size"        => "1",
                            "value"       => $model->year,
                            "extrahtml"   => "onchange=\"return btn_submit('btn_def');\"",
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


        //年度科目
        $objForm->ae( array("type"        => "select",
                            "name"        => "shareyear",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','shareyear','sharemaster',1)\"",
                            "options"     => $opt_left)); 
                    
        //科目マスタ
        $objForm->ae( array("type"        => "select",
                            "name"        => "sharemaster",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','shareyear','sharemaster',1)\"",
                            "options"     => $opt_right));
                    
        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','shareyear','sharemaster',1);\"" ) );

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','shareyear','sharemaster',1);\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','shareyear','sharemaster',1);\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','shareyear','sharemaster',1);\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("shareyear"),
                                   "RIGHT_PART"  => $objForm->ge("sharemaster"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));                    
                                      
        //分掌マスタボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ260_2/knjz260_2index.php?year_code=".$model->year;
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_master",
                            "value"       => " 分掌マスタ ",
                            "extrahtml"   => "onclick=\"document.location.href='$link'\"")); 

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
        //                    "extrahtml"   => "onclick=\"return btn_submit('end');\"" ) ); 
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
                    
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "temp_year"
                            ) );  

        $arg["info"] = array("TOP"        => "対象年度",
                             "LEFT_LIST"  => "校務分掌年度一覧",
                             "RIGHT_LIST" => "校務分掌一覧");
        $arg["finish"]  = $objForm->get_finish();


        $arg["TITLE"] = "マスタメンテナンスー校務分掌マスタ";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, TMPLDIRECTORY."/sel.html", $arg); 
    }
}
?>
