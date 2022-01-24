<?php

require_once('for_php7.php');

class knjz093Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz093index.php", "", "edit");

        //DB接続
        $db  = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //新規
        if ($model->cmd == "new") {
            unset($model->finschoolcd);
            unset($model->field);
        }

        //警告メッセージを表示しない場合
        $model->iinkai = "";
        if (!isset($model->warning) && isset($model->finschoolcd) && ($model->cmd != "search")) {
            $query = knjz093Query::getSchoolData($model->finschoolcd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else if ($model->cmd == "search") {
            //コードのゼロ埋め
            if (strlen($model->field["FINSCHOOLCD"]) < $model->finschoolcdKeta){
                $model->field["FINSCHOOLCD"] = sprintf("%0".$model->finschoolcdKeta."d", $model->field["FINSCHOOLCD"]);
            }
            //出身学校マスタ存在チェック
            $query = knjz093Query::getSchoolData($model->field["FINSCHOOLCD"]);
            $check = $db->getRow($query);
            if (is_array($check)){
                $model->setWarning("この学校コードは既に登録されています。");
                $Row["FINSCHOOLCD"] = $model->field["FINSCHOOLCD"];
            } else {
                //教育委員会から表示
                $query = knjz093Query::getSchoolData($model->field["FINSCHOOLCD"]);
                $Row = $db2->getRow($query, DB_FETCHMODE_ASSOC);
                if ($Row["FINSCHOOLCD"]) {
                    $model->finschoolcd = $Row["FINSCHOOLCD"];
                    $model->iinkai = 1;
                } else {
                    $model->setWarning("MSG303", "　　（教育委員会）");
                    $Row["FINSCHOOLCD"] = $model->field["FINSCHOOLCD"];
                }
            }
        } else {
            $Row =& $model->field;
        }

        //教育委員会用学校コード取得
        $query = knjz093Query::getEdboardSchoolcd();
        $model->edboard_schoolcd = $db->getOne($query);

        //学校コード
        $extra = ($model->finschoolcd) ? " readonly style=\"background-color:lightgray;\"" : "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FINSCHOOLCD"], "FINSCHOOLCD", $model->finschoolcdKeta, $model->finschoolcdKeta, $extra);

        //校種コンボ
        $query = knjz093Query::getNameMst('L019');
        makeCmb($objForm, $arg, $db, $query, "FINSCHOOL_TYPE", $Row["FINSCHOOL_TYPE"], "", 1);

        //学校立コードコンボ
        $query = knjz093Query::getNameMst('L001');
        makeCmb($objForm, $arg, $db, $query, "FINSCHOOL_DISTCD", $Row["FINSCHOOL_DISTCD"], "", 1);

        //学区コードコンボ
        $query = knjz093Query::getNameMst('Z015');
        makeCmb($objForm, $arg, $db, $query, "FINSCHOOL_DISTCD2", $Row["FINSCHOOL_DISTCD2"], "", 1);

        //学校種別コンボ
        $query = knjz093Query::getNameMst('L015');
        makeCmb($objForm, $arg, $db, $query, "FINSCHOOL_DIV", $Row["FINSCHOOL_DIV"], "", 1);

        //学校名
        $arg["data"]["FINSCHOOL_NAME"] = knjCreateTextBox($objForm, $Row["FINSCHOOL_NAME"], "FINSCHOOL_NAME", 50, 50, "");

        //学校名かな
        $arg["data"]["FINSCHOOL_KANA"] = knjCreateTextBox($objForm, $Row["FINSCHOOL_KANA"], "FINSCHOOL_KANA", 50, 50, "");

        //学校名略称
        $arg["data"]["FINSCHOOL_NAME_ABBV"] = knjCreateTextBox($objForm, $Row["FINSCHOOL_NAME_ABBV"], "FINSCHOOL_NAME_ABBV", 20, 20, "");

        //学校名かな略称
        $arg["data"]["FINSCHOOL_KANA_ABBV"] = knjCreateTextBox($objForm, $Row["FINSCHOOL_KANA_ABBV"], "FINSCHOOL_KANA_ABBV", 50, 50, "");

        //学校長氏名
        $arg["data"]["PRINCNAME"] = knjCreateTextBox($objForm, $Row["PRINCNAME"], "PRINCNAME", 40, 40, "");

        //学校長氏名表示用
        $arg["data"]["PRINCNAME_SHOW"] = knjCreateTextBox($objForm, $Row["PRINCNAME_SHOW"], "PRINCNAME_SHOW", 20, 20, "");

        //学校長氏名かな
        $arg["data"]["PRINCKANA"] = knjCreateTextBox($objForm, $Row["PRINCKANA"], "PRINCKANA", 80, 80, "");

        //地区コードコンボ
        $query = knjz093Query::getNameMst('Z003');
        makeCmb($objForm, $arg, $db, $query, "DISTRICTCD", $Row["DISTRICTCD"], "", 1);

        //都道府県コンボ
        $query = knjz093Query::getPrefMst();
        makeCmb($objForm, $arg, $db, $query, "FINSCHOOL_PREF_CD", $Row["FINSCHOOL_PREF_CD"], "", 1);

        //郵便番号
        $arg["data"]["FINSCHOOL_ZIPCD"] = View::popUpZipCode($objForm, "FINSCHOOL_ZIPCD", $Row["FINSCHOOL_ZIPCD"], "FINSCHOOL_ADDR1");

        //住所１
        $arg["data"]["FINSCHOOL_ADDR1"] = knjCreateTextBox($objForm, $Row["FINSCHOOL_ADDR1"], "FINSCHOOL_ADDR1", 50, 90, "");

        //住所２
        $arg["data"]["FINSCHOOL_ADDR2"] = knjCreateTextBox($objForm, $Row["FINSCHOOL_ADDR2"], "FINSCHOOL_ADDR2", 50, 90, "");

        //電話番号
        $arg["data"]["FINSCHOOL_TELNO"] = knjCreateTextBox($objForm, $Row["FINSCHOOL_TELNO"], "FINSCHOOL_TELNO", 14, 14, "");

        //FAX番号
        $arg["data"]["FINSCHOOL_FAXNO"] = knjCreateTextBox($objForm, $Row["FINSCHOOL_FAXNO"], "FINSCHOOL_FAXNO", 14, 14, "");

        //教育委員会コードコンボ
        $query = knjz093Query::getEdboardMst();
        makeCmb($objForm, $arg, $db, $query, "EDBOARDCD", $Row["EDBOARDCD"], "", 1);

        //所在地コンボ
        $query = knjz093Query::getDistrictName();
        makeCmb($objForm, $arg, $db, $query, "DISTRICT_NAME", $Row["DISTRICT_NAME"], "", 1);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $Row);

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz093index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz093Form2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $opt[] = array('label' => '', 'value' => '');
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = (($value === '0' || $value) && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {

    //ボタン表示
    if ($model->iinkai) {
        $arg["insert"] = 1;
    } else if ($model->finschoolcd) {
        $arg["update"] = 1;
    } else {
        $arg["insert"] = 1;
    }

    $disable = ($model->auth == DEF_UPDATABLE) ? "" : " disabled";

    //新規ボタン
    $extra = "onclick=\"return btn_submit('new');\"";
    $arg["button"]["btn_new"] = knjCreateBtn($objForm, "btn_new", "新 規", $extra);

    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra.$disable);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disable);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra.$disable);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra.$disable);

    //終了ボタン
    if ($model->sendSubmit == "1") {
        $link = REQUESTROOT."/Z/KNJZ093A/knjz093aindex.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
        $name = "戻 る";
    } else {
        $extra = "onclick=\"return closeWin();\"";
        $name = "終 了";
    }
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", $name, $extra);
}

//hidden作成
function makeHidden(&$objForm, $Row) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
    knjCreateHidden($objForm, "cmd");
}
?>
