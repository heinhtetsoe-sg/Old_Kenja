<?php

require_once('for_php7.php');

class knjj193Form1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjj193Form1", "POST", "knjj193index.php", "", "knjj193Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //出力対象選択ラジオボタン 1:学年 2:クラス 3.個人 4.支部 5.自宅生 6.寮生 7.下宿生 8.寮生/下宿生
        $opt = array(1, 2, 3, 4, 5, 6, 7, 8);
        $model->field["DATA_DIV"] = ($model->field["DATA_DIV"] == "") ? "1" : $model->field["DATA_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"DATA_DIV{$val}\" onClick=\"btn_submit('knjj193')\"");
        }
        $radioArray = knjCreateRadio($objForm, "DATA_DIV", $model->field["DATA_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学期コンボ
        $query = knjj193Query::getSemester($model);
        $extra = "onchange=\"return btn_submit('knjj193');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ
        if ($model->field["DATA_DIV"] == "2" || $model->field["DATA_DIV"] == "3") {
            $query = knjj193Query::getRegdGdat($model);
            $extra = "onchange=\"return btn_submit('knjj193');\"";
            makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);
        }

        //年組コンボ
        if ($model->field["DATA_DIV"] == "3") {
            $query = knjj193Query::getRegdHdat($model, $model->field["GRADE"]);
            $extra = "onchange=\"return btn_submit('knjj193');\"";
            makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->field["HR_CLASS"], $extra, 1);
        }

        //長子チェックボックス
        $extra = ($model->field["TYOUSHI_FLG"] == "1") ? "checked" : "";
        $extra .= " id=\"TYOUSHI_FLG\"";
        if (in_array($model->field["DATA_DIV"], array('3','5','6','7','8'))) {
            $extra .= " onClick=\"btn_submit('knjj193')\"";
        }
        $arg["data"]["TYOUSHI_FLG"] = knjCreateCheckBox($objForm, "TYOUSHI_FLG", "1", $extra, "");

        //異動も出力する
        $extra = ($model->field["IDOU_FLG"] == "1") ? "checked" : "";
        $extra .= " id=\"IDOU_FLG\"";
        $extra .= " onchange=\"return btn_submit('knjj193');\"";
        $arg["data"]["IDOU_FLG"] = knjCreateCheckBox($objForm, "IDOU_FLG", "1", $extra, "");

        //日付作成
        $model->field["IDOU_DATE"] = $model->field["IDOU_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["IDOU_DATE"];
        $extra = " return btn_submit('knjj193'); ";
        $arg["data"]["IDOU_DATE"] = View::popUpCalendar2($objForm, "IDOU_DATE", $model->field["IDOU_DATE"], "", $extra);

        //リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //開始位置（行）
        $rowArray = array(array('label' => "１行",'value' => 1),
                          array('label' => "２行",'value' => 2),
                          array('label' => "３行",'value' => 3),
                          array('label' => "４行",'value' => 4),
                          array('label' => "５行",'value' => 5),
                          array('label' => "６行",'value' => 6),
                          array('label' => "７行",'value' => 7));
        $arg["data"]["S_ROW"] = knjCreateCombo($objForm, "S_ROW", $model->field["S_ROW"], $rowArray, "", 1);

        //開始位置（列）
        $colArray = array(array('label' => "１列",'value' => 1),
                          array('label' => "２列",'value' => 2));
        $arg["data"]["S_COL"] = knjCreateCombo($objForm, "S_COL", $model->field["S_COL"], $colArray, "", 1);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "PROGRAMID");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "LOGIN_SCHOOLCD", sprintf("%012d", SCHOOLCD));
        knjCreateHidden($objForm, "LOGIN_SCHOOL_KIND", SCHOOLKIND);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj193Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    $list = array();
    $list[1] = array("label" => "学年",         "query" => knjj193Query::getRegdGdat($model),  "flg" => "1");
    $list[2] = array("label" => "クラス",       "query" => knjj193Query::getRegdHdat($model, $model->field["GRADE"]),  "flg" => "1");
    $list[3] = array("label" => "個人",         "query" => knjj193Query::getSchList($model),   "flg" => "1");
    $list[4] = array("label" => "支部",         "query" => knjj193Query::getBranchMst($model), "flg" => "1");
    $list[5] = array("label" => "自宅生",       "query" => knjj193Query::getEnvirSchList($model, '1'),   "flg" => "2");
    $list[6] = array("label" => "寮生",         "query" => knjj193Query::getEnvirSchList($model, '4'),   "flg" => "2");
    $list[7] = array("label" => "下宿生",       "query" => knjj193Query::getEnvirSchList($model, '2'),   "flg" => "2");
    $list[8] = array("label" => "寮生/下宿生",  "query" => knjj193Query::getEnvirSchList($model, '2,4'), "flg" => "2");

    if ($list[$model->field["DATA_DIV"]]["flg"] == "2") {
        //年組のMAX文字数取得
        $max_len = 0;
        $query = knjj193Query::getRegdHdat($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $zenkaku = (strlen($row["LABEL"]) - mb_strlen($row["LABEL"])) / 2;
            $hankaku = ($zenkaku > 0) ? mb_strlen($row["LABEL"]) - $zenkaku : mb_strlen($row["LABEL"]);
            $max_len = ($zenkaku * 2 + $hankaku > $max_len) ? $zenkaku * 2 + $hankaku : $max_len;
        }
    }

    //一覧取得
    $opt = array();
    $query = $list[$model->field["DATA_DIV"]]["query"];
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($list[$model->field["DATA_DIV"]]["flg"] == "2") {
            //クラス名称調整
            $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
            $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
            $len = $zenkaku * 2 + $hankaku;
            $hr_name = $row["HR_NAME"];
            for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) $hr_name .= "&nbsp;";

            $opt[] = array('label' => $hr_name." ".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                           'value' => $row["VALUE"]);
        } else {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //名称表示
    $arg["data"]["CLASS_LABEL"] = $list[$model->field["DATA_DIV"]]["label"];

    //一覧作成
    $extra = "multiple style=\"width:250px;\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $opt, $extra, 15);

    //出力対象作成
    $extra = "multiple style=\"width:250px;\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 15);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}
?>
