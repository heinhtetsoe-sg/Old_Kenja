<?php

require_once('for_php7.php');

class knjmp984_2Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        //$arg["start"]   = $objForm->get_start("sel", "POST", "knjmp984_2index.php", "", "sel");
        $arg["start"] = $objForm->get_start("knjmp984_2Form1", "POST", "knjmp984_2index.php", "", "knjmp984_2Form1");

        $db = Query::dbCheckOut();
        
        //年度取得
        $query = knjmp984_2Query::selectYearQuery($model);
        $result = $db->query($query);
        $opt_year = array();
        $yearflg = false;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year[] = array("label" => $row["YEAR"], 
                                "value" => $row["YEAR"]);
            if ($model->year == $row["YEAR"]) {
                $yearflg = true;
            }
        }
        if ($yearflg == false){
            $opt_year[] = array("label" => $model->year, 
                                "value" => $model->year);
        }
        $result->free();

        //グループ設定
        $result = $db->query(knjmp984_2Query::selectGroupQuery());   
        $opt = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => $row["LEVY_GROUP_CD"]."  ".$row["LEVY_GROUP_NAME"], "value" => $row["LEVY_GROUP_CD"]);
        }
        //グループに所属する一覧取得
        $result = $db->query(knjmp984_2Query::selectQuery($model));   
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_left[] = array("label" => $row["LEVY_L_CD"]."  ".$row["LEVY_L_NAME"], "value" => $row["LEVY_L_CD"]);
            $opt_left_id[] = $row["LEVY_L_CD"];
        }
        $opt_right = array();
        //グループに所属しない一覧取得
        if (is_array($opt_left_id)){
            $result = $db->query(knjmp984_2Query::selectNoGroupQuery($opt_left_id,$model));   
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_right[] = array("label" => $row["LEVY_L_CD"]."  ".$row["LEVY_L_NAME"], "value" => $row["LEVY_L_CD"]);
            }
        }

        $result->free();

        $arg["year"] = array( "VAL"       => $objForm->ge("group"),
                              "BUTTON"    => $objForm->ge("btn_def"));
        if ($model->groupcd=="") {
            $opt_left = array();
            $opt_right = array();
        }

        Query::dbCheckIn($db);

        //年度コンボボックスを作成する
        $objForm->ae( array("type"        => "select",
                            "name"        => "year",
                            "size"        => "1",
                            "extrahtml"   => "onchange=\"return btn_submit('');\"",
                            "value"       => $model->year,
                            "options"     => $opt_year));

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

        //グループに所属する
        $objForm->ae( array("type"        => "select",
                            "name"        => "isGroup",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','isGroup','noGroup',1)\"",
                            "options"     => $opt_left)); 
                    
        //グループに所属しない
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

        //更新ボタン
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
                            

        //終了ボタンを作成する
        $link = REQUESTROOT."/M/KNJMP984/knjmp984index.php";
        $extra = "onclick=\"document.location.href='$link'\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "LEVY_GROUP_CD", $model->groupcd);
        knjCreateHidden($objForm, "LEVY_GROUP_NAME", $model->groupname);

        $arg["info"] = array("TOP"        => $model->groupcd.':'.$model->groupname,
                             "LEFT_LIST"  => "設定済み会計科目一覧",
                             "RIGHT_LIST" => "グループ未設定の会計科目一覧");
        $arg["finish"]  = $objForm->get_finish();

        $arg["TITLE"] = "予算グループマスタ";
        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjmp984_2Form1.html", $arg);
    }
}
?>
