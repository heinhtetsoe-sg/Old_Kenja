<?php

require_once('for_php7.php');

class knjf010aSubForm1
{
    public function main(&$model)
    {
        $objForm        = new form();
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjf010aindex.php", "", "sel");

        //生徒一覧
        $opt_left = $opt_right = array();
        //置換処理選択時の生徒の情報
        $array = explode(",", $model->replace_data["selectdata"]);
        if ($array[0]=="") {
            $array[0] = $model->schregno;
        }
        //生徒情報
        $RowH = knjf010aQuery::getMedexamHdat($model);      //生徒健康診断ヘッダデータ取得
        $RowD = knjf010aQuery::getMedexamDetDat($model);   //生徒健康診断詳細データ取得

        $db = Query::dbCheckOut();

        $result   = $db->query(knjf010aQuery::getStudent($model));
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
        if ($model->isFukui || $model->isKoma) {
            $arg["is_fukui_koma"] = "1";
        } else {
            $arg["is_fukui_koma"] = "";
        }
        if ($model->isFukui) { // 福井は視力の文字＋数字入力
            $arg["is_fukui"] = "1";
            $arg["is_fukui_ear"] = "1";
            $arg["no_fukui"] = "";
        } else {
            $arg["is_fukui"] = "";
            if ($model->Properties["useSpecial_Support_School"] != "1" && $model->Properties["unEar_db"] == "1") {
                $arg["is_fukui_ear"] = "1";
                $arg["no_fukui"] = "";
            } else {
                $arg["is_fukui_ear"] = "";
                $arg["no_fukui"] = "1";
            }
        }
        if ($model->isHirokoudai) { // 広工大は数字入力
            $arg["is_Hirokoudai"]  = "1";
            $arg["not_Hirokoudai"] = "";
            $arg["EAR_TITLE"]      = "1000Hz";
        } else {
            $arg["is_Hirokoudai"]  = "";
            $arg["not_Hirokoudai"] = "1";
            $arg["EAR_TITLE"]      = "平均dB";
        }

        if ($model->isKoma || ($model->Properties["printKenkouSindanIppan"] == "1" && $model->Properties["KenkouSindan_Ippan_Pattern"] == "1")) {
            $arg["is_ear_otherType"]  = "1";
            $arg["no_koma"] = "";
            $arg["EAR_TITLE"]      = "1000Hz";
        } else {
            $arg["is_ear_otherType"]  = "";
            $arg["no_koma"] = "1";
        }
        //特別支援学校
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $arg["useSpecial_Support_School"] = "1";
        }

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
        //チェックボックス
        $cnt = ($model->Properties["useEar4000Hz"] == "1") ? 11 : 9;
        $cnt = ($model->isFukui
                 || ($model->Properties["useSpecial_Support_School"] != "1" && $model->Properties["unEar_db"] == "1")) ? 7: $cnt;

