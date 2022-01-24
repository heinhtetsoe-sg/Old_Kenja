<?php

require_once('for_php7.php');

class knjm440wForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjm440windex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $opt = array();
        $query = knjm440wQuery::getSemesterMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["SEMESTER"] = ($model->field["SEMESTER"]) ? $model->field["SEMESTER"] : CTRL_SEMESTER;
        $extra = "onChange=\"return btn_submit('main');\"";
        $arg["data"]["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $model->field["SEMESTER"], $opt, $extra, 1);

        //テスト種別
        $opt = array();
        $query = knjm440wQuery::getTestcd($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        if($model->Properties["useTsushinSemesKonboHyoji"] != '1') {
            $model->field["TESSTCD"] = ($model->field["TESTCD"]) ? $model->field["TESTCD"] : $opt[0]["value"];
            $extra = "";
            $arg["data"]["TESTCD"] = knjCreateCombo($objForm, "TESTCD", $model->field["TESTCD"], $opt, $extra, 1);
            $arg["data"]["TESTCD_TITLE"] = '<b>テスト種別</b>';
        }

        //ボタン
        $extra = "onClick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "登 録", $extra);

        $extra = "onClick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        $chk_data = $db->getOne(knjm440wQuery::checkSubclassStdPassDat($model));
        knjCreateHidden($objForm, "DATA_CNT", $chk_data);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjm440wForm1.html", $arg);
    }
}
?>
