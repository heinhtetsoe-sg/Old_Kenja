<?php

require_once("for_php7.php");

//ビュー作成用クラス
class knje011gForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje011gindex.php", "", "edit");
        $db = Query::dbCheckOut();

        if (!isset($model->warning) && $model->cmd != 'reload3') {
            $query = knje011gQuery::selectQuery($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $query = knje011gQuery::getHexamEntremarkRemarkHdat($model);
            $row["REMARK"] = $db->getOne($query);

            $arg["NOT_WARNING"] = 1;
        } else {
            $row = $model->field;
        }
        $arg["tyousasyo2020"] = "1";

        if ($model->Properties["useMaruA_avg"] != "") {
            $arg["MARU_A_AVG"] = $model->Properties["useMaruA_avg"];
            $arg["UseMaruA_avg"] = 1;
        } else {
            $arg["UnUseMaruA_avg"] = 1;
        }

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        $arg["ATTEND_TITLE"] = $model->attendTitle."の記録";

        //「総合的な学習の時間の内容・評価」 を表示するかしないかのフラグ
//        if ($model->Properties["tyousasyoSougouHyoukaNentani"] != 1 || "1" == $model->Properties["tyousasyo2020"]) {
            $arg["tyousasyoSougouHyoukaNentani"] = 1;
//        } else {
//            $arg["tyousasyoSougouHyoukaNentani_for_title"] = 1;
//        }

        //年次取得
        $model->gradecd = "";
        $model->gradecd = $db->getOne(knje011gQuery::getGradeCd($model));

        //総合的な学習の時間
        makeSogotekinaGakushunoJikan($arg, $objForm, $db, $model, $row);

        //備考
        makeBiko($arg, $objForm, $model, $row);

        /********************/
        /* チェックボックス */
        /********************/
        //学習成績概評チェックボックス
        //チェックボックスを作成
        $extra = $row["COMMENTEX_A_CD"] == "1" ? "checked" : "";
        $extra .= " id=\"comment\"";
        $arg["COMMENTEX_A_CD"] = knjCreateCheckBox($objForm, "COMMENTEX_A_CD", "1", $extra, "");

        //特記事項なし
        knjCreateHidden($objForm, "NO_COMMENTS_LABEL", $model->no_comments_label);
        $arg["NO_COMMENTS_LABEL"] = $model->no_comments_label;

        /**********/
        /* ボタン */
        /**********/
        makeButton($arg, $objForm, $db, $model);

        /**********/
        /* hidden */
        /**********/
        makeHidden($objForm, $db, $model);

        if (strlen($model->warning)== 0 && $model->cmd !="reset") {
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
        View::toHTML($model, "knje011gForm1.html", $arg);
    }
}

function makeSogotekinaGakushunoJikan(&$arg, &$objForm, $db, $model, &$row)
{
    //「総合的な学習の時間の内容・評価」 を表示するかしないかのフラグ
    //if ($model->Properties["tyousasyoSougouHyoukaNentani"] != 1 || "1" == $model->Properties["tyousasyo2020"]) {
    if ($model->cmd == "sogo_yomikomi") {
        //指導要録取込、通知票取込
        torikomiRowTotalstudy($db, $model, $row);
    }

        //総合的な学習の時間の「斜線を入れる」チェックボックス表示
    if ($model->Properties["useTotalstudySlashFlg"] == 1) {
        $arg["useTotalstudySlashFlg"] = 1;
    }

        //活動内容
        makeTotalstudy($arg, $objForm, $model, "TOTALSTUDYACT", $row);
        //評価
        makeTotalstudy($arg, $objForm, $model, "TOTALSTUDYVAL", $row);

        //1,2年次　指導要録取込、3年次　通知票取込み(総合的な学習の時間　通年用)
    if (intval($model->gradecd) > "2" && $model->Properties["tutihyoYoriYomikomiHyoujiFlg"] == 1) {
        $extra = " class=\"btn_torikomi\" onclick=\"return btn_submit('sogo_yomikomi');\" ";
        $arg["button"]["btn_sogo_yomikomi"] = knjCreateBtn($objForm, "btn_sogo_yomikomi", "通知票取込", $extra);
    } elseif (intval($model->gradecd) < "3" && $model->Properties["tyousasyo_SidoYorokYoriYomikomiHyoujiFlg"] == 1) {
        $extra = " class=\"btn_torikomi\" onclick=\"return btn_submit('sogo_yomikomi');\" ";
        $arg["button"]["btn_sogo_yomikomi"] = knjCreateBtn($objForm, "btn_sogo_yomikomi", "指導要録取込", $extra);
    }
    //}
}


