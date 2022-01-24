<?php

require_once('for_php7.php');


class knjc201_absence_listForm1
{
    public function main(&$model)
    {
        //フォーム作成
        $objForm = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjc201_absence_listindex.php", "", "edit");

        //年度内の処理のみを行う。
        if (!$model->checkCtrlDay($model->cntl_dt_key)) {
            $model->cntl_dt_key = str_replace("/", "-", CTRL_DATE);
        }

        $wday = array("(日)","(月)","(火)","(水)","(木)","(金)","(土)");
        $w    = date("w", strtotime($model->cntl_dt_key));
        $arg["CNTL_DT_KEY"] = str_replace("-", "/", $model->cntl_dt_key).$wday[$w];

        //選択日付を分解
        $thisMonth  = explode("-", $model->cntl_dt_key);
        $this_month = $thisMonth[1];

        $db = Query::dbCheckOut();

        //SQL文発行(選択日付の学期を取得)
        $query = knjc201_absence_listQuery::getTerm($model->cntl_dt_year, $model->cntl_dt_key);
        $model->termIs = $db->getOne($query);

        //校時
        $nmCnt = 0;
        $model->allPeriodArray = array();
        $str    = array();
        $str[0] = array('NAMECD2' => "1", 'PERIODNAME' => "年組番");
        $str[1] = array('NAMECD2' => "2", 'PERIODNAME' => "氏名");
        $str[2] = array('NAMECD2' => "3", 'PERIODNAME' => "時限");
        $str[3] = array('NAMECD2' => "4", 'PERIODNAME' => "出欠区分");
        $str[4] = array('NAMECD2' => "5", 'PERIODNAME' => "更新者");
        $str[5] = array('NAMECD2' => "6", 'PERIODNAME' => "更新日時");
        foreach ($str as $row) {
            $arg["PERIOD"][] = $row;
            $model->allPeriodArray[$row["NAMECD2"]] = $row;
            $nmCnt++;
        }
        $arg["TOTAL_WIDTH"] = 150 * $nmCnt + 100;

        //表示項目取得
        $query  = knjc201_absence_listQuery::getDispCol();
        $result = $db->query($query);
        $model->DispCol = $db->getOne($query);

        //カレンダーコントロール(カレンダーを作成)
        $extra = "btn_submit('changeDate')";
        $arg["control"]["executedate"] = View::popUpCalendar2($objForm, "executedate", str_replace("-", "/", $model->cntl_dt_key), "reload=true", $extra);

        //前のデータへボタンを作成する
        $extra = "style=\"width:110px\"onclick=\"return btn_submit('read_before');\"";
        $arg["btn_before"] = knjCreateBtn($objForm, "btn_before", "<< 前日", $extra);

        //次のデータへボタンを作成する
        $extra = "style=\"width:110px\"onclick=\"return btn_submit('read_next');\"";
        $arg["btn_next"] = knjCreateBtn($objForm, "btn_next", "翌日 >>", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "cntl_dt_key", $model->cntl_dt_key);

        //処理年度を表示
        $arg["this_year"] = "処理年度  ".CTRL_YEAR;

        //学年選択コンボ
        $query = knjc201_absence_listQuery::gradeCombo($model);
        $extra = "onChange=\"btn_submit('')\";";
        makeCmb($objForm, $arg, $db, $query, $model->GRADE, "GRADE", $extra, 1, "BLANK");

        //年組選択コンボ
        $query = knjc201_absence_listQuery::classCombo($model);
        $extra = "onChange=\"btn_submit('')\";";
        makeCmb($objForm, $arg, $db, $query, $model->CLASS, "CLASS", $extra, 1, "BLANK");

        //----------------------以下、擬似フレーム内リスト表示----------------------
        //SQL文発行(全体の出欠情報を保持)
        $query  = knjc201_absence_listQuery::readQuery($model);
        $result = $db->query($query);
        //変数初期化
        $dispDataAll   = array();
        $chairname_box = array();
        $model->data   = array();
        $attendCnt     = array();
        $chairCnt      = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dispDataAll[] = array("SCHREGNO"             => $row["SCHREGNO"],
                                   "HR_NAMEABBV"          => $row["HR_NAMEABBV"],
                                   "ATTENDNO"             => $row["ATTENDNO"],
                                   "NAME"                 => $row["NAME"],
                                   "PERIODCD"             => $row["PERIODCD"],
                                   "DI_CD"                => $row["DI_CD"],
                                   "DI_NAME1"             => $row["DI_NAME1"],
                                   "STAFFNAME"            => $row["STAFFNAME"],
                                   "UPDATED"              => $row["UPDATED"]
                                   );
        }
        //学生の時限ごとの最新出欠情報
        $lastUpdateRow = array();
        $SCHREGNO = $dispDataAll[0]["SCHREGNO"];     //学籍番号の基準値(初期値は1行目)
        $PERIODCD = $dispDataAll[0]["PERIODCD"];     //時限の基準値(初期値は1行目)

