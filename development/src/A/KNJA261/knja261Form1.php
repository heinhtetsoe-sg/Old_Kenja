<?php

require_once('for_php7.php');


class knja261Form1
{
    function main(&$model){

        $objForm = new form;

        $arg["start"]   = $objForm->get_start("knja261Form1", "POST", "knja261index.php", "", "knja261Form1");

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knja261Query::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //1:クラス,2:個人表示指定
        $opt[0]=1;
        $opt[1]=2;

        if (!$model->field["KUBUN"]) $model->field["KUBUN"] = 1;

        for ($i = 1; $i <= 2; $i++) {
            $name = "KUBUN".$i;
            $objForm->ae( array("type"       => "radio",
                                "name"       => "KUBUN",
                                "value"      => $model->field["KUBUN"],
                                "extrahtml"  => "onclick =\" return btn_submit('knja261');\" id=\"$name\"",
                                "multiple"   => $opt));

            $arg["data"][$name] = $objForm->ge("KUBUN",$i);
        }

        if ($model->field["KUBUN"] == 1) $arg["clsno"] = $model->field["KUBUN"];
        if ($model->field["KUBUN"] == 2) $arg["schno"] = $model->field["KUBUN"];

        //年度
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"      => CTRL_YEAR,
                            ) );
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knja261Query::getSemesterMst(CTRL_YEAR);
        $result = $db->query($query);
        $rtn = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_gakki[] = array("label" => htmlspecialchars($row["SEMESTERNAME"]),
                                 "value" => $row["SEMESTER"]);
        }
        $result->free();

        if (!$model->field["GAKKI"]) $model->field["GAKKI"] = CTRL_SEMESTER;
        $objForm->ae( array("type"       => "select",
                            "name"       => "GAKKI",
                            "size"       => "1",
                            "value"      => $model->field["GAKKI"],
                            "extrahtml"  => "onChange=\"return btn_submit('knja261');\"",
                            "options"    => $opt_gakki));
        $arg["data"]["GAKKI"] = $objForm->ge("GAKKI");

        //適用日付
        if (!$model->field["DATE"]) $model->field["DATE"] = $model->control["学籍処理日"];
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //学期開始日付の取得
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI_SDATE",
                            "value"     => $model->control["学期開始日付"][$model->field["GAKKI"]]
                            ) );
        //学期終了日付の取得
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI_FDATE",
                            "value"     => $model->control["学期終了日付"][$model->field["GAKKI"]]
                            ) );

        //クラス一覧リスト
        $row1 = array();
        $query = knja261Query::getHrClassAuth($model, CTRL_YEAR, $model->field["GAKKI"], $model->auth, STAFFCD);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        //2:個人表示指定用
        $opt_left = array();
        if ($model->field["KUBUN"] == 2) {
            if ($model->field["GRADE_HR_CLASS"]=="") $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];

            $objForm->ae( array("type"       => "select",
                                "name"       => "GRADE_HR_CLASS",
                                "size"       => "1",
                                "value"      => $model->field["GRADE_HR_CLASS"],
                                "extrahtml"  => "onChange=\"return btn_submit('change_class');\"",
                                "options"    => $row1));

            $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

            $row1 = array();
            //生徒単位
            $selectleft = explode(",", $model->selectleft);
            $query = knja261Query::getSchno($model, CTRL_YEAR, $model->field["GAKKI"], $model->field["GRADE_HR_CLASS"]);//生徒一覧取得
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"], 
                                                             "value" => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);

                if($model->cmd == 'change_class' ) {
                    if (!in_array($row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"], $selectleft)){
                        $row1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                        'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                    }
                } else {
                    $row1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                    'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                }
            }
            //左リストで選択されたものを再セット
            if($model->cmd == 'change_class' ) {
                foreach ($model->select_opt as $key => $val){
                    if (in_array($key, $selectleft)) {
                        $opt_left[] = $val;
                    }
                }
            }
        }

        $result->free();

        $chdt = $model->field["KUBUN"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_NAME",
                            "extrahtml"  => "multiple style=\"width:220px\" width:\"220px\" ondblclick=\"move1('left',$chdt)\"",
                            "size"       => "20",
                            "options"    => $row1));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");

        //出力対象クラスリスト
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
                            "extrahtml"  => "multiple style=\"width:220px\" width:\"220px\" ondblclick=\"move1('right',$chdt)\"",
                            "size"       => "20",
                            "options"    => $opt_left));

        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");

        //対象選択ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right',$chdt);\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");

        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left',$chdt);\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");

        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right',$chdt);\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left',$chdt);\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //実行ボタン
        $btnName = "ＣＳＶ出力";
        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knja261Query::getSchoolCd());
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
            $btnName = "エクセル出力";
        } else {
            $extra = "onclick=\"return btn_submit('csv');\"";
        }
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", $btnName, $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA261");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "selectleft");
	    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        
        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja261Form1.html", $arg); 

    }

}
?>
