<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd453Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjd453index.php", "", "edit");
        $db = Query::dbCheckOut();

        if (!isset($model->warning)) {
            $query = knjd453Query::selectQuery($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row = $model->field;
        }

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;
        
        /******************/
        /* リストtoリスト */
        /******************/
        makeListToList($objForm, $arg, $db, $model, $disabled);
        
        /**********/
        /* ボタン */
        /**********/
        //更新ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消しボタンを作成する
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        
        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "PROGRAMID", PROGRAMID);
        knjCreateHidden($objForm, "selectdata");

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd453Form1.html", $arg);
    }
}
//科目一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $disabled) {
    $opt_left = $opt_right = array();
    if (isset($model->schregno)) {
        $result      = $db->query(knjd453Query::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["SET_DATA"]) {
                $opt_left[]  = array("label" => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"], 
                                     "value" => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]);
            } else {
                $opt_right[]  = array("label" => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"], 
                                     "value" => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]);
            }
        }
        $result->free();
    }

    //タイトル設定
    $arg["info"]["LEFT_LIST"] =  '指導計画対象科目一覧';
    $arg["info"]["RIGHT_LIST"] =  '履修科目一覧';

    //更新科目一覧
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right')\"";
    $arg["main_part"]["LEFT_PART"]   = knjCreateCombo($objForm, "classyear", "right", $opt_left, $extra, 20);
    //対象科目一覧
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"";
    $arg["main_part"]["RIGHT_PART"]  = knjCreateCombo($objForm, "classmaster", "left", $opt_right, $extra, 20);
    //各種ボタン
    $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", "onclick=\"return moves('sel_add_all');\"");
    $arg["main_part"]["SEL_ADD"]     = knjCreateBtn($objForm, "sel_add", "＜", "onclick=\"return move('left');\"");
    $arg["main_part"]["SEL_DEL"]     = knjCreateBtn($objForm, "sel_del", "＞", "onclick=\"return move('right');\"");
    $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", "onclick=\"return moves('sel_del_all');\"");
}

?>
