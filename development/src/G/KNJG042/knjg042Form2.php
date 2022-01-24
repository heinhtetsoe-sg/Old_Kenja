<?php

require_once('for_php7.php');

class knjg042Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjg042index.php", "", "edit");

        //データを取得
        $PERM_data = knjg042Query::getRow_data($model->field);

        //DB接続
        $db = Query::dbCheckOut();

        //申請区分
        $perm = $db->getRow(knjg042Query::getNameMst("G100", $PERM_data["APPLYCD"]), DB_FETCHMODE_ASSOC);
        $arg["data"]["APPLYCD"] = ($PERM_data["APPLYCD"] == "") ? "" : $perm["LABEL"];
        knjCreateHidden($objForm, "APPLYCD", $PERM_data["APPLYCD"]);

        //申請日付
        $arg["data"]["APPLY_CAL"] = str_replace("-","/",$PERM_data["APPLYDAY"]);
        knjCreateHidden($objForm, "applyday", str_replace("-","/",$PERM_data["APPLYDAY"]));

        //許可区分
        $perm = $db->getRow(knjg042Query::getNameMst("G101", $PERM_data["PERM_CD"]), DB_FETCHMODE_ASSOC);
        $arg["data"]["PERM"] = ($PERM_data["PERM_CD"] == "") ? "" : $perm["NAME1"];
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
        $arg["data"]["START"] = str_replace("-","/",$Start_day);
        knjCreateHidden($objForm, "sdate", str_replace("-","/",$Start_day));
        //自（時）
        $arg["data"]["HOUR1"] = $S_hour;
        //自（分）
        $arg["data"]["MINUTE1"] = $S_miniute;

        //至（日付）
        $arg["data"]["FINISH"] = str_replace("-","/",$Finish_day);
        knjCreateHidden($objForm, "edate", str_replace("-","/",$Finish_day));
        //至（時）
        $arg["data"]["HOUR2"] = $F_hour;
        //至（分）
        $arg["data"]["MINUTE2"] = $F_miniute;

        //所要（時）
        $arg["data"]["TOTAL_HOURS"] = $PERM_data["HOURS"];
        //所要（分）
        $arg["data"]["TOTAL_MINUTES"] = $PERM_data["MINUTES"];

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
                                "extrahtml"   => "disabled",
                                "value"       => $PERM_data["VACATION"] ));

            //textarea(2_1)引率
            $objForm->ae( array("type"        => "textarea",
                                "name"        => "LEADING",
                                "rows"        => "3",
                                "cols"        => 40,
                                "extrahtml"   => "disabled",
                                "value"       => $PERM_data["GUIDE"] ));
        
            //textarea(2_1_1)生徒数
            $objForm->ae( array("type"        => "text",
                                "name"        => "ST_NUM",
                                "size"        => 4,
                                "extrahtml"   => "disabled STYLE=\"text-align: right\"",
                                "value"       => $PERM_data["GUIDE_NUM"] ));

            //textarea(2_2)出張
            $objForm->ae( array("type"        => "textarea",
                                "name"        => "BUSSI_TRIP",
                                "rows"        => "3",
                                "cols"        => 40,
                                "extrahtml"   => "disabled",
                                "value"       => $PERM_data["BUSINESSTRIP"] ));

            $arg["data"]["REASON"] = "引率 ".$objForm->ge("LEADING")."　生徒 ".$objForm->ge("ST_NUM")."名<BR>".
                                     "出張 ".$objForm->ge("BUSSI_TRIP");

            //textarea(3)その他
            $objForm->ae( array("type"        => "textarea",
                                "name"        => "OTHERS",
                                "rows"        => "2",
                                "cols"        => 25,
                                "extrahtml"   => "disabled",
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
                                "extrahtml"   => "disabled",
                                "value"       => $PERM_data["VACATION"] ));

            //textarea(3)その他
            $objForm->ae( array("type"        => "text",
                                "name"        => "CALL_NAME",
                                "size"        => 20,
                                "maxlength"   => 10,
                                "extrahtml"   => "disabled",
                                "value"       => $PERM_data["CALL_NAME"] ));
        
            //textarea(3_1)電話番号
            $objForm->ae( array("type"        => "text",
                                "name"        => "CNTCT_TELNO",
                                "size"        => 14,
                                "maxlength"   => 14,
                                "extrahtml"   => "disabled",
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
                                "extrahtml"   => "disabled",
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
                                "extrahtml"   => "disabled",
                                "value"       => $PERM_data["VACATIONREASON"] ));
            $arg["data"]["REASON"] = $objForm->ge("REASON");
        }

        if ($PERM_data["PERM_CD"] != "") {
            //更新ボタン（申請中／許可／却下）
            $btn = "";
            $query = knjg042Query::getNameMst("G101");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($PERM_data["PERM_CD"] == $row["VALUE"]) continue;
                $cmd = 'update'.$row["VALUE"];
                $extra  = "onclick=\"return btn_submit('".$cmd."');\"";
                $extra .= ($row["VALUE"] == "2") ? " style=\"height:40px;background:pink;color:red;font:bold\"" : " style=\"height:40px;font:bold\"";
                $btn .= knjCreateBtn($objForm, "btn_update".$row["VALUE"], $row["NAME1"], $extra);
            }
            $arg["button"]["btn_update"] = $btn;
            //印刷ボタン
            $extra = "onclick=\"return newwin('". SERVLET_URL ."');\"";
            $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
        }
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "NENDO", CTRL_YEAR);
        knjCreateHidden($objForm, "APPLYDAY", str_replace("/","-",$PERM_data["APPLYDAY"]));
        knjCreateHidden($objForm, "STAFFCD", $PERM_data["STAFFCD"]);
        knjCreateHidden($objForm, "SDATE", str_replace("/","-",$PERM_data["SDATE"]));
        knjCreateHidden($objForm, "EDATE", str_replace("/","-",$PERM_data["EDATE"]));
        knjCreateHidden($objForm, "PRGID", "PROGRAMID");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $PERM_data["UPDATED"]);
        knjCreateHidden($objForm, "PRINTAPPLYCD", $PERM_data["APPLYCD"]);
        knjCreateHidden($objForm, "PRINTAPPLYDAY", str_replace("-","/",$PERM_data["APPLYDAY"]));
        knjCreateHidden($objForm, "PRINTPERMCD", $PERM_data["PERM_CD"]);
        knjCreateHidden($objForm, "PRINTSTAFFCD", $PERM_data["STAFFCD"]);
        knjCreateHidden($objForm, "PRINTSTARTDAY", str_replace("-","/",$Start_day));
        knjCreateHidden($objForm, "PRINTENDDAY", str_replace("-","/",$Finish_day));
        knjCreateHidden($objForm, "PRINTFLG");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"] = "window.open('knjg042index.php?cmd=list&perm_div=".$PERM_data["PERM_CD"]."','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjg042Form2.html", $arg); 
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
