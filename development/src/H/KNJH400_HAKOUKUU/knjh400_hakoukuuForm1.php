<?php

require_once('for_php7.php');

class knjh400_hakoukuuForm1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh400_hakoukuuindex.php", "", "edit");

        knjh400_hakoukuuForm1::mainYear($objForm, $model, $arg, $model->year);
        $data1 = $arg['data'];
        $data1['upAdult'] = $arg['upAdult'];
        $data1['lwAdult'] = $arg['lwAdult'];
        $data1['upBaby'] = $arg['upBaby'];
        $data1['lwBaby'] = $arg['lwBaby'];
        unset($arg['data']);
        unset($arg['upAdult']);
        unset($arg['lwAdult']);
        unset($arg['upBaby']);
        unset($arg['lwBaby']);

        knjh400_hakoukuuForm1::mainYear($objForm, $model, $arg, $model->year - 1);
        $data2 = $arg['data'];
        $data2['upAdult'] = $arg['upAdult'];
        $data2['lwAdult'] = $arg['lwAdult'];
        $data2['upBaby'] = $arg['upBaby'];
        $data2['lwBaby'] = $arg['lwBaby'];
        unset($arg['data']);
        unset($arg['upAdult']);
        unset($arg['lwAdult']);
        unset($arg['upBaby']);
        unset($arg['lwBaby']);

        knjh400_hakoukuuForm1::mainYear($objForm, $model, $arg, $model->year - 2);
        $data3 = $arg['data'];
        $data3['upAdult'] = $arg['upAdult'];
        $data3['lwAdult'] = $arg['lwAdult'];
        $data3['upBaby'] = $arg['upBaby'];
        $data3['lwBaby'] = $arg['lwBaby'];
        unset($arg['data']);
        unset($arg['upAdult']);
        unset($arg['lwAdult']);
        unset($arg['upBaby']);
        unset($arg['lwBaby']);

        $arg['data'] = array($data1, $data2, $data3);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjh400_hakoukuuForm1.html", $arg);
    }
    public function mainYear(&$objForm, &$model, &$arg, $year)
    {
        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->schregno) && $model->cmd != "change") {
            $RowH = knjh400_hakoukuuQuery::getMedexamHdat($model, $year);     //生徒健康診断ヘッダデータ取得
            $RowT = knjh400_hakoukuuQuery::getMedexamToothDat($model, $year); //生徒健康診断歯口腔データ取得
            $arg["NOT_WARNING"] = 1;
        } else {
            $RowH =& $model->field;
            $RowT =& $model->field;
        }

        $db     = Query::dbCheckOut();

        /* ヘッダ */
        if (isset($model->schregno)) {
            //生徒学籍データを取得
            $query = knjh400_hakoukuuQuery::getSchregBaseMstData($model, $RowH['TOOTH_DATE']);
            $result = $db->query($query);
            $RowB = $result->fetchRow(DB_FETCHMODE_ASSOC);
            $result->free();
            //生徒学籍番号
            $arg["header"]["SCHREGNO"] = $model->schregno;
            //生徒名前
            $arg["header"]["NAME_SHOW"] = $db->getOne(knjh400_hakoukuuQuery::getName($model));
            //生徒生年月日
            $birth_day = explode("-", $RowB["BIRTHDAY"]);
            $arg["header"]["BIRTHDAY"] = $birth_day[0]."年".$birth_day[1]."月".$birth_day[2]."日";
            //生徒学年クラスを取得
            $result = $db->query(knjh400_hakoukuuQuery::getSchregRegdDatData($model));
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
        $arg["data"]["SPAN1"]   = ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") ? 3 : 2;
        $arg["data"]["SPAN2"]   = ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") ? 2 : 1;
        $arg["data"]["ROWSPAN"] = ($model->Properties["printKenkouSindanIppan"] == "3" || ($model->Properties["printKenkouSindanIppan"] == "1" && ($model->z010 === "mieken" || $model->KNJF030D))) ? 17 : 16;

        //Enterキーで移動する項目一覧
        $cnt = 0;
        $entMoveArray = array();
        $entMoveArray["TOOTH_DATE"]         = $cnt++;
        $entMoveArray["JAWS_JOINTCD2"]      = $cnt++;
        $entMoveArray["JAWS_JOINTCD"]       = $cnt++;
        if (($model->Properties["printKenkouSindanIppan"] == "1" && ($model->z010 === "mieken" || $model->KNJF030D))  || in_array($model->Properties["printKenkouSindanIppan"], array("2", "3"))) {
            $entMoveArray["JAWS_JOINTCD3"]      = $cnt++;
        }
        $entMoveArray["PLAQUECD"]           = $cnt++;
        $entMoveArray["GUMCD"]              = $cnt++;
        if ($model->Properties["printKenkouSindanIppan"] != "2") {
            if ($model->z010 != "miyagiken") {
                $entMoveArray["CALCULUS"]           = $cnt++;
            }
        }
        if ($model->z010 != "miyagiken") {
            $entMoveArray["ORTHODONTICS"]       = $cnt++;
        }
        $entMoveArray["btn_adultIns"]       = $cnt++;
        if ($model->Properties["printKenkouSindanIppan"] != "3") {
            $entMoveArray["OTHERDISEASECD"]     = $cnt++;
        }
        $entMoveArray["OTHERDISEASE"]       = $cnt++;
        if ($model->z010 == "miyagiken") {
            $entMoveArray["OTHERDISEASECD3"]    = $cnt++;
            $entMoveArray["OTHERDISEASECD4"]    = $cnt++;
        } elseif ($model->is_f020_otherdisese_hyouji2) {
            $entMoveArray["OTHERDISEASECD3"]    = $cnt++;
            $entMoveArray["OTHERDISEASE3"]      = $cnt++;
            $entMoveArray["OTHERDISEASECD4"]    = $cnt++;
            $entMoveArray["OTHERDISEASE4"]      = $cnt++;
        }
        if ($model->Properties["printKenkouSindanIppan"] != "2") {
            if ($model->z010 != "miyagiken") {
                $entMoveArray["OTHERDISEASECD2"]    = $cnt++;
            }
            $entMoveArray["OTHERDISEASE2"]      = $cnt++;
        }
        if ($model->is_f020_otherdisese_hyouji) {
            $entMoveArray["OTHERDISEASE_REMARK1"]      = $cnt++;
            $entMoveArray["OTHERDISEASE_REMARK2"]      = $cnt++;
            $entMoveArray["OTHERDISEASE_REMARK3"]      = $cnt++;
            $entMoveArray["OTHERDISEASE_REMARK4"]      = $cnt++;
        }
        if (in_array($model->Properties["printKenkouSindanIppan"], array("2", "3"))) {
            $entMoveArray["DENTISTREMARK_CO"]   = $cnt++;
            $entMoveArray["DENTISTREMARK_GO"]   = $cnt++;
            $entMoveArray["DENTISTREMARK_G"]    = $cnt++;
        } else {
            $entMoveArray["DENTISTREMARKCD"] = $cnt++;
            if ($model->z010 !== "mieken") {
                $entMoveArray["DENTISTREMARK"]   = $cnt++;
            }
        }
        if ($model->is_f020_dentistremark_hyouji) {
            $entMoveArray["DENTISTREMARK_REMARK1"]      = $cnt++;
            $entMoveArray["DENTISTREMARK_REMARK2"]      = $cnt++;
            $entMoveArray["DENTISTREMARK_REMARK3"]      = $cnt++;
            $entMoveArray["DENTISTREMARK_REMARK4"]      = $cnt++;
        }
        $entMoveArray["DENTISTREMARKDATE"]  = $cnt++;
        if ($model->z010 === "mieken") {
            $entMoveArray["DOC_NAME"] = $cnt++;
        }
        if ($model->Properties["printKenkouSindanIppan"] != "3") {
            $entMoveArray["DENTISTTREATCD"]     = $cnt++;
        }
        $entMoveArray["DENTISTTREAT"]       = $cnt++;
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $entMoveArray["DENTISTTREAT2"]      = $cnt++;
            $entMoveArray["DENTISTTREAT3"]      = $cnt++;
        }
        $entMoveArray["btn_update"]         = $cnt++;
        $entMoveArray["btn_up_pre"]         = $cnt++;
        $entMoveArray["btn_up_next"]        = $cnt++;

        $entMove= array();
        foreach ($entMoveArray as $key => $val) {
            $entMove[$key]  =  " id=\"ENTMOVE_{$val}\" onKeyDown=\"keyChangeEntToTab(this);\"";
        }

        if ($model->z010 == "miyagiken") {
            $arg["data"]["ROWSPAN"] = 14;
            $arg["is_miyagiken"]  = "1";
            $arg["not_miyagiken"] = "";
        } else {
            $arg["is_miyagiken"]  = "";
            $arg["not_miyagiken"] = "1";
        }

        if ($model->z010 === "mieken") {
            $arg["is_mie"] = "1";
            $arg["is_not_mie"] = "";
        } else {
            $arg["is_mie"] = "";
            $arg["is_not_mie"] = "1";
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
        $query = knjh400_hakoukuuQuery::getNameMstSpare2($model, $setInNamecd1);
        $nameSpare2 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $nameSpare2[$row["NAMECD1"]][] = $row["NAMECD2"];
        }
        $result->free();

        /* 編集項目 */
        //健康診断実施日付
        $RowH["TOOTH_DATE"] = str_replace("-", "/", $RowH["TOOTH_DATE"]);
        $extra = $entMove["TOOTH_DATE"];
        $arg["data"]["TOOTH_DATE"] = View::popUpCalendar2($objForm, "TOOTH_DATE", $RowH["TOOTH_DATE"], "", "", $extra);

        $arg['data']['YEAR'] = $year;
        $arg['data']['AGE'] = isset($RowB['AGE']) ? ($RowB['AGE'] . '歳') : '';

        //熊本改修用
        $kumaExtra = "";
        $nameCd2_Jcd = "F510";//歯列
        if ($model->z010 === "kumamoto") {
            $kumaExtra = " onChange=\"setCheckOn(this);\"";
            $nameCd2_Jcd = "F514";
            $arg["data"]["ROWSPAN"] = 15;
            if ("2016" > $model->year) {
                $nameCd2_Jcd = "F510";
                $arg["data"]["ROWSPAN"] = 16;
            }
            $minGrade = $db->getOne(knjh400_hakoukuuQuery::getMinGrade($model));
            $setGrade = ($model->year > 2022) ? 0: $model->year - 2016 + (int)$minGrade;
            if ($setGrade > 0 && $model->getGrade > sprintf("%02d", $setGrade)) {
                $nameCd2_Jcd = "F510";
                $arg["data"]["ROWSPAN"] = 16;
            }
            if ($model->getSchKind != "" && $model->getSchKind != "H") {
                $nameCd2_Jcd = "F510";
                $arg["data"]["ROWSPAN"] = 16;
            }
        }

        //歯列・咬合コンボボックス
        $query = knjh400_hakoukuuQuery::getNameMst($model, $nameCd2_Jcd);
        $extra = "style=\"width:250px;\"".$entMove["JAWS_JOINTCD"];
        makeCombo($objForm, $arg, $db, $query, $RowT["JAWS_JOINTCD"], "JAWS_JOINTCD", $extra, 1, "BLANK");

        if ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") {
            $arg["header"]["JAWS_JOINTCD_LABEL"] = '歯<br>列';
            $arg["kuma_tokiwa_miyagi"] = 1;
            $arg["Ippan".$model->Properties["printKenkouSindanIppan"]] = "1";//Ippan2:熊本、Ippan3:常磐、宮城

            $arg["kuma_Renovation"] = "1";
            if ($model->z010 === "kumamoto") {
                $arg["header"]["JAWS_JOINTCD_LABEL"] = '歯<br>列<br>・<br>咬<br>合';
                $arg["kuma_Renovation"] = "";
                if ("2016" > $model->year) {
                    $arg["header"]["JAWS_JOINTCD_LABEL"] = '歯<br>列';
                    $arg["kuma_Renovation"] = 1;
                }
                $minGrade = $db->getOne(knjh400_hakoukuuQuery::getMinGrade($model));
                $setGrade = ($model->year > 2022) ? 0: (int)$model->year - 2016 + (int)$minGrade;
                if ($setGrade > 0 && $model->getGrade > sprintf("%02d", $setGrade)) {
                    $arg["header"]["JAWS_JOINTCD_LABEL"] = '歯<br>列';
                    $arg["kuma_Renovation"] = 1;
                }
                if ($model->getSchKind != "" && $model->getSchKind != "H") {
                    $arg["header"]["JAWS_JOINTCD_LABEL"] = '歯<br>列';
                    $arg["kuma_Renovation"] = 1;
                }
            }
        } else {
            if ($model->Properties["printKenkouSindanIppan"] == "1" && ($model->z010 === "mieken" || $model->KNJF030D || $model->Properties["KenkouSindan_Ippan_Pattern"] == "1")) {
                $arg["header"]["JAWS_JOINTCD_LABEL"] = '歯<br>列';
                $arg["jaws_jointcd3_mie"] = 1;
            } else {
                $arg["header"]["JAWS_JOINTCD_LABEL"] = '歯<br>列<br>・<br>咬<br>合';
            }
            $arg["not_kuma_tokiwa_miyagi"] = 1;
        }

        if ($model->is_f020_otherdisese_hyouji) {
            $arg["F020_OTHERDISESE_HYOUJI"] = "1";
        } else {
            $arg["F020_OTHERDISESE_HYOUJI"] = "";
        }
        if ($model->is_f020_otherdisese_hyouji2) {
            $arg["F020_OTHERDISESE_HYOUJI2"] = "1";
        } else {
            $arg["F020_OTHERDISESE_HYOUJI2"] = "";
        }
        if ($model->is_f020_dentistremark_hyouji) {
            $arg["F020_DENTISTREMARK_HYOUJI"] = "1";
        } else {
            $arg["F020_DENTISTREMARK_HYOUJI"] = "";
        }

        //咬合コンボボックス
        if ($model->Properties["printKenkouSindanIppan"] == "1" && $model->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
            $arg["CHG_DISPORDER"] = 1;
            $arg["SIKOU_STRTYPE2"] = 1;
            $kougouNCd = "F510";
        } else {
            $arg["NO_CHG_DISPORDER"] = 1;
            $arg["SIKOU_STRTYPE1"] = 1;
            $kougouNCd = "F512";
        }
        $query = knjh400_hakoukuuQuery::getNameMst($model, $kougouNCd);
        $extra = "style=\"width:250px;\"".$entMove["JAWS_JOINTCD3"];
        makeCombo($objForm, $arg, $db, $query, $RowT["JAWS_JOINTCD3"], "JAWS_JOINTCD3", $extra, 1, "BLANK");

        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $arg["Ippan2"] = 1;
        } else {
            $arg["Ippan2Igai"] = 1;
        }
        //顎関節コンボボックス
        $nameCd1 = ($RowB["ENT_YEAR"] >= 2016) ? "F515" : "F511";
        $query = knjh400_hakoukuuQuery::getNameMst($model, $nameCd1);
        $extra = "style=\"width:250px;\"".$entMove["JAWS_JOINTCD2"];
        makeCombo($objForm, $arg, $db, $query, $RowT["JAWS_JOINTCD2"], "JAWS_JOINTCD2", $extra, 1, "BLANK");

        //歯垢の状態コンボ
        $nameCd2 = ($RowB["ENT_YEAR"] >= 2016) ? "F516" : "F520";
        $query = knjh400_hakoukuuQuery::getNameMst($model, $nameCd2);
        $extra = "style=\"width:150px;\"".$entMove["PLAQUECD"];
        makeCombo($objForm, $arg, $db, $query, $RowT["PLAQUECD"], "PLAQUECD", $extra, 1, "BLANK");

        //歯肉の状態コンボ
        $nameCd3 = ($RowB["ENT_YEAR"] >= 2016) ? "F517" : "F513";
        $query = knjh400_hakoukuuQuery::getNameMst($model, $nameCd3);
        $extra = "style=\"width:250px;\"".$entMove["GUMCD"].$kumaExtra;
        makeCombo($objForm, $arg, $db, $query, $RowT["GUMCD"], "GUMCD", $extra, 1, "BLANK");
        //歯石沈着コンボ
        $query = knjh400_hakoukuuQuery::getNameMst($model, "F521");
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
        $arg["data"]["ORTHODONTICS"] = $ari_nasi;

        //乳歯・現在数
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["BABYTOOTH"] =  $RowT["BABYTOOTH"];

        //乳歯・未処置数
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["REMAINBABYTOOTH"] =  $RowT["REMAINBABYTOOTH"];

        //乳歯・処置数
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["TREATEDBABYTOOTH"] =  $RowT["TREATEDBABYTOOTH"];

        //乳歯・要注意乳歯数
        $extra = ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") ? "style=\"text-align:right;background-color:darkgray\" readonly" : "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["BRACK_BABYTOOTH"] =  $RowT["BRACK_BABYTOOTH"];

        //永久歯・現在数
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["ADULTTOOTH"] =  $RowT["ADULTTOOTH"];

        //永久歯・未処置数
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["REMAINADULTTOOTH"] =  $RowT["REMAINADULTTOOTH"];

        //永久歯・処置数
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["TREATEDADULTTOOTH"] =  $RowT["TREATEDADULTTOOTH"];

        //永久歯・喪失数
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["LOSTADULTTOOTH"] =  $RowT["LOSTADULTTOOTH"];

        //永久歯・要観察歯数
        $extra = ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") ? "style=\"text-align:right;background-color:darkgray\" readonly" : "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["BRACK_ADULTTOOTH"] =  $RowT["BRACK_ADULTTOOTH"];

        //その他疾病及び異常コンボ
        $query = knjh400_hakoukuuQuery::getNameMst($model, "F530");
        $extra = "style=\"width:120px;\" onChange=\"OptionUse(this, 'OTHERDISEASE');\"".$entMove["OTHERDISEASECD"];
        makeCombo($objForm, $arg, $db, $query, $RowT["OTHERDISEASECD"], "OTHERDISEASECD", $extra, 1, "BLANK");

        //その他疾病及び異常テキスト
        $extra = ($RowT["OTHERDISEASECD"] == '99' || $model->Properties["printKenkouSindanIppan"] == "3") ? "" : "disabled style=\"background-color:darkgray\"";
        $arg["data"]["OTHERDISEASE"] =  $RowT["OTHERDISEASE"];

        //その他疾病及び異常コンボ２
        $query = knjh400_hakoukuuQuery::getNameMst($model, "F530");
        $extra = "style=\"width:120px;\"".$entMove["OTHERDISEASECD3"];
        if ($model->is_f020_otherdisese_hyouji2) {
            $extra .= "  onChange=\"OptionUse(this, 'OTHERDISEASE3');\"";
        }
        makeCombo($objForm, $arg, $db, $query, $RowT["OTHERDISEASECD3"], "OTHERDISEASECD3", $extra, 1, "BLANK");

        //その他疾病及び異常コンボ３
        $query = knjh400_hakoukuuQuery::getNameMst($model, "F530");
        $extra = "style=\"width:120px;\"".$entMove["OTHERDISEASECD4"];
        if ($model->is_f020_otherdisese_hyouji2) {
            $extra .= "  onChange=\"OptionUse(this, 'OTHERDISEASE4');\"";
        }
        makeCombo($objForm, $arg, $db, $query, $RowT["OTHERDISEASECD4"], "OTHERDISEASECD4", $extra, 1, "BLANK");

        if ($model->is_f020_otherdisese_hyouji2) {
            //その他の疾病及び異常テキスト 2行目
            $extra = ($RowT["OTHERDISEASECD3"] == '99' || $model->Properties["printKenkouSindanIppan"] == "3") ? "" : "disabled style=\"background-color:darkgray\"";
            $arg["data"]["OTHERDISEASE3"] =  $RowT["OTHERDISEASE3"];

            //その他の疾病及び異常テキスト 3行目
            $extra = ($RowT["OTHERDISEASECD4"] == '99' || $model->Properties["printKenkouSindanIppan"] == "3") ? "" : "disabled style=\"background-color:darkgray\"";
            $arg["data"]["OTHERDISEASE4"] =  $RowT["OTHERDISEASE4"];
        }

        //口腔の疾病及び異常コンボ
        $query = knjh400_hakoukuuQuery::getNameMst($model, "F531");
        $extra = "style=\"width:120px;\"".$entMove["OTHERDISEASECD2"];
        makeCombo($objForm, $arg, $db, $query, $RowT["OTHERDISEASECD2"], "OTHERDISEASECD2", $extra, 1, "BLANK");

        //口腔の疾病及び異常テキスト
        $extra = $entMove["OTHERDISEASE2"];
        $arg["data"]["OTHERDISEASE2"] =  $RowT["OTHERDISEASE2"];

        if ($model->is_f020_otherdisese_hyouji) {
            //口腔の疾病及び異常コンボ 2行目
            $query = knjh400_hakoukuuQuery::getNameMst($model, "F531");
            $extra = "style=\"width:120px;\"".$entMove["OTHERDISEASE_REMARK1"];
            makeCombo($objForm, $arg, $db, $query, $RowT["OTHERDISEASE_REMARK1"], "OTHERDISEASE_REMARK1", $extra, 1, "BLANK");

            //口腔の疾病及び異常テキスト 2行目
            $extra = $entMove["OTHERDISEASE_REMARK2"];
            $arg["data"]["OTHERDISEASE_REMARK2"] =  $RowT["OTHERDISEASE_REMARK2"];

            //口腔の疾病及び異常コンボ 3行目
            $query = knjh400_hakoukuuQuery::getNameMst($model, "F531");
            $extra = "style=\"width:120px;\"".$entMove["OTHERDISEASE_REMARK3"];
            makeCombo($objForm, $arg, $db, $query, $RowT["OTHERDISEASE_REMARK3"], "OTHERDISEASE_REMARK3", $extra, 1, "BLANK");

            //口腔の疾病及び異常テキスト 3行目
            $extra = $entMove["OTHERDISEASE_REMARK4"];
            $arg["data"]["OTHERDISEASE_REMARK4"] =  $RowT["OTHERDISEASE_REMARK4"];
        }

        //所見コンボボックス
        $query = knjh400_hakoukuuQuery::getNameMst($model, "F540");
        if ($arg["is_not_mie"] == "1") {
            $extra = "style=\"width:150px;\" onChange=\"syokenNyuryoku(this, document.forms[0].DENTISTREMARK, ['".join("','", $nameSpare2["F540"])."'])\"".$entMove["DENTISTREMARKCD"];
        } else {
            $extra = "style=\"width:150px;\"".$entMove["DENTISTREMARKCD"];
        }
        makeCombo($objForm, $arg, $db, $query, $RowT["DENTISTREMARKCD"], "DENTISTREMARKCD", $extra, 1, "BLANK");
        if ($arg["is_not_mie"] == "1") {
            //所見テキスト
            if (!in_array($RowT["DENTISTREMARKCD"], $nameSpare2["F540"])) {
                $extra = "disabled";
            } else {
                $extra = "";
            }
            $arg["data"]["DENTISTREMARK"] =  $RowT["DENTISTREMARK"];
        }

        if ($model->is_f020_dentistremark_hyouji) {
            //所見コンボボックス 2行目
            $query = knjh400_hakoukuuQuery::getNameMst($model, "F540");
            $extra = "style=\"width:150px;\" onChange=\"syokenNyuryoku(this, document.forms[0].DENTISTREMARK_REMARK2, ['".join("','", $nameSpare2["F540"])."'])\"".$entMove["DENTISTREMARK_REMARK1"];
            makeCombo($objForm, $arg, $db, $query, $RowT["DENTISTREMARK_REMARK1"], "DENTISTREMARK_REMARK1", $extra, 1, "BLANK");

            //所見テキスト 2行目
            if (!in_array($RowT["DENTISTREMARK_REMARK1"], $nameSpare2["F540"])) {
                $extra = "disabled";
            } else {
                $extra = "";
            }
            $arg["data"]["DENTISTREMARK_REMARK2"] =  $RowT["DENTISTREMARK_REMARK2"];

            //所見コンボボックス 3行目
            $query = knjh400_hakoukuuQuery::getNameMst($model, "F540");
            $extra = "style=\"width:150px;\" onChange=\"syokenNyuryoku(this, document.forms[0].DENTISTREMARK_REMARK4, ['".join("','", $nameSpare2["F540"])."'])\"".$entMove["DENTISTREMARK_REMARK2"];
            makeCombo($objForm, $arg, $db, $query, $RowT["DENTISTREMARK_REMARK3"], "DENTISTREMARK_REMARK3", $extra, 1, "BLANK");

            //所見テキスト 3行目
            if (!in_array($RowT["DENTISTREMARK_REMARK3"], $nameSpare2["F540"])) {
                $extra = "disabled";
            } else {
                $extra = "";
            }
            $arg["data"]["DENTISTREMARK_REMARK4"] =  $RowT["DENTISTREMARK_REMARK4"];
        }

        //所見(CO)本数
        $extra = ($model->Properties["printKenkouSindanIppan"] == "2") ? "style=\"text-align:right;background-color:darkgray\" readonly" : "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["DENTISTREMARK_CO"] =  $RowT["DENTISTREMARK_CO"];

        //所見(GO)チェックボックス
        if ($RowT["DENTISTREMARK_GO"] == 1) {
            $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi_go');\" checked='checked'";
            $ari_nasi_go = "<span id='ari_nasi_go' style='color:black;'>有</span>";
        } else {
            $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi_go');\"";
            $ari_nasi_go = "<span id='ari_nasi_go' style='color:black;'>無</span>";
        }
        $arg["data"]["DENTISTREMARK_GO"] = knjCreateCheckBox($objForm, "DENTISTREMARK_GO", '1', $extra.$entMove["DENTISTREMARK_GO"]).$ari_nasi_go;

        //所見(G)チェックボックス
        if ($RowT["DENTISTREMARK_G"] == 1) {
            $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi_g');\" checked='checked'";
            $ari_nasi_g = "<span id='ari_nasi_g' style='color:black;'>有</span>";
        } else {
            $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi_g');\"";
            $ari_nasi_g = "<span id='ari_nasi_g' style='color:black;'>無</span>";
        }
        $arg["data"]["DENTISTREMARK_G"] = knjCreateCheckBox($objForm, "DENTISTREMARK_G", '1', $extra.$entMove["DENTISTREMARK_G"]).$ari_nasi_g;

        //所見日付
        $RowT["DENTISTREMARKDATE"] = str_replace("-", "/", $RowT["DENTISTREMARKDATE"]);
        $arg["data"]["DENTISTREMARKDATE"] = $RowT["DENTISTREMARKDATE"];

        if ($model->z010 === "mieken") {
            //学校医コンボボックス(三重のみ)
            $extra = "";
            $arg["data"]["DOC_NAME"] =  $RowT["DOC_NAME"];
        }

        //事後措置コンボ
        $query = knjh400_hakoukuuQuery::getNameMst($model, "F541");
        if ($arg["is_mie"] == "1") {
            $extra = "style=\"width:250px;\"".$entMove["DENTISTTREATCD"];
        } else {
            $extra = "style=\"width:250px;\" onChange=\"syokenNyuryoku(this, document.forms[0].DENTISTTREAT, ['".join("','", $nameSpare2["F541"])."'])\"".$entMove["DENTISTTREATCD"];
        }
        makeCombo($objForm, $arg, $db, $query, $RowT["DENTISTTREATCD"], "DENTISTTREATCD", $extra, 1, "BLANK");

        //事後措置テキスト
        if ($arg["is_mie"] != "1") {
            $extra = $entMove["DENTISTTREAT"];
            if ($model->Properties["printKenkouSindanIppan"] == "2") {
                $arg["data"]["DENTISTTREAT"] =  $RowT["DENTISTTREAT"];
            } else {
                if (!in_array($RowT["DENTISTTREATCD"], $nameSpare2["F541"])) {
                    $extra .= "disabled";
                } else {
                    $extra .= "";
                }
                $arg["data"]["DENTISTTREAT"] =  $RowT["DENTISTTREAT"];
            }
        }

        //事後措置テキスト2
        $extra = $entMove["DENTISTTREAT2"];
        $arg["data"]["DENTISTTREAT2"] =  $RowT["DENTISTTREAT2"];

        //事後措置テキスト3
        $extra = $entMove["DENTISTTREAT3"];
        $arg["data"]["DENTISTTREAT3"] =  $RowT["DENTISTTREAT3"];


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
        $sql = knjh400_hakoukuuQuery::getNameMst($model, "F550");
        $result = $db->query($sql);
        $hiddenVal = "";
        $hiddenShow = "";
        $sep = "";
        $f550Array = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $cd = (int) $row["VALUE"];
            $arg["data"]["SHOW_DIV".$cd] = $row["LABEL2"];

            $f550Array[$row["VALUE"]] = $row["SHOW"];
            $hiddenVal .= $sep.$row["VALUE"];
            $hiddenShow .= $sep.$row["SHOW"];
            $sep = ",";
        }

        $opt_data = array();
        $extra = array();
        foreach ($f550Array as $key => $val) {
            $cd = (int) $key;
            $opt_data[] = $cd;
            $extra[] = "id=\"TYPE_DIV{$cd}\"";
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

        $sql = knjh400_hakoukuuQuery::getNameMst($model, "F550");
        $dataArray = array();
        $result = $db->query($sql);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataArray[] = array("VAL"  => "\"javascript:setClickValue('".$row["VALUE"]."')\"",
                                 "NAME" => $row["LABEL2"]);
        }
        $arg["menuTitle"]["CLICK_NAME"] = knjCreateBtn($objForm, "btn_end", "×", "onclick=\"return setClickValue('999');\"");
        $arg["menuTitle"]["CLICK_VAL"] = "javascript:setClickValue('999')";
        foreach ($dataArray as $key => $val) {
            $setData["CLICK_NAME"] = $val["NAME"];
            $setData["CLICK_VAL"] = $val["VAL"];
            $arg["menu"][] = $setData;
        }
        $result->free();


        //ボタン作成
        makeBtn($objForm, $arg, $model, $entMove);

        //hidden
        makeHidden($objForm, $model, $RowH, $hiddenVal, $hiddenShow, $cnt);

        Query::dbCheckIn($db);

        if (get_count($model->warning) == 0 && $model->cmd != "reset") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd =="reset") {
            $arg["next"] = "NextStudent(1);";
        }

        if ($model->sisikiClick == "ON") {
            $arg["next"] = "btn_submit('edit2');";
        }
        if ($model->cmd == "edit2") {
            $arg["next"] = "sisikiClick();";
        }
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
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
            $valueLabel = $row["LABEL"];
        }
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = $valueLabel;
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
    $link = REQUESTROOT."/F/KNJF020/knjh400_hakoukuuindex.php?cmd=replace&SCHREGNO=".$model->schregno;
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

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '戻 る', $extra);
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
    knjCreateHidden($objForm, "IS_Z010", $model->z010);//javascriptで使用
}

//歯列作成
function makeSiretu(&$objForm, &$arg, $dataArray, $toothInfo, $field, $setarg, $f550Array)
{
    $textAlign = "style=\"text-align:center\"";
    foreach ($dataArray as $key => $val) {
        $checked = $toothInfo[$key] ? "1" : "";
        $toothVal = $toothInfo[$key];
        $toothShow = $checked == "1" ? $f550Array[$toothInfo[$key]] : "";

        $extra = "readonly=\"readonly\" onClick=\"kirikae(this, '".$key."_ID')\" oncontextmenu=\"kirikae2(this, '".$key."_ID')\"; ";
        $setText[$field."_LABEL_NAME"] = $val;
        $setText[$field."_FORM_NAME"] =  $toothShow;
        knjCreateHidden($objForm, $key."_FORM_ID", $toothVal);
        $arg[$setarg][] = $setText;
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
