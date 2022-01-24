<?php

require_once('for_php7.php');

class knjz310_2Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz310_2index.php", "", "sel");
        $db = Query::dbCheckOut();

        //年度コンボボックスを作成する NO001

        $query = knjz310_2Query::selectYearQuery($model);
        $result = $db->query($query);
        $opt_year = array();
        $yearflg = false;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_year[] = array("label" => $row["YEAR"], "value" => $row["YEAR"]);
            if ($model->year == $row["YEAR"]){
                $yearflg = true;
            }
        }
        //NO002
//      if ($yearflg == false){
//          $model->year = $opt_year[0]["value"];
//      }
        if ($yearflg == false){
            $opt_year[] = array("label" => $model->year, "value" => $model->year);
        }
        $result->free();

        //グループ設定
        $result = $db->query(knjz310_2Query::selectGroupQuery($model));   
        $opt = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => $row["GROUPCD"]."  ".$row["GROUPNAME"], "value" => $row["GROUPCD"]);
        }
        //グループに所属する一覧取得
        $result = $db->query(knjz310_2Query::selectQuery($model));   
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            $schKindStr = getStaffSchKindStr($model, $db, $row["STAFFCD"]);
            $opt_left[] = array("label" => $row["STAFFCD"]."  ".$row["STAFFNAME_SHOW"]." ".$schKindStr, "value" => $row["STAFFCD"]);
            $opt_left_id[] = $row["STAFFCD"];
        }
        $opt_right = array();
        //グループに所属しない一覧取得
        if (is_array($opt_left_id)){
            $result = $db->query(knjz310_2Query::selectNoGroupQuery($opt_left_id,$model));   
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $schKindStr = getStaffSchKindStr($model, $db, $row["STAFFCD"]);
                $opt_right[] = array("label" => $row["STAFFCD"]."  ".$row["STAFFNAME_SHOW"]." ".$schKindStr, "value" => $row["STAFFCD"]);
            }
        }

        $result->free();

        $arg["year"] = array( "VAL"       => $objForm->ge("group"),
                              "BUTTON"    => $objForm->ge("btn_def"));
        if ($model->GROUPCD=="") {
            $opt_left = array();
            $opt_right = array();   
        }

        Query::dbCheckIn($db);

        //年度コンボボックスを作成する NO001
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
        $link = REQUESTROOT."/Z/KNJZ310/knjz310index.php";
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "戻る",
                            "extrahtml"   => "onclick=\"document.location.href='$link'\""));

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
                            "name"      => "GROUPCD",
                            "value"     => $model->GROUPCD ) ); 

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GROUPNAME",
                            "value"     => $model->GROUPNAME ) );                                         

        $arg["info"] = array("TOP"        => "グループ :".$model->GROUPNAME,
                             "LEFT_LIST"  => "所属しているメンバー",
                             "RIGHT_LIST" => "所属していないメンバー");
        $arg["finish"]  = $objForm->get_finish();

        $arg["TITLE"] = "所属グループマスタ";
        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz310_2Form1.html", $arg); 
    }
}

//担当校種の文字列生成
function getStaffSchKindStr($model, $db, $staffcd) {
    $staffFields = $db->getRow(knjz310_2Query::getStfDetailFields($model, $staffcd));
    $schKindStr = "";
    //担当校種の文字列を生成
    if (strlen($staffFields[0])) {
        $tmpArr = array();
        foreach($staffFields as $val) {
            if (!strlen($val)) continue;
             $tmpArr[] = $db->getOne(knjz310_2Query::getSchKindAbbv($model, $val));
        }
        $schKindStr = "(".implode(",", $tmpArr).")";
    }
    return $schKindStr;
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
