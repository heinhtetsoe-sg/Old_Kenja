<?php

require_once('for_php7.php');

require_once("AttendAccumulate.php");
class knjg105aForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjg105aindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjg105aQuery::getSemesterList();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //年組コンボ作成
        $query = knjg105aQuery::getGradeHrclass($model->field["SEMESTER"], $model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, 1);

        //欠席
        $extra = "style=\"text-align:right\" onblur=\"toInteger1(this);\" onkeydown=\"goEnter(this);\"";
        $model->field["SICK_CNT"] = (strlen($model->field["SICK_CNT"])) ? $model->field["SICK_CNT"] : "5";
        $arg["SICK_CNT"] = knjCreateTextBox($objForm, $model->field["SICK_CNT"], "SICK_CNT", 3, 3, $extra);

        //dummy_textbox
        $extra = "style=\"display:none;\"";
        $arg["dummy"] = knjCreateTextBox($objForm, "", "dummy", 4, 4, $extra);

        //集計日付
        $model->field["DATE"] = ($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-","/", CTRL_DATE);
        if (!$model->checkCtrlDay($model->field["DATE"])) {
            $model->field["DATE"] = str_replace("-","/", CTRL_DATE);
        }
        $extra = " btn_submit('main');\" onkeydown=\"goEnter(this);\"";
        $arg["DATE"] = makepopUpCalendar($objForm, "DATE", $model->field["DATE"], "reload=true", $extra, "");

        //チェックボックスALL
        $extra = "onClick=\"check_all(this);\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //初期化
        $model->data     = array();

        //一覧表示
        $attend_sdate = "";
        $attend_seme = "";
        $attend_month = array();
        $query = knjg105aQuery::getAttendDate($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $tmp_attend_sdate = $row["MAX_YEAR"] ."-" .$row["MONTH"] ."-" .$row["MAX_APP"];
            if (str_replace("/","-",$model->field["DATE"]) < $tmp_attend_sdate) break;
            $attend_month[] = $row["MONTH"];
            $attend_sdate = $tmp_attend_sdate;
            $attend_seme = $row["SEMESTER"];
        }
        $result->free();
        if ($attend_sdate == "") {
            $query2 = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR='".CTRL_YEAR."' AND SEMESTER='1'";
            $attend_sdate = $db->getOne($query2);   //学期開始日
        } else {
            $query2 = "VALUES Add_days(date('".$attend_sdate."'), 1)";
            $attend_sdate = $db->getOne($query2);   //次の日
        }

        $arraycheckSch = array();
        if (isset($model->warning)) $arraycheckSch = explode(',', $model->checkSch);
        $semoffdays = $db->getOne(knjg105aQuery::getSemoffdays($model));
        //一覧表示
        $result = $db->query(knjg105aQuery::selectQuery($model, $attend_seme, $attend_month, $attend_sdate, $semoffdays));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //チェックボックス
            $extra  = "id=\"CHECKED{$row["SCHREGNO"]}\" onclick=\"chkClick(this)\" tabindex=\"-1\"";
            $extra .= (isset($model->warning) && in_array($row["SCHREGNO"], $arraycheckSch)) ? " checked" : "";
            $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED".$row["SCHREGNO"], $row["SCHREGNO"], $extra, "");

            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //出席番号
            if($row["ATTENDNO"] != "") {
                $row["ATTENDNO"] = sprintf("%01d", $row["ATTENDNO"]).'番';
            }

            //主な理由
            $value = (!isset($model->warning)) ? $row["BASE_REMARK1"] : $model->field["BASE_REMARK1{$row["SCHREGNO"]}"];
            $extra = "onchange=\"checkSelf('".$row["SCHREGNO"]."');\"";
            $row["BASE_REMARK1"] = knjCreateTextArea($objForm, "BASE_REMARK1".$row["SCHREGNO"], "2", "25", "soft", $extra, $value);

            //背景色
            $row["COLOR"] = (isset($model->warning) && in_array($row["SCHREGNO"], $arraycheckSch)) ? "#ccffcc" : "#ffffff";

            $arg["data"][] = $row;
        }

        //ボタン作成
        //更新ボタンを作成する
        $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
        //取消ボタンを作成する
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "checkSch");

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjg105aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
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

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//カレンダーコントロール
function makepopUpCalendar(&$objForm, $name, $value="",$param="",$extra="",$disabled="") {
    global $sess;
    //テキストエリア
    $extra = " onblur=\"isDate(this);$extra\"".$disabled;
    $setDateText = knjCreateTextBox($objForm, $value, $name, 12, 12, $extra);

    //読込ボタンを作成する
    $extra = "onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=$name&frame='+getFrameName(self) + '&date=' + document.forms[0]['$name'].value + '&CAL_SESSID=$sess->id&$param' + '&CSSNO=$cssNo', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
    $setCalBtn = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);

    return View::setIframeJs() .$setDateText .$setCalBtn;
}
?>
