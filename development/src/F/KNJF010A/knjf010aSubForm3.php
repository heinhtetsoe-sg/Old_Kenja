<?php

require_once('for_php7.php');

class knjf010aSubForm3
{
    public function main(&$model)
    {
        $objForm        = new form();
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjf010aindex.php", "", "sel");

        if ($model->isMie) {
            $arg["is_mie"] = "1";
            $arg["is_not_mie"] = "";
        } else {
            $arg["is_mie"] = "";
            $arg["is_not_mie"] = "1";
        }

        //生徒一覧
        $opt_left = $opt_right = array();
        //置換処理選択時の生徒の情報
        $array = explode(",", $model->replace_data["selectdata"]);
        if ($array[0]=="") {
            $array[0] = $model->schregno;
        }
        //生徒情報
        $RowH = knjf010aQuery::getMedexamHdat($model);    //生徒健康診断ヘッダデータ取得
        $RowD = knjf010aQuery::getMedexamDetDat($model); //生徒健康診断詳細データ取得

        $db = Query::dbCheckOut();

        $result   = $db->query(knjf010aQuery::getStudent($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["SCHREGNO"], $array)) {
                $opt_right[]  = array("label" => $row["ATTENDNO"]." ".$row["SCHREGNO"]." ".$row["NAME_SHOW"],
                                      "value" => $row["SCHREGNO"]);
            } else {
                $opt_left[]   = array("label" => $row["ATTENDNO"]." ".$row["SCHREGNO"]." ".$row["NAME_SHOW"],
                                      "value" => $row["SCHREGNO"]);
            }
        }
        $result->free();

