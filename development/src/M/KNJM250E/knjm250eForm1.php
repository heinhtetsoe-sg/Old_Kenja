<?php

require_once('for_php7.php');

class knjm250eForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjm250eindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();
        //年度
        $opt_year  = array();
        $opt_year[0] = array('label' => CTRL_YEAR, 'value' => CTRL_YEAR);
        $opt_year[1] = array('label' => CTRL_YEAR +1 , 'value' => CTRL_YEAR +1 );

        if ($model->ObjYear == "") $model->ObjYear = $opt_year[0]["value"] ;
        $extra = "onChange=\"return btn_submit('main');\"";
        $arg["TOP"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->ObjYear, $opt_year, $extra, 1);

        //データセット
        $opt_sub  = array();
        $result = $db->query(knjm250eQuery::ReadQuery($model));
        $subcnt = 0;
        while ($RowR = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $opt_sub[$subcnt] = array('label' => $RowR["CHAIRNAME"],
                                      'value' => $RowR["CHAIRCD"].$RowR["SUBCLASSCD"]);
            $get_data[] = array( "SUBCD"        => $subcnt,             //ID設定
                                 "SUBCDNAME"    => $period);
            $subcnt++;
        }
        $result->free();

        if ($model->sub == "") $model->sub = $opt_sub[0]["value"];
        $extra = "onChange=\"return btn_submit('chg_subclass');\"";
        $arg["TOP"]["SELSUB"] = knjCreateCombo($objForm, "SELSUB", $model->sub, $opt_sub, $extra, 1);

        if ($model->Properties["useRepStandarddateCourseDat"] == '1') {
            //課程学科コンボ
            $query = knjm250eQuery::GetCourseMajor($model);
            $extra = "onChange=\"return btn_submit('chg_subclass');\"";
            makeCombo($objForm, $arg, $db, $query, $model->coursemajor, "COURSEMAJOR", $extra, 1);
        }

        //NAME_MST
        $opt_sem = array();
        $result = $db->query(knjm250eQuery::GetName($model, "M002"));
        while ($RowN = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_sem[] = array('label' => $RowN["NAME1"],
                               'value' => $RowN["NAMECD2"]);
        }
        $result->free();

        $result  = $db->query(knjm250eQuery::Getperiod($model, $model->sub));
        $peri   = $result->fetchRow(DB_FETCHMODE_ASSOC);
        $period = $peri["REP_SEQ_ALL"];
        $start  = ($peri["REP_START_SEQ"]) ? $peri["REP_START_SEQ"] : 1;
        $model->repcntall = $period;
        $model->repstartcnt = $start;
        $result->free();

        for ($kai = $start; $kai < ($start + $period); $kai++) {
            //ID
            $Row["SUBCD"] = $kai;

            //回数
            $Row["SUBCDNAME"] = "第".$Row["SUBCD"]."回";

            $result     = $db->query(knjm250eQuery::GetRepdata($model, $Row["SUBCD"]));
            $repstan    = $result->fetchRow(DB_FETCHMODE_ASSOC);

            $result->free();

            //レポート区分
            if (!$repstan["REPORTDIV"]) $repstan["REPORTDIV"] = 1;
            if ($model->cmd != "reset") {
                $model->setdata["REPDIV"][$kai] = $repstan["REPORTDIV"];
            } else if (!$model->setdata["REPDIV"][$kai]) {
                $model->setdata["REPDIV"][$kai] = $repstan["REPORTDIV"];
            }
            $extra = " id=\"".$Row["SUBCD"]."\"";
            $Row["REPDIV"] = knjCreateCombo($objForm, "REPDIV".$kai, $model->setdata["REPDIV"][$kai], $opt_sem, $extra, 1);

            //日付
            if (!$repstan["STANDARD_DATE"]) $repstan["STANDARD_DATE"] = str_replace("-","/",CTRL_DATE);
            if (!$repstan["RETURN_DATE"]) {
                $repstan["RETURN_DATE"] = getNextMonth($repstan["STANDARD_DATE"], 1);  //date('Y-m-d', strtotime(CTRL_DATE . '+1 month'));
                $chkyoubi = date('w', strtotime($repstan["RETURN_DATE"]));
                if ($chkyoubi == '6') {
                    $repstan["RETURN_DATE"] = date('Y-m-d', strtotime($repstan["RETURN_DATE"]. '+2 day'));
                }
                if ($chkyoubi == '0') {
                    $repstan["RETURN_DATE"] = date('Y-m-d', strtotime($repstan["RETURN_DATE"]. '+1 day'));
                }
            }
            if (!$repstan["DEADLINE_DATE"]) $repstan["DEADLINE_DATE"] = '';
            if (!$model->setdata["BASEDAY"][$kai]) $model->setdata["BASEDAY"][$kai] = str_replace("-","/",$repstan["STANDARD_DATE"]);
            if (!$model->setdata["ENDDAY"][$kai] ) $model->setdata["ENDDAY"][$kai]  = str_replace("-","/",$repstan["DEADLINE_DATE"]);
            if (!$model->setdata["RETRYDAY"][$kai]) $model->setdata["RETRYDAY"][$kai] = str_replace("-","/",$repstan["RETURN_DATE"]);
            $extra = "\" onchange=\"dateChange(".$kai.", this.value);";
            $param = "extra=dateChange(".$kai.", f.document.forms[0].BASEDAY".$kai.".value)";
            $Row["BASEDAY"] = View::popUpCalendar2($objForm, "BASEDAY".$kai,  $model->setdata["BASEDAY"][$kai],  $param, $extra);
            $Row["ENDDAY"]  = View::popUpCalendar($objForm,  "ENDDAY".$kai,   $model->setdata["ENDDAY"][$kai]);
            $Row["RETRYDAY"] = View::popUpCalendar($objForm, "RETRYDAY".$kai, $model->setdata["RETRYDAY"][$kai]);

            if (!$model->setdata["REMARK"][$kai]) $model->setdata["REMARK"][$kai] = $repstan["REMARK"];

            //備考
            $extra = "onblur=\"check(this)\" id=\"".$Row["SUBCD"]."\"";
            $Row["REMARK"] = knjCreateTextBox($objForm, $model->setdata["REMARK"][$kai], "REMARK".$kai, 60, 60, $extra);

            $arg["data"][] = $Row;
        }

        //ボタン作成
        $extra = "onClick=\"btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        $extra = "onClick=\"btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd", $model->cmd);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjm250eForm1.html", $arg);
    }
}


//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size)
{
    $opt = array();
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["TOP"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}



//xか月後を算出(※月末の1か月後->2か月後の月初にならないように制御)
function getNextMonth($target_date=NULL, $term=1) {
  if (empty($target_date)) $target_date = CTRL_DATE;
  // 翌月末日を取得
  $last_date = date('Y-m-d', strtotime($target_date." last day of +{$term} month"));
  $chk_last =  date('Ymd', strtotime($target_date." last day of +{$term} month"));
  // 対象日の翌月日を取得...(2)
  $prev_date = date('Y-m-d', strtotime($target_date." +{$term} month"));
  $chk_prev = date('Ymd', strtotime($target_date." +{$term} month"));
  // (1)と(2)を比較し、(2)の方が未来日の時とみ(1)を出力する
  if ($chk_prev > $chk_last) {
    return $last_date;
  } else {
    return $prev_date;
  }
}?>
