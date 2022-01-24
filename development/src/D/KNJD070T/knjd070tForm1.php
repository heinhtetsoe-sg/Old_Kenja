<?php

require_once('for_php7.php');


class knjd070tForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd070tForm1", "POST", "knjd070tindex.php", "", "knjdForm1070");

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->control["年度"];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => $model->control["年度"],
                            ) );

        //現在の学期コードを送る（hidden）
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"     => $model->control["学期"],
                            ) );

        //クラス選択コンボボックスを作成する
        $db = Query::dbCheckOut();
        $query = knjd070tQuery::getAuth($model->control["年度"], $model->control["学期"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();
        //Query::dbCheckIn($db);

        if(!isset($model->field["GRADE_HR_CLASS"])) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
                            "extrahtml"  => "onchange=\"return btn_submit('knjd070t');\"",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //対象科目一覧リストを作成する       
        $query = knjd070tQuery::getSubclass($model);
        $result2 = $db->query($query);
        while($row = $result2->fetchRow(DB_FETCHMODE_ASSOC)){
            $row2[] = array('label' => $row["VALUE"]." ".$row["LABEL"],
                            'value' => $row["VALUE"]);
        }

        $result2->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"      => "select",
                            "name"      => "SUBCLASS_NAME",
                            "size"      => "15",
                            "extrahtml" => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"",
                            "value"     => $model->field["SUBCLASS_NAME"],
                            "options"   => isset($row2)?$row2:array()));

        $arg["data"]["SUBCLASS_NAME"] = $objForm->ge("SUBCLASS_NAME");

        //出力対象科目リストを作成する
        $objForm->ae( array("type"      => "select",
                            "name"      => "SUBCLASS_SELECTED",
                            "extrahtml" => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"",
                            "size"      => "15",
                            "options"   => array()));

        $arg["data"]["SUBCLASS_SELECTED"] = $objForm->ge("SUBCLASS_SELECTED");

        //対象選択ボタンを作成する（全部）
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_rights",
                            "value"     => ">>",
                            "extrahtml" => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");

        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_lefts",
                            "value"     => "<<",
                            "extrahtml" => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");

        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_right1",
                            "value"     => "＞",
                            "extrahtml" => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_left1",
                            "value"     => "＜",
                            "extrahtml" => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //単位保留の科目チェックボックスを作成する
        if($model->field["HORYU"] == "on"){
            $check_horyu = "checked";
        } else {
            $check_horyu = "";
        }

        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "HORYU",
                            "value"     => "on",
                            "extrahtml" => $check_horyu." id=\"HORYU\"" ) );

        $arg["data"]["HORYU"] = $objForm->ge("HORYU");

        //印刷ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_print",
                            "value"     => "プレビュー／印刷",
                            "extrahtml" => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する(必須)
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"     => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD070"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "useCurriculumcd",
                            "value"     => $model->Properties["useCurriculumcd"]
                            ) );

        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd070tForm1.html", $arg); 
    }
}
?>
