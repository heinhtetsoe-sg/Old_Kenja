<?php

require_once('for_php7.php');


//ビュー作成用クラス
class knjd212cForm1
{
    function main(&$model)
    {
        //DB接続
        $db = Query::dbCheckOut();

        $objForm = new form;

        $arg["start"] = $objForm->get_start("main", "POST", "knjd212cindex.php", "", "main");

        //権限チェック:更新可
        if (AUTHORITY < DEF_UPDATE_RESTRICT) {
            $arg["jscript"] = "OnAuthError();";
        }

        //年度学期表示
        $arg["YEAR"] = CTRL_YEAR . "年度";

        //処理学期
        $query = knjd212cQuery::getSemester();
        $extra = "onChange=\"btn_submit('');\" ";
        makeCmb($objForm, $arg, $db, $query, $model->semeester, "SEMESTER", $extra, 1, "");

        //学期開始終了日付（講座基準日チェック用）
        $query = knjd212cQuery::getSemester($model->semeester);
        $semeRow = array();
        $semeRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //処理学年
        $query = knjd212cQuery::getGrade($model);
        $extra = "onChange=\"btn_submit('');\" ";
        makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1, "");

        //学校種別
        $query = knjd212cQuery::getGrade($model, $model->grade);
        $gradeRow = array();
        $gradeRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        knjCreateHidden($objForm, "SCHOOL_KIND", $gradeRow["SCHOOL_KIND"]);

        //科目コンボ
        $query = knjd212cQuery::getSubclasscd($model, $gradeRow["SCHOOL_KIND"]);
        $extra = "";
        $model->subclassArray = makeCmb($objForm, $arg, $db, $query, $model->subclasscd, "SUBCLASSCD", $extra, 1, "");

        //履歴表示
        makeListRireki($objForm, $arg, $db, $model);

        //実行ボタン
        $extra = "onclick=\"return btn_submit('execute');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SDATE", str_replace("-", "/", $semeRow["SDATE"]));
        knjCreateHidden($objForm, "EDATE", str_replace("-", "/", $semeRow["EDATE"]));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd212cForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $retArray = array();

    if ($name == "SUBCLASSCD" && AUTHORITY == DEF_UPDATABLE) {
        $opt[] = array("label" => "全て", "value" => "000000");
    } else if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }

    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
        $retArray[$row["VALUE"]] = $row["LABEL"];
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    return $retArray;
}
//履歴表示
function makeListRireki(&$objForm, &$arg, $db, &$model) {
    //履歴一覧
    $query = knjd212cQuery::getListRireki($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["CALC_DATE"] = str_replace("-", "/", $row["CALC_DATE"]);
        $subArray = array();
        $subArray = explode("-", $row["SUBCLASSCD"]);
        if ($subArray[3] == "000000") {
            $row["SUBCLASSNAME"] = "全て";
        } else {
            $row["SUBCLASSNAME"] = $row["SUBCLASSCD"].":".$row["SUBCLASSNAME"];
        }
        $arg['data2'][] = $row;
    }
    $result->free();
}
?>
