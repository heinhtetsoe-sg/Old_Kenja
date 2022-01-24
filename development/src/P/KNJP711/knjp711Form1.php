<?php

require_once('for_php7.php');

class knjp711Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjp711Form1", "POST", "knjp711index.php", "", "knjp711Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度
        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ
        $query = knjp711Query::getNameMstA023($model);
        $extra = "onChange=\"btn_submit('knjp711')\";";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1, "");

        //入金グループコンボ
        $query = knjp711Query::getCollectGrp($model);
        $extra = "onChange=\"btn_submit('knjp711')\";";
        makeCmb($objForm, $arg, $db, $query, "COLLECT_GRP_CD", $model->field["COLLECT_GRP_CD"], $extra, 1, "ALL");

        //初期化
        if (!isset($model->warning)) {
            $model->data = array();
        }

        //一覧表示
        $arr_colcd = array();
        $dataflg = false;
        $query = knjp711Query::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $val = $row["COLLECT_L_CD"].$row["COLLECT_M_CD"];

            //HIDDENに保持する用
            $arr_colcd[] = $val;

            //入金項目表示
            $row["COLLECT_M_SHOW"] = $val.':'.$row["COLLECT_M_NAME"];

            //回数表示
            $row["MONTH_CNT_SHOW"] = "<span id=\"MONTH_CNT:{$val}\">".$row["MONTH_CNT"]."</span>";
            knjCreateHidden($objForm, "MONTH_CNT:".$val, $row["MONTH_CNT"]);

            //全月チェックボックス
            $extra  = ($model->data["ALL_MONTH"][$val] == "1") ? "checked" : "";
            $extra .= " id=\"ALL_MONTH:".$val."\" onClick=\"return check_all(this);\"";
            $row["ALL_MONTH"] = knjCreateCheckBox($objForm, "ALL_MONTH:".$val, "1", $extra, "");

            //各月チェックボックス
            for ($i = 1; $i <= 12; $i++) {
                $value = ($model->isWarning()) ? $model->data["COLLECT_MONTH_".$i][$val] : $row["COLLECT_MONTH_".$i];
                $extra  = ($value == "1") ? "checked" : "";
                $extra .= " id=\"COLLECT_MONTH_".$i.":".$val."\" onClick=\"return check_cnt(this);\"";
                $row["COLLECT_MONTH_".$i] = knjCreateCheckBox($objForm, "COLLECT_MONTH_".$i.":".$val, "1", $extra, "");
            }

            $dataflg = true;
            $arg["data"][] = $row;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "COLCD_LIST", implode(",",$arr_colcd));

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp711Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") $opt[] = array('label' => "", 'value' => "");
    if ($blank == "ALL")   $opt[] = array('label' => "0000:基本設定", 'value' => "0000");
    if ($query) {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $dataflg) {
    $disable  = ($dataflg) ? "" : " disabled";

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disable;
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"".$disable;
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
