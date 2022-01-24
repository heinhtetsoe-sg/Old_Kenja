<?php

require_once('for_php7.php');

class knje370fForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knje370findex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["data"]["SCHREGNO"] = $model->schregno;
        $arg["data"]["NAME"]     = $model->name;

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $row = $db->getRow(knje370fQuery::getSchChurchRemarkDat($model->schregno), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //教会名
        $arg["data"]["CHURCH_NAME"] = getTextOrArea($objForm, "CHURCH_NAME", $model->church_name_moji, $model->church_name_gyou, $row["CHURCH_NAME"], $model);
        setInputChkHidden($objForm, "CHURCH_NAME", $model->church_name_moji, $model->church_name_gyou, $arg);

        //受洗日
        $arg["data"]["BAPTISM_DAY"] = View::popUpCalendar($objForm, "BAPTISM_DAY", str_replace("-", "/", $row["BAPTISM_DAY"]));

        //奉仕等
        $arg["data"]["HOUSHI_TOU"] = getTextOrArea($objForm, "HOUSHI_TOU", $model->houshi_tou_moji, $model->houshi_tou_gyou, $row["HOUSHI_TOU"], $model);
        setInputChkHidden($objForm, "HOUSHI_TOU", $model->houshi_tou_moji, $model->houshi_tou_gyou, $arg);

        //備考
        $arg["data"]["REMARK"] = getTextOrArea($objForm, "REMARK", $model->remark_moji, $model->remark_gyou, $row["REMARK"], $model);
        setInputChkHidden($objForm, "REMARK", $model->remark_moji, $model->remark_gyou, $arg);

        $setdis = "";
        if ($model->exp_year == "") {
            $setdis = " disabled ";
        }

	    //更新ボタン
	    $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT || $model->exp_year == "") ? "" : "disabled";
	    $extra = $disable." onclick=\"return btn_submit('update');\"".$setdis;
	    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
	    //更新後前後の生徒へ
	    if (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) {
	        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
	    } else {
	        $extra = "disabled style=\"width:130px\"";
	        $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の生徒へ", $extra);
	        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "更新後次の生徒へ", $extra);
	    }

	    //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
	    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

	    //終了ボタン
	    $extra = "onclick=\"closeWin();\"";
	    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "PRGID", "KNJE370F");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRINT_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "PRINT_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "GRADE_HR_CLASS");

        if(get_count($model->warning)== 0 && $model->cmd !="clear"){
            $arg["next"] = "NextStudent(0);";
        }elseif($model->cmd =="clear"){
            $arg["next"] = "NextStudent(1);";
        }

        //DB切断
        Query::dbCheckIn($db);

        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML5($model, "knje370fForm1.html", $arg);
    }
}

function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo, &$arg) {
    $arg["data"][$setHiddenStr."_COMMENT"] = getTextAreaComment($keta, $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_KETA", $keta*2);
    KnjCreateHidden($objForm, $setHiddenStr."_GYO", $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_STAT", "statusarea_".$setHiddenStr);
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
        $extra = "id=\"".$name."\" style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $gyou, ($moji * 2), true);\" onPaste=\"return showPaste(this);\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "id=\"".$name."\" onPaste=\"return showPaste(this);\" onkeypress=\"btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }

    return $retArg;
}

?>
