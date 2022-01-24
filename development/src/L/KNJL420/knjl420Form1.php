<?php

require_once('for_php7.php');


class knjl420Form1
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"]    = $objForm->get_start("main", "POST", "knjl420index.php", "", "main");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB接続
        $db = Query::dbCheckOut();

        global $sess;
        //一覧表示
        if (!isset($model->warning) && $model->prischoolCd) {
            //データを取得
            $query = knjl420Query::getPriSchoolInfo($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            //データを取得
            $query = knjl420Query::getPriSchoolInfo($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $row["DIRECT_MAIL_FLG"] = $model->field["DIRECT_MAIL_FLG"];
            $row["STAFF_NAME"] = $model->field["STAFF_NAME"];
            $row["MOBILE_PHONE_NUMBER"] = $model->field["MOBILE_PHONE_NUMBER"];
            $row["EMAIL"] = $model->field["EMAIL"];
        }

        $arg["data"] = $row;

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR."年度";

        $query = knjl420Query::getRecruitPsDatCnt($model);
        $psDatCnt = $db->getOne($query);
        if ($psDatCnt > 0) {
            $arg["data"]["DATA_ARI"] = "登録済";
        }

        //DM不可
        $extra  = "id=\"DIRECT_MAIL_FLG\"";
        $extra .= ($row["DIRECT_MAIL_FLG"] == "1") ? " checked": "";
        $arg["data"]["DIRECT_MAIL_FLG"] = knjCreateCheckBox($objForm, "DIRECT_MAIL_FLG", "1", $extra);

        //担当者
        $extra = "";
        $arg["data"]["STAFF_NAME"] = knjCreateTextBox($objForm, $row["STAFF_NAME"], "STAFF_NAME", 50, 75, $extra);

        //携帯番号
        $extra = "onblur=\"this.value=toTelNo(this.value)\";";
        $arg["data"]["MOBILE_PHONE_NUMBER"] = knjCreateTextBox($objForm, $row["MOBILE_PHONE_NUMBER"], "MOBILE_PHONE_NUMBER", 14, 14, $extra);

        //E-mail
        $extra = "onblur=\"this.value=checkEmail(this.value)\";";
        $arg["data"]["EMAIL"] = knjCreateTextBox($objForm, $row["EMAIL"], "EMAIL", 50, 120, $extra);

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

        if ($model->cmd == "kousinZumi"){
            $arg["reload"]  = "parent.left_frame.btn_submit('searchUpd');";
        }

        View::toHTML($model, "knjl420Form1.html", $arg);
    }
}

//イベント表示
function eventInfo(&$objForm, &$arg, $db, &$model)
{
    $query = knjl420Query::getEventInfoData($model);
    $result = $db->query($query);

    $setData = array();
    $dataCnt = 1;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
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
    $query = knjl420Query::getSendInfoData($model);
    $result = $db->query($query);

    $setData = array();
    $dataCnt = 1;
    $model->sendDelkey = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
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
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, &$model, $db, $row)
{
    //説明会受付登録
    $extra  = " onClick=\" wopen('".REQUESTROOT."/L/KNJL421/knjl421index.php?";
    $extra .= "SEND_PRGRID=KNJL420";
    $extra .= "&SEND_RECRUIT_NO=".$model->recruitNo."&cmd=";
    $extra .= "&SEND_PRISCHOOLCD=".$model->prischoolCd;
    $extra .= "&SEND_PRISCHOOL_CLASS_CD=".$model->prischoolClassCd;
    $extra .= "&SEND_AUTH=".$model->auth;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\" style=\"width:150px;\"";
    $arg["button"]["btn_event"] = knjCreateBtn($objForm, "btn_event", "説明会受付登録", $extra.$disFuban);

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
    knjCreateHidden($objForm, "PROGRAMID", "KNJL420");
    knjCreateHidden($objForm, "PRGID", "KNJL420");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SEARCH_DIV", $model->search_div);
    knjCreateHidden($objForm, "RECRUIT_YEAR", $model->recruitYear);
}

?>