function makeBiko(&$arg, &$objForm, $model, &$row)
{
    $moji = $model->mojigyou["REMARK"]["moji"];
    $gyou = $model->mojigyou["REMARK"]["gyou"];
    $height = $gyou * 13.5 + ($gyou -1 ) * 3 + 5;
    $extra = "style=\"height:{$height}px; ";
    if ($gyou < 20) {
        $arg["REMARK_HEIGHT"] = "height: ".($gyou + 1)."em; overflow-y: hidden;";
        $extra .= " overflow-y: scroll; ";
    } else {
        $arg["REMARK_HEIGHT"] = "height: 20em; overflow-y: scroll;";
        $extra .= " overflow-y: hidden; ";
    }
    $extra .= " \"";
    $arg["REMARK"] = KnjCreateTextArea($objForm, "REMARK", $gyou, ($moji * 2 + 1), "soft", $extra, $row["REMARK"]);
    $arg["REMARK_TYUI"] = "(全角{$moji}文字X{$gyou}行まで)";
    if ("1" != $model->Properties["unuse_KNJE015_HEXAM_ENTREMARK_LEARNING_DAT_REMARK"]) {
        $extra = " onclick=\"loadwindow('".REQUESTROOT."/E/KNJE011G/knje011gindex.php?&cmd=formBikouTori&TARGET=REMARK',0,document.documentElement.scrollTop || document.body.scrollTop,1000,650);\"";
        $arg["button"]["btn_formBikouTori"] = knjCreateBtn($objForm, "btn_formBikouTori", "備考取込", $extra);
    }

    //特記事項なしチェックボックス
    $extra  = ($model->field["REMARK_NO_COMMENTS"] == "1") ? "checked" : "";
    $extra .= " id=\"REMARK_NO_COMMENTS\" onclick=\"return CheckRemark();\"";
    $arg["REMARK_NO_COMMENTS"] = knjCreateCheckBox($objForm, "REMARK_NO_COMMENTS", "1", $extra, "");
}

function makeButton(&$arg, &$objForm, $db, $model)
{
    // 指導上参考となる諸事項 年組一括取込
    if ("1" == $model->Properties["tyousasho6bunkatsu_homeroomShojikouTorikomi"]) {
        $query = knje011gQuery::getHrShojikouTorikomiDataCheck($model);
        $result = $db->query($query);
        $chk_hrname = "";
        $countYears = array();
        while ($chkrow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $chk_hrname = $chkrow["HR_NAME"];
            if ($chkrow["YEAR"]) {
                $countYears[] = " '".$chkrow["YEAR"]."年度 ".$chkrow["COUNT"]."件' ";
            }
        }
        $result->free();
        $chkArg = "{'YEAR' : '".$model->exp_year."', 'HR_NAME': '".$chk_hrname."', 'COUNT_YEARS': [".implode(",", $countYears)."]}";
        $extra = " class=\"btn_torikomi\" onclick=\"return btn_submit('hrShojikouTorikomi', ".$chkArg.");\"  ";
        $arg["button"]["btn_homeRoomShojikouTori"] = knjCreateBtn($objForm, "btn_homeRoomShojikouTori", "指導上参考となる諸事項 年組一括取込", $extra);
    }

    //特別な活動～ボタンを作成する
    $extra = "onclick=\"return btn_submit('form2_first');\" style=\"width:520px\"";
    $title = '特別活動の記録 ＆ 指導上参考となる諸事項 ＆ '.$arg["ATTEND_TITLE"];
    $arg["btn_form2"] = knjCreateBtn($objForm, "btn_form2", $title, $extra);
    //成績参照ボタンを作成する
    $extra = "onclick=\"return btn_submit('formSeiseki_first');\" style=\"width:70px\"";
    $arg["btn_formSeiseki"] = knjCreateBtn($objForm, "btn_formSeiseki", "成績参照", $extra);
    //指導要録参照画面ボタンを作成する
    if ($model->Properties["sidouyourokuSansyou"] == 1) {
        $extra = "onclick=\"return btn_submit('formYorokuSanshou2_first');\"";
    } else {
        $extra = "onclick=\"return btn_submit('formYorokuSanshou_first');\"";
    }
    $arg["btn_formYorokuSanshou"] = knjCreateBtn($objForm, "btn_formYorokuSanshou", "指導要録参照", $extra);
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
    $securityCnt = $db->getOne(knje011gQuery::getSecurityHigh());
    $csvSetName = "CSV";
    if ($model->Properties["useXLS"]) {
        $csvSetName = "エクセル";
    }
    if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
        //データCSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX190/knjx190index.php?program_id=".PROGRAMID."&mode={$model->mode}&SEND_PRGID=KNJE011G&SEND_AUTH={$model->auth}&tyousasyo2020=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_check1"] = knjCreateBtn($objForm, "btn_check1", "データ".$csvSetName, $extra);
        //ヘッダデータCSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX191/knjx191index.php?program_id=".PROGRAMID."&mode={$model->mode}&SEND_PRGID=KNJE011G&SEND_AUTH={$model->auth}&tyousasyo2020=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_check2"] = knjCreateBtn($objForm, "btn_check2", "ヘッダデータ".$csvSetName, $extra);
    }
    //プレビュー／印刷
    if ($model->Properties["tyousasyoShokenPreview"] == '1') {
        $extra =  "onclick=\"return newwin('".SERVLET_URL."');\"";
        $arg["button"]["btn_print"] = KnjCreateBtn($objForm, "btn_print", "所見確認用", $extra);
    }

