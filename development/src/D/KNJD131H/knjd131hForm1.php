<?php

require_once('for_php7.php');

class knjd131hForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd131hForm1", "POST", "knjd131hindex.php", "", "knjd131hForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR.'年度';
        
        //学期
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;
        
        //データをクリア
        if ($model->cmd === 'clear' || $model->cmd === 'change') unset($model->data);

        //学期数取得
        $model->semsterCount = "";
        $model->semsterCount = $db->getOne(knjd131hQuery::getSemester());
        if ($model->semsterCount == "3") {
            $arg["3gakki"] = '1';
        } else {
            $arg["2gakki"] = '1';
        }
        
        //フォーマット設定
        $arg["setE"] = '1';
        $arg["FORMAT"] = "55%";
        //学期は使用しないので、フォーマット崩れ対策に2学期用のレイアウトをセット
        $arg["3gakki"] = "";
        $arg["2gakki"] = '1';
        
        //クラス
        $query = knjd131hQuery::getHrClassList();
        $extra = "onchange=\"return btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "blank");

        //データ一覧
        $extra_spe   = "style=\"height:145px;\"";
        $model->data["SCHREGNO"] = "";
        $query = knjd131hQuery::getMainQuery($model, $model->semsterCount);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $sep = ($model->data["SCHREGNO"] == "") ? "" : ",";
            $model->data["SCHREGNO"] .= $sep.$row["SCHREGNO"];
            //特別活動の記録
            $specialactremark = ($model->data["SPECIALACTREMARK".$row["SCHREGNO"]] == "") ? $row["SPECIALACTREMARK"] : $model->data["SPECIALACTREMARK".$row["SCHREGNO"]];
            $row["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK".$row["SCHREGNO"], 10, 21, "soft", $extra_spe, $specialactremark);
            $arg["data"][] = $row;
        }
        $result->free();

        //ボタン作成
        makeBtn($objForm, $arg, $model, $Row);

        //hidden作成
        makeHidden($objForm, $model, $row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd131hForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $Row) {

    //更新ボタン
    $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : "disabled";
    $extra = $disable." onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //取消ボタン
    $extra = " onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    
    //csv
    $extra = " onClick=\" wopen('".REQUESTROOT."/X/KNJX154E/knjx154eindex.php?','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $row) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}
?>
