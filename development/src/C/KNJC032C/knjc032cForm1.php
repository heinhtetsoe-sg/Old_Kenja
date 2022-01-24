<?php

require_once('for_php7.php');

class knjc032cForm1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjc032cindex.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $arg["year"] = $model->ctrl_year;

        /* 学期 */
        $query = knjc032cQuery::getSemesterName(CTRL_YEAR, CTRL_SEMESTER);
        $semesterName = $db->getOne($query);
        $arg["semester"] = $semesterName;

        /* 対象学級 */
        $query = knjc032cQuery::selectHrClass($model);
        $extra = "onChange=\"btn_submit('change_class')\";";
        makeCmb($objForm, $arg, $db, $query, "hr_class", "HR_CLASS", $model->field["hr_class"], $extra, 1, "BLANK");

        /* 対象生徒 */
        $query      = knjc032cQuery::selectStudent($model);
        $extra = "onChange=\"btn_submit('change')\";";
        makeCmb($objForm, $arg, $db, $query, "schregno",  "SCHREGNO", $model->field["schregno"], $extra, 1, "BLANK");

        //学校マスタ
        $schoolMst = array();
        $query = knjc032cQuery::getSchoolMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($row as $key => $val) {
                $schoolMst[$key] = $val;
            }
        }
        $result->free();

        /* タイトル設定 */
        $kahenField = setTitleData($objForm, $arg, $db, $model);

        /* 編集対象データリスト */
        makeDataList($objForm, $arg, $db, $model, $kahenField, $schoolMst);

        /* 累積 */
        $query = knjc032cQuery::selectAttendSemester($model, 0, $kahenField, $schoolMst);
        $arg["sum_attend"] = $db->getRow($query);
        /* ボタン作成 */
        makeButton($objForm, $arg);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /* hidden要素(cmdをセット)作成 */
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjc032cForm1.html", $arg);
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
    while ($row  = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $result->free();
    $arg[$name] = knjCreateCombo($objForm, $name2, $value, $opt, $extra, $size);
}

