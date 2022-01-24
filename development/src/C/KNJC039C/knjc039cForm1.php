<?php

require_once('for_php7.php');


class knjc039cForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjc039cindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ
        $query = knjc039cQuery::getSchoolKind($model);
        $extra = "onChange=\"return btn_submit('main');\" ";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);

        //集計単位コンボ作成
        $query = knjc039cQuery::getCollectionList($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "COLLECTION_CD", $model->field["COLLECTION_CD"], $extra, 1);

        //学年コンボ作成
        $query = knjc039cQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, 1);

        //年組コンボ作成
        $query = knjc039cQuery::getGradeHrclass($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, 1);

        //コメント
        $arg["ATTEND_REMARK_COMMENT"] = getTextAreaComment($model->getPro["ATTEND_REMARK"]["moji"], $model->getPro["ATTEND_REMARK"]["gyou"]);

        //初期化
        $model->data["SCHREGNO"] = array();

        $counter = 0;

        //一覧表示
        $colorFlg = false;
        $result = $db->query(knjc039cQuery::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //出席番号
            if($row["ATTENDNO"] != ""){
                $row["ATTENDNO"] = sprintf("%01d", $row["ATTENDNO"]).'番';
            }

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            //備考
            if (!isset($model->warning)) {
                $row["ATTEND_REMARK"] = getTextOrArea($objForm, "ATTEND_REMARK-".$counter, $model->getPro["ATTEND_REMARK"]["moji"], $model->getPro["ATTEND_REMARK"]["gyou"],
                                                        $row["ATTEND_REMARK"], '' , $row["ATTEND_REMARK"]);
            } else {
                $backgroundColor = "";
                if ($row["ATTEND_REMARK"] != $model->data["ATTEND_REMARK"][$counter]) {
                    $backgroundColor = "background:#ccffcc";
                }
                $row["ATTEND_REMARK"] = getTextOrArea($objForm, "ATTEND_REMARK-".$counter, $model->getPro["ATTEND_REMARK"]["moji"], $model->getPro["ATTEND_REMARK"]["gyou"],
                                                        $model->data["ATTEND_REMARK"][$counter], $backgroundColor, $row["ATTEND_REMARK"]);
            }

            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $row["COUNTER"] = $counter;

            $counter++;
            $arg["data"][] = $row;
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "counter", $counter);
        knjCreateHidden($objForm, "moji", $model->getPro["ATTEND_REMARK"]["moji"]);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc039cForm1.html", $arg);
    }
}

function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $backgroundColor = "", $defaultVal) {
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
        $extra = "style=\"height:".$height."px;\" onChange=\"changeColorIfInputChange('".$defaultVal. "', '".$name."')\" style=\"".$backgroundColor."\" ";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" onChange=\"changeColorIfInputChange('".$defaultVal. "', '".$name."')\" style=\"".$backgroundColor."\" ";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), ($moji*3), $extra);
    }
    return $retArg;
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

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="")
{
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
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
function makeBtn(&$objForm, &$arg) {
    //更新ボタンを作成する
    $disabled = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
