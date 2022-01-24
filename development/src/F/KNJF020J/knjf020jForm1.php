<?php

require_once('for_php7.php');

class knjf020jForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjf020jindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->schregno) && $model->cmd != "change") {
            $RowH = knjf020jQuery::getMedexamHdat($model);      //生徒健康診断ヘッダデータ取得
            $RowT = knjf020jQuery::getMedexamToothDat($model); //生徒健康診断歯口腔データ取得
            $arg["NOT_WARNING"] = 1;
        } else {
            $RowH =& $model->field;
            $RowT =& $model->field;
        }

        $db = Query::dbCheckOut();

        //ヘッダ
        if (isset($model->schregno)) {
            //生徒学籍データを取得
            $query = knjf020jQuery::getSchregBaseMstData($model);
            $result = $db->query($query);
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
            $result = $db->query(knjf020jQuery::getSchregRegdDatData($model));
            $RowR = $result->fetchRow(DB_FETCHMODE_ASSOC);
            $result->free();
            $model->GradeClass = $RowR["GRADE"]."-".$RowR["HR_CLASS"];
            $model->Hrname = $RowR["HR_NAME"];
        } else {
            //学籍番号
            $arg["header"]["SCHREGNO"] = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
            //生徒氏名
            $arg["header"]["NAME_SHOW"] = "&nbsp;&nbsp;&nbsp;&nbsp;";
            //生年月日
            $arg["header"]["BIRTHDAY"] = "&nbsp;&nbsp;&nbsp;&nbsp;年&nbsp;&nbsp;&nbsp;&nbsp;月&nbsp;&nbsp;&nbsp;&nbsp;日";
        }

        //レイアウト調整用
        $arg["data"]["SPAN1"]   = 3;
        $arg["data"]["SPAN2"]   = 2;
        $arg["data"]["ROWSPAN"] = 16;

        //Enterキーで移動する項目一覧
        $cnt = 0;
        $entMoveArray = array();
        $entMoveArray["TOOTH_DATE"]        = $cnt++;
        $entMoveArray["JAWS_JOINTCD2"]     = $cnt++;
        $entMoveArray["JAWS_JOINTCD"]      = $cnt++;
        $entMoveArray["JAWS_JOINTCD3"]     = $cnt++;
        $entMoveArray["PLAQUECD"]          = $cnt++;
        $entMoveArray["GUMCD"]             = $cnt++;
        $entMoveArray["ORTHODONTICS"]      = $cnt++;
        $entMoveArray["btn_adultIns"]      = $cnt++;
        $entMoveArray["OTHERDISEASECD"]    = $cnt++;
        $entMoveArray["OTHERDISEASE"]      = $cnt++;
        $entMoveArray["DENTISTREMARK_CO"]  = $cnt++;
        $entMoveArray["DENTISTREMARK_GO"]  = $cnt++;
        $entMoveArray["DENTISTREMARK_G"]   = $cnt++;
        $entMoveArray["DENTISTREMARKDATE"] = $cnt++;
        $entMoveArray["DENTISTTREATCD"]    = $cnt++;
        $entMoveArray["DENTISTTREAT"]      = $cnt++;
        $entMoveArray["DENTISTTREAT2"]     = $cnt++;
        $entMoveArray["DENTISTTREAT3"]     = $cnt++;
        $entMoveArray["btn_update"]        = $cnt++;
        $entMoveArray["btn_up_pre"]        = $cnt++;
        $entMoveArray["btn_up_next"]       = $cnt++;

        $entMove= array();
        foreach ($entMoveArray as $key => $val) {
            $entMove[$key] = " id=\"ENTMOVE_{$val}\" onKeyDown=\"keyChangeEntToTab(this);\"";
        }

        /* 編集項目 */
        //健康診断実施日付
        $RowH["TOOTH_DATE"] = str_replace("-", "/", $RowH["TOOTH_DATE"]);
        $extra = $entMove["TOOTH_DATE"];
        $arg["data"]["TOOTH_DATE"] = View::popUpCalendar2($objForm, "TOOTH_DATE", $RowH["TOOTH_DATE"], "", "", $extra);

        //歯列・咬合コンボボックス
        $query = knjf020jQuery::getNameMst($model, "F510");
        $extra = "style=\"width:250px;\"".$entMove["JAWS_JOINTCD"];
        makeCombo($objForm, $arg, $db, $query, $RowT["JAWS_JOINTCD"], "JAWS_JOINTCD", $extra, 1, "BLANK");

        $arg["data"]["JAWS_JOINTCD_LABEL"] = '★ 歯列・咬合';

        $arg["F020_OTHERDISESE_HYOUJI"]   = "";
        $arg["F020_DENTISTREMARK_HYOUJI"] = "";

        //咬合コンボボックス
        $query = knjf020jQuery::getNameMst($model, "F512");
        $extra = "style=\"width:250px;\"".$entMove["JAWS_JOINTCD3"];
        makeCombo($objForm, $arg, $db, $query, $RowT["JAWS_JOINTCD3"], "JAWS_JOINTCD3", $extra, 1, "BLANK");

        //顎関節コンボボックス
        $query = knjf020jQuery::getNameMst($model, "F511");
        $extra = "style=\"width:250px;\"".$entMove["JAWS_JOINTCD2"];
        makeCombo($objForm, $arg, $db, $query, $RowT["JAWS_JOINTCD2"], "JAWS_JOINTCD2", $extra, 1, "BLANK");

        //歯垢の状態コンボ
        $query = knjf020jQuery::getNameMst($model, "F520");
        $extra = "style=\"width:150px;\"".$entMove["PLAQUECD"];
        makeCombo($objForm, $arg, $db, $query, $RowT["PLAQUECD"], "PLAQUECD", $extra, 1, "BLANK");

        //歯肉の状態コンボ
        $query = knjf020jQuery::getNameMst($model, "F513");
        $extra = "style=\"width:250px;\"".$entMove["GUMCD"];
        makeCombo($objForm, $arg, $db, $query, $RowT["GUMCD"], "GUMCD", $extra, 1, "BLANK");

        //歯石沈着コンボ
        $query = knjf020jQuery::getNameMst($model, "F521");
        $extra = "style=\"width:250px;\"".$entMove["CALCULUS"];
        makeCombo($objForm, $arg, $db, $query, $RowT["CALCULUS"], "CALCULUS", $extra, 1, "BLANK");

        //矯正
        if ($RowT["ORTHODONTICS"] == 1) {
            $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi');\" checked='checked'";
            $ari_nasi = "<span id='ari_nasi' style='color:black;'>有</span>";
        } else {
            $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi');\"";
            $ari_nasi = "<span id='ari_nasi' style='color:black;'>無</span>";
        }
        $arg["data"]["ORTHODONTICS"] = knjCreateCheckBox($objForm, "ORTHODONTICS", '1', $extra.$entMove["ORTHODONTICS"]).$ari_nasi;

        //乳歯・現在数
        $extra = "style=\"text-align:right\" onblur=\"return Num_Check(this);\"";
        $arg["data"]["BABYTOOTH"] = knjCreateTextBox($objForm, $RowT["BABYTOOTH"], "BABYTOOTH", 2, 2, $extra);

        //乳歯・未処置数
        $extra = "style=\"text-align:right\" onblur=\"return Num_Check(this);\"";
        $arg["data"]["REMAINBABYTOOTH"] = knjCreateTextBox($objForm, $RowT["REMAINBABYTOOTH"], "REMAINBABYTOOTH", 2, 2, $extra);

        //乳歯・処置数
        $extra = "style=\"text-align:right\" onblur=\"return Num_Check(this);\"";
        $arg["data"]["TREATEDBABYTOOTH"] = knjCreateTextBox($objForm, $RowT["TREATEDBABYTOOTH"], "TREATEDBABYTOOTH", 2, 2, $extra);

        //乳歯・要注意乳歯数
        $extra = "style=\"text-align:right\" onblur=\"return Num_Check(this);\"";
        $arg["data"]["BRACK_BABYTOOTH"] = knjCreateTextBox($objForm, $RowT["BRACK_BABYTOOTH"], "BRACK_BABYTOOTH", 2, 2, $extra);

        //永久歯・現在数
        $extra = "style=\"text-align:right\" onblur=\"return Num_Check(this);\"";
        $arg["data"]["ADULTTOOTH"] = knjCreateTextBox($objForm, $RowT["ADULTTOOTH"], "ADULTTOOTH", 2, 2, $extra);

        //永久歯・未処置数
        $extra = "style=\"text-align:right\" onblur=\"return Num_Check(this);\"";
        $arg["data"]["REMAINADULTTOOTH"] = knjCreateTextBox($objForm, $RowT["REMAINADULTTOOTH"], "REMAINADULTTOOTH", 2, 2, $extra);

        //永久歯・処置数
        $extra = "style=\"text-align:right\" onblur=\"return Num_Check(this);\"";
        $arg["data"]["TREATEDADULTTOOTH"] = knjCreateTextBox($objForm, $RowT["TREATEDADULTTOOTH"], "TREATEDADULTTOOTH", 2, 2, $extra);

        //永久歯・喪失数
        $extra = "style=\"text-align:right\" onblur=\"return Num_Check(this);\"";
        $arg["data"]["LOSTADULTTOOTH"] = knjCreateTextBox($objForm, $RowT["LOSTADULTTOOTH"], "LOSTADULTTOOTH", 2, 2, $extra);

        //永久歯・要観察歯数
        $extra = "style=\"text-align:right\" onblur=\"return Num_Check(this);\"";
        $arg["data"]["BRACK_ADULTTOOTH"] = knjCreateTextBox($objForm, $RowT["BRACK_ADULTTOOTH"], "BRACK_ADULTTOOTH", 2, 2, $extra);

        //その他疾病及び異常コンボ
        $disableCodes = getDisableCodes($db, $model, "F530");
        $query = knjf020jQuery::getNameMst($model, "F530");
        $extra = "style=\"width:120px;\" onChange=\"OptionUse(this, document.forms[0].OTHERDISEASE, '".implode(",", $disableCodes)."');\"".$entMove["OTHERDISEASECD"];
        makeCombo($objForm, $arg, $db, $query, $RowT["OTHERDISEASECD"], "OTHERDISEASECD", $extra, 1, "BLANK");

        //その他疾病及び異常テキスト
        if (in_array($RowT["OTHERDISEASECD"], $disableCodes) == true || $RowT["OTHERDISEASECD"] == '') {
            //未選択時は所見欄無効
            $extra = "disabled style=\"background-color:darkgray\"";
        } else {
            $extra = "";
        }
        $arg["data"]["OTHERDISEASE"] = knjCreateTextBox($objForm, $RowT["OTHERDISEASE"], "OTHERDISEASE", 40, 60, $extra.$entMove["OTHERDISEASE"]);

        //口腔の疾病及び異常コンボ
        $disableCodes = getDisableCodes($db, $model, "F531");
        $query = knjf020jQuery::getNameMst($model, "F531");
        $extra = "style=\"width:120px;\" onChange=\"OptionUse2(this, document.forms[0].OTHERDISEASE2, '".implode(",", $disableCodes)."');\"".$entMove["OTHERDISEASECD2"];
        makeCombo($objForm, $arg, $db, $query, $RowT["OTHERDISEASECD2"], "OTHERDISEASECD2", $extra, 1, "BLANK");

        //口腔の疾病及び異常テキスト
        $extra = $entMove["OTHERDISEASE2"];
        if (in_array($RowT["OTHERDISEASECD2"], $disableCodes) == true || $RowT["OTHERDISEASECD2"] == '') {
            //未選択時は所見欄無効
            $extra .= " disabled style=\"background-color:darkgray\"";
        }
        $arg["data"]["OTHERDISEASE2"] = knjCreateTextBox($objForm, $RowT["OTHERDISEASE2"], "OTHERDISEASE2", 40, 60, $extra);

        //所見１コンボボックス
        $disableCodes = getDisableCodes($db, $model, "F540");
        $query = knjf020jQuery::getNameMst($model, "F540");
        $extra = "style=\"width:150px;\" onChange=\"OptionUse3('".implode(",", $disableCodes)."')\"".$entMove["DENTISTREMARKCD"];
        makeCombo($objForm, $arg, $db, $query, $RowT["DENTISTREMARKCD"], "DENTISTREMARKCD", $extra, 1, "BLANK");

        //所見１テキスト
        if (in_array($RowT["DENTISTREMARKCD"], $disableCodes) == true || $RowT["DENTISTREMARKCD"] == '') {
            //未選択時は所見欄無効
            $extra = "disabled style=\"background-color:darkgray\"";
        } else {
            $extra = "";
        }
        $arg["data"]["DENTISTREMARK"] = knjCreateTextBox($objForm, $RowT["DENTISTREMARK"], "DENTISTREMARK", 40, 60, $extra.$entMove["DENTISTREMARK"]);

        //所見２コンボボックス
        $disableCodes = getDisableCodes($db, $model, "F540");
        $query = knjf020jQuery::getNameMst($model, "F540");
        if ($RowT["DENTISTREMARKCD"] == '' || $RowT["DENTISTREMARKCD"] == '00') {
            //未選択時と「00:未受験時」はコンボ無効
            $extra = "style=\"width:150px; background-color:darkgray\" disabled ";
        } else {
            $extra = "style=\"width:150px;\" ";
        }
        $extra .= "onChange=\"OptionUse3('".implode(",", $disableCodes)."')\"".$entMove["DENTISTREMARKCD2"];
        makeCombo($objForm, $arg, $db, $query, $RowT["DENTISTREMARKCD2"], "DENTISTREMARKCD2", $extra, 1, "BLANK");

        //所見２テキスト
        if (in_array($RowT["DENTISTREMARKCD2"], $disableCodes) == true || $RowT["DENTISTREMARKCD2"] == '') {
            //未選択時は所見欄無効
            $extra = "disabled style=\"background-color:darkgray\"";
        } else {
            $extra = "";
        }
        $arg["data"]["DENTISTREMARK2"] = knjCreateTextBox($objForm, $RowT["DENTISTREMARK2"], "DENTISTREMARK2", 40, 60, $extra.$entMove["DENTISTREMARK2"]);

        //所見３コンボボックス
        $disableCodes = getDisableCodes($db, $model, "F540");
        $query = knjf020jQuery::getNameMst($model, "F540");
        if ($RowT["DENTISTREMARKCD2"] == '' || $RowT["DENTISTREMARKCD2"] == '00') {
            //未選択時と「00:未受験時」はコンボ無効
            $extra = "style=\"width:150px; background-color:darkgray\" disabled ";
        } else {
            $extra = "style=\"width:150px;\" ";
        }
        $extra .= "onChange=\"OptionUse3('".implode(",", $disableCodes)."')\"".$entMove["DENTISTREMARK2"];
        makeCombo($objForm, $arg, $db, $query, $RowT["DENTISTREMARKCD3"], "DENTISTREMARKCD3", $extra, 1, "BLANK");

        //所見３テキスト
        if (in_array($RowT["DENTISTREMARKCD3"], $disableCodes) == true || $RowT["DENTISTREMARKCD3"] == '') {
            //未選択時は所見欄無効
            $extra = "disabled style=\"background-color:darkgray\"";
        } else {
            $extra = "";
        }
        $arg["data"]["DENTISTREMARK3"] = knjCreateTextBox($objForm, $RowT["DENTISTREMARK3"], "DENTISTREMARK3", 40, 60, $extra.$entMove["DENTISTREMARK3"]);

        //所見日付
        $RowT["DENTISTREMARKDATE"] = str_replace("-", "/", $RowT["DENTISTREMARKDATE"]);
        $arg["data"]["DENTISTREMARKDATE"] = View::popUpCalendar2($objForm, "DENTISTREMARKDATE", $RowT["DENTISTREMARKDATE"], "", "", $entMove["DENTISTREMARKDATE"]);

        //事後措置１コンボ
        $disableCodes = getDisableCodes($db, $model, "F541");
        $query = knjf020jQuery::getNameMst($model, "F541");
        $extra = "style=\"width:250px;\" onChange=\"OptionUse4('".implode(",", $disableCodes)."')\"".$entMove["DENTISTTREATCD"];
        makeCombo($objForm, $arg, $db, $query, $RowT["DENTISTTREATCD"], "DENTISTTREATCD", $extra, 1, "BLANK");

        //事後措置１テキスト
        $extra = $entMove["DENTISTTREAT"];
        if (in_array($RowT["DENTISTTREATCD"], $disableCodes) == true || $RowT["DENTISTTREATCD"] == '') {
            $extra .= " disabled style=\"background-color:darkgray\"";
        }
        $arg["data"]["DENTISTTREAT"] = knjCreateTextBox($objForm, $RowT["DENTISTTREAT"], "DENTISTTREAT", 40, 60, $extra);

        //事後措置２コンボ
        $disableCodes = getDisableCodes($db, $model, "F541");
        $query = knjf020jQuery::getNameMst($model, "F541");
        if ($RowT["DENTISTTREATCD"] == '' || $RowT["DENTISTTREATCD"] == '01') {
            //未選択時と「01:なし」はコンボ無効
            $extra = "style=\"width:250px; background-color:darkgray\" disabled ";
        } else {
            $extra = "style=\"width:250px;\" ";
        }
        $extra .= "onChange=\"OptionUse4('".implode(",", $disableCodes)."')\"".$entMove["DENTISTTREATCD2"];
        makeCombo($objForm, $arg, $db, $query, $RowT["DENTISTTREATCD2"], "DENTISTTREATCD2", $extra, 1, "BLANK");

        //事後措置２テキスト
        $extra = $entMove["DENTISTTREAT2_1"];
        if (in_array($RowT["DENTISTTREATCD2"], $disableCodes) == true || $RowT["DENTISTTREATCD2"] == '') {
            $extra .= " disabled style=\"background-color:darkgray\"";
        }
        $arg["data"]["DENTISTTREAT2_1"] = knjCreateTextBox($objForm, $RowT["DENTISTTREAT2_1"], "DENTISTTREAT2_1", 40, 60, $extra);

        /**************/
        /*  歯式入力  */
        /**************/

        //入力方法ラジオボタン
        $opt_nyuryoku = array(1, 2);
        $model->nyuryoku = ($model->nyuryoku == "") ? "1" : $model->nyuryoku;
        $extra = array("id=\"NYURYOKU1\" onClick=\"myHidden()\"", "id=\"NYURYOKU2\" onClick=\"myHidden()\"");
        $radioArray = knjCreateRadio($objForm, "NYURYOKU", $model->nyuryoku, $extra, $opt_nyuryoku, get_count($opt_nyuryoku));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //出力データラジオボタン
        $sql        = knjf020jQuery::getNameMst($model, "F550");
        $result     = $db->query($sql);
        $hiddenVal  = "";
        $hiddenShow = "";
        $sep        = "";
        $f550Array = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $cd = (int) $row["VALUE"];
            $arg["data"]["SHOW_DIV".$cd] = $row["LABEL2"];

            $f550Array[$row["VALUE"]] = $row["SHOW"];
            $hiddenVal  .= $sep.$row["VALUE"];
            $hiddenShow .= $sep.$row["SHOW"];
            $sep = ",";
        }

        $opt_data = array();
        $extra = array();
        foreach ($f550Array as $key => $val) {
            $cd         = (int) $key;
            $opt_data[] = $cd;
            $extra[]    = "id=\"TYPE_DIV{$cd}\"";
        }
        $model->type_div = ($model->type_div == "") ? "1" : $model->type_div;
        $radioArray = knjCreateRadio($objForm, "TYPE_DIV", $model->type_div, $extra, $opt_data, get_count($opt_data));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //表示ラジオ
        $opt = array(1, 2);
        $model->disp = ($model->disp == "") ? "1" : $model->disp;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"DISP{$val}\" onClick=\"btn_submit('change')\"");
        }
        $radioArray = knjCreateRadio($objForm, "DISP", $model->disp, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //各歯列作成
        //永久歯ラベル上段
        makeSiretu($objForm, $arg, $model->adultUpLabelName, $RowT, "UP_ADULT", "upAdult", $f550Array);

        //永久歯ラベル下段
        makeSiretu($objForm, $arg, $model->adultLwLabelName, $RowT, "LW_ADULT", "lwAdult", $f550Array);

        //乳歯ラベル上段
        makeSiretu($objForm, $arg, $model->babyUpLabelName, $RowT, "UP_BABY", "upBaby", $f550Array);

        //乳歯ラベル下段
        makeSiretu($objForm, $arg, $model->babyLwLabelName, $RowT, "LW_BABY", "lwBaby", $f550Array);

        //右側の文言表示
        $arg["data"]["RIGHT_SIDE_LABEL"] = ($model->disp == "1") ? '左' : '右';
        //左側の文言表示
        $arg["data"]["LEFT_SIDE_LABEL"]  = ($model->disp == "1") ? '右' : '左';

        $sql = knjf020jQuery::getNameMst($model, "F550");
        $dataArray = array();
        $result    = $db->query($sql);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataArray[] = array("VAL"  => "\"javascript:setClickValue('".$row["VALUE"]."')\"",
                                 "NAME" => $row["LABEL2"]);
        }
        $arg["menuTitle"]["CLICK_NAME"] = knjCreateBtn($objForm, "btn_end", "×", "onclick=\"return setClickValue('999');\"");
        $arg["menuTitle"]["CLICK_VAL"]  = "javascript:setClickValue('999')";
        foreach ($dataArray as $key => $val) {
            $setData["CLICK_NAME"] = $val["NAME"];
            $setData["CLICK_VAL"]  = $val["VAL"];
            $arg["menu"][]         = $setData;
        }
        $result->free();

        //ボタン作成
        makeBtn($objForm, $arg, $model, $entMove);

        //hidden
        makeHidden($objForm, $model, $RowH, $hiddenVal, $hiddenShow, $cnt);

        Query::dbCheckIn($db);

        if (get_count($model->warning) == 0 && $model->cmd != "reset") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd == "reset") {
            $arg["next"] = "NextStudent(1);";
        }

        if ($model->sisikiClick == "ON") {
            $arg["next"] = "btn_submit('edit2');";
        }
        if ($model->cmd == "edit2") {
            $arg["next"] = "sisikiClick();";
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf020jForm1.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label"  => "",
                        "value" => "",
                        "flg"   => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label"  => $row["LABEL"],
                        "value" => $row["VALUE"],
                        "flg"   => $row["FLG"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $entMove)
{
    //永久歯正常入力
    $extra = "onmousedown=\"return dataChange('ADULT');\"";
    $arg["button"]["btn_adultIns"] = knjCreateBtn($objForm, "btn_adultIns", "永久歯正常", $extra.$entMove["btn_adultIns"]);
    //乳歯正常入力
    $extra = "onclick=\"return dataChange('BABY');\"";
    $arg["button"]["btn_babyIns"] = knjCreateBtn($objForm, "btn_babyIns", "乳歯正常", $extra);
    //全データクリア
    $extra = "onclick=\"return dataChange('CLEAR');\"";
    $arg["button"]["btn_dataClear"] = knjCreateBtn($objForm, "btn_dataClear", "データクリア", $extra);

    //一括更新ボタン
    $link = REQUESTROOT."/F/KNJF020J/knjf020jindex.php?cmd=replace&SCHREGNO=".$model->schregno;
    $extra = "onclick=\"Page_jumper('$link');\"";
    $arg["button"]["btn_replace"] = knjCreateBtn($objForm, "btn_replace", "一括更新", $extra);

    //更新ボタン
    $extra = "onmousedown=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$entMove["btn_update"]);

    //更新後前の生徒へボタン
    $extra = "style=\"width:130px\" onmousedown=\"updateNextStudent('".$model->schregno."', 1);\" style=\"width:130px\"";
    $arg["button"]["btn_up_pre"] = KnjCreateBtn($objForm, "btn_up_pre", "更新後前の".$model->sch_label."へ", $extra.$entMove["btn_up_pre"]);
    //更新後次の生徒へボタン
    $extra = "style=\"width:130px\" onmousedown=\"updateNextStudent('".$model->schregno."', 0);\" style=\"width:130px\"";
    $arg["button"]["btn_up_next"] = KnjCreateBtn($objForm, "btn_up_next", "更新後次の".$model->sch_label."へ", $extra.$entMove["btn_up_next"]);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model, $RowH, $hiddenVal, $hiddenShow, $cnt)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "HIDDENDATE", $RowH["TOOTH_DATE"]);
    knjCreateHidden($objForm, "SISIKI_CLICK", $model->sisikiClick);
    knjCreateHidden($objForm, "SETVAL", $hiddenVal);
    knjCreateHidden($objForm, "SETSHOW", $hiddenShow);
    knjCreateHidden($objForm, "ENTMOVE_COUNTER", $cnt);
}

