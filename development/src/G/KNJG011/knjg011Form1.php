<?php

require_once('for_php7.php');

class knjg011Form1
{   
    function main(&$model)
    {
        //オブジェクト作成
        $objForm        = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjg011index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["jscript"] = "";
        
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //処理年度
        $arg["data"]["YEAR"] = CTRL_YEAR."年度卒";

        //中高一貫区分
        $div = $db->getOne(knjg011Query::getNameMst());

        //学年コンボ作成
        $query = knjg011Query::getGrade($model, $div);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //校種取得
        $model->schoolKind = $db->getOne(knjg011Query::getSchoolKind($model->field["GRADE"]));

        //証明書種類コンボ作成
        $query = knjg011Query::getCertifKind($model);
        $extra = "";
        if ($model->Properties["use_certif_kind_mst_school_kind"] == "1") {
            $extra = "onchange=\"return btn_submit('main');\"";
        }
        makeCmb($objForm, $arg, $db, $query, "CERTIF_KINDCD", $model->field["CERTIF_KINDCD"], $extra, 1);

        //MAX発行番号テキストボックス
        $model->field["CERTIF_NO_MAX"] = knjg011Query::getCertifNo($db, $model, $model->Properties["certifNoSyudou"]);
        if ($model->field["CERTIF_NO_MAX"] == "") {
            $model->field["CERTIF_NO_MAX"] = "1";
        }
        $extra = "style=\"text-align: right\" onBlur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["CERTIF_NO_MAX"] = knjCreateTextBox($objForm, $model->field["CERTIF_NO_MAX"], "CERTIF_NO_MAX", 8, 8, $extra);

        //最終発行番号コメント
        $certif_no_show = knjg011Query::getCertifNo($db, $model, $model->Properties["certifNoSyudou"]);
        if ($certif_no_show > 0) {
            $certif_no_show -= 1;
        } else if ($certif_no_show == '') {
            $certif_no_show = 0;
        }
        $arg["data"]["CERTIF_NO_SHOW"] = $certif_no_show;
        $arg["data"]["CERTIF_NO_COMMENT"] = ($model->Properties["certifNoSyudou"] == "1") ? "手入力番号" : "自動採番";

        //発行対象人数
        $schnumber = $db->getOne(knjg011Query::getSchregNumber($model, $model->field["GRADE"]));
        $arg["data"]["SCH_CNT_SHOW"] = $schnumber;

        //実行ボタン
        $arg["button"]["BTN_OK"] = KnjCreateBtn($objForm, "btn_ok", "実 行", "onclick=\"return btn_submit('execute');\"");

        //終了ボタン
        $arg["button"]["BTN_CLEAR"] = KnjCreateBtn($objForm, "btn_cancel", "終 了", "onclick=\"closeWin();\"");

        //HIDDEN
        KnjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCH_CNT_CHK", $schnumber);
        knjCreateHidden($objForm, "CERTIF_NO_CHK", $certif_no_show);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjg011Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
