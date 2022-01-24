<?php

require_once('for_php7.php');

class knjp746aForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm        = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjp746aindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = $model->year;

        //結果表示
        if ($model->cmd == "resultMain") {
            $arg["RESULT_MSG"] = sprintf("合計件数：%4d件　合計金額：%11s円", $model->totalCnt, number_format($model->totalIncome));
        }

        //校種コンボ
        $query = knjp746aQuery::getSchkind($model);
        $extra = " onchange=\"return btn_submit('main')\" ";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->schoolKind, $extra, 1, "");

        //入金日
        $query = knjp746aQuery::getPaidDate($db, $model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "PAID_DATE", $model->paidDate, $extra, 1, "");

        //ボタン作成
        //実行
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_upd"] = knjCreateBtn($objForm, "btn_upd", "実 行", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp746aForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value != "" && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
