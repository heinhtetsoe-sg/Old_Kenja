<?php

require_once('for_php7.php');


//ビュー作成用クラス
class knjd219sForm1
{
    function main(&$model)
    {
        $db = Query::dbCheckOut();
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd219sindex.php", "", "main");

        //権限チェック(更新可、制限付き更新可)
        if (AUTHORITY != DEF_UPDATABLE && AUTHORITY != DEF_UPDATE_RESTRICT){
            $arg["jscript"] = "OnAuthError();";
        }

        //年度学期表示
        $arg["YEAR"] = CTRL_YEAR . "年度";

        //処理学期
        $query = knjd219sQuery::getSemester();
        $extra = "onChange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1, "");

        //処理学年
        $query = knjd219sQuery::getGrade();
        $extra = "onChange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1, "");
        $query = knjd219sQuery::getSchoolKind($model);
        $model->schoolKind = $db->getOne($query);

        //処理種別
        $query = knjd219sQuery::getName($model);
        $extra = "onChange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, $model->exam, "EXAM", $extra, 1, "");

        //科目
        $query = knjd219sQuery::getSubclassCd($model);
        $extra = "onChange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, $model->subclassCd, "SUBCLASSCD", $extra, 1, "");

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

        View::toHTML($model, "knjd219sForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
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