//歯列作成
function makeSiretu(&$objForm, &$arg, $dataArray, $toothInfo, $field, $setarg, $f550Array)
{
    $textAlign = "style=\"text-align:center\"";
    foreach ($dataArray as $key => $val) {
        $checked   = $toothInfo[$key] ? "1" : "";
        $toothVal  = $toothInfo[$key];
        $toothShow = $checked == "1" ? $f550Array[$toothInfo[$key]] : "";

        $extra = "readonly=\"readonly\" onClick=\"kirikae(this, '".$key."_ID')\" oncontextmenu=\"kirikae2(this, '".$key."_ID')\"; ";
        $setText[$field."_LABEL_NAME"] = $val;
        $setText[$field."_FORM_NAME"]  = knjCreateTextBox($objForm, $toothShow, $key, 2, 2, $textAlign.$extra);
        knjCreateHidden($objForm, $key."_FORM_ID", $toothVal);
        $arg[$setarg][] = $setText;
    }
}

//所見欄を無効にするcdの文字列
function getDisableCodes($db, $model, $setInNamecd1)
{
    $query = knjf020jQuery::getNameMstDisableCodes($model, $setInNamecd1);
    $disableCodes = array();
    $db = Query::dbCheckOut();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $disableCodes[] .= $row["NAMECD2"];
    }
    $result->free();

    return $disableCodes;
}
