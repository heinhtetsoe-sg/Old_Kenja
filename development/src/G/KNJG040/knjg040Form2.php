<?php

require_once('for_php7.php');

class knjg040Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjg040index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (isset($model->staffcd) && !isset($model->warning)){
            //データを取得
            $PERM_data = knjg040Query::getRow_data($model->field,$model->staffcd);
        }else{
            $PERM_data =& $model->field;
        }

        //ヘッダー部作成
#        if($model->sec_competence == DEF_UPDATABLE){
            $Row_user = knjg040Query::getRow_user($model);
#        }
#        else{
#            $Row_user = knjg040Query::getRow_login_user($model);
#        }
            $arg["header"]["APPLY_DIV"]     = $model->apply_data[($model->apply_div - 1)]["NAMECD2"]."："
                                             .$model->apply_data[($model->apply_div - 1)]["NAME1"];
            $arg["header"]["SECTIONNAME"]   = $Row_user["SECTIONNAME"];
            $arg["header"]["JOBNAME"]       = $Row_user["JOBNAME"];
            $arg["header"]["STAFFNAME"]     = $Row_user["STAFFNAME"];

        //申請日付
        $arg["header"]["APPLY_CAL"] = View::popUpCalendar($objForm, "applyday", str_replace("-","/",$PERM_data["APPLYDAY"]));

        //許可区分コンボボックスを作成する
        $opt = array();
        $array_count = get_count($model->perm_data);
        if($array_count == 0){
            $opt[] =array("label" => "　　　　　　　　",
                          "value" => ""
                        );
        }else{
            for($i=0;$i<$array_count;$i++)
            {
                $opt[] = array("label" => htmlspecialchars($model->perm_data[$i]["NAMECD2"].":".
                                                           $model->perm_data[$i]["NAME1"]),
                               "value" => $model->perm_data[$i]["NAMECD2"]);
            }
        }

        $objForm->ae( array("type"      => "select",
                            "name"      => "PERM_CD",
                            "size"      => "1",
                            "value"     => (($model->field["PERM_CD"] == "")? 0 : $model->field["PERM_CD"]),
                            "extrahtml" => "onChange=\"return PrintCheck('this');\"",
                            "options"   => $opt));

        if($model->sec_competence == DEF_UPDATABLE){
            $arg["header"]["PERM"] = $objForm->ge("PERM_CD");
        }else{
            if($model->field["PERM_CD"] == 0){
                $arg["header"]["PERM"] = $model->perm_data[0]["NAME1"];
                $hidden_value = 0;
            }
            elseif($model->field["PERM_CD"] == 1){
                $arg["header"]["PERM"] = $model->perm_data[1]["NAME1"];
                $hidden_value = 1;
            }
            elseif($model->field["PERM_CD"] == 2){
                $arg["header"]["PERM"] = $model->perm_data[2]["NAME1"];
                $hidden_value = 2;
            }

            //hiddenを作成する
            $objForm->ae( array("type"      => "hidden",
                                "name"      => "PERM_CD",
                                "value"     => $hidden_value
                                ) );
        }

        //期間-------
        //曜日、時、分をもとめる。
        $S_date = array();
        $F_date = array();
        $date_control = array();
        $C_4 = array();
        $Y_4 = array();
        $week_box = array(1 => "日", 2 => "月", 3 => "火", 4 => "水", 5 => "木", 6 => "金", 7 => "土");
        $month_box = array("01" => 13, "02" => 14, "03" => 3, "04" => 4,
                           "05" => 5, "06" => 6, "07" => 7, "08" => 8,
                           "09" => 9, "10" => 10, "11" => 11,"12" => 12 ) ;

        //曜日計算
        //開始月
        $S_date = explode(" ",$PERM_data["SDATE"]); //時間と日付を分離
        $Start_day = $S_date[0];                    //日付を代入
        $date_control = explode("-",$Start_day);    //日付を分離 yyyy, mm, dd
        $S_month = $month_box[$date_control[1]];    //月を代入
        $S_year = (($S_month < 13)? $date_control[0] : $date_control[0] -1 );   //1月2月は前年度として計算
        $C = substr($S_year,0,2);                   //西暦の上二桁を抽出
        $C_4 = explode(".",((int)$C/4));                 
        $Y = substr($S_year,2,2);                   //西暦の下二桁抽出
        $Y_4 = explode(".",($Y/4));                 
        $S_day = $date_control[2];                  //日を代入
        $S_month = explode(".",((26*($S_month + 1))/10));
        $W = ($C_4[0] - (2 * (int)$C) + $Y_4[0] + $Y + $S_month[0] + $S_day);
        $start_week = $week_box[((($W %= 7) <= 0)? $W += 7 : $W )];

        //終了月
        $F_date = explode(" ",$PERM_data["EDATE"]);
        $Finish_day = $F_date[0];
        $date_control = explode("-",$Finish_day);
        $F_month = $month_box[$date_control[1]];
        $F_year = (($F_month < 13)? $date_control[0] : $date_control[0] -1 );   //1月2月は前年度として計算
        $C = substr($F_year,0,2); //西暦の上二桁
        $C_4 = explode(".",((int)$C/4));
        $Y = substr($F_year,2,2); //西暦の下二桁
        $Y_4 = explode(".",($Y/4));
        $F_day = $date_control[2];
        $F_month = explode(".",((26*($F_month + 1))/10));
        $W = ($C_4[0] - (2 * (int)$C) + $Y_4[0] + $Y + $F_month[0] + $F_day);
        $finish_week = $week_box[((($W %= 7) <= 0)? $W += 7 : $W )];

        //時間
        $S_hour = substr($S_date[1],0,2);
        $S_miniute = substr($S_date[1],3,2);

        $F_hour = substr($F_date[1],0,2);
        $F_miniute = substr($F_date[1],3,2);

        //自
        $arg["data"]["START"] = View::popUpCalendar($objForm, "sdate", str_replace("-","/",$Start_day));

        //曜日1
        $arg["data"]["WEEK1"] = $start_week;

        //時1
        $objForm->ae( array("type"        => "text",
                            "name"        => "HOUR1",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"return Number_check(this,'hour');\" onChange=\"return PrintCheck('this');\" STYLE=\"text-align: right\"",
                            "value"       => $S_hour ));

        $arg["data"]["HOUR1"] = $objForm->ge("HOUR1");

        //分1
        $objForm->ae( array("type"        => "text",
                            "name"        => "MINUTE1",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"return Number_check(this,'minute');\" onChange=\"return PrintCheck('this');\" STYLE=\"text-align: right\"",
                            "value"       => $S_miniute ));

        $arg["data"]["MINUTE1"] = $objForm->ge("MINUTE1");

        //至
        // echo htmlspecialchars(View::popUpCalendar($objForm, "FDATE", str_replace("-","/",$Finish_day)));
        $arg["data"]["FINISH"] = View::popUpCalendar($objForm, "edate", str_replace("-","/",$Finish_day));

        //曜日2
        $arg["data"]["WEEK2"] = $finish_week;

        //時2
        $objForm->ae( array("type"        => "text",
                            "name"        => "HOUR2",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"return Number_check(this,'hour');\" onChange=\"return PrintCheck('this');\" STYLE=\"text-align: right\"",
                            "value"       => $F_hour ));

        $arg["data"]["HOUR2"] = $objForm->ge("HOUR2");

        //分2
        $objForm->ae( array("type"        => "text",
                            "name"        => "MINUTE2",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"return Number_check(this,'minute');\" onChange=\"return PrintCheck('this');\" STYLE=\"text-align: right\"",
                            "value"       => $F_miniute ));

        $arg["data"]["MINUTE2"] = $objForm->ge("MINUTE2");


        //所要時
        $objForm->ae( array("type"        => "text",
                            "name"        => "TOTAL_HOURS",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"return Number_check(this,'hours');\" onChange=\"return PrintCheck('this');\" STYLE=\"text-align: right\"",
                            "value"       => $PERM_data["HOURS"] ));

        $arg["data"]["TOTAL_HOURS"] = $objForm->ge("TOTAL_HOURS");

        //所要分
        $objForm->ae( array("type"        => "text",
                            "name"        => "TOTAL_MINUTES",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"return Number_check(this,'minutes');\" onChange=\"return PrintCheck('this');\" STYLE=\"text-align: right\"",
                            "value"       => $PERM_data["MINUTES"] ));

        $arg["data"]["TOTAL_MINUTES"] = $objForm->ge("TOTAL_MINUTES");

        //可変のボックス表示
        //echo " this is APPLY_CoDe(get) ... ".$model->get_field["APPLYCD"]."<BR>";
        //echo " this is APPLY_CoDe(model) ... ".$model->apply_div."<BR>";

        switch($model->apply_div){
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

        if($model->apply_div != 3 && $model->apply_div != 4){
        //textarea(2)理由
        $objForm->ae( array("type"        => "textarea",
                            "name"        => "REASON",
                            "rows"        => "5",
                            "cols"        => 48,
                            "extrahtml"   => "onChange=\"return PrintCheck('this');\"",
                            "value"       => $PERM_data["VACATIONREASON"] ));
        $arg["data"]["REASON"] = $objForm->ge("REASON");
        }

        if($model->sec_competence == DEF_UPDATABLE){
            $disabled_add = "";
            $disabled = "";
        }
//        elseif($model->field["PERM_CD"] == 0 && $model->sec_competence == DEF_UPDATE_RESTRICT){
        elseif($model->sec_competence == DEF_UPDATE_RESTRICT){
            if($model->field["PERM_CD"] == 0){
               $disabled_add = "";
               $disabled = "";
               }
            else{
               $disabled_add = "";
               $disabled = "disabled";
            }
        }
        else{
            $disabled_add = 'disabled';
            $disabled = 'disabled';
        }

        //印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_print",
                            "value"     => "印 刷",
		                    "extrahtml" => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) ); //2004/04/27 add
							//2004/04/27 "extrahtml" => "onclick=\"return newwin();\"" ) );
        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //追加ボタンを作成する
        $strAdd = ($model->sec_competence == DEF_UPDATE_RESTRICT) ? "(申請)" : "";
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_add",
                            "value"     => "追 加" .$strAdd,
                            "extrahtml" => $disabled_add." onclick=\"return btn_submit('add');\""  ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //更新ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_udpate",
                            "value"     => "更 新",
                            "extrahtml" => $disabled." onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //削除ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_del",
                            "value"     => "削 除",
                            "extrahtml" => $disabled." onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //取消ボタンを作成する
        $objForm->ae( array("type"      => "reset",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return btn_submit('reset')\""  ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\"" ) );
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

