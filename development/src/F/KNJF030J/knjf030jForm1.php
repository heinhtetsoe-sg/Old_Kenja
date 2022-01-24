<?php

require_once('for_php7.php');

class knjf030jForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("knjf030jForm1", "POST", "knjf030jindex.php", "", "knjf030jForm1");

        //リスト表示選択
        $opt = array(1, 2); //1:クラス選択 2:個人選択
        if (!$model->field["KUBUN"]) {
            $model->field["KUBUN"] = 1;
        }
        $onClick = " onclick =\" return btn_submit('knjf030j');\"";
        $extra = array("id=\"KUBUN1\"".$onClick, "id=\"KUBUN2\"".$onClick);
        $radioArray = knjCreateRadio($objForm, "KUBUN", $model->field["KUBUN"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        if ($model->field["KUBUN"] == 1) {
            $arg["clsno"] = $model->field["KUBUN"];
        }
        if ($model->field["KUBUN"] == 2) {
            $arg["schno"] = $model->field["KUBUN"];
        }

        //学校名
        $db = Query::dbCheckOut();
        $query = knjf030jQuery::getZ010();
        $result = $db->query($query);
        while ($rowf = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->schoolName = $rowf["NAME1"];
        }
        $result->free();
        Query::dbCheckIn($db);

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期名
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        //健康診断票（一般）チェックボックスを作成
        $extra  = ($model->field["CHECK1"] == "on")? "checked" : "";
        $extra .= " id=\"CHECK1\" onclick=\"KenshinCheck(1);\"";
        $arg["data"]["CHECK1"] = knjCreateCheckBox($objForm, "CHECK1", "on", $extra, "");

        //健康診断票（歯・口腔）チェックボックスを作成
        $extra  = ($model->field["CHECK2"] == "on")? "checked" : "";
        $extra .= " id=\"CHECK2\" onclick=\"KenshinCheck(2);\"";
        $arg["data"]["CHECK2"] = knjCreateCheckBox($objForm, "CHECK2", "on", $extra, "");

        //健康診断票（一般、歯・口腔）ラジオボタンを作成
        for ($i = 1; $i <= 2; $i++) {
            //ラジオボタンを作成
            $radioPrefix = 'RADIO' . $i;
            $opt = array(1, 2, 3);// 1:4年用フォーム（全日制、定時制） 2:6年用フォーム（通信制） 3:9年用フォーム（小・中のみ）
            $model->field[$radioPrefix] = ($model->field[$radioPrefix] == "") ? "1" : $model->field[$radioPrefix];
            $disabled = ($model->field["CHECK".$i] == "on") ? "" : " disabled";
            $extra = array(
                "id=\"" . $radioPrefix."_1\"" . $disabled,
                "id=\"" . $radioPrefix."_2\"" . $disabled,
                "id=\"" . $radioPrefix."_3\"" . $disabled,
            );
            $radioArray = knjCreateRadio($objForm, $radioPrefix, $model->field[$radioPrefix], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        }

        //両面印刷
        $extra  = ($model->field["CHECK1_2"] == "on") ? "checked" : "";
        $extra .= " id=\"CHECK1_2\"";
        if ($model->field["CHECK1"] != "on" || $model->field["CHECK2"] != "on") {
            $extra .= " disabled ";
        }
        $arg["data"]["CHECK1_2"] = knjCreateCheckBox($objForm, "CHECK1_2", "on", $extra, "");

        //提出日カレンダーを作成
        $value = isset($model->field["SEND_DATE"]) ? $model->field["SEND_DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["SEND_DATE"] = View::popUpCalendar($objForm, "SEND_DATE", $value);

        //健康診断結果通知書チェックボックスを作成
        for ($i = 3; $i <= 14; $i++) {
            $extra  = ($model->field["CHECK".$i] == "on") ? "checked" : "";
            $extra .= " id=\"CHECK".$i."\"";
            if ($i == 3) {
                //健康診断結果通知書（一覧）の時
                $extra .= "";//通知先ラジオボタンがない項目
            } elseif ($i == 14) {
                //健康診断結果通知書（尿）の時
                $extra .= " onclick=\"TuchiCheck(".$i."); TuchiCheck_Nyou();\"";
            } else {
                $extra .= " onclick=\"TuchiCheck(".$i.");\"";
            }
            $arg["data"]["CHECK".$i] = knjCreateCheckBox($objForm, "CHECK".$i, "on", $extra, "");

            //ラジオボタンを作成
            $radioPrefix = 'RADIO' . $i;
            $opt = array(1, 2);// 1:医療機関向け 2:保護者向け
            $model->field[$radioPrefix] = ($model->field[$radioPrefix] == "") ? "1" : $model->field[$radioPrefix];
            $disabled = ($model->field["CHECK".$i] == "on") ? "" : " disabled";
            $extra = array(
                "id=\"".$radioPrefix."_1\"" . $disabled,
                "id=\"".$radioPrefix."_2\"" . $disabled
            );
            $radioArray = knjCreateRadio($objForm, $radioPrefix, $model->field[$radioPrefix], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        }

        //健康診断結果通知書（尿）のサブチェックを作成
        for ($i = 15; $i <= 16; $i++) {
            $disabled = ($model->field["CHECK14"] == "on") ? "" : " disabled";//健康診断結果通知書（尿）がチェックオンの時
            $checked  = ($model->field["CHECK".$i] == "on") ? " checked" : "";
            $extra = " id=\"CHECK".$i."\"".$checked.$disabled;
            $arg["data"]["CHECK".$i] = knjCreateCheckBox($objForm, "CHECK".$i, "on", $extra, "");
        }

        //サブミット回避
        $extra = "style=\"display:none\"";
        $arg["data"]["SUBMIT_KAIHI"] = knjCreateTextBox($objForm, "", "SUBMIT_KAIHI", 2, 2, $extra);

        //ボタンを作成する
        makeButton($objForm, $arg, $model, $db);

        //hiddenを作成する
        makeHidden($objForm, $model, $db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf030jForm1.html", $arg);
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model, $db)
{
    //クラス一覧リスト
    $db = Query::dbCheckOut();
    $row1 = array();
    $query = knjf030jQuery::getHrClassList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }

    //2:個人表示指定用
    $opt_left = array();
    if ($model->field["KUBUN"] == 2) {
        if ($model->field["GRADE_HR_CLASS"]=="") {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        $extra = "onChange=\"return btn_submit('change_class');\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

        $row1 = array();
        //生徒単位
        $selectleft = explode(",", $model->selectleft);
        $query = knjf030jQuery::getSchno($model);//生徒一覧取得
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                                         "value" => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);

            if ($model->cmd == 'change_class') {
                if (!in_array($row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"], $selectleft)) {
                    $row1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                    'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                }
            } else {
                $row1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
            }
        }
        //左リストで選択されたものを再セット
        if ($model->cmd == 'change_class') {
            foreach ($model->select_opt as $key => $val) {
                if (in_array($key, $selectleft)) {
                    $opt_left[] = $val;
                }
            }
        }
    }

    $result->free();
    Query::dbCheckIn($db);

    $chdt = $model->field["KUBUN"];

    $extra = "multiple style=\"width:100%;\" ondblclick=\"move1('left',$chdt)\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $row1, $extra, 18);

    //出力対象クラスリスト
    $extra = "multiple style=\"width:100%;\" ondblclick=\"move1('right',$chdt)\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", $opt_left, $extra, 18);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right',$chdt);\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left',$chdt);\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right',$chdt);\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left',$chdt);\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model, $db)
{
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE); //hiddenを作成する(必須)
    knjCreateHidden($objForm, "PRGID", PROGRAMID);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectleft"); //左のリストを保持
    knjCreateHidden($objForm, "printKenkouSindanIppan", $model->Properties["printKenkouSindanIppan"]);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "SCHOOLNAME", $model->schoolName);
    knjCreateHidden($objForm, "knjf030jPrintVisionNumber", $model->Properties["knjf030jPrintVisionNumber"]);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
}
