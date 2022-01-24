<?php

require_once('for_php7.php');

class knje390mSubMaster
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("submaster", "POST", "knje390mindex.php", "", "submaster");

        //DB接続
        $db = Query::dbCheckOut();

        //マスタ情報
        if ($model->cmd === 'challenged_master') {
            $arg["CHALLENGED_NAME"] = '1';
            $setTitle = '障害名・診断名一覧';
        } elseif ($model->cmd === 'challenged_training_master') {
            $arg["NAME_ONLY"] = '1';
            $setTitle = '療育施設等の訓練内容一覧';
        } elseif ($model->cmd === 'team_member_master') {
            $arg["NAME_ONLY"] = '1';
            $setTitle = '構成員一覧';
        } elseif ($model->cmd === 'aftertime_need_service_master') {
            $arg["NAME_ONLY"] = '1';
            $setTitle = '福祉の将来必要となると考えられるサービス一覧';
        } elseif ($model->cmd === 'medical_care_master') {
            $arg["NAME_ONLY_3"] = '1';
            $setTitle = '健康管理の医療的ケア名一覧';
        } elseif ($model->cmd === 'medical_center') {
            $arg["NAME_ONLY"] = '1';
            $setTitle = '医療機関';
        } elseif ($model->cmd === 'checkname_master') {
            $arg["NAME_CODE"] = '1';
            $setTitle = '検査名';
        }
        $arg["TITLE"] = $setTitle;

        $rirekiCnt = makeList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje390mSubMaster.html", $arg);
    }
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model)
{
    $i = 0;
    $query = knje390mQuery::getMasterList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        //選択チェックボックス
        $check = "";
        $objForm->ae(array("type"       => "checkbox",
                           "name"       => "CHECK",
                           "value"      => $row["NAME"],
                           "extrahtml"  => $check,
                           "multiple"   => "1" ));
        $row["CHECK"] = $objForm->ge("CHECK");
        //福祉の将来必要と考えられるサービスの時、3箇所選択チェックボックス
        if ($model->cmd === 'medical_care_master') {
            $check2 = "";
            $objForm->ae(array("type"       => "checkbox",
                               "name"       => "CHECK2",
                               "value"      => $row["NAME"],
                               "extrahtml"  => $check2,
                               "multiple"   => "1" ));
            $row["CHECK2"] = $objForm->ge("CHECK2");
            $check3 = "";
            $objForm->ae(array("type"       => "checkbox",
                               "name"       => "CHECK3",
                               "value"      => $row["NAME"],
                               "extrahtml"  => $check3,
                               "multiple"   => "1" ));
            $row["CHECK3"] = $objForm->ge("CHECK3");
        }
        
        $arg["data"][] = $row;
        $i++;
    }
    $result->free();
    //選択ボタン
    $extra = "onclick=\"return btn_submit('".$i."')\"";
    $arg["button"]["btn_sentaku"] = knjCreateBtn($objForm, "btn_sentaku", "選 択", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //戻るボタン
    $extra = "onclick=\"return parent.closeit()\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "GET_CMD", $model->cmd);
}
?>

