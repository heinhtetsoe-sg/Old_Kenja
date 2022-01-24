<?php

require_once('for_php7.php');

class knjf012jForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjf012jindex.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //Windowサイズ
        $arg["WindowWidth"]      = $model->windowWidth  -  36;
        $arg["titleWindowWidth"] = $model->windowWidth  - 237;
        $arg["valWindowWidth"]   = $model->windowWidth  - 220;
        $arg["valWindowHeight"]  = $model->windowHeight - 210;
        $arg["tcolWindowHeight"] = $model->windowHeight - 227;
        $resizeFlg = $model->cmd == "cmdStart" ? true : false;

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $arg["SEMESTERNAME"] = CTRL_SEMESTERNAME;

        //年組
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjf012jQuery::getHrClass($model);
        $arg["HR_CLASS"] = makeCmbReturn($objForm, $arg, $db, $query, $model->hr_class, "HR_CLASS", $extra, 1, "BLANK");

        //種類
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjf012jQuery::getInputForm();
        $arg["INPUT_FORM"] = makeCmbReturn($objForm, $arg, $db, $query, $model->input_form, "INPUT_FORM", $extra, 1, "");

        //聴力のサイズ調整
        $ear_width = "650";
        $input_form1_width = "1900";

        //座高ありのとき
        if ($model->Properties["is_sitheight"] == "1") {
            $arg["is_sitheight"]  = "1";
            $input_form1_width = "2010";
        }
        if ($model->z010name1 == "fukuiken" || ($model->Properties["useSpecial_Support_School"] != "1" && $model->Properties["unEar_db"] == "1")) {
            if ($model->z010name1 == "koma") {
                $ear_width = "550";
                $input_form1_width = "1420";
            } else {
                $ear_width = "350";
                $input_form1_width = "1270";
            }
        }
        $arg["EAR_WIDTH"] = $ear_width;

        //備考
        $input_form3_width = "3380";
        if ($model->Properties["useSpecial_Support_School"] == "1") { //特別支援学校
            $model->maxRemarkByte       = '300';
            $model->maxOtherRemark2Byte = '300';
            $arg["REMARK_MOJI"]         = '100';
            $arg["OTHER_REMARK_WIDTH"]  = '900';
            $arg["OTHER_REMARK2_MOJI"]  = '100、20文字';
            $otherSize                  = '120';
            $input_form3_width = "3880";
        } else {
            $model->maxRemarkByte       = '60';
            $arg["REMARK_MOJI"]         = '60';
            $arg["OTHER_REMARK_WIDTH"]  = '350';
            $otherSize                  = '40';
        }

        //サイズ調整（幅）
        $width_array = array("1" => $input_form1_width, "2" => "7150", "3" => $input_form3_width, "4" => "1180");
        $arg["widthHeader"] = $width_array[$model->input_form] + 50;
        $arg["widthMeisai"] = $width_array[$model->input_form];
        //サイズ調整（高さ）
        $arg["heightHeader"] = "50";
        if ($model->input_form == "2") {
            $arg["heightMeisai"] = "175";
        } elseif ($model->input_form == "1") {
            $arg["heightMeisai"] = "100";
        } elseif ($model->input_form == "3") {
            $arg["heightMeisai"] = "100";
        } else {
            $arg["heightMeisai"] = "80";
        }

        //レイアウトの切り替え
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            for ($i = 1; $i <= 4; $i++) {
                if ($model->input_form == $i) {
                    $arg["inputForm" . $i] = 1;
                }
            }
        }

        //特別支援学校
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $arg["useSpecial_Support_School"] = "1";
        }

        //一覧を取得
        $medexamList = array();
        $height = $weight = "";
        $sepH = $sepW = "";
        if (!isset($model->warning) && $model->hr_class != "") {
            $query = knjf012jQuery::getMedexamList($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $medexamList[] = $row;
                if (strlen($row["HEIGHT"])) {
                    $height .= $sepH.$row["HEIGHT"];
                    $sepH = ",";
                }
                if (strlen($row["WEIGHT"])) {
                    $weight .= $sepW.$row["WEIGHT"];
                    $sepW = ",";
                }
            }
            $result->free();
        }
        knjCreateHidden($objForm, "HEIGHT_LIST", $height);
        knjCreateHidden($objForm, "WEIGHT_LIST", $weight);
        //標準偏差値取得
        $sd = $db->getRow(knjf012jQuery::getSD($model), DB_FETCHMODE_ASSOC);

        //測定評価平均値データ取得
        $phys_avg = array();
        $query = knjf012jQuery::getHexamPhysicalAvgDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $phys_avg[$row["SEX"]][$row["NENREI_YEAR"]] = array("A" => $row["STD_WEIGHT_KEISU_A"], "B" => $row["STD_WEIGHT_KEISU_B"]);
            knjCreateHidden($objForm, "KEISU_A_".$row["SEX"]."_".$row["NENREI_YEAR"], $row["STD_WEIGHT_KEISU_A"]);
            knjCreateHidden($objForm, "KEISU_B_".$row["SEX"]."_".$row["NENREI_YEAR"], $row["STD_WEIGHT_KEISU_B"]);
        }
        $result->free();

        //更新・削除時のチェックでエラーの場合、画面情報をセット
        if (isset($model->warning)) {
            for ($counter = 0; $counter < $model->data_cnt; $counter++) {
                $Row = array();
                foreach ($model->fields as $key => $val) {
                    $Row[$key] = $val[$counter];
                }
                $medexamList[] = $Row;
            }
        }

        //講座一覧の表示件数
        knjCreateHidden($objForm, "DATA_CNT", get_count($medexamList));

        //名称マスタより取得するコード一覧
        if ($model->input_form == "1") {
            $nameM["F010"] = "";
            $nameM["F017"] = "CD_NASHI";
        } elseif ($model->input_form == "2") {
            $nameM["F030"] = "";
            $nameM["F040"] = "";
            $nameM["F041"] = "";
            $nameM["F050"] = "";
            $nameM["F051"] = "";
            $nameM["F060"] = "";
            $nameM["F061"] = "";
            $nameM["F062"] = "";
            $nameM["F063"] = "";
            $nameM["F070"] = "";
            $nameM["F100"] = "";
            $nameM["F101"] = "";
            $nameM["F110"] = "";
            $nameM["F120"] = "";
            $nameM["F130"] = "";
            $nameM["F080"] = "";
            $nameM["F090"] = "";
            $nameM["F091"] = "";
        } elseif ($model->input_form == "3") {
            $nameM["F020"] = "CD_NASHI";
            $nameM["F019"] = "CD_NASHI";
            $nameM["F018"] = "CD_NASHI";
            $nameM["F021"] = "";
            $nameM["F023"] = "";
            $nameM["F140"] = "";
            $nameM["F145"] = "";
            $nameM["F146"] = "";
            $nameM["F144"] = "";
            $nameM["F150"] = "";
            $nameM["F151"] = "";
        } elseif ($model->input_form == "4") {
            $nameM["F143"] = "";
            $nameM["F141"] = "3";
            $nameM["F142"] = "";
        }
        foreach ($nameM as $namecd1 => $flg) {
            //名称マスタよりデータ取得・格納
            $query = knjf012jQuery::getNameMst($model, $namecd1, $flg);
            $optname = "opt".$namecd1;
            $$optname = makeArrayReturn($db, $query);
        }

        if ($model->input_form == "2") {
            //結核の画像番号のサイズ
            $arg["TB_FILMNO_SIZE"] = $model->tb_filmnoFieldSize;
        }

        //一覧を表示
        foreach ($medexamList as $counter => $Row) {
            //年齢取得
            $birthday = intval(str_replace('-', '', $Row["BIRTHDAY"]));
            $today = intval(CTRL_YEAR.'0401');
            $age = intval(($today - $birthday) / 10000);

            //身長別標準体重
            $std_weight = ($Row["HEIGHT"]) ? round(($phys_avg[$Row["SEX"]][$age]["A"] * $Row["HEIGHT"] - $phys_avg[$Row["SEX"]][$age]["B"]), 1) : "";

            //肥満度 = (実測体重 ― 身長別標準体重) ／ 身長別標準体重 × 100（％）
            $obesityIndex = "";
            if ($std_weight > 0) {
                $obesityIndex = round(((int)$Row["WEIGHT"] - $std_weight) / $std_weight * 100, 1);
            }

            //栄養状態NAMECD取得
            $isNormalWeight = "";
            $nutrInfo = $sep = '';
            $result     = $db->query(knjf012jQuery::getNutrition($model, ""));
            $opt        = array();
            $opt[]      = $optnull;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $namecd = substr($row["NAMECD2"], 0, 2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);

                if ($row["ABBV1"] == '' && $row["ABBV3"] == '') {
                } else {
                    if ($row["ABBV1"] < $obesityIndex && $obesityIndex < $row["ABBV3"] && $Row["NUTRITIONCD"] == "" && $Row["HEIGHT"] != null && $Row["WEIGHT"] != null) {
                        $isNormalWeight = $row["NAMECD2"];
                    }
                    $nutrInfo .= $sep.$row["NAMECD2"].':'.$row["ABBV1"].':'.$row["ABBV3"];
                    $sep = ',';
                }
            }
            knjCreateHidden($objForm, "nutrInfo", $nutrInfo);
            $result->free();

            //初期化
            $setData = array();

            //学籍番号(KEY)
            knjCreateHidden($objForm, "REGD_SCHREGNO"."-".$counter, $Row["REGD_SCHREGNO"]);
            //学校種別(P,J,H)
            knjCreateHidden($objForm, "SCHOOL_KIND"."-".$counter, $Row["SCHOOL_KIND"]);

            //出席番号(No.)
            //氏名
            $setData["ATTENDNO"] = $Row["ATTENDNO"];
            $setData["NAME_SHOW"] = $Row["NAME_SHOW"];
            knjCreateHidden($objForm, "ATTENDNO"."-".$counter, $Row["ATTENDNO"]);
            knjCreateHidden($objForm, "NAME_SHOW"."-".$counter, $Row["NAME_SHOW"]);

            //共通------------------------------------------

            //健康診断実施日付
            $Row["DATE"] = str_replace("-", "/", $Row["DATE"]);
            $setData["DATE"] = View::popUpCalendar($objForm, "DATE"."-".$counter, $Row["DATE"]);
            knjCreateHidden($objForm, "HIDDENDATE"."-".$counter, $Row["DATE"]);

            //項目1-----------------------------------------

            if ($model->input_form == "1") {
                $arg["inputForm1"] = "1";
                //身長
                $colorH = ""; // (-2.0 > $sd["H_STDDEV"] || $sd["H_STDDEV"] > 2.0) ? "red" : "";
                $id = "height-".$counter;
                $extra = "onblur=\"return Num_Check(this);\" id=\"{$id}\" style=\"color:{$colorH};\"";
                $setData["HEIGHT"] = knjCreateTextBox($objForm, $Row["HEIGHT"], "HEIGHT"."-".$counter, 5, 5, $extra);
                //体重
                $colorW = ""; // (-2.0 > $sd["W_STDDEV"] || $sd["W_STDDEV"] > 2.0) ? "red" : "";
                $id = "weight-".$counter;
                $extra = "onblur=\"return Num_Check(this);\" id=\"{$id}\" style=\"color:{$colorW};\"";
                $setData["WEIGHT"] = knjCreateTextBox($objForm, $Row["WEIGHT"], "WEIGHT"."-".$counter, 5, 5, $extra);

                //年齢取得
                $birthday = intval(str_replace('-', '', $Row["BIRTHDAY"]));
                $today = intval(CTRL_YEAR.'0401');
                $age = intval(($today - $birthday) / 10000);
                knjCreateHidden($objForm, "SEX"."-".$counter, $Row["SEX"]);
                knjCreateHidden($objForm, "AGE"."-".$counter, $age);

                //身長別標準体重
                $id = "std_weight-".$counter;
                $setData["STD_WEIGHT"] = "<span id={$id}>".$std_weight."</span>";

                //肥満度 = (実測体重 ― 身長別標準体重) ／ 身長別標準体重 × 100（％）
                if ($std_weight > 0) {
                    $setData["OBESITY_INDEX"] = $obesityIndex.'%';
                }

                //座高
                $extra = "onblur=\"return Num_Check(this);\"";
                $setData["SITHEIGHT"] = knjCreateTextBox($objForm, $Row["SITHEIGHT"], "SITHEIGHT"."-".$counter, 5, 5, $extra);

                //視力・測定困難チェック時は、視力情報はグレーアウト
                $disVisionR = ($Row["R_VISION_CANTMEASURE"] == "1") ? " disabled": "";
                $disVisionL = ($Row["L_VISION_CANTMEASURE"] == "1") ? " disabled": "";

                $extra = "";
                //視力・右裸眼(文字)
                //視力・左裸眼(文字)
                $setData["R_BAREVISION_MARK"] = makeCmbReturn2($objForm, $optF017, $Row["R_BAREVISION_MARK"], "R_BAREVISION_MARK"."-".$counter, $extra.$disVisionR, 1, "BLANK");
                $setData["L_BAREVISION_MARK"] = makeCmbReturn2($objForm, $optF017, $Row["L_BAREVISION_MARK"], "L_BAREVISION_MARK"."-".$counter, $extra.$disVisionL, 1, "BLANK");

                //視力・右矯正(文字)
                //視力・左矯正(文字)
                $setData["R_VISION_MARK"]     = makeCmbReturn2($objForm, $optF017, $Row["R_VISION_MARK"], "R_VISION_MARK"."-".$counter, $extra.$disVisionR, 1, "BLANK");
                $setData["L_VISION_MARK"]     = makeCmbReturn2($objForm, $optF017, $Row["L_VISION_MARK"], "L_VISION_MARK"."-".$counter, $extra.$disVisionL, 1, "BLANK");

                //視力・右裸眼(数字)
                $extra = "onblur=\"return Num_Check(this);\" id=\"R_BAREVISION-{$counter}\"";
                $setData["R_BAREVISION"] = knjCreateTextBox($objForm, $Row["R_BAREVISION"], "R_BAREVISION"."-".$counter, 5, 5, $extra.$disVisionR);
                //視力・右矯正(数字)
                $extra = "onblur=\"return Num_Check(this);\" id=\"R_VISION-{$counter}\"";
                $setData["R_VISION"]     = knjCreateTextBox($objForm, $Row["R_VISION"], "R_VISION"."-".$counter, 5, 5, $extra.$disVisionR);
                //視力・左裸眼(数字)
                $extra = "onblur=\"return Num_Check(this);\" id=\"L_BAREVISION-{$counter}\"";
                $setData["L_BAREVISION"] = knjCreateTextBox($objForm, $Row["L_BAREVISION"], "L_BAREVISION"."-".$counter, 5, 5, $extra.$disVisionL);
                //視力・左矯正(数字)
                $extra = "onblur=\"return Num_Check(this);\" id=\"L_VISION-{$counter}\"";
                $setData["L_VISION"]     = knjCreateTextBox($objForm, $Row["L_VISION"], "L_VISION"."-".$counter, 5, 5, $extra.$disVisionL);

                //測定不能チェックボックス
                if ($model->field["VISION_CANTMEASURE"] == "on") {
                    $check_measure = "checked";
                } else {
                    $check_measure = ($Row["VISION_CANTMEASURE"] == "1") ? "checked" : "";
                }
                $extra = " id=\"VISION_CANTMEASURE\"";
                $setData["VISION_CANTMEASURE"] = knjCreateCheckBox($objForm, "VISION_CANTMEASURE"."-".$counter, "on", $check_measure.$extra, "");

                //視力、測定困難(右・数字)
                $checkV_R = ($Row["R_VISION_CANTMEASURE"] == "1") ? " checked" : "";
                $extra = "id=\"R_VISION_CANTMEASURE-{$counter}\" onClick=\"disVision(this, 'right', '{$counter}');\"";
                $setData["R_VISION_CANTMEASURE"] = knjCreateCheckBox($objForm, "R_VISION_CANTMEASURE"."-".$counter, "1", $extra.$checkV_R);

                //視力、測定困難(左・数字)
                $checkV_L = ($Row["L_VISION_CANTMEASURE"] == "1") ? " checked" : "";
                $extra = "id=\"L_VISION_CANTMEASURE-{$counter}\" onClick=\"disVision(this, 'reft', '{$counter}');\"";
                $setData["L_VISION_CANTMEASURE"] = knjCreateCheckBox($objForm, "L_VISION_CANTMEASURE"."-".$counter, "1", $extra.$checkV_L);

                //視力、測定困難(右・記号)
                $checkV_R = ($Row["R_VISION_CANTMEASURE"] == "1") ? " checked" : "";
                $extra = "id=\"R_VISION_MARK_CANTMEASURE-{$counter}\" onClick=\"disVision(this, 'right', '{$counter}');\"";
                $setData["R_VISION_MARK_CANTMEASURE"] = knjCreateCheckBox($objForm, "R_VISION_MARK_CANTMEASURE"."-".$counter, "1", $extra.$checkV_R);

                //視力、測定困難(左・記号)
                $checkV_L = ($Row["L_VISION_CANTMEASURE"] == "1") ? " checked" : "";
                $extra = "id=\"L_VISION_MARK_CANTMEASURE-{$counter}\" onClick=\"disVision(this, 'reft', '{$counter}');\"";
                $setData["L_VISION_MARK_CANTMEASURE"] = knjCreateCheckBox($objForm, "L_VISION_MARK_CANTMEASURE"."-".$counter, "1", $extra.$checkV_L);

                //聴力・測定困難チェック時は、聴力情報はグレーアウト
                $disEarR = ($Row["R_EAR_CANTMEASURE"] == "1") ? " disabled": "";
                $disEarL = ($Row["L_EAR_CANTMEASURE"] == "1") ? " disabled": "";

                //聴力・右DB
                //聴力・左DB
                $extra = "onblur=\"this.value=toInteger(this.value)\"";
                $setData["R_EAR_DB"] = knjCreateTextBox($objForm, $Row["R_EAR_DB"], "R_EAR_DB"."-".$counter, 3, 3, $extra.$disEarR);
                $setData["L_EAR_DB"] = knjCreateTextBox($objForm, $Row["L_EAR_DB"], "L_EAR_DB"."-".$counter, 3, 3, $extra.$disEarL);

                //聴力・右状態
                //聴力・左状態
                $extra ="";
                $setData["R_EAR"] = makeCmbReturn2($objForm, $optF010, $Row["R_EAR"], "R_EAR"."-".$counter, $extra.$disEarR, 1, "BLANK");
                $setData["L_EAR"] = makeCmbReturn2($objForm, $optF010, $Row["L_EAR"], "L_EAR"."-".$counter, $extra.$disEarL, 1, "BLANK");

                //聴力、測定困難(右)
                $checkE_R = ($Row["R_EAR_CANTMEASURE"] == "1") ? " checked" : "";
                $extra = "id=\"R_EAR_CANTMEASURE-{$counter}\" onClick=\"disEar(this, 'right', '{$counter}');\"";
                $setData["R_EAR_CANTMEASURE"] = knjCreateCheckBox($objForm, "R_EAR_CANTMEASURE"."-".$counter, "1", $extra.$checkE_R);

                //聴力、測定困難(左)
                $checkE_L = ($Row["L_EAR_CANTMEASURE"] == "1") ? " checked" : "";
                $extra = "id=\"L_EAR_CANTMEASURE-{$counter}\" onClick=\"disEar(this, 'reft', '{$counter}');\"";
                $setData["L_EAR_CANTMEASURE"] = knjCreateCheckBox($objForm, "L_EAR_CANTMEASURE"."-".$counter, "1", $extra.$checkE_L);

                $setData["COUNTER"] = $counter;
            }

            //項目2-----------------------------------------

            if ($model->input_form == "2") {
                $arg["inputForm2"] = "1";

                //栄養状態
                $disableCodes = getDisabledCodes($db, "F030");

                $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, 'NUTRITIONCD_REMARK', '{$counter}','".implode(",", $disableCodes)."')\"";
                if ($isNormalWeight != "") {
                    $Row["NUTRITIONCD"] = $isNormalWeight;
                }
                $setData["NUTRITIONCD"] = makeCmbReturn2($objForm, $optF030, $Row["NUTRITIONCD"], "NUTRITIONCD"."-".$counter, $extra, 1, "BLANK");
                $extra  = "STYLE=\"WIDTH:100%\"";

                if (in_array($Row["NUTRITIONCD"], $disableCodes) == true || $Row["NUTRITIONCD"] == '') {
                    $extra .= "disabled";
                } else {
                    $extra .= "";
                }
                $setData["NUTRITIONCD_REMARK"] = knjCreateTextBox($objForm, $Row["NUTRITIONCD_REMARK"], "NUTRITIONCD_REMARK"."-".$counter, 40, 20, $extra);

                //脊柱・胸郭・四肢(全般コンボ)
                $disableCodes = getDisabledCodes($db, "F040");
                $target_objs = "SPINERIBCD1,SPINERIBCD2,SPINERIBCD3,SPINERIBCD_REMARK,SPINERIBCD_REMARK1,SPINERIBCD_REMARK2,SPINERIBCD_REMARK3";
                $text_objs   = "SPINERIBCD_REMARK1,SPINERIBCD_REMARK2,SPINERIBCD_REMARK3";
                $combo_objs   = "SPINERIBCD1,SPINERIBCD2,SPINERIBCD3";
                $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku2(this, '".$target_objs."', '{$counter}','".implode(",", $disableCodes)."', '".$text_objs."','".implode(",", getDisabledCodes($db, "F041"))."', '".$combo_objs."')\"";
                $setData["SPINERIBCD"] = makeCmbReturn2($objForm, $optF040, $Row["SPINERIBCD"], "SPINERIBCD"."-".$counter, $extra, 1, "BLANK");
                $extra  = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";

                $disableCodes = getDisabledCodes($db, "F041");
                //脊柱・胸郭・四肢(全般コンボが未選択、未受検、異常なしのとき)
                if (in_array($Row["SPINERIBCD"], getDisabledCodes($db, "F040")) == true || $Row["SPINERIBCD"] == '') {
                    //脊柱・胸郭・四肢(疾患1コンボ)
                    $extra = "style=\"width:170px;\" disabled onChange=\"syokenNyuryoku(this, 'SPINERIBCD_REMARK1', '{$counter}', '".implode(",", $disableCodes)."')\"";
                    $setData["SPINERIBCD1"] = makeCmbReturn2($objForm, $optF041, $Row["SPINERIBCD1"], "SPINERIBCD1"."-".$counter, $extra, 1, "BLANK");

                    //脊柱・胸郭・四肢(疾患2コンボ)
                    $extra = "style=\"width:170px;\" disabled onChange=\"syokenNyuryoku(this, 'SPINERIBCD_REMARK2', '{$counter}', '".implode(",", $disableCodes)."')\"";
                    $setData["SPINERIBCD2"] = makeCmbReturn2($objForm, $optF041, $Row["SPINERIBCD2"], "SPINERIBCD2"."-".$counter, $extra, 1, "BLANK");

                    //脊柱・胸郭・四肢(疾患3コンボ)
                    $extra = "style=\"width:170px;\" disabled onChange=\"syokenNyuryoku(this, 'SPINERIBCD_REMARK3', '{$counter}', '".implode(",", $disableCodes)."')\"";
                    $setData["SPINERIBCD3"] = makeCmbReturn2($objForm, $optF041, $Row["SPINERIBCD3"], "SPINERIBCD3"."-".$counter, $extra, 1, "BLANK");
                } else {
                    //それ以外の時
                    //脊柱・胸郭・四肢(疾患1コンボ)
                    $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, 'SPINERIBCD_REMARK1', '{$counter}', '".implode(",", $disableCodes)."')\"";
                    $setData["SPINERIBCD1"] = makeCmbReturn2($objForm, $optF041, $Row["SPINERIBCD1"], "SPINERIBCD1"."-".$counter, $extra, 1, "BLANK");

                    //脊柱・胸郭・四肢(疾患2コンボ)
                    $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, 'SPINERIBCD_REMARK2', '{$counter}', '".implode(",", $disableCodes)."')\"";
                    $setData["SPINERIBCD2"] = makeCmbReturn2($objForm, $optF041, $Row["SPINERIBCD2"], "SPINERIBCD2"."-".$counter, $extra, 1, "BLANK");

                    //脊柱・胸郭・四肢(疾患3コンボ)
                    $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, 'SPINERIBCD_REMARK3', '{$counter}', '".implode(",", $disableCodes)."')\"";
                    $setData["SPINERIBCD3"] = makeCmbReturn2($objForm, $optF041, $Row["SPINERIBCD3"], "SPINERIBCD3"."-".$counter, $extra, 1, "BLANK");
                }
                //脊柱・胸郭・四肢(疾患1テキスト)
                $extra  = "STYLE=\"WIDTH:100%\"";
                if (in_array($Row["SPINERIBCD1"], $disableCodes) == true || $Row["SPINERIBCD1"] == '') {
                    $extra .= "disabled";
                } else {
                    $extra .= "";
                }
                $setData["SPINERIBCD_REMARK1"] = knjCreateTextBox($objForm, $Row["SPINERIBCD_REMARK1"], "SPINERIBCD_REMARK1"."-".$counter, 40, 20, $extra);

                //脊柱・胸郭・四肢(疾患2テキスト)
                $extra  = "STYLE=\"WIDTH:100%\"";
                if (in_array($Row["SPINERIBCD2"], $disableCodes) == true || $Row["SPINERIBCD2"] == '') {
                    $extra .= "disabled";
                } else {
                    $extra .= "";
                }
                $setData["SPINERIBCD_REMARK2"] = knjCreateTextBox($objForm, $Row["SPINERIBCD_REMARK2"], "SPINERIBCD_REMARK2"."-".$counter, 40, 20, $extra);

                //脊柱・胸郭・四肢(疾患3テキスト)
                $extra  = "STYLE=\"WIDTH:100%\"";
                if (in_array($Row["SPINERIBCD3"], $disableCodes) == true || $Row["SPINERIBCD3"] == '') {
                    $extra .= "disabled";
                } else {
                    $extra .= "";
                }
                $setData["SPINERIBCD_REMARK3"] = knjCreateTextBox($objForm, $Row["SPINERIBCD_REMARK3"], "SPINERIBCD_REMARK3"."-".$counter, 40, 20, $extra);


                //脊柱・胸郭・四肢の全般がdisabled設定のとき
                if (in_array($Row["SPINERIBCD"], getDisabledCodes($db, "F040")) == true || $Row["SPINERIBCD"] == '') {
                    $extra = "STYLE=\"WIDTH:100%\" disabled";
                    $setData["SPINERIBCD_REMARK1"] = knjCreateTextBox($objForm, $Row["SPINERIBCD_REMARK1"], "SPINERIBCD_REMARK1"."-".$counter, 40, 20, $extra);
                    $setData["SPINERIBCD_REMARK2"] = knjCreateTextBox($objForm, $Row["SPINERIBCD_REMARK2"], "SPINERIBCD_REMARK2"."-".$counter, 40, 20, $extra);
                    $setData["SPINERIBCD_REMARK3"] = knjCreateTextBox($objForm, $Row["SPINERIBCD_REMARK3"], "SPINERIBCD_REMARK3"."-".$counter, 40, 20, $extra);
                } else {
                    $extra = "STYLE=\"WIDTH:100%\"";
                }
                //脊柱・胸郭・四肢(全般テキスト)
                $setData["SPINERIBCD_REMARK"] = knjCreateTextBox($objForm, $Row["SPINERIBCD_REMARK"], "SPINERIBCD_REMARK"."-".$counter, 40, 20, $extra);

                //目の疾病及び異常(コンボ)
                $disableCodes = getDisabledCodes($db, "F050");
                $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, 'EYE_TEST_RESULT', '{$counter}', '".implode(",", $disableCodes)."')\"";
                $setData["EYEDISEASECD"] = makeCmbReturn2($objForm, $optF050, $Row["EYEDISEASECD"], "EYEDISEASECD"."-".$counter, $extra, 1, "BLANK");

                //目の疾病及び異常(テキスト)
                $extra  = "STYLE=\"WIDTH:100%\"";
                if (in_array($Row["EYEDISEASECD"], $disableCodes) == true || $Row["EYEDISEASECD"] == '') {
                    $extra .= "disabled";
                } else {
                    $extra .= "";
                }
                $setData["EYE_TEST_RESULT"] = knjCreateTextBox($objForm, $Row["EYE_TEST_RESULT"], "EYE_TEST_RESULT"."-".$counter, 40, 20, $extra);

                //色覚異常(コンボ)
                $disableCodes = getDisabledCodes($db, "F051");
                $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, 'VISION_CANTMEASURE', '{$counter}', '".implode(",", $disableCodes)."')\"";
                $setData["EYEDISEASECD5"] = makeCmbReturn2($objForm, $optF051, $Row["EYEDISEASECD5"], "EYEDISEASECD5"."-".$counter, $extra, 1, "BLANK");

                //色覚異常(テキスト)
                $extra  = "STYLE=\"WIDTH:100%\"";
                if (in_array($Row["EYEDISEASECD5"], $disableCodes) == true || $Row["EYEDISEASECD5"] == '') {
                    $extra .= "disabled";
                } else {
                    $extra .= "";
                }
                $setData["VISION_CANTMEASURE"] = knjCreateTextBox($objForm, $Row["VISION_CANTMEASURE"], "VISION_CANTMEASURE"."-".$counter, 40, 20, $extra);

                //耳鼻咽頭疾患(全般コンボ)
                $disableCodes = getDisabledCodes($db, "F060");
                $target_objs  = "NOSEDISEASECD5,NOSEDISEASECD6,NOSEDISEASECD7,NOSEDISEASECD_REMARK,NOSEDISEASECD_REMARK1,NOSEDISEASECD_REMARK2,NOSEDISEASECD_REMARK3";
                $text_objs    = "NOSEDISEASECD_REMARK1,NOSEDISEASECD_REMARK2,NOSEDISEASECD_REMARK3";
                $combo_objs   = "NOSEDISEASECD5,NOSEDISEASECD6,NOSEDISEASECD6";
                $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku2(this, '".$target_objs."', '{$counter}','".implode(",", $disableCodes)."', '".$text_objs."','".implode(",", getDisabledCodes($db, "F061"))."', '".$combo_objs."')\"";
                $setData["NOSEDISEASECD"] = makeCmbReturn2($objForm, $optF060, $Row["NOSEDISEASECD"], "NOSEDISEASECD"."-".$counter, $extra, 1, "BLANK");
                $extra  = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";

                $disableCodes = getDisabledCodes($db, "F061");
                ////耳鼻咽頭疾患(全般コンボが未選択、未受検、異常なしのとき)
                if (in_array($Row["NOSEDISEASECD"], getDisabledCodes($db, "F060")) == true || $Row["NOSEDISEASECD"] == '') {
                    //耳鼻咽頭疾患(疾患1コンボ)
                    $extra = "style=\"width:170px;\" disabled onChange=\"syokenNyuryoku(this, 'NOSEDISEASECD_REMARK1', '{$counter}', '".implode(",", $disableCodes)."')\"";
                    $setData["NOSEDISEASECD5"] = makeCmbReturn2($objForm, $optF061, $Row["NOSEDISEASECD5"], "NOSEDISEASECD5"."-".$counter, $extra, 1, "BLANK");

                    //耳鼻咽頭疾患(疾患2コンボ)
                    $extra = "style=\"width:170px;\" disabled onChange=\"syokenNyuryoku(this, 'NOSEDISEASECD_REMARK2', '{$counter}', '".implode(",", $disableCodes)."')\"";
                    $setData["NOSEDISEASECD6"] = makeCmbReturn2($objForm, $optF061, $Row["NOSEDISEASECD6"], "NOSEDISEASECD6"."-".$counter, $extra, 1, "BLANK");

                    //耳鼻咽頭疾患(疾患3コンボ)
                    $extra = "style=\"width:170px;\" disabled onChange=\"syokenNyuryoku(this, 'NOSEDISEASECD_REMARK3', '{$counter}', '".implode(",", $disableCodes)."')\"";
                    $setData["NOSEDISEASECD7"] = makeCmbReturn2($objForm, $optF061, $Row["NOSEDISEASECD7"], "NOSEDISEASECD7"."-".$counter, $extra, 1, "BLANK");
                } else {
                    //それ以外の時
                    //耳鼻咽頭疾患(疾患1コンボ)
                    $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, 'NOSEDISEASECD_REMARK1', '{$counter}', '".implode(",", $disableCodes)."')\"";
                    $setData["NOSEDISEASECD5"] = makeCmbReturn2($objForm, $optF061, $Row["NOSEDISEASECD5"], "NOSEDISEASECD5"."-".$counter, $extra, 1, "BLANK");

                    //耳鼻咽頭疾患(疾患2コンボ)
                    $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, 'NOSEDISEASECD_REMARK2', '{$counter}', '".implode(",", $disableCodes)."')\"";
                    $setData["NOSEDISEASECD6"] = makeCmbReturn2($objForm, $optF061, $Row["NOSEDISEASECD6"], "NOSEDISEASECD6"."-".$counter, $extra, 1, "BLANK");

                    //耳鼻咽頭疾患(疾患3コンボ)
                    $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, 'NOSEDISEASECD_REMARK3', '{$counter}', '".implode(",", $disableCodes)."')\"";
                    $setData["NOSEDISEASECD7"] = makeCmbReturn2($objForm, $optF061, $Row["NOSEDISEASECD7"], "NOSEDISEASECD7"."-".$counter, $extra, 1, "BLANK");
                }

                //耳鼻咽頭疾患(疾患1テキスト)
                $extra  = "STYLE=\"WIDTH:100%\"";
                if (in_array($Row["NOSEDISEASECD5"], $disableCodes) == true || $Row["NOSEDISEASECD5"] == '') {
                    $extra .= "disabled";
                } else {
                    $extra .= "";
                }
                $setData["NOSEDISEASECD_REMARK1"] = knjCreateTextBox($objForm, $Row["NOSEDISEASECD_REMARK1"], "NOSEDISEASECD_REMARK1"."-".$counter, 40, 20, $extra);

                //耳鼻咽頭疾患(疾患2テキスト)
                $extra  = "STYLE=\"WIDTH:100%\"";
                if (in_array($Row["NOSEDISEASECD6"], $disableCodes) == true || $Row["NOSEDISEASECD6"] == '') {
                    $extra .= "disabled";
                } else {
                    $extra .= "";
                }
                $setData["NOSEDISEASECD_REMARK2"] = knjCreateTextBox($objForm, $Row["NOSEDISEASECD_REMARK2"], "NOSEDISEASECD_REMARK2"."-".$counter, 40, 20, $extra);

                //耳鼻咽頭疾患(疾患3テキスト)
                $extra  = "STYLE=\"WIDTH:100%\"";
                if (in_array($Row["NOSEDISEASECD7"], $disableCodes) == true || $Row["NOSEDISEASECD7"] == '') {
                    $extra .= "disabled";
                } else {
                    $extra .= "";
                }
                $setData["NOSEDISEASECD_REMARK3"] = knjCreateTextBox($objForm, $Row["NOSEDISEASECD_REMARK3"], "NOSEDISEASECD_REMARK3"."-".$counter, 40, 20, $extra);


                ////耳鼻咽頭疾患の全般がdisabled設定のとき
                if (in_array($Row["NOSEDISEASECD"], getDisabledCodes($db, "F060")) == true || $Row["NOSEDISEASECD"] == '') {
                    $extra = "STYLE=\"WIDTH:100%\" disabled";
                    $setData["NOSEDISEASECD_REMARK1"] = knjCreateTextBox($objForm, $Row["NOSEDISEASECD_REMARK1"], "NOSEDISEASECD_REMARK1"."-".$counter, 40, 20, $extra);
                    $setData["NOSEDISEASECD_REMARK2"] = knjCreateTextBox($objForm, $Row["NOSEDISEASECD_REMARK2"], "NOSEDISEASECD_REMARK2"."-".$counter, 40, 20, $extra);
                    $setData["NOSEDISEASECD_REMARK3"] = knjCreateTextBox($objForm, $Row["NOSEDISEASECD_REMARK3"], "NOSEDISEASECD_REMARK3"."-".$counter, 40, 20, $extra);
                } else {
                    $extra = "STYLE=\"WIDTH:100%\"";
                }
                //耳鼻咽頭疾患(全般テキスト)
                $setData["NOSEDISEASECD_REMARK"] = knjCreateTextBox($objForm, $Row["NOSEDISEASECD_REMARK"], "NOSEDISEASECD_REMARK"."-".$counter, 40, 20, $extra);

                //皮膚疾患(コンボ)
                $disableCodes = getDisabledCodes($db, "F070");
                $extra = "style=\"width:210px;\" onChange=\"syokenNyuryoku(this, 'SKINDISEASECD_REMARK', '{$counter}', '".implode(",", $disableCodes)."')\"";
                $setData["SKINDISEASECD"] = makeCmbReturn2($objForm, $optF070, $Row["SKINDISEASECD"], "SKINDISEASECD"."-".$counter, $extra, 1, "BLANK");

                //皮膚疾患(テキスト)
                $extra  = "STYLE=\"WIDTH:100%\"";
                if (in_array($Row["SKINDISEASECD"], $disableCodes) == true || $Row["SKINDISEASECD"] == '') {
                    $extra .= "disabled";
                } else {
                    $extra .= "";
                }
                $setData["SKINDISEASECD_REMARK"] = knjCreateTextBox($objForm, $Row["SKINDISEASECD_REMARK"], "SKINDISEASECD_REMARK"."-".$counter, 40, 20, $extra);

                //結核（間接撮影、所見、その他検査）表示
                if ($Row["SCHOOL_KIND"] == "H") {
                    $arg["tbFilmShow"] = 1;
                }
                //結核・X線・撮影日付
                $Row["TB_FILMDATE"] = str_replace("-", "/", $Row["TB_FILMDATE"]);
                $setData["TB_FILMDATE"] = View::popUpCalendar($objForm, "TB_FILMDATE"."-".$counter, $Row["TB_FILMDATE"]);
                //結核・X線・フィルム番号
                $extra = "";
                $setData["TB_FILMNO"] = knjCreateTextBox($objForm, $Row["TB_FILMNO"], "TB_FILMNO"."-".$counter, $model->tb_filmnoFieldSize, $model->tb_filmnoFieldSize, $extra);

                //結核・所見(コンボ)
                $disableCodes = getDisabledCodes($db, "F100");
                $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, 'TB_X_RAY', '{$counter}', '".implode(",", $disableCodes)."')\"";
                $setData["TB_REMARKCD"] = makeCmbReturn2($objForm, $optF100, $Row["TB_REMARKCD"], "TB_REMARKCD"."-".$counter, $extra, 1, "BLANK");

                //結核・所見(テキスト)
                $extra  = "STYLE=\"WIDTH:100%\"";
                if (in_array($Row["TB_REMARKCD"], $disableCodes) == true || $Row["TB_REMARKCD"] == '') {
                    $extra .= "disabled";
                } else {
                    $extra .= "";
                }
                $setData["TB_X_RAY"] = knjCreateTextBox($objForm, $Row["TB_X_RAY"], "TB_X_RAY"."-".$counter, 40, 20, $extra);

                //結核・再検査の検査日付
                $Row["TB_RE_EXAMINATION_DATE"] = str_replace("-", "/", $Row["TB_RE_EXAMINATION_DATE"]);
                $setData["TB_RE_EXAMINATION_DATE"] = View::popUpCalendar($objForm, "TB_RE_EXAMINATION_DATE"."-".$counter, $Row["TB_RE_EXAMINATION_DATE"]);
                //結核・再検査の結果
                $extra = "style=\"width:300px;\"";
                $setData["TB_RE_EXAMINATION_RESULT"] = makeCmbReturn2($objForm, $optF101, $Row["TB_RE_EXAMINATION_RESULT"], "TB_RE_EXAMINATION_RESULT"."-".$counter, $extra, 1, "BLANK");
                //結核・再検査・画像番号
                $extra = "";
                $setData["TB_RE_EXAMINATION_FILMNO"] = knjCreateTextBox($objForm, $Row["TB_RE_EXAMINATION_FILMNO"], "TB_RE_EXAMINATION_FILMNO"."-".$counter, $model->tb_filmnoFieldSize, $model->tb_filmnoFieldSize, $extra);

                //結核・その他検査(コンボ)
                $extra = "";
                $setData["TB_OTHERTESTCD"] = makeCmbReturn2($objForm, $optF110, $Row["TB_OTHERTESTCD"], "TB_OTHERTESTCD"."-".$counter, $extra, 1, "BLANK");

                //結核・その他検査・疾病及び異常(コンボ)
                $disableCodes = getDisabledCodes($db, "F120");
                $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, 'TB_NAME_REMARK1', '{$counter}', '".implode(",", $disableCodes)."')\"";
                $setData["TB_NAMECD"] = makeCmbReturn2($objForm, $optF120, $Row["TB_NAMECD"], "TB_NAMECD"."-".$counter, $extra, 1, "BLANK");

                //結核・その他検査・疾病及び異常(テキスト)
                $extra  = "STYLE=\"WIDTH:100%\"";
                if (in_array($Row["TB_NAMECD"], $disableCodes) == true || $Row["TB_NAMECD"] == '') {
                    $extra .= "disabled";
                } else {
                    $extra .= "";
                }
                $setData["TB_NAME_REMARK1"] = knjCreateTextBox($objForm, $Row["TB_NAME_REMARK1"], "TB_NAME_REMARK1"."-".$counter, 40, 20, $extra);

                //結核・指導区分
                $extra = "style=\"width:170px;\"";
                $setData["TB_ADVISECD"] = makeCmbReturn2($objForm, $optF130, $Row["TB_ADVISECD"], "TB_ADVISECD"."-".$counter, $extra, 1, "BLANK");
                //心臓・臨床医学的検査(コンボ)
                $disableCodes = getDisabledCodes($db, "F080");
                $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, 'HEART_MEDEXAM_REMARK', '{$counter}', '".implode(",", $disableCodes)."')\"";
                $setData["HEART_MEDEXAM"] = makeCmbReturn2($objForm, $optF080, $Row["HEART_MEDEXAM"], "HEART_MEDEXAM"."-".$counter, $extra, 1, "BLANK");

                //心臓・臨床医学的検査(テキスト)
                $extra  = "STYLE=\"WIDTH:100%\"";
                if (in_array($Row["HEART_MEDEXAM"], $disableCodes) == true || $Row["HEART_MEDEXAM"] == '') {
                    $extra .= "disabled";
                } else {
                    $extra .= "";
                }
                $setData["HEART_MEDEXAM_REMARK"] = knjCreateTextBox($objForm, $Row["HEART_MEDEXAM_REMARK"], "HEART_MEDEXAM_REMARK"."-".$counter, 80, 40, $extra);

                //心臓・疾病及び異常(コンボ)
                $disableCodes = getDisabledCodes($db, "F090");
                $extra = "onChange=\"syokenNyuryoku(this, 'HEARTDISEASECD_REMARK', '{$counter}', '".implode(",", $disableCodes)."')\"";
                $setData["HEARTDISEASECD"] = makeCmbReturn2($objForm, $optF090, $Row["HEARTDISEASECD"], "HEARTDISEASECD"."-".$counter, $extra, 1, "BLANK");

                //心臓・疾病及び異常(テキスト)
                $extra  = "STYLE=\"WIDTH:100%\"";
                if (in_array($Row["HEARTDISEASECD"], $disableCodes) == true || $Row["HEARTDISEASECD"] == '') {
                    $extra .= "disabled";
                } else {
                    $extra .= "";
                }
                $setData["HEARTDISEASECD_REMARK"] = knjCreateTextBox($objForm, $Row["HEARTDISEASECD_REMARK"], "HEARTDISEASECD_REMARK"."-".$counter, 40, 20, $extra);

                //心臓・精密検査(コンボ)
                $disableCodes = getDisabledCodes($db, "F091");
                $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, 'MANAGEMENT_REMARK', '{$counter}', '".implode(",", $disableCodes)."')\"";
                $setData["MANAGEMENT_DIV"] = makeCmbReturn2($objForm, $optF091, $Row["MANAGEMENT_DIV"], "MANAGEMENT_DIV"."-".$counter, $extra, 1, "BLANK");

                //心臓・精密検査(テキスト)
                $extra  = "STYLE=\"WIDTH:100%\"";
                if (in_array($Row["MANAGEMENT_DIV"], $disableCodes) == true || $Row["MANAGEMENT_DIV"] == '') {
                    $extra .= "disabled";
                } else {
                    $extra .= "";
                }
                $setData["MANAGEMENT_REMARK"] = knjCreateTextBox($objForm, $Row["MANAGEMENT_REMARK"], "MANAGEMENT_REMARK"."-".$counter, 40, 20, $extra);
            }

            //項目3-----------------------------------------

            if ($model->input_form == "3") {
                $arg["inputForm3"] = 1;

                //尿・１次蛋白
                //尿・１次糖
                //尿・１次潜血
                //尿・再検査蛋白
                //尿・再検査糖
                //尿・再検査潜血
                $extra = "";
                $setData["ALBUMINURIA1CD"] = makeCmbReturn2($objForm, $optF020, $Row["ALBUMINURIA1CD"], "ALBUMINURIA1CD"."-".$counter, $extra, 1, "BLANK");
                $setData["ALBUMINURIA2CD"] = makeCmbReturn2($objForm, $optF020, $Row["ALBUMINURIA2CD"], "ALBUMINURIA2CD"."-".$counter, $extra, 1, "BLANK");
                $setData["URICSUGAR1CD"] = makeCmbReturn2($objForm, $optF019, $Row["URICSUGAR1CD"], "URICSUGAR1CD"."-".$counter, $extra, 1, "BLANK");
                $setData["URICSUGAR2CD"] = makeCmbReturn2($objForm, $optF019, $Row["URICSUGAR2CD"], "URICSUGAR2CD"."-".$counter, $extra, 1, "BLANK");
                $setData["URICBLEED1CD"] = makeCmbReturn2($objForm, $optF018, $Row["URICBLEED1CD"], "URICBLEED1CD"."-".$counter, $extra, 1, "BLANK");
                $setData["URICBLEED2CD"] = makeCmbReturn2($objForm, $optF018, $Row["URICBLEED2CD"], "URICBLEED2CD"."-".$counter, $extra, 1, "BLANK");

                //尿・精密検査
                //尿・精密検査(コンボ)
                $disableCodes = getDisabledCodes($db, "F146");
                $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, 'URICOTHERTEST', '{$counter}', '".implode(",", $disableCodes)."')\"";
                $setData["DETAILED_EXAMINATION"] = makeCmbReturn2($objForm, $optF146, $Row["DETAILED_EXAMINATION"], "DETAILED_EXAMINATION"."-".$counter, $extra, 1, "BLANK");

                //尿・精密検査(所見)
                $extra  = "STYLE=\"WIDTH:100%\"";
                if (in_array($Row["DETAILED_EXAMINATION"], $disableCodes) == true || $Row["DETAILED_EXAMINATION"] == '') {
                    $extra .= "disabled";
                } else {
                    $extra .= "";
                }
                $setData["URICOTHERTEST"] = knjCreateTextBox($objForm, $Row["URICOTHERTEST"], "URICOTHERTEST"."-".$counter, 40, 20, $extra);

                //尿・精密検査(指導区分)
                $extra = "style=\"width:170px;\"";
                $setData["URI_ADVISECD"] = makeCmbReturn2($objForm, $optF021, $Row["URI_ADVISECD"], "URI_ADVISECD"."-".$counter, $extra, 1, "BLANK");

                //その他疾病及び異常
                $extra = "STYLE=\"WIDTH:100%\"";
                $setData["OTHER_ADVISECD"] = makeCmbReturn2($objForm, $optF145, $Row["OTHER_ADVISECD"], "OTHER_ADVISECD"."-".$counter, $extra, 1, "BLANK");
                $extra  = "STYLE=\"WIDTH:100%\"";
                $setData["OTHER_REMARK"] = knjCreateTextBox($objForm, $Row["OTHER_REMARK"], "OTHER_REMARK"."-".$counter, 40, 20, $extra);

                //学校医・内科検診
                if ($arg["is_mie"] == "1") {
                    $extra = "";
                    $setData["DOC_CD"] = makeCmbReturn2($objForm, $optF144, $Row["DOC_CD"], "DOC_CD"."-".$counter, $extra, 1, "BLANK");
                }
                $extra  = "STYLE=\"WIDTH:100%\"";
                $setData["DOC_REMARK"] = knjCreateTextBox($objForm, $Row["DOC_REMARK"], "DOC_REMARK"."-".$counter, 40, 20, $extra);
                //学校医・所見日付
                $Row["DOC_DATE"] = str_replace("-", "/", $Row["DOC_DATE"]);
                $setData["DOC_DATE"] = View::popUpCalendar($objForm, "DOC_DATE"."-".$counter, $Row["DOC_DATE"]);

                //事後措置1
                $disableCodes = getDisabledCodes($db, "F150");
                $target_objs  = "TREAT_REMARK1,TREATCD2,TREATCD2_REMARK1";
                $text_objs    = "TREATCD2_REMARK1";
                $combo_objs   = "TREATCD2";
                $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku2(this, '".$target_objs."', '{$counter}','".implode(",", $disableCodes)."', '".$text_objs."','".implode(",", getDisabledCodes($db, "F150"))."', '".$combo_objs."')\"";
                $setData["TREATCD"] = makeCmbReturn2($objForm, $optF150, $Row["TREATCD"], "TREATCD"."-".$counter, $extra, 1, "BLANK");


                //事後措置2
                $disableCodes = getDisabledCodes($db, "F151");
                if (in_array($Row["TREATCD"], getDisabledCodes($db, "F150")) == true || $Row["TREATCD"] == '') {
                    $extra = "style=\"width:170px;\" disabled onChange=\"syokenNyuryoku(this, 'TREATCD2_REMARK1', '{$counter}', '".implode(",", $disableCodes)."')\"";
                    $setData["TREATCD2"] = makeCmbReturn2($objForm, $optF151, $Row["TREATCD2"], "TREATCD2"."-".$counter, $extra, 1, "BLANK");
                } else {
                    $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, 'TREATCD2_REMARK1', '{$counter}', '".implode(",", $disableCodes)."')\"";
                    $setData["TREATCD2"] = makeCmbReturn2($objForm, $optF151, $Row["TREATCD2"], "TREATCD2"."-".$counter, $extra, 1, "BLANK");
                }
                //事後措置2(テキスト)
                $extra  = "STYLE=\"WIDTH:100%\"";
                if (in_array($Row["TREATCD2"], $disableCodes) == true || $Row["TREATCD2"] == '') {
                    $extra .= "disabled";
                } else {
                    $extra .= "";
                }
                $setData["TREATCD2_REMARK1"] = knjCreateTextBox($objForm, $Row["TREATCD2_REMARK1"], "TREATCD2_REMARK1"."-".$counter, 40, 20, $extra);

                if (in_array($Row["TREATCD"], getDisabledCodes($db, "F150")) == true || $Row["TREATCD"] == '') {
                    $extra  = "STYLE=\"WIDTH:100%\" disabled";
                    $setData["TREATCD2_REMARK1"] = knjCreateTextBox($objForm, $Row["TREATCD2_REMARK1"], "TREATCD2_REMARK1"."-".$counter, 40, 20, $extra);
                } else {
                    $extra  = "STYLE=\"WIDTH:100%\"";
                }
                //事後措置1(テキスト)

                $setData["TREAT_REMARK1"] = knjCreateTextBox($objForm, $Row["TREAT_REMARK1"], "TREAT_REMARK1"."-".$counter, 40, 20, $extra);

                //連絡欄
                $extra  = "STYLE=\"WIDTH:100%\"";
                $setData["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK"."-".$counter, 120, $maxRemarkByte, $extra);
            }

            //項目4-----------------------------------------

            if ($model->input_form == "4") {
                $arg["inputForm4"] = 1;
                //既往症
                $extra = "";
                $setData["MEDICAL_HISTORY1"] = makeCmbReturn2($objForm, $optF143, $Row["MEDICAL_HISTORY1"], "MEDICAL_HISTORY1"."-".$counter, $extra, 1, "BLANK");
                $setData["MEDICAL_HISTORY2"] = makeCmbReturn2($objForm, $optF143, $Row["MEDICAL_HISTORY2"], "MEDICAL_HISTORY2"."-".$counter, $extra, 1, "BLANK");
                $setData["MEDICAL_HISTORY3"] = makeCmbReturn2($objForm, $optF143, $Row["MEDICAL_HISTORY3"], "MEDICAL_HISTORY3"."-".$counter, $extra, 1, "BLANK");
                //診断名
                $extra  = "STYLE=\"WIDTH:100%\"";
                $setData["DIAGNOSIS_NAME"] = knjCreateTextBox($objForm, $Row["DIAGNOSIS_NAME"], "DIAGNOSIS_NAME"."-".$counter, 100, 50, $extra);
            }

            //----------------------------------------------

            $arg["data"][] = $setData;

            knjCreateHidden($objForm, "UPDATED"."-".$counter, $Row["UPDATED"]);
        } //foreach

        //未検査項目設定
        $notFieldSet = $sep = "";
        $query = knjf012jQuery::getMedexamDetNotExaminedDat($model);
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
            $arg["setNotExamined"] = "setNotExamined('{$notFieldSet}', '{$counter}')";
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        if ($resizeFlg) {
            $arg["reload"] = "submit_reSize()";
        }

        View::toHTML($model, "knjf012jForm1.html", $arg);
    }
}
//コンボ作成
function makeCmbReturn(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                        "value" => "");
    }

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value != ""  && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//配列作成
function makeArrayReturn($db, $query)
{
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    return $opt;
}

//コンボ作成
function makeCmbReturn2(&$objForm, &$getopt, &$value, $name, $extra, $size, $blank = "")
{
    $opt = $getopt;
    if ($blank) {
        array_unshift($opt, array("label" => "", "value" => ""));
    }
    $value_flg = false;
    foreach ($opt as $key => $val) {
        if ($value === $val["value"]) {
            $value_flg = true;
        }
    }
    $value = ($value != ""  && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    $btnSize = "";
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$btnSize);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra.$btnSize);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra.$btnSize);
}
//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "printKenkouSindanIppan", $model->Properties["printKenkouSindanIppan"]);
    knjCreateHidden($objForm, "H_HR_CLASS");
    knjCreateHidden($objForm, "H_INPUT_FORM");
}

function getDisabledCodes($db, $nameCd1)
{
    $result     = $db->query(knjf012jQuery::getNameSpare2($nameCd1));
    $nameCd2 = array();
    while ($row  = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $nameCd2[] = $row["NAMECD2"];
    }
    return $nameCd2;
}
