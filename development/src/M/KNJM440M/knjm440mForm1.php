<?php

require_once('for_php7.php');

class knjm440mForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjm440mindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期ラジオボタン
        $semester = "";
        $model->field["SEMESTER"] = ($model->field["SEMESTER"] == "") ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query = knjm440mQuery::getSemesterMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($semester != "") $semester .= "　　";
            $extra  = ($model->field["SEMESTER"] == $row["VALUE"]) ? "checked" : "";
            $extra .= " onclick =\" return btn_submit('main');\"";
            $semester .= "<input type='radio' name='SEMESTER' value={$row["VALUE"]} {$extra} id='SEMESTER{$row["VALUE"]}'><label for='SEMESTER{$row["VALUE"]}'> {$row["LABEL"]}</label>";
        }
        $arg["data"]["SEMESTER"] = $semester;

        //ボタン
        $extra = "onClick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "登 録", $extra);

        $extra = "onClick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        $chk_data = $db->getOne(knjm440mQuery::checkSubclassStdPassDat($model));
        knjCreateHidden($objForm, "DATA_CNT", $chk_data);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjm440mForm1.html", $arg);
    }
}
?>
