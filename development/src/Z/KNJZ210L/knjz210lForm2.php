<?php

require_once('for_php7.php');

class knjz210lForm2 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz210lindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if (!isset($model->warning) && $model->cmd != "edit2" && $model->cmd != "level" && $model->ibyear && $model->ibgrade && $model->ibclasscd != "" && $model->ibprg_course != "" && $model->ibcurriculum_cd != "" && $model->ibsubclasscd != "") {
            $query = knjz210lQuery::getIBSubclassGradeAssessYmst($model->ibyear, $model->ibgrade, $model->ibclasscd, $model->ibprg_course, $model->ibcurriculum_cd, $model->ibsubclasscd, "", "row");
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //学年コンボ
        $query = knjz210lQuery::getIBGrade($model, "list");
        $extra = "onchange=\"return btn_submit('edit2');\"";
        makeCmb($objForm, $arg, $db, $query, "IBGRADE", $Row["IBGRADE"], $extra, 1);

        //IBコースコンボ
        $query = knjz210lQuery::getIBPrgCourse($model, "list");
        $extra = "onchange=\"return btn_submit('edit2');\"";
        makeCmb($objForm, $arg, $db, $query, "IBPRG_COURSE", $Row["IBPRG_COURSE"], $extra, 1);

        //IB科目コンボ
        $query = knjz210lQuery::getIBSubclasscd($model, $Row["IBPRG_COURSE"], "list");
        $value = $Row["IBCLASSCD"].'-'.$Row["IBPRG_COURSE"].'-'.$Row["IBCURRICULUM_CD"].'-'.$Row["IBSUBCLASSCD"];
        $extra = "onchange=\"return btn_submit('edit2');\"";
        makeCmb($objForm, $arg, $db, $query, "IBSUBCLASS", $value, $extra, 1, "BLANK");

        $disabled = ($Row["IBCLASSCD"] == "") ? "disabled " : "";

        //データ件数
        $query = knjz210lQuery::getIBSubclassGradeAssessYmst($model->ibyear, $Row["IBGRADE"], $Row["IBCLASSCD"], $Row["IBPRG_COURSE"], $Row["IBCURRICULUM_CD"], $Row["IBSUBCLASSCD"], "", "cnt");
        $max_level = $db->getOne($query);
        $max = ($max_level > 0) ? $max_level : "";

        //Grade
        $model->field["MAX_GRADE_LEVEL"] = (($model->cmd != "level" && $model->cmd != "check") || $model->field["MAX_GRADE_LEVEL"] == "") ? $max : $model->field["MAX_GRADE_LEVEL"];
        $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
        $arg["MAX_GRADE_LEVEL"] = knjCreateTextBox($objForm, $model->field["MAX_GRADE_LEVEL"], "MAX_GRADE_LEVEL", 3, 10, $disabled.$extra);

        //確定ボタン
        $extra = "onclick=\"return level('{$max}');\"";
        $arg["button"]["btn_level"] = knjCreateBtn($objForm, "btn_level", "確 定", $disabled.$extra);

        //一覧表示
        for ($i = 1; $i <= $model->field["MAX_GRADE_LEVEL"]; $i++) {
            //データ取得
            $query = knjz210lQuery::getIBSubclassGradeAssessYmst($model->ibyear, $Row["IBGRADE"], $Row["IBCLASSCD"], $Row["IBPRG_COURSE"], $Row["IBCURRICULUM_CD"], $Row["IBSUBCLASSCD"], $i, "list");
            $Row2 = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row2["GRADE_LEVEL"] = $i;

            //下限
            if ($i == 1) {
                $Row2["GRADE_LOW"] = ($Row2["GRADE_LOW"] == "") ? "1" : sprintf("%d", $Row2["GRADE_LOW"]);
            } else {
                $Row2["GRADE_LOW"] = ($Row2["GRADE_LOW"] == "") ? "" : sprintf("%d", $Row2["GRADE_LOW"]);
            }

            //上限
            if ($model->cmd == "check") $Row2["GRADE_HIGH"] = $model->field2["GRADE_HIGH_".$i];
            $value = ($Row2["GRADE_HIGH"] == "") ? "" : sprintf("%d", $Row2["GRADE_HIGH"]);
            $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
            $Row2["GRADE_HIGH"] = knjCreateTextBox($objForm, $value, "GRADE_HIGH_".$i, 3, 6, $extra);

            $arg["data"][] = $Row2;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && $model->cmd != "edit2" && $model->cmd != "level") {
            $arg["reload"] = "window.open('knjz210lindex.php?cmd=list&shori=add','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz210lForm2.html", $arg);
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
function makeBtn(&$objForm, &$arg, $model) {
    //ボタンの有効
    $disabled = ($model->field["MAX_GRADE_LEVEL"] > 0) ? "" : "disabled";

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
