<?php

require_once('for_php7.php');

class knjl410_2Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl410_2Form1", "POST", "knjl410_2index.php", "", "knjl410_2Form1");

        //セキュリティーチェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //イベント参加者データ取得
        $info = $db->getRow(knjl410_2Query::getRecruitDat($model), DB_FETCHMODE_ASSOC);

        //ヘッダー情報（年度、管理番号、氏名）
        $arg["HEADER_INFO"] = (CTRL_YEAR + 1).'年度　　'.$info["RECRUIT_NO"].'：'.$info["NAME"];

        //データ一覧取得
        $setval = array();
        $query = knjl410_2Query::getRecruitConsultDat($model);
        if ($model->recruit_no) {
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["TOUROKU_DATE"] = str_replace("-", "/", $row["TOUROKU_DATE"]);
                $arg["data"][] = $row;
            }
        }

        //編集用データ取得
        if (isset($model->recruit_no) && !isset($model->warning) && $model->touroku_date) {
            $Row = $db->getRow(knjl410_2Query::getRecruitConsultDat($model, "1"), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //編集用データ取得(来校者要約)
        if (isset($model->recruit_no) && !isset($model->warning)) {
            $Row["REMARK"] = $db->getOne(knjl410_2Query::getRecruitConsultWrapupDat($model));
        } else {
            $Row["REMARK"] = $model->field["REMARK"];
        }

        //登録日付
        $value = ($Row["TOUROKU_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $Row["TOUROKU_DATE"]);
        $arg["data1"]["TOUROKU_DATE"] = View::popUpCalendar($objForm, "TOUROKU_DATE", $value);

        //相談者
        $query = knjl410_2Query::getNameMst('L404');
        makeCmb($objForm, $arg, $db, $query, "CONSULT_CD", $Row["CONSULT_CD"], "", 1);

        //方法
        $query = knjl410_2Query::getNameMst('L405');
        makeCmb($objForm, $arg, $db, $query, "METHOD_CD", $Row["METHOD_CD"], "", 1);

        //面談者取得・・・登録データがない時、ログイン者
        //$Row["STAFFCD"] = ($Row["STAFFCD"] == "") ? STAFFCD : $Row["STAFFCD"];
        //$staff = $db->getRow(knjl410_2Query::getStaffMst($Row["STAFFCD"]), DB_FETCHMODE_ASSOC);
        //面談者
        //$arg["data1"]["STAFFCD"] = $staff["STAFFCD"].'：'.$staff["STAFFNAME"];
        //knjCreateHidden($objForm, "STAFFCD", $staff["STAFFCD"]);
        $Row["STAFFCD"] = ($Row["STAFFCD"] == "") ? STAFFCD : $Row["STAFFCD"];
        $query = knjl410_2Query::getStaffMst($model);
        makeCmb($objForm, $arg, $db, $query, "STAFFCD", $Row["STAFFCD"], "", 1, "BLANK");

        //相談内容
        $extra = "onkeyup =\"charCount(this.value, 7, (40 * 2), true);\" oncontextmenu =\"charCount(this.value, 7, (40 * 2), true);\"";
        $arg["data1"]["CONTENTS"] = knjCreateTextArea($objForm, "CONTENTS", "7", "80", "wrap", $extra, $Row["CONTENTS"]);
        //$height = 7 * 13.5 + (7 - 1) * 3 + 5;
        /***
        $moji = 40;
        $gyou = 7;
        $height = $gyou * 13.5 + ($gyou -1 ) * 3 + 5;
        $extra = "style=\"height:{$height}px;\"";
        $extra = "style=\"height:145px;\"";
        $arg["data1"]["CONTENTS"] = knjCreateTextArea($objForm, "CONTENTS", $gyou, ($moji * 2 + 1), "soft", $extra, $Row["CONTENTS"]);
        ***/

        //来校者要約
        $extra = "";
        $arg["data1"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 41, 40, $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl410_2Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data1"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新2ボタン
    $extra = "onclick=\"return btn_submit('update2');\"";
    $arg["button"]["btn_update2"] = knjCreateBtn($objForm, "btn_update2", "更 新", $extra);
    //追加ボタン
    $extra = "onclick=\"return btn_submit('insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = " onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $extra = "onclick=\"closeMethod();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
}
?>
