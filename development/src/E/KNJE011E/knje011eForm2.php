<?php

require_once("for_php7.php");

//ビュー作成用クラス
class knje011eForm2
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("form2", "POST", "knje011eindex.php", "", "form2");

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        $arg["ATTEND_TITLE"] = $model->attendTitle."の記録";

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        //年度(年次)取得コンボ
        if ($model->cmd == "form2_first") { // すでに別の生徒を開いていた場合そのときの値が保持されているので
            $model->annual["YEAR"]   = "";  // 最初の呼出ならば、年度と年次をクリアする
            $model->annual["ANNUAL"] = "";
        }
        $opt = $chkOpt = array();

        //追加された年度を再セット
        $selectdata = ($model->selectdata != "") ? explode("-", $model->selectdata) : array();
        $selectdataText = ($model->selectdataText != "") ? explode("-", $model->selectdataText) : array();
        for ($i = 0; $i < get_count($selectdata); $i++) {
            $opt[] = array("label" => $selectdataText[$i],
                           "value" => $selectdata[$i]);
        }

        $disabled = "disabled";
        $query = knje011eQuery::selectQueryAnnual_knje011eForm2($model);
        $result = $db->query($query);
        while ( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["YEAR"] ."," .$row["ANNUAL"], $selectdata)) {
                $opt[] = array("label" => $row["YEAR"] ."年度　" .(int) $row["ANNUAL"] ."学年(年次)",
                               "value" => $row["YEAR"] ."," .$row["ANNUAL"]
                              );
            }
            $chkOpt[] = $row["YEAR"] ."," .$row["ANNUAL"];
            if (!isset($model->annual["YEAR"]) || ($model->cmd == "form2_first" &&
               (($model->mode == "ungrd" && $model->exp_year == $row["YEAR"]) || ($model->mode == "grd" && $model->grd_year == $row["YEAR"])))) {
                $model->annual["YEAR"]   = $row["YEAR"];
                $model->annual["ANNUAL"] = $row["ANNUAL"];
            }

            $disabled = "";
        }
        if (!strlen($model->annual["YEAR"]) || !strlen($model->annual["ANNUAL"])) {
            list($model->annual["YEAR"], $model->annual["ANNUAL"]) = preg_split('/,/', $opt[0]["value"]);
        }

        $addYearFlg = "";
        if (!in_array($model->annual["YEAR"].",".$model->annual["ANNUAL"], $chkOpt)) {
            $disabled = "disabled";
            $addYearFlg = "1";
        }

        //年次取得　年度-学年コンボにより切り替わる
        $query = knje011eQuery::getGradeCd($model, "set");
        $getGdat = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->combo_gradecd = "";
        $model->combo_gradecd = $getGdat["GRADE_CD"];
        $model->schoolKind = "";
        $model->schoolKind = $getGdat["SCHOOL_KIND"];

        if (!isset($model->warning) && $model->cmd != 'reload4') {
            if ($model->cmd == "reload2_ok" || $model->cmd == "reload2_cancel") {
                $query = knje011eQuery::selectQuery_Htrainremark_Dat($model);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($model->cmd == "reload2_cancel") {
                    if ($row) {
                        foreach ($row as $key => $val) {
                            $row[$key] = $model->field2[$key]."\n".$val;
                        }
                    } else {
                        $row = $model->field2;
                    }
                }
            } else {
                if ($model->cmd !== 'torikomi3' && $model->cmd !== 'torikomi4') {
                    $query = knje011eQuery::selectQueryForm2($model);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

                    if ("1" == $model->Properties["tyousasyo2020"]) {
                        for ($i = 1; $i <= 6; $i++) {
                            $row["TRAIN_REF".$i] = "";
                            $seq = "10".$i;
                            $query = knje011eQuery::getHexamEntremarkTrainrefDat($model, $model->annual["YEAR"], $seq);
                            $r = $db->getRow($query, DB_FETCHMODE_ASSOC);
                            if (is_array($r)) {
                                $row["TRAIN_REF".$i] = $r["REMARK"];
                            }
                        }
                    }
                } else {
                    $row = $model->field2;
                }
            }
            if (($model->cmd == "reload2_ok" || $model->cmd == "reload2_cancel") && $model->Properties["useSyojikou3"] == "1") {
                $row["TRAIN_REF1"] = $model->field2["TRAIN_REF1"];
                $row["TRAIN_REF2"] = $model->field2["TRAIN_REF2"];
                $row["TRAIN_REF3"] = $model->field2["TRAIN_REF3"];
                $row["TRAIN_REF4"] = $model->field2["TRAIN_REF4"];
                $row["TRAIN_REF5"] = $model->field2["TRAIN_REF5"];
                $row["TRAIN_REF6"] = $model->field2["TRAIN_REF6"];
            }
        } else {
            $row = $model->field2;
        }

        if ($model->entDiv == '4' || $model->entDiv == '5' || $model->entDiv == '7') {
            $arg["addYear"] = 1;
            //追加年度
            $query = knje011eQuery::selectYearQuery($model);
            $extra = "onChange=\"return btn_submit('form2');\"";
            makeCmb($objForm, $arg, $db, $query, $model->addYear, "ADD_YEAR", $extra, 1);

            //追加学年
            $query = knje011eQuery::selectGradeQuery($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, $model->addYearGrade, "ADD_YEAR_GRADE", $extra, 1);

            //年度追加ボタンを作成する
            $extra = " onclick=\"return add('form2');\"";
            $arg["btn_add_year"] = KnjCreateBtn($objForm, "btn_update", "年度追加", $extra);
        }


        $objForm->ae(array("type"       => "select",
                            "name"       => "ANNUAL",
                            "size"       => "1",
                            "value"      => $model->annual["YEAR"] ."," .$model->annual["ANNUAL"],
                            "extrahtml"  => "onChange=\"return btn_submit('form2');\"",
                            "options"    => $opt));

        $arg["ANNUAL"] = $objForm->ge("ANNUAL");

        $arg["COLSPAN_CHANGE"] = "colspan=\"3\"";

        /******************/
        /* テキストエリア */
        /******************/
        //出欠の記録備考
        makeAttendrecRemark($objForm, $arg, $model, $db, $disabled, $row);

        //特別活動の記録
        makeSpecialactrec($objForm, $arg, $model, $db, $disabled, $row);

        //指導上参考となる諸事項
        makeShojikou($objForm, $arg, $model, $db, $disabled, $row);

        //総合的な学習の時間
        //makeSogo($objForm, $arg, $model, $db, $disabled, $row);

        /**********/
        /* ボタン */
        /**********/
        //生徒指導要録より読込ボタンを作成する
        $extra = " class=\"btn_torikomi\" ";
        if ($model->getSchoolName == "tokiwa") {
            $extra .= " onclick=\" return btn_submit('reload2_ok');\" ";
        } else {
            $extra .= " onclick=\" return btn_submit('reload2');\" ";
        }
        $arg["btn_reload2"] = KnjCreateBtn($objForm, "btn_reload2", "生徒指導要録より読込", $extra);

        if ($addYearFlg == "1") {
            //更新・クリアボタンを使用可とする
            $disabled = "";
        }

        //更新ボタンを作成する
        $extra = $disabled ." onclick=\"return btn_submit('update2');\"";
        $arg["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //クリアボタンを作成する
        $extra = $disabled ." onclick=\"return btn_submit('reset')\"";
        $arg["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "useQualifiedMst", $model->Properties["useQualifiedMst"]);
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "selectdataText");

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje011eForm2.html", $arg);
    }
}

