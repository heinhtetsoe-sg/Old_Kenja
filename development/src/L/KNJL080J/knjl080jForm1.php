<?php

require_once('for_php7.php');

class knjl080jForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //手続日付
        $value = ($model->pro_date != "") ? $model->pro_date : str_replace("-","/",CTRL_DATE);
        $arg["TOP"]["PRO_DATE"] = View::popUpCalendar2($objForm, "PRO_DATE", $value, "", "", "");

        //対象者コンボ
        $opt = array();
        $opt[] = array("label" => "1：一次手続者" , "value" => "1");
        $opt[] = array("label" => "2：二次手続者" , "value" => "2");
        if (!strlen($model->appli_type)) $model->appli_type = "1";
        $objForm->ae( array("type"       => "select",
                            "name"       => "APPLI_TYPE",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"",
                            "value"      => $model->appli_type,
                            "options"    => $opt));
        $arg["TOP"]["APPLI_TYPE"] = $objForm->ge("APPLI_TYPE");

        //入試区分
        $opt = array();
        $opt_limit_date = array();
        $result = $db->query(knjl080jQuery::GetName("L004",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_limit_date[$row["NAMECD2"]] = ($model->appli_type == "1") ? $row["NAMESPARE3"] : $row["ABBV3"];
            $opt[]  = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            if ($model->testdiv=="" && $row["NAMESPARE2"]=='1') $model->testdiv = $row["NAMECD2"];
        }
        if (!strlen($model->testdiv)) $model->testdiv = $opt[0]["value"];
        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTDIV",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"",
                            "value"      => $model->testdiv,
                            "options"    => $opt));
        $arg["TOP"]["TESTDIV"] = $objForm->ge("TESTDIV");

        //リストタイトル用
        $left_title  = array("1" => "一次手続者一覧" , "2" => "二次手続者一覧");
        $right_title = array("1" => "合格者一覧（辞退者は除く）" , "2" => "合格者一覧（辞退者は除く）");
        $arg["LEFT_TITLE"]  = $left_title[$model->appli_type];
        $arg["RIGHT_TITLE"] = $right_title[$model->appli_type];

        //対象者・合格者一覧
        $opt_left = $opt_right = array();
        $result = $db->query(knjl080jQuery::GetLeftList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $row["PRODATE"] = str_replace("-","/",$row["PRODATE"]);
            if ($row["DIV"] == "1") {
                //対象者一覧
                $opt_left[]  = array("label" => $row["EXAMNO"]."：".$row["PRODATE1"].$row["NAME"]."：".$row["PRODATE"], "value" => $row["EXAMNO"]);
            } else {
                //合格者一覧
                $opt_right[] = array("label" => $row["EXAMNO"]."：".$row["PRODATE1"].$row["NAME"]."：".$row["PRODATE"], "value" => $row["EXAMNO"]);
            }
        }
        //対象者一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "SPECIALS",
                            "size"        => "30",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('right','SPECIALS','APPROVED',1);\"",
                            "options"     => $opt_left));
        //合格者一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "APPROVED",
                            "size"        => "30",
                            "value"       => "right",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('left','SPECIALS','APPROVED',1);\"",
                            "options"     => $opt_right));

        $result->free();
        Query::dbCheckIn($db);

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move3('sel_add_all','SPECIALS','APPROVED',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move3('left','SPECIALS','APPROVED',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move3('right','SPECIALS','APPROVED',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move3('sel_del_all','SPECIALS','APPROVED',1);\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("SPECIALS"),
                                   "RIGHT_PART"  => $objForm->ge("APPROVED"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

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

        //一括辞退ボタン
        $dis_exec = str_replace("-","/",CTRL_DATE) < $opt_limit_date[$model->testdiv] ? "disabled" : "";
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_exec",
                            "value"       => "一括辞退",
                            "extrahtml"   => $dis_exec ." onclick=\"return btn_submit('exec');\"" ) );

        $arg["button"] = array("BTN_EXEC"   =>$objForm->ge("btn_exec"),
                               "BTN_OK"     =>$objForm->ge("btn_keep"),
                               "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                               "BTN_END"    =>$objForm->ge("btn_end"));  

        //一次・二次手続締切日
        $one_two = ($model->appli_type == "1") ? "一次手続締切日：" : "二次手続締切日：";
        $arg["LIMIT_DATE"] = $one_two .$opt_limit_date[$model->testdiv];

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
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjl080jindex.php", "", "sel");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl080jForm1.html", $arg); 
    }
}
?>
