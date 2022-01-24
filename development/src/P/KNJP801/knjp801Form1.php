<?php

require_once('for_php7.php');

class knjp801Form1 {
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp801Form1", "POST", "knjp801index.php", "", "knjp801Form1");

        //年度
        $opt = array();
        $opt[] = array('label' => CTRL_YEAR+1, 'value' => CTRL_YEAR+1);
        $opt[] = array('label' => CTRL_YEAR, 'value' => CTRL_YEAR);
        if (!isset($model->field["YEAR"])) {
            $model->field["YEAR"] = CTRL_YEAR+1;
        }
        $extra = " onChange=\"return btn_submit('change_class')\"";
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt, $extra, 1);

        //学期
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);

        //クラス選択コンボボックスを作成する
        $db = Query::dbCheckOut();
        $row1 = array();
        $row1[]= array('label' => '新入生', 'value' => '00000');
        $query = knjp801Query::getAuth(CTRL_YEAR, CTRL_SEMESTER);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        if (!isset($model->field["GRADE_HR_CLASS"])) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
                            "extrahtml"  => " onChange=\"return btn_submit('change_class');\"",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");
        
        //会計グループCD
        $opt = array();
        $value_flg = false;
        $query = knjp801Query::getGrpcd($model->field["YEAR"], $model->field["GRADE_HR_CLASS"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ( $model->field["COLLECT_GRP_CD"] == $row["VALUE"]) $value_flg = true;
        }
        $model->field["COLLECT_GRP_CD"] = ( $model->field["COLLECT_GRP_CD"] && $value_flg) ?  $model->field["COLLECT_GRP_CD"] : $opt[0]["value"];
        $extra = " onChange=\"return btn_submit('change_class')\"";
        $arg["data"]["COLLECT_GRP_CD"] = knjCreateCombo($objForm, "COLLECT_GRP_CD", $model->field["COLLECT_GRP_CD"], $opt, $extra, 1);

        //帳票種類ボタン
        $opt = array(1, 2, 3);
        $model->field["OUTPUT_NO"] = ($model->field["OUTPUT_NO"] == "") ? "3" : $model->field["OUTPUT_NO"];
        $extra = array("id=\"OUTPUT_NO1\" onClick=\"return btn_submit('knjp801')\"", "id=\"OUTPUT_NO2\" onClick=\"return btn_submit('knjp801')\"", "id=\"OUTPUT_NO3\" onClick=\"return btn_submit('knjp801')\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_NO", $model->field["OUTPUT_NO"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //納入期限
        if ($model->field["OUTPUT_NO"] == "3") {
            $arg["data"]["OUTPUTNAME"] = "納入期限";
            $arg["data"]["OUTPUTDATE"] = View::popUpCalendar($objForm, "OUTPUTDATE", $model->field["OUTPUTDATE"]);
        }

        //生徒選択コンボボックスを作成する
        $opt_left = array();
        if ($model->cmd !== 'read') {
            $selectleft = array();
            $selectleftval = array();
        } else {
            $selectleft = explode(",", $model->selectleft);
            $selectleftval = explode(",", $model->selectleftval);
        }
        $query = knjp801Query::getSchno($model, CTRL_YEAR, CTRL_SEMESTER);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if ($row["OUTPUT_NO"] == "") {
                $model->select_opt[$row["SCHREGNO"]] = array("label" => '　'.$row["GRADE"].$row["HR_CLASS"]."-".$row["ATTENDNO"]."　".$row["NAME_SHOW"],
                                                             'value' => $row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]."-".$row["SCHREGNO"]);
            } else {
                $model->select_opt[$row["SCHREGNO"]] = array("label" => '●'.$row["GRADE"].$row["HR_CLASS"]."-".$row["ATTENDNO"]."　".$row["NAME_SHOW"],
                                                             'value' => $row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]."-".$row["SCHREGNO"]);
            }
            if($model->cmd == 'change_class'  || $model->cmd == 'read') {
                if (!in_array($row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]."-".$row["SCHREGNO"], $selectleft)){
                    if ($row["OUTPUT_NO"] == "") {
                        $row2[] = array('label' => '　'.$row["GRADE"].$row["HR_CLASS"]."-".$row["ATTENDNO"]."　".$row["NAME_SHOW"],
                                        'value' => $row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]."-".$row["SCHREGNO"]);
                    } else {
                        $row2[] = array('label' => '●'.$row["GRADE"].$row["HR_CLASS"]."-".$row["ATTENDNO"]."　".$row["NAME_SHOW"],
                                        'value' => $row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]."-".$row["SCHREGNO"]);
                    }
                }
            } else {
                if ($row["OUTPUT_NO"] == "") {
                    $row2[] = array('label' => '　'.$row["GRADE"].$row["HR_CLASS"]."-".$row["ATTENDNO"]."　".$row["NAME_SHOW"],
                                    'value' => $row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]."-".$row["SCHREGNO"]);
                } else {
                    $row2[] = array('label' => '●'.$row["GRADE"].$row["HR_CLASS"]."-".$row["ATTENDNO"]."　".$row["NAME_SHOW"],
                                    'value' => $row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]."-".$row["SCHREGNO"]);
                }
            }
        }

        //左リストで選択されたものを再セット
        if($model->cmd == 'change_class' || $model->cmd == 'read') {
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

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "')\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //印刷・更新ボタン
        $extra = "onclick=\"btn_submit('update')\"";
        $arg["button"]["btn_print_update"] = knjCreateBtn($objForm, "btn_print_update", "プレビュー／印刷／更新", $extra);

        if (!isset($model->warning) && $model->cmd == 'read') {
            $model->cmd = 'knjp801';
            $arg["printgo"] = " newwin('" . SERVLET_URL . "')";
        }

        //終了ボタン
        $extra = "onclick=\"closeWin()\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        $arg["TOP"]["DBNAME"] = knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        $arg["TOP"]["PRGID"] = knjCreateHidden($objForm, "PRGID", "KNJP801");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        
        //左のリストを保持
        knjCreateHidden($objForm, "selectleft");
        knjCreateHidden($objForm, "selectleftval");
        knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp801Form1.html", $arg);
    }
}
?>
