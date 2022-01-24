<?php

require_once('for_php7.php');

class knjl031eForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = $model->entexamyear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl031eQuery::getNameMst($model, 'L003');
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl031eQuery::getNameMst($model, 'L004');
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "");

        //会場区分ラジオボタン 1:面接 2:試験
        $opt = array(1, 2);
        if (!$model->examhall_type) $model->examhall_type = 1;
        $click = " onclick=\"return btn_submit('main');\"";
        $extra = array("id=\"EXAMHALL_TYPE1\"".$click, "id=\"EXAMHALL_TYPE2\"".$click);
        $radioArray = knjCreateRadio($objForm, "EXAMHALL_TYPE", $model->examhall_type, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //表示切替
        $arg["LABEL"] = ($model->examhall_type == "1") ? '面接' : '試験';

        //ALLチェック
        $extra = "id=\"CHECKALL\" onclick=\"check_all(this);\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //会場データ取得
        $examhallArray = array();
        $examhallArray[] = array('label' => '', 'value' => '');
        $result = $db->query(knjl031eQuery::getEntexamHallYdat($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $examhallArray[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        }
        $result->free();

        //一覧表示
        $arr_ExamNo = array();
        $dataFlg = false;
        if ($model->applicantdiv != "" && $model->testdiv != "") {
            //データ取得
            $query = knjl031eQuery::selectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0 ) {
                $model->setMessage("MSG303");
            }

            $counter = 0;

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                if ($model->testdiv == '21' || $model->testdiv == '22') {
                    if ($row["HEIGAN"]) {
                        $row["DESIREDIV"] .= "({$row["HEIGAN"]})";
                    }
                }

                //HIDDENに保持する用
                $arr_ExamNo[] = $row["EXAMNO"];
                knjCreateHidden($objForm, "EXAMNO-".$row["EXAMNO"], $row["EXAMNO"]);

                //対象者チェックボックス
                $extra = " id=\"CHECKED-{$row["EXAMNO"]}\" class=\"changeColor\" data-name=\"CHECKED-{$row["EXAMNO"]}\"";
                $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $row["EXAMNO"], $extra, "1");
                $row["CHECKED_NAME"] = "CHECKED-{$row["EXAMNO"]}";

                //会場コンボ
                $value = $row["EXAMHALLCD"];
                $value_flg = false;
                foreach ($examhallArray as $key => $val) {
                    if ($value === $val['value']) $value_flg = true;
                }
                $value = (strlen($value) && $value_flg) ? $value : $examhallArray[0]['value'];
                $row["EXAMHALLCD_SHOW"] = knjCreateCombo($objForm, "EXAMHALLCD-".$row["EXAMNO"], $value, $examhallArray, "", 1);

                //グループテキスト
                $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value)\" onkeydown=\"keydownEnter(this, '1')\" onPaste=\"return showPaste(this, ".$counter.");\"";
                $value = $row["EXAMHALLGROUPCD"];
                $row["EXAMHALLGROUPCD_SHOW"] = knjCreateTextBox($objForm, $value, "EXAMHALLGROUPCD-".$row["EXAMNO"], 3, 3, $extra);

                //順番テキスト
                $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value)\" onkeydown=\"keydownEnter(this, '2')\" onPaste=\"return showPaste(this, ".$counter.");\"";
                $value = $row["EXAMHALLGROUP_ORDER"];
                $row["EXAMHALLGROUP_ORDER_SHOW"] = knjCreateTextBox($objForm, $value, "EXAMHALLGROUP_ORDER-".$row["EXAMNO"], 2, 3, $extra);

                $arg["data"][] = $row;
                $dataFlg = true;
                $counter++;
            }
            $result->free();
        }

        //面接会場コンボボックス（一括設定用）
        $extra = "";
        $query = knjl031eQuery::getEntexamHallYdat($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMHALLCD", $model->examhallcd, $extra, 1, "blank");

        /****************/
        /*  ボタン作成  */
        /****************/

        //ボタン制御（データがないとき使用不可）
        $disabled = ($dataFlg) ? "" : " disabled";

        //一括設定
        //$extra = "onclick=\"return btn_submit('replace');\"".$disabled;
        $extra = "onclick=\"setCheckedExamhall(this);\"".$disabled;
        $arg["btn_replace"] = knjCreateBtn($objForm, "btn_replace", "一括設定", $extra);
        //更新
        $extra = "onclick=\"return btn_submit('update');\"".$disabled;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"".$disabled;
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /****************/
        /*  hidden作成  */
        /****************/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL031E");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->entexamyear);
        knjCreateHidden($objForm, "HID_EXAMNO", implode(",", $arr_ExamNo));

        //データ保持用
        knjCreateHidden($objForm, "HIDDEN_APPLICANTDIV", $model->applicantdiv);
        knjCreateHidden($objForm, "HIDDEN_TESTDIV", $model->testdiv);
        knjCreateHidden($objForm, "HIDDEN_EXAMHALL_TYPE", $model->examhall_type);
        knjCreateHidden($objForm, "HIDDEN_EXAMHALLCD");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl031eindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl031eForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array('label' => '', 'value' => '');
    $value_flg = false;
    $default = 0;
    $i = ($blank) ? 1 : 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();

    $value = (strlen($value) && $value_flg) ? $value : $opt[$default]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