//    //備考一括更新
//    $link = REQUESTROOT."/X/KNJX_HEXAM_REMARK/knjx_hexam_remarkindex.php?program_id=".PROGRAMID."&mode={$model->mode}&SEND_PRGID=KNJE011G&SEND_AUTH={$model->auth}&EXP_YEAR={$model->annual["YEAR"]}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&GRADE={$model->grade}&HR_CLASS={$model->hrClass}";
//    $extra = "style=\"width:100px\" onclick=\"Page_jumper('{$link}');\"";
//    $arg["button"]["btn_remark_all"] = knjCreateBtn($objForm, "btn_remark_all", "備考一括更新", $extra);
}

function makeHidden(&$objForm, $db, $model)
{
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
    knjCreateHidden($objForm, "useTotalstudySlashFlg", $model->Properties["useTotalstudySlashFlg"]);
    knjCreateHidden($objForm, "useAttendrecRemarkSlashFlg", $model->Properties["useAttendrecRemarkSlashFlg"]);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

    //プレビュー／印刷用パラメータ
    if ($model->Properties["tyousasyoShokenPreview"] == '1') {
        knjCreateHidden($objForm, "PRGID", "KNJE011G");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRINT_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "PRINT_GAKKI", CTRL_SEMESTER);
        knjCreateHidden($objForm, "GRADE_HR_CLASS");
        knjCreateHidden($objForm, "KANJI", "1");
        knjCreateHidden($objForm, "OS", "1");
        knjCreateHidden($objForm, "OUTPUT", "1");
        knjCreateHidden($objForm, "3_or_6_nenYoForm", $model->Properties["3_or_6_nenYoForm"]);
        //何年用のフォームを使うのかの初期値を判断する
        $query = knje011gQuery::getSchoolDiv($model);
        $schooldiv = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $nenyoformSyokiti = $schooldiv["NEN"] == '0' ? ($schooldiv["SCHOOLDIV"] == '0' ? '3' : '4') : $schooldiv["NEN"];
        knjCreateHidden($objForm, "NENYOFORM", $nenyoformSyokiti);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "tyousasyo2020", "1");
        knjCreateHidden($objForm, "tyousasyo2020shojikouExtends", $model->Properties["tyousasyo2020GshojikouExtends"]);

        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "useCertifSchPrintCnt", $model->Properties["useCertifSchPrintCnt"]);
        knjCreateHidden($objForm, "tyousasyoAttendrecRemarkFieldSize", $model->Properties["tyousasyoAttendrecRemarkFieldSize"]);
        knjCreateHidden($objForm, "gaihyouGakkaBetu", $model->Properties["gaihyouGakkaBetu"]);
        knjCreateHidden($objForm, "nenYoForm", $model->Properties["nenYoForm"]);
        knjCreateHidden($objForm, "train_ref_1_2_3_field_size", $model->Properties["train_ref_1_2_3_field_size"]);
        knjCreateHidden($objForm, "train_ref_1_2_3_gyo_size", $model->Properties["train_ref_1_2_3_gyo_size"]);
        knjCreateHidden($objForm, "tyousasyoSougouHyoukaNentani", $model->Properties["tyousasyoSougouHyoukaNentani"]);
        knjCreateHidden($objForm, "tyousasyoTotalstudyactFieldSize", $model->Properties["tyousasyoTotalstudyactFieldSize"]);
        knjCreateHidden($objForm, "tyousasyoTotalstudyvalFieldSize", $model->Properties["tyousasyoTotalstudyvalFieldSize"]);
        knjCreateHidden($objForm, "tyousasyoSpecialactrecFieldSize", $model->Properties["tyousasyoSpecialactrecFieldSize"]);
        knjCreateHidden($objForm, "tyousasyoEMPTokuBetuFieldSize", $model->Properties["tyousasyoEMPTokuBetuFieldSize"]);
        knjCreateHidden($objForm, "tyousasyoKinsokuForm", $model->Properties["tyousasyoKinsokuForm"]);
        knjCreateHidden($objForm, "tyousasyoNotPrintAnotherStudyrec", $model->Properties["tyousasyoNotPrintAnotherStudyrec"]);
        knjCreateHidden($objForm, "tyousasyoNotPrintEnterGrade", $model->Properties["tyousasyoNotPrintEnterGrade"]);
        knjCreateHidden($objForm, "tyousasyoSyusyokuPrintGappeiTougou", $model->Properties["tyousasyoSyusyokuPrintGappeiTougou"]);
        knjCreateHidden($objForm, "tyousasyoHankiNintei", $model->Properties["tyousasyoHankiNintei"]);
        knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
        knjCreateHidden($objForm, "useAddrField2", $model->Properties["useAddrField2"]);
        knjCreateHidden($objForm, "useProvFlg", $model->Properties["useProvFlg"]);
        knjCreateHidden($objForm, "useGakkaSchoolDiv", $model->Properties["useGakkaSchoolDiv"]);
        knjCreateHidden($objForm, "useAssessCourseMst", $model->Properties["useAssessCourseMst"]);
        knjCreateHidden($objForm, "tyousasyoRemarkFieldSize", $model->Properties["tyousasyoRemarkFieldSize"]);
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
}

