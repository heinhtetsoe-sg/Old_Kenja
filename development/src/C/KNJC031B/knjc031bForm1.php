<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjc031bForm1
{
    public function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form();
        /* CSV */
        $objUp = new csvFile();

        $arg["start"]   = $objForm->get_start("main", "POST", "knjc031bindex.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $arg["year"] = CTRL_YEAR;

        /* 学期 */
        $query = knjc031bQuery::getSemesterName(CTRL_YEAR, CTRL_SEMESTER);
        $semesterName = $db->getOne($query);
        $arg["semester"] = $semesterName;

        /* 対象学級 */
        $hrName = makeHrclassCmb($objForm, $arg, $db, $model);

        /* 対象月 */
        $monthName = makeMonthSemeCmb($objForm, $arg, $db, $model);

        /* タイトル設定 */
        $titleName = setTitleData($arg, $db);

        /*  */
        $header = getHead($titleName, $model);

        /* CSV設定 */
        setCsv($objForm, $arg, $objUp, $hrName, $monthName, $titleName, $header);

        /* 編集対象データリスト */
        makeDataList($objForm, $arg, $db, $model, $objUp, $header, $hrName, $monthName);

        /* ボタン作成 */
        makeButton($objForm, $arg);

        //CSVファイルアップロードコントロール
        $arg["FILE"] = $objUp->toFileHtml($objForm);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /* hidden要素(cmdをセット)作成 */
        $objForm->ae(createHiddenAe("cmd"));

        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjc031bForm1.html", $arg);
    }
}

//対象学級コンボ作成
function makeHrclassCmb(&$objForm, &$arg, $db, &$model)
{
    $query      = knjc031bQuery::selectHrClass($model);
    $result     = $db->query($query);
    $opt_hr     = array();

    /* 2004/08/27 arakaki 近大-作業依頼書20040824.doc */
    $opt_hr[] = array("label" => "",
                      "value" => "");

    while ($row  = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_hr[] = array("label" => $row["HR_NAME"],
                          "value" => $row["GRADE"]."-".$row["HR_CLASS"]);

        /* 初期データセット (起動時に先頭のクラスを対象学級とする) */
        /* 2004/08/27 arakaki 近大-作業依頼書20040824.doc */
        if ($model->field["hr_class"] == "" || $model->field["hr_class"] == null) {
            $model->field["hr_class"] = "";
            $model->field["grade"]    = "";
            $model->field["class"]    = "";
        }
    }
    $arg["hr_class"] = createCombo($objForm, "HR_CLASS", $model->field["hr_class"], $opt_hr, "onChange=\"btn_submit('change')\";", 1);
    $rtnHrname = "";
    for ($i = 0; $i < get_count($opt_hr); $i++) {
        $rtnHrname = ($opt_hr[$i]["value"] == $model->field["hr_class"]) ? $opt_hr[$i]["label"] : $rtnHrname;
    }
    return $rtnHrname;
}

//対象月コンボ作成
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model)
{
    $query      = knjc031bQuery::selectSemesAll();
    $result     = $db->query($query);
    $data       = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();

    $opt_month  = array();
    $opt_month[] = array("label" => "",
                         "value" => "");

    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }
            $getdata = $db->getRow(knjc031bQuery::selectMonthQuery($month), DB_FETCHMODE_ASSOC);
            if (is_array($getdata)) {
                $opt_month[] = array("label" => $getdata["NAME1"]." (".$data[$dcnt]["SEMESTERNAME"].") ",
                                     "value" => $getdata["NAMECD2"]."-".$data[$dcnt]["SEMESTER"]);
            }
        }
    }
    if ($model->field["month"] == "" || $model->field["month"] == null) {
        $model->field["month"] = "";
    }
    $arg["month"] = createCombo($objForm, "MONTH", $model->field["month"], $opt_month, "onChange=\"btn_submit('change')\";", 1);

    $rtnMonth = "";
    for ($i = 0; $i < get_count($opt_month); $i++) {
        $rtnMonth = ($opt_month[$i]["value"] == $model->field["month"]) ? $opt_month[$i]["label"] : $rtnMonth;
    }
    return $rtnMonth;
}

