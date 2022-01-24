<?php

require_once('for_php7.php');

class knjl050mForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl050mindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //確定
        if ($model->cmd == 'kakutei') {
            $query = knjl050mQuery::getEntexamControlDat($model);
            $entexam_control_dat = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($entexam_control_dat)) {
                $model->setMessage("MSG300");
            } else {
                knjl050mQuery::insertEntexamControlDat($model, $db);
                $model->kakutei = true;
            }
        }

        //科目コンボ
        $query = knjl050mQuery::cntHallDat($model);
        $hallCnt = $db->getOne($query);
        $opt = array();
        $value_flg = false;
        $query = knjl050mQuery::getSubclassDetail($model, $hallCnt);
        $extra = "style=\"font-size:27;\" onchange=\"btn_submit('main')\"";
        if ($model->kakutei) {
            $extra .= " disabled";
            $name = "HIDDEN_TESTPAPERCD";
            knjCreateHidden($objForm, "TESTPAPERCD", $model->field["TESTPAPERCD"]);
        } else {
            $name = "TESTPAPERCD";
        }
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTPAPERCD"], $name, $extra, $size, "BLANK", $model);

        //会場コンボ
        $opt = array();
        $value_flg = false;
        $query = knjl050mQuery::getHallDat($model);
        $extra = "style=\"font-size:27;\" onchange=\"btn_submit('main')\"";
        if ($model->kakutei) {
            $extra .= " disabled";
            $name = "HIDDEN_EXAMHALLCD";
            knjCreateHidden($objForm, "EXAMHALLCD", $model->field["EXAMHALLCD"]);
        } else {
            $name = "EXAMHALLCD";
        }
        makeCmb($objForm, $arg, $db, $query, $model->field["EXAMHALLCD"], $name, $extra, $size, "BLANK", $model);

        //対象受験番号
        $query = knjl050mQuery::getHallDat($model, $model->field["EXAMHALLCD"]);
        $exzam = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $query = knjl050mQuery::getReceptMaxMin($model, $exzam["S_RECEPTNO"], $exzam["E_RECEPTNO"]);
        $exzam2 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->sReceptno = $exzam2["S_RECEPTNO"];
        $model->eReceptno = $exzam2["E_RECEPTNO"];

        $model->receptno = $model->cmd == "main" ? "" : $model->receptno;
        $model->receptno = $model->receptno ? $model->receptno : $model->sReceptno;

        $checkReceptNo = 0;
        if ($model->cmd == 'upBack' || $model->cmd == 'back') {
            $checkReceptNo = $model->receptno - 1;
        } else if ($model->cmd == 'upNext' || $model->cmd == 'next') {
            $checkReceptNo = $model->receptno + 1;
        } else {
            $checkReceptNo = $model->receptno;
        }

        $query = knjl050mQuery::getReceptNo($model, sprintf("%05d", $checkReceptNo));
        $checkReceptNo = $db->getOne($query);

        if ($checkReceptNo < $model->sReceptno || $checkReceptNo > $model->eReceptno) {
            if ($model->cmd == 'upBack' || $model->cmd == 'upNext') {
                $model->setWarning("MSG303","更新しましたが、次のデータが存在しません。");
            } else if ($model->cmd == 'back' || $model->cmd == 'next') {
                $model->setWarning("MSG303");
            }
        } else {
            $model->receptno = $checkReceptNo;
        }
        $model->receptno  = sprintf("%05d", $model->receptno);
        $model->sReceptno = sprintf("%05d", $model->sReceptno);
        $model->eReceptno = sprintf("%05d", $model->eReceptno);

        if ((AUTHORITY == DEF_UPDATABLE && !$model->field["TESTPAPERCD"]) ||
            (AUTHORITY != DEF_UPDATABLE && (!$model->field["TESTPAPERCD"] || !$model->field["EXAMHALLCD"]))
        ) {
            $model->receptno = "";
            $model->sReceptno = "";
            $model->eReceptno = "";
        }

        if (!$model->kakutei) {
            $model->receptno = "";
        }

        $arg["data"]["RECEPTNO"] = preg_replace('/^0*/', '', $model->receptno);
        if (strlen($model->field["EXAMHALLCD"])) {
            $arg["data"]["SRECEPTNO"] = preg_replace('/^0*/', '', $model->sReceptno);
            $arg["data"]["ERECEPTNO"] = preg_replace('/^0*/', '', $model->eReceptno);
        }

        //得点
        $query = knjl050mQuery::getScore($model);
        $score = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $extra = "style=\"font-size:100;text-align:right;\" onblur=\"this.value=toInteger(this.value); valCheck(this.value)\";";
        $arg["data"]["SCORE"] = knjCreateTextBox($objForm, $score["SCORE"], "SCORE", 3, 3, $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);
        knjCreateHidden($objForm, "RECEPTNO", $model->receptno);
        knjCreateHidden($objForm, "SRECEPTNO", $model->sReceptno);
        knjCreateHidden($objForm, "ERECEPTNO", $model->eReceptno);
        if ($model->kakutei) {
            knjCreateHidden($objForm, "kakutei", 'true');
        }

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl050mForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $model) {
    if ($name == 'EXAMHALLCD' && !strlen($model->field["TESTPAPERCD"])) {
        $opt[] = array("label" => "", "value" => "");
    } else {
        $result = $db->query($query);
        $opt[] = array("label" => "", "value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //確定
    if ($model->field["EXAMHALLCD"] && $model->field["TESTPAPERCD"] && !$model->kakutei) {
        $extra = "onClick=\"btn_submit('kakutei')\" style='float:right; margin-right:10px;font-size:20;'";
    } else {
        $extra = "disabled style='float:right; margin-right:10px;font-size:20;'";
    }
    $arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確定", $extra);

    //完了
    if ($model->field["EXAMHALLCD"] && $model->field["TESTPAPERCD"] && $model->kakutei) {
        $extra = "onclick=\"kanryou()\" style='font-size:40;'";
    } else {
        $extra = "disabled style='font-size:40;'";
    }
    $arg["button"]["btn_kanryou"] = knjCreateBtn($objForm, "btn_kanryou", "完了", $extra);

    //先頭 ボタン
    $extra = "style=\"font-size:40;\" onclick=\"return btn_submit('first_search');\"";
    $arg["button"]["btn_first_search"] = knjCreateBtn($objForm, "btn_first_search", "先頭", $extra);

    //更新ボタン(更新後前の志願者)
    $extra = "style=\"font-size:40;\" onclick=\"return btn_submit('upBack');\"";
    $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "戻る", $extra);

    //更新ボタン(更新後次の志願者)
    $extra = "style=\"font-size:40;\" onclick=\"return btn_submit('upNext');\"";
    $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "進む", $extra);

    //終了ボタン
    $extra = "style=\"font-size:40;\" onclick=\"alert_close();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終了", $extra);
}
?>