<?php

require_once('for_php7.php');


class knjf035Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjf035Form1", "POST", "knjf035index.php", "", "knjf035Form1");

        //年度テキストボックスを作成する

        $arg["data"]["YEAR"] = $model->control["年度"];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"      => $model->control["年度"],
                            ) );

        //学期テキストボックスを作成する

        $arg["data"]["GAKKI"] = $model->control["学期名"][$model->control["学期"]];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"      => $model->control["学期"],
                            ) );

        //対象者リスト
        $db = Query::dbCheckOut();

        $query = knjf035Query::getHrClassAuth(CTRL_YEAR,CTRL_SEMESTER,AUTHORITY,STAFFCD, $model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        $disable = 0;

        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple style=\"width:230px\" ondblclick=\"move1('left',$disable)\"",  //NO001
                            "size"       => "20",
                            "options"    => isset($opt1)?$opt1:array()));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

        //生徒一覧リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple style=\"width:230px\" ondblclick=\"move1('right',$disable)\"",  //NO001
                            "size"       => "20",
                            "options"    => array()));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");


        //対象選択ボタンを作成する（全部
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right',$disable);\"" ) );  //NO001

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left',$disable);\"" ) );  //NO001

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right',$disable);\"" ) );  //NO001

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left',$disable);\"" ) );  //NO001

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");


        //印刷ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID",  "KNJF035");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();


        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf035Form1.html", $arg); 
    }
}
?>
