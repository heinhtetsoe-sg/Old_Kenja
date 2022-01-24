<?php

require_once('for_php7.php');

class knjm502aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjm502aForm1", "POST", "knjm502aindex.php", "", "knjm502aForm1");

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
        $model->semsterCount = $db->getOne(knjm502aQuery::getSemester());
        if ($model->semsterCount == "3") {
            $arg["3gakki"] = '1';
        } else {
            $arg["2gakki"] = '1';
        }
        //クラス
        $query = knjm502aQuery::getHrClassList();
        $extra = "onchange=\"return btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "blank");

        //データ一覧
        $extra_commu = "style=\"height:75px;\"";
        $model->data["SCHREGNO"] = "";
        $query = knjm502aQuery::getMainQuery($model, $model->semsterCount);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = ($model->data["SCHREGNO"] == "") ? "" : ",";
            $model->data["SCHREGNO"] .= $sep.$row["SCHREGNO"];

            $communication = ($model->data["COMMUNICATION1".$row["SCHREGNO"]] == "") ? $row["COMMUNICATION1"] : $model->data["COMMUNICATION1".$row["SCHREGNO"]];
            $row["COMMUNICATION1"] = knjCreateTextArea($objForm, "COMMUNICATION1".$row["SCHREGNO"], 10, 21, "soft", $extra_commu, $communication);

            $communication = ($model->data["COMMUNICATION2".$row["SCHREGNO"]] == "") ? $row["COMMUNICATION2"] : $model->data["COMMUNICATION2".$row["SCHREGNO"]];
            $row["COMMUNICATION2"] = knjCreateTextArea($objForm, "COMMUNICATION2".$row["SCHREGNO"], 10, 21, "soft", $extra_commu, $communication);
            
            if ($model->semsterCount == "3") {
                $communication = ($model->data["COMMUNICATION3".$row["SCHREGNO"]] == "") ? $row["COMMUNICATION3"] : $model->data["COMMUNICATION3".$row["SCHREGNO"]];
                $row["COMMUNICATION3"] = knjCreateTextArea($objForm, "COMMUNICATION3".$row["SCHREGNO"], 10, 21, "soft", $extra_commu, $communication);
            } 
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
        View::toHTML($model, "knjm502aForm1.html", $arg);
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
    $extra = " onClick=\" wopen('".REQUESTROOT."/X/KNJX_M502/knjx_m502index.php?','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $row) {
    knjCreateHidden($objForm, "cmd");
}
?>