//タイトル設定
function setTitleData(&$arg, $db)
{
    $result = $db->query(knjc031bQuery::getSickDiv());
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["TITLE_".$row["VALUE"]] = $row["LABEL"];
        $rtnTitle[] = $row["LABEL"];
    }
    $result->free();

    return $rtnTitle;
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model, &$objUp, $headerData, $hrName, $monthName)
{
    $tukiAndGakki = explode("-", $model->field["month"]);
    $tuki  = $tukiAndGakki[0];
    $gakki = $tukiAndGakki[1];
    $model->school_kind = $db->getOne(knjc031bQuery::getSchoolKind($model));
    $query = knjc031bQuery::getAppointedDay($tuki, $gakki, $model);
    $appointed_day = $db->getOne($query);

    //学校マスタ
    $schoolMst = array();
    $query = knjc031bQuery::getSchoolMst();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        foreach ($row as $key => $val) {
            $schoolMst[$key] = $val;
        }
    }

    $query      = knjc031bQuery::selectAttendQuery($model, $schoolMst);
    $result     = $db->query($query);

    $monthsem = array();
    $monthsem = preg_split("/-/", $model->field["month"]);

    $data = array();
    $schCnt = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        /* 出欠月別累積データ */
        if (isset($model->warning)) {
            if (is_array($model->reset[$row["SCHREGNO"]])) {
                foreach ($model->reset[$row["SCHREGNO"]] as $key => $val) {
                    $row[$key] = $val;
                }
            }
        }
        //CSVデータセット
        setCsvValue($objUp, $model, $headerData, $hrName, $monthName, $row);

        $setArray = array("LESSON"     => array("SIZE" => 2, "MAXLEN" => 3), //授業日数
                          "OFFDAYS"    => array("SIZE" => 2, "MAXLEN" => 3), //休学日数
                          "ABROAD"     => array("SIZE" => 2, "MAXLEN" => 3), //留学日数
                          "ABSENT"     => array("SIZE" => 2, "MAXLEN" => 3), //公欠日数
                          "SUSPEND"    => array("SIZE" => 2, "MAXLEN" => 3), //出廷
                          "VIRUS"      => array("SIZE" => 2, "MAXLEN" => 3), //伝染病
                          "MOURNING"   => array("SIZE" => 2, "MAXLEN" => 3), //忌引
                          "SICK"       => array("SIZE" => 2, "MAXLEN" => 3), //欠課遅刻
                          "NOTICE"     => array("SIZE" => 2, "MAXLEN" => 3), //欠課早退
                          "NONOTICE"   => array("SIZE" => 2, "MAXLEN" => 3), //欠席
                          "LATEDETAIL" => array("SIZE" => 2, "MAXLEN" => 3), //遅刻
                          "KEKKA_JISU" => array("SIZE" => 2, "MAXLEN" => 3), //欠課時数
                          "KEKKA"      => array("SIZE" => 2, "MAXLEN" => 3)  //早退
                          );
        foreach ($setArray as $key => $val) {
            if (strlen($row[$key]) == 0) {
                $row["BGCOLOR_NAME_SHOW"] = "bgcolor=yellow";
            }
            $row[$key] = createText($objForm, ($row[$key] != 0) ?   $row[$key] : "", $key."[]", $val["SIZE"], $val["MAXLEN"], $schCnt);
        }
        $schCnt++;
        /* hidden(学籍番号) */
        $row["SCHREGNO"] = "<input type=\"hidden\" name=\"SCHREGNO[]\" value=\"".$row["SCHREGNO"]."\">";
        $data[] = $row;
    }

    $arg["attend_data"] = $data;

    $arg["SET_APPOINTED_DAY"] = $appointed_day;
    $objForm->ae(createHiddenAe("SET_APPOINTED_DAY", $appointed_day));
}

