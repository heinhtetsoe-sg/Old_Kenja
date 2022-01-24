<?php

require_once('for_php7.php');

class knjz231Form2
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz231index.php", "", "sel");

        //db open
        $db         = Query::dbCheckOut();

        //リスト選択データ取得
        $ChosenData = array();
        $ChosenData = $db->getRow(knjz231Query::getChosenData($model->chaircd),DB_FETCHMODE_ASSOC);

        //リストボックス値初期化
        $host_cd = $opt_left = $opt_right = array();

        //一覧取得
        if (isset($model->chaircd))
        {
            //科目一覧
            $result  = $db->query(knjz231Query::getChair_dat($model->chaircd));

            if(!isset($ChosenData["CHAIRCD"])) $ChosenData["CHAIRCD"] = "";
            if(!isset($ChosenData["CHAIRNAME"])) $ChosenData["CHAIRNAME"] = "";
            
            $host_cd = array("label" => $ChosenData["CHAIRCD"]."  ".$ChosenData["CHAIRNAME"],
                             "value" => $ChosenData["CHAIRCD"]);

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if ($model->chaircd != $row["CHAIRCD"])
                {
                    $opt_right[] = array("label" => $row["SUBCLASSCD"]."  ".$row["SUBCLASSNAME"],
                                         "value" => $row["SUBCLASSCD"]);
                }
            }
            //読替/分割科目一覧
            $result    = $db->query(knjz231Query::getChairReplace_dat($model->chaircd));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if ($model->chaircd != $row["CHAIRCD"])
                {
                    $opt_left[] = array("label" => $row["SUBCLASSCD"]."  ".$row["SUBCLASSNAME"],
                                        "value" => $row["SUBCLASSCD"]);
                }
            }
        }

        //ヘッダー作成
        $model->count != "" ? $count = $model->count : $count = "";
        if($model->chaircd == "FALSE")$count = "";
        $TOP  = "&nbsp;対象講座&nbsp;&nbsp;：&nbsp;&nbsp;".$ChosenData["CHAIRCD"]."&nbsp;&nbsp;".$ChosenData["CHAIRNAME"];
        $TOP .= "&nbsp;&nbsp;&nbsp;&nbsp;講座選択数&nbsp;&nbsp;：&nbsp;&nbsp;<span id =\"chair\">".$count."</span>";
        $arg["info"]    = array("TOP"        => $TOP,
                                "LEFT_LIST"  => "読替/分割科目一覧",
                                "RIGHT_LIST" => "科目一覧");
        //DB CLOSE
        $result->free();
        Query::dbCheckIn($db);

        //評価科目リスト
        $objForm->ae(array("type"      => "select",
                           "name"      => "leftitem",
                           "size"      => "20",
                           "value"     => "left",
                           "extrahtml" => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','leftitem','rightitem',1)\"",
                           "options"   => $opt_left));

        //科目マスタリスト
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

        $arg["button"] = array("BTN_OK"     =>$objForm->ge("btn_keep"),
                               "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                               "BTN_END"    =>$objForm->ge("btn_end"));  

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata" ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CHAIRCODE",
                            "value"     => $model->chaircode) );

        $arg["SEL_WIDTH"] = "80%";

        //yearに値を入れないと上記変数arg["info"]がでない。
        $arg["year"]["VAL"] = " ";

        $arg["TITLE"]   = "マスタメンテナンス - 評価科目設定マスタ";
        $arg["finish"]  = $objForm->get_finish();

        //更新できたら左のリストを再読込
        if (isset($model->message)) {
            $arg["reload"] = "window.open('knjz231index.php?cmd=list', 'left_frame');";
        }else{
            $arg["reload"] = "";
        }

        $arg["jscript"] = $arg["show_confirm"].$arg["reload"];
        View::toHTML($model, TMPLDIRECTORY."/sel.html", $arg); 
    }
}
?>
