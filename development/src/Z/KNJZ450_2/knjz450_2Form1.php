<?php

require_once('for_php7.php');

class knjz450_2Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz450_2index.php", "", "sel");
        $db = Query::dbCheckOut();

        //グループ設定
        //選択した級・段位
        $result = $db->query(knjz450_2Query::selectQuery($model));   
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_left[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
            $opt_left_id[] = $row["VALUE"];
        }
        $opt_right = array();
        //級・段位一覧
        if (is_array($opt_left_id)){
            $result = $db->query(knjz450_2Query::selectNoGroupQuery($opt_left_id,$model));   
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_right[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
            }
        }
        $result->free();

        $query = knjz450_2Query::getSyscatColumns();
        if (0 < $db->getOne($query)) {
            $model->setKey = array();

            $query = knjz450_2Query::getList($model, $opt_left_id);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->setKey[] = $row["RANK"];

                $extra = ($row["NOT_PRINT"] == "1") ? "checked" : "";
                $row["NOT_PRINT"] = knjCreateCheckBox($objForm, "NOT_PRINT-".$row["RANK"] , "1", $extra);

                $extra = "";
                $row["SCORE"] = knjCreateTextBox($objForm, $row["SCORE"], "SCORE-".$row["RANK"], 4, 4, $extra);

                $arg["data"][] = $row;
            }
        }

        if ($model->QUALIFIED_CD=="") {
            $opt_left = array();
            $opt_right = array();   
        }

        Query::dbCheckIn($db);

        $arg['year'] = array('VAL'=>'');
        
        //選択した級・段位
        $objForm->ae( array("type"        => "select",
                            "name"        => "isGroup",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','isGroup','noGroup',1)\"",
                            "options"     => $opt_left)); 
                    
        //級・段位一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "noGroup",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','isGroup','noGroup',1)\"",
                            "options"     => $opt_right));  
                    
        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','isGroup','noGroup',1);\"" ) );

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','isGroup','noGroup',1);\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','isGroup','noGroup',1);\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','isGroup','noGroup',1);\"" ) ); 
                                        
        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("isGroup"),
                                   "RIGHT_PART"  => $objForm->ge("noGroup"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));                    

        //保存ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更新",
                            "extrahtml"   => "onclick=\"return doSubmit();\"" ) );

        //保存ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更新",
                            "extrahtml"   => "onclick=\"return btn_submit('update2');\"" ) );

        //取消ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear",
                            "value"       => "取消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );

        //終了ボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ450/knjz450index.php";
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "戻る",
                            "extrahtml"   => "onclick=\"document.location.href='$link'\""));

        $arg["button"] = array("BTN_OK"     =>$objForm->ge("btn_keep"),
                               "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                               "BTN_END"    =>$objForm->ge("btn_end"),
                               "BTN_UPDATE" =>$objForm->ge("btn_update"));

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );  

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata"
                            ) );  
                    
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "QUALIFIED_CD",
                            "value"     => $model->QUALIFIED_CD ) ); 

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "QUALIFIED_NAME",
                            "value"     => $model->QUALIFIED_NAME ) );                                         

        $arg["info"] = array("TOP"        => "資格名 :".$model->QUALIFIED_NAME,
                             "LEFT_LIST"  => "選択した級・段位",
                             "RIGHT_LIST" => "級・段位一覧");
        $arg["finish"]  = $objForm->get_finish();

        $arg["TITLE"] = "所属グループマスタ";
        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz450_2Form1.html", $arg);
    }
}
?>
