<?php

require_once('for_php7.php');

class knjd181jForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd181jForm1", "POST", "knjd181jindex.php", "", "knjd181jForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;
        $z010name1 = $db->getOne(knjd181jQuery::getZ010());

        //学期コンボ作成
        $query = knjd181jQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd181j'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //クラスコンボ作成
        $opt = array();
        $arr_trcd = array();
        $value_flg = false;
        $query = knjd181jQuery::getAuth($model->field["SEMESTER"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
            $arr_trcd[$row["VALUE"]] = $row["TR_CD1"];
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $result->free();
        $model->field["GRADE_HR_CLASS"] = ($model->field["GRADE_HR_CLASS"] && $value_flg) ? $model->field["GRADE_HR_CLASS"] : $opt[0]["value"];

        $extra = "onchange=\"return btn_submit('knjd181j'),AllClearList();\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $opt, $extra, 1);

        //異動対象日付初期値セット
        if ($model->field["DATE"] == "") {
            $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        }
        //異動対象日付（テキスト）
        $disabled = "";
        $extra = "onblur=\"isDate(this); tmp_list('knjd181j', 'on')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["DATE"], "DATE", 12, 12, $extra);
        //異動対象日付（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('knjd181j', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //異動対象日付
        $arg["data"]["DATE"] = View::setIframeJs().$date_textbox.$date_button;

        //記載日付
        if ($model->field["DESC_DATE"] == "") {
            $model->field["DESC_DATE"] = str_replace("-", "/", CTRL_DATE);
        }
        $arg["data"]["DESC_DATE"] = View::popUpCalendar($objForm, "DESC_DATE", $model->field["DESC_DATE"]);

        //対象外の生徒取得
        $query = knjd181jQuery::getSchnoIdou(CTRL_YEAR, $model->field["SEMESTER"], $model->field["GRADE_HR_CLASS"], str_replace("/", "-", $model->field["DATE"]));
        $result = $db->query($query);
        $opt_idou = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_idou[] = $row["SCHREGNO"];
        }
        $result->free();

        //対象者リストを作成する
        $query = knjd181jQuery::getSchno(CTRL_YEAR, $model->field["SEMESTER"], $model->field["GRADE_HR_CLASS"]);
        $result = $db->query($query);
        $opt_right = $opt_left = array();
        $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $idou = (in_array($row["SCHREGNO"], $opt_idou)) ? "●" : "　";
            if (in_array($row["SCHREGNO"], $selectdata)) {
                $opt_left[] = array('label' => $row["SCHREGNO"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                    'value' => $row["SCHREGNO"]);
            } else {
                $opt_right[] = array('label' => $row["SCHREGNO"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                     'value' => $row["SCHREGNO"]);
            }
        }
        $result->free();

        $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);

        //生徒一覧リストを作成する
        $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //印刷面チェックボックス
        $name = array("PRINT_SIDE4", "PRINT_SIDE1", "PRINT_SIDE2", "PRINT_SIDE3");
        foreach ($name as $key => $val) {
            $extra  = ($model->field[$val] == "1" || $model->cmd == "") ? "checked" : "";
            $extra .= " onclick=\"kubun();\" id=\"$val\"";
            $arg["data"][$val] = knjCreateCheckBox($objForm, $val, "1", $extra, "");
        }
        

        //「出欠の記録は出力しない」チェックボックス
        $extra  = ($model->field["NOT_PRINT_ATTEND"] == "1") ? "checked" : "";
        $extra .= " id=\"NOT_PRINT_ATTEND\"";
        $arg["data"]["NOT_PRINT_ATTEND"] = knjCreateCheckBox($objForm, "NOT_PRINT_ATTEND", "1", $extra, "");

        if ($z010name1 != 'miyagiken') {
            $arg["ouinran"] = '1';
            //「押印欄を出力する」チェックボックス
            $extra  = ($model->field["OUINRAN_SHUTURYOKU"] == "1") ? "checked" : "";
            $extra .= " id=\"OUINRAN_SHUTURYOKU\"";
            $arg["data"]["OUINRAN_SHUTURYOKU"] = knjCreateCheckBox($objForm, "OUINRAN_SHUTURYOKU", "1", $extra, "");
        }
        if ($z010name1 == 'kyoto') {
            $arg["iskyoto"] = '1';
            //「教育目標印字なし」チェックボックス
            $extra  = ($model->field["NOTOUTPUT_EDUTARGET"] == "1") ? "checked" : "";
            $extra .= " id=\"NOTOUTPUT_EDUTARGET\"";
            $arg["data"]["NOTOUTPUT_EDUTARGET"] = knjCreateCheckBox($objForm, "NOTOUTPUT_EDUTARGET", "1", $extra, "");
        }

        if ($model->maxSemester == '3') {
            $arg["shoken"] = '1';
            //「所見を表示しない」チェックボックス
            $extra  = ($model->field["SHOKEN_SHUTURYOKU"] == "1") ? "checked" : "";
            $extra .= " id=\"SHOKEN_SHUTURYOKU\"";
            $arg["data"]["SHOKEN_SHUTURYOKU"] = knjCreateCheckBox($objForm, "SHOKEN_SHUTURYOKU", "1", $extra, "");
        }

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "PRGID", "KNJD181J");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "TR_CD1", $arr_trcd[$model->field["GRADE_HR_CLASS"]]);
        knjCreateHidden($objForm, "IMAGEPATH", $model->control["LargePhotoPath"]);
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "PRINT_SIDE", "1");//TODO:後でカットする
        knjCreateHidden($objForm, "CHECKS", $model->field["CHECKS"]);
        knjCreateHidden($objForm, "HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J", $model->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]);
        knjCreateHidden($objForm, "tutisyoSougouHyoukaTunen", $model->Properties["tutisyoSougouHyoukaTunen"]);
        knjCreateHidden($objForm, "reportSpecialSize01_01", $model->Properties["reportSpecialSize01_01"]);
        knjCreateHidden($objForm, "reportSpecialSize01_02", $model->Properties["reportSpecialSize01_02"]);
        knjCreateHidden($objForm, "reportSpecialSize02_01", $model->Properties["reportSpecialSize02_01"]);
        knjCreateHidden($objForm, "reportSpecialSize03_01", $model->Properties["reportSpecialSize03_01"]);
        knjCreateHidden($objForm, "reportSpecialSize04_01", $model->Properties["reportSpecialSize04_01"]);
        knjCreateHidden($objForm, "reportSpecialSize04_01Title", $model->Properties["reportSpecialSize04_01Title"]);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "useFormNameD181J_3", $model->Properties["useFormNameD181J_3"]);
        knjCreateHidden($objForm, "documentMstSize_B4", $model->Properties["documentMstSize_B4"]);
        knjCreateHidden($objForm, "tutisyoShokenntunen", $model->Properties["tutisyoShokenntunen"]);
        knjCreateHidden($objForm, "tutisyoShokennSemesterTitle", $model->Properties["tutisyoShokennSemesterTitle"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd181jForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
