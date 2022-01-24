<?php

require_once("for_php7.php");

class knja125jForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knja125jindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            if (!in_array($model->cmd, array('value_set', 'reload2', 'reload_doutoku', 'torikomi3'))) {
                $row = $db->getRow(knja125jQuery::getTrainRow($model, ""), DB_FETCHMODE_ASSOC);
                $model->field["TOTALSTUDYACT_BG_COLOR_FLG"] = "";
                $model->field["TOTALSTUDYVAL_BG_COLOR_FLG"] = "";
                $model->field["REMARK1_BG_COLOR_FLG"] = "";
                $model->field["VIEWREMARK_BG_COLOR_FLG"] = "";
            } else {
                $row =& $model->field;
            }
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        $disabled = ($model->schregno) ? "" : "disabled";

        //読込みボタンが押された時の通知書より読込む
        if ($model->cmd == 'reload2') {
            $totalstudytimeArray = array();
            $totalstudyactArray  = array();
            $remark1Array  = array();
            if ($model->z010 == "KINJUNIOR") {
                $query = knja125jQuery::getKindaiSougakuKanten($model);
                $result = $db->query($query);
                while ($total_row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($total_row["VIEWNAME"] != '') {
                        // 観点
                        if (get_count($remark1Array) > 0) {
                            $remark1Array[] = "";
                        }
                        $remark1Array[] = $total_row["VIEWNAME"];
                    }
                }
                $result->free();

                $query = knja125jQuery::getKindaiTsushisyo($model, "SOUGAKU");
                $result = $db->query($query);
                while ($total_row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    foreach (array("REMARK1", "REMARK2", "REMARK3") as $field) {
                        if ($total_row[$field] != '') {
                            // 評価
                            $totalstudytimeArray[] = $total_row[$field];
                        }
                    }
                }
                $result->free();

                $totalstudyactArray[] = "総合探究";
            } else {
                $query = knja125jQuery::getRecordTotalstudytimeDat($model);
                $result = $db->query($query);
                while ($total_row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($total_row["TOTALSTUDYTIME"] != '') {
                        $totalstudytimeArray[] = $total_row["TOTALSTUDYTIME"];
                    }
                    if ($total_row["TOTALSTUDYACT"] != '') {
                        $totalstudyactArray[] = $total_row["TOTALSTUDYACT"];
                    }
                    if ($total_row["REMARK1"] != '') {
                        $remark1Array[] = $total_row["REMARK1"];
                    }
                }
            }
            $row["TOTALSTUDYVAL"] = implode("\n", $totalstudytimeArray);
            $row["TOTALSTUDYACT"] = implode("\n", $totalstudyactArray);
            $row["VIEWREMARK"] = implode("\n", $remark1Array);
        }
        if ($model->cmd == 'reload_doutoku') {
            if ($model->z010 == "KINJUNIOR") {
                $doutoku = array();
                $query = knja125jQuery::getKindaiTsushisyo($model, "DOUTOKU");

                $result = $db->query($query);
                while ($total_row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($total_row["REMARK1"] != '') {
                        $doutoku[] = $total_row["REMARK1"];
                    }
                }
                $result->free();

                $row["REMARK1"] = implode("\n", $doutoku);
            } elseif ($model->doutoku_classcd) {
                $doutoku = array();
                $query = knja125jQuery::getRecordTotalstudytimeDat($model, $model->doutoku_classcd);
                $result = $db->query($query);
                while ($total_row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($total_row["TOTALSTUDYTIME"] != '') {
                        $doutoku[] = $total_row["TOTALSTUDYTIME"];
                    }
                }
                $row["REMARK1"] = implode("\n", $doutoku);
            } elseif ($model->hreportremark) {
                $doutoku = array();
                $query = knja125jQuery::getHreportremarkDat2($model);
                $doutoku[] = $db->getOne($query);
                $row["REMARK1"] = implode("\n", $doutoku);
            } else {
                $field = array();
                //プロパティから読込テーブル・項目を取得
                if ($model->Properties["tutihyoDoutokuYomikomiField_J"]) {
                    list($tableName, $fieldName, $whereVal, $titleName) = explode("@", $model->Properties['tutihyoDoutokuYomikomiField_J']);
                    $field = array("TABLE" => $tableName, "FIELD" => $fieldName, "WHERE" => $whereVal, "TITLE" => $titleName);
                } else {
                    //プロパティの設定がない場合、デフォルトの項目
                    $field = array("TABLE" => "HREPORTREMARK_DAT", "FIELD" => "REMARK2", "WHERE" => "", "TITLE" => "道徳");
                }
                if ($field['TABLE'] == 'HREPORTREMARK_DAT') {
                    $query = knja125jQuery::getDoutokuHreportremarkDat($model, $field['FIELD']);
                } else {
                    list($div, $code) = explode(':', $field['WHERE']);
                    $query = knja125jQuery::getDoutokuHreportremarkDetailDat($model, $field['FIELD'], $div, $code);
                }
                $doutoku = array();
                $doutoku[] = $db->getOne($query);
                $row["REMARK1"] = implode("\n", $doutoku);
            }
        }

        if ($model->Properties["Totalremark_2disp_J"] == '1') {
            $arg['kansai'] = true;
            $jsTarget = 'REMARK1_009';
        } else {
            $arg['non_kansai'] = true;
            $jsTarget = 'TOTALREMARK';
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //出欠の記録備考取込
        if ($model->cmd === 'torikomi3') {
            $set_remark = knja125jQuery::getSemesRemark($db, $model);
            $row["ATTENDREC_REMARK"] = $set_remark;
        }

        //道徳
        if ($model->field["REMARK1_BG_COLOR_FLG"]) {
            $extra = " background-color:#FFCCFF ";
        } else {
            $extra = "";
        }
        if ($model->Properties["HTRAINREMARK_TEMP_SCORE_MST_J"] == '1' && $row["REMARK1"] == '') {
            $row["REMARK1"] = $db->getOne(knja125jQuery::getHtrainremarkScoreDat($model, '10'));
            $extra = " background-color:#FFCCFF ";
        }
        $arg["data"]["REMARK1"] = getTextOrArea($objForm, "REMARK1", $model->remark1_moji, $model->remark1_gyou, $row["REMARK1"], $model, $extra);
        $arg["data"]["REMARK1_COMMENT"] = "(全角".$model->remark1_moji."文字X".$model->remark1_gyou."行まで)";
        knjCreateHidden($objForm, "REMARK1_KETA", $model->remark1_moji * 2);
        knjCreateHidden($objForm, "REMARK1_GYO", $model->remark1_gyou);
        KnjCreateHidden($objForm, "REMARK1_STAT", "statusarea9");

        //総合所見（３分割）
        if ($model->Properties["train_ref_1_2_3_use_J"] == '1') {
            $arg["show_train_ref_1_2_3_use_J"] = "1";
        } else {
            $arg["show_totalremark"] = "1";
        }

        //総合所見（３分割）
        //1)学習における特徴等　2)行動の特徴、特技等
        $arg["data"]["TRAIN_REF1"] = getTextOrArea($objForm, "TRAIN_REF1", $model->train_ref1_moji, $model->train_ref1_gyou, $row["TRAIN_REF1"], $model);
        $arg["data"]["TRAIN_REF1_COMMENT"] = "(全角".$model->train_ref1_moji."文字X".$model->train_ref1_gyou."行まで)";
        //3)部活動、ボランティア活動等　4)取得資格、検定等
        $arg["data"]["TRAIN_REF2"] = getTextOrArea($objForm, "TRAIN_REF2", $model->train_ref2_moji, $model->train_ref2_gyou, $row["TRAIN_REF2"], $model);
        $arg["data"]["TRAIN_REF2_COMMENT"] = "(全角".$model->train_ref2_moji."文字X".$model->train_ref2_gyou."行まで)";
        //5)その他
        $arg["data"]["TRAIN_REF3"] = getTextOrArea($objForm, "TRAIN_REF3", $model->train_ref3_moji, $model->train_ref3_gyou, $row["TRAIN_REF3"], $model);
        $arg["data"]["TRAIN_REF3_COMMENT"] = "(全角".$model->train_ref3_moji."文字X".$model->train_ref3_gyou."行まで)";

        if ($model->cmd == 'zentorikomi') {
            $remarkText = '';
            if ($row['ANNUAL'] =='01') {
                $row2 = $db->getRow(knja125jQuery::getNyuusiZyunni($model), DB_FETCHMODE_ASSOC);
                $remarkText .= '入試順位:' . $row2['REMARK1'] . "\n";
                $row2 = $db->getRow(knja125jQuery::getIQ($model), DB_FETCHMODE_ASSOC);
                $remarkText .= 'IQ:' . $row2['IQ']."\n";
                $row2 = $db->getRow(knja125jQuery::getGakunenZyunni($model), DB_FETCHMODE_ASSOC);
                $remarkText .= '学年順位:' . $row2['GRADE_RANK'] . "\n";
            }
            if ($row['ANNUAL'] =='02' || $row['ANNUAL'] =='03') {
                $row2 = $db->getRow(knja125jQuery::getGakunenZyunni($model), DB_FETCHMODE_ASSOC);
                $remarkText .= '学年順位:' . $row2['GRADE_RANK'] . "\n";
            }
            $result = $db->query(knja125jQuery::getCommittee($model));
            while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $remarkText .= $row2['SEMESTER_SHOW']." ";
                $remarkText .= $row2['COMMITTEE_SHOW']." ";
                $remarkText .= $row2['CHARGE_SHOW']." ";
                $remarkText .= $row2['EXECUTIVE_SHOW']."\n";
            }
            $result->free();
            $result = $db->query(knja125jQuery::getHyosyo($model));
            while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $remarkText .= $row2['DETAIL_SDATE']." ";
                $remarkText .= $row2['DETAILCDNAME']." ";
                $remarkText .= $row2['CONTENT']." ";
                $remarkText .= $row2['REMARK']."\n";
            }
            $result->free();
            $result = $db->query(knja125jQuery::getBatsu($model));
            while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $remarkText .= $row2['DETAIL_SDATE']." ";
                $remarkText .= $row2['DETAILCDNAME']." ";
                $remarkText .= $row2['CONTENT']." ";
                $remarkText .= $row2['REMARK']."\n";
            }
            $result->free();
            $row['REMARK1_009'] = $remarkText;
        }

        //総合所見及び指導上参考となる諸事項
        $extra = "style=\"height:150px;\"";
        $arg["data"]["REMARK1_009"] = getTextOrArea($objForm, "REMARK1_009", $model->remark009_moji, $model->remark009_gyou, $row["REMARK1_009"], $model);
        $arg["data"]["REMARK1_009_COMMENT"] = "(全角".$model->remark009_moji."文字X".$model->remark009_gyou."行まで)";

        //総合所見及び指導上参考となる諸事項
        $extra = "style=\"height:150px;\"";
        $arg["data"]["TOTALREMARK"] = getTextOrArea($objForm, "TOTALREMARK", $model->totalremark_moji, $model->totalremark_gyou, $row["TOTALREMARK"], $model);
        $arg["data"]["TOTALREMARK_COMMENT"] = "(全角".$model->totalremark_moji."文字X".$model->totalremark_gyou."行まで)";

        //総合所見用の定型ボタン作成 (プロパティ設定時)
        if ($model->Properties["TotalRemark_HTRAINREMARK_TEMP_DAT"] == "1") {
            $arg["TEIKEI_FLG"] = "1";
            $model->createTeikeiBtn($arg, $objForm, "12-13", "総合所見", "TOTALREMARK");
        } elseif ($model->Properties["seitoSidoYorokuSougou_Teikei_Button_Hyouji"] == "1") {
            $extra = " onclick=\"loadwindow('../../X/KNJX_TEIKEIBUN/knjx_teikeibunindex.php?";
            $extra .= "cmd=teikei&EXP_YEAR={$model->exp_year}&GRADE={$model->grade}&DATA_DIV=12&TITLE=総合所見&TEXTBOX=".$jsTarget."'";
            $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 550, 350);\" ";
            $arg["button"]["btn_teikei_TotalRemark"] = knjCreateBtn($objForm, "btn_teikeiTOTALREMARK", "定型文選択", $extra.$disabled);
        }

        //学習活動
        $extra = "style=\"height:118px;";
        if ($model->Properties["HTRAINREMARK_TEMP_SCORE_MST_J"] == '1' && $row["TOTALSTUDYACT"] == '') {
            $row["TOTALSTUDYACT"] = $db->getOne(knja125jQuery::getHtrainremarkScoreDat($model, '21'));
            $extra .= "background-color:#FFCCFF\"";
        } elseif ($model->field["TOTALSTUDYACT_BG_COLOR_FLG"]) {
            $extra .= "background-color:#FFCCFF\"";
        } else {
            $extra .= "\"";
        }
        $extra .=" onkeyup=\"charCount(this.value, {$model->totalstudyact_gyou}, ({$model->totalstudyact_moji} * 2 + 1), true);\"";
        $arg["data"]["TOTALSTUDYACT"] = knjCreateTextArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_gyou, ($model->totalstudyact_moji * 2 + 1), "soft", $extra, $row["TOTALSTUDYACT"]);
        $arg["data"]["TOTALSTUDYACT_COMMENT"] = "(全角".$model->totalstudyact_moji."文字X".$model->totalstudyact_gyou."行まで)";

        //観点
        $extra = "style=\"height:118px;";
        if ($model->Properties["HTRAINREMARK_TEMP_SCORE_MST_J"] == '1' && $row["VIEWREMARK"] == '') {
            $row["VIEWREMARK"] = $db->getOne(knja125jQuery::getHtrainremarkScoreDat($model, '22'));
            $extra .= "background-color:#FFCCFF\"";
        } elseif ($model->field["VIEWREMARK_BG_COLOR_FLG"]) {
            $extra .= "background-color:#FFCCFF\"";
        } else {
            $extra .= "\"";
        }
        $extra .= "onkeyup=\"charCount(this.value, {$model->viewremark_gyou}, ({$model->viewremark_moji} * 2 + 1), true);\"";
        $arg["data"]["VIEWREMARK"] = knjCreateTextArea($objForm, "VIEWREMARK", $model->viewremark_gyou, ($model->viewremark_moji * 2 + 1), "soft", $extra, $row["VIEWREMARK"]);
        $arg["data"]["VIEWREMARK_COMMENT"] = "(全角".$model->viewremark_moji."文字X".$model->viewremark_gyou."行まで)";

        //評価
        $extra = "style=\"height:118px;";
        if ($model->Properties["HTRAINREMARK_TEMP_SCORE_MST_J"] == '1' && $row["TOTALSTUDYVAL"] == '') {
            $row["TOTALSTUDYVAL"] = $db->getOne(knja125jQuery::getHtrainremarkScoreDat($model, '23'));
            $extra .= "background-color:#FFCCFF\"";
        } elseif ($model->field["TOTALSTUDYVAL_BG_COLOR_FLG"]) {
            $extra .= "background-color:#FFCCFF\"";
        } else {
            $extra .= "\"";
        }
        $extra .=" onkeyup=\"charCount(this.value, {$model->totalstudyval_gyou}, ({$model->totalstudyval_moji} * 2 + 1), true);\"";
        $arg["data"]["TOTALSTUDYVAL"] = knjCreateTextArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_gyou, ($model->totalstudyval_moji * 2 + 1), "soft", $extra, $row["TOTALSTUDYVAL"]);
        $arg["data"]["TOTALSTUDYVAL_COMMENT"] = "(全角".$model->totalstudyval_moji."文字X".$model->totalstudyval_gyou."行まで)";

        //行動の記録・特別活動の記録ボタン
        $extra = "onclick=\"return btn_submit('form2');\"";
        $arg["button"]["btn_form2"] = KnjCreateBtn($objForm, "btn_form2", "行動の記録・特別活動の記録", $extra);

        if ($row['ATTENDREC_REMARK'] == '' && $model->kansai) {
            $kaikinText = '';
            $result = $db->query(knja125jQuery::getKaikinData($model));
            while ($specalactrow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $kaikinText .= $specalactrow['KAIKIN']."\n";
            }
            $row['ATTENDREC_REMARK'] = $kaikinText;
        }

        //出欠の記録備考
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_moji, $model->attendrec_remark_gyou, $row["ATTENDREC_REMARK"], $model);
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->attendrec_remark_moji."文字X".$model->attendrec_remark_gyou."行まで)";
        if ($model->Properties["knje125j_Chinokensa_show"] == '1') {
            //知能検査偏差値
            $arg["knje125j_Chinokensa_show"] = "1";
            $extra = " onblur=\"CheckInteger(this);\"";
            $arg["data"]["DEVIATION"] = knjCreateTextBox($objForm, $row["DEVIATION"], "DEVIATION", 6, 6, $extra);
        }

        //出欠の記録参照ボタン
        if ($model->Properties["tyousasyoSyukketsuKirokuBtn"] == 1) {
            $extra = $disabled." onclick=\"return btn_submit('syukketsu');\"";
            $arg["button"]["btn_subform2"] = knjCreateBtn($objForm, "btn_subform2", "出欠の記録参照", $extra);
        } else {
            $extra = $disabled." onclick=\"return btn_submit('subform2');\"";
            $arg["button"]["btn_subform2"] = KnjCreateBtn($objForm, "btn_subform2", "出欠の記録参照", $extra);
        }

        //出欠備考参照ボタン
        $sdate = $model->exp_year.'-04-01';
        $edate = ($model->exp_year+1).'-03-31';
        //和暦表示
        $warekiFlg = "";
        if ($model->Properties["useWarekiHyoji"] == 1) {
            $warekiFlg = "1";
        }
        if ($model->Properties["useAttendSemesRemarkDat_J"] == 1) {
            //まとめ出欠備考を取込みへ変更する
            if ($model->Properties["useTorikomiAttendSemesRemarkDat_J"] == 1) {
                $setname = 'まとめ出欠備考取込';
                $extra = " style=\"color:#1E90FF;font:bold\" ";
            } else {
                $setname = 'まとめ出欠備考参照';
                $extra = "";
            }
            //まとめ出欠備考を取込みへ変更する
            if ($model->Properties["useTorikomiAttendSemesRemarkDat_J"] == 1) {
                $extra .= $disabled ." onclick=\"return btn_submit('torikomi3');\"";
            } else {
                $extra .= $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            }
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", $setname, $extra);
        } else {
            $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "日々出欠備考参照", $extra);
        }

        //年間出欠備考選択ボタン
        if ($model->Properties["useReasonCollectionBtn"] == 1) {
            $arg["btn_reason_collection_select"] = makeSelectBtn($objForm, $model, "reason_collection", "btn_reason_collection_select", "年間出欠備考選択", "ATTENDREC_REMARK", $disabled);
            $arg["REASON_COLLECTION_SELECT"] = 1;
        }

        //中学で履修済み備考
        if ($model->z010 == 'teihachi') {
            $extra = " onkeyup=\"charCount(this.value, {$model->remark1_002_gyou}, ({$model->remark1_002_moji} * 2 + 1), true);\"";
            $arg["data"]["REMARK1_002"] = KnjCreateTextArea($objForm, "REMARK1_002", $model->remark1_002_gyou, ($model->remark1_002_moji * 2 + 1), "soft", $extra, $row["REMARK1_002"]);
            $arg["data"]["REMARK1_002_COMMENT"] = '(全角59文字X5行まで)';
        }

        //通知表所見参照ボタン
        $sapporo = $db->getOne(knja125jQuery::getSapporoHantei());
        if ($sapporo == 0) {
            $arg["subform1"] = 1;
        }
        if ($model->Properties["unUseSyokenSansyoButton_J"] == '1') {
            $arg["subform1"] = "";
        }
        $extra = $disabled." onclick=\"return btn_submit('subform1');\"";
        $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "通知表所見参照", $extra);

        //学校種別
        $schoolkind = $db->getOne(knja125jQuery::getSchoolKind($model));

        if ((AUTHORITY < DEF_UPDATE_RESTRICT) || $schoolkind != 'J') {
            //更新ボタン
            $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "disabled");
            //前の生徒へボタン
            $extra = "style=\"width:130px\" onclick=\"top.left_frame.nextStudentOnly('pre');\"";
            $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "前の生徒へ", $extra);
            //次の生徒へボタン
            $extra = "style=\"width:130px\" onclick=\"top.left_frame.nextStudentOnly('next');\"";
            $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "次の生徒へ", $extra);
        } else {
            //更新ボタン
            $extra = "onclick=\"return btn_submit('update');\"";
            $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
            //更新後前の生徒へボタン
            $arg["button"]["btn_up_next"] = View::updateNext($model, $objForm, 'btn_update');
        }

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        // 近大中学のみ表示
        $arg["KINJUNIOR"] = $model->kinJunior;
        if ($model->kinJunior == "1") {
            $extra = " onclick=\"return btn_submit('subform3');\"";
            $arg["button"]["subform3"] = knjCreateBtn($objForm, "subform3", "通知票の参照", $extra);
        }
        //既入力内容参照（道徳）
        $extra = " onclick=\"return btn_submit('shokenlist4');\"";
        $arg["button"]["shokenlist4"] = knjCreateBtn($objForm, "shokenlist4", "既入力内容の参照", $extra);
        //既入力内容参照（総合的な学習時間）
        $extra = " onclick=\"return btn_submit('shokenlist1');\"";
        $arg["button"]["shokenlist1"] = knjCreateBtn($objForm, "shokenlist1", "既入力内容の参照", $extra);
        //既入力内容参照（総合所見）
        $extra = " onclick=\"return btn_submit('shokenlist2');\"";
        $arg["button"]["shokenlist2"] = knjCreateBtn($objForm, "shokenlist2", "既入力内容の参照", $extra);
        //既入力内容参照（出欠の記録備考）
        $extra = " onclick=\"return btn_submit('shokenlist3');\"";
        $arg["button"]["shokenlist3"] = knjCreateBtn($objForm, "shokenlist3", "既入力内容の参照", $extra);

        if ($model->Properties["useHyosyoSansyoButton_J"]) {
            //賞選択ボタン
            $arg["button"]["btn_hyosyo"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo", "賞選択", $jsTarget, $disabled);
            //罰選択ボタン
            $arg["button"]["btn_batsu"] = makeSelectBtn($objForm, $model, "batsu", "btn_batsu", "罰選択", $jsTarget, $disabled);
        }

        //CSV処理
        $fieldSize  = "REMARK1=".($model->remark1_moji * $model->remark1_gyou * 3).",";
        $fieldSize .= "TOTALSTUDYACT=".($model->totalstudyact_moji * $model->totalstudyact_gyou * 3).",";
        $fieldSize .= "VIEWREMARK=".($model->viewremark_moji * $model->viewremark_gyou * 3).",";
        $fieldSize .= "TOTALSTUDYVAL=".($model->totalstudyval_moji * $model->totalstudyval_gyou * 3).",";
        //総合所見（３分割）
        if ($model->Properties["train_ref_1_2_3_use_J"] == '1') {
            $fieldSize .= "TRAIN_REF1=".($model->train_ref1_moji * $model->train_ref1_gyou * 3).",";
            $fieldSize .= "TRAIN_REF2=".($model->train_ref2_moji * $model->train_ref2_gyou * 3).",";
            $fieldSize .= "TRAIN_REF3=".($model->train_ref3_moji * $model->train_ref3_gyou * 3).",";
        } else {
            if ($model->Properties["Totalremark_2disp_J"] == "1") {
                $fieldSize .= "D2_009_REMARK1=".($model->remark009_moji * $model->remark009_gyou * 3).",";
            }
            $fieldSize .= "TOTALREMARK=".($model->totalremark_moji * $model->totalremark_gyou * 3).",";
            if ($model->Properties["knja125j_Sougoushoken_TutisyoShoken_Button_Hyouji"] == '1') {
                $arg["show_TutisyoShoken_Button"] = "1";
                $arg["button"]["btn_tuuchihyousansyou"] = makeSelectBtn($objForm, $model, "tuuchihyousansyou", "btn_tuuchihyousansyou", "通知票参照", $jsTarget, $disabled);
            }
        }

        $fieldSize .= "ATTENDREC_REMARK=".($model->attendrec_remark_moji * $model->attendrec_remark_gyou * 3).",";
        if ($model->Properties["Specialactremark_3disp_J"] == '1') {
            $fieldSize .= "CLASSACT=".(17 * 3 * 3).",";
            $fieldSize .= "STUDENTACT=".(17 * 3 * 3).",";
            $fieldSize .= "SCHOOLEVENT=".(17 * 3 * 3);
        } else {
            $fieldSize .= "SPECIALACTREMARK=".(17 * 10 * 3);
        }
        //CSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_A125J/knjx_a125jindex.php?FIELDSIZE=".$fieldSize."&AUTH=".AUTHORITY."&PRGID=".PROGRAMID."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "データCSV", $extra);

        //行動の記録参照
        $extra = $disabled." onclick=\"return btn_submit('act_doc');\"";
        $arg["button"]["btn_actdoc"] = knjCreateBtn($objForm, "btn_actdoc", "行動の記録参照", $extra);

        //部活動選択ボタン
        $arg["button"]["btn_club"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", $jsTarget, $disabled);

        //記録備考選択ボタン
        if ($model->Properties["club_kirokubikou"] == 1) {
            $arg["button"]["btn_club_kirokubikou"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", $jsTarget, $disabled);
        }

        //委員会選択ボタン
        $arg["button"]["btn_committee"] = makeSelectBtn($objForm, $model, "committee", "btn_committee", "委員会選択", $jsTarget, $disabled);

        //検定選択ボタン
        $arg["button"]["btn_qualified"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified", "検定選択", $jsTarget, $disabled);

        //定型文選択ボタンを作成する
        if ($model->Properties["HTRAINREMARK_TEMP_SCORE_MST_J"] != '1') {
            if ($model->Properties["Teikei_Button_Hyouji"] == "1") {
                $extra = "onclick=\"return btn_submit('teikei');\"";
                $arg["button"]["btn_teikei"] = knjCreateBtn($objForm, "btn_teikei", "定型文選択", $extra);
            }

            if ($model->Properties["Teikei_Button_Hyouji"] == "1") {
                $extra = "onclick=\"return btn_submit('teikei_act');\"";
                $arg["button"]["btn_teikei_act"] = knjCreateBtn($objForm, "btn_teikei_act", "定型文選択", $extra);

                $extra = "onclick=\"return btn_submit('teikei_val');\"";
                $arg["button"]["btn_teikei_val"] = knjCreateBtn($objForm, "btn_teikei_val", "定型文選択", $extra);
            }
        }

        //通知票取込ボタン
        if ($model->Properties["HTRAINREMARK_TEMP_SCORE_MST_J"] != 1) {
            if ($model->Properties["tutihyoYoriYomikomiHyoujiFlg_J"] == 1) {
                $extra = "onclick=\"return btn_submit('reload2');\" style=\"color:#1E90FF;font:bold;\"";
                $arg["button"]["btn_reload2"] = knjCreateBtn($objForm, "btn_reload2", "通知票取込", $extra);

                if ($model->doutoku_classcd) {
                    $extra = "onclick=\"return btn_submit('reload_doutoku');\" style=\"color:#1E90FF;font:bold;\"";
                    $arg["button"]["btn_reload_doutoku"] = knjCreateBtn($objForm, "btn_reload_doutoku", "通知票取込", $extra);
                }
            }
            if ($model->hreportremark) {
                $extra = "onclick=\"return btn_submit('reload_doutoku');\" style=\"color:#1E90FF;font:bold;\"";
                $arg["button"]["btn_reload_doutoku"] = knjCreateBtn($objForm, "btn_reload_doutoku", "通知票取込", $extra);
            }
            if ($model->Properties["tutihyoDoutokuYomikomiHyoujiFlg_J"] == "1") {
                $extra = "onclick=\"return btn_submit('reload_doutoku');\" style=\"color:#1E90FF;font:bold;\"";
                $arg["button"]["btn_reload_doutoku"] = knjCreateBtn($objForm, "btn_reload_doutoku", "通知票取込", $extra);
            }
        }
        $extra = $disabled . " onclick=\"return btn_submit('zentorikomi');\"";
        $arg["button"]["btn_zentorikomi"] = knjCreateBtn($objForm, "btn_zentorikomi", "全取込", $extra);

        $extra = $disabled . " onclick=\"return btn_submit('subform4');\"";
        $arg["button"]["btn_tokubetukatudou"] = knjCreateBtn($objForm, "btn_tokubetukatudou", "調査書特別活動参照", $extra);


        //hidden
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SEMES_CNT", $model->control["学期数"]);

        $arg["IFRAME"] = VIEW::setIframeJs();

        if (strlen($model->warning)== 0 && $model->cmd !="clear") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd =="clear") {
            $arg["next"] = "NextStudent(1);";
        }

        knjCreateHidden($objForm, "TOTALSTUDYACT_BG_COLOR_FLG", $model->field["TOTALSTUDYACT_BG_COLOR_FLG"]);
        knjCreateHidden($objForm, "TOTALSTUDYVAL_BG_COLOR_FLG", $model->field["TOTALSTUDYVAL_BG_COLOR_FLG"]);
        knjCreateHidden($objForm, "VIEWREMARK_BG_COLOR_FLG", $model->field["VIEWREMARK_BG_COLOR_FLG"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja125jForm1.html", $arg);
    }
}

//テキストボックスorテキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model, $style = '')
{
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $minusHasu = 0;
        $minus = 0;
        if ($gyou >= 5) {
            $minusHasu = $gyou % 5;
            $minus = ($gyou / 5) > 1 ? ($gyou / 5) * 6 : 5;
        }
        $height = $gyou * 13.5 + ($gyou -1) * 3 + (5 - ($minus + $minusHasu));
        $extra = "style=\"height:".$height."px;" . $style . "\" onkeyup=\"charCount(this.value, $gyou, ($moji * 2) + 1, true);\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"return btn_keypress();\" style='" . $style . "'";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

//選択ボタン
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $disabled = "")
{
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        if ($div == "club") {                   //部活動
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "committee") {       //委員会
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"";
        } elseif ($div == "qualified") {       //検定
            $sizeW = ($model->Properties["useQualifiedMst"] == "1") ? 800 : 670;
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$sizeW},500);\"";
        } elseif ($div == "hyosyo") {          //賞
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_HYOSYO_SELECT/knjx_hyosyo_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,600,350);\"";
        } elseif ($div == "batsu") {           //罰
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_BATSU_SELECT/knjx_batsu_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,600,350);\"";
        } elseif ($div == "kirokubikou") {     //記録備考
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_KIROKUBIKOU_SELECT/knjx_club_kirokubikou_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "reason_collection") {   //年間出欠備考
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_REASON_COLLECTION_SELECT/knjx_reason_collection_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "tuuchihyousansyou") {
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_TUUCHISYOKEN_SELECT/knjx_tuuchisyoken_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,900,350);\"";
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}
