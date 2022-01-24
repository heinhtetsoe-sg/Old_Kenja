<?php

require_once('for_php7.php');

class knjd132aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd132aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        if (!isset($model->warning)) {
            //所見取得(9学期)
            $row = $db->getRow(knjd132aQuery::getHreportremarkDat($model, "9"), DB_FETCHMODE_ASSOC);
        }
        //道徳
        $arg["data"]["REMARK2"] = getTextOrArea($objForm, "REMARK2", $model->remark2_moji, $model->remark2_gyou, $row["REMARK2"], $model);
        setInputChkHidden($objForm, "REMARK2", $model->remark2_moji, $model->remark2_gyou, $arg);

        //ボタン作成
        //更新ボタン
        $extra = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update')\"" : "disabled";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //更新後前後の生徒へ
        if (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
        } else {
            $extra = "disabled style=\"width:130px\"";
            $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の生徒へ", $extra);
            $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "更新後次の生徒へ", $extra);
        }

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "終 了", $extra);

        $grade = $db->getOne(knjd132aQuery::getGrade($model, $model->exp_semester));

        //定型文ボタン1
        $disFlg = $grade == "" ? " disabled" : "";
        $extra = " onclick=\"loadwindow('../../X/KNJX_D132A/knjx_d132aindex.php?";
        $extra .= "cmd=teikei&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$grade}&DATA_DIV=16&TITLE=道徳&TEXTBOX=REMARK2'";
        $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 550, 350);\" ".$disFlg;
        $arg["button"]["btn_teikeibun1"] = knjCreateBtn($objForm, "btn_teikeibun", "定型文1選択", $extra);

        //定型文ボタン2
        $extra = " onclick=\"loadwindow('../../X/KNJX_D132A/knjx_d132aindex.php?";
        $extra .= "cmd=teikei&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$grade}&DATA_DIV=17&TITLE=道徳&TEXTBOX=REMARK2'";
        $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 550, 350);\" ".$disFlg;
        $arg["button"]["btn_teikeibun2"] = knjCreateBtn($objForm, "btn_teikeibun", "定型文2選択", $extra);

        //定型文ボタン3
        $extra = " onclick=\"loadwindow('../../X/KNJX_D132A/knjx_d132aindex.php?";
        $extra .= "cmd=teikei&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$grade}&DATA_DIV=18&TITLE=道徳&TEXTBOX=REMARK2'";
        $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 550, 350);\" ".$disFlg;
        $arg["button"]["btn_teikeibun3"] = knjCreateBtn($objForm, "btn_teikeibun", "定型文3選択", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        if (get_count($model->warning) == 0 && $model->cmd != "reset") {
            $arg["next"] = "NextStudent2(0);";
        } else if (get_count($model->warning) != 0 || $model->cmd == "reset") {
            $arg["next"] = "NextStudent2(1);";
        }
        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd132aForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank != "") $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

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
        $extra = "style=\"height:".$height."px;\" id=\"".$name."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
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

?>
