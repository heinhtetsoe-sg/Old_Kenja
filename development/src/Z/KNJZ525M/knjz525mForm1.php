<?php

require_once('for_php7.php');

class knjz525mform1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz525mindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度
        $arg["YEAR"] = CTRL_YEAR;

        //データ取得
        if ($model->cmd != "kakutei" && $model->cmd != "chgPtrn" && !isset($model->warning)) {
            $Row = array();
            $maxDataDiv = "";
            $query = knjz525mQuery::getChallengedStatussheetItemNameDat(CTRL_YEAR);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["DATA_DIV"] == "0") {
                    $Row["SHEET_PATTERN"] = $row["SHEET_PATTERN"];
                    $Row["STATUS_NAME"]   = $row["STATUS_NAME"];
                    $Row["GROWUP_NAME"]   = $row["GROWUP_NAME"];
                    $Row["COMMENTS"]      = $row["COMMENTS"];
                } else {
                    $Row["DATA_DIV_NAME".$row["DATA_DIV"]] = $row["DATA_DIV_NAME"];
                }
                $maxDataDiv = $row["DATA_DIV"];
            }
            $Row["MAX_DATA_DIV"]  = $maxDataDiv;
            $Row["SETCNT"]        = $maxDataDiv;
        } else {
            $Row =& $model->field;
        }

        //帳票パターンラジオボタン
        $opt = array(1, 2);
        $Row["SHEET_PATTERN"] = ($Row["SHEET_PATTERN"] == "") ? "1" : $Row["SHEET_PATTERN"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SHEET_PATTERN{$val}\" onchange=\"return btn_submit('chgPtrn')\" ");
        }
        $radioArray = knjCreateRadio($objForm, "SHEET_PATTERN", $Row["SHEET_PATTERN"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //項目名テキスト（横・左）
        $extra = "";
        $arg["STATUS_NAME"] = knjCreateTextBox($objForm, $Row["STATUS_NAME"], "STATUS_NAME", 60, 60, $extra);
        $arg["STATUS_NAME_COMMENT"] = getTextAreaComment(12, 0);

        if ($Row["SHEET_PATTERN"] == 2) {
            $arg["PTRN2FLG"] = "1";
            //項目名テキスト（横・右）
            $extra = "";
            $arg["GROWUP_NAME"] = knjCreateTextBox($objForm, $Row["GROWUP_NAME"], "GROWUP_NAME", 60, 60, $extra);
            $arg["GROWUP_NAME_COMMENT"] = getTextAreaComment(12, 0);
        }

        //項目数テキスト
        $extra = " style=\"text-align: right\" ";
        $arg["MAX_DATA_DIV"] = knjCreateTextBox($objForm, $Row["MAX_DATA_DIV"], "MAX_DATA_DIV", 3, 2, $extra);

        //確定ボタン
        $extra = "onclick=\"return btn_submit('kakutei');\"";
        $arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);

        //項目数コメント
        $arg["MAX_DATA_DIV_COMMENT"] = "(半角数字1～{$model->max_item_num})";

        //一覧表示
        if ($Row["SETCNT"] > 0) {
            for ($i = 1; $i <= $Row["SETCNT"]; $i++) {
                $setTmp = array();
                $setTmp["DATA_DIV"] = '項目'.$i;
                //項目名テキスト（縦）
                $extra = "";
                $setTmp["DATA_DIV_NAME"] = knjCreateTextBox($objForm, $Row["DATA_DIV_NAME".$i], "DATA_DIV_NAME".$i, 60, 60, $extra);
                $setTmp["DATA_DIV_NAME_COMMENT"] = getTextAreaComment(30, 0);
                $arg["data"][] = $setTmp;
            }
        }
        //コメント
        $gyou = 5;
        $moji = 45;
        $arg["COMMENTS"] = knjCreateTextArea($objForm, "COMMENTS", $gyou, ($moji * 2), "soft", $extra, $Row["COMMENTS"]);
        $arg["COMMENTS_COMMENT"] = getTextAreaComment($moji, $gyou);

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

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SETCNT", $Row["SETCNT"]);
        knjCreateHidden($objForm, "MAX_ITEM_NUM", $model->max_item_num);

        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz525mForm1.html", $arg);
    }
}

function getTextAreaComment($moji, $gyo)
{
    $comment = "";
    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}
