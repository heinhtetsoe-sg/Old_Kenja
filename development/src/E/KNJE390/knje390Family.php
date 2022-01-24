<?php

require_once('for_php7.php');

class knje390Family
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("family", "POST", "knje390index.php", "", "family");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $info = $db->getRow(knje390Query::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];
        
        // Add by PP for Title 2020-02-03 start
        $arg["TITLE"] = $info["NAME_SHOW"]."の家族構成画面";
        echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        // Add by PP for Title 2020-02-20 end

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
        if ($model->cmd == "family_set"){
            if (isset($model->schregno) && !isset($model->warning)){
                $Row = $db->getRow(knje390Query::getFamilyGetData($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->subfield;
            }
        } else {
            $Row =& $model->subfield;
        }
        
        //氏名
        // Edit by PP for PC-Talker 2020-02-03 start
        $extra = "id=\"RELANAME\" aria-label=\"氏名\"";
        $arg["data"]["RELANAME"] = knjCreateTextBox($objForm, $Row["RELANAME"], "RELANAME", 40, 40, $extra);
        // Edit by PP for PC-Talker 2020-02-20 end
        
        //かな氏名
        // Edit by PP for PC-Talker 2020-02-03 start
        $extra = "id=\"RELAKANA\" aria-label=\"氏名かな\"";
        $arg["data"]["RELAKANA"] = knjCreateTextBox($objForm, $Row["RELAKANA"], "RELAKANA", 40, 80, $extra);
        // Edit by PP for PC-Talker 2020-02-20 end

        //続柄
        // Edit by PP for PC-Talker 2020-02-03 start
        $query = knje390Query::getNameMst("H201");
        makeCmb($objForm, $arg, $db, $query, "RELATIONSHIP", $Row["RELATIONSHIP"], "aria-label=\"続柄\"", 1, 1);
        // Edit by PP for PC-Talker 2020-02-20 end

        //備考
        // Edit by PP for PC-Talker 2020-02-03 start
        $extra = "aria-label=\"備考\"";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 40, 40, $extra);
        // Edit by PP for PC-Talker 2020-02-20 end

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje390Family.html", $arg); 
    }
}

//履歴一覧
function makeList(&$arg, $db, $model) {
    $retCnt = 0;
    $query = knje390Query::getFamilyRecordList($model);
    $result = $db->query($query);
    $centerName = "";
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $tsuzukigaraName = $db->getOne(knje390Query::getTsuzukigaraName($rowlist["RELATIONSHIP"]));
        $rowlist["RELATIONSHIP_NAME"] = $tsuzukigaraName;
        $rowlist["RELABIRTHDAY"] = str_replace("-", "/", $rowlist["RELABIRTHDAY"]);
        $arg["data2"][] = $rowlist;
        $retCnt++;
    }
    $result->free();
    return $retCnt;
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
    //追加ボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_insert\" onclick=\"current_cursor('btn_insert'); return btn_submit('family_insert');\" aria-label='追加'";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //更新ボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_update\" onclick=\"current_cursor('btn_update'); return btn_submit('family_update');\" aria-label='更新'";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //削除ボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_delete\" onclick=\"current_cursor('btn_delete'); return btn_submit('family_delete');\" aria-label='削除'";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //戻るボタン
    // Add by PP for PC-Talker and current cursor in parent page 2020-02-03 start
    $extra = "onclick=\"parent.current_cursor_focus(); return parent.closeit()\" aria-label='戻る'";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
    // Add by PP for PC-Talker and current cursor in parent page 2020-02-20 end
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
}
?>

