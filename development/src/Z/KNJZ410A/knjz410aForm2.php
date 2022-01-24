<?php

require_once('for_php7.php');

class knjz410aForm2 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz410aindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (isset($model->school_cd) && !isset($model->warning) && $model->cmd != 'chenge_cd') {
            $Row = knjz410aQuery::getCollegeMst($model);
        }else{
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //学校コード
        $extra = "onblur=\"btn_submit('chenge_cd')\"";
        $arg["data"]["SCHOOL_CD"] = knjCreateTextBox($objForm, $Row["SCHOOL_CD"], "SCHOOL_CD", 8, 8, $extra);

        //学校名
        $extra = "";
        $arg["data"]["SCHOOL_NAME"] = knjCreateTextBox($objForm, $Row["SCHOOL_NAME"], "SCHOOL_NAME", 80, 120, $extra);

        //学校系列コンボボックス
        $query = knjz410aQuery::getSchool_group();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["SCHOOL_GROUP"], "SCHOOL_GROUP", $extra, 1, "BLANK");

        //学部名
        $arg["data"]["BUNAME"] = $Row["BUNAME"];

        //学科名
        $arg["data"]["KANAME"] = $Row["KANAME"];

        //学校分類コンボボックス
        $query = knjz410aQuery::getSchoolcd();
        $extra = "onChange=\"btn_submit('chenge_cd')\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SCHOOL_SORT"], "SCHOOL_SORT", $extra, 1, "BLANK");

        //専門分野コンボボックス
        $query = knjz410aQuery::getBunyaNamecd2($Row["SCHOOL_SORT"]);
        $getBunya = $db->getOne($query);
        $getBunya = $getBunya ? $getBunya : "E009";
        $getBunya = $Row["SCHOOL_SORT"] ? $getBunya : "";
        $query = knjz410aQuery::getBunya($getBunya);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["BUNYA"], "BUNYA", $extra, 1, "BLANK");

        //所在地
        $extra = "";
        $arg["data"]["AREA_NAME"] = knjCreateTextBox($objForm, $Row["AREA_NAME"], "AREA_NAME", 20, 30, $extra);

        //住所
        $query = knjz410aQuery::getCollegeAddrCd($Row);
        $extra = "onChange=\"btn_submit('chenge_cd')\"";
        makeCmb($objForm, $arg, $db, $query, $Row["CAMPUS_ADDR_CD"], "CAMPUS_ADDR_CD", $extra, 1, "BLANK");

        $query = knjz410aQuery::getCollegeCampusAddrDat($Row["SCHOOL_CD"], $Row["CAMPUS_ADDR_CD"]);
        $department_group_mst = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //都道府県
        $arg["data"]["PREF_NAME"] = $department_group_mst["PREF_NAME"];

        //郵便番号
        $arg["data"]["ZIPCD"] = $department_group_mst["ZIPCD"];

        //住所１
        $arg["data"]["ADDR1"] = $department_group_mst["ADDR1"];

        //住所２
        $arg["data"]["ADDR2"] = $department_group_mst["ADDR2"];

        //電話番号
        $arg["data"]["TELNO"] = $department_group_mst["TELNO"];

        //評定基準
        $extra = "";
        $arg["data"]["GREDES"] = knjCreateTextBox($objForm, $Row["GREDES"], "GREDES", 80, 120, $extra);

        //表示用学校名1
        $extra = "";
        $arg["data"]["SCHOOL_NAME_SHOW1"] = knjCreateTextBox($objForm, $Row["SCHOOL_NAME_SHOW1"], "SCHOOL_NAME_SHOW1", 80, 120, $extra);

        //表示用学校名2
        $extra = "";
        $arg["data"]["SCHOOL_NAME_SHOW2"] = knjCreateTextBox($objForm, $Row["SCHOOL_NAME_SHOW2"], "SCHOOL_NAME_SHOW2", 80, 120, $extra);

        //基準点
        $extra = "";
        $arg["data"]["BASE_SCORE"] = knjCreateTextBox($objForm, $Row["BASE_SCORE"], "BASE_SCORE", 20, 20, $extra);

        //必要点
        $extra = "";
        $arg["data"]["NECESSARY_SCORE"] = knjCreateTextBox($objForm, $Row["NECESSARY_SCORE"], "NECESSARY_SCORE", 20, 20, $extra);

        /**********/
        /* ボタン */
        /**********/
        //住所登録
        $link = REQUESTROOT."/Z/KNJZ410A/knjz410aindex.php?cmd=add_addr";
        $extra = "onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["btn_add_addr"] = knjCreateBtn($objForm, "btn_add_addr", "住所登録", $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HIDDEN_SCHOOL_CD", $Row["SCHOOL_CD"]);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit" && $model->cmd != 'chenge_cd') {
            $arg["reload"]  = "parent.left_frame.location.href='knjz410aindex.php?cmd=list';";
        }

        Query::dbCheckIn($db);

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz410aForm2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
