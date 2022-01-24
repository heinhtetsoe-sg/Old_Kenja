<?php

require_once('for_php7.php');


class knjl410Form1
{
    public function main(&$model)
    {
        $objForm = new form;

        $arg["start"]    = $objForm->get_start("main", "POST", "knjl410index.php", "", "main");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB接続
        $db = Query::dbCheckOut();

        global $sess;
        //一覧表示
        if (!isset($model->warning) && $model->cmd != "fuban" && $model->cmd != "fubanClear" && $model->recruitNo && $model->cmd != "changeKind") {
            //データを取得
            $query = knjl410Query::getRecruitDat($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } elseif ($model->cmd != "fuban" && $model->cmd != "fubanClear" && $model->recruitNo) {
            $row =& $model->field;
        } elseif ($model->cmd == "fubanClear" || $model->cmd == "fuban") {
            if ($model->cmd == 'fubanClear') {
                $row = array();
            } else {
                $row =& $model->field;
            }
            $setRecruitNo = knjl410Query::getSinkiNo($db);
            $model->recruitNo = $setRecruitNo + 1;
            knjl410Query::insRecruitMaxRecuitNoDat($db, $model->recruitNo);
            $row["RECRUIT_NO"] = $model->recruitNo;
        }

        if ($row["YEAR"] != '' && $row["YEAR"] < (CTRL_YEAR + 1)) {
            $arg["BK"] = "bgcolor=\"yellow\"";
            $arg["TOUROKU_COMMENT"] = $row["YEAR"]."年度のデータです。追加ボタンで登録してください。";
        } else {
            $arg["BK"] = "bgcolor=\"white\"";
        }

        $query = knjl410Query::getA023();
        $model->schoolKindArray = array();
        $result = $db->query($query);
        while ($schoolRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->schoolKindArray[$schoolRow["NAME1"]] = $schoolRow["NAME1"];
        }

        //校種
        $defKind = $model->schoolKindArray["J"] ? "1" : "2";
        if ($row["SCHOOL_KIND"]) {
            $row["SCHOOL_KIND"] = $row["SCHOOL_KIND"] == "J" || $row["SCHOOL_KIND"] == "1" ? "1" : "2";
        } else {
            $row["SCHOOL_KIND"] = $defKind;
        }
        $opt = array(1, 2);
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SCHOOL_KIND{$val}\" onClick=\"btn_submit('changeKind')\"");
        }
        $radioArray = knjCreateRadio($objForm, "SCHOOL_KIND", $row["SCHOOL_KIND"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //管理番号
        $extra = "readOnly style=\"background-color=#aaaaaa\"";
        $arg["data"]["RECRUIT_NO"] = knjCreateTextBox($objForm, $row["RECRUIT_NO"], "RECRUIT_NO", 8, 8, $extra);

        //氏名
        $extra = "";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $row["NAME"], "NAME", 44, 40, $extra);

        //氏名かな
        $extra = "";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $row["NAME_KANA"], "NAME_KANA", 44, 80, $extra);

