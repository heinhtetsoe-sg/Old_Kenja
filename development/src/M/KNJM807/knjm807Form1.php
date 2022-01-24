<?php

require_once('for_php7.php');

class knjm807Form1 {
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm807Form1", "POST", "knjm807index.php", "", "knjm807Form1");

        $db = Query::dbCheckOut();

        //年度、学期を画面表示する
        $model->field["YEAR"] = ($model->field["YEAR"] == "") ? CTRL_YEAR : $model->field["YEAR"];
        $opt = array(array('label' => (CTRL_YEAR + 1),'value' => (CTRL_YEAR + 1)),
                     array('label' => CTRL_YEAR,'value' => CTRL_YEAR));
        $ext = "onChange=\"return btn_submit('knjm807')\"";
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt, $ext, 1);

        $model->field["SEMESTER"] = ($model->field["SEMESTER"] == "") ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $row1 = array();
        $query = knjm807Query::getSemester($model->field["YEAR"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $ext = "onChange=\"return btn_submit('knjm807')\"";
        $arg["data"]["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $model->field["SEMESTER"], $row1, $ext, 1);

        //年度
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        //学期
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);

        //志願者・在学者ラジオボタン
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"onClick=\"return btn_submit('knjm807')\"", "id=\"OUTPUT1\"onClick=\"return btn_submit('knjm807')\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        
        //クラス選択コンボボックスを作成する
        //生徒名チェックボックスを作成
        if ($model->field["OUTPUT"] === '2') {
            $disabled = "";
        } else {
            $disabled = "disabled";
        }
        $row1 = array();
        $semes = CTRL_YEAR == $model->field["YEAR"] ? CTRL_SEMESTER : "1";
        $query = knjm807Query::getAuth($model, $model->field["YEAR"], $semes);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        if (!isset($model->field["GRADE_HR_CLASS"]) && $model->hr_set === '') {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        } else if (!isset($model->field["GRADE_HR_CLASS"]) && $model->hr_set !== '') {
            $model->field["GRADE_HR_CLASS"] = $model->hr_set;
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
                            "extrahtml"  => " onChange=\"return btn_submit('change_class');\"".$disabled,
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");
        //ラジオボタンを戻した時に同じクラスを表示するための変数
        $model->hr_set = $model->field["GRADE_HR_CLASS"];
        
        //生徒選択コンボボックスを作成する
        $opt_left = array();
        $selectleft = array();
        $selectleftval = array();
        $query = knjm807Query::getSchno($model, $model->field["YEAR"], $semes);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["SCHREGNO"]."　".$row["NAME_SHOW"],
                                                         'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
            if($model->cmd == 'change_class' ) {
                if (!in_array($row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"], $selectleft)){
                    $row2[] = array('label' => $row["SCHREGNO"]."　".$row["NAME_SHOW"],
                                    'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                }
            } else {
                $row2[] = array('label' => $row["SCHREGNO"]."　".$row["NAME_SHOW"],
                                'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
            }
        }

        //左リストで選択されたものを再セット
        if($model->cmd == 'change_class' ) {
            for ($i = 0; $i < get_count($selectleft); $i++) {
                $opt_left[] = array("label" => $selectleftval[$i],
                "value" => $selectleft[$i]);
            }
        }

        $result->free();
        Query::dbCheckIn($db);

        //対象者リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right')\"",
                            "size"       => "20",
                            "options"    => $opt_left));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

        //生徒一覧リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"",
                            "size"       => "20",
                            "options"    => isset($row2)?$row2:array()));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");


        //対象取り消しボタンを作成する(個別)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "　＞　",
                            "extrahtml"   => " onclick=\"move('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取り消しボタンを作成する(全て)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right2",
                            "value"       => "　≫　",
                            "extrahtml"   => " onclick=\"move('rightall');\"" ) );

        $arg["button"]["btn_right2"] = $objForm->ge("btn_right2");

        //対象選択ボタンを作成する(個別)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "　＜　",
                            "extrahtml"   => " onclick=\"move('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //対象選択ボタンを作成する(全て)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left2",
                            "value"       => "　≪　",
                            "extrahtml"   => " onclick=\"move('leftall');\"" ) );

        $arg["button"]["btn_left2"] = $objForm->ge("btn_left2");

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

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJM807"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CTRL_DATE",
                            "value"     => CTRL_DATE
                            ) );

        //左のリストを保持
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectleft") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectleftval") );

        knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm807Form1.html", $arg);
    }
}
?>
