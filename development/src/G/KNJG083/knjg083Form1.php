<?php

require_once('for_php7.php');

class knjg083Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjg083index.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期名
        $arg["SEMSTER_NAME"] = $db->getOne(knjg083Query::getSemesterName());
        
        //グループコンボ
        $query = knjg083Query::getNameMst();
        $extra = "onchange=\"btn_submit('main')\";";
        makeCmb($objForm, $arg, $db, $query, "GROUP_DIV", $model->field["GROUP_DIV"], $extra, 1, "BLANK");
        
        //年組コンボ作成
        $query = knjg083Query::getGradeHrclass($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "BLANK");

        //初期化
        $model->data = array();
        $counter = 0;

        //一覧表示
        $result = $db->query(knjg083Query::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //出席番号
            if ($row["ATTENDNO"] != ""){
                $row["ATTENDNO"] = sprintf("%01d", $row["ATTENDNO"]).'番';
            }

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }
            
            //全てにチェック
            $extra = " onclick=\"checkAll(this, '{$counter}');\"";
            $row["ALL_CHECK"] = knjCreateCheckBox($objForm, "ALL_CHECK_".$counter, "1", $extra);

            //生徒項目
            for ($i = 2; $i <= 4; $i++) {
                $value = (!isset($model->warning)) ? $row["DIV1_REMARK".$i] : $model->fields["DIV1_REMARK".$i][$counter];
                if ($value == "1") {
                    $extra = "checked='checked' ";
                } else {
                    $extra = "";
                }
                $row["DIV1_REMARK".$i] = knjCreateCheckBox($objForm, "DIV1_REMARK".$i."_".$counter, "1", $extra);
            }
            
            //保護者項目
            for ($i = 1; $i <= 4; $i++) {
                $value = (!isset($model->warning)) ? $row["DIV2_REMARK".$i] : $model->fields["DIV2_REMARK".$i][$counter];
                if ($value == "1") {
                    $extra = "checked='checked' ";
                } else {
                    $extra = "";
                }
                $row["DIV2_REMARK".$i] = knjCreateCheckBox($objForm, "DIV2_REMARK".$i."_".$counter, "1", $extra);
            }

            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;

        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJG083");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjg083Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //更新ボタンを作成する
    $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
