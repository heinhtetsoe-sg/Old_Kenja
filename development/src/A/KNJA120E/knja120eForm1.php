<?php
class knja120eForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knja120eindex.php", "", "edit");
        $db = Query::dbCheckOut();

        if ($model->Properties["hide_TRAIN_REF1_button"] != "1") {
            $arg["hide_TRAIN_REF1_button"] = "1";
        }
        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            if ($model->cmd !== 'torikomi3' && $model->cmd !== 'reload' && $model->cmd !== 'reload2' && $model->cmd !== 'value_set') {
                $row = knja120eQuery::getTrainRow($db, $model->schregno, $model->exp_year);
                if (!is_array($row)) {
                    $row = array();
                }

                $query = knja120eQuery::getHtrainTrainRef($model);
                $result = $db->query($query);
                while ($getRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($getRow["REMARK"]) {
                        $seq = $getRow["TRAIN_SEQ"];
                        if ($seq == "101") {
                            $field = "TRAIN_REF1";
                        } elseif ($seq == "102") {
                            $field = "TRAIN_REF2";
                        } elseif ($seq == "103") {
                            $field = "TRAIN_REF3";
                        } elseif ($seq == "104") {
                            $field = "TRAIN_REF4";
                        } elseif ($seq == "105") {
                            $field = "TRAIN_REF5";
                        } elseif ($seq == "106") {
                            $field = "TRAIN_REF6";
                        }
                        $row[$field] = $getRow["REMARK"];
                    }
                }
                $result->free();
                $row['ZIRITUKATUDOU'] = knja120eQuery::getHtrainremarkDetail2Dat($db, $model->schregno, $model->exp_year);

                $model->field["TOTALSTUDYACT_BG_COLOR_FLG"] = "";
                $model->field["TOTALSTUDYVAL_BG_COLOR_FLG"] = "";
                $model->field["SPECIALACTREMARK_BG_COLOR_FLG"] = "";
                $model->field["TRAIN_REF1_BG_COLOR_FLG"] = "";
                $model->field["TRAIN_REF2_BG_COLOR_FLG"] = "";
                $model->field["TRAIN_REF3_BG_COLOR_FLG"] = "";
                $model->field["ATTENDREC_REMARK_BG_COLOR_FLG"] = "";
            } else {
                $row =& $model->field;
            }
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        if ($db->getOne(knja120eQuery::getSchregBaseMstHandicap($model)) == '002') {
            $arg['ZIRITUKATUDOU_FLG'] = true;
        }

        //卒業可能な学年か判定
        $getData = knja120eQuery::getGraduationGrade($db, $model);
        $model->GradGrade = "";
        $model->GradGrade = $getData["FLG"];
        $model->schoolKind = "";
        $model->schoolKind = $getData["SCHOOL_KIND"];

        $disabled = ($model->schregno) ? "" : "disabled";

        //調査書より読込ボタンを作成する
        if ($model->GradGrade == "1") {
            $setColor = "style=\"color:#1E90FF;font:bold\"";
            $arg["chousasho_yomikomi"] = "1";
            $extra = $disabled." onclick=\" return btn_submit('reload');\"".$setColor;
            $arg["btn_reload"] = KnjCreateBtn($objForm, "btn_reload", "調査書より読込", $extra);
            if ($model->cmd === 'reload') {
                $model->field["TOTALSTUDYACT_BG_COLOR_FLG"] = "1";
                $model->field["TOTALSTUDYVAL_BG_COLOR_FLG"] = "1";
                $model->field["SPECIALACTREMARK_BG_COLOR_FLG"] = "1";
                $model->field["TRAIN_REF1_BG_COLOR_FLG"] = "1";
                $model->field["TRAIN_REF2_BG_COLOR_FLG"] = "1";
                $model->field["TRAIN_REF3_BG_COLOR_FLG"] = "1";
                $model->field["ATTENDREC_REMARK_BG_COLOR_FLG"] = "1";
                $getRow = knja120eQuery::getHexamEntremark($db, $model);
                //総合的な学習の時間　活動、評価
                if ($model->Properties["tyousasyoSougouHyoukaNentani"] !== '1') {
                    $row["TOTALSTUDYACT"]       = $getRow["TOTALSTUDYACT"];
                    $row["TOTALSTUDYVAL"]       = $getRow["TOTALSTUDYVAL"];
                } else {
                    //年単位の時
                    $row["TOTALSTUDYACT"]       = $getRow["TOTALSTUDYACT_YEAR"];
                    $row["TOTALSTUDYVAL"]       = $getRow["TOTALSTUDYVAL_YEAR"];
                }
                //特別活動
                $row["SPECIALACTREMARK"]   = $getRow["SPECIALACTREC"];
                //出欠の記録備考
                $row["ATTENDREC_REMARK"]    = $getRow["ATTENDREC_REMARK"];

                //総合所見(6分割から取込)
                $query = knja120eQuery::getHexamTrainRef($model);
                $result = $db->query($query);
                while ($getRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($getRow["REMARK"]) {
                        $seq = $getRow["TRAIN_SEQ"];
                        if ($seq == "101") {
                            $field = "TRAIN_REF1";
                        } elseif ($seq == "102") {
                            $field = "TRAIN_REF2";
                        } elseif ($seq == "103") {
                            $field = "TRAIN_REF3";
                        } elseif ($seq == "104") {
                            $field = "TRAIN_REF4";
                        } elseif ($seq == "105") {
                            $field = "TRAIN_REF5";
                        } elseif ($seq == "106") {
                            $field = "TRAIN_REF6";
                        }
                        $row[$field] = $getRow["REMARK"];
                    }
                }
                $result->free();
            }
        }

        //通知票　調査書取込
        if ($model->cmd === 'reload2') {
            $model->field["TOTALSTUDYACT_BG_COLOR_FLG"] = "1";
            $model->field["TOTALSTUDYVAL_BG_COLOR_FLG"] = "1";
            //3年次　調査書
            if ($model->GradGrade == "1") {
                $getRow = array();
                $getRow = knja120eQuery::getHexamEntremark($db, $model);
                //総合的な学習の時間　活動、評価
                if ($model->Properties["tyousasyoSougouHyoukaNentani"] !== '1') {
                    $row["TOTALSTUDYACT"]       = $getRow["TOTALSTUDYACT"];
                    $row["TOTALSTUDYVAL"]       = $getRow["TOTALSTUDYVAL"];
                } else {
                    //年単位の時
                    $row["TOTALSTUDYACT"]       = $getRow["TOTALSTUDYACT_YEAR"];
                    $row["TOTALSTUDYVAL"]       = $getRow["TOTALSTUDYVAL_YEAR"];
                }
                //1, 2年次　通知票
            } else {
                $totalstudytimeArray = array();
                $totalstudyactArray  = array();
                $query = knja120eQuery::getRecordTotalstudytimeDat($model);
                $result = $db->query($query);
                while ($total_row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($total_row["TOTALSTUDYTIME"] != '') {
                        $totalstudytimeArray[] = $total_row["TOTALSTUDYTIME"];
                    }
                    if ($total_row["TOTALSTUDYACT"] != '') {
                        $totalstudyactArray[] = $total_row["TOTALSTUDYACT"];
                    }
                }
                $result->free();
                if (count($totalstudytimeArray) > 0) {
                    $row["TOTALSTUDYVAL"] = implode("\n", $totalstudytimeArray);
                }
                if (count($totalstudyactArray) > 0) {
                    $row["TOTALSTUDYACT"] = implode("\n", $totalstudyactArray);
                }
            }
        }
        if ($model->cmd === 'reload3') {
            $model->field["TRAIN_REF1_BG_COLOR_FLG"] = "1";
            $model->field["TRAIN_REF2_BG_COLOR_FLG"] = "1";
            $model->field["TRAIN_REF3_BG_COLOR_FLG"] = "1";
            $model->field["TRAIN_REF4_BG_COLOR_FLG"] = "1";
            $model->field["TRAIN_REF5_BG_COLOR_FLG"] = "1";
            $model->field["TRAIN_REF6_BG_COLOR_FLG"] = "1";

            //総合所見及び指導上参考となる諸事項
            $query = knja120eQuery::getHexamTrainRef($model);
            $result = $db->query($query);
            while ($getRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($getRow["REMARK"]) {
                    $seq = $getRow["TRAIN_SEQ"];
                    if ($seq == "101") {
                        $field = "TRAIN_REF1";
                    } elseif ($seq == "102") {
                        $field = "TRAIN_REF2";
                    } elseif ($seq == "103") {
                        $field = "TRAIN_REF3";
                    } elseif ($seq == "104") {
                        $field = "TRAIN_REF4";
                    } elseif ($seq == "105") {
                        $field = "TRAIN_REF5";
                    } elseif ($seq == "106") {
                        $field = "TRAIN_REF6";
                    }
                    $row[$field] = $getRow["REMARK"];
                }
            }
            $result->free();
        }

        //明治判定
        $meiji = knja120eQuery::getMeijiHantei($db);
        if ($meiji > 0) {
            $arg["meiji"] = 1;
        } else {
            $arg["not_meiji"] = 1;
        }

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        $model->schoolName = $db->getOne(knja120eQuery::getSchoolName("NAME1"));
        setTotalstudy($arg, $objForm, $model, $disabled, $row);

        setSpecialAct($arg, $objForm, $model, $disabled, $row);

        setTrainref($arg, $objForm, $model, $disabled, $row);

        setAttend($db, $arg, $objForm, $model, $disabled, $row);

        //1,2年次：通知票取込、3年次:調査書取込ボタンを作成する（プロパティにてボタン表示非表示の切り替え）
        if ($model->GradGrade != "1" && $model->Properties["tutihyoYoriYomikomiHyoujiFlg"] == 1) {
            $extra = $disabled ." onclick=\"return btn_submit('reload2');\" style=\"color:#1E90FF;font:bold;\"";
            $arg["btn_reload2"] = knjCreateBtn($objForm, "btn_reload2", "通知票取込", $extra);
        } elseif ($model->GradGrade == "1") {
            $extra = $disabled ." onclick=\"return btn_submit('reload2');\"".$setColor;
            $arg["btn_reload2"] = knjCreateBtn($objForm, "btn_reload2", "調査書取込", $extra);
        }

        //成績参照ボタン
        $extra = $disabled." onclick=\"return btn_submit('subformSeisekiSansho');\" style=\"width:70px\"";
        $arg["SEISEKI_SANSYO"] = knjCreateBtn($objForm, "SEISEKI_SANSYO", "成績参照", $extra);

        //自立活動の記録
        $moji = $model->mojigyo["ZIRITUKATUDOU"]["moji"];
        $gyo = $model->mojigyo["ZIRITUKATUDOU"]["gyo"];
        $extra = '';
        $arg["data"]["ZIRITUKATUDOU"] = KnjCreateTextArea($objForm, "ZIRITUKATUDOU", $gyo, ($moji * 2 + 1), "soft", $extra, $row["ZIRITUKATUDOU"]);
        $arg["data"]["ZIRITUKATUDOU_TYUI"] = "(全角{$moji}文字{$gyo}行まで)";

        //自立活動選択ボタン
        $arg["button"]["btn_ziritukatudou"] = makeSelectBtn($objForm, $model, "ziritukatudou", "btn_ziritukatudou", "通知票参照", "ZIRITUKATUDOU", $disabled);

        $arg["IFRAME"] = VIEW::setIframeJs();
        //ボタン
        //通知票所見参照ボタンを非表示とする。(近大、海上学園のみ)
        $cnt = knja120eQuery::getKindaiJudgment($db, $model);
        $schoolName = $db->getOne(knja120eQuery::getSchoolName("NAME1"));
        if ($cnt > 0 || $schoolName === 'kaijyo') {
        } else {
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_TUUCHISYOKEN_SELECT/knjx_tuuchisyoken_selectindex.php";
            $extra .= "?PROGRAMID=".PROGRAMID."&SEND_PRGID=".PROGRAMID."";
            $extra .= "&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}";
            $extra .= "&SCHREGNO={$model->schregno}&NAME={$model->name}'";
            $extra .= ",0,document.documentElement.scrollTop || document.body.scrollTop,900,350);\"";
            $arg["button"]["btn_popup"] = knjCreateBtn($objForm, "btn_popup", "通知表所見参照", $extra);
        }

        $ext = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $ext);

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext($model, $objForm, 'btn_update');

        $ext = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $ext);

        $ext = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $ext);
        //PDF取込
        if ($model->Properties["useUpdownPDF"] === '1') {
            $arg["useUpdownPDF"] = '1';
            updownPDF($objForm, $arg, $model);
        }

        //セキュリティーチェック
        $securityCnt = $db->getOne(knja120eQuery::getSecurityHigh());
        Query::dbCheckIn($db);
        $csvSetName = "ＣＳＶ";
        if ($model->Properties["useXLS"]) {
            $csvSetName = "エクセル";
        }
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            //CSV処理
            $fieldSize = "";
            $gyouSize  = "";
            $fields = array("TOTALSTUDYACT",
                            "TOTALSTUDYVAL",
                            "SPECIALACTREMARK",
                            "TRAIN_REF1",
                            "TRAIN_REF2",
                            "TRAIN_REF3",
                            "TRAIN_REF4",
                            "TRAIN_REF5",
                            "TRAIN_REF6",
                            "ATTENDREC_REMARK"
                        );
            foreach ($fields as $field) {
                $fieldSize .= $field."=".($model->mojigyo[$field]["moji"] * 3 * $model->mojigyo[$field]["gyo"]) .",";
                $gyouSize  .= $field."=".($model->mojigyo[$field]["gyo"]).",";
            }

            $fieldSize .= "VIEWREMARK=0,";
            $gyouSize  .= "VIEWREMARK=0,";
            $fieldSize .= "BEHAVEREC_REMARK=0";
            $gyouSize  .= "BEHAVEREC_REMARK=0,";

            $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", $csvSetName."出力", " onClick=\" wopen('".REQUESTROOT."/X/KNJX180/knjx180index.php?FIELDSIZE=".$fieldSize."&GYOUSIZE=".$gyouSize."&SEND_PRGID=KNJA120E&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"");
        }

        if ($model->Properties['tyousasyo_shokenTable_Seq'] === '1') {
            $link = REQUESTROOT."/A/KNJA120E/knja120eindex.php?cmd=tyousasyo";
            $extra = " onclick=\"Page_jumper('{$link}')\"";
            $arg["button"]["tyousasyo"] = knjCreateBtn($objForm, "tyousasyo", "調査書所見一括取込", $extra);
        }

        $torikomiField = array(
            "TRAIN_REF1" => "TRAIN_REF1",
            "TRAIN_REF2" => "TRAIN_REF2",
            "TRAIN_REF3" => "TRAIN_REF3",
            "TRAIN_REF4" => "TRAIN_REF4",
            "TRAIN_REF5" => "TRAIN_REF5",
            "TRAIN_REF6" => "TRAIN_REF6"
        );

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "TEXTNAME", "TRAIN_REF2");
        knjCreateHidden($objForm, "TRAINREF_TARGET", json_encode($torikomiField));
        knjCreateHidden($objForm, "TOTALSTUDYACT_BG_COLOR_FLG", $model->field["TOTALSTUDYACT_BG_COLOR_FLG"]);
        knjCreateHidden($objForm, "TOTALSTUDYVAL_BG_COLOR_FLG", $model->field["TOTALSTUDYVAL_BG_COLOR_FLG"]);
        knjCreateHidden($objForm, "SPECIALACTREMARK_BG_COLOR_FLG", $model->field["SPECIALACTREMARK_BG_COLOR_FLG"]);
        knjCreateHidden($objForm, "TRAIN_REF1_BG_COLOR_FLG", $model->field["TRAIN_REF1_BG_COLOR_FLG"]);
        knjCreateHidden($objForm, "TRAIN_REF2_BG_COLOR_FLG", $model->field["TRAIN_REF2_BG_COLOR_FLG"]);
        knjCreateHidden($objForm, "TRAIN_REF3_BG_COLOR_FLG", $model->field["TRAIN_REF3_BG_COLOR_FLG"]);
        knjCreateHidden($objForm, "ATTENDREC_REMARK_BG_COLOR_FLG", $model->field["ATTENDREC_REMARK_BG_COLOR_FLG"]);
        knjCreateHidden($objForm, "useQualifiedMst", $model->Properties["useQualifiedMst"]);

        if (strlen($model->warning)== 0 && $model->cmd !="clear") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd =="clear") {
            $arg["next"] = "NextStudent(1);";
        }
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja120eForm1.html", $arg);
    }
}

