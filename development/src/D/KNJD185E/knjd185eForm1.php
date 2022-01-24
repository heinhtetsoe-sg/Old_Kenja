<?php

require_once('for_php7.php');

class knjd185eForm1
{

    function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd185eForm1", "POST", "knjd185eindex.php", "", "knjd185eForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjd185eQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd185e'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //学年末は今学期とする
        $seme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //表示指定ラジオボタン 1:クラス 2:個人
        $opt_disp = array(1, 2);
        $model->field["DISP"] = ($model->field["DISP"] == "") ? "1" : $model->field["DISP"];
        $extra = array("id=\"DISP1\" onClick=\"return btn_submit('knjd185e')\"", "id=\"DISP2\" onClick=\"return btn_submit('knjd185e')\"");
        $radioArray = knjCreateRadio($objForm, "DISP", $model->field["DISP"], $extra, $opt_disp, count($opt_disp));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //年組コンボ
        if ($model->field["DISP"] == "1") {
            $query = knjd185eQuery::getGrade($model, $seme);
        } else {
            $query = knjd185eQuery::getGradeHrClass($model, $seme);
        }
        $extra = "onchange=\"return btn_submit('knjd185e'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //校種
        if ($model->field["DISP"] == "1") {
            $query = knjd185eQuery::getGrade($model, $seme, $model->field["GRADE_HR_CLASS"]);
        } else {
            $query = knjd185eQuery::getGradeHrClass($model, $seme, $model->field["GRADE_HR_CLASS"]);
        }
        $model->schoolKind = $db->getOne($query);

        //異動対象日付初期値セット
        if ($model->field["DATE"] == "") {
            $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        }
        //異動対象日付（テキスト）
        $disabled = "";
        $extra = "onblur=\"isDate(this); tmp_list('knjd185e', 'on')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["DATE"], "DATE", 12, 12, $extra);
        //異動対象日付（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('knjd185e', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //異動対象日付
        $arg["data"]["DATE"] = View::setIframeJs().$date_textbox.$date_button;

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model, $seme);

        //設定の初期値取得
        $dataTmp = array();
        $query = knjd185eQuery::getHreportConditionDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataTmp[$row["SEQ"]] = $row;
        }

        //帳票出力ラジオボタン 1:A 2:B 3:C 4:D 5:E
        knjCreateHidden($objForm, "SEQ001_R1", $dataTmp["001"]["REMARK1"] ? $dataTmp["001"]["REMARK1"] : "1");

        //成績の表示項目(A,Bのみ) REMARK1～8 : 1～8学期 REMARK9：学年末
        for ($rCnt = 1; $rCnt <= 10; $rCnt++) {
            knjCreateHidden($objForm, "SEQ002_R{$rCnt}", $dataTmp["002"]["REMARK{$rCnt}"]);
        }

        //合計点(A,B,Cのみ) 1:表記なし
        knjCreateHidden($objForm, "SEQ003_R1", $dataTmp["003"]["REMARK1"]);

        //平均点 REMARK1(1:表記なし 2:表記あり)
        knjCreateHidden($objForm, "SEQ004_R1", $dataTmp["004"]["REMARK1"] ? $dataTmp["004"]["REMARK1"] : "1");
        //平均点種類 REMARK2(1:クラス 2:コース 3:学年) 【REMARK1(2:表記あり)の時】
        knjCreateHidden($objForm, "SEQ004_R2", $dataTmp["004"]["REMARK2"] ? $dataTmp["004"]["REMARK2"] : "1");

        //修得単位合計(A,Bのみ) 1:表記なし
        knjCreateHidden($objForm, "SEQ005_R1", $dataTmp["005"]["REMARK1"]);

        //出欠の表示項目(A,B,Dのみ) REMARK1～8 : 1～8学期 REMARK9：学年末
        for ($rCnt = 1; $rCnt <= 10; $rCnt++) {
            knjCreateHidden($objForm, "SEQ006_R{$rCnt}", $dataTmp["006"]["REMARK{$rCnt}"]);
        }

        //修得単位合計(A,B,Dのみ) 1:表記なし
        knjCreateHidden($objForm, "SEQ007_R1", $dataTmp["007"]["REMARK1"]);

        //修得単位合計(A,B,Dのみ) 1:表記なし
        knjCreateHidden($objForm, "SEQ008_R1", $dataTmp["008"]["REMARK1"]);

        //修得単位合計(A,B,Dのみ) 1:表記なし
        knjCreateHidden($objForm, "SEQ009_R1", $dataTmp["009"]["REMARK1"]);

        //総合学習の時間(A,B,C,Eのみ) 1:表記なし
        knjCreateHidden($objForm, "SEQ010_R1", $dataTmp["010"]["REMARK1"]);

        //各教科の観点(D,Eのみ) 1:出力なし
        knjCreateHidden($objForm, "SEQ011_R1", $dataTmp["011"]["REMARK1"]);

        //担任項目名ラジオボタン 1:担任 2:チューター
        knjCreateHidden($objForm, "SEQ012_R1", $dataTmp["012"]["REMARK1"] ? $dataTmp["012"]["REMARK1"] : "1");

        //教科名ラジオボタン 1:総合的な学習(探求)の時間 2:課題研究
        knjCreateHidden($objForm, "SEQ013_R1", $dataTmp["013"]["REMARK1"] ? $dataTmp["013"]["REMARK1"] : "1");

        knjCreateHidden($objForm, "knjd185eNotPrintClass90", $model->Properties["knjd185eNotPrintClass90"]);
        knjCreateHidden($objForm, "knjd185ePatternDGakkkiTitle", $model->Properties["knjd185ePatternDGakkkiTitle"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd185eForm1.html", $arg);
    }
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $seme)
{
    //対象外の生徒取得
    $query = knjd185eQuery::getSchnoIdou($model, $seme);
    $result = $db->query($query);
    $opt_idou = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_idou[] = $row["SCHREGNO"];
    }
    $result->free();

    //対象者リスト
    if ($model->field["DISP"] == "1") {
        $query = knjd185eQuery::getGradeHrClass($model, $seme, $model->field["GRADE_HR_CLASS"]);
    } else {
        $query = knjd185eQuery::getStudent($model, $seme);
    }
    $result = $db->query($query);
    $opt_right = $opt_left = array();
    if ($model->field["DISP"] == "1") {
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
    } else {
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $idou = (in_array($row["SCHREGNO"], $opt_idou)) ? "●" : "　";
            $opt_right[] = array('label' => $row["SCHREGNO"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                 'value' => $row["SCHREGNO"]);
        }
    }
    $result->free();
    $extra = "multiple style=\"width:250px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //生徒一覧リスト
    $extra = "multiple style=\"width:250px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
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

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
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
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD185E");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "HREPORTREMARK_DAT_COMMUNICATION_SIZE", $model->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_{$model->schoolKind}"]);
    knjCreateHidden($objForm, "HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE", $model->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_{$model->schoolKind}"]);
    knjCreateHidden($objForm, "HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE", $model->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_{$model->schoolKind}"]);
}
?>
