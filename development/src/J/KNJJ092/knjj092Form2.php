<?php

require_once('for_php7.php');

class knjj092Form2 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjj092index.php", "", "edit");

        //警告メッセージを表示しない場合
        //すでにあるデータの更新の場合
        if (isset($model->schregNo) && isset($model->committeecd) && !isset($model->warning)) {
            $Row1 = knjj092Query::getCommitteeHistory_DatEdit($model);
        } else {
            $Row1 =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //委員会（左側で選ばれた委員会に合わせる）
        $queryN = knjj092Query::getCommitteeNameList($model);
        $value_flg = false;
        $resultN = $db->query($queryN);
        while ($rowN = $resultN->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optN[] = array('label' => $rowN["COMMITTEENAME"],
                            'value' => $rowN["COMMITTEECD"]);
            if ($model->SelectCommittee === $rowN["COMMITTEECD"]) $value_flg = true;
        }
        $resultN->free();

        $selectcommittee = ($model->SelectCommittee && $value_flg) ? $model->SelectCommittee : $optN[0]["value"];
        //委員会コード
        $arg["data"]["COMMITTEECODE1"] = knjCreateTextBox($objForm, $selectcommittee, "COMMITTEECODE1", 6, 6, "disabled");

        //学生情報・入力番号検索ボタン・在校生検索ボタン
        if ($model->committeeflg == "ON") {
            $schNo = "";  //学籍番号クリア
            $Row1 = "";    //詳細をクリアする
        } else {
            if (VARS::get("cmd") == "") {   //検索ボタンが押された場合
                $schNo = $model->field["SCHREGNO"];

                if (VARS::post("cmd") == "update" || VARS::post("cmd") == "add" || VARS::post("cmd") == "reset") {
                } else {
                    $Row1 = "";     //詳細をクリアする
                }
            } else if (VARS::get("cmd") == "edit") {    //左側で選択されたレコードの詳細の場合
                $schNo = $model->schregNo;
            }
        }

        //生徒の情報取得
        $querystu = knjj092Query::getStudent_data_One(CTRL_YEAR, CTRL_SEMESTER, $schNo);
        $resultstu = $db->query($querystu);
        $rowstu = $resultstu->fetchRow(DB_FETCHMODE_ASSOC);
        $resultstu->free();

        //学籍番号
        $arg["data"]["SCHREGNO"] = knjCreateTextBox($objForm, $schNo, "SCHREGNO", 9, 8, "");
        //氏名
        $arg["data"]["NAME"] = isset($rowstu["STUDENTNAME"]) ? $rowstu["STUDENTNAME"] : "";
        //年組
        $arg["data"]["HR_CLASS"] = isset($rowstu["NENKUMI"]) ? $rowstu["NENKUMI"] : "";
        //学年
        knjCreateHidden($objForm, "GRADE", isset($rowstu["GRADE"]) ? $rowstu["GRADE"] : "");

        //入力番号検索ボタン
        $extra = "onclick=\"btn_submit('search');\"";
        $arg["button"]["btn_input"] = knjCreateBtn($objForm, "btn_input", "入力番号検索", $extra);

        //在学生検索ボタン
        $extra = "onclick=\"wopen('../../X/KNJXSEARCH2/index.php?PATH=/J/KNJJ092/knjj092index.php&cmd=&target=KNJJ092','search', 0, 0, 700, 600);\"";
        $arg["button"]["btn_zaigaku"] = knjCreateBtn($objForm, "btn_zaigaku", "在校生検索", $extra);

        //役職区分コンボボックス
        $query = knjj092Query::getNameMst("J002");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "EXECUTIVECD", $Row1["EXECUTIVECD"], $extra, 1, "BLANK");

        //追加ボタン
        $extra = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('add')\"" : "disabled";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "登 録", $extra);
        //更新ボタン
        $extra = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update')\"" : "disabled";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);
        //削除ボタン
        $extra = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('delete')\"" : "disabled";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //記録詳細入力ボタン
        $extra = ($schNo) ? "onclick=\" wopen('".REQUESTROOT."/X/KNJXCOMMI_DETAIL/knjxcommi_detailindex.php?PROGRAMID=".PROGRAMID."&SCHREGNO=".$schNo."&SCHKIND={$model->schkind}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"" : "disabled";
        $arg["button"]["btn_detail"] = KnjCreateBtn($objForm, "btn_detail", "記録備考入力", $extra);

        //一括更新ボタン
        $extra = "onclick=\"return btn_submit('Ikkatsu');\"";
        $arg["button"]["btn_Ikkatsu"] = KnjCreateBtn($objForm, "btn_Ikkatsu", "一括登録", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_COMMITTEECD", $selectcommittee);
        if (isset($Row1["UPDATED"])) {
            knjCreateHidden($objForm, "UPDATED", $Row1["UPDATED"]);
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && VARS::post("cmd") != "search") {
            $arg["reload"] = "window.open('knjj092index.php?cmd=list&RELOADCOMMITTEE=".$selectcommittee."','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj092Form2.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
