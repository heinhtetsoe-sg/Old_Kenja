<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje460_gouriteki_hairyoForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;
        // Add by PP for Title 2020-02-03 start
        $arg["TITLE"] = "3.合理的配慮の情報画面";
        if($model->name != ""){
            $arg["TITLE"] = "3.合理的配慮の情報画面";
            echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        }
        // Add by PP for Title 2020-02-20 end

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knje460_gouriteki_hairyoindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度の設定
        $model->field3["YEAR"] = ($model->cmd == "edit") ? $model->field3["YEAR"] : $model->exp_year;

        //学籍番号・生徒氏名表示
        $arg["data"]["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //タイトル
        $arg["data"]["TITLE"] = "3.合理的配慮";

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform1" || $model->cmd == "edit"){
            if (isset($model->schregno) && !isset($model->warning)){
                $Row = $db->getRow(knje460_gouriteki_hairyoQuery::getMainQuery($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field3;
            }
        } else {
            $Row =& $model->field3;
        }

        //更新年度コンボ
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "id=\"YEAR\" onChange=\"current_cursor('YEAR'); return btn_submit('edit');\" aria-label=\"年度\"";
        // Add by PP for PC-Talker 2020-02-20 end
        $query = knje460_gouriteki_hairyoQuery::getYearCmb($model);
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field3["YEAR"], $extra, 1);

        //記入者
        $arg["data"]["ENTRANT_NAME"] = knje460_gouriteki_hairyoQuery::getStaffName($db, $model);

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

        //合理的配慮
        if($Row["GOURITEKI_HAIRYO"] == "" || $model->field3["PASTYEARLOADFLG"] == "1"){
            $Row["GOURITEKI_HAIRYO"] = knje460_gouriteki_hairyoQuery::getSchregChallengedSupportplanDat($db, $model, '03', '01', 'REMARK', $model->field3["PASTYEARLOADFLG"]);
        }
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = " id=GOURITEKI_HAIRYO aria-label=合理的配慮全角{$model->gouriteki_hairyo_moji}文字X{$model->gouriteki_hairyo_gyou}行まで ";
        // Add by PP for PC-Talker 2020-02-20 end
        $arg["data"]["GOURITEKI_HAIRYO"] = KnjCreateTextArea($objForm, "GOURITEKI_HAIRYO", ($model->gouriteki_hairyo_gyou + 1), ($model->gouriteki_hairyo_moji * 2 + 1), "soft", $extra, $Row["GOURITEKI_HAIRYO"]);
        setInputChkHidden($objForm, "GOURITEKI_HAIRYO", $model->gouriteki_hairyo_moji, $model->gouriteki_hairyo_gyou, $arg);

        //過年度コンボ
        $extra = "aria-label='過年度'";
        $query = knje460_gouriteki_hairyoQuery::getPastYearCmb($db, $model, '03', '01');
        makeCmb($objForm, $arg, $db, $query, "PASTYEAR", $model->field3["PASTYEAR"], $extra, 1);

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
        View::toHTML5($model, "knje460_gouriteki_hairyoForm1.html", $arg);
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


