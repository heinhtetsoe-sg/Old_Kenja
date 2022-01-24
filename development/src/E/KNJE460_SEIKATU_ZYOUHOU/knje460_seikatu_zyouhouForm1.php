<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje460_seikatu_zyouhouForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;
        // Add by PP for Title 2020-02-03 start
        if($model->name != ""){
            $arg["TITLE"] = "1.現在の生活および将来の生活に関する希望画面";
            echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        }
        // Add by PP for Title 2020-02-20 end

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knje460_seikatu_zyouhouindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //カレンダー呼び出し
        $my = new mycalendar();

        //年度の設定
        $model->field1["YEAR"] = ($model->cmd == "edit") ? $model->field1["YEAR"] : $model->exp_year;

        //学籍番号・生徒氏名表示
        $arg["data"]["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //タイトル
        $arg["data"]["TITLE"] = "1.現在の生活および将来の生活に関する希望";

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform1" || $model->cmd == "edit"){
            if (isset($model->schregno) && !isset($model->warning)){
                $Row = $db->getRow(knje460_seikatu_zyouhouQuery::getMainQuery($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field1;
            }
        } else {
            $Row =& $model->field1;
        }

        //更新年度コンボ
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "id=\"YEAR\" onChange=\"current_cursor('YEAR'); return btn_submit('edit');\" aria-label=\"年度\"";
        // Add by PP for PC-Talker 2020-02-20 end
        $query = knje460_seikatu_zyouhouQuery::getYearCmb($model);
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field1["YEAR"], $extra, 1);

        //記入者コンボ
        $extra = "";
        if($Row["ENTRANT_NAME"] == ""){
            $Row["ENTRANT_NAME"] = knje460_seikatu_zyouhouQuery::getSchregChallengedSupportplanDat($db, $model, '01', '03', 'REMARK');
        }
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "aria-label='記入者'";
        // Add by PP for PC-Talker 2020-02-20 end
        $query = knje460_seikatu_zyouhouQuery::getStaffMst();
        makeCmb($objForm, $arg, $db, $query, "ENTRANT_NAME", $Row["ENTRANT_NAME"], $extra, 1, "BLANK");

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

        //療育手帳
        $query = knje460_seikatu_zyouhouQuery::getNameMst("E061", $Row["CHALLENGED_CARD_NAME"], 1);
        $arg["data"]["CHALLENGED_CARD_NAME"] = $db->getOne($query);

        //身体障害者手帳
        //身体障害の種別
        $query = knje460_seikatu_zyouhouQuery::getNameMst("E031", $Row["CHALLENGED_CARD_CLASS"], 1);
        $arg["data"]["CHALLENGED_CARD_CLASS"] = $db->getOne($query);
        //身体障害の等級
        $query = knje460_seikatu_zyouhouQuery::getNameMst("E032", $Row["CHALLENGED_CARD_RANK"], 1);
        $arg["data"]["CHALLENGED_CARD_RANK"] = $db->getOne($query);
        //手帳の障害名
        $query = knje460_seikatu_zyouhouQuery::getChallengedCardNameMst($Row["CHALLENGED_CARD_AREA_NAME"], 1);
        $arg["data"]["CHALLENGED_CARD_AREA_NAME"] = $db->getOne($query);
        $query = knje460_seikatu_zyouhouQuery::getChallengedCardNameMst($Row["CHALLENGED_CARD_AREA_NAME2"], 1);
        $arg["data"]["CHALLENGED_CARD_AREA_NAME2"] = $db->getOne($query);
        $query = knje460_seikatu_zyouhouQuery::getChallengedCardNameMst($Row["CHALLENGED_CARD_AREA_NAME3"], 1);
        $arg["data"]["CHALLENGED_CARD_AREA_NAME3"] = $db->getOne($query);

        //精神障害者保健福祉手帳
        //等級
        $query = knje460_seikatu_zyouhouQuery::getNameMst("E063", $Row["CHALLENGED_CARD_REMARK"], "1");
        $arg["data"]["CHALLENGED_CARD_REMARK"] = $db->getOne($query);

        //療育手帳の次回判定
        $arg["data"]["CHALLENGED_CARD_CHECK_YM"] = getMonth($my, $Row["CHALLENGED_CARD_CHECK_YM"]);
//        $arg["data"]["CHALLENGED_CARD_CHECK_YM"] = $my->MyMonthWin($objForm, "CHALLENGED_CARD_CHECK_YM", $Row["CHALLENGED_CARD_CHECK_YM"]);

        //身体障害手帳の次回認定
        //チェックボックスを作成
        $val = $Row["CHALLENGED_CARD_GRANT_FLG"] == "1" ? "次回認定なし" : "　";
        //次回認定月を作成
        if ($val == "　") {
            $month = getMonth($my, $Row["CHALLENGED_CARD_GRANT_YM"]);
            if ($month) {
                $val = $month;
            }
        }
        $arg["data"]["CHALLENGED_CARD_GRANT_YM"] = $val;
//        $arg["data"]["CHALLENGED_CARD_GRANT_YM"] = $my->MyMonthWin($objForm, "CHALLENGED_CARD_GRANT_YM", $Row["CHALLENGED_CARD_GRANT_YM"]);
        //精神障害者保健福祉手帳の有効期限
        $arg["data"]["CHALLENGED_CARD_BAST_YM"] = getMonth($my, $Row["CHALLENGED_CARD_BAST_YM"]);
//        $arg["data"]["CHALLENGED_CARD_BAST_YM"] = $my->MyMonthWin($objForm, "CHALLENGED_CARD_BAST_YM", $Row["CHALLENGED_CARD_BAST_YM"]);


        /******************/
        /* テキストエリア */
        /******************/
        //本人希望
        if($Row["HOPE"] == "" || $model->field1["PASTYEARLOADFLG"] == "1"){
            $Row["HOPE"] = knje460_seikatu_zyouhouQuery::getSchregChallengedSupportplanDat($db, $model, '01', '01', 'REMARK', $model->field1["PASTYEARLOADFLG"]);
        }
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = " id=HOPE aria-label=本人全角{$model->hope_moji}文字X{$model->hope_gyou}行まで ";
        $arg["data"]["HOPE"] = KnjCreateTextArea($objForm, "HOPE", ($model->hope_gyou + 1), ($model->hope_moji * 2 + 1), "soft", $extra, $Row["HOPE"]);
        setInputChkHidden($objForm, "HOPE", $model->hope_moji, $model->hope_gyou, $arg);
        // Add by PP for PC-Talker 2020-02-20 end

        //保護者希望
        if($Row["GUARDIAN_HOPE"] == "" || $model->field1["PASTYEARLOADFLG"] == "1"){
            $Row["GUARDIAN_HOPE"] = knje460_seikatu_zyouhouQuery::getSchregChallengedSupportplanDat($db, $model, '01', '02', 'REMARK', $model->field1["PASTYEARLOADFLG"]);
        }
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = " id=GUARDIAN_HOPE aria-label=保護者全角{$model->guardian_hope_moji}文字X{$model->guardian_hope_gyou}行まで ";
        $arg["data"]["GUARDIAN_HOPE"] = KnjCreateTextArea($objForm, "GUARDIAN_HOPE", ($model->guardian_hope_gyou + 1), ($model->guardian_hope_moji * 2 + 1), "soft", $extra, $Row["GUARDIAN_HOPE"]);
        setInputChkHidden($objForm, "GUARDIAN_HOPE", $model->guardian_hope_moji, $model->guardian_hope_gyou, $arg);
        // Add by PP for PC-Talker 2020-02-20 end

        //過年度コンボ
        $extra = "aria-label='過年度'";
        $query = knje460_seikatu_zyouhouQuery::getPastYearCmb($db, $model, '01', '01');
        makeCmb($objForm, $arg, $db, $query, "PASTYEAR", $model->field1["PASTYEAR"], $extra, 1);

        Query::dbCheckIn($db);

        //年度追加ボタンを作成
        $extra = "id=\"btn_pastload\" onclick=\"current_cursor('btn_pastload'); return btn_submit('subform1_loadpastyear');\"";  //イベント名称はjsでフラグを立てたらeditに変換する。
        $arg["btn_pastload"] = KnjCreateBtn($objForm, "btn_pastload", "読込", $extra);

        //戻るボタンを作成する
        $link = REQUESTROOT."/E/KNJE390/knje390index.php?cmd=subform3&SEND_PRGID={$model->getPrgId}&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻る", $extra);

        //更新ボタンを作成
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "id=\"btn_update\" onclick=\"current_cursor('btn_update'); return btn_submit('subform1_update');\"";
        $arg["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更新", $extra);
        // Add by PP for PC-Talker 2020-02-20 end

        //hidden
        $nx = 1;
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_COLCNT", $outcnt);
        knjCreateHidden($objForm, "HID_ROWCNT", "1");
        knjCreateHidden($objForm, "HID_PASTYEARLOADFLG");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML5($model, "knje460_seikatu_zyouhouForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="")
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

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
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

//年月の変換
function getMonth($my, $value) {
    $initialdate = "";
    if(substr($value,0,1)=='H' | substr($value,0,1)=='S'|
      substr($value,0,1)=='T' | substr($value,0,1)=='M' ){
        $initialdate = $value;
    }else{
        $initialdate = $my->ChgWToJ($value); //西暦YYYY-MM-DDをGYY/MM/DDに変換
    }
    return $initialdate;
}

?>