//出欠の記録備考
function makeAttendrecRemark(&$objForm, &$arg, $model, $db, $disabled, $row)
{
    //出欠の記録備考取込
    if ($model->cmd === 'torikomi3') {
        $set_remark = knje011eQuery::getSemesRemark($model, $db, $model->annual["YEAR"]);
        $row["ATTENDREC_REMARK"] = $set_remark;
    } elseif ($model->cmd === 'torikomi4') {
        $set_remark = knje011eQuery::getHreportremarkDetailDat($db, $model);
        $row["ATTENDREC_REMARK"] = $set_remark;
    }

    //出欠の記録備考
    $moji = $model->mojigyou["ATTENDREC_REMARK"]["moji"];
    $gyou = $model->mojigyou["ATTENDREC_REMARK"]["gyou"];
    $arg["ATTENDREC_REMARK"] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK", ($gyou + 1), ($moji * 2 + 1), "soft", "", $row["ATTENDREC_REMARK"]);
    $arg["ATTENDREC_REMARK_TYUI"] = "(全角{$moji}文字{$gyou}行まで)";
    if ($model->Properties["notUseAttendrecRemarkTokkiJikouNasi"] != "1") {
        $arg["useAttendrecRemarkTokkiJikouNasi"] = "1";
        //特記事項なしチェックボックス
        $extra = " id=\"INS_COMMENTS\" onclick=\"return insertComment(this, 'ATTENDREC_REMARK', 'INS_COMMENTS_LABEL');\"";
        $arg["INS_COMMENTS"] = knjCreateCheckBox($objForm, "INS_COMMENTS", "1", $extra, "");
        //特記事項なし
        $ins_comments_label = '特記事項なし';
        knjCreateHidden($objForm, "INS_COMMENTS_LABEL", $ins_comments_label);
        $arg["INS_COMMENTS_LABEL"] = $ins_comments_label;
    }
    //出欠の記録備考の「斜線を入れる」チェックボックス表示
    if ($model->Properties["useAttendrecRemarkSlashFlg"] == 1) {
        $arg["useAttendrecRemarkSlashFlg"] = 1;
    }
    //斜線を入れるチェックボックス
    $extra  = ($row["ATTENDREC_REMARK_SLASH_FLG"] == "1") ? "checked" : "";
    $extra .= " id=\"ATTENDREC_REMARK_SLASH_FLG\"";
    $arg["ATTENDREC_REMARK_SLASH_FLG"] = knjCreateCheckBox($objForm, "ATTENDREC_REMARK_SLASH_FLG", "1", $extra, "");

    //出欠の記録参照ボタン
    if ($model->Properties["tyousasyoSyukketsuKirokuBtn"] == 1) {
        $arg["btn_syukketsu_sansyo"] = makeSelectBtn($objForm, $model, "syukketsukiroku", "btn_syukketsu_sansyo", $model->attendTitle."の記録参照", "ATTENDREC_REMARK", $disabled);
        $arg["tyousasyoSyukketsuKirokuBtn"] = 1;
    }
    //出欠備考参照ボタン
    $sdate = $model->annual["YEAR"].'-04-01';
    $edate = ($model->annual["YEAR"]+1).'-03-31';
    //和暦表示フラグ
    $warekiFlg = "";
    if ($model->Properties["useWarekiHyoji"] == 1) {
        $warekiFlg = "1";
    }
    if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
        //まとめ出欠備考を取込みへ変更する
        if ($model->Properties["useTorikomiAttendSemesRemarkDat"] == 1) {
            $setname = $model->attendTitle.'備考全月取込';
            $extra = $disabled ." style=\"color:#1E90FF;font:bold\" onclick=\"return btn_submit('torikomi3');\"";
        } else {
            $setname = $model->attendTitle.'備考全月参照';
            $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->annual["YEAR"]}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
        }
        $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", $setname, $extra);
    } elseif ($model->getSchoolName === 'mieken') {
        $extra = $disabled ." style=\"color:#1E90FF;font:bold\" onclick=\"return btn_submit('torikomi4');\"";
        $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "通知票取込", $extra);
    } else {
        $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->annual["YEAR"]}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
        $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "日々".$model->attendTitle."備考参照", $extra);
    }
    //要録の出欠備考参照ボタン
    $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_HTRAINREMARK/index.php?YEAR={$model->annual["YEAR"]}&SCHREGNO={$model->schregno}',0,document.documentElement.scrollTop || document.body.scrollTop,360,180);return;\" style=\"width:210px;\"";
    $arg["YOROKU_SANSYO"] = KnjCreateBtn($objForm, "YOROKU_SANSYO", "要録の".$model->attendTitle."の記録備考参照", $extra);
    //年間出欠備考選択ボタン
    if ($model->Properties["useReasonCollectionBtn"] == 1) {
        $arg["btn_reason_collection_select"] = makeSelectBtn($objForm, $model, "reason_collection", "btn_reason_collection_select", "年間".$model->attendTitle."備考選択", "ATTENDREC_REMARK", $disabled);
        $arg["REASON_COLLECTION_SELECT"] = 1;
    }
}

