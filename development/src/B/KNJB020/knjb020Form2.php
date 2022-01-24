<?php

require_once('for_php7.php');

class knjb020Form2
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjb020index.php", "", "sel");
        $db         = Query::dbCheckOut();

        //リスト選択データ取得
        $ChosenData = array();
        $ChosenData = $db->getRow(knjb020Query::getChosenData($model->chaircd),DB_FETCHMODE_ASSOC);

        //リストボックス値初期化
        $tmp = $opt_left = $opt_right = array();

        //一覧取得
        if ($model->chaircd!="")
        {
            //担当職員
            $result = $db->query(knjb020Query::getStaff($model->chaircd));

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt_left[] = array("label" => $row["STAFFCD"]."  ".$row["STAFFNAME"],
                                    "value" => $row["STAFFCD"]);
                $tmp[] = $row["STAFFCD"];
            }

             //職員一覧
            $result  = $db->query(knjb020Query::getStaffMst());

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if (!in_array($row["STAFFCD"],$tmp)) { 
                    $opt_right[] = array("label" => $row["STAFFCD"]."  ".$row["STAFFNAME"],
                                         "value" => $row["STAFFCD"]);
                }
            }
        }

        //ヘッダー作成
        $TOP  = "&nbsp;&nbsp;講座&nbsp;&nbsp;：&nbsp;&nbsp;"
                 .$ChosenData["CHAIRCD"]."&nbsp;&nbsp;".$ChosenData["CHAIRNAME"];

        $arg["info"]    = array("TOP"        => $TOP,
                                "LEFT_LIST"  => "担当職員",
                                "RIGHT_LIST" => "職員一覧");

        Query::dbCheckIn($db);

        $objForm->ae(array("type"      => "select",
                           "name"      => "leftitem",
                           "size"      => "20",
                           "value"     => "left",
                           "extrahtml" => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','leftitem','rightitem',1)\"",
                           "options"   => $opt_left));

        $objForm->ae(array("type"      => "select",
                           "name"      => "rightitem",
                           "size"      => "20",
                           "value"     => "left",
                           "extrahtml" => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','leftitem','rightitem')\"",
                           "options"   => $opt_right));  

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','leftitem','rightitem');\"" ) );

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','leftitem','rightitem');\"" ) );

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','leftitem','rightitem',1);\"" ) );

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','leftitem','rightitem');\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("leftitem"),
                                   "RIGHT_PART"  => $objForm->ge("rightitem"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

        //更新ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更 新",
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

        $msg = "<BR><BR><BR><font color=\"red\">※担当職員の先頭に位置する職員がメインの担当者になります。<BR>
                　一時的に出欠入力を他の職員が行う場合は担当職員の一覧に<BR>
                　職員一覧より追加し、出欠入力後に再度、担当一覧より削除<BR>
                　して下さい。</font>";        
       
        $arg["button"] = array("BTN_MASTER" => $objForm->ge("btn_keep"),
                               "BTN_OK"     => $objForm->ge("btn_clear"),
                               "BTN_CLEAR"  => $objForm->ge("btn_end"),
                               "BTN_END"    => $msg);  

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata" ) );

        $arg["SEL_WIDTH"] = "100%";

        //yearに値を入れないと上記変数$arg["info"]がでない。
        $arg["year"]["VAL"] = " ";

        $arg["TITLE"]   = "";
        $arg["finish"]  = $objForm->get_finish();

        //更新できたら左のリストを再読込
#        if (isset($model->message)) {
#            $arg["reload"] = "window.open('knjb020index.php?cmd=list', 'left_frame');";
#        }else{
#            $arg["reload"] = "";
#        }

        $arg["jscript"] = $arg["reload"];
        View::toHTML($model, TMPLDIRECTORY."/sel.html", $arg); 
    }
}
?>
