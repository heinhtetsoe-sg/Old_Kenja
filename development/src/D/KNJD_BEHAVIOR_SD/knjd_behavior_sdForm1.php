<?php

require_once('for_php7.php');
class knjd_behavior_sdForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("form1", "POST", "knjd_behavior_sdindex.php", "", "form1");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $schInfo = $db->getRow(knjd_behavior_sdQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $arg["NAME_SHOW"] = $model->schregno."　".$schInfo["NAME"];

        // Add by HPA for title start 2020/02/03
          $arg["TITLE"] = $arg["NAME_SHOW"]."の行動の記録情報画面";
          echo "<script>var title= '".$arg["TITLE"]."';
              </script>";
        // Add by HPA for title end 2020/02/20

        //学期コンボ
        $query = knjd_behavior_sdQuery::getSemester($model);
        $model->field["SEMESTER"] = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : $model->exp_semester;
        /* Edit by HPA for PC-talker 読み start 2020/02/03 */
        $extra = "id = \"edit\" aria-label = \"学期\" onChange=\"current_cursor('edit');return btn_submit('edit')\"";
        /* Edit by HPA for PC-talker 読み end 2020/02/20 */
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //記録の取得
        $Row = $row = array();
        $result = $db->query(knjd_behavior_sdQuery::getBehavior($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $Row["RECORD"][$row["CODE"]] = $row["RECORD"];
        }
        $result->free();

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if (isset($model->warning)) {
            $Row =& $model->record;
        }

        //出力項目取得
        if ($model->getPro["knjdBehaviorsd_DispViewName"] != "1") {
            $query = knjd_behavior_sdQuery::getNameMst($model, "D035");
            $codeField  = "NAMECD2";
            $nameField  = "NAME1";
        } else {
            $query = knjd_behavior_sdQuery::getBehaviorSemesMst($model);
            $codeField  = "CODE";
            $nameField  = "VIEWNAME";
        }
        $result = $db->query($query);
        $model->itemArray = array();
        while ($setItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->itemArray[$setItem[$codeField]] = $setItem;
        }

        if ($model->getPro["knjdBehaviorsd_UseText"] == "1") {
            //出力項目取得
            $query = knjd_behavior_sdQuery::getNameMst($model, "D036");
            $result = $db->query($query);
            $setCheckVal = "";
            $checkSep = "/";
            $setTextTitle = "(";
            $sep = "";
            $model->textValue = array();
            while ($setItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $setCheckVal .= $checkSep.$setItem["NAME1"];
                $model->textValue[$setItem["NAMECD1"]] = $setItem;
                $setTextTitle .= $sep.$setItem["NAME1"].":".$setItem["NAME2"];
                $sep = "　";
                $checkSep = "|";
                $dataArray[] = array("VAL"  => "\"javascript:setClickValue('".$setItem["NAME1"]."')\"",
                                     "NAME" => $setItem["NAME1"].":".$setItem["NAME2"]);
            }
            $setCheckVal .= "/";
            $setTextTitle .= ")";
            $arg["TEXT_TITLE"] = $setTextTitle;
            knjCreateHidden($objForm, "CHECK_VAL", $setCheckVal);
            knjCreateHidden($objForm, "CHECK_ERR_MSG", $setTextTitle);

            //ドロップダウンリスト
            $arg["menuTitle"]["CLICK_NAME"] = knjCreateBtn($objForm, "btn_end", "×", "onclick=\"return setClickValue('999');\"");
            $arg["menuTitle"]["CLICK_VAL"] = "javascript:setClickValue('999')";
            if (is_array($dataArray)) {
                foreach ($dataArray as $key => $val) {
                    $setData["CLICK_NAME"] = $val["NAME"];
                    $setData["CLICK_VAL"] = $val["VAL"];
                    $arg["menu"][] = $setData;
                }
            }

        }

        if (is_array($model->itemArray)) {
            foreach ($model->itemArray as $key => $val) {
                $setData = array();
                if ($model->getPro["knjdBehaviorsd_UseText"] == "1") {
                    $extra = "STYLE=\"text-align: center\"; onblur=\"calc(this);\" oncontextmenu=\"kirikae2(this, '".$key."')\";";
                    $setData["RECORD_VAL"] = knjCreateTextBox($objForm, $Row["RECORD"][$key], "RECORD".$key, 3, 1, $extra);
                    $setData["RECORD_NAME"] = $val[$nameField];
                    $setData["RECORD_LABEL"] = "RECORD".$key;
                    $arg["data"][] = $setData;
                } else {
                    $check1 = ($Row["RECORD"][$key] == "1") ? "checked" : "";
                    $extra = $check1." id=\"RECORD".$key."\"";
                    $setData["RECORD_VAL"] = knjCreateCheckBox($objForm, "RECORD".$key, "1", $extra, "");
                    $setData["RECORD_NAME"] = $val[$nameField];
                    $setData["RECORD_LABEL"] = "RECORD".$key;
                    $arg["data"][] = $setData;
                }
            }
        }

        //更新ボタン
        /* Edit by HPA for PC-talker 読み and current_cursor start 2020/02/03 */
        $extra = "aria-label = \"更新\" id = \"update\" onclick=\"current_cursor('update');return btn_submit('update')\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
        /* Edit by HPA for PC-talker 読み and current_cursor end 2020/02/20 */

        //取消ボタン
        /* Edit by HPA for PC-talker 読み and current_cursor start 2020/02/03 */
        $extra = "aria-label = \"取消\" id = \"clear\" onclick=\"current_cursor('clear');return btn_submit('clear')\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        /* Edit by HPA for PC-talker 読み and current_cursor end 2020/02/20 */

        //戻るボタン
        /* Edit by HPA for PC-talker 読み and current_cursor start 2020/02/03 */
        $extra = "aria-label = \"戻る\" onclick=\"return top.main_frame.right_frame.closeit()\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);
        /* Edit by HPA for PC-talker 読み and current_cursor end 2020/02/20 */

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //DB切断
        Query::dbCheckIn($db);

        //画面のリロード
        if ($model->cmd == "updEdit2") {
            $arg["reload"] = "parent.parent.left_frame.btn_submit('list');";
            $arg["reload"] .= " updateFrameUnLock(parent.frames);";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd_behavior_sdForm1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>