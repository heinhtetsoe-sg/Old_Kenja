<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje010bForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje010bindex.php", "", "edit");
        $db = Query::dbCheckOut();

        $model->schoolKind = $db->getOne(knje010bQuery::getSchoolKind($model));

        if (!isset($model->warning) && $model->cmd != "reload2_ok" && $model->cmd != "reload2_cancel" && $model->cmd != "reload3" && $model->cmd != "reload3_1" && $model->cmd != "reload3_2" && $model->cmd != "reload3_3") {
            $query = knje010bQuery::selectQuery($model);
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
        
        //年次取得
        $query = knje010bQuery::getGradeCd($model);
        $getGdat = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->gradecd = "";
        $model->gradecd = $getGdat["GRADE_CD"];
        $model->schoolKind = "";
        $model->schoolKind = $getGdat["SCHOOL_KIND"];

        //京都仕様の場合、総合的な学習の時間の記録の評価は入力不可とする
        $model->getSchoolName = "";
        $model->getSchoolName = $db->getOne(knje010bQuery::getNameMst("Z010"));

        //1,2年次　指導要録、3年次　通知票取込ボタンが押された時の通知書より読込む　(かつ通年のとき)
        if ($model->cmd == 'reload3' && $model->Properties["tyousasyoSougouHyoukaNentani"] != 1) {
            //1,2年次　指導要録取込
            if (intval($model->gradecd) < "3") {
                $totalstudyvalArray = array();
                $totalstudyactArray  = array();
                $query = knje010bQuery::getYourokuDat($model);
                $result = $db->query($query);
                while ($total_row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($total_row["TOTALSTUDYVAL"] != '') {
                        $totalstudyvalArray[] = $total_row["TOTALSTUDYVAL"];
                    }
                    if ($total_row["TOTALSTUDYACT"] != '') {
                        $totalstudyactArray[] = $total_row["TOTALSTUDYACT"];
                    }
                }
                $result->free();
                if (get_count($totalstudyvalArray) > 0) {
                    $row["TOTALSTUDYVAL"] = implode("\n", $totalstudyvalArray);
                }
                if (get_count($totalstudyactArray) > 0) {
                    $row["TOTALSTUDYACT"] = implode("\n", $totalstudyactArray);
                }
            //3年次　通知票取込
            } else {
                $totalstudytimeArray = array();
                $totalstudyactArray  = array();
                $query = knje010bQuery::get_record_totalstudytime_dat($model);
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
                    //入力値の後に改行して取り込む
                    $getTotalStudytime = "";
                    $getTotalStudytime = implode("\n", $totalstudytimeArray);
                    $row["TOTALSTUDYVAL"] = $row["TOTALSTUDYVAL"]."\n".$getTotalStudytime;
                }
                if (get_count($totalstudyactArray) > 0) {
                    //入力値の後に改行して取り込む
                    $getTotalStudyact = "";
                    $getTotalStudyact = implode("\n", $totalstudyactArray);
                    $row["TOTALSTUDYACT"] = $row["TOTALSTUDYACT"]."\n".$getTotalStudyact;
                }
            }
        }

        /******************/
        /* コンボボックス */
        /******************/
        //教務主任等マスタチェックと海城学園(非表示にする)のチェック
        $getIppanCount = $db->getOne(knje010bQuery::getPositionCheck($model));
        if ($getIppanCount == 0 && $model->getSchoolName !== 'kaijyo') {
            $disabled = " disabled ";
            $model->allYear = '';
            $opt = array();
            $opt[] = array("label" => "全て",
                           "value" => "0000");
            $query = knje010bQuery::selectQueryAnnual($model);
            $result = $db->query($query);
            while ($readRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $label = $readRow["GRADE_NAME1"];
                if ($label == '') {
                    $label = (int) $readRow["GRADE"] ."学年";
                }
                $label .= "　".$readRow["YEAR"] ."年度";
                $opt[] = array("label" => $label,
                               "value" => $readRow["YEAR"]
                              );
                $disabled = "";
                if($model->allYear == ''){
                    $model->allYear .= "'".$readRow["YEAR"]."'";
                } else {
                    $model->allYear .= ",'".$readRow["YEAR"]."'";
                }
            }
            $model->readYear = $model->readYear ? $model->readYear : CTRL_YEAR;
            $arg["btn_readYear"] = knjCreateCombo($objForm, "READ_YEAR", $model->readYear, $opt, $disabled, 1);
        }

        /******************/
        /* テキストエリア */
        /******************/
        makeHexamEntRemarkDat($objForm, $arg, $db, $model);

        //総合的な学習の時間の「斜線を入れる」チェックボックス表示
        if ($model->Properties["useTotalstudySlashFlg"] == 1) {
            $arg["useTotalstudySlashFlg"] = 1;
        }

        if ($model->Properties["tyousasyoSougouHyoukaNentani"] != 1) {
            //生徒指導要録より読込ボタン押下時に通年の総合的な学習の時間の内容・評価をセット
            if ($model->cmd == "reload2_ok" || $model->cmd == "reload2_cancel") {
                if ($model->Properties["sidouyourokuSansyou"] == 1) {
                    $query = knje010BQuery::getYourokuDat($model);
                } else {
                    $query = knje010BQuery::selectQuery_Htrainremark_Hdat($model);
                }
                $resultYouroku = $db->query($query);
                $kaigyou = "";
                $row["TOTALSTUDYACT"] = ($model->cmd == "reload2_ok") ? "" : $model->field["TOTALSTUDYACT"]."\n";
                $row["TOTALSTUDYVAL"] = ($model->cmd == "reload2_ok") ? "" : $model->field["TOTALSTUDYVAL"]."\n";
                while ($rowYouroku = $resultYouroku->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($model->readYear === '0000' && $model->Properties["sidouyourokuSansyou"] == 1) {
                        $row["TOTALSTUDYACT"] .= $kaigyou.$rowYouroku["TOTALSTUDYACT"];
                        $row["TOTALSTUDYVAL"] .= $kaigyou.$rowYouroku["TOTALSTUDYVAL"];
                        $kaigyou = "\r\n";
                    } else {
                        $row["TOTALSTUDYACT"] .= $rowYouroku["TOTALSTUDYACT"];
                        $row["TOTALSTUDYVAL"] .= $rowYouroku["TOTALSTUDYVAL"];
                    }
                }
            }
            //活動内容
            $height = (int)$model->totalstudyact_gyou * 13.5 + ((int)$model->totalstudyact_gyou -1 ) * 3 + 5;
            if ($model->getSchoolName === 'kyoto') {
                if ($model->Properties["kyotoSougouNyuryokuOk"] == "1") {
                    $extra = "style=\"height:{$height}px;\" onPaste=\"return showKotei(this);\" ";
                } else {
                    $extra = "style=\"height:{$height}px;background:darkgray\" onPaste=\"return showKotei(this);\" readOnly";
                }
            } else {
                $extra = "style=\"height:{$height}px;\" onPaste=\"return showKotei(this);\" ";
            }
            $arg["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_gyou, ((int)$model->totalstudyact_moji * 2 + 1), "soft", $extra, $row["TOTALSTUDYACT"]);
            $arg["TOTALSTUDYACT_TYUI"] = "(全角{$model->totalstudyact_moji}文字{$model->totalstudyact_gyou}行まで)";
            //斜線を入れるチェックボックス
            $extra  = ($row["TOTALSTUDYACT_SLASH_FLG"] == "1") ? "checked" : "";
            $extra .= " id=\"TOTALSTUDYACT_SLASH_FLG\"";
            $arg["TOTALSTUDYACT_SLASH_FLG"] = knjCreateCheckBox($objForm, "TOTALSTUDYACT_SLASH_FLG", "1", $extra, "");

            //評価
            $height = (int)$model->totalstudyval_gyou * 13.5 + ((int)$model->totalstudyval_gyou -1 ) * 3 + 5;
            if ($model->getSchoolName === 'kyoto') {
                if ($model->Properties["kyotoSougouNyuryokuOk"] == "1") {
                    $extra = "style=\"height:{$height}px;\" onPaste=\"return showKotei(this);\" ";
                } else {
                    $extra = "style=\"height:{$height}px;background:darkgray\" onPaste=\"return showKotei(this);\" readOnly";
                }
            } else {
                $extra = "style=\"height:{$height}px;\" onPaste=\"return showKotei(this);\" ";
            }
            $arg["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_gyou, ((int)$model->totalstudyval_moji * 2 + 1), "soft", $extra, $row["TOTALSTUDYVAL"]);
            $arg["TOTALSTUDYVAL_TYUI"] = "(全角{$model->totalstudyval_moji}文字{$model->totalstudyval_gyou}行まで)";
            //斜線を入れるチェックボックス
            $extra  = ($row["TOTALSTUDYVAL_SLASH_FLG"] == "1") ? "checked" : "";
            $extra .= " id=\"TOTALSTUDYVAL_SLASH_FLG\"";
            $arg["TOTALSTUDYVAL_SLASH_FLG"] = knjCreateCheckBox($objForm, "TOTALSTUDYVAL_SLASH_FLG", "1", $extra, "");
            $arg["tyousasyoSougouHyoukaNotNentani"] = "1";
        } else {
            $arg["TOTALSTUDYACT_TYUI"] = "(全角{$model->totalstudyact_moji}文字{$model->totalstudyact_gyou}行まで)";
            $arg["TOTALSTUDYVAL_TYUI"] = "(全角{$model->totalstudyval_moji}文字{$model->totalstudyval_gyou}行まで)";
            $arg["tyousasyoSougouHyoukaNentani"] = "1";
        }
        //備考
        if (in_array($model->cmd, array('torikomi0', 'torikomi1', 'torikomi2', 'torikomiT0', 'torikomiT1', 'torikomiT2'))) {
            $row["REMARK"] = $model->field["REMARK"];
        }
        $height = (int)$model->remark_gyou * 13.5 + ((int)$model->remark_gyou -1 ) * 3 + 5;
        $extra = "style=\"height:{$height}px;\" onPaste=\"return showKotei(this);\" ";
        $arg["REMARK"] = KnjCreateTextArea($objForm, "REMARK", $model->remark_gyou, ((int)$model->remark_moji * 2 + 1), "soft", $extra, $row["REMARK"]);
        $arg["REMARK_TYUI"] = "(全角{$model->remark_moji}文字X{$model->remark_gyou}行まで)";
        knjCreateHidden($objForm, "tyousasyoRemarkFieldSize", $model->Properties["tyousasyoRemarkFieldSize"]);

        /********************/
        /* チェックボックス */
        /********************/
        //学習成績概評チェックボックス
        $objForm->ae( array("type"      => "checkbox",
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
        //教務主任等マスタチェックと海城学園(非表示にする)のチェック
        if ($getIppanCount == 0 && $model->getSchoolName !== 'kaijyo') {
            //生徒指導要録より読込ボタンを作成する
            $extra = $disabled."onclick=\" return btn_submit('reload2');\" style=\"color:#1E90FF;font:bold\"";
            $arg["btn_reload2"] = KnjCreateBtn($objForm, "btn_reload2", "生徒指導要録より読込", $extra);
        }
        //指導上参考となる諸事項
        $query = knje010bQuery::cntSchregBaseMst($model);
        $base_cnt = $db->getOne($query);
        if ($base_cnt > 0) {
            $extra = "onclick=\"return btn_submit('subform6');\" style=\"width:180px;\"";
            $arg["button"]["KYU_SYOUSASYO"] = knjCreateBtn($objForm, "KYU_SYOUSASYO", "指導上参考となる諸事項", $extra);
        }
        //成績参照ボタンを作成する
        $extra = "onclick=\"return btn_submit('form3_first');\" style=\"width:70px\"";
        $arg["btn_form3"] = knjCreateBtn($objForm, "btn_form3", "成績参照", $extra);
        //1,2年次　指導要録取込、3年次　通知票取込み(総合的な学習の時間　通年用)
        if ($model->Properties["tyousasyoSougouHyoukaNentani"] != "1") {
            if (intval($model->gradecd) > "2" && $model->Properties["tutihyoYoriYomikomiHyoujiFlg"] == 1) {
                $extra = "onclick=\"return btn_submit('reload3');\" style=\"color:#1E90FF;font:bold;\"";
                $arg["btn_reload3"] = knjCreateBtn($objForm, "btn_reload3", "通知票取込", $extra);
            } else if (intval($model->gradecd) < "3" && $model->Properties["tyousasyo_SidoYorokYoriYomikomiHyoujiFlg"] == 1) {
                $extra = "onclick=\"return btn_submit('reload3');\" style=\"color:#1E90FF;font:bold;\"";
                $arg["btn_reload3"] = knjCreateBtn($objForm, "btn_reload3", "指導要録取込", $extra);
            }
        }
        //指導要録参照画面ボタンを作成する
        if ($model->Properties["sidouyourokuSansyou"] == 1) {
            $extra = "onclick=\"return btn_submit('form7_first');\" style=\"width:100px\"";
        } else {
            $extra = "onclick=\"return btn_submit('form4_first');\" style=\"width:100px\"";
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
        $securityCnt = $db->getOne(knje010bQuery::getSecurityHigh());
        $csvSetName = "CSV";
        if ($model->Properties["useXLS"]) {
            $csvSetName = "エクセル";
        }
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            //データCSVボタン
            $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX190/knjx190index.php?program_id=".PROGRAMID."&mode={$model->mode}&SEND_PRGID=KNJE010B&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_check1"] = knjCreateBtn($objForm, "btn_check1", "データ".$csvSetName, $extra);
            //ヘッダデータCSVボタン
            $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX191/knjx191index.php?program_id=".PROGRAMID."&mode={$model->mode}&SEND_PRGID=KNJE010B&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_check2"] = knjCreateBtn($objForm, "btn_check2", "ヘッダデータ".$csvSetName, $extra);
        }
        //所見確認用
        if ($model->Properties["tyousasyoShokenPreview"] == '1') {
            $extra = "onclick=\"return newwin('".SERVLET_URL."');\"";
            $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "所見確認用", $extra);
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
        knjCreateHidden($objForm, "LEFT_GRADE", $model->grade);
        knjCreateHidden($objForm, "tyousasyoSougouHyoukaNentani", $model->Properties["tyousasyoSougouHyoukaNentani"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "gaihyouGakkaBetu", $model->Properties["gaihyouGakkaBetu"]);
        knjCreateHidden($objForm, "useTotalstudySlashFlg", $model->Properties["useTotalstudySlashFlg"]);
        knjCreateHidden($objForm, "useAttendrecRemarkSlashFlg", $model->Properties["useAttendrecRemarkSlashFlg"]);
        knjCreateHidden($objForm, "tyousasyoSpecialactrecFieldSizeDefaultPrint", $model->Properties["tyousasyoSpecialactrecFieldSizeDefaultPrint"]);

        //所見確認用パラメータ
        if ($model->Properties["tyousasyoShokenPreview"] == '1') {
            knjCreateHidden($objForm, "PRGID", "KNJE010B");
            knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
            knjCreateHidden($objForm, "PRINT_YEAR", CTRL_YEAR);
            knjCreateHidden($objForm, "PRINT_GAKKI", CTRL_SEMESTER);
            knjCreateHidden($objForm, "GRADE_HR_CLASS");
            knjCreateHidden($objForm, "KANJI", "1");
            knjCreateHidden($objForm, "OS", "1");
            knjCreateHidden($objForm, "OUTPUT", "1");
            knjCreateHidden($objForm, "3_or_6_nenYoForm", $model->Properties["3_or_6_nenYoForm"]);
            //何年用のフォームを使うのかの初期値を判断する
            $query = knje010bQuery::getSchoolDiv();
            $schooldiv = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $nenyoformSyokiti = $schooldiv["NEN"] == '0' ? ($schooldiv["SCHOOLDIV"] == '0' ? '3' : '4') : $schooldiv["NEN"];
            knjCreateHidden($objForm, "NENYOFORM", $nenyoformSyokiti);
            knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
            knjCreateHidden($objForm, "useSyojikou3", $model->Properties["useSyojikou3"]);
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
            knjCreateHidden($objForm, "train_ref_1_2_3_field_size", $model->Properties["train_ref_1_2_3_field_size"]);
            knjCreateHidden($objForm, "train_ref_1_2_3_gyo_size", $model->Properties["train_ref_1_2_3_gyo_size"]);
            knjCreateHidden($objForm, "tyousasyoSougouHyoukaNentaniPrintCombined", $model->Properties["tyousasyoSougouHyoukaNentaniPrintCombined"]);

            knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
            knjCreateHidden($objForm, "useCertifSchPrintCnt",              $model->Properties["useCertifSchPrintCnt"]);
            knjCreateHidden($objForm, "nenYoForm",                         $model->Properties["nenYoForm"]);
            knjCreateHidden($objForm, "tyousasyoNotPrintEnterGrade",       $model->Properties["tyousasyoNotPrintEnterGrade"]);
            knjCreateHidden($objForm, "tyousasyoSyusyokuPrintGappeiTougou",       $model->Properties["tyousasyoSyusyokuPrintGappeiTougou"]);
            knjCreateHidden($objForm, "tyousasyoHankiNintei",              $model->Properties["tyousasyoHankiNintei"]);
            knjCreateHidden($objForm, "useClassDetailDat",                 $model->Properties["useClassDetailDat"]);
            knjCreateHidden($objForm, "useAddrField2" ,                    $model->Properties["useAddrField2"]);
            knjCreateHidden($objForm, "useProvFlg" ,                       $model->Properties["useProvFlg"]);
            knjCreateHidden($objForm, "useGakkaSchoolDiv" ,                $model->Properties["useGakkaSchoolDiv"]);
            knjCreateHidden($objForm, "useAssessCourseMst",                $model->Properties["useAssessCourseMst"]);
            knjCreateHidden($objForm, "tyousasyoUseEditKinsoku",           $model->Properties["tyousasyoUseEditKinsoku"]);
            knjCreateHidden($objForm, "certifPrintRealName",               $model->Properties["certifPrintRealName"]);
            knjCreateHidden($objForm, "tyousasyoCheckCertifDate",          $model->Properties["tyousasyoCheckCertifDate"]);
            knjCreateHidden($objForm, "tyousasyoPrintHomeRoomStaff",       $model->Properties["tyousasyoPrintHomeRoomStaff"]);
            knjCreateHidden($objForm, "tyousasyoPrintCoursecodename",      $model->Properties["tyousasyoPrintCoursecodename"]);
            knjCreateHidden($objForm, "tyousasyoPrintChairSubclassSemester2", $model->Properties["tyousasyoPrintChairSubclassSemester2"]);
            knjCreateHidden($objForm, "tyousasyoHanasuClasscd",            $model->Properties["tyousasyoHanasuClasscd"]);
            knjCreateHidden($objForm, "tyousasyoJiritsuKatsudouRemark",    $model->Properties["tyousasyoJiritsuKatsudouRemark"]);
            knjCreateHidden($objForm, "NENYOFORM_CHECK", $model->Properties["nenYoForm"]);
        }

        if(get_count($model->warning)== 0 && $model->cmd !="reset") {
            $arg["next"] = "NextStudent2(0);";
        } elseif($model->cmd =="reset") {
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
        View::toHTML($model, "knje010bForm1.html", $arg);
    }
}

function makeHexamEntRemarkDat(&$objForm, &$arg, $db, &$model) {
    $model->schArray = array();
    $disabled = "disabled";
    $query = knje010bQuery::selectQueryAnnual($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $model->schArray[$row["GRADE"]] = array("YEAR"  => $row["YEAR"],
                                                "ANNUAL" => $row["ANNUAL"]);
    }
    $result->free();

    $opt = array();
    $query = knje010bQuery::getGdat($model);

    $result = $db->query($query);
    while ($gRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("GRADE" => $gRow["GRADE"], "GRADE_CD" => $gRow["GRADE_CD"]);
    }
    $result->free();

    $totalremark_readTrainRef = "";
    $totalremark_kaigyo = "";
    $gradeCnt = 0;
    $hiddenYear = "";
    $yearSep = "";
    foreach ($opt as $key) {
        $grade = (int) $key["GRADE_CD"];
        $disabled = is_array($model->schArray[$key["GRADE"]]) ? "" : " disabled ";
        $year = $model->schArray[$key["GRADE"]]["YEAR"];

        //表示用の年度をセット
        if ($year != "" && $grade != "") {
            $arg["YEAR".$grade] = '('.$year.'年度)';
        }

        if ($year) {
            $hiddenYear .= $yearSep.$year;
            $yearSep = ",";
        }
        $isReadData = false;
        if (!isset($model->warning)) {
            if (($model->cmd == "reload2_ok" || $model->cmd == "reload2_cancel") && ($model->readYear == "0000" || $model->readYear == $year)) {
                $query = knje010bQuery::selectQuery_Htrainremark_Dat($model, $year);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($model->cmd == "reload2_cancel") {
                    if ($row) {
                        foreach ($row as $key => $val) {
                            if ($key == "TRAIN_REF1") {
                                $row[$key] = $model->field2[$year]["TRAIN_REF"]."\n".$val;
                            } else {
                                $row[$key] = $model->field2[$year][$key]."\n".$val;
                            }
                        }
                    } else {
                        $row = $model->field2[$year];
                        $row["TRAIN_REF1"] = $model->field2[$year]["TRAIN_REF"];
                    }
                }
                $isReadData = true;
            } else {
                if (!in_array($model->cmd, array('torikomi0', 'torikomi1', 'torikomi2', 'torikomiT0', 'torikomiT1', 'torikomiT2')) && $model->cmd != "reload3_1" && $model->cmd != "reload3_2" && $model->cmd != "reload3_3" && $model->cmd != "reload3" && $model->cmd != "reload2_ok" && $model->cmd != "reload2_cancel") {
                    $query = knje010bQuery::selectQueryForm2($model, $year);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                } else {
                    $row = $model->field2[$year];
                    $row["TRAIN_REF1"] = $model->field2[$year]["TRAIN_REF"];
                }
            }
            if (($model->cmd == "reload2_ok" || $model->cmd == "reload2_cancel") && $model->Properties["useSyojikou3"] == "1") {
                //文京のとき
                if ($model->schoolName === 'bunkyo') {
                    $model->field2[$year]["TRAIN_REF"] = $row["TRAIN_REF1"];
                    $model->field2[$year]["TRAIN_REF2"] = $row["TRAIN_REF2"];
                    $model->field2[$year]["TRAIN_REF3"] = $row["TRAIN_REF3"];
                //常磐仕様ではない時は、取込めないため今までどおりに画面の値をセット
                } else if ($model->schoolName !== 'tokiwa' && $model->Properties["useSyojikou3_torikomi"] != "1") {
                    $row["TRAIN_REF1"] = $model->field2[$year]["TRAIN_REF"];
                    $row["TRAIN_REF2"] = $model->field2[$year]["TRAIN_REF2"];
                    $row["TRAIN_REF3"] = $model->field2[$year]["TRAIN_REF3"];
                //常磐仕様では2013年の1年生から取込み可能
                } else if ($model->schoolName === 'tokiwa' && ($year == "" || (intval($year) < "2013") || $year === '2013' && $key["GRADE"] !== '01') || ($year === '2014' && $key["GRADE"] === '03')) {
                    $row["TRAIN_REF1"] = $model->field2[$year]["TRAIN_REF"];
                    $row["TRAIN_REF2"] = $model->field2[$year]["TRAIN_REF2"];
                    $row["TRAIN_REF3"] = $model->field2[$year]["TRAIN_REF3"];
                }
            }
            if ($model->cmd == "reload3_1" || $model->cmd == "reload3_2" || $model->cmd == "reload3_3") {
                $row["TRAIN_REF1"] = $model->field2[$year]["TRAIN_REF"];
            }

            //指導要録データ
            if ($model->Properties["useSyojikou3"] == "1") {
                $query = knje010bQuery::sansyou_data($model, $year);
                $sansyou = $db->getRow($query, DB_FETCHMODE_ASSOC);

                $totalremark_readTrainRef .= $totalremark_kaigyo.$sansyou["TOTALREMARK"];
                $totalremark_kaigyo = $totalremark_readTrainRef ? "\n-------------------------\n" : "";
            }

            if (($model->cmd == "reload2_ok" || $model->cmd == "reload2_cancel") && !$isReadData) {
                $row["ATTENDREC_REMARK"] = $model->field2[$year]["ATTENDREC_REMARK"];
                $row["SPECIALACTREC"] = $model->field2[$year]["SPECIALACTREC"];
            }
        } else {
            $row = $model->field2[$year];
            $row["TRAIN_REF1"] = $model->field2[$year]["TRAIN_REF"];
        }

        //出欠の記録備考取込（対応する学年のみ）
        if ($model->cmd === "torikomi".$gradeCnt) {
            $set_remark = knje010bQuery::getSemesRemark($model, $db, $year);
            $row["ATTENDREC_REMARK"] = $set_remark;
            $row["TRAIN_REF1"] = $model->field2[$year]["TRAIN_REF"];
        } else if ($model->cmd === "torikomiT".$gradeCnt) {
            $set_remark = knje010bQuery::getHreportremarkDetailDat($db, $model, $year);
            $row["ATTENDREC_REMARK"] = $set_remark;
        //対応しない学年の時は画面の値をセット
        } else if ($model->cmd !== "reload2_ok" && $model->cmd !== "reload2_cancel" && $model->cmd !== "reload3" && $model->cmd != "reload3_1" && $model->cmd != "reload3_2" && $model->cmd != "reload3_3" && $model->cmd !== "yomikomi" && $model->cmd !== "edit" && $model->cmd !== "reset" && $model->cmd !== "updEdit") {
            $row["ATTENDREC_REMARK"] = $model->field2[$year]["ATTENDREC_REMARK"];
            $row["TRAIN_REF1"] = $model->field2[$year]["TRAIN_REF"];
        }
        
        //出欠の記録備考
        $extra = $disabled." onPaste=\"return showPaste(this, ".$gradeCnt.");\" ";
        $arg["ATTENDREC_REMARK".$grade] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK-".$year, ($model->attendrec_remark_gyou + 1), ((int)$model->attendrec_remark_moji * 2 + 1), "soft", $extra, $row["ATTENDREC_REMARK"]);
        $arg["ATTENDREC_REMARK_TYUI"] = "(全角{$model->attendrec_remark_moji}文字{$model->attendrec_remark_gyou}行まで)";
        //特記事項なしチェックボックス
        $extra = " id=\"INS_COMMENTS{$grade}\" onclick=\"return insertComment(this, 'ATTENDREC_REMARK-{$year}', 'INS_COMMENTS_LABEL-{$year}');\"";
        $arg["INS_COMMENTS".$grade] = knjCreateCheckBox($objForm, "INS_COMMENTS-".$year, "1", $extra, "");
        //特記事項なし
        $ins_comments_label = '特記事項なし';
        knjCreateHidden($objForm, "INS_COMMENTS_LABEL-".$year, $ins_comments_label);
        $arg["INS_COMMENTS_LABEL".$grade] = $ins_comments_label;
        //出欠の記録備考の「斜線を入れる」チェックボックス表示
        if ($model->Properties["useAttendrecRemarkSlashFlg"] == 1) {
            $arg["useAttendrecRemarkSlashFlg"] = 1;
        }
        //斜線を入れるチェックボックス
        $extra  = ($row["ATTENDREC_REMARK_SLASH_FLG"] == "1") ? "checked" : "";
        $extra .= " id=\"ATTENDREC_REMARK_SLASH_FLG".$grade."\"";
        $arg["ATTENDREC_REMARK_SLASH_FLG".$grade] = knjCreateCheckBox($objForm, "ATTENDREC_REMARK_SLASH_FLG-".$year, "1", $extra, "");

        //出欠備考参照ボタン
        $sdate = ($year) ? $year.'-04-01' : "";
        $edate = ($year) ? ((int)$year+1).'-03-31' : "";
        //和暦表示フラグ
        $warekiFlg = "";
        if ($model->Properties["useWarekiHyoji"] == 1) {
            $warekiFlg = "1";
        }
        if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
            //まとめ出欠備考を取込みへ変更する
            if ($model->Properties["useTorikomiAttendSemesRemarkDat"] == 1) {
                $setname = 'まとめ出欠備考取込';
                $setcmd = "torikomi".$gradeCnt;
                $extra = $disabled ." style=\"color:#1E90FF;font:bold\" onclick=\"return btn_submit('".$setcmd."');\"";
            } else {
                $setname = 'まとめ出欠備考参照';
                $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            }
            $arg["SANSYO".$grade] = KnjCreateBtn($objForm, "SANSYO".$grade, $setname, $extra);
        } else if ($model->getSchoolName == "mieken") {
            $setcmd = "torikomiT".$gradeCnt;
            $extra = $disabled ." style=\"color:#1E90FF;font:bold\" onclick=\"return btn_submit('".$setcmd."');\"";
            $arg["SANSYO".$grade] = KnjCreateBtn($objForm, "SANSYO".$grade, "通知票取込", $extra);
        } else {
            $extra = $disabled."onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            $arg["SANSYO".$grade] = KnjCreateBtn($objForm, "SANSYO".$grade, "日々出欠備考参照", $extra);
        }
        //要録の出欠備考参照ボタン
        $extra = $disabled."onclick=\"loadwindow('../../X/KNJXATTEND_HTRAINREMARK/index.php?YEAR={$year}&SCHREGNO={$model->schregno}',0,document.documentElement.scrollTop || document.body.scrollTop,360,180);return;\" style=\"width:210px;\"";
        $arg["YOROKU_SANSYO".$grade] = KnjCreateBtn($objForm, "YOROKU_SANSYO".$grade, "要録の出欠の記録備考参照", $extra);
        //年間出欠備考選択ボタン
        if ($model->Properties["useReasonCollectionBtn"] == 1) {
            $arg["btn_reason_collection_select".$grade] = makeSelectBtn($objForm, $model, "reason_collection", "btn_reason_collection_select".$grade, "年間出欠備考選択", "ATTENDREC_REMARK-".$year, $year, $disabled);
            $arg["REASON_COLLECTION_SELECT"] = 1;
        }
        //出欠の記録参照ボタン
        if ($model->Properties["tyousasyoSyukketsuKirokuBtn"] == 1) {
            $arg["btn_syukketsu_sansyo".$grade] = makeSelectBtn($objForm, $model, "syukketsukiroku", "btn_syukketsu_sansyo".$grade, "出欠の記録参照", "ATTENDREC_REMARK-".$year, $year, $disabled);
            $arg["tyousasyoSyukketsuKirokuBtn"] = 1;
        }

        //委員会選択ボタン
        $arg["btn_committee".$grade] = makeSelectBtn($objForm, $model, "committee", "btn_committee".$grade, "委員会選択", "SPECIALACTREC-".$year, $year, $disabled);

        //賞選択ボタン
        if ($model->Properties["useHyosyoSansyoButton_H"]) {
            //特別活動の記録
            $arg["btn_hyosyo".$grade."_spe"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo".$grade, "賞選択", "SPECIALACTREC-".$year, $year, $disabled);
            if ($model->Properties["useSyojikou3"] == "1") {
                //指導上参考となる諸事項（3分割・中）
                $arg["btn_hyosyo".$grade."_tra2"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo".$grade, "賞選択", "TRAIN_REF2-".$year, $year, $disabled);
            } else {
                //指導上参考となる諸事項
                $arg["btn_hyosyo".$grade."_tra1"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo".$grade, "賞選択", "TRAIN_REF1-".$year, $year, $disabled);
            }
        }

        //特別活動の記録
        $extra = $disabled." class=\"specialactrec_\" onPaste=\"return showPaste(this, ".$gradeCnt.");\" ";
        $arg["SPECIALACTREC".$grade]    = KnjCreateTextArea($objForm, "SPECIALACTREC-".$year, ($model->specialactrec_gyou + 1), ((int)$model->specialactrec_moji * 2 + 1), "soft", $extra, $row["SPECIALACTREC"]);
        $arg["SPECIALACTREC_TYUI"] = "(全角{$model->specialactrec_moji}文字{$model->specialactrec_gyou}行まで)";

        //部活動選択ボタン
        //特別活動の記録
        $arg["btn_club".$grade."_spe"] = makeSelectBtn($objForm, $model, "club", "btn_club".$grade, "部活動選択", "SPECIALACTREC-".$year, $year, $disabled);
        if ($model->Properties["useSyojikou3"] == "1") {
            //指導上参考となる諸事項（3分割・中）
            $arg["btn_club".$grade."_tra2"] = makeSelectBtn($objForm, $model, "club", "btn_club".$grade, "部活動選択", "TRAIN_REF2-".$year, $year, $disabled);
        } else {
            //指導上参考となる諸事項
            $arg["btn_club".$grade."_tra1"] = makeSelectBtn($objForm, $model, "club", "btn_club".$grade, "部活動選択", "TRAIN_REF1-".$year, $year, $disabled);
        }

        //記録備考選択ボタン
        if ($model->Properties["club_kirokubikou"] == 1) {
            //特別活動の記録
            $arg["btn_club_kirokubikou".$grade."_spe"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou".$grade, "記録備考選択", "SPECIALACTREC-".$year, $year, $disabled);
            if ($model->Properties["useSyojikou3"] == "1") {
                //指導上参考となる諸事項（3分割・中）
                $arg["btn_club_kirokubikou".$grade."_tra2"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou".$grade, "記録備考選択", "TRAIN_REF2-".$year, $year, $disabled);
            } else {
                //指導上参考となる諸事項
                $arg["btn_club_kirokubikou".$grade."_tra1"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou".$grade, "記録備考選択", "TRAIN_REF1-".$year, $year, $disabled);
            }
        }

        //検定選択ボタン
        //特別活動の記録
        $arg["btn_qualified".$grade."_spe"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified".$grade, "検定選択", "SPECIALACTREC-".$year, $year, $disabled);
        if ($model->Properties["useSyojikou3"] == "1") {
            //指導上参考となる諸事項（3分割・中）
            $arg["btn_qualified".$grade."_tra2"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified".$grade, "検定選択", "TRAIN_REF2-".$year, $year, $disabled);
        } else {
            //指導上参考となる諸事項
            $arg["btn_qualified".$grade."_tra1"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified".$grade, "検定選択", "TRAIN_REF1-".$year, $year, $disabled);
        }

        //指導上参考となる諸事項
        if ($model->Properties["useSyojikou3"] == "1") {
            $height1 = (int)$model->train_ref1_gyou * 13.5 + ((int)$model->train_ref1_gyou -1 ) * 3 + 5;
            $height2 = (int)$model->train_ref2_gyou * 13.5 + ((int)$model->train_ref2_gyou -1 ) * 3 + 5;
            $height3 = (int)$model->train_ref3_gyou * 13.5 + ((int)$model->train_ref3_gyou -1 ) * 3 + 5;
            $extra1 = $disabled."style=\"height:{$height1}px;\" onPaste=\"return showPaste(this, ".$gradeCnt.");\" ";
            $extra2 = $disabled."style=\"height:{$height2}px;\" onPaste=\"return showPaste(this, ".$gradeCnt.");\" ";
            $extra3 = $disabled."style=\"height:{$height3}px;\" onPaste=\"return showPaste(this, ".$gradeCnt.");\" ";
            $arg["TRAIN_REF".$grade."_1"] = KnjCreateTextArea($objForm, "TRAIN_REF1-".$year, ((int)$model->train_ref1_gyou + 1), ((int)$model->train_ref1_moji * 2 + 1), "soft", $extra1, $row["TRAIN_REF1"]);
            $arg["TRAIN_REF".$grade."_2"] = KnjCreateTextArea($objForm, "TRAIN_REF2-".$year, ((int)$model->train_ref2_gyou + 1), ((int)$model->train_ref2_moji * 2 + 1), "soft", $extra2, $row["TRAIN_REF2"]);
            $arg["TRAIN_REF".$grade."_3"] = KnjCreateTextArea($objForm, "TRAIN_REF3-".$year, ((int)$model->train_ref3_gyou + 1), ((int)$model->train_ref3_moji * 2 + 1), "soft", $extra3, $row["TRAIN_REF3"]);

            if ($model->Properties["seitoSidoYoroku_dat_TotalremarkSize"]) {
                list($moji, $gyou) = preg_split("/\*/", $model->Properties["seitoSidoYoroku_dat_TotalremarkSize"]);
                $model->totalremark_moji = (int)trim($moji);
                $model->totalremark_gyou = (int)trim($gyou);
            } else {
                $model->totalremark_moji = 44; //デフォルトの値
                $model->totalremark_gyou = 6;  //デフォルトの値
            }

            $setHeight = (int)$model->totalremark_gyou * 15;
            $extra = "style=\"background-color:#D0D0D0;height:{$setHeight}px;\"";
            $arg["TOTALREMARK"] = KnjCreateTextArea($objForm, "TOTALREMARK", 5, ((int)$model->totalremark_moji * 2), "soft", $extra, $totalremark_readTrainRef);
            $arg["useSyojikou3"] = $model->Properties["useSyojikou3"];
            $arg["COLSPAN2"] = "colspan=\"3\"";
            $arg["COLSPAN_CHANGE"] = "colspan=\"4\"";
            $arg["TRAIN_REF1_COMMENT"] = "(全角{$model->train_ref1_moji}文字{$model->train_ref1_gyou}行まで)";
            $arg["TRAIN_REF2_COMMENT"] = "(全角{$model->train_ref2_moji}文字{$model->train_ref2_gyou}行まで)";
            $arg["TRAIN_REF3_COMMENT"] = "(全角{$model->train_ref3_moji}文字{$model->train_ref3_gyou}行まで)";
        } else {
            $extra = $disabled."style=\"height:105px;\" onPaste=\"return showPaste(this, ".$gradeCnt.");\" ";
            $arg["COLSPAN_TRAIN_REF"] = "colspan=\"2\"";
            $arg["COLSPAN_CHANGE"] = "colspan=\"2\"";
            if ($model->Properties["tyousasyoTokuBetuFieldSize"] == 1) {
                $arg["TRAIN_REF".$grade."_1"] = KnjCreateTextArea($objForm, "TRAIN_REF1-".$year, 7, 117, "soft", $extra, $row["TRAIN_REF"]);
                $arg["TRAIN_REF_COMMENT"] = "(全角58文字X7行まで)";
            } else {
                $arg["TRAIN_REF".$grade."_1"] = KnjCreateTextArea($objForm, "TRAIN_REF1-".$year, 5, 83, "soft", $extra, $row["TRAIN_REF"]);
                $arg["TRAIN_REF_COMMENT"] = "(全角41文字X5行まで)";
            }
        }
        
        if ($model->Properties["tyousasyoSougouHyoukaNentani"] == 1) {
            //1,2年次　指導要録取込、3年次　通知書取込(総合的な学習の時間　年単位用)
            if ($grade < "3" && $model->Properties["tyousasyo_SidoYorokYoriYomikomiHyoujiFlg"] == 1) {
                $setName = "指導要録取込";
                $setcmd = 'reload3_'.$grade;
                $extra = "onclick=\"return btn_submit('".$setcmd."');\" style=\"color:#1E90FF;font:bold;\"";
                $arg["btn_reload3_".$grade] = knjCreateBtn($objForm, "btn_reload3_".$grade, $setName, $extra);
            } else if ($grade > "2" && $model->Properties["tutihyoYoriYomikomiHyoujiFlg"] == 1) {
                $setName = "通知票取込";
                $setcmd = 'reload3_'.$grade;
                $extra = "onclick=\"return btn_submit('".$setcmd."');\" style=\"color:#1E90FF;font:bold;\"";
                $arg["btn_reload3_".$grade] = knjCreateBtn($objForm, "btn_reload3_".$grade, $setName, $extra);
            }
        
            //指導要録取込ボタンが押された時の指導要録より読込む(かつ年単位のとき)　1, 2年次用
            if ($model->cmd == 'reload3_'.$grade && $grade < "3") {
                $query = knje010bQuery::selectQuery_Htrainremark_Dat($model, $year);
                $getRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $row["TOTALSTUDYVAL"] = $getRow["TOTALSTUDYVAL"];
                $row["TOTALSTUDYACT"] = $getRow["TOTALSTUDYACT"];
            //通知票取込ボタンが押された時の通知書より読込む(かつ年単位のとき)　3年次用
            } else if ($model->cmd == 'reload3_'.$grade && $grade > "2") {
                $query = knje010bQuery::get_record_totalstudytime_dat($model, $year);
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
            //活動内容
            $height = (int)$model->totalstudyact_gyou * 13.5 + ((int)$model->totalstudyact_gyou -1 ) * 3 + 5;
            if ($model->getSchoolName === 'kyoto') {
                if ($model->Properties["kyotoSougouNyuryokuOk"] == "1") {
                    $extra = $disabled."style=\"height:{$height}px;\" onPaste=\"return showKotei(this);\" ";
                } else {
                    $extra = $disabled."style=\"height:{$height}px;background:darkgray\" onPaste=\"return showKotei(this);\" readOnly";
                }
            } else {
                $extra = $disabled."style=\"height:{$height}px;\" onPaste=\"return showKotei(this);\" ";
            }
            $arg["TOTALSTUDYACT".$grade] = KnjCreateTextArea($objForm, "TOTALSTUDYACT-".$year, $model->totalstudyact_gyou, ((int)$model->totalstudyact_moji * 2 + 1), "soft", $extra, $row["TOTALSTUDYACT"]);
            //斜線を入れるチェックボックス
            $extra  = ($row["TOTALSTUDYACT_SLASH_FLG"] == "1") ? "checked" : "";
            $extra .= " id=\"TOTALSTUDYACT_SLASH_FLG{$grade}\"";
            $arg["TOTALSTUDYACT_SLASH_FLG".$grade] = knjCreateCheckBox($objForm, "TOTALSTUDYACT_SLASH_FLG-".$year, "1", $extra, "");

            //評価
            $height = (int)$model->totalstudyval_gyou * 13.5 + ((int)$model->totalstudyval_gyou -1 ) * 3 + 5;
            if ($model->getSchoolName === 'kyoto') {
                if ($model->Properties["kyotoSougouNyuryokuOk"] == "1") {
                    $extra = $disabled."style=\"height:{$height}px;\" onPaste=\"return showKotei(this);\" ";
                } else {
                    $extra = $disabled."style=\"height:{$height}px;background:darkgray\" onPaste=\"return showKotei(this);\" readOnly";
                }
            } else {
                $extra = $disabled."style=\"height:{$height}px;\" onPaste=\"return showKotei(this);\" ";
            }
            $arg["TOTALSTUDYVAL".$grade] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL-".$year, $model->totalstudyval_gyou, ((int)$model->totalstudyval_moji * 2 + 1), "soft", $extra, $row["TOTALSTUDYVAL"]);
            //斜線を入れるチェックボックス
            $extra  = ($row["TOTALSTUDYVAL_SLASH_FLG"] == "1") ? "checked" : "";
            $extra .= " id=\"TOTALSTUDYVAL_SLASH_FLG{$grade}\"";
            $arg["TOTALSTUDYVAL_SLASH_FLG".$grade] = knjCreateCheckBox($objForm, "TOTALSTUDYVAL_SLASH_FLG-".$year, "1", $extra, "");
        }
        $gradeCnt++;
    }
    knjCreateHidden($objForm, "hiddenYear", $hiddenYear);
}

//選択ボタン
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $year, $disabled="") {
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        if ($div == "club") {                   //部活動
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } else if ($div == "committee") {       //委員会
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"";
        } else if ($div == "qualified") {       //検定
            $sizeW = ($model->Properties["useQualifiedMst"] == "1") ? 800 : 670;
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$sizeW},500);\"";
        } else if ($div == "hyosyo") {          //賞
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_HYOSYO_SELECT/knjx_hyosyo_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,600,350);\"";
        } else if ($div == "kirokubikou") {     //記録備考
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_KIROKUBIKOU_SELECT/knjx_club_kirokubikou_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } else if ($div == "reason_collection") {   //年間出欠備考
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_REASON_COLLECTION_SELECT/knjx_reason_collection_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } else if ($div == "syukketsukiroku") {   //出欠の記録参照
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_SYUKKETSUKIROKU/knjx_syukketsukirokuindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}
?>
