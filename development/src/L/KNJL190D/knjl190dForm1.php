<?php

require_once('for_php7.php');

class knjl190dForm1
{

    public function main(&$model)
    {

        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl190dForm1", "POST", "knjl190dindex.php", "", "knjl190dForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $opt = array();
        $opt[] = array('value' => CTRL_YEAR,     'label' => CTRL_YEAR);
        $opt[] = array('value' => CTRL_YEAR + 1, 'label' => CTRL_YEAR + 1);
        $model->examyear = ($model->examyear == "") ? substr(CTRL_DATE, 0, 4): $model->examyear;
        $extra = "onChange=\" return btn_submit('changeTest');\"";
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->examyear, $opt, $extra, 1);

        //入試種別
        $maxTestDiv = $db->getOne(knjl190dQuery::getMaxTestDiv($model->examyear));
        $model->testdiv = ($model->testdiv) ? $model->testdiv: $maxTestDiv;
        $query = knjl190dQuery::getTestDivList($model->examyear);
        $extra = "onchange=\"return btn_submit('changeTest')\"";
        makeCmb($objForm, $arg, $db, $query, $model->testdiv, "TESTDIV", $extra, 1);

        //志望区分
        $model->desirediv = ($model->desirediv) ? $model->desirediv: "1";
        $query = knjl190dQuery::getNameMst($model->examyear, "L058");
        $extra = "onchange=\"return btn_submit('changeTest')\"";
        makeCmb($objForm, $arg, $db, $query, $model->desirediv, "DESIREDIV", $extra, 1);

