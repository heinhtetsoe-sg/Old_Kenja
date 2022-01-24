<?php
class knje371bSubForm_Qualified
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knje371bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度
        $arg["YEAR_SHOW"] = $model->year.'年度';

        //学校名
        $schoolAllName = knje371bQuery::getSchoolAllName($db, $model);
        $arg["SCHOOLNAME_SHOW"] = (strlen(trim($schoolAllName)) > 0) ? "学校名:".$schoolAllName : "";

        /***一覧リスト***/
        $query = knje371bQuery::getList4($model, "list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //取得有効開始日付リンク
            $aHash = array("cmd"            => "subQualified_edit",
                           "QUALIFIED_CD"      => $row["QUALIFIED_CD"]);
            $row["VALID_S_DATE"] = View::alink("knje371bindex.php", str_replace("-", "/", $row["VALID_S_DATE"]), "", $aHash);

            //区分名称
            $row["CONDITION_DIV"] = ($row["CONDITION_DIV"] == "1") ? "国家資格" : (($row["CONDITION_DIV"] == "2") ? "公的資格(検定等)" : "民間資格(TOEIC等)");

            //得点
            $row["CONDITION_SCORE"] = ($row["CONDITION_SCORE"] != "") ? $row["CONDITION_SCORE"]." 以上" : "";

            //要件チェック
            $row["COURSE_CONDITION_FLG"]    = ($row["COURSE_CONDITION_FLG"] == "1") ? "レ" : "";
            $row["SUBCLASS_CONDITION_FLG"]  = ($row["SUBCLASS_CONDITION_FLG"] == "1") ? "レ" : "";
            $row["SUBCLASS_NUM"]            = ($row["SUBCLASS_NUM"] >= 2) ? "{$row["SUBCLASS_NUM"]}科目" : "1科目"; 
            $row["QUALIFIED_CONDITION_FLG"] = ($row["QUALIFIED_CONDITION_FLG"] == "1") ? "レ" : "";

            $arg["list"][] = $row;
        }

        /***入力欄***/
        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->cmd != "subQualified_chg") {
            $Row = $db->getRow(knje371bQuery::getList4($model, "link"), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //取得有効開始日
        $arg["data"]["VALID_S_DATE"] = View::popUpCalendar($objForm, "VALID_S_DATE", str_replace("-", "/", $Row["VALID_S_DATE"]));

        //設定区分
        $opt = array(1, 2, 3); //1:国家資格 2:公的資格 3:民間資格
        $Row["CONDITION_DIV"] = ($Row["CONDITION_DIV"] == "") ? "1" : $Row["CONDITION_DIV"];
        $extra = array("id=\"CONDITION_DIV1\" onClick=\"btn_submit('subQualified_chg')\"", "id=\"CONDITION_DIV2\" onClick=\"btn_submit('subQualified_chg')\"", "id=\"CONDITION_DIV3\" onClick=\"btn_submit('subQualified_chg')\"");
        $radioArray = knjCreateRadio($objForm, "CONDITION_DIV", $Row["CONDITION_DIV"], $extra, $opt, count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //資格名称
        $opt = array();
        $value_flg = false;
        $query = knje371bQuery::getQualifiedMst($Row);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["QUALIFIED_CD"] == $row["VALUE"]) $value_flg = true;
        }
        $Row["QUALIFIED_CD"] = ($Row["QUALIFIED_CD"] && $value_flg) ? $Row["QUALIFIED_CD"] : $opt[0]["value"];
        $extra = "onChange=\"btn_submit('subQualified_chg')\"";
        $arg["data"]["QUALIFIED_CD"] = knjCreateCombo($objForm, "QUALIFIED_CD", $Row["QUALIFIED_CD"], $opt, $extra, 1);

        $model->managementFlg = "";
        if ($model->Properties["useQualifiedManagementFlg"] == '1') {
            $model->managementFlg = $db->getOne(knje371bQuery::getQualifiedMst_MFlg($Row));
        }

        //級・段位
        $opt = array();
        $opt[] = array(label => '', value => '');
        $value_flg = false;
        if ($model->managementFlg == "1") {
            $query = knje371bQuery::getRankResultMst($Row["QUALIFIED_CD"], $model);
        } else {
            $query = knje371bQuery::getSelectedRank($Row["QUALIFIED_CD"], $model);
            $ret = $db->getOne($query);
            if (!isset($ret)) {
                $query = knje371bQuery::getRank();
            }
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["CONDITION_RANK"] == $row["VALUE"]) $value_flg = true;
        }
        $Row["CONDITION_RANK"] = ($Row["CONDITION_RANK"] && $value_flg) ? $Row["CONDITION_RANK"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["CONDITION_RANK"] = knjCreateCombo($objForm, "CONDITION_RANK", $Row["CONDITION_RANK"], $opt, $extra, 1);

        //得点
        $extra = "";
        $arg["data"]["CONDITION_SCORE"] = knjCreateTextBox($objForm, $Row["CONDITION_SCORE"], "CONDITION_SCORE", 3, 3, $extra);

        //主催
        $query = knje371bQuery::getPromoter($Row);
        $promoter = $db->getOne($query);
        $arg["data"]["PROMOTER"] = $promoter;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        $arg["IFRAME"] = View::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knje371bSubForm_Qualified.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="")
{
    $opt = array();
    if($space) $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{

    //追加ボタンを作成する
    $extra = "onclick=\"return btn_submit('subQualified_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('subQualified_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタンを作成する
    $extra = "onclick=\"return btn_submit('subQualified_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //取消しボタンを作成する
    $extra = "onclick=\"return btn_submit('subQualified_reset')\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタンを作成する
    $extra = "onclick=\"return btn_submit('main_edit')\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJE371B");
    //キーを保持
    knjCreateHidden($objForm, "SCHOOL_CD"   , $model->schoolCd);
    knjCreateHidden($objForm, "FACULTYCD"   , $model->facultyCd);
    knjCreateHidden($objForm, "DEPARTMENTCD", $model->departmentCd);

}

?>