function makeTotalstudy(&$arg, &$objForm, $model, $field, $row)
{
        $height = $model->mojigyou[$field]["gyou"] * 13.5 + ($model->mojigyou[$field]["gyou"] -1 ) * 3 + 5;
        //京都仕様の場合、総合的な学習の時間の記録の評価は入力不可とする
//        if ($model->getSchoolName === 'kyoto') {
//            if ($model->Properties["kyotoSougouNyuryokuOk"] == "1") {
//                $extra = "style=\"height:{$height}px;\"";
//            } else {
//                $extra = "style=\"height:{$height}px;background:darkgray\" readOnly";
//            }
//        } else {
            $extra = "style=\"height:{$height}px;\"";
//        }
        $arg[$field] = KnjCreateTextArea($objForm, $field, $model->mojigyou[$field]["gyou"], ($model->mojigyou[$field]["moji"] * 2 + 1), "soft", $extra, $row[$field]);
        $arg[$field."_TYUI"] = "(全角{$model->mojigyou[$field]["moji"]}文字{$model->mojigyou[$field]["gyou"]}行まで)";

        //斜線を入れるチェックボックス
        $extra  = ($row[$field."_SLASH_FLG"] == "1") ? "checked" : "";
        $extra .= " id=\"".$field."_SLASH_FLG\"";
        $arg[$field."_SLASH_FLG"] = knjCreateCheckBox($objForm, $field."_SLASH_FLG", "1", $extra, "");
}

function torikomiRowTotalstudy($db, $model, &$row)
{
    if (intval($model->gradecd) < "3") {
        //1,2年次　指導要録取込
        $query = knje011gQuery::getYouroku($model);
        $resultYouroku = $db->query($query);
        $arrAct = array();
        $arrVal = array();
        while ($rowYouroku = $resultYouroku->fetchRow(DB_FETCHMODE_ASSOC)) {
            $head = "";
            if (($model->getSchoolName === 'kyoto' || $model->Properties["tyousashoShokenNyuryokuTorikomiTotalstudyHeader"] == "1") && $rowYouroku["GRADE_NAME1"]) {
                $head = $rowYouroku["GRADE_NAME1"]." ";
            }
            $arrAct[] = $head.$rowYouroku["TOTALSTUDYACT"];
            $arrVal[] = $head.$rowYouroku["TOTALSTUDYVAL"];
        }
        $row["TOTALSTUDYACT"] = implode("\r\n", $arrAct);
        $row["TOTALSTUDYVAL"] = implode("\r\n", $arrVal);
    } else {
        //3年次　通知票取込
        $totalstudytimeArray = array();
        $totalstudyactArray  = array();
        $query = knje011gQuery::get_record_totalstudytime_dat($model, $model->exp_year);
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
            $getTotalStudytime = implode("\n", $totalstudytimeArray);
            $row["TOTALSTUDYVAL"] = $row["TOTALSTUDYVAL"]."\n".$getTotalStudytime;
        }
        if (get_count($totalstudyactArray) > 0) {
            //入力値の後に改行して取り込む
            $getTotalStudyact = implode("\n", $totalstudyactArray);
            $row["TOTALSTUDYACT"] = $row["TOTALSTUDYACT"]."\n".$getTotalStudyact;
        }
    }
}
