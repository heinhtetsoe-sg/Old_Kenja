<?php

require_once('for_php7.php');

class knjz210nForm1 {

    function main(&$model) {

        $arg["reload"] = "";

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz210nindex.php", "", "edit");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjz210nQuery::getYear();
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1);

        //前年度からのコピーボタン
        $extra = "onclick=\"return btn_submit('copy')\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からのコピー", $extra);

        if ($model->cmd == "combo") {
            unset($model->grade);
            unset($model->ibprg_course);
            unset($model->classcd);
            unset($model->school_kind);
            unset($model->curriculum_cd);
            unset($model->subclasscd);
            $model->field = array();
            $model->field2 = array();
        }

        //リスト作成
        makeList($arg, $db, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //左のフレームを表示し終わってから右のフレームを表示しないとセッションの値がレスポンスのタイミングによって変わる
        //indexの「分割フレーム作成」では右フレームを呼ばない。
        if (VARS::get("shori") != "add") {
            $arg["reload"] = "window.open('knjz210nindex.php?cmd=edit','right_frame')";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz210nForm1.html", $arg); 
    }
}

//リスト作成
function makeList(&$arg, $db, $model) {
    $bifKey1 = $bifKey2 = "";
    $query = knjz210nQuery::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        //科目
        if ($row["CLASSCD"] == "00") $row["SUBCLASSNAME"] = "基本設定";
        $row["SUBCLASS_SHOW"] = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"].' '.$row["SUBCLASSNAME"];

        //列結合
        if ($bifKey1 !== $row["GRADE"]) {
            $cnt1 = $db->getOne(knjz210nQuery::getRowDataCnt($row, "1"));
            $row["ROWSPAN1"] = $cnt1 > 0 ? $cnt1 : 1;
        }
        if ($bifKey2 !== $row["GRADE"].'-'.$row["IBPRG_COURSE"]) {
            $cnt2 = $db->getOne(knjz210nQuery::getRowDataCnt($row, "2"));
            $row["ROWSPAN2"] = $cnt2 > 0 ? $cnt2 : 1;
        }
        $bifKey1 = $row["GRADE"];
        $bifKey2 = $row["GRADE"].'-'.$row["IBPRG_COURSE"];

        //更新後この行が画面の先頭に来るようにする
        if ($model->grade == $row["GRADE"] && $model->ibprg_course == $row["IBPRG_COURSE"]
         && $model->classcd == $row["CLASSCD"] && $model->school_kind == $row["SCHOOL_KIND"]
         && $model->curriculum_cd == $row["CURRICULUM_CD"] && $model->subclasscd == $row["SUBCLASSCD"]) {
            $row["SEQ_CNT"] = ($row["SEQ_CNT"]) ? $row["SEQ_CNT"] : "　";
            $row["SEQ_CNT"] = "<a name=\"target\">{$row["SEQ_CNT"]}</a><script>location.href='#target';</script>";
        }

        $arg["data"][] = $row;
    }
    $result->free();
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
