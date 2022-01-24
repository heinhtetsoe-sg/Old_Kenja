<?php

require_once('for_php7.php');

class knjxattend_remarkForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("detail", "POST", "index.php", "", "detail");

        //DB OPEN
        $db = Query::dbCheckOut();
        //参照テーブル切替
        if ($model->semesflg !== '1') {
            $arg["TITLE"] = "日々出欠備考参照画面";
            $arg["ABSENCE_REMARK"] = "1";
            $query = knjxattend_remarkQuery::getRemark($model->schregno, $model->year, $model->sdate, $model->edate);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //和暦表示
                if ($model->warekiFlg == "1") {
                    $row["ATTENDDATE"] = str_replace("-","/",$row["ATTENDDATE"]);
                    $row["ATTENDDATE"] = common::DateConv1($row["ATTENDDATE"], 0);
                }
                $arg["data"][] = array("ATTENDDATE" => $row["ATTENDDATE"],
                                       "DI_REMARK"  => $row["DI_REMARK"]);
            }
        } else {
            $arg["TITLE"] = "まとめ出欠備考参照画面";
            $arg["SEMES_REMARK"] = "1";
            $query = knjxattend_remarkQuery::getSemesRemark($model->schregno, $model->year);
            $result = $db->query($query);
            $SET_REMARK = "";
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $SET_REMARK .= $row["REMARK1"]."\n";
            }
            //textarea
            // Edit by PP for PC-Talker(voice) 2020-01-20 start
            $query = knjxattend_remarkQuery::getName($model->schregno);
            $schName = $db->getOne($query);
            $extra = "aria-label=\"年度:$model->year $model->schregno $schName\"";
            // Edit by PP for PC-Talker(voice) 2020-01-31 end
            $arg["REMARK1"] = KnjCreateTextArea($objForm, "REMARK1", 12, 43, "soft", $extra, $SET_REMARK);
        }
        //年度コンボ
        $arg["YEAR"] = $model->year;

        //学籍番号
        $arg["SCHREGNO"] = $model->schregno;

        //氏名
        $query = knjxattend_remarkQuery::getName($model->schregno);
        $schName = $db->getOne($query);
        $arg["NAME"] = $schName;

        //終了ボタンを作成する
        // Add by PP for PC-Talker(voice) and cursor focus 2020-01-20 start
        $extra = "onclick=\"return parent.closeit()\" aria-label='戻る'";
        // Add by PP for PC-Talker(voice) and cursor focus 2020-01-31 end
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJXATTEND_REMARK");
        knjCreateHidden($objForm, "cmd", "");

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを$arg経由で渡す
        View::toHTML($model, "knjxattend_remarkForm1.html", $arg);
    }
}
?>
