<?php

require_once('for_php7.php');

class knjf020SubForm1
{
    public function main(&$model)
    {
        $objForm        = new form();
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjf020index.php", "", "sel");
        $arg["jscript"] = "";

        //生徒一覧
        $opt_left = $opt_right = array();
        //置換処理選択時の生徒の情報
        $array = explode(",", $model->replace_data["selectdata"]);
        if ($array[0]=="") {
            $array[0] = $model->schregno;
        }
        //生徒情報
        $RowH = knjf020Query::getMedexamHdat($model);      //生徒健康診断ヘッダデータ取得
        $RowT = knjf020Query::getMedexamToothDat($model); //生徒健康診断歯口腔データ取得

        $db = Query::dbCheckOut();

        $result   = $db->query(knjf020Query::getStudent($model));
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

        //レイアウト調整用
        $arg["data"]["SPAN1"] = ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") ? 3 : 2;
        $arg["data"]["SPAN2"] = ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") ? 2 : 1;

        if ($model->z010 == "miyagiken") {
            $arg["not_miyagiken"] = "";
        } else {
            $arg["not_miyagiken"] = "1";
        }

        if ($model->z010 == "mieken") {
            $arg["is_mie"] = "1";
            $arg["is_not_mie"] = "";
            $arg["data"]["SPAN3"] = 4;
        } else {
            $arg["is_mie"] = "";
            $arg["is_not_mie"] = "1";
            $arg["data"]["SPAN3"] = 3;
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
        $query = knjf020Query::getNameMstSpare2($model, $setInNamecd1);
        $nameSpare2 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $nameSpare2[$row["NAMECD1"]][] = $row["NAMECD2"];
        }
        $result->free();

        /* 編集項目 */
        //チェックボックス
        for ($i = 0; $i < 19; $i++) {
            if ($i==17) {
                $objForm->ae(array("type"       => "checkbox",
                                    "name"      => "RCHECK".$i,
                                    "value"     => "1",
                                    "checked"   => (($model->replace_data["check"][8] == "1") ? 1 : 0),
                                    "extrahtml" => "onClick=\"return check_all(this);\""));
                $arg["data"]["RCHECK".$i] = $objForm->ge("RCHECK".$i);
            } else {
                $objForm->ae(array("type"       => "checkbox",
                                   "name"       => "RCHECK".$i,
                                   "value"      => "1",
                                   "checked"    => (($model->replace_data["check"][$i] == "1") ? 1 : 0)));
                $arg["data"]["RCHECK".$i] = $objForm->ge("RCHECK".$i);
            }
        }
        //健康診断実施日付
        if ($RowH["TOOTH_DATE"] == "") {
            $RowH["TOOTH_DATE"] = CTRL_DATE;
        }
        $RowH["TOOTH_DATE"] = str_replace("-", "/", $RowH["TOOTH_DATE"]);
        $arg["data"]["TOOTH_DATE"] = View::popUpCalendar($objForm, "TOOTH_DATE", $RowH["TOOTH_DATE"]);

        //熊本改修用
        $kumaExtra = "";
        $nameCd2_Jcd  = "F510";//歯列
        if ($model->z010 === "kumamoto") {
            $kumaExtra = " onChange=\"setCheckOn(this);\"";
            $nameCd2_Jcd  = "F514";
            if ("2016" > $model->year) {
                $nameCd2_Jcd  = "F510";
            }
            $minGrade = $db->getOne(knjf020Query::getMinGrade($model));
            $setGrade = ($model->year > 2022) ? 0: (int)$model->year - 2016 + (int)$minGrade;
            if ($setGrade > 0 && $model->getGrade > sprintf("%02d", $setGrade)) {
                $nameCd2_Jcd  = "F510";
            }
        }

        //歯列・咬合コンボボックス
        $query = knjf020Query::getNameMst($model, $nameCd2_Jcd);
        $extra = "style=\"width:250px;\"";
        makeCombo($objForm, $arg, $db, $query, $RowT["JAWS_JOINTCD"], "JAWS_JOINTCD", $extra, 1, "BLANK");

        if ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") {
            $arg["data"]["JAWS_JOINTCD_LABEL"] = '歯列';
            $arg["kuma_tokiwa_miyagi"] = 1;
            $arg["Ippan".$model->Properties["printKenkouSindanIppan"]] = "1";

            $arg["kuma_Renovation"] = "1";
            if ($model->z010 === "kumamoto") {
                $arg["data"]["JAWS_JOINTCD_LABEL"] = '歯列・咬合';
                $arg["kuma_Renovation"] = "";
                if ("2016" > $model->year) {
                    $arg["data"]["JAWS_JOINTCD_LABEL"] = '歯列';
                    $arg["kuma_Renovation"] = 1;
                }
                $minGrade = $db->getOne(knjf020Query::getMinGrade($model));
                $setGrade = ($model->year > 2022) ? 0: (int)$model->year - 2016 + (int)$minGrade;
                if ($setGrade > 0 && $model->getGrade > sprintf("%02d", $setGrade)) {
                    $arg["data"]["JAWS_JOINTCD_LABEL"] = '歯列';
                    $arg["kuma_Renovation"] = 1;
                }
                if ($model->getSchKind != "" && $model->getSchKind != "H") {
                    $arg["data"]["JAWS_JOINTCD_LABEL"] = '歯列';
                    $arg["kuma_Renovation"] = 1;
                }
            }
        } else {
            if ($model->Properties["printKenkouSindanIppan"] == "1" && ($model->z010 === "mieken" || $model->KNJF030D || $model->Properties["KenkouSindan_Ippan_Pattern"] == "1")) {
                $arg["data"]["JAWS_JOINTCD_LABEL"] = '歯列';
                $arg["jaws_jointcd3_mie"] = 1;
            } else {
                $arg["data"]["JAWS_JOINTCD_LABEL"] = '歯列・咬合';
            }
            $arg["not_kuma_tokiwa_miyagi"] = 1;
        }

