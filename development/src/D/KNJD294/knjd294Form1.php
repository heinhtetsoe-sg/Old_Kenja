<?php

require_once('for_php7.php');

class knjd294Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd294index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        //年組コンボ
        $query = knjd294Query::getHrClass($model);
        $extra = "onchange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->grade_hr_class, "GRADE_HR_CLASS", $extra, 1, "BLANK");

        //ALLチェック
        $extra  = "onClick=\"check_all(this);\"";
        $extra .= " id=\"CHECKALL\"";
        $extra .= (isset($model->warning) && $model->check_all) ? " checked" : "";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "1", $extra, "");

        //一覧
        $counter = 0;
        $over_grd_credit = $sep = "";
        if ($model->grade_hr_class != "") {
            $query = knjd294Query::getDataList($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //卒業予定
                if (isset($model->warning)) {
                    if (get_count($model->data_chk) > 0) {
                        $extra = (in_array($row["SCHREGNO"], $model->data_chk)) ? "checked" : "";
                    } else {
                        $extra =  "";
                    }
                } else {
                    $extra = ($row["GRAD_YOTEI"] == "1") ? "checked" : "";
                }
                $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $row["SCHREGNO"], $extra, "1");

                //在籍期間
                $month_to = date("Y", strtotime(CTRL_DATE)) * 12 + date("m", strtotime(CTRL_DATE));
                $month_from = date("Y", strtotime($row["ENT_DATE"])) * 12 + date("m", strtotime($row["ENT_DATE"]));
                $row["REGD_MONTHS"] = $month_to - $month_from + $row["ANOTHER_MONTHS"];

                //卒業単位数以上のとき、背景色を変える
                $row["BGCOLOR"] = ($row["OVER_GRAD_CREDIT"] == "1") ? "#ccffff" : "#ffffff";

                $arg["data"][] = $row;
                $counter++;
            }
            $result->free();
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "DATA_CNT", $counter);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjd294Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array ("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
