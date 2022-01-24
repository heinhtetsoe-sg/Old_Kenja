<?php

require_once('for_php7.php');

class knjc035bForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjc035bForm1", "POST", "knjc035bindex.php", "", "knjc035bForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        /**************/
        /*  実行削除  */
        /**************/
        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学年コンボ
        $query = knjc035bQuery::getGrade($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1);

        //削除月コンボ（学期・月）
        makeMonthSemeCmb($objForm, $arg, $db, $model);


        /**************/
        /*  履歴削除  */
        /**************/
        //ALLチェック
        $extra = "onClick=\"check_all(this);\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //月名称取得
        $month_name = array();
        $query = knjc035bQuery::selectMonthQuery($month, "name", $model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $month_name[$row["NAMECD2"]] = $row["NAME1"];
        }
        $result->free();

        //削除履歴一覧
        $counter = 0;
        $query = knjc035bQuery::getHistData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //削除チェックボックス
            $row["LABEL_ID"] = "CHECKED_".$row["EXECUTEDATE"]."_".$row["SEQ"];
            $extra = "id=\"{$row["LABEL_ID"]}\" onclick=\"OptionUse();\"";
            $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $row["EXECUTEDATE"]."_".$row["SEQ"], $extra, "1");

            $row["MONTHNAME"] = $month_name[$row["MONTH"]]."（".$row["SEMESTERNAME"]."）";
            $row["UPDATED"] = str_replace("-", "/", $row["UPDATED"]);

            $arg["list"][] = $row;
            $counter++;
        }
        $result->free();

        //サイズ調整用
        $arg["HEIGHT"] = ($counter > 7) ? "height:200;" : "";

        //実行削除ボタン
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実行削除", $extra);

        //履歴削除ボタン
        $extra = "disabled onclick=\"return btn_submit('histdel');\"";
        $arg["button"]["btn_histdel"] = knjCreateBtn($objForm, "btn_histdel", "履歴削除", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc035bForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//対削除月コンボ作成
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model) {
    $data = array();
    $query = knjc035bQuery::selectSemesAll();
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();

    $opt_month = setMonth($db, $data, $objForm, $model);
    $model->field["SEM_MONTH"] = ($model->field["SEM_MONTH"] != "") ? $model->field["SEM_MONTH"] : getDefaultMonthCd($opt_month);

    $extra = "";
    $arg["data"]["SEM_MONTH"] = knjCreateCombo($objForm, "SEM_MONTH", $model->field["SEM_MONTH"], $opt_month, $extra, 1);

    return;
}

//学期・月データ取得
function setMonth($db, $data, &$objForm, $model) {
    //月名称取得
    $getdata = array();
    $max_len = 0;
    $query = knjc035bQuery::selectMonthQuery($month, "combo", $model);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $getdata[$row["NAMECD2"]] = $row["NAME1"];

        //名称調整用
        $zenkaku = (strlen($row["NAME1"]) - mb_strlen($row["NAME1"])) / 2;
        $hankaku = ($zenkaku > 0) ? mb_strlen($row["NAME1"]) - $zenkaku : mb_strlen($row["NAME1"]);
        $max_len = ($zenkaku * 2 + $hankaku > $max_len) ? $zenkaku * 2 + $hankaku : $max_len;
    }
    $result->free();

    $cnt = 0;
    $opt_month = array();
    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = ($i > 12) ? $i - 12 : $i;
            $month = sprintf('%02d', $month);

            if (strlen($getdata[$month]) > 0) {
                //名称調整
                $zenkaku = (strlen($getdata[$month]) - mb_strlen($getdata[$month])) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($getdata[$month]) - $zenkaku : mb_strlen($getdata[$month]);
                $len = $zenkaku * 2 + $hankaku;
                $month_label = "";
                for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) $month_label .= "&nbsp;";
                $month_label .= $getdata[$month];

                $opt_month[] = array("label" => $month_label." (".$data[$dcnt]["SEMESTERNAME"].") ",
                                     "value" => $month."-".$data[$dcnt]["SEMESTER"]);
                knjCreateHidden($objForm, "LIST_MONTH" . $month."-".$data[$dcnt]["SEMESTER"], $cnt);
                $cnt++;
            }
        }
    }
    return $opt_month;
}

//学期・月の初期値を取得
function getDefaultMonthCd($opt_month) {
    $rtnMonthCd = $opt_month[0]["value"];
    $setFlg = true;
    for ($dcnt = 0; $dcnt < get_count($opt_month); $dcnt++) {
        $monthCd = preg_split("/-/", $opt_month[$dcnt]["value"]);
        $month    = (int) $monthCd[0];
        $semester = $monthCd[1];
        if ($month < 4) {
            $month += 12;
        }

        $ymd = preg_split("/-/", CTRL_DATE);
        $mm  = (int) $ymd[1];
        if ($mm < 4) {
            $mm += 12;
        }

        if ($semester == CTRL_SEMESTER && $setFlg) {
            $rtnMonthCd = $opt_month[$dcnt]["value"];
            if ($month >= $mm) {
                $setFlg = false;
            }
        }
    }
    return $rtnMonthCd;
}
?>
