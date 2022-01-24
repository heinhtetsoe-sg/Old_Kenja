<?php

require_once('for_php7.php');
class knjh400_TyousasyoSyuusyokuForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh400_TyousasyoSyuusyokuindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $name = $db->getOne(knjh400_TyousasyoSyuusyokuQuery::getName($model));
        $attendno = $db->getOne(knjh400_TyousasyoSyuusyokuQuery::getAttendno($model));
        //名前表示
        $arg["name"] = "出席番号　".$attendno."　氏名　".$name;

        if ($model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
            //パターン
            makePattern($objForm, $arg, $db, $model);
        }

        //調査所見データ取得
        if ($model->warning == "") {
            if ($model->cmd === 'tsuchiTorikomi') {
                $row =& $model->field;
            } else {
                $query = knjh400_TyousasyoSyuusyokuQuery::getReportRemarkDat($model);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            }
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //就職用特別活動記録
        $extra = "";
        $arg["data"]["JOBHUNT_REC"] = KnjCreateTextArea($objForm, "jobhunt_rec", $model->gyou["JOBHUNT_REC"]["GYOU"], $model->gyou["JOBHUNT_REC"]["MOJI"] * 2 + 1, "soft", $extra, $row["JOBHUNT_REC"]);
        $arg["data"]["JOBHUNT_REC_TYUI"] = "(全角{$model->gyou["JOBHUNT_REC"]["MOJI"]}文字X{$model->gyou["JOBHUNT_REC"]["GYOU"]}行まで)";

        //就職用欠席理由
        if ($model->cmd === 'tsuchiTorikomi') {
            $set_remark = knjh400_TyousasyoSyuusyokuQuery::getHreportremarkDetailDat($db, $model);
            $row["JOBHUNT_ABSENCE"] = $set_remark;
        }
        $extra = "";
        $arg["data"]["JOBHUNT_ABSENCE"] = KnjCreateTextArea($objForm, "jobhunt_absence", $model->gyou["JOBHUNT_ABSENCE"]["GYOU"], $model->gyou["JOBHUNT_ABSENCE"]["MOJI"] * 2 + 1, "soft", $extra, $row["JOBHUNT_ABSENCE"]);
        $arg["data"]["JOBHUNT_ABSENCE_TYUI"] = "(全角{$model->gyou["JOBHUNT_ABSENCE"]["MOJI"]}文字X{$model->gyou["JOBHUNT_ABSENCE"]["GYOU"]}行まで)";

        //就職用身体状況備考
        $extra = "";
        $arg["data"]["JOBHUNT_HEALTHREMARK"] = KnjCreateTextArea($objForm, "jobhunt_healthremark", $model->gyou["JOBHUNT_HEALTHREMARK"]["GYOU"], $model->gyou["JOBHUNT_HEALTHREMARK"]["MOJI"] * 2 + 1, "soft", $extra, $row["JOBHUNT_HEALTHREMARK"]);

        //異常なしチェックボックス
        $extra = "id=\"CHECK\" onclick=\"return CheckHealthRemark();\"";
        $arg["data"]["CHECK"] = knjCreateCheckBox($objForm, "CHECK", "1", $extra, "");
        $arg["data"]["CHECK_LABEL"] =$model->remarkValue;

        //健康診断詳細データ取得
        $query = knjh400_TyousasyoSyuusyokuQuery::getMedexamDetDat($model);
        $medexam_row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //身長
        $arg["data"]["HEIGHT"] = $medexam_row["HEIGHT"]. " cm";
        //体重
        $arg["data"]["WEIGHT"] = $medexam_row["WEIGHT"]. " kg";
        //視力（右）
        if ($medexam_row["R_VISION_MARK"]) {
            $arg["data"]["R_VISION_MARK"] = $medexam_row["R_BAREVISION_MARK"]."　( ".$medexam_row["R_VISION_MARK"]." )";
        } else {
            $arg["data"]["R_VISION_MARK"] = $medexam_row["R_BAREVISION_MARK"]."　(　　)";
        }
        //視力（左）
        if ($medexam_row["L_VISION_MARK"]) {
            $arg["data"]["L_VISION_MARK"] = $medexam_row["L_BAREVISION_MARK"]."　( ".$medexam_row["L_VISION_MARK"]." )";
        } else {
            $arg["data"]["L_VISION_MARK"] = $medexam_row["L_BAREVISION_MARK"]."　(　　)";
        }
        //聴力（右）
        $arg["data"]["R_EAR_NAME"] = $medexam_row["R_EAR_NAME"];
        //聴力（左）
        $arg["data"]["L_EAR_NAME"] = $medexam_row["L_EAR_NAME"];
        //検査日
        if (preg_match("/([0-9]{4})-([0-9]{2})-([0-9]{2})/", $medexam_row["DATE"], $date_std)) {
            $arg["data"]["DATE"] = $date_std[1]."年".$date_std[2]."月".$date_std[3]."日";
        }

        //指導要録データ
        $sido_cnt = 0;
        $query = knjh400_TyousasyoSyuusyokuQuery::getHtrainremarkDat($model);
        $result = $db->query($query);
        while ($sido_row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sido_row["YEAR"] = $sido_row["YEAR"].'年度';
            $sido_row["TOTALREMARK"] = KnjCreateTextArea($objForm, "TOTALREMARK", 5, 83, "soft", "style=\"background-color:#D0D0D0;height:60px;\"", $sido_row["TOTALREMARK"]);
            $arg["sido_data"][] = $sido_row;
            $sido_cnt++;
        }
        if ($sido_cnt == "0") {
            $arg["sidoyoroku"] = 1;
            $arg["no_data"] = ($model->schregno) ? '　データなし' : '　';
        }

        //就職用推薦事由
        $extra = "";
        $arg["data"]["JOBHUNT_RECOMMEND"] = KnjCreateTextArea($objForm, "jobhunt_recommend", $model->gyou["JOBHUNT_RECOMMEND"]["GYOU"], $model->gyou["JOBHUNT_RECOMMEND"]["MOJI"] * 2 + 1, "soft", $extra, $row["JOBHUNT_RECOMMEND"]);
        $arg["data"]["JOBHUNT_RECOMMEND_TYUI"] = "(全角{$model->gyou["JOBHUNT_RECOMMEND"]["MOJI"]}文字X{$model->gyou["JOBHUNT_RECOMMEND"]["GYOU"]}行まで)";

        //ボタン作成
        makeBtn($objForm, $arg, $db, $model);

        //hidden作成
        makeHidden($objForm, $db, $model, $row);

        //DB切断
        Query::dbCheckIn($db);

        if (get_count($model->warning) == 0 && $model->cmd != "reset") {
            $arg["next"] = "NextStudent2(0);";
        } elseif ($model->cmd == "reset") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh400_TyousasyoSyuusyokuForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $db, $model)
{
    $disabled = ($model->schregno) ? "" : "disabled";

    //欠席日数参照ボタン
    $extra = $disabled." onclick=\"loadwindow('../../X/KNJXATTEND3/index.php?cmd=detail&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&PRGID=KNJE020',0,0,500,500);return;\"";
    $arg["ATTEND3_SANSYO"] = knjCreateBtn($objForm, "ATTEND3_SANSYO", "欠席日数参照", $extra);
    //出欠備考参照ボタン
    $sdate = $model->exp_year.'-04-01';
    $edate = ($model->exp_year+1).'-03-31';
    //和暦表示フラグ
    $warekiFlg = "";
    if ($model->Properties["useWarekiHyoji"] == 1) {
        $warekiFlg = "1";
    }
    if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
        $extra = $disabled." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
        $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "まとめ出欠備考参照", $extra);
    } elseif ($model->schoolName === 'mieken') {
        $extra = $disabled." style=\"color:#1E90FF;font:bold\" onclick=\"return btn_submit('tsuchiTorikomi');\"";
        $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "通知票取込", $extra);
    } else {
        $extra = $disabled." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
        $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "日々出欠備考参照", $extra);
    }
    //要録の出欠備考参照ボタン
    $extra = $disabled." onclick=\"loadwindow('../../X/KNJXATTEND_HTRAINREMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}',0,0,360,180);return;\" style=\"width:210px;\"";
    $arg["YOROKU_SANSYO"] = knjCreateBtn($objForm, "YOROKU_SANSYO", "要録の出欠の記録備考参照", $extra);

    //年間出欠備考選択ボタン
    if ($model->Properties["useReasonCollectionBtn"] == 1) {
        $arg["btn_reason_collection_select"] = makeSelectBtn($objForm, $model, "reason_collection", "btn_reason_collection_select", "年間出欠備考選択", "jobhunt_absence", $disabled);
        $arg["REASON_COLLECTION_SELECT"] = 1;
    }

    //委員会選択ボタン
    $arg["button"]["btn_committee"] = makeSelectBtn($objForm, $model, "committee", "btn_committee", "委員会選択", "jobhunt_rec", $disabled);
    //部活動選択ボタン（特別活動の記録）
    $arg["button"]["btn_club1"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "jobhunt_rec", $disabled);
    //部活動選択ボタン（本人の長所・推薦事由）
    $arg["button"]["btn_club2"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "jobhunt_recommend", $disabled);
    //記録備考選択ボタン
    if ($model->Properties["club_kirokubikou"] == 1) {
        $arg["button"]["btn_club_kirokubikou"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", "jobhunt_recommend", $disabled);
    }
    //賞選択ボタン
    if ($model->Properties["useHyosyoSansyoButton_H"]) {
        //特別活動の記録
        $arg["button"]["btn_hyosyo1"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo", "賞選択", "jobhunt_rec", $disabled);
        //本人の長所・推薦事由
        $arg["button"]["btn_hyosyo2"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo", "賞選択", "jobhunt_recommend", $disabled);
    }
    //検定選択ボタン
    $arg["button"]["btn_qualified"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified", "検定選択", "jobhunt_recommend", $disabled);

    if ($model->Properties["useSpecialActivityMst"] == "1") {
        //特別活動選択ボタン
        $arg["button"]["btn_special_activity_sele"] = makeSelectBtn($objForm, $model, "specialActivityMst", "btn_specialActivityMst", "特別活動選択", "jobhunt_rec", $disabled);
    }

    if ($model->schoolName == "koma") {
        $arg["isKoma"] = "1";
        //マラソン大会
        $arg["btn_marathon"] = makeSelectBtn($objForm, $model, "marathon", "btn_marathon", "マラソン大会選択", "jobhunt_rec", $disabled);
        //臘八摂心皆勤
        $rouhatsuKaikin = "";
        $query = knjh400_TyousasyoSyuusyokuQuery::getRouhatsuKaikin($model);
        $rouhatsuRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($rouhatsuRow["REC_CNT"] > 0 && $rouhatsuRow["REC_CNT"] == $rouhatsuRow["KAIKIN_CNT"]) {
            $rouhatsuKaikin = "臘八摂心皆勤";
        }
        knjCreateHidden($objForm, "ROUHATSU_KAIKIN", $rouhatsuKaikin);
        $extra = $disabled." onclick=\"document.forms[0].jobhunt_rec.value += document.forms[0].ROUHATSU_KAIKIN.value\"";
        $arg["btn_rouhatsu"] = knjCreateBtn($objForm, "btn_rouhatsu", "臘八摂心皆勤", $extra);
    }

    //成績参照ボタン
    $extra = $disabled." onclick=\"return btn_submit('subform1');\" style=\"width:70px\"";
    $arg["button"]["reference"] = knjCreateBtn($objForm, "btn_popup", "成績参照", $extra);
    $arg["IFRAME"] = VIEW::setIframeJs();

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //更新後前の生徒へボタン
    $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "updEdit", "update");
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //データCSVボタン
    //セキュリティーチェック
    $securityCnt = $db->getOne(knjh400_TyousasyoSyuusyokuQuery::getSecurityHigh());
    $csvSetName = "CSV";
    if ($model->Properties["useXLS"]) {
        $csvSetName = "エクセル";
    }
    if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
        $extra = " onClick=\" wopen('".REQUESTROOT."/X/KNJX192/knjx192index.php?program_id=".PROGRAMID."&mode={$model->mode}&SEND_PRGID=KNJE020&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv1"] = knjCreateBtn($objForm, "btn_csv1", "データ".$csvSetName, $extra);
    }
    //所見確認用
    if ($model->Properties["tyousasyoShokenPreview"] == '1') {
        $extra =  "onclick=\"return newwin('".SERVLET_URL."');\"";
        $arg["button"]["btn_print"] = KnjCreateBtn($objForm, "btn_print", "所見確認用", $extra);
    }

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '戻 る', $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "updated", $row["UPDATED"]);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "mode", $model->mode);
    knjCreateHidden($objForm, "GRD_YEAR", $model->grd_year);
    knjCreateHidden($objForm, "GRD_SEMESTER", $model->grd_semester);
    knjCreateHidden($objForm, "PROGRAMID", PROGRAMID);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

    //鳥取とその他で「備考」に入れる値を変える
    knjCreateHidden($objForm, "REMARK_VALUE", $model->remarkValue);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

    //所見確認用パラメータ
    if ($model->Properties["tyousasyoShokenPreview"] == '1') {
        knjCreateHidden($objForm, "PRGID", "KNJE020");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRINT_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "PRINT_GAKKI", CTRL_SEMESTER);
        knjCreateHidden($objForm, "GRADE_HR_CLASS");
        knjCreateHidden($objForm, "KANJI", "1");
        knjCreateHidden($objForm, "OS", "1");
        knjCreateHidden($objForm, "OUTPUT", "2");
        knjCreateHidden($objForm, "3_or_6_nenYoForm", $model->Properties["3_or_6_nenYoForm"]);
        //何年用のフォームを使うのかの初期値を判断する
        $query = knjh400_TyousasyoSyuusyokuQuery::getSchoolDiv();
        $schooldiv = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $nenyoformSyokiti = $schooldiv["NEN"] == '0' ? ($schooldiv["SCHOOLDIV"] == '0' ? '3' : '4') : $schooldiv["NEN"];
        knjCreateHidden($objForm, "NENYOFORM", $nenyoformSyokiti);
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
    }
}

