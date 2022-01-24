<?php

require_once('for_php7.php');

class knjz300_2Form1
{
    function main(&$model)
    {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz300_2index.php", "", "sel");
        $db = Query::dbCheckOut();
        
        //ユーザー名取得
        $result = $db->query(knjz300_2Query::selectNameQuery($model->userscd, $model->post_year));
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        $user_name = $row["STAFFNAME"];

        //利用者が所有するグループ一覧取得
        $result = $db->query(knjz300_2Query::selectQuery($model));
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_left[] = array("label" => $row["GROUPCD"]."　".$row["GROUPNAME"], "value" => $row["GROUPCD"]);
            $opt_left_id[] = $row["GROUPCD"];
        }
        $opt_right = array();

       //利用者が所有しないグループ一覧取得
           $result = $db->query(knjz300_2Query::selectNoGroupQuery($model));
           while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
               $opt_right[] = array("label" => $row["GROUPCD"]."　".$row["GROUPNAME"], "value" => $row["GROUPCD"]);
           }

        $result->free();
        Query::dbCheckIn($db);

        //グループに所属する
        $objForm->ae( array("type"        => "select",
                            "name"        => "isGroup",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','isGroup','noGroup',1);\"",
                            "options"     => $opt_left));

        //グループに所属しない
        $objForm->ae( array("type"        => "select",
                            "name"        => "noGroup",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','isGroup','noGroup',1);\"",
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
                            "extrahtml"   => " onclick=\"return move('left','isGroup','noGroup',1);\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => " onclick=\"return move('right','isGroup','noGroup',1);\"" ) );

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
                            "value"       => "更 新",
                            "extrahtml"   => " onclick=\"return doSubmit();\"" ) );

        //取消ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear",
                            "value"       => "取 消",
                            "extrahtml"   => " onclick=\"return btn_submit('clear');\"" ) );

        //戻るボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ300/knjz300index.php";
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"document.location.href='$link?year=".$model->post_year."&USERSCD=".$model->userscd."'\""));

        $arg["button"] = array("BTN_OK"     =>$objForm->ge("btn_keep"),
                               "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                               "BTN_END"    =>$objForm->ge("btn_end"),
                               "BTN_MASTER" => "");  

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

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "USERSCD",
                            "value"     => $model->userscd ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => $model->post_year ) );


        $arg["info"] = array("TOP"        => "対象年度 :".$model->post_year."      利用者 :".$user_name,
                             "LEFT_LIST"  => "所属しているグループ",
                             "RIGHT_LIST" => "所属していないグループ");
        $arg["finish"]  = $objForm->get_finish();

        $arg["TITLE"] = "利用者グループ登録";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz300_2Form1.html", $arg); 
    }
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
