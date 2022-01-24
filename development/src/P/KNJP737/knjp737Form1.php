<?php

require_once('for_php7.php');

class knjp737form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度コンボ
        $query = knjp737Query::getYear($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1);

        //校種コンボ
        $query = knjp737Query::getSchoolKind($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->school_kind, $extra, 1);

        //コンボを変更したら段階数をリセット
        if ($model->cmd == "change") {
            unset($model->s_row_no);
            unset($model->row_no_cnt);
        }

        //開始列番号テキスト
        $minRowNo = $db->getOne(knjp737Query::getRowNoData($model, "min"));
        $model->s_row_no = (($model->cmd != "reflect" && $model->cmd != "check") || $model->s_row_no == "") ? $minRowNo : $model->s_row_no;
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["S_ROW_NO"] = knjCreateTextBox($objForm, $model->s_row_no, "S_ROW_NO", 3, 3, $extra);

        //項目数取得
        $cnt = $db->getOne(knjp737Query::getRowNoData($model, "cnt"));
        $cnt = ($cnt > 0) ? $cnt : "";

        //項目数テキスト
        $model->row_no_cnt = (($model->cmd != "reflect" && $model->cmd != "check") || $model->row_no_cnt == "") ? $cnt : $model->row_no_cnt;
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["ROW_NO_CNT"] = knjCreateTextBox($objForm, $model->row_no_cnt, "ROW_NO_CNT", 3, 3, $extra);

        //反映ボタン
        $extra = "onclick=\"return reflect();\"";
        $arg["button"]["btn_ref"] = knjCreateBtn($objForm, "btn_ref", "反 映", $extra);

        //ファイルからの取り込み
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

        //実行ボタン
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //列番号情報格納
        $csvInfoArray = array();
        $query = knjp737Query::getRowNoData($model, "list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $csvInfoArray[$row["ROW_NO"]] = $row["GRP_CD"];
        }
        $result->free();

        //CSVヘッダ
        $headNameArray = array();
        $query = knjp737Query::getHeadName($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $headNameArray[$row["ROW_NO"]] = $row["HEAD_NAME"];
        }
        $result->free();

        //グループコンボ配列
        $groupArray = array();
        $groupArray[] = array("label" => "", "value" => "");
        $query = knjp737Query::getCsvGrp($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $groupArray[] = array('label' => $row["LABEL"],
                                  'value' => $row["VALUE"]);
        }
        $result->free();

        //一覧表示
        if ($model->s_row_no > 0 && $model->s_row_no > 0) {
            for ($i = $model->s_row_no; $i < ($model->s_row_no + $model->row_no_cnt); $i++) {
                //列番号
                $setData["ROW_NO"] = $i;

                //ヘッダー名
                $setData["HEAD_NAME"] = $headNameArray[$i];

                //グループコンボ
                if (!isset($model->warning) && $model->cmd != "reflect") {
                    $model->field["GRP_CD_".$i] = $csvInfoArray[$i] ? $csvInfoArray[$i] : $groupArray[0]["value"];
                }
                $extra = "";
                $setData["GRP_CD"] = knjCreateCombo($objForm, "GRP_CD_".$i, $model->field["GRP_CD_".$i], $groupArray, $extra, 1);
                $arg["data"][] = $setData;
            }
        }
        //高さ調整
        $arg["HEIGHT"] = ($model->row_no_cnt > 10) ? "height:270;" : "";

        //更新ボタン
        $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT && $model->s_row_no > 0 && $model->row_no_cnt > 0) ? "onclick=\"return btn_submit('update');\"" : "disabled";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除ボタン
        $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT && $cnt > 0) ? "onclick=\"return btn_submit('delete');\"" : "disabled";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"return closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        //コピー
        $extra = "onclick=\"return btn_submit('copy')\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //Hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "ROW", $model->row_no_cnt);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjp737index.php", "", "main");
        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp737Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
