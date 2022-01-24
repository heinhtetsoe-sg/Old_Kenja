<?php

require_once('for_php7.php');

class knjz400jForm1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz400jindex.php", "", "sel");
        $db = Query::dbCheckOut();
        //年度設定
        $result = $db->query(knjz400jQuery::selectYearQuery());
        $opt = array();
        $j=0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => $row["YEAR"], "value" => $row["YEAR"]);
            if ($model->year==$row["YEAR"]) $j++;
        }
        if ($j==0) {
            $model->year = $opt[0]["value"];
        }
        //年度観点一覧取得
        $result = $db->query(knjz400jQuery::selectQuery($model));
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_left[] = array("label" => $row["VIEWCD"]."  ".$row["VIEWNAME"], "value" => $row["VIEWCD"]);
            $opt_left_id[] = $row["VIEWCD"];
        }
        $opt_right = array();
        //観点一覧取得
        $result = $db->query(knjz400jQuery::selectSubclassQuery($opt_left_id,$model));   
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_right[] = array("label" => $row["VIEWCD"]."  ".$row["VIEWNAME"], "value" => $row["VIEWCD"]);
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

        //年度観点
        $objForm->ae( array("type"        => "select",
                            "name"        => "subclassyear",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','subclassyear','subclassmaster',1)\"",
                            "options"     => $opt_left)); 

        //観点マスタ
        $objForm->ae( array("type"        => "select",
                            "name"        => "subclassmaster",
                            "size"        => "20",
                            "value"       => "right",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','subclassyear','subclassmaster',1)\"",
                            "options"     => $opt_right));  

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','subclassyear','subclassmaster',1);\"" ) );

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','subclassyear','subclassmaster',1);\"" ) );

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','subclassyear','subclassmaster',1);\"" ) );

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','subclassyear','subclassmaster',1);\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("subclassyear"),
                                   "RIGHT_PART"  => $objForm->ge("subclassmaster"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));                    
                                      
        //観点マスタボタン
        $link = REQUESTROOT."/Z/KNJZ400J_2/knjz400j_2index.php?year_code=".$model->year;
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_master",
                            "value"       => " 観点マスタ ",
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
                            "name"      => "cmd"
                            ) );  

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata"
                            ) );  

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "temp_year"
                            ) );  

        $arg["info"] = array("TOP"        => "対象年度",
                             "LEFT_LIST"  => "年度観点一覧",
                             "RIGHT_LIST" => "観点一覧");
        $arg["finish"]  = $objForm->get_finish();

        $arg["TITLE"] = "マスタメンテナンスー観点マスタ";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, TMPLDIRECTORY."/sel.html", $arg); 
    }
}
?>
