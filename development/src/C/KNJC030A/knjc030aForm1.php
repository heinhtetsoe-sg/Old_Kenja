<?php

require_once('for_php7.php');

class knjc030aForm1
{
    public function main(&$model)
    {
        //フォーム作成
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjc030aindex.php", "", "edit");

        //年度内の処理のみを行う。
        if (!$model->checkCtrlDay($model->cntl_dt_key)) {
            $model->cntl_dt_key=$model->cntl_dt_year."-04-01";
            $reset_day = knjc030aQuery::keyMoverQuery($model);
            $model->cntl_dt_key = ($reset_day != "")? $reset_day : $model->cntl_dt_year."-04-01" ;
        }

        $wday = array("(日)","(月)","(火)","(水)","(木)","(金)","(土)");
        $w = date("w", strtotime($model->cntl_dt_key));
        $arg["CNTL_DT_KEY"] = str_replace("-", "/", $model->cntl_dt_key).$wday[$w];

        //選択日付を分解
        $thisMonth = explode("-", $model->cntl_dt_key);
        $this_month=$thisMonth[1];

        $db = Query::dbCheckOut();

        //SQL文発行(選択日付の学期を取得)
        $query = knjc030aQuery::getTerm($model->cntl_dt_year, $model->cntl_dt_key);
        $model->termIs = $db->getOne($query);

        //校時
        $query = knjc030aQuery::getPeriodName();
        $result = $db->query($query);
        $nmCnt = 0;
        $model->allPeriodArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["PERIOD"][] = $row;
            $model->allPeriodArray[$row["NAMECD2"]] = $row;
            $nmCnt++;
        }
        $arg["TOTAL_WIDTH"] = 95 * $nmCnt + 100;

        //表示項目取得
        $query = knjc030aQuery::getDispCol();
        $result = $db->query($query);
        $model->DispCol = $db->getOne($query);

        //カレンダーコントロール(カレンダーを作成)
        $arg["control"]["executedate"] = View::popUpCalendar($objForm, "executedate", str_replace("-", "/", $model->cntl_dt_key), "reload=true");

        //前のデータへボタンを作成する
        $extra = "style=\"width:110px\"onclick=\"return btn_submit('read_before');\"";
        $arg["btn_before"] = knjCreateBtn($objForm, "btn_before", "<< 前のデータ", $extra);

        //次のデータへボタンを作成する
        $extra = "style=\"width:110px\"onclick=\"return btn_submit('read_next');\"";
        $arg["btn_next"] = knjCreateBtn($objForm, "btn_next", "次のデータ >>", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "cntl_dt_key", $model->cntl_dt_key);

        //処理年度を表示
        $arg["this_year"] = "処理年度:".CTRL_YEAR ."年";

        //初期化
        $first_time_flg = "off";

        //学年選択コンボ
        $query = knjc030aQuery::gradeCombo($model);
        $extra = "onChange=\"btn_submit('')\";";
        makeCmb($objForm, $arg, $model, $db, $query, $model->GRADE, "GRADE", $extra, 1, "BLANK");

        //----------------------以下、擬似フレーム内リスト表示----------------------
        //クラス情報を保持
        $query = knjc030aQuery::getHrClass($model);
        $results = $db->query($query);
        //SQL文発行(クラス情報を保持)
        $class_cnt = 0;

        $hrClassArray = array();
        while ($Row = $results->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hrClassArray[] = $Row["GR_CL"];
            $class_name_show[$Row["GR_CL"]] = $Row["HR_NAME"];
            $class_cnt++;
        }

        //SQL文発行(全体の出欠情報を保持)
        $query  = knjc030aQuery::readQuery($model);
        $result = $db->query($query);

        //変数初期化
        $dispDataAll = array();
        $chairname_box = array();
        $model->data = array();
        $attendCnt   = array();
        $chairCnt    = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $scd_executediv_name = $row["SCD_EXECUTEDIV_NAME"] ? "(".$row["SCD_EXECUTEDIV_NAME"].")<BR>" : "";
            //データを配列に保持
            if ($model->DispCol == 1) {
                $chairname_box[$row["TARGETCLASS"]][$row["PERIODCD"]] .= $row["SUBCLASSABBV"]."<BR>".$row["STAFFNAME"]."<BR>".$scd_executediv_name;
            } else {
                $chairname_box[$row["TARGETCLASS"]][$row["PERIODCD"]] .= $row["CHAIRNAME"]."<BR>".$row["STAFFNAME"]."<BR>".$scd_executediv_name;
            }
            //出欠と講座数
            $attendCnt[$row["TARGETCLASS"]][$row["PERIODCD"]] += $row["EXECUTED_CHK"];
            $chairCnt[$row["TARGETCLASS"]][$row["PERIODCD"]]++;