        for ($i=0; $i <= $cnt; $i++) {
            if ($i == $cnt) {
                $extra = "id=\"RCHECK_ALL\" onClick=\"return check_all(this);\"";
                $checked = ($model->replace_data["check"][$cnt] == "1") ? 1 : 0;
                $arg["data"]["RCHECK_ALL"] = knjCreateCheckBox($objForm, "RCHECK_ALL", "1", $extra);
            } else {
                $objForm->ae(array("type"       => "checkbox",
                                   "name"       => "RCHECK".$i,
                                   "value"      => "1",
                                   "checked"    => (($model->replace_data["check"][$i] == "1") ? 1 : 0)));
                $arg["data"]["RCHECK".$i] = $objForm->ge("RCHECK".$i);
            }
        }
        //健康診断実施日付
        if ($RowH["DATE"] == "") {
            $RowH["DATE"] = CTRL_DATE;
        }
        $RowH["DATE"] = str_replace("-", "/", $RowH["DATE"]);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $RowH["DATE"]);

        //測定困難チェック時は、視力情報はグレーアウト
        if ($model->field["R_VISION_CANTMEASURE"] == "1") {
            $disVisionR = " disabled";
        } else {
            $disVisionR = ($RowD["R_VISION_CANTMEASURE"] == "1") ? " disabled": "";
        }
        if ($model->field["L_VISION_CANTMEASURE"] == "1") {
            $disVisionL = " disabled";
        } else {
            $disVisionL = ($RowD["L_VISION_CANTMEASURE"] == "1") ? " disabled": "";
        }

        //視力・右裸眼(数字)
        $extra = "onblur=\"return Num_Check(this);\" id=\"R_BAREVISION\"";
        $arg["data"]["R_BAREVISION"] = knjCreateTextBox($objForm, $RowD["R_BAREVISION"], "R_BAREVISION", 5, 5, $extra.$disVisionR);
        //視力・右矯正(数字)
        $extra = "onblur=\"return Num_Check(this);\" id=\"R_VISION\"";
        $arg["data"]["R_VISION"] = knjCreateTextBox($objForm, $RowD["R_VISION"], "R_VISION", 5, 5, $extra.$disVisionR);
        //視力・左裸眼(数字)
        $extra = "onblur=\"return Num_Check(this);\" id=\"L_BAREVISION\"";
        $arg["data"]["L_BAREVISION"] = knjCreateTextBox($objForm, $RowD["L_BAREVISION"], "L_BAREVISION", 5, 5, $extra.$disVisionL);
        //視力・左矯正(数字)
        $extra = "onblur=\"return Num_Check(this);\" id=\"L_VISION\"";
        $arg["data"]["L_VISION"] = knjCreateTextBox($objForm, $RowD["L_VISION"], "L_VISION", 5, 5, $extra.$disVisionL);

        //視力
        if ($model->isMie) {
            //視力・右裸眼(文字)
            $extra = "onblur=\"return Mark_Check(this);\"";
            $arg["data"]["R_BAREVISION_MARK"] = knjCreateTextBox($objForm, $RowD["R_BAREVISION_MARK"], "R_BAREVISION_MARK", 1, 1, $extra);
            //視力・右矯正(文字)
            $extra = "onblur=\"return Mark_Check(this);\"";
            $arg["data"]["R_VISION_MARK"] = knjCreateTextBox($objForm, $RowD["R_VISION_MARK"], "R_VISION_MARK", 1, 1, $extra);
            //視力・左矯正(文字)
            $extra = "onblur=\"return Mark_Check(this);\"";
            $arg["data"]["L_BAREVISION_MARK"] = knjCreateTextBox($objForm, $RowD["L_BAREVISION_MARK"], "L_BAREVISION_MARK", 1, 1, $extra);
            //視力・左裸眼(文字)
            $extra = "onblur=\"return Mark_Check(this);\"";
            $arg["data"]["L_VISION_MARK"] = knjCreateTextBox($objForm, $RowD["L_VISION_MARK"], "L_VISION_MARK", 1, 1, $extra);
        } else {
            $query = knjf010aQuery::getLRBarevisionMark($model);
            $extra = "";
            //視力・右裸眼(文字)
            makeCmb($objForm, $arg, $db, $query, "R_BAREVISION_MARK", $RowD["R_BAREVISION_MARK"], $extra.$disVisionR, 1, "BLANK");
            //視力・右矯正(文字)
            makeCmb($objForm, $arg, $db, $query, "R_VISION_MARK", $RowD["R_VISION_MARK"], $extra.$disVisionR, 1, "BLANK");
            //視力・左矯正(文字)
            makeCmb($objForm, $arg, $db, $query, "L_BAREVISION_MARK", $RowD["L_BAREVISION_MARK"], $extra.$disVisionL, 1, "BLANK");
            //視力・左裸眼(文字)
            makeCmb($objForm, $arg, $db, $query, "L_VISION_MARK", $RowD["L_VISION_MARK"], $extra.$disVisionL, 1, "BLANK");
        }

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

        //測定困難チェック時は、視力情報はグレーアウト
        if ($model->field["R_EAR_CANTMEASURE"] == "1") {
            $disEarR = " disabled";
        } else {
            $disEarR = ($RowD["R_EAR_CANTMEASURE"] == "1") ? " disabled": "";
        }
        if ($model->field["L_EAR_CANTMEASURE"] == "1") {
            $disEarL = " disabled";
        } else {
            $disEarL = ($RowD["L_EAR_CANTMEASURE"] == "1") ? " disabled": "";
        }

        //聴力・右DB
        if ($arg["is_ear_otherType"] == "1") {
            $query = knjf010aQuery::getNameMst($model, "F010");
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "R_EAR_DB_1000", $RowD["R_EAR_DB_1000"], $extra.$disEarR, 1, "BLANK");
        } else {
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data"]["R_EAR_DB"] = knjCreateTextBox($objForm, $RowD["R_EAR_DB"], "R_EAR_DB", 4, 3, $extra.$disEarR);
        }
        //聴力・右状態コンボ
        $optnull    = array("label" => "","value" => "");   //初期値：空白項目
        $result     = $db->query(knjf010aQuery::getLREar($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $objForm->ae(array("type"        => "select",
                            "name"        => "R_EAR",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"".$disEarR,
                            "value"       => $RowD["R_EAR"],
                            "options"     => $opt ));
        $arg["data"]["R_EAR"] = $objForm->ge("R_EAR");
        //聴力・左DB
        if ($arg["is_ear_otherType"] == "1") {
            $query = knjf010aQuery::getNameMst($model, "F010");
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "L_EAR_DB_1000", $RowD["L_EAR_DB_1000"], $extra.$disEarL, 1, "BLANK");
        } else {
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data"]["L_EAR_DB"] = knjCreateTextBox($objForm, $RowD["L_EAR_DB"], "L_EAR_DB", 4, 3, $extra.$disEarL);
        }
        //聴力・左状態コンボ
        $objForm->ae(array("type"        => "select",
                            "name"        => "L_EAR",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"".$disEarL,
                            "value"       => $RowD["L_EAR"],
                            "options"     => $opt ));
        $arg["data"]["L_EAR"] = $objForm->ge("L_EAR");

        /* 装用時 */
        //聴力・右DB
        $extra = "onblur=\"return Num_Check(this);\"";
        $arg["data"]["R_EAR_DB_IN"] = knjCreateTextBox($objForm, $RowD["R_EAR_DB_IN"], "R_EAR_DB_IN", 4, 3, $extra.$disEarR);
        //聴力・右状態コンボ
        $optnull    = array("label" => "","value" => "");   //初期値：空白項目
        $result     = $db->query(knjf010aQuery::getLREar($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $objForm->ae(array("type"        => "select",
                            "name"        => "R_EAR_IN",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"".$disEarR,
                            "value"       => $RowD["R_EAR_IN"],
                            "options"     => $opt ));
        $arg["data"]["R_EAR_IN"] = $objForm->ge("R_EAR_IN");
        //聴力・左DB
        $extra = "onblur=\"return Num_Check(this);\"";
        $arg["data"]["L_EAR_DB_IN"] = knjCreateTextBox($objForm, $RowD["L_EAR_DB_IN"], "L_EAR_DB_IN", 4, 3, $extra.$disEarL);
        //聴力・左状態コンボ
        $objForm->ae(array("type"        => "select",
                            "name"        => "L_EAR_IN",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:250px;\"".$disEarL,
                            "value"       => $RowD["L_EAR_IN"],
                            "options"     => $opt ));
        $arg["data"]["L_EAR_IN"] = $objForm->ge("L_EAR_IN");

        if ($model->Properties["useEar4000Hz"] == "1") {
            $arg["useEar4000Hz"] = 1;
            if ($arg["is_ear_otherType"] == "1") {
                $query = knjf010aQuery::getNameMst($model, "F010");
                //聴力・右4000Hz
                $extra = "";
                makeCmb($objForm, $arg, $db, $query, "R_EAR_DB_4000", $RowD["R_EAR_DB_4000"], $extra.$disEarR, 1, "BLANK");
                //聴力・左4000Hz
                $extra = "";
                makeCmb($objForm, $arg, $db, $query, "L_EAR_DB_4000", $RowD["L_EAR_DB_4000"], $extra.$disEarL, 1, "BLANK");
            } else {
                //聴力・右4000Hz
                $extra = "onblur=\"return Num_Check(this);\"";
                $arg["data"]["R_EAR_DB_4000"] = knjCreateTextBox($objForm, $RowD["R_EAR_DB_4000"], "R_EAR_DB_4000", 4, 3, $extra.$disEarR);
                //聴力・左4000Hz
                $extra = "onblur=\"return Num_Check(this);\"";
                $arg["data"]["L_EAR_DB_4000"] = knjCreateTextBox($objForm, $RowD["L_EAR_DB_4000"], "L_EAR_DB_4000", 4, 3, $extra.$disEarL);
            }
            /* 装用時 */
            //聴力・右4000Hz
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data"]["R_EAR_DB_4000_IN"] = knjCreateTextBox($objForm, $RowD["R_EAR_DB_4000_IN"], "R_EAR_DB_4000_IN", 4, 3, $extra.$disEarR);
            //聴力・左4000Hz
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data"]["L_EAR_DB_4000_IN"] = knjCreateTextBox($objForm, $RowD["L_EAR_DB_4000_IN"], "L_EAR_DB_4000_IN", 4, 3, $extra.$disEarL);
        } else {
            $arg["unuseEar4000Hz"] = 1;
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

        Query::dbCheckIn($db);

        /* ボタン作成 */
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit()\"" ));
        //戻るボタン
        $link = REQUESTROOT."/F/KNJF010A/knjf010aindex.php?cmd=back&ini2=1";
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"window.open('$link','_self');\"" ));
        $arg["BUTTONS"] = $objForm->ge("btn_update")."    ".$objForm->ge("btn_back");
        //対象者一覧
        $objForm->ae(array("type"        => "select",
                            "name"        => "left_select",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','left_select','right_select',1)\" ",
                            "options"     => $opt_left));
        //生徒一覧
        $objForm->ae(array("type"        => "select",
                            "name"        => "right_select",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','left_select','right_select',1)\" ",
                            "options"     => $opt_right));
        //全て追加
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','left_select','right_select',1);\"" ));
        //追加
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','left_select','right_select',1);\"" ));
        //削除
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','left_select','right_select',1);\"" ));
        //全て削除
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','left_select','right_select',1);\"" ));
        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("left_select"),
                                   "RIGHT_PART"  => $objForm->ge("right_select"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));
        /* ヘッダ */
        $arg["info"]    = array("TOP"        =>  $model->year."年度  "
                                                .$model->control_data["学期名"][$model->semester]
                                                ."  対象クラス  ".$model->Hrname,
                                "LEFT_LIST"  => "対象者一覧",
                                "RIGHT_LIST" => $model->sch_label."一覧");
        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "selectdata"));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "REPLACEHIDDENDATE",
                            "value"     => $RowH["DATE"]));
        knjCreateHidden($objForm, "printKenkouSindanIppan", $model->Properties["printKenkouSindanIppan"]);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf010aSubForm1.html", $arg);
    }
}

function makeTextBox($objForm, $arg, $name, $size, $maxlength, $value, $extra = "onblur=\"return Num_Check(this);\"")
{
    $objForm->ae(array("type"        => "text",
                            "name"        => $name,
                            "size"        => $size,
                            "maxlength"   => $maxlength,
                            "extrahtml"   => $extra,
                            "value"       => $value ));
    $arg["data"][$name] = $objForm->ge($name);
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
