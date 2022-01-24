<?php

require_once('for_php7.php');


class sel
{
    function main(&$model){

        //権限チェック
        if(AUTHORITY != DEF_UPDATABLE) $arg["jscript"] = "OnAuthError();" ;

        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz280index.php", "", "sel");
        $db             = Query::dbCheckOut();
        $no_year        = 0;

        //年度設定
        $opt       = array();
        $result    = $db->query(knjz280Query::selectYearQuery());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["YEAR"],
                           "value" => $row["YEAR"]);
            if ($row["YEAR"] == $model->year)
                $no_year = 1;
        }
        if ($no_year == 0)
            $model->year = $opt[0]["value"];

        //年度職名一覧取得
        $result      = $db->query(knjz280Query::selectQuery($model));
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_left[]    = array("label" => $row["JOBCD"]."　".$row["JOBNAME"],
                                   "value" => $row["JOBCD"]);
            $opt_left_id[] = $row["JOBCD"];
        }
        $opt_right = array();

        //職名一覧取得
        $result = $db->query(knjz280Query::selectJobQuery($opt_left_id,$model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_right[] = array("label" => $row["JOBCD"]."　".$row["JOBNAME"],
                                     "value" => $row["JOBCD"]);
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
                            "value"       => "" ));

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_year_add",
                            "value"       => "年度追加",
                            "extrahtml"   => "onclick=\"return add('');\"" ));                           
                                          
        $arg["year"] = array( "VAL"       => $objForm->ge("year")."&nbsp;&nbsp;".
                                             $objForm->ge("year_add")."&nbsp;".$objForm->ge("btn_year_add"));

        //職名年度
        $objForm->ae( array("type"        => "select",
                            "name"        => "classyear",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"",
                            "options"     => $opt_left)); 

        //職名マスタ
        $objForm->ae( array("type"        => "select",
                            "name"        => "classmaster",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"",
                            "options"     => $opt_right));  

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all');\"" ) );

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left');\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right');\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all');\"" ) ); 

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("classyear"),
                                   "RIGHT_PART"  => $objForm->ge("classmaster"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));                    

        //職名マスタボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ280_2/knjz280_2index.php?mode=1";

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_master",
                            "value"       => " 職名マスタ ",
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

        $arg["info"]    = array("TOP"        => "対象年度",
                                "LEFT_LIST"  => "職名年度一覧",
                                "RIGHT_LIST" => "職名一覧");
    
        $arg["TITLE"]   = "マスタメンテナンス - 職名マスタ";
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, TMPLDIRECTORY."/sel.html", $arg); 
    }
}
?>
