<?php

require_once('for_php7.php');
class knjz211dForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ
        $query = knjz211dQuery::getSchKind($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->schoolkind, $extra, 1);

        //学年コンボボックス
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjz211dQuery::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->grade, $extra, 1, "");

        //科目コンボボックス
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjz211dQuery::getSubclasscd($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASS", $model->subclass, $extra, 1, "");

        //学期評価しないの各学期列の基本幅
        $width = 300 /get_count($model->semester);

        //パターン一覧
        $opt = $pattern = array();
        $valid1 = false;
        $query = knjz211dQuery::getPattern($model);
        $opt[] = array("label" => "", "value" => "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            $pattern[] = $row["VALUE"];
            $valid1 = true;
        }
        $result->free();

        //パターンコンボ（反映用）
        $arg["PATTERN_CD"] = knjCreateCombo($objForm, "PATTERN_CD", $opt[0]["value"], $opt, "", 1);

        //タイトル（学期）
        $setTitle = "";
        foreach ($model->semester as $semester => $semestername) {
            //学期内全チェックボックス
            $name = "VIEWFLG".$semester;
            $extra = " onClick=\"changeAllOnOff('{$name}')\" ";
            $checkbox = knjCreateCheckBox($objForm, $name, "1", $extra);
            //幅設定
            $setWidth = ($semester == max(array_keys($model->semester))) ? "width=\"*\"" : "width=\"".$width."\"";
            //セット
            $event = " id=\"".$name."\" onClick=\"changeAllOnOff('{$name}')\" onMouseOver=\"changeColor('on', '1', '{$name}')\"  onMouseOut=\"changeColor('off', '1', '{$name}')\" ";
            $setTitle .= "<td ".$setWidth." ".$event.">".$semestername."<br>".$checkbox."</td>";
        }
        $arg["TITLE"] = $setTitle;
        $arg["COLSPAN"] = (get_count($model->semester) > 1) ? "colspan=\"".get_count($model->semester)."\"" : "";

        //一覧表示
        $arr_Viewcd = array();
        $valid2 = false;
        if ($model->grade && $model->subclass) {
            //データ取得
            $counter = 0;
            $query = knjz211dQuery::selectQuery($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $counter++;
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_Viewcd[] = $row["VIEWCD"];

                //警告メッセージがある場合、保持データを表示
                if (isset($model->warning)) {
                    $row["PATTERN_CD"] = $model->arrPatternCd[$row["VIEWCD"]];
                    foreach ($model->semester as $semester => $semestername) {
                        $row["VIEWFLG".$semester] = $model->arrViewFlg[$row["VIEWCD"]][$semester];
                    }
                }

                //パターンコンボ
                $name = "PATTERN_CD-".$row["VIEWCD"];
                $value = $row["PATTERN_CD"];
                $value = ($value && in_array($row["PATTERN_CD"], $pattern)) ? $value : $opt[0]["value"];
                $row["PATTERN_CD"] = knjCreateCombo($objForm, $name, $value, $opt, "", 1);

                //学期評価なしフラグチェックボックス
                $setData = "";
                foreach ($model->semester as $semester => $semestername) {
                    //部品
                    $name = "VIEWFLG".$semester;
                    $id = $name."-".$row["VIEWCD"];
                    $extra  = ($row[$name] == "1") ? "checked" : "";
                    $extra .= " onClick=\"changeOnOff('{$id}')\" ";
                    $row[$name] = knjCreateCheckBox($objForm, $name."-".$row["VIEWCD"], "1", $extra);
                    //幅設定
                    $setWidth = ($counter == 1) ? ($semester == max(array_keys($model->semester))) ? "width=\"*\"" : "width=\"".$width."\"" : "";
                    //セット
                    $event = " id=\"".$id."\" onClick=\"changeOnOff('{$id}')\" onMouseOver=\"changeColor('on', '0', '{$id}')\"  onMouseOut=\"changeColor('off', '0', '{$id}')\" ";
                    $setData .= "<td ".$setWidth." align=\"center\" ".$event.">".$row[$name]."</td>";
                }
                $row["SETDATA"] = $setData;

                $arg["data"][] = $row;
                $valid2 = true;
            }
            $result->free();
        }

        //反映ボタンの使用可・不可
        $valid = ($valid1 && $valid2) ? true : false;

        //JVIEWNAME_GRADE_YDATに該当の学年が登録されていない場合はメッセージ表示。
        if (!$valid) {
            $mstdatCnt = $db->getOne(knjz211dQuery::checkExistsGradeYDat(CTRL_YEAR, $model->schoolkind, $model->grade));
            if ($mstdatCnt == 0) {
                $model->setWarning("MSG305", "選択した学年の学年別観点年度データが設定されていません。");
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $valid);

        //存在チェック
        $thisCnt    = $db->getOne(knjz211dQuery::checkExistsSubclassPattern(CTRL_YEAR, $model->schoolkind));
        $preCnt     = $db->getOne(knjz211dQuery::checkExistsSubclassPattern((CTRL_YEAR - 1), $model->schoolkind));
        knjCreateHidden($objForm, "THIS_YEAR_CNT", $thisCnt);
        knjCreateHidden($objForm, "PRE_YEAR_CNT", $preCnt);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_VIEWCD", implode(",",$arr_Viewcd));

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz211dindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz211dForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $valid) {
    //パターンのマスタ登録ボタン
    $link = REQUESTROOT."/Z/KNJZ211C/knjz211cindex.php?cmd=&PROGRAMID=KNJZ211C&SEND_PRGID=KNJZ211D&SCHOOL_KIND=".$model->schoolkind."&SEND_GRADE=".$model->grade."&SEND_SUBCLASS=".$model->subclass;
    $extra = " onclick=\"Page_jumper('{$link}');\"";
    $arg["btn_KNJZ211C"] = knjCreateBtn($objForm, "btn_KNJZ211C", "マスタ登録", $extra);

    //反映ボタン
    $extra = ($valid) ? "onclick=\"reflect();\"" : "disabled";
    $arg["btn_reflect"] = knjCreateBtn($objForm, "btn_reflect", "反 映", $extra);

    //権限  
    $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : " disabled";

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disable);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //権限
    $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : " disabled";
    //前年度コピーボタン
    $extra = "onclick=\"return btn_submit('copy')\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra.disable);

}
?>
