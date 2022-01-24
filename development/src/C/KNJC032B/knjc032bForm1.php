<?php

require_once('for_php7.php');

class knjc032bForm1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjc032bindex.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $arg["year"] = $model->ctrl_year;

        /* 学期 */
        $query = knjc032bQuery::getSemesterName(CTRL_YEAR, CTRL_SEMESTER);
        $semesterName = $db->getOne($query);
        $arg["semester"] = $semesterName;

        /* 対象学級 */
        $query = knjc032bQuery::selectHrClass($model);
        $extra = "onChange=\"btn_submit('change_class')\";";
        makeCmb($objForm, $arg, $db, $query, "hr_class", "HR_CLASS", $model->field["hr_class"], $extra, 1, "BLANK");

        /* 対象生徒 */
        $query      = knjc032bQuery::selectStudent($model);
        $extra = "onChange=\"btn_submit('change')\";";
        makeCmb($objForm, $arg, $db, $query, "schregno",  "SCHREGNO", $model->field["schregno"], $extra, 1, "BLANK");

        /* タイトル設定 */
        setTitleData($arg, $db);

        /* 編集対象データリスト */
        makeDataList($objForm, $arg, $db, $model);

        /* 累積 */
        $query = knjc032bQuery::selectAttendSemester($model,0);
        $arg["sum_attend"] = $db->getRow($query);
        /* ボタン作成 */
        makeButton($objForm, $arg);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /* hidden要素(cmdをセット)作成 */
        $objForm->ae(createHiddenAe("cmd"));

        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjc032bForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, $name2, $value, $extra, $size, $blank = "")
{
    $opt = array();
    //先頭に空リストをセット
    if ($blank == "BLANK") {
        $opt[]  = array("label" => "", "value" => "");
    }

    $result = $db->query($query);
    while($row  = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $result->free();
    $arg[$name] = createCombo($objForm, $name2, $value, $opt, $extra, $size);
}

//タイトル設定
function setTitleData(&$arg, $db)
{
    $result = $db->query(knjc032bQuery::getSickDiv());
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["TITLE_".$row["VALUE"]] = $row["LABEL"];
    }
    $result->free();
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model) {
    $query      = knjc032bQuery::selectSemesAll();
    $result     = $db->query($query);
    $data       = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();

    //学校マスタ
    $schoolMst = array();
    $query = knjc032bQuery::getSchoolMst();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        foreach ($row as $key => $val) {
            $schoolMst[$key] = $val;
        }
    }
    $result->free();

    $monthCnt = 0;
    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }
            $query  = knjc032bQuery::selectAttendQuery($model, $month, $data[$dcnt]["SEMESTER"], $schoolMst);
            $result = $db->query($query);
            $rowMeisai = array();
            while ($rowMeisai = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($rowMeisai["CONTROL_CODE"] == $rowMeisai["MONTH"]) {
                    if (!isset($model->warning)) {
                        $rowMeisai = makeTextData($rowMeisai, $data[$dcnt]["SEMESTER"], $monthCnt);
                    } else {
                        $rowMeisai = makeErrTextData($model, $rowMeisai["MONTH"], $data[$dcnt]["SEMESTER"], $rowMeisai, $monthCnt);
                    }
                } else {
                    $rowMeisai["MONTH"] = "";
                }
                $arg["attend_data".$data[$dcnt]["SEMESTER"]][] = $rowMeisai;
            }
            $result->free();
            $monthCnt++;
        }
        /* 学期計 */
        $query = knjc032bQuery::selectAttendSemester($model, $data[$dcnt]["SEMESTER"]);
        $result2 = $db->query($query);
        $rowsemes = array();
        while ($rowsemes = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!$model->field["schregno"]) {
                $rowsemes["MONTH_NAME"]    = $db->getOne($query);;
            }
            $arg["sum_semester".$data[$dcnt]["SEMESTER"]][] = $rowsemes;
        }
        $result2->free();
    }
}

