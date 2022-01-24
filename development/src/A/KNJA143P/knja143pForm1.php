<?php

require_once('for_php7.php');

/*
 *　修正履歴
 *
 */
class knja143pForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja143pForm1", "POST", "knja143pindex.php", "", "knja143pForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //学期マスタ
        $query = knja143pQuery::getSemeMst(CTRL_YEAR, CTRL_SEMESTER);
        $Row_Mst = $db->getRow($query,DB_FETCHMODE_ASSOC);

        //年度
        knjCreateHidden($objForm, "YEAR", $Row_Mst["YEAR"]);
        $arg["data"]["YEAR"] = $Row_Mst["YEAR"];

        //学期
        knjCreateHidden($objForm, "GAKKI", $Row_Mst["SEMESTER"]);
        $arg["data"]["GAKKI"] = $Row_Mst["SEMESTERNAME"];

        //校種コンボ
        $extra = "onChange=\"return btn_submit('knja143p');\"";
        $query = knja143pQuery::getSchoolKind($model);
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);

        //発行日
        if( !isset($model->field["TERM_SDATE"]) ) 
            $model->field["TERM_SDATE"] = str_replace("-","/",CTRL_DATE);
        $arg["data"]["TERM_SDATE"]=View::popUpCalendar($objForm,"TERM_SDATE",$model->field["TERM_SDATE"]);

        //有効期限
        $setYear = CTRL_YEAR + 1;
        $setDate = $setYear.'-03-31';
        if( !isset($model->field["TERM_EDATE"]) ) 
            $model->field["TERM_EDATE"] = str_replace("-","/",$setDate);
        $arg["data"]["TERM_EDATE"]=View::popUpCalendar($objForm,"TERM_EDATE",$model->field["TERM_EDATE"]);

        //表示指定ラジオボタン 1:クラス 2:個人
        $opt_disp = array(1, 2);
        $model->field["DISP"] = ($model->field["DISP"] == "") ? "1" : $model->field["DISP"];
        $extra = array("id=\"DISP1\" onClick=\"return btn_submit('knja143p')\"", "id=\"DISP2\" onClick=\"return btn_submit('knja143p')\"");
        $radioArray = knjCreateRadio($objForm, "DISP", $model->field["DISP"], $extra, $opt_disp, get_count($opt_disp));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["DISP"] == 2) {
            //年組コンボ
            $extra = "onChange=\"return btn_submit('change');\"";
            $query = knja143pQuery::getAuth($model, CTRL_YEAR, CTRL_SEMESTER);
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);
        }

        if($model->Properties["useFormNameA143P"] != ""){
            //ソート順を表示
            $arg["data"]["SORT"] = 1; 
            //ソート順ラジオボタン 1:組番号順 2:学籍番号順
            $sort = array(1, 2);
            $model->field["SORT_DIV"] = ($model->field["SORT_DIV"] == "") ? "1" : $model->field["SORT_DIV"];
            $extra = "";
            $radioArray = knjCreateRadio($objForm, "SORT_DIV", $model->field["SORT_DIV"], $extra, $sort, get_count($sort));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        }

        /******************/
        /* リストtoリスト */
        /******************/
        //表示切替
        if ($model->field["DISP"] == 2) {
            $arg["data"]["TITLE_LEFT"]  = "出力対象一覧";
            $arg["data"]["TITLE_RIGHT"] = "生徒一覧";
        } else {
            $arg["data"]["TITLE_LEFT"]  = "出力対象クラス";
            $arg["data"]["TITLE_RIGHT"] = "クラス一覧";
        }

        //生徒一覧リスト
        $opt_right = array();
        $opt_left  = array();

        //年組取得
        $query = knja143pQuery::getAuth($model, CTRL_YEAR, CTRL_SEMESTER);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->field["DISP"] == 2) {
                //年組のMAX文字数取得
                $zenkaku = (strlen($row["LABEL"]) - mb_strlen($row["LABEL"])) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($row["LABEL"]) - $zenkaku : mb_strlen($row["LABEL"]);
                $max_len = ($zenkaku * 2 + $hankaku > $max_len) ? $zenkaku * 2 + $hankaku : $max_len;
            } else {
                //一覧リスト（右側）
                $opt_right[] = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
            }
        }
        $result->free();

        //個人指定
        if ($model->field["DISP"] == 2) {
            $selectleft = ($model->selectleft != "") ? explode(",", $model->selectleft) : array();
            $selectleftval = ($model->selectleftval != "") ? explode(",", $model->selectleftval) : array();

            //生徒取得
            $query = knja143pQuery::getSchno($model, CTRL_YEAR, CTRL_SEMESTER);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //クラス名称調整
                $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
                $len = $zenkaku * 2 + $hankaku;
                $hr_name = $row["HR_NAME"];
                for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) $hr_name .= "&nbsp;";

                if ($model->cmd == 'change') {
                    if (!in_array($row["VALUE"], $selectleft)) {
                        $opt_right[] = array('label' => $hr_name."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                             'value' => $row["VALUE"]);
                    }
                } else {
                    $opt_right[] = array('label' => $hr_name."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                         'value' => $row["VALUE"]);
                }
            }
            $result->free();

            //左リストで選択されたものを再セット
            if ($model->cmd == 'change') {
                for ($i = 0; $i < get_count($selectleft); $i++) {
                    $opt_left[] = array("label" => $selectleftval[$i],
                                        "value" => $selectleft[$i]);
                }
            }
        }

        $disp = $model->field["DISP"];

        //生徒一覧リスト(右)
        $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('left', $disp)\"";
        $arg["data"]["CATEGORY_NAME"] = createCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

        //出力対象一覧リスト(左)
        $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('right', $disp)\"";
        $arg["data"]["CATEGORY_SELECTED"] = createCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

        //対象取消ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', $disp);\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象選択ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', $disp);\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象取消ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', $disp);\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象選択ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', $disp);\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        knjCreateHidden($objForm, "selectleft");
        knjCreateHidden($objForm, "selectleftval");

        //ボタンを作成する
        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA143P");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
        knjCreateHidden($objForm, "certifPrintRealName" , $model->Properties["certifPrintRealName"]);
        knjCreateHidden($objForm, "useFormNameA143P" , $model->Properties["useFormNameA143P"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja143pForm1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array('label' => "全　て",
                       'value' => "");
    }
    if ($blank == "BLANK") {
        $opt[] = array('label' => "",
                       'value' => "");
    }
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

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "YEAR_SEMESTER") ? CTRL_YEAR.":".CTRL_SEMESTER : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}
?>
