<?php

require_once('for_php7.php');

class knjd452Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //$arg["Read"] = "start();";

        //フォーム作成
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjd452index.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();

        //年度・学期コンボ
        //makeCmb($objForm, $arg, $db, $query, $model->term, "TERM", $extra, 1);

        //左クラスコンボ
        $query = knjd452Query::getGrade($model);
        $extra = "onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1);

        //右クラスコンボ
        $query = knjd452Query::getHrClass($model);
        $extra = "onchange=\"btn_submit('change_hr_class');\"";
        makeCmb($objForm, $arg, $db, $query, $model->hr_class, "HR_CLASS", $extra, 1);

        //生徒リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd452Form1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();

    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//生徒リストToリスト作成
function makeStudentList(&$objForm, &$arg, $db, $model) {
    //左生徒リスト(溜める式)
    $selectdata      = ($model->selectdata != "")       ? explode(",", $model->selectdata)      : array();
    $selectdataLabel = ($model->selectdataLabel != "")  ? explode(",", $model->selectdataLabel) : array();

    //左生徒リスト
    $cnt = 0;
    $opt_left = array();
    if ($model->cmd == 'change_hr_class' ) {
        for ($i = 0; $i < get_count($selectdata); $i++) {
            $opt_left[] = array("label" => $selectdataLabel[$i],
                                "value" => $selectdata[$i]);
            $cnt++;
        }
    } else {
        $result = $db->query(knjd452Query::getGhrStudents($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[] = array("label" => $row["HR_NAME"].$row["ATTENDNO"]."番　".$row["SCHREGNO"]."　".$row["NAME"],
                                "value" => $row["SCHREGNO"]);
            $cnt++;
        }
        $result->free();
    }
    $arg["RIGHT_NUM"] = $cnt;

    //右生徒リスト
    $cnt = 0;
    $opt_right = array();
    $result = $db->query(knjd452Query::getHrStudents($model));
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_right[]= array("label" => $row["HR_NAME"].$row["ATTENDNO"]."番　".$row["SCHREGNO"]."　".$row["NAME"],
                            "value" => $row["SCHREGNO"]);
        $cnt++;
    }
    $result->free();
    $arg["LEFT_NUM"] = $cnt;

    //生徒一覧リスト(右)
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"moveStudent('left')\"";
    $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "RIGHT_PART", "", $opt_right, $extra, 35);

    //対象者一覧リスト(左)
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"moveStudent('right')\"";
    $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "LEFT_PART", "", $opt_left, $extra, 35);

    //対象選択ボタン
    $extra = "onclick=\"return moveStudent('sel_add_all');\"";
    $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
    //対象選択ボタン
    $extra = "onclick=\"return moveStudent('left');\"";
    $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
    //対象取消ボタン
    $extra = "onclick=\"return moveStudent('right');\"";
    $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    //対象取消ボタン
    $extra = "onclick=\"return moveStudent('sel_del_all');\"";
    $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消
    $extra = "onclick=\"closecheck();return btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //終了
    $extra = "onclick=\"return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdataLabel");
}
?>