//PDF取込
function updownPDF(&$objForm, &$arg, $model)
{
    //移動後のファイルパス単位
    if ($model->schregno) {
        $dir = "/pdf/" . $model->schregno . "/";
        $dataDir = DOCUMENTROOT . $dir;
        if (!is_dir($dataDir)) {
            //echo "ディレクトリがありません。";
        } elseif ($aa = opendir($dataDir)) {
            $cnt = 0;
            while (false !== ($filename = readdir($aa))) {
                $filedir = REQUESTROOT . $dir . $filename;
                $info = pathinfo($filedir);
                //拡張子
                if ($info["extension"] == "pdf" && $cnt < 5) {
                    $setFilename = mb_convert_encoding($filename, "UTF-8", "SJIS-win");
                    $setFiles = array();
                    $setFiles["PDF_FILE_NAME"] = $setFilename;
                    $setFiles["PDF_URL"] = REQUESTROOT . $dir . $setFilename;
                    $arg["down"][] = $setFiles;
                    $cnt++;
                }
            }
            closedir($aa);
        }
    }
    //ファイルからの取り込み
    $arg["up"]["FILE"] = knjCreateFile($objForm, "FILE", "", 10240000);
    //実行
    $extra = ($model->schregno) ? "onclick=\"return btn_submit('execute');\"" : "disabled";
    $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_ok", "実 行", $extra);
}

