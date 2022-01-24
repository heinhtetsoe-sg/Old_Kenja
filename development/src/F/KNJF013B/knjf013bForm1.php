<?php

require_once('for_php7.php');

class knjf013bForm1
{
    public function main(&$model)
    {
        $objForm        = new form();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjf013bindex.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //Windowサイズ
        $arg["WindowWidth"]      = $model->windowWidth  - 36;
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
        $query = knjf013bQuery::getHrClass($model);
        $arg["HR_CLASS"] = makeCmbReturn($objForm, $arg, $db, $query, $model->hr_class, "HR_CLASS", $extra, 1, "BLANK");

        //種類
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjf013bQuery::getInputForm();
        $arg["INPUT_FORM"] = makeCmbReturn($objForm, $arg, $db, $query, $model->input_form, "INPUT_FORM", $extra, 1, "");

        //聴力のサイズ調整
        $ear_width = "460";
        $input_form1_width = "1380";
        if ($model->Properties["useEar4000Hz"] == "1") {
            $ear_width = "560";
            $input_form1_width = "1480";
        }
        if ($model->z010name1 == "fukuiken" || $model->z010name1 == "koma" || ($model->Properties["useSpecial_Support_School"] != "1" && $model->Properties["unEar_db"] == "1")) {
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
            $model->maxRemarkByte       = '180';
            $model->maxOtherRemark2Byte = '60';
            $arg["REMARK_MOJI"]         = '60';
            $arg["OTHER_REMARK_WIDTH"]  = '400';
            $arg["OTHER_REMARK2_MOJI"]  = '20文字';
            $otherSize                  = '40';
        }
        if ($model->z010name1 == "fukuiken") {
            $input_form3_width += "160";
        }

        //サイズ調整（幅）
        $width_array = array("1" => $input_form1_width, "2" => "3585", "3" => $input_form3_width, "4" => "1740");
        $arg["widthHeader"] = $width_array[$model->input_form];
        $arg["widthMeisai"] = $width_array[$model->input_form];
        //サイズ調整（高さ）
        $arg["heightHeader"] = "50";
        if ($model->input_form == "2") {
            $arg["heightMeisai"] = "200";
        } elseif ($model->input_form == "1") {
            $arg["heightMeisai"] = "100";
        } elseif ($model->input_form == "3") {
            $arg["heightMeisai"] = "160";
        } else {
            $arg["heightMeisai"] = "80";
        }

        //レイアウトの切り替え
        for ($i = 1; $i <= 4; $i++) {
            if ($model->input_form == $i) {
                $arg["inputForm" . $i] = 1;
            }
        }

        $arg["is_sitheight"]  = "1";
        if ($model->z010name1 == "miyagiken") {
            $arg["is_miyagiken"]  = "1";
            $arg["is_sitheight"]  = "";
        }
        if ($model->z010name1 == "fukuiken") { // 福井は視力の文字＋数字入力、貧血を使用する
            $arg["is_fukui"] = "1";
            $arg["no_fukui"] = "";
            $arg["no_fukui_ear"] = "";
            $arg["is_sitheight"] = "";
            $arg["is_fukui_koma"] = "1";
        } else {
            $arg["is_fukui"] = "";
            $arg["no_fukui"] = "1";
            if ($model->Properties["useSpecial_Support_School"] != "1" && $model->Properties["unEar_db"] == "1") {
                $arg["no_fukui_ear"] = "";
            } else {
                $arg["no_fukui_ear"] = "1";
            }
        }
        if ($model->z010name1 == "hirokoudai") { // 広工大は数字入力
            $arg["is_hirokoudai"] = "1";
            $arg["no_hirokoudai"] = "";
            $arg["EAR_TITLE"]     = "1000Hz";
        } else {
            $arg["is_hirokoudai"] = "";
            $arg["no_hirokoudai"] = "1";
            $arg["EAR_TITLE"]     = "平均dB";
        }
        if ($model->z010name1 == "mieken") {
            $arg["is_mie"] = "1";
            $arg["is_not_mie"] = "";
        } else {
            $arg["is_mie"] = "";
            $arg["is_not_mie"] = "1";
        }
        if ($model->z010name1 == "koma") {
            $arg["is_koma"] = "1";
            $arg["no_koma"] = "";
            $arg["EAR_TITLE"]     = "1000Hz";
            $arg["is_fukui_koma"] = "1";
            $arg["is_sitheight"] = "";
        } else {
            $arg["is_koma"] = "";
            $arg["no_koma"] = "1";
            $arg["is_fukui_koma"] = "";
        }

        if ($model->Properties["hideSitHeight"] == "1") {
            $arg["is_sitheight"] = "";
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
            $query = knjf013bQuery::getMedexamList($model);
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
        $sd = $db->getRow(knjf013bQuery::getSD($model), DB_FETCHMODE_ASSOC);

        //測定評価平均値データ取得
        $phys_avg = array();
        $query = knjf013bQuery::getHexamPhysicalAvgDat($model);
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
            $nameM["F050"] = "";
            $nameM["F051"] = "";
            $nameM["F060"] = "";
            $nameM["F061"] = "";
            $nameM["F062"] = "";
            $nameM["F063"] = "";
            $nameM["F070"] = "";
            $nameM["F100"] = "";
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
            $query = knjf013bQuery::getNameMst($model, $namecd1, $flg);
            $optname = "opt".$namecd1;
            $$optname = makeArrayReturn($db, $query);
        }

        if ($model->input_form == "2") {
            //結核の画像番号のサイズ
            $arg["TB_FILMNO_SIZE"] = $model->tb_filmnoFieldSize;
        }

        //栄養状態NAMECD取得
        $nutrInfo = $sep = '';
        $result     = $db->query(knjf013bQuery::getNutrition($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);

            if ($row["ABBV1"] == '' && $row["ABBV2"] == '') {
            } else {
                $nutrInfo .= $sep.$row["NAMECD2"].':'.$row["ABBV1"].':'.$row["ABBV2"];
                $sep = ',';
            }
        }
        knjCreateHidden($objForm, "nutrInfo", $nutrInfo);

        //一覧を表示
        foreach ($medexamList as $counter => $Row) {
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
                $std_weight = ($Row["HEIGHT"]) ? ((int)$phys_avg[$Row["SEX"]][$age]["A"] * (int)$Row["HEIGHT"] - (int)$phys_avg[$Row["SEX"]][$age]["B"]) : "";
                $id = "std_weight-".$counter;
                $setData["STD_WEIGHT"] = "<span id={$id}>".$std_weight."</span>";
                if ($model->z010name1 == "sapporo") { //札幌は赤字表示
                    $arg["sapporo"] = 1;
                } else {
                    unset($arg["sapporo"]);
                }

                //肥満度 = (実測体重 ― 身長別標準体重) ／ 身長別標準体重 × 100（％）
                if ($std_weight > 0) {
                    $setData["OBESITY_INDEX"] = round(((int)$Row["WEIGHT"] - $std_weight) / $std_weight * 100, 1).'%';
                }

                //肥満度印字無しcheckbox
                $checkNI = ($Row["NO_PRINT_OBESITY_INDEX"] == "1") ? " checked" : "";
                $extra = "id=\"NO_PRINT_OBESITY_INDEX-{$counter}\"";
                $setData["NO_PRINT_OBESITY_INDEX"] = knjCreateCheckBox($objForm, "NO_PRINT_OBESITY_INDEX"."-".$counter, "1", $extra.$checkNI);

                //座高
                $extra = "onblur=\"return Num_Check(this);\"";
                $setData["SITHEIGHT"] = knjCreateTextBox($objForm, $Row["SITHEIGHT"], "SITHEIGHT"."-".$counter, 5, 5, $extra);

                //視力・測定困難チェック時は、視力情報はグレーアウト
                $disVisionR = ($Row["R_VISION_CANTMEASURE"] == "1") ? " disabled": "";
                $disVisionL = ($Row["L_VISION_CANTMEASURE"] == "1") ? " disabled": "";

                //視力・右裸眼(文字)
                //視力・左裸眼(文字)
                //視力・右矯正(文字)
                //視力・左矯正(文字)
                if ($arg["is_mie"] == "1") {
                    $extra = "onblur=\"return Mark_Check(this);\"";
                    $setData["R_BAREVISION_MARK"] = knjCreateTextBox($objForm, $Row["R_BAREVISION_MARK"], "R_BAREVISION_MARK"."-".$counter, 1, 1, $extra);
                    $setData["L_BAREVISION_MARK"] = knjCreateTextBox($objForm, $Row["L_BAREVISION_MARK"], "L_BAREVISION_MARK"."-".$counter, 1, 1, $extra);
                    $setData["R_VISION_MARK"] = knjCreateTextBox($objForm, $Row["R_VISION_MARK"], "R_VISION_MARK"."-".$counter, 1, 1, $extra);
                    $setData["L_VISION_MARK"] = knjCreateTextBox($objForm, $Row["L_VISION_MARK"], "L_VISION_MARK"."-".$counter, 1, 1, $extra);
                } else {
                    $extra = "";
                    $setData["R_BAREVISION_MARK"] = makeCmbReturn2($objForm, $optF017, $Row["R_BAREVISION_MARK"], "R_BAREVISION_MARK"."-".$counter, $extra.$disVisionR, 1, "BLANK");
                    $setData["L_BAREVISION_MARK"] = makeCmbReturn2($objForm, $optF017, $Row["L_BAREVISION_MARK"], "L_BAREVISION_MARK"."-".$counter, $extra.$disVisionL, 1, "BLANK");
                    $setData["R_VISION_MARK"]     = makeCmbReturn2($objForm, $optF017, $Row["R_VISION_MARK"], "R_VISION_MARK"."-".$counter, $extra.$disVisionR, 1, "BLANK");
                    $setData["L_VISION_MARK"]     = makeCmbReturn2($objForm, $optF017, $Row["L_VISION_MARK"], "L_VISION_MARK"."-".$counter, $extra.$disVisionL, 1, "BLANK");
                }

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

                //視力、測定困難(右)
                $checkV_R = ($Row["R_VISION_CANTMEASURE"] == "1") ? " checked" : "";
                $extra = "id=\"R_VISION_CANTMEASURE-{$counter}\" onClick=\"disVision(this, 'right', '{$counter}');\"";
                $setData["R_VISION_CANTMEASURE"] = knjCreateCheckBox($objForm, "R_VISION_CANTMEASURE"."-".$counter, "1", $extra.$checkV_R);

                //視力、測定困難(左)
                $checkV_L = ($Row["L_VISION_CANTMEASURE"] == "1") ? " checked" : "";
                $extra = "id=\"L_VISION_CANTMEASURE-{$counter}\" onClick=\"disVision(this, 'reft', '{$counter}');\"";
                $setData["L_VISION_CANTMEASURE"] = knjCreateCheckBox($objForm, "L_VISION_CANTMEASURE"."-".$counter, "1", $extra.$checkV_L);

                //聴力・測定困難チェック時は、聴力情報はグレーアウト
                $disEarR = ($Row["R_EAR_CANTMEASURE"] == "1") ? " disabled": "";
                $disEarL = ($Row["L_EAR_CANTMEASURE"] == "1") ? " disabled": "";

                //聴力・右DB
                //聴力・左DB
                //聴力・右状態
                //聴力・左状態
                $extra = "onblur=\"return Num_Check(this);\"";
                $setData["R_EAR_DB"] = knjCreateTextBox($objForm, $Row["R_EAR_DB"], "R_EAR_DB"."-".$counter, 4, 3, $extra.$disEarR);
                $setData["L_EAR_DB"] = knjCreateTextBox($objForm, $Row["L_EAR_DB"], "L_EAR_DB"."-".$counter, 4, 3, $extra.$disEarL);
                $setData["R_EAR_DB_IN"] = knjCreateTextBox($objForm, $Row["R_EAR_DB_IN"], "R_EAR_DB_IN"."-".$counter, 4, 3, $extra.$disEarR);
                $setData["L_EAR_DB_IN"] = knjCreateTextBox($objForm, $Row["L_EAR_DB_IN"], "L_EAR_DB_IN"."-".$counter, 4, 3, $extra.$disEarL);
                $extra = "";
                $setData["R_EAR"] = makeCmbReturn2($objForm, $optF010, $Row["R_EAR"], "R_EAR"."-".$counter, $extra.$disEarR, 1, "BLANK");
                $setData["L_EAR"] = makeCmbReturn2($objForm, $optF010, $Row["L_EAR"], "L_EAR"."-".$counter, $extra.$disEarL, 1, "BLANK");
                $setData["R_EAR_IN"] = makeCmbReturn2($objForm, $optF010, $Row["R_EAR_IN"], "R_EAR_IN"."-".$counter, $extra.$disEarR, 1, "BLANK");
                $setData["L_EAR_IN"] = makeCmbReturn2($objForm, $optF010, $Row["L_EAR_IN"], "L_EAR_IN"."-".$counter, $extra.$disEarL, 1, "BLANK");
                //聴力・右4000Hz
                //聴力・左4000Hz
                if ($model->Properties["useEar4000Hz"] == "1") {
                    $extra = "onblur=\"return Num_Check(this);\"";
                    $setData["R_EAR_DB_4000"] = "4000Hz：&nbsp;".knjCreateTextBox($objForm, $Row["R_EAR_DB_4000"], "R_EAR_DB_4000"."-".$counter, 4, 3, $extra.$disEarR);
                    $setData["L_EAR_DB_4000"] = "4000Hz：&nbsp;".knjCreateTextBox($objForm, $Row["L_EAR_DB_4000"], "L_EAR_DB_4000"."-".$counter, 4, 3, $extra.$disEarL);
                    $setData["R_EAR_DB_4000_IN"] = "4000Hz：&nbsp;".knjCreateTextBox($objForm, $Row["R_EAR_DB_4000_IN"], "R_EAR_DB_4000_IN"."-".$counter, 4, 3, $extra.$disEarR);
                    $setData["L_EAR_DB_4000_IN"] = "4000Hz：&nbsp;".knjCreateTextBox($objForm, $Row["L_EAR_DB_4000_IN"], "L_EAR_DB_4000_IN"."-".$counter, 4, 3, $extra.$disEarL);
                }

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
                //栄養状態
                $extra = "style=\"width:170px;\"";
                $setData["NUTRITIONCD"] = makeCmbReturn2($objForm, $optF030, $Row["NUTRITIONCD"], "NUTRITIONCD"."-".$counter, $extra, 1, "BLANK");
                $extra  = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
                $extra .= ((int)$Row["NUTRITIONCD"] < 2) ? " disabled" : "";
                $setData["NUTRITIONCD_REMARK"] = knjCreateTextBox($objForm, $Row["NUTRITIONCD_REMARK"], "NUTRITIONCD_REMARK"."-".$counter, 40, 20, $extra);
                //脊柱・胸部
                $extra = "style=\"width:170px;\"";
                $setData["SPINERIBCD"] = makeCmbReturn2($objForm, $optF040, $Row["SPINERIBCD"], "SPINERIBCD"."-".$counter, $extra, 1, "BLANK");
                $extra  = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
                $setData["SPINERIBCD_REMARK"] = knjCreateTextBox($objForm, $Row["SPINERIBCD_REMARK"], "SPINERIBCD_REMARK"."-".$counter, 40, 20, $extra);
                //目の疾病及び異常
                $extra = "style=\"width:170px;\"";
                $setData["EYEDISEASECD"] = makeCmbReturn2($objForm, $optF050, $Row["EYEDISEASECD"], "EYEDISEASECD"."-".$counter, $extra, 1, "BLANK");
                $setData["EYEDISEASECD2"] = makeCmbReturn2($objForm, $optF050, $Row["EYEDISEASECD2"], "EYEDISEASECD2"."-".$counter, $extra, 1, "BLANK");
                $setData["EYEDISEASECD3"] = makeCmbReturn2($objForm, $optF050, $Row["EYEDISEASECD3"], "EYEDISEASECD3"."-".$counter, $extra, 1, "BLANK");
                $setData["EYEDISEASECD4"] = makeCmbReturn2($objForm, $optF050, $Row["EYEDISEASECD4"], "EYEDISEASECD4"."-".$counter, $extra, 1, "BLANK");

                $extra  = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
                $setData["EYE_TEST_RESULT"] = knjCreateTextBox($objForm, $Row["EYE_TEST_RESULT"], "EYE_TEST_RESULT"."-".$counter, 40, 20, $extra);
                $setData["EYE_TEST_RESULT2"] = knjCreateTextBox($objForm, $Row["EYE_TEST_RESULT2"], "EYE_TEST_RESULT2"."-".$counter, 40, 20, $extra);
                $setData["EYE_TEST_RESULT3"] = knjCreateTextBox($objForm, $Row["EYE_TEST_RESULT3"], "EYE_TEST_RESULT3"."-".$counter, 40, 20, $extra);

                $extra = "style=\"width:170px;\"";
                $setData["EYEDISEASECD5"] = makeCmbReturn2($objForm, $optF051, $Row["EYEDISEASECD5"], "EYEDISEASECD5"."-".$counter, $extra, 1, "BLANK");
                if ($model->z010name1 == "miyagiken") { // 宮城は色覚異常コンボを表示しない
                    $arg["not_miyagiken"] = "";
                } else {
                    $arg["not_miyagiken"] = "1";
                }
                //耳鼻咽頭疾患
                $extra = "style=\"width:170px;\"";
                $setData["NOSEDISEASECD"] = makeCmbReturn2($objForm, $optF060, $Row["NOSEDISEASECD"], "NOSEDISEASECD"."-".$counter, $extra, 1, "BLANK");
                $setData["NOSEDISEASECD2"] = makeCmbReturn2($objForm, $optF060, $Row["NOSEDISEASECD2"], "NOSEDISEASECD2"."-".$counter, $extra, 1, "BLANK");
                $setData["NOSEDISEASECD3"] = makeCmbReturn2($objForm, $optF060, $Row["NOSEDISEASECD3"], "NOSEDISEASECD3"."-".$counter, $extra, 1, "BLANK");
                $setData["NOSEDISEASECD4"] = makeCmbReturn2($objForm, $optF060, $Row["NOSEDISEASECD4"], "NOSEDISEASECD4"."-".$counter, $extra, 1, "BLANK");
                $extra  = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
                $setData["NOSEDISEASECD_REMARK"] = knjCreateTextBox($objForm, $Row["NOSEDISEASECD_REMARK"], "NOSEDISEASECD_REMARK"."-".$counter, 40, 20, $extra);
                //耳疾患
                $extra = "style=\"width:170px;\"";
                $setData["NOSEDISEASECD5"] = makeCmbReturn2($objForm, $optF061, $Row["NOSEDISEASECD5"], "NOSEDISEASECD5"."-".$counter, $extra, 1, "BLANK");
                $extra  = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
                $setData["NOSEDISEASECD_REMARK1"] = knjCreateTextBox($objForm, $Row["NOSEDISEASECD_REMARK1"], "NOSEDISEASECD_REMARK1"."-".$counter, 40, 20, $extra);
                //鼻・副鼻腔疾患
                $extra = "style=\"width:170px;\"";
                $setData["NOSEDISEASECD6"] = makeCmbReturn2($objForm, $optF062, $Row["NOSEDISEASECD6"], "NOSEDISEASECD6"."-".$counter, $extra, 1, "BLANK");
                $extra  = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
                $setData["NOSEDISEASECD_REMARK2"] = knjCreateTextBox($objForm, $Row["NOSEDISEASECD_REMARK2"], "NOSEDISEASECD_REMARK2"."-".$counter, 40, 20, $extra);
                //口腔咽頭疾患・異常
                $extra = "style=\"width:170px;\"";
                $setData["NOSEDISEASECD7"] = makeCmbReturn2($objForm, $optF063, $Row["NOSEDISEASECD7"], "NOSEDISEASECD7"."-".$counter, $extra, 1, "BLANK");
                $extra  = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
                $setData["NOSEDISEASECD_REMARK3"] = knjCreateTextBox($objForm, $Row["NOSEDISEASECD_REMARK3"], "NOSEDISEASECD_REMARK3"."-".$counter, 40, 20, $extra);
                //皮膚疾患
                $extra = "style=\"width:170px;\"";
                $setData["SKINDISEASECD"] = makeCmbReturn2($objForm, $optF070, $Row["SKINDISEASECD"], "SKINDISEASECD"."-".$counter, $extra, 1, "BLANK");
                $extra  = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
                $setData["SKINDISEASECD_REMARK"] = knjCreateTextBox($objForm, $Row["SKINDISEASECD_REMARK"], "SKINDISEASECD_REMARK"."-".$counter, 40, 20, $extra);
                //結核（間接撮影、所見、その他検査）表示
                if ($Row["SCHOOL_KIND"] == "H") {
                    $arg["tbFilmShow"] = 1;
                }
                //結核・撮影日付
                $Row["TB_FILMDATE"] = str_replace("-", "/", $Row["TB_FILMDATE"]);
                $setData["TB_FILMDATE"] = View::popUpCalendar($objForm, "TB_FILMDATE"."-".$counter, $Row["TB_FILMDATE"]);
                //結核・フィルム番号
                $extra = "";
                $setData["TB_FILMNO"] = knjCreateTextBox($objForm, $Row["TB_FILMNO"], "TB_FILMNO"."-".$counter, $model->tb_filmnoFieldSize, $model->tb_filmnoFieldSize, $extra);
                $setData["TB_FILMNO_SIZE"] = $model->tb_filmnoFieldSize;
                //結核・所見
                $extra = "style=\"width:170px;\"";
                $setData["TB_REMARKCD"] = makeCmbReturn2($objForm, $optF100, $Row["TB_REMARKCD"], "TB_REMARKCD"."-".$counter, $extra, 1, "BLANK");
                //結核検査(X線)
                $extra  = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
                $setData["TB_X_RAY"] = knjCreateTextBox($objForm, $Row["TB_X_RAY"], "TB_X_RAY"."-".$counter, 40, 20, $extra);
                //結核・その他検査
                $extra = "style=\"width:170px;\"";
                $setData["TB_OTHERTESTCD"] = makeCmbReturn2($objForm, $optF110, $Row["TB_OTHERTESTCD"], "TB_OTHERTESTCD"."-".$counter, $extra, 1, "BLANK");
                $setData["TB_OTHERTEST_REMARK1"] = knjCreateTextBox($objForm, $Row["TB_OTHERTEST_REMARK1"], "TB_OTHERTEST_REMARK1"."-".$counter, 40, 20, $extra);
                //結核・病名
                if ($arg["is_mie"] == "1") {
                    $extra = "style=\"width:170px;\"";
                } else {
                    $extra = "style=\"width:170px;\"";
                }
                $setData["TB_NAMECD"] = makeCmbReturn2($objForm, $optF120, $Row["TB_NAMECD"], "TB_NAMECD"."-".$counter, $extra, 1, "BLANK");
                $setData["TB_NAMECD_LABEL"] = ($model->z010name1 == "miyagiken") ? "病名" : "疾病及び異常";
                if ($arg["is_not_mie"] == "1") {
                    $extra = "style=\"width:170px;\"";
                    $setData["TB_NAME_REMARK1"] = knjCreateTextBox($objForm, $Row["TB_NAME_REMARK1"], "TB_NAME_REMARK1"."-".$counter, 40, 20, $extra);
                }
                //結核・指導区分
                $extra = "style=\"width:170px;\"";
                $setData["TB_ADVISECD"] = makeCmbReturn2($objForm, $optF130, $Row["TB_ADVISECD"], "TB_ADVISECD"."-".$counter, $extra, 1, "BLANK");
                $setData["TB_ADVISE_REMARK1"] = knjCreateTextBox($objForm, $Row["TB_ADVISE_REMARK1"], "TB_ADVISE_REMARK1"."-".$counter, 40, 20, $extra);
                //心臓・臨床医学的検査
                $extra = "style=\"width:170px;\"";
                $setData["HEART_MEDEXAM"] = makeCmbReturn2($objForm, $optF080, $Row["HEART_MEDEXAM"], "HEART_MEDEXAM"."-".$counter, $extra, 1, "BLANK");
                $extra  = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
                $extra .= ((int)$Row["HEART_MEDEXAM"] < 2) ? " disabled" : "";
                $setData["HEART_MEDEXAM_REMARK"] = knjCreateTextBox($objForm, $Row["HEART_MEDEXAM_REMARK"], "HEART_MEDEXAM_REMARK"."-".$counter, 80, 40, $extra);
                if ($model->z010name1 == "koma") {
                    $extra = "onblur=\"return Num_Check(this);\"";
                    $setData["HEART_GRAPH_NO"] = knjCreateTextBox($objForm, $Row["HEART_GRAPH_NO"], "HEART_GRAPH_NO"."-".$counter, 12, 12, $extra);
                }
                //心臓・疾病及び異常
                $extra = "style=\"width:170px;\"";
                $setData["HEARTDISEASECD"] = makeCmbReturn2($objForm, $optF090, $Row["HEARTDISEASECD"], "HEARTDISEASECD"."-".$counter, $extra, 1, "BLANK");
                $extra  = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
                $extra .= ((int)$Row["HEARTDISEASECD"] < 2) ? " disabled" : "";
                $setData["HEARTDISEASECD_REMARK"] = knjCreateTextBox($objForm, $Row["HEARTDISEASECD_REMARK"], "HEARTDISEASECD_REMARK"."-".$counter, 40, 20, $extra);
                //心臓・管理区分
                $extra = "style=\"width:170px;\"";
                $setData["MANAGEMENT_DIV"] = makeCmbReturn2($objForm, $optF091, $Row["MANAGEMENT_DIV"], "MANAGEMENT_DIV"."-".$counter, $extra, 1, "BLANK");
                $extra  = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
                $setData["MANAGEMENT_REMARK"] = knjCreateTextBox($objForm, $Row["MANAGEMENT_REMARK"], "MANAGEMENT_REMARK"."-".$counter, 40, 20, $extra);
            }

            //項目3-----------------------------------------

            if ($model->input_form == "3") {
                //尿・１次蛋白
                //尿・１次糖
                //尿・１次潜血
                //尿・２次蛋白
                //尿・２次糖
                //尿・２次潜血
                $extra = "";
                $setData["ALBUMINURIA1CD"] = makeCmbReturn2($objForm, $optF020, $Row["ALBUMINURIA1CD"], "ALBUMINURIA1CD"."-".$counter, $extra, 1, "BLANK");
                $setData["ALBUMINURIA2CD"] = makeCmbReturn2($objForm, $optF020, $Row["ALBUMINURIA2CD"], "ALBUMINURIA2CD"."-".$counter, $extra, 1, "BLANK");
                $setData["URICSUGAR1CD"] = makeCmbReturn2($objForm, $optF019, $Row["URICSUGAR1CD"], "URICSUGAR1CD"."-".$counter, $extra, 1, "BLANK");
                $setData["URICSUGAR2CD"] = makeCmbReturn2($objForm, $optF019, $Row["URICSUGAR2CD"], "URICSUGAR2CD"."-".$counter, $extra, 1, "BLANK");
                $setData["URICBLEED1CD"] = makeCmbReturn2($objForm, $optF018, $Row["URICBLEED1CD"], "URICBLEED1CD"."-".$counter, $extra, 1, "BLANK");
                $setData["URICBLEED2CD"] = makeCmbReturn2($objForm, $optF018, $Row["URICBLEED2CD"], "URICBLEED2CD"."-".$counter, $extra, 1, "BLANK");
                if ($model->Properties["printKenkouSindanIppan"] == "1" && $model->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                    $arg["DISP_PH"] = 1;
                    $extra = "onblur=\"return Num_Check(this);\"";
                    $setData["URICPH1"] = knjCreateTextBox($objForm, $Row["URICPH1"], "URICPH1"."-".$counter, 4, 4, $extra);
                    $setData["URICPH2"] = knjCreateTextBox($objForm, $Row["URICPH2"], "URICPH2"."-".$counter, 4, 4, $extra);
                }
                //尿・その他の検査
                //尿・指導区分
                $extra  = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
                $setData["URICOTHERTEST"] = knjCreateTextBox($objForm, $Row["URICOTHERTEST"], "URICOTHERTEST"."-".$counter, 40, 20, $extra);
                $extra = "style=\"width:170px;\"";
                $setData["URI_ADVISECD"] = makeCmbReturn2($objForm, $optF021, $Row["URI_ADVISECD"], "URI_ADVISECD"."-".$counter, $extra, 1, "BLANK");

                $result     = $db->query(knjf013bQuery::getUriCothertest($model));
                $opt        = array();
                $opt[]      = $optnull;
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt[] = array("label" => $row["LABEL"],
                               "value" => $row["VALUE"]);
                }
                $result->free();
                $extra = "style=\"width:190px;\"";
                $setData["URICOTHERTESTCD"] = knjCreateCombo($objForm, "URICOTHERTESTCD"."-".$counter, $Row["URICOTHERTESTCD"], $opt, $extra.$entMove, 1);

                //寄生虫卵
                $extra = "";
                $setData["PARASITE"] = makeCmbReturn2($objForm, $optF023, $Row["PARASITE"], "PARASITE"."-".$counter, $extra, 1, "BLANK");
                //寄生虫卵表示
                if (($Row["SCHOOL_KIND"] == "P" && $model->Properties["useParasite_P"] == "1") ||
                    ($Row["SCHOOL_KIND"] == "J" && $model->Properties["useParasite_J"] == "1") ||
                    ($Row["SCHOOL_KIND"] == "H" && $model->Properties["useParasite_H"] == "1")) {
                    $arg["para"] = 1;
                }
                //その他疾病及び異常
                $extra = "style=\"width:170px;\"";
                $setData["OTHERDISEASECD"] = makeCmbReturn2($objForm, $optF140, $Row["OTHERDISEASECD"], "OTHERDISEASECD"."-".$counter, $extra, 1, "BLANK");
                $extra = "style=\"width:170px;\"";
                $setData["OTHER_ADVISECD"] = makeCmbReturn2($objForm, $optF145, $Row["OTHER_ADVISECD"], "OTHER_ADVISECD"."-".$counter, $extra, 1, "BLANK");
                $extra  = "";
                $setData["OTHER_REMARK"] = knjCreateTextBox($objForm, $Row["OTHER_REMARK"], "OTHER_REMARK"."-".$counter, 40, 20, $extra);

                $extra  = "";
                if ($model->Properties["useSpecial_Support_School"] == "1") { //特別支援学校
                    $model->maxOtherRemark2Byte = '300';
                    $otherSize = '120';
                } else {
                    $model->maxOtherRemark2Byte = '60';
                    $otherSize = '40';
                }
                $setData["OTHER_REMARK2"] = knjCreateTextBox($objForm, $Row["OTHER_REMARK2"], "OTHER_REMARK2"."-".$counter, $otherSize, $model->maxOtherRemark2Byte, $extra);
                //その他疾病及び異常:所見3
                $setData["OTHER_REMARK3"] = knjCreateTextBox($objForm, $Row["OTHER_REMARK3"], "OTHER_REMARK3"."-".$counter, 40, 20, $entMove);

                //貧血
                //所見
                $extra = "";
                $setData["ANEMIA_REMARK"] = knjCreateTextBox($objForm, $Row["ANEMIA_REMARK"], "ANEMIA_REMARK"."-".$counter, 21, 30, $extra);

                //ヘモグロビン
                $extra = "style=\"text-align:right\" onblur=\"return Num_Check(this);\"";
                $setData["HEMOGLOBIN"] = knjCreateTextBox($objForm, $Row["HEMOGLOBIN"], "HEMOGLOBIN"."-".$counter, 4, 4, $extra);

                //学校医・内科検診
                if ($arg["is_mie"] == "1") {
                    $extra = "";
                    $setData["DOC_CD"] = makeCmbReturn2($objForm, $optF144, $Row["DOC_CD"], "DOC_CD"."-".$counter, $extra, 1, "BLANK");
                }
                $extra  = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
                $setData["DOC_REMARK"] = knjCreateTextBox($objForm, $Row["DOC_REMARK"], "DOC_REMARK"."-".$counter, 40, 20, $extra);
                //学校医・所見日付
                $Row["DOC_DATE"] = str_replace("-", "/", $Row["DOC_DATE"]);
                $setData["DOC_DATE"] = View::popUpCalendar($objForm, "DOC_DATE"."-".$counter, $Row["DOC_DATE"]);
                //事後措置
                $extra = "style=\"width:170px;\"";
                $setData["TREATCD"] = makeCmbReturn2($objForm, $optF150, $Row["TREATCD"], "TREATCD"."-".$counter, $extra, 1, "BLANK");
                //事後措置:所見1
                $extra = '';
                $setData["TREAT_REMARK1"] = knjCreateTextBox($objForm, $Row["TREAT_REMARK1"], "TREAT_REMARK1"."-".$counter, 40, 20, $extra);
                //事後措置:所見2
                $setData["TREAT_REMARK2"] = knjCreateTextBox($objForm, $Row["TREAT_REMARK2"], "TREAT_REMARK2"."-".$counter, 40, 20, $extra);
                //事後措置:所見3
                $setData["TREAT_REMARK3"] = knjCreateTextBox($objForm, $Row["TREAT_REMARK3"], "TREAT_REMARK3"."-".$counter, 40, 20, $extra);
                //備考
                $extra  = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
                $setData["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK"."-".$counter, 120, $model->maxRemarkByte, $extra);
                //メッセージ
                $gyo = 4;
                $moji = 21;
                $setData["MESSAGE"] = KnjCreateTextArea($objForm, "MESSAGE"."-".$counter, $gyo, ((int)$moji * 2 + 1), "soft", "", $Row["MESSAGE"]);
            }

