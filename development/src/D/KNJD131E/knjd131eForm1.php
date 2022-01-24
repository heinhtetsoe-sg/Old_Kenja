<?php

require_once('for_php7.php');

class knjd131eForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd131eindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学期コンボ
        $query = knjd131eQuery::getSemester();
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $row = $db->getRow(knjd131eQuery::getTrainRow($model->schregno, $model),DB_FETCHMODE_ASSOC);
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
        $arg["data"]["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 10, 21, "soft", $extra_spe, $row["SPECIALACTREMARK"]);

        //ＣＳＶ処理
        $fieldSize  = "SPECIALACTREMARK=300";

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);

        //hidden
        makeHidden($objForm, $model);

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent2(0);";
        } else if ($model->cmd == "clear") {
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

        View::toHTML($model, "knjd131eForm1.html", $arg);
    }
}

//Hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SCHREGNOS");
    knjCreateHidden($objForm, "PRGID", "KNJD131E");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "GRADE_HR_CLASS");
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db) {
    //部活動参照
    $extra = "onclick=\"return btn_submit('subform1');\"";
    $arg["button"]["btn_club"] = knjCreateBtn($objForm, "btn_club", "部活動参照", $extra);
    //委員会参照
    $extra = "onclick=\"return btn_submit('subform2');\"";
    $arg["button"]["btn_committee"] = knjCreateBtn($objForm, "btn_committee", "委員会参照", $extra);

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
    $extra = " onClick=\" wopen('".REQUESTROOT."/X/KNJX154E/knjx154eindex.php?FIELDSIZE=".$fieldSize."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //プレビュー／印刷
    if ($model->Properties["tutihyoShokenPreview"] == '1') {
        $arg["data"]["PATARN_ROW"] = "2";
        $arg["useStaffcdFieldSize"] = "1";
        //radio
        $opt = array(1, 2, 3, 4, 5);
        $model->field["PATARN_DIV"] = ($model->field["PATARN_DIV"] == "") ? "1" : $model->field["PATARN_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"PATARN_DIV{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "PATARN_DIV", $model->field["PATARN_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //hidden
        knjCreateHidden($objForm, "FORMNAME");

        $extra = "onclick=\"return newwin('".SERVLET_URL."');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    }
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

?>
