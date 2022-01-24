<?php

require_once('for_php7.php');

class knjf010jSubForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("sel", "POST", "knjf010jindex.php", "", "sel");

        //生徒一覧
        $opt_left = $opt_right = array();
        //置換処理選択時の生徒の情報
        $array = explode(",", $model->replace_data["selectdata"]);
        if ($array[0] == "") {
            $array[0] = $model->schregno;
        }
        //生徒情報
        $RowH = knjf010jQuery::getMedexamHdat($model);  //生徒健康診断ヘッダデータ取得
        $RowD = knjf010jQuery::getMedexamDetDat($model);//生徒健康診断詳細データ取得

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

        if ($model->isKoma) {
            $arg["is_koma"]  = "1";
            $arg["no_koma"] = "";
            $arg["EAR_TITLE"]      = "1000Hz";
        } else {
            $arg["is_koma"]  = "";
            $arg["no_koma"] = "1";
        }
        //特別支援学校
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $arg["useSpecial_Support_School"] = "1";
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
        $cnt = 13;
        for ($i = 0; $i <= $cnt; $i++) {
            if ($i == $cnt) {
                $extra = "id=\"RCHECK_ALL\" onClick=\"return check_all(this);\"";
                if ($model->replace_data["check"][$cnt] == "1") {
                    $extra .= "checked = 'checked'";
                } else {
                    $extra .= "";
                }
                $arg["data"]["RCHECK_ALL"] = knjCreateCheckBox($objForm, "RCHECK_ALL", "1", $extra);
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

        $query = knjf010jQuery::getLrBarevisionMark($model);
        if ($model->field["R_VISION_CANTMEASURE"] == "1") {
            //視力・右裸眼(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"R_BAREVISION\"  disabled";
            $arg["data"]["R_BAREVISION"] = knjCreateTextBox($objForm, $RowD["R_BAREVISION"], "R_BAREVISION", 5, 5, $extra.$entMove.$disVisionR);
            //視力・右矯正(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"R_VISION\"  disabled";
            $arg["data"]["R_VISION"] = knjCreateTextBox($objForm, $RowD["R_VISION"], "R_VISION", 5, 5, $extra.$entMove.$disVisionR);

            //視力・右裸眼(文字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"R_BAREVISION_MARK\"  disabled";
            makeCmb($objForm, $arg, $db, $query, "R_BAREVISION_MARK", $RowD["R_BAREVISION_MARK"], $extra.$entMove.$disVisionR, 1, "BLANK");
            //視力・右矯正(文字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"R_VISION_MARK\"  disabled";
            makeCmb($objForm, $arg, $db, $query, "R_VISION_MARK", $RowD["R_VISION_MARK"], $extra.$entMove.$disVisionR, 1, "BLANK");
        } else {
            //視力・右裸眼(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"R_BAREVISION\" ";
            $arg["data"]["R_BAREVISION"] = knjCreateTextBox($objForm, $RowD["R_BAREVISION"], "R_BAREVISION", 5, 5, $extra.$entMove.$disVisionR);
            //視力・右矯正(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"R_VISION\" ";
            $arg["data"]["R_VISION"] = knjCreateTextBox($objForm, $RowD["R_VISION"], "R_VISION", 5, 5, $extra.$entMove.$disVisionR);

            //視力・右裸眼(記号)
            $extra = "onblur=\"return Num_Check(this);\" id=\"R_BAREVISION_MARK\" ";
            makeCmb($objForm, $arg, $db, $query, "R_BAREVISION_MARK", $RowD["R_BAREVISION_MARK"], $extra.$entMove.$disVisionR, 1, "BLANK");
            //視力・右矯正(記号)
            $extra = "onblur=\"return Num_Check(this);\" id=\"R_VISION_MARK\" ";
            makeCmb($objForm, $arg, $db, $query, "R_VISION_MARK", $RowD["R_VISION_MARK"], $extra.$entMove.$disVisionR, 1, "BLANK");
        }

        if ($model->field["L_VISION_CANTMEASURE"] == "1") {
            //視力・左裸眼(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"L_BAREVISION\"  disabled";
            $arg["data"]["L_BAREVISION"] = knjCreateTextBox($objForm, $RowD["L_BAREVISION"], "L_BAREVISION", 5, 5, $extra.$entMove.$disVisionL);
            //視力・左矯正(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"L_VISION\"  disabled";
            $arg["data"]["L_VISION"] = knjCreateTextBox($objForm, $RowD["L_VISION"], "L_VISION", 5, 5, $extra.$entMove.$disVisionL);

            //視力・左裸眼(文字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"L_BAREVISION_MARK\"  disabled";
            makeCmb($objForm, $arg, $db, $query, "L_BAREVISION_MARK", $RowD["L_BAREVISION_MARK"], $extra.$entMove.$disVisionL, 1, "BLANK");
            //視力・左矯正(文字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"L_VISION_MARK\"  disabled";
            makeCmb($objForm, $arg, $db, $query, "L_VISION_MARK", $RowD["L_VISION_MARK"], $extra.$entMove.$disVisionL, 1, "BLANK");
        } else {
            //視力・左裸眼(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"L_BAREVISION\" ";
            $arg["data"]["L_BAREVISION"] = knjCreateTextBox($objForm, $RowD["L_BAREVISION"], "L_BAREVISION", 5, 5, $extra.$entMove.$disVisionL);
            //視力・左矯正(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"L_VISION\" ";
            $arg["data"]["L_VISION"] = knjCreateTextBox($objForm, $RowD["L_VISION"], "L_VISION", 5, 5, $extra.$entMove.$disVisionL);

            //視力・左裸眼(記号)
            $extra = "onblur=\"return Num_Check(this);\" id=\"L_BAREVISION_MARK\" ";
            makeCmb($objForm, $arg, $db, $query, "L_BAREVISION_MARK", $RowD["L_BAREVISION_MARK"], $extra.$entMove.$disVisionL, 1, "BLANK");
            //視力・左矯正(記号)
            $extra = "onblur=\"return Num_Check(this);\" id=\"L_VISION_MARK\" ";
            makeCmb($objForm, $arg, $db, $query, "L_VISION_MARK", $RowD["L_VISION_MARK"], $extra.$entMove.$disVisionL, 1, "BLANK");
        }
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
        if ($model->isKoma) {
            $query = knjf010jQuery::getNameMst($model, "F010");
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "R_EAR_DB_1000", $RowD["R_EAR_DB_1000"], $extra.$disEarR, 1, "BLANK");
        } else {
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["R_EAR_DB"] = knjCreateTextBox($objForm, $RowD["R_EAR_DB"], "R_EAR_DB", 3, 3, $extra.$disEarR);
        }
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
        $extra = "style=\"width:300px;\"".$disEarR;
        $arg["data"]["R_EAR"] = knjCreateCombo($objForm, "R_EAR", $RowD["R_EAR"], $opt, $extra, "1");

        //聴力・左DB
        if ($model->isKoma) {
            $query = knjf010jQuery::getNameMst($model, "F010");
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "L_EAR_DB_1000", $RowD["L_EAR_DB_1000"], $extra.$disEarL, 1, "BLANK");
        } else {
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["L_EAR_DB"] = knjCreateTextBox($objForm, $RowD["L_EAR_DB"], "L_EAR_DB", 3, 3, $extra.$disEarL);
        }
        //聴力・左状態コンボ
        $extra = "style=\"width:300px;\"".$disEarL;
        $arg["data"]["L_EAR"] = knjCreateCombo($objForm, "L_EAR", $RowD["L_EAR"], $opt, $extra, "1");

        /* 装用時 */
        //聴力・右DB
        $extra = "onblur=\"return Num_Check(this);\"";
        $arg["data"]["R_EAR_DB_IN"] = knjCreateTextBox($objForm, $RowD["R_EAR_DB_IN"], "R_EAR_DB_IN", 4, 3, $extra.$disEarR);
        //聴力・右状態コンボ
        $optnull    = array("label" => "","value" => "");   //初期値：空白項目
        $result     = $db->query(knjf010jQuery::getLrEAR($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $extra = "style=\"width:250px;\"".$disEarR;
        $arg["data"]["R_EAR_IN"] = knjCreateCombo($objForm, "R_EAR_IN", $RowD["R_EAR_IN"], $opt, $extra, "1");

        //聴力・左DB
        $extra = "onblur=\"return Num_Check(this);\"";
        $arg["data"]["L_EAR_DB_IN"] = knjCreateTextBox($objForm, $RowD["L_EAR_DB_IN"], "L_EAR_DB_IN", 4, 3, $extra.$disEarL);
        //聴力・左状態コンボ
        $extra = "style=\"width:250px;\"".$disEarL;
        $arg["data"]["L_EAR_IN"] = knjCreateCombo($objForm, "L_EAR_IN", $RowD["L_EAR_IN"], $opt, $extra, "1");

        if ($model->Properties["useEar4000Hz"] == "1") {
            $arg["useEar4000Hz"] = 1;
            if ($model->isKoma) {
                $query = knjf010jQuery::getNameMst($model, "F010");
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

        //更新ボタン
        $extra = "onclick=\"return doSubmit()\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //戻るボタン
        $link = REQUESTROOT."/F/KNJF010J/knjf010jindex.php?cmd=back&ini2=1";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //対象者一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','left_select','right_select',1)\"";
        $arg["data"]["left_select"] = knjCreateCombo($objForm, "left_select", "left", $opt_left, $extra, "20");

        //生徒一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','left_select','right_select',1)\"";
        $arg["data"]["right_select"] = knjCreateCombo($objForm, "right_select", "left", $opt_right, $extra, "20");

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
        knjCreateHidden($objForm, "printKenkouSindanIppan", $model->Properties["printKenkouSindanIppan"]);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf010jSubForm1.html", $arg);
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