            //講座別、ＨＲ別の何れかにフラグが立っていれば出欠済みとする
            if ($row["EXECUTED_CHK"] == 0) {
                //出欠未
                $select_executed = 0;
            } else {
                //出欠済み
                $select_executed = 1;
            }

            $model->data["CHAIRCD"][] = $row["CHAIRCD"];
            $model->data["PERIODCD"][] = $row["PERIODCD"];
            $model->data["GRADE"][] = $row["GRADE"];
            $model->data["HR_CLASS"][] = $row["HR_CLASS"];

            $dispDataAll[$row["TARGETCLASS"]][] = array("TR_CD1"               => $row["TR_CD1"],
                                                        "PERIODCD"             => $row["PERIODCD"],
                                                        "CHAIRCD"              => $row["CHAIRCD"],
                                                        "GRADE"                => $row["GRADE"],
                                                        "HR_CLASS"             => $row["HR_CLASS"],
                                                        "TARGETCLASS"          => $row["TARGETCLASS"],
                                                        "STAFFNAME"            => $row["STAFFNAME"],
                                                        "SELECT_EXECUTED"      => $select_executed
                                                    );
        }

        //初期化
        $dispHrData = array();
        $first  = "true"; //フォーカス対象

        //リンク先作成
        $jumping = "/C/KNJC010A";
        $jumping = REQUESTROOT.$jumping."/knjc010aindex.php";

        //表示用データを作成する
        for ($i = 0; $i < $class_cnt; $i++) {
            //配列を初期化
            $list_tag["PERIODCD1"] = "<td width=\"95\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCD2"] = "<td width=\"95\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCD3"] = "<td width=\"95\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCD4"] = "<td width=\"95\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCD5"] = "<td width=\"95\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCD6"] = "<td width=\"95\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCD7"] = "<td width=\"95\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCD8"] = "<td width=\"95\" height=\"40\">&nbsp;</td>";
            $list_tag["PERIODCD9"] = "<td width=\"*\" height=\"40\">&nbsp;</td>";

            $extra = "";

            //消す
            $list_tag["TARGETCLASS"] = knjCreateCheckBox($objForm, "TARGET_CHECK", $hrClassArray[$i], $extra, 1);
            $list_tag["TARGETCLASS"] .= $class_name_show[$hrClassArray[$i]];
            //

            $dispHrData = $dispDataAll[$hrClassArray[$i]];
            $dispHrDataCnt = get_count($dispHrData);

            $setTag = array();
            for ($ii = 0; $ii < $dispHrDataCnt; $ii++) {
                $attendSum = "";
                $chairSum  = "";
                $fontHead = "";
                $fontFoot = "";
                foreach (($dispDataAll[$hrClassArray[$i]][$ii]) as $key => $val) {
                    switch ($key) {
                        case "PERIODCD":            //表示用配列作成 //校時コードを作成
                            $noFound = $chairname_box[$hrClassArray[$i]][$val];
                            $attendSum = $attendCnt[$hrClassArray[$i]][$val];
                            $chairSum  = $chairCnt[$hrClassArray[$i]][$val];

                            if ($attendSum == $chairSum) {
                                $bgcolor = "bgcolor=\"#3399ff\"";
                            } elseif ($attendSum == "0") {
                                $bgcolor = "bgcolor=\"#ff0099\"";
                            } else {
                                $bgcolor = "bgcolor=\"#ffff00\"";
                                $fontHead = "<font color=\"black\">";
                                $fontFoot = "</font>";
                            }

                            $set_target = $key.$val;
                            $id_val = $val;
                            break;
                        case "TARGETCLASS":         //学級名を作成
                            $targetclass = $val;
                            break;
                        case "CHAIRCD":             //講座名を作成
                            $chaircd = $val;
                            break;
                        case "GRADE":               //講座名を作成
                            $grade = $val;
                            break;
                        case "HR_CLASS":            //講座名を作成
                            $hr_class = $val;
                            break;
                        case "TR_CD1":              //職員コードを作成
                            $tr_cd1 = $val;
                            break;
                        case "SELECT_EXECUTED":
                            //出欠の色を作成
                        default:
                            $list_tag[$key] = $val;
                    }   //end of switch
                }   //end of foreach

                //タグの作成
                $set_tag  = "<td nowrap width=\"95\" ";
                $set_tag .= " id=\"".$id_val.",".$hrClassArray[$i] ;
                $set_tag .= "\"".$bgcolor." value=\"".$chaircd."\" onClick=\"celcolchan(this,'$model->cntl_dt_key','$id_val','$grade','$hr_class','$tr_cd1','".STAFFCD."','$chaircd');\" ondblclick=\"IsUserOK_ToJump('$jumping','$model->cntl_dt_key','$id_val','$grade','$hr_class','$tr_cd1','".STAFFCD."','$chaircd');\">".$fontHead."<b>";
                $set_tag .= "".$noFound."</b>".$fontFoot."</td>";
                $list_tag["$set_target"] = $set_tag ;
                $setTag[$id_val]["TD"] = $set_tag ;
            }
            $dispTag["TD"] = "";
            $baseHeight = 30;
            $maxHeight = 30;
            $lineSize = 14;
            foreach ($model->allPeriodArray as $periCd => $periRow) {
                if ($setTag[$periCd]["TD"]) {
                    $dispTag["TD"] .= $setTag[$periCd]["TD"];
                    $brArray = explode("<BR>", $setTag[$periCd]["TD"]);
                    $celCnt = (get_count($brArray)-1);
                    $yohaku = $celCnt * 4;
                    $setLineSize = $lineSize;

                    $yohaku = $celCnt % 4 + 5;
                    $bityousei = getBityousei($celCnt);
                    $yohaku = $yohaku * 4 + $yohaku + ($celCnt / 2) + $bityousei;
                    $setLineSize = $lineSize + $celCnt % 4;

                    if ($maxHeight < ($celCnt * $setLineSize + $yohaku)) {
                        $maxHeight = ($celCnt * $setLineSize + $yohaku);
                    }
                } else {
                    $dispTag["TD"] .= "<td nowrap width=\"95\" height=\"{$maxHeight}\" bgcolor=\"#FFFFFF\">&nbsp;</td>";
                }
            }
            $dispTag["HR_HEIGHT"] = $maxHeight + $baseHeight;
            $arg["attend_data"][] = $dispTag;

            $setHrdata["HR_NAME"]  = knjCreateCheckBox($objForm, "TARGET_CHECK", $hrClassArray[$i], $extra, 1);
            $setHrdata["HR_NAME"] .= $class_name_show[$hrClassArray[$i]];
            $setHrdata["HR_HEIGHT"] = $maxHeight + $baseHeight;
            $arg["attend_data2"][] = $setHrdata;

            $arg["data"][] = $list_tag;
        }

        //フォーカス対象無し
        $first = ($first == "true")?"off":"first";

        //リンクボタンを作成する
        $extra = " onClick=\" Page_jumper('".$jumping."','1');\"";
        $arg["btn_jump"] = knjCreateBtn($objForm, "btn_jump", "出欠入力画面へ", $extra);

        //終 了ボタンを作成する
        $extra = " onclick=\"return closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", " 終 了 ", $extra);

        //更新可のユーザは全員出席ボタン不可
        $ch = ($class_cnt == 0) ? "disabled" : "";

        //更 新ボタンを作成する
        $extra = $ch." onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "全員出席", $extra);

        if (!isset($model->first_time_flg)) {
            $model->first_time_flg = "off";
        }

        //hiddenを作成する
        knjCreateHidden($objForm, "locker", $first);
        knjCreateHidden($objForm, "ID_NO", $model->first_id);
        knjCreateHidden($objForm, "backupper", $model->color_bk);
        knjCreateHidden($objForm, "chosen_id", $model->chosen_id);

        //ATTEND_DATに出欠データがある場合更新処理を続けるかを表示
        $attendexsits = $db->getOne(knjc030aQuery::getUpdateSelectData($model));
        $ex = (get_count($attendexsits)>0)?1:0;

        //制限処理月のチェック
        $monthch = $db->getOne(knjc030aQuery::chControlMonth($model));
        $mch = (get_count($monthch)>0)?1:0;

        //出席制御日付チェック
        $acd = IS_KANRISYA || ($model->cntl_dt_key > $model->attnd_cntl_dt) ? 1 : 0;

        knjCreateHidden($objForm, "attendexsits", $ex);
        knjCreateHidden($objForm, "monthch", $mch);
        knjCreateHidden($objForm, "attendctrldate", $acd);
        knjCreateHidden($objForm, "SEND_AUTH", AUTHORITY);

        $result->free();
        Query::dbCheckIn($db);

        //権限が無ければ閉じる
        if (AUTHORITY <= 2) {
            $arg["Closing"] = "  closing_window('cm'); " ;
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc030aForm1.html", $arg, 1);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $model, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    $count = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
        $count++;
    }
    if ($name == "GRADE") {
        if ($value != "" && $value_flg) {
            $value = $value;
        } else {
            if ($blank == "BLANK" && $model->Properties["knjc030aDefaultGrade"] == "1" && $count == 1) {
                $value = $opt[1]["value"];
            } else {
                $value = $opt[0]["value"];
            }
        }
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//微調整
function getBityousei($celCnt)
{
    if ($celCnt > 20) {
        return $celCnt / 2 * ($celCnt / 3);
    } else {
        return $celCnt / 2 * ($celCnt / 2);
    }
}
