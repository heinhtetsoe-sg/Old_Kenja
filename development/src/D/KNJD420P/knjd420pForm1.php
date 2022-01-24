<?php

require_once('for_php7.php');

class knjd420pForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd420pindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //タイトル
        $arg["TITLE"] = "個別の指導計画（自立活動中心用）";

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $arg["NOT_WARNING"] = 1;
        }

        // BTN_SUBFORMCALL : ボタン名称
        // KINDCD          : データ種別(2:1,4,9 3:2,3,5,6 4:10 5:7,8 <- ※sqlにて指定)
        // SCHREGNO        : 生徒情報
        // GHR             : 法廷/実クラス
        // KIND_NO         : DB登録タイプ

        $schregno_is_selected = "";
        $arrTitle_wrk = array();
        //生徒が選択されてから表示する処理
        if ($model->schregno) {
            $schregno_is_selected = "1";
            //useKnjd425DispUpdDateプロパティが立っているときのみ、日付を利用。
            if ($model->Properties["useKnjd425DispUpdDate"] == "1") {
                $arg["dispseldate"] = "1";
                //日付選択
                $arg["data"]["UPDTITLE"] = "更新日:&nbsp;";
                $query = knjd420pQuery::getUpdatedDateList($model);
                $extra = "onchange=\"btn_submit('edit');\"";
                $opt = array();
                $opt[] = array("label"=>"新規", "value"=>"9999/99/99");
                makeDateCmb($objForm, $arg, $db, $query, "UPDDATE", $model->upddate, $extra, 1, $opt);

                if ($model->upddate == "9999/99/99") {
                    $arg["newdate"] = "1";
                    $model->selnewdate = $model->selnewdate == "" ? str_replace("-", "/", CTRL_DATE) : $model->selnewdate;

                    $param = "extra=btn_submit(\'edit\');";
                    $arg["data"]["SELNEWDATE"] = View::popUpCalendar($objForm, "SELNEWDATE", str_replace("-", "/", $model->selnewdate), $param);
                } else {
                    $model->selnewdate = "";
                }
            } else {
                //固定日付で処理
                $model->selnewdate = "9999/03/31";
                $model->upddate = $model->selnewdate;
            }

            //項目タイトル取得
            $arrTitle = array();
            $kindNo = "30";
            $query = knjd420pQuery::getHDKindNameDat($model, $kindNo);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $arrTitle[$row["KIND_SEQ"]] = $row["KIND_REMARK"];
            }
            $result->free();

            foreach($model->title_key as $key => $value) {
                $arrTitle_wrk[$value] = (isset($arrTitle[$key]) && $arrTitle[$key] != "") ? $arrTitle[$key] : $model->title_default[$value];
                $arg[$value."_TITLE"] = $arrTitle_wrk[$value];
            }

            //学期コンボ
            if ($model->field["SEMESTER"] == "") $model->field["SEMESTER"] = $model->exp_semester;
            $query = knjd420pQuery::getSemester($model);
            $extra = "onchange=\"return btn_submit('changeSemester');\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

            //対象データ取得
            $arrZirituKatudou= array();
            $query = knjd420pQuery::getZirituKatudouList($model);
            $result = $db->query($query);

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["SEMESTER"] == "9") {
                    //学年末の場合
                    for ($i = 1; $i <= 4; $i++) {
                        $key = sprintf("%03d", $i);
                        $arrZirituKatudou[$model->title_key[$key]] = isset($row[$model->title_key[$key]]) ? $row[$model->title_key[$key]] : "";
                    }
                } else {
                    for ($i = 1; $i <= 4; $i++) {
                        $arg["list/SEMESTERNAME"] = ($i == 1) ? "1" : "";
                        $list_wrk = array();
                        if ($i == 1) {
                            $list_wrk["SEMESTERNAME"] = $row["SEMESTERNAME"];
                        }
                        $key = sprintf("%03d", $i+8);
                        $list_wrk["ITEM"] = $arrTitle_wrk[$model->title_key[$key]];
                        $list_wrk["SORT_SEMESTER"] = $row["SEMESTER"];
                        $list_wrk["SORT_ITEM"] = $i;
                        for ($j = 5; $j <= 8; $j++) {
                            $key = sprintf("%03d", $j);
                            $value = isset($row[$model->title_key[$key].$i]) ? $row[$model->title_key[$key].$i] : "";
                            $list_wrk[$model->title_key[$key]] = str_replace("\n", "<BR>", str_replace("\r\n", "<BR>", $value));
                            if ($row["SEMESTER"] == $model->field["SEMESTER"]) {
                                //選択学期の場合
                                $arrZirituKatudou[$model->title_key[$key].$i] = $value;
                            }
                        }
                        $arg["list"][] = $list_wrk;
                    }
                }
            }
            $result->free();

            //ソート用
            makeSortLink($arg, $objForm, $model);

            //入力テキスト作成
            makeText($objForm, $arg, $model, $arrZirituKatudou);
        }

        $arg["schregno_is_selected"] = $schregno_is_selected == "1" ? "1" : "";
        $arg["schregno_is_not_selected"] = $schregno_is_selected == "1" ? "" : "1";

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "PRGID", "KNJD420P");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "GRADE_HR_CLASS");
        knjCreateHidden($objForm, "USEKNJD420PDISPUPDDATE", $model->Properties["useKnjd425DispUpdDate"]);
        knjCreateHidden($objForm, "UPDATE_INDEX");
        //タイトルキー
        foreach($model->title_key as $key => $value) {
            knjCreateHidden($objForm, "TITLE_".$key, $value);
        }    
        //タイトル
        foreach($arrTitle_wrk as $key => $value) {
            knjCreateHidden($objForm, "INIT_".$key."_TITLE", $value);
        }        
        //項目値
        for ($i = 1; $i <= 4; $i++) {
            $key = sprintf("%03d", $i);
            knjCreateHidden($objForm, "INIT_".$model->title_key[$key], isset($arrZirituKatudou[$model->title_key[$key]]) ? $arrZirituKatudou[$model->title_key[$key]] : "");
        }
        for ($i = 1; $i <= 4; $i++) {
            for ($j = 5; $j <= 8; $j++) {
                $key = sprintf("%03d", $j);
                knjCreateHidden($objForm, "INIT_".$model->title_key[$key].$i, isset($arrZirituKatudou[$model->title_key[$key].$i]) ? $arrZirituKatudou[$model->title_key[$key].$i] : "");
            }
        }

        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjd420pForm1.html", $arg);
    }
}

