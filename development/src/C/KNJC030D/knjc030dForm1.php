<?php

require_once('for_php7.php');

class knjc030dForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjc030dindex.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //日付
        $model->field["DATE"] = $model->field["DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $extra = "btn_submit('main')";
        $arg["DATE"] = View::popUpCalendar2($objForm, "DATE", $model->field["DATE"], "reload=true", $extra);

        //日付チェック＆学期取得
        $seme = "";
        $semeCol = $db->getCol(knjc030dQuery::getSemester($model->field["DATE"]));
        $semeRow = $db->getRow(knjc030dQuery::getSemester($model->field["DATE"], '9'), DB_FETCHMODE_ASSOC);
        if(str_replace("-", "/", ATTEND_CTRL_DATE) > $model->field["DATE"]) {
            $arg["jscript"] = "alert('日付が出欠制御日付より前です。')";
        } else if(get_count($semeCol) > 1) {
            $seme = $semeCol[0];
        } else if (get_count($semeCol) == 1) {
            $arg["jscript"] = "alert('日付が学期の範囲外です。')";
        } else {
            $arg["jscript"] = "alert('日付が年度の範囲外です。')";
        }

        if ($model->Properties["useFi_Hrclass"] == "1" || $model->Properties["useSpecial_Support_Hrclass"] == "1") {
            //クラス方式選択 (1:法定クラス 2:複式クラス/実クラス)
            $opt = array(1, 2);
            if ($model->field["HR_CLASS_TYPE"] == "") $model->field["HR_CLASS_TYPE"] = "1";
            $click = " onClick=\"return btn_submit('main');\"";
            $extra = array("id=\"HR_CLASS_TYPE1\"".$click, "id=\"HR_CLASS_TYPE2\"".$click);
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
            $arg["data"]["HR_CLASS_TYPE2_LABEL"] = ($model->Properties["useFi_Hrclass"] == "1") ? '複式クラス' : '実クラス';
            $arg["useFi_Hrclass"] = 1;
        } else {
            if ($model->field["HR_CLASS_TYPE"] == "") $model->field["HR_CLASS_TYPE"] = "1";
            knjCreateHidden($objForm, "HR_CLASS_TYPE", "1");
        }

        //学級コンボ
        $query = knjc030dQuery::getHrClass($model, $seme);
        $extra = "onchange=\"return btn_submit('main')\";";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "blank");

        $query = knjc030dQuery::getCloseMsg($model, $seme);
        $result = $db->query($query);
        $closeMsg = "";
        $sep = "";
        while ($closeMsgRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $closeMsg .= $sep.$closeMsgRow["NAME"];
            $sep = "、";
        }
        $result->free();

        //リンク先のURL
        $jump = REQUESTROOT."/C/KNJC030D_2/knjc030d_2index.php";

        //詳細画面に渡すパラメータ
        $prgid  = "KNJC030D";
        $auth   = AUTHORITY;

        $param  = "?prgid={$prgid}";
        $param .= "&auth={$auth}";
        $param .= "&HR_CLASS_TYPE={$model->field["HR_CLASS_TYPE"]}";
        $param .= "&GRADE_HR_CLASS={$model->field["GRADE_HR_CLASS"]}";
        $param .= "&DATE={$model->field["DATE"]}";
        $param .= "&SEMESTER={$seme}";

        //出欠項目名表示
        $SUSPEND = $MOURNING = false;
        foreach ($model->attendItem as $key => $val) {

            //詳細入力画面に渡すパラメータ（項目名）
            $param .= "&TITLE={$key}";

            //詳細出欠項目がある場合はリンクにする
            $sub_exist_check = "0";
            $extra = "style=\"font-color:white;\" onClick=\"openSubWindow('{$jump}{$param}');\"";
            $sub_exist_check = get_count($db->getCol(knjc030dQuery::getNameMst('C006', $val[0])));
            if ($sub_exist_check > "0" && $model->field["GRADE_HR_CLASS"] && $key != "NONOTICE") {
                $arg[$key] = View::alink("#", "<font color=\"hotpink\">".$val[1]."</font>", $extra);
            } else {
                $arg[$key] = $val[1];
            }

            if (in_array($key, array("SUSPEND", "MOURNING"))) $$key = true;
        }

        //出停・忌引の表示切替
        if ($SUSPEND && $MOURNING) {
            $arg["SUS_MOUR"] = 1;
            $arg["NOT_SUS_MOUR"] = "";
        } else {
            $arg["SUS_MOUR"] = "";
            $arg["NOT_SUS_MOUR"] = 1;
        }

        //欠席の詳細出欠項目名（大分類）を表示
        $subl_title = "";
        $subl_cnt = "0";
        $result = $db->query(knjc030dQuery::getNameMst('C006','6'));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //詳細入力画面に渡すパラメータ（項目名）
            $param .= "&TITLE=CNT{$row["VALUE"]}";

            //詳細出欠項目がある場合はリンクにする
            $sub_exist_check = "0";
            $extra = "style=\"font-color:white;\" onClick=\"openSubWindow('{$jump}{$param}');\"";
            $sub_exist_check = get_count($db->getCol(knjc030dQuery::getNameMst('C007', $row["VALUE"])));
            if ($sub_exist_check > "0" && $model->field["GRADE_HR_CLASS"] && $model->field["DATE"]) {
                $label = View::alink("#", "<font color=\"hotpink\">".$row["LABEL"]."</font>", $extra);
                $subl_title .= "<td rowspan=\"2\" width=\"35\"><font size=\"1\">".$label."</font></td>";
            } else {
                $subl_title .= "<td rowspan=\"2\" width=\"35\"><font size=\"1\">".$row["LABEL"]."</font></td>";
            }
            $subl_cnt++;
        }
        $result->free();

        //事故欠（無）があるとき、大分類を表示する
        $sickdiv = array();
        $c001Namecd1 = ($model->schoolkind) ? "C".$model->schoolkind."01" : "C001";
        $sickdiv = $db->getCol(knjc030dQuery::getSickDiv($c001Namecd1));
        if(in_array('6', $sickdiv)) {
            $arg["subl_title"] = $subl_title;
            $arg["subl_title_cnt"] = $subl_cnt;
            $arg["subl_title_width"] = $subl_cnt * 35;
        }

        //編集対象データリスト
        makeDataList($objForm, $arg, $db, $model, $seme);

        //ボタン作成
        makeButton($objForm, $arg, $db, $model, $closeMsg);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "useSpecial_Support_Hrclass", $model->Properties["useSpecial_Support_Hrclass"]);
        knjCreateHidden($objForm, "useFi_Hrclass", $model->Properties["useFi_Hrclass"]);

        knjCreateHidden($objForm, "HIDDEN_DATE");
        knjCreateHidden($objForm, "HIDDEN_HR_CLASS_TYPE");
        knjCreateHidden($objForm, "HIDDEN_GRADE_HR_CLASS");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc030dForm1.html", $arg);
    }
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model, $seme)
{
    $subl_data = array();
    $subl_data = $db->getCol(knjc030dQuery::getNameMst('C006', "6"));

    $query      = knjc030dQuery::selectAttendQuery($model, $seme, $subl_data);
    $result     = $db->query($query);

    $counter  = 0;
    $colorFlg = false;
    $data = array();
    $schCnt = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //異動者（退学・転学・卒業）
        $idou = $db->getOne(knjc030dQuery::getIdouData($row["SCHREGNO"], str_replace("/", "-", $model->field["DATE"])));
        $row["BGCOLOR_IDOU"] = ($idou > 0) ? "bgcolor=yellow" : "";

        $total = "";
        if($row["TOTAL"] > "0" ) $total = "1";

        //大分類・中分類データチェック
        $att_check_cnt = 0;
        $subl_data_cnt = $db->getOne(knjc030dQuery::checkAttendDaySublDat($model, $row["SCHREGNO"], "", "flg"));
        $subm_data_cnt = $db->getOne(knjc030dQuery::checkAttendDaySubmDat($model, $row["SCHREGNO"], "", "flg"));
        $att_check_cnt = $subl_data_cnt + $subm_data_cnt;

        //出欠データ編集
        $att_data = "";
        $check_fin = array();
        foreach ($model->attendItem as $key => $val) {
            $check_fin[] = $row[$key];

            //大分類・中分類の存在チェック
            $sub_exist_check = 0;
            if(substr($key,0,3) == 'CNT'){
                $sub_exist_check = get_count($db->getCol(knjc030dQuery::getNameMst('C007', $val[0])));
            } else {
                $sub_exist_check = get_count($db->getCol(knjc030dQuery::getNameMst('C006', $val[0])));
            }

            if($sub_exist_check > "0") {
                $row[$key] = ($row[$key] == "1") ? 'レ' : '';
                $att_data .= "<td {$row["BGCOLOR_IDOU"]} width=\"35\">".$row[$key]."</td>";
                continue;
            } else {
                $check_name = "SELECTDATA_{$row["SCHREGNO"]}_{$key}"; //チェックボックスの名前
                //チェックボックス
                $extra  = ($row[$key] == "1") ? "checked" : "";
                $extra .= " onclick=\"return check_change(this, {$att_check_cnt})\";";
                $checkbox = knjCreateCheckBox($objForm, $check_name, "1", $extra, "");
                $att_data .= "<td {$row["BGCOLOR_IDOU"]} width=\"35\">".$checkbox."</td>";
            }
        }
        $schCnt++;

        //未入力の背景色設定
        $chkFin = $db->getOne(knjc030dQuery::checkAttendDayDat($model, $row["SCHREGNO"]));
        if ($chkFin == 0) {
            $row["BGCOLOR_NAME_SHOW"] = "bgcolor=#ccffcc";
        }

        $row["att_data"] = $att_data;

        //備考テキスト
        $extra = "STYLE=\"WIDTH:95%\" WIDTH=\"95%\" onChange=\"this.style.background='#ccffcc'\" ";
        $row["REMARK"] = knjCreateTextBox($objForm, $row["REMARK"], "REMARK_{$row["SCHREGNO"]}", 40, 20, $extra);

        //hidden(学籍番号)
        $row["SCHREGNO"] = "<input type=\"hidden\" name=\"SCHREGNO[]\" value=\"".$row["SCHREGNO"]."\">";

        //5行毎に色を変える
        if ($counter % 5 == 0) {
            $colorFlg = !$colorFlg;
        }
        $row["BGCOLOR_ROW"] = $colorFlg ? "#ffffff" : "#cccccc";
        $counter++;

        $data[] = $row;
    }
    $arg["attend_data"] = $data;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="")
{
    $opt = array();
    $value_flg = false;
    if($blank) $opt[] = array('label' => "", 'value' => "");
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
function makeButton(&$objForm, &$arg, $db, $model, $closeMsg)
{
    if(AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) {
        if ($model->field["HR_CLASS_TYPE"] != "2") {
            //ATTEND_DAY_HRATE_DAT存在チェック
            $grade = substr($model->field["GRADE_HR_CLASS"], 0, 2);
            $hr_class = substr($model->field["GRADE_HR_CLASS"], 2);
            $hrate = $db->getRow(knjc030dQuery::checkAttendDayHrateDat($grade, $hr_class, $model), DB_FETCHMODE_ASSOC);

            $label = ($hrate["EXECUTED"] == "1") ? "出欠完了取消" : "出欠入力完了";
            $cmd = ($hrate["EXECUTED"] == "1") ? 'cancelHrAte' : 'updateHrAte';
            //出欠入力完了ボタン
            $arg["btn_updateHrAte"] = knjCreateBtn($objForm, "btn_updateHrAte", $label, " onclick=\"return btn_submit('{$cmd}');\"");
        }
        //保存ボタン
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", " onclick=\"return btn_submit('update');\"");
        //取消ボタン
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"");
    }
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    if ($closeMsg) {
        $extra = "onclick=\"closeMsgFunc('{$closeMsg}');\"";
    }
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
