<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje460_sien_kikanForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        // Add by PP for Title 2020-02-03 start
        if($model->name != ""){
            $arg["TITLE"] = "5.各関係機関画面";
            echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        }
        // Add by PP for Title 2020-02-20 end

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knje460_sien_kikanindex.php", "", "subform1");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB接続
        $db = Query::dbCheckOut();
        
        //年度の設定
        $model->field5["YEAR"] = ($model->cmd == "edit") ? $model->field5["YEAR"] : $model->exp_year;

        //学籍番号・生徒氏名表示
        $arg["data"]["NAME_SHOW"] = $model->schregno."  :  ".$model->name;
        
        //タイトル
        $arg["data"]["TITLE"] = "5.各関係機関からの具体的な支援について";

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform1" || $model->cmd == "edit"){
            if (isset($model->schregno) && !isset($model->warning)){
                $Row = $db->getRow(knje460_sien_kikanQuery::getMainQuery($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field5;
            }
        } else {
            $Row =& $model->field5;
        }

        //関係機関の設定
        $Row["SIEN_KIKAN"] = ($model->cmd == "edit") ? $model->field5["SIEN_KIKAN"] : $Row["SIEN_KIKAN"];

        //更新年度コンボ
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "id=\"YEAR\" onChange=\"current_cursor('YEAR'); return btn_submit('edit');\" aria-label=\"年度\"";
        // Add by PP for PC-Talker 2020-02-20 end
        $query = knje460_sien_kikanQuery::getYearCmb($model);
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field5["YEAR"], $extra, 1);

        //記入者
        $arg["data"]["ENTRANT_NAME"] = knje460_sien_kikanQuery::getStaffName($db, $model);

        //学校名
        $arg["data"]["SCHOOL_NAME"] = $Row["SCHOOL_NAME"];

        //学部
        $arg["data"]["FACULTY_NAME"] = $Row["FACULTY_NAME"];

        //学年
        $printGrade = "";
        if ("P" == $Row["SCHOOL_KIND"]) {
            if ($Row["GRADE_CD"] == '' || intval($Row["GRADE_CD"]) <= 3) {
                $printGrade = "1年～3年";
            } else if (4 <= intval($Row["GRADE_CD"])) {
                $printGrade = "4年～6年";
            }
        } else {
            $printGrade = intval($Row["SCHOOL_KIND_MIN_GRADE_CD"])."年～".intval($Row["SCHOOL_KIND_MAX_GRADE_CD"])."年";
        }
        $arg["data"]["GRADE"] = $printGrade;

        //よみがな
        $arg["data"]["KANA"] = $Row["KANA"];

        //氏名
        $arg["data"]["NAME"] = $Row["NAME"];

        //性別
        $arg["data"]["SEX"] = $Row["SEX"];

        //生年月日
        $arg["data"]["BIRTHDAY"] = $Row["BIRTHDAY"];

        //住所
        $arg["data"]["ADDR"] = $Row["ADDR"];

        //電話番号
        $arg["data"]["TELNO"] = $Row["TELNO"];

        //関係機関コンボ
        $extra = "onChange=\"return btn_submit('edit');\" aria-label='関係機関'";
        $query = knje460_sien_kikanQuery::getSienKikan($model, "ALL");
        makeCmb($objForm, $arg, $db, $query, "SIEN_KIKAN", $Row["SIEN_KIKAN"], $extra, 1, "ALL");

        //関係機関数
        $model->field5["SIEN_KIKAN_COUNT"] = $db->getOne(knje460_sien_kikanQuery::getSienKikan($model, $Row["SIEN_KIKAN"], 'COUNT'));
        knjCreateHidden($objForm, "SIEN_KIKAN_COUNT", $model->field5["SIEN_KIKAN_COUNT"]);

        //項目数
        $model->field5["SELECT_COUNT"] = 4;
        knjCreateHidden($objForm, "SELECT_COUNT", $model->field5["SELECT_COUNT"]);

        //更新する関係機関を設定
        $query = knje460_sien_kikanQuery::getSienKikan($model, $Row["SIEN_KIKAN"], $model->field5["PASTYEARLOADFLG"]);  //★
        $result = $db->query($query);
        $re = 1;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //関係機関
            $sienKikan = $row["VALUE"];
            knjCreateHidden($objForm, "SIEN_KIKAN".$re, $sienKikan);
            knjCreateHidden($objForm, "SIEN_KIKAN_MEI".$re, $row["LABEL"]);

            $dataList = array();
            $dataList["KIKAN_MEI"] = $row["LABEL"];

            //関係機関ごとの項目を設定
            for ($idx = 1;$idx <= $model->field5["SELECT_COUNT"]; $idx++) {
                $setVal = "";

                //項目名
                $setVal["KOUMOKU"] .= "項目".$idx;

                //支援内容テキスト
                if($model->cmd == "edit" || $Row["SIEN_".$sienKikan.$idx] == ""){
                    $Row["SIEN_".$sienKikan.$idx] = knje460_sien_kikanQuery::getSchregChallengedSupportplanFacilityDat($db, $model, $sienKikan, sprintf('%02d',$idx), 'REMARK', $model->field5["PASTYEARLOADFLG"]);  //★
                }
                $extra  = " id=\"SIEN_".$sienKikan.$idx."\" ";
                $setVal["SIEN"] .= knjCreateTextArea($objForm, "SIEN_".$sienKikan.$idx, $model->sien_gyou, ($model->sien_moji * 2), "", $extra, $Row["SIEN_".$sienKikan.$idx]);

                //次年度コンボ
                if($model->cmd == "edit" || $Row["STATUS_".$sienKikan.$idx] == ""){
                    $Row["STATUS_".$sienKikan.$idx] = knje460_sien_kikanQuery::getSchregChallengedSupportplanFacilityDat($db, $model, $sienKikan, sprintf('%02d',$idx), 'STATUS', $model->field5["PASTYEARLOADFLG"]);  //★
                }
                $extra = "";
                $query = knje460_sien_kikanQuery::getNameMst("E067");
                makeCmbList($objForm, $setVal, $db, $query, "STATUS", "STATUS_".$sienKikan.$idx, $Row["STATUS_".$sienKikan.$idx], $extra, 1, "BLANK");

                //設定
                $dataList["list"][] = $setVal;
            }
            $arg["shienKikan"][] = $dataList;
            $re++;
        }

        $arg["data"]["SIEN_TYUI"] = "(全角{$model->sien_moji}文字X{$model->sien_gyou}行)";

        //全支援内容数を設定
        $query = knje460_sien_kikanQuery::getSienKikanAll($model, 'COUNT');
        $cnt = $db->getOne($query);
        knjCreateHidden($objForm, "HID_SELECT_COUNT", $cnt);

        //全支援内容を設定
        $query = knje460_sien_kikanQuery::getSienKikanAll($model);
        $result = $db->query($query);
        $idx = 1;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            knjCreateHidden($objForm, "HID_SIEN_KIKAN_CD".$idx, $row["SPRT_FACILITY_CD"]);
            knjCreateHidden($objForm, "HID_SIEN_KIKAN".$idx, $row["SPRT_FACILITY_NAME"]);
            knjCreateHidden($objForm, "HID_SIEN".$idx, $row["REMARK"]);
            $idx++;
        }
        $result->free();

        //基礎情報
        if($Row["HIKITSUGI"] == "" || $model->field5["PASTYEARLOADFLG"] == "1"){
            $Row["HIKITSUGI"] = knje460_sien_kikanQuery::getSchregChallengedSupportplanDat($db, $model, '05', '01', 'REMARK', $model->field5["PASTYEARLOADFLG"]);  //★
        }
        $extra = " id=HIKITSUGI aria-label=引継事項全角{$model->hikitsugi_moji}文字X{$model->hikitsugi_gyou}行まで ";
        $arg["data"]["HIKITSUGI"] = KnjCreateTextArea($objForm, "HIKITSUGI", $model->hikitsugi_gyou, ($model->hikitsugi_moji * 2 + 1), "soft", $extra, $Row["HIKITSUGI"]);
        setInputChkHidden($objForm, "HIKITSUGI", $model->hikitsugi_moji, $model->hikitsugi_gyou, $arg);

        //過年度コンボ
        $extra = "aria-label='過年度'";
        $query = knje460_sien_kikanQuery::getPastYearCmb($db, $model, '05', '01');
        makeCmb($objForm, $arg, $db, $query, "PASTYEAR", $model->field5["PASTYEAR"], $extra, 1);

        Query::dbCheckIn($db);

        //年度追加ボタンを作成
        $extra = "id=\"btn_pastload\" onclick=\"current_cursor('btn_pastload'); return btn_submit('subform1_loadpastyear');\"";  //イベント名称はjsでフラグを立てたらeditに変換する。
        $arg["btn_pastload"] = KnjCreateBtn($objForm, "btn_pastload", "読込", $extra);

        //更新ボタンを作成
        // Add by PP for PC-Talker and current curosor 2020-02-03 start
        $extra = "id=\"btn_update\" onclick=\"current_cursor('btn_update'); return btn_submit('subform1_update');\"";
        $arg["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更新", $extra);
        // Add by PP for PC-Talker and current curosor 2020-02-20 end

        //更新ボタンを作成 (second btn)
        // Add by PP for PC-Talker and current curosor 2020-02-03 start
        $extra = "id=\"btn_update_1\" onclick=\"current_cursor('btn_update_1'); return btn_submit('subform1_update');\"";
        $arg["btn_update_1"] = KnjCreateBtn($objForm, "btn_update_1", "更新", $extra);
        // Add by PP for PC-Talker and current curosor 2020-02-20 end

        //戻るボタンを作成する
        $link = REQUESTROOT."/E/KNJE390/knje390index.php?cmd=subform3&SEND_PRGID={$model->getPrgId}&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻る", $extra);

        //hidden
        $nx = 1;
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_COLCNT", $outcnt);
        knjCreateHidden($objForm, "HID_ROWCNT", "1");
        knjCreateHidden($objForm, "HID_PASTYEARLOADFLG");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knje460_sien_kikanForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="")
{
    $opt = array();
    if ($blank == "BLANK") $opt[] = array('label' => "", 'value' => "");
    if ($blank == "ALL") $opt[] = array('label' => "全て", 'value' => "ALL");
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//コンボ作成(リスト)
function makeCmbList(&$objForm, &$setVal, $db, $query, $name1, $name2, &$value, $extra, $size, $blank="")
{
    $opt = array();
    if ($blank == "BLANK") $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $setVal[$name1] .= knjCreateCombo($objForm, $name2, $value, $opt, $extra, $size);
}
//選択ボタン
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $disabled="") {
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        if ($div == "kiso_zyouhou_select") {   //基礎情報選択
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/E/KNJE460_KISO_ZYOUHOU/knje460_kiso_zyouhouindex.php?cmd=kiso_zyouhou_select&program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&SEND_AUTH={$model->auth}&EXP_YEAR={$model->field2["YEAR"]}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}
function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo, &$arg) {
    $arg["data"][$setHiddenStr."_COMMENT"] = getTextAreaComment($keta, $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_KETA", $keta*2);
    KnjCreateHidden($objForm, $setHiddenStr."_GYO", $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_STAT", "statusarea_".$setHiddenStr);
}
function getTextAreaComment($moji, $gyo) {
    $comment = "";
    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}
?>
