<?php

require_once('for_php7.php');


class knjf070Form1{

    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjf070Form1", "POST", "knjf070index.php", "", "knjf070Form1");

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        $db = Query::dbCheckOut();
        $opt1 = array();
        $query = knjf070Query::getSchoolKind($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "SCHOOL_KIND",
                            "size"       => "1",
                            "value"      => $model->field["SCHOOL_KIND"],
                            "extrahtml"  => "",
                            "options"    => $opt1));

        $arg["data"]["SCHOOL_KIND"] = $objForm->ge("SCHOOL_KIND");

        //視力検査の統計チェックボックス
        $check_1  = ($model->field["CHECK1"] == "on") ? "checked" : "";
        $check_1 .= " id=\"CHECK1\"";
        $check_1 .= " onclick=\"OptionUse('this');\"";
        $objForm->ae( array("type"      => "checkbox",
                            "name"		=> "CHECK1",
                            "value"		=> "on",
                            "extrahtml"	=> $check_1 ) );

        $arg["data"]["CHECK1"] = $objForm->ge("CHECK1");

        //統計対象データ選択ラジオボタン 1:文字 2:数値
        $opt_eye = array(1, 2);
        $model->field["EYESIGHT"] = ($model->field["EYESIGHT"] == "") ? "1" : $model->field["EYESIGHT"];
        $disabled = " disabled";
        $extra = array("id=\"EYESIGHT1\"".$disabled, "id=\"EYESIGHT2\"".$disabled);
        $radioArray = knjCreateRadio($objForm, "EYESIGHT", $model->field["EYESIGHT"], $extra, $opt_eye, get_count($opt_eye));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //尿検査の統計チェックボックス
        $check_2 = ($model->field["CHECK2"] == "on") ? "checked" : "";
        $check_2 .= " id=\"CHECK2\"";
        $objForm->ae( array("type"      => "checkbox",
                            "name"		=> "CHECK2",
                            "value"		=> "on",
                            "extrahtml"	=> $check_2 ) );

        $arg["data"]["CHECK2"] = $objForm->ge("CHECK2");

        //貧血検査の統計チェックボックス
        $check_3 = ($model->field["CHECK3"] == "on") ? "checked" : "";
        $check_3 .= " id=\"CHECK3\"";
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "CHECK3",
                            "value"		=> "on",
                            "extrahtml"	=> $check_3 ) );

        $arg["data"]["CHECK3"] = $objForm->ge("CHECK3");

        //歯科検査の統計チェックボックス
        $check_4 = ($model->field["CHECK4"] == "on") ? "checked" : "";
        $check_4 .= " id=\"CHECK4\"";
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "CHECK4",
                            "value"		=> "on",
                            "extrahtml"	=> $check_4 ) );

        $arg["data"]["CHECK4"] = $objForm->ge("CHECK4");

        //歯科検査他の統計チェックボックス
        $check_5 = ($model->field["CHECK5"] == "on") ? "checked" : "";
        $check_5 .= " id=\"CHECK5\"";
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "CHECK5",
                            "value"		=> "on",
                            "extrahtml"	=> $check_5 ) );

        $arg["data"]["CHECK5"] = $objForm->ge("CHECK5");

        //身体測定の統計チェックボックス
        $check_6 = ($model->field["CHECK6"] == "on") ? "checked" : "";
        $check_6 .= " id=\"CHECK6\"";
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "CHECK6",
                            "value"		=> "on",
                            "extrahtml"	=> $check_6 ) );

        $arg["data"]["CHECK6"] = $objForm->ge("CHECK6");

        //異年令を除くチェックボックス
        $extra  = ($model->field["NOT_AGE"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"NOT_AGE\"";
        $arg["data"]["NOT_AGE"] = knjCreateCheckBox($objForm, "NOT_AGE", "on", $extra);

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
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJF070");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf070Form1.html", $arg); 
    }
}
?>
