<?php

require_once('for_php7.php');

class knjd110dForm1
{   
    function main(&$model)
    {
        $objForm        = new form;
        $db = Query::dbCheckOut();
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd110dindex.php", "", "main");
        $arg["jscript"] = "";
        $arg["Closing"] = "";
        
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        //事前処理チェック
        if (!knjd110dQuery::ChecktoStart($db)) {
            $arg["Closing"] = " closing_window(2);";
        }

        //実行確認表示
        if (strlen($model->error_msg)) {
            $arg["confirm_msg"] = "ConfirmOnError('$model->error_msg');";
        }

        //処理年度
        $opt_year = $opt = $model->allMonth = array();
        $result = $db->query(knjd110dQuery::getYear());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_year[] = array("label" => $row["YEAR"]."年度",
                                "value" => $row["YEAR"]);
        }

        //初期値は処理年度
        if ($model->year == "") $model->year = CTRL_YEAR;

        $objForm->ae( array("type"      => "select",
                            "name"      => "year",
                            "value"     => $model->year,
                            "options"   => $opt_year,
                            "extrahtml" => "onChange=\"btn_submit('chg_year');\""));

        $arg["data"]["YEAR"] = $objForm->ge("year");

        //校種コンボ
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query = knjd110dQuery::getNameMstA023($model);
            $extra = "onChange=\"btn_submit('main')\";";
            $model->allSchoolKind = makeCmbSchoolKind($objForm, $arg, $db, $query, $model->school_kind, "SCHOOL_KIND", $extra, 1);
        }

        //処理月(各学期の期間の月のみをコンボにセット
        $result = $db->query(knjd110dQuery::getSemesterMonth($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            for ($i = 4; $i < 16; $i++) 
            {
                $mon = ($i<13) ? $i : ($i-12);

                if ($mon < 4) {
                    $year = (int)$model->year + 1;
                } else {
                    $year = $model->year;
                }

                //年と月を合わせて比較する
                if ((int)($year.sprintf("%02d",$mon)) >= (int)strftime("%Y%m",strtotime($row["SDATE"])) 
                && ((int)$year.sprintf("%02d",$mon)) <= (int)strftime("%Y%m",strtotime($row["EDATE"]))) 
                {
                    //月が学期の開始月または終了月かチェック
                    //開始月の場合は開始日以降その月末日まで集計
                    if ($mon == (int)strftime("%m",strtotime($row["SDATE"]))) {

                        $flg = "1";

                    //終了月の場合はその月の１日から終了日まで集計
                    } else if ($mon == (int)strftime("%m",strtotime($row["EDATE"]))) {

                        $flg = "2";

                    //それ以外はその月の１日から月末日まで集計
                    } else {

                        $flg = "0";
                    }

                    //初期値(学籍処理日の月にする）
                    if ($model->month == "") {
                        if ($mon == strftime("%m", strtotime(CTRL_DATE))) {
                            $model->month = $row["SEMESTER"]."-".sprintf("%02d",$mon)."-".$flg;
                        }
                    }

                    $opt[] = array("label"    =>$mon."月 ( ".$row["SEMESTERNAME"]." )",
                                   "value"    => $row["SEMESTER"]."-".sprintf("%02d",$mon)."-".$flg);

                    //未実施チェック用
                    $model->allMonth[] = $row["SEMESTER"]."-".sprintf("%02d",$mon)."-".$flg;
                }
            }
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "month",
                            "size"        => "1",
                            "extrahtml"   => "onchange=\"clearDay();\"",
                            "value"       => $model->month,
                            "options"     => $opt));
        $arg["data"]["MONTH"] = $objForm->ge("month");

        //日指定
        $objForm->ae( array("type"        => "text",
                            "name"        => "day",
                            "size"        => "2",
                            "maxlength"   => "2",
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value);\" style=\"text-align:center\"",
                            "value"       => $model->day));
        $arg["data"]["DAY"] = $objForm->ge("day");

        //出席制御日付
        $date = str_replace("-","/",$db->getOne(knjd110dQuery::getAttendDate()));
        $arg["data"]["LIMIT_DATE"] = View::popUpCalendar($objForm, "limit_date", $date);

        //夜間バッチ
        makeDoRun($objForm, $arg, $model);

        //生成済み一覧
        $result = $db->query(knjd110dQuery::GetList($model->year,$model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");

            $del_key = $row["YEAR"] ."-" .sprintf("%02d", $row["MONTH"]) ."-" .$row["SEMESTER"];
            $checked = "";
            if (get_count($model->del_check)) {
                foreach ($model->del_check as $val) {
                    if ($del_key == $val) $checked = "checked ";
                }
            }
            $objForm->ae( array("type"        => "checkbox",
                                "name"        => "DEL_CHECK",
                                "value"       => $del_key,
                                "multiple"    => "1",
                                "extrahtml"   => $checked ."id=\"" .$del_key ."\" "));
            $row["DEL_CHECK"] = $objForm->ge("DEL_CHECK");

            //登録者を取得
            //処理日付欄で表示したレコードの更新者（複数いたら、MAX職員番号）
            $rgstRow = array();
            $rgstRow = $db->getRow(knjd110dQuery::getRegister($model, $row), DB_FETCHMODE_ASSOC);
            $row["REGISTERCD"]  = $rgstRow["REGISTERCD"];
            $row["STAFFNAME"]   = $rgstRow["STAFFNAME"];

            $row["SEMESTER"] = $model->ctrl["学期名"][$row["SEMESTER"]];
            $row["MONTH"]    = $row["MONTH"]."月";
            $row["DAY"]      = $row["APPOINTED_DAY"]."日";
            $row["UPDATED"]  = str_replace("-","/",$row["UPDATED"]);

            $arg["data2"][]  = $row;
        }

        Query::dbCheckIn($db);

        //ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "出欠入力制御日付のみ更新",
                            "extrahtml"   => "style=\"width:190px\" onclick=\"return btn_submit('execute2');\"" ));

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_ok",
                            "value"       => "実 行",
                            "extrahtml"   => "onclick=\"return btn_submit('execute');\"" ));

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_cancel",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "生成済みデータ削除",
                            "extrahtml"   => "onclick=\"return btn_submit('del');\"" ));

        $arg["button"] = array("BTN_DEL" => $objForm->ge("btn_del"),
                               "BTN_UPDATE" => $objForm->ge("btn_update"),
                               "BTN_OK"     => $objForm->ge("btn_ok"),
                               "BTN_CLEAR"  => $objForm->ge("btn_cancel") );  
        
        //HIDDEN
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd2") );

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd110dForm1.html", $arg); 
    }
}

