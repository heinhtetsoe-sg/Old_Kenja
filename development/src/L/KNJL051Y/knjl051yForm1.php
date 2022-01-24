<?php

class knjl051yForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl051yQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $model->testdiv = ($model->applicantdiv == $model->appHold) ? $model->testdiv : "";
        $namecd1 = ($model->applicantdiv == "1") ? "L024" : "L004";
        $query = knjl051yQuery::getNameMst($namecd1, $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //会場コンボボックス
        $query = knjl051yQuery::getHallName($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMHALLCD", $model->examhallcd, $extra, 1, "BLANK");

        //一覧表示
        $arr_examno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->examhallcd != "")
        {
            if (!$model->isWarning()) $model->score = array();

            //データ取得
            $result = $db->query(knjl051yQuery::SelectQuery($model));

            if ($result->numRows() == 0 ){
                $model->setMessage("MSG303","\\n座席番号登録が行われていないか、志願者数確定処理が行われていません。");
            }

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];

                //面接備考テキストボックス
                $name  = "INTERVIEW_REMARK";
                $value = ($model->isWarning()) ? $model->score[$row["EXAMNO"]][$name] : $row[$name];
                $extra = "OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"width:100%\" ";
                $objForm->ae( array("type"        => "text",
                                    "name"        => $name,
                                    "extrahtml"   => $extra,
                                    "maxlength"   => "210",
                                    "multiple"    => "1",
                                    "value"       => $value));

                $row[$name] = $objForm->ge($name);

                //面接評価テキストボックス
                $name  = "INTERVIEW_VALUE";
                $value = ($model->isWarning()) ? $model->score[$row["EXAMNO"]][$name] : $row[$name];
                $extra = "OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"text-align:center;\" onblur=\"CheckScore(this);\"";
                $objForm->ae( array("type"        => "text",
                                    "name"        => $name,
                                    "extrahtml"   => $extra,
                                    "size"        => "2",
                                    "maxlength"   => "1",
                                    "multiple"    => "1",
                                    "value"       => $value));

                $row[$name] = $objForm->ge($name);

                //作文評価テキストボックス
                $name  = "COMPOSITION_VALUE";
                $value = ($model->isWarning()) ? $model->score[$row["EXAMNO"]][$name] : $row[$name];
                $extra = "OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"text-align:center;\" onblur=\"CheckScore(this);\"";
                $objForm->ae( array("type"        => "text",
                                    "name"        => $name,
                                    "extrahtml"   => $extra,
                                    "size"        => "2",
                                    "maxlength"   => "1",
                                    "multiple"    => "1",
                                    "value"       => $value));

                $row[$name] = $objForm->ge($name);

                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl051yindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl051yForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
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

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_examno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_examno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_EXAMHALLCD");

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
?>
