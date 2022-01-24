<?php

require_once('for_php7.php');

class knje390mFamily
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("family", "POST", "knje390mindex.php", "", "family");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        /************/
        /* 履歴一覧 */
        /************/
        $rirekiCnt = makeList($arg, $db, $model);

        /************/
        /* テキスト */
        /************/
        //初期画面または画面サブミット時は、GET取得の変数を初期化する
        if ($model->cmd == "family") {
            unset($model->getYear);
            unset($model->getRelano);
        }
        //家族構成取得
        if ($model->cmd == "family_set") {
            if (isset($model->schregno) && !isset($model->warning)) {
                $Row = $db->getRow(knje390mQuery::getFamilyGetData($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->subfield;
            }
        } else {
            $Row =& $model->subfield;
        }
        
        //氏名
        $extra = "";
        $arg["data"]["RELANAME"] = knjCreateTextBox($objForm, $Row["RELANAME"], "RELANAME", 42, 40, $extra);
        $arg["data"]["RELANAME_COMMENT"] = getTextAreaComment(20, 0);

        //かな氏名
        $extra = "";
        $arg["data"]["RELAKANA"] = knjCreateTextBox($objForm, $Row["RELAKANA"], "RELAKANA", 82, 80, $extra);
        $arg["data"]["RELAKANA_COMMENT"] = getTextAreaComment(40, 0);

        //続柄
        $query = knje390mQuery::getNameMst("H201");
        makeCmb($objForm, $arg, $db, $query, "RELATIONSHIP", $Row["RELATIONSHIP"], "", 1, 1);

        //備考
        $extra = "";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 32, 30, $extra);
        $arg["data"]["REMARK_COMMENT"] = getTextAreaComment(15, 0);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje390mFamily.html", $arg);
    }
}

//履歴一覧
function makeList(&$arg, $db, $model)
{
    $retCnt = 0;
    $query = knje390mQuery::getFamilyRecordList($model);
    $result = $db->query($query);
    $centerName = "";
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $tsuzukigaraName = $db->getOne(knje390mQuery::getTsuzukigaraName($rowlist["RELATIONSHIP"]));
        $rowlist["RELATIONSHIP_NAME"] = $tsuzukigaraName;
        $rowlist["RELABIRTHDAY"] = str_replace("-", "/", $rowlist["RELABIRTHDAY"]);
        $arg["data2"][] = $rowlist;
        $retCnt++;
    }
    $result->free();
    return $retCnt;
}

// テキストボックス文字数
function getTextAreaComment($moji, $gyo)
{
    $comment = "";
    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //追加ボタン
    $extra = "onclick=\"return btn_submit('family_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('family_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('family_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //戻るボタン
    $extra = "onclick=\"return parent.closeit()\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
}
?>

