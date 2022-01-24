<?php

require_once('for_php7.php');

class knjc030d_2Form1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjc030d_2index.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //日付
        $arg["DATE"] = $model->date;

        //学級
        $query = knjc030d_2Query::getHrName($model);
        $arg["HR_CLASS"] = $db->getOne($query);

        //リンク先のURL
        $jump = REQUESTROOT."/C/KNJC030D_2/knjc030d_2index.php";

        //リンク元のプログラムＩＤ＆権限
        $prgid  = "KNJC030D_2";
        $auth   = $model->auth;

        //出欠タイトル
        $attendItem = array("ABSENT"        => array("1","公欠"),
                            "SUSPEND"       => array("2","出停"),
                            "MOURNING"      => array("3","忌引"));

        //欠席は名称マスタより取得
        $setFieldName = array("4" => "SICK", "5" => "NOTICE", "6" => "NONOTICE");
        foreach ($setFieldName as $key => $val) {
            $result = $db->query(knjc030d_2Query::getSickDiv());
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if($key == $row["VALUE"]) {
                    $attendItem[$val] = array($row["VALUE"], $row["LABEL"]);
                }
            }
            $result->free();
        }

        //ヘッダのタイトルをセット
        if(substr($model->title,0,3) != "CNT") {    //出欠種別名
            foreach ($attendItem as $key => $val) {
                if($key == $model->title){
                    $title = $val[1];
                    $col_cnt = get_count($db->getCol(knjc030d_2Query::getNameMst('C006', $val[0])));
                    $arg["TITLE1"] = "<td colspan={$col_cnt}>".$title."</td>";
                    $namespare1 = $val[0];
                    knjCreateHidden($objForm, "DI_CD", $namespare1);
                    knjCreateHidden($objForm, "DI_NAME", $key);
                }
            }
        } else {    //大分類
            $title = "";
            $namespare1 = substr($model->title,3);
            $result = $db->query(knjc030d_2Query::getNameMst2('C006', $namespare1));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

                $col_cnt = get_count($db->getCol(knjc030d_2Query::getNameMst('C007', $row["VALUE"])));
                $p_namespare1 = $db->getOne(knjc030d_2Query::getNameMst3('C006', $row["VALUE"]));

                foreach ($attendItem as $key => $val) {
                    if($val[0] == $p_namespare1) {
                        $arg["TITLE1"] = "<td colspan={$col_cnt}>".$val[1]."</td>";
                        knjCreateHidden($objForm, "DI_CD", $p_namespare1);
                        knjCreateHidden($objForm, "DI_NAME", $key);
                    }
                }
                $arg["TITLE2"] = "<td colspan={$col_cnt}>".$row["LABEL"]."</td>";
            }
            $result->free();
        }

        //大分類or中分類タイトル
        $sub_cnt = "1";
        $sub_title = "";
        $namecd1 = (substr($model->title,0,3) == 'CNT') ? 'C007' : 'C006';
        $result = $db->query(knjc030d_2Query::getNameMst($namecd1, $namespare1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            if($namecd1 == 'C006'){
                $param  = "?prgid={$prgid}";
                $param .= "&auth={$auth}";
                $param .= "&HR_CLASS_TYPE={$model->hr_class_type}";
                $param .= "&GRADE_HR_CLASS={$model->grade_hr_class}";
                $param .= "&DATE={$model->date}";
                $param .= "&SEMESTER={$model->semester}";
                $param .= "&TITLE=CNT{$row["VALUE"]}";

                $cnt_check = "0";
                $extra = "style=\"font-color:white;\" onClick=\"openSubWindow('{$jump}{$param}');\"";
                $cnt_check = get_count($db->getCol(knjc030d_2Query::getNameMst('C007', $row["VALUE"])));
                if ($cnt_check > "0" && $model->grade_hr_class && $model->date) {
                    $label = View::alink("#", "<font color=\"hotpink\">".$row["LABEL"]."</font>", $extra);
                } else {
                    $label = $row["LABEL"];
                }
            } else {
                $label = $row["LABEL"];
            }

            $sub_width = ($col_cnt == $sub_cnt) ? "#" : (round(600 / $col_cnt));
            $sub_title .= "<td width={$sub_width}>".$label."</td>";

            $sub_cnt++;
        }
        $result->free();

        $arg["SUB_TITLE"] = $sub_title;

        //編集対象データリスト
        makeDataList($objForm, $arg, $db, $model, $objUp, $header, $hrName, $attendItem, $namespare1, $p_namespare1, $col_cnt);

        //ボタン作成
        makeButton($objForm, $arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc030d_2Form1.html", $arg);
    }
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model, &$objUp, $headerData, $hrName, $attendItem, $namespare1, $p_namespare1, $col_cnt)
{
    //登録されている大分類・中分類を抽出
    $sub_data = array();
    $namecd1 = (substr($model->title,0,3) == 'CNT') ? 'C007' : 'C006';
    $sub_data = $db->getCol(knjc030d_2Query::getNameMst($namecd1, $namespare1));

    //メインデータ
    $query      = knjc030d_2Query::selectAttendQuery($model, $sub_data, $namespare1);
    $result     = $db->query($query);

    $counter  = 0;
    $colorFlg = false;
    $data = array();
    $schCnt = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //出欠データ
        if (isset($model->warning)) {
            if (is_array($model->reset[$row["SCHREGNO"]])) {
                foreach ($model->reset[$row["SCHREGNO"]] as $key => $val) {
                    $row[$key] = $val;
                }
            }
        }

        //チェック
        if(substr($model->title,0,3) != 'CNT') {
            $checked_data = $db->getOne(knjc030d_2Query::checkAttendDaySublDat($model, $row["SCHREGNO"], $namespare1, ""));
            $low_data = $db->getOne(knjc030d_2Query::checkAttendDaySubmDat($model, $row["SCHREGNO"], $namespare1, "", ""));
        } else {
            $checked_data = $db->getOne(knjc030d_2Query::checkAttendDaySubmDat($model, $row["SCHREGNO"], $p_namespare1, $namespare1, ""));
            $low_data = "0";
        }

        //未入力の背景色設定
        $chkFin = $db->getOne(knjc030d_2Query::checkAttendDayDat($model, $row["SCHREGNO"], "All"));
        if ($chkFin == "0") {
            $row["BGCOLOR_NAME_SHOW"] = "bgcolor=#ccffcc";
        }

        foreach ($sub_data as $sublKey) {
            $setArray["CNT".$sublKey] = array("SIZE" => 2, "MAXLEN" => 3);
        }

        //異動者（退学・転学・卒業）
        $idou = $db->getOne(knjc030d_2Query::getIdouData($row["SCHREGNO"], str_replace("/", "-", $model->date)));
        $row["BGCOLOR_IDOU"] = ($idou > 0) ? "bgcolor=yellow" : "";

        $sub_text = "";
        $sub_cnt = "1";
        foreach ($setArray as $key => $val) {
            $cnt_check = 0;
            if(substr($model->title,0,3) != 'CNT') {
                foreach ($sub_data as $sublKey) {

                    if("CNT".$sublKey == $key) {
                        $cnt_check = get_count($db->getCol(knjc030d_2Query::getNameMst('C007', $sublKey)));
                    }
                }
            }

            if($cnt_check > "0") {
                $sub_width = ($col_cnt == $sub_cnt) ? "#" : (round(600 / $col_cnt));
                $row[$key] = ($row[$key] == "1") ? 'レ' : "";
                $sub_text .= "<td {$row["BGCOLOR_IDOU"]} width={$sub_width}>".$row[$key]."</td>";
                $sub_cnt++;
                continue;
            } else {
                //checkbox
                $check_name = "SUBDATA_{$row["SCHREGNO"]}_{$key}"; //チェックボックスの名前
                $extra  = ($row[$key] == "1") ? "checked" : "";
                $extra .= " onclick=\"return check_change(this, {$chkFin}, {$checked_data}, {$low_data})\";";
                $row[$key] = knjCreateCheckBox($objForm, $check_name, "1", $extra, "");
                $sub_width = ($col_cnt == $sub_cnt) ? "#" : (round(600 / $col_cnt));
                $sub_text .= "<td {$row["BGCOLOR_IDOU"]} width={$sub_width}>".$row[$key]."</td>";
                $sub_cnt++;
            }
        }
        $schCnt++;

        $row["sub_data"] = $sub_text;

        /* hidden(学籍番号) */
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

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model)
{
    //保存ボタン
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", " onclick=\"return btn_submit('update');\"");
    //取消ボタン
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"");
    //戻るボタン
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"btn_back('main');\"");
}
?>
