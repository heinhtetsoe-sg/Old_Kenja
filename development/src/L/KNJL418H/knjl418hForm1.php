<?php
class knjl418hForm1
{

    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //入試年度
        $arg["TOP"]["YEAR"] = $model->examYear;

        //入試制度コンボ
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl418hQuery::getNameMst($model->examYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //受験コースコンボ
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl418hQuery::getExamCourseMst($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMCOURSECD", $model->examcoursecd, $extra, 1);

        //最終特別奨学生区分comboboxのリストを取得
        $query = knjl418hQuery::getSettingMst($model, "L102");
        $dummy = "";
        $kubunOpt = makeCmb($objForm, $arg, $db, $query, "KUBUN", $dummy, "", 1, "BLANK", "", "1");

        $listHantei = array();
        $listHantei[] = array(
            "LABEL" => "全額",
            "VALUE" => "1"
        );
        $listHantei[] = array(
            "LABEL" => "半額",
            "VALUE" => "2"
        );

        //入試回数取得
        $result = $db->query(knjl418hQuery::getSettingMst($model, "L004"));
        $keyKai = 0;
        $headerKai = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $headerKai[$keyKai]["TITLE"] = $row["NAME1"];
            $headerKai[$keyKai]["SEQ"] = $row["VALUE"];
            $keyKai++;
        }
        $result->free();

        $arg["headerKai"] = $headerKai;

        //一覧表示
        $receptnoArray = array();
        if ($model->examcoursecd != "") {
            //データ取得
            $query = knjl418hQuery::selectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setWarning("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $examno = $row["EXAMNO"];

                //HIDDENに保持する用
                $examnoArray[] = $examno;

                $keyKai = 0;
                $cmbKai = array();
                foreach ($headerKai as $val) {
                    //各回判定combobox
                    $extra  = "id=\"HANTEI_{$val['SEQ']}-{$examno}\" disabled";
                    $extra .= "";
                    $cmbKai[$keyKai]["FORM"] = makeCmbList($objForm, $arg, $listHantei, "HANTEI_{$val['SEQ']}-{$examno}", $row["HANTEI_{$val['SEQ']}"], $extra, 1, "BLANK");
                    $keyKai++;
                }
                $row["cmbKai"] = $cmbKai;

                //最終特別奨学生判定combobox
                $extra = "id=\"HANTEI_LAST-{$examno}\"";
                $row["HANTEI_LAST"] = makeCmbList($objForm, $arg, $listHantei, "HANTEI_LAST-{$examno}", $row["HANTEI_LAST"], $extra, 1, "BLANK");

                //最終特別奨学生区分combobox
                $extra = "id=\"KUBUN-{$examno}\" onchange=\"changeFlg(this);\"";
                $row["KUBUN"] = knjCreateCombo($objForm, "KUBUN-{$examno}", $row["KUBUN"], $kubunOpt, $extra, 1);

                $dataflg = true;

                $arg["data"][] = $row;
            }
            $result->free();
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg);

        //CSVフォーム部品作成
        makeCsvForm($objForm, $arg, $model, $dataflg);

        //hidden作成
        makeHidden($objForm, $model, $examnoArray);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl418hindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl418hForm1.html", $arg);
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

//コンボ作成
function makeCmbList(&$objForm, &$arg, $orgOpt, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;

    foreach ($orgOpt as $row) {
        $opt[] = array("label" => $row["VALUE"].":".$row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
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
function makeHidden(&$objForm, $model, $examnoArray)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_TESTDIV");
    $hidExamno = "";
    if (!empty($examnoArray)) {
        $hidExamno = implode(",", $examnoArray);
    }
    knjCreateHidden($objForm, "HID_EXAMNO", $hidExamno);
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
