<?php

require_once('for_php7.php');

class knjd173bForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd173bForm1", "POST", "knjd173bindex.php", "", "knjd173bForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //学期の設定
        $gakki = $model->field["TEST_ITEM"] ? substr($model->field["TEST_ITEM"],0,1) : CTRL_SEMESTER;
        if ($gakki == '9') {//学期が9の時は今学期の生徒を対象とする
            $gakki = CTRL_SEMESTER;
        }
        $model->field["TEST_ITEM"] = $model->field["TEST_ITEM"] ? $model->field["TEST_ITEM"] : CTRL_SEMESTER . "-01-01";
        //異動対象日付の設定
        $model->field["TAISYOBI"]  = $model->field["TAISYOBI"]  ? $model->field["TAISYOBI"]  : str_replace("-","/",CTRL_DATE);


        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->control["年度"];

        /**************/
        /* コンボ作成 */
        /**************/
        //試験名コンボ
        $extra = "onchange=\"return btn_submit('gakki'),AllClearList();\"";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW") {
            $opt = array();
            $query = knjd173bQuery::getTestName2();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $testName[$row["SEMESTER"]] = $row["TESTITEMNAME"];
            }

            $query = knjd173bQuery::getSemeName();
            $result = $db->query($query);
            $semeName = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $semeName[$row["SEMESTER"]] = $row["SEMESTERNAME"];
            }
            $opt[] = array("label" => $semeName[1] . $testName[1],
                           "value" => "1-01-01");
            $opt[] = array("label" => $semeName[1] . "末",
                           "value" => "1-99-00");
            $opt[] = array("label" => $semeName[2] . $testName[2],
                           "value" => "2-01-01");
            $opt[] = array("label" => $semeName[9],
                           "value" => "9-99-00");
            $arg["data"]["TEST_ITEM"] = knjCreateCombo($objForm, "TEST_ITEM", $model->field["TEST_ITEM"], $opt, $extra, 1);
        } else {
            $opt = array();
            $query = knjd173bQuery::getTestName();
            $testName = $db->getOne($query);
            $query = knjd173bQuery::getSemeName();
            $result = $db->query($query);
            $semeName = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $semeName[$row["SEMESTER"]] = $row["SEMESTERNAME"];
            }
            $opt[] = array("label" => $semeName[1] . $testName,
                           "value" => "1-01-01");
            $opt[] = array("label" => $semeName[1] . "末",
                           "value" => "1-99-00");
            $opt[] = array("label" => $semeName[2] . $testName,
                           "value" => "2-01-01");
            $opt[] = array("label" => $semeName[9],
                           "value" => "9-99-00");
            $arg["data"]["TEST_ITEM"] = knjCreateCombo($objForm, "TEST_ITEM", $model->field["TEST_ITEM"], $opt, $extra, 1);
        }

        //クラス選択コンボ
        $extra = "onchange=\"return btn_submit('knjd173b'),AllClearList();\"";
        $query = knjd173bQuery::getAuth($model->control["年度"],$gakki);
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1);

        /* List to List */

        //対象外の生徒取得
        $query = knjd173bQuery::getSchnoIdou($model,$gakki);
        $result = $db->query($query);
        $opt_idou = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_idou[] = $row["SCHREGNO"];
        }
        $result->free();

        //対象者リストを作成する
        $query = knjd173bQuery::getcategoryName($model, $gakki);
        $result = $db->query($query);
        $opt_right = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $idou = (in_array($row["SCHREGNO"], $opt_idou)) ? "●" : "　";
            if (in_array($row["SCHREGNO"], $model->select_data["selectdata"])) {
                $opt_left[] = array('label' => $row["SCHREGNO"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                    'value' => $row["SCHREGNO"]);
            } else {
                $opt_right[] = array('label' => $row["SCHREGNO"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                     'value' => $row["SCHREGNO"]);
            }
        }
        $result->free();

        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

        //出力対象一覧リストを作成する
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);


        /******************/
        /* カレンダー作成 */
        /******************/
        //異動対象日付初期値セット
        if ($model->field["TAISYOBI"] == "") $model->field["TAISYOBI"] = str_replace("-", "/", CTRL_DATE);
        //異動対象日付（テキスト）
        $disabled = "";
        $extra = "onblur=\"isDate(this); tmp_list('knjd173b')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["TAISYOBI"], "TAISYOBI", 12, 12, $extra);
        //異動対象日付（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list(''); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=TAISYOBI&frame='+getFrameName(self) + '&date=' + document.forms[0]['TAISYOBI'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //異動対象日付
        $arg["data"]["TAISYOBI"] = View::setIframeJs().$date_textbox.$date_button;


        /**************/
        /* ボタン作成 */
        /**************/
        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, 'btn_rights', '>>', $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, 'btn_lefts', '<<', $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, 'btn_right1', '＞', $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, 'btn_left1', '＜', $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('".SERVLET_URL."');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, 'btn_print', 'プレビュー／印刷', $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        /**************/
        /* hidden作成 */
        /**************/
        knjCreateHidden($objForm, "YEAR",  $model->control["年度"]);
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJD173B");
        knjCreateHidden($objForm, "TABLE_NAME", $model->testTable);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "OUTPUT2", "on");//認定単位・委員会・部活動の表記なし (チェックボックスだった値)
        knjCreateHidden($objForm, "OUTPUT3", "on");//遅刻回数・早退回数の表記なし       (チェックボックスだった値)
        knjCreateHidden($objForm, "OUTPUT4", "on");//「総合的な学習の時間」所見印刷     (チェックボックスだった値)
        knjCreateHidden($objForm, "OUTPUT5", "on");//「奉仕」所見印刷                   (チェックボックスだった値)
        knjCreateHidden($objForm, "OUTPUT6", "on");//「通信欄」所見印刷                 (チェックボックスだった値)
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd173bForm1.html", $arg); 
    }
}
/****************************************** 以下関数 ***********************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    if ($query) {
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        if ($name == "SEMESTER") {
            $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
        } else {
            $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        }
        $result->free();
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
