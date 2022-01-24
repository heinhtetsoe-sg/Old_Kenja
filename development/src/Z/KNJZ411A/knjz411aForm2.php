<?php

require_once('for_php7.php');

class knjz411aForm2
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;

        $db = Query::dbCheckOut();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz411aindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (isset($model->school_cd) && !isset($model->warning) && $model->cmd != 'chenge_cd') {
            $query = knjz411aQuery::getCollegeFacultyMst($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        /********************/
        /* テキストボックス */
        /********************/
        //学部コード
        $extra = "";
        $arg["data"]["FACULTYCD"] = knjCreateTextBox($objForm, $Row["FACULTYCD"], "FACULTYCD", 3, 3, $extra);
        //学部名
        $extra = "";
        $arg["data"]["FACULTYNAME"] = knjCreateTextBox($objForm, $Row["FACULTYNAME"], "FACULTYNAME", 40, 30, $extra);
        //学部略称1
        $extra = "";
        $arg["data"]["FACULTYNAME_SHOW1"] = knjCreateTextBox($objForm, $Row["FACULTYNAME_SHOW1"], "FACULTYNAME_SHOW1", 40, 30, $extra);
        //学部略称2
        $extra = "";
        $arg["data"]["FACULTYNAME_SHOW2"] = knjCreateTextBox($objForm, $Row["FACULTYNAME_SHOW2"], "FACULTYNAME_SHOW2", 40, 30, $extra);
        //校内推薦用学部コード
        if ($model->Properties["Internal_Recommendation"] == "1") {
            $extra = " onblur=\"this.value=toInteger(this.value);\" ";
            $arg["data"]["CAMPUS_FACULTYCD"] = knjCreateTextBox($objForm, $Row["CAMPUS_FACULTYCD"], "CAMPUS_FACULTYCD", 2, 2, $extra);
        }

        /******************/
        /* コンボボックス */
        /******************/
        //住所
        $opt = array();
        $opt[] = array("label" => "","value" => "");
        $query = knjz411aQuery::getCollegeAddrCd($model->school_cd);
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["CAMPUS_ADDR_CD"] == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $Row["CAMPUS_ADDR_CD"] = ($Row["CAMPUS_ADDR_CD"] && $value_flg) ? $Row["CAMPUS_ADDR_CD"] : $opt[0]["value"];
        $extra = "onChange=\"btn_submit('chenge_cd')\"";
        $arg["data"]["CAMPUS_ADDR_CD"] = knjCreateCombo($objForm, "CAMPUS_ADDR_CD", $Row["CAMPUS_ADDR_CD"], $opt, $extra, 1);

        /************/
        /* 表示のみ */
        /************/
        $query = knjz411aQuery::getCollegeCampusAddrDat($model->school_cd, $Row["CAMPUS_ADDR_CD"]);
        $addr = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //郵便番号
        $arg["data"]["ZIPCD"] = $addr["ZIPCD"];
        //住所1
        $arg["data"]["ADDR1"] = $addr["ADDR1"];
        //住所2
        $arg["data"]["ADDR2"] = $addr["ADDR2"];
        //電話番号
        $arg["data"]["TELNO"] = $addr["TELNO"];

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
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "tmp_SCHOOL_CD", $model->school_cd);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit" && $model->cmd != "chenge_cd") {
            $arg["reload"]  = "parent.left_frame.location.href='knjz411aindex.php?cmd=list_from_right';";
        }

        Query::dbCheckIn($db);

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz411aForm2.html", $arg);
    }
}
