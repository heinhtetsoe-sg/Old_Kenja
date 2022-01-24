<?php

require_once('for_php7.php');

class knjl025qForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl025qQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        if (SCHOOLKIND == "J") {
            $query = knjl025qQuery::getNameMst($model->ObjYear, "L024");
        } else {
            $query = knjl025qQuery::getNameMst($model->ObjYear, "L004");
        }
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //表示順ラジオボタン 1:中学校順 2:番号順
        $opt = array(1, 2);
        $model->sort = ($model->sort == "") ? "1" : $model->sort;
        $extra = array();
        foreach($opt as $key => $val) array_push($extra, " id=\"SORT{$val}\" onClick=\"btn_submit('main')\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //表示内容ラジオボタン 1:すべて 2:特定中学
        $opt = array(1, 2);
        $model->show = ($model->show == "") ? "1" : $model->show;
        $extra = array();
        foreach($opt as $key => $val) array_push($extra, " id=\"SHOW{$val}\" onClick=\"btn_submit('main')\"");
        $radioArray = knjCreateRadio($objForm, "SHOW", $model->show, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //中学校コンボボックス　表示内容(特定中学)選択で表示
        if ($model->show == "2") {
            $extra = "onchange=\"return btn_submit('main');\"";
            $query = knjl025qQuery::getFinSchoolMst($model);
            makeCmb($objForm, $arg, $db, $query, "FS_CD", $model->fs_cd, $extra, 1);
        }

        //エンター押下時の移動対象一覧
        $setField = array();
        $setField[] = "SCORE1";
        $setField[] = "SCORE2";
        $setField[] = "KAKUYAKU_FLG";
        knjCreateHidden($objForm, "setField", implode(',', $setField));

        //一覧表示
        $arr_examno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {

            //データ取得
            $query = knjl025qQuery::SelectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0 ) {
                //$model->setMessage("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];

                //公的試験１
                $extra = "onchange=\"bgcolorYellow(this, '{$row["EXAMNO"]}');\" onblur=\"this.value=toInteger(this.value);\" onKeyDown=\"keyChangeEntToTab(this);\"";
                $row["SCORE1"] = knjCreateTextBox($objForm, $row["SCORE1"], "SCORE1"."-".$row["EXAMNO"], 3, 3, $extra);

                //公的試験２
                $extra = "onchange=\"bgcolorYellow(this, '{$row["EXAMNO"]}');\" onblur=\"this.value=toInteger(this.value);\" onKeyDown=\"keyChangeEntToTab(this);\"";
                $row["SCORE2"] = knjCreateTextBox($objForm, $row["SCORE2"], "SCORE2"."-".$row["EXAMNO"], 3, 3, $extra);

                //確約
                $extra = "onclick=\"bgcolorYellow(this, '{$row["EXAMNO"]}');\" onKeyDown=\"keyChangeEntToTab(this);\"";
                $chk = ($row["KAKUYAKU_FLG"] == "1") ? " checked" : "";
                $row["KAKUYAKU_FLG"] = knjCreateCheckBox($objForm, "KAKUYAKU_FLG"."-".$row["EXAMNO"], "1", $extra.$chk);

                $arg["data"][] = $row;
            }
        }

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"return btn_submit('end');\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_examno));
        knjCreateHidden($objForm, "HID_APPLICANTDIV");
        knjCreateHidden($objForm, "HID_TESTDIV");
        knjCreateHidden($objForm, "HID_SORT");
        knjCreateHidden($objForm, "HID_SHOW");
        knjCreateHidden($objForm, "HID_FS_CD");

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL025Q");
        knjCreateHidden($objForm, "YEAR", $model->ObjYear);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl025qindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl025qForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
