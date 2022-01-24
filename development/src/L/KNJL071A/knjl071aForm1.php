<?php

require_once('for_php7.php');

class knjl071aForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = $model->ObjYear;

        //extra
        $change = " onchange=\"return btn_submit('main');\" tabindex=-1";
        $click = " onclick=\"return btn_submit('main');\"";

        //初期画面判定
        $defaultFlg = (!$model->applicantdiv && !$model->testdiv) ? true : false;

        //校種区分コンボボックス
        $extra = $change;
        $query = knjl071aQuery::getNameMst($model->ObjYear, 'L003');
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //受験校種(J,H)
        $query = knjl071aQuery::getSchoolKind($model);
        $model->schoolKind = $db->getOne($query);

        //試験コンボボックス
        $extra = $change;
        $query = knjl071aQuery::getTestdivMst($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "");

        //表示順
        $opt = array(1, 2, 3);
        $model->sort = ($model->sort == "") ? "1" : $model->sort;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SORT{$val}\"".$change);
        }
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //出身学校
        $extra = $change;
        $query = knjl071aQuery::getFinschoolMst($model);
        makeCmb($objForm, $arg, $db, $query, "SYUSSIN_SCHOOL", $model->syussinSchool, $extra, 1, "ALL");

        //志望コース
        $extra = $change;
        $query = knjl071aQuery::getCourseCmb($model);
        makeCmb($objForm, $arg, $db, $query, "SIBOU_COURSE", $model->sibouCourse, $extra, 1, "ALL");

        //事前チェック(特待区分)
        $valArray = $labelArray = $clubFlgArray = array();
        $query = knjl071aQuery::getHonordiv($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $valArray[]     = $row["VALUE"];
            $labelArray[]   = $row["LABEL"];
            $clubFlgArray[] = $row["CLUB_FLG"];
        }
        if (get_count($valArray) == 0) {
            $arg["pre_check"] = "errorPreCheck();";
        }
        knjCreateHidden($objForm, "HONORDIV_LIST", implode(',', $valArray));
        knjCreateHidden($objForm, "HONORDIV_CLUB_FLG_LIST", implode(',', $clubFlgArray));

        //受験校種(J,H)
        $query = knjl071aQuery::getNameMst($model->ObjYear, 'L003', $model->applicantdiv);
        $rowL003 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //クラブ情報コンボボックス
        $query = knjl071aQuery::getClubcd($rowL003["NAMESPARE3"]);
        $defaultClub = 0;
        $optClub = getClubCmbList($db, $query, $defaultClub, "BLANK");

        //確定特待コンボボックス
        $typeKakuteiTokutai = array(1, 2, 3); //特待区分:1、2、3
        $optKakuteiTokutai = getHonordivCmbList($db, $model, $typeKakuteiTokutai);

        //事前特待コンボボックス
        $typeJizenTokutai = array(1); //特待区分:1
        $optJizenTokutai = getHonordivCmbList($db, $model, $typeJizenTokutai);

        //特待申請コンボボックス
        $typeTokutaiSinsei = array(2); //特待区分:2
        $optTokutaiSinsei = getHonordivCmbList($db, $model, $typeTokutaiSinsei);

        //資格活用コンボボックス
        $typeSikakuKatsuyo = array(3); //特待区分:3
        $optSikakuKatsuyo = getHonordivCmbList($db, $model, $typeSikakuKatsuyo);

        //事前判定コンボ
        $query = knjl071aQuery::getJizenHanteiCmb($model);
        $optJizenHantei = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optJizenHantei[] = $row;
        }
        $result->free();

        //事前専願コンボボックス
        $query = knjl071aQuery::getJizenSenganCmb($model);
        $result = $db->query($query);
        $optJizenSengan = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optJizenSengan[] = $row;
        }
        $result->free();

        //合計
        $arg["TOTAL_LABEL"] = ($model->applicantdiv == "1") ? "判定" : "５科";

        //テキスト名
        $text_name = array("1"  => "KAKUTEI_TOKUTAI"
                          ,"2"  => "JIZEN_TOKUTAI"
                          ,"3"  => "TOKUTAI_SINSEI"
                          ,"4"  => "SIKAKU_KATSUYO"
                          ,"5"  => "DESIREDIV"
                          ,"6"  => "JIZEN_SENGAN"
                          ,"7"  => "CLUB_CD"
                          ,"8" => "HONOR_REMARK");
        $setTextField = "";
        $textSep = "";
        foreach ($text_name as $code => $col) {
            $setTextField .= $textSep.$col."-";
            $textSep = ",";
        }

        //一覧表示
        $dataFlg = false;
        $model->aryAllRecept = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {
            //データ取得
            $query = knjl071aQuery::selectQuery($model);
            $result = $db->query($query);
            
            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0 && !$defaultFlg) {
                $model->setMessage("MSG303");
            }
            
            $count = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $model->aryAllRecept[] = $row["RECEPTNO"];

                knjCreateHidden($objForm, "EXAMNO-".$row["RECEPTNO"], $row["EXAMNO"]);

                //エラー時は画面の値をセット
                if (isset($model->warning)) {
                    $row["KAKUTEI_TOKUTAI"] = $model->arrInput[$row["RECEPTNO"]]["KAKUTEI_TOKUTAI"];
                    $row["JIZEN_TOKUTAI"]   = $model->arrInput[$row["RECEPTNO"]]["JIZEN_TOKUTAI"];
                    $row["TOKUTAI_SINSEI"]  = $model->arrInput[$row["RECEPTNO"]]["TOKUTAI_SINSEI"];
                    $row["SIKAKU_KATSUYO"]  = $model->arrInput[$row["RECEPTNO"]]["SIKAKU_KATSUYO"];
                    $row["DESIREDIV"]       = $model->arrInput[$row["RECEPTNO"]]["DESIREDIV"];
                    $row["JIZEN_SENGAN"]    = $model->arrInput[$row["RECEPTNO"]]["JIZEN_SENGAN"];
                    $row["CLUB_CD"]         = $model->arrInput[$row["RECEPTNO"]]["CLUB_CD"];
                    $row["HONOR_REMARK"]    = $model->arrInput[$row["RECEPTNO"]]["HONOR_REMARK"];
                }

                //クラブ名コンボ
                if ($row["CLUB_FLG1"] == "1" || $row["CLUB_FLG2"] == "1" || $row["CLUB_FLG3"] == "1") {
                    $extra = "style=\"width: 95%;\" onChange=\"Setflg(this, '{$row["RECEPTNO"]}');\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', '{$row["RECEPTNO"]}');\" ";
                    $row["CLUB_CD"] = makeTableCmb($objForm, $arg, $optClub, $defaultClub, "CLUB_CD-".$row["RECEPTNO"], $row["CLUB_CD"], $extra, 1);
                } else {
                    $extra = "style=\"width: 95%;\" onChange=\"Setflg(this, '{$row["RECEPTNO"]}');\" disabled=\"disabled\" ";
                    $ignoreval = "";
                    $row["CLUB_CD"] = makeTableCmb($objForm, $arg, $optClub, $defaultClub, "CLUB_CD-".$row["RECEPTNO"], $ignoreval, $extra, 1);
                }

                //確定特待コンボ
                $extra = "style=\"width: 95%;\" onChange=\"Setflg(this, '{$row["RECEPTNO"]}');\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', '{$row["RECEPTNO"]}');\" ";
                $row["KAKUTEI_TOKUTAI"] = makeTableCmb2($objForm, $optKakuteiTokutai, "KAKUTEI_TOKUTAI-".$row["RECEPTNO"], $row["KAKUTEI_TOKUTAI"], $extra, 1, "BLANK");

                //事前特待コンボ
                $extra = "style=\"width: 95%;\" onChange=\"checkValueHonordiv(this, '{$row["RECEPTNO"]}');Setflg(this, '{$row["RECEPTNO"]}');\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', '{$row["RECEPTNO"]}');\" ";
                $row["JIZEN_TOKUTAI"] = makeTableCmb2($objForm, $optJizenTokutai, "JIZEN_TOKUTAI-".$row["RECEPTNO"], $row["JIZEN_TOKUTAI"], $extra, 1, "BLANK");

                //特待申請コンボ
                $extra = "style=\"width: 95%;\" onChange=\"checkValueHonordiv(this, '{$row["RECEPTNO"]}');Setflg(this, '{$row["RECEPTNO"]}');\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', '{$row["RECEPTNO"]}');\" ";
                $row["TOKUTAI_SINSEI"] = makeTableCmb2($objForm, $optTokutaiSinsei, "TOKUTAI_SINSEI-".$row["RECEPTNO"], $row["TOKUTAI_SINSEI"], $extra, 1, "BLANK");

                //資格活用コンボ
                $extra = "style=\"width: 95%;\" onChange=\"checkValueHonordiv(this, '{$row["RECEPTNO"]}');Setflg(this, '{$row["RECEPTNO"]}');\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', '{$row["RECEPTNO"]}');\" ";
                $row["SIKAKU_KATSUYO"] = makeTableCmb2($objForm, $optSikakuKatsuyo, "SIKAKU_KATSUYO-".$row["RECEPTNO"], $row["SIKAKU_KATSUYO"], $extra, 1, "BLANK");

                //事前判定コンボ
                $extra = "style=\"width: 95%;\" onChange=\"Setflg(this, '{$row["RECEPTNO"]}');\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', '{$row["RECEPTNO"]}');\" ";
                $row["DESIREDIV"] = makeTableCmb2($objForm, $optJizenHantei, "DESIREDIV-".$row["RECEPTNO"], $row["DESIREDIV"], $extra, 1, "BLANK");

                //事前専願コンボ
                $extra = "style=\"width: 95%;\" onChange=\"Setflg(this, '{$row["RECEPTNO"]}');\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', '{$row["RECEPTNO"]}');\" ";
                $row["JIZEN_SENGAN"] = makeTableCmb2($objForm, $optJizenSengan, "JIZEN_SENGAN-".$row["RECEPTNO"], $row["JIZEN_SENGAN"], $extra, 1, "BLANK");

                //特待備考テキスト
                $extra = "onblur=\"check(this);\" onChange=\"Setflg(this, '{$row["RECEPTNO"]}');\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', '{$row["RECEPTNO"]}');\" ";
                $row["HONOR_REMARK"] = knjCreateTextBox($objForm, $row["HONOR_REMARK"], "HONOR_REMARK-".$row["RECEPTNO"], 10, 20, $extra);

                $arg["data"][] = $row;
                $dataFlg = true;
                $count++;
            }
            $result->free();
        }

        /****************/
        /*  ボタン作成  */
        /****************/
        $disabled = ($dataFlg) ? "" : " disabled";
        //更新
        $extra = "onclick=\"return btn_submit('update');\"".$disabled;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"".$disabled;
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        //CSV出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
        //特待の確定ボタン
        $extra = "onclick=\"return btn_submit('updateTokutaiKakutei');\"".$disabled;
        $arg["btn_update_tokutai_kakutei"] = knjCreateBtn($objForm, "btn_update", "特待の確定", $extra);

        /****************/
        /*  hidden作成  */
        /****************/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_RECEPTNO", "");

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL071A");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "COUNT", $count);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl071aindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl071aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "-- 全て --", "value" => "ALL");
    }
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $default = 0;
    $i = ($blank) ? 1 : 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg && $value != "ALL") {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();

    if ($name == "PASS_DIV" || $name == "UPD_COURSE") {
        $opt[] = array("label" => "未入力", "value" => "NO_DATA");
        if ($value === "NO_DATA") {
            $value_flg = true;
        }
    }

    $value = (strlen($value) && $value_flg) ? $value : $opt[$default]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラブ情報コンボボックス
function getClubCmbList($db, $query, &$default, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $i = $default = 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    return $opt;
}

//特待区分コンボボックス
function getHonordivCmbList($db, $model, $arrType)
{
    $query = knjl071aQuery::getHonordivMst($model, $arrType);
    $result = $db->query($query);
    $opt = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = $row;
    }
    $result->free();

    return $opt;
}

//表内コンボ作成
function makeTableCmb(&$objForm, &$arg, $opt, $default, $name, &$value, $extra, $size)
{
    $value_flg = false;
    foreach ($opt as $row) {
        if ($value == $row["value"]) {
            $value_flg = true;
        }
    }
    $value = (strlen($value) && $value_flg) ? $value : $opt[$default]["value"];
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//表内コンボ作成
function makeTableCmb2(&$objForm, $orgOpt, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $default = 0;

    foreach ($orgOpt as $row) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = (strlen($value) && $value_flg) ? $value : $opt[$default]["value"];
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
