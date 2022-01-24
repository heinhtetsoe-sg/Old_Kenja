<?php

require_once('for_php7.php');

class knjc034dSubform1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjc034dindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //変更処理
        if ($model->replaceItem) {
            //曜日一覧
            $weekday = array( "日", "月", "火", "水", "木", "金", "土" );

            //項目名表示
            list($year, $month, $day) = explode('-', $model->replaceItem);
            $week = '('.$weekday[date("w", mktime(0, 0, 0, $month, $day, $year))].')';
            $arg["data"]["LABEL"] = sprintf('%2d', $day).$week;

            //背景色
            if ($model->replaceItem == CTRL_DATE) {
                $bgcolor = "#00ff00";   //ログイン日付
            } elseif (date("w", mktime(0, 0, 0, $month, $day, $year)) == 6) {
                $bgcolor = "lightblue"; //土曜日
            } elseif (date("w", mktime(0, 0, 0, $month, $day, $year)) == 0) {
                $bgcolor = "pink";      //日曜日
            } else {
                $bgcolor = "#ffffff";
            }
            $arg["data"]["COLOR"] = $bgcolor;

            /* Add by HPA for PC-talker 読み start 2020/02/03 */
            $date = $arg["data"]["LABEL"];
            $arg["data"]["TITLE"] = "$date の出欠画面";
            /* Add by HPA for PC-talker 読み end 2020/02/20 */

            //出欠コードコンボに表示しないコード＆子・孫が設定されている親の出欠コード取得
            $dicd["omitcd"] = $dicd["parents"] = array();
            if ($model->Properties["attend_Shosai"] == 1) {
                $query = knjc034dQuery::getAttendCode($model);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    list($di_cd, $subl_cd, $subm_cd) = preg_split("/-/", $row["VALUE"]);

                    //出欠コードコンボに表示しないコード取得
                    if ($subm_cd != '0000' && !in_array($di_cd.'-'.$subl_cd.'-0000', $dicd["omitcd"])) {
                        $dicd["omitcd"][] = $di_cd.'-'.$subl_cd.'-0000';
                    }
                    //子・孫が設定されている親の出欠コード取得
                    if ($subl_cd != '0000' && !in_array($di_cd.'-0000-0000', $dicd["parents"])) {
                        $dicd["parents"][] = $di_cd.'-0000-0000';
                    }
                }
            }

            //データ入力
            /* Edit by HPA for PC-talker 読み start 2020/02/03 */
            $query = knjc034dQuery::getAttendCode($model);
            $day = preg_replace("/[^A-Za-z0-9 ]/", '', $date);
            $extra = "aria-label = \"".$day."日の出欠\" style=\"font-weight:bold;\" ";
            /* Edit by HPA for PC-talker 読み end 2020/02/20 */
            $arg["data"]["REP_VALUE"] = makeCmb($objForm, $arg, $db, $query, $model->replaceValue, "REP_VALUE", $extra, 1, $model, $dicd, "BLANK");
        }

        //ALLチェック
        /* Edit by HPA for PC-talker 読み start 2020/02/03 */
        $extra = "aria-label =\"全てをチェック\" checked onClick=\"check_all(this); OptionUse(this, '".$model->cmd."')\"";
        /* Edit by HPA for PC-talker 読み end 2020/02/20 */
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //出席番号の項目名切替
        $arg["ATTENDNO_LABEL"] = ($model->replaceTYPE == "3") ?  '年組番' : 'No.';

        if ($model->replaceTYPE && $model->replaceGHR && $model->replaceSEM) {
            $counter = 0;
            $sch_list = array();
            $colorFlg = $updateflg = false;

            //生徒取得
            $query = knjc034dQuery::getSchInfo($model->replaceTYPE, $model->replaceMIX, $model->replaceSEM, $model->replaceGHR);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sch_array[] = $row["SCHREGNO"];
            }
            $result->free();

            //休日（訪問生）
            $schHoliday = $schEvent = $holiday = array();
            if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $result = $db->query(knjc034dQuery::getSchHoliday($model, $month, $sch_array));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $schEvent[] = $row["SCHREGNO"];

                    if ($row["HOLIDAY_FLG"] == "1") {
                        $schHoliday[$row["SCHREGNO"]][$row["EXECUTEDATE"]] = 1;
                    }
                }
                $result->free();
            }

            if ($model->replaceTYPE == "1" && $model->replaceMIX == "1") {
                //休日取得 -- EVENT_MST
                $holiday = $db->getCol(knjc034dQuery::getHoliday2($model, $model->replaceTYPE, $model->replaceGHR, $model->replaceItem, $model->replaceItem));
            } elseif ($model->replaceTYPE == "2" && $model->Properties["useSpecial_Support_Hrclass"] == '1') {
                //休日取得 -- PUBLIC_HOLIDAY_MST
                $query = knjc034dQuery::getPublicHoliday($month);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //月日を2桁変換、1月から3月の時は年を+1する
                    $row["HOLIDAY_MONTH"] = sprintf("%02d", $row["HOLIDAY_MONTH"]);
                    if (in_array($row["HOLIDAY_MONTH"], array("01", "02", "03"))) {
                        $row["YEAR"] = (int)$row["YEAR"] + 1;
                    }
                    $row["HOLIDAY_DAY"] = sprintf("%02d", $row["HOLIDAY_DAY"]);

                    //日付指定をする場合
                    if ($row["HOLIDAY_DIV"] == "1") {
                        $holiday[] = $row["YEAR"]."-".$row["HOLIDAY_MONTH"]."-".$row["HOLIDAY_DAY"];
                    //曜日指定をする場合
                    } elseif ($row["HOLIDAY_DIV"] == "2") {
                        //曜日コードをPHP用のコードに変換
                        $row["HOLIDAY_WEEKDAY"] = (int)$row["HOLIDAY_WEEKDAY"] - 1;

                        $holiday[] = getWhatDayOfWeek($row["YEAR"], $row["HOLIDAY_MONTH"], $row["HOLIDAY_WEEK_PERIOD"], $row["HOLIDAY_WEEKDAY"]);
                    }
                }
                $result->free();
            } else {
                //休日取得 -- EVENT_DAT
                $holiday = $db->getCol(knjc034dQuery::getHoliday($model, $model->replaceTYPE, $model->replaceGHR, $model->replaceItem, $model->replaceItem));
            }

            //生徒一覧表示
            $query = knjc034dQuery::getSchInfo($model->replaceTYPE, $model->replaceMIX, $model->replaceSEM, $model->replaceGHR);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //休日のとき、使用不可
                $holiday_flg = false;
                if (in_array($row["SCHREGNO"], $schEvent)) {
                    if ($schHoliday[$row["SCHREGNO"]][$model->replaceItem] == 1) {
                        $holiday_flg = true;
                    }
                } elseif (in_array($model->replaceItem, $holiday)) {
                    $holiday_flg = true;
                }

                //背景色切替フラグ
                if ($counter % 5 == 0) {
                    $colorFlg = !$colorFlg;
                }

                //異動チェック
                $idou = $db->getRow(knjc034dQuery::checkIdou(array($row["SCHREGNO"]), $model->replaceSEM, $model->replaceItem), DB_FETCHMODE_ASSOC);

                //背景色
                if ($idou["IDOU_COLOR"] > 0) {
                    $row["BGCOLOR"] = "yellow";
                } elseif ($colorFlg) {
                    $row["BGCOLOR"] = "#ffffff";
                } else {
                    $row["BGCOLOR"] = "#cccccc";
                }

                //対象者チェックボックス
                if ($holiday_flg || $idou["IDOU_DISABLE"] > 0) {
                    $extra = "disabled";
                } else {
                    /* Edit by HPA for PC-talker 読み start 2020/02/03 */
                    $extra = " aria-label = \"".$row["NAME_SHOW"]."\"checked onclick=\"OptionUse(this, '".$model->cmd."');\"";
                    /* Edit by HPA for PC-talker 読み end 2020/02/20 */
                    $updateflg = true;
                }
                $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $counter, $extra, "1");

                $arg["list"][] = $row;

                $sch_list[$counter] = $row["SCHREGNO"];
                $counter++;
            }
            $result->free();

            knjCreateHidden($objForm, "SCH_LIST", $sch_list);

            $arg["HEIGHT"] = ($counter > 6) ? "height:180;" : "";
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $updateflg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "GET_ITEM", $model->replaceItem);
        knjCreateHidden($objForm, "GET_COUNTER", $model->replaceCounter);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc034dSubform1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $model, $dicd, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->Properties["attend_Shosai"] == 1) {
            //孫コードがあるときは子コードは出力しない
            if (in_array($row["VALUE"], $dicd["omitcd"])) {
                continue;
            }
        }
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    //出欠（詳細）表示
    if ($model->Properties["attend_Shosai"] == 1) {
        $setCombo  = "";
        $setCombo .= "<select name='".$name."' size='1' ".$extra.">";

        $tmpP = "";
        for ($i = 0; $i < get_count($opt); $i++) {
            //選択されている値
            $selected = ($value == $opt[$i]["value"]) ? "selected" : "";

            list($di_cd, $subl_cd, $subm_cd) = preg_split("/-/", $opt[$i]["value"]);

            if ($tmpP != $di_cd && in_array($tmpP.'-0000-0000', $dicd["parents"])) {
                $setCombo .= "</optgroup>";
            }

            if (in_array($opt[$i]["value"], $dicd["parents"])) {
                //子・孫コードがあればグループ化
                $setCombo .= "<optgroup label='".$opt[$i]["label"]."'>";
                /* Edit by HPA for PC-talker 読み start 2020/02/03 */
                $optGroup = $opt[$i]["label"];
            } else {
                if ($opt[$i]["label"] == "忌引" || $opt[$i]["label"] == "事欠" || $opt[$i]["label"] == "遅刻" || $opt[$i]["label"] == "早退" || $opt[$i]["label"] == "遅刻+早退") {
                    $label ="";
                } else {
                    $label =" aria-label = \"".$optGroup."の".$opt[$i]["label"]."\"";
                }
                $setCombo .= "<option value='".$opt[$i]["value"]."'$label ".$selected.">".$opt[$i]["label"]."</option>";
                /* Edit by HPA for PC-talker 読み end 2020/02/20 */
            }
            $tmpP = $di_cd;
        }
        $setCombo .= "</select>";

        return $setCombo;
    } else {
        return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $updateflg)
{
    //変更ボタン
    /* Edit by HPA for PC-talker 読み start 2020/02/03 */
    $extra = ($updateflg) ? "id=\"replace\" aria-label = \"変更\" onclick=\"return btn_submit()\"" : "disabled";
    $arg["button"]["btn_replace"] = knjCreateBtn($objForm, "btn_replace", "変 更", $extra);

    //戻るボタン
    $extra = "aria-label = \"戻る\" onclick=\"parent.current_cursor_focus();return parent.closeit()\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
    /* Edit by HPA for PC-talker 読み end 2020/02/20 */
}


//任意の年月の第n曜日の日付を求める関数
//  $year 年
//  $month 月
//  $number 何番目の曜日か、第1曜日なら1。第3曜日なら3
//  $dayOfWeek 求めたい曜日。0～6までの数字で曜日の日～土を指定する
function getWhatDayOfWeek($year, $month, $number, $dayOfWeek)
{
    //指定した年月の1日の曜日を取得
    $firstDayOfWeek = date("w", mktime(0, 0, 0, $month, 1, $year));
    $day = (int)$dayOfWeek - (int)$firstDayOfWeek + 1;
    //1週間を足す
    if ($day <= 0) {
        $day += 7;
    }
    $weekselect = mktime(0, 0, 0, $month, $day, $year);
    //n曜日まで1週間を足し込み
    $weekselect += (86400 * 7 * ((int)$number - 1));
    return date("Y-m-d", $weekselect);
}