// 総合
function setTotalstudy(&$arg, &$objForm, &$model, $disabled, &$row)
{
    $fields = array("TOTALSTUDYACT", "TOTALSTUDYVAL");
    foreach ($fields as $field) {
        if ($model->field[$field."_BG_COLOR_FLG"]) {
            $extra = "style=\"background-color:#FFCCFF\"";
        } else {
            $extra = "";
        }
        //活動内容 評価
        $moji = $model->mojigyo[$field]["moji"];
        $gyo = $model->mojigyo[$field]["gyo"];
        $height = "height: " . $gyo . ".3em;line-height: 1;padding:2px;";
        if ($model->field[$field."_BG_COLOR_FLG"]) {
            $extra = "style=\"{$height}background-color:#FFCCFF\"";
        } else {
            $extra = "style=\"{$height}\" ";
        }
        $arg["data"][$field] = KnjCreateTextArea($objForm, $field, $gyo, ($moji * 2 + 1), "soft", $extra, $row[$field]);
        $arg["data"][$field."_TYUI"] = "(全角{$moji}文字{$gyo}行まで)";
    }

    //定型文選択ボタンを作成する
    if ($model->Properties["Teikei_Button_Hyouji"] == "1") {
        $extra = "onclick=\"return btn_submit('teikei_act');\"";
        $arg["button"]["btn_teikei_act"] = knjCreateBtn($objForm, "btn_teikei_act", "定型文選択", $extra);

        $extra = "onclick=\"return btn_submit('teikei_val');\"";
        $arg["button"]["btn_teikei_val"] = knjCreateBtn($objForm, "btn_teikei_val", "定型文選択", $extra);
    }
}

