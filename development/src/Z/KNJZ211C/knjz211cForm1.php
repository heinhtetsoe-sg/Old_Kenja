<?php

require_once('for_php7.php');

class knjz211cForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz211cindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ
        $query = knjz211cQuery::getSchKind($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->schoolkind, $extra, 1);

        //一覧取得
        $query = knjz211cQuery::getJviewstatLevelPatternYmst($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            //リンク
            $row["PATTERN_SHOW"] = View::alink("knjz211cindex.php", $row["PATTERN_CD"].'：'.$row["PATTERN_NAME"], "target=\"right_frame\"",
                                         array("PATTERN_CD"         => $row["PATTERN_CD"],
                                               "SCHOOL_KIND"        => $row["SCHOOL_KIND"],
                                               "ASSESSLEVEL_CNT"    => $row["ASSESSLEVEL_CNT"],
                                               "cmd"                => "edit"));

            $arg["data"][] = $row;
        }
        $result->free();

        //権限
        $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : " disabled";

        //前年度コピーボタン
        $extra = "onclick=\"return btn_submit('copy')\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra.$disable);

        //存在チェック
        $thisCnt    = $db->getOne(knjz211cQuery::checkExistsYmst(CTRL_YEAR, $model->schoolkind));
        $preCnt     = $db->getOne(knjz211cQuery::checkExistsYmst((CTRL_YEAR - 1), $model->schoolkind));
        knjCreateHidden($objForm, "THIS_YEAR_CNT", $thisCnt);
        knjCreateHidden($objForm, "PRE_YEAR_CNT", $preCnt);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::post("cmd") == "change" || $model->cmd == "change") {
            unset($model->pattern_cd);
            $arg["reload"] = "window.open('knjz211cindex.php?cmd=edit&SCHOOL_KIND={$model->schoolkind}','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz211cForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
