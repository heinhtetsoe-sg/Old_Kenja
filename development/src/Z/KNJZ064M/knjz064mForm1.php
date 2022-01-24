<?php

require_once('for_php7.php');

class knjz064mForm1 {

    function main(&$model) {

        $arg["jscript"] = "";

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz064mindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ
        $query = knjz064mQuery::getSchkind($model);
        $extra = "onchange=\"return btn_submit('changeKind');\"";
        makeCmb($objForm, $arg, $db, $query, "GAKUBU_SCHOOL_KIND", $model->gakubu_school_kind, $extra, 1);

        //前年度からコピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //一覧表示
        $key = "";
        $list = array();
        $sep = "";
        $showList = false;
        $query = knjz064mQuery::getList($model, "", "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $showList = true;

            //列結合
            if ($key !== $row["CONDITION"].'-'.$row["GROUPCD"].'-'.$row["GHR_CD"].'-'.$row["GRADE"].'-'.$row["HR_CLASS"]) {
                if ($key !== "") {
                    $arg["data"][] = $list;
                }

                $list = array();
                $sep = "";

                $cnt = $db->getOne(knjz064mQuery::getList($model, $row["CONDITION"], $row["GROUPCD"]));
                $list["ROWSPAN"] = $cnt > 0 ? $cnt : 1;

                //状態区分
                $list["CONDITION_NAME"] = $db->getOne(knjz064mQuery::getCondition($row["CONDITION"]));

                $list["GHR_CD"] = $row["GHR_CD"];
                $list["GRADE"] = $row["GRADE"];
                $list["HR_CLASS"] = $row["HR_CLASS"];
                $list["CONDITION"] = $row["CONDITION"];
                $list["GROUPCD"] = $row["GROUPCD"];
                $list["GROUPNAME"] = $row["GROUPNAME"];
            }

            $list["SUBCLASS"] .= $sep.$row["SUBCLASS"];
            $sep = "<br>";

            $key = $row["CONDITION"].'-'.$row["GROUPCD"].'-'.$row["GHR_CD"].'-'.$row["GRADE"].'-'.$row["HR_CLASS"];
        }
        if ($showList) $arg["data"][] = $list;

        //Hidden作成
        knjCreateHidden($objForm, "cmd");

        //左のフレームを表示し終わってから右のフレームを表示しないとセッションの値がレスポンスのタイミングによって変わる
        //indexの「分割フレーム作成」では右フレームを呼ばない。
        if (VARS::get("shori") != "update") {
            if ($model->cmd == "changeKind") {
                $model->condition = "";
                $model->groupcd = "";
            }
            $arg["jscript"] = "window.open('knjz064mindex.php?cmd=edit','right_frame')";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz064mForm1.html", $arg);
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

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
