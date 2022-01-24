<?php

require_once('for_php7.php');

class knjl326aForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl326aForm1", "POST", "knjl326aindex.php", "", "knjl326aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["data"]["YEAR"] = $model->examyear."年度";

        //受験校種コンボ
        $extra = $change;
        $query = knjl326aQuery::getNameMst($model, 'L003');
        $extra = "onchange=\"return btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, $model->applicantdiv, "APPLICANTDIV", $extra, 1);

        //校種
        $query = knjl326aQuery::getNameMst($model, 'L003', $model->applicantdiv);
        $appRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->schoolkind = $appRow["NAMESPARE3"];

        //試験コンボ
        $query = knjl326aQuery::getTestdivMst($model);
        $extra = "onchange=\"return btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, $model->testdiv, "TESTDIV", $extra, 1);

        //専併区分(受験番号範囲用)
        $extra = "onchange=\"return btn_submit('knjl326a')\"";
        $query = knjl326aQuery::getNameMst($model, "L006");
        makeCmb($objForm, $arg, $db, $query, $model->shdiv, "SHDIV", $extra, 1, ($model->noticeType == '3') ? "" : "ALL");

        //志望コース(受験番号範囲用)
        $extra = "";
        $query = knjl326aQuery::getNameMst($model, "L".$model->schoolkind."58");
        makeCmb($objForm, $arg, $db, $query, $model->wish_course, "WISH_COURSE", $extra, 1, "ALL");

        //受験番号範囲
        if ($model->cmd == "read") {
            $query = knjl326aQuery::getReceptnoFromTo($model);
            $receptRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->receptnoFrom = $receptRow["RECEPTNO_FROM"];
            $model->receptnoTo = $receptRow["RECEPTNO_TO"];
        }
        $extra = ($model->noticeType == '3' && $model->shdiv == '2') ? "onblur=\"return btn_submit('knjl326a')\"" : "";
        $arg["data"]["RECEPTNO_FROM"] = knjCreateTextBox($objForm, $model->receptnoFrom, "RECEPTNO_FROM", 7, 7, $extra);
        $extra = ($model->noticeType == '3' && $model->shdiv == '2') ? "disabled" : "";
        $arg["data"]["RECEPTNO_TO"] = knjCreateTextBox($objForm, $model->receptnoTo, "RECEPTNO_TO", 7, 7, $extra);

        //通知種別一覧      [受験校種][通知種別番号] = (ラベル / 専願切替手続期限フラグ / 併願手続期限フラグ)
        $noticeTypeArray = array();
        //中学
        $noticeTypeArray[1][1]  = array("1:合格通知",                   '',     '');
        $noticeTypeArray[1][3]  = array("3:入学許可書",                 '',     '');
        $noticeTypeArray[1][4]  = array("4:特待生決定通知書",           '',     '');
        $noticeTypeArray[1][5]  = array("5:区域外就学届出書",           '',     '');
        //高校
        $noticeTypeArray[2][1]  = array("1:選考結果通知書（合格）",     '1',    '');
        $noticeTypeArray[2][2]  = array("2:選考結果通知書（不合格）",   '',     '');
        $noticeTypeArray[2][3]  = array("3:入学許可書",                 '',     '');
        $noticeTypeArray[2][4]  = array("4:特待生通知書",               '1',    '1');

        //通知種別コンボ
        $opt = $s_limitdate = $h_limitdate = array();
        foreach ($noticeTypeArray[$model->applicantdiv] as $val => $array) {
            list($label, $s_limitdate[$val], $h_limitdate[$val]) = $array;
            $opt[] = array('label' => $label, 'value' => $val);
        }
        $extra = "onchange=\"return btn_submit('change')\"";
        $model->noticeType = ($model->noticeType) ? $model->noticeType: "1";
        $arg["data"]["NOTICE_TYPE"] = knjCreateCombo($objForm, "NOTICE_TYPE", $model->noticeType, $opt, $extra, 1);

        if ($model->noticeType == '3') {
            //入学コース選択コンボ（専願合格コース、併願合格コース）
            $query = knjl326aQuery::getPassCourse($model);
            $extra = ($model->shdiv == '2') ? "" : "disabled";
            makeCmb($objForm, $arg, $db, $query, $model->passCourse, "PASS_COURSE", $extra, 1);
            $arg["showPASS_COURSE"] = 1;
        }

        if ($model->noticeType == '4') {
            //特待区分コンボ
            $query = knjl326aQuery::getEntexamHonordivMst($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, $model->honordiv, "HONORDIV", $extra, 1);
            $arg["showHONORDIV"] = 1;
        }

        //通知日付
        $model->noticeDate = str_replace("-", "/", $model->noticeDate);
        $arg["data"]["NOTICE_DATE"] = View::popUpCalendarAlp($objForm, "NOTICE_DATE", $model->noticeDate, "", "");

        //日付欄の高さ調整用
        $height = 50;

        if ($s_limitdate[$model->noticeType] == "1") {
            //専願切替手続期限
            $model->sLimitDate = str_replace("-", "/", $model->sLimitDate);
            $arg["data"]["S_LIMIT_DATE"] = View::popUpCalendarAlp($objForm, "S_LIMIT_DATE", $model->sLimitDate, "", "");
            $arg["showS_LIMIT_DATE"] = 1;
            $height += 25;
        }

        if ($h_limitdate[$model->noticeType] == "1") {
            //併願手続期限
            $model->hLimitDate = str_replace("-", "/", $model->hLimitDate);
            $arg["data"]["H_LIMIT_DATE"] = View::popUpCalendarAlp($objForm, "H_LIMIT_DATE", $model->hLimitDate, "", "");
            $arg["showH_LIMIT_DATE"] = 1;
            $height += 25;
        }

        //日付欄の高さ
        $arg["height"] = $height;

        //読込ボタン(受験番号範囲用)
        $extra = "onclick=\"return btn_submit('read')\"";
        $arg["button"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);


        //通知種別が入学許可書の場合、入学コース登録処理をして印刷する。
        if ($model->noticeType == '3') {
            //更新／印刷ボタン
            $extra = "onclick=\"return btn_submit('updateAndPrint');\"";
            $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "更新／プレビュー／印刷", $extra);
            if (!isset($model->warning) && $model->cmd == "print") {
                $arg["jscript"] = " newwin('" . SERVLET_URL . "');";
            }
        } else {
            //印刷ボタン
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
            $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        }

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->examyear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL326A");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl326aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "-- 全て --", "value" => "ALL");
    }
    $value_flg = false;
    $default = 0;
    $i = ($blank) ? 1 : 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg && ($name == "APPLICANTDIV" || $name == "TESTDIV")) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();

    $value = (strlen($value) && $value_flg) ? $value : $opt[$default]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