//選択ボタン
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $disabled = "")
{
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        if ($div == "club") {                   //部活動
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR=".CTRL_YEAR."&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "committee") {       //委員会
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR=".CTRL_YEAR."&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"";
        } elseif ($div == "qualified") {       //検定
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR=".CTRL_YEAR."&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,900,500);\"";
        } elseif ($div == "hyosyo") {          //賞
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_HYOSYO_SELECT/knjx_hyosyo_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR=".CTRL_YEAR."&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,600,350);\"";
        } elseif ($div == "kirokubikou") {     //記録備考
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_KIROKUBIKOU_SELECT/knjx_club_kirokubikou_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR=".CTRL_YEAR."&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "reason_collection") {   //年間出欠備考
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_REASON_COLLECTION_SELECT/knjx_reason_collection_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR=".CTRL_YEAR."&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "specialActivityMst") { //特別活動
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_SPECIAL_ACTIVITY_SELECT/knjx_special_activity_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "marathon") {   //マラソン大会選択
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_MARATHON_SELECT/knjx_marathon_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}

function makePattern(&$objForm, &$arg, $db, &$model)
{
    //参照パターン
    $query = knjh400_TyousasyoSyuusyokuQuery::getPatternSeq($model);
    $extra = "id=\"REFER_PATTERN\" ";
    makeCmb($objForm, $arg, $db, $query, "REFER_PATTERN", $model->referPattern, $extra, 1);

    $listPattern = array();
    for ($i = 1; $i <= $model->maxPattern; $i++) {
        $listPattern[] = array(
            "LABEL" => "パターン{$i}",
            "VALUE" => $i
        );
    }

    //対象パターン
    $extra = "id=\"SELECT_PATTERN\" onChange=\"return cmb_submit(this, 'select_pattern');\"";
    makeCmbList($objForm, $arg, $listPattern, "SELECT_PATTERN", $model->selectPattern, $extra, 1);

    //左のパターンのデータをコピーボタンを作成する
    $extra = "onclick=\"return btn_submit('copy_pattern');\" ";
    $title = "左のパターンのデータをコピー";
    $arg["btn_copy_pattern"] = knjCreateBtn($objForm, "btn_copy_pattern", $title, $extra);

    $arg["use_tyousasyo_shokenTable_Seq"] = 1;

    knjCreateHidden($objForm, "HID_SELECT_PATTERN", $model->selectPattern);
}

//コンボ作成
function makeCmbList(&$objForm, &$arg, $orgOpt, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;

    foreach ($orgOpt as $row) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $value_flg = false;

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
