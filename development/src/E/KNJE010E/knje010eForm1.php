<?php

require_once("for_php7.php");

//ビュー作成用クラス
class knje010eForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje010eindex.php", "", "edit");
        $db = Query::dbCheckOut();

        $model->schoolKind = $db->getOne(knje010eQuery::getSchoolKind($model));

        if (!isset($model->warning) && !in_array($model->cmd, array("yorokuYoriYomikomi_ok", "yorokuYoriYomikomi_cancel", "reload3")) && !preg_match('/(torikomi|torikomiT)[0-2]/', $model->cmd)) {
            $query = knje010eQuery::selectQuery($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $query = knje010eQuery::selectHexamEntremarkRemarkHdatQuery($model);
            $row["REMARK"] = $db->getOne($query);
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
        $query = knje010eQuery::getGradeCd($model);
        $getGdat = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->gradecd = "";
        $model->gradecd = $getGdat["GRADE_CD"];
        $model->schoolKind = "";
        $model->schoolKind = $getGdat["SCHOOL_KIND"];

        //京都仕様の場合、総合的な学習の時間の記録の評価は入力不可とする
        $model->getSchoolName = "";
        $model->getSchoolName = $db->getOne(knje010eQuery::getNameMst("Z010"));

        //1,2年次　指導要録、3年次　通知票取込ボタンが押された時の通知書より読込む　(かつ通年のとき)
        if ($model->cmd == 'reload3') {
            if ($model->Properties["tyousasyoSougouHyoukaNentani"] != 1 || "1" == $model->Properties["tyousasyo2020"]) {
                reload3setTotalStudy($row, $db, $model);
            }
        }

        /******************/
        /* コンボボックス */
        /******************/
        //教務主任等マスタチェックと海城学園(非表示にする)のチェック
        $getIppanCount = $db->getOne(knje010eQuery::getPositionCheck($model));
        if ($getIppanCount == 0 && $model->getSchoolName !== 'kaijyo') {
            $disabled = " disabled ";
            $model->allYear = '';
            $opt = array();
            $opt[] = array("label" => "全て",
                           "value" => "0000");
            $query = knje010eQuery::selectQueryAnnual($model);
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
                if ($model->allYear == '') {
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
        makeNendogoto($objForm, $arg, $db, $model);

        //総合的な学習の時間
        makeSogotekinaGakushunoJikan($objForm, $arg, $db, $model, $row);

        //備考
        makeBiko($objForm, $arg, $db, $model, $row);

        /********************/
        /* チェックボックス */
        /********************/
        //学習成績概評チェックボックス
        $extra = $row["COMMENTEX_A_CD"] == "1" ? "checked" : "";
        $extra .= " id=\"COMMENTEX_A_CD\" ";
        $arg["COMMENTEX_A_CD"] = knjCreateCheckBox($objForm, "COMMENTEX_A_CD", "1", $extra, "");


        //特記事項なし
        knjCreateHidden($objForm, "NO_COMMENTS_LABEL", $model->no_comments_label);
        $arg["NO_COMMENTS_LABEL"] = $model->no_comments_label;

        /**********/
        /* ボタン */
        /**********/
        makeButton($objForm, $arg, $db, $model);

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
        View::toHTML($model, "knje010eForm1.html", $arg);
    }
}
function createTextArea(&$objForm, &$arg, $model, $name, $extra, $val)
{

    $moji = $model->mojigyou[$name]["moji"];
    $gyou = $model->mojigyou[$name]["gyou"];
    $height = $gyou * 13.5 + ($gyou -1 ) * 3 + 5;
    $extra .= " id=\"".$name."\" style=\"height:{$height}px;\" ";
    $arg[$name] = KnjCreateTextArea($objForm, $name, $gyou, ($moji * 2 + 1), "soft", $extra, $val);
    $arg[$name."_TYUI"] = "(全角{$moji}文字X{$gyou}行まで)";

//    //斜線を入れるチェックボックス
//    $extra  = ($row[$name."_SLASH_FLG"] == "1") ? "checked" : "";
//    $extra .= " id=\"".$name."_SLASH_FLG\"";
//    $arg[$name."_SLASH_FLG"] = knjCreateCheckBox($objForm, $name."_SLASH_FLG", "1", $extra, "");
}

function makeButton(&$objForm, &$arg, $db, &$model)
{
    //教務主任等マスタチェックと海城学園(非表示にする)のチェック
    if ($getIppanCount == 0 && $model->getSchoolName !== 'kaijyo') {
        //生徒指導要録より読込ボタンを作成する
        $extra = $disabled;
        $extra .= " class=\"btn_torikomi\" ";
        if ($model->schoolName == 'tokiwa') {
            $extra .= " onclick=\" return btn_submit('yorokuYoriYomikomi_ok');\" ";
        } else {
            $extra .= " onclick=\" return btn_submit('yorokuYoriYomikomi');\" ";
        }
        $arg["btn_yorokuYoriYomikomi"] = KnjCreateBtn($objForm, "btn_yorokuYoriYomikomi", "生徒指導要録より読込", $extra);
    }
    //成績参照ボタンを作成する
    $extra = "onclick=\"return btn_submit('formSeiseki_first');\" style=\"width:70px\"";
    $arg["btn_formSeiseki"] = knjCreateBtn($objForm, "btn_formSeiseki", "成績参照", $extra);
    //1,2年次　指導要録取込、3年次　通知票取込み(総合的な学習の時間　通年用)
    if ($model->Properties["tyousasyoSougouHyoukaNentani"] != "1" || "1" == $model->Properties["tyousasyo2020"]) {
        if (intval($model->gradecd) > "2" && $model->Properties["tutihyoYoriYomikomiHyoujiFlg"] == 1) {
            $extra = "class=\"btn_torikomi\" onclick=\"return btn_submit('reload3');\" ";
            $arg["btn_reload3"] = knjCreateBtn($objForm, "btn_reload3", "通知票取込", $extra);
        } elseif (intval($model->gradecd) < "3" && $model->Properties["tyousasyo_SidoYorokYoriYomikomiHyoujiFlg"] == 1) {
            $extra = "class=\"btn_torikomi\" onclick=\"return btn_submit('reload3');\" ";
            $arg["btn_reload3"] = knjCreateBtn($objForm, "btn_reload3", "指導要録取込", $extra);
        }
    }
    // 総合的な学習の時間の指導要録参照画面ボタンを作成する
    if ($model->Properties["sidouyourokuSansyou"] == 1) {
        $extra = "onclick=\"return btn_submit('formYorokuSanshou2_first');\" style=\"width:100px\"";
    } else {
        $extra = "onclick=\"return btn_submit('formYorokuSanshou_first');\" style=\"width:100px\"";
    }
    $arg["btn_yorokuSanshou"] = knjCreateBtn($objForm, "btn_yorokuSanshou", "指導要録参照", $extra);

    // 指導上参考となる諸事項 年組一括取込
    if ("1" == $model->Properties["tyousasho6bunkatsu_homeroomShojikouTorikomi"]) {
        $query = knje010eQuery::getHrShojikouTorikomiDataCheck($model);
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
    $securityCnt = $db->getOne(knje010eQuery::getSecurityHigh());
    $csvSetName = "CSV";
    if ($model->Properties["useXLS"]) {
        $csvSetName = "エクセル";
    }
    if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
        //データCSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX190/knjx190index.php?program_id=".PROGRAMID."&mode={$model->mode}&SEND_PRGID=KNJE010E&SEND_AUTH={$model->auth}&tyousasyo2020=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_check1"] = knjCreateBtn($objForm, "btn_check1", "データ".$csvSetName, $extra);
        //ヘッダデータCSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX191/knjx191index.php?program_id=".PROGRAMID."&mode={$model->mode}&SEND_PRGID=KNJE010E&SEND_AUTH={$model->auth}&tyousasyo2020=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_check2"] = knjCreateBtn($objForm, "btn_check2", "ヘッダデータ".$csvSetName, $extra);
    }
    //プレビュー／印刷
    if ($model->Properties["tyousasyoShokenPreview"] == '1') {
        $extra = "onclick=\"return newwin('".SERVLET_URL."');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "所見確認用", $extra);
    }
}

//出欠の記録備考
function makeAttendrecRemark(&$objForm, &$arg, $db, $model, $disabled, $gradeCnt, $grade, $year, $val, $slashFlg)
{
    //出欠の記録備考取込（対応する学年のみ）
    if ($model->cmd === "torikomi".$gradeCnt) {
        $set_remark = knje010eQuery::getSemesRemark($model, $db, $year);
        $val = $set_remark;
    } elseif ($model->cmd === "torikomiT".$gradeCnt) {
        $set_remark = knje010eQuery::getHreportremarkDetailDat($db, $model, $year);
        $val = $set_remark;
    //対応しない学年の時は画面の値をセット
    } elseif (!in_array($model->cmd, array("yorokuYoriYomikomi_ok", "yorokuYoriYomikomi_cancel", "reload3", "yomikomi", "edit", "reset", "updEdit"))) {
        $val = $model->field2[$year]["ATTENDREC_REMARK"];
    }

    //出欠の記録備考
    $extra = $disabled." onPaste=\"return showPaste(this, ".$gradeCnt.");\" ";
    $gyou = $model->mojigyou["ATTENDREC_REMARK"]["gyou"];
    $moji = $model->mojigyou["ATTENDREC_REMARK"]["moji"];
    $arg["ATTENDREC_REMARK".$grade] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK-".$year, ($gyou + 1), ($moji * 2 + 1), "soft", $extra, $val);
    $arg["ATTENDREC_REMARK_TYUI"] = "(全角{$moji}文字{$gyou}行まで)";

    //出欠の記録 特記事項なしチェックボックス
    if ($model->Properties["notUseAttendrecRemarkTokkiJikouNasi"] != "1") {
        $arg["useAttendrecRemarkTokkiJikouNasi"] = "1";
        $extra = $disabled." id=\"INS_COMMENTS".$grade."\" onclick=\"return insertComment(this, 'ATTENDREC_REMARK-".$year."', 'INS_COMMENTS_LABEL-".$year."');\"";
        $arg["INS_COMMENTS".$grade] = knjCreateCheckBox($objForm, "INS_COMMENTS-".$year, "1", $extra, "");
        //特記事項なし
        $ins_comments_label = '特記事項なし';
        $arg["INS_COMMENTS_LABEL".$grade] = $ins_comments_label;
        knjCreateHidden($objForm, "INS_COMMENTS_LABEL-".$year, $ins_comments_label);
    }

    //斜線を入れるチェックボックス
    if ($model->Properties["useAttendrecRemarkSlashFlg"] == "1") {
        $arg["useAttendrecRemarkSlashFlg"] = 1;
        $extra  = $disabled.($slashFlg == "1" ? " checked" : "");
        $extra .= " id=\"ATTENDREC_REMARK_SLASH_FLG".$grade."\"";
        $arg["ATTENDREC_REMARK_SLASH_FLG".$grade] = knjCreateCheckBox($objForm, "ATTENDREC_REMARK_SLASH_FLG-".$year, "1", $extra, "");
    }

    //出欠備考参照ボタン
    $sdate = ($year) ? $year.'-04-01' : "";
    $edate = ($year) ? ($year+1).'-03-31' : "";
    //和暦表示フラグ
    $warekiFlg = "";
    if ($model->Properties["useWarekiHyoji"] == 1) {
        $warekiFlg = "1";
    }
    if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
        if ($model->Properties["useTorikomiAttendSemesRemarkDat"] == 1) {
            //まとめ出欠備考を取込みへ変更する
            $setname = $model->attendTitle.'備考全月取込';
            $setcmd = "torikomi".$gradeCnt;
            $extra = $disabled ." class=\"btn_torikomi\" onclick=\"return btn_submit('".$setcmd."');\"";
        } else {
            $setname = $model->attendTitle.'備考全月参照';
            $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
        }
        $arg["SANSYO".$grade] = KnjCreateBtn($objForm, "SANSYO".$grade, $setname, $extra);
    } elseif ($model->getSchoolName == "mieken") {
        $setcmd = "torikomiT".$gradeCnt;
        $extra = $disabled ." class=\"btn_torikomi\" onclick=\"return btn_submit('".$setcmd."');\"";
        $arg["SANSYO".$grade] = KnjCreateBtn($objForm, "SANSYO".$grade, "通知票取込", $extra);
    } else {
        $extra = $disabled."onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
        $arg["SANSYO".$grade] = KnjCreateBtn($objForm, "SANSYO".$grade, "日々".$model->attendTitle."備考参照", $extra);
    }
    //要録の出欠備考参照ボタン
    $extra = $disabled."onclick=\"loadwindow('../../X/KNJXATTEND_HTRAINREMARK/index.php?YEAR={$year}&SCHREGNO={$model->schregno}',0,document.documentElement.scrollTop || document.body.scrollTop,360,180);return;\" style=\"width:210px;\"";
    $arg["YOROKU_SANSYO".$grade] = KnjCreateBtn($objForm, "YOROKU_SANSYO".$grade, "要録の".$model->attendTitle."の記録備考参照", $extra);
    //年間出欠備考選択ボタン
    if ($model->Properties["useReasonCollectionBtn"] == 1) {
        $arg["btn_reason_collection_select".$grade] = makeSelectBtn($objForm, $model, "reason_collection", "btn_reason_collection_select".$grade, "年間".$model->attendTitle."備考選択", "ATTENDREC_REMARK-".$year, $year, $disabled);
        $arg["REASON_COLLECTION_SELECT"] = 1;
    }
    //出欠の記録参照ボタン
    if ($model->Properties["tyousasyoSyukketsuKirokuBtn"] == 1) {
        $arg["btn_syukketsu_sansyo".$grade] = makeSelectBtn($objForm, $model, "syukketsukiroku", "btn_syukketsu_sansyo".$grade, $model->attendTitle."の記録参照", "ATTENDREC_REMARK-".$year, $year, $disabled);
        $arg["tyousasyoSyukketsuKirokuBtn"] = 1;
    }
}

//特別活動の記録
function makeSpecialactrec(&$objForm, &$arg, $db, $model, $disabled, $gradeCnt, $grade, $year, $val)
{
    $spa = "SPECIALACTREC".$year;

    //委員会選択ボタン
    $arg["btn_committee".$grade] = makeSelectBtn($objForm, $model, "committee", "btn_committee".$grade, "委員会選択", $spa, $year, $disabled);

    //賞選択ボタン
    if ($model->Properties["useHyosyoSansyoButton_H"]) {
        $arg["btn_hyosyo".$grade."_spe"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo".$grade, "賞選択", $spa, $year, $disabled);
    }

    //部活動選択ボタン
    if ($model->Properties["tyousashoShokenNyuryokuSpecialActNotUseClub"] != 1) {
        $arg["btn_club".$grade."_spe"] = makeSelectBtn($objForm, $model, "club", "btn_club".$grade, "部活動選択", $spa, $year, $disabled);
    }

    //検定選択ボタン
    $arg["btn_qualified".$grade."_spe"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified".$grade, "検定選択", $spa, $year, $disabled);

    //記録備考選択ボタン
    if ($model->Properties["club_kirokubikou"] == 1) {
        $arg["btn_club_kirokubikou".$grade."_spe"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou".$grade, "記録備考選択", $spa, $year, $disabled);
    }

    //特別活動の記録
    $extra = $disabled." class=\"specialactrec_\" onPaste=\"return showPaste(this, ".$gradeCnt.");\" ";
    $moji = $model->mojigyou["SPECIALACTREC"]["moji"];
    $gyou = $model->mojigyou["SPECIALACTREC"]["gyou"];
    $arg["SPECIALACTREC".$grade]    = KnjCreateTextArea($objForm, $spa, ($gyou + 1), ($moji * 2 + 1), "soft", $extra, $val);
    $arg["SPECIALACTREC_TYUI"] = "(全角{$moji}文字{$gyou}行まで)";

    if ($model->getSchoolName == "koma") {
        $arg["isKoma"] = "1";
        //マラソン大会
        $arg["btn_marathon".$grade] = makeSelectBtn($objForm, $model, "marathon", "btn_marathon".$grade, "マラソン大会選択", $spa, $year, $disabled);
        //臘八摂心皆勤
        $rouhatsuKaikin = "";
        $query = knje010eQuery::getRouhatsuKaikin($model, $year);
        $rouhatsuRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($rouhatsuRow["REC_CNT"] > 0 && $rouhatsuRow["REC_CNT"] == $rouhatsuRow["KAIKIN_CNT"]) {
            $rouhatsuKaikin = "臘八摂心皆勤";
        }
        knjCreateHidden($objForm, "ROUHATSU_KAIKIN".$grade, $rouhatsuKaikin);
        $extra = $disabled."";
        $tag = "document.forms[0].".$spa.".value";
        $kaikin = "document.forms[0].ROUHATSU_KAIKIN".$grade.".value";
        $extra = $disabled." onclick=\"".$tag." += ".$kaikin."\"";
        $arg["btn_rouhatsu".$grade] = knjCreateBtn($objForm, "btn_rouhatsu".$grade, "臘八摂心皆勤", $extra);
    }
}

//指導上参考となる諸事項
function makeShojikou(&$objForm, &$arg, $db, $model, $disabled, $gradeCnt, $grade, $year, $row)
{
    //賞選択ボタン 指導上参考となる諸事項（5）
    if ($model->Properties["useHyosyoSansyoButton_H"]) {
        $arg["btn_hyosyo".$grade."_tra5"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo".$grade, "賞選択", "TRAIN_REF5-".$year, $year, $disabled);
    }

    //部活動選択ボタン 指導上参考となる諸事項（3）
    $arg["btn_club".$grade."_tra3"] = makeSelectBtn($objForm, $model, "club", "btn_club".$grade, "部活動選択", "TRAIN_REF3-".$year, $year, $disabled);

    //記録備考選択ボタン 指導上参考となる諸事項（3）
    if ($model->Properties["club_kirokubikou"] == 1) {
        $arg["btn_club_kirokubikou".$grade."_tra3"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou".$grade, "記録備考選択", "TRAIN_REF3-".$year, $year, $disabled);
        $arg["btn_club_kirokubikou".$grade."_tra5"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou".$grade, "記録備考選択", "TRAIN_REF5-".$year, $year, $disabled);
    }

    //検定選択ボタン 指導上参考となる諸事項（4）
    $arg["btn_qualified".$grade."_tra4"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified".$grade, "検定選択", "TRAIN_REF4-".$year, $year, $disabled);

    //指導上参考となる諸事項 取込画面
    $arg["btn_formShojikouTori_".$grade] = makeSelectBtn($objForm, $model, "formShojikouTori_".$grade, "btn_formShojikouTori_".$grade, "指導上参考となる諸事項取込", "DUMMY", $year, $disabled);

    //指導上参考となる諸事項
    $gyouMax = 0;
    for ($i = 1; $i <= 6; $i++) {
        $moji = $model->mojigyou["TRAIN_REF".$i]["moji"];
        $gyou = $model->mojigyou["TRAIN_REF".$i]["gyou"];
        if ($gyouMax < $gyou) {
            $gyouMax = $gyou;
        }
    }
    $innerScroll = "";
    if ($gyouMax < 15) {
        $arg["SHOJIKOU_HEIGHT"] = " height: ".($gyouMax + 1)."em; overflow-y: hidden; ";
        $innerScroll = "scroll";
    } else {
        $arg["SHOJIKOU_HEIGHT"] = " height: 15em; overflow-y: scroll; ";
        $innerScroll = "hidden";
    }
    for ($i = 1; $i <= 6; $i++) {
        $moji = $model->mojigyou["TRAIN_REF".$i]["moji"];
        $gyou = $model->mojigyou["TRAIN_REF".$i]["gyou"];
        $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;
        $id = "TRAIN_REF".$i."-".$year;
        $extra1 = $disabled." id=\"".$id."\" style=\"height:{$height}px; overflow-y: {$innerScroll}; \" onPaste=\"return showPaste(this, ".$gradeCnt.");\" ";
        $arg["TRAIN_REF".$grade."_".$i] = KnjCreateTextArea($objForm, $id, ($gyou + 1), ($moji * 2 + 1), "soft", $extra1, $row["TRAIN_REF".$i]);
        $arg["TRAIN_REF".$i."_COMMENT"] = "(全角{$moji}文字{$gyou}行まで)";

        //特記事項なしチェックボックス
        $id2 = "TRAIN_REF".$i."-".$year."_NO_COMM";
        $extra  = ($model->field[$id2] == "1") ? "checked" : "";
        $extra .= $disabled." id=\"".$id2."\" onclick=\"return CheckRemark('".$id."', '".$id2."');\"";
        $arg["TRAIN_REF".$grade."_".$i."_NO_COMM"] = knjCreateCheckBox($objForm, $id2, "1", $extra, "");
        $arg["ID_TRAIN_REF".$grade."_".$i."_NO_COMM"] = $id2;
    }
}


function makeNendogoto(&$objForm, &$arg, $db, &$model)
{
    $model->schArray = array();
    $disabled = "disabled";
    $query = knje010eQuery::selectQueryAnnual($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $model->schArray[$row["GRADE"]] = array("YEAR"  => $row["YEAR"],
                                                "ANNUAL" => $row["ANNUAL"]);
    }
    $result->free();

    $query = knje010eQuery::getGdat($model);
    $result = $db->query($query);
    $opt_grades = array();
    while ($gRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_grades[] = array("GRADE" => $gRow["GRADE"], "GRADE_CD" => $gRow["GRADE_CD"]);
    }
    if (empty($opt_grades)) {
        $opt_grades[] = array("GRADE" => "01", "GRADE_CD" => "01");
        $opt_grades[] = array("GRADE" => "02", "GRADE_CD" => "02");
        $opt_grades[] = array("GRADE" => "03", "GRADE_CD" => "03");
    }

    $result->free();

    foreach ($model->itemname as $key => $name) {
        $arg[$key."NAME"] = $name;
    }

    $gradeCnt = 0;
    $hiddenYear = array();
    foreach ($opt_grades as $r) {
        $grade = (int) $r["GRADE_CD"];
        $disabled = is_array($model->schArray[$r["GRADE"]]) ? "" : " disabled ";
        $year = $model->schArray[$r["GRADE"]]["YEAR"];

        //表示用の年度をセット
        if ($year != "" && $grade != "") {
            $arg["YEAR".$grade] = '('.$year.'年度)';
        }

        if ($year) {
            $hiddenYear[] = $year;
        }
        $isReadData = false;
        if (!isset($model->warning)) {
            if (($model->cmd == "yorokuYoriYomikomi_ok" || $model->cmd == "yorokuYoriYomikomi_cancel") && ($model->readYear == "0000" || $model->readYear == $year)) {
                $query = knje010eQuery::selectQuery_Htrainremark_Dat($model, $year);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($model->cmd == "yorokuYoriYomikomi_cancel") {
                    if ($row) {
                        foreach ($row as $key => $val) {
                            $row[$key] = $model->field2[$year][$key]."\n".$val;
                        }
                    } else {
                        $row = $model->field2[$year];
                    }
                }
                for ($i = 1; $i <= 6; $i++) {
                    $row["TRAIN_REF".$i] = "";
                    $seq = "10".$i;
                    $query = knje010eQuery::getHexamEntremarkTrainrefDat($model, $year, $seq);
                    $r = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if (is_array($r)) {
                        $row["TRAIN_REF".$i] = $r["REMARK"];
                    }
                }
                $isReadData = true;
            } else {
                if (!in_array($model->cmd, array('torikomi0', 'torikomi1', 'torikomi2', 'torikomiT0', 'torikomiT1', 'torikomiT2', "reload3", "yorokuYoriYomikomi_ok", "yorokuYoriYomikomi_cancel"))) {
                    $query = knje010eQuery::selectQueryForm2($model, $year);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    for ($i = 1; $i <= 6; $i++) {
                        $row["TRAIN_REF".$i] = "";
                        $seq = "10".$i;
                        $query = knje010eQuery::getHexamEntremarkTrainrefDat($model, $year, $seq);
                        $r = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        if (is_array($r)) {
                            $row["TRAIN_REF".$i] = $r["REMARK"];
                        }
                    }
                } else {
                    $row = $model->field2[$year];
                }
            }

            //指導要録 総合所見 3分割取込み
            if (($model->cmd == "yorokuYoriYomikomi_ok" || $model->cmd == "yorokuYoriYomikomi_cancel") && $model->Properties["useSyojikou3"] == "1") {
                //文京のとき
                if ($model->schoolName === 'bunkyo') {
                    $model->field2[$year]["TRAIN_REF"] = $row["TRAIN_REF1"];
                    $model->field2[$year]["TRAIN_REF2"] = $row["TRAIN_REF2"];
                    $model->field2[$year]["TRAIN_REF3"] = $row["TRAIN_REF3"];
                } elseif ($model->Properties["useSyojikou3_torikomi"] != "1") {
                    $row["TRAIN_REF1"] = $model->field2[$year]["TRAIN_REF"];
                    $row["TRAIN_REF2"] = $model->field2[$year]["TRAIN_REF2"];
                    $row["TRAIN_REF3"] = $model->field2[$year]["TRAIN_REF3"];
                }
            }

            if (($model->cmd == "yorokuYoriYomikomi_ok" || $model->cmd == "yorokuYoriYomikomi_cancel") && !$isReadData) {
                $row["ATTENDREC_REMARK"] = $model->field2[$year]["ATTENDREC_REMARK"];
                $row["SPECIALACTREC"] = $model->field2[$year]["SPECIALACTREC"];
            }
        } else {
            $row = $model->field2[$year];
        }

        //出欠の記録備考
        makeAttendrecRemark($objForm, $arg, $db, $model, $disabled, $gradeCnt, $grade, $year, $row["ATTENDREC_REMARK"], $row["ATTENDREC_REMARK_SLASH_FLG"]);

        //特別活動の記録
        makeSpecialactrec($objForm, $arg, $db, $model, $disabled, $gradeCnt, $grade, $year, $row["SPECIALACTREC"]);

        //指導上参考となる諸事項
        makeShojikou($objForm, $arg, $db, $model, $disabled, $gradeCnt, $grade, $year, $row);

        $gradeCnt++;
    }
    knjCreateHidden($objForm, "hiddenYear", implode(",", $hiddenYear));

    //指導要録 総合所見参照
    if ("1" == $model->Properties["seitoSidoYorokuSogoShoken3Bunkatsu"] || $model->schoolName == 'tokiwa') {
        $arg["seitoSidoYorokuSogoShoken3Bunkatsu"] = "1";
        $fields = array("TRAIN_REF1", "TRAIN_REF2", "TRAIN_REF3");
        $yoroku_trainref = array();
        foreach ($fields as $field) {
            $yoroku_trainref["YOROKU_".$field] = array();
        }
        foreach ($opt_grades as $r) {
            $year = $model->schArray[$r["GRADE"]]["YEAR"];

            $query = knje010eQuery::yoroku_trainref123($model, $year);
            $sansyou = $db->getRow($query, DB_FETCHMODE_ASSOC);

            foreach ($fields as $field) {
                $data = $sansyou[$field];
                if ($data) {
                    $yoroku_trainref["YOROKU_".$field][] = $data;
                }
            }
        }
        $prop = $model->Properties["seitoSidoYoroku_train_ref_1_2_3_field_size"];
        if ($prop == "") {
            $prop = $model->Properties["train_ref_1_2_3_field_size"];
        }

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
        $yoroku_totalremark = array();
        foreach ($opt_grades as $r) {
            $year = $model->schArray[$r["GRADE"]]["YEAR"];

            $query = knje010eQuery::yoroku_sogoshoken($model, $year);
            $sansyou = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $data = $sansyou["TOTALREMARK"];

            if ($data) {
                $yoroku_totalremark[] = $data;
            }
        }

        if ($model->Properties["seitoSidoYoroku_dat_TotalremarkSize"]) {
            list($moji, $gyou) = preg_split('/\*/', $model->Properties["seitoSidoYoroku_dat_TotalremarkSize"]);
            $model->mojigyou["totalremark"]["moji"] = (int)trim($moji);
            $model->mojigyou["totalremark"]["gyou"] = (int)trim($gyou);
        } else {
            $model->mojigyou["totalremark"]["moji"] = 44; //デフォルトの値
            $model->mojigyou["totalremark"]["gyou"] = 6;  //デフォルトの値
        }
        $height = $model->mojigyou["totalremark"]["gyou"] * 15;
        $extra = "style=\"background-color: #D0D0D0; height:{$height}px;\"";
        $arg["TOTALREMARK"] = KnjCreateTextArea($objForm, "TOTALREMARK", 5, ($model->mojigyou["totalremark"]["moji"] * 2), "soft", $extra, implode("\n-------------------------\n", $yoroku_totalremark));
    }

    $arg["COLSPAN_CHANGE"] = "colspan=\"4\"";
}

function makeSogotekinaGakushunoJikan(&$objForm, &$arg, $db, &$model, &$row)
{

//    if ($model->Properties["tyousasyoSougouHyoukaNentani"] != 1 || "1" == $model->Properties["tyousasyo2020"]) {
        //生徒指導要録より読込ボタン押下時に通年の総合的な学習の時間の内容・評価をセット
    if ($model->cmd == "yorokuYoriYomikomi_ok" || $model->cmd == "yorokuYoriYomikomi_cancel") {
        if ($model->Properties["sidouyourokuSansyou"] == 1) {
            $query = knje010eQuery::getYourokuDat($model);
        } else {
            $query = knje010eQuery::selectQuery_Htrainremark_Hdat($model);
        }
        $resultYouroku = $db->query($query);
        $kaigyou = "";
        if ($model->cmd == "yorokuYoriYomikomi_ok") {
            $row["TOTALSTUDYACT"] = "";
            $row["TOTALSTUDYVAL"] = "";
        } else {
            $row["TOTALSTUDYACT"] = $model->field["TOTALSTUDYACT"]."\n";
            $row["TOTALSTUDYVAL"] = $model->field["TOTALSTUDYVAL"]."\n";
        }
        while ($rowYouroku = $resultYouroku->fetchRow(DB_FETCHMODE_ASSOC)) {
            $head = "";
            if (($model->getSchoolName === 'kyoto' || $model->Properties["tyousashoShokenNyuryokuTorikomiTotalstudyHeader"] == "1") && $rowYouroku["GRADE_NAME1"]) {
                $head = $rowYouroku["GRADE_NAME1"]." ";
            }
            if ($model->Properties["sidouyourokuSansyou"] == 1) {
                $row["TOTALSTUDYACT"] .= $kaigyou.$head.$rowYouroku["TOTALSTUDYACT"];
                $row["TOTALSTUDYVAL"] .= $kaigyou.$head.$rowYouroku["TOTALSTUDYVAL"];
                $kaigyou = "\r\n";
            } elseif ($model->readYear === '0000') {
                $row["TOTALSTUDYACT"] .= $kaigyou.$head.$rowYouroku["TOTALSTUDYACT"];
                $row["TOTALSTUDYVAL"] .= $kaigyou.$head.$rowYouroku["TOTALSTUDYVAL"];
                $kaigyou = "\r\n";
            } else {
                $row["TOTALSTUDYACT"] .= $rowYouroku["TOTALSTUDYACT"];
                $row["TOTALSTUDYVAL"] .= $rowYouroku["TOTALSTUDYVAL"];
            }
        }
    }
        //活動内容
        createTextArea($objForm, $arg, $model, "TOTALSTUDYACT", " onPaste=\"return showKotei(this);\" ", $row["TOTALSTUDYACT"]);

        //評価
        createTextArea($objForm, $arg, $model, "TOTALSTUDYVAL", " onPaste=\"return showKotei(this);\" ", $row["TOTALSTUDYVAL"]);

        //総合的な学習の時間の「斜線を入れる」チェックボックス表示
    if ($model->Properties["useTotalstudySlashFlg"] == 1) {
        $arg["useTotalstudySlashFlg"] = 1;

        $extra  = ($row["TOTALSTUDYACT_SLASH_FLG"] == "1") ? "checked" : "";
        $extra .= " id=\"TOTALSTUDYACT_SLASH_FLG\"";
        $arg["TOTALSTUDYACT_SLASH_FLG"] = knjCreateCheckBox($objForm, "TOTALSTUDYACT_SLASH_FLG", "1", $extra, "");

        $extra  = ($row["TOTALSTUDYVAL_SLASH_FLG"] == "1") ? "checked" : "";
        $extra .= " id=\"TOTALSTUDYVAL_SLASH_FLG\"";
        $arg["TOTALSTUDYVAL_SLASH_FLG"] = knjCreateCheckBox($objForm, "TOTALSTUDYVAL_SLASH_FLG", "1", $extra, "");
    }

        $arg["tyousasyoSougouHyoukaNotNentani"] = "1";
    if ("1" == $model->Properties["tyousasyo2020"]) {
        $arg["tyousasyoSougouHyoukaNentaniBiko"] = "1";
    }
//  } else {
//      $arg["TOTALSTUDYACT_TYUI"] = "(全角{$model->mojigyou["TOTALSTUDYACT"]["moji"]}文字{$model->mojigyou["TOTALSTUDYACT"]["gyou"]}行まで)";
//      $arg["TOTALSTUDYVAL_TYUI"] = "(全角{$model->mojigyou["TOTALSTUDYVAL"]["moji"]}文字{$model->mojigyou["TOTALSTUDYVAL"]["gyou"]}行まで)";
//      $arg["tyousasyoSougouHyoukaNentani"] = "1";
//      $arg["tyousasyoSougouHyoukaNentaniBiko"] = "1";
//  }
}

function makeBiko(&$objForm, &$arg, $db, &$model, &$row)
{
    if (in_array($model->cmd, array('torikomi0', 'torikomi1', 'torikomi2', 'torikomiT0', 'torikomiT1', 'torikomiT2'))) {
        $row["REMARK"] = $model->field["REMARK"];
    }
    createTextArea($objForm, $arg, $model, "REMARK", " onPaste=\"return showKotei(this);\" ", $row["REMARK"]);

    //
    knjCreateHidden($objForm, "tyousasyoRemarkFieldSize", $model->Properties["tyousasyoRemarkFieldSize"]);
    if ("1" != $model->Properties["unuse_KNJE015_HEXAM_ENTREMARK_LEARNING_DAT_REMARK"]) {
        $extra = " onclick=\"loadwindow('".REQUESTROOT."/E/KNJE010E/knje010eindex.php?&cmd=formBikoTori&TARGET=REMARK',0,document.documentElement.scrollTop || document.body.scrollTop,1000,650);\"";
        $arg["btn_formBikoTori"] = knjCreateBtn($objForm, "btn_formBikoTori", "備考取込", $extra);
    }

    //特記事項なしチェックボックス
    $extra  = ($model->field["REMARK_NO_COMMENTS"] == "1") ? "checked" : "";
    $extra .= " id=\"REMARK_NO_COMMENTS\" onclick=\"return CheckRemark('REMARK', 'REMARK_NO_COMMENTS');\"";
    $arg["REMARK_NO_COMMENTS"] = knjCreateCheckBox($objForm, "REMARK_NO_COMMENTS", "1", $extra, "");
}

function reload3setTotalStudy(&$row, $db, $model)
{
    //1,2年次　指導要録取込
    if (intval($model->gradecd) < "3") {
        $valArray = array();
        $actArray  = array();
        $query = knje010eQuery::getYourokuDat($model);
        $result = $db->query($query);
        while ($total_row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $head = "";
            if (($model->getSchoolName === 'kyoto' || $model->Properties["tyousashoShokenNyuryokuTorikomiTotalstudyHeader"] == "1") && $total_row["GRADE_NAME1"]) {
                $head = $total_row["GRADE_NAME1"]." ";
            }
            if ($total_row["TOTALSTUDYVAL"] != '') {
                $valArray[] = $head.$total_row["TOTALSTUDYVAL"];
            }
            if ($total_row["TOTALSTUDYACT"] != '') {
                $actArray[] = $head.$total_row["TOTALSTUDYACT"];
            }
        }
        $result->free();
        if (get_count($valArray) > 0) {
            $row["TOTALSTUDYVAL"] = implode("\n", $valArray);
        }
        if (get_count($actArray) > 0) {
            $row["TOTALSTUDYACT"] = implode("\n", $actArray);
        }
    //3年次　通知票取込
    } else {
        $totalstudytimeArray = array();
        $totalstudyactArray  = array();
        $query = knje010eQuery::get_record_totalstudytime_dat($model);
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

//選択ボタン
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $year, $disabled = "")
{
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        $tgt = "";
        $extra = "";
        if ($div == "club") {                   //部活動
            $tgt = "/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php";
            $sizeW = 800;
            $sizeH = 350;
        } elseif ($div == "committee") {       //委員会
            $tgt = "/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php";
            $sizeW = 700;
            $sizeH = 350;
        } elseif ($div == "qualified") {       //検定
            $tgt = "/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php";
            $sizeW = ($model->Properties["useQualifiedMst"] == "1") ? 800 : 670;
            $sizeH = 500;
        } elseif ($div == "hyosyo") {          //賞
            $tgt = "/X/KNJX_HYOSYO_SELECT/knjx_hyosyo_selectindex.php";
            $sizeW = 600;
            $sizeH = 350;
        } elseif ($div == "kirokubikou") {     //記録備考
            $tgt = "/X/KNJX_CLUB_KIROKUBIKOU_SELECT/knjx_club_kirokubikou_selectindex.php";
            $sizeW = 800;
            $sizeH = 350;
        } elseif ($div == "reason_collection") {   //年間出欠備考
            $tgt = "/X/KNJX_REASON_COLLECTION_SELECT/knjx_reason_collection_selectindex.php";
            $sizeW = 800;
            $sizeH = 350;
        } elseif ($div == "syukketsukiroku") {   //出欠の記録参照
            $tgt = "/X/KNJX_SYUKKETSUKIROKU/knjx_syukketsukirokuindex.php";
            $sizeW = 800;
            $sizeH = 350;
        } elseif (preg_match("/^formShojikouTori/", $div)) {   //指導上参考となる諸事項選択
            $tgt = "/E/KNJE010E/knje010eindex.php";
            $sizeW = 900;
            $sizeH = 650;
            $onclick = " onclick=\"loadwindow('".REQUESTROOT.$tgt."?cmd={$div}&TARGET={$target}&TARGET_YEAR={$year}',0,document.documentElement.scrollTop || document.body.scrollTop,900,650);\"";
            $extra = $disabled.$onclick;
        } elseif ($div == "marathon") {   //マラソン大会選択
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_MARATHON_SELECT/knjx_marathon_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        }
        if (!$extra) {
            $onclick = " onclick=\"loadwindow('".REQUESTROOT.$tgt."?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$sizeW},{$sizeH});\"";
            $extra = $disabled.$onclick;
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}

function makeHidden(&$objForm, $db, &$model)
{
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

    //プレビュー／印刷用パラメータ
    if ($model->Properties["tyousasyoShokenPreview"] == '1') {
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "PRGID", "KNJE010E");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRINT_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "PRINT_GAKKI", CTRL_SEMESTER);
        knjCreateHidden($objForm, "GRADE_HR_CLASS");
        knjCreateHidden($objForm, "KANJI", "1");
        knjCreateHidden($objForm, "OS", "1");
        knjCreateHidden($objForm, "OUTPUT", "1");
        knjCreateHidden($objForm, "3_or_6_nenYoForm", $model->Properties["3_or_6_nenYoForm"]);
        //何年用のフォームを使うのかの初期値を判断する
        $query = knje010eQuery::getSchoolDiv($model);
        $schooldiv = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $nenyoformSyokiti = $schooldiv["NEN"] == '0' ? ($schooldiv["SCHOOLDIV"] == '0' ? '3' : '4') : $schooldiv["NEN"];
        knjCreateHidden($objForm, "NENYOFORM", $nenyoformSyokiti);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "tyousasyo2020", "1");
        knjCreateHidden($objForm, "tyousasyo2020shojikouExtends", $model->Properties["tyousasyo2020EshojikouExtends"]);

        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "useCertifSchPrintCnt", $model->Properties["useCertifSchPrintCnt"]);
        knjCreateHidden($objForm, "tyousasyoAttendrecRemarkFieldSize", $model->Properties["tyousasyoAttendrecRemarkFieldSize"]);
        knjCreateHidden($objForm, "nenYoForm", $model->Properties["nenYoForm"]);
        knjCreateHidden($objForm, "train_ref_1_2_3_field_size", $model->Properties["train_ref_1_2_3_field_size"]);
        knjCreateHidden($objForm, "train_ref_1_2_3_gyo_size", $model->Properties["train_ref_1_2_3_gyo_size"]);
        knjCreateHidden($objForm, "tyousasyoTotalstudyactFieldSize", $model->Properties["tyousasyoTotalstudyactFieldSize"]);
        knjCreateHidden($objForm, "tyousasyoTotalstudyvalFieldSize", $model->Properties["tyousasyoTotalstudyvalFieldSize"]);
        knjCreateHidden($objForm, "tyousasyoSpecialactrecFieldSize", $model->Properties["tyousasyoSpecialactrecFieldSize"]);
        knjCreateHidden($objForm, "tyousasyoTokuBetuFieldSize", $model->Properties["tyousasyoTokuBetuFieldSize"]);
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
