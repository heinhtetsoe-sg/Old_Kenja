<?php

require_once('for_php7.php');

class knjd644Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd644Form1", "POST", "knjd644index.php", "", "knjd644Form1");

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //学部コンボ
        $db = Query::dbCheckOut();
        $result      = $db->query(knjd644Query::getBucd());
        $opt_bu       = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_bu[]= array("label"        => $row["BU_CD"]." ".$row["BU_NAME"],   
                             "value"        => $row["BU_CD"]);
        }

        if(!isset($model->field["BU_CD"])) {
            $model->field["BU_CD"] = $opt_bu[0]["value"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "BU_CD",
                            "size"       => "1",
                            "value"      => $model->field["BU_CD"],
                            "extrahtml"  => "onchange=\"return btn_submit('knjd644'),AllClearList();\"",
                            "options"    => $opt_bu));

        $arg["data"]["BU_CD"] = $objForm->ge("BU_CD");

        //学科コンボ
        $result      = $db->query(knjd644Query::getKacd($model));
        $opt_ka       = array();
        $opt_ka[]= array("label" => "",  "value" => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_ka[]= array("label"        => $row["KA_CD"]." ".$row["KA_NAME"],   
                             "value"        => $row["KA_CD"]);
        }

        if(!isset($model->field["KA_CD"])) {
            $model->field["KA_CD"] = $opt_ka[1]["value"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "KA_CD",
                            "size"       => "1",
                            "value"      => $model->field["KA_CD"],
                            "extrahtml"  => "onchange=\"return btn_submit('knjd644'),AllClearList();\"",
                            "options"    => $opt_ka));

        $arg["data"]["KA_CD"] = $objForm->ge("KA_CD");

        //希望順位コンボ
        $result      = $db->query(knjd644Query::getWishrank($model));
        $opt_wish       = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_wish[]= array("label"      => $row["WISH_RANK"]."位",  
                             "value"        => $row["WISH_RANK"]);
        }

        if(!isset($model->field["WISH_RANK"])) {
            $model->field["WISH_RANK"] = $opt_wish[0]["value"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "WISH_RANK",
                            "size"       => "1",
                            "value"      => $model->field["WISH_RANK"],
                            "extrahtml"  => "onchange=\"return btn_submit('knjd644'),AllClearList();\"",
                            "options"    => $opt_wish));

        $arg["data"]["WISH_RANK"] = $objForm->ge("WISH_RANK");
        $arg["data"]["WISH_RANK2"] = $model->field["WISH_RANK"]."位";

        //推薦枠
        $arg["FRAME"] = $db->getOne(knjd644Query::getRecomFrame($model));

        //決定者数
        $arg["DECISION"] = $db->getOne(knjd644Query::getRecomDecis($model));

        //候補人数数
        $arg["CANDIDATE"] = $db->getOne(knjd644Query::getRecomCand($model));

        //決定者一覧取得
        $result = $db->query(knjd644Query::selectStdDecision($model));   
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_left[] = array("label" => $row["HR_NAME"].$row["ATTENDNO"]."番　".$row["NAME"]." ".$row["SCORE3"],
                                "value" => $row["SCHREGNO"]);
            $opt_left_id[] = $row["SCHREGNO"];
        }
        $opt_right = array();

        //候補者一覧取得
        $result = $db->query(knjd644Query::selectStdCandidate($model));   
        $opt_right = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_right[] = array("label" => $row["HR_NAME"].$row["ATTENDNO"]."番　".$row["NAME"]." ".$row["SCORE3"],
                                 "value" => $row["SCHREGNO"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        //決定者一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "CATEGORY_SELECTED",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','CATEGORY_SELECTED','CATEGORY_NAME',1)\"",
                            "options"     => $opt_left)); 
                    
        //候補者一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "CATEGORY_NAME",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','CATEGORY_SELECTED','CATEGORY_NAME',1)\"",
                            "options"     => $opt_right));  
                    
        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"return move('sel_add_all','CATEGORY_SELECTED','CATEGORY_NAME',1);\"" ) );

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"return move('left','CATEGORY_SELECTED','CATEGORY_NAME',1);\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"return move('right','CATEGORY_SELECTED','CATEGORY_NAME',1);\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"return move('sel_del_all','CATEGORY_SELECTED','CATEGORY_NAME',1);\"" ) ); 
                                        
        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("CATEGORY_SELECTED"),
                                   "RIGHT_PART"  => $objForm->ge("CATEGORY_NAME"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));                    

        //保存ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit();\"" ) );

        //取消ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"] = array("BTN_OK"     =>$objForm->ge("btn_keep"),
                               "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                               "BTN_END"    =>$objForm->ge("btn_end"));  

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );  

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata"
                            ) );  

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => CTRL_YEAR
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEMESTER",
                            "value"     => CTRL_SEMESTER,
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd644Form1.html", $arg); 
    }
}
?>
