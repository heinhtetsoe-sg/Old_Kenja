<?php

require_once('for_php7.php');

class knjd425lForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd425lindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //タイトル
        $arg["TITLE"] = "個別の指導計画";

        if (!$model->exp_year) {
            $model->exp_year = CTRL_YEAR;
        }
        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $arg["NOT_WARNING"] = 1;
        }

        //名称マスタ取得
        $query = knjd425lQuery::getNameMst("Z055", "01");
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($row) {
            $arg["KIND_TITLE"] = $row["NAME1"];
        }

        $kindList = array();
        //支援計画項目名取得
        $query = knjd425lQuery::getChallengedSupportplanKindName($model, "01");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["KIND_SEQ"] == "000") {
                $arg["STATUS_NAME1"] = $row["STATUS_NAME1"];
                $arg["STATUS_NAME2"] = $row["STATUS_NAME2"];
            } else {
                $kindData = array();
                $kindData["KIND_SEQ"]  = $row["KIND_SEQ"];
                $kindData["KIND_NAME"] = $row["KIND_NAME"];
                $kindList[] = $kindData;
            }
        }
        $arg["KIND_LIST"] = $kindList;

        //生徒が選択されてから表示する処理
        if ($model->schregno) {
            //useKnjd425DispUpdDateプロパティが立っているときのみ、日付を利用。
            if ($model->Properties["useKnjd425DispUpdDate"] == "1") {
                $arg["dispseldate"] = "1";
                //日付選択
                $arg["data"]["UPDTITLE"] = "更新日:";
                $query = knjd425lQuery::getUpdatedDateList($model);
                $extra = "onchange=\"btn_submit('edit');\"";
                $opt = array();
                $opt[] = array("label"=>"新規", "value"=>"9999/99/99");
                makeDateCmb($objForm, $arg, $db, $query, "UPDDATE", $model->upddate, $extra, 1, $opt);

                if ($model->upddate == "9999/99/99") {
                    $arg["newdate"] = "1";
                    $model->recordDate = $model->recordDate == "" ? str_replace("-", "/", CTRL_DATE) : $model->recordDate;

                    $extra = "extra=btn_submit(\'edit\');";
                    $arg["data"]["RECORD_DATE"] = View::popUpCalendar($objForm, "RECORD_DATE", str_replace("-", "/", $model->recordDate), $extra);
                } else {
                    $model->recordDate = $model->upddate;
                    knjCreateHidden($objForm, "RECORD_DATE", $model->recordDate);
                }
            } else {
                //固定日付で処理
                $model->recordDate = "9999/03/31";
                knjCreateHidden($objForm, "RECORD_DATE", $model->recordDate);
            }

            //可変ボタン作成
            $query = loadGuidanceKindName($db, $model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (!$row["KINDCD"]) {
                    continue;
                }
                $prmdatestr = "";
                if ($model->recordDate != "") {
                    $prmdatestr = "&UPDDATE={$model->recordDate}";
                } elseif ($model->upddate != "") {
                    $prmdatestr = "&UPDDATE={$model->upddate}";
                }
                $link = REQUESTROOT."/D/KNJD425L_{$row["KINDCD"]}/knjd425l_{$row["KINDCD"]}index.php";
                $link .= "?mode=1&PROGRAMID=KNJD425L_{$row["KINDCD"]}&SEND_PRGID=KNJD425L";
                $link .= "&SEND_AUTH={$model->auth}&SEND_selectSchoolKind={$model->selectSchoolKind}";
                $link .= "&KINDNO={$row["KIND_NO"]}&SCHREGNO={$model->schregno}";
                $link .= "&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}";
                $link .= "&GRADE={$model->grade}&NAME={$model->name}".$prmdatestr;

                $extra = "style=\"height:22px;width:250px;background:#00FFFF;color:#000080;font:bold;\" ";
                $extra .= "id=\"KNJD425L_{$row["KINDCD"]}\" aria-label=\"{$row["BTN_SUBFORMCALL"]}\" ";
                // $extra .= "onclick=\"current_cursor('KNJD425L_{$row["KINDCD"]}');if (chksetdate()) {document.location.href='$link'}\" ";
                $extra .= "onclick=\"current_cursor('KNJD425L_{$row["KINDCD"]}');if (chksetdate()) { clickLink('$link') }\" ";

                $row["BTN_SUBFORMCALL"] = KnjCreateBtn($objForm, "btn_subform1", $row["BTN_SUBFORMCALL"], $extra);

                $arg["list"][] = $row;
            }
            knjCreateHidden($objForm, "HID_SELKINDNO");

            //生徒情報設定
            $query = knjd425lQuery::getSchInfoShousai($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($row) {
                //学部
                $arg["SCHKIND_NAME"] = $row["COURSENAME"];
                //年・組
                $arg["GRADE_HR_NAME"] = $row["GHR_NAME"];
                $arg["GRADE_HR_NAME"] = $row["GRADE_NAME"];
                if ($row["GHR_NAME"]) {
                    $arg["GRADE_HR_NAME"] .= $row["GHR_NAME"];
                } else {
                    $arg["GRADE_HR_NAME"] .= $row["HR_CLASS_NAME"];
                }
                //氏名
                $arg["NAME_KANA"] = $row["NAME_KANA"];
                $arg["NAME"] = $row["NAME"];


                //障害名等、障害の概要
                $query = knjd425lQuery::getAssessmentMain($model);
                $assessRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $temp = str_replace(array("\n", "\r\n"), "<br>", $assessRow["CHALLENGED_NAMES"]);
                $arg["DISEASE_NAME"] = $temp;
                $temp = str_replace(array("\n", "\r\n"), "<br>", $assessRow["CHALLENGED_STATUS"]);
                $arg["DISEASE_OVERVIEW"] = $temp;

                //検査日
                $query = knjd425lQuery::getAssessmentCheckRecord($model, $assessRow["RECORD_DATE"], 1);
                $checkRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($checkRow) {
                    $arg["CHECKUP_DATE1"] = str_replace("-", "/", $checkRow["CHECK_DATE"]);
                    $arg["CHECKUP_FACILITY1"] = $checkRow["CHECK_CENTER_TEXT"];
                    $arg["CHECKUP_NAME1"] = $checkRow["CHECK_NAME"];
                }
                $query = knjd425lQuery::getAssessmentCheckRecord($model, $assessRow["RECORD_DATE"], 2);
                $checkRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($checkRow) {
                    $arg["CHECKUP_DATE2"] = str_replace("-", "/", $checkRow["CHECK_DATE"]);
                    $arg["CHECKUP_FACILITY2"] = $checkRow["CHECK_CENTER_TEXT"];
                    $arg["CHECKUP_NAME2"] = $checkRow["CHECK_NAME"];
                }

                //願い
                for ($i=0; $i < get_count($kindList); $i++) {
                    $kindData = $kindList[$i];
                    $query = knjd425lQuery::getSupportplanRecord($model, substr($kindData["KIND_SEQ"], -2));
                    $supportRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if ($supportRow) {
                        $kindData["HOPE1"] = str_replace(array("\n", "\r\n"), "<br>", $supportRow["HOPE1"]);
                        $kindData["HOPE2"] = str_replace(array("\n", "\r\n"), "<br>", $supportRow["HOPE2"]);
                    }
                    $kindList[$i] = $kindData;
                }
                $arg["KIND_LIST"] = $kindList;

                //合理的配慮
                $query = knjd425lQuery::getSupportplanMain($model);
                $supportRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($supportRow) {
                    $arg["REASONABLE_ACCOMMODATION"] = str_replace(array("\n", "\r\n"), "<br>", $supportRow["REASONABLE_ACCOMMODATION"]);
                }

                //作成日、作成者
                if ($model->cmd == "edit") {
                    $query = knjd425lQuery::getGuidanceSchregRemark($model, 1);
                    $guidRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if ($guidRow) {
                        $model->field["SUPPORTPLAN_DATE"] = str_replace("-", "/", $guidRow["REMARK"]);
                    }

                    $query = knjd425lQuery::getGuidanceSchregRemark($model, 2);
                    $guidRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if ($guidRow) {
                        $model->field["SUPPORTPLAN_STAFFCD"] = $guidRow["REMARK"];
                    }
                }
                $extra = "";
                $arg["SUPPORTPLAN_DATE"] = View::popUpCalendar($objForm, "SUPPORTPLAN_DATE", str_replace("-", "/", $model->field["SUPPORTPLAN_DATE"]), $extra);
                //職員情報取得
                $query = knjd425lQuery::getStaff($model);
                $extra = "";
                makeCmb($objForm, $arg, $db, $query, $model->field["SUPPORTPLAN_STAFFCD"], "SUPPORTPLAN_STAFFCD", $extra, 1, "BLANK");
            }
        }

        //更新
        $extra = "id=\"update\" aria-label=\"更新\" onclick=\"current_cursor('update');return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //終了ボタン
        $extra = "id=\"close\" aria-label=\"終了\" onclick=\"current_cursor('close');closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        knjCreateHidden($objForm, "GRADE_HR_CLASS");
        knjCreateHidden($objForm, "USEKNJD425LDISPUPDDATE", $model->Properties["useKnjd425DispUpdDate"]);

        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjd425lForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//日付コンボ作成
function makeDateCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $newOpt)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => str_replace("-", "/", $row["LABEL"])
                     , 'value' => str_replace("-", "/", $row["VALUE"]));
        if ($value == str_replace("-", "/", $row["VALUE"])) {
            $value_flg = true;
        }
    }
    $result->free();
    // 「新規」追加
    for ($i=0; $i < get_count($newOpt); $i++) {
        $appendOpt = $newOpt[$i];
        $opt[] = array('label' => str_replace("-", "/", $appendOpt["label"])
                     , 'value' => str_replace("-", "/", $appendOpt["value"]));
        if ($value == str_replace("-", "/", $appendOpt["value"])) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//項目名称取得
function loadGuidanceKindName($db, $model, $kindNo = "")
{

    //データの取得優先度として、個人>年組>年度となる。
    //データの取得（個人）
    $query = knjd425lQuery::getGuidanceKindName($model, $model->schregno, $model->grade, $kindNo);
    $row = $db->getRow($query);
    if ($row) {
        return $query;
    }

    //データの取得（年組）
    $query = knjd425lQuery::getGuidanceKindName($model, "", $model->grade, $kindNo);
    $row = $db->getRow($query);
    if ($row) {
        return $query;
    }

    //データの取得（年度）
    $query = knjd425lQuery::getGuidanceKindName($model, "", "", $kindNo);
    $row = $db->getRow($query);
    return $query;
}
