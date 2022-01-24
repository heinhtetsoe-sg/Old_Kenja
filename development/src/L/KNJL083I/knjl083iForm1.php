<?php
class knjl083iForm1
{

    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->examYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl083iQuery::getNameMst($model->examYear, 'L003');
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl083iQuery::getTestdivMst($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //班選択コンボボックス
        $query = knjl083iQuery::getEntexamHallYdat($model);
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $model->examHallCd = $model->examHallCd == '' ? 'ALL' : $model->examHallCd;
        makeCmb($objForm, $arg, $db, $query, "EXAMHALLCD", $model->examHallCd, $extra, 1, "ALL");

        //班選択コンボボックス
        $extra = "id=\"TEST_ABSENCE_ALL\" onchange=\"SetflgAll(this);\" ";
        $arg["TEST_ABSENCE_ALL"] = knjCreateCheckBox($objForm, "TEST_ABSENCE_ALL", "1", $extra);

        //学校名称変更
        if ($model->applicantdiv == "3") {
            $arg["FINSCHOOL_LABEL"] = "園名";
        } elseif ($model->applicantdiv == "1") {
            $arg["FINSCHOOL_LABEL"] = "小学校名";
        } else {
            $arg["FINSCHOOL_LABEL"] = "中学校名";
        }

        //一覧表示
        $arr_examno = array();
        $dataflg = false;
        if ($model->applicantdiv != "" && $model->testdiv != "") {
            //データ取得
            $query = knjl083iQuery::SelectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setMessage("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持するための処理
                $arr_examno[] = $row["EXAMNO"];

                //欠席checkbox
                $checked1 = ($row["JUDGEMENT"] == "4") ? " checked": "";
                $extra = "id=\"{$row["EXAMNO"]}\" class=\"test-absence-elem\" onchange=\"Setflg(this);\" ".$checked1;
                $row["TEST_ABSENCE"] = knjCreateCheckBox($objForm, "TEST_ABSENCE-".$row["EXAMNO"], "4", $extra);

                //欠席更新フラグ　Setflg()で更新フラグ"1"を立てる
                knjCreateHidden($objForm, "UPD_FLG_".$row["EXAMNO"], "");

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
        //終了ボタン
        $extra = "onclick=\"return btn_submit('end');\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL083I");
        knjCreateHidden($objForm, "HID_APPLICANTDIV", $model->applicantdiv);
        knjCreateHidden($objForm, "HID_TESTDIV");
        knjCreateHidden($objForm, "HID_EXAMHALLCD");
        knjCreateHidden($objForm, "HID_EXAMNO", implode(",", $arr_examno));

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl083iindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl083iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
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

        if ($row["NAMESPARE2"] && $default_flg && $value != "ALL") {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    if ($blank == "ALL") {
        $opt[] = array("label" => "全て", "value" => "ALL");
    }

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
