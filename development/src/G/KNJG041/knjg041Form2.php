<?php

require_once('for_php7.php');

class knjg041Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjg041index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->cmd != "apply") {
            //データを取得
            $PERM_data = knjg041Query::getRow_data($model->field, STAFFCD);
        } else {
            $PERM_data =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //申請区分コンボボックス
        $query = knjg041Query::getNameMst("G100");
        $extra = "onchange=\"return btn_submit('apply');\"";
        makeCmb($objForm, $arg, $db, $query, "APPLYCD", $PERM_data["APPLYCD"], $extra, 1);
        if ($model->cmd == "apply") knjCreateHidden($objForm, "PRINTFLG", "error");

        //申請日付
        $arg["data"]["APPLY_CAL"] = View::popUpCalendar($objForm, "applyday", str_replace("-","/",$PERM_data["APPLYDAY"]));

        //許可区分
        $perm = $db->getRow(knjg041Query::getNameMst("G101", $PERM_data["PERM_CD"]), DB_FETCHMODE_ASSOC);
        $arg["data"]["PERM"] = $perm["NAME1"];
        knjCreateHidden($objForm, "PERM_CD", $PERM_data["PERM_CD"]);

        //期間-------
        //時、分をもとめる。
        $S_date = array();
        $F_date = array();
        $date_control = array();
        $C_4 = array();
        $Y_4 = array();
        $month_box = array("01" => 13, "02" => 14, "03" => 3, "04" => 4,
                           "05" => 5, "06" => 6, "07" => 7, "08" => 8,
                           "09" => 9, "10" => 10, "11" => 11,"12" => 12 ) ;

        //開始月
        $S_date = explode(" ",$PERM_data["SDATE"]); //時間と日付を分離
        $Start_day = $S_date[0];                    //日付を代入
        $date_control = explode("-",$Start_day);    //日付を分離 yyyy, mm, dd
        $S_month = $month_box[$date_control[1]];    //月を代入
        $S_year = (($S_month < 13)? $date_control[0] : $date_control[0] -1 );   //1月2月は前年度として計算
        $C = substr($S_year,0,2);                   //西暦の上二桁を抽出
        $C_4 = explode(".",($C/4));                 
        $Y = substr($S_year,2,2);                   //西暦の下二桁抽出
        $Y_4 = explode(".",($Y/4));                 
        $S_day = $date_control[2];                  //日を代入
        $S_month = explode(".",((26*($S_month + 1))/10));

        //終了月
        $F_date = explode(" ",$PERM_data["EDATE"]);
        $Finish_day = $F_date[0];
        $date_control = explode("-",$Finish_day);
        $F_month = $month_box[$date_control[1]];
        $F_year = (($F_month < 13)? $date_control[0] : $date_control[0] -1 );   //1月2月は前年度として計算
        $C = substr($F_year,0,2); //西暦の上二桁
        $C_4 = explode(".",($C/4));
        $Y = substr($F_year,2,2); //西暦の下二桁
        $Y_4 = explode(".",($Y/4));
        $F_day = $date_control[2];
        $F_month = explode(".",((26*($F_month + 1))/10));

        //時間
        $S_hour = substr($S_date[1],0,2);
        $S_miniute = substr($S_date[1],3,2);

        $F_hour = substr($F_date[1],0,2);
        $F_miniute = substr($F_date[1],3,2);

        //自（日付）
        $arg["data"]["START"] = View::popUpCalendar($objForm, "sdate", str_replace("-","/",$Start_day));
        //自（時）
        $extra = "onblur=\"return Number_check(this,'hour');\" onChange=\"return PrintCheck('this');\" STYLE=\"text-align: right\"";
        $arg["data"]["HOUR1"] = knjCreateTextBox($objForm, $S_hour, "HOUR1", 2, 2, $extra);
        //自（分）
        $extra = "onblur=\"return Number_check(this,'minute');\" onChange=\"return PrintCheck('this');\" STYLE=\"text-align: right\"";
        $arg["data"]["MINUTE1"] = knjCreateTextBox($objForm, $S_miniute, "MINUTE1", 2, 2, $extra);

        //至（日付）
        $arg["data"]["FINISH"] = View::popUpCalendar($objForm, "edate", str_replace("-","/",$Finish_day));
        //至（時）
        $extra = "onblur=\"return Number_check(this,'hour');\" onChange=\"return PrintCheck('this');\" STYLE=\"text-align: right\"";
        $arg["data"]["HOUR2"] = knjCreateTextBox($objForm, $F_hour, "HOUR2", 2, 2, $extra);
        //至（分）
        $extra = "onblur=\"return Number_check(this,'minute');\" onChange=\"return PrintCheck('this');\" STYLE=\"text-align: right\"";
        $arg["data"]["MINUTE2"] = knjCreateTextBox($objForm, $F_miniute, "MINUTE2", 2, 2, $extra);

        //所要（時）
        $extra = "onblur=\"return Number_check(this,'hours');\" onChange=\"return PrintCheck('this');\" STYLE=\"text-align: right\"";
        $arg["data"]["TOTAL_HOURS"] = knjCreateTextBox($objForm, $PERM_data["HOURS"], "TOTAL_HOURS", 2, 2, $extra);
        //所要（分）
        $extra = "onblur=\"return Number_check(this,'minutes');\" onChange=\"return PrintCheck('this');\" STYLE=\"text-align: right\"";
        $arg["data"]["TOTAL_MINUTES"] = knjCreateTextBox($objForm, $PERM_data["MINUTES"], "TOTAL_MINUTES", 2, 2, $extra);

        switch($PERM_data["APPLYCD"]) {
        case 3:
        case 4:
            //キャプション設定
            $arg["data"]["LOC_CAP"] = "目的地";
            $arg["data"]["REA_CAP"] = "用　件";
            $oth_cap = "備考";

            //textarea(1)場所
            $objForm->ae( array("type"        => "textarea",
                                "name"        => "LOCATION",
                                "rows"        => "4",
                                "cols"        => 50,
                                "extrahtml"   => "onChange=\"return PrintCheck('this');\"",
                                "value"       => $PERM_data["VACATION"] ));

            //textarea(2_1)引率
            $objForm->ae( array("type"        => "textarea",
                                "name"        => "LEADING",
                                "rows"        => "3",
                                "cols"        => 40,
                                "extrahtml"   => "onChange=\"return PrintCheck('this');\"",
                                "value"       => $PERM_data["GUIDE"] ));
        
            //textarea(2_1_1)生徒数
            $objForm->ae( array("type"        => "text",
                                "name"        => "ST_NUM",
                                "size"        => 4,
                                "extrahtml"   => "onblur=\"return Number_check(this,'num');\" onChange=\"return PrintCheck('this');\" STYLE=\"text-align: right\"",
                                "value"       => $PERM_data["GUIDE_NUM"] ));

            //textarea(2_2)出張
            $objForm->ae( array("type"        => "textarea",
                                "name"        => "BUSSI_TRIP",
                                "rows"        => "3",
                                "cols"        => 40,
                                "extrahtml"   => "onChange=\"return PrintCheck('this');\"",
                                "value"       => $PERM_data["BUSINESSTRIP"] ));

            $arg["data"]["REASON"] = "引率 ".$objForm->ge("LEADING")."　生徒 ".$objForm->ge("ST_NUM")."名<BR>".
                                     "出張 ".$objForm->ge("BUSSI_TRIP");

            //textarea(3)その他
            $objForm->ae( array("type"        => "textarea",
                                "name"        => "OTHERS",
                                "rows"        => "2",
                                "cols"        => 25,
                                "extrahtml"   => "onChange=\"return PrintCheck('this');\"",
                                "value"       => $PERM_data["REMARK"] ));

            $arg["data"]["OTHERS"] = "<tr><th class=\"no_search\" width=\"15%\" nowrap >".$oth_cap.
                                     "</th><td bgcolor=\"white\" >".$objForm->ge("OTHERS")."</td></tr>";

            break;

        case 5:
            //キャプション設定
            $arg["data"]["LOC_CAP"] = "旅行先<BR>(研修先)";
            $arg["data"]["REA_CAP"] = "目的理由<BR>(研修目的)";
            $oth_cap = "旅行(研修)<BR>中の連絡先<BR>電話番号";

            //textarea(1)場所
            $objForm->ae( array("type"        => "textarea",
                                "name"        => "LOCATION",
                                "rows"        => "2",
                                "cols"        => 48,
                                "extrahtml"   => "onChange=\"return PrintCheck('this');\"",
                                "value"       => $PERM_data["VACATION"] ));

            //textarea(3)その他
            $objForm->ae( array("type"        => "text",
                                "name"        => "CALL_NAME",
                                "size"        => 20,
                                "maxlength"   => 10,
                                "extrahtml"   => "onChange=\"return PrintCheck('this');\"",
                                "value"       => $PERM_data["CALL_NAME"] ));
        
            //textarea(3_1)電話番号
            $objForm->ae( array("type"        => "text",
                                "name"        => "CNTCT_TELNO",
                                "size"        => 14,
                                "maxlength"   => 14,
                                "extrahtml"   => "onChange=\"return PrintCheck('this');\"",
                                "value"       => $PERM_data["CALL_TELNO"] ));


            $arg["data"]["OTHERS"] = "<tr><th class=\"no_search\" width=\"15%\" nowrap >".$oth_cap.
                                     "</th><td bgcolor=\"white\" >".$objForm->ge("CALL_NAME")."<br>　TEL　：　".
                                     $objForm->ge("CNTCT_TELNO")."</td></tr>";
            break;

        default:
            //キャプション設定
            $arg["data"]["LOC_CAP"] = "休暇地";
            $arg["data"]["REA_CAP"] = "目的理由";


            //textarea(1)場所
            $objForm->ae( array("type"        => "textarea",
                                "name"        => "LOCATION",
                                "rows"        => "5",
                                "cols"        => 48,
                                "extrahtml"   => "onChange=\"return PrintCheck('this');\"",
                                "value"       => $PERM_data["VACATION"] ));

            break;
        }

        $arg["data"]["LOCATION"] = $objForm->ge("LOCATION");

        if ($PERM_data["APPLYCD"] != 3 && $PERM_data["APPLYCD"] != 4) {
            //textarea(2)理由
            $objForm->ae( array("type"        => "textarea",
                                "name"        => "REASON",
                                "rows"        => "5",
                                "cols"        => 48,
                                "extrahtml"   => "onChange=\"return PrintCheck('this');\"",
                                "value"       => $PERM_data["VACATIONREASON"] ));
            $arg["data"]["REASON"] = $objForm->ge("REASON");
        }

        //印刷ボタン
        $extra = "onclick=\"return newwin('". SERVLET_URL ."');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加(申請)", $extra);
        //更新ボタン
        $extra  = "onclick=\"return btn_submit('update');\"";
        $extra .= ($PERM_data["PERM_CD"] == 0) ? "" : " disabled";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除ボタン
        $extra  = "onclick=\"return btn_submit('delete');\"";
        $extra .= ($PERM_data["PERM_CD"] == 0) ? "" : " disabled";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "NENDO", CTRL_YEAR);
        knjCreateHidden($objForm, "APPLYDAY", str_replace("/","-",$PERM_data["APPLYDAY"]));
        knjCreateHidden($objForm, "STAFFCD", STAFFCD);
        knjCreateHidden($objForm, "SDATE", str_replace("/","-",$PERM_data["SDATE"]));
        knjCreateHidden($objForm, "EDATE", str_replace("/","-",$PERM_data["EDATE"]));
        knjCreateHidden($objForm, "PRGID", "PROGRAMID");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $PERM_data["UPDATED"]);
        knjCreateHidden($objForm, "PRINTAPPLYCD", $PERM_data["APPLYCD"]);
        knjCreateHidden($objForm, "PRINTAPPLYDAY", str_replace("-","/",$PERM_data["APPLYDAY"]));
        knjCreateHidden($objForm, "PRINTPERMCD", $model->field["PERM_CD"]);
        knjCreateHidden($objForm, "PRINTSTAFFCD", STAFFCD);
        knjCreateHidden($objForm, "PRINTSTARTDAY", str_replace("-","/",$Start_day));
        knjCreateHidden($objForm, "PRINTENDDAY", str_replace("-","/",$Finish_day));
        knjCreateHidden($objForm, "PRINTFLG");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"] = "window.open('knjg041index.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjg041Form2.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
