<?php

require_once('for_php7.php');

class knjh111aSubForm1
{
    public function main(&$model)
    {
        $objForm        = new form();
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjh111aindex.php", "", "sel");

        $db = Query::dbCheckOut();

        $query = knjh111aQuery::getHrclass($model);
        $hr_name = $db->getOne($query);
        $arg["TOP"] =  CTRL_YEAR."年度 {$model->control_data["学期名"][CTRL_SEMESTER]} 対象クラス  {$hr_name}";

        //生徒情報
        if (preg_match('/replace1|replace_qualifiedCd|replace_conditionDiv/', $model->cmd)) {
            $Row =& $model->field;
        } else {
            if ($model->seq == "00") {
                $query = knjh111aQuery::getSchregQualifiedTestDat($model);
            } else {
                $query = knjh111aQuery::getSchregQualifiedHobbyDat($model);
            }
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

/* 編集項目 */
        //登録日付
        $Row["REGDDATE"] = $Row["REGDDATE"] ? $Row["REGDDATE"] : CTRL_DATE;
        $date_ymd = strtr($Row["REGDDATE"], "-", "/");
        $arg["data"]["REGDDATE"] = View::popUpCalendar($objForm, "REGDDATE", $date_ymd);

        /**********/
        /* ラジオ */
        /**********/
        //設定区分
        $opt = array(1, 2, 3); //1:国家資格 2:公的資格 3:民間資格
        $Row["CONDITION_DIV"] = (trim($Row["CONDITION_DIV"]) == "") ? "1" : $Row["CONDITION_DIV"];
        $extra = array("id=\"CONDITION_DIV1\" onClick=\"btn_submit('replace_conditionDiv')\"", "id=\"CONDITION_DIV2\" onClick=\"btn_submit('replace_conditionDiv')\"", "id=\"CONDITION_DIV3\" onClick=\"btn_submit('replace_conditionDiv')\"");
        $radioArray = knjCreateRadio($objForm, "CONDITION_DIV", $Row["CONDITION_DIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        /******************/
        /* コンボボックス */
        /******************/
        //資格名称
        $opt = array();
        $value_flg = false;
        $query = knjh111aQuery::getQualifiedMst($Row);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["QUALIFIED_CD"] == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $Row["QUALIFIED_CD"] = ($Row["QUALIFIED_CD"] && $value_flg) ? $Row["QUALIFIED_CD"] : $opt[0]["value"];
        $extra = "onChange=\"btn_submit('replace_qualifiedCd')\"";
        $arg["data"]["QUALIFIED_CD"] = knjCreateCombo($objForm, "QUALIFIED_CD", $Row["QUALIFIED_CD"], $opt, $extra, 1);

        //備考入力不可
        $model->managementFlg = $remarkDisabled = "";
        if ($model->Properties["useQualifiedManagementFlg"] == '1') {
            $model->managementFlg = $db->getOne(knjh111aQuery::getQualifiedMst_MFlg($Row));
            $remarkDisabled = ($model->managementFlg == "1") ? " disabled": "";
        }
        knjCreateHidden($objForm, "managementFlg", $model->managementFlg);

        //級・段位
        $opt = array();
        $opt[] = array("label" => '', "value" => '');
        $value_flg = false;
        if ($model->managementFlg == "1") {
            $query = knjh111aQuery::getRankResultMst($Row["QUALIFIED_CD"]);
        } else {
            $query = knjh111aQuery::getSelectedRank($Row["QUALIFIED_CD"]);
            $ret = $db->getOne($query);
            if (!isset($ret)) {
                $query = knjh111aQuery::getRank();
            }
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["RANK"] == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $Row["RANK"] = ($Row["RANK"] && $value_flg) ? $Row["RANK"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["RANK"] = knjCreateCombo($objForm, "RANK", $Row["RANK"], $opt, $extra, 1);

        //主催
        $query = knjh111aQuery::getPromoter($Row);
        $promoter = $db->getOne($query);
        $arg["data"]["PROMOTER"] = $promoter;

        //チェックボックス
        for ($i=0; $i<7; $i++) {
            if ($i==6) {
                $extra = "onClick=\"return check_all(this);\"";
                if ($Row["check_all"] == "1") {
                    $extra .= " checked='checked' ";
                } else {
                    $extra .= "";
                }
                $arg["data"]["RCHECK".$i] = knjCreateCheckBox($objForm, "RCHECK".$i, "1", $extra);
            } else {
                if ($Row["check"][$i] == "1") {
                    $extra = "checked='checked' ";
                } else {
                    $extra = "";
                }
                $extra .= ($i==3) ? "".$remarkDisabled: "";
                $arg["data"]["RCHECK".$i] = knjCreateCheckBox($objForm, "RCHECK".$i, "1", $extra);
            }
        }

        /********************/
        /* チェックボックス */
        /********************/
        //資格証書
        $extra = 'id="CERTIFICATE" '.(($Row["CERTIFICATE"] == "1") ? "checked" : "");
        $arg["data"]["CERTIFICATE"]  = knjCreateCheckBox($objForm, "CERTIFICATE", "1", $extra, "");
        /********************/
        /* テキストボックス */
        /********************/
        //得点
        $extra = "";
        $arg["data"]["HOBBY_SCORE"] = knjCreateTextBox($objForm, $Row["HOBBY_SCORE"], "HOBBY_SCORE", 3, 3, $extra);
        //備考
        $extra = "".$remarkDisabled;
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 30, 30, $extra);


        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "更 新", $extra);
        //戻る
        $link = REQUESTROOT."/H/KNJH111A/knjh111aindex.php?cmd=back";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //生徒一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjh111aSubForm1.html", $arg);
    }
}

/******************/
/* リストToリスト */
/******************/
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //生徒一覧
    $selectdata = explode(",", $model->field["selectdata"]);
    if ($selectdata[0]=="") {
        $selectdata[0] = $model->schregno;
    }
    $opt_left = $opt_right = array();
    $query = knjh111aQuery::getStudent($model);
    $result   = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["SCHREGNO"], $selectdata)) {
            $opt_left[]   = array("label" => $row["LABEL"],
                                  "value" => $row["VALUE"]);
        } else {
            $opt_right[]  = array("label" => $row["LABEL"],
                                  "value" => $row["VALUE"]);
        }
    }
    $result->free();

    //生徒一覧作成
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

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