/* データベース名：　ＤＢＮＡＭＥ
    処理年度　　　：ＮＥＮＤＯ
    申請日　　　　：ＡＰＰＬＹＤＡＹ
    申請区分　　　：ＡＰＰＬＹＣＤ
    職員コード　　：ＳＴＡＦＦＣＤ
    期間開始日　　：ＳＤＡＴＥ、 ＨＯＵＲ１、    ＭＩＮＵＴＥ１（わざわざ分解しなくてTIMESTAMP型でわたしても良い）
    開始終了日　　：ＥＤＡＴＥ　　ＨＯＵＲ２　　ＭＩＮＵＴＥ２（わざわざ分解しなくて
    TIMESTAMP型でわたしても良い）
    プログラムＩＤ：ＰＲＧＩＤ（”ＫＮＪＧ０４０”をいれる）
*/
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "NENDO",
                            "value"     => CTRL_YEAR
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "APPLYDAY",
                            "value"     => str_replace("/","-",$PERM_data["APPLYDAY"])
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "STAFFCD",
                            "value"     => STAFFCD
                            ) );
        //hiddenを作成する・期間開始日
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SDATE",
                            "value"     => str_replace("/","-",$PERM_data["SDATE"])
                            ) );

        //hiddenを作成する・期間終了日
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "EDATE",
                            "value"     => str_replace("/","-",$PERM_data["EDATE"])
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => PROGRAMID
                            ) );


        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $PERM_data["UPDATED"]
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "STAFFCD",
                            "value"     => $model->field["STAFFCD"]
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "APPLYCD",
                            "value"     => $model->apply_div
                            ) );

        //hiddenを作成する・申請区分（listから受け取った値）
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRINTAPPLYCD",
                            "value"     => $model->apply_div
                            ) );

        //hiddenを作成する・申請日付（listから受け取った値）
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRINTAPPLYDAY",
                            "value"     => str_replace("-","/",$PERM_data["APPLYDAY"])
                            ) );

        //hiddenを作成する・許可区分（listから受け取った値）
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRINTPERMCD",
                            "value"     => $model->field["PERM_CD"]
                            ) );

        //hiddenを作成する・職員コード（listから受け取った値）
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRINTSTAFFCD",
                            "value"     => $model->field["STAFFCD"]
                            ) );

        //hiddenを作成する・期間開始日（listから受け取った値）
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRINTSTARTDAY",
                            "value"     => str_replace("-","/",$Start_day)
                            ) );

        //hiddenを作成する・期間終了日（listから受け取った値）
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRINTENDDAY",
                            "value"     => str_replace("-","/",$Finish_day)
                            ) );

        //hiddenを作成する・チェックフラグ（画面上で値が変更されたかチェックするためのフラグ）
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRINTFLG",
                            "value"     => ""
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );



        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjg040index.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjg040Form2.html", $arg); 

    }
}
?>
