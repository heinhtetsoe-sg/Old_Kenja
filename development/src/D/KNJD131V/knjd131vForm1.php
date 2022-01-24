<?php

require_once('for_php7.php');
class knjd131vForm1
{
    public function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd131vindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学期コンボ
        $query = knjd131vQuery::getSemester($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $row = $db->getRow(knjd131vQuery::getTrainRow($model->schregno, $model), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //生徒情報
        $arg["data"]["SCHREGNO"] = $model->schregno;
        $arg["data"]["NAME"]     = $model->name;

        //extra
        $extra_commu = "style=\"height:145px;\"";
        $extra_spe   = "style=\"height:145px;\"";

        //特別活動の記録
        $arg["data"]["COMMUNICATION"] = getTextOrArea($objForm, "COMMUNICATION", $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $row["COMMUNICATION"], $model);
        setInputChkHidden($objForm, "COMMUNICATION", $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $arg);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);

        //hidden
        makeHidden($objForm, $model);

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent2(0);";
        } elseif ($model->cmd == "clear") {
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

        View::toHTML5($model, "knjd131vForm1.html", $arg);
    }
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SCHREGNOS");
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db)
{
    $disSelectBtn = ($model->schregno) ? "" : "disabled";

    //部活動選択
    $clubParam  = "?program_id=".PROGRAMID;
    $clubParam .= "&SEND_PRGID=".PROGRAMID;
    $clubParam .= "&EXP_YEAR=".CTRL_YEAR;
    $clubParam .= "&EXP_SEMESTER={$model->field["SEMESTER"]}";
    $clubParam .= "&SCHREGNO={$model->schregno}";
    $clubParam .= "&NAME={$model->name}";
    $clubParam .= "&TARGET=COMMUNICATION";
    $extra  = $disSelectBtn." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php{$clubParam}',";
    $extra .= "0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
    $arg["button"]["btn_club"] = knjCreateBtn($objForm, "btn_club", "部活動選択", $extra);

    //委員会選択
    $extra  = $disSelectBtn." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php{$clubParam}',";
    $extra .= "0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"";
    $arg["button"]["btn_committee"] = knjCreateBtn($objForm, "btn_committee", "委員会選択", $extra);

    //定型文選択ボタン
    if ($model->Properties["Knjd131v_Teikei_Button_Hyouji"] == "1") {
        $extra  = "onclick=\"return btn_submit_teikei('subform3', '{$model->schregno}', '{$model->field['SEMESTER']}', '{$model->grade}');\"";
        $arg["button"]["btn_teikei"] = knjCreateBtn($objForm, "btn_teikei", "定型文選択", $extra);
    }

    //更新
    $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : "disabled";
    $extra = $disable." onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //更新後前後の生徒へ
    if (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) {
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
    } else {
        $extra = "disabled style=\"width:130px\"";
        $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の生徒へ", $extra);
        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "更新後次の生徒へ", $extra);
    }

    //csv
    $commuSize  = ($model->getPro["COMMUNICATION"]["moji"] * $model->getPro["COMMUNICATION"]["gyou"] * 3);
    $fieldSize  = "COMMUNICATION={$commuSize}=通信欄";

    if ($model->Properties["use_prg_schoolkind"] == "1") {
        $disCsvBtn = ($model->schregno == "") ? " disabled": "";
    } else {
        $disCsvBtn = "";
    }
    $extra = $disCsvBtn." onClick=\" wopen('".REQUESTROOT."/X/KNJX154J/knjx154jindex.php?FIELDSIZE=".$fieldSize."&SEND_SCHOOLKIND={$model->setSchKind}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ入出力", $extra);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
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
        $extra = "style=\"height:".$height."px;\" id=\"".$name."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo, &$arg)
{
    $arg["data"][$setHiddenStr."_COMMENT"] = getTextAreaComment($keta, $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_KETA", $keta*2);
    KnjCreateHidden($objForm, $setHiddenStr."_GYO", $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_STAT", "statusarea_".$setHiddenStr);
}

function getTextAreaComment($moji, $gyo)
{
    $comment = "";
    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}