        //性別
        $query = knjl410Query::getNameMst($model, "Z002");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $row["SEX"], "SEX", $extra, 1, "BLANK");

        //生年月日
        $extra = "";
        $value = ($row["BIRTHDAY"] == "") ? "" : str_replace("-", "/", $row["BIRTHDAY"]);
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar($objForm, "BIRTHDAY", $value);

        //出身学校
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $row["FINSCHOOLCD"], "FINSCHOOLCD", 7, 7, $extra);

        //学年
        $query = knjl410Query::getGrade($model->schoolKindHenkan[$row["SCHOOL_KIND"]]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $row["GRADE"], "GRADE", $extra, 1, "BLANK");

        //担任
        $extra = "";
        $arg["data"]["SCHOOL_TEACHER"] = knjCreateTextBox($objForm, $row["SCHOOL_TEACHER"], "SCHOOL_TEACHER", 34, 40, $extra);

        //かな検索ボタン（出身学校）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=&fscdname=&fsname=&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);

        $query = knjl410Query::getFinSchoolName($row["FINSCHOOLCD"]);
        $setFin = $db->getOne($query);
        $arg["data"]["FINSCHOOL_NAME"] = $setFin;

        //塾
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["PRISCHOOLCD"] = knjCreateTextBox($objForm, $row["PRISCHOOLCD"], "PRISCHOOLCD", 7, 7, $extra);

        //教室コード
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["PRISCHOOL_CLASS_CD"] = knjCreateTextBox($objForm, $row["PRISCHOOL_CLASS_CD"], "PRISCHOOL_CLASS_CD", 7, 7, $extra);

        //塾先生
        $extra = "";
        $arg["data"]["PRISCHOOL_TEACHER"] = knjCreateTextBox($objForm, $row["PRISCHOOL_TEACHER"], "PRISCHOOL_TEACHER", 44, 40, $extra);

        //かな検索ボタン（塾）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_PRISCHOOL/knjwpri_searchindex.php?cmd=&pricdname=&priname=&priaddr=&prischool_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_pri_kana_reference"] = knjCreateBtn($objForm, "btn_pri_kana_reference", "検 索", $extra);

        $query = knjl410Query::getPriSchoolName($row["PRISCHOOLCD"]);
        $setFin = $db->getOne($query);
        $arg["data"]["PRISCHOOL_NAME"] = $setFin;

        $query = knjl410Query::getPriSchoolClassName($row["PRISCHOOLCD"], $row["PRISCHOOL_CLASS_CD"]);
        $setFin = $db->getOne($query);
        $arg["data"]["PRISCHOOL_CLASS_NAME"] = $setFin;

        //郵便番号
        $arg["data"]["ZIPCD"] = View::popUpZipCode($objForm, "ZIPCD", $row["ZIPCD"], "ADDR1");

        //保護者氏名
        $extra = "";
        $arg["data"]["GUARD_NAME"] = knjCreateTextBox($objForm, $row["GUARD_NAME"], "GUARD_NAME", 26, 40, $extra);

        //保護者かな
        $extra = "";
        $arg["data"]["GUARD_KANA"] = knjCreateTextBox($objForm, $row["GUARD_KANA"], "GUARD_KANA", 26, 80, $extra);

        //住所１
        $extra = "";
        $arg["data"]["ADDR1"] = knjCreateTextBox($objForm, $row["ADDR1"], "ADDR1", 44, 80, $extra);

        //住所２
        $extra = "";
        $arg["data"]["ADDR2"] = knjCreateTextBox($objForm, $row["ADDR2"], "ADDR2", 44, 80, $extra);

        //希望
        $query = knjl410Query::getHopeData($model);
        $hope = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $sp = "";
        for ($i = 1; $i <= 5; $i++) {
            $hopeName = "";
            if ($hope["COURSENAME{$i}"]) {
                $hopeName = "第{$i}希望：";
            }
            //希望学科
            $arg["data"]["HOPE_COURSE_MAJOR{$i}"] = $sp.$hopeName.$hope["COURSENAME{$i}"].$hope["MAJORNAME{$i}"];
            //希望コース
            $arg["data"]["HOPE_COURSECODE{$i}"] = $hope["COURSECODENAME{$i}"];
            $sp = $hope["COURSENAME{$i}"] ? "　" : "";
        }

        //電話番号
        $extra = "onblur=\"this.value=toTelNo(this.value)\";";
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $row["TELNO"], "TELNO", 14, 14, $extra);

        //携帯番号
        $extra = "onblur=\"this.value=toTelNo(this.value)\";";
        $arg["data"]["TELNO2"] = knjCreateTextBox($objForm, $row["TELNO2"], "TELNO2", 14, 14, $extra);

        //FAX
        $extra = "onblur=\"this.value=toTelNo(this.value)\";";
        $arg["data"]["FAXNO"] = knjCreateTextBox($objForm, $row["FAXNO"], "FAXNO", 14, 14, $extra);

        //E-mail
        $extra = "onblur=\"this.value=checkEmail(this.value)\";";
        $arg["data"]["EMAIL"] = knjCreateTextBox($objForm, $row["EMAIL"], "EMAIL", 50, 120, $extra);

        //備考
        $extra = "onkeyup =\"charCount(this.value, 2, (40 * 2), true);\" oncontextmenu =\"charCount(this.value, 2, (40 * 2), true);\"";
        $arg["data"]["REMARK"] = knjCreateTextArea($objForm, "REMARK", "2", "80", "wrap", $extra, $row["REMARK"]);

        //イベントデータ表示
        eventInfo($objForm, $arg, $db, $model);

        //発送物一覧表示
        sendInfo($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db, $row);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "kousinZumi") {
            $arg["reload"]  = "parent.left_frame.btn_submit('searchUpd');";
        }

        View::toHTML($model, "knjl410Form1.html", $arg);
    }
}

//イベント表示
function eventInfo(&$objForm, &$arg, $db, &$model)
{
    $query = knjl410Query::getEventInfoData($model);
    $result = $db->query($query);

    $setData = array();
    $dataCnt = 1;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["KOUBAN"] = $dataCnt;
        $row["TOUROKU_YMD"] = str_replace("-", "/", $row["TOUROKU_DATE"]);
        $arg["eventData"][] = $row;
        $dataCnt++;
    }
    $result->free();
}

