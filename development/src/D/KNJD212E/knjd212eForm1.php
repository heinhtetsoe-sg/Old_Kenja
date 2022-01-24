<?php

require_once('for_php7.php');


//ビュー作成用クラス
class knjd212eForm1
{
    function main(&$model)
    {
        $db = Query::dbCheckOut();
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd212eindex.php", "", "main");

        //権限チェック(更新可、制限付き更新可)
        if (AUTHORITY != DEF_UPDATABLE && AUTHORITY != DEF_UPDATE_RESTRICT){
            $arg["jscript"] = "OnAuthError();";
        }

        //年度学期表示
        $arg["YEAR"] = CTRL_YEAR;

        //校種
        $query = knjd212eQuery::getSchoolKind();
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1);

        //学年
        $query = knjd212eQuery::getGrade($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1);

        //学期
        $query = knjd212eQuery::getSemester();
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1);

        //考査種別
        $query = knjd212eQuery::getTestcd($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->testcd, "TESTCD", $extra, 1);

        //実行ボタン
        $extra = "onclick=\"return btn_submit('execute');\"";
        $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd212eForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size)
{
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "SEMESTER") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
