<?php

require_once('for_php7.php');

class knjd135Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd135index.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //学校名取得
        $query = knjd135Query::getZ010();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->schoolName = $row["NAME1"];
        }
        $result->free();

        //学期コンボ作成
        $query = knjd135Query::getSemesterList();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //年組コンボ作成
        $query = knjd135Query::getGradeHrclass($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, 1);

        //初期化
        $model->data = array();
        $counter = 0;

        //一覧表示
        $colorFlg = false;
        $result = $db->query(knjd135Query::selectQuery($model));
        $arg["COMMUNICATION_TYUI"] = "(全角{$model->hreportremark_dat_communication_moji}文字{$model->hreportremark_dat_communication_gyou}行まで)";
        $arg["COMMUNICATION_TITLE"] = $model->tutisyoSyokenTitle;

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //出席番号
            if ($row["ATTENDNO"] != ""){
                $row["ATTENDNO"] = sprintf("%01d", $row["ATTENDNO"]).'番';
            }

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            //備考
            $value = (!isset($model->warning)) ? $row["REMARK"] : $model->fields["REMARK"][$counter];
            $height = $model->hreportremark_dat_communication_gyou * 13.5 + ($model->hreportremark_dat_communication_gyou -1 ) * 3 + 5;
            $row["REMARK"] = KnjCreateTextArea($objForm, "REMARK-".$counter, $model->hreportremark_dat_communication_gyou, ($model->hreportremark_dat_communication_moji * 2 + 1), "soft", "style=\"height:{$height}px;\" onPaste=\"return showPaste(this);\"", $value);

            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "hreportremark_dat_communication_gyou", $model->hreportremark_dat_communication_gyou);
        knjCreateHidden($objForm, "hreportremark_dat_communication_moji", $model->hreportremark_dat_communication_moji);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJD135");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd135Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //更新ボタンを作成する
    $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    //データCSVボタン
    $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX135CSV/knjx135csvindex.php?','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "データCSV", $extra);
}
?>
