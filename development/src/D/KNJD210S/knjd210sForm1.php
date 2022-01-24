<?php

require_once('for_php7.php');


//ビュー作成用クラス
class knjd210sForm1
{
    function main(&$model)
    {
        //DB接続
        $db = Query::dbCheckOut();

        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjd210sindex.php", "", "main");

        //権限チェック:更新可
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //年度学期表示
        $arg["YEAR"] = CTRL_YEAR . "年度";

        //処理学期
        $extra = "onChange=\"btn_submit('');\" ";
        $query = knjd210sQuery::GetSemester();
        makeCmb($objForm, $arg, $db, $query, $model->seme, "SEMESTER", $extra, 1, "");

        //処理学年
        $extra = "onChange=\"btn_submit('');\" ";
        $query = knjd210sQuery::GetGrade($model);
        makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1, "");

        //学校種別
        $query = knjd210sQuery::GetGrade($model, $model->grade);
        $gradeRow = array();
        $gradeRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->school_kind = $gradeRow["SCHOOL_KIND"];

        //処理種別(成績)
        $extra = "onChange=\"btn_submit('');\" ";
        $query = knjd210sQuery::GetName($model->seme, $model);
        makeCmb($objForm, $arg, $db, $query, $model->exam, "EXAM", $extra, 1, "");

        //講座基準日
        if ($model->chairdate == "") $model->chairdate = str_replace("-", "/", CTRL_DATE);
        $arg["CHAIRDATE"] = View::popUpCalendar($objForm, "CHAIRDATE", $model->chairdate);

        //科目コンボ
        $extra = "";
        $query = knjd210sQuery::getSubclasscd($model);
        makeCmb($objForm, $arg, $db, $query, $model->subclasscd, "SUBCLASSCD", $extra, 1, "");

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
        //学期開始終了日付
        $query = knjd210sQuery::GetSemester($model->seme);
        $semeRow = array();
        $semeRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        knjCreateHidden($objForm, "SDATE", $semeRow["SDATE"]);
        knjCreateHidden($objForm, "EDATE", $semeRow["EDATE"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd210sForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();

    if ($name == "SUBCLASSCD" && AUTHORITY == DEF_UPDATABLE) {
        $opt[] = array("label" => "全て", "value" => "");
    }

    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SUBCLASSCD" && AUTHORITY == DEF_UPDATABLE) {
        $opt[] = array("label" => "総合計（３・５・９）", "value" => "999999");
        if ($value == "999999") $value_flg = true;
    }

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//履歴表示
function makeListRireki(&$objForm, &$arg, $db, &$model) {
    $dummycd = "";
    $dummycd = "00-".$model->school_kind."-00-";
    //履歴一覧
    $query = knjd210sQuery::getListRireki($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["CALC_DATE"] = str_replace("-", "/", $row["CALC_DATE"]);
        $row["CHAIRDATE"] = str_replace("-", "/", $row["CHAIRDATE"]);
        if ($row["SUBCLASSCD"] == $dummycd."ALL") {
            $row["SUBCLASSNAME"] = "全て";
        } else if ($row["SUBCLASSCD"] == $dummycd."999999") {
            $row["SUBCLASSNAME"] = "総合計（３・５・９）";
        } else {
            $row["SUBCLASSNAME"] = $row["SUBCLASSCD"].":".$row["SUBCLASSNAME"];
        }
        $arg['data2'][] = $row;
    }
    $result->free();
}
?>