// 特別活動所見
function setSpecialAct(&$arg, &$objForm, &$model, $disabled, &$row)
{
    $moji = $model->mojigyo["SPECIALACTREMARK"]["moji"];
    $gyo = $model->mojigyo["SPECIALACTREMARK"]["gyo"];
    $height = "height: " . $gyo . ".3em;line-height: 1;padding:2px;";
    if ($model->field["SPECIALACTREMARK_BG_COLOR_FLG"]) {
        $extra = "style=\"{$height}background-color:#FFCCFF\"";
    } else {
        $extra = "style=\"{$height}\" ";
    }
    //特別活動所見
    $arg["data"]["SPECIALACTREMARK"] = KnjCreateTextArea($objForm, "SPECIALACTREMARK", $gyo, ($moji * 2 + 1), "soft", $extra, $row["SPECIALACTREMARK"]);
    $arg["data"]["SPECIALACTREMARK_TYUI"] = "(全角{$moji}文字{$gyo}行まで)";

    //委員会選択ボタン
    $arg["button"]["btn_committee"] = makeSelectBtn($objForm, $model, "committee", "btn_committee", "委員会選択", "SPECIALACTREMARK", $disabled);

    //部活動選択ボタン（特別活動所見）1:表示
    if ($model->Properties["useKnja120_clubselect_Button"] == "1") {
        $arg["useclubselect"] = 1;
        $arg["button"]["btn_club_spe"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "SPECIALACTREMARK", $disabled);
    } else {
        $arg["useclubselect"] = 0;
    }
}