//発送物一覧表示
function sendInfo(&$objForm, &$arg, $db, &$model)
{
    $query = knjl410Query::getSendInfoData($model);
    $result = $db->query($query);

    $setData = array();
    $dataCnt = 1;
    $model->sendDelkey = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["KOUBAN"] = $dataCnt;
        list($year, $month, $day) = preg_split("/-/", $row["SEND_DATE"]);
        $row["SEND_YM"] = $month."/".$day;
        //checkbox
        $name = $row["YEAR"]."-".$row["EVENT_CLASS_CD"]."-".$row["EVENT_CD"]."-".$row["SEND_CD"]."-".$row["SEND_COUNT"]."-".$row["RECRUIT_NO"];
        $model->sendDelkey[] = $name;
        $extra = "";
        $row["DELCHECK"] = knjCreateCheckBox($objForm, "DELCHECK_".$name, "1", $extra);

        $arg["sendData"][] = $row;
        $dataCnt++;
    }
    $result->free();
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($name == 'SEX') {
            if ($value == "" && $row["NAMESPARE2"] == '1') {
                $value = $row["VALUE"];
            }
        }
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, &$model, $db, $row)
{
    //新規ボタンの有効・無効の切換
    if ($model->cmd == "fubanClear" || $model->cmd == "fuban") {
        $model->fubanMukou = "1"; //無効
    } elseif ($model->fubanMukou == "1" && $model->cmd == "changeKind") {
        //無効のまま
    } elseif ($model->fubanMukou == "1" && isset($model->warning) && $model->cmd == "kousinZumi") {
        //無効のまま
    } else {
        $model->fubanMukou = ""; //有効
    }
    $disFuban = ($model->fubanMukou == "1") ? " disabled" : "";
    $disAdd = ($model->fubanMukou == "1") ? "" : " disabled";
    //過去の生徒選択時
    if ($row["YEAR"] != '' && $row["YEAR"] < (CTRL_YEAR + 1)) {
        $disAdd = "";
    }

    //追加
    if ($model->recruitNo == '') {
        $extra  = "onclick=\"return btn_submit('fuban');\"";
    } else {
        $extra  = "onclick=\"return btn_submit('fubanClear');\"";
    }
    $arg["button"]["btn_fuban"] = knjCreateBtn($objForm, "btn_fuban", "新 規", $extra.$disFuban);

    //イベント登録
    $extra  = " onClick=\" wopen('".REQUESTROOT."/L/KNJL411/knjl411index.php?";
    $extra .= "SEND_PRGRID=KNJL410";
    $extra .= "&SEND_RECRUIT_NO=".$model->recruitNo."&cmd=";
    $extra .= "&SEND_AUTH=".$model->auth;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\" style=\"width:150px;\"";
    $arg["button"]["btn_event"] = knjCreateBtn($objForm, "btn_event", "イベント受付登録", $extra.$disFuban);

    //Z010のNAME1を取得
    $z010Name1 = $db->getOne(knjl410Query::getZ010());

    //来校者情報登録
    if ($row["SCHOOL_KIND"] == "2" && $z010Name1 == "bunkyo") {
        $extra  = " onClick=\" wopen('".REQUESTROOT."/L/KNJL410_1/knjl410_1index.php?";
        $extra .= "SEND_PRGRID=KNJL410";
        $extra .= "&SEND_RECRUIT_NO=".$model->recruitNo."&cmd=";
        $extra .= "&SEND_AUTH=".$model->auth;
        $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\" style=\"width:150px;\"";
        $arg["button"]["btn_visit"] = knjCreateBtn($objForm, "btn_visit", "来校者情報登録", $extra.$disFuban);
    }

    //相談登録
    if ($z010Name1 == "bunkyo") {
        $extra  = " onClick=\" wopen('".REQUESTROOT."/L/KNJL410_2/knjl410_2index.php?";
        $extra .= "SEND_PRGRID=KNJL410";
        $extra .= "&SEND_RECRUIT_NO=".$model->recruitNo."&cmd=";
        $extra .= "&SEND_AUTH=".$model->auth;
        $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\" style=\"width:150px;\"";
        $arg["button"]["btn_consult"] = knjCreateBtn($objForm, "btn_consult", "相談登録", $extra.$disFuban);
    }

    //追加
    $extra  = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra.$disAdd);
    //更新
    $extra  = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_upd"] = knjCreateBtn($objForm, "btn_upd", "更 新", $extra.$disFuban);
    //削除
    $extra  = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra.$disFuban);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra.$disFuban);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
    //削除
    $extra  = "onclick=\"return btn_submit('sendDel');\"";
    $arg["button"]["btn_sendDel"] = knjCreateBtn($objForm, "btn_sendDel", "削 除", $extra.$disFuban);
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR", (CTRL_YEAR + 1));
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "PROGRAMID", "KNJL410");
    knjCreateHidden($objForm, "PRGID", "KNJL410");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SEARCH_DIV", $model->search_div);
    knjCreateHidden($objForm, "RECRUIT_YEAR", $model->recruitYear);
    //住所検索のエラー回避用
    knjCreateHidden($objForm, "PREF_CD");
    knjCreateHidden($objForm, "FAMILY_REGISTER");
}

?>
