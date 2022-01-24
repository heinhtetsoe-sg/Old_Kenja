<?php

require_once('for_php7.php');

class knja110_2bForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knja110_2bindex.php", "", "edit");
        $arg["close"] = "";
        $arg["data"] = array();

        $db = Query::dbCheckOut();

        //起動チェック
        if (!knja110_2bQuery::ChecktoStart($db)) {
            $link = REQUESTROOT."/A/KNJA110B/knja110bindex.php?cmd=edit&schregno=".$model->schregno;
            $arg["close"] = "closing_window('$link');";
        }

        if ($model->cmd == "list2") {
            $link = REQUESTROOT."/A/KNJA110_2B/knja110_2bindex.php?cmd=edit2&schregno=".$model->schregno."&INFO_DIV=".$model->infoDiv;
            $arg["close"] = "openEdit('$link');";
        }

        //学籍基礎マスタより学籍番号と名前を取得
        $Row         = $db->getRow(knja110_2bQuery::getSchregno_name($model),DB_FETCHMODE_ASSOC);
        $arg["NO"]   = $Row["SCHREGNO"];
        $arg["NAME"] = $Row["NAME"];

        //編集対象選択ラジオ
        if ($model->Properties["useGuardian2"] == '1') {
            $arg["useGuardian2"] = "ON";
            $opt = array(1, 2, 3); //1:生徒情報 2:保護者情報 3:保護者情報２
            $model->infoDiv = ($model->infoDiv == "") ? "1" : $model->infoDiv;
            $extra = array("id=\"INFO_DIV1\" onClick=\"btn_submit('list2')\" ", "id=\"INFO_DIV2\" onClick=\"btn_submit('list2')\" ", "id=\"INFO_DIV3\" onClick=\"btn_submit('list2')\" ");
            $radioArray = knjCreateRadio($objForm, "INFO_DIV", $model->infoDiv, $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["top"][$key] = $val;
        } else {
            $arg["useGuardian2"] = "";
            $opt = array(1, 2); //1:生徒情報 2:保護者情報
            $model->infoDiv = ($model->infoDiv == "") ? "1" : $model->infoDiv;
            $extra = array("id=\"INFO_DIV1\" onClick=\"btn_submit('list2')\" ", "id=\"INFO_DIV2\" onClick=\"btn_submit('list2')\" ");
            $radioArray = knjCreateRadio($objForm, "INFO_DIV", $model->infoDiv, $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["top"][$key] = $val;
        }

        if ($model->infoDiv == "2" || $model->infoDiv == "3") {
            $arg["infoDiv1"] = "";
            $arg["infoDiv2"] = "ON";
        } else {
            $arg["infoDiv1"] = "ON";
            $arg["infoDiv2"] = "";
        }

        //学籍住所データよりデータを取得
        if($model->schregno)
        {
           //緊急連絡先情報の取得(2005/10/20 ADD)
            $row2 = $db->getRow(knja110_2bQuery::getEmergencyInfo($model->schregno),DB_FETCHMODE_ASSOC);
            $result = $db->query(knja110_2bQuery::getAddress_all($model));

            while($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $row  = array_merge((array)$row1,(array)$row2);

                $name1 = $db->getOne(knja110_2bQuery::List_AreaCd($row["AREACD"]));
                $row["AREA_CD"]          = $row["AREACD"].":".$name1;
                $row["ISSUEDATE"]        = str_replace("-","/",$row["ISSUEDATE"]);
                $row["EXPIREDATE"]       = str_replace("-","/",$row["EXPIREDATE"]);
                $row["GUARD_ISSUEDATE"]  = str_replace("-","/",$row["GUARD_ISSUEDATE"]);
                $row["GUARD_EXPIREDATE"] = str_replace("-","/",$row["GUARD_EXPIREDATE"]);
                $row["INFO_DIV"]         = $model->infoDiv;
                $row["GUARD_ADDR_FLG"]   = $row["GUARD_ADDR_FLG"] == "1" ? "可" : "";
                $row["ADDR_FLG"]         = $row["ADDR_FLG"] == "1" ? "可" : "";
                $arg["data"][] = $row;
            }

        }
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "clear",
                            "value"     => "0"));

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja110_2bForm1.html", $arg);
    }
}
?>
