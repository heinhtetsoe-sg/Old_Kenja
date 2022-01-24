<?php

require_once('for_php7.php');

class knjz526mform1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz526mindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度
        $arg["YEAR"] = CTRL_YEAR;

        //名称マスタ取得
        $query = knjz526mQuery::getNameMst("Z055", "");
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "KIND_NO", $model->kindNo, $extra, 1);

        //縦項目数最大取得
        $query = knjz526mQuery::getNameMst("Z055", $model->kindNo);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $maxKindNo = $row["ABBV3"];

        if ($model->cmd != "kakutei" && !isset($model->warning)) {
            $dataCnt = 0;
            // データ取得
            $query = knjz526mQuery::getChallengedSupportplanKindNameDat(CTRL_YEAR, $model->kindNo);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["KIND_SEQ"] == "000") {
                    //横項目取得
                    $statusList = $model->pattern[$model->kindNo];
                    for ($i=0; $i < get_count($statusList); $i++) { 
                        $colnum = $statusList[$i];
                        $Row[$colnum["COLNUM_NAME"]] = $row[$colnum["COLNUM_NAME"]];
                    }
                } else {
                    //縦項目取得
                    $dataCnt++;
                    $Row["KIND_NAME".$dataCnt] = $row["KIND_NAME"];
                }
            }
            $result->free();

            $Row["DATA_CNT"]  = $dataCnt > 0 ? $dataCnt : "";
            $Row["SETCNT"]    = $dataCnt;
        } else {
            $Row =& $model->field;
        }

        //横項目設定
        $statusList = $model->pattern[$model->kindNo];
        for ($i=0; $i < get_count($statusList); $i++) { 
            $colnum = $statusList[$i];
            $status = array();
            $status["STATUS_LABEL"] = $colnum["LABEL"];
            $extra = "";
            $status["STATUS_NAME"] = knjCreateTextBox($objForm, $Row[$colnum["COLNUM_NAME"]], $colnum["COLNUM_NAME"], 26, 24, $extra);
            $status["STATUS_NAME_COMMENT"] = getTextAreaComment(12, 0);
            $arg["data"][] = $status;
        }

        //縦項目設定
        if ($maxKindNo > 0) {
            $arg["supportplanKind"] = 1;
            for ($i=1; $i <= $Row["SETCNT"]; $i++) { 
                $kind = array();
                $kind["KIND_LABEL"] = "縦項目".$i;
                $extra = "";
                $kind["KIND_NAME"] = knjCreateTextBox($objForm, $Row["KIND_NAME".$i], "KIND_NAME".$i, 26, 24, $extra);
                $kind["KIND_NAME_COMMENT"] = getTextAreaComment(12, 0);
                $arg["data2"][] = $kind;
            }
            //項目数テキスト
            $extra = "style=\"text-align: right\" onblur=\"checkNum(this);\"";
            $arg["DATA_CNT"] = knjCreateTextBox($objForm, $Row["DATA_CNT"], "DATA_CNT", 3, 2, $extra);
        }

        //確定ボタン
        $extra = "onclick=\"return btn_submit('kakutei');\"";
        $arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);

        //項目数コメント
        $arg["DATA_CNT_COMMENT"] = "(半角数字1～{$maxKindNo})";

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SETCNT", $Row["SETCNT"]);
        knjCreateHidden($objForm, "MAX_DATA_CNT", $maxKindNo);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz526mForm1.html", $arg);
    }

}

// テキストボックス文字数
function getTextAreaComment($moji, $gyo) {
    $comment = "";
    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {

    //disabled
    $disable = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "" : " disabled";

    //前年度コピーボタン
    $extra = "onclick=\"return btn_submit('copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra.$disable);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra.$disable);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra.$disable);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

}

?>
