<?php

require_once('for_php7.php');

class knjl433hForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl433hindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl433hQuery::getNameMst($model->year, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //切替
        $arg["schoolKindJ"] = ($model->applicantdiv == "1") ? "1" : "";
        $arg["schoolKindH"] = ($model->applicantdiv == "2") ? "1" : "";

        //入試回数コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl433hQuery::getSettingMst($model, "L004");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //受験型コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl433hQuery::getExamType($model);
        makeCmb($objForm, $arg, $db, $query, "EXAM_TYPE", $model->examtype, $extra, 1);

        //受験コースコンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl433hQuery::getEntExamCourse($model);
        makeCmb($objForm, $arg, $db, $query, "TOTALCD", $model->totalcd, $extra, 1, "BLANK");

        //試験科目コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl433hQuery::getExamclass($model, "L009");
        makeCmb($objForm, $arg, $db, $query, "EXAM_CLASS", $model->examclass, $extra, 1);

        //満点マスタより点数上限を取得
        $perfect = $db->getOne(knjl433hQuery::getPerfectAtClass($model));

        //ボタン有効フラグ
        $isEnableButtons = false;

        //一覧表示
        $arr_receptno = array();
        if (strlen($model->examclass) && ($model->cmd == "read" || $model->cmd == "reset")) {
            //データ取得
            $query = knjl433hQuery::selectQuery($model);
            $result = $db->query($query);

            //データなし
            if ($result->numRows() == 0) {
                $model->setMessage("MSG303");
            }

            //データ表示
            $count = 0;
            $tabs = array();
            $tabs[] = "EXAM_CLASS";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"].'-'.$row["EXAMNO"];

                if ($row["ROUNIN_FLG"]) {
                    $row["ROUNIN_FLG"] = "style=\"color:red\"";
                }

                //欠席者は、入力不可
                $disInput = ($row["JUDGEDIV"] == "4") ? " disabled" : "";
                knjCreateHidden($objForm, "JUDGEDIV"."-".$row["RECEPTNO"], $row["JUDGEDIV"]);

                //得点
                $extra = " onblur=\"validateScore(this)\" onkeydown=\"keyChangeEntToTab(this)\" max='{$perfect}'";
                $row["SCORE"] = knjCreateTextBox($objForm, $row["SCORE"], "SCORE"."-".$row["RECEPTNO"], 3, 3, $extra.$disInput);

                $arg["data"][] = $row;
                $tabs[] = "SCORE"."-".$row["RECEPTNO"];
                $count++;
            }

            $isEnableButtons = (0 < $count) ;

            //各得点テキストボックス要素のNameおよびBack Tab／Front Tab向けの受験科目コンボボックスおよびCSV取込ラジオボタンそれぞれのNameをタブインデックス順に配置したものをHIDDEN項目に持たせる。
            $tabs[] = "CSV_IO";

            //以下HIDDENは得点テキストボックス内でEnterキーを押下した際のフォーカス移動処理（JS）および得点入力後の更新ボタン押下時の得点項目識別で使用
            knjCreateHidden($objForm, "TABSFEILDS", implode(",", $tabs));
        }

        //レコード件数
        knjCreateHidden($objForm, "COUNT", $count);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $isEnableButtons);

        //CSVフォーム部品作成
        makeCsvForm($objForm, $arg, $model, $isEnableButtons);

        //hidden作成
        makeHidden($objForm, $model, $arr_receptno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl433hForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
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

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $dataflg)
{
    $disable  = ($dataflg) ? "" : " disabled";

    //読込ボタン
    $extra  = "onclick=\"return btn_submit('read');\"";
    $arg["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);

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

//CSVフォーム部品作成
function makeCsvForm(&$objForm, &$arg, $model, $dataflg)
{
    $disable  = ($dataflg) ? "" : " disabled";

    //ファイル
    $extra = "accept='.csv'".$disable;
    $arg["csv"]["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //実行
    $extra = "onclick=\"return btn_submit('exec');\"".$disable;
    $arg["csv"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

    //ヘッダ有チェックボックス
    if ($model->field["HEADER"] == "on") {
        $check_header = " checked";
    } else {
        $check_header = ($model->cmd == "main") ? " checked" : "";
    }
    $extra = "id=\"HEADER\"".$check_header.$disable;
    $arg["csv"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

    //CSV取込書出種別ラジオボタン 1:取込 2:書出
    $opt_shubetsu = array(1, 2);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
    $extra = array("id=\"CSV_IMPORT\"".$disable, "id=\"CSV_EXPORT\"".$disable);
    $radioArray = knjCreateRadio($objForm, "CSV_IO", $model->csvInOutMode, $extra, $opt_shubetsu, get_count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["csv"][$key] = $val;
    }
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_receptno)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $arr_receptno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV0");
    knjCreateHidden($objForm, "HID_TOTALCD");
    knjCreateHidden($objForm, "HID_EXAM_TYPE");
    knjCreateHidden($objForm, "HID_EXAM_CLASS");
    knjCreateHidden($objForm, "HID_CSV_IO");
    knjCreateHidden($objForm, "HID_HEADER");

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL433H");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
}
