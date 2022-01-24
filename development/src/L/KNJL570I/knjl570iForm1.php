<?php
class knjl570iForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $extra = "onChange=\"return btn_submit('main')\"";
        $query = knjl570iQuery::selectYearQuery($model);
        makeCmb($objForm, $arg, $db, $query, "year", $model->entexamyear, $extra, 1, "");

        //学科コンボ
        $extra = "onChange=\"return btn_submit('main')\"";
        $arg["TOP"]["MAJORCD"] = knjCreateCombo($objForm, "MAJORCD", $model->majorcd, $model->majorcdList, $extra, 1);

        //入試区分コンボ
        $query = knjl570iQuery::getTestDivMst($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], "", 1, "");

        //コースコンボ
        $query = knjl570iQuery::getMajorCourcecd($model);
        makeCmb($objForm, $arg, $db, $query, "MAJOR_COURCECD", $model->field["MAJOR_COURCECD"], "", 1, "");

        //受験番号
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["TOP"]["RECEPTNO"] = knjCreateTextBox($objForm, $model->field["RECEPTNO"], "RECEPTNO", 4, 4, $extra);

        //試験科目
        $rowNum = 1;
        $query = knjl570iQuery::getSettingMst($model, "L009");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["TOP"]["TESTSUBCLASS".$rowNum] = $row["ABBV1"];
            $rowNum++;
            if ($rowNum > 5) {
                break;
            }
        }
        $result->free();

        //判定コースコンボ用のクエリ
        $query = knjl570iQuery::getJudgeCourceQuery($model);
        $judgeCourceOpt = getCmbOpt($db, $query, "BLANK");

        //特待コースコンボ用のクエリ
        $query = knjl570iQuery::getHonordivQuery($model);
        $honordivOpt = getCmbOpt($db, $query, "BLANK");

        //特待理由コースコンボ用のクエリ
        $query = knjl570iQuery::getHonorReasondivQuery($model);
        $honorReasondivOpt = getCmbOpt($db, $query, "BLANK");

        //一覧表示
        $year = "";
        $testdiv = "";
        $receptnoArray = array();
        if ($model->field["MAJORCD"] != "" && $model->field["TESTDIV"] != "" && $model->field["MAJOR_COURCECD"] != "" && ($model->cmd == "read" || $model->cmd == "getdef" || $model->cmd == "updread")) {
            //データ取得
            $defTokutaiFlg = $model->getDefTokutai == "1" ? true : false;
            $query = knjl570iQuery::selectQuery($model, $defTokutaiFlg);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setWarning("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $receptno = $row["RECEPTNO"];

                //HIDDENに保持する用
                $receptnoArray[] = $receptno;


                //判定マークコンボ
                if ($row["JUDGE_COURCE"] == '') {
                    $row["JUDGE_COURCE"] = $row["JUDGE_COURCE_KARI"];
                    $extra = 'style="background-color:#FFCCCC"';
                } else {
                    $extra = '';
                }
                $value = (!isset($model->warning)) ? $row["JUDGE_COURCE"] : $model->updField[$receptno]["JUDGE_COURCE"];
                $row["JUDGE_COURCE"] = knjCreateCombo($objForm, "JUDGE_COURCE-{$receptno}", $value, $judgeCourceOpt, $extra, 1);

                for ($i = 0; $i < get_count($judgeCourceOpt); $i++) {
                    if ($judgeCourceOpt[$i]['value'] == $row["JUDGE_COURCE_KARI"]) {
                        list($temp, $row["JUDGE_COURCE_KARI"]) = explode(':', $judgeCourceOpt[$i]['label']);
                    }
                }

                //特待コースコンボ
                $value = (!isset($model->warning)) ? $row["HONORDIV"] : $model->updField[$receptno]["HONORDIV"];
                $row["HONORDIV"] = knjCreateCombo($objForm, "HONORDIV-{$receptno}", $value, $honordivOpt, "", 1);

                //特待理由コースコンボ
                $value = (!isset($model->warning)) ? $row["HONOR_REASONDIV"] : $model->updField[$receptno]["HONOR_REASONDIV"];
                $row["HONOR_REASONDIV"] = knjCreateCombo($objForm, "HONOR_REASONDIV-{$receptno}", $value, $honorReasondivOpt, "", 1);

                $dataflg = true;

                $arg["data"][] = $row;
            }

            $year    = $model->field["YEAR"];    //検索時の入試年度を退避する
            $testdiv = $model->field["TESTDIV"]; //検索時の入試区分を退避する
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg);

        //hidden作成
        makeHidden($objForm, $model, $year, $testdiv, $receptnoArray);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl570iindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl570iForm1.html", $arg);
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
    //読込ボタン
    $extra  = "onclick=\"return btn_submit('read');\"";
    $arg["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);

    $disable  = ($dataflg) ? "" : " disabled";

    //初期値読込みボタン(GET_DEFTOKUTAIに値を入れてreadイベント実施)
    $extra = "onclick=\"return btn_submit('getdef');\"".$disable;
    $arg["btn_getdef"] = knjCreateBtn($objForm, "btn_getdef", "特待参照", $extra);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disable;
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $year, $testdiv, $receptnoArray)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_YEAR", $year);
    knjCreateHidden($objForm, "HID_TESTDIV", $testdiv);
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $receptnoArray));
    knjCreateHidden($objForm, "CHANGE_FLG");
    knjCreateHidden($objForm, "GET_DEFTOKUTAI");
}

//コンボ作成用の要素を取得
function getCmbOpt($db, $query, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    return $opt;
}
