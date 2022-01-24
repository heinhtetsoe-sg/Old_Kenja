<?php

require_once('for_php7.php');
class knjp909Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm        = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjp909index.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        // 学年進行処理チェック
        $query = knjp909Query::checkClassFormationDat($model);
        $checkFormation = $db->getOne($query);
        if (1 > $checkFormation) {
            $arg["closeCheck"] = " closeCheck(); ";
        }

        //年度
        $arg["YEAR"] = $model->year;

        //学年コンボ
        $query = knjp909Query::getGdat($model);
        $extra = " onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->grade, $extra, 1, "");

        //校種
        $query = knjp909Query::getGdat($model, $model->grade);
        $model->schoolKind = $db->getOne($query);

        //実行履歴あるか
        $query = knjp909Query::getLevyCloseGradeDat($model);
        $model->close = $db->getOne($query);
        $disPrint = ($model->close == '1') ? "": " disabled";
        if ($model->grade == '99') {
            $disPrint = ' disabled';
        }

        //端数項目コンボ
        $query = knjp909Query::getHasuuMeisaiDat($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "HASUU_CD", $model->hasuuCd, $extra, 1, "");

        //最終端数振替先コンボ
        $query = knjp909Query::getHasuuLMcd($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "HASUU_FURIKAE_CD", $model->hasuuFrikaeCd, $extra, 1, "");

        // 返金日
        $value = ($value == '') ? str_replace("-", "/", CTRL_DATE): $model->henkinDate;
        $arg["data"]["HENKIN_DATE"] = View::popUpCalendarAlp($objForm, "HENKIN_DATE", $value, $disabled.$disPrint, "");

        //科目項目マスタ情報取得
        $model->levyLMarray = array();
        $query = knjp909Query::getLevyLMdat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->levyLMdat[$row["LM_CD"]] = $row;
        }

        //ボタン作成
        //実行
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_upd"] = knjCreateBtn($objForm, "btn_upd", "実 行", $extra);
        //印刷
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra.$disPrint);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJP909");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "SCHOOLCD", (sprintf("%012d", SCHOOLCD)));
        knjCreateHidden($objForm, "SCHOOL_KIND", $model->schoolKind);
        knjCreateHidden($objForm, "useFormKNJP909", $model->Properties["useFormKNJP909"]);
        //給付対象使用するか
        knjCreateHidden($objForm, "useBenefit", $model->Properties["useBenefit"]);
        //予算実績管理を使用するか
        knjCreateHidden($objForm, "LevyBudget", $model->Properties["LevyBudget"]);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp909Form1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($name == "GRADE") {
        $opt[] = array("label" => "-- 全て --", "value" => "99");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
