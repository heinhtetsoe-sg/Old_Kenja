<?php

require_once('for_php7.php');


require_once('knjb3042ChairStdQuery.inc');

class knjb3042ChairStd
{
    public function knjb3042ChairStd()
    {
    }
    public function main(&$model)
    {
//         //権限チェック
//         if (AUTHORITY != DEF_UPDATABLE){
//             $arg["jscript"] = "OnAuthError();";
//         }

        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjb3042index.php", "", "chairStd");
//         $arg["start"] = $objForm->get_start("list", "POST", "knjb3042ChairStd.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

//         if ($model->cmd == "getChair") {
//             //講座コンボ
//             $response = makeChairList($objForm, $arg, $db, $model, "returnGe");
//             echo $response;
//             die();
//         }

        //重複講座コンボ
        $query = knjb3042ChairStdQuery::getDupChairDat($model);
        $extra = "onchange=\"return btn_submit('chairStd');\"";
        makeCmb($objForm, $arg, $db, $query, $val, "DUP_CHAIRCD", $extra, 1, "BLANK");

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row['VALUE'] == $model->ciField['CI_CHAIRCD']) {
                $arg['data']['DUP_CHAIR_LIST'] = $row['LABEL'];
                break;
            }
        }

        //適用開始日付


        //上行作成
//         $query = knjb3042ChairStdQuery::getDupChairDat($model);
//         $extra = '';
//         $value=null;
//         $arg["head"]["chairCd1"] = makeCmb($objForm, $arg, $db, $query, "CHAIRCD1", $value, $extra, 1);
//         $arg["head"]["chairCd2"] = makeCmb($objForm, $arg, $db, $query, "CHAIRCD2", $value, $extra, 1);
//         $result = $db->query($query);
//         $cnt = 0;
//         $model->chairJoudan = array();
//         while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
//             $model->chairJoudan[] = $row['VALUE'];
//             $data=array();
//             $data['CHAIRCD'] = $row['VALUE'];
//             $data['CHAIRNAME'] = $row['LABEL'];
//             $data['SUBCLASSCD'] = $row['SUBCLASSCD'];
//             $data['SUBCLASSNAME'] = $row['SUBCLASSNAME'];

//             $staff = array();
//             $result2 = $db->query(knjb3050Query::getMainDataHeadSTAFF($model));
//             while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
//                 $staff[] = $row2['STAFFCD'].' '.$row2['STAFFNAME'];
//             }
//             $data['STAFF'] = implode('<br>', $staff);

//             $value = null;
//             $data['CHAIRLIST'] =  makeCmb2($objForm, $arg, $db, $query, "CHAIRLIST_".$cnt, $value, $extra, 15);
//             knjCreateHidden($objForm, "CHAIRLIST_VALUE_".$cnt);
//             $arg['data'][] = $data;
//             $cnt++;
//         }
//         $result->free();
//         knjCreateHidden($objForm, "CHAIRLIST_MAX_CNT",$cnt);


        //下行作成
        //  現状表示なし

        //ボタン作成
        makeBtn($objForm, $arg, $lcFlag);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb3042ChairStd.html", $arg);
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
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    if ($name == "DUP_CHAIRCD") {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    }

    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $lcFlag)
{

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "cmd2");
    knjCreateHidden($objForm, "SELECT_DATE");
}


// if ($_REQUEST["CHASTD_SESSID"] == "") exit;
// $sess_cha = new APP_Session($_REQUEST["CHASTD_SESSID"], 'knjb3042ChairStd');
// if (!$sess_cha->isCached($_REQUEST["CHASTD_SESSID"], 'knjb3042ChairStd')) {
//     $sess_cha->data = new knjb3042ChairStd();
// }
// $sess_cha->data = new knjb3042ChairStd();
// $sess_cha->data->main($_REQUEST);
