<?php

require_once('for_php7.php');

class shokenlist {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("detail", "POST", "knja120bindex.php", "", "detail");

        //DB OPEN
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //項目の表示内容を設定
        if ($model->cmd === 'shokenlist1') {
            $arg["shokenlist1"] = "1";
        } else if ($model->cmd === 'shokenlist2') {
            $arg["shokenlist2"] = "1";
        } else if ($model->cmd === 'shokenlist3') {
            $arg["shokenlist3"] = "1";
        }

        //HTRAINREMARK_DAT 取得
        $model->data["YEAR"] = "";
        $result = $db->query(knja120bQuery::getTrainRow($model, "sanshou"));
        while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //特別活動所見
            if ($model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
                $extra = "style=\"height:145px;\"";
                $Row["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 10, 45, "soft", $extra, $Row["SPECIALACTREMARK"]);
            } else {
                $extra = "style=\"height:90px;\"";
                $Row["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 6, 23, "soft", $extra, $Row["SPECIALACTREMARK"]);
            }
            
            //総合所見
            if ($model->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
                $extra = "style=\"height:120px;\"";
                $Row["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", 8, 133, "soft", $extra, $Row["TOTALREMARK"]);
            } elseif ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
                $extra = "style=\"height:105px;\"";
                $Row["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", 7, 133, "soft", $extra, $Row["TOTALREMARK"]);
            } else {
                $extra = "style=\"height:90px;\"";
                $Row["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", 6, 89, "soft", $extra, $Row["TOTALREMARK"]);
            }
            
            //出欠の記録備考
            if ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
                $extra = "style=\"height:35px;\"";
                $Row["ATTENDREC_REMARK"] = knjCreateTextArea($objForm, "ATTENDREC_REMARK", 3, 81, "soft", $extra, $Row["ATTENDREC_REMARK"]);
            } else {
                $extra = "style=\"height:35px;\"";
                $Row["ATTENDREC_REMARK"] = knjCreateTextArea($objForm, "ATTENDREC_REMARK", 3, 41, "soft", $extra, $Row["ATTENDREC_REMARK"]);
            }
            
            $arg["data"][] = $Row;
        }
        $result->free();

        //戻るボタンを作成する
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA120B");
        knjCreateHidden($objForm, "SCHREGNO", "$model->schregno");

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを$arg経由で渡す
        View::toHTML($model, "shokenlist.html", $arg);
    }
}
/********************************************** 以下関数 ******************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if($name == "YEAR"){
        $value = ($value && $value_flg) ? $value : $model->exp_year;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>