//夜間バッチ
function makeDoRun(&$objForm, &$arg, $model) {

    $checked = "";
    $disable = "disabled ";
    $filename = DOCUMENTROOT ."/batch/AccumulateSummaryBatch.properties";
    if (is_readable($filename)) {
        $fp = @fopen($filename, 'r');
        while ($line = fgets($fp,1024)) {
            $pos = strpos($line, "doRun");
            // === を使用していることに注目しましょう。単純に == を使ったのでは
            // 期待通りに動作しません。なぜなら 'doRun' が 0 番目 (最初) の文字だからです。
            if ($pos === false) {
                continue;
            } else {
                $checked = strpos($line, "true") === false ? "" : "checked ";
                break;
            }
        }
        fclose($fp);
        if ($model->Properties["AccumulateSummaryBatch"] != "1") $disable = "";
    }

    $extra = "onClick=\"btn_submit('do_run');\"";
    $arg["data"]["DO_RUN"] = createCheckBox($objForm, "DO_RUN", "ON", $disable.$checked.$extra, "1");
}

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra, $multi = "")
{

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));

    return $objForm->ge($name);
}

//コンボ作成
function makeCmbSchoolKind(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $allSchoolKind = array();

    $opt = array();
    $opt[] = array("label" => "全て", "value" => "ALL");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $allSchoolKind[] = $row["VALUE"];

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    return $allSchoolKind;
}

?>