//最終日取得
function getFinalDay($db, $month, $semester)
{
    $year = CTRL_YEAR;
    if ($month != "" && $month < "04") {
        $year = CTRL_YEAR + 1;
    }

    $lastday = date("t", mktime(0, 0, 0, $month, 1, $year));
    $semeday = $db->getRow(knjc031bQuery::selectSemesAll($semester), DB_FETCHMODE_ASSOC);
    //学期マスタの最終日より大きい場合
    if (sprintf('%02d', $semeday["E_MONTH"]) == $month && $semeday["E_DAY"] < $lastday) {
        $lastday = $semeday["E_DAY"];
    }
    return $lastday;
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

//CSV設定
function getHead($titleName, $model)
{
    //CSVヘッダ名
    $header = array("0"  => "学級コード",
                    "1"  => "学級名",
                    "2"  => "対象月コード",
                    "3"  => "対象月名",
                    "4"  => "学籍番号",
                    "5"  => "No.",
                    "6"  => "氏名",
                    "7"  => "締め日",
                    "8"  => "授業日数",
                    "9"  => "休学日数",
                    "10" => "留学日数",
                    "11" => "公欠日数",
                    "12" => "出停",
                    "13" => "伝染病",
                    "14" => "忌引",
                    "15" => "出席すべき日数",
                    "16" => $titleName[0],
                    "17" => $titleName[1],
                    "18" => $titleName[2],
                    "19" => "出席日数",
                    "20" => "遅刻",
                    "21" => "欠課時数",
                    "22" => "早退",
                    "23" => "累計授業日数",
                    "24" => "累計出停",
                    "25" => "累計伝染病",
                    "26" => "累計忌引",
                    "27" => "累計出席すべき日数",
                    "28" => "累計欠席",
                    "29" => "累計出席日数",
                    "30" => "累計遅刻",
                    "31" => "累計欠課時数",
                    "32" => "累計早退",
                    "33" => $model->lastColumn);
    return $header;
}

//CSV設定
function setCsv(&$objForm, &$arg, &$objUp, $hrName, $monthName, $titleName, $header)
{
    $objUp->setHeader(array_values($header));

    //CSV出力ファイル名
    $objUp->setFileName(CTRL_YEAR."年度_".$hrName."_".$monthName."_"."出欠情報入力.csv");

    $objUp->setType(array(8=>'S',9=>'S',10=>'S',11=>'S',12=>'S',13=>'S',14=>'S',16=>'S',17=>'S',18=>'S',20=>'S',21=>'S',22=>'S'));
    $objUp->setSize(array(8=>3,  9=>3,  10=>3,  11=>3,  12=>3,  13=>3,  14=>3,  16=>3,  17=>3,  18=>3,  20=>3,  21=>3,  22=>3));
}

//CSV値設定
function setCsvValue(&$objUp, $model, $headerData, $hrName, $monthName, $row)
{
    //キー値をセット
    $key = array("学級コード"   => $model->field["hr_class"],
                 "対象月コード" => $model->field["month"],
                 "学籍番号"     => $row["SCHREGNO"]);

    $csv = array($model->field["hr_class"],
                 $hrName,
                 $model->field["month"],
                 $monthName);
    $fieldCnt = 4;
    $header = array("LESSON", "OFFDAYS", "ABROAD", "ABSENT", "SUSPEND", "VIRUS", "MOURNING", "SICK", "NOTICE", "NONOTICE", "LATEDETAIL", "KEKKA_JISU", "KEKKA");
    foreach ($row as $rowKey => $val) {
        $csv[] = $val;

        if (in_array($rowKey, $header)) {
            //入力エリアとキーをセットする
            $objUp->setElementsValue($rowKey."[]", $headerData[$fieldCnt], $key);
        }
        $fieldCnt++;
    }
    $csv[] = $model->lastColumn;
    $objUp->addCsvValue($csv);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae(array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ));
    return $objForm->ge($name);
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae(array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
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

function createText(&$objForm, $data, $name, $size, $maxlength, $schCnt)
{
    $objForm->ae(array("type"      => "text",
                        "name"      => $name,
                        "size"      => $size,
                        "maxlength" => $maxlength,
                        "value"     => $data,
                        "extrahtml" => " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"; onPaste=\"return show(this, ".$schCnt.");\" "));
    return $objForm->ge($name);
}