        //レイアウトの切り替え
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            $arg["new"] = 1;
        } elseif ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") {
            $arg["new2"] = 1;
            $arg["Ippan".$model->Properties["printKenkouSindanIppan"]] = "1";
        } else {
            $arg["base"] = 1;
        }

        if ($model->isMie) {
            $arg["is_mie"] = "1";
        }

        //寄生虫卵表示
        if (($model->school_kind == "P" && $model->Properties["useParasite_P"] == "1") || ($model->school_kind == "J" && $model->Properties["useParasite_J"] == "1") || ($model->school_kind == "H" && $model->Properties["useParasite_H"] == "1")) {
            $arg["para"] = 1;
            $check_cnt1 = 13;
            $check_cnt2 = 14;
        } else {
            $check_cnt1 = 12;
            $check_cnt2 = 13;
        }

        //尿)その他の検査テキスト、指導区分コンボ
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            $check_cnt2 += ($arg["para"] == 1) ? 2 : 3;
        }

        //福井（貧血）
        if ($model->isFukui) {
            $arg["is_fukui_koma"] = "1";
            $check_cnt2 += 1;
        }
        if ($model->isMie) {
            $arg["is_mie"] = "1";
            $arg["rowspan1"] = 3;
            //学校医のチェックボックスが17なので、そこまでの不足分を加算
            $check_cnt2 += 2;
        } else {
            $arg["rowspan1"] = 2;
        }
        //駒澤（貧血）
        if ($model->isKoma) {
            $arg["is_fukui_koma"] = "1";
            $check_cnt2 += 1;
        }

        //結核の間接撮影、所見、その他検査 非表示
        if ($model->school_kind == "H") {
            $arg["tbFilmShow"] = 1;
        } else {
            $arg["tbFilmUnShow"] = 1;
        }

        //未検査項目設定
        $notFieldSet = $sep = "";
        $query = knjf010aQuery::getMedexamDetNotExaminedDat($model);
        $result = $db->query($query);
        while ($notExamined = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($notExamined as $field => $val) {
                if ($field == "YEAR" || $field == "GRADE") {
                    continue;
                }

                if ($val == "1") {
                    $notFieldSet .= $sep.$field;
                    $sep = ":";
                }
            }
        }
        if ($notFieldSet != "") {
            $arg["setNotExamined"] = "setNotExamined('{$notFieldSet}')";
        }

        /* 編集項目 */
        if ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") {
            //チェックボックス
            for ($i=0; $i<$check_cnt1; $i++) {
                if ($i==11) {
                    $objForm->ae(array("type"       => "checkbox",
                                        "name"      => "RCHECK".$i,
                                        "value"     => $i,
                                        "checked"   => ((is_array($model->replace_data["check"])) ? (in_array($i, $model->replace_data["check"]) ? 1 : 0) : 0),
                                        "extrahtml" => "onClick=\"return check_all(this);\""));
                    $arg["data"]["RCHECK".$i] = $objForm->ge("RCHECK".$i);

                    knjCreateHidden($objForm, "CHECK_ALL", "RCHECK11");
                } else {
                    $objForm->ae(array("type"       => "checkbox",
                                       "name"       => "RCHECK".$i,
                                       "value"      => $i,
                                       "checked"    => ((is_array($model->replace_data["check"])) ? (in_array($i, $model->replace_data["check"]) ? 1 : 0) : 0)));
                    $arg["data"]["RCHECK".$i] = $objForm->ge("RCHECK".$i);
                }
            }
        } else {
            //チェックボックス
            for ($i=0; $i<$check_cnt2; $i++) {
                if ($i==12) {
                    $objForm->ae(array("type"       => "checkbox",
                                        "name"      => "RCHECK".$i,
                                        "value"     => $i,
                                        "checked"   => ((is_array($model->replace_data["check"])) ? (in_array($i, $model->replace_data["check"]) ? 1 : 0) : 0),
                                        "extrahtml" => "onClick=\"return check_all(this);\""));
                    $arg["data"]["RCHECK".$i] = $objForm->ge("RCHECK".$i);

                    knjCreateHidden($objForm, "CHECK_ALL", "RCHECK12");
                } else {
                    $objForm->ae(array("type"       => "checkbox",
                                       "name"       => "RCHECK".$i,
                                       "value"      => $i,
                                       "checked"    => ((is_array($model->replace_data["check"])) ? (in_array($i, $model->replace_data["check"]) ? 1 : 0) : 0)));
                    $arg["data"]["RCHECK".$i] = $objForm->ge("RCHECK".$i);
                }
            }
        }

        //結核・撮影日付
        $RowD["TB_FILMDATE"] = str_replace("-", "/", $RowD["TB_FILMDATE"]);
        $arg["data"]["TB_FILMDATE"] = View::popUpCalendar($objForm, "TB_FILMDATE", $RowD["TB_FILMDATE"]);
        //結核・所見コンボ
        $result     = $db->query(knjf010aQuery::getTbRemark($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $objForm->ae(array("type"        => "select",
                            "name"        => "TB_REMARKCD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:260px;\"",
                            "value"       => $RowD["TB_REMARKCD"],
                            "options"     => $opt ));
        $arg["data"]["TB_REMARKCD"] = $objForm->ge("TB_REMARKCD");

        //結核検査(X線)
        $extra = "";
        $arg["data"]["TB_X_RAY"] = knjCreateTextBox($objForm, $RowD["TB_X_RAY"], "TB_X_RAY", 40, 20, $extra);

        //結核・その他検査コンボ
        $result     = $db->query(knjf010aQuery::getTbOthertest($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $objForm->ae(array("type"        => "select",
                            "name"        => "TB_OTHERTESTCD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:260px;\"",
                            "value"       => $RowD["TB_OTHERTESTCD"],
                            "options"     => $opt ));
        $arg["data"]["TB_OTHERTESTCD"] = $objForm->ge("TB_OTHERTESTCD");
        //結核・病名コンボ
        $result     = $db->query(knjf010aQuery::getTbName($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        if ($model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "style=\"width:260px;\" onChange=\"syokenNyuryoku(this, document.forms[0].TB_NAME_REMARK1)\"";
        } else {
            $extra = "style=\"width:260px;\"";
        }
        $objForm->ae(array("type"        => "select",
                            "name"        => "TB_NAMECD",
                            "size"        => "1",
                            "extrahtml"   => $extra,
                            "value"       => $RowD["TB_NAMECD"],
                            "options"     => $opt ));
        $arg["data"]["TB_NAMECD"] = $objForm->ge("TB_NAMECD");
        //結核・病名テキスト
        if ((int)$RowD["TB_NAMECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["TB_NAME_REMARK1"] = knjCreateTextBox($objForm, $RowD["TB_NAME_REMARK1"], "TB_NAME_REMARK1", 40, 20, $extra);
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            $arg["data"]["TB_NAMECD_LABEL"] = ($model->z010name1 == "miyagiken") ? "病名" : "疾病及び異常";
        } else {
            $arg["data"]["TB_NAMECD_LABEL"] = "病名";
        }
        //結核・指導区分コンボ
        $result     = $db->query(knjf010aQuery::getTbAdvise($model));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $objForm->ae(array("type"        => "select",
                            "name"        => "TB_ADVISECD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:260px;\"",
                            "value"       => $RowD["TB_ADVISECD"],
                            "options"     => $opt ));
        $arg["data"]["TB_ADVISECD"] = $objForm->ge("TB_ADVISECD");

        //尿
        $extra = "style=\"width:260px;\"";
        //尿・１次蛋白コンボ
        //尿・２次蛋白コンボ
        $opt        = array();
        $opt[]      = $optnull;
        $result     = $db->query(knjf010aQuery::getUric($model, "F020"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $arg["data"]["ALBUMINURIA1CD"] = knjCreateCombo($objForm, "ALBUMINURIA1CD", $RowD["ALBUMINURIA1CD"], $opt, $extra, 1);
        $arg["data"]["ALBUMINURIA2CD"] = knjCreateCombo($objForm, "ALBUMINURIA2CD", $RowD["ALBUMINURIA2CD"], $opt, $extra, 1);
        //尿・１次糖コンボ
        //尿・２次糖コンボ
        $opt        = array();
        $opt[]      = $optnull;
        $result     = $db->query(knjf010aQuery::getUric($model, "F019"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $arg["data"]["URICSUGAR1CD"] = knjCreateCombo($objForm, "URICSUGAR1CD", $RowD["URICSUGAR1CD"], $opt, $extra, 1);
        $arg["data"]["URICSUGAR2CD"] = knjCreateCombo($objForm, "URICSUGAR2CD", $RowD["URICSUGAR2CD"], $opt, $extra, 1);
        //尿・１次潜血コンボ
        //尿・２次潜血コンボ
        $opt        = array();
        $opt[]      = $optnull;
        $result     = $db->query(knjf010aQuery::getUric($model, "F018"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $arg["data"]["URICBLEED1CD"] = knjCreateCombo($objForm, "URICBLEED1CD", $RowD["URICBLEED1CD"], $opt, $extra, 1);
        $arg["data"]["URICBLEED2CD"] = knjCreateCombo($objForm, "URICBLEED2CD", $RowD["URICBLEED2CD"], $opt, $extra, 1);

        if ($model->Properties["printKenkouSindanIppan"] == "1" && $model->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
            $arg["URIC_CONN1"]  = "10";
            $arg["URIC_CONN21"] = "4";
            $arg["URIC_CONN22"] = "4";
            $arg["DISP_PH"] = 1;
            //尿・１次PHテキスト
            //尿・２次PHテキスト
            $arg["data"]["URICPH1"] = knjCreateTextBox($objForm, $RowD["URICPH1"], "URICPH1", 4, 4, "");
            $arg["data"]["URICPH2"] = knjCreateTextBox($objForm, $RowD["URICPH2"], "URICPH2", 4, 4, "");
        } else {
            $arg["URIC_CONN1"]  = "8";
            $arg["URIC_CONN21"] = "3";
            $arg["URIC_CONN22"] = "3";
        }

        //尿・その他の検査
        if ($model->Properties["printKenkouSindanIppan"] != "2") {
            $objForm->ae(array("type"        => "text",
                                "name"        => "URICOTHERTEST",
                                "size"        => 40,
                                "maxlength"   => 20,
                                "extrahtml"   => "",
                                "value"       => $RowD["URICOTHERTEST"] ));
            $arg["data"]["URICOTHERTEST"] = $objForm->ge("URICOTHERTEST");
        } else {
            $result     = $db->query(knjf010aQuery::getUriCothertest($model));
            $opt        = array();
            $opt[]      = $optnull;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label" => $row["LABEL"],
                               "value" => $row["VALUE"]);
            }
            $result->free();
            $extra = "style=\"width:260px;\"";
            $arg["data"]["URICOTHERTESTCD"] = knjCreateCombo($objForm, "URICOTHERTESTCD", $RowD["URICOTHERTESTCD"], $opt, $extra, 1);
        }
        //尿:指導区分コンボ
        $result     = $db->query(knjf010aQuery::getUriAdvisecd($model));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "style=\"width:260px;\"";
        $arg["data"]["URI_ADVISECD"] = knjCreateCombo($objForm, "URI_ADVISECD", $RowD["URI_ADVISECD"], $opt, $extra, 1);

        //寄生虫卵コンボ
        $query = knjf010aQuery::getNameMst($model, "F023");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "PARASITE", $RowD["PARASITE"], $extra, 1, "blank");

        //その他疾病及び異常コンボ
        $result     = $db->query(knjf010aQuery::getOtherDisease($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        if ($model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "style=\"width:260px;\" onChange=\"syokenNyuryoku(this, document.forms[0].OTHER_REMARK2)\"";
        } else {
            $extra = "style=\"width:260px;\"";
        }
        $objForm->ae(array("type"        => "select",
                            "name"        => "OTHERDISEASECD",
                            "size"        => "1",
                            "extrahtml"   => $extra,
                            "value"       => $RowD["OTHERDISEASECD"],
                            "options"     => $opt ));
        $arg["data"]["OTHERDISEASECD"] = $objForm->ge("OTHERDISEASECD");
        //その他疾病及び異常:指導区分コンボ
        $result     = $db->query(knjf010aQuery::getOtherAdvisecd($model));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "style=\"width:260px;\"";
        $arg["data"]["OTHER_ADVISECD"] = knjCreateCombo($objForm, "OTHER_ADVISECD", $RowD["OTHER_ADVISECD"], $opt, $extra, 1);

        //その他疾病及び異常:所見
        $arg["data"]["OTHER_REMARK"] = knjCreateTextBox($objForm, $RowD["OTHER_REMARK"], "OTHER_REMARK", 40, 20, "");
        if ((int)$RowD["OTHERDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        if ($model->Properties["useSpecial_Support_School"] == "1") { //特別支援学校
            $otherSize = '80';
            $arg["data"]["OTHER_REMARK2_MOJI"] = '100';
        } else {
            $otherSize = '40';
            $arg["data"]["OTHER_REMARK2_MOJI"] = '20';
        }
        $arg["data"]["OTHER_REMARK2"] = knjCreateTextBox($objForm, $RowD["OTHER_REMARK2"], "OTHER_REMARK2", $otherSize, $model->maxOtherRemark2Byte, $extra);
        $arg["data"]["OTHER_REMARK3"] = knjCreateTextBox($objForm, $RowD["OTHER_REMARK3"], "OTHER_REMARK3", 40, 20, "");

        //貧血
        //所見
        $extra = "";
        $arg["data"]["ANEMIA_REMARK"] = knjCreateTextBox($objForm, $RowD["ANEMIA_REMARK"], "ANEMIA_REMARK", 21, 30, $extra);

        //ヘモグロビン
        $extra = "style=\"text-align:right\" onblur=\"return Num_Check(this);\"";
        $arg["data"]["HEMOGLOBIN"] = knjCreateTextBox($objForm, $RowD["HEMOGLOBIN"], "HEMOGLOBIN", 4, 4, $extra);

        //学校医・所見
        if ($arg["is_mie"] == "1") {
            $result     = $db->query(knjf010aQuery::getDocCd($model));
            $opt        = array();
            $opt[]      = $optnull;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $namecd = substr($row["NAMECD2"], 0, 2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            $objForm->ae(array("type"        => "select",
                                "name"        => "DOC_CD",
                                "size"        => "1",
                                "extrahtml"   => "style=\"width:260px;\"",
                                "value"       => $RowD["DOC_CD"],
                                "options"     => $opt ));
            $arg["data"]["DOC_CD"] = $objForm->ge("DOC_CD");
        }
        $arg["data"]["DOC_REMARK"] = knjCreateTextBox($objForm, $RowD["DOC_REMARK"], "DOC_REMARK", 40, 20, "");

        //学校医・所見日付
        $RowD["DOC_DATE"] = str_replace("-", "/", $RowD["DOC_DATE"]);
        $arg["data"]["DOC_DATE"] = View::popUpCalendar($objForm, "DOC_DATE", $RowD["DOC_DATE"]);

        //学校医
        if ($model->isMie) {
            //学校医コンボボックス(三重のみ)
            $arg["data"]["DOC_NAME"] = knjCreateTextBox($objForm, $RowD["DOC_NAME"], "DOC_NAME", 20, 10, "");
        }

        //備考
        if ($model->Properties["useSpecial_Support_School"] == "1") { //特別支援学校
            $arg["data"]["REMARK_MOJI"] = '100';
        } else {
            $arg["data"]["REMARK_MOJI"] = '60';
        }
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $RowD["REMARK"], "REMARK", 80, $model->maxRemarkByte, "");

        Query::dbCheckIn($db);

        /* ボタン作成 */
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit()\"" ));
        //戻るボタン
        $link = REQUESTROOT."/F/KNJF010A/knjf010aindex.php?cmd=back&ini2=1";
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"window.open('$link','_self');\"" ));
        $arg["BUTTONS"] = $objForm->ge("btn_update")."    ".$objForm->ge("btn_back");
        //対象者一覧
        $objForm->ae(array("type"        => "select",
                            "name"        => "left_select",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','left_select','right_select',1)\" ",
                            "options"     => $opt_left));
        //生徒一覧
        $objForm->ae(array("type"        => "select",
                            "name"        => "right_select",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','left_select','right_select',1)\" ",
                            "options"     => $opt_right));
        //全て追加
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','left_select','right_select',1);\"" ));
        //追加
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','left_select','right_select',1);\"" ));
        //削除
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','left_select','right_select',1);\"" ));
        //全て削除
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','left_select','right_select',1);\"" ));
        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("left_select"),
                                   "RIGHT_PART"  => $objForm->ge("right_select"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));
        /* ヘッダ */
        $arg["info"]    = array("TOP"        =>  $model->year."年度  "
                                                .$model->control_data["学期名"][$model->semester]
                                                ."  対象クラス  ".$model->Hrname,
                                "LEFT_LIST"  => "対象者一覧",
                                "RIGHT_LIST" => $model->sch_label."一覧");
        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "selectdata"));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "REPLACEHIDDENDATE",
                            "value"     => $RowH["DATE"]));

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf010aSubForm3.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array();
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