        //咬合コンボボックス
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
        $query = knjf020Query::getNameMst($model, $kougouNCd);
        $extra = "style=\"width:250px;\"";
        makeCombo($objForm, $arg, $db, $query, $RowT["JAWS_JOINTCD3"], "JAWS_JOINTCD3", $extra, 1, "BLANK");

        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $arg["Ippan2"] = 1;
        } else {
            $arg["Ippan2Igai"] = 1;
        }

        //顎関節コンボボックス
        $nameCd1 = ($row["ENT_YEAR"] >= 2016) ? "F515" : "F511";
        $query = knjf020Query::getNameMst($model, $nameCd1);
        $extra = "style=\"width:250px;\"";
        makeCombo($objForm, $arg, $db, $query, $RowT["JAWS_JOINTCD2"], "JAWS_JOINTCD2", $extra, 1, "BLANK");

        //歯垢の状態コンボ
        $nameCd2 = ($row["ENT_YEAR"] >= 2016) ? "F516" : "F520";
        $query = knjf020Query::getNameMst($model, $nameCd2);
        $extra = "style=\"width:250px;\"";
        makeCombo($objForm, $arg, $db, $query, $RowT["PLAQUECD"], "PLAQUECD", $extra, 1, "BLANK");

        //歯肉の状態コンボ
        $nameCd3 = ($row["ENT_YEAR"] >= 2016) ? "F517" : "F513";
        $query = knjf020Query::getNameMst($model, "$nameCd3");
        $extra = "style=\"width:250px;\"".$kumaExtra;
        makeCombo($objForm, $arg, $db, $query, $RowT["GUMCD"], "GUMCD", $extra, 1, "BLANK");

        //歯肉の状態コンボ
        $query = knjf020Query::getNameMst($model, "F521");
        $extra = "style=\"width:250px;\"";
        makeCombo($objForm, $arg, $db, $query, $RowT["CALCULUS"], "CALCULUS", $extra, 1, "BLANK");

        //矯正
        if ($RowT["ORTHODONTICS"] == 1) {
            $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi');\" checked='checked'";
            $ari_nasi = "<span id='ari_nasi' style='color:black;'>有</span>";
        } else {
            $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi');\"";
            $ari_nasi = "<span id='ari_nasi' style='color:black;'>無</span>";
        }
        $arg["data"]["ORTHODONTICS"] = knjCreateCheckBox($objForm, "ORTHODONTICS", 1, $extra).$ari_nasi;

        //その他疾病及び異常コンボ
        $query = knjf020Query::getNameMst($model, "F530");
        $extra = "style=\"width:250px;\" onclick=\"OptionUse(this);\"";
        makeCombo($objForm, $arg, $db, $query, $RowT["OTHERDISEASECD"], "OTHERDISEASECD", $extra, 1, "BLANK");

        //その他疾病及び異常テキスト
        $extra = ($RowT["OTHERDISEASECD"] == '99' || $model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") ? "" : "disabled style=\"background-color:darkgray\"";
        $arg["data"]["OTHERDISEASE"] = knjCreateTextBox($objForm, $RowT["OTHERDISEASE"], "OTHERDISEASE", 40, 60, $extra);

        //所見コンボボックス
        $query = knjf020Query::getNameMst($model, "F540");
        if ($arg["is_not_mie"] == "1") {
            $extra = "style=\"width:250px;\" onChange=\"syokenNyuryoku(this, document.forms[0].DENTISTREMARK, ['".join("','", $nameSpare2["F540"])."'])\"";
        } else {
            $extra = "style=\"width:250px;\"";
        }
        makeCombo($objForm, $arg, $db, $query, $RowT["DENTISTREMARKCD"], "DENTISTREMARKCD", $extra, 1, "BLANK");
        if ($arg["is_not_mie"] == "1") {
            //所見テキスト
            if (!in_array($RowT["DENTISTREMARKCD"], $nameSpare2["F540"])) {
                $extra = "disabled";
            } else {
                $extra = "";
            }
            $arg["data"]["DENTISTREMARK"] = knjCreateTextBox($objForm, $RowT["DENTISTREMARK"], "DENTISTREMARK", 40, 20, $extra);
        }

        //所見(CO)本数
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["DENTISTREMARK_CO"] = knjCreateTextBox($objForm, $RowT["DENTISTREMARK_CO"], "DENTISTREMARK_CO", 2, 2, $extra);

        //所見(GO)チェックボックス
        if ($RowT["DENTISTREMARK_GO"] == 1) {
            $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi_go');\" checked='checked'";
            $ari_nasi_go = "<span id='ari_nasi_go' style='color:black;'>有</span>";
        } else {
            $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi_go');\"";
            $ari_nasi_go = "<span id='ari_nasi_go' style='color:black;'>無</span>";
        }
        $arg["data"]["DENTISTREMARK_GO"] = knjCreateCheckBox($objForm, "DENTISTREMARK_GO", '1', $extra).$ari_nasi_go;

        //所見(G)チェックボックス
        if ($RowT["DENTISTREMARK_G"] == 1) {
            $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi_g');\" checked='checked'";
            $ari_nasi_g = "<span id='ari_nasi_g' style='color:black;'>有</span>";
        } else {
            $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi_g');\"";
            $ari_nasi_g = "<span id='ari_nasi_g' style='color:black;'>無</span>";
        }
        $arg["data"]["DENTISTREMARK_G"] = knjCreateCheckBox($objForm, "DENTISTREMARK_G", '1', $extra).$ari_nasi_g;

        //所見日付
        $RowT["DENTISTREMARKDATE"] = str_replace("-", "/", $RowT["DENTISTREMARKDATE"]);
        $arg["data"]["DENTISTREMARKDATE"] = View::popUpCalendar($objForm, "DENTISTREMARKDATE", $RowT["DENTISTREMARKDATE"]);

        //学校医
        if ($model->z010 === "mieken") {
            //学校医コンボボックス(三重のみ)
            $extra = "";
            $arg["data"]["DOC_NAME"] = knjCreateTextBox($objForm, $RowT["DOC_NAME"], "DOC_NAME", 20, 10, $extra);
        }

        //事後措置コンボボックス
        $query = knjf020Query::getNameMst($model, "F541");
        if ($arg["is_not_mie"] == "1") {
            $extra = "style=\"width:250px;\" onChange=\"syokenNyuryoku(this, document.forms[0].DENTISTTREAT, ['".join("','", $nameSpare2["F541"])."'])\"";
        } else {
            $extra = "style=\"width:250px;\"";
        }
        makeCombo($objForm, $arg, $db, $query, $RowT["DENTISTTREATCD"], "DENTISTTREATCD", $extra, 1, "BLANK");

        if ($arg["is_not_mie"] == "1") {
            //事後措置テキスト
            $extra =  "";
            if ($model->Properties["printKenkouSindanIppan"] == "2") {
                $arg["data"]["DENTISTTREAT"] = knjCreateTextBox($objForm, $RowT["DENTISTTREAT"], "DENTISTTREAT", 20, 30, $extra);
            } else {
                if (!in_array($RowT["DENTISTTREATCD"], $nameSpare2["F541"])) {
                    $extra .= "disabled";
                } else {
                    $extra .= "";
                }
                $arg["data"]["DENTISTTREAT"] = knjCreateTextBox($objForm, $RowT["DENTISTTREAT"], "DENTISTTREAT", 40, 20, $extra);
            }
        }

        //事後措置テキスト2
        $extra =  "";
        $arg["data"]["DENTISTTREAT2"] = knjCreateTextBox($objForm, $RowT["DENTISTTREAT2"], "DENTISTTREAT2", 20, 30, $extra);

        //事後措置テキスト3
        $extra =  "";
        $arg["data"]["DENTISTTREAT3"] = knjCreateTextBox($objForm, $RowT["DENTISTTREAT3"], "DENTISTTREAT3", 20, 30, $extra);

        //対象者一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','left_select','right_select',1)\" ";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "left_select", "left", $opt_left, $extra, 20);

        //生徒一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','left_select','right_select',1)\" ";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "right_select", "left", $opt_right, $extra, 20);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $RowH);

        /* ヘッダ */
        $arg["info"]    = array("TOP"        =>  $model->year."年度  "
                                                .$model->control_data["学期名"][$model->semester]
                                                ."  対象クラス  ".$model->Hrname,
                                "LEFT_LIST"  => "対象者一覧",
                                "RIGHT_LIST" => $model->sch_label."一覧");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf020SubForm1.html", $arg);
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
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //更新・戻るボタン
    $extra = "onclick=\"return doSubmit()\"";
    //更新
    $arg["BUTTONS"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //戻るボタン
    $link = REQUESTROOT."/F/KNJF020/knjf020index.php?cmd=back&ini2=1";
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["BUTTONS"] .= "    ".knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

    //全て追加
    $extra = "onclick=\"return move('sel_add_all','left_select','right_select',1);\"";
    $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
    //追加
    $extra = "onclick=\"return move('left','left_select','right_select',1);\"";
    $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
    //削除
    $extra = "onclick=\"return move('right','left_select','right_select',1);\"";
    $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    //全て削除
    $extra = "onclick=\"return move('sel_del_all','left_select','right_select',1);\"";
    $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $RowH)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "REPLACEHIDDENDATE", $RowH["TOOTH_DATE"]);
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
