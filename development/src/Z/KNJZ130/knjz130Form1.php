<?php

require_once('for_php7.php');

class knjz130Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz130index.php", "", "sel");
        $db = Query::dbCheckOut();
        //年度設定
        $result = $db->query(knjz130Query::selectYearQuery());   
        $opt = array();
        $j=0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => $row["YEAR"], "value" => $row["YEAR"]);
            if ($model->year==$row["YEAR"]) $j++;
        }
        if ($j==0) {
        	$model->year = $opt[0]["value"];
        }
        //名称年度一覧取得
        $result = $db->query(knjz130Query::selectQuery($model));   
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_left[] = array("label" => $row["NAMECD1"]."　".$row["NAMECD2"]."　".$row["NAME1"], "value" => $row["NAMECD1"].":".$row["NAMECD2"]);
            $opt_left_id[] = $row["NAMECD1"].$row["NAMECD2"];
        }
        $opt_right = array();
        $result = $db->query(knjz130Query::selectnameQuery($opt_left_id,$model));   
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_right[] = array("label" => $row["NAMECD1"]."　".$row["NAMECD2"]."　".$row["NAME1"], "value" => $row["NAMECD1"].":".$row["NAMECD2"]);
        }

        $result->free();
        Query::dbCheckIn($db);
        $year_add = "";
        
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
                            "value"       => $year_add )); 
                    
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_year_add",
                            "value"       => "年度追加",
                            "extrahtml"   => "onclick=\"return add('');\"" ));                           
                                          
        $arg["year"] = array( "VAL"       => $objForm->ge("year")."&nbsp;&nbsp;".
                                             $objForm->ge("year_add")."&nbsp;".$objForm->ge("btn_year_add"));

        //名称年度
        $objForm->ae( array("type"        => "select",
                            "name"        => "nameyear",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"return move('right','nameyear','namemaster',1);\"",
                            "options"     => $opt_left)); 
        //名称
        $objForm->ae( array("type"        => "select",
                            "name"        => "namemaster",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"return move('left','nameyear','namemaster',1);\"",
                            "options"     => $opt_right));  
                    
        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','nameyear','namemaster',1);\"" ) );

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\" return move('left','nameyear','namemaster',1);\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\" return move('right','nameyear','namemaster',1);\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\" return move('sel_del_all','nameyear','namemaster',1);\"" ) ); 
                                        
        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("nameyear"),
                                   "RIGHT_PART"  => $objForm->ge("namemaster"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));                    
                                      
        //証明書マスタボタンを作成する
        //$link = REQUESTROOT."/LZ/LZB070_2/index.php?year_code=".$model->year;
        $link = REQUESTROOT."/Z/KNJZ130_2/knjz130_2index.php";
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_master",
                            "value"       => " 名称マスタ ",
                            "extrahtml"   => "onclick=\"document.location.href='$link'\"")); 

/*
        //教科科目名称マスタのマスタ画面のボタン表示設定
        //詳細登録ボタン
        if ($model->Properties["hyoujiClassDetailDat"] == 1) {
            $arg["hyoujiClassDetailDat"] = '1';
        }
        $link = REQUESTROOT."/Z/KNJZ070_3/knjz070_3index.php?mode=1&SEND_YEAR=$model->year&SEND_PRGID=KNJZ130&SEND_AUTH=".AUTHORITY."";

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_shousai",
                            "value"       => " 詳細登録 ",
                            "extrahtml"   => "onclick=\"document.location.href='$link'\"") );
*/

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
                               "BTN_END"    =>$objForm->ge("btn_end"),  
                               "BTN_SHOUSAI" =>$objForm->ge("btn_shousai"));

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );  

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata"
                            ) );                      

        $arg["info"] = array("TOP"        => "対象年度",
                             "LEFT_LIST"  => "名称年度一覧",
                             "RIGHT_LIST" => "名称一覧");
        $arg["finish"]  = $objForm->get_finish();


        $arg["TITLE"] = "マスタメンテナンスー名称マスタ";

        //ローカルのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "sel.html", $arg); 
    }
}    
?>