        //送付先(1:志願者 2:出身校)
        $opt = array(1, 2);
        $model->field["SEND_TO"] = ($model->field["SEND_TO"] == "") ? "1" : $model->field["SEND_TO"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SEND_TO{$val}\" onClick=\"btn_submit('changeRadio')\"");
        }
        $radioArray = knjCreateRadio($objForm, "SEND_TO", $model->field["SEND_TO"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //通知種別
        $opt = array();
        if ($model->field["SEND_TO"] == '1') {
            $opt[] = array('label' => '1:合格通知書',               'value' => '1');
            $opt[] = array('label' => '2:合否通知書（不合格通知）', 'value' => '2');
            $opt[] = array('label' => '3:転籍許可通知書',           'value' => '3');
            $opt[] = array('label' => '4:転籍不許可通知書',         'value' => '4');
        } else {
            $opt[] = array('label' => '5:入学試験（通信制課程）の選考結果について', 'value' => '5');
            $opt[] = array('label' => '6:生徒転入学について（合格者）'            , 'value' => '6');
            $opt[] = array('label' => '9:生徒転籍について（合格者）'              , 'value' => '9');
            $opt[] = array('label' => '7:転入学試験の選考結果について（不合格者）', 'value' => '7');
            $opt[] = array('label' => '8:転籍試験の選考結果について（不合格者）'  , 'value' => '8');
        }
        $model->field["FORM_KIND"] = ($model->field["FORM_KIND"] != '') ? $model->field["FORM_KIND"]: $opt[0]["value"];
        $model->field["FORM_KIND"] = ($model->cmd == 'changeRadio') ? $opt[0]["value"]: $model->field["FORM_KIND"];
        $extra = "onChange=\" return btn_submit('knjl190d');\"";
        $arg["data"]["FORM_KIND"] = knjCreateCombo($objForm, "FORM_KIND", $model->field["FORM_KIND"], $opt, $extra, 1);

        if ($model->field["FORM_KIND"] == '1') {
            $arg["form_kind1"] = 1;
        } elseif ($model->field["FORM_KIND"] == '3') {
            $arg["form_kind3"] = 1;
        } elseif ($model->field["FORM_KIND"] == '6' || $model->field["FORM_KIND"] == '9') {
            $arg["form_kind6"] = 1;
            if ($model->field["FORM_KIND"] == '9') {
                $arg["data"]["TENNYUGAKU_DATE_TITLE"] = "転籍許可日　";
            }
        }
        if ($model->field["SEND_TO"] == '2') {
            $arg["form_kind5to8"] = 1;
        }
        if ($arg["data"]["TENNYUGAKU_DATE_TITLE"] == "") {
            $arg["data"]["TENNYUGAKU_DATE_TITLE"] = "転入学許可日";
        }

        //通知書発行日（1～8）
        $defNoDate = $db->getOne(knjl190dQuery::getNameMstSpare($model, "1"));
        $model->noticeDate = ($model->noticeDate) ? $model->noticeDate: str_replace("-", "/", $defNoDate);
        if ($model->cmd == "changeTest") {
            $model->noticeDate = str_replace("-", "/", $defNoDate);
        }
        $arg["data"]["NOTICE_DATE"] = View::popUpCalendarAlp($objForm, "NOTICE_DATE", $model->noticeDate, $disabled, "");

        //納入締切日（1）
        $defTrDate1 = $db->getOne(knjl190dQuery::getNameMstSpare($model, "2"));
        $model->transferDate1 = ($model->transferDate1) ? $model->transferDate1: str_replace("-", "/", $defTrDate1);
        if ($model->cmd == "changeTest") {
            $model->transferDate1 = str_replace("-", "/", $defTrDate1);
        }
        $arg["data"]["TRANSFER_DATE1"] = View::popUpCalendarAlp($objForm, "TRANSFER_DATE1", $model->transferDate1, "", "");

        //転入学日（1）
        $model->field["TENNYUGAKU_DATE"] = ($model->field["TENNYUGAKU_DATE"] == '') ? str_replace('-', '/', CTRL_DATE): $model->field["TENNYUGAKU_DATE"];
        $arg["data"]["TENNYUGAKU_DATE"] = View::popUpCalendarAlp($objForm, "TENNYUGAKU_DATE", $model->field["TENNYUGAKU_DATE"], "", "");

        //転籍許可日（3）
        $model->field["TENSEKI_DATE"] = ($model->field["TENSEKI_DATE"] == '') ? str_replace('-', '/', CTRL_DATE): $model->field["TENSEKI_DATE"];
        $arg["data"]["TENSEKI_DATE"] = View::popUpCalendarAlp($objForm, "TENSEKI_DATE", $model->field["TENSEKI_DATE"], "", "");

        //日本スポーツ振興センター加入証明書checkbox（6）
        $extra = "id=\"SPORT_CERTIF\" checked";
        $arg["data"]["SPORT_CERTIF"] = knjCreateCheckBox($objForm, "SPORT_CERTIF", "1", $extra);

        //時候の挨拶（5～8）
        $query = knjl190dQuery::getGreet();
        $extra = " onchange=\"return btn_submit('knjl190d')\"";
        $model->field["MONTH"] = ($model->field["MONTH"] == '') ? substr(CTRL_DATE, 5, 2): $model->field["MONTH"];
        makeCmb($objForm, $arg, $db, $query, $model->field["MONTH"], "MONTH", $extra, 1, "");
        $query = knjl190dQuery::getGreet($model->field["MONTH"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["GREET"], "GREET", $extra, 1, "");

        /******************/
        /** List to List **/
        /******************/
        //合格者一覧リストToリスト
        $row1 = array();
        $query = knjl190dQuery::getPassList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }

        //クラス一覧作成
        $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
        $arg["data"]["PASS_LIST"] = knjCreateCombo($objForm, "PASS_LIST", "", $row1, $extra, 20);

        //出力対象作成
        $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
        $arg["data"]["SPORT_SELECTED"] = knjCreateCombo($objForm, "SPORT_SELECTED", "", array(), $extra, 20);

        // << ボタン作成
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
        // ＜ ボタン作成
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
        // ＞ ボタン作成
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
        // >> ボタン作成
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
        /******************/

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->examyear);
        knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantdiv);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL190D");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "IMAGEPATH", $model->control["LargePhotoPath"]);
        knjCreateHidden($objForm, "SELECTED_DATA");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl190dForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == "" && $row["NAMESPARE2"] == '1') {
            $value = $row["VALUE"];
        }
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
