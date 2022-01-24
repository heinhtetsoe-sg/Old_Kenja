<?php

require_once('for_php7.php');

class knjf030Form1
{
    public function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjf030Form1", "POST", "knjf030index.php", "", "knjf030Form1");

        //リスト表示選択
        $opt = array(1, 2); //1:クラス選択 2:個人選択
        if (!$model->field["KUBUN"]) {
            $model->field["KUBUN"] = 1;
        }
        $onClick = " onclick =\" return btn_submit('knjf030');\"";
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

        //パターン
        if ($model->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
            $arg["isPattern1"] = "1";
        } else {
            $arg["isNotPattern1"] = "1";
        }

        //学校名
        $db = Query::dbCheckOut();
        $query = knjf030Query::getZ010();
        $result = $db->query($query);
        while ($rowf = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->schoolName = $rowf["NAME1"];
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($model->schoolName == "risshisha") {
            $arg["showNotRisshi_1"] = 0;
            $arg["showNotRisshi_2"] = 0;
        } else {
            $arg["showNotRisshi_1"] = 1;
            $arg["showNotRisshi_2"] = 1;
        }
        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"      => CTRL_YEAR,
                            ));

        //学期名
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        //現在の学期コードをhiddenで送る
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"      => CTRL_SEMESTER,
                            ));

        //クラス一覧リスト
        $db = Query::dbCheckOut();
        $row1 = array();
        $query = knjf030Query::getHrClassList($model);
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

            $objForm->ae(array("type"       => "select",
                                "name"       => "GRADE_HR_CLASS",
                                "size"       => "1",
                                "value"      => $model->field["GRADE_HR_CLASS"],
                                "extrahtml"  => "onChange=\"return btn_submit('change_class');\"",
                                "options"    => $row1));

            $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

            $row1 = array();
            //生徒単位
            $selectleft = explode(",", $model->selectleft);
            $query = knjf030Query::getSchno($model);//生徒一覧取得
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

        $objForm->ae(array("type"       => "select",
                            "name"       => "CLASS_NAME",
                            "extrahtml"  => "multiple style=\"width:300px; height:170px;\" ondblclick=\"move1('left',$chdt)\"",
                            "size"       => "18",
                            "options"    => $row1));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");

        //出力対象クラスリスト
        $objForm->ae(array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
                            "extrahtml"  => "multiple style=\"width:300px; height:170px;\" ondblclick=\"move1('right',$chdt)\"",
                            "size"       => "18",
                            "options"    => $opt_left));

        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");

        //対象選択ボタンを作成する（全部）
        $objForm->ae(array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right',$chdt);\"" ));

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");

        //対象取消ボタンを作成する（全部）
        $objForm->ae(array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left',$chdt);\"" ));

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");

        //対象選択ボタンを作成する（一部）
        $objForm->ae(array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right',$chdt);\"" ));

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取消ボタンを作成する（一部）
        $objForm->ae(array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left',$chdt);\"" ));

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");


        //健康診断票チェックボックスを作成（一般）
        $extra  = ($model->field["CHECK1"] == "on") ? "checked" : "";
        $extra .= " id=\"CHECK1\" onclick=\"OptionUse2('this');\"";
        $arg["data"]["CHECK1"] = knjCreateCheckBox($objForm, "CHECK1", "on", $extra, "");

        if ("7" == $model->Properties["useFormTypeKNJF030"]) {
            $arg["showForm7"] = "1";
            //7年用フォーム（中高のみ）
            $disable = ($model->field["CHECK1"] == "on") ? "" : " disabled";
            $extra  = ($model->field["useForm7_JH_Ippan"] == "1") ? "checked" : "";
            $extra .= " id=\"useForm7_JH_Ippan\"".$disable;
            $arg["data"]["useForm7_JH_Ippan"] = knjCreateCheckBox($objForm, "useForm7_JH_Ippan", "1", $extra, "");
        } else {
            $arg["showForm9"] = "1";
            //9年用フォーム（小中のみ）
            $disable = ($model->field["CHECK1"] == "on") ? "" : " disabled";
            $extra  = ($model->field["useForm9_PJ_Ippan"] == "1") ? "checked" : "";
            $extra .= " id=\"useForm9_PJ_Ippan\"".$disable;
            $arg["data"]["useForm9_PJ_Ippan"] = knjCreateCheckBox($objForm, "useForm9_PJ_Ippan", "1", $extra, "");
        }

        //出力順ラジオボタンを作成
        $opt = array(1, 2);     // 1:結果印刷 2:フォーム印刷
        $model->field["OUTPUTA"] = ($model->field["OUTPUTA"] == "") ? "1" : $model->field["OUTPUTA"];
        $disable = ($model->field["CHECK1"] == "on") ? "" : " disabled";
        $extra = array("id=\"OUTPUTA1\"".$disable, "id=\"OUTPUTA2\"".$disable);
        $radioArray = knjCreateRadio($objForm, "OUTPUTA", $model->field["OUTPUTA"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //印影を出力
        $disable = ($model->field["CHECK1"] == "on") ? "" : " disabled";
        $extra  = ($model->field["PRINT_STAMP"] == "on") ? "checked" : "";
        $extra .= " id=\"PRINT_STAMP\"".$disable;
        $arg["data"]["PRINT_STAMP"] = knjCreateCheckBox($objForm, "PRINT_STAMP", "on", $extra, "");

        if ($model->Properties["printKenkouSindanIppan"] != "2") {
            $arg["showPrintStamp2"] = "1";
            $disable = ($model->field["CHECK2"] == "on") ? "" : " disabled";
            $extra  = ($model->field["PRINT_STAMP2"] == "on") ? "checked" : "";
            $extra .= " id=\"PRINT_STAMP2\"".$disable;
            $arg["data"]["PRINT_STAMP2"] = knjCreateCheckBox($objForm, "PRINT_STAMP2", "on", $extra, "");
        }

        //学籍番号を出力
        for ($i=1; $i <= 2; $i++) {
            $disSch = ($model->field["CHECK{$i}"] == "on") ? "" : " disabled";
            $extra  = ($model->field["PRINT_SCHREGNO{$i}"] == "on") ? "checked" : "";
            $extra .= " id=\"PRINT_SCHREGNO{$i}\"".$disSch;
            $arg["data"]["PRINT_SCHREGNO{$i}"] = knjCreateCheckBox($objForm, "PRINT_SCHREGNO{$i}", "on", $extra, "");
        }

        //健康診断票チェックボックスを作成（歯・口腔）
        $extra  = ($model->field["CHECK2"] == "on") ? "checked" : "";
        $extra .= " id=\"CHECK2\" onclick=\"OptionUse3('this');\"";
        $arg["data"]["CHECK2"] = knjCreateCheckBox($objForm, "CHECK2", "on", $extra, "");

        //9年用フォーム（小中のみ）
        $disable = ($model->field["CHECK2"] == "on") ? "" : " disabled";
        $extra  = ($model->field["useForm9_PJ_Ha"] == "1") ? "checked" : "";
        $extra .= " id=\"useForm9_PJ_Ha\"".$disable;
        $arg["data"]["useForm9_PJ_Ha"] = knjCreateCheckBox($objForm, "useForm9_PJ_Ha", "1", $extra, "");

        //7年用フォーム（中高のみ）
        $disable = ($model->field["CHECK2"] == "on") ? "" : " disabled";
        $extra  = ($model->field["useForm7_JH_Ha"] == "1") ? "checked" : "";
        $extra .= " id=\"useForm7_JH_Ha\"".$disable;
        $arg["data"]["useForm7_JH_Ha"] = knjCreateCheckBox($objForm, "useForm7_JH_Ha", "1", $extra, "");

        // 両面印刷
        $extra  = ($model->field["CHECK1_2"] == "on") ? "checked" : "";
        $extra .= " id=\"CHECK1_2\" onclick=\"OptionUse1_2('this');\"";
        if ($model->field["CHECK1"] != "on" || $model->field["CHECK2"] != "on") {
            $extra .= " disabled ";
        }
        $arg["data"]["CHECK1_2"] = knjCreateCheckBox($objForm, "CHECK1_2", "on", $extra, "");

        //出力順ラジオボタンを作成
        $opt = array(1, 2);     // 1:結果印刷 2:フォーム印刷
        $model->field["OUTPUTB"] = ($model->field["OUTPUTB"] == "") ? "1" : $model->field["OUTPUTB"];
        $disable = ($model->field["CHECK2"] == "on") ? "" : " disabled";
        $extra = array("id=\"OUTPUTB1\"".$disable, "id=\"OUTPUTB2\"".$disable);
        $radioArray = knjCreateRadio($objForm, "OUTPUTB", $model->field["OUTPUTB"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //未提出項目生徒チェックボックスを作成
        $extra  = ($model->field["CHECK3"] == "on") ? "checked" : "";
        $extra .= " id=\"CHECK3\" onclick=\"DataUse('this');\"";
        $arg["data"]["CHECK3"] = knjCreateCheckBox($objForm, "CHECK3", "on", $extra, "");
        if ($model->Properties["printKenkouSindanIppan"] != "2") {
            // 未受験項目選択
            $arg["showMijukenItem"] = "1";
            $mijukenitem = array("01" => "尿検査",
                                 "02" => "貧血検査",
                                 "03" => "内科（校医）検診",
                                 "04" => "歯科検診",
                                 "05" => "胸部レントゲン撮影",
                                 "06" => "心電図検査");
            foreach ($mijukenitem as $key => $val) {
                $extra  = ($model->field["MIJUKEN_ITEM".$key] == "on" || $model->cmd == '') ? "checked" : "";
                $extra .= " id=\"MIJUKEN_ITEM".$key."\" ";
                $arg["data"]["MIJUKEN_ITEM".$key] = knjCreateCheckBox($objForm, "MIJUKEN_ITEM".$key, "on", $extra, "");
                $arg["data"]["MIJUKEN_ITEM".$key."_NAME"] = $val;
                knjCreateHidden($objForm, "MIJUKEN_ITEM".$key."_NAME", $val);
            }
        }

        //眼科検診チェックボックスを作成
        $extra  = ($model->field["CHECK4"] == "on") ? "checked" : "";
        $extra .= " id=\"CHECK4\" onclick=\"DataUse('this');\"";
        $arg["data"]["CHECK4"] = knjCreateCheckBox($objForm, "CHECK4", "on", $extra, "");

        //学校への提出日カレンダーを作成
        if (($model->field["CHECK3"] == "on") || ($model->field["CHECK4"] == "on")) {
            $dis3 = "";
            $arg["Dis_Date"] = " dis_date(false); " ;
        } else {
            $dis3 = "disabled";
            $arg["Dis_Date"] = " dis_date(true); " ;
        }
        $value = isset($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $value);

        //診断結果チェックボックスを作成
        $extra  = ($model->field["CHECK5"] == "on") ? "checked" : "";
        $extra .= " id=\"CHECK5\" onclick=\"dis_date5()\"";
        $arg["data"]["CHECK5"] = knjCreateCheckBox($objForm, "CHECK5", "on", $extra, "");
        $arg["Dis_Date5"] = " dis_date5(); " ;

        //作成日カレンダーを作成
        $value = isset($model->field["DATE5"]) ? $model->field["DATE5"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE5"] = View::popUpCalendar2($objForm, "DATE5", $value);

        //尿検査診断結果チェックボックスを作成
        $extra  = ($model->field["URINALYSIS_CHECK"] == "on") ? "checked" : "";
        $extra .= " id=\"URINALYSIS_CHECK\" onclick=\"dis_urinalysis('this');\"";
        $arg["data"]["URINALYSIS_CHECK"] = knjCreateCheckBox($objForm, "URINALYSIS_CHECK", "on", $extra, "");

        //尿検査診断結果 検査結果ラジオボタンを作成
        $opt = array(1, 2);     // 1:１次検査陽性 2:２次検査陽性（１次検査結果含む）
        $model->field["URINALYSIS_OUTPUT"] = ($model->field["URINALYSIS_OUTPUT"] == "") ? "1" : $model->field["URINALYSIS_OUTPUT"];
        $disable = ($model->field["URINALYSIS_CHECK"] == "on") ? "" : " disabled";
        $extra = array("id=\"URINALYSIS_OUTPUT1\"".$disable, "id=\"URINALYSIS_OUTPUT2\"".$disable);
        $radioArray = knjCreateRadio($objForm, "URINALYSIS_OUTPUT", $model->field["URINALYSIS_OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //健康診断結果チェックボックスを作成
        $extra  = ($model->field["CHECK6"] == "on") ? "checked" : "";
        $extra .= " id=\"CHECK6\" onclick=\"OptionUse('this');\"";
        $arg["data"]["CHECK6"] = knjCreateCheckBox($objForm, "CHECK6", "on", $extra, "");

        //出力順ラジオボタンを作成
        $opt = array(1, 2);     // 1:１人１枚 2:１人各種類
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $disable = ($model->field["CHECK6"] == "on") ? "" : " disabled";
        $extra = array("id=\"OUTPUT1\"".$disable, "id=\"OUTPUT2\"".$disable);
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //定期検診結果チェックボックスを作成
        $extra  = ($model->field["CHECK7"] == "on") ? "checked" : "";
        $extra .= " id=\"CHECK7\" onclick=\"dis_check7(this)\"";
        $arg["data"]["CHECK7"] = knjCreateCheckBox($objForm, "CHECK7", "on", $extra, "");

        //disabled
        $dis7 = ($model->field["CHECK7"] == "on") ? "" : "disabled";

        //定期検診結果 標準体重・肥満度チェックボックスを作成
        $extra  = ($model->field["STANDARD_NOTSHOW"] == "on"|| $model->cmd == '') ? "checked" : "";
        $extra .= " id=\"STANDARD_NOTSHOW\" onclick=\"dis_check7(this)\"";
        $extra .= $dis7;
        $arg["data"]["STANDARD_NOTSHOW"] = knjCreateCheckBox($objForm, "STANDARD_NOTSHOW", "on", $extra, "");

        if ($model->schoolName == 'miyagiken') {
            //作成日カレンダーを作成６）
            $value = isset($model->field["DATE7"]) ? $model->field["DATE7"] : str_replace("-", "/", CTRL_DATE);
            $arg["data"]["DATE7"] = View::popUpCalendar2($objForm, "DATE7", $value, $dis7);
            $arg["dis_check7"] = " dis_check7(); " ;
            $arg["miyagiken"] = "1";
        }

        //内科検診所見ありチェックボックスを作成
        $extra  = ($model->field["CHECK8"] == "on") ? "checked" : "";
        $extra .= " id=\"CHECK8\"";
        $arg["data"]["CHECK8"] = knjCreateCheckBox($objForm, "CHECK8", "on", $extra, "");

        //診断異常者一覧チェックボックスを作成
        $extra  = ($model->field["CHECK9"] == "on") ? "checked" : "";
        $extra .= " id=\"CHECK9\" onclick=\"SelectUse('this');\"";
        $arg["data"]["CHECK9"] = knjCreateCheckBox($objForm, "CHECK9", "on", $extra, "");

        //disabled
        $dis9 = ($model->field["CHECK9"] == "on") ? "" : "disabled";

        //一般条件コンボボックスを作成する
        $db = Query::dbCheckOut();
        $query2 = knjf030Query::getNameMst("F610");
        $result2 = $db->query($query2);
        while ($rowf = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row2[]= array('label' => $rowf["LABEL"],
                           'value' => $rowf["VALUE"]);
        }
        $result2->free();
        Query::dbCheckIn($db);

        $objForm->ae(array("type"       => "select",
                            "name"       => "SELECT1",
                            "size"       => "1",
                            "value"      => $model->field["SELECT1"],
                            "extrahtml"  => "style=\"width:185px\" ".$dis9,
                            "options"    => isset($row2)?$row2:array()));

        $arg["data"]["SELECT1"] = $objForm->ge("SELECT1");

        //歯・口腔条件コンボボックスを作成する
        $db = Query::dbCheckOut();
        $query3 = knjf030Query::getNameMst("F620");
        $result3 = $db->query($query3);
        while ($rowt = $result3->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row3[]= array('label' => $rowt["LABEL"],
                           'value' => $rowt["VALUE"]);
        }
        $result3->free();
        Query::dbCheckIn($db);

        $objForm->ae(array("type"       => "select",
                            "name"       => "SELECT2",
                            "size"       => "1",
                            "value"      => $model->field["SELECT2"],
                            "extrahtml"  => "style=\"width:185px\" ".$dis9,
                            "options"    => isset($row3)?$row3:array()));

        $arg["data"]["SELECT2"] = $objForm->ge("SELECT2");

        if ($model->schoolName == 'kumamoto') {
            $arg["kumamoto"] = "1";

            //家庭連絡コメント記入checkbox
            $extra = "id=\"FAMILY_CONTACT_COMMENT\"";
            $arg["data"]["FAMILY_CONTACT_COMMENT"] = knjCreateCheckBox($objForm, "FAMILY_CONTACT_COMMENT", "on", $extra.$dis7);

            //文面種類
            $db = Query::dbCheckOut();
            $query = knjf030Query::getDocumentCd();
            $extra = " onMouseOver=\"ViewDocument(this)\" onMouseOut=\"ViewDocumentMouseout()\"";
            makeCmb($objForm, $arg, $db, $query, "DOCUMENTCD", $model->field["DOCUMENTCD"], $extra, 1, "");

            //文面取得
            $query = knjf030Query::getDocumentTxst();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(array("\r\n", "\r", "\n"), "<br>", $row);
                knjCreateHidden($objForm, "DOUMENT_TEXT-".$row["VALUE"], $row["LABEL"]);
            }
            Query::dbCheckIn($db);

            //尿検査結果のお知らせ
            $extra  = ($model->field["CHECK10"] == "on") ? "checked" : "";
            $extra .= " id=\"CHECK10\" onclick=\"SelectUse('this');\"";
            $arg["data"]["CHECK10"] = knjCreateCheckBox($objForm, "CHECK10", "on", $extra, "");

            //視力の検査結果のお知らせ
            $extra  = ($model->field["CHECK11"] == "on") ? "checked" : "";
            $extra .= " id=\"CHECK11\" onclick=\"dis_cmb11(this)\"";
            $arg["data"]["CHECK11"] = knjCreateCheckBox($objForm, "CHECK11", "on", $extra, "");

            //disabled
            $dis11 = ($model->field["CHECK11"] == "on") ? "" : "disabled";
            //視力（裸眼及び矯正）条件
            $opt = array();
            $opt[] = array('label' => "01 条件なし",             'value' => "01");
            $opt[] = array('label' => "02 Ｂ以下（Ｂ、Ｃ、Ｄ）", 'value' => "02");
            $opt[] = array('label' => "03 Ｃ以下（Ｃ、Ｄ）",     'value' => "03");
            $extra = "";
            $arg["data"]["SIGHT_CONDITION"] = knjCreateCombo($objForm, "SIGHT_CONDITION", "", $opt, $extra.$dis11, 1);

            //聴力の検査結果のお知らせ
            $extra  = ($model->field["CHECK12"] == "on") ? "checked" : "";
            $extra .= " id=\"CHECK12\" onclick=\"SelectUse('this');\"";
            $arg["data"]["CHECK12"] = knjCreateCheckBox($objForm, "CHECK12", "on", $extra, "");

            //定期健康診断結果一覧
            $extra  = ($model->field["CHECK13"] == "on") ? "checked" : "";
            $extra .= " id=\"CHECK13\" onclick=\"SelectUse('this');\"";
            $arg["data"]["CHECK13"] = knjCreateCheckBox($objForm, "CHECK13", "on", $extra, "");
        } else {
            $arg["notkumamoto"] = "1";
        }

        //印刷ボタンを作成する
        $objForm->ae(array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ));

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae(array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE); //hiddenを作成する(必須)
        knjCreateHidden($objForm, "PRGID", PROGRAMID);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectleft"); //左のリストを保持

        knjCreateHidden($objForm, "printKenkouSindanIppan", $model->Properties["printKenkouSindanIppan"]);
        knjCreateHidden($objForm, "useKnjf030AHeartBiko", $model->Properties["useKnjf030AHeartBiko"]);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "useParasite_J", $model->Properties["useParasite_J"]);
        knjCreateHidden($objForm, "useParasite_H", $model->Properties["useParasite_H"]);
        knjCreateHidden($objForm, "useParasite_P", $model->Properties["useParasite_P"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "SCHOOLNAME", $model->schoolName);
        knjCreateHidden($objForm, "useForm5_H_Ha", $model->Properties["useForm5_H_Ha"]);
        knjCreateHidden($objForm, "useForm5_H_Ippan", $model->Properties["useForm5_H_Ippan"]);
        knjCreateHidden($objForm, "kenkouSindanIppanNotPrintNameMstComboNamespare2Is1", $model->Properties["kenkouSindanIppanNotPrintNameMstComboNamespare2Is1"]);
        knjCreateHidden($objForm, "knjf030PrintVisionNumber", $model->Properties["knjf030PrintVisionNumber"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "KenkouSindan_Ippan_Pattern", $model->Properties["KenkouSindan_Ippan_Pattern"]);
        knjCreateHidden($objForm, "knjf030addBlankGradeColumn", $model->Properties["knjf030addBlankGradeColumn"]);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf030Form1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
