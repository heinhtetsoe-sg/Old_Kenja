<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje460_zirituForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        // Add by PP for Title 2020-02-03 start
        if($model->name != ""){
            $arg["TITLE"] = "4.3年後に目指したい自立の姿画面";
            echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        }
        // Add by PP for Title 2020-02-20 end

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knje460_zirituindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度の設定
        $model->field4["YEAR"] = ($model->cmd == "edit") ? $model->field4["YEAR"] : $model->exp_year;

        //学籍番号・生徒氏名表示
        $arg["data"]["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //タイトル
        $arg["data"]["TITLE"] = "4.3年後に目指したい自立の姿";

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform1" || $model->cmd == "edit"){
            if (isset($model->schregno) && !isset($model->warning)){
                $Row = $db->getRow(knje460_zirituQuery::getMainQuery($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field4;
            }
        } else {
            $Row =& $model->field4;
        }

        //更新年度コンボ
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "id=\"YEAR\" onChange=\"current_cursor('YEAR'); return btn_submit('edit');\" aria-label=\"年度\"";
        // Add by PP for PC-Talker 2020-02-20 end
        $query = knje460_zirituQuery::getYearCmb($model);
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field4["YEAR"], $extra, 1);

        //記入者
        $arg["data"]["ENTRANT_NAME"] = knje460_zirituQuery::getStaffName($db, $model);

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
        
        //項目数
        $query = knje460_zirituQuery::getChallengedGoalYmst($model, 'COUNT');
        $cnt = $db->getOne($query);
        knjCreateHidden($objForm, "SELECT_COUNT", $cnt);  //項目数

        //項目を設定
        $query = knje460_zirituQuery::getChallengedGoalYmst($model);
        $result = $db->query($query);
        $idx = 1;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setVal = "";

            //SEQ
            knjCreateHidden($objForm, "SPRT_SEQ".$idx, $row["SPRT_SEQ"]);

            //項目名
            $extra  = " id=\"GOAL_TITLE".$idx."\" ";
            $setVal["GOAL_TITLE"] .= $row["GOAL_TITLE"];

            //自立テキスト
            if($Row["ZIRITU".$idx] == ""){
                $Row["ZIRITU".$idx] = knje460_zirituQuery::getSchregChallengedSupportplanDat($db, $model, '04', $row["SPRT_SEQ"], 'REMARK', $model->field4["PASTYEARLOADFLG"]);
            }
            // Add by PP for PC-Talker 2020-02-03 start
            $comment = "全角{$model->ziritu_moji}文字X{$model->ziritu_gyou}行";
            $extra  = " id=\"ZIRITU".$idx."\" aria-label=\"{$row["GOAL_TITLE"]}$comment\"";
            // Add by PP for PC-Talker 2020-02-20 end
            $setVal["ZIRITU"] .= knjCreateTextArea($objForm, "ZIRITU".$idx, $model->ziritu_gyou, ($model->ziritu_moji * 2 + 1), "", $extra, $Row["ZIRITU".$idx]);

            //設定
            $arg["list"][] = $setVal;
            $idx++;
        }
        $result->free();

        $arg["data"]["ZIRITU_TYUI"] = "(全角{$model->ziritu_moji}文字X{$model->ziritu_gyou}行)";

        //過年度コンボ
        $extra = "aria-label=\"過年度\"";
        $query = knje460_zirituQuery::getPastYearCmb($db, $model, '04', '01');  //登録件数が可変前提なので、先頭SEQでチェックする。
        makeCmb($objForm, $arg, $db, $query, "PASTYEAR", $model->field4["PASTYEAR"], $extra, 1);

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
        View::toHTML5($model, "knje460_zirituForm1.html", $arg);
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
?>
