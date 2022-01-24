<?php

require_once('for_php7.php');

class knjd415Form1 {

    function main(&$model) {

        $arg["jscript"] = "";

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjd415index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();
        if(!isset($model->year)) $model->year = CTRL_YEAR;

        //年度コンボボックス
        $query = knjd415Query::getYear($model);
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1, $model);

        //前年度データ件数
        $pre_year = $model->year - 1;
        $preYear_cnt = $db->getOne(knjd415Query::getCopyData($pre_year, "cnt"));
        knjCreateHidden($objForm, "PRE_YEAR_CNT", $preYear_cnt);
        //今年度データ件数
        $this_year = $model->year;
        $thisYear_cnt = $db->getOne(knjd415Query::getCopyData($this_year, "cnt"));
        knjCreateHidden($objForm, "THIS_YEAR_CNT", $thisYear_cnt);

        //前年度からコピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //一覧表示
        $key = "";
        $query = knjd415Query::getList($model, "", "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //列結合
            if ($key !== $row["CONDITION"].'-'.$row["CLASSGROUP_CD"]) {
                $cnt = $db->getOne(knjd415Query::getList($model, $row["CONDITION"], $row["CLASSGROUP_CD"]));
                $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
            }

            //状態区分
            $row["CONDITION_NAME"] = $db->getOne(knjd415Query::getCondition($row["CONDITION"]));
            $arg["data"][] = $row;
            $key = $row["CONDITION"].'-'.$row["CLASSGROUP_CD"];
        }

        //Hidden作成
        knjCreateHidden($objForm, "cmd");

        //左のフレームを表示し終わってから右のフレームを表示しないとセッションの値がレスポンスのタイミングによって変わる
        //indexの「分割フレーム作成」では右フレームを呼ばない。
        if (VARS::get("shori") != "update") {
            $arg["jscript"] = "window.open('knjd415index.php?cmd=edit','right_frame')";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd415Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, &$model) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name === 'YEAR') {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
