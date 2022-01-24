<?php

require_once('for_php7.php');

class knjd139sForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd139sindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //学期コンボ
        $query = knjd139sQuery::getSemester($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        if ($model->dispPatern == 1) {
            $arg["data"]["dispSpcRowCnt"] = 2;
            $arg["isNotPrimary"] = "";
        } else {
            $arg["data"]["dispSpcRowCnt"] = 3;
            $arg["isNotPrimary"] = "1";
        }
        $arg["data"]["PRJ1_TITLE"] = $model->prjTitle;
        //警告メッセージを表示しない場合
        if (((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) && $model->cmd != 'attend') {
            //各テキストエリアの内容をテーブルから取得
            $div = "01";
            for($i = 1; $i <= 3; $i++){
                $code = "0{$i}";
                $query = knjd139sQuery::getHreportremarkDetailDatByCode($model, "9", $div, $code);
                $setwk = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $model->field["REMARK{$div}_{$code}"] = $setwk["REMARK1"];
            }
            $div = "02";
            for($i = 1; $i <= 4; $i++){
                $code = "0{$i}";
                $query = knjd139sQuery::getHreportremarkDetailDatByCode($model, ($i == 2 ? "9" : $model->field["SEMESTER"]), $div, $code);
                $setwk = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $model->field["REMARK{$div}_{$code}"] = $setwk["REMARK1"];
            }
            $query = knjd139sQuery::getHreportremarkDat($model, $model->field["SEMESTER"]);
            $setwk = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->field["ATTENDREC_REMARK"] = $setwk["ATTENDREC_REMARK"];
            $model->field["COMMUNICATION"] = $setwk["COMMUNICATION"];
            
            $arg["NOT_WARNING"] = 1;
        }
        $row =& $model->field;


        //通知票所見データ

        //通年データ
        $moji = 25;
        $gyou = 1;
        $arg["data"]["REMARK01_01"] = getTextOrArea($objForm, "REMARK01_01", $moji, $gyou, $row["REMARK01_01"], $model);
        setInputChkHidden($objForm, "REMARK01_01", $moji, $gyou);
        $arg["data"]["REMARK01_01_COMMENT"] = getTextAreaComment($moji, $gyou);

        $moji = 10;
        $gyou = 1;
        $arg["data"]["REMARK01_02"] = getTextOrArea($objForm, "REMARK01_02", $moji, $gyou, $row["REMARK01_02"], $model);
        setInputChkHidden($objForm, "REMARK01_02", $moji, $gyou);
        $arg["data"]["REMARK01_02_COMMENT"] = getTextAreaComment($moji, $gyou);

        $moji = 10;
        $gyou = 1;
        $arg["data"]["REMARK01_03"] = getTextOrArea($objForm, "REMARK01_03", $moji, $gyou, $row["REMARK01_03"], $model);
        setInputChkHidden($objForm, "REMARK01_03", $moji, $gyou);
        $arg["data"]["REMARK01_03_COMMENT"] = getTextAreaComment($moji, $gyou);

        $moji = 25;
        $gyou = 1;
        $arg["data"]["REMARK02_02"] = getTextOrArea($objForm, "REMARK02_02", $moji, $gyou, $row["REMARK02_02"], $model);
        setInputChkHidden($objForm, "REMARK02_02", $moji, $gyou);
        $arg["data"]["REMARK02_02_COMMENT"] = getTextAreaComment($moji, $gyou);

        //各学期データ
        $moji = 10;
        $gyou = 3;
        $arg["data"]["REMARK02_01"] = getTextOrArea($objForm, "REMARK02_01", $moji, $gyou, $row["REMARK02_01"], $model);
        setInputChkHidden($objForm, "REMARK02_01", $moji, $gyou);
        $arg["data"]["REMARK02_01_COMMENT"] = getTextAreaComment($moji, $gyou);

        $moji = 10;
        $gyou = 1;
        $arg["data"]["REMARK02_03"] = getTextOrArea($objForm, "REMARK02_03", $moji, $gyou, $row["REMARK02_03"], $model);
        setInputChkHidden($objForm, "REMARK02_03", $moji, $gyou);
        $arg["data"]["REMARK02_03_COMMENT"] = getTextAreaComment($moji, $gyou);

        if ($model->dispPatern == 2) {
            $moji = 10;
            $gyou = 3;
            $arg["data"]["REMARK02_04"] = getTextOrArea($objForm, "REMARK02_04", $moji, $gyou, $row["REMARK02_04"], $model);
            setInputChkHidden($objForm, "REMARK02_04", $moji, $gyou);
            $arg["data"]["REMARK02_04_COMMENT"] = getTextAreaComment($moji, $gyou);
        }

        $moji = 25;
        $gyou = 1;
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $moji, $gyou, $row["ATTENDREC_REMARK"], $model);
        setInputChkHidden($objForm, "ATTENDREC_REMARK", $moji, $gyou);
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = getTextAreaComment($moji, $gyou);
        
        $moji = 18;
        $gyou = 7;
        $arg["data"]["COMMUNICATION"] = getTextOrArea($objForm, "COMMUNICATION", $moji, $gyou, $row["COMMUNICATION"], $model);
        setInputChkHidden($objForm, "COMMUNICATION", $moji, $gyou);
        $arg["data"]["COMMUNICATION_COMMENT"] = getTextAreaComment($moji, $gyou);

        /**********/
        /* ボタン */
        /**********/

        $setdis = "";
        if ($model->exp_year == "") {
            $setdis = " disabled ";
        }
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"".$setdis;
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //更新後前後の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
        //取消ボタン（途中）
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //一括更新ボタン
        $link = REQUESTROOT."/D/KNJD139S/knjd139sindex.php?cmd=ikkatsu&SCHREGNO={$model->schregno}&sendSEME={$model->field["SEMESTER"]}";
        $extra = "onclick=\"Page_jumper('$link');\"";
        $arg["button"]["ikkatsu"] = knjCreateBtn($objForm, "ikkatsu", "一括更新", $extra);

        //別画面表示用ボタン
        //委員会選択
        $target = "REMARK02_01";
        $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"".$setdis;
        $arg["button"]["btn_committee1"] = knjCreateBtn($objForm, "btn_committee", "委員会選択", $extra);
        //クラブ活動
        $target = "REMARK02_04";
        $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"".$setdis;
        $arg["button"]["btn_club"] = knjCreateBtn($objForm, "btn_club", "部活動選択", $extra);
        //検定選択
        $target = "REMARK02_04";
        $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"".$setdis;
        $arg["button"]["btn_qualified"] = knjCreateBtn($objForm, "btn_qualified", "検定選択", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "cmd");

        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "useQualifiedMst", $model->Properties["useQualifiedMst"]);

        $arg["IFRAME"] = VIEW::setIframeJs();

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent2(0);";
        } else if ($model->cmd == "clear") {
            $arg["next"] = "NextStudent2(1);";
        }

        //DB切断
        Query::dbCheckIn($db);

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd139sForm1.html", $arg);
    }
}

//コンボボックス作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo) {
    KnjCreateHidden($objForm, $setHiddenStr."_KETA", $keta*2);
    KnjCreateHidden($objForm, $setHiddenStr."_GYO", $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_STAT", "statusarea".$setHiddenStr);
}

function getTextAreaComment($moji, $gyo) {
    $comment = "";
    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}

//テキストボックスorテキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model) {
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
        $extra = "id=\"".$name."\" style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $gyou, ($moji * 2)+1, true);\" onPaste=\"return showPaste(this);\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "id=\"".$name."\" onPaste=\"return showPaste(this);\" onkeypress=\"btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}
?>
