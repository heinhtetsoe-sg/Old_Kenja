<?php

require_once('for_php7.php');

class knjl458hForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //入試年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //extra
        $change = " onchange=\"return btn_submit('main');\" tabindex=-1";
        $click = " onclick=\"return btn_submit('main');\"";

        //入試制度コンボ
        $query = knjl458hQuery::getNameMst($model->ObjYear, "L003");
        $extra = $change;
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入学コースコンボ
        $query = knjl458hQuery::getExamCourseMst($model);
        $extra = $change;
        makeCmb($objForm, $arg, $db, $query, "EXAMCOURSECD", $model->coursecd, $extra, 1, "ALL");

        //表示順ラジオボタン 1:かな氏名順 2:出身学校順 3:SEQ順
        $opt = array(1, 2, 3);
        if (!$model->sort) {
            $model->sort = 1;
        }
        $extra = array("id=\"SORT1\"".$click, "id=\"SORT2\"".$click, "id=\"SORT3\"".$click);
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //一覧表示
        $examnoArray = array();
        if ($model->applicantdiv != "" && $model->coursecd != "") {
            //データ取得
            $query = knjl458hQuery::selectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setWarning("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $examNo = $row["EXAMNO"];

                //HIDDENに保持する用
                $examnoArray[] = $examNo;

                //辞退フラグチェック
                $checked = ($row["ENTDIV"] == "2") ? " checked": "";
                $extra = "id=\"ENTDIV-{$examNo}\" onclick=\"changeFlg(this, '{$examNo}');\" ".$checked;
                $row["ENTDIV"] = knjCreateCheckBox($objForm, "ENTDIV-{$examNo}", "2", $extra);

                //辞退更新フラグ　changeFlg()で更新フラグ"1"を立てる
                knjCreateHidden($objForm, "UPD_FLG_".$examNo, "");

                $dataflg = true;

                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg);

        //hidden作成
        makeHidden($objForm, $model, $examnoArray);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl458hindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl458hForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "", $retOpt = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "-- 全て --", "value" => "ALL");
    }
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    if ($retOpt) {
        return $opt;
    } else {
        $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $dataflg)
{
    $disable  = ($dataflg) ? "" : " disabled";

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disable;
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"".$disable;
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $examnoArray)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_EXAMCOURSECD");
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",", $examnoArray));
    knjCreateHidden($objForm, "CHANGE_FLG");
}
