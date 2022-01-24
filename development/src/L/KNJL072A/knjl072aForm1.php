<?php

require_once('for_php7.php');

class knjl072aForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg["start"] = $objForm->get_start("main", "POST", "knjl072aindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダ--------------------------------------------

        //入試年度
        $arg["CTRL_YEAR"] = $model->year ."年度";

        //受験校種コンボ,
        $query = knjl072aQuery::getName($model->year, "L003");
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->applicantdiv, "APPLICANTDIV", $extra, 1, "");

        $query = knjl072aQuery::getName($model->year, "L003", $model->applicantdiv);
        $appRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->schoolKind = $appRow["NAMESPARE3"];

        //試験コンボ
        $query = knjl072aQuery::getTestdivMst($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->testdiv, "TESTDIV", $extra, 1, "");

        //受験コースコンボ
        $extra = " onchange=\"return btn_submit('main')\"";
        $namecd1 = 'L'.$model->schoolKind.'58';
        $query = knjl072aQuery::getName($model->year, $namecd1);
        makeCmb($objForm, $arg, $db, $query, $model->wish_course, "WISH_COURSE", $extra, 1, "BLANK");

        //専併区分コンボ
        $extra = " onchange=\"return btn_submit('main')\"";
        $query = knjl072aQuery::getName($model->year, "L006");
        makeCmb($objForm, $arg, $db, $query, $model->shdiv, "SHDIV", $extra, 1, "BLANK");

        //選択した受験コース、専併区分で、それ以下のランクを一覧画面に表示する
        $model->borderScoreList = array();
        if ($model->wish_course != "" && $model->shdiv != "") {
            $query = knjl072aQuery::getRank($model, $namecd1);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->borderScoreList[] = $row;
            }
            $result->free();
        }

        //リストを画面に表示
        $simDis = " disabled ";
        foreach ($model->borderScoreList as $key => $row) {
            //合格コース名
            $setRow["PASS_COURSE_NAME"] = $row["COURSE_NAME"];

            //専併区分名
            $setRow["PASS_SHDIV_NAME"] = $row["SHDIV_NAME"];

            //上限点
            $extra = "style=\"text-align:right\" onchange=\"document.forms[0].btn_decision.disabled = true;\" onblur=\"this.value=toInteger(this.value);\"";
            $row["JOGEN_SCORE"] = ($model->cmd == "simShow" || $model->cmd == "decisionShow") ? $model->jogen_score[$key] : "";
            $setRow["JOGEN_SCORE"] = knjCreateTextBox($objForm, $row["JOGEN_SCORE"], "JOGEN_SCORE"."-".$key, 5, 3, $extra);

            //下限点
            $extra = "style=\"text-align:right\" onchange=\"document.forms[0].btn_decision.disabled = true;\" onblur=\"this.value=toInteger(this.value);\"";
            $row["KAGEN_SCORE"] = ($model->cmd == "simShow" || $model->cmd == "decisionShow") ? $model->kagen_score[$key] : "";
            $setRow["KAGEN_SCORE"] = knjCreateTextBox($objForm, $row["KAGEN_SCORE"], "KAGEN_SCORE"."-".$key, 5, 3, $extra);

            //合格者／対象者数
            if ($model->cmd == "simShow" || $model->cmd == "decisionShow") {
                $query = knjl072aQuery::selectQuerySuccessCnt($model, $row["SHDIV"], $row["COURSE"]);
                $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $setRow["SUCCESS_CNT"] = $passingRow["SUCCESS_CNT"];
            }

            $arg["dataDesirediv"][] = $setRow;
            $simDis = "";
        }

        //合格者／対象者合計、不合格者数
        $decishionDis = " disabled ";
        if ($model->cmd == "simShow" || $model->cmd == "decisionShow") {
            $query = knjl072aQuery::selectQuerySuccessCnt($model, "ALL", "");
            $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $arg["sum"]["SUCCESS_CNT_TOTAL"] = $passingRow["SUCCESS_CNT"];
            $arg["sum"]["SUCCESS_CNT_NO_TOTAL"] = $passingRow["SUCCESS_CNT_NO"];
            $decishionDis = "";
        }

        //DB切断
        Query::dbCheckIn($db);

        //シミュレーションボタン
        $extra = "onclick=\"return btn_submit('sim');\"";
        $arg["btn_sim"] = knjCreateBtn($objForm, "btn_sim", "シミュレーション", $extra.$simDis);

        //確定ボタン
        $extra = "onclick=\"return btn_submit('decision');\"";
        $arg["btn_decision"] = knjCreateBtn($objForm, "btn_decision", "確 定", $extra.$decishionDis);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl072aForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
        if ($row["NAMESPARE2"] == '1' && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