        for ($row = 1; $row < count($dispDataAll); $row++) {
            if ($SCHREGNO != $dispDataAll[$row]["SCHREGNO"]
                ||$PERIODCD != $dispDataAll[$row]["PERIODCD"]) {
                //学生の時限ごとの最新出欠情報を配列に保存
                $lastUpdateRow[] = $dispDataAll[$row - 1];
                $SCHREGNO = $dispDataAll[$row]["SCHREGNO"];
                $PERIODCD = $dispDataAll[$row]["PERIODCD"];
            } else {
                continue;
            }
        }

        if ($SCHREGNO != $dispDataAll[$row]["SCHREGNO"]
            || $PERIODCD != $dispDataAll[$row]["PERIODCD"]) {
            //学生の時限ごとの最新出欠情報を配列に保存
            $lastUpdateRow[] = $dispDataAll[$row - 1];
            $SCHREGNO = $dispDataAll[$row]["SCHREGNO"];
            $PERIODCD = $dispDataAll[$row]["PERIODCD"];
        }

        //授業数取得
        $period_num = 0;
        $query = knjc201_absence_listQuery::getPeriodNum($model);
        $results = $db->query($query);
        while ($Row = $results->fetchRow(DB_FETCHMODE_ASSOC)) {
            $period_num = $Row["COUNT"];
        }

        //一度同じ学籍番号ので出欠区分をカウント
        $arrCnt = array();
        foreach ($lastUpdateRow as $k => $v) {
            //学籍番号毎に出欠区分(DI_CD)を保持
            $arrCnt[ $v['SCHREGNO'] ]['cnt'][] = $v['DI_CD'];

            //更新日時が一番新しいものの更新者と更新日時を保持
            if (isset($v['SCHREGNO'] ['UPDATED']) == true) {
                if ($v['UPDATED'] > $arrCnt[ $v['SCHREGNO'] ]['UPDATED']) {
                    $arrCnt[ $v['SCHREGNO'] ]['STAFFNAME']  = $v['STAFFNAME'];
                    $arrCnt[ $v['SCHREGNO'] ]['UPDATED']    = $v['UPDATED'];
                }
            } else {
                $arrCnt[ $v['SCHREGNO'] ]['STAFFNAME']  = $v['STAFFNAME'];
                $arrCnt[ $v['SCHREGNO'] ]['UPDATED']    = $v['UPDATED'];
            }
        }

        foreach ($arrCnt as $schRegNo => $v) {
            $arrShukketsu = $v['cnt'];
            if (get_count($arrShukketsu) != $period_num) {
                //出欠区分が$period_num分ない→明らかに1日ではない
                $arrCnt[$schRegNo]['ichi_niti'] = false;
            } else {
                //→全て同じ出欠区分か？
                $shukketu = $arrShukketsu[0];
                $arrCnt[$schRegNo]['ichi_niti'] = true;
                foreach ($arrShukketsu as $v2) {
                    if ($shukketu != $v2) {
                        //異なる出欠区分が見つかった
                        $arrCnt[$schRegNo]['ichi_niti'] = false;
                        break;
                    }
                }
            }
        }