//特別活動の記録
function makeSpecialactrec(&$objForm, &$arg, $model, $db, $disabled, $row)
{
    $extra = " class=\"specialactrec_\" ";
    $moji = $model->mojigyou["SPECIALACTREC"]["moji"];
    $gyou = $model->mojigyou["SPECIALACTREC"]["gyou"];
    $arg["SPECIALACTREC"] = KnjCreateTextArea($objForm, "SPECIALACTREC", ($gyou + 1), ($moji * 2 + 1), "soft", $extra, $row["SPECIALACTREC"]);
    $arg["SPECIALACTREC_TYUI"] = "(全角{$moji}文字{$gyou}行まで)";
    knjCreateHidden($objForm, "tyousasyoSpecialactrecFieldSizeDefaultPrint", $model->Properties["tyousasyoSpecialactrecFieldSizeDefaultPrint"]);

    //部活動選択ボタン
    if ($model->Properties["tyousashoShokenNyuryokuSpecialActNotUseClub"] != 1) {
        $arg["btn_club_spe"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "SPECIALACTREC", $disabled);
    }
    //委員会選択ボタン
    $arg["btn_committee"] = makeSelectBtn($objForm, $model, "committee", "btn_committee", "委員会選択", "SPECIALACTREC", $disabled);
    //賞選択ボタン
    if ($model->Properties["useHyosyoSansyoButton_H"]) {
        $arg["btn_hyosyo_spe"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo", "賞選択", "SPECIALACTREC", $disabled);
    }

    if ($model->Properties["useSpecialActivityMst"] == "1") {
        $arg["button"]["btn_special_activity_sele"] = makeSelectBtn($objForm, $model, "specialActivityMst", "btn_specialActivityMst", "特別活動選択", "SPECIALACTREC", $disabled);
    }

    if ($model->getSchoolName == "koma") {
        $arg["isKoma"] = "1";
        //マラソン大会
        $arg["btn_marathon"] = makeSelectBtn($objForm, $model, "marathon", "btn_marathon", "マラソン大会選択", "SPECIALACTREC", $disabled);
        //臘八摂心皆勤
        $rouhatsuKaikin = "";
        $query = knje011eQuery::getRouhatsuKaikin($model);
        $rouhatsuRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($rouhatsuRow["REC_CNT"] > 0 && $rouhatsuRow["REC_CNT"] == $rouhatsuRow["KAIKIN_CNT"]) {
            $rouhatsuKaikin = "臘八摂心皆勤";
        }
        knjCreateHidden($objForm, "ROUHATSU_KAIKIN", $rouhatsuKaikin);
        $extra = $disabled." onclick=\"document.forms[0].SPECIALACTREC.value += document.forms[0].ROUHATSU_KAIKIN.value\"";
        $arg["btn_rouhatsu"] = knjCreateBtn($objForm, "btn_rouhatsu", "臘八摂心皆勤", $extra);
    }
}


//指導上参考となる諸事項
function makeShojikou(&$objForm, &$arg, $model, $db, $disabled, $row)
{

    //一括参照ボタンを作成する
    $field = "TRAIN_REF_ALL";
    $arg["btn_formShojikouTori"] = makeSelectBtn($objForm, $model, "formShojikouTori", "btn_formShojikouTori", "指導上参考となる諸事項取込", $field, $disabled);

    $gyouMax = 0;
    for ($i = 1; $i <= 6; $i++) {
        $field = "TRAIN_REF".$i;
        $gyou = $model->mojigyou[$field]["gyou"];
        $gyouMax = $gyouMax < $gyou ? $gyou : $gyouMax;
    }
    if ($gyouMax < 15) {
        $style = " height: ".($gyouMax + 1)."em; overflow-y:hidden; ";
        $innerOverflowy = "scroll";
    } else {
        $style = " height: 15em; overflow-y:scroll; ";
        $innerOverflowy = "hidden";
    }
    $arg["SHOJIKOU_STYLE"] = $style;
    for ($i = 1; $i <= 6; $i++) {
        $field = "TRAIN_REF".$i;
        $moji = $model->mojigyou[$field]["moji"];
        $gyou = $model->mojigyou[$field]["gyou"];
        $height = $gyou * 13.5 + ($gyou -1 ) * 3 + 5;
        $extra = " id=\"".$field."\" style=\"height:{$height}px; overflow-y: {$innerOverflowy}; \"";
        $arg[$field] = KnjCreateTextArea($objForm, $field, ($gyou + 1), ($moji * 2 + 1), "soft", $extra, $row[$field]);
        $arg[$field."_COMMENT"] = "(全角{$moji}文字{$gyou}行まで)";

        //特記事項なし
        $extra = " id=\"".$field."_NO_COMM\" onclick=\"return CheckRemark('".$field."', '".$field."_NO_COMM');\" ";
        $arg[$field."_NO_COMM"] = knjCreateCheckBox($objForm, $field."_NO_COMM", "1", $extra, "");
    }

    $arg["NO_COMMENTS_LABEL"] = $model->no_comments_label;
    knjCreateHidden($objForm, "NO_COMMENTS_LABEL", $model->no_comments_label);

    // 指導要録 総合所見参照
    if ("1" == $model->Properties["seitoSidoYorokuSogoShoken3Bunkatsu"] || $model->getSchoolName == 'tokiwa') {
        $arg["seitoSidoYorokuSogoShoken3Bunkatsu"] = "1";
        $fields = array("TRAIN_REF1", "TRAIN_REF2", "TRAIN_REF3");
        $yoroku_trainref = array();
        foreach ($fields as $field) {
            $yoroku_trainref["YOROKU_".$field] = array();
        }

        $query = knje011eQuery::yoroku_trainref123($model, $model->annual["YEAR"]);
        $sansyou = $db->getRow($query, DB_FETCHMODE_ASSOC);

        foreach ($fields as $field) {
            $data = $sansyou[$field];
            if ($data) {
                $yoroku_trainref["YOROKU_".$field][] = $data;
            }
        }

        $prop = $model->Properties["seitoSidoYoroku_train_ref_1_2_3_field_size"];
        if ($prop == "") {
            $prop = $model->Properties["train_ref_1_2_3_field_size"];
        }

        $moji = array();
        if (get_count(preg_split("/-/", $prop)) == 3) {
            list($moji["TRAIN_REF1"], $moji["TRAIN_REF2"], $moji["TRAIN_REF3"]) = preg_split('/-/', $prop);
        } elseif ($prop == "1") {
            list($moji["TRAIN_REF1"], $moji["TRAIN_REF2"], $moji["TRAIN_REF3"]) = array(14, 21, 7);
        } elseif ($prop == "2") {
            list($moji["TRAIN_REF1"], $moji["TRAIN_REF2"], $moji["TRAIN_REF3"]) = array(21, 21, 7);
        } else {
            list($moji["TRAIN_REF1"], $moji["TRAIN_REF2"], $moji["TRAIN_REF3"]) = array(14, 14, 14);
        }
        $gyou = 5;
        $height = $gyou * 15;
        $extra = "style=\"background-color: #D0D0D0; height:{$height}px;\"";
        foreach ($fields as $field) {
            $keta = $moji[$field] * 2;
            $sen = "";
            for ($k = 0; $k < $keta; $k++) {
                $sen .= "-";
            }
            $sen = "\n".$sen."\n";
            $arg["YOROKU_".$field] = KnjCreateTextArea($objForm, "YOROKU_".$field, $gyou, $keta, "soft", $extra, implode($sen, $yoroku_trainref["YOROKU_".$field]));
        }
    } else {
        $arg["not_seitoSidoYorokuSogoShoken3Bunkatsu"] = "1";

        $query = knje011eQuery::yoroku_sogoshoken($model);
        $sansyou = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $row["TOTALREMARK"] = $sansyou["TOTALREMARK"];

        if ($model->Properties["seitoSidoYoroku_dat_TotalremarkSize"]) {
            list($moji, $gyou) = preg_split('/\*/', $model->Properties["seitoSidoYoroku_dat_TotalremarkSize"]);
            $model->mojigyou["TOTALREMARK"]["moji"] = (int)trim($moji);
            $model->mojigyou["TOTALREMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $model->mojigyou["TOTALREMARK"]["moji"] = 44; //デフォルトの値
            $model->mojigyou["TOTALREMARK"]["gyou"] = 6;  //デフォルトの値
        }

        $setHeight = $model->mojigyou["TOTALREMARK"]["gyou"] * 15;
        $arg["TOTALREMARK"]  = KnjCreateTextArea($objForm, "TOTALREMARK", 5, $model->mojigyou["TOTALREMARK"]["moji"] * 2 + 1, "soft", "style=\"background-color:#D0D0D0;height:{$setHeight}px;\"", $row["TOTALREMARK"]);
    }

    $arg["btn_club_tra3"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "TRAIN_REF3", $disabled);

    //賞選択ボタン
    if ($model->Properties["useHyosyoSansyoButton_H"]) {
        $arg["btn_hyosyo_tra5"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo", "賞選択", "TRAIN_REF5", $disabled);
    }
    //検定選択ボタン
    $arg["btn_qualified"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified", "検定選択", "TRAIN_REF4", $disabled);
    //記録備考選択ボタン
    if ($model->Properties["club_kirokubikou"] == 1) {
        //指導上参考となる諸事項
        $arg["btn_club_kirokubikou_tra3"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", "TRAIN_REF3", $disabled);
        $arg["btn_club_kirokubikou_tra5"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", "TRAIN_REF5", $disabled);
    }
}

//総合的な学習の時間
function makeSogo(&$objForm, &$arg, $model, $db, $disabled, $row)
{
        //1,2年次　指導要録、3年次　通知票取込
//        if ($model->cmd == 'reload4') {
//            //1,2年次　指導要録取込
//            if (intval($model->combo_gradecd) < "3") {
//                $rowYouroku = array();
//                $query = knje011eQuery::getYouroku($model, $model->annual["YEAR"]);
//                $rowYouroku = $db->getRow($query, DB_FETCHMODE_ASSOC);
//                $row["TOTALSTUDYACT"] = $rowYouroku["TOTALSTUDYACT"];
//                $row["TOTALSTUDYVAL"] = $rowYouroku["TOTALSTUDYVAL"];
//            //3年次　通知票取込
//            } else {
//                $totalstudytimeArray = array();
//                $totalstudyactArray  = array();
//                $query = knje011eQuery::get_record_totalstudytime_dat($model, $model->annual["YEAR"]);
//                $result = $db->query($query);
//                while ($total_row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
//                    if ($total_row["TOTALSTUDYTIME"] != '') {
//                        $totalstudytimeArray[] = $total_row["TOTALSTUDYTIME"];
//                    }
//                    if ($total_row["TOTALSTUDYACT"] != '') {
//                        $totalstudyactArray[] = $total_row["TOTALSTUDYACT"];
//                    }
//                }
//                $result->free();
//                if (get_count($totalstudytimeArray) > 0) {
//                    $row["TOTALSTUDYVAL"] = implode("\n", $totalstudytimeArray);
//                }
//                if (get_count($totalstudyactArray) > 0) {
//                    $row["TOTALSTUDYACT"] = implode("\n", $totalstudyactArray);
//                }
//            }
//        }

        //「総合的な学習の時間の内容・評価」 を表示するかしないかのフラグ
//        if ($model->Properties["tyousasyoSougouHyoukaNentani"] == 1 && "1" != $model->Properties["tyousasyo2020"]) {
//            //HTML側で表示・非表示の判定に使う
//            $arg["tyousasyoSougouHyoukaNentani"] = 1;
//
////            //総合的な学習の時間の「斜線を入れる」チェックボックス表示
////            if ($model->Properties["useTotalstudySlashFlg"] == 1) {
////                $arg["useTotalstudySlashFlg"] = 1;
////            }
//
//            //活動内容
//            $height = $model->mojigyou["TOTALSTUDYACT"]["gyou"] * 13.5 + ($model->mojigyou["TOTALSTUDYACT"]["gyou"] -1 ) * 3 + 5;
//            if ($model->getSchoolName === 'kyoto') {
//                if ($model->Properties["kyotoSougouNyuryokuOk"] == "1") {
//                    $extra = "style=\"height:{$height}px;\"";
//                } else {
//                    $extra = "style=\"height:{$height}px;background:darkgray\" readOnly";
//                }
//            } else {
//                $extra = "style=\"height:{$height}px;\"";
//            }
//            $arg["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", $model->mojigyou["TOTALSTUDYACT"]["gyou"], ($model->mojigyou["TOTALSTUDYACT"]["moji"] * 2 + 1), "soft", $extra, $row["TOTALSTUDYACT"]);
//            $arg["TOTALSTUDYACT_TYUI"] = "(全角{$model->mojigyou["TOTALSTUDYACT"]["moji"]}文字{$model->mojigyou["TOTALSTUDYACT"]["gyou"]}行まで)";
////            //斜線を入れるチェックボックス
////            $extra  = ($row["TOTALSTUDYACT_SLASH_FLG"] == "1") ? "checked" : "";
////            $extra .= " id=\"TOTALSTUDYACT_SLASH_FLG\"";
////            $arg["TOTALSTUDYACT_SLASH_FLG"] = knjCreateCheckBox($objForm, "TOTALSTUDYACT_SLASH_FLG", "1", $extra, "");
//
//            //評価
//            $height = $model->mojigyou["TOTALSTUDYVAL"]["gyou"] * 13.5 + ($model->mojigyou["TOTALSTUDYVAL"]["gyou"] -1 ) * 3 + 5;
//            if ($model->getSchoolName === 'kyoto') {
//                if ($model->Properties["kyotoSougouNyuryokuOk"] == "1") {
//                    $extra = "style=\"height:{$height}px;\"";
//                } else {
//                    $extra = "style=\"height:{$height}px;background:darkgray\" readOnly";
//                }
//            } else {
//                $extra = "style=\"height:{$height}px;\"";
//            }
//            $arg["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", $model->mojigyou["TOTALSTUDYVAL"]["gyou"], ($model->mojigyou["TOTALSTUDYVAL"]["moji"] * 2 + 1), "soft", $extra, $row["TOTALSTUDYVAL"]);
//            $arg["TOTALSTUDYVAL_TYUI"] = "(全角{$model->mojigyou["TOTALSTUDYVAL"]["moji"]}文字{$model->mojigyou["TOTALSTUDYVAL"]["gyou"]}行まで)";
////            //斜線を入れるチェックボックス
////            $extra  = ($row["TOTALSTUDYVAL_SLASH_FLG"] == "1") ? "checked" : "";
////            $extra .= " id=\"TOTALSTUDYVAL_SLASH_FLG\"";
////            $arg["TOTALSTUDYVAL_SLASH_FLG"] = knjCreateCheckBox($objForm, "TOTALSTUDYVAL_SLASH_FLG", "1", $extra, "");
//        }
//        //定型文選択ボタンを作成する
//        if ($model->Properties["Teikei_Button_Hyouji_Tyousasyo"] == "1") {
//            $extra = " onclick=\"loadwindow('../../X/KNJX_TEIKEIBUN_TYOUSASYO/knjx_teikeibun_tyousasyoindex.php?cmd=teikei_act&EXP_YEAR={$model->exp_year}&GRADE={$model->grade}', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY - 100 + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 550, 350);\" ";
//            $arg["btn_teikei_act"] = knjCreateBtn($objForm, "btn_teikei_act", "定型文選択", $extra);
//
//            $extra = " onclick=\"loadwindow('../../X/KNJX_TEIKEIBUN_TYOUSASYO/knjx_teikeibun_tyousasyoindex.php?cmd=teikei_val&EXP_YEAR={$model->exp_year}&GRADE={$model->grade}', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY - 250 + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 550, 350);\" ";
//            $arg["btn_teikei_val"] = knjCreateBtn($objForm, "btn_teikei_val", "定型文選択", $extra);
//        }

        //「総合的な学習の時間の内容・評価」 を表示するかしないかのフラグ
//        if ($model->Properties["tyousasyoSougouHyoukaNentani"] == 1) {
//            //1,2年次　指導要録取込、3年次　通知票取込み(総合的な学習の時間　通年用)
//            if (intval($model->combo_gradecd) > "2" && $model->Properties["tutihyoYoriYomikomiHyoujiFlg"] == 1) {
//                $extra = "onclick=\"return btn_submit('reload4');\" style=\"color:#1E90FF;font:bold;\"";
//                $arg["btn_reload4"] = knjCreateBtn($objForm, "btn_reload4", "通知票取込", $extra);
//            } else if (intval($model->combo_gradecd) < "3" && $model->Properties["tyousasyo_SidoYorokYoriYomikomiHyoujiFlg"] == 1) {
//                $extra = "onclick=\"return btn_submit('reload4');\" style=\"color:#1E90FF;font:bold;\"";
//                $arg["btn_reload4"] = knjCreateBtn($objForm, "btn_reload4", "指導要録取込", $extra);
//            }
//        }
}



//選択ボタン
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $disabled = "")
{
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        $extra = "";
        if ($div == "club") {       //部活動
            $tgt = "/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php";
            $w = 800;
            $h = 350;
        } elseif ($div == "committee") {       //委員会
            $tgt = "/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php";
            $w = 700;
            $h = 350;
        } elseif ($div == "qualified") {       //検定
            $tgt = "/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php";
            $w = 900;
            $h = 500;
        } elseif ($div == "hyosyo") {          //賞
            $tgt = "/X/KNJX_HYOSYO_SELECT/knjx_hyosyo_selectindex.php";
            $w = 600;
            $h = 350;
        } elseif ($div == "kirokubikou") {     //記録備考
            $tgt = "/X/KNJX_CLUB_KIROKUBIKOU_SELECT/knjx_club_kirokubikou_selectindex.php";
            $w = 800;
            $h = 350;
        } elseif ($div == "reason_collection") {   //年間出欠備考
            $tgt = "/X/KNJX_REASON_COLLECTION_SELECT/knjx_reason_collection_selectindex.php";
            $w = 800;
            $h = 350;
        } elseif ($div == "syukketsukiroku") {   //出欠の記録参照
            $tgt = "/X/KNJX_SYUKKETSUKIROKU/knjx_syukketsukirokuindex.php";
            $w = 800;
            $h = 350;
        } elseif ($div == "specialActivityMst") { //特別活動
            $tgt = "/X/KNJX_SPECIAL_ACTIVITY_SELECT/knjx_special_activity_selectindex.php";
            $w = 800;
            $h = 350;
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT.$tgt."?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$w},{$h});\"";
        } elseif (in_array($div, array("formShojikouTori"))) { // 指導上参考となる諸事項 一括取込
            $tgt = "/E/KNJE011E/knje011eindex.php";
            $w = 900;
            $h = 600;
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT.$tgt."?&cmd=".$div."&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$w},{$h});\"";
        } elseif ($div == "marathon") {   //マラソン大会選択
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_MARATHON_SELECT/knjx_marathon_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->annual["YEAR"]}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        }
        if ($extra == "") {
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT.$tgt."?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->annual["YEAR"]}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$w},{$h});\"";
        }

        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    if ($name == "SEMESTER") {
        $value = ($value) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];
    }

    if ($name == "ADD_YEAR_GRADE" || $name == "ADD_YEAR") {
        $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    }
}
