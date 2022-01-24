<?php

require_once('for_php7.php');

class knjl120kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $result = $db->query(knjl120kQuery::GetName("L003",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[]  = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
        }

        if (!strlen($model->testdiv)){
            $model->testdiv = $opt[0]["value"];
        }

#        //リストタイトル用
#205.12.30 minei        $right_title = array("1" => "合格者一覧", "2" => "手続者一覧");

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTDIV",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"",
                            "value"      => $model->testdiv,
                            "options"    => $opt));
        $arg["TOP"]["TESTDIV"] = $objForm->ge("TESTDIV");

        //専併区分
        $opt = array();
        $result = $db->query(knjl120kQuery::GetName("L006",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[]  = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
        }

        if (!strlen($model->shdiv)){
            $model->shdiv = $opt[0]["value"];
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "SHDIV",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"",
                            "value"      => $model->shdiv,
                            "options"    => $opt));
        $arg["TOP"]["SHDIV"] = $objForm->ge("SHDIV");

        $arg["LEFT_TITLE"]  = "入学辞退者一覧";
        $arg["CENTER_TITLE"]= "手続者一覧（入学者一覧）";
        $arg["RIGHT_TITLE"] = "合格者一覧（手続辞退者一覧）";

        //対象者コンボ
        $opt = array();
        //2005.12.29 minei 合格者の選択肢追加 & valueの値をCSV取込みの区分と同様に変更。
#        $opt[0] = array("label" => "1：手続者(入学者)", "value" => "1");
#        $opt[1] = array("label" => "2：入学辞退者"    , "value" => "2");
        $opt[0] = array("label" => "1:合格者一覧(手続辞退者一覧)", "value" => "1");
        $opt[1] = array("label" => "2:手続者一覧(入学者一覧)"    , "value" => "2");
        $opt[2] = array("label" => "3:入学辞退者一覧"            , "value" => "3");


        if (!strlen($model->appli_type))
            $model->appli_type = "2"; //デフォルト：合格者一覧(手続辞退者一覧)

        $objForm->ae( array("type"       => "select",
                            "name"       => "APPLI_TYPE",
                            "size"       => "1",
                            "extrahtml"  => "onChange=\"change_apptype()\"",
                            "value"      => $model->appli_type,
                            "options"    => $opt));
        $arg["TOP"]["APPLI_TYPE"] = $objForm->ge("APPLI_TYPE");

        $result = $db->query(knjl120kQuery::GetList($model));

        $opt_right = array();
        $opt_center = array();
        $opt_left = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $a = array("label" => $row["EXAMNO"]."：".htmlspecialchars($row["NAME"]), "value" => $row["EXAMNO"]);
            if ($row["PROCEDUREDIV"] == 2 && $row["ENTDIV"] == 1 ){ //手続き済み入学無しまたは未設定
                $opt_left[] = $a;
            }else if ($row["PROCEDUREDIV"] == 2 && $row["ENTDIV"] == 2){ //手続き済み
                $opt_center[] = $a;
            }else{
                $opt_right[] = $a;
            }
            
        }
        //辞退者
        $objForm->ae( array("type"        => "select",
                            "name"        => "LEFTLIST",
                            "size"        => "30",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('right','LEFTLIST','CENTERLIST',1);\"",
                            "options"     => $opt_left));

        //手続者
        $objForm->ae( array("type"        => "select",
                            "name"        => "CENTERLIST",
                            "size"        => "30",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\"",
                            "options"     => $opt_center));

        //合格者一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "RIGHTLIST",
                            "size"        => "30",
                            "value"       => "right",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('left','CENTERLIST','RIGHTLIST',1);\"",
                            "options"     => $opt_right));

        $result->free();
        Query::dbCheckIn($db);

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move3('sel_add_all','LEFTLIST','CENTERLIST',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move3('left','LEFTLIST','CENTERLIST',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move3('right','LEFTLIST','CENTERLIST',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move3('sel_del_all','LEFTLIST','CENTERLIST',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all2",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move3('sel_add_all','CENTERLIST','RIGHTLIST',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add2",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move3('left','CENTERLIST','RIGHTLIST',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del2",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move3('right','CENTERLIST','RIGHTLIST',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all2",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move3('sel_del_all','CENTERLIST','RIGHTLIST',1);\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("LEFTLIST"),
                                   "CENTER_PART"  => $objForm->ge("CENTERLIST"),
                                   "RIGHT_PART"  => $objForm->ge("RIGHTLIST"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"),
                                   "SEL_ADD_ALL2" => $objForm->ge("sel_add_all2"),
                                   "SEL_ADD2"     => $objForm->ge("sel_add2"),
                                   "SEL_DEL2"     => $objForm->ge("sel_del2"),
                                   "SEL_DEL_ALL2" => $objForm->ge("sel_del_all2")
                                   
                                   );

        //CSV用
        $objForm->ae( array("type"        => "file",
                            "name"        => "csvfile",
                            "size"        => "409600") );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_csv",
                            "value"       => "実 行",
                            "extrahtml"   => "onclick=\"return btn_submit('csv');\"" ) );

        $objForm->ae( array("type"        => "checkbox",
                            "name"        => "chk_header",
                            "extrahtml"   => "checked",
                            "value"       => "1" ) );
        $arg["CSV_ITEM"] = $objForm->ge("csvfile").$objForm->ge("btn_csv").$objForm->ge("chk_header");

        //保存ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit();\"" ) );

        //取消ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"] = array("BTN_OK"     =>$objForm->ge("btn_keep"),
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
                            "name"      => "selectdata2"
                            ) );  

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata3"
                            ) );  
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjl120kindex.php", "", "sel");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl120kForm1.html", $arg); 
    }
}
?>
