<?php

require_once('for_php7.php');

class knjz210nForm2 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz210nindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if (!isset($model->warning) && $model->cmd != "edit2" && $model->year && $model->grade && $model->ibprg_course != "" && $model->classcd != "" && $model->school_kind != "" && $model->curriculum_cd != "" && $model->subclasscd != "") {
            $query = knjz210nQuery::getIBViewCuttingDat($model->year, $model->grade, $model->ibprg_course, $model->classcd, $model->school_kind, $model->curriculum_cd, $model->subclasscd, "", "row");
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //学年コンボ
        $query = knjz210nQuery::getGrade($model, "list");
        $extra = "onchange=\"return btn_submit('edit2');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $Row["GRADE"], $extra, 1);

        //IBコースコンボ
        $query = knjz210nQuery::getIBPrgCourse($model, "list");
        $extra = "onchange=\"return btn_submit('edit2');\"";
        makeCmb($objForm, $arg, $db, $query, "IBPRG_COURSE", $Row["IBPRG_COURSE"], $extra, 1);

        //科目コンボ
        $query = knjz210nQuery::getSubclasscd($model, "list");
        $value = $Row["CLASSCD"].'-'.$Row["SCHOOL_KIND"].'-'.$Row["CURRICULUM_CD"].'-'.$Row["SUBCLASSCD"];
        $extra = "onchange=\"return btn_submit('edit2');\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASS", $value, $extra, 1, "BLANK");

        //一覧表示
        for ($i = 1; $i <= 5; $i++) {
            //データ取得
            $query = knjz210nQuery::getIBViewCuttingDat($model->year, $Row["GRADE"], $Row["IBPRG_COURSE"], $Row["CLASSCD"], $Row["SCHOOL_KIND"], $Row["CURRICULUM_CD"], $Row["SUBCLASSCD"], $i, "list");
            $Row2 = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row2["SEQ"] = $i;

            //到達度下限
            if ($i == 1) {
                $Row2["CUTTING_LOW"] = ($Row2["CUTTING_LOW"] == "") ? "0" : sprintf("%d", $Row2["CUTTING_LOW"]);
            } else {
                $Row2["CUTTING_LOW"] = ($Row2["CUTTING_LOW"] == "") ? "" : $Row2["CUTTING_LOW"];
            }

            //到達度上限
            if ($i == 5) {
                $Row2["CUTTING_HIGH"] = 100;
                knjCreateHidden($objForm, "CUTTING_HIGH_".$i, 100);
            } else {
                if ($model->cmd == "check") $Row2["CUTTING_HIGH"] = $model->field2["CUTTING_HIGH_".$i];
                $value = ($Row2["CUTTING_HIGH"] == "") ? "" : $Row2["CUTTING_HIGH"];
                $extra = "onblur=\"checkDecimal(this)\" STYLE=\"text-align: right\"";
                $Row2["CUTTING_HIGH"] = knjCreateTextBox($objForm, $value, "CUTTING_HIGH_".$i, 4, 4, $extra);
            }

            $arg["data"][] = $Row2;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $Row);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && $model->cmd != "edit2") {
            $arg["reload"] = "window.open('knjz210nindex.php?cmd=list&shori=add','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz210nForm2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $BLANK="") {
    $opt = array();
    $value_flg = false;
    if ($BLANK) $opt[] = array('label' => "", 'value' => "");
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

//ボタン作成
function makeBtn(&$objForm, &$arg, $Row) {
    //ボタンの有効
    $disabled = ($Row["GRADE"] == "" || $Row["IBPRG_COURSE"] == "" || $Row["CLASSCD"] == "") ? "disabled" : "";

    //登録ボタン
    $extra = " onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "登 録", $disabled.$extra);
    //削除ボタン
    $extra = " onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $disabled.$extra);
    //取消ボタン
    $extra = " onclick=\"return btn_submit('reset')\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = " onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
}
?>
