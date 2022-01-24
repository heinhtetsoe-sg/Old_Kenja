<?php

require_once('for_php7.php');

class knjf023jForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjf023jindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        //年組コンボ
        $query = knjf023jQuery::getHrClass($model);
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

        //サブタイトル
        $arg["SUBTITLE"] = $opt[$model->screen-1]["label"];

        //表幅
        $arg["WIDTH"] = ($model->screen == 1) ? "5100px" : "4470px";

        //高さ
        $arg["heightHeader"] = ($model->screen == 1) ? "90px"  : "80px";
        $arg["heightMeisai"] = ($model->screen == 1) ? "100px" : "70px";

        //歯列・咬合の幅
        $jaws_jointcd_width = "340";
        $arg["JAWS_JOINTCD_WIDTH"] = $jaws_jointcd_width;

        //データ取得
        $schList = array();
        if (!isset($model->warning) && $model->grade_hr_class != "") {
            //生徒一覧
            $query = knjf023jQuery::getSchToothInfo($model);
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
        $nameM = array();
        if ($model->screen == "1") {
            $nameM = array("F510","F511","F520","F513","F521","F530","F531","F540","F541");
        } else {
            $nameM = array("F550");
        }

        foreach ($nameM as $namecd1) {
            //名称マスタよりデータ取得・格納
            $query    = knjf023jQuery::getNameMst($model, $namecd1);
            $optname  = "opt".$namecd1;
            $$optname = makeArrayReturn($db, $query);
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
                //顎関節コンボ
                $extra = "";
                $setData["JAWS_JOINTCD2"] = makeCmbReturn($objForm, $optF511, $Row["JAWS_JOINTCD2"], "JAWS_JOINTCD2"."-".$counter, $extra, 1, "BLANK");

                //歯列・咬合コンボ
                $jaws_jointcd_label = $jaws_jointcd3 = "";
                $extra = "";
                $jaws_jointcd  = $jaws_jointcd_label.makeCmbReturn($objForm, $optF510, $Row["JAWS_JOINTCD"], "JAWS_JOINTCD"."-".$counter, $extra, 1, "BLANK");
                $jaws_jointcd .= $jaws_jointcd3;
                $setData["JAWS_JOINTCD"] = $jaws_jointcd;

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
                    $extra = "STYLE=\"text-align:right\" onblur=\"return Num_Check(this);\"";
                    $setData[$key] = knjCreateTextBox($objForm, $Row[$key], $key."-".$counter, 2, 2, $extra);
                }

                //その他疾病及び異常コンボ
                $disableCodes = getDisableCodes($db, $model, "F530");
                $extra = " onclick=\"OptionUse(this, 'OTHERDISEASE-$counter', '".implode(",", $disableCodes)."')\"";
                $setData["OTHERDISEASECD"] = makeCmbReturn($objForm, $optF530, $Row["OTHERDISEASECD"], "OTHERDISEASECD"."-".$counter, $extra, 1, "BLANK");

                //その他疾病及び異常テキスト
                if (in_array($Row["OTHERDISEASECD"], $disableCodes) == true || $Row["OTHERDISEASECD"] == '') {
                    //未選択時は所見欄無効
                    $extra = "disabled style=\"background-color:darkgray\"";
                } else {
                    $extra = "";
                }
                $disableCodes = getDisableCodes($db, $model, "F530");
                $setData["OTHERDISEASE"] = knjCreateTextBox($objForm, $Row["OTHERDISEASE"], "OTHERDISEASE"."-".$counter, 40, 60, $extra);

                //口腔の疾病及び異常コンボ
                $disableCodes = getDisableCodes($db, $model, "F531");
                $extra = " onclick=\"OptionUse2(this, 'OTHERDISEASE2-$counter', '".implode(",", $disableCodes)."')\"";
                $setData["OTHERDISEASECD2"] = makeCmbReturn($objForm, $optF531, $Row["OTHERDISEASECD2"], "OTHERDISEASECD2"."-".$counter, $extra, 1, "BLANK");

                //口腔の疾病及び異常テキスト
                if (in_array($Row["OTHERDISEASECD2"], $disableCodes) == true || $Row["OTHERDISEASECD2"] == '') {
                    //未選択時は所見欄無効
                    $extra = "disabled style=\"background-color:darkgray\"";
                } else {
                    $extra = "";
                }
                $setData["OTHERDISEASE2"] = knjCreateTextBox($objForm, $Row["OTHERDISEASE2"], "OTHERDISEASE2"."-".$counter, 40, 60, $extra);

                //学校歯科医・所見1コンボ
                $disableCodes = getDisableCodes($db, $model, "F540");
                $extra = " onChange=\"OptionUse3('".implode(",", $disableCodes)."', $counter)\"";
                $setData["DENTISTREMARKCD"] = makeCmbReturn($objForm, $optF540, $Row["DENTISTREMARKCD"], "DENTISTREMARKCD"."-".$counter, $extra, 1, "BLANK");

                //所見1テキスト
                if (in_array($Row["DENTISTREMARKCD"], $disableCodes) == true || $Row["DENTISTREMARKCD"] == '') {
                    //未選択時は所見欄無効
                    $extra = "disabled style=\"background-color:darkgray\"";
                } else {
                    $extra = "";
                }
                $setData["DENTISTREMARK"] = knjCreateTextBox($objForm, $Row["DENTISTREMARK"], "DENTISTREMARK"."-".$counter, 40, 20, $extra);

                //学校歯科医・所見2コンボ
                $disableCodes = getDisableCodes($db, $model, "F540");
                if ($Row["DENTISTREMARKCD"] == '' || $Row["DENTISTREMARKCD"] == '00') {
                    //未選択時と「00:未受験時」はコンボ無効
                    $extra = "style=\"background-color:darkgray\" disabled ";
                } else {
                    $extra = "";
                }
                $extra .= " onChange=\"OptionUse3('".implode(",", $disableCodes)."', $counter)\"";
                $setData["DENTISTREMARKCD2"] = makeCmbReturn($objForm, $optF540, $Row["DENTISTREMARKCD2"], "DENTISTREMARKCD2"."-".$counter, $extra, 1, "BLANK");

                //所見2テキスト
                if (in_array($Row["DENTISTREMARKCD2"], $disableCodes) == true || $Row["DENTISTREMARKCD2"] == '') {
                    //未選択時は所見欄無効
                    $extra = "disabled style=\"background-color:darkgray\"";
                } else {
                    $extra = "";
                }
                $setData["DENTISTREMARK2"] = knjCreateTextBox($objForm, $Row["DENTISTREMARK2"], "DENTISTREMARK2"."-".$counter, 40, 20, $extra);

                //学校歯科医・所見3コンボ
                $disableCodes = getDisableCodes($db, $model, "F540");
                if ($Row["DENTISTREMARKCD2"] == '' || $Row["DENTISTREMARKCD2"] == '00') {
                    //未選択時と「00:未受験時」はコンボ無効
                    $extra = "style=\"background-color:darkgray\" disabled ";
                } else {
                    $extra = "";
                }
                $extra .= " onChange=\"OptionUse3('".implode(",", $disableCodes)."', $counter)\"";
                $setData["DENTISTREMARKCD3"] = makeCmbReturn($objForm, $optF540, $Row["DENTISTREMARKCD3"], "DENTISTREMARKCD3"."-".$counter, $extra, 1, "BLANK");

                //所見3テキスト
                if (in_array($Row["DENTISTREMARKCD3"], $disableCodes) == true || $Row["DENTISTREMARKCD3"] == '') {
                    //未選択時は所見欄無効
                    $extra = "disabled style=\"background-color:darkgray\"";
                } else {
                    $extra = "";
                }
                $setData["DENTISTREMARK3"] = knjCreateTextBox($objForm, $Row["DENTISTREMARK3"], "DENTISTREMARK3"."-".$counter, 40, 20, $extra);

                //学校歯科医・所見日付
                $Row["DENTISTREMARKDATE"] = str_replace("-", "/", $Row["DENTISTREMARKDATE"]);
                $setData["DENTISTREMARKDATE"] = View::popUpCalendar($objForm, "DENTISTREMARKDATE"."-".$counter, $Row["DENTISTREMARKDATE"]);

                //学校歯科医・事後措置1コンボ
                $disableCodes = getDisableCodes($db, $model, "F541");
                //$extra = " onChange=\"OptionUse6(this, 'DENTISTTREAT-$counter', '".implode(",", $disableCodes)."', $counter)\"";
                $extra = " onChange=\"OptionUse4('".implode(",", $disableCodes)."', $counter)\"";
                $setData["DENTISTTREATCD"] = makeCmbReturn($objForm, $optF541, $Row["DENTISTTREATCD"], "DENTISTTREATCD"."-".$counter, $extra, 1, "BLANK");

                //学校歯科医・事後措置1テキスト
                if (in_array($Row["DENTISTTREATCD"], $disableCodes) == true || $Row["DENTISTTREATCD"] == '') {
                    //未選択時は所見欄無効
                    $extra = "disabled style=\"background-color:darkgray\"";
                } else {
                    $extra = "";
                }
                $setData["DENTISTTREAT"] = knjCreateTextBox($objForm, $Row["DENTISTTREAT"], "DENTISTTREAT"."-".$counter, 40, 20, $extra);

                //学校歯科医・事後措置2コンボ
                $disableCodes = getDisableCodes($db, $model, "F541");
                if ($Row["DENTISTTREATCD"] == '' || $Row["DENTISTTREATCD"] == '01') {
                    //未選択時と「01:なし」はコンボ無効
                    $extra = "style=\"background-color:darkgray\" disabled ";
                } else {
                    $extra = "";
                }
                //$extra .= " onChange=\"OptionUse7(this, 'DENTISTTREAT2-$counter', '".implode(",", $disableCodes)."')\"";
                $extra = " onChange=\"OptionUse4('".implode(",", $disableCodes)."', $counter)\"";
                $setData["DENTISTTREATCD2"] = makeCmbReturn($objForm, $optF541, $Row["DENTISTTREATCD2"], "DENTISTTREATCD2"."-".$counter, $extra, 1, "BLANK");

                //学校歯科医・事後措置2テキスト
                if (in_array($Row["DENTISTTREATCD2"], $disableCodes) == true || $Row["DENTISTTREATCD2"] == '') {
                    //未選択時は所見欄無効
                    $extra = "disabled style=\"background-color:darkgray\"";
                } else {
                    $extra = "";
                }
                $setData["DENTISTTREAT2"] = knjCreateTextBox($objForm, $Row["DENTISTTREAT2"], "DENTISTTREAT2"."-".$counter, 40, 20, $extra);
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

        View::toHTML($model, "knjf023jForm1.html", $arg);
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

//所見欄を無効にするcdの文字列
function getDisableCodes($db, $model, $setInNamecd1)
{
    $query = knjf023jQuery::getNameMstDisableCodes($model, $setInNamecd1);
    $disableCodes = array();
    $db = Query::dbCheckOut();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $disableCodes[] .= $row["NAMECD2"];
    }
    $result->free();

    return $disableCodes;
}
