<?php

require_once('for_php7.php');

class knjf010aSubForm2
{
    public function main(&$model)
    {
        $objForm        = new form();
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjf010aindex.php", "", "sel");

        //生徒一覧
        $opt_left = $opt_right = array();
        //置換処理選択時の生徒の情報
        $array = explode(",", $model->replace_data["selectdata"]);
        if ($array[0]=="") {
            $array[0] = $model->schregno;
        }
        //生徒情報
        if (!isset($model->warning) && isset($model->schregno)) {
            $RowH = knjf010aQuery::getMedexamHdat($model);      //生徒健康診断ヘッダデータ取得
            $RowD = knjf010aQuery::getMedexamDetDat($model);   //生徒健康診断詳細データ取得
        } else {
            $RowD =& $model->replace_data["det_field"];
            $RowH =& $model->replace_data["det_field"];
        }

        $db = Query::dbCheckOut();

        //結核の間接撮影、所見、その他検査 非表示
        if ($model->school_kind == "H") {
            $arg["tbFilmShow"] = 1;
        } else {
            $arg["tbFilmUnShow"] = 1;
        }

        if ($model->isMie) {
            $arg["is_mie"] = "1";
            $arg["is_not_mie"] = "";
        } else {
            $arg["is_mie"] = "";
            $arg["is_not_mie"] = "1";
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

        //レイアウトの切り替え
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            $arg["new"] = 1;

            //チェックボックス
            for ($i=0; $i<16; $i++) {
                if ($i==15) {
                    $objForm->ae(array("type"       => "checkbox",
                                        "name"      => "RCHECK".$i,
                                        "value"     => "1",
                                        "checked"   => (($model->replace_data["check"][$i] == "1") ? 1 : 0),
                                        "extrahtml" => "onClick=\"return check_all(this);\""));
                    $arg["data"]["RCHECK".$i] = $objForm->ge("RCHECK".$i);

                    knjCreateHidden($objForm, "CHECK_ALL", "RCHECK".$i);
                } else {
                    $objForm->ae(array("type"       => "checkbox",
                                       "name"       => "RCHECK".$i,
                                       "value"      => "1",
                                       "checked"    => (($model->replace_data["check"][$i] == "1") ? 1 : 0)));
                    $arg["data"]["RCHECK".$i] = $objForm->ge("RCHECK".$i);
                }
            }
        } elseif ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") {
            $arg["new2"] = 1;
            $arg["Ippan".$model->Properties["printKenkouSindanIppan"]] = "1";

            //チェックボックス
            for ($i=0; $i<15; $i++) {
                if ($i==14) {
                    $objForm->ae(array("type"       => "checkbox",
                                        "name"      => "RCHECK".$i,
                                        "value"     => "1",
                                        "checked"   => (($model->replace_data["check"][$i] == "1") ? 1 : 0),
                                        "extrahtml" => "onClick=\"return check_all(this);\""));
                    $arg["data"]["RCHECK".$i] = $objForm->ge("RCHECK".$i);

                    knjCreateHidden($objForm, "CHECK_ALL", "RCHECK".$i);
                } else {
                    $objForm->ae(array("type"       => "checkbox",
                                       "name"       => "RCHECK".$i,
                                       "value"      => "1",
                                       "checked"    => (($model->replace_data["check"][$i] == "1") ? 1 : 0)));
                    $arg["data"]["RCHECK".$i] = $objForm->ge("RCHECK".$i);
                }
            }
        } else {
            $arg["base"] = 1;

            //チェックボックス
            for ($i=0; $i<18; $i++) {
                if ($i==17) {
                    $objForm->ae(array("type"       => "checkbox",
                                        "name"      => "RCHECK".$i,
                                        "value"     => "1",
                                        "checked"   => (($model->replace_data["check"][$i] == "1") ? 1 : 0),
                                        "extrahtml" => "onClick=\"return check_all(this);\""));
                    $arg["data"]["RCHECK".$i] = $objForm->ge("RCHECK".$i);

                    knjCreateHidden($objForm, "CHECK_ALL", "RCHECK".$i);
                } else {
                    $objForm->ae(array("type"       => "checkbox",
                                       "name"       => "RCHECK".$i,
                                       "value"      => "1",
                                       "checked"    => (($model->replace_data["check"][$i] == "1") ? 1 : 0)));
                    $arg["data"]["RCHECK".$i] = $objForm->ge("RCHECK".$i);
                }
            }
        }

