<?php

require_once('for_php7.php');

class knjc034aForm1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjc034aindex.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $arg["year"] = CTRL_YEAR;

        /* 学期 */
        $query = knjc034aQuery::getSemesterName(CTRL_YEAR, CTRL_SEMESTER);
        $semesterName = $db->getOne($query);
        $arg["semester"] = $semesterName;

        //科目コンボ
        $query = knjc034aQuery::selectSubclassQuery($model);
        $extra = "onChange=\"btn_submit('subclasscd')\";";
        $model->field["CHAIRCD"] = ($model->cmd == "subclasscd") ? "" : $model->field["CHAIRCD"];
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");

        //講座コンボ
        $query = knjc034aQuery::selectChairQuery($model);
        $extra = "onChange=\"btn_submit('chaircd')\";";
        $model->field["SCHREGNO"] = ($model->cmd == "chaircd") ? "" : $model->field["SCHREGNO"];
        makeCmb($objForm, $arg, $db, $query, $model->field["CHAIRCD"], "CHAIRCD", $extra, 1, "BLANK");

        /* 対象生徒 */
        $query      = knjc034aQuery::selectStudent($model);
        $extra = "onChange=\"btn_submit('change')\";";
        makeCmb($objForm, $arg, $db, $query, $model->field["SCHREGNO"], "SCHREGNO", $extra, 1, "BLANK");

        $query = knjc034aQuery::getStudentGrade($model);
        $model->schGrade = $db->getOne($query);
        //hidden
        knjCreateHidden($objForm, "schGrade", $model->schGrade);

        /* 編集対象データリスト */
        makeDataList($objForm, $arg, $db, $model);

        /* 累積 */
        $query = knjc034aQuery::selectAttendSemester($model,0);
        $arg["sum_attend"] = $db->getRow($query);
        /* ボタン作成 */
        makeButton($objForm, $arg);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /* hidden要素(cmdをセット)作成 */
        $objForm->ae(createHiddenAe("cmd"));
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjc034aForm1.html", $arg);
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
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model) {
    $query      = knjc034aQuery::selectSemesAll($model);
    $result     = $db->query($query);
    $data       = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();
    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }
            $query  = knjc034aQuery::selectAttendQuery($model, $month, $data[$dcnt]["SEMESTER"]);
            $result = $db->query($query);
            $rowMeisai = array();
            while ($rowMeisai = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($rowMeisai["CONTROL_CODE"] == $rowMeisai["MONTH"]) {
                    if (!isset($model->warning)) {
                        $rowMeisai = makeTextData($rowMeisai, $data[$dcnt]["SEMESTER"]);
                    } else {
                        $rowMeisai = makeErrTextData($model, $rowMeisai["MONTH"], $data[$dcnt]["SEMESTER"], $rowMeisai);
                    }
                } else {
                    $rowMeisai["MONTH"] = "";
                }
                $arg["attend_data".$data[$dcnt]["SEMESTER"]][] = $rowMeisai;
            }
            $result->free();
        }
        /* 学期計 */
        $query = knjc034aQuery::selectAttendSemester($model, $data[$dcnt]["SEMESTER"]);
        $result2 = $db->query($query);
        $rowsemes = array();
        while ($rowsemes = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!$model->field["SCHREGNO"]) {
                $rowsemes["MONTH_NAME"]    = $db->getOne($query);;
            }
            $arg["sum_semester".$data[$dcnt]["SEMESTER"]][] = $rowsemes;
        }
        $result2->free();
    }
}