        $dispData = array();  //まとめた後のデータを保持する
        foreach ($lastUpdateRow as $k => $v) {
            if ($arrCnt[ $v['SCHREGNO'] ]['ichi_niti']  == true) {
                //$dispDataをループして同じ学籍番号のデータがあるかどうか調べる
                $existGakuseki = false;
                foreach ($dispData as $k2 => $v2) {
                    if ($v['SCHREGNO'] == $v2['SCHREGNO']) {
                        $existGakuseki = true;
                    }
                }

                //同じ学籍番号のデータがなければ追加
                if ($existGakuseki == false) {
                    $temp = $v;
                    $temp['STAFFNAME'] = $arrCnt[ $v['SCHREGNO'] ]['STAFFNAME'];
                    $temp['UPDATED']   = $arrCnt[ $v['SCHREGNO'] ]['UPDATED'];
                    $temp['PERIODCD']  = '1日';
                    $dispData[]        = $temp;
                }
            } else {
                //1日じゃない
                $dispData[] = $v;
            }
        }
        //初期化
        $dispHrData = array();
        $first  = "true"; //フォーカス対象

        //表示用データを作成する
        for ($i = 0; $i < get_count($dispData); $i++) {
            $strTag = "";
            foreach ($dispData[$i] as $key => $val) {
                switch ($key) {
                    case "HR_NAMEABBV":
                        $expstr = explode("-", $val);
                        $str = $expstr[0]."年".$expstr[1]."組";
                        break;
                    case "ATTENDNO":
                        $str .= $val."番";
                        break;
                    case "PERIODCD":
                        $str = str_replace("限目", "校時", $val);
                        break;
                    default:
                        $str = $val;
                }
                if ($key != "SCHREGNO" &&
                    $key != "HR_NAMEABBV" &&
                    $key != "DI_CD") {
                    $strTag.= "<td nowrap bgcolor=\"#FFFFFF\" width=\"150\" height=\"{$maxHeight}\">".$str."</td>";
                }
            }   //end of foreach

            $baseHeight = 10;
            $maxHeight  = 10;

            $dispTag["TD"] = $strTag;
            $dispTag["HR_HEIGHT"] = $maxHeight + $baseHeight;

            $arg["attend_data"][] = $dispTag;

            $setHrdata["HR_NAME"]   = knjCreateCheckBox($objForm, "TARGET_CHECK", $hrClassArray[$i], $extra, 1);
            $setHrdata["HR_NAME"]  .= $class_name_show[$hrClassArray[$i]];
            $setHrdata["HR_HEIGHT"] = $maxHeight + $baseHeight;
            $arg["attend_data2"][]  = $setHrdata;
        }

        //フォーカス対象無し
        $first = ($first == "true")?"off":"first";

        //サブミット回避
        $extra = "style=\"display:none\"";
        $arg["data"]["SUBMIT_KAIHI"] = knjCreateTextBox($objForm, "", "SUBMIT_KAIHI", 2, 2, $extra);

        //終 了ボタンを作成する
        $extra = " onclick=\"return closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", " 戻 る ", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "locker", $first);
        knjCreateHidden($objForm, "ID_NO", $model->first_id);
        knjCreateHidden($objForm, "backupper", $model->color_bk);
        knjCreateHidden($objForm, "chosen_id", $model->chosen_id);

        knjCreateHidden($objForm, "attendexsits", $ex);
        knjCreateHidden($objForm, "monthch", $mch);
        knjCreateHidden($objForm, "attendctrldate", $acd);
        knjCreateHidden($objForm, "SEND_AUTH", AUTHORITY);

        $result->free();
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc201_absence_listForm1.html", $arg, 1);
    }
}

function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
