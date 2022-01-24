<?php

require_once('for_php7.php');


// kanji=漢字
class knjd210cForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd210cindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //権限チェック:更新可
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //年度学期表示
        $arg["YEAR"] = CTRL_YEAR . "年度";

        //学期コンボ作成
        $query = knjd210cQuery::getSemester();
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //学年末は今学期とする
        $model->seme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //処理学年
        $query = knjd210cQuery::getGrade($model->seme);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], "", 1);

        //テスト
        $query = knjd210cQuery::getTest($model);
        makeCmb($objForm, $arg, $db, $query, "TESTKIND", $model->field["TESTKIND"], "", 1);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd210cForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //実行ボタン
    $extra = "onclick=\"return btn_submit('execute');\"";
    $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
}

?>
