<?php

require_once('for_php7.php');

class knjd183dForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd183dindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //学期コンボ
        $query = knjd183dQuery::getSemester();
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //警告メッセージを表示しない場合
        if (((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) && $model->cmd != 'attend') {
            $row  = $db->getRow(knjd183dQuery::getHreportremarkDat($model), DB_FETCHMODE_ASSOC);
            $row2 = $db->getRow(knjd183dQuery::getHreportremarkDetailDat($model), DB_FETCHMODE_ASSOC);

            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        if ($model->field['SEMESTER'] == '9') {
            $arg["SEMESTER_9"] = true;
        } else {
            $arg["NOT_SEMESTER_9"] = true;
        }

        //行動の記録
        $recactvnamelist = array();
        $recactvidlist = array();
        $arg["disprecactflg"] = "";
        if (!is_null($model -> name)) {
            //観点マスタ
            $bsm_usechkgrade = array("01","02","03");
            $maxlen = 0;
            $itemArray = array();

            $query = knjd183dQuery::getBehaviorSemesMst($model);
            $result = $db->query($query);
            $tmpval = 0;
            while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $ival = sprintf("%02d", $tmpval);
                if (in_array($gradeCd, $bsm_usechkgrade)) {
                    $bsmusedetail = get_count($model->warning) != 0 ? $this->record["RECORD"][$ival] : $row1["LABEL"];
                } else {
                    $bsmusedetail = get_count($model->warning) != 0 ? $this->record["RECORD"][$ival] : $row1["DETAIL"];
                }
                $itemArray[$row1["VALUE"]] = $bsmusedetail;
                //MAX文字数
                if ($maxlen < mb_strwidth($bsmusedetail)) {
                    $maxlen = mb_strwidth($bsmusedetail);
                }
                $tmpval++;
            }
            $result->free();

            $width = ($maxlen * 8 < 280) ? 280 : $maxlen * 8;
            $arg["TABLE_LABEL_WIDTH"] = $width;
            $arg["RECORD_LABEL_WIDTH"] = $width - 50;
            $arg["RECORD_VALUE_WIDTH"] = 50;

            //記録の取得
            $Row = $row1 = array();
            $result = $db->query(knjd183dQuery::getBehavior($model));
            while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Row["RECORD"][$row1["CODE"]] = $row1["RECORD"];
            }
            $result->free();

            $arg["disprecactflg"] = "1";

            if ($model->Properties["knjdBehaviorsd_UseText"] == "1") {
                $query = knjd183dQuery::getNameMst($model->exp_year, "D036");
                $result = $db->query($query);
                $sep = "";
                $settxt = "(";
                while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $settxt .= $sep . $row1["LABEL"];
                    $recactvidlist[] = $row1["VALUE"];
                    $sep = " ";
                }
                $settxt .= ")";
                $arg["TEXT_TITLE"] = $settxt;
                $result->free();
            }

            if (is_array($itemArray)) {
                foreach ($itemArray as $key => $val) {
                    $setData = array();
                    //データ
                    if ($model->Properties["knjdBehaviorsd_UseText"] == "1") {
                        //項目名
                        $setData["RECORD_LABEL"] = $val;
                        //テキスト
                        $extra = "id=\"RECORD".$key."\" STYLE=\"text-align: center\"; onblur=\"chkrecactv(this);\"  onKeyDown=\"keyChangeEntToTab(this);\"";
                        $setData["RECORD_VALUE"] = knjCreateTextBox($objForm, $Row["RECORD"][$key], "RECORD".$key, 3, 1, $extra);
                    } else {
                        $id = "RECORD".$key;
                        //項目名
                        $setData["RECORD_LABEL"] = "<LABEL for={$id}>".$val."</LABEL>";
                        //チェックボックス
                        $check1 = ($Row["RECORD"][$key] == "1") ? "checked" : "";
                        $extra = $check1." id={$id}";
                        $setData["RECORD_VALUE"] = knjCreateCheckBox($objForm, "RECORD".$key, "1", $extra, "");
                    }
                    $recactvnamelist[] = "RECORD".$key;
                    $arg["data2"][] = $setData;
                }
            }
        }

        $nyuryokuPattern = "";

        $result = $db->query(knjd183dQuery::getNyuryokuPattern());
        while ($row1 = $result -> fetchRow(DB_FETCHMODE_ASSOC)) {
            $nyuryokuPattern = $row1["REMARK1"];
        }
        $result->free();

        $handicap = "";
        $result = $db->query(knjd183dQuery::getHandCap($model->schregno));
        while ($row1 = $result -> fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"]["NAME_SHOW"] = $row1["NAME"];
            $handicap= $row1["HANDICAP"];
        }
        $result->free();

        //取得資格・検定
        if ($nyuryokuPattern == '2' || $nyuryokuPattern == '3') {
            //画面の表示非表示
            $arg["SHUTOKUSIKAKU_VISIBLE_FLG"] = 1;
        }
        if ($handicap != '002') {
            //通知表Aパターン
            if ($nyuryokuPattern == '2') {
                $model->getPro["SHUTOKUSIKAKU"]["moji"]=55;
                $model->getPro["SHUTOKUSIKAKU"]["gyou"]=4;
            } elseif ($nyuryokuPattern == '3') {
                $model->getPro["SHUTOKUSIKAKU"]["moji"]=25;
                $model->getPro["SHUTOKUSIKAKU"]["gyou"]=4;
            } else {
                //総探のみ使用、両方表示しないのときは何もしない
            }
        } else {
            //通知表Bパターン
            if ($nyuryokuPattern == '2') {
                $model->getPro["SHUTOKUSIKAKU"]["moji"]=35;
                $model->getPro["SHUTOKUSIKAKU"]["gyou"]=4;
            } elseif ($nyuryokuPattern == '3') {
                $model->getPro["SHUTOKUSIKAKU"]["moji"]=15;
                $model->getPro["SHUTOKUSIKAKU"]["gyou"]=4;
            } else {
                //総探のみ使用、両方表示しないのときは何もしない
            }
        }
        $arg["data"]["SHUTOKUSIKAKU"] = getTextOrArea($objForm, "SHUTOKUSIKAKU", $model->getPro["SHUTOKUSIKAKU"]["moji"], $model->getPro["SHUTOKUSIKAKU"]["gyou"], $row["REMARK1"], $model);
        setInputChkHidden($objForm, "SHUTOKUSIKAKU", $model->getPro["SHUTOKUSIKAKU"]["moji"], $model->getPro["SHUTOKUSIKAKU"]["gyou"], $arg);

        //自立活動
        if ($handicap == '002') {
            //画面の表示非表示
            $arg["ZIRITUKATUDO_VISIBLE_FLG"] = 1;
        }
        $arg["data"]["ZIRITUKATUDO"] = getTextOrArea($objForm, "ZIRITUKATUDO", $model->getPro["ZIRITUKATUDO"]["moji"], $model->getPro["ZIRITUKATUDO"]["gyou"], $row2["REMARK1"], $model);
        setInputChkHidden($objForm, "ZIRITUKATUDO", $model->getPro["ZIRITUKATUDO"]["moji"], $model->getPro["ZIRITUKATUDO"]["gyou"], $arg);

        //所見
        $arg["data"]["COMMUNICATION"] = getTextOrArea($objForm, "COMMUNICATION", $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $row["COMMUNICATION"], $model);
        setInputChkHidden($objForm, "COMMUNICATION", $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $arg);

        //精勤フラグ
        $extra = "";
        if ($row["ATTENDREC_REMARK"] == '精勤') {
            //チェックオン時or登録済はcheckedにする
            $extra .= " checked";
        }
        if (checkKaikin($db, $model->schregno, $model->field["SEMESTER"]) == true) {
            //皆勤の場合はチェックボックスをdisabledにする
            $extra .= " disabled";
        }
        $extra .= " id=\"SEIKIN_CHECK\"";
        $arg["data"]["SEIKIN_CHECK"] = knjCreateCheckBox($objForm, "SEIKIN_CHECK", "1", $extra, "");

        /**********/
        /* ボタン */
        /**********/
        //検定選択ボタン
        $extra = "onclick=\"return btn_submit('subform6');\"";
        $arg["button"]["btn_shukketu_kiroku"] = knjCreateBtn($objForm, "btn_shukketu_kiroku", "出欠記録参照", $extra);

        //検定選択ボタン
        $extra = "onclick=\"return btn_submit('subform5');\"";
        $arg["button"]["btn_sikaku_ansyo"] = knjCreateBtn($objForm, "btn_sikaku_ansyo", "検定選択", $extra);
        //賞選択ボタン
        $extra = " onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_HYOSYO_SELECT/knjx_hyosyo_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET=REMARK1',400,document.documentElement.scrollTop || document.body.scrollTop,600,350);\"";
        $arg["button"]["btn_hyosyo"] = knjCreateBtn($objForm, "btn_hyosyo", "賞選択", $extra);
        //記録備考選択参照
        $extra = " onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_KIROKUBIKOU_SELECT/knjx_club_kirokubikou_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET=REMARK1',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        $arg["button"]["btn_kirokubikou"] = knjCreateBtn($objForm, "btn_kirokubikou", "記録備考選択", $extra);
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //更新後前後の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
        //取消ボタン
        $extra = " onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //CSVボタン
        $extra = "onclick=\"return wopen('".REQUESTROOT."/X/KNJX_D183D/knjx_d183dindex.php?FIELDSIZE=".$fieldSize."&SEND_SCHOOLKIND={$model->setSchKind}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "remark1_gyou", $model->getPro["REMARK1"]["gyou"]);
        knjCreateHidden($objForm, "remark1_moji", $model->getPro["REMARK1"]["moji"]);
        knjCreateHidden($objForm, "SHUTOKUSIKAKU_GYOU", $model->getPro["SHUTOKUSIKAKU"]["gyou"]);
        knjCreateHidden($objForm, "SHUTOKUSIKAKU_MOJI", $model->getPro["SHUTOKUSIKAKU"]["moji"]);
        knjCreateHidden($objForm, "communication_gyou", $model->getPro["COMMUNICATION"]["gyou"]);
        knjCreateHidden($objForm, "communication_moji", $model->getPro["COMMUNICATION"]["moji"]);
        knjCreateHidden($objForm, "attendrec_remark_gyou", $model->getPro["ATTENDREC_REMARK"]["gyou"]);
        knjCreateHidden($objForm, "attendrec_remark_moji", $model->getPro["ATTENDREC_REMARK"]["moji"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "useQualifiedMst", $model->Properties["useQualifiedMst"]);
        knjCreateHidden($objForm, "HID_RECACTVNAMELIST", implode(",", $recactvnamelist));
        knjCreateHidden($objForm, "HID_RECACTVIDLIST", implode(",", $recactvidlist));

        $arg["IFRAME"] = VIEW::setIframeJs();

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent2(0);";
        } elseif ($model->cmd == "clear") {
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
        View::toHTML5($model, "knjd183dForm1.html", $arg);
    }
}

//コンボボックス作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
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

//テキストボックスorテキストエリア作成
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
        $extra = "style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $gyou, ($moji * 2), true);\" onPaste=\"return showPaste(this);\" id=\"".$name."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onPaste=\"return showPaste(this);\" onkeypress=\"btn_keypress();\" id=\"".$name."\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo, &$arg)
{
    $arg["data"][$setHiddenStr."_COMMENT"] = getTextAreaComment($keta, $gyo);
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

function checkKaikin($db, $schregno, $semester)
{
    //欠時数取得
    $result = $db->query(knjd183dQuery::getKetsuji($schregno, $semester));
    while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $ketsuji = $row1["KETSUJI"];
    }

    //(欠席＋遅刻＋早退)数取得
    $result = $db->query(knjd183dQuery::getSyussekiTikokuSoutai($schregno, $semester));
    while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $sts = $row1["SYUSSEKI_TIKOKU_SOUTAI"];
    }
    //欠時 + (欠席、遅刻、早退)
    $kaikin = $ketsuji + $sts;

    //皆勤の可能性があるとき
    if ($kaikin == '0') {
        return true;
    }
    return false;
}