            //項目4-----------------------------------------

            if ($model->input_form == "4") {
                //既往症
                $extra = "";
                $setData["MEDICAL_HISTORY1"] = makeCmbReturn2($objForm, $optF143, $Row["MEDICAL_HISTORY1"], "MEDICAL_HISTORY1"."-".$counter, $extra, 1, "BLANK");
                $setData["MEDICAL_HISTORY2"] = makeCmbReturn2($objForm, $optF143, $Row["MEDICAL_HISTORY2"], "MEDICAL_HISTORY2"."-".$counter, $extra, 1, "BLANK");
                $setData["MEDICAL_HISTORY3"] = makeCmbReturn2($objForm, $optF143, $Row["MEDICAL_HISTORY3"], "MEDICAL_HISTORY3"."-".$counter, $extra, 1, "BLANK");
                //診断名
                $extra  = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
                $setData["DIAGNOSIS_NAME"] = knjCreateTextBox($objForm, $Row["DIAGNOSIS_NAME"], "DIAGNOSIS_NAME"."-".$counter, 100, 50, $extra);
                //運動・指導区分
                $extra = "";
                $setData["GUIDE_DIV"] = makeCmbReturn2($objForm, $optF141, $Row["GUIDE_DIV"], "GUIDE_DIV"."-".$counter, $extra, 1, "BLANK");
                //運動・部活動
                $extra = "style=\"width:170px;\"";
                $setData["JOINING_SPORTS_CLUB"] = makeCmbReturn2($objForm, $optF142, $Row["JOINING_SPORTS_CLUB"], "JOINING_SPORTS_CLUB"."-".$counter, $extra, 1, "BLANK");
            }

            //----------------------------------------------

            $arg["data"][] = $setData;

            knjCreateHidden($objForm, "UPDATED"."-".$counter, $Row["UPDATED"]);
        } //foreach

        //未検査項目設定
        $notFieldSet = $sep = "";
        $query = knjf013bQuery::getMedexamDetNotExaminedDat($model);
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

        View::toHTML($model, "knjf013bForm1.html", $arg);
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
