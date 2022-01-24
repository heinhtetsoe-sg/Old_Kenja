<?php

require_once('for_php7.php');

class knjz177Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz177index.php", "", "main");
        $db = Query::dbCheckOut();

        $row = array();

        //処理年度
        $arg['YEAR'] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query = knjz177Query::getNameMstA023($model);
            $extra = "onChange=\"btn_submit('main')\";";
            makeCmb($objForm, $arg, $db, $query, $model->school_kind, "SCHOOL_KIND", $extra, 1);
        }

        //対象月コンボ
        makeMonthSemeCmb($objForm, $arg, $db, $model);

        //締め日テキストボックス
        $extra = "";
        $arg["SIMEBI"] = knjCreateTextBox($objForm, $model->field["SIMEBI"], "SIMEBI", 3, 2, $extra);

        //ボタン作成
        makeButton($objForm, $arg, $model);

        //リスト表示
        makeList($objForm, $arg, $db, $model);

        //プロパティーズを見に行って開くべきかどうかを判断する。
        $arg["close"] ="";
        if($model->Properties["KNJZ177"] != '1') {
               $arg["close"] = " closing_window(); " ;
        }

        //hidden
        makeHidden($objForm);

        $arg["finish"] = $objForm->get_finish();
        Query::dbCheckIn($db);
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz177Form1.html", $arg); 
    }
}
/***************************************    これ以下は関数    **************************************************/

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

////////////////////////
////////////////////////対象月コンボ
////////////////////////
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model) {
    $opt_month[] = array("label" => "",
                         "value" => "");

    $defaultValueFlag = true;
    $value = '';
    $query      = knjz177Query::selectSemesAll(CTRL_YEAR);
    $result     = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        for ($i = $row["S_MONTH"]; $i <= $row["E_MONTH"]; $i++) {
            if ($i > 12) {
                $month = $i - 12;
            } else {
                $month = $i;
            }

            $query = knjz177Query::getAppointedDay($row['YEAR'], $model->school_kind, $month, $row['SEMESTER']);
            $appointedDay = $db->getOne($query);

            $query = knjz177Query::selectMonthQuery($month, CTRL_YEAR, $model);
            $getdata = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($getdata)) {
                $opt_month[] = array("label" => $getdata["NAME1"]." (".$row["SEMESTERNAME"].") ",
                                     "value" => CTRL_YEAR . ',' . $month . ',' . $row['SEMESTER'] . ',' . $appointedDay);
                if ($defaultValueFlag) {
                    if (CTRL_YEAR == $model->YEAR   &&   $month == $model->MONTH   &&   $row['SEMESTER'] == $model->SEMESTER) {
                        $value = CTRL_YEAR . ',' . $month . ',' . $row['SEMESTER'] . ',' . $appointedDay;
                        $defaultValueFlag = false;
                    }
                }
            }
        }
    }
    $result->free();

    $arg["TARGET_MONTH"] = knjCreateCombo($objForm, "TARGET_MONTH", $value, $opt_month, 'onChange="upDateText()";', 1);
}
////////////////////////
////////////////////////リスト表示
////////////////////////
function makeList(&$objForm, &$arg, $db, $model) {
    $valArray = array();
    $query = knjz177Query::getList(CTRL_YEAR);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        for ($i = $row['SMONTH']; $i <= $row['EMONTH']; $i++) {
            $valArray = array();
            if ($i > 12) {
                $target_month = $i - 12;
            } else {
                $target_month = $i;
            }

            $query = knjz177Query::getAppointedDay($row['YEAR'], $model->school_kind, $target_month, $row['SEMESTER']);
            $appointedDay = $db->getOne($query);

            //月の表示文字するをDBから取得
            $query = knjz177Query::getMonthName($row['YEAR'], sprintf('%02d',$target_month), $model);
            $monthName = $db->getOne($query);
            //背景に色をつけるかどうか、
            $query = knjz177Query::getListColor(CTRL_YEAR, sprintf('%02d',$target_month), $model);
            $rowOfAdmin = $db->getRow($query);
            $td = '';
            if ($rowOfAdmin) {
                $td .= "<td align='center' class='no_search'>{$row['SEMESTERNAME']}</td>";
                $td .= "<td align='center' bgcolor='#C6DCEC'>{$monthName}</td>";
                $td .= "<td align='center' bgcolor='#C6DCEC'>{$appointedDay}</td>";
            } else {
                $td .= "<td align='center' class='no_search'>{$row['SEMESTERNAME']}</td>";
                $td .= "<td align='center' bgcolor='#ffffff'>{$monthName}</td>";
                $td .= "<td align='center' bgcolor='#ffffff'>{$appointedDay}</td>";
            }
            $arg['data'][] = array('td' => $td);
        }
    }
    $result->free();
}
////////////////////////
////////////////////////ボタン作成
////////////////////////
function makeButton(&$objForm, &$arg, $model) {
    //更新ボタン
    $arg["button"]["btn_updte"] = knjCreateBtn($objForm, "btn_updte", "保 存", "onclick=\"return btn_submit('update');\"");

    //取消ボタンを作成する
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", "onclick=\"return btn_submit('reset');\"");

    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"return closeWin();\"");
    
    //学年別締め日登録ボタン表示のプロパティ
    $link  = REQUESTROOT."/Z/KNJZ177_GRADE/knjz177_gradeindex.php?mode=1&SEND_PRGID="."KNJZ177"."&SEND_YEAR=$model->year";
    $link .= "&URL_SCHOOLKIND={$model->urlSchoolKind}";
    $link .= "&URL_SCHOOLCD={$model->urlSchoolCd}";
    $link .= "&MN_ID={$model->mnId}";
    if ($model->Properties["useAppointedDayGradeMst"] == '1') {
        $arg["useAppointedDayGradeMst"] = 1;
    } else {
        $arg["NotuseAppointedDayGradeMst"] = 1;
    }
    $extra  = "onclick=\"document.location.href='$link'\"";
    $arg["button"]["btn_grade_sime"] = knjCreateBtn($objForm, "btn_grade_sime", "学年別締め日登録", $extra);
}
////////////////////////
////////////////////////hidden作成
////////////////////////
function makeHidden(&$objForm) {
        knjCreateHidden($objForm, "cmd");
}
?>