// 諸事項
function setTrainref(&$arg, &$objForm, &$model, $disabled, &$row)
{
    foreach ($model->itemname as $key => $name) {
        $arg[$key."NAME"] = $name;
    }

    $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_HTRAINREMARK_TRAINREF/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&OUTPUT_FIELD=TOTALREMARK&OUTPUT_HEIGHT=75&OUTPUT_WIDTH=320',0,document.documentElement.scrollTop || document.body.scrollTop,900,350);return;\"";
    $arg["button"]["SOUGOU_SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "過年度参照", $extra);

    //6分割
    $prgid = "KNJX_HEXAM_ENTREMARK_TRAINREF_SELECT";
    $extra = $disabled ." onclick=\"loadwindow('../../X/{$prgid}/index.php?TORIKOMI_MULTI=1&GRADE_YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SEND_PRGID={$prgid}&SEND_AUTH={$model->auth}&TRAINREF_TARGET=' + document.forms[0].TRAINREF_TARGET.value, 0, document.documentElement.scrollTop || document.body.scrollTop,550,570);return;\"";
    $arg["button"]["TYOUSASYO_SENTAKU"] = KnjCreateBtn($objForm, "TYOUSASYO_SENTAKU", "調査書選択", $extra);

    //指導上参考となる諸事項
    $prgid = "KNJX_HEXAM_ENTREMARK_TRAINREF_SELECT";
    $extra = $disabled ." onclick=\"loadwindow('../../X/{$prgid}/index.php?SHOJIKOU_FLG=1&TORIKOMI_MULTI=1&GRADE_YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SEND_PRGID={$prgid}&SEND_AUTH={$model->auth}&TRAINREF_TARGET=' + document.forms[0].TRAINREF_TARGET.value, 0, document.documentElement.scrollTop || document.body.scrollTop,550,570);return;\"";
    if ($model->Properties['tyousasyo_shokenTable_Seq'] === '1') {
        // プロパテイーtyousasyo_shokenTable_Seq = 1　の時は、【指導上参考となる諸事項選択】ボタンを非表示とする。
        $arg["button"]["SHOJIKOU_SENTAKU"] = "";
    } else {
        $arg["button"]["SHOJIKOU_SENTAKU"] = KnjCreateBtn($objForm, "SHOJIKOU_SENTAKU", "指導上参考となる諸事項選択", $extra);
    }

    //特記事項なし
    $no_comments_label = '特記事項なし';
    knjCreateHidden($objForm, "NO_COMMENTS_LABEL", $no_comments_label);
    $arg["NO_COMMENTS_LABEL"] = $no_comments_label;

    for ($i = 1; $i <= 6; $i++) {
        $field = "TRAIN_REF".$i;
        $moji = $model->mojigyo[$field]["moji"];
        $gyo = $model->mojigyo[$field]["gyo"];
        $height = "height: " . $gyo . ".3em;line-height: 1;padding:2px;";

        if ($model->field[$field."_BG_COLOR_FLG"]) {
            $bgcolor = "background-color:#FFCCFF;";
        } else {
            $bgcolor = "";
        }

        $extra = " id=\"{$field}\" style=\"{$height}{$bgcolor}\" ";
        $arg[$field] = KnjCreateTextArea($objForm, $field, ($gyo + 1), ($moji * 2 + 1), "soft", $extra, $row[$field]);
        $arg[$field."_COMMENT"] = "(全角{$moji}文字{$gyo}行まで)";

        //特記事項なしチェックボックス
        $id2 = "TRAIN_REF".$i."_NO_COMM";
        $extra  = ($model->field[$id2] == "1") ? "checked" : "";
        $extra .= $disabled." id=\"".$id2."\" onclick=\"return CheckRemark('".$field."', '".$id2."');\"";
        $arg["TRAIN_REF".$i."_NO_COMM"] = knjCreateCheckBox($objForm, $id2, "1", $extra, "");
    }

    //行動の記録参照ボタン
    $extra = $disabled." onclick=\"return btn_submit('act_doc');\"";
    $arg["button"]["btn_actdoc"] = knjCreateBtn($objForm, "btn_actdoc", "行動の記録参照", $extra);

    //賞選択ボタン 指導上参考となる諸事項（5）
    if ($model->Properties["useHyosyoSansyoButton_H"]) {
        $arg["btn_hyosyo_tra5"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo", "賞選択", "TRAIN_REF5", $disabled);
    }

    //部活動選択ボタン 指導上参考となる諸事項（3）
    if ("shimaneken" == $model->schoolName) {
        $arg["btn_club_tra5"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "TRAIN_REF5", $disabled);
    } else {
        $arg["btn_club_tra3"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "TRAIN_REF3", $disabled);
    }

    //記録備考選択ボタン 指導上参考となる諸事項（3）
    if ($model->Properties["club_kirokubikou"] == 1) {
        if ("shimaneken" != $model->schoolName) {
            $arg["btn_club_kirokubikou_tra3"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", "TRAIN_REF3", $disabled);
        }
        $arg["btn_club_kirokubikou_tra5"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", "TRAIN_REF5", $disabled);
    }

    //検定選択ボタン 指導上参考となる諸事項（4）
    $arg["btn_qualified_tra4"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified", "検定選択", "TRAIN_REF4", $disabled);
}


// 出欠
function setAttend(&$db, &$arg, &$objForm, &$model, $disabled, &$row)
{
    //出欠の記録備考取込
    if ($model->cmd === 'torikomi3') {
        $set_remark = knja120eQuery::getSemesRemark($db, $model);
        $row["ATTENDREC_REMARK"] = $set_remark;
        $model->field["ATTENDREC_REMARK_BG_COLOR_FLG"] = "1";
    }

    //出欠の記録備考
    $style = "";
    if ($model->field["ATTENDREC_REMARK_BG_COLOR_FLG"]) {
        $style = "background-color:#FFCCFF;";
    }
    $moji = $model->mojigyo["ATTENDREC_REMARK"]["moji"];
    $gyo = $model->mojigyo["ATTENDREC_REMARK"]["gyo"];
    $height = "height: " . $gyo . ".3em;line-height: 1;padding:2px;";
    $style .= $height;
    $extra = " style=\"{$style}\" ";

    //出欠の記録備考
    $arg["data"]["ATTENDREC_REMARK"] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK", $gyo, ($moji * 2 + 1), "soft", $extra, $row["ATTENDREC_REMARK"]);
    $arg["data"]["ATTENDREC_REMARK_TYUI"] = "(全角{$moji}文字{$gyo}行まで)";

    //出欠の記録参照ボタン
    if ($model->Properties["tyousasyoSyukketsuKirokuBtn"] == 1) {
        $arg["btn_syukketsu_sansyo"] = makeSelectBtn($objForm, $model, "syukketsukiroku", "btn_syukketsu_sansyo", "出欠の記録参照", "ATTENDREC_REMARK", $disabled);
        $arg["tyousasyoSyukketsuKirokuBtn"] = 1;
    }

    //出欠備考参照ボタン
    $sdate = $model->exp_year.'-04-01';
    $edate = ((int) ($model->exp_year) + 1).'-03-31';
    //和暦表示フラグ
    $warekiFlg = "";
    if ($model->Properties["useWarekiHyoji"] == "1") {
        $warekiFlg = "1";
    }
    if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
        //まとめ出欠備考を取込みへ変更する
        if ($model->Properties["useTorikomiAttendSemesRemarkDat"] == 1) {
            $setname = 'まとめ出欠備考取込';
            $extra = "style=\"color:#1E90FF;font:bold;\"";
        } else {
            $setname = 'まとめ出欠備考参照';
            $extra = "";
        }
        //まとめ出欠備考を取込みへ変更する
        if ($model->Properties["useTorikomiAttendSemesRemarkDat"] == 1) {
            $extra .= $disabled ." onclick=\"return btn_submit('torikomi3');\"";
        } else {
            $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
        }
        $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", $setname, $extra);
    } else {
        $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
        $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "日々出欠備考参照", $extra);
    }

    //要録の出欠備考参照ボタン
    $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_ENTREMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}',0,0,360,180);return;\" style=\"width:230px;\"";
    $arg["TYOSASYO_SANSYO"] = KnjCreateBtn($objForm, "TYOSASYO_SANSYO", "調査書(進学用)の出欠の記録参照", $extra);

    //年間出欠備考選択ボタン
    if ($model->Properties["useReasonCollectionBtn"] == 1) {
        $arg["btn_reason_collection_select"] = makeSelectBtn($objForm, $model, "reason_collection", "btn_reason_collection_select", "年間出欠備考選択", "ATTENDREC_REMARK", $disabled);
        $arg["REASON_COLLECTION_SELECT"] = 1;
    }
}

//選択ボタン
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $disabled = "")
{
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        if ($div == "club") {                   //部活動
            $extra = " onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "committee") {       //委員会
            $extra = " onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"";
        } elseif ($div == "qualified") {       //検定
            $sizeW = ($model->Properties["useQualifiedMst"] == "1") ? 800 : 670;
            $extra = " onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$sizeW},500);\"";
        } elseif ($div == "hyosyo") {          //賞
            $extra = " onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_HYOSYO_SELECT/knjx_hyosyo_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,600,350);\"";
        } elseif ($div == "kirokubikou") {     //記録備考
            $extra = " onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_KIROKUBIKOU_SELECT/knjx_club_kirokubikou_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "reason_collection") {   //年間出欠備考
            $extra = " onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_REASON_COLLECTION_SELECT/knjx_reason_collection_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "syukketsukiroku") {   //出欠の記録参照
            $extra = " onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_SYUKKETSUKIROKU/knjx_syukketsukirokuindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "ziritukatudou") {   //通知票参照
            $extra = " onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_ZIRITUKATUDOU_SELECT/knjx_ziritukatudou_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        }
        $extra = $disabled.$extra;
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}
