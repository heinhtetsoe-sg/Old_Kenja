<?php

require_once('for_php7.php');

class knjb1231Form1
{
    function main(&$model){

        $objForm = new form;

        $arg["start"]   = $objForm->get_start("knjb1231Form1", "POST", "knjb1231index.php", "", "knjb1231Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //出力種別ラヂオ 1:新入生 2:在校生
        $opt = array(1, 2);
        $model->field["SEARCH_DIV"] = ($model->field["SEARCH_DIV"] == "") ? "2" : $model->field["SEARCH_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"SEARCH_DIV{$val}\" onClick=\"btn_submit('knjb1231')\"");
        }
        $radioArray = knjCreateRadio($objForm, "SEARCH_DIV", $model->field["SEARCH_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //登録年度年度コンボ
        $query = knjb1231Query::getExeYear($model);
        $extra = "onChange=\"return btn_submit('knjb1231');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["EXE_YEAR"], "EXE_YEAR", $extra, 1);

        //処理年度パターン
        $model->field["EXE_NENDO_PATERN"] = CTRL_YEAR == $model->field["EXE_YEAR"] ? "1" : "2";
        knjCreateHidden($objForm, "EXE_NENDO_PATERN", $model->field["EXE_NENDO_PATERN"]);

        $opt=array();
        if (is_numeric($model->control["学期数"])){
            //年度,学期コンボの設定
            for ( $i = 0; $i < (int) $model->control["学期数"]; $i++ ){
                $opt[]= array("label" => $model->control["学期名"][$i+1], 
                              "value" => sprintf("%d", $i+1)
                             );            
            }
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "SEMESTER",
                            "size"       => "1",
                            "value"      => isset($model->field["SEMESTER"])?$model->field["SEMESTER"]:CTRL_SEMESTER,
                            "extrahtml"  => "onChange=\"return btn_submit('knjb1231');\"",
                            "options"    => $opt));

        $arg["data"]["SEMESTER"] = $objForm->ge("SEMESTER");

        if(isset($model->field["SEMESTER"])) {
            $ga = $model->field["SEMESTER"];
        }
        else {
            $ga = CTRL_SEMESTER;
        }

        //出力指定選択 (1:個人指定 2:クラス指定)
        $opt = array(1, 2);
        $model->field["CHOICE"] = ($model->field["CHOICE"] == "") ? "1" : $model->field["CHOICE"];
        $click = " onClick=\"return btn_submit('knjb1231');\"";
        $extra = array("id=\"CHOICE1\"".$click, "id=\"CHOICE2\"".$click);
        $radioArray = knjCreateRadio($objForm, "CHOICE", $model->field["CHOICE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //クラス一覧リスト
        $db = Query::dbCheckOut();
        $row1 = array();
        $value_flg = false;
        $max_len = 0;
        $query = knjb1231Query::getAuth($model, CTRL_YEAR, $ga);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) $value_flg = true;

            if ($model->field["CHOICE"] == "1") {
                //年組のMAX文字数取得
                $zenkaku = (strlen($row["LABEL"]) - mb_strlen($row["LABEL"])) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($row["LABEL"]) - $zenkaku : mb_strlen($row["LABEL"]);
                $max_len = ($zenkaku * 2 + $hankaku > $max_len) ? $zenkaku * 2 + $hankaku : $max_len;
            }
        }

        //1:個人表示指定用
        $opt_left = array();
        if ($model->field["CHOICE"] == "1") {
            if(!isset($model->field["GRADE_HR_CLASS"]) || !$value_flg) {
                $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
            }

            $objForm->ae( array("type"       => "select",
                                "name"       => "GRADE_HR_CLASS",
                                "size"       => "1",
                                "value"      => $model->field["GRADE_HR_CLASS"],
                                "extrahtml"  => " onChange=\"return btn_submit('change_class');\"",
                                "options"    => $row1));

            $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

            $row1 = array();
            //生徒単位
            $selectleft = $model->selectleft ? explode(",", $model->selectleft) : array();
            $selectleftval = explode(",", $model->selectleftval);
            $query = knjb1231Query::getSchno($model,CTRL_YEAR,$ga);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //クラス名称調整
                $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
                $len = $zenkaku * 2 + $hankaku;
                $hr_name = $row["HR_NAME"];
                for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) $hr_name .= "&nbsp;";

                $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["LABEL"], 
                                                             'value' => $row["SCHREGNO"]);
                if($model->cmd == 'change_class' ) {
                    if (!in_array($row["SCHREGNO"], $selectleft)){
                        $row1[] = array('label' => $row["LABEL"],
                                        'value' => $row["SCHREGNO"]);
                    }
                } else {
                    $row1[] = array('label' => $row["LABEL"],
                                    'value' => $row["SCHREGNO"]);
                }
            }
            //左リストで選択されたものを再セット
            if($model->cmd == 'change_class' ) {
                for ($i = 0; $i < get_count($selectleft); $i++) {
                    $opt_left[] = array("label" => $selectleftval[$i],
                    "value" => $selectleft[$i]);
                }
            }
        }

        $result->free();

        $chdt = $model->field["CHOICE"];

        //対象者リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right',$chdt)\"",
                            "size"       => "15",
                            "options"    => $opt_left));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

        //生徒一覧リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left',$chdt)\"",
                            "size"       => "15",
                            "options"    => $row1));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");

        // << ボタン作成
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('leftall', $chdt);\"";
        $arg["button"]["btn_left2"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
        // ＜ ボタン作成
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('left', $chdt);\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
        // ＞ ボタン作成
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('right', $chdt);\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
        // >> ボタン作成
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('rightall', $chdt);\"";
        $arg["button"]["btn_right2"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);


        // //対象取り消しボタンを作成する(個別)
        // $objForm->ae( array("type" => "button",
        //                     "name"        => "btn_right1",
        //                     "value"       => "　＞　",
        //                     "extrahtml"   => " onclick=\"move('right',$chdt);\"" ) );
        // 
        // $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");
        // 
        // //対象取り消しボタンを作成する(全て)
        // $objForm->ae( array("type" => "button",
        //                     "name"        => "btn_right2",
        //                     "value"       => "　≫　",
        //                     "extrahtml"   => " onclick=\"move('rightall',$chdt);\"" ) );
        // 
        // $arg["button"]["btn_right2"] = $objForm->ge("btn_right2");
        // 
        // //対象選択ボタンを作成する(個別)
        // $objForm->ae( array("type" => "button",
        //                     "name"        => "btn_left1",
        //                     "value"       => "　＜　",
        //                     "extrahtml"   => " onclick=\"move('left',$chdt);\"" ) );
        // 
        // $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");
        // 
        // //対象選択ボタンを作成する(全て)
        // $objForm->ae( array("type" => "button",
        //                     "name"        => "btn_left2",
        //                     "value"       => "　≪　",
        //                     "extrahtml"   => " onclick=\"move('leftall',$chdt);\"" ) );
        // 
        // $arg["button"]["btn_left2"] = $objForm->ge("btn_left2");

        //生徒項目名切替処理
        $sch_label  = "";
        $sch_label  = (strlen($sch_label) > 0) ? $sch_label : '生徒';
        $list_label = 'クラス';
        //項目名セット
        $arg["data"]["SCH_LABEL"] = $sch_label;
        $arg["data"]["CATEGORY_LABEL"] = ($model->field["CHOICE"] == "1") ? $sch_label : $list_label;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する
        knjCreateHidden($objForm, "YEAR" , CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER" , CTRL_SEMESTER);
        knjCreateHidden($objForm, "DBNAME" , DB_DATABASE);
        knjCreateHidden($objForm, "PRGID" , "KNJB1231");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useSpecial_Support_Hrclass" , $model->Properties["useSpecial_Support_Hrclass"]);
        knjCreateHidden($objForm, "useFi_Hrclass" , $model->Properties["useFi_Hrclass"]);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        
        //左のリストを保持
        knjCreateHidden($objForm, "selectleft");
        knjCreateHidden($objForm, "selectleftval");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjb1231Form1.html", $arg); 
    }
}
//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "", "value" => "");
    } else if ($blank == "ALL") {
        $opt[] = array ("label" => "全て", "value" => "");
    }
    $result = $db->query($query);

    $retArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;
        if ($name == 'RISYUU_COURSE') {
            $retArray[$row["VALUE"]][$row["VALUE"]] = 1;
            if ($row["NAMESPARE1"]) {
                $setValArray = explode(",", $row["NAMESPARE1"]);
                foreach ($setValArray as $key => $val) {
                    $retArray[$row["VALUE"]][$val] = 1;
                }
            }
        }
    }
    $result->free();

    $value = (strlen($value) > 0 && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    return $retArray;
}
//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
