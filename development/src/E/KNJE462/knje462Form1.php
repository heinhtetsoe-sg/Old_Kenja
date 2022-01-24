<?php

require_once('for_php7.php');

class knje462form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knje462index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度
        $arg["YEAR"] = CTRL_YEAR;

        //前年度コピーボタン
        $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('copy');\"" : "disabled";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //登録済データ取得
        $savedat = array();
        $savecnt = 0;
        $query = knje462Query::getChallengedSupportBaseInfoYmst($model, "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["SPRT_SEQ"] != "") {
                $savedat[$row["SPRT_SEQ"]] = $row["BASE_TITLE"];
                $savecnt++;
            }
        }

        //コンボを変更したら項目数をリセット
        if ($model->cmd == "reset" || $model->cmd == "clear") {
            unset($model->compcnt);
        }

        //データ件数
        if ($model->compcnt == "") {
            $cnt = ($savecnt > 0 && $model->cmd != "clear") ? $savecnt : "";
        } else {
            $cnt = $model->compcnt;
        }

        //項目数数テキスト
        $model->compcnt = (($model->cmd != "level" && $model->cmd != "check") || $model->compcnt == "") ? $cnt : $model->compcnt;
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["COMPCNT"] = knjCreateTextBox($objForm, $model->compcnt, "COMPCNT", 2, 2, $extra);

        //確定ボタン
        $extra = "onclick=\"return level(".$cnt.");\"";
        $arg["button"]["btn_comp"] = knjCreateBtn($objForm, "btn_comp", "確 定", $extra);

        if ($model->compcnt != "" && is_numeric($model->compcnt) && is_int(intval($model->compcnt))) {
            $arg["dispcomptype_p2"] = "1";

            //一覧表示
            $extra = "";
            for ($i = 1; $i <= $model->compcnt; $i++) {
                $Row = array();
                //注意：条件によって文字色を調整するため、styleの終了記号を”後付けしている”ので、注意。
                //切り替わりのタイミングで設定する
                if ($model->cmd == "main" || $model->cmd == "" || $model->cmd == "reset" || $model->cmd == "level") {
                    $extra = "id =\"BASETITLENAME_".$i."\" onchange=\"resetBaseTitleNamecolor(this, '".$i."');\" style=\"text-align: left;\"";
                    $Row["BASETITLENAME"] = knjCreateTextBox($objForm, $savedat[sprintf("%02d", $i)], "BASETITLENAME_".$i, 100, 50, $extra);
                    $model->field["BASETITLENAME_INFLG_".$i] = "0";
                } else {
                    if ($model->field["BASETITLENAME_INFLG_".$i] == "1") {
                        $extra = "id =\"BASETITLENAME_".$i."\" onchange=\"resetBaseTitleNamecolor(this, '".$i."');\" style=\"text-align: left; color: #FF0000;\"";
                    } else {
                        $extra = "id =\"BASETITLENAME_".$i."\" onchange=\"resetBaseTitleNamecolor(this, '".$i."');\" style=\"text-align: left;\"";
                    }
                    $Row["BASETITLENAME"] = knjCreateTextBox($objForm, $model->field["BASETITLENAME_".$i], "BASETITLENAME_".$i, 100, 50, $extra);
                }

                knjCreateHidden($objForm, "BASETITLENAME_INFLG_".$i, $model->field["BASETITLENAME_INFLG_".$i]);
                $Row["ASSESSTITLE"] = "項目".$i;
                
                $arg["data"][] = $Row;
            }
        } else {
            unset($model->compcnt);
        }

        //更新ボタン
        $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update');\"" : "disabled";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);
        //削除ボタン
        $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('delete');\"" : "disabled";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"return closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //Hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "COMPVAL", $model->compcnt);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJD418");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje462Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    $maxlabellen = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
        $maxlabellen = $maxlabellen < strlen($row["LABEL"]) ? strlen($row["LABEL"]) : $maxlabellen;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    return $maxlabellen;
}

function makeCmbInList(&$objForm, &$arg, $db, $query, $name, $nameId, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    $maxlabellen = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
        $maxlabellen = $maxlabellen < strlen($row["LABEL"]) ? strlen($row["LABEL"]) : $maxlabellen;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $nameId, $value, $opt, $extra, $size);
    return $maxlabellen;
}
?>
