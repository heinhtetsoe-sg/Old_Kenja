<?php

require_once('for_php7.php');

/********************************************************************/
/* 学籍番号バーコード付出席票(生徒別)               山城 2005/03/11 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjm021mForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm021mForm1", "POST", "knjm021mindex.php", "", "knjm021mForm1");

        //年度テキストボックスを作成する

        $arg["data"]["YEAR"] = $model->control["年度"];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"      => $model->control["年度"], ) );

        //学期テキストボックス設定

        $arg["data"]["GAKKI"] = $model->control["学期"];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"      => $model->control["学期"] ) );

        //クラス選択コンボボックスを作成する
        $db = Query::dbCheckOut();
        $query = knjm021mQuery::getAuth($model->control["年度"],$model->control["学期"]);

        $result = $db->query($query);
        $grade_hr_class_flg = false;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"])
                $grade_hr_class_flg = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        if(!isset($model->field["GRADE_HR_CLASS"]) || !$grade_hr_class_flg) 
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
                            "extrahtml"     => "onchange=\"return btn_submit('gakki'),AllClearList();\"",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //対象者リストを作成する
        $db = Query::dbCheckOut();
        $query = knjm021mQuery::getSchreg($model);

        $result = $db->query($query);

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt1[]= array('label' =>  $row["NAME"],
                           'value' => $row["SCHREGNO"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple style=\"width:230px\" ondblclick=\"move1('left')\"",
                            "size"       => "20",
                            "options"    => isset($opt1)?$opt1:array()));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

        //生徒一覧リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple style=\"width:230px\" ondblclick=\"move1('right')\"",
                            "size"       => "20",
                            "options"    => array()));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");

        //対象選択ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //部数
        if (!$model->field["BUSU"]) $model->field["BUSU"] = 1;
        $objForm->ae( array("type"        => "text",
                            "name"        => "BUSU",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);check(this)\"",
                            "value"       => $model->field["BUSU"]));

        $arg["data"]["BUSU"] = $objForm->ge("BUSU");

        //フォームのみ出力
        if ($model->field["FORM_ONLY"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["FORM_ONLY"] = knjCreateCheckBox($objForm, "FORM_ONLY", "1", $extra);

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
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJM021M"
                            ) );


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "LOGIN_DATE",
                            "value"      => CTRL_DATE
                            ) );

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm021mForm1.html", $arg); 
    }
}
?>
