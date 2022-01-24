<?php

require_once('for_php7.php');

class knjd135pForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd135pindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        $model->schoolName = $db->getOne(knjd135pQuery::getNameMstZ010($model));
        //学期コンボ作成
        $query = knjd135pQuery::getSemesterList($model);
        $extra = "onchange=\"return btn_submit('main', '', '', '');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年末は今学期とする
        $semester = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //年組コンボ作成
        $query = knjd135pQuery::getGradeHrclass($model, $semester);
        $extra = "onchange=\"return btn_submit('main', '', '', '');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, 1);
        
        //コメント
        $arg["COMMUNICATION_COMMENT"] = "(全角".$model->getPro["COMMUNICATION"]["moji"]."文字X".$model->getPro["COMMUNICATION"]["gyou"]."行まで)";
        $arg["ATTEND_COMMENT"] = "(全角".$model->getPro["ATTEND_STR"]["moji"]."文字X".$model->getPro["ATTEND_STR"]["gyou"]."行まで)";

        //初期化
        $model->data = array();
        $counter = 0;

        //一覧表示
        $colorFlg = false;
        $result = $db->query(knjd135pQuery::selectQuery($model, $semester));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //出席番号
            if ($row["ATTENDNO"] != "") {
                $row["ATTENDNO"] = sprintf("%01d", $row["ATTENDNO"]).'番';
            }

            $targetName = "COMMUNICATION"."-".$counter;
            //特記事項・その他
            $value = (!isset($model->warning)) ? $row["COMMUNICATION"] : $model->fields["COMMUNICATION"][$counter];
            $row["COMMUNICATION"] = getTextOrArea($objForm, "COMMUNICATION"."-".$counter, $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $value, $model);
            setInputChkHidden($objForm, "COMMUNICATION"."-".$counter, $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"]);
            
            //部活動参照ボタン
            $year = CTRL_YEAR;
            $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php?program_id=KNJD135P&SEND_PRGID=KNJD135P&EXP_YEAR={$year}&EXP_SEMESTER={$model->field["SEMESTER"]}&SCHREGNO={$row["SCHREGNO"]}&NAME={$row["NAME"]}&TARGET={$targetName}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);return;\"";
            $row["CLUB"] = KnjCreateBtn($objForm, "CLUB", "部活動選択", $extra);
            //委員会参照ボタン
            $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=KNJD135P&SEND_PRGID=KNJD135P&EXP_YEAR={$year}&EXP_SEMESTER={$model->field["SEMESTER"]}&SCHREGNO={$row["SCHREGNO"]}&NAME={$row["NAME"]}&TARGET={$targetName}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);return;\"";
            $row["COMMITTEE"] = KnjCreateBtn($objForm, "COMMITTEE", "委員会選択", $extra);

            //記録備考選択ボタン
            $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_KIROKUBIKOU_SELECT/knjx_club_kirokubikou_selectindex.php?program_id=KNJD135P&SEND_PRGID=KNJD135P&EXP_YEAR={$year}&EXP_SEMESTER={$model->field["SEMESTER"]}&SCHREGNO={$row["SCHREGNO"]}&NAME={$row["NAME"]}&TARGET={$targetName}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);return;\"";
            $row["RECREMARK"] = KnjCreateBtn($objForm, "RECREMARK", "記録備考選択", $extra);
            //検定選択ボタン
            $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php?program_id=KNJD135P&SEND_PRGID=KNJD135P&EXP_YEAR={$year}&EXP_SEMESTER={$model->field["SEMESTER"]}&SCHREGNO={$row["SCHREGNO"]}&NAME={$row["NAME"]}&TARGET={$targetName}',0,document.documentElement.scrollTop || document.body.scrollTop,800,500);return;\"";
            $row["CERTIF"] = KnjCreateBtn($objForm, "CERTIF", "検定選択", $extra);
            //賞選択ボタン
            $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_HYOSYO_SELECT/knjx_hyosyo_selectindex.php?program_id=KNJD135P&SEND_PRGID=KNJD135P&EXP_YEAR={$year}&EXP_SEMESTER={$model->field["SEMESTER"]}&SCHREGNO={$row["SCHREGNO"]}&NAME={$row["NAME"]}&TARGET={$targetName}',0,document.documentElement.scrollTop || document.body.scrollTop,600,350);return;\"";
            $row["AWARD"] = KnjCreateBtn($objForm, "AWARD", "賞選択", $extra);
            //全取込ボタン
            $fullStr = getFullInsStr($model, $db, $row["SCHREGNO"]);
            $extra = "onclick=\"setFullStr('{$targetName}','{$fullStr}', {$model->getPro["COMMUNICATION"]["moji"]}, {$model->getPro["COMMUNICATION"]["gyou"]})\";";
            $row["FULL_IN"] = knjCreateBtn($objForm, "FULL_IN", "全 取 込", $extra);

            $row["COLOR"] = "#ffffff";

            //出欠の特記事項
            $value = (!isset($model->warning)) ? $row["ATTENDREC_REMARK"] : $model->fields["ATTENDREC_REMARK"][$counter];
            $row["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK"."-".$counter, $model->getPro["ATTEND_STR"]["moji"], $model->getPro["ATTEND_STR"]["gyou"], $value, $model);
            setInputChkHidden($objForm, "ATTENDREC_REMARK"."-".$counter, $model->getPro["ATTEND_STR"]["moji"], $model->getPro["ATTEND_STR"]["gyou"]);
            //出欠の記録参照ボタン
            $targetName = "ATTENDREC_REMARK"."-".$counter;
            $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_SYUKKETSUKIROKU/knjx_syukketsukirokuindex.php?program_id=KNJD135P&SEND_PRGID=KNJD135P&EXP_YEAR={$year}&EXP_SEMESTER={$model->field["SEMESTER"]}&SCHREGNO={$row["SCHREGNO"]}&NAME={$row["NAME"]}&TARGET={$targetName}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);return;\"";
            $row["ATTEND_RECSTR"] = KnjCreateBtn($objForm, "ATTEND_RECSTR", "出欠の記録参照", $extra);

            $counter++;
            $arg["data"][] = $row;
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd135pForm1.html", $arg);
    }
}

