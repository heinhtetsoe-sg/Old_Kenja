<?php

require_once('for_php7.php');

class knjd414Form1 {

    function main(&$model) {

        $arg["jscript"] = "";

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjd414index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        $model->year = CTRL_YEAR;

        //前年度からコピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //校種コンボ
        $query = knjd414Query::getSchoolKind();
        $extra = "onChange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->school_kind, $extra, 1, $model);

        //教科コンボ
        $query = knjd414Query::getClassMst($model);
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "CLASSCD_LEFT", $model->classcd_left, $extra, 1, $model);

        //段階コンボ
        $query = knjd414Query::getNameMst($model, 'left');
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "STEP_CD_LEFT", $model->step_cd_left, $extra, 1, $model);

        //前年度データ件数
        $pre_year = CTRL_YEAR - 1;
        $preYear_cnt = $db->getOne(knjd414Query::getCopyData($pre_year, "cnt"));
        knjCreateHidden($objForm, "PRE_YEAR_CNT", $preYear_cnt);
        //今年度データ件数
        $this_year = CTRL_YEAR;
        $thisYear_cnt = $db->getOne(knjd414Query::getCopyData($this_year, "cnt"));
        knjCreateHidden($objForm, "THIS_YEAR_CNT", $thisYear_cnt);

        //一覧表示
        $key = "";
        $query = knjd414Query::getList($model, "", "", "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //列結合
            if ($key !== $row["CLASSCD"].'-'.$row["STEP_CD"].'-'.$row["LEARNING_CONTENT_CD"]) {
                $cnt = $db->getOne(knjd414Query::getList($model, $row["CLASSCD"], $row["STEP_CD"], $row["LEARNING_CONTENT_CD"]));
                $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
            }
            $arg["data"][] = $row;
            $key = $row["CLASSCD"].'-'.$row["STEP_CD"].'-'.$row["LEARNING_CONTENT_CD"];
        }

        //Hidden作成
        knjCreateHidden($objForm, "cmd");

        //左のフレームを表示し終わってから右のフレームを表示しないとセッションの値がレスポンスのタイミングによって変わる
        //indexの「分割フレーム作成」では右フレームを呼ばない。
        if (VARS::get("shori") != "update") {
            $arg["jscript"] = "window.open('knjd414index.php?cmd=edit','right_frame')";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd414Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, &$model) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    $opt[] = array('label' => "--全て--",
                   'value' => 'ALL');
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
