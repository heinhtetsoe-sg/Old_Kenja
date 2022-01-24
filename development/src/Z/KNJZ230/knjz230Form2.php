<?php

require_once('for_php7.php');

class knjz230Form2
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz230index.php", "", "sel");

        //db open
        $db         = Query::dbCheckOut();

        //リスト選択データ取得
        $ChosenData = array();
        $ChosenData = $db->getRow(knjz230Query::getChosenData($model->chaircd),DB_FETCHMODE_ASSOC);

        //リストボックス値初期化
        $host_cd = $opt_left = $opt_right = array();

        //合併後科目コンボボックス設定
        $opt    = array();
        $result = $db->query(knjz230Query::getSub_Cls_Mst($model->subclasscd));

        //合併後科目コンボボックスオプション作成
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"], 
                           "value" => $row["SUBCLASSCD"]);     
        }

        //合併後科目コンボボックスの初期値
        if($ChosenData["GRADINGCLASSCD"] == ""){
           $ChosenData["GRADINGCLASSCD"] = ($ChosenData["SUBCLASSCD"] != "" )? $ChosenData["SUBCLASSCD"] : $opt[0]["value"];
        }

        //合併後科目コンボボックス
        $objForm->ae( array("type"      => "select",
                            "name"      => "CMB_SUB_CLS",
                            "size"      => "1",
                            "extrahtml" => "",
                            "value"     => $ChosenData["GRADINGCLASSCD"],
                            "options"   => $opt ));

        //一覧取得
        if (isset($model->chaircd))
        {
            $data = common::GetMasterData("SELECT TRGTGRADE,TRGTCLASS FROM CHAIR_CLS_DAT WHERE YEAR = '".CTRL_YEAR."' AND SEMESTER = '".CTRL_SEMESTER."' AND CHAIRCD = '".$model->chaircd."'");
            //講座一覧
            $result  = $db->query(knjz230Query::getChair_dat($model->subclasscd,$data["TRGTGRADE"],$data["TRGTCLASS"]));

            if(!isset($ChosenData["CHAIRCD"])) $ChosenData["CHAIRCD"] = "";
            if(!isset($ChosenData["CHAIRNAME"])) $ChosenData["CHAIRNAME"] = "";
            
            $host_cd = array("label" => $ChosenData["CHAIRCD"]."  ".$ChosenData["CHAIRNAME"],
                             "value" => $ChosenData["CHAIRCD"]);

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if ($model->chaircd != $row["CHAIRCD"])
                {
                    $opt_right[] = array("label" => $row["CHAIRCD"]."  ".$row["CHAIRNAME"],
                                         "value" => $row["CHAIRCD"]);
                }
            }
            //合併対象講座一覧
            $result    = $db->query(knjz230Query::getMergeChair_dat($model->chaircd));
            //$opt_left[] = ($host_cd != "")? $host_cd : array() ;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if ($model->chaircd != $row["CHAIRCD"])
                {
                    $opt_left[] = array("label" => $row["CHAIRCD"]."　".$row["CHAIRNAME"],
                                        "value" => $row["CHAIRCD"]);
                }
            }
        }

        //ヘッダー作成
        $TOP  = "&nbsp;&nbsp;合併先講座&nbsp;&nbsp;：&nbsp;&nbsp;"
                 .$ChosenData["CHAIRCD"]."&nbsp;&nbsp;".$ChosenData["CHAIRNAME"];
        $TOP .= "";
        $TOP .= "<HR>&nbsp;&nbsp;合併後の科目：&nbsp;&nbsp;".$objForm->ge("CMB_SUB_CLS");

        $arg["info"]    = array("TOP"        => $TOP,
                                "LEFT_LIST"  => "合併対象講座一覧",
                                "RIGHT_LIST" => "講座一覧");
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

        $arg["SEL_WIDTH"] = "80%";

        //yearに値を入れないと上記変数arg["info"]がでない。
        $arg["year"]["VAL"] = " ";

        $arg["TITLE"]   = "マスタメンテナンス - 評価科目設定マスタ";
        $arg["finish"]  = $objForm->get_finish();

        //更新できたら左のリストを再読込
        if (isset($model->message)) {
            $arg["reload"] = "window.open('knjz230index.php?cmd=list', 'left_frame');";
        }else{
            $arg["reload"] = "";
        }

        $arg["jscript"] = $arg["show_confirm"].$arg["reload"];
        View::toHTML($model, TMPLDIRECTORY."/sel.html", $arg); 
    }
}
?>
