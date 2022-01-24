<?php

require_once('for_php7.php');

class knjl018vForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->examYear;

        //校種コンボボックス
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl018vQuery::getExamSchoolKind($model);
        makeCmb($objForm, $arg, $db, $query, "EXAM_SCHOOL_KIND", $model->examSchoolKind, $extra, 1);

        //試験IDコンボボックス
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl018vQuery::getExamID($model);
        makeCmb($objForm, $arg, $db, $query, "EXAM_ID", $model->examID, $extra, 1);

        //会場コンボボックス
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl018vQuery::getPlaceID($model);
        makeCmb($objForm, $arg, $db, $query, "PLACE_ID", $model->placeID, $extra, 1, "ALL");

        //出身学校コンボボックス
        $query = knjl018vQuery::getFinschoolCD($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "FINSCHOOLCD", $model->finschoolCD, $extra, 1, "ALL");

        //ソートラジオボタン（1:受験番号、2:かな）
        $opt = array(1, 2);
        $extra = array("id=\"ORDER1\" onchange=\"return btn_submit('main')\"", "id=\"ORDER2\" onchange=\"return btn_submit('main')\"");
        $radioArray = knjCreateRadio($objForm, "ORDER", $model->order, $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }

        //一覧表示
        $arr_receptno = array();
        $dataflg = false;
        if ($model->examSchoolKind != "" && $model->examID != "") {
            //データ取得
            $query = knjl018vQuery::selectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setMessage("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持するための処理
                $arr_receptno[] = $row["RECEPTNO"]."-".$row["ABSENCE_FLG"];

                //欠席checkbox
                $checked1 = ($row["ABSENCE_FLG"] == "1") ? " checked" : (in_array($row["RECEPTNO"], $model->selectedAbsenceFlg)) ? " checked" : "";
                $extra = "id=\"{$row["RECEPTNO"]}\" ".$checked1;
                $row["ABSENCE_FLG"] = knjCreateCheckBox($objForm, "ABSENCE_FLG_".$row["RECEPTNO"], "1", $extra);

                $dataflg = true;

                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        $disable  = ($dataflg) ? "" : " disabled";

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"".$disable;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"".$disable;
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //ＣＳＶ処理ボタン
        $extra = "onClick=\"wopen('".REQUESTROOT."/X/KNJX_L018V/knjx_l018vindex.php?EXAM_YEAR=".$model->examYear."&EXAM_SCHOOL_KIND=".$model->examSchoolKind."&EXAM_ID=".$model->examID."&PLACE_ID=".$model->placeID."&,0,0,screen.availWidth,screen.availHeight');\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV処理", $extra);
        //終了ボタン
        $extra = "onclick=\"return btn_submit('end');\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL018V");
        knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $arr_receptno));

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl018vindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl018vForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    } elseif ($blank == "ALL") {
        $opt[] = array("label" => "全て", "value" => "ALL");
    }
    $value_flg = ($blank == "ALL") ? true : false;
    $default = 0;
    $i = ($blank) ? 1 : 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if (($name == 'EXAM_SCHOOL_KIND') && ($row["NAMESPARE2"] == '1') && $default_flg && $value != "ALL") {
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
