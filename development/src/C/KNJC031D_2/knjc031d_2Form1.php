<?php

require_once('for_php7.php');

class knjc031d_2Form1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjc031d_2index.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $arg["year"] = CTRL_YEAR;

        /* 学期 */
        $query = knjc031d_2Query::getSemesterName(CTRL_YEAR, CTRL_SEMESTER);
        $semesterName = $db->getOne($query);
        $arg["semester"] = $semesterName;

        /* 対象学級 */
        $query = knjc031d_2Query::getHrName($model);
        $arg["HR_CLASS"] = $db->getOne($query);

        /* 対象月 */
        list($month, $semester) = explode('-',$model->month);
        //学期名
        $query = knjc031d_2Query::getSemesterName(CTRL_YEAR, $semester);
        $semeName = $db->getOne($query);
        //月名
        $query = knjc031d_2Query::getMonthName($month, $model);
        $monthName = $db->getOne($query);
        $arg["MONTH"] = $monthName.' ('.$semeName.') ';

        /* 締め日 */
        $arg["APPOINTED_DAY"] = $model->appointed_day;

        //リンク先のURL
        $jump = REQUESTROOT."/C/KNJC031D_2/knjc031d_2index.php";

        //リンク元のプログラムＩＤ＆権限
        $prgid  = "KNJC031D_2";
        $auth   = $model->auth;

        //出欠タイトル
        $attendItem = array("ABSENT"        => array("1","公欠日数"),
                            "SUSPEND"       => array("2","出停"),
                            "MOURNING"      => array("3","忌引"));

        //欠席は名称マスタより取得
        $setFieldName = array("4" => "SICK", "5" => "NOTICE", "6" => "NONOTICE");
        foreach ($setFieldName as $key => $val) {
            $result = $db->query(knjc031d_2Query::getSickDiv());
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if($key == $row["VALUE"]) {
                    $attendItem[$val] = array($row["VALUE"],$row["LABEL"]);
                }
            }
            $result->free();
        }

        //ヘッダのタイトルをセット
        if(substr($model->title,0,3) != "CNT") {    //出欠種別名
            foreach ($attendItem as $key => $val) {
                if($key == $model->title){
                    $title = $val[1];
                    $col_cnt = get_count($db->getCol(knjc031d_2Query::getNameMst('C006', $val[0])));
                    $arg["TITLE1"] = "<td colspan={$col_cnt}>".$title."</td>";
                    $namespare1 = $val[0];
                    knjCreateHidden($objForm, "DI_CD", $namespare1);
                    knjCreateHidden($objForm, "DI_NAME", $key);
                }
            }
        } else {    //大分類
            $title = "";
            $namespare1 = substr($model->title,3);
            $result = $db->query(knjc031d_2Query::getNameMst2('C006', $namespare1));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

                $col_cnt = get_count($db->getCol(knjc031d_2Query::getNameMst('C007', $row["VALUE"])));
                $p_namespare1 = $db->getOne(knjc031d_2Query::getNameMst3('C006', $row["VALUE"]));

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
        $notUpdateItem = "";
        $namecd1 = (substr($model->title,0,3) == 'CNT') ? 'C007' : 'C006';
        $result = $db->query(knjc031d_2Query::getNameMst($namecd1, $namespare1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            if($namecd1 == 'C006'){
                $param  = "?prgid={$prgid}";
                $param .= "&auth={$auth}";
                $param .= "&HR_CLASS_TYPE={$model->hr_class_type}";
                $param .= "&GRADE={$model->grade}";
                $param .= "&HR_CLASS={$model->hr_class}";
                $param .= "&MONTH={$model->month}";
                $param .= "&APPOINTED_DAY={$model->appointed_day}";
                $param .= "&TITLE=CNT{$row["VALUE"]}";

                $cnt_check = "0";
                $extra = "style=\"font-color:white;\" onClick=\"openSubWindow('{$jump}{$param}');\"";
                $cnt_check = get_count($db->getCol(knjc031d_2Query::getNameMst('C007', $row["VALUE"])));
                if ($cnt_check > "0" && $model->grade && $model->hr_class && $model->month) {
                    $label = View::alink("#", "<font color=\"hotpink\">".$row["LABEL"]."</font>", $extra);
                    $notUpdateItem .= ($notUpdateItem) ? ','.$row["VALUE"] : $row["VALUE"];
                } else {
                    $label = $row["LABEL"];
                }
            } else {
                $label = $row["LABEL"];
            }

            $sub_width = ($col_cnt == $sub_cnt) ? "#" : (round(700 / $col_cnt));
            $sub_title .= "<td width={$sub_width}>".$label."</td>";

            $sub_cnt++;
        }
        $result->free();
        knjCreateHidden($objForm, "NOT_UPDATE_ITEM", $notUpdateItem);

        $arg["SUB_TITLE"] = $sub_title;


        /* 編集対象データリスト */
        makeDataList($objForm, $arg, $db, $model, $objUp, $header, $hrName, $monthName, $attendItem, $namespare1, $col_cnt);

        /* ボタン作成 */
        makeButton($objForm, $arg, $db, $model);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /**********/
        /* hidden */
        /**********/
        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJC031D_2");
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        $arg["finish"]  = $objForm->get_finish();

        /* テンプレート呼び出し */
        View::toHTML($model, "knjc031d_2Form1.html", $arg);
    }
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model, &$objUp, $headerData, $hrName, $monthName, $attendItem, $namespare1, $col_cnt)
{
    $tukiAndGakki = explode("-", $model->month);
    $tuki  = $tukiAndGakki[0];
    $gakki = $tukiAndGakki[1];
    $query = knjc031d_2Query::getAppointedDay($tuki, $gakki, $model);
    $appointed_day = $db->getOne($query);

    $schoolMst = array();
    $query = knjc031d_2Query::getSchoolMst();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        foreach ($row as $key => $val) {
            $schoolMst[$key] = $val;
        }
    }

    //登録されている大分類・中分類を抽出
    $sub_data = array();
    $namecd1 = (substr($model->title,0,3) == 'CNT') ? 'C007' : 'C006';
    $sub_data = $db->getCol(knjc031d_2Query::getNameMst($namecd1, $namespare1));


    //貼り付け機能対象項目
    $setFieldData = $sep = "";
    foreach ($sub_data as $sublKey) {
        $cnt_check = "0";
        $cnt_check = get_count($db->getCol(knjc031d_2Query::getNameMst('C007', $sublKey)));

        if($cnt_check == "0") {
            $setFieldData .= $sep."CNT".$sublKey."[]";
            $sep = ",";
        }
    }
    knjCreateHidden($objForm, "SET_FIELD", $setFieldData);

    //メインデータ
    $query      = knjc031d_2Query::selectAttendQuery($model, $schoolMst, $sub_data, $namespare1);
    $result     = $db->query($query);

    $monthsem = array();
    $monthsem = preg_split("/-/", $model->month);

    //行数（貼り付け機能で使用）
    knjCreateHidden($objForm, "objCntSub", get_count($db->getCol($query)));

    $counter  = 0;
    $colorFlg = false;
    $data = array();
    $schCnt = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        /* 出欠月別累積データ */
        if (isset($model->warning)) {
            if (is_array($model->reset[$row["SCHREGNO"]])) {
                foreach ($model->reset[$row["SCHREGNO"]] as $key => $val) {
                    $row[$key] = $val;
                }
            }
        }

        foreach ($sub_data as $sublKey) {
            $setArray["CNT".$sublKey] = array("SIZE" => 2, "MAXLEN" => 3);
        }

        //異動者（退学・転学・卒業）
        $idou_year = ($monthsem[0] < '04') ? CTRL_YEAR + 1 : CTRL_YEAR;
        $idou_day = ($appointed_day == "") ? getFinalDay($db, $monthsem[0], $monthsem[1]) : $appointed_day;
        $idou_date = $idou_year.'-'.$monthsem[0].'-'.$idou_day;
        $idou = $db->getOne(knjc031d_2Query::getIdouData($row["SCHREGNO"], $idou_date));
        $row["BGCOLOR_IDOU"] = ($idou > 0) ? "bgcolor=yellow" : "";

        $sub_text = "";
        $sub_cnt = "1";
        $schregno = "";
        foreach ($setArray as $key => $val) {

            $check_fin[] = $row[$key];

            $schregno = $row["SCHREGNO"];

            $cnt_check = 0;
            if(substr($model->title,0,3) != 'CNT') {
                foreach ($sub_data as $sublKey) {

                    if("CNT".$sublKey == $key) {
                        $cnt_check = get_count($db->getCol(knjc031d_2Query::getNameMst('C007', $sublKey)));
                    }
                }
            }

            if($cnt_check > "0") {
                $sub_width = ($col_cnt == $sub_cnt) ? "#" : (round(700 / $col_cnt));
                $sub_text .= "<td {$row["BGCOLOR_IDOU"]} width={$sub_width}>".$row[$key]."</td>";
                $sub_cnt++;
                continue;
            } else {
                //textbox
                $extra = "STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"; onPaste=\"return showPaste(this, ".$schCnt.");\"";
                if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                    $value = $row[$key];
                } else {
                    $value = ($row[$key] != 0) ? $row[$key] : "";
                }
                $row[$key] = knjCreateTextBox($objForm, $value, $key."[]", $val["SIZE"], $val["MAXLEN"], $extra);

                $sub_width = ($col_cnt == $sub_cnt) ? "#" : (round(700 / $col_cnt));
                $sub_text .= "<td {$row["BGCOLOR_IDOU"]} width={$sub_width}>".$row[$key]."</td>";
                $sub_cnt++;
            }

        }
        $schCnt++;

        $chkKey = "";
        foreach ($row as $key => $val) {
            if (preg_match("/^SUB_SCHREGNO/", $key))  {
                $chkKey .= $val;
            }
        }

        if (strlen($chkKey) == 0) {
            $row["BGCOLOR_NAME_SHOW"] = "bgcolor=#ccffcc";
        }

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


    $arg["SET_APPOINTED_DAY"] = $appointed_day;
    //hidden
    knjCreateHidden($objForm, "SET_APPOINTED_DAY", $appointed_day);
}

//最終日取得
function getFinalDay($db, $month, $semester)
{
    $year = CTRL_YEAR;
    if ($month != "" && $month < "04") {
        $year = CTRL_YEAR + 1;
    }

    $lastday = date("t", mktime( 0, 0, 0, $month, 1, $year ));
    $semeday = $db->getRow(knjc031d_2Query::selectSemesAll($semester),DB_FETCHMODE_ASSOC);
    //学期マスタの最終日より大きい場合
    if (sprintf('%02d', $semeday["E_MONTH"]) == $month &&
        $semeday["E_DAY"] < $lastday) {
        $lastday = $semeday["E_DAY"];
    }
    return $lastday;
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
