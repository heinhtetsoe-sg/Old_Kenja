<?php

require_once('for_php7.php');

class knjd425nForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd425nindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //タイトル
        $arg["TITLE"] = "個別の指導計画（知的用）";

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $arg["NOT_WARNING"] = 1;
        }

        //生徒が選択されてから表示する処理
        if ($model->schregno) {
            //useKnjd425DispUpdDateプロパティが立っているときのみ、日付を利用。
            if ($model->Properties["useKnjd425DispUpdDate"] == "1") {
                $arg["dispseldate"] = "1";
                //日付選択
                $arg["data"]["UPDTITLE"] = "更新日:&nbsp;";
                $query = knjd425nQuery::getUpdatedDateList($model);
                $extra = "onchange=\"btn_submit('edit');\"";
                $opt = array();
                $opt[] = array("label"=>"新規", "value"=>"9999/99/99");
                makeDateCmb($objForm, $arg, $db, $query, "UPDDATE", $model->upddate, $extra, 1, $opt);

                if ($model->upddate == "9999/99/99") {
                    $arg["newdate"] = "1";
                    $model->selnewdate = $model->selnewdate == "" ? str_replace("-", "/", CTRL_DATE) : $model->selnewdate;

                    $param = "extra=btn_submit(\'edit\');";
                    $arg["data"]["SELNEWDATE"] = View::popUpCalendar($objForm, "SELNEWDATE", str_replace("-", "/", $model->selnewdate), $param);
                } else {
                    $model->selnewdate = "";
                }
            } else {
                //固定日付で処理
                $model->selnewdate = "9999/03/31";
                $model->upddate = $model->selnewdate;
            }

            //可変ボタン作成
            $result = loadGuidanceKindName($db, $model);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $prmdatestr = "";
                if ($model->selnewdate != "") {
                    $prmdatestr = "&UPDDATE={$model->selnewdate}";
                } else if ($model->upddate != "") {
                    $prmdatestr = "&UPDDATE={$model->upddate}";
                }
                $link = REQUESTROOT."/D/KNJD425N_{$row["KINDCD"]}/knjd425n_{$row["KINDCD"]}index.php?mode=1&PROGRAMID=KNJD425N_{$row["KINDCD"]}&SEND_PRGID=KNJD425N&SEND_AUTH={$model->auth}&SEND_selectSchoolKind={$model->selectSchoolKind}&KINDNO={$row["KIND_NO"]}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}".$prmdatestr;
                $extra = "style=\"height:22px;width:250px;background:#00FFFF;color:#000080;font:bold;\" onclick=\"if (chksetdate()) {document.location.href='$link'}\"";
                $row["BTN_SUBFORMCALL"] = KnjCreateBtn($objForm, "btn_subform1", $row["BTN_SUBFORMCALL"], $extra);

                $arg["list"][] = $row;
            }
            knjCreateHidden($objForm, "HID_SELKINDNO");
        }

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "PRGID", "KNJD425N");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "GRADE_HR_CLASS");
        knjCreateHidden($objForm, "USEKNJD425NDISPUPDDATE", $model->Properties["useKnjd425DispUpdDate"]);

        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjd425nForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $opt=array())
{
    $result = $db->query($query);
    $defValue = '';
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($row["DEF_VALUE_FLG"] == '1') {
            $defValue = $row["VALUE"];
        }
    }

    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value == "" && $defValue) ? $defValue : ($value ? $value : $opt[0]["value"]);
    } else if ($name == "TORIKOMI_B_DATE") {
        $value = ($value == "") ? $opt[0]["value"] : $value;
        //初期値設定のみで、オブジェクト作成は不要。
        return;
    } else {
        $value = ($value == "") ? $opt[0]["value"] : $value;
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//日付コンボ作成
function makeDateCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $opt=array())
{
    $result = $db->query($query);
    $defValue = '';
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_unshift($opt, array('label' => str_replace("-", "/", $row["LABEL"]),
                       'value' => str_replace("-", "/", $row["VALUE"])));
        if ($row["DEF_VALUE_FLG"] == '1') {
            $defValue = str_replace("-", "/", $row["VALUE"]);
        }
    }

    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value == "" && $defValue) ? $defValue : ($value ? $value : $opt[0]["value"]);
    } else {
        $value = ($value == "") ? $opt[0]["value"] : $value;
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//項目名称取得
function loadGuidanceKindName($db, $model, $kno="")
{
    //データの優先度として、個人>年組>年度となる。データが無ければ下位SQLでデータを取得していく。
    $specifyschregflg = "";
    $query = knjd425nQuery::getGuidanceKindName($model, $specifyschregflg, $kno);
    $result = $db->getRow($query);
    if (get_count($result) > 0) {
        //if文を抜けて最後の処理を実施。
    } else {
        //データが無い時は学籍番号抜きで取り直し。
        $specifyschregflg = "1";
        $query = knjd425nQuery::getGuidanceKindName($model, $specifyschregflg, $kno);
        $result = $db->getRow($query);
        if (get_count($result) > 0) {
            //if文を抜けて最後の処理を実施。
        } else {
            //データが無い時は年組、学籍番号抜きで取り直し。
            $specifyschregflg = "2";
            $query = knjd425nQuery::getGuidanceKindName($model, $specifyschregflg, $kno);
            $result = $db->getRow($query);
        }
    }

    if ($kno != "") {
        return $result;
    } else {
        $query = knjd425nQuery::getGuidanceKindName($model, $specifyschregflg, $kno);
        $result = $db->query($query);
        return $result;
    }
}

//西暦をH99.00.00形式で変換
function Calc_SNameWareki(&$year, $month, $day)
{
    $border = array();

    $warekiList = array();
    $warekiList = common::getWarekiList();

    for ($i = 0; $i < get_count($warekiList); $i++) {
        $warekiInfo = $warekiList[$i];
        $start = str_replace("/", "", $warekiInfo['Start']);
        $end = str_replace("/", "", $warekiInfo['End']);
        $border[] = array("開始日" =>  $start, "終了日" => $end, "元号" => $warekiInfo['SName']);
    }

    $target = sprintf("%04d%02d%02d", $year, $month, $day);
    for ($i = 0; $border[$i]; $i++){
        if ($border[$i]["開始日"] <= $target &&
            $target <= $border[$i]["終了日"] ){
            $year = ($year - substr($border[$i]["開始日"], 0, 4) + 1);
            return $border[$i]["元号"] .(sprintf("%02d", (int) $year));
        }

    }
    return false;
}

?>