function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model)
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
        $extra = "id=\"".$name."\" style=\"height:".$height."px;\" onPaste=\"return showPaste(this);\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "id=\"".$name."\" onkeypress=\"btn_keypress();\" onPaste=\"return showPaste(this);\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo)
{
    knjCreateHidden($objForm, $setHiddenStr."_KETA", $keta*2);
    knjCreateHidden($objForm, $setHiddenStr."_GYO", $gyo);
    knjCreateHidden($objForm, $setHiddenStr."_STAT", "statusarea".$setHiddenStr);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank != "") {
        $opt[] = array('label' => "", 'value' => "");
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

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //CSV入出力ボタンを作成する
    $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_D135P/knjx_d135pindex.php?SCHOOL_KIND={$model->schKind}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV入出力", $extra);
    //更新ボタンを作成する
    $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('update', '', '', '');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset', '', '', '');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//全取込文字列
function getFullInsStr($model, $db, $schregno)
{
    $dlmStr = "‡";
    $retStr = "";
    //部活
    $dlm = "";
    $resultW = $db->query(knjd135pQuery::getClub($model, $schregno, CTRL_YEAR.'-04-01', (intval(CTRL_YEAR) + 1).'-03-31'));
    while ($Row = $resultW->fetchRow(DB_FETCHMODE_ASSOC)) {
        $retStr .= $dlm.$Row["CLUB_SHOW"]." ".$Row["EXECUTIVE_SHOW"];
        $dlm = $dlmStr;
    }
    $resultW->free();
    if (strlen($retStr) > 0) {
        $retStr .= $dlm;
    }

    //記録備考
    $resultW = $db->query(knjd135pQuery::getSchregClubHdetailDat($model, $schregno));
    $dlm = "";
    while ($Row = $resultW->fetchRow(DB_FETCHMODE_ASSOC)) {
        $Row["MEET_SHOW"]  = $Row["MEET_NAME"];
        $Row["MEET_SHOW"] .= ((strlen($Row["MEET_SHOW"]) > 0 && strlen($Row["KINDNAME"]) > 0) ? " " : "").$Row["KINDNAME"];
        $Row["MEET_SHOW"] .= ((strlen($Row["MEET_SHOW"]) > 0 && strlen($Row["RECORDNAME"]) > 0) ? " " : "").$Row["RECORDNAME"];
        $Row["MEET_SHOW"] .= ((strlen($Row["MEET_SHOW"]) > 0 && strlen($Row["DOCUMENT"]) > 0) ? " " : "").$Row["DOCUMENT"];
        $retStr .= $dlm.$Row["CLUB_SHOW"]." ".$Row["DIV_NAME"]." ".$Row["MEET_SHOW"]." ".$Row["DETAIL_REMARK"];
        $dlm = $dlmStr;
    }
    $resultW->free();
    if (strlen($retStr) > 0) {
        $retStr .= $dlm;
    }

    //委員会
    $dlm = "";
    $resultW = $db->query(knjd135pQuery::getCommittee($model, CTRL_YEAR, $schregno));
    while ($Row = $resultW->fetchRow(DB_FETCHMODE_ASSOC)) {
        $retStr .= $dlm.$Row["SEMESTER_SHOW"]." ".$Row["COMMITTEE_SHOW"]." ".$Row["CHARGE_SHOW"]." ".$Row["EXECUTIVE_SHOW"];
        $dlm = $dlmStr;
    }
    $resultW->free();
    if (strlen($retStr) > 0) {
        $retStr .= $dlm;
    }

    //検定
    $dlm = "";
    $resultW = $db->query(knjd135pQuery::getQualified($model, $db, $schregno));
    while ($Row = $resultW->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->Properties["useQualifiedMst"] == '1') {
            $Row["RANK_SHOW"]  = $Row["RANK"];
            $Row["RANK_SHOW"] .= ((strlen($Row["RANK_SHOW"]) > 0) ? " " : "").((strlen($Row["SCORE"]) > 0) ? $Row["SCORE"]."点" : "");
            $Row["RANK_SHOW"] .= ((strlen($Row["RANK_SHOW"]) > 0) ? " " : "").$Row["REMARK"];
            $retStr .= $dlm.$Row["QUALIFIED_SHOW"]." ".$Row["RANK_SHOW"]." ".str_replace("-", "/", $Row["REGDDATE"]);
        } else {
            $Row["CONTENTS_SHOW"] = $Row["CONTENTS"];
            $Row["REMARK_SHOW"] = $Row["REMARK"];
            $retStr .= $dlm.$Row["CONTENTS_SHOW"]." ".$Row["REMARK_SHOW"]." ".str_replace("-", "/", $Row["REGDDATE"]);
        }
        $dlm = $dlmStr;
    }
    $resultW->free();
    if (strlen($retStr) > 0) {
        $retStr .= $dlm;
    }

    //賞
    $dlm = "";
    $resultW = $db->query(knjd135pQuery::getHyosyo($model, $schregno));
    while ($Row = $resultW->fetchRow(DB_FETCHMODE_ASSOC)) {
        $retStr .= $dlm.$Row["DETAIL_SDATE"]." ".$Row["DETAILCDNAME"]." ".$Row["CONTENT"]." ".$Row["REMARK"];
        $dlm = $dlmStr;
    }
    $resultW->free();
    $retStr = str_replace("\r\n", $dlmStr, $retStr);
    $retStr = str_replace("\n", $dlmStr, $retStr);
    return $retStr;
}
