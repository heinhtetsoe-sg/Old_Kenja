<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje460_kiso_zyouhouForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        // Add by PP for Title 2020-02-03 start
        if($model->name != ""){
            $arg["TITLE"] = "2.支援をする上での基礎情報画面";
            echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        }
        // Add by PP for Title 2020-02-20 end

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knje460_kiso_zyouhouindex.php", "", "subform1");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB接続
        $db = Query::dbCheckOut();

        //年度の設定
        $model->field2["YEAR"] = ($model->cmd == "edit") ? $model->field2["YEAR"] : $model->exp_year;

        //学籍番号・生徒氏名表示
        $arg["data"]["NAME_SHOW"] = $model->schregno."  :  ".$model->name;
        
        //タイトル
        $arg["data"]["TITLE"] = "2.支援をする上での基礎情報";

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform1" || $model->cmd == "edit"){
            if (isset($model->schregno) && !isset($model->warning)){
                $Row = $db->getRow(knje460_kiso_zyouhouQuery::getMainQuery($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field2;
            }
        } else {
            $Row =& $model->field2;
        }

        //更新年度コンボ
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "id=\"YEAR\" onChange=\"current_cursor('YEAR'); return btn_submit('edit');\" aria-label=\"年度\"";
        // Add by PP for PC-Talker 2020-02-20 end
        $query = knje460_kiso_zyouhouQuery::getYearCmb($model);
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field2["YEAR"], $extra, 1);

        //記入者
        $arg["data"]["ENTRANT_NAME"] = knje460_kiso_zyouhouQuery::getStaffName($db, $model);

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

        //基礎情報選択ボタン
        $arg["btn_kiso_zyouhou_select"] = makeSelectBtn($objForm, $model, "kiso_zyouhou_select", "btn_kiso_zyouhou_select", "基礎情報選択", "KISO_ZYOUHOU", "");

        //基礎情報
        if($Row["KISO_ZYOUHOU"] == "" || $model->field2["PASTYEARLOADFLG"] == "1"){
            $Row["KISO_ZYOUHOU"] = knje460_kiso_zyouhouQuery::getSchregChallengedSupportplanDat($db, $model, '02', '01', 'REMARK', $model->field2["PASTYEARLOADFLG"]);
        }
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = " id=KISO_ZYOUHOU aria-label=基礎情報選択全角{$model->kiso_zyouhou_moji}文字X{$model->kiso_zyouhou_gyou}行まで";
        // Add by PP for PC-Talker 2020-02-20 end
        $arg["data"]["KISO_ZYOUHOU"] = KnjCreateTextArea($objForm, "KISO_ZYOUHOU", ($model->kiso_zyouhou_gyou + 1), ($model->kiso_zyouhou_moji * 2 + 1), "soft", $extra, $Row["KISO_ZYOUHOU"]);
        setInputChkHidden($objForm, "KISO_ZYOUHOU", $model->kiso_zyouhou_moji, $model->kiso_zyouhou_gyou, $arg);

        //過年度コンボ
        $extra = "aria-label='過年度'";
        $query = knje460_kiso_zyouhouQuery::getPastYearCmb($db, $model, '02', '01');
        makeCmb($objForm, $arg, $db, $query, "PASTYEAR", $model->field2["PASTYEAR"], $extra, 1);

        Query::dbCheckIn($db);

        //年度追加ボタンを作成
        $extra = "id=\"btn_pastload\" onclick=\"current_cursor('btn_pastload'); return btn_submit('subform1_loadpastyear');\"";  //イベント名称はjsでフラグを立てたらeditに変換する。
        $arg["btn_pastload"] = KnjCreateBtn($objForm, "btn_pastload", "読込", $extra);
        
        //更新ボタンを作成
        // Add by PP for PC-Talker and current curosor 2020-02-03 start
        $extra = "id=\"btn_update\" onclick=\"current_cursor('btn_update'); return btn_submit('subform1_update');\"";
        $arg["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更新", $extra);
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
        View::toHTML5($model, "knje460_kiso_zyouhouForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="")
{
    $opt = array();
    if($space) $opt[] = array('label' => "", 'value' => "");
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

//選択ボタン
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $disabled="") {
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        if ($div == "kiso_zyouhou_select") {   //基礎情報選択
        // Add by PP for PC-Talker 2020-02-03 start
            $extra = $disabled." id=\"kiso_zyouhou_select\" onclick=\"current_cursor('kiso_zyouhou_select'); loadwindow('".REQUESTROOT."/E/KNJE460_KISO_ZYOUHOU/knje460_kiso_zyouhouindex.php?cmd=kiso_zyouhou_select&program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&SEND_AUTH={$model->auth}&EXP_YEAR={$model->field2["YEAR"]}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        // Add by PP for PC-Talker 2020-02-20 end
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