//エラー時の編集可能データ
function makeErrTextData($model, $month, $seme, $meisai)
{
    foreach($model->field["MONTH"] as $key => $val){
        //$monthAr[0] = 月、$monthAr[1] = 学期
        $monthAr = preg_split("/-/", $val);
        if ($month == $monthAr[0] && $seme == $monthAr[1]) {
            $meisai["APPOINTED_DAY"] = $model->field["APPOINTED_DAY"][$key]; //締め日
            $meisai["LESSON"]        = $model->field["LESSON"][$key];        //授業日数
            $meisai["OFFDAYS"]       = $model->field["OFFDAYS"][$key];       //休学日数
            $meisai["ABROAD"]        = $model->field["ABROAD"][$key];        //留学日数
            $meisai["ABSENT"]        = $model->field["ABSENT"][$key];        //公欠日数
            $meisai["SUSPEND"]       = $model->field["SUSPEND"][$key];       //出停日数
            $meisai["VIRUS"]         = $model->field["VIRUS"][$key];         //伝染病日数
            $meisai["MOURNING"]      = $model->field["MOURNING"][$key];      //忌引日数
            $meisai["NOTICE"]        = $model->field["NOTICE"][$key];        //事故欠届
            $meisai["NONOTICE"]      = $model->field["NONOTICE"][$key];      //事故欠無
            $meisai["LATE"]          = $model->field["LATE"][$key];          //遅刻回数
            $meisai["EARLY"]         = $model->field["EARLY"][$key];         //早退回数
        }

    }
    $row = makeTextData($meisai, $seme);
    return $row;
}

//編集可能データの作成
function makeTextData($row, $seme)
{
    $row["MONTH"]         = "<input type=\"hidden\" name=\"MONTH[]\" value=\"".$row["MONTH"]."-".$seme."\">";
    $row["MONTH_NAME"]    = $row["MONTH_NAME"];
    $row["APPOINTED_DAY"] = "<input type='hidden' name='APPOINTED_DAY[]' value='{$row["APPOINTED_DAY"]}'>{$row["APPOINTED_DAY"]}"; //締め日
//    $row["APPOINTED_DAY"] = createTextBox($row["APPOINTED_DAY"], "APPOINTED_DAY[]", 2, 2); //締め日

    $setArray = array("LESSON"      => array("SIZE" => 2, "MAXLEN" => 3),               //授業日数
                      "OFFDAYS"     => array("SIZE" => 2, "MAXLEN" => 3),
                      "ABROAD"      => array("SIZE" => 2, "MAXLEN" => 3),
                      "ABSENT"      => array("SIZE" => 2, "MAXLEN" => 3),
                      "SUSPEND"     => array("SIZE" => 2, "MAXLEN" => 3),
                      "VIRUS"       => array("SIZE" => 2, "MAXLEN" => 3),
                      "MOURNING"    => array("SIZE" => 2, "MAXLEN" => 3),
                      "NOTICE"      => array("SIZE" => 2, "MAXLEN" => 3),
                      "NONOTICE"    => array("SIZE" => 2, "MAXLEN" => 3),
                      "LATE"        => array("SIZE" => 2, "MAXLEN" => 3),
                      "EARLY"       => array("SIZE" => 2, "MAXLEN" => 3)
                      );
    foreach ($setArray as $key => $val) {
        if (strlen($row[$key]) == 0) {
            $row["BGCOLOR_MONTH_NAME"] = "bgcolor=yellow";
        }
        $row[$key] = createTextBox(($row[$key] != 0) ?   $row[$key] : "", $key."[]", $val["SIZE"], $val["MAXLEN"]);
    }

    return $row;
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}

/**** テキストボックスを作成する関数    ****
 **** $data … テキストボックスに入る値 ****
 **** $name … テキストボックスの名称   ****/
function createTextBox($data, $name, $size, $maxlen){
    $objForm = new form;
    $objForm->ae( array("type"      => "text",
                        "name"      => $name,
                        "size"      => $size,
                        "maxlength" => $maxlen,
                        "value"     => $data,
                        "extrahtml" => " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"; ") );
    return $objForm->ge($name);
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //保存ボタン
    $arg["btn_update"] = createBtn($objForm, "btn_update", "更 新", " onclick=\"return btn_submit('update');\"");
    //取消ボタン
    $arg["btn_reset"] = createBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"");
    //終了ボタン
    $arg["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ) );
    return $objForm->ge($name);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}
?>
