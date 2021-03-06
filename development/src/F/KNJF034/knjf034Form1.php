<?php

require_once('for_php7.php');


class knjf034Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjf034Form1", "POST", "knjf034index.php", "", "knjf034Form1");

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

        //タイトルセット
        if ($model->Properties["useSpecial_Support_Hrclass"] == "1") {
            $arg["HR_CLASS_TITLE"] = "実";
        } else if ($model->Properties["useFi_Hrclass"] == "1") {
            $arg["HR_CLASS_TITLE"] = "FI";
        } else {
            $arg["HR_CLASS_TITLE"] = "";
        }

        //出力順ラジオボタン 1:個人選択 2:クラス選択
        $opt = array(1, 2);
        $disable = 0;
        if (!$model->field["OUTPUT"]) $model->field["OUTPUT"] = 2;
        if ($model->field["OUTPUT"] == 1) $disable = 1;
        $click = " onclick =\" return btn_submit('clickchange');\"";
        $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click);
        $value = isset($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $value, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($disable == 1) {
            $arg["student"] = 1;
        }else {
            $arg["hr_class"] = 2;
        }

        //クラス選択コンボ
        $db = Query::dbCheckOut();
        if ($disable == 1) {
            if ($model->Properties["useSpecial_Support_Hrclass"] == "1") {
                $query = knjf034Query::getGhrCd($model);
            } else if ($model->Properties["useFi_Hrclass"] == "1") {
                $query = knjf034Query::getFiGradeHrclass($model);
            } else {
                $query = knjf034Query::getHrClassAuth(CTRL_YEAR, CTRL_SEMESTER, AUTHORITY, STAFFCD, $model);
            }
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row1[]= array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
            }
            $result->free();
            Query::dbCheckIn($db);

            if(!isset($model->field["GRADE_HR_CLASS"])) {
                $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
            }

            if($model->cmd == 'clickchange' ) {
                $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
                $model->cmd = 'knjf034';
            }
            $objForm->ae( array("type"       => "select",
                                "name"       => "GRADE_HR_CLASS",
                                "size"       => "1",
                                "value"      => $model->field["GRADE_HR_CLASS"],
                                "extrahtml"  => "onchange=\"return btn_submit('knjf034'),AllClearList();\"",
                                "options"    => isset($row1)?$row1:array()));

            $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");
        }

        //対象者リスト
        $db = Query::dbCheckOut();

        if ($disable == 1) { 
            if ($model->Properties["useSpecial_Support_Hrclass"] == "1") {
                $query = knjf034Query::GetS_S_Students($model);//生徒一覧取得（実クラス）
            } else if ($model->Properties["useFi_Hrclass"] == "1") {
                $query = knjf034Query::getFiStudents($model);//生徒一覧取得（FI）
            } else {
                $query = knjf034Query::getClassStudents($model);//生徒一覧取得
            }
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt1[]= array('label' => $row["NAME"],
                               'value' => $row["SCHREGNO"]);
            }
        } else {
            if ($model->Properties["useSpecial_Support_Hrclass"] == "1") {
                $query = knjf034Query::getGhrCd($model);
            } else if ($model->Properties["useFi_Hrclass"] == "1") {
                $query = knjf034Query::getFiGradeHrclass($model);
            } else {
                $query = knjf034Query::getHrClassAuth(CTRL_YEAR, CTRL_SEMESTER, AUTHORITY, STAFFCD, $model);
            }
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt1[]= array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
            }
        }

        $result->free();
        Query::dbCheckIn($db);

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
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJF034"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata") );  

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DOCUMENTROOT",
                            "value"     => DOCUMENTROOT
                            ) );

        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "useFormKNJF034_2", $model->Properties["useFormKNJF034_2"]);
        knjCreateHidden($objForm, "useSpecial_Support_Hrclass", $model->Properties["useSpecial_Support_Hrclass"]);
        knjCreateHidden($objForm, "useFi_Hrclass", $model->Properties["useFi_Hrclass"]);
        knjCreateHidden($objForm, "knjf034HiddenHiman", $model->Properties["knjf034HiddenHiman"]);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();


        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf034Form1.html", $arg); 
    }
}
?>
