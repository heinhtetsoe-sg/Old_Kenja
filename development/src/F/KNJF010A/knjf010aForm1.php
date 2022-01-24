<?php

require_once('for_php7.php');

class knjf010aForm1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjf010aindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->schregno)) {
            $RowH = knjf010aQuery::getMedexamHdat($model);      //生徒健康診断ヘッダデータ取得
            $RowD = knjf010aQuery::getMedexamDetDat($model);   //生徒健康診断詳細データ取得
            $arg["NOT_WARNING"] = 1;
        } else {
            $RowH =& $model->field;
            $RowD =& $model->field;
        }

        $db     = Query::dbCheckOut();

        /* ヘッダ */
        if (isset($model->schregno)) {
            //生徒学籍データを取得
            $result = $db->query(knjf010aQuery::getSchregBaseMst($model));
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
            $result = $db->query(knjf010aQuery::getSchregRegdDat($model));
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
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            $arg["new"] = 1;
        } elseif ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") {
            $arg["new2"] = 1;
            $arg["Ippan".$model->Properties["printKenkouSindanIppan"]] = "1";
        } else {
            $arg["base"] = 1;
        }
        $arg["useEarInput"] = "1";
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
        if ($model->isFukui || $model->isSaga || $model->isKoma) {
            $arg["is_fukui_saga_koma"] = "1";
        } else {
            $arg["is_fukui_saga_koma"] = "";
        }
        if ($model->isFukui || $model->isKoma) {
            $arg["is_fukui_koma"] = "1"; //駒澤は福井と同様の入力
        } else {
            $arg["is_fukui_koma"] = ""; //駒澤は福井と同様の入力
        }
        if ($model->isFukui) { // 福井は視力の文字＋数字入力、貧血を使用する
            $arg["is_fukui"] = "1";
            $arg["no_fukui"] = "";
            $arg["no_fukui_ear"] = "";
            $arg["is_sitheight"] = "";
            $arg["unHyouji_SITHEIGHT"] = "";
        } else {
            $arg["is_fukui"] = "";
            if ($model->isHirokoudai) {
                $arg["is_Hirokoudai"] = "1";//広工大は視力を数値入力にする
            } else {
                if (!$model->isKoma) {
                    $arg["no_fukui"] = "1";
                }
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
            $arg["is_ear_otherType"] = "1";
            $arg["is_koma"] = "1";
            $arg["EAR_TITLE"] = "";
            $arg["is_fukui_koma"] = "1";
            $arg["is_sitheight"] = "";
            $arg["unHyouji_SITHEIGHT"] = "";
        } else {
            if ($model->Properties["printKenkouSindanIppan"] == "1" && $model->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                $arg["useEarInput"] = "";
                $arg["is_ear_otherType"] = "1";
                $arg["EAR_TITLE"] = "";
            } else {
                $arg["no_koma"] = "1";
            }
        }
        if ($model->Properties["hideSitHeight"] == "1") {
            $arg["is_sitheight"] = "";
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
        /**********************************/
        /***** 1項目目 ********************/
        /**********************************/
        //健康診断実施日付
        $RowH["DATE"] = str_replace("-", "/", $RowH["DATE"]);
        $arg["data"]["DATE"] = View::popUpCalendar2($objForm, "DATE", $RowH["DATE"], "", "", $entMove);

        //同じ年組の生徒の身長・体重取得
        $height = $weight = "";
        $sepH = $sepW = "";
        $result = $db->query(knjf010aQuery::getHeightWeightList($model));
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
        $sd = $db->getRow(knjf010aQuery::getSD($model), DB_FETCHMODE_ASSOC);

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
        $phys_avg = $db->getRow(knjf010aQuery::getHexamPhysicalAvgDat($model, $RowB["SEX"], $age), DB_FETCHMODE_ASSOC);
        knjCreateHidden($objForm, "STD_WEIGHT_KEISU_A", $phys_avg["STD_WEIGHT_KEISU_A"]);
        knjCreateHidden($objForm, "STD_WEIGHT_KEISU_B", $phys_avg["STD_WEIGHT_KEISU_B"]);
        //身長別標準体重
        if ($RowD["HEIGHT"]) {
            $arg["data"]["STD_WEIGHT"] = $phys_avg["STD_WEIGHT_KEISU_A"] * $RowD["HEIGHT"] - $phys_avg["STD_WEIGHT_KEISU_B"];
        }
        if ($model->z010name1 == "sapporo") { //札幌は赤字表示
            $arg["sapporo"] = 1;
        } else {
            unset($arg["sapporo"]);
        }

        //座高
        $extra = "onblur=\"return Num_Check(this);\"";
        $arg["data"]["SITHEIGHT"] = knjCreateTextBox($objForm, $RowD["SITHEIGHT"], "SITHEIGHT", 5, 5, $extra.$entMove);

        //福井佐賀でないなら、既存。福井佐賀でも、値があるなら既存。
        if (!($model->isFukui || $model->isSaga) || (($model->isFukui || $model->isSaga) && is_array($phys_avg) && get_count($phys_avg) > 0 && $phys_avg["STD_WEIGHT_KEISU_A"] != "" && $phys_avg["STD_WEIGHT_KEISU_B"] != "")) {
            //肥満度 = (実測体重 ― 身長別標準体重) ／ 身長別標準体重 × 100（％）
            $stdWeght = ($RowD["HEIGHT"]) ? ((int)$phys_avg["STD_WEIGHT_KEISU_A"] * (int)$RowD["HEIGHT"] - (int)$phys_avg["STD_WEIGHT_KEISU_B"]): "";
            if ($stdWeght > 0) {
                $arg["data"]["OBESITY_INDEX"] = round(((int)$RowD["WEIGHT"] - $stdWeght) / $stdWeght * 100, 1).'%';
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

        //視力・測定困難チェック時は、視力情報はグレーアウト
        $disVisionR = ($RowD["R_VISION_CANTMEASURE"] == "1") ? " disabled": "";
        $disVisionL = ($RowD["L_VISION_CANTMEASURE"] == "1") ? " disabled": "";

        //視力・右裸眼(数字)
        $extra = "onblur=\"return Num_Check(this);\" id=\"R_BAREVISION\"";
        $arg["data"]["R_BAREVISION"] = knjCreateTextBox($objForm, $RowD["R_BAREVISION"], "R_BAREVISION", 5, 5, $extra.$entMove.$disVisionR);
        //視力・右矯正(数字)
        $extra = "onblur=\"return Num_Check(this);\" id=\"R_VISION\"";
        $arg["data"]["R_VISION"] = knjCreateTextBox($objForm, $RowD["R_VISION"], "R_VISION", 5, 5, $extra.$entMove.$disVisionR);
        //視力・左裸眼(数字)
        $extra = "onblur=\"return Num_Check(this);\" id=\"L_BAREVISION\"";
        $arg["data"]["L_BAREVISION"] = knjCreateTextBox($objForm, $RowD["L_BAREVISION"], "L_BAREVISION", 5, 5, $extra.$entMove.$disVisionL);
        //視力・左矯正(数字)
        $extra = "onblur=\"return Num_Check(this);\" id=\"L_VISION\"";
        $arg["data"]["L_VISION"] = knjCreateTextBox($objForm, $RowD["L_VISION"], "L_VISION", 5, 5, $extra.$entMove.$disVisionL);

        //視力
        $query = knjf010aQuery::getLRBarevisionMark($model);
        $extra = "";
        if ($arg["is_mie"] == "1") {
            //視力・右裸眼(文字)
            $extra = "onblur=\"return Mark_Check(this);\"";
            $arg["data"]["R_BAREVISION_MARK"] = knjCreateTextBox($objForm, $RowD["R_BAREVISION_MARK"], "R_BAREVISION_MARK", 1, 1, $extra.$entMove);
            //視力・右矯正(文字)
            $extra = "onblur=\"return Mark_Check(this);\"";
            $arg["data"]["R_VISION_MARK"] = knjCreateTextBox($objForm, $RowD["R_VISION_MARK"], "R_VISION_MARK", 1, 1, $extra.$entMove);
            //視力・左矯正(文字)
            $extra = "onblur=\"return Mark_Check(this);\"";
            $arg["data"]["L_BAREVISION_MARK"] = knjCreateTextBox($objForm, $RowD["L_BAREVISION_MARK"], "L_BAREVISION_MARK", 1, 1, $extra.$entMove);
            //視力・左裸眼(文字)
            $extra = "onblur=\"return Mark_Check(this);\"";
            $arg["data"]["L_VISION_MARK"] = knjCreateTextBox($objForm, $RowD["L_VISION_MARK"], "L_VISION_MARK", 1, 1, $extra.$entMove);
        } else {
            //視力・右裸眼(文字)
            makeCmb($objForm, $arg, $db, $query, "R_BAREVISION_MARK", $RowD["R_BAREVISION_MARK"], $extra.$entMove.$disVisionR, 1, "BLANK");
            //視力・右矯正(文字)
            makeCmb($objForm, $arg, $db, $query, "R_VISION_MARK", $RowD["R_VISION_MARK"], $extra.$entMove.$disVisionR, 1, "BLANK");
            //視力・左矯正(文字)
            makeCmb($objForm, $arg, $db, $query, "L_BAREVISION_MARK", $RowD["L_BAREVISION_MARK"], $extra.$entMove.$disVisionL, 1, "BLANK");
            //視力・左裸眼(文字)
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

        //視力、測定困難(右)
        if ($model->field["R_VISION_CANTMEASURE"] == "1") {
            $checkV_R = "checked";
        } else {
            $checkV_R = ($RowD["R_VISION_CANTMEASURE"] == "1") ? " checked" : "";
        }
        $extra = "id=\"R_VISION_CANTMEASURE\" onClick=\"disVision(this, 'right');\"";
        $arg["data"]["R_VISION_CANTMEASURE"] = knjCreateCheckBox($objForm, "R_VISION_CANTMEASURE", "1", $extra.$checkV_R);

        //視力、測定困難(左)
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
        $result  = $db->query(knjf010aQuery::getLREar($model, ""));
        $opt     = array();
        $opt[]   = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        //聴力・右DB
        $extra = "onblur=\"return Num_Check(this);\"";
        if ($arg["is_ear_otherType"] == "1") {
            $query = knjf010aQuery::getNameMst($model, "F010");
            $extra = "".$entMove.$disEarR;
            $arg["data"]["R_EAR_DB_1000"] = "1000Hz：&nbsp;".retMakeCmb($objForm, $arg, $db, $query, "R_EAR_DB_1000", $RowD["R_EAR_DB_1000"], $extra, 1, "blank");
        } else {
            $arg["data"]["R_EAR_DB"] = knjCreateTextBox($objForm, $RowD["R_EAR_DB"], "R_EAR_DB", 4, 3, $extra.$entMove.$disEarR);
            $extra = "";
            $arg["data"]["R_EAR"] = knjCreateCombo($objForm, "R_EAR", $RowD["R_EAR"], $opt, $extra.$entMove.$disEarR, 1);
        }
        //聴力・左DB
        $extra = "onblur=\"return Num_Check(this);\"";
        if ($arg["is_ear_otherType"] == "1") {
            $query = knjf010aQuery::getNameMst($model, "F010");
            $extra = "".$entMove.$disEarL;
            $arg["data"]["L_EAR_DB_1000"] = "1000Hz：&nbsp;".retMakeCmb($objForm, $arg, $db, $query, "L_EAR_DB_1000", $RowD["L_EAR_DB_1000"], $extra, 1, "blank");
        } else {
            $arg["data"]["L_EAR_DB"] = knjCreateTextBox($objForm, $RowD["L_EAR_DB"], "L_EAR_DB", 4, 3, $extra.$entMove.$disEarL);
            //聴力・左状態コンボ
            $extra = "";
            $arg["data"]["L_EAR"] = knjCreateCombo($objForm, "L_EAR", $RowD["L_EAR"], $opt, $extra.$entMove.$disEarL, 1);
        }
        if ($model->Properties["useEar4000Hz"] == "1") {
            if ($arg["is_ear_otherType"] == "1") {
                $query = knjf010aQuery::getNameMst($model, "F010");
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
        $optnull = array("label" => "","value" => "");   //初期値：空白項目
        $result  = $db->query(knjf010aQuery::getLREar($model, ""));
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
        //尿・２次蛋白コンボ
        $opt    = array();
        $opt[]  = $optnull;
        $result = $db->query(knjf010aQuery::getUric($model, "F020"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $arg["data"]["ALBUMINURIA1CD"] = knjCreateCombo($objForm, "ALBUMINURIA1CD", $RowD["ALBUMINURIA1CD"], $opt, $extra.$entMove, 1);
        $arg["data"]["ALBUMINURIA2CD"] = knjCreateCombo($objForm, "ALBUMINURIA2CD", $RowD["ALBUMINURIA2CD"], $opt, $extra.$entMove, 1);
        //尿・１次糖コンボ
        //尿・２次糖コンボ
        $opt    = array();
        $opt[]  = $optnull;
        $result = $db->query(knjf010aQuery::getUric($model, "F019"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $arg["data"]["URICSUGAR1CD"] = knjCreateCombo($objForm, "URICSUGAR1CD", $RowD["URICSUGAR1CD"], $opt, $extra.$entMove, 1);
        $arg["data"]["URICSUGAR2CD"] = knjCreateCombo($objForm, "URICSUGAR2CD", $RowD["URICSUGAR2CD"], $opt, $extra.$entMove, 1);
        //尿・１次潜血コンボ
        //尿・２次潜血コンボ
        $opt    = array();
        $opt[]  = $optnull;
        $result = $db->query(knjf010aQuery::getUric($model, "F018"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $arg["data"]["URICBLEED1CD"] = knjCreateCombo($objForm, "URICBLEED1CD", $RowD["URICBLEED1CD"], $opt, $extra.$entMove, 1);
        $arg["data"]["URICBLEED2CD"] = knjCreateCombo($objForm, "URICBLEED2CD", $RowD["URICBLEED2CD"], $opt, $extra.$entMove, 1);
        if ($model->Properties["printKenkouSindanIppan"] == "1" && $model->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
            $arg["DISP_PH"] = 1;
            $arg["DISPORDERCHG_OTHERDISEASE"] = 1;
            $arg["rowspan2"]++;
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data"]["URICPH1"] = knjCreateTextBox($objForm, $RowD["URICPH1"], "URICPH1", 4, 4, $extra.$entMove);
            $arg["data"]["URICPH2"] = knjCreateTextBox($objForm, $RowD["URICPH2"], "URICPH2", 4, 4, $extra.$entMove);
        } else {
            $arg["NO_DISPORDERCHG_OTHERDISEASE"] = 1;
        }
        //尿・その他の検査
        if ($model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "";
            $arg["data"]["URICOTHERTEST"] = knjCreateTextBox($objForm, $RowD["URICOTHERTEST"], "URICOTHERTEST", 40, 20, $extra.$entMove);
        } else {
            $result     = $db->query(knjf010aQuery::getUriCothertest($model));
            $opt        = array();
            $opt[]      = $optnull;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label" => $row["LABEL"],
                               "value" => $row["VALUE"]);
            }
            $result->free();
            $extra = "style=\"width:190px;\"";
            $arg["data"]["URICOTHERTESTCD"] = knjCreateCombo($objForm, "URICOTHERTESTCD", $RowD["URICOTHERTESTCD"], $opt, $extra.$entMove, 1);
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
        $extra = "style=\"width:170px;\"";
        $arg["data"]["URI_ADVISECD"] = knjCreateCombo($objForm, "URI_ADVISECD", $RowD["URI_ADVISECD"], $opt, $extra.$entMove, 1);
        //栄養状態コンボ
        $nutrInfo = $sep = '';
        $result     = $db->query(knjf010aQuery::getNutrition($model, ""));
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
        $result->free();
        $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].NUTRITIONCD_REMARK)\"";
        $arg["data"]["NUTRITIONCD"] = knjCreateCombo($objForm, "NUTRITIONCD", $RowD["NUTRITIONCD"], $opt, $extra.$entMove, 1);

        //栄養状態テキスト
        if ($RowD["NUTRITIONCD"] <= "1") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["NUTRITIONCD_REMARK"] = knjCreateTextBox($objForm, $RowD["NUTRITIONCD_REMARK"], "NUTRITIONCD_REMARK", 40, 20, $extra.$entMove);

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
            $extra = "style=\"width:170px;\"";
        } else {
            $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].EYE_TEST_RESULT)\"";
        }
        $arg["data"]["EYEDISEASECD"] = knjCreateCombo($objForm, "EYEDISEASECD", $RowD["EYEDISEASECD"], $opt, $extra.$entMove, 1);
        $arg["data"]["EYEDISEASECD2"] = knjCreateCombo($objForm, "EYEDISEASECD2", $RowD["EYEDISEASECD2"], $opt, $extra.$entMove, 1);
        $arg["data"]["EYEDISEASECD3"] = knjCreateCombo($objForm, "EYEDISEASECD3", $RowD["EYEDISEASECD3"], $opt, $extra.$entMove, 1);
        $arg["data"]["EYEDISEASECD4"] = knjCreateCombo($objForm, "EYEDISEASECD4", $RowD["EYEDISEASECD4"], $opt, $extra.$entMove, 1);
        /*****************************/
        if ((int)$RowD["EYEDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["EYE_TEST_RESULT"] = knjCreateTextBox($objForm, $RowD["EYE_TEST_RESULT"], "EYE_TEST_RESULT", 40, 20, $extra.$entMove);
        $arg["data"]["EYE_TEST_RESULT2"] = knjCreateTextBox($objForm, $RowD["EYE_TEST_RESULT2"], "EYE_TEST_RESULT2", 40, 20, $extra.$entMove);
        $arg["data"]["EYE_TEST_RESULT3"] = knjCreateTextBox($objForm, $RowD["EYE_TEST_RESULT3"], "EYE_TEST_RESULT3", 40, 20, $extra.$entMove);
        /*****************************/
        //色覚異常コンボ
        $opt        = array();
        $opt[]      = $optnull;
        $result     = $db->query(knjf010aQuery::getEyedisease5($model, "F051", ""));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $extra = "style=\"width:170px;\"";
        $arg["data"]["EYEDISEASECD5"] = knjCreateCombo($objForm, "EYEDISEASECD5", $RowD["EYEDISEASECD5"], $opt, $extra.$entMove, 1);

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
            $extra = "style=\"width:170px;\"";
        } else {
            $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].SPINERIBCD_REMARK)\"";
        }
        $arg["data"]["SPINERIBCD"] = knjCreateCombo($objForm, "SPINERIBCD", $RowD["SPINERIBCD"], $opt, $extra.$entMove, 1);

        /*****************************/
        //脊柱・胸部
        if ((int)$RowD["SPINERIBCD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["SPINERIBCD_REMARK"] = knjCreateTextBox($objForm, $RowD["SPINERIBCD_REMARK"], "SPINERIBCD_REMARK", 40, 20, $extra.$entMove);
        /*****************************/

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
            $extra = "style=\"width:170px;\"";
        } else {
            $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].NOSEDISEASECD_REMARK)\"";
        }
        $arg["data"]["NOSEDISEASECD"] = knjCreateCombo($objForm, "NOSEDISEASECD", $RowD["NOSEDISEASECD"], $opt, $extra.$entMove, 1);
        $arg["data"]["NOSEDISEASECD2"] = knjCreateCombo($objForm, "NOSEDISEASECD2", $RowD["NOSEDISEASECD2"], $opt, $extra.$entMove, 1);
        $arg["data"]["NOSEDISEASECD3"] = knjCreateCombo($objForm, "NOSEDISEASECD3", $RowD["NOSEDISEASECD3"], $opt, $extra.$entMove, 1);
        $arg["data"]["NOSEDISEASECD4"] = knjCreateCombo($objForm, "NOSEDISEASECD4", $RowD["NOSEDISEASECD4"], $opt, $extra.$entMove, 1);

        /*****************************/
        //耳疾患コンボ
        $opt        = array();
        $opt[]      = $optnull;
        $result     = $db->query(knjf010aQuery::getNosedisease567($model, "F061", ""));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].NOSEDISEASECD_REMARK1)\"";
        } else {
            $extra = "style=\"width:170px;\"";
        }
        $arg["data"]["NOSEDISEASECD5"] = knjCreateCombo($objForm, "NOSEDISEASECD5", $RowD["NOSEDISEASECD5"], $opt, $extra.$entMove, 1);
        //鼻・副鼻腔疾患コンボ
        $opt        = array();
        $opt[]      = $optnull;
        $result     = $db->query(knjf010aQuery::getNosedisease567($model, "F062", ""));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].NOSEDISEASECD_REMARK2)\"";
        } else {
            $extra = "style=\"width:170px;\"";
        }
        $arg["data"]["NOSEDISEASECD6"] = knjCreateCombo($objForm, "NOSEDISEASECD6", $RowD["NOSEDISEASECD6"], $opt, $extra.$entMove, 1);
        //口腔咽頭疾患・異常コンボ
        $opt        = array();
        $opt[]      = $optnull;
        $result     = $db->query(knjf010aQuery::getNosedisease567($model, "F063", ""));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].NOSEDISEASECD_REMARK3)\"";
        } else {
            $extra = "style=\"width:170px;\"";
        }
        $arg["data"]["NOSEDISEASECD7"] = knjCreateCombo($objForm, "NOSEDISEASECD7", $RowD["NOSEDISEASECD7"], $opt, $extra.$entMove, 1);

        /*****************************/
        //耳鼻咽頭疾患テキスト
        if ((int)$RowD["NOSEDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["NOSEDISEASECD_REMARK"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK"], "NOSEDISEASECD_REMARK", 40, 20, $extra.$entMove);
        //耳疾患テキスト
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            if ((int)$RowD["NOSEDISEASECD5"] < 2) {
                $extra = "disabled";
            } else {
                $extra = "";
            }
        }
        $arg["data"]["NOSEDISEASECD_REMARK1"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK1"], "NOSEDISEASECD_REMARK1", 40, 20, $extra.$entMove);
        //鼻・副鼻腔疾患テキスト
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            if ((int)$RowD["NOSEDISEASECD6"] < 2) {
                $extra = "disabled";
            } else {
                $extra = "";
            }
        }
        $arg["data"]["NOSEDISEASECD_REMARK2"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK2"], "NOSEDISEASECD_REMARK2", 40, 20, $extra.$entMove);
        //口腔咽頭疾患・異常テキスト
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            if ((int)$RowD["NOSEDISEASECD7"] < 2) {
                $extra = "disabled";
            } else {
                $extra = "";
            }
        }
        $arg["data"]["NOSEDISEASECD_REMARK3"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK3"], "NOSEDISEASECD_REMARK3", 40, 20, $extra.$entMove);
        /*****************************/

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
        $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].SKINDISEASECD_REMARK)\"";
        $arg["data"]["SKINDISEASECD"] = knjCreateCombo($objForm, "SKINDISEASECD", $RowD["SKINDISEASECD"], $opt, $extra.$entMove, 1);
        
        //皮膚疾患テキスト
        if ((int)$RowD["SKINDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["SKINDISEASECD_REMARK"] = knjCreateTextBox($objForm, $RowD["SKINDISEASECD_REMARK"], "SKINDISEASECD_REMARK", 40, 20, $extra.$entMove);
        
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
            $extra = "style=\"width:170px;\"";
        } else {
            $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].HEART_MEDEXAM_REMARK)\"";
        }
        $arg["data"]["HEART_MEDEXAM"] = knjCreateCombo($objForm, "HEART_MEDEXAM", $RowD["HEART_MEDEXAM"], $opt, $extra.$entMove, 1);

        /*****************************/
        //心臓・臨床医学的検査テキスト
        if ((int)$RowD["HEART_MEDEXAM"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["HEART_MEDEXAM_REMARK"] = knjCreateTextBox($objForm, $RowD["HEART_MEDEXAM_REMARK"], "HEART_MEDEXAM_REMARK", 80, 40, $extra.$entMove);

        if ($model->isKoma) {
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data"]["HEART_GRAPH_NO"] = knjCreateTextBox($objForm, $RowD["HEART_GRAPH_NO"], "HEART_GRAPH_NO", 12, 12, $extra.$entMove);
        }
        /*****************************/

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
            $extra = "style=\"width:170px;\"";
        } else {
            $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].HEARTDISEASECD_REMARK)\"";
        }
        $arg["data"]["HEARTDISEASECD"] = knjCreateCombo($objForm, "HEARTDISEASECD", $RowD["HEARTDISEASECD"], $opt, $extra.$entMove, 1);

        /*****************************/
        //心臓・疾病及び異常テキスト
        if ((int)$RowD["HEARTDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["HEARTDISEASECD_REMARK"] = knjCreateTextBox($objForm, $RowD["HEARTDISEASECD_REMARK"], "HEARTDISEASECD_REMARK", 40, 20, $extra.$entMove);
        /*****************************/

        //心臓・管理区分
        $optnull = array("label" => "","value" => "");   //初期値：空白項目
        $result  = $db->query(knjf010aQuery::getManagementDiv($model));
        $opt     = array();
        $opt[]   = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $extra = "";
        $arg["data"]["MANAGEMENT_DIV"] = knjCreateCombo($objForm, "MANAGEMENT_DIV", $RowD["MANAGEMENT_DIV"], $opt, $extra.$entMove, 1);

        //心臓・管理区分テキスト
        $arg["data"]["MANAGEMENT_REMARK"] = knjCreateTextBox($objForm, $RowD["MANAGEMENT_REMARK"], "MANAGEMENT_REMARK", 40, 20, $entMove);

        /**********************************/
        /***** 3項目目 ********************/
        /**********************************/
        //結核・撮影日付
        $RowD["TB_FILMDATE"] = str_replace("-", "/", $RowD["TB_FILMDATE"]);
        $arg["data"]["TB_FILMDATE"] = View::popUpCalendar2($objForm, "TB_FILMDATE", $RowD["TB_FILMDATE"], "", "", $entMove);

        //結核・フィルム番号
        $arg["data"]["TB_FILMNO"] = knjCreateTextBox($objForm, $RowD["TB_FILMNO"], "TB_FILMNO", $model->tb_filmnoFieldSize, $model->tb_filmnoFieldSize, $entMove);
        $arg["data"]["TB_FILMNO_SIZE"] = $model->tb_filmnoFieldSize;

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
        $extra = "style=\"width:170px;\"";
        $arg["data"]["TB_REMARKCD"] = knjCreateCombo($objForm, "TB_REMARKCD", $RowD["TB_REMARKCD"], $opt, $extra.$entMove, 1);

        //結核検査(X線)
        $extra = "";
        $arg["data"]["TB_X_RAY"] = knjCreateTextBox($objForm, $RowD["TB_X_RAY"], "TB_X_RAY", 40, 20, $extra.$entMove);

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
        $extra = "style=\"width:100px;\"";
        $arg["data"]["TB_OTHERTESTCD"] = knjCreateCombo($objForm, "TB_OTHERTESTCD", $RowD["TB_OTHERTESTCD"], $opt, $extra.$entMove, 1);
        $arg["data"]["TB_OTHERTEST_REMARK1"] = knjCreateTextBox($objForm, $RowD["TB_OTHERTEST_REMARK1"], "TB_OTHERTEST_REMARK1", 40, 20, $extra.$entMove);

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
            $extra = "style=\"width:100px;\" onChange=\"syokenNyuryoku(this, document.forms[0].TB_NAME_REMARK1)\"";
        } else {
            $extra = "style=\"width:100px;\"";
        }
        $arg["data"]["TB_NAMECD"] = knjCreateCombo($objForm, "TB_NAMECD", $RowD["TB_NAMECD"], $opt, $extra.$entMove, 1);
        if ($arg["is_not_mie"] == "1") {
            //結核・病名テキスト
            if ((int)$RowD["TB_NAMECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
                $extra = "disabled";
            } else {
                $extra = "";
            }
            $arg["data"]["TB_NAME_REMARK1"] = knjCreateTextBox($objForm, $RowD["TB_NAME_REMARK1"], "TB_NAME_REMARK1", 40, 20, $extra.$entMove);
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
        $extra = "style=\"width:150px;\"";
        $arg["data"]["TB_ADVISECD"] = knjCreateCombo($objForm, "TB_ADVISECD", $RowD["TB_ADVISECD"], $opt, $extra.$entMove, 1);
        $arg["data"]["TB_ADVISE_REMARK1"] = knjCreateTextBox($objForm, $RowD["TB_ADVISE_REMARK1"], "TB_ADVISE_REMARK1", 40, 20, $extra.$entMove);

        //寄生虫卵コンボ
        $query = knjf010aQuery::getNameMst($model, "F023");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "PARASITE", $RowD["PARASITE"], $extra.$entMove, 1, "blank");

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
            $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].OTHER_REMARK2)\"";
        } else {
            $extra = "style=\"width:170px;\"";
        }
        $arg["data"]["OTHERDISEASECD"] = knjCreateCombo($objForm, "OTHERDISEASECD", $RowD["OTHERDISEASECD"], $opt, $extra.$entMove, 1);

        //その他疾病及び異常:指導区分コンボ
        $result     = $db->query(knjf010aQuery::getOtherAdvisecd($model));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "style=\"width:170px;\"";
        $arg["data"]["OTHER_ADVISECD"] = knjCreateCombo($objForm, "OTHER_ADVISECD", $RowD["OTHER_ADVISECD"], $opt, $extra.$entMove, 1);

        //その他疾病及び異常:所見1
        $arg["data"]["OTHER_REMARK"] = knjCreateTextBox($objForm, $RowD["OTHER_REMARK"], "OTHER_REMARK", 40, 20, $entMove);
        //その他疾病及び異常:所見2
        if ((int)$RowD["OTHERDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        if ($model->Properties["useSpecial_Support_School"] == "1") { //特別支援学校
            $model->maxOtherRemark2Byte = '300';
            $otherSize = '120';
            $arg["data"]["OTHER_REMARK2_MOJI"] = '100';
        } else {
            $model->maxOtherRemark2Byte = '60';
            $otherSize = '40';
            $arg["data"]["OTHER_REMARK2_MOJI"] = '20';
        }
        $arg["data"]["OTHER_REMARK2"] = knjCreateTextBox($objForm, $RowD["OTHER_REMARK2"], "OTHER_REMARK2", $otherSize, $model->maxOtherRemark2Byte, $extra.$entMove);
        //その他疾病及び異常:所見3
        $arg["data"]["OTHER_REMARK3"] = knjCreateTextBox($objForm, $RowD["OTHER_REMARK3"], "OTHER_REMARK3", 40, 20, $entMove);

        if ($arg["is_mie"] == "1") {
            //学校医（内科検診）
            $result     = $db->query(knjf010aQuery::getDocCd($model));
            $opt        = array();
            $opt[]      = $optnull;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $namecd = substr($row["NAMECD2"], 0, 2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            $extra = "style=\"width:170px;\"";
            $arg["data"]["DOC_CD"] = knjCreateCombo($objForm, "DOC_CD", $RowD["DOC_CD"], $opt, $extra.$entMove, 1);
        }

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

        if ($model->isMie) {
            //学校医コンボボックス(三重のみ)
            $arg["data"]["DOC_NAME"] = knjCreateTextBox($objForm, $RowD["DOC_NAME"], "DOC_NAME", 20, 10, $entMove);
        }

        //事後措置コンボ
        $result     = $db->query(knjf010aQuery::getTreat($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        if ($model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].TREAT_REMARK1)\"";
        } else {
            $extra = "style=\"width:170px;\"";
        }
        $arg["data"]["TREATCD"] = knjCreateCombo($objForm, "TREATCD", $RowD["TREATCD"], $opt, $extra.$entMove, 1);

        //事後措置2コンボ
        $query = knjf010aQuery::getTreat2($model);
        $extra = "style=\"width:170px;\"";
        makeCmb($objForm, $arg, $db, $query, "TREATCD2", $RowD["TREATCD2"], $extra.$entMove, 1, "BLANK");

        //事後措置:所見1
        if ((int)$RowD["TREATCD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["TREAT_REMARK1"] = knjCreateTextBox($objForm, $RowD["TREAT_REMARK1"], "TREAT_REMARK1", 40, 20, $extra.$entMove);
        //事後措置:所見2
        $arg["data"]["TREAT_REMARK2"] = knjCreateTextBox($objForm, $RowD["TREAT_REMARK2"], "TREAT_REMARK2", 40, 20, $entMove);
        //事後措置:所見3
        $arg["data"]["TREAT_REMARK3"] = knjCreateTextBox($objForm, $RowD["TREAT_REMARK3"], "TREAT_REMARK3", 40, 20, $entMove);

        //備考
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
        $result     = $db->query(knjf010aQuery::getMedicalHist($model));
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
        $result     = $db->query(knjf010aQuery::getGuideDiv($model));
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
        $result     = $db->query(knjf010aQuery::getJoiningSportsClub($model));
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
        $link = REQUESTROOT."/F/KNJF010A/knjf010aindex.php?cmd=replace1&SCHREGNO=".$model->schregno;
        $extra = "style=\"width:80px\" onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["btn_replace1"] = knjCreateBtn($objForm, "btn_replace", "一括更新1", $extra);

        //一括更新ボタン2
        $link = REQUESTROOT."/F/KNJF010A/knjf010aindex.php?cmd=replace2&SCHREGNO=".$model->schregno;
        $extra = "style=\"width:80px\" onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["btn_replace2"] = knjCreateBtn($objForm, "btn_replace", "一括更新2", $extra);

        //一括更新ボタン3
        $link = REQUESTROOT."/F/KNJF010A/knjf010aindex.php?cmd=replace3&SCHREGNO=".$model->schregno;
        $extra = "style=\"width:80px\" onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["btn_replace3"] = knjCreateBtn($objForm, "btn_replace", "一括更新3", $extra);

        //一括更新ボタン4
        $link = REQUESTROOT."/F/KNJF010A/knjf010aindex.php?cmd=replace4&SCHREGNO=".$model->schregno;
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

        $tmp = knjf010aQuery::getMedexamDetDat($model);
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            if (!$model->isFukui && !$model->isHirokoudai) {
                knjCreateHidden($objForm, "R_BAREVISION", $tmp["R_BAREVISION"]);
                knjCreateHidden($objForm, "R_VISION", $tmp["R_VISION"]);
                knjCreateHidden($objForm, "L_BAREVISION", $tmp["L_BAREVISION"]);
                knjCreateHidden($objForm, "L_VISION", $tmp["L_VISION"]);
            }
        } elseif ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") {
            knjCreateHidden($objForm, "R_BAREVISION", $tmp["R_BAREVISION"]);
            knjCreateHidden($objForm, "R_VISION", $tmp["R_VISION"]);
            knjCreateHidden($objForm, "L_BAREVISION", $tmp["L_BAREVISION"]);
            knjCreateHidden($objForm, "L_VISION", $tmp["L_VISION"]);
            knjCreateHidden($objForm, "MANAGEMENT_DIV", $tmp["MANAGEMENT_DIV"]);
            knjCreateHidden($objForm, "MANAGEMENT_REMARK", $tmp["MANAGEMENT_REMARK"]);
            knjCreateHidden($objForm, "URI_ADVISECD", $tmp["URI_ADVISECD"]);
            knjCreateHidden($objForm, "OTHER_ADVISECD", $tmp["OTHER_ADVISECD"]);
            knjCreateHidden($objForm, "DOC_REMARK", $tmp["DOC_REMARK"]);
        } else {
            knjCreateHidden($objForm, "MANAGEMENT_REMARK", $tmp["MANAGEMENT_REMARK"]);
            knjCreateHidden($objForm, "OTHER_REMARK", $tmp["OTHER_REMARK"]);
            knjCreateHidden($objForm, "TREATCD", $tmp["TREATCD"]);
        }

        if (get_count($model->warning)== 0 && $model->cmd !="reset") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd =="reset") {
            $arg["next"] = "NextStudent(1);";
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf010aForm1.html", $arg);
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
