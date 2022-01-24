<?php
class knjl117iform1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl117iindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["YEAR"] = $model->examyear;

        //入試制度コンボボックス
        $extra = "onChange=\"return btn_submit('change')\"";
        $query = knjl117iQuery::getNameMst($model->examyear, "L003", $model->applicantdiv);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = " onchange=\"return btn_submit('change')\" ";
        $query = knjl117iQuery::getTestDiv($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //評価区分コンボ
        $extra = " onchange=\"return btn_submit('change')\" ";
        $query = knjl117iQuery::getPointCd($model);
        makeCmb($objForm, $arg, $db, $query, "POINTCD", $model->pointcd, $extra, 1);

        //評価段階数
        if ($model->cmd == "" || $model->cmd == "main" || $model->cmd == "change" || $model->cmd == "clear") {
            $levelCnt = $db->getOne(knjl117iQuery::selectMainQuery($model));
            $model->maxLevel = $levelCnt;
        }
        if ($model->maxLevel == "") {
            $model->maxLevel = 0;
        }
        $extra = "style=\"text-align:right\" onblur=\"level(this, '{$model->maxLevel}');\"";
        $arg["MAX_POINTLEVEL"] = knjCreateTextBox($objForm, $model->maxLevel, "MAX_POINTLEVEL", 2, 2, $extra);
        knjCreateHidden($objForm, "HID_MAX_POINTLEVEL", $model->maxLevel); //エラーになった場合に入力前の値に戻すためのhidden

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $query = knjl117iQuery::selectMainQuery($model);
            $result = $db->query($query);
            
            $mainData = array();
            if ($model->cmd != "kakutei") { //※確定後は指定の段階数分の入力欄を未入力状態で出すのでデータは空
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $mainData[$row["POINTLEVEL"]] = $row;
                }
            }
        } else {
            $mainData =& $model->dataField;
            $result = "";
        }

        //確定後は指定の段階数分の入力欄を未入力状態で出す
        if ($model->cmd == "kakutei") {
            $mainData = array();
        }
        //設定段階値=5 固定
        $model->maxLevel = ($model->maxLevel != "") ? $model->maxLevel : 0;
        for ($i = $model->maxLevel; $i > 0 ; $i--) {
            //+ポイント
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $mainData[$i]["PLUS_POINT"] = knjCreateTextBox($objForm, $mainData[$i]["PLUS_POINT"], "PLUS_POINT".$i, 5, 2, $extra);
            //-ポイント
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $mainData[$i]["MINUS_POINT"] = knjCreateTextBox($objForm, $mainData[$i]["MINUS_POINT"], "MINUS_POINT".$i, 5, 2, $extra);
            //下限
            $extra = "style=\"text-align:right\" onblur=\"inputRange(this, '{$i}');\"";
            $mainData[$i]["POINTLOW"] = knjCreateTextBox($objForm, $mainData[$i]["POINTLOW"], "POINTLOW".$i, 5, 2, $extra);
            //上限
            $mainData[$i]["POINTHIGH_ID"] = "POINTHIGH_{$i}";
            knjCreateHidden($objForm, "HID_POINTHIGH".$i); //更新ボタン押し時に値を格納

            $arg["data"][] = $mainData[$i];
        }

        //入力値範囲設定
        $rangeFrom = 0;
        $rangeTo = 999;
        if ($model->pointcd == "1") {
            $rangeTo = 45;
        } else if ($model->pointcd == "2") {
            $rangeTo = 90;
        } else if ($model->pointcd == "3") {
            $rangeTo = 15;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "rangeFrom", $rangeFrom);
        knjCreateHidden($objForm, "rangeTo", $rangeTo);
        knjCreateHidden($objForm, "notKakuteiFlg", "0"); //評定段階数を変更するとフラグが立つ、段階数を確定する前に更新を防ぐために使用

        //DB切断
        Query::dbCheckIn($db);
        
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl117iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, &$model) {
    //コピーボタンを作成する
    $extra = "onclick=\"return btn_submit('copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度コピー", $extra);
    //確定ボタンを作成する
    $extra = "onclick=\"return btn_submit('kakutei');\"";
    $arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確定", $extra);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

?>
