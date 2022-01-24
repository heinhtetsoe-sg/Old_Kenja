<?php

require_once('for_php7.php');

class knjz250Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz250index.php", "", "sel");
        $db             = Query::dbCheckOut();
        $no_year        = 0;
        //年度設定
        $result    = $db->query(knjz250Query::selectYearQuery());
        $opt       = array();

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["YEAR"],
                           "value" => $row["YEAR"]);
            if($row["YEAR"] == $model->year) $no_year = 1;
        }
        if($no_year == 0) $model->year = $opt[0]["value"];

        //証明書種類年度一覧取得
        $result     = $db->query(knjz250Query::selectQuery($model));
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_left[]     = array("label" => $row["CERTIF_KINDCD"]."  ".$row["KINDNAME"],
                                    "value" => $row["CERTIF_KINDCD"]);
            $opt_left_id[] = $row["CERTIF_KINDCD"];
        }
        $opt_right = array();

        //証明書種類一覧取得
        $result = $db->query(knjz250Query::selectcertifQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_right[] = array("label" => $row["CERTIF_KINDCD"]."  ".$row["KINDNAME"],
                                 "value" => $row["CERTIF_KINDCD"]);
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

        //年度証明書
        $objForm->ae( array("type"        => "select",
                            "name"        => "certifyear",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','certifyear','certifmaster',1)\"",
                            "options"     => $opt_left)); 

        //証明書種類
        $objForm->ae( array("type"        => "select",
                            "name"        => "certifmaster",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','certifyear','certifmaster',1)\"",
                            "options"     => $opt_right));  

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','certifyear','certifmaster',1);\"" ) );

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','certifyear','certifmaster',1);\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','certifyear','certifmaster',1);\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','certifyear','certifmaster',1);\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("certifyear"),
                                   "RIGHT_PART"  => $objForm->ge("certifmaster"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

        //証明書マスタボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ250_2/knjz250_2index.php?year_code=".$model->year;
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_master",
                            "value"       => " 証明書マスタ ",
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
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        if ($model->Properties["certif_no_8keta"] == "1") {
            $arg["certif_no_8keta"] = 1;
            //証明書グルーピングボタンを作成する
            $link = REQUESTROOT."/Z/KNJZ250_3/knjz250_3index.php?year_code=".$model->year;
            $objForm->ae( array("type"        => "button",
                                "name"        => "btn_divmst",
                                "value"       => " 証明書グルーピング ",
                                "extrahtml"   => "onclick=\"document.location.href='$link'\"")); 
        }


        if ($model->Properties["certif_no_8keta"] == "1") {
            $arg["button"] = array("BTN_MASTER" =>$objForm->ge("btn_master"),
                                   "BTN_OK"     =>$objForm->ge("btn_keep"),
                                   "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                                   "BTN_DIVMST" =>$objForm->ge("btn_divmst"),
                                   "BTN_END"    =>$objForm->ge("btn_end"));  
        } else {
            $arg["button"] = array("BTN_MASTER" =>$objForm->ge("btn_master"),
                                   "BTN_OK"     =>$objForm->ge("btn_keep"),
                                   "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                                   "BTN_END"    =>$objForm->ge("btn_end"));  
        }
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
                             "LEFT_LIST"  => "証明書種類年度一覧",
                             "RIGHT_LIST" => "証明書種類一覧");

        $arg["TITLE"] = "マスタメンテナンスー証明書種類マスタ";
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz250Form1.html", $arg);
    }
}
?>
