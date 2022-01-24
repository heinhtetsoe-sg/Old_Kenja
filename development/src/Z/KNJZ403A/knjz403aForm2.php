<?php

require_once('for_php7.php');


class knjz403aForm2{

    function main(&$model){
    
        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz403aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $query = knjz403aQuery::getSelectOne($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row["GRADE"] = $Row["SCHOOL_KIND"]."-".$Row["GRADE"];

            $query = knjz403aQuery::getSelectMmst($model);
            $result = $db->query($query);
            $mMstArray = array();
            $mCnt = 1;
            while ($rowMmst = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $mMstArray[$mCnt]["M_CD"] = $rowMmst["M_CD"];
                $mMstArray[$mCnt]["M_NAME"] = $rowMmst["M_NAME"];
                $mCnt++;
            }
            $result->free();

        } else {
            $Row =& $model->field;
            $mMstArray = $model->mMstArray;
            $mCnt = get_count($mMstArray) + 1;
        }

        //makeCmb2
        $query = knjz403aQuery::getGrade($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["GRADE"], "GRADE", $extra, 1, "BLANK");

        //行動の記録コード
        $extra = "STYLE=\"text-align: right\" ";
        $arg["data"]["L_CD"] = knjCreateTextBox($objForm, $Row["L_CD"], "L_CD", 2, 2, $extra);

        //行動の記録名称
        $extra = "";
        $arg["data"]["L_NAME"] = knjCreateTextBox($objForm, $Row["L_NAME"], "L_NAME", 20, 30, $extra);

        $mCnt = 1;
        foreach ($mMstArray as $soeji => $mArray) {
            $mKey = $mArray["M_CD"];
            $mVal = $mArray["M_NAME"];
            if ($mKey == "" && $mVal == "") {
                continue;
            }
            $extra = "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
            $setMdata["M_CD"] = knjCreateTextBox($objForm, $mKey, "M_CD".$mCnt, 2, 2, $extra);

            $extra = "";
            $setMdata["M_NAME"] = knjCreateTextBox($objForm, $mVal, "M_NAME".$mCnt, 70, 100, $extra);

            $arg["mdata"][] = $setMdata;
            $mCnt++;
        }
        $extra = "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $setMdata["M_CD"] = knjCreateTextBox($objForm, "", "M_CD".$mCnt, 2, 2, $extra);

        $extra = "";
        $setMdata["M_NAME"] = knjCreateTextBox($objForm, "", "M_NAME".$mCnt, 70, 100, $extra);
        $arg["mdata"][] = $setMdata;
        //hidden
        knjCreateHidden($objForm, "mCnt", $mCnt);

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
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"return closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.reload();";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz403aForm2.html", $arg); 
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