        /* 編集項目 */
        //尿
        $extra = "style=\"width:250px;\"";
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
        //尿・その他の検査
        $objForm->ae(array("type"        => "text",
                            "name"        => "URICOTHERTEST",
                            "size"        => 40,
                            "maxlength"   => 20,
                            "extrahtml"   => "",
                            "value"       => $RowD["URICOTHERTEST"] ));
        $arg["data"]["URICOTHERTEST"] = $objForm->ge("URICOTHERTEST");
        //尿:指導区分コンボ
        $result     = $db->query(knjf010aQuery::getUriAdvisecd($model));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "style=\"width:250px;\"";
        $arg["data"]["URI_ADVISECD"] = knjCreateCombo($objForm, "URI_ADVISECD", $RowD["URI_ADVISECD"], $opt, $extra, 1);
        //栄養状態コンボ
        $result     = $db->query(knjf010aQuery::getNutrition($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //hidden
            knjCreateHidden($objForm, "NUTRITION_SPARE2_{$row["NAMECD2"]}", $row["NAMESPARE2"]);

            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();

        $extra = "style=\"width:250px;\" onChange=\"disableChange(this, 'NUTRITION_SPARE2_', document.forms[0].NUTRITIONCD_REMARK)\"";
        $arg["data"]["NUTRITIONCD"] = knjCreateCombo($objForm, "NUTRITIONCD", $RowD["NUTRITIONCD"], $opt, $extra, 1);

        //栄養状態テキスト
        if ($model->nutritionSpare2[$RowD["NUTRITIONCD"]] != "1") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["NUTRITIONCD_REMARK"] = knjCreateTextBox($objForm, $RowD["NUTRITIONCD_REMARK"], "NUTRITIONCD_REMARK", 40, 20, $extra);

        //目の疾病及び異常コンボ
        $result     = $db->query(knjf010aQuery::getEyedisease($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $extra = "style=\"width:250px;\"";
        } else {
            $extra = "style=\"width:250px;\" onChange=\"syokenNyuryoku(this, document.forms[0].EYE_TEST_RESULT)\"";
        }
        $arg["data"]["EYEDISEASECD"] = knjCreateCombo($objForm, "EYEDISEASECD", $RowD["EYEDISEASECD"], $opt, $extra, 1);
        $arg["data"]["EYEDISEASECD2"] = knjCreateCombo($objForm, "EYEDISEASECD2", $RowD["EYEDISEASECD2"], $opt, $extra, 1);
        $arg["data"]["EYEDISEASECD3"] = knjCreateCombo($objForm, "EYEDISEASECD3", $RowD["EYEDISEASECD3"], $opt, $extra, 1);
        $arg["data"]["EYEDISEASECD4"] = knjCreateCombo($objForm, "EYEDISEASECD4", $RowD["EYEDISEASECD4"], $opt, $extra, 1);

        //眼科検診結果
        if ((int)$RowD["EYEDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["EYE_TEST_RESULT"] = knjCreateTextBox($objForm, $RowD["EYE_TEST_RESULT"], "EYE_TEST_RESULT", 40, 20, $extra);
        $arg["data"]["EYE_TEST_RESULT2"] = knjCreateTextBox($objForm, $RowD["EYE_TEST_RESULT2"], "EYE_TEST_RESULT2", 40, 20, $extra);
        $arg["data"]["EYE_TEST_RESULT3"] = knjCreateTextBox($objForm, $RowD["EYE_TEST_RESULT3"], "EYE_TEST_RESULT3", 40, 20, $extra);

        //脊柱・胸部コンボ
        $result     = $db->query(knjf010aQuery::getSpinerib($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $extra = "style=\"width:250px;\"";
        } else {
            $extra = "style=\"width:250px;\" onChange=\"syokenNyuryoku(this, document.forms[0].SPINERIBCD_REMARK)\"";
        }
        $objForm->ae(array("type"        => "select",
                            "name"        => "SPINERIBCD",
                            "size"        => "1",
                            "extrahtml"   => $extra,
                            "value"       => $RowD["SPINERIBCD"],
                            "options"     => $opt ));
        $arg["data"]["SPINERIBCD"] = $objForm->ge("SPINERIBCD");

        //脊柱・胸部テキスト
        if ((int)$RowD["SPINERIBCD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["SPINERIBCD_REMARK"] = knjCreateTextBox($objForm, $RowD["SPINERIBCD_REMARK"], "SPINERIBCD_REMARK", 40, 20, $extra);

        //耳鼻咽頭疾患コンボ
        $result     = $db->query(knjf010aQuery::getNosedisease($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $extra = "style=\"width:250px;\"";
        } else {
            $extra = "style=\"width:250px;\" onChange=\"syokenNyuryoku(this, document.forms[0].NOSEDISEASECD_REMARK)\"";
        }
        $arg["data"]["NOSEDISEASECD"] = knjCreateCombo($objForm, "NOSEDISEASECD", $RowD["NOSEDISEASECD"], $opt, $extra, 1);
        $arg["data"]["NOSEDISEASECD2"] = knjCreateCombo($objForm, "NOSEDISEASECD2", $RowD["NOSEDISEASECD2"], $opt, $extra, 1);
        $arg["data"]["NOSEDISEASECD3"] = knjCreateCombo($objForm, "NOSEDISEASECD3", $RowD["NOSEDISEASECD3"], $opt, $extra, 1);
        $arg["data"]["NOSEDISEASECD4"] = knjCreateCombo($objForm, "NOSEDISEASECD4", $RowD["NOSEDISEASECD4"], $opt, $extra, 1);

        //耳鼻咽頭疾患テキスト
        if ((int)$RowD["NOSEDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["NOSEDISEASECD_REMARK"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK"], "NOSEDISEASECD_REMARK", 40, 20, $extra);
        $arg["data"]["NOSEDISEASECD_REMARK2"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK2"], "NOSEDISEASECD_REMARK2", 40, 20, $extra);
        $arg["data"]["NOSEDISEASECD_REMARK3"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK3"], "NOSEDISEASECD_REMARK3", 40, 20, $extra);

        //皮膚疾患コンボ
        $result     = $db->query(knjf010aQuery::getSkindisease($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $extra = "style=\"width:250px;\"";
        } else {
            $extra = "style=\"width:250px;\" onChange=\"syokenNyuryoku(this, document.forms[0].SKINDISEASECD_REMARK)\"";
        }
        $objForm->ae(array("type"        => "select",
                            "name"        => "SKINDISEASECD",
                            "size"        => "1",
                            "extrahtml"   => $extra,
                            "value"       => $RowD["SKINDISEASECD"],
                            "options"     => $opt ));
        $arg["data"]["SKINDISEASECD"] = $objForm->ge("SKINDISEASECD");
        
        //皮膚疾患テキスト
        if ((int)$RowD["SKINDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["SKINDISEASECD_REMARK"] = knjCreateTextBox($objForm, $RowD["SKINDISEASECD_REMARK"], "SKINDISEASECD_REMARK", 40, 20, $extra);

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
        $arg["data"]["TB_OTHERTEST_REMARK1"] = knjCreateTextBox($objForm, $RowD["TB_OTHERTEST_REMARK1"], "TB_OTHERTEST_REMARK1", 40, 20, $extra);

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
        if ($model->Properties["printKenkouSindanIppan"] != "2" && $arg["is_not_mie"] == "1") {
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
        if ($arg["is_not_mie"] == "1") {
            //結核・病名テキスト
            if ((int)$RowD["TB_NAMECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
                $extra = "disabled";
            } else {
                $extra = "";
            }
            $arg["data"]["TB_NAME_REMARK1"] = knjCreateTextBox($objForm, $RowD["TB_NAME_REMARK1"], "TB_NAME_REMARK1", 40, 20, $extra);
        }
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
        $arg["data"]["TB_ADVISE_REMARK1"] = knjCreateTextBox($objForm, $RowD["TB_ADVISE_REMARK1"], "TB_ADVISE_REMARK1", 40, 20, $extra);

        //心臓・臨床医学的検査コンボ
        $result     = $db->query(knjf010aQuery::getHeartMedexam($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $extra = "style=\"width:250px;\"";
        } else {
            $extra = "style=\"width:250px;\" onChange=\"syokenNyuryoku(this, document.forms[0].HEART_MEDEXAM_REMARK)\"";
        }
        $objForm->ae(array("type"        => "select",
                            "name"        => "HEART_MEDEXAM",
                            "size"        => "1",
                            "extrahtml"   => $extra,
                            "value"       => $RowD["HEART_MEDEXAM"],
                            "options"     => $opt ));
        $arg["data"]["HEART_MEDEXAM"] = $objForm->ge("HEART_MEDEXAM");

        //心臓・臨床医学的検査テキスト
        if ((int)$RowD["HEART_MEDEXAM"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["HEART_MEDEXAM_REMARK"] = knjCreateTextBox($objForm, $RowD["HEART_MEDEXAM_REMARK"], "HEART_MEDEXAM_REMARK", 40, 40, $extra);

        if ($model->isKoma) {
            $arg["is_koma"] = "1";
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data"]["HEART_GRAPH_NO"] = knjCreateTextBox($objForm, $RowD["HEART_GRAPH_NO"], "HEART_GRAPH_NO", 12, 12, $extra.$entMove);
        }

        //心臓・疾病及び異常コンボ
        $result     = $db->query(knjf010aQuery::getHeartdisease($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $extra = "style=\"width:250px;\"";
        } else {
            $extra = "style=\"width:250px;\" onChange=\"syokenNyuryoku(this, document.forms[0].HEARTDISEASECD_REMARK)\"";
        }
        $objForm->ae(array("type"        => "select",
                            "name"        => "HEARTDISEASECD",
                            "size"        => "1",
                            "extrahtml"   => $extra,
                            "value"       => $RowD["HEARTDISEASECD"],
                            "options"     => $opt ));
        $arg["data"]["HEARTDISEASECD"] = $objForm->ge("HEARTDISEASECD");

        //心臓・疾病及び異常テキスト
        if ((int)$RowD["HEARTDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["HEARTDISEASECD_REMARK"] = knjCreateTextBox($objForm, $RowD["HEARTDISEASECD_REMARK"], "HEARTDISEASECD_REMARK", 40, 20, $extra);

        //心臓・管理区分
        $result     = $db->query(knjf010aQuery::getManagementDiv($model));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $objForm->ae(array("type"        => "select",
                            "name"        => "MANAGEMENT_DIV",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"",
                            "value"       => $RowD["MANAGEMENT_DIV"],
                            "options"     => $opt ));
        $arg["data"]["MANAGEMENT_DIV"] = $objForm->ge("MANAGEMENT_DIV");

        //心臓・管理区分テキスト
        $arg["data"]["MANAGEMENT_REMARK"] = knjCreateTextBox($objForm, $RowD["MANAGEMENT_REMARK"], "MANAGEMENT_REMARK", 40, 20, "");


        /******************/
        /* リストtoリスト */
        /******************/
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
        View::toHTML($model, "knjf010aSubForm2.html", $arg);
    }
}