//タイトル設定
function setTitleData(&$objForm, &$arg, $db, $model)
{
    $setFieldName = array("4" => "SICK[]", "5" => "NOTICE[]", "6" => "NONOTICE[]");
    $result = $db->query(knjc032cQuery::getSickDiv());
    $setFieldData = "";
    $sep = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["TITLE_".$row["VALUE"]] = $row["LABEL"];
        $rtnField["C001"][] = $row["VALUE"];
        $setFieldData .= $sep.$setFieldName[$row["VALUE"]];
        $sep = ",";
    }
    $result->free();
    //hidden
    knjCreateHidden($objForm, "SET_FIELD", $setFieldData);

    $result = $db->query(knjc032cQuery::getDetailDiv());
    $detailCnt = 1;
    $setFieldData = "";
    $sep = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["DETAIL_TITLE_".$row["VALUE"]] = $row["LABEL"];
        $rtnField["C002"][] = $row["VALUE"];
        $setFieldData .= $sep."DETAIL_".$row["VALUE"]."[]";
        $sep = ",";
        $detailCnt++;
    }
    $arg["DATA_COLSPAN"] = 15 + $detailCnt;
    $arg["RUIKEI_COLSPAN"] = 7 + $detailCnt;
    $result->free();
    //hidden
    knjCreateHidden($objForm, "SET_DETAIL_FIELD", $setFieldData);

    //出停
    $setSuspendData = "";
    $sep = "";
    $arg["TITLE_SUSPEND"] = '出停';
    if ($model->Properties["useKoudome"] == "true") {
        $arg["useKoudome"] = 1;
        $arg["TITLE_SUSPEND"] = '法止';
        $setSuspendData .= "KOUDOME[]";
        $sep = ",";
    }
    if ($model->Properties["useVirus"] == "true") {
        $arg["useVirus"] = 1;
        $setSuspendData .= $sep."VIRUS[]";
    }
    //列の幅
    $arg["width"] = ($model->Properties["useVirus"] == "true" || $model->Properties["useKoudome"] == "true") ? 35 : 40;
    //hidden
    knjCreateHidden($objForm, "SET_SUSPEND_FIELD", $setSuspendData);

    return $rtnField;
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model, $kahenField, $schoolMst) {
    $query      = knjc032cQuery::selectSemesAll();
    $result     = $db->query($query);
    $data       = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();

    //異動月取得
    $smonth = $emonth = array();
    $smonth = $db->getCol(knjc032cQuery::getTransferData2($model->field["schregno"], "s"));
    $emonth = $db->getCol(knjc032cQuery::getTransferData2($model->field["schregno"], "e"));

    $monthCnt = 0;
    $textLineCnt = 0;
    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }
            $query  = knjc032cQuery::selectAttendQuery($model, $month, $data[$dcnt]["SEMESTER"], $schoolMst);
            $result = $db->query($query);
            $rowMeisai = array();
            while ($rowMeisai = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($rowMeisai["CONTROL_CODE"] == $rowMeisai["MONTH"]) {
                    if (!isset($model->warning)) {
                        $rowMeisai = makeTextData($rowMeisai, $data[$dcnt]["SEMESTER"], $monthCnt, $kahenField, $model);
                        $textLineCnt++;
                    } else {
                        $rowMeisai = makeErrTextData($model, $rowMeisai["MONTH"], $data[$dcnt]["SEMESTER"], $rowMeisai, $monthCnt, $kahenField);
                    }
                } else {
                    $rowMeisai["MONTH"] = "";
                }

                //異動者（退学・転学・卒業）
                $idou_year = ($month < '04') ? CTRL_YEAR + 1 : CTRL_YEAR;
                $appointed_day = $db->getOne(knjc032cQuery::getAppointedDay(sprintf('%02d', $month), $data[$dcnt]["SEMESTER"], $model));
                $idou_day = ($appointed_day == "") ? getFinalDay($db, sprintf('%02d', $month), $data[$dcnt]["SEMESTER"]) : $appointed_day;
                $idou_date = $idou_year.'-'.sprintf('%02d', $month).'-'.$idou_day;
                $idou = $db->getOne(knjc032cQuery::getIdouData($model->field["schregno"], $idou_date));

                //異動者（留学・休学）
                $idou2 = $db->getOne(knjc032cQuery::getTransferData1($model->field["schregno"], $idou_date));
                $idou3 = 0;
                if (in_array(sprintf('%02d', $month), $smonth) || in_array(sprintf('%02d', $month), $emonth)) {
                    $idou3 = 1;
                }

                //異動期間は背景色を黄色にする
                $rowMeisai["BGCOLOR_IDOU"] = ($idou > 0 || $idou2 > 0 || $idou3 > 0) ? "bgcolor=yellow" : "";

                //未入力月は背景色を変える
                if (strlen($rowMeisai["SEM_SCHREGNO"]) == 0) {
                    $rowMeisai["BGCOLOR_MONTH_NAME"] = "bgcolor=#ccffcc";
                }

                $arg["attend_data".$data[$dcnt]["SEMESTER"]][] = $rowMeisai;
            }
            $result->free();
            $monthCnt++;
        }
        /* 学期計 */
        $query = knjc032cQuery::selectAttendSemester($model, $data[$dcnt]["SEMESTER"], $kahenField, $schoolMst);
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
    knjCreateHidden($objForm, "objCntSub", $textLineCnt);
}

//エラー時の編集可能データ
function makeErrTextData($model, $month, $seme, $meisai, $monthCnt, $kahenField) {
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
            if ($model->Properties["useKoudome"] == "true") {
                $meisai["KOUDOME"]       = $model->field["KOUDOME"][$key];       //出停（交止）日数
            }
            if ($model->Properties["useVirus"] == "true") {
                $meisai["VIRUS"]         = $model->field["VIRUS"][$key];         //出停（伝染病）日数
            }
            $meisai["MOURNING"]      = $model->field["MOURNING"][$key];      //忌引日数
            $meisai["SICK"]          = $model->field["SICK"][$key];          //病欠
            $meisai["NOTICE"]        = $model->field["NOTICE"][$key];        //事故欠届
            $meisai["NONOTICE"]      = $model->field["NONOTICE"][$key];      //事故欠無
            $meisai["LATE"]          = $model->field["LATE"][$key];          //遅刻回数
            $meisai["EARLY"]         = $model->field["EARLY"][$key];         //早退回数
        }

    }
    $row = makeTextData($meisai, $seme, $monthCnt, $kahenField, $model);
    return $row;
}

