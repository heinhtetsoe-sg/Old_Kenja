<?php

require_once('for_php7.php');

class knjd131iForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd131iindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学期コンボ
        $opt = array();
        $value_flg = false;
        $query = knjd131iQuery::getSemester();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $extra = "onchange=\"return btn_submit('edit');\"";
        $arg["data"]["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $model->field["SEMESTER"], $opt, $extra, 1);

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            if ($model->cmd !== 'torikomi') {
                $row = $db->getRow(knjd131iQuery::getTrainRow($model, $model->schregno),DB_FETCHMODE_ASSOC);
            } else {
                $row =& $model->field;
            }
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
        
        //今年度の部活動、委員会活動を取込
        if ($model->cmd === 'torikomi') {
            $setremark = "";
            $query = knjd131iQuery::getClubCommittee($model, "");
            $result = $db->query($query);
            while ($torikomi = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $setremark .= $torikomi["NAME"].'／'.$torikomi["DETAIL_REMARK"]."\r\n";
            }
            $row["SPECIALACTREMARK"] = $setremark;
        }
        
        //特別活動の記録
        $arg["data"]["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 10, 21, "soft", $extra_spe, $row["SPECIALACTREMARK"]);

        //通信欄
        $arg["data"]["COMMUNICATION"] = knjCreateTextArea($objForm, "COMMUNICATION", 10, 21, "soft", $extra_commu, $row["COMMUNICATION"]);

        //ＣＳＶ処理
        $fieldSize  = "SPECIALACTREMARK=300,";
        $fieldSize .= "COMMUNICATION=300";

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

        View::toHTML($model, "knjd131iForm1.html", $arg);
    }
}

//Hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SCHREGNOS");
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db) {
    //部活動・委員会取込
    $extra = "style=\"color:#1E90FF;font:bold\" onclick=\"return btn_submit('torikomi');\"";
    $arg["button"]["btn_torikomi"] = createBtn($objForm, "btn_torikomi", "部活動・委員会取込", $extra);
    //部活動参照
    $extra = "onclick=\"return btn_submit('subform1');\"";
    $arg["button"]["btn_club"] = createBtn($objForm, "btn_club", "部活動参照", $extra);
    //委員会参照
    $extra = "onclick=\"return btn_submit('subform2');\"";
    $arg["button"]["btn_committee"] = createBtn($objForm, "btn_committee", "委員会参照", $extra);

    //更新
    $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : "disabled";
    $extra = $disable." onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = createBtn($objForm, "btn_update", "更 新", $extra);
    //更新後前後の生徒へ
    if (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) {
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
    } else {
        $extra = "disabled style=\"width:130px\"";
        $arg["button"]["btn_up_pre"] = createBtn($objForm, "btn_up_pre", "更新後前の生徒へ", $extra);
        $arg["button"]["btn_up_next"] = createBtn($objForm, "btn_up_next", "更新後次の生徒へ", $extra);
    }
    //取消
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["btn_reset"] = createBtn($objForm, "btn_reset", "取 消", $extra);
    //csv
    $extra = " onClick=\" wopen('".REQUESTROOT."/X/KNJX154I/knjx154iindex.php?FIELDSIZE=".$fieldSize."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_csv"] = createBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", $extra);
    //一括更新ボタン
    $link  = REQUESTROOT."/D/KNJD131I/knjd131iindex.php?cmd=replace&SCHREGNO=".$model->schregno."&SEMESTER=".$model->field["SEMESTER"];
    $extra = "style=\"width:80px\" onclick=\"Page_jumper('$link');\"";
    $arg["button"]["btn_replace"] = createBtn($objForm, "btn_replace", "一括更新", $extra);
}

//テキストエリア作成
function createTextArea(&$objForm, $name, $rows, $cols, $wrap, $extra, $value) {
    $objForm->ae( array("type"        => "textarea",
                        "name"        => $name,
                        "rows"        => $rows,
                        "cols"        => $cols,
                        "wrap"        => $wrap,
                        "extrahtml"   => $extra,
                        "value"       => $value));
    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra) {
    $objForm->ae( array("type"      => "button",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

//Hidden作成ae
function createHiddenAe($name, $value = "") {
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}
?>