//テキスト作成
function makeText(&$objForm, &$arg, $model, $arrZirituKatudou) {
    //支援計画の目標・配慮事項・重点目標・目標設定理由
    for ($i = 1; $i <= 4; $i++) {
        $key = sprintf("%03d", $i);
        $value = ($model->cmd == "update" || $model->cmd == "sort" || $model->cmd == "updateEnd") ? $model->field[$model->title_key[$key]] : $arrZirituKatudou[$model->title_key[$key]];
        setRemarkTextArea($objForm, $arg, $model, $value, $model->title_key[$key]);
    }

    //縦項目・横項目
    for ($i = 1; $i <= 4; $i++) {
        for ($j = 5; $j <= 8; $j++) {
            $key = sprintf("%03d", $j);
            $value = ($model->cmd == "update" || $model->cmd == "sort" || $model->cmd == "updateEnd") ? $model->field[$model->title_key[$key].$i] : $arrZirituKatudou[$model->title_key[$key].$i];
            setRemarkTextArea($objForm, $arg, $model, $value, $model->title_key[$key].$i, true);
        }
    }
}

function setRemarkTextArea(&$objForm, &$arg, $model, $value, $key, $list_flg = false) {
    $br_str = $list_flg ? "<br>" : "";
    $css_str = $list_flg ? "overflow-y:scroll;" : "";
    $moji = $model->textLimit[$key]["moji"];
    $gyou = $model->textLimit[$key]["gyou"];

    $extra = "id=\"".$key."\"";
    if ($css_str != "") {
        $extra .= "style=\"".$css_str."\"";
    }

    $arg[$key] = knjCreateTextArea($objForm, $key, $gyou, ($moji * 2), "", $extra, $value);
    $arg["EXTFMT_".$key] .= "<font size=2, color=\"red\">(全角".$moji."文字X".$gyou."行まで)</font>".$br_str;
    knjCreateHidden($objForm, $key."_KETA", ($moji * 2));
    knjCreateHidden($objForm, $key."_GYO", $gyou);
    KnjCreateHidden($objForm, $key."_STAT", "statusarea_".$key);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    if ($model->schregno) {
        for ($i; $i <= 3; $i++) {
            //更新
            $extra = "onClick=\"return btn_submit('update', '".$i."')\"";
            $arg["btn_update_".$i] = knjCreateBtn($objForm, "btn_update_".$i, "更 新", $extra);

            //取消
            $extra = "onClick=\"return resetRemark('".$i."')\"";
            $arg["btn_reset_".$i] = knjCreateBtn($objForm, "btn_reset_".$i, "取 消", $extra);
        }

        //戻る
        $link = REQUESTROOT."/D/KNJD420P/knjd420pindex.php?cmd=clear&PROGRAMID=KNJD420P";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);
    }

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//日付コンボ作成
function makeDateCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $opt=array())
{
    $result = $db->query($query);
    $defValue = '';
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_unshift($opt, array('label' => str_replace("-", "/", $row["LABEL"]),
                       'value' => str_replace("-", "/", $row["VALUE"])));
        if ($row["DEF_VALUE_FLG"] == '1') {
            $defValue = str_replace("-", "/", $row["VALUE"]);
        }
    }

    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value == "" && $defValue) ? $defValue : ($value ? $value : $opt[0]["value"]);
    } else {
        $value = ($value == "") ? $opt[0]["value"] : $value;
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//西暦をH99.00.00形式で変換
function Calc_SNameWareki(&$year, $month, $day)
{
    $border = array();

    $warekiList = array();
    $warekiList = common::getWarekiList();

    for ($i = 0; $i < get_count($warekiList); $i++) {
        $warekiInfo = $warekiList[$i];
        $start = str_replace("/", "", $warekiInfo['Start']);
        $end = str_replace("/", "", $warekiInfo['End']);
        $border[] = array("開始日" =>  $start, "終了日" => $end, "元号" => $warekiInfo['SName']);
    }

    $target = sprintf("%04d%02d%02d", $year, $month, $day);
    for ($i = 0; $border[$i]; $i++){
        if ($border[$i]["開始日"] <= $target &&
            $target <= $border[$i]["終了日"] ){
            $year = ($year - substr($border[$i]["開始日"], 0, 4) + 1);
            return $border[$i]["元号"] .(sprintf("%02d", (int) $year));
        }

    }
    return false;
}

function makeSortLink(&$arg, &$objForm, &$model) {
    $makeDataArray = array();
    $makeDataArray[] = array('name' => 'typeSemester',             'label' => '学期');
    $mark = "▲";

    foreach ($makeDataArray as $key => $val) {
        $extra = "style=\"color:#FFFFFF;\" onClick=\"return dataSort('{$val['name']}');\"";
        $arg[$val['name']] = View::alink("javascript:void(0)", htmlspecialchars($val['label'].$mark), $extra);
        knjCreateHidden($objForm, "hidden_{$val['name']}", "1");
    }
}

?>
