<?php

require_once('for_php7.php');

class knje390SubForm3_5
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform3_5", "POST", "knje390index.php", "", "subform3_5");

        //DB接続
        $db = Query::dbCheckOut();

        //表示日付をセット
        if ($model->record_date === 'NEW' && $model->main_year === CTRL_YEAR) {
            $setHyoujiDate = '';
        } else {
            if ($model->record_date === 'NEW') {
                $setHyoujiDate = '　　<font color="RED"><B>'.$model->main_year.'年度 最終更新データ 参照中</B></font>';
            } else {
                $setHyoujiDate = '　　<font color="RED"><B>'.str_replace("-", "/", $model->record_date).' 履歴データ 参照中</B></font>';
            }
        }

        //生徒情報
        $info = $db->getRow(knje390Query::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"].$setHyoujiDate;

        /************/
        /* テキスト */
        /************/
        //家庭地域生活取得
        if (isset($model->schregno) && !isset($model->warning)){
            $Row = $db->getRow(knje390Query::getSubQuery3CommunityGetData($model), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field3;
        }
        
        //家庭
        //移動手段
        $extra = "style=\"height:75px; overflow:auto;\"";
        $arg["data"]["MOVE_WAY_PRESENT"] = knjCreateTextArea($objForm, "MOVE_WAY_PRESENT", 5, 27, "soft", $extra, $Row["MOVE_WAY_PRESENT"]);
        $arg["data"]["MOVE_WAY_GOAL"] = knjCreateTextArea($objForm, "MOVE_WAY_GOAL", 5, 21, "soft", $extra, $Row["MOVE_WAY_GOAL"]);
        $arg["data"]["MOVE_WAY_MEANS"] = knjCreateTextArea($objForm, "MOVE_WAY_MEANS", 5, 21, "soft", $extra, $Row["MOVE_WAY_MEANS"]);
        //余暇
        $arg["data"]["LEISURE_TIME_PRESENT"] = knjCreateTextArea($objForm, "LEISURE_TIME_PRESENT", 5, 27, "soft", $extra, $Row["LEISURE_TIME_PRESENT"]);
        $arg["data"]["LEISURE_TIME_GOAL"] = knjCreateTextArea($objForm, "LEISURE_TIME_GOAL", 5, 21, "soft", $extra, $Row["LEISURE_TIME_GOAL"]);
        $arg["data"]["LEISURE_TIME_MEANS"] = knjCreateTextArea($objForm, "LEISURE_TIME_MEANS", 5, 21, "soft", $extra, $Row["LEISURE_TIME_MEANS"]);

        //地域
        //地域行事
        $arg["data"]["COMMUNITY_EVENTS_PRESENT"] = knjCreateTextArea($objForm, "COMMUNITY_EVENTS_PRESENT", 5, 27, "soft", $extra, $Row["COMMUNITY_EVENTS_PRESENT"]);
        $arg["data"]["COMMUNITY_EVENTS_GOAL"] = knjCreateTextArea($objForm, "COMMUNITY_EVENTS_GOAL", 5, 21, "soft", $extra, $Row["COMMUNITY_EVENTS_GOAL"]);
        $arg["data"]["COMMUNITY_EVENTS_MEANS"] = knjCreateTextArea($objForm, "COMMUNITY_EVENTS_MEANS", 5, 21, "soft", $extra, $Row["COMMUNITY_EVENTS_MEANS"]);
        //子供会行事
        $arg["data"]["CHILDREN_EVENTS_PRESENT"] = knjCreateTextArea($objForm, "CHILDREN_EVENTS_PRESENT", 5, 27, "soft", $extra, $Row["CHILDREN_EVENTS_PRESENT"]);
        $arg["data"]["CHILDREN_EVENTS_GOAL"] = knjCreateTextArea($objForm, "CHILDREN_EVENTS_GOAL", 5, 21, "soft", $extra, $Row["CHILDREN_EVENTS_GOAL"]);
        $arg["data"]["CHILDREN_EVENTS_MEANS"] = knjCreateTextArea($objForm, "CHILDREN_EVENTS_MEANS", 5, 21, "soft", $extra, $Row["CHILDREN_EVENTS_MEANS"]);
        //その他
        $arg["data"]["OTHER_PRESENT"] = knjCreateTextArea($objForm, "OTHER_PRESENT", 5, 27, "soft", $extra, $Row["OTHER_PRESENT"]);
        $arg["data"]["OTHER_GOAL"] = knjCreateTextArea($objForm, "OTHER_GOAL", 5, 21, "soft", $extra, $Row["OTHER_GOAL"]);
        $arg["data"]["OTHER_MEANS"] = knjCreateTextArea($objForm, "OTHER_MEANS", 5, 21, "soft", $extra, $Row["OTHER_MEANS"]);
        
        //居住他校交流
        //小学校
        $arg["data"]["RESIDENCE_P_SCHOOL_PRESENT"] = knjCreateTextArea($objForm, "RESIDENCE_P_SCHOOL_PRESENT", 5, 27, "soft", $extra, $Row["RESIDENCE_P_SCHOOL_PRESENT"]);
        $arg["data"]["RESIDENCE_P_SCHOOL_GOAL"] = knjCreateTextArea($objForm, "RESIDENCE_P_SCHOOL_GOAL", 5, 21, "soft", $extra, $Row["RESIDENCE_P_SCHOOL_GOAL"]);
        $arg["data"]["RESIDENCE_P_SCHOOL_MEANS"] = knjCreateTextArea($objForm, "RESIDENCE_P_SCHOOL_MEANS", 5, 21, "soft", $extra, $Row["RESIDENCE_P_SCHOOL_MEANS"]);
        //中学校
        $arg["data"]["RESIDENCE_J_SCHOOL_PRESENT"] = knjCreateTextArea($objForm, "RESIDENCE_J_SCHOOL_PRESENT", 5, 27, "soft", $extra, $Row["RESIDENCE_J_SCHOOL_PRESENT"]);
        $arg["data"]["RESIDENCE_J_SCHOOL_GOAL"] = knjCreateTextArea($objForm, "RESIDENCE_J_SCHOOL_GOAL", 5, 21, "soft", $extra, $Row["RESIDENCE_J_SCHOOL_GOAL"]);
        $arg["data"]["RESIDENCE_J_SCHOOL_MEANS"] = knjCreateTextArea($objForm, "RESIDENCE_J_SCHOOL_MEANS", 5, 21, "soft", $extra, $Row["RESIDENCE_J_SCHOOL_MEANS"]);
        
        $arg["data"]["PRESENT_SIZE"] = '<font size="1" color="red">(全角13文字5行まで)</font>';
        $arg["data"]["GOAL_MEANS_SIZE"] = '<font size="1" color="red">(全角10文字5行まで)</font>';

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje390SubForm3_5.html", $arg); 
    }
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
    //更新ボタン
    $extra = "onclick=\"return btn_submit('community3_updatemain');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('subform3A');\"");
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
}
?>

