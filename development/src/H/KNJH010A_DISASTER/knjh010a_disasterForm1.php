<?php

require_once('for_php7.php');

class knjh010a_disasterForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        if (isset($model->schregno) && !isset($model->warning) && VARS::post("cmd")!="update") {
            $row1  = knjh010a_disasterQuery::getSchregEnvirDat($model);
            $row2 = knjh010a_disasterQuery::getSchregEnvirDetailDat($model);
            $row  = array_merge((array)$row1, (array)$row2);
            if ($row["COMMUTE_HOURS"]) {
                $row["COMMUTE_HOURS"] = sprintf("%d", $row["COMMUTE_HOURS"]);
            }
            if ($row["COMMUTE_MINUTES"]) {
                $row["COMMUTE_MINUTES"] = sprintf("%d", $row["COMMUTE_MINUTES"]);
            }
        } else {
            $row =& $model->field;
        }
        if ($model->schregno) {
            $arg["name"] = $model->schregno."&nbsp;&nbsp;：&nbsp;&nbsp;".$model->name_show;
        } else {
            $arg["name"] = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:";
        }

        $db = Query::dbCheckOut();

        //左テーブル項目
        //住居調査(コンボ)
        $query = knjh010a_disasterQuery::getVNameMst("H108", $model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "RESIDENTCD", $row["RESIDENTCD"], $extra, 1, "BLANK");
        //住居調査(REMARK1)
        $extra = "";
        $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $row["REMARK1"], "REMARK1", 25, 60, $extra);
        //住居調査(REMARK2)
        $extra = "onBlur=\"return to_Integer(this);\"";
        $arg["data"]["REMARK2"] = knjCreateTextBox($objForm, $row["REMARK2"], "REMARK2", 7, 5, $extra);

        //災害時帰宅グループ番号
        $query = knjh010a_disasterQuery::getGoHome();
        $extra = '';
        makeCmb($objForm, $arg, $db, $query, "GO_HOME_GROUP_NO", $row["GO_HOME_GROUP_NO"], $extra, 1, "BLANK");

        //責任者
        $query = knjh010a_disasterQuery::getResponsibility();
        $extra = '';
        makeCmb($objForm, $arg, $db, $query, "RESPONSIBILITY", $row["RESPONSIBILITY"], $extra, 1, "BLANK");

        //引き取り者氏名１
        $extra = "";
        $arg["data"]["HIKINAME1"] = knjCreateTextBox($objForm, $row["HIKINAME1"], "HIKINAME1", 25, 150, $extra);
        //本人との関係１
        $query = knjh010a_disasterQuery::getVNameMst("H201", $model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "RELATION1", $row["RELATION1"], $extra, 1, "BLANK");
        //電話番号１
        $extra = "onblur=\"this.value=toTelNo(this.value)\"";
        $arg["data"]["HIKITELNO1"] = knjCreateTextBox($objForm, $row["HIKITELNO1"], "HIKITELNO1", 16, 14, $extra);

        //引き取り者氏名２
        $extra = "";
        $arg["data"]["HIKINAME2"] = knjCreateTextBox($objForm, $row["HIKINAME2"], "HIKINAME2", 25, 150, $extra);
        //本人との関係２
        $query = knjh010a_disasterQuery::getVNameMst("H201", $model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "RELATION2", $row["RELATION2"], $extra, 1, "BLANK");
        //電話番号２
        $extra = "onblur=\"this.value=toTelNo(this.value)\"";
        $arg["data"]["HIKITELNO2"] = knjCreateTextBox($objForm, $row["HIKITELNO2"], "HIKITELNO2", 16, 14, $extra);

        //引き取り者氏名３
        $extra = "";
        $arg["data"]["HIKINAME3"] = knjCreateTextBox($objForm, $row["HIKINAME3"], "HIKINAME3", 25, 150, $extra);
        //本人との関係３
        $query = knjh010a_disasterQuery::getVNameMst("H201", $model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "RELATION3", $row["RELATION3"], $extra, 1, "BLANK");
        //電話番号３
        $extra = "onblur=\"this.value=toTelNo(this.value)\"";
        $arg["data"]["HIKITELNO3"] = knjCreateTextBox($objForm, $row["HIKITELNO3"], "HIKITELNO3", 16, 14, $extra);

        /*避難先１*/
        //避難先名
        $extra = "";
        $arg["data"]["HINANAMES"] = knjCreateTextBox($objForm, $row["HINANAMES"], "HINANAMES", 25, 150, $extra);
        //郵便番号
        $extra = "onblur=\"this.value=toTelNo(this.value)\"";
        $arg["data"]["HINAZIPS"] = knjCreateTextBox($objForm, $row["HINAZIPS"], "HINAZIPS", 10, 8, $extra);
        //住所
        $extra = "";
        $arg["data"]["HINAADD1S"] = knjCreateTextBox($objForm, $row["HINAADD1S"], "HINAADD1S", 25, 150, $extra);
        //方書き
        $extra = "";
        $arg["data"]["HINAADD2S"] = knjCreateTextBox($objForm, $row["HINAADD2S"], "HINAADD2S", 25, 150, $extra);
        //電話番号１
        $extra = "onblur=\"this.value=toTelNo(this.value)\"";
        $arg["data"]["HINATEL1S"] = knjCreateTextBox($objForm, $row["HINATEL1S"], "HINATEL1S", 16, 14, $extra);
        //電話番号２
        $extra = "onblur=\"this.value=toTelNo(this.value)\"";
        $arg["data"]["HINATEL2S"] = knjCreateTextBox($objForm, $row["HINATEL2S"], "HINATEL2S", 16, 14, $extra);

        /*避難先２*/
        //避難先名
        $extra = "";
        $arg["data"]["HINANAMEH"] = knjCreateTextBox($objForm, $row["HINANAMEH"], "HINANAMEH", 25, 150, $extra);
        //郵便番号
        $extra = "onblur=\"this.value=toTelNo(this.value)\"";
        $arg["data"]["HINAZIPH"] = knjCreateTextBox($objForm, $row["HINAZIPH"], "HINAZIPH", 10, 8, $extra);
        //住所
        $extra = "";
        $arg["data"]["HINAADD1H"] = knjCreateTextBox($objForm, $row["HINAADD1H"], "HINAADD1H", 25, 150, $extra);
        //方書き
        $extra = "";
        $arg["data"]["HINAADD2H"] = knjCreateTextBox($objForm, $row["HINAADD2H"], "HINAADD2H", 25, 150, $extra);
        //電話番号１
        $extra = "onblur=\"this.value=toTelNo(this.value)\"";
        $arg["data"]["HINATEL1H"] = knjCreateTextBox($objForm, $row["HINATEL1H"], "HINATEL1H", 16, 14, $extra);
        //電話番号２
        $extra = "onblur=\"this.value=toTelNo(this.value)\"";
        $arg["data"]["HINATEL2H"] = knjCreateTextBox($objForm, $row["HINATEL2H"], "HINATEL2H", 16, 14, $extra);

        /*備考*/
        //氏名１
        $extra = "";
        $arg["data"]["BIKONAME1"] = knjCreateTextBox($objForm, $row["BIKONAME1"], "BIKONAME1", 25, 150, $extra);
        //所属１
        $extra = "";
        $arg["data"]["BIKOGROUP1"] = knjCreateTextBox($objForm, $row["BIKOGROUP1"], "BIKOGROUP1", 25, 150, $extra);
        //氏名２
        $extra = "";
        $arg["data"]["BIKONAME2"] = knjCreateTextBox($objForm, $row["BIKONAME2"], "BIKONAME2", 25, 150, $extra);
        //所属２
        $extra = "";
        $arg["data"]["BIKOGROUP2"] = knjCreateTextBox($objForm, $row["BIKOGROUP2"], "BIKOGROUP2", 25, 150, $extra);

        //右テーブル項目
        //通学：所要時間
        $extra = "onBlur=\"return to_Integer(this);\"";
        $arg["data"]["COMMUTE_HOURS"] = knjCreateTextBox($objForm, $row["COMMUTE_HOURS"], "COMMUTE_HOURS", 2, 2, $extra);

        //通学：所要分
        $extra = "onBlur=\"return to_Integer(this);\"";
        $arg["data"]["COMMUTE_MINUTES"] = knjCreateTextBox($objForm, $row["COMMUTE_MINUTES"], "COMMUTE_MINUTES", 2, 2, $extra);

        //通学方法
        $query = knjh010a_disasterQuery::getVNameMst("H100", $model);
        $extra = '';
        makeCmb($objForm, $arg, $db, $query, "HOWTOCOMMUTECD", $row["HOWTOCOMMUTECD"], $extra, 1, "0");

        //radio
        $opt = array(1, 2);
        $row["UP_DOWN"] = ($row["UP_DOWN"] == "") ? "1" : $row["UP_DOWN"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"UP_DOWN{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "UP_DOWN", $row["UP_DOWN"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //途中経由駅
        for ($i = 1; $i <= 7; $i++) {
            $josya = "JOSYA_" . $i;
            $rosen = "ROSEN_" . $i;
            $gesya = "GESYA_" . $i;
            $flg   = "FLG_" . $i;
            $extra = "disabled=\"true\" class=\"right_side\" style=\"width:270px;\"";

            $hidden_josya = "HIDDEN_JOSYA_" . $i;
            $hidden_rosen = "HIDDEN_ROSEN_" . $i;
            $hidden_gesya = "HIDDEN_GESYA_" . $i;

            if ($row[$flg] == '1') {
                $josya_cd = $row[$hidden_josya] ? $row[$hidden_josya] : $row[$josya];
                $rosen_cd = $row[$hidden_rosen] ? $row[$hidden_rosen] : $row[$rosen];
                $gesya_cd = $row[$hidden_gesya] ? $row[$hidden_gesya] : $row[$gesya];

                $query = knjh010a_disasterQuery::getStationName($josya_cd);
                list($josya_mei, $rosen_mei) = $db->getRow($query);
                $query = knjh010a_disasterQuery::getStationName($gesya_cd);
                list($gesya_mei, $rosen_mei) = $db->getRow($query);
            } elseif ($row[$flg] == '3') {
                $rosen_cd = $row[$hidden_rosen] ? $row[$hidden_rosen] : $row[$rosen];

                $query = knjh010a_disasterQuery::getBusCourse($rosen_cd);
                list($rosen_cd, $rosen_mei) = $db->getRow($query);

                $josya_mei = $row[$josya];
                $gesya_mei = $row[$gesya];
            } else {
                $josya_mei = $row[$josya];
                $rosen_mei = $row[$rosen];
                $gesya_mei = $row[$gesya];
            }

            $arg["data"][$josya] = knjCreateTextBox($objForm, $josya_mei, $josya, 20, 20, $extra);
            $arg["data"][$rosen] = knjCreateTextBox($objForm, $rosen_mei, $rosen, 20, 20, $extra);
            $arg["data"][$gesya] = knjCreateTextBox($objForm, $gesya_mei, $gesya, 20, 20, $extra);
            knjCreateHidden($objForm, $flg, $row[$flg]);
            knjCreateHidden($objForm, $hidden_josya, $josya_cd);
            knjCreateHidden($objForm, $hidden_rosen, $rosen_cd);
            knjCreateHidden($objForm, $hidden_gesya, $gesya_cd);
        }

        /**************/
        /* ボタン作成 */
        /**************/
        //性質・学業・他
        $link = REQUESTROOT."/H/KNJH010A_DISASTER/knjh010a_disasterindex.php?cmd=subform1&SCHREGNOSUB=".$model->schregno."&PRG=".VARS::get("PRG");
        $extra = "onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["OTHER"] = knjCreateBtn($objForm, 'OTHER', '性質・学業・他', $extra);

        //通学経路登録
        $extra = "onclick=\"Page_jumper2('{$model->prg}');\"";
        $arg["button"]["btn_suport"] = knjCreateBtn($objForm, 'btn_suport', '通学経路登録', $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"]    = View::updateNext($model, $objForm, 'btn_update');

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, 'btn_del', '削 除', $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        /********/
        /*hidden*/
        /********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "REQUESTROOT", REQUESTROOT);

        //通学方法（スクールバス）
        $school_bus = $db->getCol(knjh010a_disasterQuery::getSchoolBus($model));
        knjCreateHidden($objForm, "SCHOOL_BUS", implode(',', $school_bus ? $school_bus : array()));

        if (get_count($model->warning)== 0 && $model->cmd !="reset") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd =="reset") {
            $arg["next"] = "NextStudent(1);";
        }

        Query::dbCheckIn($db);

        $arg["start"] = $objForm->get_start("edit", "POST", "knjh010a_disasterindex.php", "", "edit");
        $arg["finish"] = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す
        View::toHTML($model, "knjh010a_disasterForm1.html", $arg);
    }
}
/********************************************** 以下関数 **********************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, $value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array('label' => "",
                       'value' => "");
    } elseif ($blank == "0") {
        $opt[] = array('label' => "",
                       'value' => "0");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
