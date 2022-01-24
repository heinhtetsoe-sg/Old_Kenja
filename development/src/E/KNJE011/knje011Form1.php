<?php

require_once('for_php7.php');
//ビュー作成用クラス
class knje011Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje011index.php", "", "edit");
        $db = Query::dbCheckOut();

        if (!isset($model->warning) && $model->cmd != 'yomikomi') {
            $query = knje011Query::selectQuery($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row = $model->field;
        }

        if ($model->Properties["useMaruA_avg"] != "") {
            $arg["MARU_A_AVG"] = $model->Properties["useMaruA_avg"];
            $arg["UseMaruA_avg"] = 1;
        } else {
            $arg["UnUseMaruA_avg"] = 1;
        }

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        $arg["ATTEND_TITLE"] = $model->attendTitle."の記録";
        
        //年次取得
        $model->gradecd = "";
        $model->gradecd = $db->getOne(knje011Query::getGradeCd($model));

        //「総合的な学習の時間の内容・評価」 を表示するかしないかのフラグ
        if ($model->Properties["tyousasyoSougouHyoukaNentani"] != 1) {
            $arg["tyousasyoSougouHyoukaNentani"] = 1;
        } else {
            $arg["tyousasyoSougouHyoukaNentani_for_title"] = 1;
        }

        //「備考2」の表示制御
        if ($model->Properties["useHexamRemark2Flg"] == 1) {
            $arg["useHexamRemark2Flg"] = 1;
        }

        /******************/
        /* テキストエリア */
        /******************/
        //「総合的な学習の時間の内容・評価」 を表示するかしないかのフラグ
        if ($model->Properties["tyousasyoSougouHyoukaNentani"] != 1) {
            if ($model->cmd == "yomikomi") {
                //1,2年次　指導要録取込
                if (intval($model->gradecd) < "3") {
                    $query = knje011Query::getYouroku($model);
                    $resultYouroku = $db->query($query);
                    $kaigyou = "";
                    $row["TOTALSTUDYACT"] = "";
                    $row["TOTALSTUDYVAL"] = "";
                    while ($rowYouroku = $resultYouroku->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $row["TOTALSTUDYACT"] .= $kaigyou.$rowYouroku["TOTALSTUDYACT"];
                        $row["TOTALSTUDYVAL"] .= $kaigyou.$rowYouroku["TOTALSTUDYVAL"];
                        $kaigyou = "\r\n";
                    }
                } else {
                    //3年次　通知票取込
                    $totalstudytimeArray = array();
                    $totalstudyactArray  = array();
                    $query = knje011Query::get_record_totalstudytime_dat($model);
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
                    if (get_count($totalstudytimeArray) > 0) {
                        $row["TOTALSTUDYVAL"] = implode("\n", $totalstudytimeArray);
                    }
                    if (get_count($totalstudyactArray) > 0) {
                        $row["TOTALSTUDYACT"] = implode("\n", $totalstudyactArray);
                    }
                }
            }

            //総合的な学習の時間の「斜線を入れる」チェックボックス表示
            if ($model->Properties["useTotalstudySlashFlg"] == 1) {
                $arg["useTotalstudySlashFlg"] = 1;
            }

            //活動内容
            $height = $model->totalstudyact_gyou * 13.5 + ($model->totalstudyact_gyou -1) * 3 + 5;
            $arg["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_gyou, ($model->totalstudyact_moji * 2 + 1), "soft", "style=\"height:{$height}px;\"", $row["TOTALSTUDYACT"]);
            $arg["TOTALSTUDYACT_TYUI"] = "(全角{$model->totalstudyact_moji}文字{$model->totalstudyact_gyou}行まで)";
            //斜線を入れるチェックボックス
            $extra  = ($row["TOTALSTUDYACT_SLASH_FLG"] == "1") ? "checked" : "";
            $extra .= " id=\"TOTALSTUDYACT_SLASH_FLG\"";
            $arg["TOTALSTUDYACT_SLASH_FLG"] = knjCreateCheckBox($objForm, "TOTALSTUDYACT_SLASH_FLG", "1", $extra, "");

            //評価
            $height = $model->totalstudyval_gyou * 13.5 + ($model->totalstudyval_gyou -1) * 3 + 5;
            $arg["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_gyou, ($model->totalstudyval_moji * 2 + 1), "soft", "style=\"height:{$height}px;\"", $row["TOTALSTUDYVAL"]);
            $arg["TOTALSTUDYVAL_TYUI"] = "(全角{$model->totalstudyval_moji}文字{$model->totalstudyval_gyou}行まで)";
            //斜線を入れるチェックボックス
            $extra  = ($row["TOTALSTUDYVAL_SLASH_FLG"] == "1") ? "checked" : "";
            $extra .= " id=\"TOTALSTUDYVAL_SLASH_FLG\"";
            $arg["TOTALSTUDYVAL_SLASH_FLG"] = knjCreateCheckBox($objForm, "TOTALSTUDYVAL_SLASH_FLG", "1", $extra, "");

            //1,2年次　指導要録取込、3年次　通知票取込み(総合的な学習の時間　通年用)
            if (intval($model->gradecd) > "2" && $model->Properties["tutihyoYoriYomikomiHyoujiFlg"] == 1) {
                $extra = "onclick=\"return btn_submit('yomikomi');\" style=\"color:#1E90FF;font:bold;\"";
                $arg["button"]["btn_yomikomi"] = knjCreateBtn($objForm, "btn_yomikomi", "通知票取込", $extra);
            } elseif (intval($model->gradecd) < "3" && $model->Properties["tyousasyo_SidoYorokYoriYomikomiHyoujiFlg"] == 1) {
                $extra = "onclick=\"return btn_submit('yomikomi');\" style=\"color:#1E90FF;font:bold;\"";
                $arg["button"]["btn_yomikomi"] = knjCreateBtn($objForm, "btn_yomikomi", "指導要録取込", $extra);
            }
        }
        //備考
        $height = $model->remark_gyou * 13.5 + ($model->remark_gyou -1) * 3 + 5;
        $arg["REMARK"] = KnjCreateTextArea($objForm, "REMARK", $model->remark_gyou, ($model->remark_moji * 2 + 1), "soft", "style=\"height:{$height}px;\"", $row["REMARK"]);
        $arg["REMARK_TYUI"] = "(全角{$model->remark_moji}文字X{$model->remark_gyou}行まで)";

        //備考2
        $height = $model->remark2_gyou * 13.5 + ($model->remark2_gyou -1) * 3 + 5;
        $arg["REMARK2"] = KnjCreateTextArea($objForm, "REMARK2", $model->remark2_gyou, ($model->remark_moji * 2 + 1), "soft", "style=\"height:{$height}px;\"", $row["REMARK2"]);
        $arg["REMARK2_TYUI"] = "(全角{$model->remark_moji}文字X{$model->remark2_gyou}行まで)";

        /********************/
        /* チェックボックス */
        /********************/
        //学習成績概評チェックボックス
        $objForm->ae(array("type"      => "checkbox",
                           "name"      => "COMMENTEX_A_CD",
                           "checked"   => ($row["COMMENTEX_A_CD"]==1)? true:false,
                           "value"     => 1,
                           "extrahtml" => "id=\"comment\""));
        $arg["COMMENTEX_A_CD"] = $objForm->ge("COMMENTEX_A_CD");

        //特記事項なしチェックボックス
        $extra  = ($model->field["NO_COMMENTS"] == "1") ? "checked" : "";
        $extra .= " id=\"NO_COMMENTS\" onclick=\"return CheckRemark();\"";
        $arg["NO_COMMENTS"] = knjCreateCheckBox($objForm, "NO_COMMENTS", "1", $extra, "");

        //特記事項なし
        knjCreateHidden($objForm, "NO_COMMENTS_LABEL", $model->no_comments_label);
        $arg["NO_COMMENTS_LABEL"] = $model->no_comments_label;

        /**********/
        /* ボタン */
        /**********/
        //特別な活動～ボタンを作成する
        $extra = "onclick=\"return btn_submit('form2_first');\" style=\"width:520px\"";
        if ($model->Properties["tyousasyoSougouHyoukaNentani"] == 1) {
            $title = $arg["ATTEND_TITLE"].' & 特別活動の記録 & 指導上参考になる諸事項 & 総合的な学習の時間';
        } else {
            $title = $arg["ATTEND_TITLE"].' ＆ 特別活動の記録 ＆ 指導上参考になる諸事項';
        }
        $arg["btn_form2"] = knjCreateBtn($objForm, "btn_form2", $title, $extra);
        //成績参照ボタンを作成する
        $extra = "onclick=\"return btn_submit('form3_first');\" style=\"width:70px\"";
        $arg["btn_form3"] = knjCreateBtn($objForm, "btn_form3", "成績参照", $extra);
        //指導要録参照画面ボタンを作成する
        if ($model->Properties["sidouyourokuSansyou"] == 1) {
            $extra = "onclick=\"return btn_submit('form6_first');\"";
        } else {
            $extra = "onclick=\"return btn_submit('form4_first');\"";
        }
        $arg["btn_form4"] = knjCreateBtn($objForm, "btn_form4", "指導要録参照", $extra);
        //更新ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //更新後前の生徒へボタン
        $arg["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "updEdit", "update");
        //取消しボタンを作成する
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //セキュリティーチェック
        $securityCnt = $db->getOne(knje011Query::getSecurityHigh());
        $csvSetName = "CSV";
        if ($model->Properties["useXLS"]) {
            $csvSetName = "エクセル";
        }
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            //データCSVボタン
            $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX190/knjx190index.php?program_id=".PROGRAMID."&mode={$model->mode}&SEND_PRGID=KNJE011&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_check1"] = knjCreateBtn($objForm, "btn_check1", "データ".$csvSetName, $extra);
            //ヘッダデータCSVボタン
            $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX191/knjx191index.php?program_id=".PROGRAMID."&mode={$model->mode}&SEND_PRGID=KNJE011&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_check2"] = knjCreateBtn($objForm, "btn_check2", "ヘッダデータ".$csvSetName, $extra);
        }
        //所見確認用
        if ($model->Properties["tyousasyoShokenPreview"] == '1') {
            $extra =  "onclick=\"return newwin('".SERVLET_URL."');\"";
            $arg["button"]["btn_print"] = KnjCreateBtn($objForm, "btn_print", "所見確認用", $extra);
        }
        //備考一括更新
        $link = REQUESTROOT."/X/KNJX_HEXAM_REMARK/knjx_hexam_remarkindex.php?program_id=".PROGRAMID."&mode={$model->mode}&SEND_PRGID=KNJE011&SEND_AUTH={$model->auth}&EXP_YEAR={$model->annual["YEAR"]}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&GRADE={$model->grade}&HR_CLASS={$model->hrClass}";
        $extra = "style=\"width:100px\" onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["btn_remark_all"] = knjCreateBtn($objForm, "btn_remark_all", "備考一括更新", $extra);

        //異動情報ボタン
        if ("1" == $model->Properties["useTransferButton_H"]) {
            $disabledBtnTransfer = ($model->schregno == "") ? " disabled": "";
            $tgt = "/X/KNJX_TRANSFER_SELECT/knjx_transfer_selectindex.php";
            $param  = "?program_id=".PROGRAMID;
            $param .= "&SEND_PRGID=".PROGRAMID;
            $param .= "&EXP_YEAR=".CTRL_YEAR;
            $param .= "&EXP_SEMESTER={$model->exp_semester}";
            $param .= "&SCHREGNO={$model->schregno}";
            $param .= "&NAME={$model->name}";
            $param .= "&TARGET=REMARK";
            $param .= "',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
            $onclick = " onclick=\"loadwindow('".REQUESTROOT.$tgt.$param;
            $extra = $disabledBtnTransfer.$onclick;
            $arg["btn_transfer"] = knjCreateBtn($objForm, "btn_transfer", "異動情報選択", $extra);
        }

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "nextURL", $model->nextURL);
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "mode", $model->mode);
        knjCreateHidden($objForm, "GRD_YEAR", $model->grd_year);
        knjCreateHidden($objForm, "GRD_SEMESTER", $model->grd_semester);
        knjCreateHidden($objForm, "PROGRAMID", PROGRAMID);
        knjCreateHidden($objForm, "useSyojikou3", $model->Properties["useSyojikou3"]);
        knjCreateHidden($objForm, "tyousasyoTokuBetuFieldSize", $model->Properties["tyousasyoTokuBetuFieldSize"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "useTotalstudySlashFlg", $model->Properties["useTotalstudySlashFlg"]);
        knjCreateHidden($objForm, "useAttendrecRemarkSlashFlg", $model->Properties["useAttendrecRemarkSlashFlg"]);
        //所見確認用パラメータ
        if ($model->Properties["tyousasyoShokenPreview"] == '1') {
            knjCreateHidden($objForm, "PRGID", "KNJE011");
            knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
            knjCreateHidden($objForm, "PRINT_YEAR", CTRL_YEAR);
            knjCreateHidden($objForm, "PRINT_GAKKI", CTRL_SEMESTER);
            knjCreateHidden($objForm, "GRADE_HR_CLASS");
            knjCreateHidden($objForm, "KANJI", "1");
            knjCreateHidden($objForm, "OS", "1");
            knjCreateHidden($objForm, "OUTPUT", "1");
            knjCreateHidden($objForm, "3_or_6_nenYoForm", $model->Properties["3_or_6_nenYoForm"]);
            //何年用のフォームを使うのかの初期値を判断する
            $query = knje011Query::getSchoolDiv();
            $schooldiv = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $nenyoformSyokiti = $schooldiv["NEN"] == '0' ? ($schooldiv["SCHOOLDIV"] == '0' ? '3' : '4') : $schooldiv["NEN"];
            knjCreateHidden($objForm, "NENYOFORM", $nenyoformSyokiti);
            knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
            knjCreateHidden($objForm, "tyousasyoAttendrecRemarkFieldSize", $model->Properties["tyousasyoAttendrecRemarkFieldSize"]);
            knjCreateHidden($objForm, "tyousasyoSougouHyoukaNentani", $model->Properties["tyousasyoSougouHyoukaNentani"]);
            knjCreateHidden($objForm, "tyousasyoTotalstudyactFieldSize", $model->Properties["tyousasyoTotalstudyactFieldSize"]);
            knjCreateHidden($objForm, "tyousasyoTotalstudyvalFieldSize", $model->Properties["tyousasyoTotalstudyvalFieldSize"]);
            knjCreateHidden($objForm, "tyousasyoSpecialactrecFieldSize", $model->Properties["tyousasyoSpecialactrecFieldSize"]);
            knjCreateHidden($objForm, "tyousasyoTokuBetuFieldSize", $model->Properties["tyousasyoTokuBetuFieldSize"]);
            knjCreateHidden($objForm, "tyousasyoEMPTokuBetuFieldSize", $model->Properties["tyousasyoEMPTokuBetuFieldSize"]);
            knjCreateHidden($objForm, "tyousasyoKinsokuForm", $model->Properties["tyousasyoKinsokuForm"]);
            knjCreateHidden($objForm, "tyousasyoNotPrintAnotherAttendrec", $model->Properties["tyousasyoNotPrintAnotherAttendrec"]);
            knjCreateHidden($objForm, "tyousasyoNotPrintAnotherStudyrec", $model->Properties["tyousasyoNotPrintAnotherStudyrec"]);
            knjCreateHidden($objForm, "tyousasyoRemarkFieldSize", $model->Properties["tyousasyoRemarkFieldSize"]);

            knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
            knjCreateHidden($objForm, "useCertifSchPrintCnt", $model->Properties["useCertifSchPrintCnt"]);
            knjCreateHidden($objForm, "gaihyouGakkaBetu", $model->Properties["gaihyouGakkaBetu"]);
            knjCreateHidden($objForm, "nenYoForm", $model->Properties["nenYoForm"]);
            knjCreateHidden($objForm, "train_ref_1_2_3_field_size", $model->Properties["train_ref_1_2_3_field_size"]);
            knjCreateHidden($objForm, "train_ref_1_2_3_gyo_size", $model->Properties["train_ref_1_2_3_gyo_size"]);
            knjCreateHidden($objForm, "tyousasyoNotPrintEnterGrade", $model->Properties["tyousasyoNotPrintEnterGrade"]);
            knjCreateHidden($objForm, "tyousasyoSyusyokuPrintGappeiTougou", $model->Properties["tyousasyoSyusyokuPrintGappeiTougou"]);
            knjCreateHidden($objForm, "tyousasyoHankiNintei", $model->Properties["tyousasyoHankiNintei"]);
            knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
            knjCreateHidden($objForm, "useAddrField2", $model->Properties["useAddrField2"]);
            knjCreateHidden($objForm, "useProvFlg", $model->Properties["useProvFlg"]);
            knjCreateHidden($objForm, "useGakkaSchoolDiv", $model->Properties["useGakkaSchoolDiv"]);
            knjCreateHidden($objForm, "useAssessCourseMst", $model->Properties["useAssessCourseMst"]);
            knjCreateHidden($objForm, "tyousasyoUseEditKinsoku", $model->Properties["tyousasyoUseEditKinsoku"]);
            knjCreateHidden($objForm, "certifPrintRealName", $model->Properties["certifPrintRealName"]);
            knjCreateHidden($objForm, "tyousasyoCheckCertifDate", $model->Properties["tyousasyoCheckCertifDate"]);
            knjCreateHidden($objForm, "tyousasyoPrintHomeRoomStaff", $model->Properties["tyousasyoPrintHomeRoomStaff"]);
            knjCreateHidden($objForm, "tyousasyoPrintCoursecodename", $model->Properties["tyousasyoPrintCoursecodename"]);
            knjCreateHidden($objForm, "tyousasyoPrintChairSubclassSemester2", $model->Properties["tyousasyoPrintChairSubclassSemester2"]);
            knjCreateHidden($objForm, "tyousasyoHanasuClasscd", $model->Properties["tyousasyoHanasuClasscd"]);
            knjCreateHidden($objForm, "tyousasyoJiritsuKatsudouRemark", $model->Properties["tyousasyoJiritsuKatsudouRemark"]);
            knjCreateHidden($objForm, "tyousasyoSougouHyoukaNentaniPrintCombined", $model->Properties["tyousasyoSougouHyoukaNentaniPrintCombined"]);
            knjCreateHidden($objForm, "NENYOFORM_CHECK", $model->Properties["nenYoForm"]);
        }

        if (get_count($model->warning)== 0 && $model->cmd !="reset") {
            $arg["next"] = "NextStudent2(0);";
        } elseif ($model->cmd =="reset") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje011Form1.html", $arg);
    }
}
