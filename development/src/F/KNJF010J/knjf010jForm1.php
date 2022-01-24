<?php

require_once('for_php7.php');

class knjf010jForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjf010jindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->schregno)) {
            $RowH = knjf010jQuery::getMedexamHdat($model);      //生徒健康診断ヘッダデータ取得
            $RowD = knjf010jQuery::getMedexamDetDat($model);   //生徒健康診断詳細データ取得
            $arg["NOT_WARNING"] = 1;
        } else {
            $RowH =& $model->field;
            $RowD =& $model->field;
        }
        $db = Query::dbCheckOut();

        //ヘッダ
        if (isset($model->schregno)) {
            //生徒学籍データを取得
            $result = $db->query(knjf010jQuery::getSchregBaseMst($model));
            $RowB = $result->fetchRow(DB_FETCHMODE_ASSOC);
            $result->free();
            //生徒学籍番号
            $arg["header"]["SCHREGNO"] = $model->schregno;
            //生徒名前
            $arg["header"]["NAME_SHOW"] = $model->name;
            //生徒生年月日
            $birth_day = explode("-", $RowB["BIRTHDAY"]);
            $arg["header"]["BIRTHDAY"] = $birth_day[0]."年".$birth_day[1]."月".$birth_day[2]."日";
            //生徒学年クラスを取得
            $result = $db->query(knjf010jQuery::getSchregRegdDat($model));
            $RowR = $result->fetchRow(DB_FETCHMODE_ASSOC);
            $result->free();
            $model->GradeClass = $RowR["GRADE"]."-".$RowR["HR_CLASS"];
            $model->Hrname = $RowR["HR_NAME"];
            $model->school_kind = $RowR["SCHOOL_KIND"];
        } else {
            //学籍番号
            $arg["header"]["SCHREGNO"] = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
            //生徒氏名
            $arg["header"]["NAME_SHOW"] = "&nbsp;&nbsp;&nbsp;&nbsp;";
            //生年月日
            $arg["header"]["BIRTHDAY"] = "&nbsp;&nbsp;&nbsp;&nbsp;年&nbsp;&nbsp;&nbsp;&nbsp;月&nbsp;&nbsp;&nbsp;&nbsp;日";
        }

        //レイアウトの切り替え
        $arg["is_sitheight"]  = "1";
        $arg["unHyouji_SITHEIGHT"] = "";    //肥満の表示(宮城タイプ)
        if ($model->Properties["unHyouji_SITHEIGHT"] == "1") {
            $arg["unHyouji_SITHEIGHT"] = "1";
            $arg["is_sitheight"] = "";
        }
        if ($model->z010name1 == "miyagiken") { // 宮城は色覚異常コンボを表示しない
            $arg["is_miyagiken"]  = "1";
            $arg["not_miyagiken"] = "";
            $arg["is_sitheight"]  = "";
            $arg["unHyouji_SITHEIGHT"] = "1";
        } else {
            $arg["not_miyagiken"] = "1";
        }
        if ($model->isFukui) { // 福井は視力の文字＋数字入力、貧血を使用する
            $arg["is_fukui"] = "1";
            $arg["is_fukui_saga"] = "1";
            $arg["no_fukui"] = "";
            $arg["no_fukui_ear"] = "";
            $arg["is_sitheight"] = "";
            $arg["unHyouji_SITHEIGHT"] = "";
        } else {
            $arg["is_fukui"] = "";
            if ($model->isSaga) {
                $arg["is_fukui_saga"] = "1";
            } else {
                $arg["is_fukui_saga"] = "";
            }
            if ($model->isHirokoudai) {
                $arg["is_Hirokoudai"] = "1";//広工大は視力を数値入力にする
            } else {
                $arg["no_fukui"] = "1";
            }
            if ($model->Properties["useSpecial_Support_School"] != "1" && $model->Properties["unEar_db"] == "1") {
                $arg["no_fukui_ear"] = "";
            } else {
                $arg["no_fukui_ear"] = "1";
            }
        }
        if ($model->isMie) {
            $arg["is_mie"] = "1";
            $arg["is_not_mie"] = "";
        } else {
            $arg["is_mie"] = "";
            $arg["is_not_mie"] = "1";
        }

        if ($model->isHirokoudai) {
            $arg["EAR_TITLE"] = "1000Hz";
        } else {
            $arg["EAR_TITLE"] = "平均dB";
        }

        if ($model->isKoma) {
            $arg["is_koma"] = "1";
            $arg["EAR_TITLE"] = "";
        } else {
            $arg["no_koma"] = "1";
        }

        //特別支援学校
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $arg["useSpecial_Support_School"] = "1";
        }

        //寄生虫卵表示
        if (($model->school_kind == "P" && $model->Properties["useParasite_P"] == "1") || ($model->school_kind == "J" && $model->Properties["useParasite_J"] == "1") || ($model->school_kind == "H" && $model->Properties["useParasite_H"] == "1")) {
            $arg["para"] = 1;
            $arg["rowspan3"] = 8;
        } else {
            $arg["rowspan3"] = 7;
        }
        //メッセージ
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $arg["rowspan3"] += 1;
        }
        //尿)その他の検査テキスト、指導区分コンボ
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            $arg["rowspan3"] += 1;
        }

        //結核のX線撮影、所見、その他検査 非表示
        if ($model->Properties["printKenkouSindanIppan"] == "2" ||
            $model->Properties["printKenkouSindanIppan"] == "3") {
            $arg["rowspan2"] = 10;
        } else {
            $arg["rowspan2"] = 11;
        }
        if ($model->school_kind == "H") {
            $arg["tbFilmShow"] = 1;
        } else {
            $arg["tbFilmUnShow"] = 1;
            if ($model->Properties["printKenkouSindanIppan"] == "1" ||
                $model->Properties["printKenkouSindanIppan"] == "2" ||
                $model->Properties["printKenkouSindanIppan"] == "3") {
                $arg["rowspan2"] -= 2;
            } else {
                $arg["rowspan3"] -= 2;
            }
        }
        if ($model->isFukui) {
            $arg["rowspan3"] += 1;
        }

        //Enterキーで移動
        $entMove = " onkeydown=\"keyChangeEntToTab(this)\"";

        //未検査項目設定
        $notFieldSet = $sep = "";
        $query = knjf010jQuery::getMedexamDetNotExaminedDat($model);
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

        /**********************************/
        /***** 1項目目 ********************/
        /**********************************/
        //健康診断実施日付
        $RowH["DATE"] = str_replace("-", "/", $RowH["DATE"]);
        $arg["data"]["DATE"] = View::popUpCalendar2($objForm, "DATE", $RowH["DATE"], "", "", $entMove);

        //同じ年組の生徒の身長・体重取得
        $height = $weight = "";
        $sepH = $sepW = "";
        $result = $db->query(knjf010jQuery::getHeightWeightList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
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
        knjCreateHidden($objForm, "HEIGHT_LIST", $height);
        knjCreateHidden($objForm, "WEIGHT_LIST", $weight);
        //標準偏差値取得
        $sd = $db->getRow(knjf010jQuery::getSD($model), DB_FETCHMODE_ASSOC);

        //身長
        $colorH = ""; // (-2.0 > $sd["H_STDDEV"] || $sd["H_STDDEV"] > 2.0) ? "red" : "";
        $extra  = "onblur=\"return Num_Check(this);\" id=\"height\" style=\"color:{$colorH};\"";
        $arg["data"]["HEIGHT"] = knjCreateTextBox($objForm, $RowD["HEIGHT"], "HEIGHT", 5, 5, $extra.$entMove);
        //体重
        $colorW = ""; // (-2.0 > $sd["W_STDDEV"] || $sd["W_STDDEV"] > 2.0) ? "red" : "";
        $extra = "onblur=\"return Num_Check(this);\" id=\"weight\" style=\"color:{$colorW};\"";
        $arg["data"]["WEIGHT"] = knjCreateTextBox($objForm, $RowD["WEIGHT"], "WEIGHT", 5, 5, $extra.$entMove);

        //年齢取得
        $birthday = intval(str_replace('-', '', $RowB["BIRTHDAY"]));
        $today = intval(CTRL_YEAR.'0401');
        $age = intval(($today - $birthday) / 10000);
        //測定評価平均値データ取得
        $phys_avg = $db->getRow(knjf010jQuery::getHexamPhysicalAvgDat($model, $RowB["SEX"], $age), DB_FETCHMODE_ASSOC);
        knjCreateHidden($objForm, "STD_WEIGHT_KEISU_A", $phys_avg["STD_WEIGHT_KEISU_A"]);
        knjCreateHidden($objForm, "STD_WEIGHT_KEISU_B", $phys_avg["STD_WEIGHT_KEISU_B"]);
        //標準体重
        $stdWeght = $phys_avg["STD_WEIGHT_KEISU_A"] * $RowD["HEIGHT"] - $phys_avg["STD_WEIGHT_KEISU_B"];
        if ($RowD["HEIGHT"]) {
            $arg["data"]["STD_WEIGHT"] = "標準体重：".round($stdWeght, 1)."kg";
        }

        //座高
        $extra = "onblur=\"return Num_Check(this);\"";
        $arg["data"]["SITHEIGHT"] = knjCreateTextBox($objForm, $RowD["SITHEIGHT"], "SITHEIGHT", 5, 5, $extra.$entMove);

        //福井佐賀でないなら、既存。福井佐賀でも、値があるなら既存。
        $obesityIndex = 0;
        if (!($model->isFukui || $model->isSaga) || (($model->isFukui || $model->isSaga) && is_array($phys_avg) && get_count($phys_avg) > 0 && $phys_avg["STD_WEIGHT_KEISU_A"] != "" && $phys_avg["STD_WEIGHT_KEISU_B"] != "")) {
            if ($stdWeght > 0) {
                //肥満度 = (実測体重 ― 身長別標準体重) ／ 身長別標準体重 × 100（％）
                $obesityIndex = round((round($RowD["WEIGHT"], 1) - $stdWeght) / $stdWeght * 100, 1);
                $arg["data"]["OBESITY_INDEX"] = $obesityIndex.'%';
            }
        } elseif ($model->schregno != "") {
            //福井佐賀でDBから値が取得できなかった場合、下記計算を実施。端数(小数2桁以降)は切り捨て
            $normWeight = (($sex == "1" ? 0.733 : 0.56) * (int)$RowD["HEIGHT"]) - ($sex == "1" ? 70.989 : 37.002);
            $himanLev = floor((((int)$RowD["WEIGHT"] - $normWeight) / $normWeight) * 10.0) / 10.0 ;
            if ($himanLev < -0.3) {
                $outStr = "高度痩せ";
            } elseif ($himanLev == -0.2) {
                $outStr = "やせ";
            } elseif (in_array($himanLev, array(-0.1, 0, 0.1))) {
                $outStr = "普通";
            } elseif ($himanLev == 0.2) {
                $outStr = "軽度肥満";
            } elseif (in_array($himanLev, array(0.3, 0.4))) {
                $outStr = "中等度肥満";
            } else {
                $outStr = "高度肥満";
            }
            $arg["data"]["OBESITY_INDEX"] = $outStr;
        }

        //肥満度印字無しcheckbox
        if ($model->field["NO_PRINT_OBESITY_INDEX"] == "1") {
            $checkNI = " checked";
        } else {
            $checkNI = ($RowD["NO_PRINT_OBESITY_INDEX"] == "1") ? " checked" : "";
        }
        $extra = "id=\"NO_PRINT_OBESITY_INDEX\"";
        $arg["data"]["NO_PRINT_OBESITY_INDEX"] = knjCreateCheckBox($objForm, "NO_PRINT_OBESITY_INDEX", "1", $extra.$checkNI);

        //視力入力方法
        $opt001 = array(1, 2);
        $extra = array("id=\"NYURYOKU_HOUHO1\" checked onChange=\"changeReadOnly()\"", "id=\"NYURYOKU_HOUHO2\" onChange=\"changeReadOnly()\"");
        $radioArray = knjCreateRadio($objForm, "NYURYOKU_HOUHO", $model->field["NYURYOKU_HOUHO"], $extra, $opt001, get_count($opt001));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //視力・測定困難チェック時は、視力情報はグレーアウト
        $disVisionR = ($RowD["R_VISION_CANTMEASURE"] == "1") ? " disabled": "";
        $disVisionL = ($RowD["L_VISION_CANTMEASURE"] == "1") ? " disabled": "";

        $query = knjf010jQuery::getLrBarevisionMark($model);
        if ($model->field["R_VISION_CANTMEASURE"] == "1") {
            //視力・右裸眼(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"R_BAREVISION\" readonly disabled style=\"background-color:darkgray\"";
            $arg["data"]["R_BAREVISION"] = knjCreateTextBox($objForm, $RowD["R_BAREVISION"], "R_BAREVISION", 5, 5, $extra.$entMove.$disVisionR);
            //視力・右矯正(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"R_VISION\" readonly disabled style=\"background-color:darkgray\"";
            $arg["data"]["R_VISION"] = knjCreateTextBox($objForm, $RowD["R_VISION"], "R_VISION", 5, 5, $extra.$entMove.$disVisionR);

            //視力・右裸眼(文字)
            $extra = "id=\"R_BAREVISION_MARK\" readonly disabled";
            makeCmb($objForm, $arg, $db, $query, "R_BAREVISION_MARK", $RowD["R_BAREVISION_MARK"], $extra.$entMove.$disVisionR, 1, "BLANK");
            //視力・右矯正(文字)
            $extra = "id=\"R_VISION_MARK\" readonly disabled";
            makeCmb($objForm, $arg, $db, $query, "R_VISION_MARK", $RowD["R_VISION_MARK"], $extra.$entMove.$disVisionR, 1, "BLANK");
        } else {
            //視力・右裸眼(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"R_BAREVISION\" readonly style=\"background-color:darkgray\"";
            $arg["data"]["R_BAREVISION"] = knjCreateTextBox($objForm, $RowD["R_BAREVISION"], "R_BAREVISION", 5, 5, $extra.$entMove.$disVisionR);
            //視力・右矯正(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"R_VISION\" readonly style=\"background-color:darkgray\"";
            $arg["data"]["R_VISION"] = knjCreateTextBox($objForm, $RowD["R_VISION"], "R_VISION", 5, 5, $extra.$entMove.$disVisionR);

            //視力・右裸眼(記号)
            $extra = "id=\"R_BAREVISION_MARK\" readonly";
            makeCmb($objForm, $arg, $db, $query, "R_BAREVISION_MARK", $RowD["R_BAREVISION_MARK"], $extra.$entMove.$disVisionR, 1, "BLANK");
            //視力・右矯正(記号)
            $extra = "id=\"R_VISION_MARK\" readonly";
            makeCmb($objForm, $arg, $db, $query, "R_VISION_MARK", $RowD["R_VISION_MARK"], $extra.$entMove.$disVisionR, 1, "BLANK");
        }

        if ($model->field["L_VISION_CANTMEASURE"] == "1") {
            //視力・左裸眼(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"L_BAREVISION\" readonly disabled style=\"background-color:darkgray\"";
            $arg["data"]["L_BAREVISION"] = knjCreateTextBox($objForm, $RowD["L_BAREVISION"], "L_BAREVISION", 5, 5, $extra.$entMove.$disVisionL);
            //視力・左矯正(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"L_VISION\" readonly disabled style=\"background-color:darkgray\"";
            $arg["data"]["L_VISION"] = knjCreateTextBox($objForm, $RowD["L_VISION"], "L_VISION", 5, 5, $extra.$entMove.$disVisionL);

            //視力・左裸眼(文字)
            $extra = "id=\"L_BAREVISION_MARK\" readonly disabled";
            makeCmb($objForm, $arg, $db, $query, "L_BAREVISION_MARK", $RowD["L_BAREVISION_MARK"], $extra.$entMove.$disVisionL, 1, "BLANK");
            //視力・左矯正(文字)
            $extra = "id=\"L_VISION_MARK\" readonly disabled";
            makeCmb($objForm, $arg, $db, $query, "L_VISION_MARK", $RowD["L_VISION_MARK"], $extra.$entMove.$disVisionL, 1, "BLANK");
        } else {
            //視力・左裸眼(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"L_BAREVISION\" readonly style=\"background-color:darkgray\"";
            $arg["data"]["L_BAREVISION"] = knjCreateTextBox($objForm, $RowD["L_BAREVISION"], "L_BAREVISION", 5, 5, $extra.$entMove.$disVisionL);
            //視力・左矯正(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"L_VISION\" readonly style=\"background-color:darkgray\"";
            $arg["data"]["L_VISION"] = knjCreateTextBox($objForm, $RowD["L_VISION"], "L_VISION", 5, 5, $extra.$entMove.$disVisionL);

            //視力・左裸眼(記号)
            $extra = "id=\"L_BAREVISION_MARK\" readonly";
            makeCmb($objForm, $arg, $db, $query, "L_BAREVISION_MARK", $RowD["L_BAREVISION_MARK"], $extra.$entMove.$disVisionL, 1, "BLANK");
            //視力・左矯正(記号)
            $extra = "id=\"L_VISION_MARK\" readonly";
            makeCmb($objForm, $arg, $db, $query, "L_VISION_MARK", $RowD["L_VISION_MARK"], $extra.$entMove.$disVisionL, 1, "BLANK");
        }

        //測定不能チェックボックス
        if ($model->field["VISION_CANTMEASURE"] == "on") {
            $check_measure = "checked";
        } else {
            $check_measure = ($RowD["VISION_CANTMEASURE"] == "1") ? "checked" : "";
        }
        $extra = " id=\"VISION_CANTMEASURE\"";
        $arg["data"]["VISION_CANTMEASURE"] = knjCreateCheckBox($objForm, "VISION_CANTMEASURE", "on", $check_measure.$extra, "");

        //視力、測定困難(記号右)
        if ($model->field["R_VISION_CANTMEASURE"] == "1") {
            $checkV_R = "checked";
        } else {
            $checkV_R = ($RowD["R_VISION_CANTMEASURE"] == "1") ? " checked" : "";
        }
        $extra = "id=\"R_VISION_MARK_CANTMEASURE\" onClick=\"disVision(this, 'right');\"";
        $arg["data"]["R_VISION_MARK_CANTMEASURE"] = knjCreateCheckBox($objForm, "R_VISION_MARK_CANTMEASURE", "1", $extra.$checkV_R);

        //視力、測定困難(記号左)
        if ($model->field["L_VISION_CANTMEASURE"] == "1") {
            $checkV_L = "checked";
        } else {
            $checkV_L = ($RowD["L_VISION_CANTMEASURE"] == "1") ? " checked" : "";
        }
        $extra = "id=\"L_VISION_MARK_CANTMEASURE\" onClick=\"disVision(this, 'reft');\"";
        $arg["data"]["L_VISION_MARK_CANTMEASURE"] = knjCreateCheckBox($objForm, "L_VISION_MARK_CANTMEASURE", "1", $extra.$checkV_L);

        //視力、測定困難(数字右)
        if ($model->field["R_VISION_CANTMEASURE"] == "1") {
            $checkV_R = "checked";
        } else {
            $checkV_R = ($RowD["R_VISION_CANTMEASURE"] == "1") ? " checked" : "";
        }
        $extra = "id=\"R_VISION_CANTMEASURE\" onClick=\"disVision(this, 'right');\"";
        $arg["data"]["R_VISION_CANTMEASURE"] = knjCreateCheckBox($objForm, "R_VISION_CANTMEASURE", "1", $extra.$checkV_R);

        //視力、測定困難(数字左)
        if ($model->field["L_VISION_CANTMEASURE"] == "1") {
            $checkV_L = "checked";
        } else {
            $checkV_L = ($RowD["L_VISION_CANTMEASURE"] == "1") ? " checked" : "";
        }
        $extra = "id=\"L_VISION_CANTMEASURE\" onClick=\"disVision(this, 'reft');\"";
        $arg["data"]["L_VISION_CANTMEASURE"] = knjCreateCheckBox($objForm, "L_VISION_CANTMEASURE", "1", $extra.$checkV_L);

        //聴力・測定困難チェック時は、聴力情報はグレーアウト
        $disEarR = ($RowD["R_EAR_CANTMEASURE"] == "1") ? " disabled": "";
        $disEarL = ($RowD["L_EAR_CANTMEASURE"] == "1") ? " disabled": "";

        //聴力・右状態コンボ
        $optnull = array("label" => "","value" => "");   //初期値：空白項目
        $result  = $db->query(knjf010jQuery::getNameMst($model, 'F010'));
        $opt     = array();
        $opt[]   = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["VALUE"], 0, 2);
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();

        //聴力・右DB
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        if ($model->isKoma) {
            $query = knjf010jQuery::getNameMst($model, "F010");
            $extra = "".$entMove.$disEarR."";
            $arg["data"]["R_EAR_DB_1000"] = "1000Hz：&nbsp;".retMakeCmb($objForm, $arg, $db, $query, "R_EAR_DB_1000", $RowD["R_EAR_DB_1000"], $extra, 1, "blank");
        } else {
            $arg["data"]["R_EAR_DB"] = knjCreateTextBox($objForm, $RowD["R_EAR_DB"], "R_EAR_DB", 3, 3, $extra.$entMove.$disEarR);
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data"]["R_EAR"] = knjCreateCombo($objForm, "R_EAR", $RowD["R_EAR"], $opt, $extra.$entMove.$disEarR, 1);
        }
        //聴力・左DB
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        if ($model->isKoma) {
            $query = knjf010jQuery::getNameMst($model, "F010");
            $extra = "".$entMove.$disEarL;
            $arg["data"]["L_EAR_DB_1000"] = "1000Hz：&nbsp;".retMakeCmb($objForm, $arg, $db, $query, "L_EAR_DB_1000", $RowD["L_EAR_DB_1000"], $extra, 1, "blank");
        } else {
            $arg["data"]["L_EAR_DB"] = knjCreateTextBox($objForm, $RowD["L_EAR_DB"], "L_EAR_DB", 3, 3, $extra.$entMove.$disEarL);
            //聴力・左状態コンボ
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data"]["L_EAR"] = knjCreateCombo($objForm, "L_EAR", $RowD["L_EAR"], $opt, $extra.$entMove.$disEarL, 1);
        }
        if ($model->Properties["useEar4000Hz"] == "1") {
            if ($model->isKoma) {
                $query = knjf010jQuery::getNameMst($model, "F010");
                $extra = "".$entMove.$disEarR;
                $arg["data"]["R_EAR_DB_4000"] = "4000Hz：&nbsp;".retMakeCmb($objForm, $arg, $db, $query, "R_EAR_DB_4000", $RowD["R_EAR_DB_4000"], $extra, 1, "blank");
                $extra = "".$entMove.$disEarL;
                $arg["data"]["L_EAR_DB_4000"] = "4000Hz：&nbsp;".retMakeCmb($objForm, $arg, $db, $query, "L_EAR_DB_4000", $RowD["L_EAR_DB_4000"], $extra, 1, "blank");
            } else {
                //聴力・右・4000Hz
                $extra = "onblur=\"return Num_Check(this);\"";
                $arg["data"]["R_EAR_DB_4000"] = "4000Hz：&nbsp;".knjCreateTextBox($objForm, $RowD["R_EAR_DB_4000"], "R_EAR_DB_4000", 4, 3, $extra.$entMove.$disEarR);
                //聴力・左・4000Hz
                $extra = "onblur=\"return Num_Check(this);\"";
                $arg["data"]["L_EAR_DB_4000"] = "4000Hz：&nbsp;".knjCreateTextBox($objForm, $RowD["L_EAR_DB_4000"], "L_EAR_DB_4000", 4, 3, $extra.$entMove.$disEarL);
            }
        }

        /* 装用時 */
        //聴力・右DB
        $extra = "onblur=\"return Num_Check(this);\"";
        $arg["data"]["R_EAR_DB_IN"] = knjCreateTextBox($objForm, $RowD["R_EAR_DB_IN"], "R_EAR_DB_IN", 4, 3, $extra.$entMove.$disEarR);

        //聴力・右状態コンボ
        $optnull = array("label" => "", "value" => "");   //初期値：空白項目
        $result  = $db->query(knjf010jQuery::getLrEAR($model, ""));
        $opt     = array();
        $opt[]   = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $extra = "";
        $arg["data"]["R_EAR_IN"] = knjCreateCombo($objForm, "R_EAR_IN", $RowD["R_EAR_IN"], $opt, $extra.$entMove.$disEarR, 1);
        //聴力・左DB
        $extra = "onblur=\"return Num_Check(this);\"";
        $arg["data"]["L_EAR_DB_IN"] = knjCreateTextBox($objForm, $RowD["L_EAR_DB_IN"], "L_EAR_DB_IN", 4, 3, $extra.$entMove.$disEarL);
        //聴力・左状態コンボ
        $extra = "";
        $arg["data"]["L_EAR_IN"] = knjCreateCombo($objForm, "L_EAR_IN", $RowD["L_EAR_IN"], $opt, $extra.$entMove.$disEarL, 1);
        if ($model->Properties["useEar4000Hz"] == "1") {
            //聴力・右・4000Hz
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data"]["R_EAR_DB_4000_IN"] = "4000Hz：&nbsp;".knjCreateTextBox($objForm, $RowD["R_EAR_DB_4000_IN"], "R_EAR_DB_4000_IN", 4, 3, $extra.$entMove.$disEarR);
            //聴力・左・4000Hz
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data"]["L_EAR_DB_4000_IN"] = "4000Hz：&nbsp;".knjCreateTextBox($objForm, $RowD["L_EAR_DB_4000_IN"], "L_EAR_DB_4000_IN", 4, 3, $extra.$entMove.$disEarL);
        }

        //聴力、測定困難(右)
        if ($model->field["R_EAR_CANTMEASURE"] == "1") {
            $checkE_R = "checked";
        } else {
            $checkE_R = ($RowD["R_EAR_CANTMEASURE"] == "1") ? " checked" : "";
        }
        $extra = "id=\"R_EAR_CANTMEASURE\" onClick=\"disEar(this, 'right');\"";
        $arg["data"]["R_EAR_CANTMEASURE"] = knjCreateCheckBox($objForm, "R_EAR_CANTMEASURE", "1", $extra.$checkE_R);

        //聴力、測定困難(左)
        if ($model->field["L_EAR_CANTMEASURE"] == "1") {
            $checkE_L = "checked";
        } else {
            $checkE_L = ($RowD["L_EAR_CANTMEASURE"] == "1") ? " checked" : "";
        }
        $extra = "id=\"L_EAR_CANTMEASURE\" onClick=\"disEar(this, 'reft');\"";
        $arg["data"]["L_EAR_CANTMEASURE"] = knjCreateCheckBox($objForm, "L_EAR_CANTMEASURE", "1", $extra.$checkE_L);

        /**********************************/
        /***** 2項目目 ********************/
        /**********************************/
        $extra = "";
        //尿・１次蛋白コンボ
        //尿・再検査白コンボ
        $opt    = array();
        $opt[]  = $optnull;
        $result = $db->query(knjf010jQuery::getUric($model, "F020"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $arg["data"]["ALBUMINURIA1CD"] = knjCreateCombo($objForm, "ALBUMINURIA1CD", $RowD["ALBUMINURIA1CD"], $opt, $extra.$entMove, 1);
        $arg["data"]["ALBUMINURIA2CD"] = knjCreateCombo($objForm, "ALBUMINURIA2CD", $RowD["ALBUMINURIA2CD"], $opt, $extra.$entMove, 1);
        //尿・１次糖コンボ
        //尿・再検査糖コンボ
        $opt    = array();
        $opt[]  = $optnull;
        $result = $db->query(knjf010jQuery::getUric($model, "F019"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $arg["data"]["URICSUGAR1CD"] = knjCreateCombo($objForm, "URICSUGAR1CD", $RowD["URICSUGAR1CD"], $opt, $extra.$entMove, 1);
        $arg["data"]["URICSUGAR2CD"] = knjCreateCombo($objForm, "URICSUGAR2CD", $RowD["URICSUGAR2CD"], $opt, $extra.$entMove, 1);
        //尿・１次潜血コンボ
        //尿・再検査潜血コンボ
        $opt    = array();
        $opt[]  = $optnull;
        $result = $db->query(knjf010jQuery::getUric($model, "F018"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $arg["data"]["URICBLEED1CD"] = knjCreateCombo($objForm, "URICBLEED1CD", $RowD["URICBLEED1CD"], $opt, $extra.$entMove, 1);
        $arg["data"]["URICBLEED2CD"] = knjCreateCombo($objForm, "URICBLEED2CD", $RowD["URICBLEED2CD"], $opt, $extra.$entMove, 1);

        //尿・精密検査コンボ
        $opt    = array();
        $opt[]  = $optnull;
        $result = $db->query(knjf010jQuery::getNameMst($model, "F146"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $disableCodes = getDisabledCodes($db, "F146");

        $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].URICOTHERTEST, '".implode(",", $disableCodes)."')\"";
        $arg["data"]["DETAILED_EXAMINATION"] = knjCreateCombo($objForm, "DETAILED_EXAMINATION", $RowD["DETAILED_EXAMINATION"], $opt, $extra.$entMove, 1);

        //尿:精密検査(所見)
        if (in_array($RowD["DETAILED_EXAMINATION"], $disableCodes) == true || $RowD["DETAILED_EXAMINATION"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["URICOTHERTEST"] = knjCreateTextBox($objForm, $RowD["URICOTHERTEST"], "URICOTHERTEST", 40, 20, $extra.$entMove);

        //尿:精密検査(指導区分コンボ)
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F021"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "";
        $arg["data"]["URI_ADVISECD"] = knjCreateCombo($objForm, "URI_ADVISECD", $RowD["URI_ADVISECD"], $opt, $extra.$entMove, 1);

        //その他疾病及び異常
        $extra = "";
        $arg["data"]["OTHER_REMARK"] = knjCreateTextBox($objForm, $RowD["OTHER_REMARK"], "OTHER_REMARK", 40, 20, $extra.$entMove);

        //栄養状態コンボ
        $isNormalWeight = "";
        $nutrInfo = $sep = '';
        $result     = $db->query(knjf010jQuery::getNutrition($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);

            if ($row["ABBV1"] == '' && $row["ABBV3"] == '') {
            } else {
                if ($row["ABBV1"] < $obesityIndex && $obesityIndex < $row["ABBV3"] && $RowD["NUTRITIONCD"] == "" && $RowD["HEIGHT"] != "" && $RowD["WEIGHT"] != "") {
                    $isNormalWeight = $row["NAMECD2"];
                }
                $nutrInfo .= $sep.$row["NAMECD2"].':'.$row["ABBV1"].':'.$row["ABBV3"];
                $sep = ',';
            }
        }
        knjCreateHidden($objForm, "nutrInfo", $nutrInfo);
        $result->free();

        $disableCodes = getDisabledCodes($db, "F030");

        $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].NUTRITIONCD_REMARK, '".implode(",", $disableCodes)."')\"";
        if ($isNormalWeight != "") {
            $RowD["NUTRITIONCD"] = $isNormalWeight;
        }
        $arg["data"]["NUTRITIONCD"] = knjCreateCombo($objForm, "NUTRITIONCD", $RowD["NUTRITIONCD"], $opt, $extra.$entMove, 1);

        //栄養状態テキスト
        if (in_array($RowD["NUTRITIONCD"], $disableCodes) == true || $RowD["NUTRITIONCD"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["NUTRITIONCD_REMARK"] = knjCreateTextBox($objForm, $RowD["NUTRITIONCD_REMARK"], "NUTRITIONCD_REMARK", 40, 20, $extra.$entMove);

        //目の疾病及び異常コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F050"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $disableCodes = getDisabledCodes($db, "F050");

        $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].EYE_TEST_RESULT, '".implode(",", $disableCodes)."')\"";

        $arg["data"]["EYEDISEASECD"] = knjCreateCombo($objForm, "EYEDISEASECD", $RowD["EYEDISEASECD"], $opt, $extra.$entMove, 1);

        /*****************************/
        if (in_array($RowD["EYEDISEASECD"], $disableCodes) == true || $RowD["EYEDISEASECD"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["EYE_TEST_RESULT"] = knjCreateTextBox($objForm, $RowD["EYE_TEST_RESULT"], "EYE_TEST_RESULT", 40, 20, $extra.$entMove);
        /*****************************/

        //色覚異常コンボ
        $opt        = array();
        $opt[]      = $optnull;
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F051"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $disableCodes = getDisabledCodes($db, "F051");

        $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].VISION_CANTMEASURE, '".implode(",", $disableCodes)."')\"";
        $arg["data"]["EYEDISEASECD5"] = knjCreateCombo($objForm, "EYEDISEASECD5", $RowD["EYEDISEASECD5"], $opt, $extra.$entMove, 1);

        //色覚異常(所見)
        if (in_array($RowD["EYEDISEASECD5"], $disableCodes) == true || $RowD["EYEDISEASECD5"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["VISION_CANTMEASURE"] = knjCreateTextBox($objForm, $RowD["VISION_CANTMEASURE"], "VISION_CANTMEASURE", 40, 20, $extra.$entMove);

        //脊柱・胸郭・四肢(全般)コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F040"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();

        $disableCodes = getDisabledCodes($db, "F040");

        $target_objs = "SPINERIBCD1,SPINERIBCD2,SPINERIBCD3,SPINERIBCD_REMARK,SPINERIBCD_REMARK1,SPINERIBCD_REMARK2,SPINERIBCD_REMARK3";
        $text_objs   = "SPINERIBCD_REMARK1,SPINERIBCD_REMARK2,SPINERIBCD_REMARK3";
        $combo_objs   = "SPINERIBCD1,SPINERIBCD2,SPINERIBCD3";

        $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku2(this, '".$target_objs."','".implode(",", $disableCodes)."', '".$text_objs."','".implode(",", getDisabledCodes($db, "F041"))."', '".$combo_objs."')\"";
        $arg["data"]["SPINERIBCD"] = knjCreateCombo($objForm, "SPINERIBCD", $RowD["SPINERIBCD"], $opt, $extra.$entMove, 1);

        //脊柱・胸郭・四肢(疾患)コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F041"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();

        //脊柱・胸郭・四肢(全般コンボが未選択、未受検、異常なしのとき)
        $disableCodes = getDisabledCodes($db, "F041");
        if (in_array($RowD["SPINERIBCD"], getDisabledCodes($db, "F040")) == true || $RowD["SPINERIBCD"] == '') {
            $extra = "style=\"width:200px;\" disabled onChange=\"syokenNyuryoku(this, document.forms[0].SPINERIBCD_REMARK1, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["SPINERIBCD1"] = knjCreateCombo($objForm, "SPINERIBCD1", $RowD["SPINERIBCD1"], $opt, $extra.$entMove, 1);

            $extra = "style=\"width:200px;\" disabled onChange=\"syokenNyuryoku(this, document.forms[0].SPINERIBCD_REMARK2, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["SPINERIBCD2"] = knjCreateCombo($objForm, "SPINERIBCD2", $RowD["SPINERIBCD2"], $opt, $extra.$entMove, 1);

            $extra = "style=\"width:200px;\" disabled onChange=\"syokenNyuryoku(this, document.forms[0].SPINERIBCD_REMARK3, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["SPINERIBCD3"] = knjCreateCombo($objForm, "SPINERIBCD3", $RowD["SPINERIBCD3"], $opt, $extra.$entMove, 1);
        } else {
            //それ以外の時
            $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].SPINERIBCD_REMARK1, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["SPINERIBCD1"] = knjCreateCombo($objForm, "SPINERIBCD1", $RowD["SPINERIBCD1"], $opt, $extra.$entMove, 1);

            $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].SPINERIBCD_REMARK2, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["SPINERIBCD2"] = knjCreateCombo($objForm, "SPINERIBCD2", $RowD["SPINERIBCD2"], $opt, $extra.$entMove, 1);

            $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].SPINERIBCD_REMARK3, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["SPINERIBCD3"] = knjCreateCombo($objForm, "SPINERIBCD3", $RowD["SPINERIBCD3"], $opt, $extra.$entMove, 1);
        }

        /*****************************/
        //脊柱・胸郭・四肢テキスト
        //疾患1テキスト
        if (in_array($RowD["SPINERIBCD1"], $disableCodes) == true || $RowD["SPINERIBCD1"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["SPINERIBCD_REMARK1"] = knjCreateTextBox($objForm, $RowD["SPINERIBCD_REMARK1"], "SPINERIBCD_REMARK1", 40, 20, $extra.$entMove);

        //疾患2テキスト
        if (in_array($RowD["SPINERIBCD2"], $disableCodes) == true || $RowD["SPINERIBCD2"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["SPINERIBCD_REMARK2"] = knjCreateTextBox($objForm, $RowD["SPINERIBCD_REMARK2"], "SPINERIBCD_REMARK2", 40, 20, $extra.$entMove);

        //疾患3テキスト
        if (in_array($RowD["SPINERIBCD3"], $disableCodes) == true || $RowD["SPINERIBCD3"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["SPINERIBCD_REMARK3"] = knjCreateTextBox($objForm, $RowD["SPINERIBCD_REMARK3"], "SPINERIBCD_REMARK3", 40, 20, $extra.$entMove);

        //脊柱・胸郭・四肢の全般がdisabled設定のとき
        if (in_array($RowD["SPINERIBCD"], getDisabledCodes($db, "F040")) == true || $RowD["SPINERIBCD"] == '') {
            $extra = "disabled";
            $arg["data"]["SPINERIBCD_REMARK1"] = knjCreateTextBox($objForm, $RowD["SPINERIBCD_REMARK1"], "SPINERIBCD_REMARK1", 40, 20, $extra.$entMove);
            $arg["data"]["SPINERIBCD_REMARK2"] = knjCreateTextBox($objForm, $RowD["SPINERIBCD_REMARK2"], "SPINERIBCD_REMARK2", 40, 20, $extra.$entMove);
            $arg["data"]["SPINERIBCD_REMARK3"] = knjCreateTextBox($objForm, $RowD["SPINERIBCD_REMARK3"], "SPINERIBCD_REMARK3", 40, 20, $extra.$entMove);
        } else {
            $extra = "";
        }
        $arg["data"]["SPINERIBCD_REMARK"]  = knjCreateTextBox($objForm, $RowD["SPINERIBCD_REMARK"], "SPINERIBCD_REMARK", 40, 20, $extra.$entMove);
        /*****************************/

        //耳鼻咽頭疾患(全般コンボ)
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F060"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();

        $disableCodes = getDisabledCodes($db, "F060");

        $target_objs  = "NOSEDISEASECD5,NOSEDISEASECD6,NOSEDISEASECD7,NOSEDISEASECD_REMARK,NOSEDISEASECD_REMARK1,NOSEDISEASECD_REMARK2,NOSEDISEASECD_REMARK3";
        $text_objs    = "NOSEDISEASECD_REMARK1,NOSEDISEASECD_REMARK2,NOSEDISEASECD_REMARK3";
        $combo_objs   = "NOSEDISEASECD5,NOSEDISEASECD6,NOSEDISEASECD6";

        $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku2(this, '".$target_objs."','".implode(",", $disableCodes)."', '".$text_objs."','".implode(",", getDisabledCodes($db, "F061"))."', '".$combo_objs."')\"";
        $arg["data"]["NOSEDISEASECD"] = knjCreateCombo($objForm, "NOSEDISEASECD", $RowD["NOSEDISEASECD"], $opt, $extra.$entMove, 1);

        //耳鼻咽頭疾患(疾患コンボ)
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F061"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();

        //耳鼻咽喉疾患(全般コンボが未選択、未受検、異常なしのとき)
        $disableCodes = getDisabledCodes($db, "F061");
        if (in_array($RowD["NOSEDISEASECD"], getDisabledCodes($db, "F060")) == true || $RowD["NOSEDISEASECD"] == '') {
            $extra = "style=\"width:200px;\" disabled onChange=\"syokenNyuryoku(this, document.forms[0].NOSEDISEASECD_REMARK1, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["NOSEDISEASECD5"] = knjCreateCombo($objForm, "NOSEDISEASECD5", $RowD["NOSEDISEASECD5"], $opt, $extra.$entMove, 1);

            $extra = "style=\"width:200px;\" disabled onChange=\"syokenNyuryoku(this, document.forms[0].NOSEDISEASECD_REMARK2, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["NOSEDISEASECD6"] = knjCreateCombo($objForm, "NOSEDISEASECD6", $RowD["NOSEDISEASECD6"], $opt, $extra.$entMove, 1);

            $extra = "style=\"width:200px;\" disabled onChange=\"syokenNyuryoku(this, document.forms[0].NOSEDISEASECD_REMARK3, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["NOSEDISEASECD7"] = knjCreateCombo($objForm, "NOSEDISEASECD7", $RowD["NOSEDISEASECD7"], $opt, $extra.$entMove, 1);
        } else {
            //それ以外の時
            $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].NOSEDISEASECD_REMARK1, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["NOSEDISEASECD5"] = knjCreateCombo($objForm, "NOSEDISEASECD5", $RowD["NOSEDISEASECD5"], $opt, $extra.$entMove, 1);

            $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].NOSEDISEASECD_REMARK2, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["NOSEDISEASECD6"] = knjCreateCombo($objForm, "NOSEDISEASECD6", $RowD["NOSEDISEASECD6"], $opt, $extra.$entMove, 1);

            $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].NOSEDISEASECD_REMARK3, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["NOSEDISEASECD7"] = knjCreateCombo($objForm, "NOSEDISEASECD7", $RowD["NOSEDISEASECD7"], $opt, $extra.$entMove, 1);
        }

        //耳鼻咽喉疾患テキスト
        //疾患1テキスト
        if (in_array($RowD["NOSEDISEASECD5"], $disableCodes) == true || $RowD["NOSEDISEASECD5"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["NOSEDISEASECD_REMARK1"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK1"], "NOSEDISEASECD_REMARK1", 40, 20, $extra.$entMove);

        //疾患2テキスト
        if (in_array($RowD["NOSEDISEASECD6"], $disableCodes) == true || $RowD["NOSEDISEASECD6"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["NOSEDISEASECD_REMARK2"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK2"], "NOSEDISEASECD_REMARK2", 40, 20, $extra.$entMove);

        //疾患3テキスト
        if (in_array($RowD["NOSEDISEASECD7"], $disableCodes) == true || $RowD["NOSEDISEASECD7"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["NOSEDISEASECD_REMARK3"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK3"], "NOSEDISEASECD_REMARK3", 40, 20, $extra.$entMove);

        //耳鼻咽喉疾患の全般がdisabled設定のとき
        if (in_array($RowD["NOSEDISEASECD"], getDisabledCodes($db, "F060")) == true || $RowD["NOSEDISEASECD"] == '') {
            $extra = "disabled";
            $arg["data"]["NOSEDISEASECD_REMARK1"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK1"], "NOSEDISEASECD_REMARK1", 40, 20, $extra.$entMove);
            $arg["data"]["NOSEDISEASECD_REMARK2"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK2"], "NOSEDISEASECD_REMARK2", 40, 20, $extra.$entMove);
            $arg["data"]["NOSEDISEASECD_REMARK3"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK3"], "NOSEDISEASECD_REMARK3", 40, 20, $extra.$entMove);
        } else {
            $extra = "";
        }
        $arg["data"]["NOSEDISEASECD_REMARK"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK"], "NOSEDISEASECD_REMARK", 40, 20, $extra.$entMove);

        /*****************************/

        //皮膚疾患コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F070"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();

        $disableCodes = getDisabledCodes($db, "F070");
        $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].SKINDISEASECD_REMARK, '".implode(",", $disableCodes)."')\"";
        $arg["data"]["SKINDISEASECD"] = knjCreateCombo($objForm, "SKINDISEASECD", $RowD["SKINDISEASECD"], $opt, $extra.$entMove, 1);

        //皮膚疾患テキスト
        if (in_array($RowD["SKINDISEASECD"], $disableCodes) == true || $RowD["SKINDISEASECD"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["SKINDISEASECD_REMARK"] = knjCreateTextBox($objForm, $RowD["SKINDISEASECD_REMARK"], "SKINDISEASECD_REMARK", 40, 20, $extra.$entMove);

        //心臓・臨床医学的検査コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F080"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();

        $disableCodes = getDisabledCodes($db, "F080");
        $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].HEART_MEDEXAM_REMARK, '".implode(",", $disableCodes)."')\"";
        $arg["data"]["HEART_MEDEXAM"] = knjCreateCombo($objForm, "HEART_MEDEXAM", $RowD["HEART_MEDEXAM"], $opt, $extra.$entMove, 1);

        //心臓・臨床医学的検査テキスト
        if (in_array($RowD["HEART_MEDEXAM"], $disableCodes) == true || $RowD["HEART_MEDEXAM"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["HEART_MEDEXAM_REMARK"] = knjCreateTextBox($objForm, $RowD["HEART_MEDEXAM_REMARK"], "HEART_MEDEXAM_REMARK", 80, 40, $extra.$entMove);

        //心臓・精密検査コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F091"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $disableCodes = getDisabledCodes($db, "F091");

        $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].MANAGEMENT_REMARK, '".implode(",", $disableCodes)."')\"";
        $arg["data"]["MANAGEMENT_DIV"] = knjCreateCombo($objForm, "MANAGEMENT_DIV", $RowD["MANAGEMENT_DIV"], $opt, $extra.$entMove, 1);

        //心臓・精密検査テキスト
        if (in_array($RowD["MANAGEMENT_DIV"], $disableCodes) == true || $RowD["MANAGEMENT_DIV"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["MANAGEMENT_REMARK"] = knjCreateTextBox($objForm, $RowD["MANAGEMENT_REMARK"], "MANAGEMENT_REMARK", 80, 40, $extra.$entMove);

        /*****************************/
        /*****************************/

        //心臓・疾病及び異常コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F090"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();

        $disableCodes = getDisabledCodes($db, "F090");
        $extra = "style=\"width:250px;\" onChange=\"syokenNyuryoku(this, document.forms[0].HEARTDISEASECD_REMARK, '".implode(",", $disableCodes)."')\"";
        $arg["data"]["HEARTDISEASECD"] = knjCreateCombo($objForm, "HEARTDISEASECD", $RowD["HEARTDISEASECD"], $opt, $extra.$entMove, 1);

        //心臓・疾病及び異常テキスト
        if (in_array($RowD["HEARTDISEASECD"], $disableCodes) == true || $RowD["HEARTDISEASECD"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["HEARTDISEASECD_REMARK"] = knjCreateTextBox($objForm, $RowD["HEARTDISEASECD_REMARK"], "HEARTDISEASECD_REMARK", 40, 20, $extra.$entMove);
        /*****************************/

        //結核・撮影日付
        $RowD["TB_FILMDATE"] = str_replace("-", "/", $RowD["TB_FILMDATE"]);
        $arg["data"]["TB_FILMDATE"] = View::popUpCalendar2($objForm, "TB_FILMDATE", $RowD["TB_FILMDATE"], "", "", $entMove);

        //結核・再検査
        $RowD["TB_RE_EXAMINATION_DATE"] = str_replace("-", "/", $RowD["TB_RE_EXAMINATION_DATE"]);
        $arg["data"]["TB_RE_EXAMINATION_DATE"] = View::popUpCalendar2($objForm, "TB_RE_EXAMINATION_DATE", $RowD["TB_RE_EXAMINATION_DATE"], "", "", $entMove);

        //結核・フィルム番号
        $arg["data"]["TB_FILMNO"] = knjCreateTextBox($objForm, $RowD["TB_FILMNO"], "TB_FILMNO", $model->tb_filmnoFieldSize, $model->tb_filmnoFieldSize, $entMove);
        $arg["data"]["TB_FILMNO_SIZE"] = $model->tb_filmnoFieldSize;

        //結核・再検査フィルム番号
        $arg["data"]["TB_RE_EXAMINATION_FILMNO"] = knjCreateTextBox($objForm, $RowD["TB_RE_EXAMINATION_FILMNO"], "TB_RE_EXAMINATION_FILMNO", $model->tb_filmnoFieldSize, $model->tb_filmnoFieldSize, $entMove);

        //結核・所見コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F100"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $disableCodes = getDisabledCodes($db, "F100");

        $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].TB_X_RAY, '".implode(",", $disableCodes)."')\"";
        $arg["data"]["TB_REMARKCD"] = knjCreateCombo($objForm, "TB_REMARKCD", $RowD["TB_REMARKCD"], $opt, $extra.$entMove, 1);

        //結核・所見テキストボックス
        if (in_array($RowD["TB_REMARKCD"], $disableCodes) == true || $RowD["TB_REMARKCD"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["TB_X_RAY"] = knjCreateTextBox($objForm, $RowD["TB_X_RAY"], "TB_X_RAY", 40, 20, $extra.$entMove);

        //結核・再検査・結果
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F101"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "";
        $arg["data"]["TB_RE_EXAMINATION_RESULT"] = knjCreateCombo($objForm, "TB_RE_EXAMINATION_RESULT", $RowD["TB_RE_EXAMINATION_RESULT"], $opt, $extra.$entMove, 1);

        //結核・その他検査コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F110"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "style=\"width:400px;\"";
        $arg["data"]["TB_OTHERTESTCD"] = knjCreateCombo($objForm, "TB_OTHERTESTCD", $RowD["TB_OTHERTESTCD"], $opt, $extra.$entMove, 1);
        $arg["data"]["TB_OTHERTEST_REMARK1"] = knjCreateTextBox($objForm, "TB_OTHERTEST_REMARK1", $RowD["TB_OTHERTEST_REMARK1"], 40, 20, $extra.$entMove);

        //結核・疾病及び異常コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F120"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $disableCodes = getDisabledCodes($db, "F120");

        $extra = "style=\"width:100px;\" onChange=\"syokenNyuryoku(this, document.forms[0].TB_NAME_REMARK1, '".implode(",", $disableCodes)."')\"";
        $arg["data"]["TB_NAMECD"] = knjCreateCombo($objForm, "TB_NAMECD", $RowD["TB_NAMECD"], $opt, $extra.$entMove, 1);

        //結核・疾病及び異常テキスト
        if (in_array($RowD["TB_NAMECD"], $disableCodes) == true || $RowD["TB_NAMECD"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["TB_NAME_REMARK1"] = knjCreateTextBox($objForm, $RowD["TB_NAME_REMARK1"], "TB_NAME_REMARK1", 40, 20, $extra.$entMove);

        //結核・指導区分コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F130"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "style=\"width:150px;\"";
        $arg["data"]["TB_ADVISECD"] = knjCreateCombo($objForm, "TB_ADVISECD", $RowD["TB_ADVISECD"], $opt, $extra.$entMove, 1);

        //寄生虫卵コンボ
        $query = knjf010jQuery::getNameMst($model, "F023");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "PARASITE", $RowD["PARASITE"], $extra.$entMove, 1, "blank");

        //その他疾病及び異常コンボ
        $result     = $db->query(knjf010jQuery::getOtherDisease($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        if ($model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].OTHER_REMARK2)\"";
        } else {
            $extra = "style=\"width:170px;\"";
        }
        $arg["data"]["OTHERDISEASECD"] = knjCreateCombo($objForm, "OTHERDISEASECD", $RowD["OTHERDISEASECD"], $opt, $extra.$entMove, 1);

        //その他疾病及び異常:指導区分コンボ
        $result     = $db->query(knjf010jQuery::getOtherAdvisecd($model));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "style=\"width:170px;\"";
        $arg["data"]["OTHER_ADVISECD"] = knjCreateCombo($objForm, "OTHER_ADVISECD", $RowD["OTHER_ADVISECD"], $opt, $extra.$entMove, 1);

        //貧血
        //所見
        $extra = "";
        $arg["data"]["ANEMIA_REMARK"] = knjCreateTextBox($objForm, $RowD["ANEMIA_REMARK"], "ANEMIA_REMARK", 21, 30, $extra);

        //ヘモグロビン
        $extra = "style=\"text-align:right\" onblur=\"return Num_Check(this);\"";
        $arg["data"]["HEMOGLOBIN"] = knjCreateTextBox($objForm, $RowD["HEMOGLOBIN"], "HEMOGLOBIN", 4, 4, $extra);

        //学校医・所見（内科検診）
        $arg["data"]["DOC_REMARK"] = knjCreateTextBox($objForm, $RowD["DOC_REMARK"], "DOC_REMARK", 40, 20, $entMove);

        //学校医・所見日付
        $RowD["DOC_DATE"] = str_replace("-", "/", $RowD["DOC_DATE"]);
        $arg["data"]["DOC_DATE"] = View::popUpCalendar2($objForm, "DOC_DATE", $RowD["DOC_DATE"], "", "", $entMove);

        //事後措置1コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F150"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();

        $disableCodes = getDisabledCodes($db, "F150");
        $target_objs  = "TREAT_REMARK1,TREATCD2,TREATCD2_REMARK1";
        $text_objs    = "TREATCD2_REMARK1";
        $combo_objs   = "TREATCD2";
        $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku2(this, '".$target_objs."','".implode(",", $disableCodes)."', '".$text_objs."','".implode(",", getDisabledCodes($db, "F150"))."', '".$combo_objs."')\"";
        $arg["data"]["TREATCD"] = knjCreateCombo($objForm, "TREATCD", $RowD["TREATCD"], $opt, $extra.$entMove, 1);

        //事後措置2コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F151"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();

        $disableCodes = getDisabledCodes($db, "F151");
        if (in_array($RowD["TREATCD"], getDisabledCodes($db, "F150")) == true || $RowD["TREATCD"] == '') {
            $extra = "style=\"width:170px;\" disabled onChange=\"syokenNyuryoku(this, document.forms[0].TREATCD2_REMARK1, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["TREATCD2"] = knjCreateCombo($objForm, "TREATCD2", $RowD["TREATCD2"], $opt, $extra.$entMove, 1);
        } else {
            $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].TREATCD2_REMARK1, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["TREATCD2"] = knjCreateCombo($objForm, "TREATCD2", $RowD["TREATCD2"], $opt, $extra.$entMove, 1);
        }
        //事後措置:所見2
        if (in_array($RowD["TREATCD2"], $disableCodes) == true || $RowD["TREATCD2"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["TREATCD2_REMARK1"] = knjCreateTextBox($objForm, $RowD["TREATCD2_REMARK1"], "TREATCD2_REMARK1", 40, 20, $extra.$entMove);

        if (in_array($RowD["TREATCD"], getDisabledCodes($db, "F150")) == true || $RowD["TREATCD"] == '') {
            $extra = "disabled";
            $arg["data"]["TREATCD2_REMARK1"] = knjCreateTextBox($objForm, $RowD["TREATCD2_REMARK1"], "TREATCD2_REMARK1", 40, 20, $extra.$entMove);
        } else {
            $extra = "";
        }
        //事後措置:所見1
        $arg["data"]["TREAT_REMARK1"] = knjCreateTextBox($objForm, $RowD["TREAT_REMARK1"], "TREAT_REMARK1", 40, 20, $extra.$entMove);

        //連絡欄
        if ($model->Properties["useSpecial_Support_School"] == "1") { //特別支援学校
            $model->maxRemarkByte = '300';
            $arg["data"]["REMARK_MOJI"] = '100';
        } else {
            $model->maxRemarkByte = '180';
            $arg["data"]["REMARK_MOJI"] = '60';
        }
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $RowD["REMARK"], "REMARK", 120, $model->maxRemarkByte, $entMove);

        //メッセージ
        $gyo = 4;
        $moji = 21;
        $arg["data"]["MESSAGE"] = KnjCreateTextArea($objForm, "MESSAGE", $gyo, ((int)$moji * 2 + 1), "soft", "", $RowD["MESSAGE"]);

        /**********************************/
        /***** 4項目目 ********************/
        /**********************************/
        //既往症
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F143"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "";
        $arg["data"]["MEDICAL_HISTORY1"] = knjCreateCombo($objForm, "MEDICAL_HISTORY1", $RowD["MEDICAL_HISTORY1"], $opt, $extra.$entMove, 1);
        $arg["data"]["MEDICAL_HISTORY2"] = knjCreateCombo($objForm, "MEDICAL_HISTORY2", $RowD["MEDICAL_HISTORY2"], $opt, $extra.$entMove, 1);
        $arg["data"]["MEDICAL_HISTORY3"] = knjCreateCombo($objForm, "MEDICAL_HISTORY3", $RowD["MEDICAL_HISTORY3"], $opt, $extra.$entMove, 1);

        //診断名
        $extra = "";
        $arg["data"]["DIAGNOSIS_NAME"] = knjCreateTextBox($objForm, $RowD["DIAGNOSIS_NAME"], "DIAGNOSIS_NAME", 100, 50, $extra.$entMove);


        //運動/指導区分
        $result     = $db->query(knjf010jQuery::getGuideDiv($model));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "";
        $arg["data"]["GUIDE_DIV"] = knjCreateCombo($objForm, "GUIDE_DIV", $RowD["GUIDE_DIV"], $opt, $extra.$entMove, 1);

        //運動/部活動
        $result     = $db->query(knjf010jQuery::getJoiningSportsClub($model));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "style=\"width:170px;\"";
        $arg["data"]["JOINING_SPORTS_CLUB"] = knjCreateCombo($objForm, "JOINING_SPORTS_CLUB", $RowD["JOINING_SPORTS_CLUB"], $opt, $extra.$entMove, 1);

        Query::dbCheckIn($db);

        /* ボタン作成 */
        //一括更新ボタン1
        $link = REQUESTROOT."/F/KNJF010J/knjf010jindex.php?cmd=replace1&SCHREGNO=".$model->schregno;
        $extra = "style=\"width:80px\" onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["btn_replace1"] = knjCreateBtn($objForm, "btn_replace", "一括更新1", $extra);

        //一括更新ボタン2
        $link = REQUESTROOT."/F/KNJF010J/knjf010jindex.php?cmd=replace2&SCHREGNO=".$model->schregno;
        $extra = "style=\"width:80px\" onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["btn_replace2"] = knjCreateBtn($objForm, "btn_replace", "一括更新2", $extra);

        //一括更新ボタン3
        $link = REQUESTROOT."/F/KNJF010J/knjf010jindex.php?cmd=replace3&SCHREGNO=".$model->schregno;
        $extra = "style=\"width:80px\" onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["btn_replace3"] = knjCreateBtn($objForm, "btn_replace", "一括更新3", $extra);

        //一括更新ボタン4
        $link = REQUESTROOT."/F/KNJF010J/knjf010jindex.php?cmd=replace4&SCHREGNO=".$model->schregno;
        $extra = "style=\"width:80px\" onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["btn_replace4"] = knjCreateBtn($objForm, "btn_replace", "一括更新4", $extra);

        //更新ボタン
        $extra = "onmousedown=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$entMove);

        //更新後前の生徒へボタン
        $extra = "style=\"width:130px\" onmousedown=\"updateNextStudent('".$model->schregno."', 1);\" style=\"width:130px\"";
        $arg["button"]["btn_up_pre"] = KnjCreateBtn($objForm, "btn_up_pre", "更新後前の".$model->sch_label."へ", $extra.$entMove);
        //更新後次の生徒へボタン
        $extra = "style=\"width:130px\" onmousedown=\"updateNextStudent('".$model->schregno."', 0);\" style=\"width:130px\"";
        $arg["button"]["btn_up_next"] = KnjCreateBtn($objForm, "btn_up_next", "更新後次の".$model->sch_label."へ", $extra.$entMove);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "HIDDENDATE", $RowH["DATE"]);
        knjCreateHidden($objForm, "printKenkouSindanIppan", $model->Properties["printKenkouSindanIppan"]);

        $tmp = knjf010jQuery::getMedexamDetDat($model);
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            if (!$model->isFukui && !$model->isHirokoudai) {
            }
        } elseif ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") {
        } else {
            knjCreateHidden($objForm, "MANAGEMENT_REMARK", $tmp["MANAGEMENT_REMARK"]);
            knjCreateHidden($objForm, "OTHER_REMARK", $tmp["OTHER_REMARK"]);
            knjCreateHidden($objForm, "TREATCD", $tmp["TREATCD"]);
        }

        if (get_count($model->warning) == 0 && $model->cmd != "reset") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd == "reset") {
            $arg["next"] = "NextStudent(1);";
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf010jForm1.html", $arg);
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

//コンボ作成
function retMakeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
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

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function getDisabledCodes($db, $nameCd1)
{
    $result     = $db->query(knjf010jQuery::getNameSpare2($nameCd1));
    $nameCd2 = array();
    while ($row  = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $nameCd2[] = $row["NAMECD2"];
    }
    return $nameCd2;
}
