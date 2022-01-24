<?php

require_once('for_php7.php');

class knjp904Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjp904Form1", "POST", "knjp904index.php", "", "knjp904Form1");

        //DB接続
        $db = Query::dbCheckOut();

        // マスタ存在チェック
        $query = knjp904Query::checkLevyMst($model, "LEVY_L_MST");
        $checkCnt1 = $db->getOne($query);
        $query = knjp904Query::checkLevyMst($model, "LEVY_M_MST");
        $checkCnt2 = $db->getOne($query);
        $query = knjp904Query::checkLevyMst($model, "LEVY_S_MST");
        $checkCnt3 = $db->getOne($query);
        if (1 > ($checkCnt1 + $checkCnt2 + $checkCnt3)) {
            $arg["closeCheck"] = " closeCheck(); ";
        }

        if ($model->cmd == "clear") unset($model->field);

        //校種コンボ
        $arg["schkind"] = "1";
        $query = knjp904Query::getSchoolKind($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1, "", $model);

        //年度
        $arg["YEAR"] = $model->year."年度";

        //前年度
        $arg["LAST_YEAR"] = $model->lastYear."年度";

        //預り金科目
        $query = knjp904Query::getLevyLMst($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["LEVY_L_CD"], "LEVY_L_CD", $extra, 1, "", $model);

        //去年の情報をセット
        $lastDataArr = array();
        $query = knjp904Query::getLastData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setKey = $row["LEVY_L_CD"].$row["LEVY_M_CD"];

            $lastDataArr[$setKey] = $row;
        }

        //リスト
        $query = knjp904Query::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $lmCd = $row["OUTGO_L_CD"].$row["OUTGO_M_CD"];

            //リンク設定
            $link  = REQUESTROOT."/P/KNJP904_SAIMOKU/knjp904_saimokuindex.php?";
            $link .= "cmd=main";
            $link .= "&SEND_AUTH=".AUTHORITY;
            $link .= "&SEND_PRGID=KNJ904";
            $link .= "&SEND_SCHOOLCD=".$row["SCHOOLCD"];
            $link .= "&SEND_SCHOOL_KIND=".$row["SCHOOL_KIND"];
            $link .= "&SEND_YEAR=".$row["YEAR"];
            $link .= "&SEND_OUTGO_L_CD=".$row["OUTGO_L_CD"];
            $link .= "&SEND_OUTGO_M_CD=".$row["OUTGO_M_CD"];
            $extra = "onClick=\" Page_jumper('{$link}');\"";
            $row["LEVY_M_NAME"] = View::alink("#", htmlspecialchars($row["LEVY_M_NAME"]), $extra);

            //カンマ区切り
            $row["BUDGET_MONEY"]       = number_format($row["BUDGET_MONEY"]);
            $row["LASTYEAR_BUDGET"]    = number_format($lastDataArr[$lmCd]["LASTYEAR_BUDGET"]);
            $row["LASTYEAR_SCH_PRICE"] = number_format($lastDataArr[$lmCd]["LASTYEAR_SCH_PRICE"]);

            $arg["data2"][] = $row;
        }
        $result->free();

        /**************/
        /* ボタン作成 */
        /**************/
        //終了
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp904Form1.html", $arg);
    }
}
//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $model) {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
