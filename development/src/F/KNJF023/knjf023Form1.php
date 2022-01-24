<?php

require_once('for_php7.php');

class knjf023Form1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjf023index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        //年組コンボ
        $query = knjf023Query::getHrClass($model);
        $extra = "onchange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->grade_hr_class, "GRADE_HR_CLASS", $extra, 1, "BLANK");

        //画面切替コンボ
        $opt = array();
        $opt[] = array("label" => "歯・口腔",  "value" => "1");
        $opt[] = array("label" => "歯式",      "value" => "2");
        if ($model->screen == "") {
            $model->screen = 1;
        }
        $extra = "onchange=\"return btn_submit('edit');\"";
        $arg["SCREEN"] = knjCreateCombo($objForm, "SCREEN", $model->screen, $opt, $extra, 1);

        if ($model->screen == 1) {
            $arg["tooth"] = 1;
        }
        if ($model->screen == 2) {
            $arg["shisiki"] = 1;
        }

        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $arg["Ippan2"] = 1;
        } else {
            $arg["Ippan2Igai"] = 1;
        }

        if ($model->z010 == "miyagiken") {
            $arg["is_miyagiken"]  = "1";
            $arg["not_miyagiken"] = "";
        } else {
            $arg["is_miyagiken"]  = "";
            $arg["not_miyagiken"] = "1";
        }

        if ($model->z010 == "mieken") {
            $arg["is_mieken"]  = "1";
            $arg["is_not_mie"] = "";
        } else {
            $arg["is_mieken"]  = "";
            $arg["is_not_mie"] = "1";
        }

        if ($model->is_f020_otherdisese_hyouji) {
            $arg["F020_OTHERDISESE_HYOUJI"] = "1";
        } else {
            $arg["F020_OTHERDISESE_HYOUJI"] = "";
        }
        if ($model->z010 != "miyagiken" && $model->is_f020_otherdisese_hyouji2) {
            $arg["F020_OTHERDISESE_HYOUJI2"] = "1";
        } else {
            $arg["F020_OTHERDISESE_HYOUJI2"] = "";
        }
        if ($model->is_f020_dentistremark_hyouji) {
            $arg["F020_DENTISTREMARK_HYOUJI"] = "1";
        } else {
            $arg["F020_DENTISTREMARK_HYOUJI"] = "";
        }

        //名称マスタより取得するコード一覧
        $nameM = array();
        $nameM["F540"] = "";
        $nameM["F541"] = "";

        $sep = "";
        $setInNamecd1 = "'";
        foreach ($nameM as $namecd1 => $flg) {
            $setInNamecd1 .= $sep.$namecd1;
            $sep = "', '";
        }
        $setInNamecd1 .= "'";
        $query = knjf023Query::getNameMstSpare2($model, $setInNamecd1);
        $nameSpare2 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $nameSpare2[$row["NAMECD1"]][] = $row["NAMECD2"];
        }
        $result->free();

        //サブタイトル
        $arg["SUBTITLE"] = $opt[$model->screen-1]["label"];

        //表幅
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $arg["WIDTH"] = ($model->screen == 1) ? "3600px" : "4470px";
        } else {
            $arg["WIDTH"] = ($model->screen == 1) ? "3650px" : "4200px";
        }

        //高さ
        $arg["heightHeader"] = ($model->screen == 1) ? "90px" : "80px";
        $arg["heightMeisai"] = ($model->screen == 1) ? "120px" : "70px";

        //歯列・咬合の幅
        $jaws_jointcd_width = "300";
        if ($model->Properties["printKenkouSindanIppan"] == "1" && ($model->z010 === "mieken" || $model->KNJF030D || $model->Properties["KenkouSindan_Ippan_Pattern"] == "1")) {
            $jaws_jointcd_width = "340";
        }
        //歯列・咬合が分かれるときは、その分の幅を加味する。
        if ($model->Properties["printKenkouSindanIppan"] == "1" && $model->Properties["KenkouSindan_Ippan_Pattern"] == "1" && $model->screen == "1") {
            $arg["WIDTH"] += $jaws_jointcd_width;
        }
        $arg["JAWS_JOINTCD_WIDTH"] = $jaws_jointcd_width;

        //データ取得
        $schList = array();
        if (!isset($model->warning) && $model->grade_hr_class != "") {
            //生徒一覧
            $query = knjf023Query::getSchToothInfo($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $schList[] = $row;
            }
            $result->free();
        }

        //更新時のチェックでエラーの場合、画面情報をセット
        if (isset($model->warning)) {
            //生徒一覧
            for ($counter = 0; $counter < $model->data_cnt; $counter++) {
                $Row = array();
                foreach ($model->fields as $key => $val) {
                    $Row[$key] = $val[$counter];
                }
                $schList[] = $Row;
            }
        }

        //生徒一覧の表示件数
        knjCreateHidden($objForm, "DATA_CNT", get_count($schList));

        //歯式項目名
        $cnt = 1;
        $cnt_shisiki = get_count($model->shisiki);
        foreach ($model->shisiki as $key => $val) {
            $label["NAME"]  = str_replace("-", "<br>", $val[0]);
            $label["WIDTH"] = ($cnt == $cnt_shisiki) ? "*" : "70";

            $arg["label"][] = $label;
            $cnt++;
        }

        //名称マスタより取得するコード一覧
        if ($model->screen == "1") {
            $nameM = array("F510","F511","F512","F520","F513","F521","F530","F531","F540","F541");
        } else {
            $nameM = array("F550");
        }

        foreach ($nameM as $namecd1) {
            //名称マスタよりデータ取得・格納
            $query = knjf023Query::getNameMst($model, $namecd1);
            $optname = "opt".$namecd1;
            $$optname = makeArrayReturn($db, $query);
        }

        if ($model->Properties["printKenkouSindanIppan"] == "1" && $model->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
            $arg["CHG_DISPORDER"] = 1;
            $arg["SIKOU_STRTYPE2"] = 1;
        } else {
            $arg["NO_CHG_DISPORDER"] = 1;
            $arg["SIKOU_STRTYPE1"] = 1;
        }
        //生徒一覧を表示
        foreach ($schList as $counter => $Row) {
            //出席番号
            $setData["ATTENDNO"] = $Row["ATTENDNO"];
            knjCreateHidden($objForm, "ATTENDNO"."-".$counter, $Row["ATTENDNO"]);

            //氏名
            $setData["NAME_SHOW"] = $Row["NAME_SHOW"];
            knjCreateHidden($objForm, "NAME_SHOW"."-".$counter, $Row["NAME_SHOW"]);

            //健康診断実施日
            $Row["TOOTH_DATE"] = str_replace("-", "/", $Row["TOOTH_DATE"]);
            $setData["TOOTH_DATE"] = View::popUpCalendar($objForm, "TOOTH_DATE"."-".$counter, $Row["TOOTH_DATE"]);

            if ($model->screen == "1") {
                $kougouOpt = array();
                if ($model->Properties["printKenkouSindanIppan"] == "1" && $model->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                    $kougouOpt = $optF510;
                } else {
                    $kougouOpt = $optF512;
                }
                //歯列・咬合コンボ
                $jaws_jointcd_label = $jaws_jointcd3 = "";
                if ($model->Properties["printKenkouSindanIppan"] == "1" && ($model->z010 === "mieken" || $model->KNJF030D || $model->Properties["KenkouSindan_Ippan_Pattern"] == "1") ) {
                    //咬合コンボ
                    $extra = "";
                    if ($model->Properties["KenkouSindan_Ippan_Pattern"] != "1") {
                        $jaws_jointcd_label = "歯列：";
                        $jaws_jointcd3 = "<br>&nbsp;咬合：";
                    }
                    $jaws_jointcd3 = $jaws_jointcd3.makeCmbReturn($objForm, $kougouOpt, $Row["JAWS_JOINTCD3"], "JAWS_JOINTCD3"."-".$counter, $extra, 1, "BLANK");
                }
                $extra = "";
                $jaws_jointcd  = $jaws_jointcd_label.makeCmbReturn($objForm, $optF510, $Row["JAWS_JOINTCD"], "JAWS_JOINTCD"."-".$counter, $extra, 1, "BLANK");
                if ($model->Properties["printKenkouSindanIppan"] == "1" && $model->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                    $setData["JAWS_JOINTCD3"] = $jaws_jointcd3;
                } else {
                    $jaws_jointcd .= $jaws_jointcd3;
                }
                $setData["JAWS_JOINTCD"] = $jaws_jointcd;

                //顎関節コンボ
                $extra = "";
                $setData["JAWS_JOINTCD2"] = makeCmbReturn($objForm, $optF511, $Row["JAWS_JOINTCD2"], "JAWS_JOINTCD2"."-".$counter, $extra, 1, "BLANK");

                //歯垢の状態コンボ
                $extra = "";
                $setData["PLAQUECD"] = makeCmbReturn($objForm, $optF520, $Row["PLAQUECD"], "PLAQUECD"."-".$counter, $extra, 1, "BLANK");

                //歯肉の状態コンボ
                $extra = "";
                $setData["GUMCD"] = makeCmbReturn($objForm, $optF513, $Row["GUMCD"], "GUMCD"."-".$counter, $extra, 1, "BLANK");

                //歯石沈着コンボ
                $extra = "";
                $setData["CALCULUS"] = makeCmbReturn($objForm, $optF521, $Row["CALCULUS"], "CALCULUS"."-".$counter, $extra, 1, "BLANK");

                //矯正
                $id = 'ari_nasi-'.$counter;
                if ($Row["ORTHODONTICS"] == 1) {
                    $extra = "onclick=\"checkAri_Nasi(this, '$id');\" checked='checked'";
                    $ari_nasi = "<span id='$id' style='color:black;'>有</span>";
                } else {
                    $extra = "onclick=\"checkAri_Nasi(this, '$id');\"";
                    $ari_nasi = "<span id='$id' style='color:black;'>無</span>";
                }
                $setData["ORTHODONTICS"] = knjCreateCheckBox($objForm, "ORTHODONTICS"."-".$counter, '1', $extra).$ari_nasi;

                //歯数テキスト
                foreach ($model->cntItem as $key => $val) {
                    $extra = "STYLE=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
                    $setData[$key] = knjCreateTextBox($objForm, $Row[$key], $key."-".$counter, 2, 2, $extra);
                }

                //その他疾病及び異常コンボ
                $extra = " onclick=\"OptionUse(this, 'OTHERDISEASE-$counter');\"";
                $setData["OTHERDISEASECD"] = makeCmbReturn($objForm, $optF530, $Row["OTHERDISEASECD"], "OTHERDISEASECD"."-".$counter, $extra, 1, "BLANK");

                //その他疾病及び異常テキスト
                $extra = ($Row["OTHERDISEASECD"] == '99') ? "" : " disabled";
                $setData["OTHERDISEASE"] = knjCreateTextBox($objForm, $Row["OTHERDISEASE"], "OTHERDISEASE"."-".$counter, 40, 60, $extra);

                //その他疾病及び異常コンボ２
                $extra = "";
                if ($model->is_f020_otherdisese_hyouji2) {
                    $extra .= "  onChange=\"OptionUse(this, 'OTHERDISEASE3-".$counter."');\"";
                }
                $setData["OTHERDISEASECD3"] = makeCmbReturn($objForm, $optF530, $Row["OTHERDISEASECD3"], "OTHERDISEASECD3"."-".$counter, $extra, 1, "BLANK");

                //その他疾病及び異常コンボ３
                $extra = "";
                if ($model->is_f020_otherdisese_hyouji2) {
                    $extra .= "  onChange=\"OptionUse(this, 'OTHERDISEASE4-".$counter."');\"";
                }
                $setData["OTHERDISEASECD4"] = makeCmbReturn($objForm, $optF530, $Row["OTHERDISEASECD4"], "OTHERDISEASECD4"."-".$counter, $extra, 1, "BLANK");

                if ($model->is_f020_otherdisese_hyouji2) {
                    //その他の疾病及び異常テキスト 2行目
                    $extra = ($Row["OTHERDISEASECD3"] == '99' || $model->Properties["printKenkouSindanIppan"] == "3") ? "" : "disabled ";
                    $setData["OTHERDISEASE3"] = knjCreateTextBox($objForm, $Row["OTHERDISEASE3"], "OTHERDISEASE3"."-".$counter, 40, 60, $extra);

                    //その他の疾病及び異常テキスト 3行目
                    $extra = ($Row["OTHERDISEASECD4"] == '99' || $model->Properties["printKenkouSindanIppan"] == "3") ? "" : "disabled ";
                    $setData["OTHERDISEASE4"] = knjCreateTextBox($objForm, $Row["OTHERDISEASE4"], "OTHERDISEASE4"."-".$counter, 40, 60, $extra);
                }

                //口腔の疾病及び異常コンボ
                $extra = "";
                $setData["OTHERDISEASECD2"] = makeCmbReturn($objForm, $optF531, $Row["OTHERDISEASECD2"], "OTHERDISEASECD2"."-".$counter, $extra, 1, "BLANK");

                //口腔の疾病及び異常テキスト
                $extra = "";
                $setData["OTHERDISEASE2"] = knjCreateTextBox($objForm, $Row["OTHERDISEASE2"], "OTHERDISEASE2"."-".$counter, 40, 60, $extra);

                if ($model->is_f020_otherdisese_hyouji) {
                    //口腔の疾病及び異常コンボ 2行目
                    $extra = "";
                    $setData["OTHERDISEASE_REMARK1"] = makeCmbReturn($objForm, $optF531, $Row["OTHERDISEASE_REMARK1"], "OTHERDISEASE_REMARK1"."-".$counter, $extra, 1, "BLANK");

                    //口腔の疾病及び異常テキスト 2行目
                    $extra = "";
                    $setData["OTHERDISEASE_REMARK2"] = knjCreateTextBox($objForm, $Row["OTHERDISEASE_REMARK2"], "OTHERDISEASE_REMARK2"."-".$counter, 40, 60, $extra);

                    //口腔の疾病及び異常コンボ 3行目
                    $extra = "";
                    $setData["OTHERDISEASE_REMARK3"] = makeCmbReturn($objForm, $optF531, $Row["OTHERDISEASE_REMARK3"], "OTHERDISEASE_REMARK3"."-".$counter, $extra, 1, "BLANK");

                    //口腔の疾病及び異常テキスト 3行目
                    $extra = "";
                    $setData["OTHERDISEASE_REMARK4"] = knjCreateTextBox($objForm, $Row["OTHERDISEASE_REMARK4"], "OTHERDISEASE_REMARK4"."-".$counter, 40, 60, $extra);
                }

                //学校歯科医・所見コンボ
                if ($arg["is_mie"] == "1") {
                    $extra = "";
                } else {
                    $extra = " onChange=\"syokenNyuryoku(this, 'DENTISTREMARK', ['".join("','", (array)$nameSpare2["F540"])."'], '{$counter}')\"";
                }
                $setData["DENTISTREMARKCD"] = makeCmbReturn($objForm, $optF540, $Row["DENTISTREMARKCD"], "DENTISTREMARKCD"."-".$counter, $extra, 1, "BLANK");
                if ($arg["is_not_mie"] == "1") {
                    //所見テキスト
                    if (!in_array($Row["DENTISTREMARKCD"], (array)$nameSpare2["F540"])) {
                        $extra = "disabled";
                    } else {
                        $extra = "";
                    }
                    $setData["DENTISTREMARK"] = knjCreateTextBox($objForm, $Row["DENTISTREMARK"], "DENTISTREMARK"."-".$counter, 40, 20, $extra);
                }

                if ($model->is_f020_dentistremark_hyouji) {
                    //学校歯科医・所見コンボ 2行目
                    if ($arg["is_mie"] == "1") {
                        $extra = "";
                    } else {
                        $extra = " onChange=\"syokenNyuryoku(this, 'DENTISTREMARK_REMARK2', ['".join("','", $nameSpare2["F540"])."'], '{$counter}')\"";
                    }
                    $setData["DENTISTREMARK_REMARK1"] = makeCmbReturn($objForm, $optF540, $Row["DENTISTREMARK_REMARK1"], "DENTISTREMARK_REMARK1"."-".$counter, $extra, 1, "BLANK");

                    //所見テキスト 2行目
                    if (!in_array($Row["DENTISTREMARK_REMARK1"], (array)$nameSpare2["F540"])) {
                        $extra = "disabled";
                    } else {
                        $extra = "";
                    }
                    $setData["DENTISTREMARK_REMARK2"] = knjCreateTextBox($objForm, $Row["DENTISTREMARK_REMARK2"], "DENTISTREMARK_REMARK2"."-".$counter, 40, 20, $extra);

                    //学校歯科医・所見コンボ 3行目
                    if ($arg["is_mie"] == "1") {
                        $extra = "";
                    } else {
                        $extra = " onChange=\"syokenNyuryoku(this, 'DENTISTREMARK_REMARK4', ['".join("','", $nameSpare2["F540"])."'], '{$counter}')\"";
                    }
                    $setData["DENTISTREMARK_REMARK3"] = makeCmbReturn($objForm, $optF540, $Row["DENTISTREMARK_REMARK3"], "DENTISTREMARK_REMARK3"."-".$counter, $extra, 1, "BLANK");

                    //所見テキスト 3行目
                    if (!in_array($Row["DENTISTREMARK_REMARK3"], (array)$nameSpare2["F540"])) {
                        $extra = "disabled";
                    } else {
                        $extra = "";
                    }
                    $setData["DENTISTREMARK_REMARK4"] = knjCreateTextBox($objForm, $Row["DENTISTREMARK_REMARK4"], "DENTISTREMARK_REMARK4"."-".$counter, 40, 20, $extra);
                }

                //学校歯科医・所見日付
                $Row["DENTISTREMARKDATE"] = str_replace("-", "/", $Row["DENTISTREMARKDATE"]);
                $setData["DENTISTREMARKDATE"] = View::popUpCalendar($objForm, "DENTISTREMARKDATE"."-".$counter, $Row["DENTISTREMARKDATE"]);

                //学校歯科医・事後措置コンボ
                if ($arg["is_mie"] == "1") {
                    $extra = "";
                } else {
                    $extra = " onChange=\"syokenNyuryoku(this, 'DENTISTTREAT', ['".join("','", (array)$nameSpare2["F541"])."'], '{$counter}')\"";
                }
                $setData["DENTISTTREATCD"] = makeCmbReturn($objForm, $optF541, $Row["DENTISTTREATCD"], "DENTISTTREATCD"."-".$counter, $extra, 1, "BLANK");

                if ($arg["is_not_mie"] == "1") {
                    //学校歯科医・事後措置テキスト
                    if (!in_array($Row["DENTISTTREATCD"], (array)$nameSpare2["F541"])) {
                        $extra = "disabled";
                    } else {
                        $extra = "";
                    }
                    $setData["DENTISTTREAT"] = knjCreateTextBox($objForm, $Row["DENTISTTREAT"], "DENTISTTREAT"."-".$counter, 40, 20, $extra);
                }
            } else {
                foreach ($model->shisiki as $key => $val) {
                    //歯式コンボ
                    $extra = "";
                    $setData[$key] = makeCmbReturn($objForm, $optF550, $Row[$key], $key."-".$counter, $extra, 1, "BLANK");
                }
            }

            $arg["data"][] = $setData;

            knjCreateHidden($objForm, "SCHREGNO"."-".$counter, $Row["SCHREGNO"]);
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjf023Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
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

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
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

//コンボ作成（表内）
function makeCmbReturn(&$objForm, &$getopt, &$value, $name, $extra, $size, $blank = "")
{
    $opt = $getopt;
    $value_flg = false;
    if ($blank) {
        array_unshift($opt, array("label" => "", "value" => ""));
    }
    foreach ($opt as $key => $val) {
        if ($value === $val["value"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "H_GRADE_HR_CLASS");
    knjCreateHidden($objForm, "H_SCREEN");
}