//エラー時の編集可能データ
function makeErrTextData($model, $month, $seme, $meisai, $monthCnt) {
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
            $meisai["VIRUS"]         = $model->field["VIRUS"][$key];         //出停日数
            $meisai["MOURNING"]      = $model->field["MOURNING"][$key];      //忌引日数
            $meisai["SICK"]          = $model->field["SICK"][$key];          //病欠
            $meisai["NOTICE"]        = $model->field["NOTICE"][$key];        //事故欠届
            $meisai["NONOTICE"]      = $model->field["NONOTICE"][$key];      //事故欠無
            $meisai["LATEDETAIL"]    = $model->field["LATEDETAIL"][$key];    //遅刻回数
            $meisai["KEKKA_JISU"]    = $model->field["KEKKA_JISU"][$key];    //欠課時数
            $meisai["KEKKA"]         = $model->field["KEKKA"][$key];         //早退回数
        }
    }
    $row = makeTextData($meisai, $seme, $monthCnt);
    return $row;
}

//編集可能データの作成
function makeTextData($row, $seme, $monthCnt)
{
    $row["MONTH"]         = "<input type=\"hidden\" name=\"MONTH[]\" value=\"".$row["MONTH"]."-".$seme."\">";
    $row["MONTH_NAME"]    = $row["MONTH_NAME"];
    $row["APPOINTED_DAY"] = "<input type='hidden' name='APPOINTED_DAY[]' value='{$row["APPOINTED_DAY"]}'>{$row["APPOINTED_DAY"]}"; //締め日

    $setArray = array("LESSON"      => array("SIZE" => 2, "MAXLEN" => 3),  //授業日数
                      "OFFDAYS"     => array("SIZE" => 2, "MAXLEN" => 3),  //休学日数
                      "ABROAD"      => array("SIZE" => 2, "MAXLEN" => 3),  //留学日数
                      "ABSENT"      => array("SIZE" => 2, "MAXLEN" => 3),  //公欠日数
                      "SUSPEND"     => array("SIZE" => 2, "MAXLEN" => 3),  //出停日数
                      "VIRUS"       => array("SIZE" => 2, "MAXLEN" => 3),  //伝染病
                      "MOURNING"    => array("SIZE" => 2, "MAXLEN" => 3),  //忌引日数
                      "SICK"        => array("SIZE" => 2, "MAXLEN" => 3),  //病欠
                      "NOTICE"      => array("SIZE" => 2, "MAXLEN" => 3),  //事故欠届
                      "NONOTICE"    => array("SIZE" => 2, "MAXLEN" => 3),  //事故欠無
                      "LATEDETAIL"  => array("SIZE" => 2, "MAXLEN" => 3),  //遅刻回数
                      "KEKKA_JISU"  => array("SIZE" => 2, "MAXLEN" => 3),  //欠課時数
                      "KEKKA"       => array("SIZE" => 2, "MAXLEN" => 3)   //早退回数
                      );
    foreach ($setArray as $key => $val) {
        if (strlen($row[$key]) == 0) {
            $row["BGCOLOR_MONTH_NAME"] = "bgcolor=yellow";
        }
        $row[$key] = createTextBox(($row[$key] != 0) ?   $row[$key] : "", $key."[]", $val["SIZE"], $val["MAXLEN"], $monthCnt);
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
function createTextBox($data, $name, $size, $maxlen, $monthCnt){
    $objForm = new form;
    $objForm->ae( array("type"      => "text",
                        "name"      => $name,
                        "size"      => $size,
                        "maxlength" => $maxlen,
                        "value"     => $data,
                        "extrahtml" => " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"; onPaste=\"return show(this, ".$monthCnt.");\" ") );
    return $objForm->ge($name);
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //保存ボタン
    $arg["btn_update"] = createBtn($objForm, "btn_update", "保 存", " onclick=\"return btn_submit('update');\"");
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
