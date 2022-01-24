<?php

require_once('for_php7.php');

class knjf010jSubForm3
{
    public function main(&$model)
    {
        $objForm        = new form();
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjf010jindex.php", "", "sel");

        if ($model->isMie) {
            $arg["is_mie"] = "1";
            $arg["is_not_mie"] = "";
        } else {
            $arg["is_mie"] = "";
            $arg["is_not_mie"] = "1";
        }

        //生徒一覧
        $opt_left = $opt_right = array();
        //置換処理選択時の生徒の情報
        $array = explode(",", $model->replace_data["selectdata"]);
        if ($array[0] == "") {
            $array[0] = $model->schregno;
        }
        //生徒情報
        $RowH = knjf010jQuery::getMedexamHdat($model);   //生徒健康診断ヘッダデータ取得
        $RowD = knjf010jQuery::getMedexamDetDat($model); //生徒健康診断詳細データ取得

        $db = Query::dbCheckOut();

        $result   = $db->query(knjf010jQuery::getStudent($model));
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

        //レイアウトの切り替え
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            $arg["new"] = 1;
        } elseif ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") {
            $arg["new2"] = 1;
            $arg["Ippan".$model->Properties["printKenkouSindanIppan"]] = "1";
        } else {
            $arg["base"] = 1;
        }

        //寄生虫卵表示
        if (($model->school_kind == "P" && $model->Properties["useParasite_P"] == "1") || ($model->school_kind == "J" && $model->Properties["useParasite_J"] == "1") || ($model->school_kind == "H" && $model->Properties["useParasite_H"] == "1")) {
            $arg["para"] = 1;
            $check_cnt1 = 13;
            $check_cnt2 = 14;
        } else {
            $check_cnt1 = 12;
            $check_cnt2 = 13;
        }

        //尿)その他の検査テキスト、指導区分コンボ
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            $check_cnt2 += ($arg["para"] == 1) ? 2 : 3;
        }

        //福井（貧血）
        if ($model->isFukui) {
            $arg["is_fukui"] = "1";
            $check_cnt2 += 1;
        }
        if ($model->isMie) {
            $arg["is_mie"] = "1";
            $arg["rowspan1"] = 3;
            //学校医のチェックボックスが17なので、そこまでの不足分を加算
            $check_cnt2 += 2;
        } else {
            $arg["rowspan1"] = 2;
        }

        //結核の間接撮影、所見、その他検査 非表示
        if ($model->school_kind == "H") {
            $arg["tbFilmShow"] = 1;
        } else {
            $arg["tbFilmUnShow"] = 1;
        }

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

        //チェックボックス
        for ($i = 0; $i < 18; $i++) {
            if ($i == 1) {
                $extra = "onClick=\"return check_all(this);\"";
                if ($model->replace_data["check"][$i] == "1") {
                    $extra .= "checked = 'checked'";
                } else {
                    $extra .= "";
                }
                $arg["data"]["RCHECK".$i] = knjCreateCheckBox($objForm, "RCHECK".$i, "1", $extra);
                knjCreateHidden($objForm, "CHECK_ALL", "RCHECK1");
            } else {
                $extra = "id=\"RCHECK{$i}\"";
                if ($model->replace_data["check"][$i] == "1") {
                    $extra .= "checked = 'checked'";
                } else {
                    $extra .= "";
                }
                $arg["data"]["RCHECK".$i] = knjCreateCheckBox($objForm, "RCHECK".$i, "1", $extra);
            }
        }
        //結核・所見コンボ
        $result     = $db->query(knjf010jQuery::getTbRemark($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $extra = "style=\"width:260px;\"";
        $arg["data"]["TB_REMARKCD"] = knjCreateCombo($objForm, "TB_REMARKCD", $RowD["TB_REMARKCD"], $opt, $extra, "1");

        //結核検査(X線)
        $extra = "";
        $arg["data"]["TB_X_RAY"] = knjCreateTextBox($objForm, $RowD["TB_X_RAY"], "TB_X_RAY", 40, 20, $extra);

        //結核・その他検査コンボ
        $result     = $db->query(knjf010jQuery::getTbOthertest($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $extra = "style=\"width:260px;\"";
        $arg["data"]["TB_OTHERTESTCD"] = knjCreateCombo($objForm, "TB_OTHERTESTCD", $RowD["TB_OTHERTESTCD"], $opt, $extra, "1");

        //結核・病名コンボ
        $result     = $db->query(knjf010jQuery::getTbName($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        if ($model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "style=\"width:260px;\" onChange=\"syokenNyuryoku(this, document.forms[0].TB_NAME_REMARK1)\"";
        } else {
            $extra = "style=\"width:260px;\"";
        }
        $arg["data"]["TB_NAMECD"] = knjCreateCombo($objForm, "TB_NAMECD", $RowD["TB_NAMECD"], $opt, $extra, "1");

        //結核・病名テキスト
        if ((int)$RowD["TB_NAMECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["TB_NAME_REMARK1"] = knjCreateTextBox($objForm, $RowD["TB_NAME_REMARK1"], "TB_NAME_REMARK1", 40, 20, $extra);
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            $arg["data"]["TB_NAMECD_LABEL"] = ($model->z010name1 == "miyagiken") ? "病名" : "疾病及び異常";
        } else {
            $arg["data"]["TB_NAMECD_LABEL"] = "病名";
        }
        //結核・指導区分コンボ
        $result     = $db->query(knjf010jQuery::getTbAdvise($model));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $extra = "style=\"width:260px;\"";
        $arg["data"]["TB_ADVISECD"] = knjCreateCombo($objForm, "TB_ADVISECD", $RowD["TB_ADVISECD"], $opt, $extra, "1");

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

        $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].URICOTHERTEST, '".implode(",", $disableCodes)."')\"";
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

        //寄生虫卵コンボ
        $query = knjf010jQuery::getNameMst($model, "F023");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "PARASITE", $RowD["PARASITE"], $extra, 1, "blank");

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
            $extra = "style=\"width:260px;\" onChange=\"syokenNyuryoku(this, document.forms[0].OTHER_REMARK2)\"";
        } else {
            $extra = "style=\"width:260px;\"";
        }
        $arg["data"]["OTHERDISEASECD"] = knjCreateCombo($objForm, "OTHERDISEASECD", $RowD["OTHERDISEASECD"], $opt, $extra, "1");

        //その他疾病及び異常:指導区分コンボ
        $result     = $db->query(knjf010jQuery::getOtherAdvisecd($model));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "style=\"width:260px;\"";
        $arg["data"]["OTHER_ADVISECD"] = knjCreateCombo($objForm, "OTHER_ADVISECD", $RowD["OTHER_ADVISECD"], $opt, $extra, 1);

        //その他疾病及び異常:所見
        $arg["data"]["OTHER_REMARK"] = knjCreateTextBox($objForm, $RowD["OTHER_REMARK"], "OTHER_REMARK", 40, 20, "");

        //貧血
        //所見
        $extra = "";
        $arg["data"]["ANEMIA_REMARK"] = knjCreateTextBox($objForm, $RowD["ANEMIA_REMARK"], "ANEMIA_REMARK", 21, 30, $extra);

        //ヘモグロビン
        $extra = "style=\"text-align:right\" onblur=\"return Num_Check(this);\"";
        $arg["data"]["HEMOGLOBIN"] = knjCreateTextBox($objForm, $RowD["HEMOGLOBIN"], "HEMOGLOBIN", 4, 4, $extra);

        //学校医・所見
        if ($arg["is_mie"] == "1") {
            $result     = $db->query(knjf010jQuery::getDocCd($model));
            $opt        = array();
            $opt[]      = $optnull;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $namecd = substr($row["NAMECD2"], 0, 2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            $extra = "style=\"width:260px;\"";
            $arg["data"]["DOC_CD"] = knjCreateCombo($objForm, "DOC_CD", $RowD["DOC_CD"], $opt, $extra, "1");
        }
        $arg["data"]["DOC_REMARK"] = knjCreateTextBox($objForm, $RowD["DOC_REMARK"], "DOC_REMARK", 40, 20, "");

        //学校医・所見日付
        $RowD["DOC_DATE"] = str_replace("-", "/", $RowD["DOC_DATE"]);
        $arg["data"]["DOC_DATE"] = View::popUpCalendar($objForm, "DOC_DATE", $RowD["DOC_DATE"]);

        //学校医
        if ($model->isMie) {
            //学校医コンボボックス(三重のみ)
            $arg["data"]["DOC_NAME"] = knjCreateTextBox($objForm, $RowD["DOC_NAME"], "DOC_NAME", 20, 10, "");
        }
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
            $namecd = substr($row["NAMECD2"], 0, 2);
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
            $arg["data"]["REMARK_MOJI"] = '100';
        } else {
            $arg["data"]["REMARK_MOJI"] = '60';
        }
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $RowD["REMARK"], "REMARK", 80, $model->maxRemarkByte, "");

        Query::dbCheckIn($db);

        /* ボタン作成 */
        //更新ボタン
        $extra = "onclick=\"return doSubmit()\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //戻るボタン
        $link = REQUESTROOT."/F/KNJF010J/knjf010jindex.php?cmd=back&ini2=1";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //対象者一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','left_select','right_select',1)\"";
        $arg["date"]["left_select"] = knjCreateCombo($objForm, "left_select", "left", $opt_left, $extra, "20");

        //生徒一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','left_select','right_select',1)\"";
        $arg["date"]["right_select"] = knjCreateCombo($objForm, "right_select", "left", $opt_right, $extra, "20");

        //全て追加
        $extra = "onclick=\"return move('sel_add_all','left_select','right_select',1);\"";
        $arg["button"]["sel_add_all"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);

        //追加
        $extra = "onclick=\"return move('left','left_select','right_select',1);\"";
        $arg["button"]["sel_add"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);

        //削除
        $extra = "onclick=\"return move('right','left_select','right_select',1);\"";
        $arg["button"]["sel_del"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);

        //全て削除
        $extra = "onclick=\"return move('sel_del_all','left_select','right_select',1);\"";
        $arg["button"]["sel_del_all"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        /* ヘッダ */
        $arg["info"]    = array("TOP"        =>  $model->year."年度  "
                                                .$model->control_data["学期名"][$model->semester]
                                                ."  対象クラス  ".$model->Hrname,
                                "LEFT_LIST"  => "対象者一覧",
                                "RIGHT_LIST" => $model->sch_label."一覧");
        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "REPLACEHIDDENDATE", $RowH["DATE"]);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf010jSubForm3.html", $arg);
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

function getDisabledCodes($db, $nameCd1)
{
    $result     = $db->query(knjf010jQuery::getNameSpare2($nameCd1));
    $nameCd2 = array();
    while ($row  = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $nameCd2[] = $row["NAMECD2"];
    }
    return $nameCd2;
}
