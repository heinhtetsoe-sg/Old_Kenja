<?php

require_once('for_php7.php');

class knja126mSubForm4 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform4", "POST", "knja126mindex.php", "", "subform4");

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knja126mQuery::getHTrainRemarkYear($model->schregno);
        $extra = "onchange=\"return btn_submit('subform4');\"";
        makeCmb($objForm, $arg, $db, $query, "R_YEAR", $model->r_year, $extra, 1, $model);

        //生徒情報
        $arg["data"]["SCHREGNO"] = $model->schregno;
        $arg["data"]["NAME"]     = $model->name;

        //備考
        $query = knja126mQuery::getHExamEntRemark($model->schregno, $model->r_year);
        $remark = $db->getOne($query);
        $remark = preg_replace("/\r\n/","",$remark);
        $remark = preg_replace("/\n/","",$remark);
        $remark = preg_replace("/\r/","",$remark);
        $extra = "style=\"height:75px;width:200px;\"";
        $arg["data"]["REMARK"] = knjCreateTextArea($objForm, "REMARK", "", "", "", $extra, $remark);

        //戻るボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja126mSubForm4.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "R_YEAR") {
        $value = ($value && $value_flg) ? $value : $model->exp_year;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
