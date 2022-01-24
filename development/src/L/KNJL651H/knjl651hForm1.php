<?php
class knjl651hForm1
{

    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //入試年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試日程
        $query = knjl651hQuery::getTestdivMst($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //開始受験番号
        $extra = "";
        $arg["TOP"]["S_RECEPTNO"] = knjCreateTextBox($objForm, $model->s_receptno, "S_RECEPTNO", 4, 4, $extra);

        //終了受験番号
        $extra = "";
        $arg["TOP"]["E_RECEPTNO"] = knjCreateTextBox($objForm, $model->e_receptno, "E_RECEPTNO", 4, 4, $extra);

        //面接評価comboboxのリストを取得
        $query = knjl651hQuery::getInterview($model->ObjYear, $model->applicantdiv); // 高校用を取得
        $interviewOpt = makeCmb($objForm, $arg, $db, $query, "INTERVIEW", $model->interview, "", 1, "BLANK", "", "1");
        
        //面接評価comboboxのデフォルト値を取得
        $query = knjl651hQuery::getInterview($model->ObjYear, $model->applicantdiv, "", "1");
        $rowInterview = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $interviewDefaultCd = $rowInterview["VALUE"];

        //一覧表示
        $receptnoArray = array();
        if ($model->testdiv != "") {
            //データ取得
            $query = knjl651hQuery::selectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setWarning("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $receptNo = $row["RECEPTNO"];

                //HIDDENに保持する用
                $receptnoArray[] = $receptNo;

                //試験欠席「JUDGEMENT = 3」の時、得点欄は入力不可とする
                //但し、この生徒は「ATTEND_FLG = 1」で更新する
                $disabled = ($row["JUDGEMENT"] == "3") ? " disabled" : "";
                knjCreateHidden($objForm, "JUDGEMENT-{$receptNo}", $row["JUDGEMENT"]);
                //EXAMNO
                knjCreateHidden($objForm, "EXAMNO-{$receptNo}", $row["EXAMNO"]);

                //面接評価comboboxのデフォルト値をセットする。面接評価のDB値がNULLの場合はcomboboxを赤色で表示
                $interviewA = $row["INTERVIEW_A"];
                $styleInterviewA = "";
                if ($row["INTERVIEW_A"] == "" && $row["JUDGEMENT"] != "3" && $row["ATTEND_FLG"] != "1") {
                    $interviewA = $interviewDefaultCd;
                    if ($interviewDefaultCd != "") {
                        $styleInterviewA = " style=\"color:red;\"";
                    }
                }

                //面接評価combobox
                $extra = "id=\"INTERVIEW_A-{$receptNo}\" onchange=\"changeFlg(this);\"".$disabled.$styleInterviewA;
                $row["INTERVIEW_A"] = knjCreateCombo($objForm, "INTERVIEW_A-{$receptNo}", $interviewA, $interviewOpt, $extra, 1);

                //面接欠席checkbox
                $checked = ($row["ATTEND_FLG"] == "1") ? " checked": "";
                $extra = "id=\"ATTEND_FLG-{$receptNo}\" onclick=\"changeFlg(this);\" ".$checked.$disabled;
                $row["ATTEND_FLG"] = knjCreateCheckBox($objForm, "ATTEND_FLG-{$receptNo}", "1", $extra);

                $dataflg = true;

                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg);

        //CSVフォーム部品作成
        makeCsvForm($objForm, $arg, $model, $dataflg);

        //hidden作成
        makeHidden($objForm, $model, $receptnoArray);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl651hindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl651hForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "", $all = "", $retDiv = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($all) {
        $opt[] = array("label" => "全て", "value" => "ALL");
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

    if ($retDiv == "") {
        $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        return $opt;
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $dataflg)
{
    $disable  = ($dataflg) ? "" : " disabled";

    //読込ボタン
    $extra  = "onclick=\"return btn_submit('read');\"";
    $arg["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);

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
function makeHidden(&$objForm, $model, $receptnoArray)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $receptnoArray));
    knjCreateHidden($objForm, "CHANGE_FLG");
}

//CSVフォーム部品作成
function makeCsvForm(&$objForm, &$arg, $model, $dataflg)
{
    $disable  = ($dataflg) ? "" : " disabled";

    //ファイル
    $extra = "".$disable;
    $arg["csv"]["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

    //実行
    $extra = "onclick=\"return btn_submit('exec');\"".$disable;
    $arg["csv"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

    //ヘッダ有チェックボックス
    if ($model->field["HEADER"] == "on") {
        $check_header = " checked";
    } else {
        $check_header = ($model->cmd == "main") ? " checked" : "";
    }
    $extra = "id=\"HEADER\"".$check_header;
    $arg["csv"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

    //CSV取込書出種別ラジオボタン 1:取込 2:書出
    $opt_shubetsu = array(1, 2);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
    $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["csv"][$key] = $val;
    }
}
?>
