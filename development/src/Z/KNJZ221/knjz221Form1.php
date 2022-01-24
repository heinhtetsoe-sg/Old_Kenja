<?php

require_once('for_php7.php');

class knjz221Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz221index.php", "", "edit");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjz221Query::getYear();
        $extra = "onChange=\"return btn_submit('list'), reload_window();\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1);

        //データ取得 -- ATTEND_SCORE_CREDIT_MST
        $att_score = array();
        $query = knjz221Query::getAttendScoreCreditMst($model, "left");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $att_score[$row["SEMESTER"]][$row["CREDIT"]][$row["ATTEND_SCORE"]] = $row;
        }
        $result->free();

        //MAX単位数取得
        $query = knjz221Query::getMaxCredit($model);
        $maxCredit = $db->getOne($query);

        //一覧表示
        $setTmp = array();
        $query = knjz221Query::getSemester($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            for ($credit = 1; $credit <= $maxCredit; $credit++) {
                if ($credit == 1) {
                    $setTmp[$row["SEMESTER"]."_".$credit]["SEMESTERNAME"] = $row["SEMESTERNAME"];
                    $setTmp[$row["SEMESTER"]."_".$credit]["ROWSPAN"] = ($maxCredit > 0) ? $maxCredit : 1;
                }

                $setTmp[$row["SEMESTER"]."_".$credit]["YEAR"]       = $model->year;
                $setTmp[$row["SEMESTER"]."_".$credit]["SEMESTER"]   = $row["SEMESTER"];
                $setTmp[$row["SEMESTER"]."_".$credit]["CREDIT"]     = $credit;

                for ($score = 10; $score >= 0; $score--) {
                    $kekka_low  = $att_score[$row["SEMESTER"]][$credit][$score]["KEKKA_LOW"];
                    $kekka_high = $att_score[$row["SEMESTER"]][$credit][$score]["KEKKA_HIGH"];

                    $to = ($score == 0 || $kekka_high != "") ? "～" : "";
                    $setTmp[$row["SEMESTER"]."_".$credit]["KEKKA".$score] = ($kekka_low == $kekka_high) ? $kekka_low : $kekka_low.$to.$kekka_high;
                }
            }
        }
        $result->free();

        foreach ($setTmp as $key => $val) {
             $arg["data"][] = $val;
        }

        //前年度コピーボタン
        $extra = ($model->year && $maxCredit > 0) ? "onclick=\"return btn_submit('copy');\"" : "disabled";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "maxCredit", $maxCredit);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz221Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
