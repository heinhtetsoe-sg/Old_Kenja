<?php

require_once('for_php7.php');


class knjd132vForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd132vindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        $model->schoolName = $db->getOne(knjd132vQuery::getNameMstZ010($model));
        //学期コンボ作成
        $query = knjd132vQuery::getSemesterList($model);
        $extra = "onchange=\"return btn_submit('main', '', '', '');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //学年末は今学期とする
        $semester = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //年組コンボ作成
        $query = knjd132vQuery::getGradeHrclass($model, $semester);
        $extra = "onchange=\"return btn_submit('main', '', '', '');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, 1);
        
        //コメント
        $arg["COMMUNICATION_COMMENT"] = "(全角".$model->getPro["COMMUNICATION"]["moji"]."文字X".$model->getPro["COMMUNICATION"]["gyou"]."行まで)";

        //初期化
        $model->data = array();
        $counter = 0;

        //一覧表示
        $colorFlg = false;
        $result = $db->query(knjd132vQuery::selectQuery($model, $semester));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //出席番号
            if ($row["ATTENDNO"] != "") {
                $row["ATTENDNO"] = sprintf("%01d", $row["ATTENDNO"]).'番';
            }

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            //備考
            $value = (!isset($model->warning)) ? $row["COMMUNICATION"] : $model->fields["COMMUNICATION"][$counter];
            $row["COMMUNICATION"] = getTextOrArea($objForm, "COMMUNICATION"."-".$counter, $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $value, $model);
            
            //部活動選択ボタン
            $clubParam  = "?program_id=".PROGRAMID;
            $clubParam .= "&SEND_PRGID=".PROGRAMID;
            $clubParam .= "&EXP_YEAR=".CTRL_YEAR;
            $clubParam .= "&EXP_SEMESTER={$model->field["SEMESTER"]}";
            $clubParam .= "&SCHREGNO={$row["SCHREGNO"]}";
            $clubParam .= "&NAME={$row["NAME"]}";
            $clubParam .= "&TARGET=COMMUNICATION"."-".$counter;
            $extra  = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php{$clubParam}',";
            $extra .= "0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
            $row["CLUB"] = knjCreateBtn($objForm, "CLUB", "部活動選択", $extra);

            //委員会選択ボタン
            $extra  = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php{$clubParam}',";
            $extra .= "0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"";
            $row["COMMITTEE"] = knjCreateBtn($objForm, "COMMITTEE", "委員会選択", $extra);

            //定型文選択ボタン
            if ($model->Properties["Knjd132v_Teikei_Button_Hyouji"] == "1") {
                $extra  = "onclick=\"return btn_submit('teikei', '{$row["SCHREGNO"]}', '{$semester}', '{$counter}');\"";
                $row["btn_teikei"] = knjCreateBtn($objForm, "btn_teikei", "定型文選択", $extra);
            }

            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

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
        View::toHTML($model, "knjd132vForm1.html", $arg);
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
            $minusHasu = (int)$gyou % 5;
            $minus = ((int)$gyou / 5) > 1 ? ((int)$gyou / 5) * 6 : 5;
        }
        $height = (int)$gyou * 13.5 + ((int)$gyou -1) * 3 + (5 - ($minus + $minusHasu));
        $extra = "style=\"height:".$height."px;\" onPaste=\"return showPaste(this);\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ((int)$moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" onPaste=\"return showPaste(this);\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ((int)$moji * 2), $moji, $extra);
    }
    return $retArg;
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
    //部活動参照
    $extra = "onclick=\"return btn_submit('subform1', '', '', '');\"";
    $arg["btn_club"] = knjCreateBtn($objForm, "btn_club", "部活動参照", $extra);
    //委員会参照
    $extra = "onclick=\"return btn_submit('subform2', '', '', '');\"";
    $arg["btn_committee"] = knjCreateBtn($objForm, "btn_committee", "委員会参照", $extra);
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
