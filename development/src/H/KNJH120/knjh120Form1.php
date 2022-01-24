<?php

require_once('for_php7.php');


class knjh120Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成////////////////////////////////////////////////////////////////////////
        $arg["start"]   = $objForm->get_start("knjh120Form1", "POST", "knjh120index.php", "", "knjh120Form1");

        //DB接続
        $db = Query::dbCheckOut();
        
        //年度テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////

        $arg["data"]["YEAR"] = $model->control["年度"];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"      => $model->control["年度"],
                            ) );


        //現在の学期コードをhiddenで送る//////////////////////////////////////////////////////////////////////////////////
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"      => $model->control["学期"],
                            ) );


        //クラス一覧リスト作成する///////////////////////////////////////////////////////////////////////////////
        $query = knjh120Query::getClassList($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();

        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_NAME",
                            "extrahtml"  => "multiple style=\"width:180px\" ondblclick=\"move1('left')\"",
                            "size"       => "15",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");


        //出力対象クラスリストを作成する///////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
                            "extrahtml"  => "multiple style=\"width:180px\" ondblclick=\"move1('right')\"",
                            "size"       => "15",
                            "options"    => array()));

        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");


        //対象選択ボタンを作成する（全部）/////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


        //対象取消ボタンを作成する（全部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


        //対象選択ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


        //対象取消ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");



        //科目コンボ
        $query = knjh120Query::getName($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                array_walk($row, "htmlspecialchars_array");
                $opt_subclasscd[] = array('label' => $row["LABEL"],
                                          'value' => $row["VALUE"]);
        }
        $result->free();

        $objForm->ae( array("type"      => "select",
                            "name"      => "SUBCLASSCD_FROM",
                            "size"      => "1",
                            "value"     => $Row["SUBCLASSCD"],
                            "options"   => $opt_subclasscd));

        $arg["data"]["SUBCLASSCD_FROM"] = $objForm->ge("SUBCLASSCD_FROM");


        $objForm->ae( array("type"      => "select",
                            "name"      => "SUBCLASSCD_TO",
                            "size"      => "1",
        //                  "value"     => $Row["SUBCLASSCD"],
                            "value"      => isset($model->field["SUBCLASSCD_TO"])?$model->field["SUBCLASSCD_TO"]:$opt_subclasscd[get_count($opt_subclasscd)-1]["value"],
                            "options"   => $opt_subclasscd));

        $arg["data"]["SUBCLASSCD_TO"] = $objForm->ge("SUBCLASSCD_TO");


        //印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");


        //終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");


        //hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJH120"
                            ) );


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "useCurriculumcd",
                            "value"     =>  $model->Properties["useCurriculumcd"]
                            ) );


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
                            
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();


        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh120Form1.html", $arg); 
    }
}
?>