//編集可能データの作成
function makeTextData($row, $seme, $monthCnt, $kahenField, $model)
{
    $row["MONTH"]         = "<input type=\"hidden\" name=\"MONTH[]\" value=\"".$row["MONTH"]."-".$seme."\">";
    $row["MONTH_NAME"]    = $row["MONTH_NAME"];
    $row["APPOINTED_DAY"] = "<input type='hidden' name='APPOINTED_DAY[]' value='{$row["APPOINTED_DAY"]}'>{$row["APPOINTED_DAY"]}"; //締め日

    $setArray = array("LESSON"      => array("SIZE" => 2, "MAXLEN" => 3),               //授業日数
                      "OFFDAYS"     => array("SIZE" => 2, "MAXLEN" => 3),
                      "ABROAD"      => array("SIZE" => 2, "MAXLEN" => 3),
                      "ABSENT"      => array("SIZE" => 2, "MAXLEN" => 3),
                      "SUSPEND"     => array("SIZE" => 2, "MAXLEN" => 3),
                      "MOURNING"    => array("SIZE" => 2, "MAXLEN" => 3),
                      "LATE"        => array("SIZE" => 2, "MAXLEN" => 3),
                      "EARLY"       => array("SIZE" => 2, "MAXLEN" => 3)
                      );

    if ($model->Properties["useVirus"] == "true") {
        $setArray["VIRUS"] = array("SIZE" => 2, "MAXLEN" => 3);
    }
    if ($model->Properties["useKoudome"] == "true") {
        $setArray["KOUDOME"] = array("SIZE" => 2, "MAXLEN" => 3);
    }

    //C001可変
    $setFieldName = array("4" => "SICK", "5" => "NOTICE", "6" => "NONOTICE");
    $kahenField["C001"] = is_array($kahenField["C001"]) ? $kahenField["C001"] : array();
    foreach ($kahenField["C001"] as $c001Key => $c001Val) {
        $setArray[$setFieldName[$c001Val]] = array("SIZE" => 2, "MAXLEN" => 3);
    }

    //C002可変
    $kahenField["C002"] = is_array($kahenField["C002"]) ? $kahenField["C002"] : array();
    foreach ($kahenField["C002"] as $c002Key => $c002Val) {
        $setArray["DETAIL_".$c002Val] = array("SIZE" => 2, "MAXLEN" => 3);
    }

    foreach ($setArray as $key => $val) {
        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
            $value = $row[$key];
        } else {
            $value = ($row[$key] != 0) ? $row[$key] : "";
        }
        $row[$key] = createTextBox($value, $key."[]", $val["SIZE"], $val["MAXLEN"], $monthCnt);
    }

    return $row;
}

//最終日取得
function getFinalDay($db, $month, $semester)
{
    $year = CTRL_YEAR;
    if ($month != "" && $month < "04") {
        $year = CTRL_YEAR + 1;
    }

    $lastday = date("t", mktime( 0, 0, 0, $month, 1, $year ));
    $semeday = $db->getRow(knjc032cQuery::selectSemesAll($semester),DB_FETCHMODE_ASSOC);
    //学期マスタの最終日より大きい場合
    if (sprintf('%02d', $semeday["E_MONTH"]) == $month &&
        $semeday["E_DAY"] < $lastday) {
        $lastday = $semeday["E_DAY"];
    }
    return $lastday;
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
                        "extrahtml" => " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"; onPaste=\"return showPaste(this, ".$monthCnt.");\" ") );
    return $objForm->ge($name);
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //保存ボタン
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", " onclick=\"return btn_submit('update');\"");
    //取消ボタン
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"");
    //終了ボタン
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

?>
