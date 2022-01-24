<?php

require_once('for_php7.php');

class knjh111aform2
{
    public function main(&$model)
    {
        $objForm = new form();
        //DB接続
        $db = Query::dbCheckOut();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh111aindex.php", "", "edit");

        if (isset($model->schregno) && isset($model->regddate) && !isset($model->warning) && $model->cmd != 'qualifiedCd' && $model->cmd != 'conditionDiv') {
            if (VARS::get("MANAGEMENT_FLG") == "1" || $model->seq == "00") {
                $query = knjh111aQuery::getSchregQualifiedTestDat($model);
            } else {
                $query = knjh111aQuery::getSchregQualifiedHobbyDat($model);
            }
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //登録日付
        $Row["REGDDATE"] = $Row["REGDDATE"] ? $Row["REGDDATE"] : CTRL_DATE;
        $date_ymd = strtr($Row["REGDDATE"], "-", "/");
        $arg["data"]["REGDDATE"] = View::popUpCalendar($objForm, "REGDDATE", $date_ymd);

        //学校校種取得 ※教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query = knjh111aQuery::getSchoolKind($model);
            $model->schoolkind = $db->getOne($query);
        }

        /**********/
        /* ラジオ */
        /**********/
        //設定区分
        $opt = array(1, 2, 3); //1:国家資格 2:公的資格 3:民間資格
        if ($Row["CONDITION_DIV"] == "") {
            $Row["CONDITION_DIV"] = $model->Properties["qualifiedMstConditionDivDefault"] ? $model->Properties["qualifiedMstConditionDivDefault"] : "3";
        }
        $extra = array("id=\"CONDITION_DIV1\" onClick=\"btn_submit('conditionDiv')\"", "id=\"CONDITION_DIV2\" onClick=\"btn_submit('conditionDiv')\"", "id=\"CONDITION_DIV3\" onClick=\"btn_submit('conditionDiv')\"");
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
        $extra = "onChange=\"btn_submit('qualifiedCd')\"";
        $arg["data"]["QUALIFIED_CD"] = knjCreateCombo($objForm, "QUALIFIED_CD", $Row["QUALIFIED_CD"], $opt, $extra, 1);

        //備考入力不可
        $model->managementFlg = $remarkDisabled = "";
        if ($model->Properties["useQualifiedManagementFlg"] == '1') {
            $model->managementFlg = $db->getOne(knjh111aQuery::getQualifiedMst_MFlg($Row));
            $remarkDisabled = ($model->managementFlg == "1") ? " disabled": "";
        }

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
        //資格内容
        $extra = "";
        $arg["data"]["CONTENTS"] = $Row["CONTENTS"];
        //備考
        $extra = "".$remarkDisabled;
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 60, 30, $extra);

        /**********/
        /* ボタン */
        /**********/
        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //修正
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //クリア
        $extra = "onclick=\"return Btn_reset('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        //一括更新1
        $link = REQUESTROOT."/H/KNJH111A/knjh111aindex.php?cmd=replace1&SCHREGNO=".$model->schregno;
        $extra = "onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["btn_replace1"] = knjCreateBtn($objForm, "btn_replace1", "一括更新", $extra);
        //ＣＳＶ処理
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_H111A/knjx_h111aindex.php?program_id=".PROGRAMID."&SEND_PRGID=KNJH111A&SEND_AUTH=".AUTHORITY."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ処理", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        //回数
        knjCreateHidden($objForm, "SEQ", $Row["SEQ"]);
        //更新日
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
        //学籍番号
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
                $arg["reload"]  = "window.open('knjh111aindex.php?cmd=list&SCHREGNO={$model->schregno}','right_frame');";
        }

        View::toHTML($model, "knjh111aForm2.html", $arg);
    }
}
